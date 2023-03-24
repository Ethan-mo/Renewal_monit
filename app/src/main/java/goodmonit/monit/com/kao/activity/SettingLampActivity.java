package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dfu.LampFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dialog.ProgressCircleDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.SettingButton;
import goodmonit.monit.com.kao.widget.SettingButtonSwitch;
import goodmonit.monit.com.kao.widget.SettingButtonWarning;
import goodmonit.monit.com.kao.widget.SettingButtonWheelView;
import goodmonit.monit.com.kao.widget.SettingEditText;
import goodmonit.monit.com.kao.widget.SettingTextDivider;
import goodmonit.monit.com.kao.widget.WheelView;

public class SettingLampActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "SettLamp";
    private static final boolean DBG = Configuration.DBG;

    private static final int VIEW_MAIN                  = 1;
    private static final int VIEW_CHANGE_NAME           = 2;
    private static final int VIEW_TEMPERATURE_ALARM     = 3;
    private static final int VIEW_HUMIDITY_ALARM        = 4;
    private static final int VIEW_LED_ON_OFF_TIME       = 5;

    private static final int MSG_CLOSE_FINDING_DEVICE_DIALOG    = 1;
    private static final int MSG_SHOW_VIEW                      = 3;
    private static final int MSG_UPDATE_FIRMWARE_VERSION        = 4;
    private static final int MSG_UPDATE_VIEW_VALUE              = 5;
    private static final int MSG_SHOW_OTA_UPDATE_DIALOG         = 6;

    private static final int MIN_TEMPERATURE_CELSIUS = 0;
    private static final int MAX_TEMPERATURE_CELSIUS = 50;
    private static final int MIN_TEMPERATURE_FAHRENHEIT = 32;
    private static final int MAX_TEMPERATURE_FAHRENHEIT = 122;

    private int mCurrentViewIndex;

    private LinearLayout lctnButtonList;
    private SettingButton btnName, btnFirmwareVersion, btnTemperature, btnHumidity, btnTemperatureUnit, btnAPInfo, btnLEDLightTime, btnSerialNumber;
    private SettingButtonSwitch btnEnableAlarm, btnEnableAlarmConnection, btnEnableAlarmVOC;
    private SettingButtonWarning btnRemove, btnInit;

    private SettingTextDivider btnTextDivider;
    private SettingTextDivider btnTextDividerDescription;

    private SettingButtonSwitch btnEnableAlarmTemperature;
    private SettingButtonWheelView btnHighTemperatureWheelView, btnLowTemperatureWheelView;

    private SettingButtonSwitch btnEnableAlarmHumidity;
    private SettingButtonWheelView btnHighHumidityWheelView, btnLowHumidityWheelView;

    private SettingButtonSwitch btnLedOnOff;
    private SettingButtonWheelView btnLedOnTimeWheelView, btnLedOffTimeWheelView;
    private String mLedOnLocalTime, mLedOffLocalTime;

    private SettingEditText etName;

    private long mConnectedDeviceId;
    private DeviceLamp mLampDevice;

    private SimpleDialog mDlgRemoveConfirmation, mDlgInitConfirmation;
    private SimpleDialog mDlgDfuError;
    private ProgressCircleDialog mDlgFinding, mDlgOTAUpdate;

    private boolean isConnected;

    private float mMaxTemperature, mMinTemperature, mMaxHumidity, mMinHumidity;

    private PreferenceManager mPreferenceMgr;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        _setToolBar();

        mContext = this;
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mScreenInfo = new ScreenInfo(1201);

        _setLamp();

        _initView();

        if (mLampDevice.cloudId == mPreferenceMgr.getAccountId()) {
            if (DBG) Log.d(TAG, "Owner");
            isOwner = true;
        } else {
            if (DBG) Log.d(TAG, "Other : " + mLampDevice.cloudId + " / " + mPreferenceMgr.getAccountId());
            isOwner = false;
        }

        mCurrentViewIndex = VIEW_MAIN;
        _showSettingMainView();
    }

    private void _setLamp() {
        mConnectedDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        mLampDevice = ConnectionManager.getDeviceLamp(mConnectedDeviceId);
        if (mLampDevice != null) {
            if (DBG) Log.d(TAG, "targetDevice : [" + mLampDevice.deviceId + "] " + mLampDevice.name);
        } else {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedDeviceId);
            finish();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");

        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        if ((mLampDevice.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) ||
                (mLampDevice.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED)) {
            setConnected(true);
        } else {
            setConnected(false);
        }

        if (mLampDevice.serial == null) {
            mConnectionMgr.getUserInfoFromCloud();
        }
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getString(R.string.title_setting).toUpperCase());

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mCurrentViewIndex == VIEW_MAIN) {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
        } else {
            _showSettingView(VIEW_MAIN);
        }
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
        lctnButtonList = (LinearLayout) findViewById(R.id.lctn_setting_list);
    }

    private void _showSettingView(int viewIndex) {
        mHandler.obtainMessage(MSG_SHOW_VIEW, viewIndex, -1).sendToTarget();
    }

    private void _showSettingChangeName() {
        tvToolbarTitle.setText(getString(R.string.setting_device_name).toUpperCase() + " " + getString(R.string.title_setting).toUpperCase());
        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = etName.getText();
                // 데이터 업데이트
                mLampDevice = ConnectionManager.getDeviceLamp(mLampDevice.deviceId);
                ServerQueryManager.getInstance(mContext).setDeviceName(
                        mLampDevice.type,
                        mLampDevice.deviceId,
                        mLampDevice.getEnc(),
                        name,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    mLampDevice.name = name;
                                    showToast(getString(R.string.toast_change_device_name_succeeded));
                                    mPreferenceMgr.setDeviceName(DeviceType.LAMP, mLampDevice.deviceId, name);
                                    _showSettingView(VIEW_MAIN);
                                } else {
                                    showToast(getString(R.string.toast_change_device_name_failed));
                                }
                            }
                        });

            }
        });
        _initSettingChangeName();

        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(etName);
    }

    private void _showSettingMainView() {
        tvToolbarTitle.setText(getString(R.string.title_setting).toUpperCase());
        btnToolbarRight.setVisibility(View.GONE);

        _initSettingMain();
        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnName);
        lctnButtonList.addView(btnAPInfo);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnTemperatureUnit);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnEnableAlarm);
        lctnButtonList.addView(btnTemperature);
        lctnButtonList.addView(btnHumidity);
        //lctnButtonList.addView(btnEnableAlarmConnection);
        //lctnButtonList.addView(btnEnableAlarmVOC);

        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnLedOnOff);
        lctnButtonList.addView(btnLEDLightTime);

        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnFirmwareVersion);
        lctnButtonList.addView(btnSerialNumber);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));

        if (isOwner) {
            lctnButtonList.addView(btnInit);
        } else {
            lctnButtonList.addView(btnRemove);
            lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
            lctnButtonList.addView(btnInit);
        }
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider_footer, null));

        _updateAlarmSettings(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.LAMP, mLampDevice.deviceId, NotificationType.DEVICE_ALL));
        _hideKeyboard();
    }

    private void _showTemperatureAlarmView() {
        _initTemperatureAlarmView();
        lctnButtonList.removeAllViews();
        //lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        //lctnButtonList.addView(btnEnableAlarmTemperature);
        lctnButtonList.addView(btnTextDivider);
        lctnButtonList.addView(btnHighTemperatureWheelView);
        lctnButtonList.addView(btnLowTemperatureWheelView);

        lctnButtonList.addView(btnTextDividerDescription);
    }

    private void _initTemperatureAlarmView() {
        tvToolbarTitle.setText(getString(R.string.device_environment_temperature).toUpperCase() + " " + getString(R.string.title_setting).toUpperCase());

        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMaxTemperature <= mMinTemperature) {
                    showToast(mContext.getString(R.string.toast_invalid_min_max_range));
                    return;
                }

                if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                    mMaxTemperature = UnitConvertUtil.getCelsiusFromFahrenheit(mMaxTemperature);
                    mMinTemperature = UnitConvertUtil.getCelsiusFromFahrenheit(mMinTemperature);
                }

                mServerQueryMgr.setAlarmThreshold(
                        mLampDevice.type,
                        mLampDevice.deviceId,
                        mLampDevice.getEnc(),
                        mMaxTemperature,
                        mMinTemperature,
                        -1,
                        -1,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    mLampDevice.setMaxTemperature(mMaxTemperature);
                                    mLampDevice.setMinTemperature(mMinTemperature);
                                    showToast(getString(R.string.toast_setting_temperature_succeeded));
                                } else {
                                    showToast(getString(R.string.toast_setting_temperature_failed));
                                }
                                _showSettingView(VIEW_MAIN);
                            }
                        });
            }
        });

        if (btnTextDivider == null) {
            btnTextDivider = new SettingTextDivider(this);
        }
        btnTextDivider.setTitle(getString(R.string.setting_custom_temperature));

        if (btnTextDividerDescription == null) {
            btnTextDividerDescription = new SettingTextDivider(this);
            btnTextDividerDescription.setPrimaryColor(false);
            btnTextDividerDescription.showBottomDivider(false);
        }
        btnTextDividerDescription.setTitle(getString(R.string.setting_custom_temperature_description));

        mMaxTemperature = mLampDevice.getMaxTemperature();
        mMinTemperature = mLampDevice.getMinTemperature();

        if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
            mMaxTemperature = UnitConvertUtil.getFahrenheitFromCelsius(mMaxTemperature);
            mMinTemperature = UnitConvertUtil.getFahrenheitFromCelsius(mMinTemperature);
        }

        ArrayList<String> items = new ArrayList<String>();
        if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
            for(float i = MIN_TEMPERATURE_FAHRENHEIT; i < MAX_TEMPERATURE_FAHRENHEIT; i = i + (float)0.5) { // 0도~50도
                items.add(i + "");
            }
        } else {
            for(float i = MIN_TEMPERATURE_CELSIUS; i < MAX_TEMPERATURE_CELSIUS; i = i + (float)0.5) {
                items.add(i + "");
            }
        }

        if (btnHighTemperatureWheelView == null) {
            btnHighTemperatureWheelView = new SettingButtonWheelView(this);
            btnHighTemperatureWheelView.setTitle(getString(R.string.setting_max_temperature_threshold));
            btnHighTemperatureWheelView.setDividerForOtherCategory(false);
            btnHighTemperatureWheelView.showDirection(true);
            btnHighTemperatureWheelView.setOnSelectedListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onValueChanged(int selectedIndex, String item) {
                    mMaxTemperature = selectedIndex * (float) 0.5;
                    if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                        mMaxTemperature = mMaxTemperature + 32;
                    }
                    btnHighTemperatureWheelView.setContent(mMaxTemperature + " " + mPreferenceMgr.getTemperatureScale());
                }
            });
            btnHighTemperatureWheelView.setOnExpandListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onExpanded() {
                    //int idx = (int)(mPreferenceMgr.getThresholdHighTemperature() / (float)0.5);
                    int idx = (int) (mMaxTemperature / (float) 0.5);
                    if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                        idx = idx - 32 * 2;
                    }
                    btnHighTemperatureWheelView.selectItem(idx);
                }
            });
        }
        btnHighTemperatureWheelView.setItems(items);
        btnHighTemperatureWheelView.setExtraText(" " + mPreferenceMgr.getTemperatureScale());
        btnHighTemperatureWheelView.setContent(mMaxTemperature + " " + mPreferenceMgr.getTemperatureScale());
        btnHighTemperatureWheelView.expandWheelView(false);

        if (btnLowTemperatureWheelView == null) {
            btnLowTemperatureWheelView = new SettingButtonWheelView(this);
            btnLowTemperatureWheelView.setTitle(getString(R.string.setting_min_temperature_threshold));
            btnLowTemperatureWheelView.setDividerForOtherCategory(true);
            btnLowTemperatureWheelView.showDirection(true);
            btnLowTemperatureWheelView.setOnSelectedListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onValueChanged(int selectedIndex, String item) {
                    mMinTemperature = selectedIndex * (float) 0.5;
                    if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                        mMinTemperature = mMinTemperature + 32;
                    }
                    btnLowTemperatureWheelView.setContent(mMinTemperature + " " + mPreferenceMgr.getTemperatureScale());
                }
            });
            btnLowTemperatureWheelView.setOnExpandListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onExpanded() {
                    int idx = (int) (mMinTemperature / (float) 0.5);
                    if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                        idx = (int)(mMinTemperature / (float) 0.5) - 32 * 2; // 0.5씩 증가하므로 2를 곱함
                    }
                    btnLowTemperatureWheelView.selectItem(idx);
                }
            });
        }
        btnLowTemperatureWheelView.setItems(items);
        btnLowTemperatureWheelView.setExtraText(" " + mPreferenceMgr.getTemperatureScale());
        btnLowTemperatureWheelView.setContent(mMinTemperature + " " + mPreferenceMgr.getTemperatureScale());
        btnLowTemperatureWheelView.expandWheelView(false);

        if (btnEnableAlarmTemperature == null) {
            btnEnableAlarmTemperature = new SettingButtonSwitch(this);
            btnEnableAlarmTemperature.setDividerForOtherCategory(true);
            btnEnableAlarmTemperature.setTitle(getString(R.string.setting_device_enable_alarm));
            btnEnableAlarmTemperature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = btnEnableAlarmTemperature.isChecked();
                    if (DBG) Log.d(TAG, "Enable AlarmTemperature : " + btnEnableAlarmTemperature.isChecked());
                    if (checked) {
                        btnHighTemperatureWheelView.setEnabled(true);
                        btnLowTemperatureWheelView.setEnabled(true);
                    } else {
                        btnHighTemperatureWheelView.expandWheelView(false);
                        btnHighTemperatureWheelView.setEnabled(false);
                        btnLowTemperatureWheelView.expandWheelView(false);
                        btnLowTemperatureWheelView.setEnabled(false);
                    }
                }
            });
        }
        btnEnableAlarmTemperature.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.LAMP, mLampDevice.deviceId, NotificationType.HIGH_TEMPERATURE));
    }

    private void _showHumidityAlarmView() {
        _initHumidityAlarmView();
        lctnButtonList.removeAllViews();
        //lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        //lctnButtonList.addView(btnEnableAlarmHumidity);
        lctnButtonList.addView(btnTextDivider);
        lctnButtonList.addView(btnHighHumidityWheelView);
        lctnButtonList.addView(btnLowHumidityWheelView);

        lctnButtonList.addView(btnTextDividerDescription);
    }

    private void _initHumidityAlarmView() {
        tvToolbarTitle.setText(getString(R.string.device_environment_humidity).toUpperCase() + " " + getString(R.string.title_setting).toUpperCase());

        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMaxHumidity <= mMinHumidity) {
                    showToast(mContext.getString(R.string.toast_invalid_min_max_range));
                    return;
                }

                mServerQueryMgr.setAlarmThreshold(
                        mLampDevice.type,
                        mLampDevice.deviceId,
                        mLampDevice.getEnc(),
                        -1,
                        -1,
                        mMaxHumidity,
                        mMinHumidity,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    mLampDevice.setMaxHumidity(mMaxHumidity);
                                    mLampDevice.setMinHumidity(mMinHumidity);
                                    showToast(getString(R.string.toast_setting_humidity_succeeded));
                                } else {
                                    showToast(getString(R.string.toast_setting_humidity_failed));
                                }
                                _showSettingView(VIEW_MAIN);
                            }
                        });
            }
        });

        if (btnTextDivider == null) {
            btnTextDivider = new SettingTextDivider(this);
        }
        btnTextDivider.setTitle(getString(R.string.setting_custom_humidity));

        if (btnTextDividerDescription == null) {
            btnTextDividerDescription = new SettingTextDivider(this);
            btnTextDividerDescription.setPrimaryColor(false);
            btnTextDividerDescription.showBottomDivider(false);
        }
        btnTextDividerDescription.setTitle(getString(R.string.setting_custom_humidity_description));

        mMaxHumidity = mLampDevice.getMaxHumidity();
        mMinHumidity = mLampDevice.getMinHumidity();

        ArrayList<String> items = new ArrayList<String>();
        for (int i = 10; i <= 90; i = i + 5) {
            items.add(i + "");
        }

        if (btnHighHumidityWheelView == null) {
            btnHighHumidityWheelView = new SettingButtonWheelView(this);
            btnHighHumidityWheelView.setTitle(getString(R.string.setting_max_humidity_threshold));
            btnHighHumidityWheelView.setDividerForOtherCategory(false);
            btnHighHumidityWheelView.showDirection(true);
            btnHighHumidityWheelView.setOnSelectedListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onValueChanged(int selectedIndex, String item) {
                    int selectedHumidity = selectedIndex * 5 + 10;
                    btnHighHumidityWheelView.setContent(selectedHumidity + " %");
                    //mLampDevice.setMaxHumidity(selectedHumidity);
                    mMaxHumidity = selectedHumidity;
                    //mPreferenceMgr.setThresholdHighHumidity(selectedHumidity);
                }
            });
            btnHighHumidityWheelView.setExtraText(" %");

            btnHighHumidityWheelView.setItems(items);
        }

        btnHighHumidityWheelView.setContent(mLampDevice.getMaxHumidity() + " %");
        //btnHighHumidityWheelView.setContent((int)mPreferenceMgr.getThresholdHighHumidity() + " %");
        //btnHighHumidityWheelView.setMinValue((int)mPreferenceMgr.getThresholdLowHumidity());
        //btnHighHumidityWheelView.setValue((int)mPreferenceMgr.getThresholdHighHumidity());
        btnHighHumidityWheelView.invalidate();
        btnHighHumidityWheelView.setOnExpandListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onExpanded() {
                //int idx = (int)(mLampDevice.mPreferenceMgr.getThresholdHighHumidity() / 5 - 2);
                int idx = (int)(mLampDevice.getMaxHumidity() / 5 - 2);
                btnHighHumidityWheelView.selectItem(idx);
            }
        });
        btnHighHumidityWheelView.expandWheelView(false);

        if (btnLowHumidityWheelView == null) {
            btnLowHumidityWheelView = new SettingButtonWheelView(this);
            btnLowHumidityWheelView.setTitle(getString(R.string.setting_min_humidity_threshold));
            btnLowHumidityWheelView.setDividerForOtherCategory(true);
            btnLowHumidityWheelView.showDirection(true);
            btnLowHumidityWheelView.setOnSelectedListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onValueChanged(int selectedIndex, String item) {
                    int selectedHumidity = selectedIndex * 5 + 10;
                    btnLowHumidityWheelView.setContent(selectedHumidity + " %");
                    //mLampDevice.setMinHumidity(selectedHumidity);
                    mMinHumidity = selectedHumidity;
                    //mPreferenceMgr.setThresholdLowHumidity(selectedHumidity);
                }
            });
            btnLowHumidityWheelView.setExtraText(" %");
            btnLowHumidityWheelView.setItems(items);
        }
        //btnLowHumidityWheelView.setContent((int)mPreferenceMgr.getThresholdLowHumidity() + " %");
        btnLowHumidityWheelView.setContent(mLampDevice.getMinHumidity() + " %");
        //btnLowHumidityWheelView.setMaxValue((int)mPreferenceMgr.getThresholdHighHumidity());
        //btnLowHumidityWheelView.setValue((int)mPreferenceMgr.getThresholdLowHumidity());
        btnLowHumidityWheelView.invalidate();
        btnLowHumidityWheelView.setOnExpandListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onExpanded() {
                //int idx = (int)(mPreferenceMgr.getThresholdLowHumidity() / 5 - 2);
                int idx = (int)(mLampDevice.getMinHumidity() / 5 - 2);
                btnLowHumidityWheelView.selectItem(idx);
            }
        });

        btnLowHumidityWheelView.expandWheelView(false);

        if (btnEnableAlarmHumidity == null) {
            btnEnableAlarmHumidity = new SettingButtonSwitch(this);
            btnEnableAlarmHumidity.setDividerForOtherCategory(true);
            btnEnableAlarmHumidity.setTitle(getString(R.string.setting_device_enable_alarm));
            btnEnableAlarmHumidity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = btnEnableAlarmHumidity.isChecked();
                    if (DBG) Log.d(TAG, "Enable AlarmHumidity : " + btnEnableAlarmHumidity.isChecked());
                    if (checked) {
                        btnHighHumidityWheelView.setEnabled(true);
                        btnLowHumidityWheelView.setEnabled(true);
                    } else {
                        btnHighHumidityWheelView.expandWheelView(false);
                        btnHighHumidityWheelView.setEnabled(false);
                        btnLowHumidityWheelView.expandWheelView(false);
                        btnLowHumidityWheelView.setEnabled(false);
                    }
                }
            });
        }
        btnEnableAlarmHumidity.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.LAMP, mLampDevice.deviceId, NotificationType.HIGH_HUMIDITY));
    }
    private void _updateAlarmSettings(boolean checked) {
        if (checked) {
            btnEnableAlarmVOC.setVisibility(View.VISIBLE);
            btnEnableAlarmConnection.setVisibility(View.VISIBLE);
            btnTemperature.setVisibility(View.VISIBLE);
            btnHumidity.setVisibility(View.VISIBLE);
            btnEnableAlarm.setDividerForOtherCategory(false);
        } else {
            btnEnableAlarmVOC.setVisibility(View.GONE);
            btnEnableAlarmConnection.setVisibility(View.GONE);
            btnTemperature.setVisibility(View.GONE);
            btnHumidity.setVisibility(View.GONE);
            btnEnableAlarm.setDividerForOtherCategory(true);
        }
    }

    private void _updateLEDSettings(boolean checked) {
        btnLedOnOff.setChecked(checked);
        if (checked) {
            btnLedOnOff.setDividerForOtherCategory(false);
            btnLEDLightTime.setVisibility(View.VISIBLE);
        } else {
            btnLedOnOff.setDividerForOtherCategory(true);
            btnLEDLightTime.setVisibility(View.GONE);
        }
    }

    private void _showLEDOnTimeSettingView() {
        _initLEDOnTimeSettingView();
        lctnButtonList.removeAllViews();

        lctnButtonList.addView(btnTextDivider);
        lctnButtonList.addView(btnLedOnTimeWheelView);
        lctnButtonList.addView(btnLedOffTimeWheelView);

        lctnButtonList.addView(btnTextDividerDescription);
    }

    private void _initLEDOnTimeSettingView() {
        tvToolbarTitle.setText(getString(R.string.device_environment_led).toUpperCase() + " " + getString(R.string.title_setting).toUpperCase());

        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ledOnUtcTime = DateTimeUtil.convertLocalToUTCTime(mLedOnLocalTime);
                String ledOffUtcTime = DateTimeUtil.convertLocalToUTCTime(mLedOffLocalTime) + mLedOffLocalTime.substring(4);
                if (DBG) Log.d(TAG, "Converted : " + ledOnUtcTime + " ~ " + ledOffUtcTime + " / " + mLedOffLocalTime + " / " + mLedOffLocalTime.substring(4));

                mServerQueryMgr.setLedOnOffTime(
                        mLampDevice.type,
                        mLampDevice.deviceId,
                        mLampDevice.getEnc(),
                        ledOnUtcTime,
                        ledOffUtcTime,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    mLampDevice.setLedOnTime(mLedOnLocalTime);
                                    mLampDevice.setLedOffTime(mLedOffLocalTime);
                                    showToast(getString(R.string.toast_setting_ledontime_succeeded));
                                } else {
                                    showToast(getString(R.string.toast_setting_ledontime_failed));
                                }
                                _showSettingView(VIEW_MAIN);
                            }
                        });
            }
        });

        if (btnTextDivider == null) {
            btnTextDivider = new SettingTextDivider(this);
        }
        btnTextDivider.setTitle(getString(R.string.setting_custom_led_on_off_time));

        if (btnTextDividerDescription == null) {
            btnTextDividerDescription = new SettingTextDivider(this);
            btnTextDividerDescription.setPrimaryColor(false);
            btnTextDividerDescription.showBottomDivider(false);
        }
        btnTextDividerDescription.setTitle(getString(R.string.setting_custom_led_on_off_time_description));

        mLedOnLocalTime = mLampDevice.getLedOnTime();
        mLedOffLocalTime = mLampDevice.getLedOffTime();
        if (mLedOffLocalTime == null) {
            mLedOffLocalTime = "0000";
        }
        if (mLedOffLocalTime.length() == 4) {
            mLedOffLocalTime += (btnLedOnOff.isChecked() ? "0" : "1");
        }

        ArrayList<String> hourList = new ArrayList<String>();
        for (int i = 0; i < 24; i = i + 1) {
            if (i < 10) {
                hourList.add("0" + i);
            } else {
                hourList.add("" + i);
            }
        }

        ArrayList<String> minuteList = new ArrayList<String>();
        for (int i = 0; i < 60; i = i + 5) {
            if (i < 10) {
                minuteList.add("0" + i);
            } else {
                minuteList.add("" + i);
            }
        }

        if (btnLedOnTimeWheelView == null) {
            btnLedOnTimeWheelView = new SettingButtonWheelView(this);
            btnLedOnTimeWheelView.setTitle(getString(R.string.setting_led_on_time));
            btnLedOnTimeWheelView.setDividerForOtherCategory(false);
            btnLedOnTimeWheelView.showDirection(true);
            btnLedOnTimeWheelView.setOnSelectedListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onValueChanged(int selectedIndex, String item) {
                    mLedOnLocalTime = item + mLedOnLocalTime.substring(2, 4);
                    btnLedOnTimeWheelView.setContent(item + " : " + mLedOnLocalTime.substring(2, 4));
                }
            });
            btnLedOnTimeWheelView.setExtraText(" " + getString(R.string.time_hour_short));
            btnLedOnTimeWheelView.setItems(hourList);
        }

        btnLedOnTimeWheelView.setContent(mLampDevice.getLedOnTime().substring(0, 2) + " : " + mLampDevice.getLedOnTime().substring(2, 4));
        btnLedOnTimeWheelView.invalidate();
        btnLedOnTimeWheelView.setOnExpandListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onExpanded() {
                btnLedOnTimeWheelView.selectItem(mLedOnLocalTime.substring(0, 2));
            }
        });
        btnLedOnTimeWheelView.expandWheelView(false);

        if (btnLedOffTimeWheelView == null) {
            btnLedOffTimeWheelView = new SettingButtonWheelView(this);
            btnLedOffTimeWheelView.setTitle(getString(R.string.setting_led_off_time));
            btnLedOffTimeWheelView.setDividerForOtherCategory(true);
            btnLedOffTimeWheelView.showDirection(true);
            btnLedOffTimeWheelView.setOnSelectedListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onValueChanged(int selectedIndex, String item) {
                    mLedOffLocalTime = item + mLedOffLocalTime.substring(2, 5);
                    btnLedOffTimeWheelView.setContent(item + " : " + mLedOffLocalTime.substring(2, 4));
                }
            });
            btnLedOffTimeWheelView.setExtraText(" " + getString(R.string.time_hour_short));
            btnLedOffTimeWheelView.setItems(hourList);
        }

        btnLedOffTimeWheelView.setContent(mLampDevice.getLedOffTime().substring(0, 2) + " : " + mLampDevice.getLedOffTime().substring(2, 4));
        btnLedOffTimeWheelView.invalidate();
        btnLedOffTimeWheelView.setOnExpandListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onExpanded() {
                btnLedOffTimeWheelView.selectItem(mLedOffLocalTime.substring(0, 2));
            }
        });
        btnLedOffTimeWheelView.expandWheelView(false);
    }

    private void _initSettingChangeName() {
        etName = new SettingEditText(this);
        etName.setTitle(getString(R.string.setting_device_name));
        etName.setText(mLampDevice.name);
    }

    private void _initPreference() {
        mPreferenceMgr.setLatestLampGraphUpdatedTimeSec(mLampDevice.deviceId, 0);
        mPreferenceMgr.setLatestNotificationTimeMs(DeviceType.LAMP, mLampDevice.deviceId, 0);
        mPreferenceMgr.setLatestCheckedNotificationIndex(DeviceType.LAMP, mLampDevice.deviceId, 0);
        mPreferenceMgr.setLatestSavedNotificationIndex(DeviceType.LAMP, mLampDevice.deviceId, 0, 0);
        mPreferenceMgr.setLatestSavedNotificationIndex(DeviceType.LAMP, mLampDevice.deviceId, 1, 0);
        mPreferenceMgr.setLatestSavedNotificationIndex(DeviceType.LAMP, mLampDevice.deviceId, 2, 0);
        mPreferenceMgr.setDeviceSerialNumber(DeviceType.LAMP, mLampDevice.deviceId, null);
    }

    private void _initSettingMain() {
        if (mDlgRemoveConfirmation == null) {
            mDlgRemoveConfirmation = new SimpleDialog(SettingLampActivity.this,
                    getString(R.string.dialog_contents_remove_device),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgRemoveConfirmation.dismiss();
                        }
                    },
                    getString(R.string.btn_remove),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 그룹탈퇴
                            String targetEmailAddress = UserInfoManager.getInstance(mContext).getEmailAddress(mLampDevice.cloudId);
                            _initPreference();
                            mDlgRemoveConfirmation.dismiss();
                            showProgressBar(true);
                            mServerQueryMgr.leaveCloud(mLampDevice.cloudId, new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    showProgressBar(false);
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        showToast(getString(R.string.toast_leave_group_succeeded));
                                    } else {
                                        showToast(getString(R.string.toast_leave_group_failed));
                                    }
                                    mConnectionMgr.leaveGroup(mLampDevice.cloudId);
                                    onBackPressed();
                                }
                            });
                        }
                    });
            mDlgRemoveConfirmation.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        if (mDlgInitConfirmation == null) {
            mDlgInitConfirmation = new SimpleDialog(SettingLampActivity.this,
                    getString(R.string.dialog_contents_initialize_device),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgInitConfirmation.dismiss();
                        }
                    },
                    getString(R.string.btn_remove),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _initPreference();
                            mConnectionMgr.initDeviceStatusToCloud(mLampDevice.getDeviceInfo());
                            DatabaseManager.getInstance(mContext).deleteNotificationMessages(DeviceType.LAMP, mLampDevice.deviceId);

                            // Need to do initialize before unregister and backpressed
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mDlgInitConfirmation != null && mDlgInitConfirmation.isShowing()) {
                                        try {
                                            mDlgInitConfirmation.dismiss();
                                        } catch(IllegalArgumentException e) {

                                        }
                                    }
                                    DeviceBLEConnection bleDevice = ConnectionManager.getDeviceBLEConnection(mLampDevice.deviceId, DeviceType.LAMP);
                                    if (bleDevice != null) {
                                        ConnectionManager.mRegisteredLampList.remove(mLampDevice.deviceId);
                                        bleDevice.unregister();
                                        ConnectionManager.removeDeviceBLEConnection(mLampDevice.deviceId, DeviceType.LAMP);
                                    }
                                    mConnectionMgr.removeRegisteredDevice(DeviceType.LAMP, mConnectedDeviceId);
                                    onBackPressed();
                                }
                            }, 1000);
                        }
                    });
            mDlgInitConfirmation.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        mDlgFinding = new ProgressCircleDialog(SettingLampActivity.this,
                getString(R.string.dialog_contents_finding_device),
                getString(R.string.btn_close),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgFinding.dismiss();
                    }
                });

        if (btnName == null) {
            btnName = new SettingButton(this);
            btnName.setTitle(getString(R.string.setting_device_name));
            btnName.setDividerForOtherCategory(false);
            btnName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _showSettingView(VIEW_CHANGE_NAME);
                }
            });
            /*
            if (!isOwner) {
                btnName.setBackgroundResource(R.color.colorWhite);
                btnName.showDirection(false);
                btnName.setOnClickListener(null);
            }
            */
        }
        btnName.setContent(mLampDevice.name);

        // AP이름
        if (btnAPInfo == null) {
            btnAPInfo = new SettingButton(this);
            btnAPInfo.setTitle(getString(R.string.setting_ap_info_title));
            btnAPInfo.setDescription(getResources().getString(R.string.setting_ap_info_title_description));
            btnAPInfo.setDividerForOtherCategory(true);
            btnAPInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingLampActivity.this, ConnectionActivity.class);
                    intent.putExtra("startContent", ConnectionActivity.STEP_LAMP_CHANGE_AP);
                    intent.putExtra("targetDeviceId", mLampDevice.deviceId);
                    mContext.startActivity(intent);
                }
            });
            /*
            if (!isOwner) {
                btnAPInfo.setBackgroundResource(R.color.colorWhite);
                btnAPInfo.showDirection(false);
                btnAPInfo.setOnClickListener(null);
            }
            */
        }
        if (mLampDevice.getApName() == null || mLampDevice.getApName().length() == 0) {
            btnAPInfo.setContent(getString(R.string.setting_ap_info_not_connected));
        } else {
            btnAPInfo.setContent(mLampDevice.getApName());
        }

        // LED 상태등 켜기 Switch
        mLedOnLocalTime = mLampDevice.getLedOnTime();
        if (mLedOnLocalTime == null) {
            mLedOnLocalTime = "0000";
        }

        mLedOffLocalTime = mLampDevice.getLedOffTime();
        if (mLedOffLocalTime == null) {
            mLedOffLocalTime = "00000";
        }
        if (mLedOffLocalTime.length() == 4) {
            mLedOffLocalTime += "0";
        }

        // 켜짐시간
        if (btnLEDLightTime == null) {
            btnLEDLightTime = new SettingButton(this);
            btnLEDLightTime.setTitle(getString(R.string.setting_device_led_light_on_time));
            btnLEDLightTime.setDividerForOtherCategory(true);
            btnLEDLightTime.setDepth(2);
            btnLEDLightTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _showSettingView(VIEW_LED_ON_OFF_TIME);
                }
            });
            /*
            if (!isOwner) {
                btnLEDLightTime.setBackgroundResource(R.color.colorWhite);
                btnLEDLightTime.showDirection(false);
                btnLEDLightTime.setOnClickListener(null);
            }
            */
        }

        btnLEDLightTime.setContent(
                mLedOnLocalTime.substring(0, 2) + ":" + mLedOnLocalTime.substring(2, 4) + " ~ " +
                mLedOffLocalTime.substring(0, 2) + ":" + mLedOffLocalTime.substring(2, 4));

        if (btnLedOnOff == null) {
            btnLedOnOff = new SettingButtonSwitch(this);
            btnLedOnOff.setDividerForOtherCategory(false);
            btnLedOnOff.setTitle(getString(R.string.setting_device_led_light));
            btnLedOnOff.setDescription(getResources().getString(R.string.setting_device_led_light_description));
        }
        btnLedOnOff.setOnClickListener(null); // _updateLEDSetting 하기 전에 필요함(Switch를 check하면 checkListener동작)
        if ((mLedOffLocalTime.substring(4).length() == 0) || ("0".equals(mLedOffLocalTime.substring(4)))) {
            _updateLEDSettings(true);
        } else {
            _updateLEDSettings(false);
        }

        /*
        if (!isOwner) {
            btnLedOnOff.setBackgroundResource(R.color.colorWhite);
            btnLedOnOff.setOnClickListener(null);
        } else {
        */
            btnLedOnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = btnLedOnOff.isChecked();
                    if (DBG) Log.d(TAG, "Turn on LED : " + checked);

                    if (mLedOnLocalTime == null) {
                        mLedOnLocalTime = "0000";
                    }

                    if (mLedOffLocalTime == null) {
                        mLedOffLocalTime = "0000";
                    }

                    String ledOnUtcTime = DateTimeUtil.convertLocalToUTCTime(mLedOnLocalTime);
                    String ledOffUtcTime = DateTimeUtil.convertLocalToUTCTime(mLedOffLocalTime) + (checked ? "0" : "1");
                    mLedOffLocalTime = mLedOffLocalTime.substring(0, 4) + (checked ? "0" : "1");

                    if (DBG) Log.d(TAG, "converted button : " + ledOnUtcTime + " ~ " + ledOffUtcTime);

                    mServerQueryMgr.setLedOnOffTime(
                            mLampDevice.type,
                            mLampDevice.deviceId,
                            mLampDevice.getEnc(),
                            ledOnUtcTime,
                            ledOffUtcTime,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        mLampDevice.setLedOnTime(mLedOnLocalTime);
                                        mLampDevice.setLedOffTime(mLedOffLocalTime);
                                    }
                                    if (DBG) Log.d(TAG, "setLedOffTime : " + mLampDevice.getLedOffTime());
                                }
                            });

                    _updateLEDSettings(checked);
                }
            });

        //}

        if (btnTemperature == null) {
            btnTemperature = new SettingButton(this);
            btnTemperature.setTitle(getString(R.string.device_environment_temperature));
            btnTemperature.setContent("");
            btnTemperature.setDescription(getString(R.string.setting_device_environment_temperature_description));
            btnTemperature.setDepth(2);
            btnTemperature.setDividerForOtherCategory(false);
            btnTemperature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _showSettingView(VIEW_TEMPERATURE_ALARM);
                }
            });
            /*
            if (!isOwner) {
                btnTemperature.setBackgroundResource(R.color.colorWhite);
                btnTemperature.showDirection(false);
                btnTemperature.setOnClickListener(null);
            }
            */
        }
        btnTemperature.setContent(
                UnitConvertUtil.getConvertedTemperature(mContext, mLampDevice.getMinTemperature()) + mPreferenceMgr.getTemperatureScale() + " ~ " +
                        UnitConvertUtil.getConvertedTemperature(mContext, mLampDevice.getMaxTemperature()) + mPreferenceMgr.getTemperatureScale());


        if (btnTemperatureUnit == null) {
            btnTemperatureUnit = new SettingButton(this);
            btnTemperatureUnit.setTitle(getString(R.string.device_environment_temperature_unit));
            btnTemperatureUnit.setContent(mPreferenceMgr.getTemperatureScale());
            btnTemperatureUnit.setDividerForOtherCategory(true);

            btnTemperatureUnit.showDirection(true);
            btnTemperatureUnit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String setUnit = "C";
                    if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_celsius))) {
                        mPreferenceMgr.setTemperatureScale(getString(R.string.unit_temperature_fahrenheit));
                        setUnit = "F";
                    } else {
                        mPreferenceMgr.setTemperatureScale(getString(R.string.unit_temperature_celsius));
                    }
                    mServerQueryMgr.setAppInfo(setUnit, new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

                            }
                        }
                    });

                    btnTemperatureUnit.setContent(mPreferenceMgr.getTemperatureScale());
                    btnTemperature.setContent(
                            UnitConvertUtil.getConvertedTemperature(mContext, mLampDevice.getMinTemperature()) + mPreferenceMgr.getTemperatureScale() + " ~ " +
                                    UnitConvertUtil.getConvertedTemperature(mContext, mLampDevice.getMaxTemperature()) + mPreferenceMgr.getTemperatureScale());

                }
            });
        }
        btnTemperatureUnit.setContent(mPreferenceMgr.getTemperatureScale());

        if (btnHumidity == null) {
            btnHumidity = new SettingButton(this);
            btnHumidity.setTitle(getString(R.string.device_environment_humidity));
            btnHumidity.setDescription(getString(R.string.setting_device_environment_humidity_description));
            btnHumidity.setContent("");
            btnHumidity.setDepth(2);
            btnHumidity.setDividerForOtherCategory(true);
            btnHumidity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _showSettingView(VIEW_HUMIDITY_ALARM);
                }
            });
            /*
            if (!isOwner) {
                btnHumidity.setBackgroundResource(R.color.colorWhite);
                btnHumidity.showDirection(false);
                btnHumidity.setOnClickListener(null);
            }
            */
        }
        btnHumidity.setContent(mLampDevice.getMinHumidity() + "% ~ " + mLampDevice.getMaxHumidity() + "%");

        // 연결 알람
        if (btnEnableAlarmConnection == null) {
            btnEnableAlarmConnection = new SettingButtonSwitch(this);
            btnEnableAlarmConnection.setDividerForOtherCategory(false);
            btnEnableAlarmConnection.setDepth(2);
            btnEnableAlarmConnection.setTitle(getString(R.string.setting_device_enable_connection_alarm));
            btnEnableAlarmConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = btnEnableAlarmConnection.isChecked();
                    if (DBG) Log.d(TAG, "Enable AlarmConnection : " + btnEnableAlarmConnection.isChecked());
                }
            });
        }
        btnEnableAlarmConnection.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.LAMP, mLampDevice.deviceId, NotificationType.HUB_DISCONNECTED));

        // VOC 알람
        if (btnEnableAlarmVOC == null) {
            btnEnableAlarmVOC = new SettingButtonSwitch(this);
            btnEnableAlarmVOC.setDividerForOtherCategory(true);
            btnEnableAlarmVOC.setDepth(2);
            btnEnableAlarmVOC.setTitle(getString(R.string.setting_device_enable_voc_alarm));
            btnEnableAlarmVOC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = btnEnableAlarmVOC.isChecked();
                    if (DBG) Log.d(TAG, "Enable AlarmVoc : " + btnEnableAlarmVOC.isChecked());
                    }
            });
        }
        btnEnableAlarmVOC.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.LAMP, mLampDevice.deviceId, NotificationType.VOC_WARNING));

        if (btnEnableAlarm == null) {
            btnEnableAlarm = new SettingButtonSwitch(this);
            btnEnableAlarm.setDividerForOtherCategory(false);
            btnEnableAlarm.setTitle(getString(R.string.setting_device_enable_alarm));
        }

        btnEnableAlarm.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.LAMP, mLampDevice.deviceId, NotificationType.DEVICE_ALL));
        btnEnableAlarm.setOnClickListener(new View.OnClickListener() { // Need to set after setChecked
            @Override
            public void onClick(View v) {
                final boolean checked = btnEnableAlarm.isChecked();
                if (DBG) Log.d(TAG, "Enable Alarm : " + checked);
                _updateAlarmSettings(checked);

                mServerQueryMgr.setDeviceAlarmStatus(mLampDevice.type, mLampDevice.deviceId, NotificationType.DEVICE_ALL, checked,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                    mPreferenceMgr.setDeviceAlarmEnabled(
                                            DeviceType.LAMP,
                                            mLampDevice.deviceId,
                                            NotificationType.DEVICE_ALL,
                                            checked);
                                } else {
                                    if (DBG) Log.d(TAG, "Set alarm status failed");
                                }
                            }
                        });
            }
        });

        if (btnFirmwareVersion == null) {
            btnFirmwareVersion = new SettingButton(this);
            btnFirmwareVersion.setTitle(getString(R.string.setting_device_firmware_version));
            btnFirmwareVersion.setDescription(getResources().getString(R.string.setting_device_firmware_version_description));
            btnFirmwareVersion.showDirection(true);
            btnFirmwareVersion.setDividerForOtherCategory(true);
            btnFirmwareVersion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DBG) Log.d(TAG, "Check connection state: " + mLampDevice.getConnectionState());
                    if (mLampDevice.getConnectionState() == DeviceConnectionState.BLE_CONNECTED && mLampDevice.getServerDirectConnectionState() == false) {
                        if (mDlgDfuError == null) {
                            mDlgDfuError = new SimpleDialog(SettingLampActivity.this,
                                    getString(R.string.setting_device_firmware_lamp_internet),
                                    getString(R.string.btn_ok),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mDlgDfuError.dismiss();
                                        }
                                    });
                        }
                        mDlgDfuError.setContentsGravity(Gravity.LEFT);

                        if (mDlgDfuError != null && !mDlgDfuError.isShowing()) {
                            mDlgDfuError.show();
                        }
                        return;
                    }

                    Intent intent = new Intent(SettingLampActivity.this, LampFirmwareUpdateActivity.class);
                    intent.putExtra("targetDeviceId", mLampDevice.deviceId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
        }
        btnFirmwareVersion.setContent(mLampDevice.firmwareVersion + " / " + mPreferenceMgr.getLampVersion());
        if (new VersionManager(mContext).checkUpdateAvailable(mLampDevice.firmwareVersion, mPreferenceMgr.getLampVersion())) {
            btnFirmwareVersion.showNewMark(true);
        } else {
            btnFirmwareVersion.showNewMark(false);
        }

        if (btnSerialNumber == null) {
            btnSerialNumber = new SettingButton(this);
            btnSerialNumber.setTitle(getString(R.string.setting_device_serial_number));
            btnSerialNumber.setDividerForOtherCategory(true);
            btnSerialNumber.setContent(mLampDevice.serial);
            btnSerialNumber.showDirection(false);
            btnSerialNumber.setOnClickListener(null);
        }

        if (btnInit == null) {
            btnInit = new SettingButtonWarning(this);
            btnInit.setWarning(getString(R.string.setting_device_initialize));
            btnInit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDlgInitConfirmation != null) {
                        try {
                            mDlgInitConfirmation.show();
                        } catch (Exception e) {

                        }
                    }
                }
            });
        }

        if (btnRemove == null) {
            btnRemove = new SettingButtonWarning(this);
            btnRemove.setWarning(getString(R.string.setting_device_remove));
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDlgRemoveConfirmation != null) {
                        try {
                            mDlgRemoveConfirmation.show();
                        } catch (Exception e) {

                        }
                    }
                }
            });
        }
    }

    /*
    public void showOwnerToast() {
        showToast("Owner can set the values");
    }
    */

    public void setConnected(boolean connected) {
        isConnected = connected;
        if (btnName == null) {
            return;
        }
        if (connected) {
            btnName.setEnabled(true);
            btnAPInfo.setEnabled(true);
            btnTemperatureUnit.setEnabled(true);
            btnEnableAlarm.setEnabled(true);
            btnTemperature.setEnabled(true);
            btnHumidity.setEnabled(true);
            btnLedOnOff.setEnabled(true);
            btnLEDLightTime.setEnabled(true);
            btnFirmwareVersion.setEnabled(true);
            btnFirmwareVersion.setContent(mLampDevice.firmwareVersion + " / " + mPreferenceMgr.getLampVersion());
            if (new VersionManager(mContext).checkUpdateAvailable(mLampDevice.firmwareVersion, mPreferenceMgr.getLampVersion())) {
                btnFirmwareVersion.showNewMark(true);
            } else {
                btnFirmwareVersion.showNewMark(false);
            }
            btnAPInfo.setContent(mLampDevice.getApName());
            btnSerialNumber.setEnabled(true);
        } else {
            /*
            if (isOwner) {

            } else {

            }
            */

            btnName.setEnabled(false);
            btnAPInfo.setEnabled(true);
            btnTemperatureUnit.setEnabled(false);
            btnEnableAlarm.setEnabled(false);
            btnTemperature.setEnabled(false);
            btnHumidity.setEnabled(false);
            btnLedOnOff.setEnabled(false);
            btnLEDLightTime.setEnabled(false);
            btnFirmwareVersion.setEnabled(false);
            btnFirmwareVersion.setOnClickListener(null);
            btnFirmwareVersion.showNewMark(false);
            btnAPInfo.setContent(getString(R.string.setting_ap_info_not_connected));
            btnSerialNumber.setEnabled(false);
        }

        //btnEnableAlarmConnection.setEnabled(isConnected);
        //btnEnableAlarmVOC.setEnabled(isConnected);
    }

    private void _hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (etName != null) {
            imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ConnectionManager.MSG_WIFI_CONNECTION_STATE_CHANGE:
                    final int wifiConnectionState = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_WIFI_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "/" + mConnectedDeviceId + "] " + wifiConnectionState + " / ");
                    if (mConnectedDeviceId == deviceInfo.deviceId) {
                        if (wifiConnectionState == DeviceConnectionState.WIFI_CONNECTED) {
                            _showSettingView(VIEW_MAIN);
                            setConnected(true);
                        } else {
                            _showSettingView(VIEW_MAIN);
                            setConnected(false);
                        }
                    }
                    break;

                case ConnectionManager.MSG_LAMP_VALUE_UPDATED:
                    final String deviceId2 = msg.arg1 + "";
                    final CurrentLampValue currLampValue = (CurrentLampValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_LAMP_VALUE_UPDATED : [" + deviceId2 + "] " + currLampValue.toString());
                    break;
                case MSG_UPDATE_FIRMWARE_VERSION:
                    if (DBG) Log.d(TAG, "MSG_UPDATE_FIRMWARE_VERSION");
                    if (mConnectionMgr != null) {
                        mConnectionMgr.getUserInfoFromCloud();
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_VIEW_VALUE, 5 * 1000L);
                    break;

                case MSG_UPDATE_VIEW_VALUE:
                    if (DBG) Log.d(TAG, "MSG_UPDATE_VIEW_VALUE : " + mLampDevice.firmwareVersion);
                    btnFirmwareVersion.setContent(mLampDevice.firmwareVersion);
                    showToast(getString(R.string.toast_sharing_member_renewed));
                    break;

                case MSG_SHOW_OTA_UPDATE_DIALOG:
                    if (mDlgOTAUpdate == null) {
                        mDlgOTAUpdate = new ProgressCircleDialog(SettingLampActivity.this,
                                getString(R.string.dialog_contents_update_available_ota_updating),
                                getString(R.string.btn_ok),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mHandler.removeMessages(MSG_UPDATE_VIEW_VALUE);
                                        mHandler.removeMessages(MSG_UPDATE_FIRMWARE_VERSION);
                                        mConnectionMgr.getUserInfoFromCloud();
                                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_VIEW_VALUE, 3 * 1000L);
                                        mDlgOTAUpdate.dismiss();
                                    }
                                });
                    }

                    if (mDlgOTAUpdate != null && !mDlgOTAUpdate.isShowing()) {
                        try {
                            mDlgOTAUpdate.show();
                        } catch (Exception e) {

                        }
                    }
                    break;

                case MSG_SHOW_VIEW:
                    int viewIndex = msg.arg1;
                    mCurrentViewIndex = viewIndex;
                    switch (viewIndex) {
                        case VIEW_MAIN:
                            _showSettingMainView();
                            break;
                        case VIEW_CHANGE_NAME:
                            _showSettingChangeName();
                            break;
                        case VIEW_TEMPERATURE_ALARM:
                            _showTemperatureAlarmView();
                            break;
                        case VIEW_HUMIDITY_ALARM:
                            _showHumidityAlarmView();
                            break;
                        case VIEW_LED_ON_OFF_TIME:
                            _showLEDOnTimeSettingView();
                            break;
                    }
            }
        }
    };
}
