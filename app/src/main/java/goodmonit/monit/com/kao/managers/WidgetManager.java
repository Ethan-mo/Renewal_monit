package goodmonit.monit.com.kao.managers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.SplashActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.provider.WidgetProvider2x1;
import goodmonit.monit.com.kao.provider.WidgetProvider2x2;
import goodmonit.monit.com.kao.provider.WidgetProvider2x3;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.UnitConvertUtil;

public class WidgetManager {
	private static final String TAG = Configuration.BASE_TAG + "WidgetMgr";
	private static final boolean DBG = Configuration.DBG;

	public static final String BROADCAST_MESSAGE_APPWIDGET_UPDATE_REFRESH   = "monit.com.monit.appwidget.refresh";
	public static final String BROADCAST_MESSAGE_APPWIDGET_CHANGE_DIAPER    = "monit.com.monit.appwidget.changediaper";
    public static final String BROADCAST_MESSAGE_APPWIDGET_SETTING          = "monit.com.monit.appwidget.setting";
	public static final String BROADCAST_MESSAGE_APPWIDGET_BRIGHTNESS       = "monit.com.monit.appwidget.brightness";

	public static final String APP_WIDGET_ID = "appWidgetId";
	public static final String APP_WIDGET_SIZE = "widgetSize";

	public static final int MSG_FULL_SYNC_FINISHED         = 1;
	public static final int MSG_CONNECTION_MANAGER_STARTED = 2;
	public static final int MSG_NEED_TO_SELECT_DEVICE      = 3;
	public static final int MSG_FULL_BRIGHTNESS_CHANGED    = 4;

	public static final int MAX_SELECT_DEVICE				= 4;

	public static final int DEFAULT_WIDGET_DEVICE_INFO		= -1;
	public static final int DEFAULT_APP_WIDGET_SIZE			= 3;

	private Context mContext;
	private PreferenceManager mPreferenceMgr;
	private ConnectionManager mConnectionMgr;

	public WidgetManager(Context context) {
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
	}

	public void deleteWidget(int[] appWidgetIds) {
		for(int i = 0; i < appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];
			if (DBG) Log.d(TAG, "onDeleted: "+ appWidgetId);

			// 업데이트 시간 초기화
			// mPreferenceMgr.setWidgetRefreshPeriodMin(appWidgetId, 0);

			// 최대 4개까지 위젯에 포함시킬 수 있음
			for (int j = 1; j <= MAX_SELECT_DEVICE; j++) {
				mPreferenceMgr.setWidgetShowDeviceInfo(appWidgetId, j, DEFAULT_WIDGET_DEVICE_INFO);
			}
		}
	}

	public RemoteViews buildViews(final int appWidgetId, final int widgetSize, final boolean onRefreshing) {
		if (DBG) Log.d(TAG, "_buildViews: " + appWidgetId + " / " + widgetSize + " / " + onRefreshing);

		RemoteViews updateViews = null;

		// 앱 실행
		Intent intentLaunch = new Intent(mContext, SplashActivity.class);
		PendingIntent pendingIntentLaunch = PendingIntent.getActivity(mContext, 0, intentLaunch, PendingIntent.FLAG_UPDATE_CURRENT);

		// 업데이트 시간
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.getDefault());

		// 새로고침 버튼
		Intent intentSync = null;
		PendingIntent pendingIntentSync = null;

		// 설정버튼
        Intent intentSetting = null;
        PendingIntent pendingIntentSetting = null;

		switch(widgetSize) {
			case 1:
				updateViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_2x1v2);
				updateViews.setOnClickPendingIntent(R.id.lctn_app_widget1_status_dashboard, pendingIntentLaunch);
				updateViews.setOnClickPendingIntent(R.id.lctn_app_widget1_status_disconnected, pendingIntentLaunch);

				// 업데이트 시간 설정
				updateViews.setTextViewText(R.id.btn_app_widget_updated_time, sdf.format(calendar.getTime()));

				Uri appWidgetUri = Uri.parse("" + appWidgetId);
				// 새로고침 버튼 설정
				intentSync = new Intent(mContext, WidgetProvider2x1.class);
				intentSync.setAction(BROADCAST_MESSAGE_APPWIDGET_UPDATE_REFRESH);
				intentSync.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
				intentSync.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
				intentSync.setData(appWidgetUri);
                pendingIntentSync = PendingIntent.getBroadcast(mContext, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.btn_app_widget_refresh, pendingIntentSync);
				if (onRefreshing) {
					updateViews.setViewVisibility(R.id.btn_app_widget_refresh, View.GONE);
					updateViews.setViewVisibility(R.id.pb_app_widget_refresh, View.VISIBLE);
				} else {
					updateViews.setViewVisibility(R.id.btn_app_widget_refresh, View.VISIBLE);
					updateViews.setViewVisibility(R.id.pb_app_widget_refresh, View.GONE);
				}

				// 설정버튼 설정
                intentSetting = new Intent(mContext, WidgetProvider2x1.class);
                intentSetting.setAction(BROADCAST_MESSAGE_APPWIDGET_SETTING);
                intentSetting.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
                intentSetting.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
				intentSetting.setData(appWidgetUri);
                pendingIntentSetting = PendingIntent.getBroadcast(mContext, 0, intentSetting, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.btn_app_widget_setting, pendingIntentSetting);
				break;

			case 2:
				updateViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_2x2);
				//updateViews.setOnClickPendingIntent(R.id.lctn_app_widget, pendingIntentLaunch);

				// 업데이트 시간 설정
				updateViews.setTextViewText(R.id.btn_app_widget_updated_time_2x2, sdf.format(calendar.getTime()));

				// 새로고침 버튼 설정
				intentSync = new Intent(mContext, WidgetProvider2x2.class);
				intentSync.setAction(BROADCAST_MESSAGE_APPWIDGET_UPDATE_REFRESH);
				intentSync.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
                intentSync.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
                pendingIntentSync = PendingIntent.getBroadcast(mContext, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.btn_app_widget_refresh_2x2, pendingIntentSync);
				if (onRefreshing) {
					updateViews.setViewVisibility(R.id.btn_app_widget_refresh_2x2, View.GONE);
					updateViews.setViewVisibility(R.id.pb_app_widget_refresh_2x2, View.VISIBLE);
				} else {
					updateViews.setViewVisibility(R.id.btn_app_widget_refresh_2x2, View.VISIBLE);
					updateViews.setViewVisibility(R.id.pb_app_widget_refresh_2x2, View.GONE);
				}

                // 설정버튼 설정
                intentSetting = new Intent(mContext, WidgetProvider2x2.class);
                intentSetting.setAction(BROADCAST_MESSAGE_APPWIDGET_SETTING);
                intentSetting.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
                intentSetting.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
                pendingIntentSetting = PendingIntent.getBroadcast(mContext, 0, intentSetting, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.btn_app_widget_setting_2x2, pendingIntentSetting);
				break;

			case 3:
			    //Through
            default:
				updateViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_2x3);
				updateViews.setOnClickPendingIntent(R.id.lctn_app_widget, pendingIntentLaunch);

				// 업데이트 시간 설정
				updateViews.setTextViewText(R.id.btn_app_widget_updated_time_2x3, sdf.format(calendar.getTime()));

				// 새로고침 버튼 설정
				intentSync = new Intent(mContext, WidgetProvider2x3.class);
				intentSync.setAction(BROADCAST_MESSAGE_APPWIDGET_UPDATE_REFRESH);
				intentSync.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
                intentSync.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
                pendingIntentSync = PendingIntent.getBroadcast(mContext, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.btn_app_widget_refresh_2x3, pendingIntentSync);
				if (onRefreshing) {
					updateViews.setViewVisibility(R.id.btn_app_widget_refresh_2x3, View.GONE);
					updateViews.setViewVisibility(R.id.pb_app_widget_refresh_2x3, View.VISIBLE);
				} else {
					updateViews.setViewVisibility(R.id.btn_app_widget_refresh_2x3, View.VISIBLE);
					updateViews.setViewVisibility(R.id.pb_app_widget_refresh_2x3, View.GONE);
				}

                // 설정버튼 설정
                intentSetting = new Intent(mContext, WidgetProvider2x3.class);
                intentSetting.setAction(BROADCAST_MESSAGE_APPWIDGET_SETTING);
                intentSetting.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
                intentSetting.putExtra(WidgetManager.APP_WIDGET_SIZE, widgetSize);
                pendingIntentSetting = PendingIntent.getBroadcast(mContext, 0, intentSetting, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.btn_app_widget_setting_2x3, pendingIntentSetting);
				break;
		}

		// 선택된 기기목록 가져오기
		ArrayList<Long> arrDeviceInfo = new ArrayList<>();
		for (int i = 1 ; i <= widgetSize; i++) {
			arrDeviceInfo.add(mPreferenceMgr.getWidgetShowDeviceInfo(appWidgetId, i));
		}

		// 선택된 기기목록이 없는 최초 등록이라면, 기존 기기목록에서 랜덤으로 등록함
		int index = 1;
		int cntNotSelected = 0;
		for (long deviceInfo : arrDeviceInfo) {
			if (DBG) Log.d(TAG, "getWidgetShowDeviceInfo[" + appWidgetId + "," + index + "]: " + deviceInfo);
			index++;
			if (deviceInfo == DEFAULT_WIDGET_DEVICE_INFO) {
				cntNotSelected++;
			}
		}
		if (cntNotSelected >= widgetSize) {
			index = 1;
			for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
				if (sensor == null) continue;
				long deviceInfo = sensor.deviceId * 10 + DeviceType.DIAPER_SENSOR;
				if (index <= widgetSize) {
					mPreferenceMgr.setWidgetShowDeviceInfo(appWidgetId, index, deviceInfo);
					index++;
				}
			}

			for (DeviceAQMHub hub : ConnectionManager.mRegisteredAQMHubList.values()) {
				if (hub == null) continue;
				long deviceInfo = hub.deviceId * 10 + DeviceType.AIR_QUALITY_MONITORING_HUB;
				if (index <= widgetSize) {
					mPreferenceMgr.setWidgetShowDeviceInfo(appWidgetId, index, deviceInfo);
					index++;
				}
			}

			for (DeviceLamp lamp : ConnectionManager.mRegisteredLampList.values()) {
				if (lamp == null) continue;
				long deviceInfo = lamp.deviceId * 10 + DeviceType.LAMP;
				if (index <= widgetSize) {
					mPreferenceMgr.setWidgetShowDeviceInfo(appWidgetId, index, deviceInfo);
					index++;
				}
			}

			for (int i = 1; i <= widgetSize; i++) {
				arrDeviceInfo.add(mPreferenceMgr.getWidgetShowDeviceInfo(appWidgetId, i));
			}
		}

		int showIndex = 1;
		for (long deviceInfo : arrDeviceInfo) {
			if (deviceInfo == DEFAULT_WIDGET_DEVICE_INFO) continue;

			if (showIndex == 1) {
				if (_updateDeviceInfo(mContext,
						appWidgetId,
						widgetSize,
						updateViews,
						showIndex,
						deviceInfo) == true) {
					showIndex++;
				}
			} else if (showIndex == 2) {
				if (_updateDeviceInfo(mContext,
						appWidgetId,
						widgetSize,
						updateViews,
						showIndex,
						deviceInfo) == true) {
					showIndex++;
				}
			} else if (showIndex == 3) {
				if (_updateDeviceInfo(mContext,
						appWidgetId,
						widgetSize,
						updateViews,
						showIndex,
						deviceInfo) == true) {
					showIndex++;
				}
			}
		}

		// 기저귀 센서 설정
		//_updateDiaperSensorInfo(context, updateViews, appWidgetId);

		// 허브 설정
		//_updateAQMHubInfo(context, updateViews, appWidgetId);

		// 전체 클릭시 앱실행
//        Intent intent = new Intent(context, MainLightActivity.class);
//        intent.putExtra("mode", 0);
//        intent.putExtra("deviceType", 0);
//        intent.putExtra("deviceId", 0);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        updateViews.setOnClickPendingIntent(R.id.lctn_app_widget, pendingIntent);

		return updateViews;
	}

	private boolean _updateDeviceInfo(Context context,
									  int appWidgetId,
									  int widgetSize,
									  RemoteViews updateViews,
									  int showIndex,
									  long deviceInfo) {

		if (DBG) Log.d(TAG, "_updateDeviceInfo: " + appWidgetId + " / " + widgetSize + " / " + deviceInfo);

		for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
			if (sensor == null) continue;
			long sensorDeviceInfo = sensor.deviceId * 10 + DeviceType.DIAPER_SENSOR;
			if (sensorDeviceInfo != deviceInfo) continue;

			return _updateDiaperSensorInfo(context,
					appWidgetId,
					widgetSize,
					updateViews,
					showIndex,
					sensor);
		}

		for (DeviceAQMHub hub : ConnectionManager.mRegisteredAQMHubList.values()) {
			if (hub == null) continue;
			long hubDeviceInfo = hub.deviceId * 10 + DeviceType.AIR_QUALITY_MONITORING_HUB;
			if (hubDeviceInfo != deviceInfo) continue;

			return _updateAQMHubInfo(context,
					appWidgetId,
					widgetSize,
					updateViews,
					showIndex,
					hub);
		}

		for (DeviceLamp lamp : ConnectionManager.mRegisteredLampList.values()) {
			if (lamp == null) continue;
			long lampDeviceInfo = lamp.deviceId * 10 + DeviceType.LAMP;
			if (lampDeviceInfo != deviceInfo) continue;

			return _updateLampInfo(context,
					appWidgetId,
					widgetSize,
					updateViews,
					showIndex,
					lamp);
		}
		return false;
	}

	private boolean _updateDiaperSensorInfo(Context context,
											 int appWidgetId,
											 int widgetSize,
											RemoteViews updateViews,
											int showIndex,
											 final DeviceDiaperSensor sensor) {

		if (DBG) Log.d(TAG, "_updateDiaperSensorInfo: " + appWidgetId + " / " + widgetSize + " / " + sensor.getName() + "(" + sensor.deviceId + ")");

		final long deviceId = sensor.deviceId;
		final String deviceName = sensor.getName();

		int resDeviceName = R.id.tv_app_widget1_device_name;
		int resDisconnectedContainer = R.id.lctn_app_widget1_status_disconnected;
		int resDisconnectedTitle = R.id.tv_app_widget1_status_disconnected_title;
		int resDisconnectedContents = R.id.tv_app_widget1_status_disconnected_contents;

		int resStatusContainer1 = R.id.lctn_app_widget1_status1;
		int resStatusContainer2 = R.id.lctn_app_widget1_status2;
		int resStatusContainer3 = R.id.lctn_app_widget1_status3;

		int resIvStatusIcon1 = R.id.iv_app_widget1_status1;
		int resIvStatusIcon2 = R.id.iv_app_widget1_status2;
		int resIvStatusIcon3 = R.id.iv_app_widget1_status3;

		int resStatusTextTitle1 = R.id.tv_app_widget1_status1_title;
		int resStatusTextTitle2 = R.id.tv_app_widget1_status2_title;
		int resStatusTextTitle3 = R.id.tv_app_widget1_status3_title;

		int resStatusTextContents1 = R.id.tv_app_widget1_status1_contents;
		int resStatusTextContents2 = R.id.tv_app_widget1_status2_contents;
		int resStatusTextContents3 = R.id.tv_app_widget1_status3_contents;

		if (widgetSize == 2) {

		}


		if (showIndex == 1) {
			// 센서 이름
			updateViews.setTextViewText(resDeviceName, deviceName);
			updateViews.setViewVisibility(R.id.rctn_app_widget1_lamp_onoff, View.GONE);

			// 연결상태
			boolean isConnected = sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED;
			if (isConnected) {
				updateViews.setViewVisibility(resDisconnectedContainer, View.GONE);
				updateViews.setViewVisibility(R.id.rctn_app_widget1_sensor_description, View.VISIBLE);

				// 기저귀 상태
				int diaperScore = sensor.getDiaperScore();
				updateViews.setTextViewText(resStatusTextTitle1, context.getString(R.string.device_sensor_diaper_status_title));
				updateViews.setTextViewText(resStatusTextContents1, context.getString(DeviceStatus.getDiaperScoreStringResource(diaperScore)));
				updateViews.setImageViewResource(resIvStatusIcon1, DeviceStatus.getDiaperScoreWidgetResource(diaperScore));

				// 가스량
				float vocValue = sensor.getVocAvg();
				updateViews.setTextViewText(resStatusTextTitle2, context.getString(R.string.device_sensor_voc_status));
				updateViews.setTextViewText(resStatusTextContents2, context.getString(DeviceStatus.getDiaperSensorVocStringResource(vocValue)));
				updateViews.setImageViewResource(resIvStatusIcon2, DeviceStatus.getDiaperSensorVocWidgetResource(vocValue));

				// 움직임
				int movement = sensor.getMovementStatus();
				boolean isSleep = mPreferenceMgr.getSleepingEnabled(sensor.deviceId);
				updateViews.setTextViewText(resStatusTextTitle3, context.getString(R.string.device_sensor_movement));
				updateViews.setImageViewResource(resIvStatusIcon3, R.drawable.bg_btn_oval_movement);
				if (isSleep) {
					updateViews.setTextViewText(resStatusTextContents3, context.getString(DeviceStatus.getMovementStringResource(DeviceStatus.MOVEMENT_SLEEP)));
				} else {
					updateViews.setTextViewText(resStatusTextContents3, context.getString(DeviceStatus.getMovementStringResource(movement)));
				}

				// 배터리 상태
				int batteryPower = sensor.getBatteryPower();
				updateViews.setTextViewText(R.id.tv_app_widget1_sensor_description, batteryPower + "%");
			} else {
				updateViews.setViewVisibility(resDisconnectedContainer, View.VISIBLE);
				updateViews.setViewVisibility(R.id.rctn_app_widget1_sensor_description, View.GONE);
				updateViews.setTextViewText(resDisconnectedTitle, context.getString(R.string.device_sensor_disconnected_title));
				updateViews.setTextViewText(resDisconnectedContents, context.getString(R.string.device_sensor_disconnected_detail));
			/*
			// 센서 아이콘
			updateViews.setImageViewResource(resDeviceIcon, R.drawable.ic_device_sensor_disconnected);

			// 센서 상태
			updateViews.setImageViewResource(resStatusIcon1, R.drawable.ic_sensor_operation_deactivated);
			updateViews.setTextViewText(resStatusText1, "");
			updateViews.setTextColor(resStatusText1, context.getResources().getColor(R.color.colorTextPrimary));

			// 기저귀 상태
			updateViews.setImageViewResource(resStatusIcon2, R.drawable.ic_sensor_diaper_deactivated);
			updateViews.setTextViewText(resStatusText2, context.getString(R.string.setting_ap_info_not_connected));
			updateViews.setTextColor(resStatusText2, context.getResources().getColor(R.color.colorTextPrimary));

			// 배터리 상태
			updateViews.setImageViewResource(resStatusIcon3, R.drawable.ic_sensor_diaper_battery_deactivated);
			updateViews.setTextViewText(resStatusText3, "");
			updateViews.setTextColor(resStatusText3, context.getResources().getColor(R.color.colorTextPrimary));
			*/
			}
		}

		return true;
		/*
		// 기저귀교체 버튼
		Intent intentChangeDiaper = new Intent(context, WidgetExternalProvider.class);
		intentChangeDiaper.setAction(BROADCAST_MESSAGE_APPWIDGET_CHANGE_DIAPER);
		intentChangeDiaper.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
		intentChangeDiaper.putExtra("targetDeviceId", sensor.deviceId);
		PendingIntent pendingChangeDiaper = PendingIntent.getBroadcast(context, 0, intentChangeDiaper, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.iv_widget_external_diaper_refresh, pendingChangeDiaper);
		*/
	}

	private boolean _updateAQMHubInfo(Context context,
									    int appWidgetId,
									  	int widgetSize,
										RemoteViews updateViews,
										int showIndex,
										final DeviceAQMHub hub) {

		if (DBG) Log.d(TAG, "_updateAQMHubInfo: " + appWidgetId + " / " + widgetSize + " / " + hub.getName() + "(" + hub.deviceId + ")");
		final long deviceId = hub.deviceId;

		int resDeviceName = R.id.tv_app_widget1_device_name;
		int resDisconnectedContainer = R.id.lctn_app_widget1_status_disconnected;
		int resDisconnectedTitle = R.id.tv_app_widget1_status_disconnected_title;
		int resDisconnectedContents = R.id.tv_app_widget1_status_disconnected_contents;

		int resStatusContainer1 = R.id.lctn_app_widget1_status1;
		int resStatusContainer2 = R.id.lctn_app_widget1_status2;
		int resStatusContainer3 = R.id.lctn_app_widget1_status3;

		int resIvStatusIcon1 = R.id.iv_app_widget1_status1;
		int resIvStatusIcon2 = R.id.iv_app_widget1_status2;
		int resIvStatusIcon3 = R.id.iv_app_widget1_status3;

		int resStatusTextTitle1 = R.id.tv_app_widget1_status1_title;
		int resStatusTextTitle2 = R.id.tv_app_widget1_status2_title;
		int resStatusTextTitle3 = R.id.tv_app_widget1_status3_title;

		int resStatusTextContents1 = R.id.tv_app_widget1_status1_contents;
		int resStatusTextContents2 = R.id.tv_app_widget1_status2_contents;
		int resStatusTextContents3 = R.id.tv_app_widget1_status3_contents;

		if (widgetSize == 2) {

		}

		if (showIndex == 1) {

			// 허브 이름
			updateViews.setTextViewText(resDeviceName, hub.getName());
			updateViews.setViewVisibility(R.id.rctn_app_widget1_sensor_description, View.GONE);

			// 연결상태
			boolean isConnected = hub.getConnectionState() != DeviceConnectionState.DISCONNECTED;
			if (isConnected) {
				updateViews.setViewVisibility(resDisconnectedContainer, View.GONE);

				updateViews.setViewVisibility(R.id.rctn_app_widget1_lamp_onoff, View.VISIBLE);

				Intent intentLampPower = new Intent(context, WidgetProvider2x1.class);
				intentLampPower.setAction(BROADCAST_MESSAGE_APPWIDGET_BRIGHTNESS);
				intentLampPower.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
				intentLampPower.putExtra("targetDeviceType", DeviceType.AIR_QUALITY_MONITORING_HUB);
				intentLampPower.putExtra("targetDeviceId", hub.deviceId);
				Uri appWidgetUri = Uri.parse("" + appWidgetId);
				intentLampPower.setData(appWidgetUri);
				PendingIntent pendingLampPower = PendingIntent.getBroadcast(context, 0, intentLampPower, PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.btn_app_widget1_lamp_onoff, pendingLampPower);

				// 온도설정
				float temperatureValue = hub.getTemperature();
				String temperatureScale = mPreferenceMgr.getTemperatureScale();
				if (temperatureScale != null && temperatureScale.equals(context.getString(R.string.unit_temperature_fahrenheit))) {
					temperatureValue = UnitConvertUtil.getFahrenheitFromCelsius(hub.getTemperature());
				}
				updateViews.setTextViewText(resStatusTextTitle1, context.getString(R.string.device_environment_temperature));
				updateViews.setTextViewText(resStatusTextContents1, temperatureValue + temperatureScale);

				switch (hub.getTemperatureStatus()) {
					case EnvironmentCheckManager.HIGH:
						updateViews.setImageViewResource(resIvStatusIcon1, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents1, context.getResources().getColor(R.color.colorTextWarning));

						break;
					case EnvironmentCheckManager.LOW:
						updateViews.setImageViewResource(resIvStatusIcon1, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents1, context.getResources().getColor(R.color.colorTextWarning));
						break;
					case EnvironmentCheckManager.NORMAL:
					default:
						updateViews.setImageViewResource(resIvStatusIcon1, R.drawable.bg_btn_oval_airquality_normal);
						updateViews.setTextColor(resStatusTextContents1, context.getResources().getColor(R.color.colorTextPrimary));
						break;
				}

				// 습도설정
				float humidityValue = hub.getHumidity();
				updateViews.setTextViewText(resStatusTextTitle2, context.getString(R.string.device_environment_humidity));
				updateViews.setTextViewText(resStatusTextContents2, humidityValue + "%");

				switch (hub.getHumidityStatus()) {
					case EnvironmentCheckManager.HIGH:
						updateViews.setImageViewResource(resIvStatusIcon2, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents2, context.getResources().getColor(R.color.colorTextWarning));
						break;
					case EnvironmentCheckManager.LOW:
						updateViews.setImageViewResource(resIvStatusIcon2, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents2, context.getResources().getColor(R.color.colorTextWarning));
						break;
					case EnvironmentCheckManager.NORMAL:
					default:
						updateViews.setImageViewResource(resIvStatusIcon2, R.drawable.bg_btn_oval_airquality_normal);
						updateViews.setTextColor(resStatusTextContents2, context.getResources().getColor(R.color.colorTextPrimary));
						break;
				}

				// VOC 상태
				if (hub.getSensorAttached() == 0) {
					updateViews.setViewVisibility(resStatusContainer3, View.GONE);
				} else {
					updateViews.setViewVisibility(resStatusContainer3, View.VISIBLE);
					float vocValue = hub.getVoc();
					EnvironmentCheckManager environmentCheckMgr = new EnvironmentCheckManager(context);
					String vocString = environmentCheckMgr.getVocString(vocValue);
					updateViews.setTextViewText(resStatusTextTitle3, context.getString(R.string.device_environment_voc));
					updateViews.setTextViewText(resStatusTextContents3, vocString);

					switch (hub.getVocStatus()) {
						case EnvironmentCheckManager.HIGH:
							updateViews.setImageViewResource(R.id.iv_app_widget_status1_widget3, R.drawable.bg_btn_oval_airquality_warning);
							updateViews.setTextColor(resStatusTextContents3, context.getResources().getColor(R.color.colorTextWarning));
							break;
						case EnvironmentCheckManager.NORMAL:
						default:
							updateViews.setImageViewResource(R.id.iv_app_widget_status1_widget3, R.drawable.bg_btn_oval_airquality_normal);
							updateViews.setTextColor(resStatusTextContents3, context.getResources().getColor(R.color.colorTextPrimary));
							break;
					}
				}

			} else {
				updateViews.setViewVisibility(R.id.rctn_app_widget1_lamp_onoff, View.GONE);
				updateViews.setViewVisibility(resDisconnectedContainer, View.VISIBLE);
				updateViews.setTextViewText(resDisconnectedTitle, context.getString(R.string.device_hub_disconnected_title));
				updateViews.setTextViewText(resDisconnectedContents, context.getString(R.string.device_hub_disconnected_detail));
			}
		}


		return true;
	}


	private boolean _updateLampInfo(Context context,
									  int appWidgetId,
									  int widgetSize,
									  RemoteViews updateViews,
									  int showIndex,
									  final DeviceLamp lamp) {

		if (DBG) Log.d(TAG, "_updateLampInfo: " + appWidgetId + " / " + widgetSize + " / " + lamp.getName() + "(" + lamp.deviceId + ")");
		final long deviceId = lamp.deviceId;

		int resDeviceName = R.id.tv_app_widget1_device_name;
		int resDisconnectedContainer = R.id.lctn_app_widget1_status_disconnected;
		int resDisconnectedTitle = R.id.tv_app_widget1_status_disconnected_title;
		int resDisconnectedContents = R.id.tv_app_widget1_status_disconnected_contents;

		int resStatusContainer1 = R.id.lctn_app_widget1_status1;
		int resStatusContainer2 = R.id.lctn_app_widget1_status2;
		int resStatusContainer3 = R.id.lctn_app_widget1_status3;

		int resIvStatusIcon1 = R.id.iv_app_widget1_status1;
		int resIvStatusIcon2 = R.id.iv_app_widget1_status2;
		int resIvStatusIcon3 = R.id.iv_app_widget1_status3;

		int resStatusTextTitle1 = R.id.tv_app_widget1_status1_title;
		int resStatusTextTitle2 = R.id.tv_app_widget1_status2_title;
		int resStatusTextTitle3 = R.id.tv_app_widget1_status3_title;

		int resStatusTextContents1 = R.id.tv_app_widget1_status1_contents;
		int resStatusTextContents2 = R.id.tv_app_widget1_status2_contents;
		int resStatusTextContents3 = R.id.tv_app_widget1_status3_contents;

		if (widgetSize == 2) {

		}

		if (showIndex == 1) {

			// 허브 이름
			updateViews.setTextViewText(resDeviceName, lamp.getName());
			updateViews.setViewVisibility(R.id.rctn_app_widget1_sensor_description, View.GONE);

			// 연결상태
			boolean isConnected = lamp.getConnectionState() != DeviceConnectionState.DISCONNECTED;
			if (isConnected) {
				updateViews.setViewVisibility(resDisconnectedContainer, View.GONE);

				updateViews.setViewVisibility(R.id.rctn_app_widget1_lamp_onoff, View.VISIBLE);
				Intent intentLampPower = new Intent(context, WidgetProvider2x1.class);
				intentLampPower.setAction(BROADCAST_MESSAGE_APPWIDGET_BRIGHTNESS);
				intentLampPower.putExtra(WidgetManager.APP_WIDGET_ID, appWidgetId);
				intentLampPower.putExtra("targetDeviceType", DeviceType.LAMP);
				intentLampPower.putExtra("targetDeviceId", lamp.deviceId);
				Uri appWidgetUri = Uri.parse("" + appWidgetId);
				intentLampPower.setData(appWidgetUri);
				PendingIntent pendingLampPower = PendingIntent.getBroadcast(context, 0, intentLampPower, PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.btn_app_widget1_lamp_onoff, pendingLampPower);

				// 온도설정
				float temperatureValue = lamp.getTemperature();
				String temperatureScale = mPreferenceMgr.getTemperatureScale();
				if (temperatureScale != null && temperatureScale.equals(context.getString(R.string.unit_temperature_fahrenheit))) {
					temperatureValue = UnitConvertUtil.getFahrenheitFromCelsius(lamp.getTemperature());
				}
				updateViews.setTextViewText(resStatusTextTitle1, context.getString(R.string.device_environment_temperature));
				updateViews.setTextViewText(resStatusTextContents1, temperatureValue + temperatureScale);

				switch (lamp.getTemperatureStatus()) {
					case EnvironmentCheckManager.HIGH:
						updateViews.setImageViewResource(resIvStatusIcon1, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents1, context.getResources().getColor(R.color.colorTextWarning));

						break;
					case EnvironmentCheckManager.LOW:
						updateViews.setImageViewResource(resIvStatusIcon1, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents1, context.getResources().getColor(R.color.colorTextWarning));
						break;
					case EnvironmentCheckManager.NORMAL:
					default:
						updateViews.setImageViewResource(resIvStatusIcon1, R.drawable.bg_btn_oval_airquality_normal);
						updateViews.setTextColor(resStatusTextContents1, context.getResources().getColor(R.color.colorTextPrimary));
						break;
				}

				// 습도설정
				float humidityValue = lamp.getHumidity();
				updateViews.setTextViewText(resStatusTextTitle2, context.getString(R.string.device_environment_humidity));
				updateViews.setTextViewText(resStatusTextContents2, humidityValue + "%");

				switch (lamp.getHumidityStatus()) {
					case EnvironmentCheckManager.HIGH:
						updateViews.setImageViewResource(resIvStatusIcon2, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents2, context.getResources().getColor(R.color.colorTextWarning));
						break;
					case EnvironmentCheckManager.LOW:
						updateViews.setImageViewResource(resIvStatusIcon2, R.drawable.bg_btn_oval_airquality_warning);
						updateViews.setTextColor(resStatusTextContents2, context.getResources().getColor(R.color.colorTextWarning));
						break;
					case EnvironmentCheckManager.NORMAL:
					default:
						updateViews.setImageViewResource(resIvStatusIcon2, R.drawable.bg_btn_oval_airquality_normal);
						updateViews.setTextColor(resStatusTextContents2, context.getResources().getColor(R.color.colorTextPrimary));
						break;
				}

				// VOC 상태
				updateViews.setViewVisibility(resStatusContainer3, View.GONE);
			} else {
				updateViews.setViewVisibility(R.id.rctn_app_widget1_lamp_onoff, View.GONE);
				updateViews.setViewVisibility(resDisconnectedContainer, View.VISIBLE);
				updateViews.setTextViewText(resDisconnectedTitle, context.getString(R.string.device_lamp_disconnected_title));
				updateViews.setTextViewText(resDisconnectedContents, context.getString(R.string.device_lamp_disconnected_detail));
			}
		}


		return true;
	}
}