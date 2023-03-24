package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.WidgetManager;
import goodmonit.monit.com.kao.provider.WidgetProvider2x1;
import goodmonit.monit.com.kao.provider.WidgetProvider2x2;
import goodmonit.monit.com.kao.provider.WidgetProvider2x3;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.widget.DeviceButton;

public class SettingWidgetActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "WidgetSetting";
    private static final boolean DBG = Configuration.DBG;

    private Button btnRefreshPeriod1, btnRefreshPeriod2, btnRefreshPeriod3, btnRefreshPeriod4, btnRefreshPeriod5;
    private ImageView ivRefreshPeriod1, ivRefreshPeriod2, ivRefreshPeriod3, ivRefreshPeriod4, ivRefreshPeriod5;
    private TextView tvMaxCountSelectableDevice, tvShareDeviceListEmpty;
    private LinearLayout lctnShareDeviceList, lctnShareDeviceListLeft, lctnShareDeviceListRight;

    ArrayList<DeviceButton> mArrDeviceButtons;
    ArrayList<Long> mArrSelectedDeviceInfo;

    private int mSelectedWidgetId;
    private int mMaxWidgetSize;
    private int mWidgetRefreshPeriodMin;
    private static final int WIDGET_REFRESH_PERIOD_MIN1 = 0;
    private static final int WIDGET_REFRESH_PERIOD_MIN2 = 30;
    private static final int WIDGET_REFRESH_PERIOD_MIN3 = 60;
    private static final int WIDGET_REFRESH_PERIOD_MIN4 = 120;
    private static final int WIDGET_REFRESH_PERIOD_MIN5 = 180;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_widget);
        _setToolBar();

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mArrDeviceButtons = new ArrayList<>();
        mArrSelectedDeviceInfo = new ArrayList<>();

        _initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSelectedWidgetId = getIntent().getIntExtra(WidgetManager.APP_WIDGET_ID, 0);
        mMaxWidgetSize = getIntent().getIntExtra(WidgetManager.APP_WIDGET_SIZE, WidgetManager.DEFAULT_APP_WIDGET_SIZE);
        if (DBG) Log.i(TAG, "onResume: " + mSelectedWidgetId + " / " + mMaxWidgetSize);

        mConnectionMgr = ConnectionManager.getInstance(null);

        mWidgetRefreshPeriodMin = mPreferenceMgr.getWidgetRefreshPeriodMin(mSelectedWidgetId);

        mArrSelectedDeviceInfo.clear();
        mArrSelectedDeviceInfo.add(mPreferenceMgr.getWidgetShowDeviceInfo(mSelectedWidgetId, 1));
        mArrSelectedDeviceInfo.add(mPreferenceMgr.getWidgetShowDeviceInfo(mSelectedWidgetId, 2));
        mArrSelectedDeviceInfo.add(mPreferenceMgr.getWidgetShowDeviceInfo(mSelectedWidgetId, 3));
        mArrSelectedDeviceInfo.add(mPreferenceMgr.getWidgetShowDeviceInfo(mSelectedWidgetId, 4));

        _setShareDeviceList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getString(R.string.widget_settings));

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedDevices = 0;
                for (DeviceButton btn: mArrDeviceButtons) {
                    if (btn == null) continue;
                    if (btn.isSelected()) {
                        selectedDevices++;
                    }
                }

                for (int i = 1; i <= WidgetManager.MAX_SELECT_DEVICE; i++) {
                    mPreferenceMgr.setWidgetShowDeviceInfo(mSelectedWidgetId, i, WidgetManager.DEFAULT_WIDGET_DEVICE_INFO);
                }

                int index = 1;
                for (DeviceButton btn: mArrDeviceButtons) {
                    if (btn == null) continue;

                    if (btn.isSelected()) {
                        int deviceType = btn.getDeviceType();
                        long deviceId = btn.getDeviceId();
                        long deviceInfo = deviceId * 10 + deviceType;
                        mPreferenceMgr.setWidgetShowDeviceInfo(mSelectedWidgetId, index, deviceInfo);
                        if (DBG) Log.d(TAG, "setWidgetShowDeviceInfo[" + mSelectedWidgetId + "," + index + "]: " + deviceInfo);
                        index++;
                    }
                }

                if (index == mMaxWidgetSize + 1) {
                    // 새로고침 버튼
                    Intent intentSync = null;
                    switch(mMaxWidgetSize) {
                        case 1:
                            intentSync = new Intent(mContext, WidgetProvider2x1.class);
                            break;
                        case 2:
                            intentSync = new Intent(mContext, WidgetProvider2x2.class);
                            break;
                        case 3:
                            intentSync = new Intent(mContext, WidgetProvider2x3.class);
                            break;
                        default:
                            intentSync = new Intent(mContext, WidgetProvider2x2.class);
                            break;
                    }
                    intentSync.setAction(WidgetManager.BROADCAST_MESSAGE_APPWIDGET_UPDATE_REFRESH);
                    intentSync.putExtra(WidgetManager.APP_WIDGET_ID, mSelectedWidgetId);
                    sendBroadcast(intentSync);

                    showToast(getString(R.string.toast_change_widget_setting_succeeded));
                    mPreferenceMgr.setWidgetRefreshPeriodMin(mSelectedWidgetId, mWidgetRefreshPeriodMin);
                    finish();
                } else {
                    showToast("Please select " + mMaxWidgetSize + " devices");
                }
            }
        });

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }

    private void updatedSelectedButton(int minutes) {
        btnRefreshPeriod1.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        btnRefreshPeriod2.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        btnRefreshPeriod3.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        btnRefreshPeriod4.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        btnRefreshPeriod5.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        ivRefreshPeriod1.setBackgroundResource(R.drawable.ic_radio_default);
        ivRefreshPeriod2.setBackgroundResource(R.drawable.ic_radio_default);
        ivRefreshPeriod3.setBackgroundResource(R.drawable.ic_radio_default);
        ivRefreshPeriod4.setBackgroundResource(R.drawable.ic_radio_default);
        ivRefreshPeriod5.setBackgroundResource(R.drawable.ic_radio_default);

        Button btn;
        ImageView iv;
        switch(minutes) {
            case WIDGET_REFRESH_PERIOD_MIN2:
                btn = btnRefreshPeriod2;
                iv = ivRefreshPeriod2;
                break;
            case WIDGET_REFRESH_PERIOD_MIN3:
                btn = btnRefreshPeriod3;
                iv = ivRefreshPeriod3;
                break;
            case WIDGET_REFRESH_PERIOD_MIN4:
                btn = btnRefreshPeriod4;
                iv = ivRefreshPeriod4;
                break;
            case WIDGET_REFRESH_PERIOD_MIN5:
                btn = btnRefreshPeriod5;
                iv = ivRefreshPeriod5;
                break;
            case WIDGET_REFRESH_PERIOD_MIN1:
                // Through
            default:
                btn = btnRefreshPeriod1;
                iv = ivRefreshPeriod1;
                break;
        }

        if (btn != null) {
            btn.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        if (iv != null) {
            iv.setBackgroundResource(R.drawable.ic_radio_selected);
        }

        mWidgetRefreshPeriodMin = minutes;
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
/*
        btnRefreshPeriod1 = (Button)findViewById(R.id.btn_setting_widget_refresh_period1); // 수동
        btnRefreshPeriod2 = (Button)findViewById(R.id.btn_setting_widget_refresh_period2); // 30분
        btnRefreshPeriod3 = (Button)findViewById(R.id.btn_setting_widget_refresh_period3); // 1시간
        btnRefreshPeriod4 = (Button)findViewById(R.id.btn_setting_widget_refresh_period4); // 2시간
        btnRefreshPeriod5 = (Button)findViewById(R.id.btn_setting_widget_refresh_period5); // 3시간

        ivRefreshPeriod1 = (ImageView)findViewById(R.id.iv_setting_widget_refresh_period1); // 수동
        ivRefreshPeriod2 = (ImageView)findViewById(R.id.iv_setting_widget_refresh_period2); // 30분
        ivRefreshPeriod3 = (ImageView)findViewById(R.id.iv_setting_widget_refresh_period3); // 1시간
        ivRefreshPeriod4 = (ImageView)findViewById(R.id.iv_setting_widget_refresh_period4); // 2시간
        ivRefreshPeriod5 = (ImageView)findViewById(R.id.iv_setting_widget_refresh_period5); // 3시간

        btnRefreshPeriod1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedSelectedButton(WIDGET_REFRESH_PERIOD_MIN1);
            }
        });
        btnRefreshPeriod2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedSelectedButton(WIDGET_REFRESH_PERIOD_MIN2);
            }
        });
        btnRefreshPeriod3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedSelectedButton(WIDGET_REFRESH_PERIOD_MIN3);
            }
        });
        btnRefreshPeriod4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedSelectedButton(WIDGET_REFRESH_PERIOD_MIN4);
            }
        });
        btnRefreshPeriod5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedSelectedButton(WIDGET_REFRESH_PERIOD_MIN5);
            }
        });
*/
        tvMaxCountSelectableDevice = (TextView)findViewById(R.id.tv_setting_widget_select_max_device_description);
        tvShareDeviceListEmpty = (TextView)findViewById(R.id.tv_setting_widget_share_device_list_empty);
        lctnShareDeviceList = (LinearLayout)findViewById(R.id.lctn_setting_widget_share_device_list);
        lctnShareDeviceListLeft = (LinearLayout)findViewById(R.id.lctn_setting_widget_share_device_list_left);
        lctnShareDeviceListRight = (LinearLayout)findViewById(R.id.lctn_setting_widget_share_device_list_right);
    }

    private void _setShareDeviceList() {
        lctnShareDeviceListLeft.removeAllViews();
        lctnShareDeviceListRight.removeAllViews();
        mArrDeviceButtons.clear();

        tvMaxCountSelectableDevice.setText("(" + mMaxWidgetSize + ")");

        boolean putIntoLeftContainer = true;
        ArrayList<Long> arrDeviceInfoList = new ArrayList<>(); // 리스트에 현재 등록된 기기들 추가
        for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
            if (sensor == null) continue;

            arrDeviceInfoList.add(sensor.deviceId * 10 + DeviceType.DIAPER_SENSOR);

            DeviceButton deviceButton = new DeviceButton(mContext);
            deviceButton.setDeviceType(DeviceType.DIAPER_SENSOR);
            deviceButton.setDeviceId(sensor.deviceId);
            deviceButton.setDeviceName(sensor.name);
            deviceButton.setSelected(false);
            mArrDeviceButtons.add(deviceButton);

            if (putIntoLeftContainer) {
                lctnShareDeviceListLeft.addView(deviceButton);
            } else {
                lctnShareDeviceListRight.addView(deviceButton);
            }
            putIntoLeftContainer = !putIntoLeftContainer;
        }

        for (DeviceAQMHub hub : ConnectionManager.mRegisteredAQMHubList.values()) {
            if (hub == null) continue;
            DeviceButton deviceButton = new DeviceButton(mContext);
            deviceButton.setDeviceType(DeviceType.AIR_QUALITY_MONITORING_HUB);
            deviceButton.setDeviceId(hub.deviceId);
            deviceButton.setDeviceName(hub.name);
            deviceButton.setSelected(false);
            mArrDeviceButtons.add(deviceButton);

            if (putIntoLeftContainer) {
                lctnShareDeviceListLeft.addView(deviceButton);
            } else {
                lctnShareDeviceListRight.addView(deviceButton);
            }
            putIntoLeftContainer = !putIntoLeftContainer;
        }

        for (DeviceLamp lamp : ConnectionManager.mRegisteredLampList.values()) {
            if (lamp == null) continue;
            DeviceButton deviceButton = new DeviceButton(mContext);
            deviceButton.setDeviceType(DeviceType.LAMP);
            deviceButton.setDeviceId(lamp.deviceId);
            deviceButton.setDeviceName(lamp.name);
            deviceButton.setSelected(false);
            mArrDeviceButtons.add(deviceButton);

            if (putIntoLeftContainer) {
                lctnShareDeviceListLeft.addView(deviceButton);
            } else {
                lctnShareDeviceListRight.addView(deviceButton);
            }
            putIntoLeftContainer = !putIntoLeftContainer;
        }

        // Selected 설정하기
        for (DeviceButton btn : mArrDeviceButtons) {
            if (btn == null) continue;

            long deviceInfo = btn.getDeviceId() * 10 + btn.getDeviceType();
            if (mArrSelectedDeviceInfo.contains(deviceInfo)) {
                if (DBG) Log.d(TAG, "selected btn: " + deviceInfo);
                btn.setSelected(true);
            }
        }

        // EmptyView 보이기/숨기기
        if (mArrDeviceButtons.size() > 0) {
            tvShareDeviceListEmpty.setVisibility(View.GONE);
        } else {
            tvShareDeviceListEmpty.setVisibility(View.VISIBLE);
        }
    }
}
