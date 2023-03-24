package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dfu.FirmwareUpdateActivity;
import goodmonit.monit.com.kao.dialog.ProgressCircleDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.widget.SettingButton;
import goodmonit.monit.com.kao.widget.SettingButtonSwitch;
import goodmonit.monit.com.kao.widget.SettingButtonWarning;
import goodmonit.monit.com.kao.widget.SettingEditText;
import goodmonit.monit.com.kao.widget.ValidationBirthdayYYMMDD;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationImageButtons;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class SettingSensorActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "SettingSensor";
    private static final boolean DBG = Configuration.DBG;

    private static final int VIEW_MAIN              = 1;
    private static final int VIEW_CHANGE_NAME       = 2;
    private static final int VIEW_CHANGE_BABY_INFO  = 3;

    private static final int MSG_CLOSE_FINDING_DEVICE_DIALOG   = 1;
    private static final int MSG_UPDATE_SENSITIVITY_VIEW       = 2;
    private static final int MSG_SHOW_VIEW                     = 3;
    private static final int MSG_CLOSE_DETECTION_TEST_DIALOG   = 4;

    private static final int REQCODE_BLE_DIRECT_CONNECTION  = 1;

    private int mCurrentViewIndex;

    private LinearLayout lctnButtonList;
    private SettingButton btnFind, btnFirmwareVersion, btnBabyInfo, btnPooSensitivity, btnSerialNumber;
    private SettingButton btnDetectionTest, btnPeeFakeAlarm, btnPooFakeAlarm, btnFartFakeAlarm, btnDebugCommand;

    private SettingButtonSwitch btnEnableAlarm, btnEnableAlarmConnection, btnEnableAlarmPee, btnEnableAlarmPoo, btnEnableAlarmFart, btnEnableAlarmMovementDetection, btnEnableAlarmDiaperCheck, btnEnableAlarmDiaperSoiled, btnEnableSleepAutoDetection;
    private SettingButtonWarning btnRemove, btnInit;

    private View vBabyInfo;
    private ValidationEditText vetBabyName;
    private ValidationRadio vrBabySex;
    private ValidationBirthdayYYMMDD vtvBabyBirthday;
    private ValidationManager mValidationMgr;
    private ValidationImageButtons vibBabyEating;

    private SettingEditText etName;

    private long mConnectedSensorDeviceId;
    private DeviceDiaperSensor mMonitSensor;

    private SimpleDialog mDlgRemoveConfirmation, mDlgInitConfirmation;
    private ProgressCircleDialog mDlgFinding, mDlgDetectionTest;
    private SimpleDialog mDlgDebugCommand;

    private boolean isConnected;

    private PreferenceManager mPreferenceMgr;
    private VersionManager mVersionMgr;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        _setToolBar();

        mContext = this;
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mValidationMgr = new ValidationManager(this);
        mVersionMgr = new VersionManager(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mScreenInfo = new ScreenInfo(1001);
        _setMonitSensor();

        _initView();

        if (mMonitSensor != null && (mMonitSensor.cloudId == mPreferenceMgr.getAccountId())) {
            if (DBG) Log.d(TAG, "Owner");
            isOwner = true;
        } else {
            isOwner = false;
        }

        mCurrentViewIndex = VIEW_MAIN;
        _showSettingMainView();
        //_showSettingView(VIEW_MAIN);
    }

    private void _setMonitSensor() {
        mConnectedSensorDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        mMonitSensor = ConnectionManager.getDeviceDiaperSensor(mConnectedSensorDeviceId);
        if (mMonitSensor != null) {
            if (DBG) Log.d(TAG, "targetDevice : [" + mMonitSensor.deviceId + "] " + mMonitSensor.name);
        } else {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedSensorDeviceId);
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
        if (mMonitSensor == null) {
            _setMonitSensor();
        }
        if ((mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) ||
                (mMonitSensor.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED)) {
            setConnected(true);
        } else {
            setConnected(false);
        }
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getString(R.string.title_setting));

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        btnToolbarRight.setVisibility(View.GONE);

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

    private void _showSettingChangeBabyInfo() {
        tvToolbarTitle.setText(getString(R.string.setting_device_babyinfo));
        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vetBabyName.isValid()) {
                    vetBabyName.showWarning(true, 1000);
                }
                if (!vrBabySex.isValid()) {
                    vrBabySex.showWarning(true, 1000);
                }
                if (!vtvBabyBirthday.isValid()) {
                    vtvBabyBirthday.showWarning(true, 1000);
                }
                if (!vibBabyEating.isValid()) {
                    vibBabyEating.showWarning(true, 1000);
                }

                if (vetBabyName.isValid() && vrBabySex.isValid() && vtvBabyBirthday.isValid() && vibBabyEating.isValid()) {
                    final String babyName = vetBabyName.getText();
                    String tempBirthdayYYMMDD = vtvBabyBirthday.getSelectedDateStringYYMMDD();
                    final int babySex = (vrBabySex.getSelectedRadioIndex() == 1 ? 1 : 0);
                    final int babyEating = vibBabyEating.getSelectedImageButtonIdx();

                    if (tempBirthdayYYMMDD == null) {
                        tempBirthdayYYMMDD = "180101";
                    }

                    final String babyBirthdayYYMMDD = tempBirthdayYYMMDD;

                    showProgressBar(true);
                    ServerQueryManager.getInstance(mContext).setBabyInfo(
                            mMonitSensor.type,
                            mMonitSensor.deviceId,
                            mMonitSensor.getEnc(),
                            babyName,
                            babyBirthdayYYMMDD,
                            babySex,
                            babyEating,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    showProgressBar(false);
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type) != null) {
                                            ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type).setBabyInfo(babyName, babyBirthdayYYMMDD, babySex, babyEating);
                                        }
                                        _showSettingView(VIEW_MAIN);
                                        mPreferenceMgr.setDeviceName(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, babyName);
                                        showToast(getString(R.string.toast_change_baby_info_succeeded));
                                    } else {
                                        showToast(getString(R.string.toast_change_baby_info_failed));
                                    }
                                }
                            });
                }
            }
        });
        _initSettingChangeBabyInfo();

        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(vBabyInfo);
    }

    private void _showSettingChangeName() {
        tvToolbarTitle.setText(getString(R.string.setting_device_name) + " " + getString(R.string.title_setting));
        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingSensorActivity.this, getString(R.string.toast_change_device_name_succeeded), Toast.LENGTH_SHORT).show();
                mMonitSensor.name = etName.getText();
                _showSettingView(VIEW_MAIN);
            }
        });
        _initSettingChangeName();

        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(etName);
    }

    private void _showSettingMainView() {
        tvToolbarTitle.setText(getString(R.string.title_setting));
        btnToolbarRight.setVisibility(View.GONE);

        _initSettingMain();
        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnBabyInfo);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnEnableAlarm);
//        if (Configuration.BETA_TEST_MODE || Configuration.B2B_MODE || Configuration.MASTER || Configuration.DEVELOPER) {
//            lctnButtonList.addView(btnEnableAlarmPee);
//            lctnButtonList.addView(btnEnableAlarmPoo);
//            lctnButtonList.addView(btnEnableAlarmFart);
//        }

        lctnButtonList.addView(btnEnableAlarmDiaperSoiled);
        lctnButtonList.addView(btnEnableAlarmDiaperCheck);
        lctnButtonList.addView(btnEnableAlarmMovementDetection);
        lctnButtonList.addView(btnEnableAlarmConnection);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));

        if (Configuration.MONIT_AUTO_SLEEP_DETECTION) {
            lctnButtonList.addView(btnEnableSleepAutoDetection);
            lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        }

        lctnButtonList.addView(btnFind);
        lctnButtonList.addView(btnDetectionTest);
        if (Configuration.MASTER) {
            lctnButtonList.addView(btnDebugCommand);
        }

        if (Configuration.DEVELOPER || Configuration.MASTER) {
            lctnButtonList.addView(btnPeeFakeAlarm);
            lctnButtonList.addView(btnPooFakeAlarm);
            lctnButtonList.addView(btnFartFakeAlarm);
        }

        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnFirmwareVersion);
        lctnButtonList.addView(btnSerialNumber);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));

        if (isOwner) {
            lctnButtonList.addView(btnInit);
        } else {
            lctnButtonList.addView(btnRemove);
            // 주인이 아니어도 BLE다이렉트로 연결되어 있으면 초기화 가능
            if ((mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED)) {
                    //&& mMonitSensor.hasBleConnected) {
                lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
                lctnButtonList.addView(btnInit);
            }
        }
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider_footer, null));

        _updateAlarmSettings(mPreferenceMgr.getDeviceAlarmEnabled(
                DeviceType.DIAPER_SENSOR,
                mMonitSensor.deviceId,
                NotificationType.DEVICE_ALL));

        _hideKeyboard();
    }

    private void _updateAlarmSettings(boolean checked) {
        if (checked) {
            btnEnableAlarmPee.setVisibility(View.VISIBLE);
            btnEnableAlarmPoo.setVisibility(View.VISIBLE);
            btnEnableAlarmFart.setVisibility(View.VISIBLE);
            btnEnableAlarmDiaperSoiled.setVisibility(View.VISIBLE);
            btnEnableAlarmDiaperCheck.setVisibility(View.VISIBLE);
            btnEnableAlarmConnection.setVisibility(View.VISIBLE);
            btnEnableAlarmMovementDetection.setVisibility(View.VISIBLE);
            btnEnableAlarm.setDividerForOtherCategory(false);
        } else {
            btnEnableAlarmPee.setVisibility(View.GONE);
            btnEnableAlarmPoo.setVisibility(View.GONE);
            btnEnableAlarmFart.setVisibility(View.GONE);
            btnEnableAlarmDiaperSoiled.setVisibility(View.GONE);
            btnEnableAlarmDiaperCheck.setVisibility(View.GONE);
            btnEnableAlarmConnection.setVisibility(View.GONE);
            btnEnableAlarmMovementDetection.setVisibility(View.GONE);
            btnEnableAlarm.setDividerForOtherCategory(true);
        }
    }

    private void _initSettingChangeName() {
        if (etName == null) {
            etName = new SettingEditText(this);
        }
        etName.setTitle(getString(R.string.setting_device_name));
        etName.setText(mMonitSensor.name);
    }

    private void _initSettingChangeBabyInfo() {
        if (DBG) Log.d(TAG, "_initSettingChangeBabyInfo : " + mMonitSensor.name + " / " + mMonitSensor.getBabyBirthdayYYMMDD() + " / " + mMonitSensor.getBabySex());
        if (vBabyInfo == null) {
            vBabyInfo = getLayoutInflater().inflate(R.layout.widget_input_babyinfo, null);
        }

        if (vetBabyName == null) {
            vetBabyName = (ValidationEditText) vBabyInfo.findViewById(R.id.vet_input_baby_info_babyname);
            vetBabyName.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    vetBabyName.setValid(mValidationMgr.isValidBabyname(vetBabyName.getText()));
                }
            });
        }
        vetBabyName.setText(mMonitSensor.name);
        vetBabyName.setValid(true);

        if (vrBabySex == null) {
            vrBabySex = (ValidationRadio) vBabyInfo.findViewById(R.id.vr_input_baby_info_babysex);
            vrBabySex.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    vrBabySex.setValid(true);
                }
            });
            vrBabySex.addOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _hideKeyboard();
                }
            });
        }
        vrBabySex.selectItem(mMonitSensor.getBabySex() == 1 ? 1 : 2);
        vrBabySex.setValid(true);

        if (vtvBabyBirthday == null) {
            vtvBabyBirthday = (ValidationBirthdayYYMMDD) vBabyInfo.findViewById(R.id.vtv_input_baby_info_babybday);
            vtvBabyBirthday.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    vtvBabyBirthday.setValid(true);
                }
            });
            vtvBabyBirthday.setBirthdayFromYear(2000);
            vtvBabyBirthday.addOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _hideKeyboard();
                }
            });
        }
        vtvBabyBirthday.showDay(false);
        vtvBabyBirthday.setBirthDayYYMMDD(mMonitSensor.getBabyBirthdayYYMMDD());
        vtvBabyBirthday.showWheelPicker(true);
        vtvBabyBirthday.setValid(true);

        if (vibBabyEating == null) {
            vibBabyEating = (ValidationImageButtons) vBabyInfo.findViewById(R.id.vib_input_baby_info_eating);
            vibBabyEating.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    if (vibBabyEating.getSelectedImageButtonIdx() == 0) {
                        vibBabyEating.setValid(false);
                    } else {
                        vibBabyEating.setValid(true);
                    }
                }
            });
        }
        vibBabyEating.selectItem(mMonitSensor.getBabyEating());
        if (mMonitSensor.getBabyEating() == 0) {
            vibBabyEating.setValid(false);
        } else {
            vibBabyEating.setValid(true);
        }
    }

    private void _initSettingMain() {
        if (mDlgRemoveConfirmation == null) {
            mDlgRemoveConfirmation = new SimpleDialog(SettingSensorActivity.this,
                    getString(R.string.dialog_contents_remove_device),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mDlgRemoveConfirmation != null && mDlgRemoveConfirmation.isShowing()) {
                                try {
                                    mDlgRemoveConfirmation.dismiss();
                                } catch(IllegalArgumentException e) {

                                }
                            }
                        }
                    },
                    getString(R.string.btn_remove),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 그룹탈퇴
                            String targetEmailAddress = UserInfoManager.getInstance(mContext).getEmailAddress(mMonitSensor.cloudId);
                            if (mDlgRemoveConfirmation != null && mDlgRemoveConfirmation.isShowing()) {
                                try {
                                    mDlgRemoveConfirmation.dismiss();
                                } catch(IllegalArgumentException e) {

                                }
                            }
                            showProgressBar(true);
                            mServerQueryMgr.leaveCloud(mMonitSensor.cloudId, new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    showProgressBar(false);
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        showToast(getString(R.string.toast_leave_group_succeeded));
                                    } else {
                                        showToast(getString(R.string.toast_leave_group_failed));
                                    }

                                    mPreferenceMgr.initDiaperSensorPreference(mMonitSensor.deviceId);
                                    ConnectionManager.removeDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type);
                                    mConnectionMgr.leaveGroup(mMonitSensor.cloudId);
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
            mDlgInitConfirmation = new SimpleDialog(SettingSensorActivity.this,
                    getString(R.string.dialog_contents_initialize_device),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mDlgInitConfirmation != null && mDlgInitConfirmation.isShowing()) {
                                try {
                                    mDlgInitConfirmation.dismiss();
                                } catch(IllegalArgumentException e) {

                                }
                            }
                        }
                    },
                    getString(R.string.btn_remove),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, DeviceType.DIAPER_SENSOR) != null) {
                                ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, DeviceType.DIAPER_SENSOR).initialize();
                            }
                            mPreferenceMgr.initDiaperSensorPreference(mMonitSensor.deviceId);
                            mConnectionMgr.initDeviceStatusToCloud(mMonitSensor.getDeviceInfo());
                            DatabaseManager.getInstance(mContext).deleteNotificationMessages(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);
                            DatabaseManager.getInstance(mContext).deleteMovementGraphInfoDB(mMonitSensor.deviceId);

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
                                    onBackPressed();
                                }
                            }, 1000);
                        }
                    });
            mDlgInitConfirmation.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        mDlgDetectionTest = new ProgressCircleDialog(SettingSensorActivity.this,
                getString(R.string.setting_device_detection_test_mode_description),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConnectionMgr.setFastDetectionAlarm(false);
                        if (mDlgDetectionTest != null && mDlgDetectionTest.isShowing()) {
                            try {
                                mDlgDetectionTest.dismiss();
                            } catch(IllegalArgumentException e) {

                            }
                        }
                    }
                });

        mDlgFinding = new ProgressCircleDialog(SettingSensorActivity.this,
                getString(R.string.dialog_contents_finding_device),
                getString(R.string.btn_close),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDlgFinding != null && mDlgFinding.isShowing()) {
                            try {
                                mDlgFinding.dismiss();
                            } catch(IllegalArgumentException e) {

                            }
                        }
                    }
                });

        if (btnBabyInfo == null) {
            btnBabyInfo = new SettingButton(this);
            btnBabyInfo.setTitle(getString(R.string.setting_device_babyinfo));
            btnBabyInfo.setDescription(getString(R.string.setting_device_babyinfo_description));
            btnBabyInfo.setDividerForOtherCategory(true);
                btnBabyInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _showSettingView(VIEW_CHANGE_BABY_INFO);
                    }
                });
        }
        btnBabyInfo.setContent(mMonitSensor.name);

        if (btnFind == null) {
            btnFind = new SettingButton(this);
            btnFind.setTitle(getString(R.string.setting_device_find));
            btnFind.setContent("");
            btnFind.setDividerForOtherCategory(true);
            btnFind.showDirection(true);
            btnFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                        if (mDlgFinding != null) {
                            try {
                                mDlgFinding.show();
                            } catch (Exception e) {

                            }
                            mHandler.sendEmptyMessageDelayed(MSG_CLOSE_FINDING_DEVICE_DIALOG, 5000);
                            if (ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type) != null) {
                                ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type).blink();
                            }
                        }
                    } else {
                        Intent intent = new Intent(SettingSensorActivity.this, GuideDirectConnectionActivity.class);
                        intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
                        startActivityForResult(intent, REQCODE_BLE_DIRECT_CONNECTION);
                        overridePendingTransition(0, 0);
                    }
                }
            });
        }

        if (btnDetectionTest == null) {
            btnDetectionTest = new SettingButton(this);
            btnDetectionTest.setTitle(getString(R.string.setting_device_detection_test_mode_title));
            btnDetectionTest.setContent("");
            btnDetectionTest.setDividerForOtherCategory(true);
            btnDetectionTest.showDirection(true);
            btnDetectionTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                        if (mDlgDetectionTest != null) {
                            try {
                                mDlgDetectionTest.show();
                            } catch (Exception e) {

                            }
                            mHandler.removeMessages(MSG_CLOSE_DETECTION_TEST_DIALOG);
                            mHandler.sendEmptyMessageDelayed(MSG_CLOSE_DETECTION_TEST_DIALOG, 60 * 1000);
                        }
                        if (mConnectionMgr != null) {
                            mConnectionMgr.setFastDetectionAlarm(true);
                        }
                    } else {
                        Intent intent = new Intent(SettingSensorActivity.this, GuideDirectConnectionActivity.class);
                        intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
                        startActivityForResult(intent, REQCODE_BLE_DIRECT_CONNECTION);
                        overridePendingTransition(0, 0);
                    }
                }
            });
        }

        if (btnPeeFakeAlarm == null) {
            btnPeeFakeAlarm = new SettingButton(this);
            btnPeeFakeAlarm.setTitle("Fake Pee Alert");
            btnPeeFakeAlarm.setContent("");
            btnPeeFakeAlarm.setDividerForOtherCategory(true);
            btnPeeFakeAlarm.showDirection(true);
            btnPeeFakeAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectionMgr != null) {
                        long utcTimeMs = System.currentTimeMillis();
                        mMonitSensor.setDiaperStatus(NotificationType.PEE_DETECTED, utcTimeMs);
                        mConnectionMgr.sendFakeAlert(
                                NotificationType.PEE_DETECTED,
                                DeviceType.DIAPER_SENSOR,
                                mMonitSensor.deviceId,
                                mMonitSensor.getEnc(),
                                utcTimeMs / 1000);
                        mConnectionMgr.setFastDetectionAlarm(true);
                    }
                }
            });
        }

        if (btnPooFakeAlarm == null) {
            btnPooFakeAlarm = new SettingButton(this);
            btnPooFakeAlarm.setTitle("Fake Poo Alert");
            btnPooFakeAlarm.setContent("");
            btnPooFakeAlarm.setDividerForOtherCategory(true);
            btnPooFakeAlarm.showDirection(true);
            btnPooFakeAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectionMgr != null) {
                        long utcTimeMs = System.currentTimeMillis();
                        mMonitSensor.setDiaperStatus(NotificationType.POO_DETECTED, utcTimeMs);
                        mConnectionMgr.sendFakeAlert(
                                NotificationType.POO_DETECTED,
                                DeviceType.DIAPER_SENSOR,
                                mMonitSensor.deviceId,
                                mMonitSensor.getEnc(),
                                utcTimeMs / 1000);
                    }
                }
            });
        }

        if (btnFartFakeAlarm == null) {
            btnFartFakeAlarm = new SettingButton(this);
            btnFartFakeAlarm.setTitle("Fake Fart Alert");
            btnFartFakeAlarm.setContent("");
            btnFartFakeAlarm.setDividerForOtherCategory(true);
            btnFartFakeAlarm.showDirection(true);
            btnFartFakeAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectionMgr != null) {
                        long utcTimeMs = System.currentTimeMillis();
                        mMonitSensor.setDiaperStatus(NotificationType.FART_DETECTED, utcTimeMs);
                        mConnectionMgr.sendFakeAlert(
                                NotificationType.FART_DETECTED,
                                DeviceType.DIAPER_SENSOR,
                                mMonitSensor.deviceId,
                                mMonitSensor.getEnc(),
                                utcTimeMs / 1000);
                    }
                }
            });
        }

        if (btnDebugCommand == null) {
            btnDebugCommand = new SettingButton(this);
            btnDebugCommand.setTitle("Command");
            btnDebugCommand.setContent("");
            btnDebugCommand.setDividerForOtherCategory(true);
            btnDebugCommand.showDirection(true);
            btnDebugCommand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                        if (mDlgDebugCommand == null) {
                            mDlgDebugCommand = new SimpleDialog(mContext,
                                    "Command",
                                    getString(R.string.btn_cancel),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mDlgDebugCommand.dismiss();
                                        }
                                    },
                                    getString(R.string.btn_ok),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String msg = mDlgDebugCommand.getInputText();
                                            if (msg.length() >= 1 && msg.length() <= 16) {
                                                if (ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type) != null) {
                                                    ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, mMonitSensor.type).sendDebugCommand(msg);
                                                }
                                                mDlgDebugCommand.dismiss();
                                            } else {
                                                showToast("Invalid length");
                                            }
                                        }
                                    });
                            mDlgDebugCommand.setInputMode(true);
                        }

                        try {
                            mDlgDebugCommand.show();
                        } catch (Exception e) {

                        }
                    } else {
                        Intent intent = new Intent(SettingSensorActivity.this, GuideDirectConnectionActivity.class);
                        intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
                        startActivityForResult(intent, REQCODE_BLE_DIRECT_CONNECTION);
                        overridePendingTransition(0, 0);
                    }
                }
            });
        }

        if (btnEnableSleepAutoDetection == null) {
            btnEnableSleepAutoDetection = new SettingButtonSwitch(this);
            btnEnableSleepAutoDetection.setDividerForOtherCategory(true);
            btnEnableSleepAutoDetection.setTitle(getString(R.string.setting_device_enable_auto_sleep_monitoring));
            btnEnableSleepAutoDetection.setChecked(mPreferenceMgr.getAutoSleepingDetectionEnabled(mMonitSensor.deviceId));
            btnEnableSleepAutoDetection.setOnClickListener(new View.OnClickListener() { // Need to set after setChecked
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableSleepAutoDetection.isChecked();
                    mPreferenceMgr.setAutoSleepingDetectionEnabled(mMonitSensor.deviceId, checked);
                    if (DBG) Log.d(TAG, "Enable Auto Sleep Detection : " + checked);

                    mServerQueryMgr.setDeviceAlarmStatusCommon(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.DIAPER_AUTO_SLLEP_MONITOR, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    Log.d(TAG, "error code : " + errCode);
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });

                }
            });
        } else {
            btnEnableSleepAutoDetection.setChecked(mPreferenceMgr.getAutoSleepingDetectionEnabled(mMonitSensor.deviceId));
        }

        if (btnEnableAlarm == null) {
            btnEnableAlarm = new SettingButtonSwitch(this);
            btnEnableAlarm.setDividerForOtherCategory(true);
            btnEnableAlarm.setTitle(getString(R.string.setting_device_enable_alarm));
            btnEnableAlarm.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.DEVICE_ALL));
            btnEnableAlarm.setOnClickListener(new View.OnClickListener() { // Need to set after setChecked
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarm.isChecked();
                    if (DBG) Log.d(TAG, "Enable Alarm : " + checked);
                    _updateAlarmSettings(checked);

                    mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.DEVICE_ALL, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        FirebaseAnalyticsManager.getInstance(mContext).sendSensorSettingAlarmEnabled(mMonitSensor.deviceId, checked);
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.DEVICE_ALL,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarm.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.DEVICE_ALL));
        }

        if (btnEnableAlarmDiaperCheck == null) {
            btnEnableAlarmDiaperCheck = new SettingButtonSwitch(this);
            btnEnableAlarmDiaperCheck.setDividerForOtherCategory(false);
            btnEnableAlarmDiaperCheck.setDepth(2);
            btnEnableAlarmDiaperCheck.setTitle(getString(R.string.setting_device_enable_diaper_check_alarm));
            btnEnableAlarmDiaperCheck.setDescription(getString(R.string.setting_device_enable_diaper_check_alarm_description));
            btnEnableAlarmDiaperCheck.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE));
            btnEnableAlarmDiaperCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmDiaperCheck.isChecked();
                    if (DBG) Log.d(TAG, "Enable DiaperCheck Alarm : " + checked);
                    //_updateAlarmSettings(checked);

                    mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.DIAPER_NEED_TO_CHANGE,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmDiaperCheck.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE));
        }

        if (btnEnableAlarmDiaperSoiled == null) {
            btnEnableAlarmDiaperSoiled = new SettingButtonSwitch(this);
            btnEnableAlarmDiaperSoiled.setDividerForOtherCategory(false);
            btnEnableAlarmDiaperSoiled.setDepth(2);
            btnEnableAlarmDiaperSoiled.setTitle(getString(R.string.setting_device_enable_diaper_soiled_alarm));
            btnEnableAlarmDiaperSoiled.setDescription(getString(R.string.setting_device_enable_diaper_soiled_alarm_description));
            btnEnableAlarmDiaperSoiled.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.DIAPER_SOILED));
            btnEnableAlarmDiaperSoiled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmDiaperSoiled.isChecked();
                    if (DBG) Log.d(TAG, "Enable DiaperCheck Alarm : " + checked);
                    //_updateAlarmSettings(checked);

                    mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.DIAPER_SOILED, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.DIAPER_SOILED,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmDiaperSoiled.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.DIAPER_SOILED));
        }

        if (btnEnableAlarmPee == null) {
            btnEnableAlarmPee = new SettingButtonSwitch(this);
            btnEnableAlarmPee.setDividerForOtherCategory(false);
            btnEnableAlarmPee.setDepth(2);
            btnEnableAlarmPee.setTitle(getString(R.string.device_sensor_diaper_status_pee));
            btnEnableAlarmPee.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.PEE_DETECTED));
            btnEnableAlarmPee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmPee.isChecked();
                    if (DBG) Log.d(TAG, "Enable Pee Alarm : " + checked);
                    //_updateAlarmSettings(checked);

                    mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.PEE_DETECTED, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        FirebaseAnalyticsManager.getInstance(mContext).sendSensorSettingPeeAlarmEnabled(mMonitSensor.deviceId, checked);
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.PEE_DETECTED,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmPee.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.PEE_DETECTED));
        }

        if (btnEnableAlarmPoo == null) {
            btnEnableAlarmPoo = new SettingButtonSwitch(this);
            btnEnableAlarmPoo.setDividerForOtherCategory(false);
            btnEnableAlarmPoo.setDepth(2);
            btnEnableAlarmPoo.setTitle(getString(R.string.device_sensor_diaper_status_poo));
            btnEnableAlarmPoo.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.POO_DETECTED));
            btnEnableAlarmPoo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmPoo.isChecked();
                    if (DBG) Log.d(TAG, "Enable Poo Alarm : " + checked);
                    //_updateAlarmSettings(checked);

                    mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.POO_DETECTED, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        FirebaseAnalyticsManager.getInstance(mContext).sendSensorSettingPooAlarmEnabled(mMonitSensor.deviceId, checked);
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.POO_DETECTED,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmPoo.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.POO_DETECTED));
        }

        if (btnEnableAlarmFart == null) {
            btnEnableAlarmFart = new SettingButtonSwitch(this);
            btnEnableAlarmFart.setDividerForOtherCategory(false);
            btnEnableAlarmFart.setDepth(2);
            btnEnableAlarmFart.setTitle(getString(R.string.device_sensor_diaper_status_fart));
            btnEnableAlarmFart.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.FART_DETECTED));
            btnEnableAlarmFart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmFart.isChecked();
                    if (DBG) Log.d(TAG, "Enable Fart Alarm : " + checked);
                    //_updateAlarmSettings(checked);

                    mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.FART_DETECTED, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        FirebaseAnalyticsManager.getInstance(mContext).sendSensorSettingFartAlarmEnabled(mMonitSensor.deviceId, checked);
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.FART_DETECTED,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmFart.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.FART_DETECTED));
        }

        if (btnEnableAlarmMovementDetection == null) {
            btnEnableAlarmMovementDetection = new SettingButtonSwitch(this);
            btnEnableAlarmMovementDetection.setDividerForOtherCategory(false);
            btnEnableAlarmMovementDetection.setDepth(2);
            btnEnableAlarmMovementDetection.setTitle(getString(R.string.device_sensor_movement_during_sleep));
            btnEnableAlarmMovementDetection.setDescription(getString(R.string.setting_device_sensor_movement_during_sleep_description));
            btnEnableAlarmMovementDetection.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.MOVEMENT_DETECTED));
            btnEnableAlarmMovementDetection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmMovementDetection.isChecked();
                    if (DBG) Log.d(TAG, "Enable Movement Alarm : " + checked);

                    mPreferenceMgr.setDeviceAlarmEnabled(
                            DeviceType.DIAPER_SENSOR,
                            mMonitSensor.deviceId,
                            NotificationType.MOVEMENT_DETECTED,
                            checked);
                    mServerQueryMgr.setDeviceAlarmStatusCommon(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.MOVEMENT_DETECTED, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.POO_DETECTED,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmMovementDetection.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.MOVEMENT_DETECTED));
        }

        if (btnEnableAlarmConnection == null) {
            btnEnableAlarmConnection = new SettingButtonSwitch(this);
            btnEnableAlarmConnection.setDividerForOtherCategory(true);
            btnEnableAlarmConnection.setDepth(2);
            btnEnableAlarmConnection.setTitle(getString(R.string.setting_device_enable_connection_alarm));
            btnEnableAlarmConnection.setDescription(getString(R.string.setting_device_enable_connection_alarm_description));
            btnEnableAlarmConnection.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED));
            btnEnableAlarmConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean checked = btnEnableAlarmConnection.isChecked();
                    if (DBG) Log.d(TAG, "Enable Connection Alarm : " + checked);

                    mServerQueryMgr.setDeviceAlarmStatus(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, checked,
                            new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                        FirebaseAnalyticsManager.getInstance(mContext).sendSensorSettingConnectionAlarmEnabled(mMonitSensor.deviceId, checked);
                                        mPreferenceMgr.setDeviceAlarmEnabled(
                                                DeviceType.DIAPER_SENSOR,
                                                mMonitSensor.deviceId,
                                                NotificationType.SENSOR_LONG_DISCONNECTED,
                                                checked);
                                    } else {
                                        if (DBG) Log.d(TAG, "Set alarm status failed");
                                    }
                                }
                            });
                }
            });
        } else {
            btnEnableAlarmConnection.setChecked(mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED));
        }

        if (btnPooSensitivity == null) {
            btnPooSensitivity = new SettingButton(this);
            btnPooSensitivity.setTitle(getString(R.string.sensitivity_title));
            btnPooSensitivity.setDividerForOtherCategory(true);
            btnPooSensitivity.showDirection(true);
            btnPooSensitivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                        int tempSensitivity = mMonitSensor.getSensitivity() % 10;
                        switch (tempSensitivity) {
                            case DeviceStatus.SENSITIVITY_LOWEST:
                                tempSensitivity = DeviceStatus.SENSITIVITY_NORMAL;
                                break;
                            case DeviceStatus.SENSITIVITY_NORMAL:
                                tempSensitivity = DeviceStatus.SENSITIVITY_HIGHEST;
                                break;
                            case DeviceStatus.SENSITIVITY_HIGHEST:
                                tempSensitivity = DeviceStatus.SENSITIVITY_LOWEST;
                                break;
                            default:
                                tempSensitivity = DeviceStatus.SENSITIVITY_NORMAL;
                                break;
                        }

                        final int target = tempSensitivity * 10 + tempSensitivity;

                        if (DBG) Log.d(TAG, "sensitivity: " + mMonitSensor.getSensitivity() + " -> " + target);
                        mServerQueryMgr.setSensorSensitivity(
                                mMonitSensor.getDeviceInfo().deviceId,
                                mMonitSensor.getDeviceInfo().getEnc(),
                                target,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            mMonitSensor.setSensitivity(target);
                                            if (ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, DeviceType.DIAPER_SENSOR) != null) {
                                                ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, DeviceType.DIAPER_SENSOR).setSensitivity(mMonitSensor.getSensitivity());
                                            }
                                            mHandler.obtainMessage(MSG_UPDATE_SENSITIVITY_VIEW, mMonitSensor.getSensitivity(), -1).sendToTarget();
                                        } else {
                                        }
                                    }
                                });
                    } else {
                        Intent intent = new Intent(SettingSensorActivity.this, GuideDirectConnectionActivity.class);
                        intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
                        startActivityForResult(intent, REQCODE_BLE_DIRECT_CONNECTION);
                        overridePendingTransition(0, 0);
                    }
                }
            });
        }
        if (mHandler != null) {
            mHandler.obtainMessage(MSG_UPDATE_SENSITIVITY_VIEW, mMonitSensor.getSensitivity(), -1).sendToTarget();
        }

        if (btnFirmwareVersion == null) {
            btnFirmwareVersion = new SettingButton(this);
            btnFirmwareVersion.setTitle(getString(R.string.setting_device_firmware_version));
            btnFirmwareVersion.setDescription(getString(R.string.setting_device_firmware_version_description));
            btnFirmwareVersion.setDividerForOtherCategory(true);
        }

        if (mVersionMgr.supportDiaperSensorFwUpdate(mMonitSensor.firmwareVersion) || Configuration.MASTER || Configuration.DEVELOPER) {

            if (mVersionMgr.checkDiaperSensorFwUpdateAvailable(mMonitSensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion())) {
                btnFirmwareVersion.showNewMark(true);
            } else {
                btnFirmwareVersion.showNewMark(false);
            }
            btnFirmwareVersion.setContent(mMonitSensor.firmwareVersion + " / " + mPreferenceMgr.getDiaperSensorVersion());
            btnFirmwareVersion.showDirection(true);
            btnFirmwareVersion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingSensorActivity.this, FirmwareUpdateActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
        } else {
            btnFirmwareVersion.setContent(mMonitSensor.firmwareVersion);
            btnFirmwareVersion.showDirection(false);
            btnFirmwareVersion.setOnClickListener(null);
        }

        if (btnSerialNumber == null) {
            btnSerialNumber = new SettingButton(this);
            btnSerialNumber.setTitle(getString(R.string.setting_device_serial_number));
            btnSerialNumber.setDividerForOtherCategory(true);
            btnSerialNumber.setContent(mMonitSensor.serial);
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

    public void setConnected(boolean connected) {
        if (DBG) Log.i(TAG, "setConnected");
        _initSettingMain();
        isConnected = connected;
        if (btnBabyInfo != null) btnBabyInfo.setEnabled(isConnected);
        if (btnFind != null) btnFind.setEnabled(isConnected);
        if (btnEnableAlarm != null) btnEnableAlarm.setEnabled(isConnected);
        if (btnEnableAlarmPee != null) btnEnableAlarmPee.setEnabled(isConnected);
        if (btnEnableAlarmPoo != null) btnEnableAlarmPoo.setEnabled(isConnected);
        if (btnEnableAlarmFart != null) btnEnableAlarmFart.setEnabled(isConnected);
        if (btnEnableAlarmConnection != null) btnEnableAlarmConnection.setEnabled(isConnected);
        if (btnEnableAlarmDiaperCheck != null) btnEnableAlarmDiaperCheck.setEnabled(isConnected);
        if (btnEnableAlarmDiaperSoiled != null) btnEnableAlarmDiaperSoiled.setEnabled(isConnected);
        if (btnEnableAlarmMovementDetection != null) btnEnableAlarmMovementDetection.setEnabled(isConnected);
        if (btnEnableSleepAutoDetection != null) btnEnableSleepAutoDetection.setEnabled(isConnected);

        if (btnDetectionTest != null) btnDetectionTest.setEnabled(isConnected);
        if (btnFirmwareVersion != null) btnFirmwareVersion.setEnabled(isConnected);
        if (btnPooSensitivity != null) btnPooSensitivity.setEnabled(isConnected);
        if (btnPeeFakeAlarm != null) btnPeeFakeAlarm.setEnabled(isConnected);
        if (btnPooFakeAlarm != null) btnPooFakeAlarm.setEnabled(isConnected);
        if (btnFartFakeAlarm != null) btnFartFakeAlarm.setEnabled(isConnected);
        if (btnDebugCommand != null) btnDebugCommand.setEnabled(isConnected);
        if ((mMonitSensor != null)
                && (mMonitSensor.firmwareVersion != null)
                && mVersionMgr.checkDiaperSensorFwUpdateAvailable(mMonitSensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion())
                && connected) {
            if (btnFirmwareVersion != null) btnFirmwareVersion.showNewMark(true);
        } else {
            if (btnFirmwareVersion != null) btnFirmwareVersion.showNewMark(false);
        }
        if (btnSerialNumber != null) btnSerialNumber.setEnabled(isConnected);
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
                case MSG_UPDATE_SENSITIVITY_VIEW:
                    int sensitivity = msg.arg1;
                    switch(sensitivity % 10) {
                        case DeviceStatus.SENSITIVITY_LOWEST:
                            btnPooSensitivity.setContent(getString(R.string.sensitivity_low));
                            break;
                        case DeviceStatus.SENSITIVITY_LOWER:
                            btnPooSensitivity.setContent(getString(R.string.sensitivity_low));
                            break;
                        case DeviceStatus.SENSITIVITY_NORMAL:
                            btnPooSensitivity.setContent(getString(R.string.sensitivity_normal));
                            break;
                        case DeviceStatus.SENSITIVITY_HIGHER:
                            btnPooSensitivity.setContent(getString(R.string.sensitivity_high));
                            break;
                        case DeviceStatus.SENSITIVITY_HIGHEST:
                            btnPooSensitivity.setContent(getString(R.string.sensitivity_high));
                            break;
                        default:
                            btnPooSensitivity.setContent(getString(R.string.sensitivity_normal));
                            break;
                    }
                    break;
                case ConnectionManager.MSG_WIFI_CONNECTION_STATE_CHANGE:
                    final int wifiConnectionState = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_WIFI_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "/" + mConnectedSensorDeviceId + "] " + wifiConnectionState + " / ");
                    if (mConnectedSensorDeviceId == deviceInfo.deviceId) {
                        if (wifiConnectionState == DeviceConnectionState.WIFI_CONNECTED) {
                            _showSettingView(VIEW_MAIN);
                            setConnected(true);
                        } else {
                            _showSettingView(VIEW_MAIN);
                            setConnected(false);
                        }
                    }
                    break;
                case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
                    final int state = msg.arg1;
                    final DeviceInfo deviceInfo2 = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + deviceInfo2.deviceId + "/" + mConnectedSensorDeviceId + "] " + state + " / ");

                    if (mConnectedSensorDeviceId == deviceInfo2.deviceId) {
                        if (state == DeviceConnectionState.BLE_CONNECTED) {
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
                case ConnectionManager.MSG_SENSOR_VALUE_UPDATED:
                    final String deviceId3 = msg.arg1 + "";
                    final CurrentSensorValue sensorValue = (CurrentSensorValue) msg.obj;
                    break;
                case MSG_CLOSE_FINDING_DEVICE_DIALOG:
                    if (mDlgFinding != null && mDlgFinding.isShowing()) {
                        try {
                            mDlgFinding.dismiss();
                        } catch(IllegalArgumentException e) {

                        }
                    }
                    break;
                case MSG_CLOSE_DETECTION_TEST_DIALOG:
                    if (mDlgDetectionTest != null && mDlgDetectionTest.isShowing()) {
                        try {
                            mDlgDetectionTest.dismiss();
                        } catch(IllegalArgumentException e) {

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
                        case VIEW_CHANGE_BABY_INFO:
                            _showSettingChangeBabyInfo();
                            break;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DBG) Log.i(TAG, "onActivityResult : " + requestCode + " / " + mMonitSensor.getConnectionState());
        switch (requestCode) {
            case REQCODE_BLE_DIRECT_CONNECTION:
                if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                    if (DBG) Log.i(TAG, "onActivityResult : " + requestCode + " / " + mMonitSensor.getConnectionState());
                } else {
                    showToast(getString(R.string.toast_sensor_is_not_connected_directly));
                }
                break;
        }
    }
}
