package goodmonit.monit.com.kao.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.Policy;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceElderlyDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.devices.FeedingType;
import goodmonit.monit.com.kao.dfu.FirmwareUpdateActivity;
import goodmonit.monit.com.kao.dfu.HubFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dfu.LampFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dialog.DialogType;
import goodmonit.monit.com.kao.dialog.DiaperInputDialog;
import goodmonit.monit.com.kao.dialog.DoNotShowDialog;
import goodmonit.monit.com.kao.dialog.FeedingInputDialog;
import goodmonit.monit.com.kao.dialog.NoticeDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.dialog.SleepInputDialog;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.NotiManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.PushManager;
import goodmonit.monit.com.kao.managers.ScreenAnalyticsManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.managers.sm;
import goodmonit.monit.com.kao.message.FeedbackMsgAdapter;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationMsgAdapter;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.message.RecyclerViewAdapter;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.CardNoticeButton;
import goodmonit.monit.com.kao.widget.DeviceStatusRowDiaperSensor;
import goodmonit.monit.com.kao.widget.DeviceStatusRowDiaperSensor2;
import goodmonit.monit.com.kao.widget.DeviceStatusRowElderlyDiaperSensor;
import goodmonit.monit.com.kao.widget.DeviceStatusRowEnvironment;
import goodmonit.monit.com.kao.widget.DeviceStatusRowLamp;

import static goodmonit.monit.com.kao.activity.DeviceSensorActivity.REQCODE_FLOAT_ADD_NOTIFICATION;
import static goodmonit.monit.com.kao.message.RecyclerViewAdapter.LOADING_MESSAGE_SEC;

public class MainLightActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = Configuration.BASE_TAG + "MainLight";
    private static final boolean DBG = Configuration.DBG;

    public static final int MODE_SHOW_DEVICE_DETAIL_PAGE    			= 1;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH				= 1;
    private static final int REQUEST_CODE_ALLOW_PERMISSIONS             = 2;
    private static final int REQUEST_CODE_AGREEMENT                     = 3;
    private static final int REQUEST_CODE_NOTICE                        = 4;

    private static final int MSG_REFRESH_VIEW                    = 1;
    private static final int MSG_UPDATE_CONNECTED_DEVICE_COUNT   = 2;
    private static final int MSG_FULL_SYNC_FINISHED              = 3;
    private static final int MSG_SHOW_NOTICE                     = 4;
    private static final int MSG_CHECK_STEP                      = 5;
    private static final int MSG_SHOW_MAINTENANCE_NOTICE         = 6;
    private static final int MSG_SEND_SCREEN_ANALYTICS           = 7;

    private static final int STEP_SHOW_AGREEMENT                = 1;
    private static final int STEP_SHOW_ALLOW_PERMISSION_DIALOG  = 2;
    private static final int STEP_FORCE_CLOSE_WARNING_DIALOG    = 3;
    private static final int STEP_SHOW_NOTICE                   = 4;
    private static final int STEP_ALLOW_BLUETOOTH_TURN_ON       = 5;
    private static final int STEP_INPUT_NICKNAME                = 6;
    private static final int STEP_COMPLETED                     = 7;
    private int mCurrentStep = STEP_SHOW_AGREEMENT;

    private static final int REFRESH_VIEW_INTERVAL_SEC          = 1;

    /** UI Resources */
    // 1st page
    private LinearLayout lctnEmptyList;

    // 2st page
    private LinearLayout lctnDeviceList;
    private SwipeRefreshLayout srlDeviceList;

    private HashMap<Long, DeviceStatusRowLamp> mDeviceLampRows;
    private HashMap<Long, DeviceStatusRowEnvironment> mDeviceEnvironmentRows;
    private HashMap<Long, DeviceStatusRowDiaperSensor> mDeviceDiaperSensorRows;
    private HashMap<Long, DeviceStatusRowDiaperSensor2> mDeviceDiaperSensorRows2;
    private HashMap<Long, DeviceStatusRowElderlyDiaperSensor> mDeviceElderlyDiaperSensorRows;

    private DeviceStatusRowEnvironment mDeviceEnvironment;
    private DeviceStatusRowDiaperSensor mDeviceDiaperSensor;
    private DeviceStatusRowDiaperSensor2 mDeviceDiaperSensor2;
    private DeviceStatusRowLamp mDeviceLamp;

    private boolean isLoadedOnce;
    private boolean didRefreshScreen;
    private ImageButton btnGroup;
    private Button btnShare;

    private SimpleDialog mDlgInputNickname, mDlgMaintenanceNotice;
    private SimpleDialog mDlgConnectHub;
    private DoNotShowDialog mDlgCloseWarning;
    private NoticeDialog mDlgNotice;

    private EnvironmentCheckManager mEnvironmentMgr;
    private VersionManager mVersionMgr;
    private UserInfoManager mUserInfoMgr;
    private sm mStringMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_light);
        _setToolBar();

        if (DBG) Log.i(TAG, "onCreate");

        mContext = this;
        mDeviceEnvironmentRows = new HashMap<>();
        mDeviceDiaperSensorRows = new HashMap<>();
        mDeviceDiaperSensorRows2 = new HashMap<>();
        mDeviceElderlyDiaperSensorRows = new HashMap<>();
        mDeviceLampRows = new HashMap<>();
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mEnvironmentMgr = new EnvironmentCheckManager(this);
        mVersionMgr = new VersionManager(this);
        mUserInfoMgr = UserInfoManager.getInstance(this);
        mStringMgr = new sm();
        mScreenInfo = new ScreenInfo(501);

        if (DBG) Log.i (TAG, "Push : " + mPreferenceMgr.getPushToken());
        _initView();

        isLoadedOnce = true;
        didRefreshScreen = false;

        // 앱 로컬버전 업데이트
        if (mPreferenceMgr.getLocalVersion().compareTo(mPreferenceMgr.getLatestInstalledVersion()) > 0) {
            if (DBG) Log.i(TAG, "new version installed");
            mPreferenceMgr.setLatestInstalledVersion(mPreferenceMgr.getLocalVersion());
        }

        mHandler.sendEmptyMessage(MSG_CHECK_STEP);

        Crashlytics.setInt(mServerQueryMgr.getParameter(3), (int)mPreferenceMgr.getAccountId());

        _checkStartPage();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DBG) Log.i(TAG, "onNewIntent");
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
        mConnectionMgr = ConnectionManager.getInstance(mHandler);

        _checkSignInState(); // Signin State와 Valid Token 확인

        if (isLoadedOnce) {
            if (!Configuration.NO_INTERNET) {
                PushManager.getInstance(mContext).checkPushUpdated();

                showProgressBar(true);
                //mServerQueryMgr.getPushToken(mContext); Pushy 서비스는 아직 사용 안함
                mServerQueryMgr.init(new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errcode, String data) {
                        if (responseCode == ServerManager.RESPONSE_CODE_OK && InternetErrorCode.SUCCEEDED.equals(errcode)) {
                            int runningMode = ServerManager.getIntFromJSONObj(data, mServerQueryMgr.getParameter(70));
                            Configuration.setRunningMode(runningMode);
                        }
                    }
                });
                fullSyncWithCloud();
                mServerQueryMgr.setAccountActiveUser(null);
            } else {
                mHandler.obtainMessage(MSG_UPDATE_CONNECTED_DEVICE_COUNT, ConnectionManager.getRegisteredDeviceTotalCount(), -1).sendToTarget();
            }
            isLoadedOnce = false;
            // Always Full Sync
        } else {
            if (!Configuration.NO_INTERNET) {
                if (mConnectionMgr != null) {
                    showProgressBar(true);
                    fullSyncWithCloud();
//                    mConnectionMgr.updateDeviceFullStatusFromCloud(new ServerManager.ServerResponseListener() {
//                        @Override
//                        public void onReceive(int responseCode, String errCode, String data) {
//                            showProgressBar(false);
//                            mHandler.obtainMessage(MSG_UPDATE_CONNECTED_DEVICE_COUNT, ConnectionManager.getRegisteredDeviceTotalCount(), -1).sendToTarget();
//                        }
//                    });
                }
            } else {
                mHandler.obtainMessage(MSG_UPDATE_CONNECTED_DEVICE_COUNT, ConnectionManager.getRegisteredDeviceTotalCount(), -1).sendToTarget();
            }
        }

        if (mConnectionMgr != null) {
            mConnectionMgr.reconnectBleDevice();
        }

        _checkStartPage();
        _checkMaintenance();
        _checkShowHubConnectDialog();
        _sendScreenAnalytics();

        mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);

        if (Configuration.BETA_TEST_MODE) {
            onResumeNotification();
        }

        if (Configuration.MONIT_ELDERLY_TEST) {
            onResumeElderlyNotification();
        }
    }

    private void _sendScreenAnalytics() {
        mHandler.sendEmptyMessageDelayed(MSG_SEND_SCREEN_ANALYTICS, 1000);
    }

    private void _checkStartPage() {
        Intent notiIntent = getIntent();
        int mode = notiIntent.getIntExtra("mode", 0);
        int deviceType = notiIntent.getIntExtra("deviceType", 0);
        long deviceId = notiIntent.getLongExtra("deviceId", 0);

        if (DBG) Log.i(TAG, "checkStartPage: " + mode + " / " + deviceType + " / " + deviceId);

        if (mode == MODE_SHOW_DEVICE_DETAIL_PAGE) {
            switch(deviceType) {
                case DeviceType.DIAPER_SENSOR:
                    Intent intentDiaperSensor = new Intent(MainLightActivity.this, DeviceSensorActivity.class);
                    intentDiaperSensor.putExtra("targetDeviceId", deviceId);
                    startActivity(intentDiaperSensor);
                    overridePendingTransition(0, 0);
                    break;
                case DeviceType.AIR_QUALITY_MONITORING_HUB:
                    Intent intentAQMHub = new Intent(MainLightActivity.this, DeviceEnvironmentActivity.class);
                    intentAQMHub.putExtra("targetDeviceId", deviceId);
                    startActivity(intentAQMHub);
                    overridePendingTransition(0, 0);
                    break;
                case DeviceType.LAMP:
                    Intent intentLamp = new Intent(MainLightActivity.this, DeviceLampActivity.class);
                    intentLamp.putExtra("targetDeviceId", deviceId);
                    startActivity(intentLamp);
                    overridePendingTransition(0, 0);
                    break;
            }
        }

        setIntent(new Intent());
    }

    private void _checkSignInState() {
        if (DBG) Log.d(TAG, "checkSignInState: " + mPreferenceMgr.getSigninState() + " / " + mPreferenceMgr.getSigninEmail());
        if (mPreferenceMgr.getSigninState() == SignInState.STEP_SIGN_IN ||  mPreferenceMgr.getSigninEmail() == null) {
            NotiManager.getInstance(mContext).cancelMessageNotification(); // cancel all notification
            Intent intent = null;

            switch (Configuration.APP_MODE) {
                case Configuration.APP_GLOBAL:
                case Configuration.APP_KC_HUGGIES_X_MONIT:
                case Configuration.APP_MONIT_X_KAO:
                    intent = new Intent(MainLightActivity.this, SigninActivity.class);
                    break;
//                //case Configuration.APP_MONIT_X_HUGGIES:
//                    intent = new Intent(MainLightActivity.this, YKSigninActivity.class);
//                    break;
            }

            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }

        checkInvalidToken();
    }

    public void checkInvalidToken() {
        mHandler.obtainMessage(ConnectionManager.MSG_CHECK_INVALID_TOKEN).sendToTarget();
    }

    private void _checkMaintenance() {
        // Notice띄워줄 내용이 있는지 확인하고 띄워준다.
        mServerQueryMgr.getMaintenance(new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                    int isServerRunning = 1;
                    try {
                        JSONObject jObject = new JSONObject(data);
                        isServerRunning = jObject.optInt(mServerQueryMgr.getParameter(102), 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (isServerRunning == 0) { // 긴급유지보수로 서버가 실행되지 않는 경우
                        mServerQueryMgr.getMaintenanceNotice(new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    mHandler.obtainMessage(MSG_SHOW_MAINTENANCE_NOTICE, data).sendToTarget();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void _showMaintenance(String data) {
        try {
            JSONObject jObject = new JSONObject(data);
            String title = jObject.getString(mServerQueryMgr.getParameter(71));
            String contents = jObject.getString(mServerQueryMgr.getParameter(101));
            String fromStrTimeUtc = jObject.getString(mServerQueryMgr.getParameter(73));
            String toStrTimeUtc = jObject.getString(mServerQueryMgr.getParameter(103));

            if ((title == null || title.length() == 0)
                    || (contents == null || contents.length() == 0)) {
                return;
            }
            contents = contents.replace("\\n", "\n");

            String duration = "";
            if (fromStrTimeUtc != null && fromStrTimeUtc.length() > 0) {
                String localDateTimeFrom = DateTimeUtil.getLocalDateTimeStringFromUTCDateTimeString("yyMMdd-HHmmss", "MMM dd, aaa hh:mm", fromStrTimeUtc);
                duration += "\n" + getString(R.string.time_from) + ": " + localDateTimeFrom;
            }
            if (toStrTimeUtc != null && toStrTimeUtc.length() > 0) {
                String localDateTimeTo = DateTimeUtil.getLocalDateTimeStringFromUTCDateTimeString("yyMMdd-HHmmss", "MMM dd, aaa hh:mm", toStrTimeUtc);
                duration += "\n" + getString(R.string.time_to) + ": " + localDateTimeTo;
            }

            mDlgMaintenanceNotice = new SimpleDialog(
                    MainLightActivity.this,
                    title,
                    contents + duration,
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

            if (mDlgMaintenanceNotice != null && !mDlgMaintenanceNotice.isShowing()) {
                try {
                    mDlgMaintenanceNotice.show();
                } catch (Exception e) {

                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

    }

    private void _checkBluetoothEnabled() {
        if (Configuration.MONIT_ELDERLY_TEST) {
            mCurrentStep = STEP_INPUT_NICKNAME;
            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
            return;
        }

        if (ConnectionManager.checkBluetoothStatus() == ConnectionManager.STATE_DISABLED) {
            if (DBG) Log.d(TAG, "show checkBluetoothEnabled");
            Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnableIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            mCurrentStep = STEP_INPUT_NICKNAME;
            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
        }
    }

    private void _checkNicknameSetting() {
        if (mPreferenceMgr.getProfileNickname() == null || mPreferenceMgr.getProfileNickname().length() == 0 || "null".equals(mPreferenceMgr.getProfileNickname())) {
            if (mDlgInputNickname == null) {
                mDlgInputNickname = new SimpleDialog(
                        mContext,
                        getString(R.string.account_change_nickname),
                        getString(R.string.dialog_contents_input_nickname),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mDlgInputNickname.getInputText().length() == 0 || mDlgInputNickname.getInputText().length() > 12) {
                                    showToast(getString(R.string.account_warning_nickname));
                                } else {
                                    mServerQueryMgr.changeNickname(mDlgInputNickname.getInputText(), new ServerManager.ServerResponseListener() {
                                        @Override
                                        public void onReceive(int responseCode, String errCode, String data) {
                                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                                if (DBG)
                                                    Log.d(TAG, "nickname : " + data);
                                                mDlgInputNickname.dismiss();
                                                mPreferenceMgr.setProfileNickname(mDlgInputNickname.getInputText());
                                                mCurrentStep = STEP_COMPLETED;
                                                mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                                                showToast(getString(R.string.toast_change_nickname_succeeded));
                                            } else {
                                                showToast(getString(R.string.toast_change_nickname_failed));
                                            }
                                        }
                                    });
                                }
                            }
                        });
                mDlgInputNickname.setInputMode(true);
            }
            if (mDlgInputNickname != null && !mDlgInputNickname.isShowing()) {
                try {
                    if (DBG) Log.d(TAG, "show nickname setting");
                    mDlgInputNickname.show();
                } catch (Exception e) {

                }
            }
        } else {
            mCurrentStep = STEP_COMPLETED;
            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
        }
    }

    private void _checkForceClosedWarningDialog() {
        /*
        안드로이드는 강제종료되도 앱 자동 재시작되므로 다이얼로그 필요없음
        if (!mPreferenceMgr.getDoNotShowDialog(DialogType.FORCE_CLOSED_WARNING)) {
            mDlgCloseWarning = new DoNotShowDialog(mContext,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.isSelected()) {
                                v.setSelected(false);
                                mPreferenceMgr.setDoNotShowDialog(DialogType.FORCE_CLOSED_WARNING, false);
                            } else {
                                v.setSelected(true);
                                mPreferenceMgr.setDoNotShowDialog(DialogType.FORCE_CLOSED_WARNING, true);
                            }
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgCloseWarning.dismiss();
                            mCurrentStep = STEP_SHOW_NOTICE;
                            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                        }
                    });
            if (mDlgCloseWarning != null && !mDlgCloseWarning.isShowing()) {
                try {
                    mDlgCloseWarning.show();
                } catch (Exception e) {

                }
            }
        } else {
            mCurrentStep = STEP_SHOW_NOTICE;
            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
        }
        */
        mCurrentStep = STEP_SHOW_NOTICE;
        mHandler.sendEmptyMessage(MSG_CHECK_STEP);
    }

    private void _checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int permissionCheckFineLocation = mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            int permissionCheck = mContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(mContext.getPackageName());
            if (DBG) Log.d(TAG, "checkPermission: " + permissionCheck + " / " + isWhiteListing);
            // 앱 시작하자마자: 0, false
            // 거부/거부: -1, false
            // 허용/거부: 0, false
            // 거부/허용: -1, true
            // 허용/허용: 0, true
            if (permissionCheck != 0 || !isWhiteListing || permissionCheckFineLocation != 0) {
                if (DBG) Log.d(TAG, "show Permission");
                Intent allowPermissionIntent = new Intent(MainLightActivity.this, GuideAllowPermission.class);
                startActivityForResult(allowPermissionIntent, REQUEST_CODE_ALLOW_PERMISSIONS);
            } else {
                mCurrentStep = STEP_FORCE_CLOSE_WARNING_DIALOG;
                mHandler.sendEmptyMessage(MSG_CHECK_STEP);
            }
        } else {
            mCurrentStep = STEP_FORCE_CLOSE_WARNING_DIALOG;
            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
        }
    }

    private void _checkAgreement() {
        mServerQueryMgr.getPolicy(new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                    try {
                        JSONObject jObject = new JSONObject(data);
                        String policyData = jObject.getString(mServerQueryMgr.getParameter(11));
                        JSONArray jarr = new JSONArray(policyData);
                        for (int i = 0; i < jarr.length(); i++) {
                            JSONObject jobj = jarr.getJSONObject(i);
                            int ptype = jobj.getInt(mServerQueryMgr.getParameter(20));
                            int agree = jobj.getInt(mServerQueryMgr.getParameter(98));
                            String time = jobj.getString(mServerQueryMgr.getParameter(15));

                            mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), ptype, agree);
                            mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), ptype, time);
                            if (DBG) Log.d(TAG, "[policy] " + ptype + " / " + agree + " / " + time);
                        }
                    } catch (JSONException j) {
                        if (DBG) Log.e(TAG, "JSONException: " + j.toString());
                    } finally {
                        switch (Configuration.APP_MODE) {
                            case Configuration.APP_GLOBAL:
                            case Configuration.APP_KC_HUGGIES_X_MONIT:
                            case Configuration.APP_MONIT_X_KAO:
                                break;
//                            case Configuration.APP_MONIT_X_HUGGIES:
//                                boolean policyValid = true;
//                                if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_KR) != 1)
//                                        && (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_TERMS_OF_USE_KR) != 1)) {
//                                    if (DBG) Log.e(TAG, "NOT Agreed Terms of use");
//                                    policyValid = false;
//                                }
//
//                                if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_KR) != 1)
//                                        && (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PRIVACY_KR) != 1)) {
//                                    if (DBG) Log.e(TAG, "NOT Agreed Privacy");
//                                    policyValid = false;
//                                }
//
//                                if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.COLLECT_INFO_KR) != 1)
//                                        && (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_COLLECT_INFO_KR) != 1)) {
//                                    if (DBG) Log.e(TAG, "NOT Agreed CollectInfo");
//                                    policyValid = false;
//                                }
//
//                                if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PROVIDE_3RD_PARTY_KR) != 1)
//                                        && (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PROVIDE_3RD_PARTY_KR) != 1)) {
//                                    if (DBG) Log.e(TAG, "NOT Agreed 3rdParty");
//                                    policyValid = false;
//                                }
//
//                                if (!policyValid) {
//                                    if (DBG) Log.d(TAG, "show Agreement");
//                                    Intent agreement = new Intent(MainLightActivity.this, AgreementActivity.class);
//                                    agreement.putExtra(mStringMgr.getParameter(84), data);
//                                    startActivityForResult(agreement, REQUEST_CODE_AGREEMENT);
//                                    return;
//                                }
//                                break;
                        }

                        mCurrentStep = STEP_SHOW_ALLOW_PERMISSION_DIALOG;
                        mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                    }

                } else {
                    if (DBG) Log.e(TAG, "getPolicy failed");
                }
            }
        });
    }

    private void _checkNotice() {
        // Notice띄워줄 내용이 있는지 확인하고 띄워준다.
        mServerQueryMgr.getNotice(new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                    mHandler.obtainMessage(MSG_SHOW_NOTICE, data).sendToTarget();
                } else {
                    mCurrentStep = STEP_ALLOW_BLUETOOTH_TURN_ON;
                    mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                }
            }
        });
    }

    private void _showNotice(String data) {
        String title = "";
        String contents = "";
        int noticeId = -1;
        int noticeType = -1;
        int boardType = 0;
        int boardId = 0;
        int cntShowingDialog = 0;
        try {
            JSONObject wholeObj = new JSONObject(data);
            JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(11));
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jObject = jarr.getJSONObject(i);
                title = jObject.getString(mServerQueryMgr.getParameter(71));
                contents = jObject.getString(mServerQueryMgr.getParameter(101));
                noticeId = jObject.optInt(mServerQueryMgr.getParameter(99), -1);
                noticeType = jObject.optInt(mServerQueryMgr.getParameter(100), -1);
                boardType = jObject.optInt(mServerQueryMgr.getParameter(123), 0);
                boardId = jObject.optInt(mServerQueryMgr.getParameter(124), 0);

                if ((title == null || title.length() == 0)
                        || (contents == null || contents.length() == 0)
                        || (noticeId == -1 || noticeType == -1)) {
                    continue;
                }
                contents = contents.replace("\\n", "\n");

                final int doNotShowDialogNoticeId = DialogType.NOTICE + noticeId;

                if (!mPreferenceMgr.getDoNotShowDialog(doNotShowDialogNoticeId)) {
                    mDlgNotice = new NoticeDialog(MainLightActivity.this, title, contents);
                    final NoticeDialog dlgNotice = mDlgNotice;
                    dlgNotice.setDoNotShowButtonListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (v.isSelected()) {
                                        v.setSelected(false);
                                        mPreferenceMgr.setDoNotShowDialog(doNotShowDialogNoticeId, false);
                                    } else {
                                        v.setSelected(true);
                                        mPreferenceMgr.setDoNotShowDialog(doNotShowDialogNoticeId, true);
                                    }
                                }
                            }
                    );
                    dlgNotice.setCloseButtonListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dlgNotice.dismiss();
                                    mCurrentStep = STEP_ALLOW_BLUETOOTH_TURN_ON;
                                    mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                                }
                            }
                    );

                    if (boardType > 0 && boardId > 0) {
                        final Intent noticeIntent = new Intent(MainLightActivity.this, NoticeActivity.class);
                        noticeIntent.putExtra("boardType", boardType);
                        noticeIntent.putExtra("boardId", boardId);
                        dlgNotice.showMoreButton(true);
                        dlgNotice.setMoreButtonListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dlgNotice.dismiss();
                                        startActivityForResult(noticeIntent, REQUEST_CODE_NOTICE);
                                    }
                                }
                        );
                    } else {
                        dlgNotice.showMoreButton(false);
                    }



                    if (dlgNotice != null && !dlgNotice.isShowing()) {
                        try {
                            dlgNotice.show();
                            cntShowingDialog++;
                        } catch (Exception e) {

                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 띄워진 공지사항이 아무것도 없으면 블루투스 체크로 이동
        if (cntShowingDialog == 0) {
            mCurrentStep = STEP_ALLOW_BLUETOOTH_TURN_ON;
            mHandler.sendEmptyMessage(MSG_CHECK_STEP);
        }
    }

    private void fullSyncWithCloud() {
        if (mConnectionMgr != null) {
            mConnectionMgr.getUserInfoFromCloud();
            mConnectionMgr.reconnectBleDevice();
            mConnectionMgr.updateDeviceFullStatusFromCloud(new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errCode, String data) {
                    showProgressBar(false);
                    mHandler.obtainMessage(MSG_UPDATE_CONNECTED_DEVICE_COUNT, ConnectionManager.getRegisteredDeviceTotalCount(), -1).sendToTarget();
                    mHandler.obtainMessage(MSG_FULL_SYNC_FINISHED).sendToTarget();
                }
            });
        } else {
            showProgressBar(false);
            srlDeviceList.setRefreshing(false);
        }
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_light);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ivToolbarCenter = (ImageView) findViewById(R.id.iv_toolbar_main_light_center);
        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            //case Configuration.APP_MONIT_X_HUGGIES:
            case Configuration.APP_MONIT_X_KAO:
                ivToolbarCenter.setImageResource(R.drawable.ic_logo_main_light);
                break;
            case Configuration.APP_KC_HUGGIES_X_MONIT:
                ivToolbarCenter.setImageResource(R.drawable.ic_logo_main_light_kc);
                break;
        }

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_main_light_right);
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showSettingAccountActivity();
            }
        });

        btnShare = (Button) findViewById(R.id.btn_toolbar_main_light_right2);
        btnShare.setVisibility(View.VISIBLE);
        btnShare.setBackgroundResource(R.drawable.ic_diary_share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainLightActivity.this, GroupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
            }
        });

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_main_light_left);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                    _showConnectionActivity(ConnectionActivity.STEP_SELECT_PACKAGE);
                } else {
                    _showConnectionActivity(ConnectionActivity.STEP_SELECT_DEVICE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG, "onPause");
        mHandler.removeMessages(MSG_REFRESH_VIEW);
        mHandler.removeMessages(MSG_SEND_SCREEN_ANALYTICS);
    }

    @Override
    public void onRefresh() {
        if (!Configuration.NO_INTERNET) {
            didRefreshScreen = true;
            fullSyncWithCloud();
        } else {
            _refreshView();
            srlDeviceList.setRefreshing(false);
        }
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        srlDeviceList = (SwipeRefreshLayout)findViewById(R.id.srl_main_light_current_status);
        srlDeviceList.setOnRefreshListener(this);

        // Content1
        lctnEmptyList = (LinearLayout)findViewById(R.id.lctn_main_light_empty);

        // Content2
        lctnDeviceList = (LinearLayout)findViewById(R.id.lctn_main_light_current_status);

        // Set screen for No Connected Device
        lctnEmptyList.setVisibility(View.GONE);
        lctnDeviceList.setVisibility(View.GONE);

        btnGroup = (ImageButton)findViewById(R.id.btn_main_light_group);
        if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
            btnGroup.setBackgroundResource(R.drawable.btn_group_selector_kc);
        } else {
            btnGroup.setBackgroundResource(R.drawable.btn_group_selector);
        }
        btnGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainLightActivity.this, GroupActivity.class);
                startActivity(intent);
            }
        });
        btnGroup.setVisibility(View.GONE);

        rctnNotifiationList = (RelativeLayout)findViewById(R.id.rctn_notification_list);
    }

    private void _showConnectionActivity(int startPage) {
        Intent intent = new Intent(MainLightActivity.this, ConnectionActivity.class);
        if (startPage > 1) {
            intent.putExtra("startContent", startPage);
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void _showSettingAccountActivity() {
        Intent intent = new Intent(MainLightActivity.this, SettingAccountActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void _loadRowFromDeviceViewObject() {
        mDeviceDiaperSensorRows.clear();
        mDeviceDiaperSensorRows2.clear();
        mDeviceEnvironmentRows.clear();
        mDeviceLampRows.clear();
        mDeviceElderlyDiaperSensorRows.clear();

        lctnDeviceList.removeAllViews();

        /*
        SettingTextDivider elderlySensor1 = new SettingTextDivider(this);
        elderlySensor1.setTitle("조남춘_Elderly1");
        lctnDeviceList.addView(elderlySensor1);
        //_addElderlyDiaperSensorRows(1);
        //_addAQMHubRows(198);

        SettingTextDivider elderlySensor2 = new SettingTextDivider(this);
        elderlySensor2.setTitle("조점순_Elderly2");
        lctnDeviceList.addView(elderlySensor2);
        //_addElderlyDiaperSensorRows(2);
        //_addAQMHubRows(1560);
        */

        _addElderlyDiaperSensorRows();
        _addDiaperSensorRows();
        _addAQMHubRows();
        _addLampRows();

        _refreshView();
    }

    private void _refreshView() {
        if (DBG) Log.d(TAG, "_refreshView");
        if (Configuration.MONIT20) {
            for (DeviceStatusRowDiaperSensor2 row : mDeviceDiaperSensorRows2.values()) {
                long deviceId = row.getDeviceId();
                DeviceDiaperSensor sensor = ConnectionManager.getDeviceDiaperSensor(deviceId);
                if (sensor == null) continue;
                sensor.updateDiaperStatus();

                if (Configuration.MASTER) {
                    long whereConnId = sensor.getConnectionId();	// 1의자리에 Type이 있는 값
                    int whereConnType = (int)(sensor.getConnectionId() % 10);
                    if (whereConnId > 0) {
                        whereConnId = whereConnId / 10; // Type이외의 값을 ID로 전환
                        if (whereConnType == 0) { // 스마트폰에 붙었을 때,
                            String nickName = "";
                            if (mUserInfoMgr != null) nickName = mUserInfoMgr.getUserNickname(whereConnId);
                            row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nP" + whereConnId + "/" + nickName);
                        } else if (whereConnType == 2) { // 허브에 붙었을 때
                            String hubName = "";
                            if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
                            row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nH" + whereConnId + "/" + hubName);
                        } else if (whereConnType == 3) { // 허브에 붙었을 때
                            String hubName = "";
                            if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
                            row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nU" + whereConnId + "/" + hubName);
                        }
                    } else {
                        row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")");
                    }
                    row.setSleepStatus(sensor.sleepStatus);
                } else {
                    row.setDeviceName(sensor.name);
                }

                if (sensor.getConnectionState() == DeviceConnectionState.DISCONNECTED) {
                    row.setConnected(false);
                } else {
                    row.setConnectionType(sensor.getConnectionState());
                    row.setConnected(true);
                    if (Configuration.CERTIFICATE_MODE) {
                        row.setTemperature(sensor.getTemperature());
                        row.setHumidity(sensor.getHumidity());
                        row.setVoc(sensor.getVoc());
                        row.setDeviceName(row.getDeviceName() + "(Cert)");
                    } else {
                        row.setOperationStatus(sensor.getOperationStatus());
                        row.setVocStatus(sensor.getVocAvg());
                        if (Configuration.MONIT_AUTO_SLEEP_DETECTION) {
                            if (mPreferenceMgr.getAutoSleepingDetectionEnabled(sensor.deviceId)) {
                                int currentSleepingStatus = mPreferenceMgr.getDiaperSensorCurrentSleepingLevel(sensor.deviceId);
                                boolean isSleeping = false;
                                switch (currentSleepingStatus) {
                                    case DeviceStatus.MOVEMENT_SLEEP:
                                    case DeviceStatus.MOVEMENT_DEEP_SLEEP:
                                        isSleeping = true;
                                        break;
                                }
                                row.setMovementStatus(sensor.getMovementStatus(), isSleeping);
                            } else {
                                row.setMovementStatus(sensor.getMovementStatus(), mPreferenceMgr.getSleepingEnabled(sensor.deviceId));
                            }
                        } else {
                            row.setMovementStatus(sensor.getMovementStatus(), mPreferenceMgr.getSleepingEnabled(sensor.deviceId));
                        }
                        row.setDiaperScore(sensor.getDiaperScore());
                        row.setDiaperStatus(sensor.getDiaperStatus());
                        boolean charging = false;
                        switch (sensor.getOperationStatus()) {
                            case DeviceStatus.OPERATION_CABLE_CHARGING:
                            case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
                            case DeviceStatus.OPERATION_HUB_CHARGING:
                            case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
                                charging = true;
                        }
                        row.setBatteryStatus(sensor.batteryPower, charging);
                    }
                }
            }
        } else {
            for (DeviceStatusRowDiaperSensor row : mDeviceDiaperSensorRows.values()) {
                long deviceId = row.getDeviceId();
                DeviceDiaperSensor sensor = ConnectionManager.getDeviceDiaperSensor(deviceId);
                if (sensor == null) continue;
                sensor.updateDiaperStatus();

                if (Configuration.MASTER) {
                    long whereConnId = sensor.getConnectionId();	// 1의자리에 Type이 있는 값
                    int whereConnType = (int)(sensor.getConnectionId() % 10);
                    if (whereConnId > 0) {
                        whereConnId = whereConnId / 10; // Type이외의 값을 ID로 전환
                        if (whereConnType == 0) { // 스마트폰에 붙었을 때,
                            String nickName = "";
                            if (mUserInfoMgr != null) nickName = mUserInfoMgr.getUserNickname(whereConnId);
                            row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nP" + whereConnId + "/" + nickName);
                        } else if (whereConnType == 2) { // 허브에 붙었을 때
                            String hubName = "";
                            if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
                            row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nH" + whereConnId + "/" + hubName);
                        } else if (whereConnType == 3) { // 허브에 붙었을 때
                            String hubName = "";
                            if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
                            row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nU" + whereConnId + "/" + hubName);
                        }
                    } else {
                        row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")");
                    }
                } else {
                    row.setDeviceName(sensor.name);
                }
                if (sensor.getConnectionState() == DeviceConnectionState.DISCONNECTED) {
                    row.setConnected(false);
                } else {
                    row.setConnectionType(sensor.getConnectionState());
                    row.setConnected(true);
                    if (Configuration.CERTIFICATE_MODE) {
                        row.setTemperature(sensor.getTemperature());
                        row.setHumidity(sensor.getHumidity());
                        row.setVoc(sensor.getVoc());
                        row.setDeviceName(row.getDeviceName() + "(Cert)");
                    } else {
                        row.setOperationStatus(sensor.getOperationStatus());
                        row.setMovementStatus(sensor.getMovementStatus());
                        row.setDiaperStatus(sensor.getDiaperStatus());
                        boolean charging = false;
                        switch (sensor.getOperationStatus()) {
                            case DeviceStatus.OPERATION_CABLE_CHARGING:
                            case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
                            case DeviceStatus.OPERATION_HUB_CHARGING:
                            case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
                                charging = true;
                        }
                        row.setBatteryStatus(sensor.batteryPower, charging);
                    }
                }
            }
        }

        for (DeviceStatusRowEnvironment row : mDeviceEnvironmentRows.values()) {
            long deviceId = row.getDeviceId();
            DeviceAQMHub hub = ConnectionManager.getDeviceAQMHub(deviceId);
            if (hub == null) continue;

            if (Configuration.MASTER) {
                int attachedId = hub.getSensorAttached();
                if (attachedId != DeviceStatus.SENSOR_DETACHED) {
                    String sensorName = "";
                    if (mPreferenceMgr != null) sensorName = mPreferenceMgr.getDeviceName(DeviceType.DIAPER_SENSOR, attachedId);
                    row.setDeviceName(hub.name + "(" + hub.deviceId + ")" + "\nC" + attachedId + "/" + sensorName);
                } else {
                    row.setDeviceName(hub.name + "(" + hub.deviceId + ")");
                }
            } else {
                row.setDeviceName(hub.name);
            }
            if (Configuration.CERTIFICATE_MODE) {
                row.setDeviceName(row.getDeviceName() + "(Cert)");
            }
            if (hub.getConnectionState() == DeviceConnectionState.DISCONNECTED || hub.getTemperature() == -1 || hub.getHumidity() == -1) {
                row.setConnected(false);
            } else {
                row.setConnected(true);
                row.setBrightLevel(hub.getLampPower(), hub.getBrightLevel());
                row.setTemperature(hub.getTemperature());
                row.setHumidity(hub.getHumidity());

                boolean warning = hub.getTemperatureStatus() != EnvironmentCheckManager.NORMAL;
                warning = warning || (hub.getHumidityStatus() != EnvironmentCheckManager.NORMAL);

                // Set Warning
                row.setTemperatureWarning(hub.getTemperatureStatus());
                row.setHumidityWarning(hub.getHumidityStatus());
                if (hub.getSensorAttached() != DeviceStatus.SENSOR_DETACHED) {
                    row.setSensorAttached(true);
                    if (Configuration.CERTIFICATE_MODE) {
                        row.setVocStatus(hub.getVoc() + "");
                    } else {
                        row.setVocStatus(mEnvironmentMgr.getVocString(hub.getVoc()));
                    }
                    row.setVocWarning(hub.getVocStatus() != EnvironmentCheckManager.NORMAL);
                    warning = warning || (hub.getVocStatus() != EnvironmentCheckManager.NORMAL);
                } else {
                    row.setSensorAttached(false);
                }
            }
        }

        for (DeviceStatusRowLamp row : mDeviceLampRows.values()) {
            long deviceId = row.getDeviceId();
            DeviceLamp lamp = ConnectionManager.getDeviceLamp(deviceId);
            if (lamp == null) continue;

            if (Configuration.MASTER) {
                row.setDeviceName(lamp.name + "(" + lamp.deviceId + ")");
            } else {
                row.setDeviceName(lamp.name);
            }

            if (lamp.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED || lamp.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) { // 허브는 Wi-Fi Connected 일때에만 연결된 것으로 보여주기
                row.setConnectionType(lamp.getConnectionState());
                row.setConnected(true);
                row.setBrightLevel(lamp.getLampPower(), lamp.getBrightLevel());
                row.setTemperature(lamp.getTemperature());
                row.setHumidity(lamp.getHumidity());

                // Set Warning
                row.setTemperatureWarning(lamp.getTemperatureStatus());
                row.setHumidityWarning(lamp.getHumidityStatus());
            } else {
                row.setConnected(false);
            }
        }

        for (DeviceStatusRowElderlyDiaperSensor row : mDeviceElderlyDiaperSensorRows.values()) {
            if (row == null) continue;
            long deviceId = row.getDeviceId();
            DeviceElderlyDiaperSensor sensor = ConnectionManager.getDeviceElderlyDiaperSensor(deviceId);
            if (sensor == null) continue;
            sensor.updateDiaperStatus();

            // 알람 종료
            mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.PEE_DETECTED, true); // Default Off
            mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.POO_DETECTED, false); // Default Off
            mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.FART_DETECTED, false); // Default Off
            mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
            mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.CONNECTED, false);
            mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED, false);
            mServerQueryMgr.setDeviceAlarmStatus(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL, true,
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                if (DBG) Log.d(TAG, "Set alarm status succeeded");
                            } else {
                                if (DBG) Log.d(TAG, "Set alarm status failed");
                            }
                        }
                    });
            mServerQueryMgr.setDeviceAlarmStatus(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.PEE_DETECTED, true,
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                if (DBG) Log.d(TAG, "Set alarm status succeeded");
                            } else {
                                if (DBG) Log.d(TAG, "Set alarm status failed");
                            }
                        }
                    });

            row.setDeviceId(deviceId);
            row.setNotificationListOnUpdateView(new FeedbackMsgAdapter.OnUpdateViewListener() {
                @Override
                public void onUpdate() {
                    resumeElderlyNotification();
                }
            });

            if (Configuration.MASTER) {
                long whereConnId = sensor.getConnectionId();	// 1의자리에 Type이 있는 값
                int whereConnType = (int)(sensor.getConnectionId() % 10);
                if (whereConnId > 0) {
                    whereConnId = whereConnId / 10; // Type이외의 값을 ID로 전환
                    if (whereConnType == 0) { // 스마트폰에 붙었을 때,
                        String nickName = "";
                        if (mUserInfoMgr != null) nickName = mUserInfoMgr.getUserNickname(whereConnId);
                        row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nP" + whereConnId + "/" + nickName);
                    } else if (whereConnType == 2) { // 허브에 붙었을 때
                        String hubName = "";
                        if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
                        row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nH" + whereConnId + "/" + hubName);
                    } else if (whereConnType == 3) { // 허브에 붙었을 때
                        String hubName = "";
                        if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
                        row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")" + "\nU" + whereConnId + "/" + hubName);
                    }
                } else {
                    row.setDeviceName(sensor.name + "(" + sensor.deviceId + ")");
                }
            } else {
                row.setDeviceName(sensor.name);
            }

            if (sensor.getConnectionState() == DeviceConnectionState.DISCONNECTED) {
                row.setConnected(false);
            } else {
                row.setConnectionType(sensor.getConnectionState());
                row.setConnected(true);
                if (Configuration.CERTIFICATE_MODE) {
                    row.setTemperature(sensor.getTemperature());
                    row.setHumidity(sensor.getHumidity());
                    row.setVoc(sensor.getVoc());
                    row.setDeviceName(row.getDeviceName() + "(Cert)");
                } else {
                    row.setTemperature(sensor.getTemperature());
                    row.setHumidity(sensor.getHumidity());
                    row.setVoc(sensor.getVoc());
                    row.setOperationStatus(sensor.getOperationStatus());
                    row.setVocStatus(sensor.getVocAvg());
                    row.setMovementStatus(sensor.getMovementStatus(), mPreferenceMgr.getSleepingEnabled(sensor.deviceId));
                    row.setDiaperScore(sensor.getDiaperScore());
                    row.setDiaperStatus(sensor.getDiaperStatus());
                    boolean charging = false;
                    switch (sensor.getOperationStatus()) {
                        case DeviceStatus.OPERATION_CABLE_CHARGING:
                        case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
                        case DeviceStatus.OPERATION_HUB_CHARGING:
                        case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
                            charging = true;
                    }
                    row.setBatteryStatus(sensor.batteryPower, charging);
                }

                if (sensor.isStrapAttached()) {
                    row.setStrapAttached(true);
                    row.setStrapBatteryPower(sensor.getStrapBatteryPower());
                    row.setMultiTouch(sensor.getMultiTouch());
                    row.setContamination(sensor.getContamination());
                    row.setTouchDetectedInfo(sensor.getTouchDetectedCount(), sensor.getTouchLatestDetectedTimeMs());
                } else {
                    row.setStrapAttached(false);
                }
            }
        }

        checkInvalidToken();
    }

    private void _addAQMHubRows() {
        _addAQMHubRows(-1);
    }

    private void _addAQMHubRows(long selectId) {
        for (DeviceAQMHub hub : ConnectionManager.mRegisteredAQMHubList.values()) {
            if (hub == null) continue;
            final long deviceId = hub.deviceId;

            // 선택한 device가 없거나(-1) device를 선택한 경우 그것만 추가
            if (selectId != deviceId && selectId != -1) continue;
            // 이미 추가된 경우 SKIP
            if (mDeviceEnvironmentRows.get(deviceId) != null) continue;

            DeviceStatusRowEnvironment environment = new DeviceStatusRowEnvironment(mContext);
            environment.setDeviceId(deviceId);

            if (Configuration.MASTER) {
                environment.setDeviceName(hub.name + "(" + hub.deviceId + ")");
            } else {
                environment.setDeviceName(hub.name);
            }
            if (DBG) Log.d(TAG, "_addMonitHubRows : " + environment.getDeviceId() + " / " + environment.getDeviceName());

            if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                environment.setDeviceStatusItem3Visible(false);
            }

            if (hub.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                if (DBG) Log.d(TAG, "connected environment found : " + deviceId);
                environment.setConnected(true);
            } else {
                environment.setConnected(false);
            }

            environment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainLightActivity.this, DeviceEnvironmentActivity.class);
                    intent.putExtra("targetDeviceId", deviceId);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
            mDeviceEnvironmentRows.put(deviceId, environment);
            /*
            if (Configuration.NEW_PRODUCT_MODE) {
                if (deviceId == 198 || deviceId == 1560) {
                    environment.showTitlebar(false);
                }
            }
            */
            lctnDeviceList.addView(environment);

            if (mPreferenceMgr.getLatestSavedNotificationIndex(hub.type, hub.deviceId, 0) > mPreferenceMgr.getLatestCheckedNotificationIndex(hub.type, hub.deviceId)) {
                environment.showTemperatureAlarmMark(true);
            } else {
                environment.showTemperatureAlarmMark(false);
            }

            if (mPreferenceMgr.getLatestSavedNotificationIndex(hub.type, hub.deviceId, 1) > mPreferenceMgr.getLatestCheckedNotificationIndex(hub.type, hub.deviceId)) {
                environment.showHumidityAlarmMark(true);
            } else {
                environment.showHumidityAlarmMark(false);
            }

            if (mPreferenceMgr.getLatestSavedNotificationIndex(hub.type, hub.deviceId, 2) > mPreferenceMgr.getLatestCheckedNotificationIndex(hub.type, hub.deviceId)) {
                environment.showVocAlarmMark(true);
            } else {
                environment.showVocAlarmMark(false);
            }

            if (mVersionMgr.checkUpdateAvailable(hub.firmwareVersion, mPreferenceMgr.getHubVersion()) &&
                    (hub.getConnectionState() != DeviceConnectionState.DISCONNECTED)) {

                String description = getString(R.string.contents_need_hub_firmware_update);
                if (mVersionMgr.checkUpdateAvailable(hub.firmwareVersion, mPreferenceMgr.getHubForceVersion())) {
                    description = getString(R.string.contents_need_hub_firmware_update_force);
                }

                CardNoticeButton cardNoticeButton = new CardNoticeButton(
                    mContext,
                    0,
                    "[" + hub.getName() + "] " + description,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainLightActivity.this, HubFirmwareUpdateActivity.class);
                            intent.putExtra("targetDeviceId", deviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }
                    });
                lctnDeviceList.addView(cardNoticeButton, 0);
                environment.showNewMark(true);
            } else {
                environment.showNewMark(false);
            }
        }
    }

    private void _addLampRows() {
        _addLampRows(-1);
    }

    private void _addLampRows(long selectId) {
        for (DeviceLamp lamp : ConnectionManager.mRegisteredLampList.values()) {
            if (lamp == null) continue;
            final long deviceId = lamp.deviceId;

            // 선택한 device가 없거나(-1) device를 선택한 경우 그것만 추가
            if (selectId != deviceId && selectId != -1) continue;
            // 이미 추가된 경우 SKIP
            if (mDeviceLampRows.get(deviceId) != null) continue;

            DeviceStatusRowLamp lampRow = new DeviceStatusRowLamp(mContext);
            lampRow.setDeviceId(deviceId);

            if (Configuration.MASTER) {
                lampRow.setDeviceName(lamp.name + "(" + lamp.deviceId + ")");
            } else {
                lampRow.setDeviceName(lamp.name);
            }
            if (DBG) Log.d(TAG, "_addLampRows : " + lampRow.getDeviceId() + " / " + lampRow.getDeviceName());

            if (lamp.getConnectionState() == DeviceConnectionState.BLE_CONNECTED || lamp.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
                if (DBG) Log.d(TAG, "connected environment found : " + deviceId);
                lampRow.setConnected(true);
            } else {
                lampRow.setConnected(false);
            }

            lampRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainLightActivity.this, DeviceLampActivity.class);
                    intent.putExtra("targetDeviceId", deviceId);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
            mDeviceLampRows.put(deviceId, lampRow);
            lctnDeviceList.addView(lampRow);

            if (mPreferenceMgr.getLatestSavedNotificationIndex(lamp.type, lamp.deviceId, 0) > mPreferenceMgr.getLatestCheckedNotificationIndex(lamp.type, lamp.deviceId)) {
                lampRow.showTemperatureAlarmMark(true);
            } else {
                lampRow.showTemperatureAlarmMark(false);
            }

            if (mPreferenceMgr.getLatestSavedNotificationIndex(lamp.type, lamp.deviceId, 1) > mPreferenceMgr.getLatestCheckedNotificationIndex(lamp.type, lamp.deviceId)) {
                lampRow.showHumidityAlarmMark(true);
            } else {
                lampRow.showHumidityAlarmMark(false);
            }

            if (mPreferenceMgr.getLatestSavedNotificationIndex(lamp.type, lamp.deviceId, 2) > mPreferenceMgr.getLatestCheckedNotificationIndex(lamp.type, lamp.deviceId)) {
                lampRow.showVocAlarmMark(true);
            } else {
                lampRow.showVocAlarmMark(false);
            }

            if (mVersionMgr.checkUpdateAvailable(lamp.firmwareVersion, mPreferenceMgr.getLampVersion()) &&
                    (lamp.getConnectionState() != DeviceConnectionState.DISCONNECTED)) {

                String description = getString(R.string.contents_need_lamp_firmware_update);
                if (mVersionMgr.checkUpdateAvailable(lamp.firmwareVersion, mPreferenceMgr.getLampForceVersion())) {
                    description = getString(R.string.contents_need_lamp_firmware_update);
                }

                CardNoticeButton cardNoticeButton = new CardNoticeButton(
                        mContext,
                        0,
                        "[" + lamp.getName() + "] " + description,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainLightActivity.this, LampFirmwareUpdateActivity.class);
                                intent.putExtra("targetDeviceId", deviceId);
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                            }
                        });
                lctnDeviceList.addView(cardNoticeButton, 0);
                lampRow.showNewMark(true);
            } else {
                lampRow.showNewMark(false);
            }
        }
    }

    private void _addDiaperSensorRows() {
        _addDiaperSensorRows(-1);
    }

    private void _addDiaperSensorRows(long selectId) {
        for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
            if (sensor == null) continue;
            final long deviceId = sensor.deviceId;
            if (Configuration.MONIT20) {
                // 선택한 device가 없거나(-1) device를 선택한 경우 그것만 추가
                if (selectId != deviceId && selectId != -1) continue;
                // 이미 추가된 경우 SKIP
                if (mDeviceDiaperSensorRows2.get(deviceId) != null) continue;

                DeviceStatusRowDiaperSensor2 diaper = new DeviceStatusRowDiaperSensor2(mContext);
                diaper.setDeviceId(deviceId);
                if (Configuration.MASTER) {
                    diaper.setDeviceName(sensor.name + "(" + sensor.deviceId + ")");
                } else {
                    diaper.setDeviceName(sensor.name);
                }
                if (DBG)
                    Log.d(TAG, "_addDiaperSensorRows : " + diaper.getDeviceId() + " / " + diaper.getDeviceName());

                if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                    if (DBG) Log.d(TAG, "connected diaper sensor found : " + deviceId);
                    diaper.setConnected(true);
                } else {
                    diaper.setConnected(false);
                }

                diaper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainLightActivity.this, DeviceSensorActivity.class);
                        intent.putExtra("targetDeviceId", deviceId);
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                    }
                });
                mDeviceDiaperSensorRows2.put(deviceId, diaper);
                lctnDeviceList.addView(diaper);

                if (mPreferenceMgr.getLatestSavedNotificationIndex(sensor.type, sensor.deviceId, 0) > mPreferenceMgr.getLatestCheckedNotificationIndex(sensor.type, sensor.deviceId)) {
                    diaper.showAlarmMark(true);
                } else {
                    diaper.showAlarmMark(false);
                }

                if (mVersionMgr.checkDiaperSensorFwUpdateAvailable(sensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion()) &&
                        (sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED)) {

                    String description = getString(R.string.contents_need_sensor_firmware_update);
                    if (mVersionMgr.checkUpdateAvailable(sensor.firmwareVersion, mPreferenceMgr.getDiaperSensorForceVersion())) {
                        description = getString(R.string.contents_need_sensor_firmware_update_force);
                    }

                    CardNoticeButton cardNoticeButton = new CardNoticeButton(
                            mContext,
                            0,
                            "[" + sensor.getName() + "] " + description,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MainLightActivity.this, FirmwareUpdateActivity.class);
                                    intent.putExtra("targetDeviceId", deviceId);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                }
                            });
                    lctnDeviceList.addView(cardNoticeButton, 0);
                    diaper.showNewMark(true);
                } else {
                    diaper.showNewMark(false);
                }
            } else {
                // 선택한 device가 없거나(-1) device를 선택한 경우 그것만 추가
                if (selectId != deviceId && selectId != -1) continue;
                // 이미 추가된 경우 SKIP
                if (mDeviceDiaperSensorRows.get(deviceId) != null) continue;

                DeviceStatusRowDiaperSensor diaper = new DeviceStatusRowDiaperSensor(mContext);
                diaper.setDeviceId(deviceId);
                if (Configuration.MASTER) {
                    diaper.setDeviceName(sensor.name + "(" + sensor.deviceId + ")");
                } else {
                    diaper.setDeviceName(sensor.name);
                }
                if (DBG)
                    Log.d(TAG, "_addDiaperSensorRows : " + diaper.getDeviceId() + " / " + diaper.getDeviceName());

                if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                    if (DBG) Log.d(TAG, "connected diaper sensor found : " + deviceId);
                    diaper.setConnected(true);
                } else {
                    diaper.setConnected(false);
                }

                diaper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainLightActivity.this, DeviceSensorActivity.class);
                        intent.putExtra("targetDeviceId", deviceId);
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                    }
                });
                mDeviceDiaperSensorRows.put(deviceId, diaper);
                lctnDeviceList.addView(diaper);

                if (mPreferenceMgr.getLatestSavedNotificationIndex(sensor.type, sensor.deviceId, 0) > mPreferenceMgr.getLatestCheckedNotificationIndex(sensor.type, sensor.deviceId)) {
                    diaper.showAlarmMark(true);
                } else {
                    diaper.showAlarmMark(false);
                }

                if (mVersionMgr.checkDiaperSensorFwUpdateAvailable(sensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion()) &&
                        (sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED)) {

                    String description = getString(R.string.contents_need_sensor_firmware_update);
                    if (mVersionMgr.checkUpdateAvailable(sensor.firmwareVersion, mPreferenceMgr.getDiaperSensorForceVersion())) {
                        description = getString(R.string.contents_need_sensor_firmware_update_force);
                    }

                    CardNoticeButton cardNoticeButton = new CardNoticeButton(
                            mContext,
                            0,
                            "[" + sensor.getName() + "] " + description,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MainLightActivity.this, FirmwareUpdateActivity.class);
                                    intent.putExtra("targetDeviceId", deviceId);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                }
                            });
                    lctnDeviceList.addView(cardNoticeButton, 0);
                    diaper.showNewMark(true);
                } else {
                    diaper.showNewMark(false);
                }
            }
        }
    }

    private void _addElderlyDiaperSensorRows() {
        _addElderlyDiaperSensorRows(-1);
    }

    private void _addElderlyDiaperSensorRows(long selectId) {
        for (DeviceElderlyDiaperSensor sensor : ConnectionManager.mRegisteredElderlyDiaperSensorList.values()) {
            if (sensor == null) continue;
            final long deviceId = sensor.deviceId;

            // 선택한 device가 없거나(-1) device를 선택한 경우 그것만 추가
            if (selectId != deviceId && selectId != -1) continue;

            // 이미 추가된 경우 SKIP
            if (mDeviceElderlyDiaperSensorRows.get(deviceId) != null) continue;

            DeviceStatusRowElderlyDiaperSensor diaper = new DeviceStatusRowElderlyDiaperSensor(mContext);
            diaper.setDeviceId(deviceId);
            diaper.setDeviceType(DeviceType.ELDERLY_DIAPER_SENSOR);
            diaper.setDeviceEnc(sensor.getEnc());
            if (Configuration.MASTER) {
                diaper.setDeviceName(sensor.name + "(" + sensor.deviceId + ")");
            } else {
                diaper.setDeviceName(sensor.name);
            }
            if (DBG) Log.d(TAG, "_addElderlyDiaperSensorRows : " + diaper.getDeviceId() + " / " + diaper.getDeviceName());

            if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                if (DBG) Log.d(TAG, "connected elderly diaper sensor found : " + deviceId);
                diaper.setConnected(true);
            } else {
                diaper.setConnected(false);
            }

            diaper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(MainLightActivity.this, DeviceElderlyDiaperSensorActivity.class);
//                    intent.putExtra("targetDeviceId", deviceId);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
            mDeviceElderlyDiaperSensorRows.put(deviceId, diaper);
            /*
            if (Configuration.NEW_PRODUCT_MODE) {
                if (deviceId == 1 || deviceId == 2) {
                    diaper.showTitlebar(false);
                }
            }
            */
            lctnDeviceList.addView(diaper);

            if (mPreferenceMgr.getLatestSavedNotificationIndex(sensor.type, sensor.deviceId, 0) > mPreferenceMgr.getLatestCheckedNotificationIndex(sensor.type, sensor.deviceId)) {
                diaper.showAlarmMark(true);
            } else {
                diaper.showAlarmMark(false);
            }

            if (mVersionMgr.checkDiaperSensorFwUpdateAvailable(sensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion()) &&
                    (sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED)) {

                String description = getString(R.string.contents_need_sensor_firmware_update);
                if (mVersionMgr.checkUpdateAvailable(sensor.firmwareVersion, mPreferenceMgr.getDiaperSensorForceVersion())) {
                    description = getString(R.string.contents_need_sensor_firmware_update_force);
                }

                CardNoticeButton cardNoticeButton = new CardNoticeButton(
                        mContext,
                        0,
                        "[" + sensor.getName() + "] " + description,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainLightActivity.this, FirmwareUpdateActivity.class);
                                intent.putExtra("targetDeviceId", deviceId);
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                            }
                        });
                lctnDeviceList.addView(cardNoticeButton, 0);
                diaper.showNewMark(true);
            } else {
                diaper.showNewMark(false);
            }
        }

        // Notification관련 객체 생성
        if (mDeviceElderlyDiaperSensorRows != null && mDeviceElderlyDiaperSensorRows.size() > 0) {
            onCreateElderlyNotification();
        }
    }

    private void _checkShowHubConnectDialog() {
        // 허브 등록 다이얼로그가 필요한지 확인
        // 센서 등록 후 펌웨어 업데이트로 허브 등록 여부를 물어보지 못한 상황
        if (DBG) Log.d(TAG, "show Hub Connect Dialog: " + mPreferenceMgr.getNeedHubRegistrationDialog() + " / " + ConnectionManager.mRegisteredAQMHubList.values().size());
        if ((mPreferenceMgr.getNeedHubRegistrationDialog() == true)
                && (ConnectionManager.mRegisteredAQMHubList.values().size() == 0)) {
            if (mDlgConnectHub == null) {
                mDlgConnectHub = new SimpleDialog(
                        MainLightActivity.this,
                        getString(R.string.dialog_contents_ask_for_connectint_hub),
                        getString(R.string.btn_no),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDlgConnectHub.dismiss();
                            }
                        },
                        getString(R.string.btn_yes),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDlgConnectHub.dismiss();
                                _showConnectionActivity(ConnectionActivity.STEP_HUB_READY_FOR_CONNECTING);
                            }
                        });
            }
            try {
                if (DBG) Log.d(TAG, "show Hub Connect Dialog");
                mDlgConnectHub.show();
            } catch(Exception e) {

            }
        }
        mPreferenceMgr.setNeedHubRegistrationDialog(false);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DBG) Log.d(TAG, "handleMessage: " + msg.what);
            switch(msg.what) {
                case MSG_SEND_SCREEN_ANALYTICS:
                    ScreenAnalyticsManager screenManager = new ScreenAnalyticsManager(mContext);
                    screenManager.sendScreenAnalytics();
                    break;
                case MSG_CHECK_STEP:
                    if (DBG) Log.d(TAG, "step: " + mCurrentStep);
                    if (mCurrentStep == STEP_SHOW_AGREEMENT) {
                        _checkAgreement();
                    } else if (mCurrentStep == STEP_SHOW_ALLOW_PERMISSION_DIALOG) {
                        _checkPermissions();
                    } else if (mCurrentStep == STEP_FORCE_CLOSE_WARNING_DIALOG) {
                        _checkForceClosedWarningDialog();
                    } else if (mCurrentStep == STEP_SHOW_NOTICE) {
                        _checkNotice();
                    } else if (mCurrentStep == STEP_ALLOW_BLUETOOTH_TURN_ON) {
                        _checkBluetoothEnabled();
                    } else if (mCurrentStep == STEP_INPUT_NICKNAME) {
                        _checkNicknameSetting();
                    } else if (mCurrentStep == STEP_COMPLETED) {
                        if (mDlgInputNickname != null && mDlgInputNickname.isShowing()) {
                            try {
                                mDlgInputNickname.dismiss();
                            } catch (Exception e) {

                            }
                        }
                    }
                    break;
                case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
                    final int state = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;

                    if (deviceInfo == null) break;
                    String deviceId = deviceInfo.deviceId + "";
                    if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "] " + state);
                    if (deviceInfo.type == DeviceType.DIAPER_SENSOR) {
                        if (state == DeviceConnectionState.BLE_CONNECTED) {
                            if (mDeviceDiaperSensorRows.get(deviceId) != null) {
                                mDeviceDiaperSensorRows.get(deviceId).setConnected(true);
                            }
                        } else if (state == DeviceConnectionState.DISCONNECTED) {
                            if (mDeviceDiaperSensorRows.get(deviceId) != null) {
                                mDeviceDiaperSensorRows.get(deviceId).setConnected(false);
                            }
                        }
                    } else if (deviceInfo.type == DeviceType.AIR_QUALITY_MONITORING_HUB) {
                        if (state == DeviceConnectionState.BLE_CONNECTED) {
                            if (mDeviceEnvironmentRows.get(deviceId) != null) {
                                mDeviceEnvironmentRows.get(deviceId).setConnected(true);
                            }
                        } else if (state == DeviceConnectionState.DISCONNECTED) {
                            if (mDeviceEnvironmentRows.get(deviceId) != null) {
                                mDeviceEnvironmentRows.get(deviceId).setConnected(false);
                            }
                        }
                    } else if (deviceInfo.type == DeviceType.LAMP) {
                        if (state == DeviceConnectionState.BLE_CONNECTED) {
                            if (mDeviceLampRows.get(deviceId) != null) {
                                mDeviceLampRows.get(deviceId).setConnected(true);
                            }
                        } else if (state == DeviceConnectionState.DISCONNECTED) {
                            if (mDeviceLampRows.get(deviceId) != null) {
                                mDeviceLampRows.get(deviceId).setConnected(false);
                            }
                        }
                    }

                    _refreshView();
                    break;

                case ConnectionManager.MSG_WIFI_CONNECTION_STATE_CHANGE:
                    final int wifiState = msg.arg1;
                    final DeviceInfo deviceInfo2 = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_WIFI_CONNECTION_STATE_CHANGE : [" + deviceInfo2.deviceId + "] " + wifiState);


                    /*
                    if (wifiState == DeviceConnectionState.WIFI_CONNECTED) {

                    } else if (wifiState == DeviceConnectionState.DISCONNECTED) {

                    }
                    */
                    _refreshView();
                    break;

                case ConnectionManager.MSG_CONNECTION_ERROR:
                    if (ConnectionManager.checkBluetoothStatus() == ConnectionManager.STATE_DISABLED) {
                        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(btEnableIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
                    }
                    break;

                case MSG_REFRESH_VIEW:
                    mHandler.removeMessages(MSG_REFRESH_VIEW);
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, REFRESH_VIEW_INTERVAL_SEC * 1000L);
                    _refreshView();
                    break;

                case ConnectionManager.MSG_LAMP_VALUE_UPDATED:
                    final String deviceId2 = msg.arg1 + "";
                    final CurrentLampValue currLampValue = (CurrentLampValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_LAMP_VALUE_UPDATED : [" + deviceId2 + "] " + currLampValue.toString());

                    /**
                     * Environment
                     */
                    if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
                        currLampValue.temperature = UnitConvertUtil.getFahrenheitFromCelsius(currLampValue.temperature);
                    }
                    mDeviceEnvironment = mDeviceEnvironmentRows.get(deviceId2);
                    if (mDeviceEnvironment != null) {
                        if (!mDeviceEnvironment.isConnected()) {
                            mDeviceEnvironment.setConnected(true);
                        }
                        /*
                        mEnvironmentManager.setTemperature(currLampValue.temperature);
                        mEnvironmentManager.setHumidity(currLampValue.humidity);
                        mEnvironmentManager.setVoc(currLampValue.vocFromSensor);

                        mDeviceEnvironment.setHumidity(currLampValue.humidity);
                        mDeviceEnvironment.setTemperature(currLampValue.temperature);

                        mDeviceEnvironment.setSensorAttached(true);
                        mDeviceEnvironment.setVocStatus(VocStatus.getString(mContext, currLampValue.vocFromSensor));

                        // Set Warning
                        mDeviceEnvironment.setTemperatureWarning(mEnvironmentManager.getTemperatureStatus() != EnvironmentCheckManager.NORMAL);
                        mDeviceEnvironment.setHumidityWarning(mEnvironmentManager.getHumidityStatus() != EnvironmentCheckManager.NORMAL);
                        mDeviceEnvironment.setVocWarning(mEnvironmentManager.getVocStatus() != EnvironmentCheckManager.NORMAL);
                        mDeviceEnvironment.setEnvironmentScore(mEnvironmentManager.getScore());
                        */
                    }
                    break;

                case ConnectionManager.MSG_SENSOR_VALUE_UPDATED:
                    final String deviceId3 = msg.arg1 + "";
                    final CurrentSensorValue sensorValue = (CurrentSensorValue) msg.obj;
                    if (DBG) Log.d(TAG, "MSG_SENSOR_VALUE_UPDATED : [" + deviceId3 + "] " + sensorValue.toString());

                    /**
                     * Diaper
                     */
                    mDeviceDiaperSensor = mDeviceDiaperSensorRows.get(deviceId3);
                    if (mDeviceDiaperSensor != null) {
                        if (!mDeviceDiaperSensor.isConnected()) {
                            mDeviceDiaperSensor.setConnected(true);
                        }

                        mDeviceDiaperSensor.setOperationStatus(sensorValue.status_operation);
                        mDeviceDiaperSensor.setMovementStatus(sensorValue.status_movement);

                        if (sensorValue.count_poo_detected > 0) {
                            mDeviceDiaperSensor.setDiaperStatus(DeviceStatus.DETECT_POO);
                        } else if (sensorValue.count_abnormal_detected > 0) {
                            mDeviceDiaperSensor.setDiaperStatus(DeviceStatus.DETECT_ABNORMAL);
                        } else if (sensorValue.count_pee_detected > 0) {
                            mDeviceDiaperSensor.setDiaperStatus(DeviceStatus.DETECT_PEE);
                        } else {
                            mDeviceDiaperSensor.setDiaperStatus(DeviceStatus.DETECT_NONE);
                        }
                    }
                    break;
                case ConnectionManager.MSG_UPDATE_SCREEN_DEVICE_OBJECT_VIEW:
                    if (DBG) Log.d(TAG, "update screen");
                    lctnDeviceList.removeAllViews();
                    _loadRowFromDeviceViewObject();
                    break;

                case MSG_UPDATE_CONNECTED_DEVICE_COUNT:
                    int count = msg.arg1;
                    if (DBG) Log.d(TAG, "MSG_UPDATE_CONNECTED_DEVICE_COUNT : " + count);
                    if (count == 0) {
                        lctnDeviceList.removeAllViews();
                        lctnEmptyList.setVisibility(View.VISIBLE);
                        lctnDeviceList.setVisibility(View.GONE);
                    } else {
                        lctnEmptyList.setVisibility(View.GONE);
                        lctnDeviceList.setVisibility(View.VISIBLE);
                        lctnDeviceList.removeAllViews();

                        // 베타테스트용
                        if (Configuration.BETA_TEST_MODE) {
                            //rctnNotifiationList.setVisibility(View.VISIBLE);
                            //onCreateNotification();
                            rctnNotifiationList.setVisibility(View.GONE);
                        } else {
                            rctnNotifiationList.setVisibility(View.GONE);
                        }
                        _loadRowFromDeviceViewObject();
                        _refreshView();

                        View vSwipeDown = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.widget_textview_swipe_down, null);
                        lctnDeviceList.addView(vSwipeDown);
                    }
                    break;

                case MSG_FULL_SYNC_FINISHED:
                    srlDeviceList.setRefreshing(false);
                    break;

                case MSG_SHOW_NOTICE:
                    String data = (String)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_SHOW_NOTICE : " + data);
                    _showNotice(data);
                    break;

                case MSG_SHOW_MAINTENANCE_NOTICE:
                    String maintenanceData = (String)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_SHOW_MAINTENANCE_NOTICE : " + maintenanceData);
                    _showMaintenance(maintenanceData);
                    break;

                case ConnectionManager.MSG_USER_INFO_UPDATED:
                    if (DBG) Log.d(TAG, "MSG_USER_INFO_UPDATED");
                    if (didRefreshScreen) {
                        showToast(getString(R.string.toast_sharing_member_renewed));
                        didRefreshScreen = false;
                    }
                    _refreshView();
                    break;

                case ConnectionManager.MSG_NOTIFICATION_MESSAGE_UPDATED:
                    if (DBG) Log.d(TAG, "MSG_NOTIFICATION_MESSAGE_UPDATED");
                    loadElderlyLatestMessageList();
                    showElderlyFilteredList();

                    loadLatestMessageList();
                    showFilteredList();
                    break;

                case ConnectionManager.MSG_CHECK_INVALID_TOKEN:
                    if (mPreferenceMgr.getInvalidTokenReceived()) {
                        if (DBG) Log.d(TAG, "InvalidTokenReceived");
                        mPreferenceMgr.setInvalidTokenReceived(false);
                        showToast(mContext.getString(R.string.toast_invalid_user_session));
                        mUserInfoMgr.signout();
                        Intent intent = null;

                        switch (Configuration.APP_MODE) {
                            case Configuration.APP_GLOBAL:
                            case Configuration.APP_KC_HUGGIES_X_MONIT:
                            case Configuration.APP_MONIT_X_KAO:
                                intent = new Intent(MainLightActivity.this, SigninActivity.class);
                                break;
//                            //case Configuration.APP_MONIT_X_HUGGIES:
//                                intent = new Intent(MainLightActivity.this, YKSigninActivity.class);
//                                break;
                        }

                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                } else {
                    showToast(getString(R.string.toast_need_to_enable_bluetooth));
                }
                mCurrentStep = STEP_INPUT_NICKNAME;
                mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                break;
            case REQUEST_CODE_AGREEMENT:
                mCurrentStep = STEP_SHOW_ALLOW_PERMISSION_DIALOG;
                mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                break;
            case REQUEST_CODE_ALLOW_PERMISSIONS:
                mCurrentStep = STEP_FORCE_CLOSE_WARNING_DIALOG;
                mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                break;
            case REQUEST_CODE_NOTICE:
                mCurrentStep = STEP_ALLOW_BLUETOOTH_TURN_ON;
                mHandler.sendEmptyMessage(MSG_CHECK_STEP);
                break;
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

    /* 데이터 수집 */
    private RelativeLayout rctnNotifiationList;
    private RecyclerView rvNotificationList;
    private DeviceDiaperSensor mNotificationDiaperSensorCandidate;
    private DeviceElderlyDiaperSensor mNotificationElderlyDiaperSensorCandidate;

    private boolean hasElderlyNotificationCreated = false;

    private int mNotificationDeviceType = -1;
    private long mNotificationDeviceId = -1;
    private String mNotificationDeviceEnc = "";

    private TextView tvEmpty;
    private long mLastLoadedMessageTimeMs;

    private Button btnAddNotification;
    private ArrayList<NotificationMessage> mNotificationMessageList;
    private ArrayList<Integer> mFilteredTypeList;
    private ArrayList<Integer> mElderlyFilteredTypeList;
    private RecyclerViewAdapter mMsgAdapter;
    private boolean hasNotificationCreated = false;

    private DiaperInputDialog mDlgFeedbackDiaperStatus;
    private FeedingInputDialog mDlgFeeding;
    private SleepInputDialog mDlgSleep;
    private SimpleDialog mDlgUserComment;
    private Button btnAlarmDetail;

    public void onCreateElderlyNotification() {
        if (DBG) Log.e(TAG, "onCreateElderlyNotification: " + hasElderlyNotificationCreated);
        if (hasElderlyNotificationCreated) {
            loadElderlyMessageList();
            showElderlyFilteredList();
        } else {
            hasElderlyNotificationCreated = true;

            if (mElderlyFilteredTypeList == null) {
                mElderlyFilteredTypeList = new ArrayList<>();
            }

            // 필터 삭제
            mElderlyFilteredTypeList.add((Integer)NotificationType.DIAPER_CHANGED);
            mElderlyFilteredTypeList.add((Integer)NotificationType.PEE_DETECTED);
            mElderlyFilteredTypeList.add((Integer)NotificationType.POO_DETECTED);
            mElderlyFilteredTypeList.add((Integer)NotificationType.ABNORMAL_DETECTED);
            mElderlyFilteredTypeList.add((Integer)NotificationType.FART_DETECTED);
//        mElderlyFilteredTypeList.add((Integer)NotificationType.BABY_SLEEP);
//        mElderlyFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BABY_FOOD);
//        mElderlyFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK);
//        mElderlyFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK);
//        mElderlyFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_NURSED_BREAST_MILK);

            loadElderlyMessageList();
            showElderlyFilteredList();
        }
    }

    public void loadElderlyLatestMessageList() {
        if (DBG) Log.e(TAG, "loadElderlyLatestMessageList");
        if (mDeviceElderlyDiaperSensorRows == null || mDeviceElderlyDiaperSensorRows.size() == 0) return;

        if (hasElderlyNotificationCreated == false) {
            onCreateElderlyNotification();
        }

        for (DeviceStatusRowElderlyDiaperSensor row : mDeviceElderlyDiaperSensorRows.values()) {
            if (row == null) continue;
            long deviceId = row.getDeviceId();
            if (deviceId == -1) continue;

            row.setLastLoadedMessageTimeMs(System.currentTimeMillis());
        }
        loadElderlyMessageList();
    }

    public void loadElderlyMessageList() {
        if (mDeviceElderlyDiaperSensorRows == null || mDeviceElderlyDiaperSensorRows.size() == 0) return;

        for (DeviceStatusRowElderlyDiaperSensor row : mDeviceElderlyDiaperSensorRows.values()) {
            if (row == null) continue;
            long deviceId = row.getDeviceId();
            if (deviceId == -1) continue;

            long lastLoadedMessageTimeMs = System.currentTimeMillis() + 3000;
            if (DBG) Log.e(TAG, "loadElderlyMessageList: " + deviceId + " / " + lastLoadedMessageTimeMs);

            ArrayList<NotificationMessage> msgNotificationList = row.getNotificationMsgList();
            FeedbackMsgAdapter feedbackMsgAdapter = row.getFeedbackMsgAdapter();
            msgNotificationList.clear();

            long latestCheckedNotificationIdx = mPreferenceMgr.getLatestCheckedNotificationIndex(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId);
            mPreferenceMgr.setLatestCheckedNotificationIndex(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, mPreferenceMgr.getLatestSavedNotificationIndex(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, 0));
            feedbackMsgAdapter.setLatestCheckedNotificationIndex(latestCheckedNotificationIdx);

            ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getDiaperSensorMessagesV2(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, lastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
            if (messageList != null && messageList.size() > 0) {
                for (NotificationMessage mm : messageList) {
                    msgNotificationList.add(mm);
                    if (lastLoadedMessageTimeMs > mm.timeMs) {
                        lastLoadedMessageTimeMs = mm.timeMs;
                        row.setLastLoadedMessageTimeMs(mm.timeMs);
                    }
                    if (DBG) Log.d(TAG, "msg: " + mm.toString());
                }
                if (DBG) Log.e(TAG, "loadMessageList : " + latestCheckedNotificationIdx + " / " + messageList.get(0).msgId + " / checked: " + mPreferenceMgr.getLatestCheckedNotificationIndex(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId));
            }

        }
    }

    public void showElderlyFilteredList() {
        if (DBG) Log.e(TAG, "showElderlyFilteredList");

        if (mDeviceElderlyDiaperSensorRows == null || mDeviceElderlyDiaperSensorRows.size() == 0) return;

        for (DeviceStatusRowElderlyDiaperSensor row : mDeviceElderlyDiaperSensorRows.values()) {
            if (row == null) continue;
            long deviceId = row.getDeviceId();
            if (deviceId == -1) continue;

            ArrayList<NotificationMessage> msgNotificationList = row.getNotificationMsgList();
            FeedbackMsgAdapter feedbackMsgAdapter = row.getFeedbackMsgAdapter();

            if (msgNotificationList == null) continue;

            boolean isValid;
            ArrayList<NotificationMessage> tempNotificationMessageList = new ArrayList<>();

            for (NotificationMessage mm : msgNotificationList) {
                isValid = false;
                for (int filterType : mElderlyFilteredTypeList) {
                    if (mm.notiType == filterType) {
                        if (mm.notiType == NotificationType.PEE_DETECTED || mm.notiType == NotificationType.POO_DETECTED || mm.notiType == NotificationType.FART_DETECTED || mm.notiType == NotificationType.ABNORMAL_DETECTED) {
                            if (Configuration.BETA_TEST_MODE) {
                                if (mm.extra != null && !mm.extra.equals("-") && !mm.extra.equals("")) {
                                    // 피드백이 입력되어 있으면 보여주지 않기
                                } else {
                                    // 입력이 안되어있으면, 보여주기
                                    isValid = true;
                                }
                            } else {
                                isValid = true;
                            }
                        } else {
                            // 기저귀확인메시지 안보여주기
                            //isValid = true;
                        }
                        break;
                    } else if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK && mm.notiType == NotificationType.CHAT_USER_INPUT) {
                        isValid = true;
                    } else if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK && mm.notiType == NotificationType.CHAT_USER_FEEDBACK) {
                        // 피드백이 입력되어 있지 않으면 보여주기
                        if (mm.extra != null && (mm.extra.equals("d10") || mm.extra.equals("d40") || mm.extra.equals("11") || mm.extra.equals("12") || mm.extra.equals("13") || mm.extra.equals("-") || mm.extra.equals(""))) {
                            isValid = true;
                        }
                        break;
                    } else if (mm.notiType == NotificationType.DIAPER_DETACHMENT_DETECTED) {
                        isValid = true;
                        break;
                    }
                }

                if (isValid) {
                    tempNotificationMessageList.add(mm);
                }
            }

            if (tempNotificationMessageList.size() == 0) {
                row.showFeedbackMessageList(false);
                feedbackMsgAdapter.setList(tempNotificationMessageList);
            } else {
                row.showFeedbackMessageList(true);
                feedbackMsgAdapter.setList(tempNotificationMessageList);
                feedbackMsgAdapter.notifyDataSetChanged();
            }

        }
    }

    public void resumeElderlyNotification() {
        if (DBG) Log.d(TAG, "resumeElderlyNotification");
        loadElderlyMessageList();
        showElderlyFilteredList();
    }

    public void onResumeElderlyNotification() {
        if (Configuration.MONIT_ELDERLY_TEST) {
            if (mConnectionMgr != null) {
                if (mDeviceElderlyDiaperSensorRows == null || mDeviceElderlyDiaperSensorRows.size() == 0) return;

                for (DeviceStatusRowElderlyDiaperSensor row : mDeviceElderlyDiaperSensorRows.values()) {
                    if (row == null) continue;
                    long deviceId = row.getDeviceId();
                    if (deviceId == -1) continue;

                    mConnectionMgr.getNotificationFromCloudV2(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId);
                }
            }
        }
    }

    public void onCreateNotification() {
        for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
            if (sensor == null) continue;
            mNotificationDiaperSensorCandidate = sensor;
            mNotificationDeviceType = DeviceType.DIAPER_SENSOR;
            mNotificationDeviceId = sensor.deviceId;
            mNotificationDeviceEnc = sensor.getEnc();
            break;
        }

        if (mNotificationDiaperSensorCandidate == null) {
            for (DeviceElderlyDiaperSensor sensor : ConnectionManager.mRegisteredElderlyDiaperSensorList.values()) {
                if (sensor == null) continue;
                mNotificationElderlyDiaperSensorCandidate = sensor;
                mNotificationDeviceType = DeviceType.ELDERLY_DIAPER_SENSOR;
                mNotificationDeviceId = sensor.deviceId;
                mNotificationDeviceEnc = sensor.getEnc();
                break;
            }
        }

        if (hasNotificationCreated) return;
        hasNotificationCreated = true;

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
                        final long selectedDateTimeMs = mDlgFeedbackDiaperStatus.getDateTimeUtcMs();
                        final int selectedMode = mDlgFeedbackDiaperStatus.getSelectedMode();
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        mDlgFeedbackDiaperStatus.dismiss();

                        //addDiaperFeedbackInput(selectedMode, selectedDateTimeMs); // 기저귀 확인알람

                        // 기저귀 교체 알람
                        if (mNotificationDeviceId != -1) {
                            mServerQueryMgr.setDiaperChanged(
                                    mNotificationDeviceType,
                                    mNotificationDeviceId,
                                    mNotificationDeviceEnc,
                                    selectedDateTimeMs,
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

                                            if (utcRespTimeSec <= 0) utcRespTimeSec = selectedDateTimeMs / 1000;
                                            String utcTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcRespTimeSec * 1000);
                                            if (DBG) Log.d(TAG, "utc datetime string: " + utcTimeString);

                                            if (mNotificationDiaperSensorCandidate != null) {
                                                mNotificationDiaperSensorCandidate.setDiaperChanged(utcRespTimeSec * 1000);
                                                mPreferenceMgr.setLatestDiaperStatusUpdatedTimeSec(mNotificationDiaperSensorCandidate.deviceId, utcRespTimeSec);
                                                if (mPreferenceMgr.getLatestDiaperChangedTimeSec(mNotificationDiaperSensorCandidate.deviceId) < utcRespTimeSec) {
                                                    mPreferenceMgr.setLatestDiaperChangedTimeSec(mNotificationDiaperSensorCandidate.deviceId, utcRespTimeSec);
                                                }
                                                mConnectionMgr.getNotificationFromCloudV2(mNotificationDiaperSensorCandidate.type, mNotificationDiaperSensorCandidate.deviceId);
                                            }
                                            if (mNotificationElderlyDiaperSensorCandidate != null) {
                                                mNotificationElderlyDiaperSensorCandidate.setDiaperChanged(utcRespTimeSec * 1000);
                                                mPreferenceMgr.setLatestDiaperStatusUpdatedTimeSec(mNotificationElderlyDiaperSensorCandidate.deviceId, utcRespTimeSec);
                                                if (mPreferenceMgr.getLatestDiaperChangedTimeSec(mNotificationElderlyDiaperSensorCandidate.deviceId) < utcRespTimeSec) {
                                                    mPreferenceMgr.setLatestDiaperChangedTimeSec(mNotificationElderlyDiaperSensorCandidate.deviceId, utcRespTimeSec);
                                                }
                                                mConnectionMgr.getNotificationFromCloudV2(mNotificationElderlyDiaperSensorCandidate.type, mNotificationElderlyDiaperSensorCandidate.deviceId);
                                            }
                                        }
                                    });
                        }
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

        mNotificationMessageList = new ArrayList<>();
        mFilteredTypeList = new ArrayList<>();

        tvEmpty = (TextView)findViewById(R.id.tv_notification_empty);

        // 필터 삭제
        mFilteredTypeList.add((Integer)NotificationType.DIAPER_CHANGED);
        mFilteredTypeList.add((Integer)NotificationType.PEE_DETECTED);
        mFilteredTypeList.add((Integer)NotificationType.POO_DETECTED);
        mFilteredTypeList.add((Integer)NotificationType.ABNORMAL_DETECTED);
        mFilteredTypeList.add((Integer)NotificationType.FART_DETECTED);
        mFilteredTypeList.add((Integer)NotificationType.BABY_SLEEP);
        mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BABY_FOOD);
        mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK);
        mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK);
        mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_NURSED_BREAST_MILK);

        // Add Notification
        btnAddNotification = (Button)findViewById(R.id.btn_notification_add_notification);
        btnAddNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainLightActivity.this, FloatActivity.class);
                startActivityForResult(intent, REQCODE_FLOAT_ADD_NOTIFICATION);
                overridePendingTransition(0, 0);
            }
        });

        btnAlarmDetail = (Button)findViewById(R.id.btn_notification_detail);
        btnAlarmDetail.setText(getString(R.string.notification_check_detail_information) + " > ");
        btnAlarmDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentDiaperSensor = new Intent(MainLightActivity.this, DeviceSensorActivity.class);
                intentDiaperSensor.putExtra("targetDeviceId", mNotificationDeviceId);
                intentDiaperSensor.putExtra("startPage", DeviceSensorActivity.VIEW_DIAPER_NOTIFICATION);
                startActivity(intentDiaperSensor);
                overridePendingTransition(0, 0);
            }
        });

        rvNotificationList = (RecyclerView)findViewById(R.id.rv_notification_list);
        mMsgAdapter = new FeedbackMsgAdapter(mContext);
        rvNotificationList.setAdapter(mMsgAdapter);
        mMsgAdapter.setRecyclerView(rvNotificationList);
        mMsgAdapter.setOnLoadMoreListener(new NotificationMsgAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (DBG) Log.d(TAG, "onLoadMore");
                mMsgAdapter.setProgressMore(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMsgAdapter.setProgressMore(false);
                        ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getDiaperSensorMessagesV2(mNotificationDeviceType, mNotificationDeviceId, mLastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
                        if (messageList != null && messageList.size() > 0) {
                            for (NotificationMessage mm : messageList) {
                                mNotificationMessageList.add(mm);
                                if (mLastLoadedMessageTimeMs > mm.timeMs) {
                                    mLastLoadedMessageTimeMs = mm.timeMs;
                                }
                            }
                            mMsgAdapter.addList(messageList);
                            showFilteredList();
                            mMsgAdapter.setMoreLoading(false);
                        }
                    }
                }, LOADING_MESSAGE_SEC * 1000);
            }
        });

        loadMessageList();
        showFilteredList();
    }

    public void loadLatestMessageList() {
        if (hasNotificationCreated == false) {
            onCreateNotification();
        }
        if (DBG) Log.e(TAG, "loadLatestMessageList: " + mNotificationDeviceId);
        mLastLoadedMessageTimeMs = System.currentTimeMillis();
        loadMessageList();
    }

    public void loadMessageList() {
        mLastLoadedMessageTimeMs = System.currentTimeMillis() + 3000;
        if (DBG) Log.e(TAG, "loadMessageList: " + mLastLoadedMessageTimeMs);
        mNotificationMessageList.clear();
        long latestCheckedNotificationIdx = mPreferenceMgr.getLatestCheckedNotificationIndex(mNotificationDeviceType, mNotificationDeviceId);
        mPreferenceMgr.setLatestCheckedNotificationIndex(mNotificationDeviceType, mNotificationDeviceId, mPreferenceMgr.getLatestSavedNotificationIndex(mNotificationDeviceType, mNotificationDeviceId, 0));
        mMsgAdapter.setLatestCheckedNotificationIndex(latestCheckedNotificationIdx);

        ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getDiaperSensorMessagesV2(mNotificationDeviceType, mNotificationDeviceId, mLastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
        if (messageList != null && messageList.size() > 0) {
            for (NotificationMessage mm : messageList) {
                mNotificationMessageList.add(mm);
                if (mLastLoadedMessageTimeMs > mm.timeMs) {
                    mLastLoadedMessageTimeMs = mm.timeMs;
                }
                if (DBG) Log.d(TAG, "msg: " + mm.toString());
            }
            if (DBG) Log.e(TAG, "loadMessageList : " + latestCheckedNotificationIdx + " / " + messageList.get(0).msgId + " / checked: " + mPreferenceMgr.getLatestCheckedNotificationIndex(mNotificationDeviceType, mNotificationDeviceId));
        }
    }

    public void showFilteredList() {
        if (DBG) Log.e(TAG, "showFilteredList");
        if (mNotificationMessageList == null) return;
        boolean isValid;
        ArrayList<NotificationMessage> tempNotificationMessageList = new ArrayList<>();
        for (NotificationMessage mm : mNotificationMessageList) {
            isValid = false;
            for (int filterType : mFilteredTypeList) {
                if (mm.notiType == filterType) {
                    if (mm.notiType == NotificationType.PEE_DETECTED || mm.notiType == NotificationType.POO_DETECTED || mm.notiType == NotificationType.FART_DETECTED || mm.notiType == NotificationType.ABNORMAL_DETECTED) {
                        if (Configuration.BETA_TEST_MODE) {
                            if (mm.extra != null && !mm.extra.equals("-") && !mm.extra.equals("")) {
                                // 피드백이 입력되어 있으면 보여주지 않기
                            } else {
                                // 입력이 안되어있으면, 보여주기
                                isValid = true;
                            }
                        } else {
                            isValid = true;
                        }
                    } else {
                        isValid = true;
                    }
                    break;
                } else if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK && mm.notiType == NotificationType.CHAT_USER_INPUT) {
                    isValid = true;
                } else if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK && mm.notiType == NotificationType.CHAT_USER_FEEDBACK) {
                    // 피드백이 입력되어 있지 않으면 보여주기
                    if (mm.extra != null && (mm.extra.equals("d10") || mm.extra.equals("d40") || mm.extra.equals("11") || mm.extra.equals("12") || mm.extra.equals("13") || mm.extra.equals("-") || mm.extra.equals(""))) {
                        isValid = true;
                    }
                    break;
                } else if (mm.notiType == NotificationType.DIAPER_DETACHMENT_DETECTED) {
                    isValid = true;
                    break;
                }
            }
            if (isValid) {
                tempNotificationMessageList.add(mm);
            }
        }

        if (tempNotificationMessageList.size() == 0) {
            mMsgAdapter.setList(tempNotificationMessageList);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mMsgAdapter.setList(tempNotificationMessageList);
        mMsgAdapter.notifyDataSetChanged();
    }

    /*
    public void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG, "onPause");

        NotiManager.getInstance(mContext).cancelMessageNotification(mDeviceType, mDeviceId);
    }
    */

    public void onResumeNotification() {
        if (mConnectionMgr != null) {
            mConnectionMgr.getNotificationFromCloudV2(mNotificationDeviceType, mNotificationDeviceId);
        }
    }


    public void addUserComment(String comment) {
        final NotificationMessage notiMsg = new NotificationMessage(NotificationType.CHAT_USER_INPUT, mNotificationDeviceType, mNotificationDeviceId, comment, System.currentTimeMillis() / 1000 * 1000);
        mServerQueryMgr.setNotificationFeedback(
                notiMsg,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            onResumeNotification();
                        }
                    }
                });
    }

    public void addSleepInput(final long sleepStartUtcTimeMs, final long sleepEndUtcTimeMs) {
        String endTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(sleepEndUtcTimeMs);
        final NotificationMessage notiMsg = new NotificationMessage(NotificationType.BABY_SLEEP, mNotificationDeviceType, mNotificationDeviceId, endTimeString, sleepStartUtcTimeMs);
        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                notiMsg,
                new ServerManager.ServerResponseListener(){
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            onResumeNotification();
                        }
                    }
                });
    }

    public void addFeedingInput(final int feedingType, final String extraValue, final long utcTimeMs) {
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

        final NotificationMessage notiMsg = new NotificationMessage(notiType, mNotificationDeviceType, mNotificationDeviceId, extraValue, utcTimeMs);
        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                notiMsg,
                new ServerManager.ServerResponseListener(){
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            onResumeNotification();
                        }
                    }
                });
    }
}