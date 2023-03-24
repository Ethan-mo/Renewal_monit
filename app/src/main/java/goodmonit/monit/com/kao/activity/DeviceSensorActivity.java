package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
import goodmonit.monit.com.kao.devices.CurrentSensorLog;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.FeedingType;
import goodmonit.monit.com.kao.devicestatus.DiaperGraph2Fragment;
import goodmonit.monit.com.kao.devicestatus.DiaperGraphFragment;
import goodmonit.monit.com.kao.devicestatus.DiaperNotificationFragment;
import goodmonit.monit.com.kao.devicestatus.DiaperStatus2Fragment;
import goodmonit.monit.com.kao.devicestatus.DiaperStatusFragment;
import goodmonit.monit.com.kao.dfu.FirmwareUpdateActivity;
import goodmonit.monit.com.kao.dialog.DateTimeDialog;
import goodmonit.monit.com.kao.dialog.DiaperInputDialog;
import goodmonit.monit.com.kao.dialog.FeedingInputDialog;
import goodmonit.monit.com.kao.dialog.ProgressHorizontalDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.dialog.SleepInputDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.NotoButton;

public class DeviceSensorActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "DeviceDetail";
    private static final boolean DBG = Configuration.DBG;

    public static final int REQCODE_FLOAT_ADD_NOTIFICATION   = 100;

    public static final int VIEW_DIAPER_STATUS         = 0;
    public static final int VIEW_DIAPER_GRAPH          = 1;
    public static final int VIEW_DIAPER_NOTIFICATION   = 2;

    /** UI Resources */
    //private ViewPager viewPager;
    //private TabPagerAdapter tabPagerAdapter;

    private DiaperStatusFragment mDiaperStatusFragment;
    private DiaperStatus2Fragment mDiaperStatus2Fragment;
    private DiaperGraphFragment mDiaperGraphFragment;
    private DiaperGraph2Fragment mDiaperGraph2Fragment;
    private DiaperNotificationFragment mNotificationFragment;
    private DiaperInputDialog mDlgDiaperChanged;
    private DiaperInputDialog mDlgFeedbackDiaperStatus;
    private FeedingInputDialog mDlgFeeding;
    private SleepInputDialog mDlgSleep;
    private SimpleDialog mDlgPackageSecurityPatchUpdate;
    private SimpleDialog mDlgUserComment;

    private DateTimeDialog mSleepDateTimeDlg;
    private boolean isSleepStart;

    private Button btnTabStatus, btnTabGraph, btnTabNotification;
    private ImageView ivTabNotificationNew;

    private int mCurrentViewIndex;

    private long mConnectedSensorDeviceId;
    private int mStartPage;
    private DeviceDiaperSensor mMonitSensor;

    private ProgressHorizontalDialog mReceivingDataDialog;
    private boolean mFinishedGetNotification = false;
    private boolean mFinishedGetGraph = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        _setToolBar();

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);

        mConnectedSensorDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        mStartPage = getIntent().getIntExtra("startPage", VIEW_DIAPER_STATUS);

        _initView();

        if (Configuration.MONIT20) {
            mDiaperStatus2Fragment = new DiaperStatus2Fragment();
        } else {
            mDiaperStatusFragment = new DiaperStatusFragment();
        }
        mNotificationFragment = new DiaperNotificationFragment();

        if (Configuration.MONIT20) {
            mDiaperGraph2Fragment = new DiaperGraph2Fragment();
        } else {
            mDiaperGraphFragment = new DiaperGraphFragment();
        }

        mNotificationFragment.setDeviceId(mConnectedSensorDeviceId);
        mNotificationFragment.setDeviceType(DeviceType.DIAPER_SENSOR);

        //viewPager.setCurrentItem(VIEW_DIAPER_STATUS);
        selectTabButton(mStartPage);
        showFragment(mStartPage);
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
        mPreferenceMgr = PreferenceManager.getInstance(mContext);

        mMonitSensor = ConnectionManager.getDeviceDiaperSensor(mConnectedSensorDeviceId);
        if (mMonitSensor != null) {
            if (DBG) Log.d(TAG, "targetDevice : [" + mMonitSensor.deviceId + "] " + mMonitSensor.name);
        } else {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedSensorDeviceId);
            finish();
            return;
        }
        if (mMonitSensor.serial == null || mMonitSensor.cloudId == 0) {
            mConnectionMgr.getUserInfoFromCloud();
        }

        // 우측상단 알람 버튼 업데이트
        boolean alarmEnabled = mPreferenceMgr.getDeviceAlarmEnabled(
                DeviceType.DIAPER_SENSOR,
                mConnectedSensorDeviceId,
                NotificationType.DEVICE_ALL);
        try {
            btnToolbarRight2.setSelected(!alarmEnabled);
        } catch (Exception e) {

        }

        mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);

        if (ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, DeviceType.DIAPER_SENSOR) != null) {
            ConnectionManager.getDeviceBLEConnection(mMonitSensor.deviceId, DeviceType.DIAPER_SENSOR).checkSensorStatus();
        }

        if (Configuration.MONIT20) {
            if (mDiaperStatus2Fragment != null) {
                if ((mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) ||
                        (mMonitSensor.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED)) {
                    mDiaperStatus2Fragment.setConnected(true);
                } else {
                    mDiaperStatus2Fragment.setConnected(false);
                }
            }
        } else {
            if (mDiaperStatusFragment != null) {
                if ((mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) ||
                        (mMonitSensor.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED)) {
                    mDiaperStatusFragment.setConnected(true);
                } else {
                    mDiaperStatusFragment.setConnected(false);
                }
            }
        }

        if (Configuration.MASTER) {
            tvToolbarTitle.setText(mMonitSensor.name + "(" + mMonitSensor.deviceId + ")");
        } else {
            tvToolbarTitle.setText(mMonitSensor.name);
        }

        updateNewMark();
    }

    public void updateNewMark() {
        if (mPreferenceMgr != null && mMonitSensor != null) {
            if (ivToolbarNewRight != null) {
                if (new VersionManager(mContext).checkDiaperSensorFwUpdateAvailable(mMonitSensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion()) &&
                        (mMonitSensor.getConnectionState() != DeviceConnectionState.DISCONNECTED)) {
                    ivToolbarNewRight.setVisibility(View.VISIBLE);

                    if (mDlgPackageSecurityPatchUpdate == null) {
                        mDlgPackageSecurityPatchUpdate = new SimpleDialog(
                                DeviceSensorActivity.this,
                                getString(R.string.contents_need_firmware_update_force),
                                getString(R.string.btn_cancel),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mDlgPackageSecurityPatchUpdate.dismiss();
                                    }
                                },
                                getString(R.string.btn_ok),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            mDlgPackageSecurityPatchUpdate.dismiss();
                                        } catch (Exception e) {

                                        }
                                        Intent intent = new Intent(DeviceSensorActivity.this, FirmwareUpdateActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("targetDeviceId", mMonitSensor.deviceId);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                                    }
                                });
                    }

                    // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
                    if (DBG) Log.d(TAG, "sensor ver: " + mMonitSensor.firmwareVersion + " / " + mPreferenceMgr.getDiaperSensorVersion() + " / " + mPreferenceMgr.getDiaperSensorForceVersion());
                    boolean updateSensorForce = new VersionManager(mContext).checkUpdateAvailable(mMonitSensor.firmwareVersion, mPreferenceMgr.getDiaperSensorForceVersion());
                    if (updateSensorForce) {
                        try {
                            mDlgPackageSecurityPatchUpdate.show();
                        } catch (Exception e) {

                        }
                    }
                } else {
                    ivToolbarNewRight.setVisibility(View.GONE);
                }
            }

            if (ivTabNotificationNew != null) {
                if (mPreferenceMgr.getLatestCheckedNotificationIndex(mMonitSensor.type, mMonitSensor.deviceId) < mPreferenceMgr.getLatestSavedNotificationIndex(mMonitSensor.type, mMonitSensor.deviceId, 0)) {
                    ivTabNotificationNew.setVisibility(View.VISIBLE);
                } else {
                    ivTabNotificationNew.setVisibility(View.GONE);
                }
            }
        }
    }


    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_light);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ivToolbarCenter = (ImageView) findViewById(R.id.iv_toolbar_main_light_center);
        ivToolbarCenter.setVisibility(View.GONE);
        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_main_light_center);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_main_light_right);
        btnToolbarRight.setBackgroundResource(R.drawable.ic_device_setting);
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceSensorActivity.this, SettingSensorActivity.class);
                intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
            }
        });

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_main_light_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_black);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
            }
        });

        btnToolbarRight2 = (Button) findViewById(R.id.btn_toolbar_main_light_right2);
        btnToolbarRight2.setVisibility(View.VISIBLE);
        btnToolbarRight2.setBackgroundResource(R.drawable.ic_alarm_enabled_selector);
        btnToolbarRight2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMonitSensor == null) return;
                // 현재상태 Selected: Disabled, Not Selected: Enabled
                final boolean alarmEnabled = btnToolbarRight2.isSelected();
                mServerQueryMgr.setDeviceAlarmStatus(mMonitSensor.type, mMonitSensor.deviceId, NotificationType.DEVICE_ALL, alarmEnabled,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                    mPreferenceMgr.setDeviceAlarmEnabled(
                                            mMonitSensor.type,
                                            mMonitSensor.deviceId,
                                            NotificationType.DEVICE_ALL,
                                            alarmEnabled);
                                    try {
                                        btnToolbarRight2.setSelected(!btnToolbarRight2.isSelected());
                                    } catch (Exception e) {

                                    }
                                    if (alarmEnabled) {
                                        showImageToast(R.drawable.ic_alarm_enabled_toast);
                                    } else {
                                        showImageToast(R.drawable.ic_alarm_disabled_toast);
                                    }
                                } else {
                                    if (DBG) Log.d(TAG, "Set alarm status failed");
                                }
                            }
                        });
            }
        });

        ivToolbarNewRight = (ImageView)findViewById(R.id.iv_toolbar_main_light_right_new_mark);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQCODE_FLOAT_ADD_NOTIFICATION:
                if (resultCode == NotificationType.PEE_DETECTED) {
                    if (mDlgFeedbackDiaperStatus != null) {
                        mDlgFeedbackDiaperStatus.setSelectedIndex(2);
                        mDlgFeedbackDiaperStatus.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeedbackDiaperStatus.show();
                    }
                } else if (resultCode == NotificationType.POO_DETECTED) {
                    if (mDlgFeedbackDiaperStatus != null) {
                        mDlgFeedbackDiaperStatus.setSelectedIndex(3);
                        mDlgFeedbackDiaperStatus.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeedbackDiaperStatus.show();
                    }
                } else if (resultCode == NotificationType.DIAPER_CHANGED) {
                    if (mDlgFeedbackDiaperStatus != null) {
                        mDlgFeedbackDiaperStatus.setSelectedIndex(1);
                        mDlgFeedbackDiaperStatus.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeedbackDiaperStatus.show();
                    }
                } else if (resultCode == NotificationType.ABNORMAL_DETECTED) {
                    if (mDlgFeedbackDiaperStatus != null) {
                        mDlgFeedbackDiaperStatus.setSelectedIndex(4);
                        mDlgFeedbackDiaperStatus.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeedbackDiaperStatus.show();
                    }
                } else if (resultCode == NotificationType.CHAT_USER_FEEDBACK) {
                    if (mDlgUserComment != null) {
                        mDlgUserComment.show();
                    }
                } else if (resultCode == NotificationType.BABY_SLEEP) { // Sleep
                    if (mDlgSleep != null && !mDlgSleep.isShowing()) {
                        mDlgSleep.setStartDateTimeUtcMs(System.currentTimeMillis());
                        mDlgSleep.show();
                        mDlgSleep.expandEndTime(false);
                    }
                } else if (resultCode == NotificationType.BABY_FEEDING_BABY_FOOD) {
                    if (mDlgFeeding != null && !mDlgFeeding.isShowing()) {
                        mDlgFeeding.setSelectedIndex(FeedingType.BABY_FOOD);
                        mDlgFeeding.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeeding.show();
                    }
                } else if (resultCode == NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK) {
                    if (mDlgFeeding != null && !mDlgFeeding.isShowing()) {
                        mDlgFeeding.setSelectedIndex(FeedingType.BOTTLE_FORMULA_MILK);
                        mDlgFeeding.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeeding.show();
                    }
                } else if (resultCode == NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK) {
                    if (mDlgFeeding != null && !mDlgFeeding.isShowing()) {
                        mDlgFeeding.setSelectedIndex(FeedingType.BOTTLE_BREAST_MILK);
                        mDlgFeeding.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeeding.show();
                    }
                } else if (resultCode == NotificationType.BABY_FEEDING_NURSED_BREAST_MILK) {
                    if (mDlgFeeding != null && !mDlgFeeding.isShowing()) {
                        mDlgFeeding.setSelectedIndex(FeedingType.NURSED_BREAST_MILK);
                        mDlgFeeding.setDateTimeUtcMs(System.currentTimeMillis());
                        mDlgFeeding.show();
                    }
                }
                break;
        }
    }

    private void _initView() {
        // Initializing ViewPager
        //viewPager = (ViewPager) findViewById(R.id.viewpager_device_detail);
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        btnTabStatus = (Button)findViewById(R.id.btn_tabbar_device_detail_item1);
        btnTabStatus.setText(getString(R.string.tab_status));
        btnTabStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(VIEW_DIAPER_STATUS);
                selectTabButton(VIEW_DIAPER_STATUS);
                //viewPager.setCurrentItem(VIEW_DIAPER_STATUS);
            }
        });

        btnTabGraph = (Button)findViewById(R.id.btn_tabbar_device_detail_item2);
        btnTabGraph.setText(getString(R.string.tab_graph));
        btnTabGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(VIEW_DIAPER_GRAPH);
                selectTabButton(VIEW_DIAPER_GRAPH);

                if (mFinishedGetGraph == false) {
                    if (mReceivingDataDialog != null && !mReceivingDataDialog.isShowing()) {
                        try {
                            mReceivingDataDialog.show();
                        } catch (Exception e) {

                        }
                    }
                }
                //viewPager.setCurrentItem(VIEW_DIAPER_GRAPH);
            }
        });
        //btnTabGraph.setVisibility(View.GONE);

        btnTabNotification = (Button)findViewById(R.id.btn_tabbar_device_detail_item3);
        btnTabNotification.setText(getString(R.string.tab_notification));
        btnTabNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(VIEW_DIAPER_NOTIFICATION);
                selectTabButton(VIEW_DIAPER_NOTIFICATION);

                if (mFinishedGetNotification == false) {
                    if (mReceivingDataDialog != null && !mReceivingDataDialog.isShowing()) {
                        try {
                            mReceivingDataDialog.show();
                        } catch (Exception e) {

                        }
                    }
                }
                //viewPager.setCurrentItem(VIEW_DIAPER_NOTIFICATION);
            }
        });

        ivTabNotificationNew = (ImageView)findViewById(R.id.iv_tabbar_device_detail_item3_new);

        mReceivingDataDialog = new ProgressHorizontalDialog(mContext,
                getString(R.string.dialog_update_device_information),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mReceivingDataDialog.dismiss();
                    }
                });
        // Creating TabPagerAdapter adapter
        /*
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), 2);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {;}

            @Override
            public void onPageSelected(int position) {
                if (DBG) Log.d(TAG, "onPageSelected");
                switch (position) {
                    case VIEW_DIAPER_NOTIFICATION:
                        if (mNotificationFragment != null) {
                            mNotificationFragment.onResume();
                        }
                        break;
                }
                selectTabButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {;}
        });
        */

        mDlgDiaperChanged = new DiaperInputDialog(mContext,
                getString(R.string.dialog_sensor_diaper_changed_date_time),
                getString(R.string.dialog_sensor_diaper_change_record),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgDiaperChanged.dismiss();
                    }
                },
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedDateTimeMs = mDlgDiaperChanged.getDateTimeUtcMs();
                        int selectedMode = mDlgDiaperChanged.getSelectedMode();
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        mDlgDiaperChanged.dismiss();
                        setDiaperChanged(selectedDateTimeMs / 1000, selectedMode);
                    }
                });

        // 기저귀 상태 입력이 아닌 기저귀 교체입력으로 수정
        mDlgFeedbackDiaperStatus = new DiaperInputDialog(mContext,
                getString(R.string.dialog_sensor_diaper_changed_date_time),
                getString(R.string.dialog_sensor_diaper_change_record),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgFeedbackDiaperStatus.dismiss();
                    }
                },
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedDateTimeMs = mDlgFeedbackDiaperStatus.getDateTimeUtcMs();
                        int selectedMode = mDlgFeedbackDiaperStatus.getSelectedMode();
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        mDlgFeedbackDiaperStatus.dismiss();
                        //addDiaperFeedbackInput(selectedMode, selectedDateTimeMs);
                        setDiaperChanged(selectedDateTimeMs / 1000, selectedMode);
                    }
                });

        mDlgFeeding = new FeedingInputDialog(mContext,
                getString(R.string.dialog_sensor_feeding_input),
                getString(R.string.dialog_sensor_feeding_record),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgFeeding.dismiss();
                    }
                },
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedDateTimeMs = mDlgFeeding.getDateTimeUtcMs();
                        int selectedMode = mDlgFeeding.getSelectedMode(); // 1: 모유, 2: 유축, 3: 분유, 4: 이유식
                        String extraValue = mDlgFeeding.getExtraValue();

                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode + " / " + extraValue);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (extraValue == null || extraValue.equals("")) {
                            showToast("양/시간을 입력해 주세요.");
                        } else {
                            mDlgFeeding.dismiss();
                            addFeedingInput(selectedMode, extraValue, selectedDateTimeMs);
                        }
                    }
                });

        mDlgSleep = new SleepInputDialog(mContext,
                getString(R.string.dialog_sensor_sleep_input),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgSleep.dismiss();
                    }
                },
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedStartDateTimeMs = mDlgSleep.getStartDateTimeUtcMs();
                        long selectedEndDateTimeMs = mDlgSleep.getEndDateTimeUtcMs();
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedStartDateTimeMs + " / " + selectedEndDateTimeMs);
                        mDlgSleep.dismiss();
                        addSleepInput(selectedStartDateTimeMs, selectedEndDateTimeMs);
                    }
                });

        mSleepDateTimeDlg = new DateTimeDialog(mContext,
                getString(R.string.dialog_sensor_sleep_start_date_time),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSleepDateTimeDlg.dismiss();
                    }
                },
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedDateTimeMs = mSleepDateTimeDlg.getDateTimeUtcMs();
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        mSleepDateTimeDlg.dismiss();
                        setSleepMode(selectedDateTimeMs / 1000, isSleepStart);
                    }
                });

        mDlgUserComment = new SimpleDialog(mContext,
                getString(R.string.help_feedback),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgUserComment.dismiss();
                    }
                },
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String msg = mDlgUserComment.getInputText();
                        if (msg.length() > 1) {
                            mDlgUserComment.dismiss();
                            addUserComment(msg);
                        }
                    }
                });
        mDlgUserComment.setInputMode(true);
    }

    public void showFragment(int idx) {
        Fragment fr = null;
        switch(idx) {
            case VIEW_DIAPER_STATUS:
                if (Configuration.MONIT20) {
                    if (mDiaperStatus2Fragment == null) {
                        mDiaperStatus2Fragment = new DiaperStatus2Fragment();
                    }
                    fr = mDiaperStatus2Fragment;
                } else {
                    if (mDiaperStatusFragment == null) {
                        mDiaperStatusFragment = new DiaperStatusFragment();
                    }
                    fr = mDiaperStatusFragment;
                }
                break;
            case VIEW_DIAPER_GRAPH:
                if (Configuration.MONIT20) {
                    if (mDiaperGraph2Fragment == null) {
                        mDiaperGraph2Fragment = new DiaperGraph2Fragment();
                    }
                    fr = mDiaperGraph2Fragment;
                } else {
                    if (mDiaperGraphFragment == null) {
                        mDiaperGraphFragment = new DiaperGraphFragment();
                    }
                    fr = mDiaperGraphFragment;
                }
                break;
            case VIEW_DIAPER_NOTIFICATION:
                if (mNotificationFragment == null) {
                    mNotificationFragment = new DiaperNotificationFragment();
                }
                fr = mNotificationFragment;
                break;
        }

        if (fr != null) {
            try {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();

                if (mCurrentViewIndex < idx) {
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                } else if (mCurrentViewIndex > idx) {
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right, R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                }

                mCurrentViewIndex = idx;
                fragmentTransaction.replace(R.id.fragment_device_detail, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } catch (IllegalStateException e) {

            }
        }
    }

    public void selectTabButton(int position) {
        mCurrentViewIndex = position;

        btnTabStatus.setSelected(false);
        btnTabStatus.setTextColor(getResources().getColor(R.color.colorTextGrey));
        ((NotoButton)btnTabStatus).setTypeface("regular");

        btnTabGraph.setSelected(false);
        btnTabGraph.setTextColor(getResources().getColor(R.color.colorTextGrey));
        ((NotoButton)btnTabGraph).setTypeface("regular");

        btnTabNotification.setSelected(false);
        btnTabNotification.setTextColor(getResources().getColor(R.color.colorTextGrey));
        ((NotoButton)btnTabNotification).setTypeface("regular");

        switch (position) {
            case VIEW_DIAPER_STATUS:
                btnTabStatus.setSelected(true);
                btnTabStatus.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabStatus).setTypeface("medium");
                break;
            case VIEW_DIAPER_GRAPH:
                btnTabGraph.setSelected(true);
                btnTabGraph.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabGraph).setTypeface("medium");
                break;
            case VIEW_DIAPER_NOTIFICATION:
                btnTabNotification.setSelected(true);
                btnTabNotification.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabNotification).setTypeface("medium");
                break;
        }
    }

    public DeviceDiaperSensor getDiaperSensorObject() {
        mMonitSensor = ConnectionManager.getDeviceDiaperSensor(mConnectedSensorDeviceId);
        return mMonitSensor;
    }

    public void initDiaperStatus() {
        if (mMonitSensor != null) {
            final long timeSec = System.currentTimeMillis() / 1000;
            mServerQueryMgr.initDiaperStatus(
                    mMonitSensor.type,
                    mMonitSensor.deviceId,
                    mMonitSensor.getEnc(),
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            long respTimeSec = 0;
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mMonitSensor.initDetectedStatus(timeSec * 1000);
                                respTimeSec = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(15));
                                showToast(getString(R.string.toast_diaper_status_initialized));
                            } else {

                            }
                            if (respTimeSec <= 0) respTimeSec = timeSec;
                            mPreferenceMgr.setLatestDiaperStatusUpdatedTimeSec(mMonitSensor.deviceId, respTimeSec);
                        }
                    });
        }
    }

    public void showSleepDateTimeDialog(boolean isStart) {
        if (mSleepDateTimeDlg != null) {
            isSleepStart = isStart;
            if (isStart) {
                mSleepDateTimeDlg.setTitle(getString(R.string.dialog_sensor_sleep_start_date_time));
            } else {
                mSleepDateTimeDlg.setTitle(getString(R.string.dialog_sensor_sleep_end_date_time));
            }
            mSleepDateTimeDlg.setDateTimeUtcMs(System.currentTimeMillis());
            mSleepDateTimeDlg.show();
        }
    }

    public void showDateTimeDialog() {
        if (mDlgDiaperChanged != null) {
            mDlgDiaperChanged.setDateTimeUtcMs(System.currentTimeMillis());
            mDlgDiaperChanged.show();
        }
    }

    public void setDiaperChanged() {
        setDiaperChanged(System.currentTimeMillis() / 1000, 0);
    }

    public void setDiaperChanged(final long utcTimeSec, final int selectedMode) {
        if (mMonitSensor != null) {
            mServerQueryMgr.setDiaperChanged(
                    mMonitSensor.type,
                    mMonitSensor.deviceId,
                    mMonitSensor.getEnc(),
                    utcTimeSec * 1000,
                    selectedMode + "",
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            long utcRespTimeSec = 0;
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                String time = ServerManager.getStringFromJSONObj(data, mServerQueryMgr.getParameter(15));
                                if (time != null && time.length() > 0) {
                                    if (time.contains("-")) { // YYMMDD-HHMMSS 형식
                                        try {
                                            Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time);
                                            // date.getTime() 은 Local시간을 UTC로 변경하므로 다시 +9 해야함
                                            utcRespTimeSec = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()) / 1000;
                                        } catch (ParseException e) {
                                        }
                                    } else { // Timestamp형식
                                        try {
                                            utcRespTimeSec = Long.parseLong(time);
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                            }

                            if (utcRespTimeSec <= 0) utcRespTimeSec = utcTimeSec;
                            String utcTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcRespTimeSec * 1000);
                            if (DBG) Log.d(TAG, "utc datetime string: " + utcTimeString);

                            mMonitSensor.setDiaperChanged(utcRespTimeSec * 1000);

                            mPreferenceMgr.setLatestDiaperStatusUpdatedTimeSec(mMonitSensor.deviceId, utcRespTimeSec);
                            if (mPreferenceMgr.getLatestDiaperChangedTimeSec(mMonitSensor.deviceId) < utcRespTimeSec) {
                                mPreferenceMgr.setLatestDiaperChangedTimeSec(mMonitSensor.deviceId, utcRespTimeSec);
                            }

                            mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);
                        }
                    });
        }
    }

    public void setSleepMode(final long utcTimeSec, final boolean isStart) {
        if (mMonitSensor != null) {
            mServerQueryMgr.setSleepMode(
                    mMonitSensor.deviceId,
                    mMonitSensor.getEnc(),
                    isStart,
                    utcTimeSec * 1000,
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mPreferenceMgr.setSleepingEnabled(mMonitSensor.deviceId, isStart);
                                mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.MOVEMENT_DETECTED, isStart);
                                if (isStart == true) {
                                    mPreferenceMgr.setSleepingStartTimeMs(mMonitSensor.deviceId, utcTimeSec * 1000);
                                }
                                mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);
                            }
                        }
                    });
        }
    }

    public void addUserComment(String comment) {
        if (mMonitSensor != null) {
            final NotificationMessage notiMsg = new NotificationMessage(NotificationType.CHAT_USER_INPUT, mMonitSensor.type, mMonitSensor.deviceId, comment, System.currentTimeMillis() / 1000 * 1000);
            mServerQueryMgr.setNotificationFeedback(
                    notiMsg,
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);
                            }
                        }
                    });
        }
    }

    public void addSleepInput(final long sleepStartUtcTimeMs, final long sleepEndUtcTimeMs) {
        if (mMonitSensor != null) {
            String endTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(sleepEndUtcTimeMs);
            final NotificationMessage notiMsg = new NotificationMessage(NotificationType.BABY_SLEEP, mMonitSensor.type, mMonitSensor.deviceId, endTimeString, sleepStartUtcTimeMs);
            ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                    notiMsg,
                    new ServerManager.ServerResponseListener(){
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);
                            }
                        }
                    });
        }
    }

    public void addFeedingInput(final int feedingType, final String extraValue, final long utcTimeMs) {
        if (mMonitSensor != null) {
            int notiType = 0;
            if (feedingType == FeedingType.NURSED_BREAST_MILK) {
                notiType = NotificationType.BABY_FEEDING_NURSED_BREAST_MILK;
            } else if (feedingType == FeedingType.BOTTLE_BREAST_MILK) {
                notiType = NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK;
            } else if (feedingType == FeedingType.BOTTLE_FORMULA_MILK) {
                notiType = NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK;
            } else if (feedingType == FeedingType.BABY_FOOD) {
                notiType = NotificationType.BABY_FEEDING_BABY_FOOD;
            }

            final NotificationMessage notiMsg = new NotificationMessage(notiType, mMonitSensor.type, mMonitSensor.deviceId, extraValue, utcTimeMs);
            ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                    notiMsg,
                    new ServerManager.ServerResponseListener(){
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId);
                            }
                        }
                    });
        }
    }

    public void startBetatestInputAlarm() {
        mConnectionMgr.startBetaTestInputAlarm();
    }

    public void stopBetatestInputAlarm() {
        mConnectionMgr.stopBetaTestInputAlarm();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DBG) Log.d(TAG, "handleMessage: " + msg.what);
            switch(msg.what) {
                case ConnectionManager.MSG_NOTIFICATION_MESSAGE_RECEIVED:
                    int currIdx = msg.arg1;
                    int totalIdx = msg.arg2;
                    if (DBG) Log.d(TAG, "MSG_NOTIFICATION_MESSAGE_RECEIVED : " + currIdx + " / " + totalIdx);
                    if (totalIdx == 0) {
                        if (mReceivingDataDialog != null) {
                            mReceivingDataDialog.setProgress(100);
                        }
                        if (!mFinishedGetNotification) {
                            if (DBG) Log.d(TAG, "notification received");
                            mFinishedGetNotification = true;
                            mConnectionMgr.getMovementGraphList(mMonitSensor);

                            if (mCurrentViewIndex == VIEW_DIAPER_NOTIFICATION) {
                                mNotificationFragment.showFilteredList();
                            }
                        }
                    } else {
                        if (mReceivingDataDialog != null) {
                            mReceivingDataDialog.setProgress(currIdx * 100 / totalIdx);
                        }
                    }
                    break;
                case ConnectionManager.MSG_MOVEMENT_GRAPH_DATA_RECEIVED:
                    int movementGraphCurrIdx = msg.arg1;
                    int movementGraphTotalIdx = msg.arg2;
                    if (mFinishedGetNotification) {
                        if (DBG) Log.d(TAG, "MSG_MOVEMENT_GRAPH_DATA_RECEIVED: " + movementGraphCurrIdx + " / " + movementGraphTotalIdx);
                        if (movementGraphTotalIdx == 0) {
                            if (mReceivingDataDialog != null) {
                                mFinishedGetGraph = true;
                                mReceivingDataDialog.setProgress(100);
                                try {
                                    mReceivingDataDialog.dismiss();
                                } catch (Exception e) {

                                }
                                if (mCurrentViewIndex == VIEW_DIAPER_GRAPH) {
                                    if (Configuration.MONIT20) {
                                        mDiaperGraph2Fragment.updateView();
                                    } else {
                                        mDiaperGraphFragment.updateView();
                                    }
                                }
                            }
                        } else {
                            if (mReceivingDataDialog != null) {
                                mReceivingDataDialog.setProgress(movementGraphCurrIdx * 100 / movementGraphTotalIdx);
                            }
                        }
                    } else {
                        if (DBG) Log.d(TAG, "MSG_MOVEMENT_GRAPH_DATA_RECEIVED noti not finished: " + movementGraphCurrIdx + " / " + movementGraphTotalIdx);
                    }
                    break;

                case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
                    final int localId = msg.arg1;
                    final int state = msg.arg2;
                    if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + localId + "] " + state);

                    if (Configuration.MONIT20) {
                        if (mDiaperStatus2Fragment != null) {
                            if (state == DeviceConnectionState.BLE_CONNECTED) {
                                mDiaperStatus2Fragment.setConnected(true);
                            } else {
                                mDiaperStatus2Fragment.setConnected(false);
                            }
                        }
                    } else {
                        if (mDiaperStatusFragment != null) {
                            if (state == DeviceConnectionState.BLE_CONNECTED) {
                                mDiaperStatusFragment.setConnected(true);
                            } else {
                                mDiaperStatusFragment.setConnected(false);
                            }
                        }
                    }
                    break;
                case ConnectionManager.MSG_LAMP_VALUE_UPDATED:
                    final int deviceId = msg.arg1;
                    final CurrentLampValue currLampValue = (CurrentLampValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_LAMP_VALUE_UPDATED : [" + deviceId + "] " + currLampValue.toString());
                    break;
                case ConnectionManager.MSG_SENSOR_VALUE_UPDATED:
                    final int deviceId2 = msg.arg1;
                    final CurrentSensorValue sensorValue = (CurrentSensorValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_SENSOR_VALUE_UPDATED : [" + deviceId2 + "] " + sensorValue.toString());

                    if (Configuration.MONIT20) {
                        if (mCurrentViewIndex == VIEW_DIAPER_STATUS &&
                                mDiaperStatus2Fragment != null) {
                            mDiaperStatus2Fragment.setConnected(true);
                            //mDiaperStatusFragment.setSensorValue(sensorValue);
                            //mDiaperStatusFragment.setPassedTimeFromDetection(mMonitSensor.getDiaperDetectedTimeMs());
                        }

                        if (mCurrentViewIndex == VIEW_DIAPER_STATUS && mDiaperStatus2Fragment != null) {
                            mDiaperStatus2Fragment.addDebuggingText(DateFormat.format("[HH:mm:ss]", System.currentTimeMillis()).toString() + sensorValue.temperature + " / " + sensorValue.humidity + " / " + sensorValue.voc);
                        }
                    } else {
                        if (mCurrentViewIndex == VIEW_DIAPER_STATUS &&
                                mDiaperStatusFragment != null) {
                            mDiaperStatusFragment.setConnected(true);
                            //mDiaperStatusFragment.setSensorValue(sensorValue);
                            //mDiaperStatusFragment.setPassedTimeFromDetection(mMonitSensor.getDiaperDetectedTimeMs());
                        }

                        if (mCurrentViewIndex == VIEW_DIAPER_STATUS && mDiaperStatusFragment != null) {
                            mDiaperStatusFragment.addDebuggingText(DateFormat.format("[HH:mm:ss]", System.currentTimeMillis()).toString() + sensorValue.temperature + " / " + sensorValue.humidity + " / " + sensorValue.voc);
                        }
                    }
                    break;
                case ConnectionManager.MSG_SET_DEVICE_DATA:
                    if (Configuration.MONIT20) {
                        if (mCurrentViewIndex == VIEW_DIAPER_STATUS && mDiaperStatus2Fragment != null && mMonitSensor != null) {
                            CurrentSensorLog log = (CurrentSensorLog)msg.obj;
                            if (msg.arg1 == DeviceType.DIAPER_SENSOR && msg.arg2 == mMonitSensor.deviceId) {
                                mDiaperStatus2Fragment.addDebuggingText(DateFormat.format("[HH:mm:ss]", System.currentTimeMillis()) + log.data);
                            }
                        }
                    } else {
                        if (mCurrentViewIndex == VIEW_DIAPER_STATUS && mDiaperStatusFragment != null && mMonitSensor != null) {
                            CurrentSensorLog log = (CurrentSensorLog)msg.obj;
                            if (msg.arg1 == DeviceType.DIAPER_SENSOR && msg.arg2 == mMonitSensor.deviceId) {
                                mDiaperStatusFragment.addDebuggingText(DateFormat.format("[HH:mm:ss]", System.currentTimeMillis()) + log.data);
                            }
                        }
                    }
                    break;
                case ConnectionManager.MSG_NOTIFICATION_MESSAGE_UPDATED:
                    if (DBG) Log.d(TAG, "MSG_NOTIFICATION_MESSAGE_UPDATED");
                    updateNewMark();
                    if (mNotificationFragment != null && mCurrentViewIndex == VIEW_DIAPER_NOTIFICATION) {
                        mNotificationFragment.loadLatestMessageList();
                        mNotificationFragment.showFilteredList();
                        ivTabNotificationNew.setVisibility(View.GONE);
                    }
                    break;

                case ConnectionManager.MSG_CHECK_INVALID_TOKEN:
                    if (DBG) Log.d(TAG, "MSG_CHECK_INVALID_TOKEN");
                    checkInvalidToken();
                    break;
            }
        }
    };

    public class TabPagerAdapter extends FragmentStatePagerAdapter {

        // Count number of tabs
        private int tabCount;

        public TabPagerAdapter(FragmentManager fm, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            // Returning the current tabs
            switch (position) {
                case VIEW_DIAPER_STATUS:
                    if (Configuration.MONIT20) {
                        if (mDiaperStatus2Fragment == null) {
                            mDiaperStatus2Fragment = new DiaperStatus2Fragment();
                        }
                        return mDiaperStatus2Fragment;
                    } else {
                        if (mDiaperStatusFragment == null) {
                            mDiaperStatusFragment = new DiaperStatusFragment();
                        }
                        return mDiaperStatusFragment;
                    }
                case VIEW_DIAPER_NOTIFICATION:
                    if (mNotificationFragment == null) {
                        mNotificationFragment = new DiaperNotificationFragment();
                    }
                    return mNotificationFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }
}
