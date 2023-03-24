package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.devicestatus.EnvironmentGraph2Fragment;
import goodmonit.monit.com.kao.devicestatus.EnvironmentNotificationFragment;
import goodmonit.monit.com.kao.devicestatus.EnvironmentStatus2Fragment;
import goodmonit.monit.com.kao.devicestatus.EnvironmentStatusFragment;
import goodmonit.monit.com.kao.dfu.HubFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dialog.ProgressHorizontalDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.NotoButton;

public class DeviceEnvironmentActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "DeviceEnv";
    private static final boolean DBG = Configuration.DBG;

    private static final int VIEW_ENVIRONMENT_STATUS        = 0;
    private static final int VIEW_ENVIRONMENT_GRAPH         = 1;
    private static final int VIEW_ENVIRONMENT_NOTIFICATION  = 2;

    /** UI Resources */
    //private ViewPager viewPager;
    //private TabPagerAdapter tabPagerAdapter;

    private EnvironmentStatusFragment mEnvironmentStatusFragment;
    private EnvironmentStatus2Fragment mEnvironmentStatusFragment2;
    private EnvironmentNotificationFragment mNotificationFragment;
    private EnvironmentGraph2Fragment mGraphFragment;

    private Button btnTabStatus, btnTabGraph, btnTabNotification;
    private ImageView ivTabNotificationNew;

    private int mCurrentViewIndex;

    private long mConnectedDeviceId;
    private DeviceAQMHub mHub;
    private DatabaseManager mDatabaseMgr;
    private EnvironmentCheckManager mEnvironmentCheckMgr;

    private ProgressHorizontalDialog mReceivingDataDialog;
    private SimpleDialog mDlgPackageSecurityPatchUpdate;
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
        mDatabaseMgr = DatabaseManager.getInstance(this);
        mEnvironmentCheckMgr = new EnvironmentCheckManager(this);

        mConnectedDeviceId = getIntent().getLongExtra("targetDeviceId", -1);

        if (DBG) Log.i(TAG, "onCreate : " + mConnectedDeviceId);
        _initView();

        mEnvironmentStatusFragment = new EnvironmentStatusFragment();
        mEnvironmentStatusFragment2 = new EnvironmentStatus2Fragment();
        mNotificationFragment = new EnvironmentNotificationFragment();
        mNotificationFragment.setDeviceId(mConnectedDeviceId);
        mNotificationFragment.setDeviceType(DeviceType.AIR_QUALITY_MONITORING_HUB);
        mGraphFragment = new EnvironmentGraph2Fragment();

        //viewPager.setCurrentItem(VIEW_ENVIRONMENT_STATUS);

        _showFragment(VIEW_ENVIRONMENT_STATUS);
        _selectTabButton(VIEW_ENVIRONMENT_STATUS);
    }

    public DeviceAQMHub getAQMHubObject() {
        return mHub;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFinishedGetNotification = false;
        mFinishedGetGraph = false;
        if (DBG) Log.i(TAG, "onResume");
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(mContext);

        mHub = ConnectionManager.getDeviceAQMHub(mConnectedDeviceId);
        if (mHub != null) {
            if (DBG) Log.d(TAG, "targetDevice : [" + mHub.deviceId + "] " + mHub.name);
        } else {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedDeviceId);
            finish();
            return;
        }
        if (mHub.serial == null || mHub.cloudId == 0) {
            mConnectionMgr.getUserInfoFromCloud();
        }

        // 우측상단 알람 버튼 업데이트
        boolean alarmEnabled = mPreferenceMgr.getDeviceAlarmEnabled(
                DeviceType.AIR_QUALITY_MONITORING_HUB,
                mConnectedDeviceId,
                NotificationType.DEVICE_ALL);
        btnToolbarRight2.setSelected(!alarmEnabled);

        if (Configuration.MONIT20_HUB) {
            mEnvironmentStatusFragment2.setAQMHubDeviceInfo(mHub);
            if (mEnvironmentStatusFragment2 != null) {
                if ((mHub.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) ||
                        (mHub.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED)) {
                    if (mHub.getHumidity() == -1 || mHub.getTemperature() == -1) {
                        mEnvironmentStatusFragment2.setConnected(false);
                    } else {
                        mEnvironmentStatusFragment2.setConnected(true);
                    }
                } else {
                    mEnvironmentStatusFragment2.setConnected(false);
                }
            }
        } else {
            mEnvironmentStatusFragment.setAQMHubDeviceInfo(mHub);
            if (mEnvironmentStatusFragment != null) {
                if ((mHub.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) ||
                        (mHub.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED)) {
                    if (mHub.getHumidity() == -1 || mHub.getTemperature() == -1) {
                        mEnvironmentStatusFragment.setConnected(false);
                    } else {
                        mEnvironmentStatusFragment.setConnected(true);
                    }
                } else {
                    mEnvironmentStatusFragment.setConnected(false);
                }
            }
        }

        if (Configuration.MASTER) {
            tvToolbarTitle.setText(mHub.name + "(" + mHub.deviceId + ")");
        } else {
            tvToolbarTitle.setText(mHub.name);
        }

        mServerQueryMgr.getLampOffTimerInfo(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, mHub.getEnc(),
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            JSONObject jobj = null;
                            try {
                                jobj = new JSONObject(data);
                                String ontime = jobj.optString(mServerQueryMgr.getParameter(126), null);
                                String offtime = jobj.optString(mServerQueryMgr.getParameter(125), null);
                                Date date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(ontime);
                                long onUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴

                                date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(offtime);
                                long offUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴

                                if (DBG) Log.d(TAG, "GetHubTimerInfo: onTime: " + ontime + "(" + onUtcTimeMs + "), offTime: " + offtime + "(" + offUtcTimeMs + ")");
                                if (offUtcTimeMs > 0 && offUtcTimeMs > System.currentTimeMillis()) {
                                    mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, offUtcTimeMs);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (DBG) Log.d(TAG, "GetHubTimerInfo Failed");
                        }
                    }
                });

        mConnectionMgr.getNotificationFromCloudV2(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId);
        updateNewMark();
    }

    public void updateNewMark() {
        if (mPreferenceMgr != null && mHub != null && ivTabNotificationNew != null) {
            long maxIdx = -999;
            long idx = mPreferenceMgr.getLatestSavedNotificationIndex(mHub.type, mHub.deviceId, 0);
            if (maxIdx < idx) maxIdx = idx;
            idx = mPreferenceMgr.getLatestSavedNotificationIndex(mHub.type, mHub.deviceId, 1);
            if (maxIdx < idx) maxIdx = idx;
            idx = mPreferenceMgr.getLatestSavedNotificationIndex(mHub.type, mHub.deviceId, 2);
            if (maxIdx < idx) maxIdx = idx;

            if (mPreferenceMgr.getLatestCheckedNotificationIndex(mHub.type, mHub.deviceId) < maxIdx) {
                ivTabNotificationNew.setVisibility(View.VISIBLE);
            } else {
                ivTabNotificationNew.setVisibility(View.GONE);
            }

            if (ivToolbarNewRight != null) {
                if (new VersionManager(mContext).checkUpdateAvailable(mHub.firmwareVersion, mPreferenceMgr.getHubVersion()) &&
                        (mHub.getConnectionState() != DeviceConnectionState.DISCONNECTED) &&
                        (mHub.getTemperature() != -1 && mHub.getHumidity() != -1)) {
                    ivToolbarNewRight.setVisibility(View.VISIBLE);

                    if (mDlgPackageSecurityPatchUpdate == null) {
                        mDlgPackageSecurityPatchUpdate = new SimpleDialog(
                                DeviceEnvironmentActivity.this,
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
                                        Intent intent = new Intent(DeviceEnvironmentActivity.this, HubFirmwareUpdateActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("targetDeviceId", mHub.deviceId);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                                    }
                                });
                    }

                    // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
                    if (DBG) Log.d(TAG, "hub ver: " + mHub.firmwareVersion + " / " + mPreferenceMgr.getHubVersion() + " / " + mPreferenceMgr.getHubForceVersion());
                    boolean updateHubForce = new VersionManager(mContext).checkUpdateAvailable(mHub.firmwareVersion, mPreferenceMgr.getHubForceVersion());
                    if (updateHubForce) {
                        try {
                            mDlgPackageSecurityPatchUpdate.show();
                        } catch (Exception e) {

                        }
                    }
                } else {
                    ivToolbarNewRight.setVisibility(View.GONE);
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
                Intent intent = new Intent(DeviceEnvironmentActivity.this, SettingEnvironmentActivity.class);
                intent.putExtra("targetDeviceId", mConnectedDeviceId);
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
                if (mHub == null) return;
                // 현재상태 Selected: Disabled, Not Selected: Enabled
                final boolean alarmEnabled = btnToolbarRight2.isSelected();
                mServerQueryMgr.setDeviceAlarmStatus(mHub.type, mHub.deviceId, NotificationType.DEVICE_ALL, alarmEnabled,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    if (DBG) Log.d(TAG, "Set alarm status succeeded");
                                    mPreferenceMgr.setDeviceAlarmEnabled(
                                            mHub.type,
                                            mHub.deviceId,
                                            NotificationType.DEVICE_ALL,
                                            alarmEnabled);
                                    btnToolbarRight2.setSelected(!btnToolbarRight2.isSelected());
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
        ivToolbarNewRight.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
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
                if (mCurrentViewIndex != VIEW_ENVIRONMENT_STATUS) {
                    _showFragment(VIEW_ENVIRONMENT_STATUS);
                    _selectTabButton(VIEW_ENVIRONMENT_STATUS);
                }
                //viewPager.setCurrentItem(VIEW_ENVIRONMENT_STATUS);
            }
        });
        btnTabGraph = (Button)findViewById(R.id.btn_tabbar_device_detail_item2);
        btnTabGraph.setText(getString(R.string.tab_graph));
        btnTabGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentViewIndex != VIEW_ENVIRONMENT_GRAPH) {
                    _showFragment(VIEW_ENVIRONMENT_GRAPH);
                    _selectTabButton(VIEW_ENVIRONMENT_GRAPH);

                    if (mFinishedGetGraph == false) {
                        if (mReceivingDataDialog != null && !mReceivingDataDialog.isShowing()) {
                            try {
                                mReceivingDataDialog.show();
                            } catch (Exception e) {

                            }
                        }
                    }
                }
                //viewPager.setCurrentItem(VIEW_ENVIRONMENT_GRAPH);
            }
        });
        //btnTabGraph.setVisibility(View.GONE);

        btnTabNotification = (Button)findViewById(R.id.btn_tabbar_device_detail_item3);
        btnTabNotification.setText(getString(R.string.tab_notification));
        btnTabNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentViewIndex != VIEW_ENVIRONMENT_NOTIFICATION) {
                    _showFragment(VIEW_ENVIRONMENT_NOTIFICATION);
                    _selectTabButton(VIEW_ENVIRONMENT_NOTIFICATION);

                    if (mFinishedGetNotification == false) {
                        if (mReceivingDataDialog != null && !mReceivingDataDialog.isShowing()) {
                            try {
                                mReceivingDataDialog.show();
                            } catch (Exception e) {

                            }
                        }
                    }
                }
                //viewPager.setCurrentItem(VIEW_ENVIRONMENT_NOTIFICATION);
            }
        });

        ivTabNotificationNew = (ImageView)findViewById(R.id.iv_tabbar_device_detail_item3_new);

        mReceivingDataDialog = new ProgressHorizontalDialog(mContext,
                getString(R.string.dialog_update_hub_information),
                getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mReceivingDataDialog.dismiss();
                    }
                });
        // Creating TabPagerAdapter adapter
        /*
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), 3);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {;}

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case VIEW_ENVIRONMENT_STATUS:
                        if (mEnvironmentStatusFragment != null) {
                            mEnvironmentStatusFragment.onResume();
                        }
                        break;
                    case VIEW_ENVIRONMENT_NOTIFICATION:
                        if (mNotificationFragment != null) {
                            mNotificationFragment.onResume();
                        }
                        break;
                }
                _selectTabButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {;}
        });
        */
    }

    private void _showFragment(int idx) {
        Fragment fr = null;
        switch(idx) {
            case VIEW_ENVIRONMENT_STATUS:
                if (Configuration.MONIT20_HUB) {
                    if (mEnvironmentStatusFragment2 == null) {
                        mEnvironmentStatusFragment2 = new EnvironmentStatus2Fragment();
                    }
                    fr = mEnvironmentStatusFragment2;
                } else {
                    if (mEnvironmentStatusFragment == null) {
                        mEnvironmentStatusFragment = new EnvironmentStatusFragment();
                    }
                    fr = mEnvironmentStatusFragment;
                }
                break;
            case VIEW_ENVIRONMENT_GRAPH:
                if (mGraphFragment == null) {
                    mGraphFragment = new EnvironmentGraph2Fragment();
                }
                fr = mGraphFragment;
                break;
            case VIEW_ENVIRONMENT_NOTIFICATION:
                if (mNotificationFragment == null) {
                    mNotificationFragment = new EnvironmentNotificationFragment();
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

    public void updateLampPower(int power) {
        mConnectionMgr.updateDeviceLampPower(DeviceType.AIR_QUALITY_MONITORING_HUB, mConnectedDeviceId, power);
    }

    public void updateLampBrightLevel(int brightLevel) {
        mConnectionMgr.updateDeviceLampBrightLevel(DeviceType.AIR_QUALITY_MONITORING_HUB, mConnectedDeviceId, brightLevel);
    }

    public void updateLampOffTimer(long utcTimeMs) {
        mConnectionMgr.updateDeviceLampOffTimer(DeviceType.AIR_QUALITY_MONITORING_HUB, mConnectedDeviceId, utcTimeMs);
    }

    private void _selectTabButton(int position) {
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
            case VIEW_ENVIRONMENT_STATUS:
                btnTabStatus.setSelected(true);
                btnTabStatus.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabStatus).setTypeface("medium");
                break;
            case VIEW_ENVIRONMENT_GRAPH:
                btnTabGraph.setSelected(true);
                btnTabGraph.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabGraph).setTypeface("medium");
                break;
            case VIEW_ENVIRONMENT_NOTIFICATION:
                btnTabNotification.setSelected(true);
                btnTabNotification.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabNotification).setTypeface("medium");
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
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
                            mConnectionMgr.getHubGraphList(mHub);

                            if (mCurrentViewIndex == VIEW_ENVIRONMENT_NOTIFICATION) {
                                mNotificationFragment.showFilteredList();
                            }
                        }
                    } else {
                        if (mReceivingDataDialog != null) {
                            mReceivingDataDialog.setProgress(currIdx * 100 / totalIdx);
                        }
                    }
                    break;
                case ConnectionManager.MSG_HUB_GRAPH_DATA_RECEIVED:
                    int hubGraphCurrIdx = msg.arg1;
                    int hubGraphTotalIdx = msg.arg2;

                    if (mFinishedGetNotification) {
                        if (DBG) Log.d(TAG, "MSG_HUB_GRAPH_DATA_RECEIVED: " + hubGraphCurrIdx + " / " + hubGraphTotalIdx);
                        if (hubGraphTotalIdx == 0) {
                            if (mReceivingDataDialog != null) {
                                mFinishedGetGraph = true;
                                mReceivingDataDialog.setProgress(100);
                                try {
                                    mReceivingDataDialog.dismiss();
                                } catch (Exception e) {

                                }
                                if (mCurrentViewIndex == VIEW_ENVIRONMENT_GRAPH) {
                                    mGraphFragment.updateView();
                                }
                            }
                        } else {
                            if (mReceivingDataDialog != null) {
                                mReceivingDataDialog.setProgress(hubGraphCurrIdx * 100 / hubGraphTotalIdx);
                            }
                        }
                    } else {
                        if (DBG) Log.d(TAG, "MSG_HUB_GRAPH_DATA_RECEIVED noti not finished: " + hubGraphCurrIdx + " / " + hubGraphTotalIdx);
                    }
                    break;
                case ConnectionManager.MSG_WIFI_CONNECTION_STATE_CHANGE:
                    final int wifiConnectionState = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_WIFI_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "/" + mConnectedDeviceId + "] " + wifiConnectionState + " / ");
                    if (mConnectedDeviceId == deviceInfo.deviceId) {
                        if (Configuration.MONIT20_HUB) {
                            if (mEnvironmentStatusFragment2 != null) {
                                if (mHub != null && mHub.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
                                    mEnvironmentStatusFragment2.setConnected(true);
                                } else {
                                    mEnvironmentStatusFragment2.setConnected(false);
                                }
                            }
                        } else {
                            if (mEnvironmentStatusFragment != null) {
                                if (mHub != null && mHub.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
                                    mEnvironmentStatusFragment.setConnected(true);
                                } else {
                                    mEnvironmentStatusFragment.setConnected(false);
                                }
                            }
                        }
                    }
                    break;

                case ConnectionManager.MSG_LAMP_VALUE_UPDATED:
                    final int deviceId = msg.arg1;
                    final CurrentLampValue currLampValue = (CurrentLampValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_LAMP_VALUE_UPDATED : [" + deviceId + "] " + currLampValue.toString());
                    if (mCurrentViewIndex == VIEW_ENVIRONMENT_STATUS &&
                            mEnvironmentStatusFragment != null) {
                        mEnvironmentStatusFragment.setConnected(true);
                        if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                            currLampValue.temperature = UnitConvertUtil.getFahrenheitFromCelsius(currLampValue.temperature);
                        }
                        mEnvironmentStatusFragment.setEnvironmentValue(currLampValue);
                    }
                    break;
                case ConnectionManager.MSG_SENSOR_VALUE_UPDATED:
                    final int deviceId2 = msg.arg1;
                    final CurrentSensorValue sensorValue = (CurrentSensorValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_SENSOR_VALUE_UPDATED : [" + deviceId2 + "] " + sensorValue.toString());
                    break;

                case ConnectionManager.MSG_NOTIFICATION_MESSAGE_UPDATED:
                    if (DBG) Log.d(TAG, "MSG_NOTIFICATION_MESSAGE_UPDATED");
                    updateNewMark();
                    if (mNotificationFragment != null && mCurrentViewIndex == VIEW_ENVIRONMENT_NOTIFICATION) {
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
    /*
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
                case VIEW_ENVIRONMENT_STATUS:
                    if (mEnvironmentStatusFragment == null) {
                        mEnvironmentStatusFragment = new EnvironmentStatusFragment();
                    }
                    return mEnvironmentStatusFragment;
                case VIEW_ENVIRONMENT_GRAPH:
                    if (mGraphFragment == null) {
                        mGraphFragment = new DeviceGraphFragment();
                    }
                    return mGraphFragment;
                case VIEW_ENVIRONMENT_NOTIFICATION:
                    if (mNotificationFragment == null) {
                        mNotificationFragment = new DeviceNotificationFragment();
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
    */
}
