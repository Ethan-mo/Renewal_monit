package goodmonit.monit.com.kao.provider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import goodmonit.monit.com.kao.activity.SettingWidgetActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.WidgetManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class WidgetProvider extends AppWidgetProvider {
    private static final String TAG = Configuration.BASE_TAG + "WidgetProvider";
    private static final boolean DBG = Configuration.DBG;

    protected static Context mContext;
    protected ConnectionManager mConnectionMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        mContext = context;

        final String action = intent.getAction();
        if (DBG) Log.d(TAG, "onReceive: " + intent.getAction());
        Bundle extras = intent.getExtras();
        int[] appWidgetIdsTemp = null;
        if (extras != null) {
            appWidgetIdsTemp = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        }
        final int[] appWidgetIds = appWidgetIdsTemp;

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            _fullSyncFromServer(context, appWidgetIds);

        } else if (WidgetManager.BROADCAST_MESSAGE_APPWIDGET_UPDATE_REFRESH.equals(action)) {
            final int widgetId = intent.getIntExtra(WidgetManager.APP_WIDGET_ID, -1);
            final int widgetSize = intent.getIntExtra(WidgetManager.APP_WIDGET_SIZE, WidgetManager.DEFAULT_APP_WIDGET_SIZE);
            if (DBG) Log.d(TAG, "onRefresh: " + widgetId + " / " + widgetSize);
            int[] appWidgetId = new int[1];
            appWidgetId[0] = widgetId;

            // Refreshing 되는 중
            WidgetManager widgetMgr = new WidgetManager(context);
            RemoteViews views = widgetMgr.buildViews(widgetId, widgetSize, true);
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
            _fullSyncFromServer(context, appWidgetId);

        } else if (WidgetManager.BROADCAST_MESSAGE_APPWIDGET_SETTING.equals(action)) {
            final int widgetId = intent.getIntExtra(WidgetManager.APP_WIDGET_ID, -1);
            final int widgetSize = intent.getIntExtra(WidgetManager.APP_WIDGET_SIZE, WidgetManager.DEFAULT_APP_WIDGET_SIZE);
            if (DBG) Log.d(TAG, "onSetting: " + widgetId + " / " + widgetSize);
            final Intent intentSetting = new Intent(mContext, SettingWidgetActivity.class);
            intentSetting.putExtra(WidgetManager.APP_WIDGET_ID, widgetId);
            intentSetting.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
            intentSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentSetting);

        } else if (WidgetManager.BROADCAST_MESSAGE_APPWIDGET_CHANGE_DIAPER.equals(action)) {
            final long targetDeviceId = intent.getLongExtra("targetDeviceId", -1);
            if (DBG) Log.d(TAG, "onChangeDiaper: " + targetDeviceId);
            for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
                if (sensor == null) continue;
                if (sensor.deviceId == targetDeviceId) {
                    sensor.setDiaperChanged(System.currentTimeMillis());
                }
            }
            final int widgetId = intent.getIntExtra(WidgetManager.APP_WIDGET_ID, -1);
            int[] appWidgetId = new int[1];
            appWidgetId[0] = widgetId;
            onUpdate(context, AppWidgetManager.getInstance(context), appWidgetId);

        } else if (WidgetManager.BROADCAST_MESSAGE_APPWIDGET_BRIGHTNESS.equals(action)) {
            final int targetDeviceType = intent.getIntExtra("targetDeviceType", -1);
            final long targetDeviceId = intent.getLongExtra("targetDeviceId", -1);

            int power = -1;
            if (targetDeviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
                DeviceAQMHub hub = ConnectionManager.getDeviceAQMHub(targetDeviceId);
                if (hub != null) {
                    power = hub.getLampPower();
                    if (power == DeviceStatus.LAMP_POWER_OFF) {
                        power = DeviceStatus.LAMP_POWER_ON;
                    } else {
                        power = DeviceStatus.LAMP_POWER_OFF;
                    }
                    ConnectionManager.getInstance().updateDeviceLampPower(DeviceType.AIR_QUALITY_MONITORING_HUB, targetDeviceId, power);
                }
            } else if (targetDeviceType == DeviceType.LAMP) {
                DeviceLamp lamp = ConnectionManager.getDeviceLamp(targetDeviceId);
                if (lamp != null) {
                    power = lamp.getLampPower();
                    if (power == DeviceStatus.LAMP_POWER_OFF) {
                        power = DeviceStatus.LAMP_POWER_ON;
                    } else {
                        power = DeviceStatus.LAMP_POWER_OFF;
                    }
                    ConnectionManager.getInstance().updateDeviceLampPower(DeviceType.LAMP, targetDeviceId, power);
                }
            }

            if (DBG) Log.d(TAG, "onChangeLampPower: " + targetDeviceType + " / " + targetDeviceId + " / " + power);
            final int widgetId = intent.getIntExtra(WidgetManager.APP_WIDGET_ID, -1);
            int[] appWidgetId = new int[1];
            appWidgetId[0] = widgetId;
            _fullSyncFromServer(context, appWidgetId);
        }
    }

    private void _fullSyncFromServer(Context context, final int[] updateWidgetIds) {
        mContext = context;
        mConnectionMgr = ConnectionManager.getInstance();
        if (mConnectionMgr != null) {
            mConnectionMgr.getUserInfoFromCloud();
            mConnectionMgr.reconnectBleDevice();
            mConnectionMgr.updateDeviceFullStatusFromCloud(new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errCode, String data) {
                    mHandler.obtainMessage(WidgetManager.MSG_FULL_SYNC_FINISHED, updateWidgetIds).sendToTarget();
                }
            });
        } else {
            if (DBG) Log.e(TAG, "Service NULL");
            Message msg = mHandler.obtainMessage(WidgetManager.MSG_CONNECTION_MANAGER_STARTED, updateWidgetIds);
            mHandler.sendMessageDelayed(msg, 3 * 1000);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int cntWidgetId = appWidgetIds.length;
        WidgetManager widgetMgr = new WidgetManager(context);
        for(int i = 0; i < cntWidgetId; i++) {
            int appWidgetId = appWidgetIds[i];
            if (DBG) Log.d(TAG, "onUpdate: "+ appWidgetId);
            RemoteViews views = widgetMgr.buildViews(appWidgetId, 1, false);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        if (DBG) Log.d(TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (DBG) Log.d(TAG, "onDisabled");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        WidgetManager widgetMgr = new WidgetManager(context);
        widgetMgr.deleteWidget(appWidgetIds);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int[] appWidgetIds;
            switch(msg.what) {
                case WidgetManager.MSG_FULL_SYNC_FINISHED:
                    appWidgetIds = (int[])msg.obj;
                    if (appWidgetIds != null && appWidgetIds.length > 0) {
                        if (DBG) Log.d(TAG, "MSG_FULL_SYNC_FINISHED: " + appWidgetIds.length);
                        onUpdate(mContext, AppWidgetManager.getInstance(mContext), appWidgetIds);
                    } else {
                        if (DBG) Log.e(TAG, "MSG_FULL_SYNC_FINISHED: NULL");
                    }
                    break;
                case WidgetManager.MSG_CONNECTION_MANAGER_STARTED:
                    appWidgetIds = (int[])msg.obj;
                    if (appWidgetIds != null && appWidgetIds.length > 0) {
                        if (DBG) Log.d(TAG, "MSG_CONNECTION_MANAGER_STARTED: " + appWidgetIds.length);
                        _fullSyncFromServer(mContext, appWidgetIds);
                    } else {
                        if (DBG) Log.e(TAG, "MSG_CONNECTION_MANAGER_STARTED: NULL");
                    }
                    break;
                case WidgetManager.MSG_NEED_TO_SELECT_DEVICE:
                    int appWidgetId = msg.arg1;
                    if (DBG) Log.d(TAG, "MSG_NEED_TO_SELECT_DEVICE: " + appWidgetId);
                    Intent intentSetting = new Intent(mContext, SettingWidgetActivity.class);
                    intentSetting.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
                    mContext.startActivity(intentSetting);
                    break;
                case WidgetManager.MSG_FULL_BRIGHTNESS_CHANGED:
                    appWidgetIds = (int[])msg.obj;
                    if (appWidgetIds != null && appWidgetIds.length > 0) {
                        if (DBG) Log.d(TAG, "MSG_FULL_BRIGHTNESS_CHANGED: " + appWidgetIds.length);
                        onUpdate(mContext, AppWidgetManager.getInstance(mContext), appWidgetIds);
                    } else {
                        if (DBG) Log.e(TAG, "MSG_FULL_BRIGHTNESS_CHANGED: NULL");
                    }
                    break;
            }
        }
    };
}
