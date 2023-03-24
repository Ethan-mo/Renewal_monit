package goodmonit.monit.com.kao.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Iterator;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.connection.ConnectionSelectDeviceFragment;
import goodmonit.monit.com.kao.connection.ConnectionSelectPackageFragment;
import goodmonit.monit.com.kao.connection.DiaperSensor.ConnectionMonitBabyInfo;
import goodmonit.monit.com.kao.connection.DiaperSensor.ConnectionMonitCompleted;
import goodmonit.monit.com.kao.connection.DiaperSensor.ConnectionMonitHowToAttachSensor;
import goodmonit.monit.com.kao.connection.DiaperSensor.ConnectionMonitReady;
import goodmonit.monit.com.kao.connection.ElderlySensor.ConnectionElderlySensorCompleted;
import goodmonit.monit.com.kao.connection.ElderlySensor.ConnectionElderlySensorHowToAttach;
import goodmonit.monit.com.kao.connection.ElderlySensor.ConnectionElderlySensorInfo;
import goodmonit.monit.com.kao.connection.ElderlySensor.ConnectionElderlySensorReady;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubAddNewNetwork;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubCompleted;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubInputApPassword;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubPutSensor;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubReady;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubSelectAP;
import goodmonit.monit.com.kao.connection.Hub.ConnectionHubSelectApSecurity;
import goodmonit.monit.com.kao.connection.Lamp.ConnectionLampAddNewNetwork;
import goodmonit.monit.com.kao.connection.Lamp.ConnectionLampCompleted;
import goodmonit.monit.com.kao.connection.Lamp.ConnectionLampInputApPassword;
import goodmonit.monit.com.kao.connection.Lamp.ConnectionLampReady;
import goodmonit.monit.com.kao.connection.Lamp.ConnectionLampSelectAP;
import goodmonit.monit.com.kao.connection.Lamp.ConnectionLampSelectApSecurity;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackageHowToAttachSensor;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackageHubAddNewNetwork;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackageHubInputApPassword;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackageHubSelectAP;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackageHubSelectApSecurity;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackageInfo;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackagePrepareDiaperSensor;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackagePrepareHub;
import goodmonit.monit.com.kao.connection.Package.ConnectionMonitPackagePutSensor;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.NetworkSecurityType;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceElderlyDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.HubApInfo;
import goodmonit.monit.com.kao.dfu.FirmwareUpdateActivity;
import goodmonit.monit.com.kao.dfu.HubFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dfu.LampFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dfu.PackageFirmwareUpdateActivity;
import goodmonit.monit.com.kao.dialog.ProgressHorizontalDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.OrderIndicatorBar;

public class ConnectionActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "ConnectionActv";
    private static final boolean DBG = Configuration.DBG;

    public static final int REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN  = 1;
    public static final int REQUEST_CODE_ALLOW_PERMISSIONS          = 2;

    public static final int STEP_SELECT_DEVICE                 = 1;

    public static final int STEP_MONIT_READY_FOR_CONNECTING    = 10;
    public static final int STEP_MONIT_BABY_INFO               = 11;
    public static final int STEP_MONIT_COMPLETED               = 12;
    public static final int STEP_MONIT_HOW_TO_ATTACH_SENSOR    = 13;

    public static final int STEP_HUB_READY_FOR_CONNECTING      = 20;
    public static final int STEP_HUB_PUT_SENSOR_TO_HUB         = 21;
    public static final int STEP_HUB_SELECT_AP                 = 22;
    public static final int STEP_HUB_INPUT_PASSWORD            = 23;
    public static final int STEP_HUB_ADD_NETWORK               = 24;
    public static final int STEP_HUB_SELECT_SECURITY           = 25;
    public static final int STEP_HUB_COMPLETED                 = 26;

    public static final int STEP_LAMP_READY_FOR_CONNECTING     = 30;
    public static final int STEP_LAMP_SELECT_AP                = 31;
    public static final int STEP_LAMP_CHANGE_AP                = 32;
    public static final int STEP_LAMP_INPUT_PASSWORD           = 33;
    public static final int STEP_LAMP_ADD_NETWORK              = 34;
    public static final int STEP_LAMP_SELECT_SECURITY          = 35;
    public static final int STEP_LAMP_COMPLETED                = 36;

    public static final int STEP_SELECT_PACKAGE                 = 2;
    public static final int STEP_MONIT_PACKAGE_PREPARE_DIAPER_SENSOR    = 40;
    public static final int STEP_MONIT_PACKAGE_PREPARE_HUB              = 41;
    public static final int STEP_MONIT_PACKAGE_PUT_SENSOR_TO_HUB        = 42;
    public static final int STEP_MONIT_PACKAGE_HUB_SELECT_AP            = 43;
    public static final int STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD       = 44;
    public static final int STEP_MONIT_PACKAGE_HUB_ADD_NETWORK          = 45;
    public static final int STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY      = 46;
    public static final int STEP_MONIT_PACKAGE_INFO                     = 47;
    public static final int STEP_MONIT_PACKAGE_HOW_ATTACH_SENSOR        = 48;

    public static final int STEP_ELDERLY_SENSOR_READY_FOR_CONNECTING    = 50;
    public static final int STEP_ELDERLY_SENSOR_INFO                    = 51;
    public static final int STEP_ELDERLY_SENSOR_COMPLETED               = 52;
    public static final int STEP_ELDERLY_SENSOR_HOW_TO_ATTACH_SENSOR    = 53;

    public static final int CODE_HELP_DIAPER_SENSOR_NOT_FOUND           = 100;
    public static final int CODE_HELP_DIAPER_SENSOR_NOT_CONNECTED       = 101;
    public static final int CODE_HELP_DIAPER_SENSOR_ALREADY_REGISTERED  = 102;
    public static final int CODE_HELP_DIAPER_SENSOR_LED_NOT_TURNED_ON   = 103;
    public static final int CODE_HELP_HUB_NOT_FOUND                     = 200;
    public static final int CODE_HELP_HUB_ALREADY_REGISTERED            = 202;
    public static final int CODE_HELP_HUB_AP_NOT_FOUND                  = 203;
    public static final int CODE_HELP_HUB_AP_NOT_CONNECTED              = 204;

    public static final int CODE_HELP_LAMP_NOT_FOUND                     = 300;
    public static final int CODE_HELP_LAMP_ALREADY_REGISTERED            = 302;
    public static final int CODE_HELP_LAMP_AP_NOT_FOUND                  = 303;
    public static final int CODE_HELP_LAMP_AP_NOT_CONNECTED              = 304;

    private static final int MSG_SHOW_FRAGMENT                          = 1;
    private static final int MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS 	= 2;
    private static final int MSG_SHOW_FAILED_DIALOG 	                = 3;

    private static final int TIME_HUB_AP_CONNECTION_WAIT_SEC            = 30;
    public static final int PARAM_FOR_NEW_NETWORK	                    = 99;
    //private static final int TIME_SHOW_ORDER_INDICATOR_WAIT_MS          = 300;

    private ConnectionSelectDeviceFragment mSelectDeviceFragment;

    private ConnectionMonitReady mReadyForMonit;
    private ConnectionMonitBabyInfo mBabyInfoForMonit;
    private ConnectionMonitCompleted mCompletedMonit;
    private ConnectionMonitHowToAttachSensor mHowToAttachMonit;

    private ConnectionElderlySensorReady mReadyForElderlyMonit;
    private ConnectionElderlySensorInfo mInfoForElderlyMonit;
    private ConnectionElderlySensorCompleted mCompletedElderlyMonit;
    private ConnectionElderlySensorHowToAttach mHowToAttachElderlyMonit;

    private ConnectionHubReady mReadyForHub;
    private ConnectionHubPutSensor mPutSensorToHub;
    private ConnectionHubSelectAP mSelectApHub;
    private ConnectionHubInputApPassword mInputPasswordHub;
    private ConnectionHubAddNewNetwork mAddNewNetworkHub;
    private ConnectionHubCompleted mCompletedHub;
    private ConnectionHubSelectApSecurity mSelectApSecurityTypeHub;

    private ConnectionLampReady mReadyForLamp;
    private ConnectionLampSelectAP mSelectApLamp;
    private ConnectionLampInputApPassword mInputPasswordLamp;
    private ConnectionLampAddNewNetwork mAddNewNetworkLamp;
    private ConnectionLampCompleted mCompletedLamp;
    private ConnectionLampSelectApSecurity mSelectApSecurityTypeLamp;

    private ConnectionSelectPackageFragment mSelectPackageFragment;
    private ConnectionMonitPackagePrepareDiaperSensor mPrepareMonitPackageDiaperSensor;
    private ConnectionMonitPackagePrepareHub mPrepareMonitPackageHub;
    private ConnectionMonitPackagePutSensor mPutMonitPackageDiaperSensorToHub;
    private ConnectionMonitPackageHubSelectAP mSelectApMonitPackageHub;
    private ConnectionMonitPackageHubInputApPassword mInputPasswordMonitPackageHub;
    private ConnectionMonitPackageHubAddNewNetwork mAddNewNetworkMonitPackageHub;
    private ConnectionMonitPackageHubSelectApSecurity mSelectApSecurityTypeMonitPackageHub;
    private ConnectionMonitPackageInfo mCompletedMonitPackageInfo;
    private ConnectionMonitPackageHowToAttachSensor mCompletedMonitPackageHowToAttachSensor;

    /** UI Resources */
    // Indicator
    private OrderIndicatorBar orderIndicatorBar;
    private ArrayList<Integer> mHistoryContentIdx;
    private HubApInfo mSelectedHubApInfo;

    private ProgressHorizontalDialog mDlgHubApConnectionProcessing;
    private SimpleDialog mDlgHubApConnectionFailed;
    private int mHubApConnectionWaitSeconds;
    private SimpleDialog mDlgConnectHub;
    private SimpleDialog mDlgSensorFirmwareUpdate;
    private SimpleDialog mDlgHubFirmwareUpdate;
    private SimpleDialog mDlgLampFirmwareUpdate;
    private SimpleDialog mDlgPackageSecurityPatchUpdate, mDlgPackageLatestUpdate;
    private SimpleDialog mDlgSensorSecurityPatchUpdate;
    private SimpleDialog mDlgHubSecurityPatchUpdate;
    private SimpleDialog mDlgLocation;

    private Handler mFragmentHandler;

    /** Control */
    private int mCurrentShowingContentIndex;

    private DeviceElderlyDiaperSensor mConnectingElderlyDiaperSensor;
    private DeviceDiaperSensor mConnectingDiaperSensor;
    private DeviceLamp mConnectingLamp;
    private DeviceInfo mConnectingSensorForHubInfo;
    private DeviceInfo mConnectedHubInfo;
    private DeviceInfo mConnectedLampInfo;
    private long mConnectedHubDeviceId;
    private int mStartStep = STEP_SELECT_DEVICE;
    private long mHubDeviceId = -1;
    private DeviceBLEConnection mTargetBleConnection;
    private DeviceInfo mGuestDeviceInfo;
    private boolean isWaitingForHubCompletedPage = false;
    private boolean isWaitingForSettingDeviceCloudId = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        _setToolBar();

        mContext = this;
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mPreferenceMgr = PreferenceManager.getInstance(this);

        mHistoryContentIdx = new ArrayList<>();
        _initView();
        mCurrentShowingContentIndex = getIntent().getIntExtra("startContent", STEP_SELECT_DEVICE);
        mHubDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        if (DBG) Log.i(TAG, "onCreate : " + mCurrentShowingContentIndex + " / " + mHubDeviceId);
        mStartStep = mCurrentShowingContentIndex;
        showFragment(mCurrentShowingContentIndex);

        _checkPermissions();
    }

    public void checkLocation() {
        if (mConnectionMgr.checkLocationStatus()) {
            if (DBG) Log.d(TAG, "Location On");
        } else {
            if (DBG) Log.d(TAG, "Location Off");
            if (mDlgLocation == null) {
                mDlgLocation = new SimpleDialog(
                        mContext,
                        getString(R.string.dialog_need_to_enable_location),
                        getString(R.string.btn_cancel),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mDlgLocation.dismiss();
                            }
                        },
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                mDlgLocation.dismiss();
                            }
                        });
            }
            if (mDlgLocation != null && !mDlgLocation.isShowing()) {
                if (DBG) Log.d(TAG, "Show location dialog");
                mDlgLocation.show();
            }
        }
    }

    private void _checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int permissionCheck = mContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(mContext.getPackageName());
            if (DBG) Log.d(TAG, "checkPermission: " + permissionCheck + " / " + isWhiteListing);
            // 앱 시작하자마자: 0, false
            // 거부/거부: -1, false
            // 허용/거부: 0, false
            // 거부/허용: -1, true
            // 허용/허용: 0, true
            if (permissionCheck != 0 || !isWhiteListing) {
                if (DBG) Log.d(TAG, "show Permission");
                Intent allowPermissionIntent = new Intent(ConnectionActivity.this, GuideAllowPermission.class);
                startActivityForResult(allowPermissionIntent, REQUEST_CODE_ALLOW_PERMISSIONS);
            }
        }
    }

    public void showFragment(int idx) {
        mHandler.obtainMessage(MSG_SHOW_FRAGMENT, idx, -1).sendToTarget();
    }

    private void _showFragment(int idx) {
        if (DBG) Log.d(TAG, "showFragment : " +idx);
        Fragment fr = null;
        mHistoryContentIdx.add(idx);
        switch(idx) {
            case STEP_SELECT_DEVICE:
                if(DBG)Log.d(TAG, "STEP_SELECT_DEVICE");
                if (mSelectDeviceFragment == null) {
                    mSelectDeviceFragment = new ConnectionSelectDeviceFragment();
                }
                fr = mSelectDeviceFragment;
                break;
            case STEP_MONIT_READY_FOR_CONNECTING:
                if(DBG)Log.d(TAG, "STEP_MONIT_READY_FOR_CONNECTING");
                if (mReadyForMonit == null) {
                    mReadyForMonit = new ConnectionMonitReady();
                }
                fr = mReadyForMonit;
                break;
            case STEP_MONIT_BABY_INFO:
                if(DBG)Log.d(TAG, "STEP_MONIT_BABY_INFO");
                if (mBabyInfoForMonit == null) {
                    mBabyInfoForMonit = new ConnectionMonitBabyInfo();
                }

                if (mConnectingDiaperSensor != null) {
                    if (mConnectingDiaperSensor.getBabyBirthdayYYMMDD() != null && Long.parseLong(mConnectingDiaperSensor.getBabyBirthdayYYMMDD()) > 0) {
                        mBabyInfoForMonit.setBabyName(mConnectingDiaperSensor.name);
                        mBabyInfoForMonit.setBabyBirthdayYYMMDD(mConnectingDiaperSensor.getBabyBirthdayYYMMDD());
                        mBabyInfoForMonit.setBabySex(mConnectingDiaperSensor.getBabySex());
                        mBabyInfoForMonit.setBabyEating(mConnectingDiaperSensor.getBabyEating());
                    } else {
                        mBabyInfoForMonit.setBabySex(-1);
                    }
                }
                fr = mBabyInfoForMonit;
                break;
            case STEP_MONIT_COMPLETED:
                if(DBG)Log.d(TAG, "STEP_MONIT_COMPLETED");
                if (mCompletedMonit == null) {
                    mCompletedMonit = new ConnectionMonitCompleted();
                }
                fr = mCompletedMonit;
                break;
            case STEP_MONIT_HOW_TO_ATTACH_SENSOR:
                if (mHowToAttachMonit == null) {
                    mHowToAttachMonit = new ConnectionMonitHowToAttachSensor();
                }
                fr = mHowToAttachMonit;
                break;

            case STEP_ELDERLY_SENSOR_READY_FOR_CONNECTING:
                if (mReadyForElderlyMonit == null) {
                    mReadyForElderlyMonit = new ConnectionElderlySensorReady();
                }
                fr = mReadyForElderlyMonit;
                break;
            case STEP_ELDERLY_SENSOR_INFO:
                if (mInfoForElderlyMonit == null) {
                    mInfoForElderlyMonit = new ConnectionElderlySensorInfo();
                }

                if (mConnectingElderlyDiaperSensor != null) {
                    if (mConnectingElderlyDiaperSensor.getBabyBirthdayYYMMDD() != null && Long.parseLong(mConnectingElderlyDiaperSensor.getBabyBirthdayYYMMDD()) > 0) {
                        mInfoForElderlyMonit.setName(mConnectingElderlyDiaperSensor.name);
                        mInfoForElderlyMonit.setBirthdayYYMMDD(mConnectingElderlyDiaperSensor.getBabyBirthdayYYMMDD());
                        mInfoForElderlyMonit.setGender(mConnectingElderlyDiaperSensor.getBabySex());
                    } else {
                        mInfoForElderlyMonit.setGender(-1);
                    }
                }
                fr = mInfoForElderlyMonit;
                break;
            case STEP_ELDERLY_SENSOR_COMPLETED:
                if (mCompletedElderlyMonit == null) {
                    mCompletedElderlyMonit = new ConnectionElderlySensorCompleted();
                }
                fr = mCompletedElderlyMonit;
                break;
            case STEP_ELDERLY_SENSOR_HOW_TO_ATTACH_SENSOR:
                if (mHowToAttachElderlyMonit == null) {
                    mHowToAttachElderlyMonit = new ConnectionElderlySensorHowToAttach();
                }
                fr = mHowToAttachElderlyMonit;
                break;
            case STEP_HUB_READY_FOR_CONNECTING:
                if (mReadyForHub == null) {
                    mReadyForHub = new ConnectionHubReady();
                }
                fr = mReadyForHub;
                break;
            case STEP_HUB_PUT_SENSOR_TO_HUB:
                if (mPutSensorToHub == null) {
                    mPutSensorToHub = new ConnectionHubPutSensor();
                }
                fr = mPutSensorToHub;
                break;
            case STEP_HUB_SELECT_AP:
                if (mSelectApHub == null) {
                    mSelectApHub = new ConnectionHubSelectAP();
                }
                fr = mSelectApHub;
                break;
            case STEP_HUB_INPUT_PASSWORD:
                if (mInputPasswordHub == null) {
                    mInputPasswordHub = new ConnectionHubInputApPassword();
                }
                fr = mInputPasswordHub;
                break;
            case STEP_HUB_ADD_NETWORK:
                if (mAddNewNetworkHub == null) {
                    mAddNewNetworkHub = new ConnectionHubAddNewNetwork();
                }
                fr = mAddNewNetworkHub;
                break;
            case STEP_HUB_SELECT_SECURITY:
                if (mSelectApSecurityTypeHub == null) {
                    mSelectApSecurityTypeHub = new ConnectionHubSelectApSecurity();
                }
                fr = mSelectApSecurityTypeHub;
                break;
            case STEP_HUB_COMPLETED:
                if (mCompletedHub == null) {
                    mCompletedHub = new ConnectionHubCompleted();
                }
                fr = mCompletedHub;
                break;
            case STEP_LAMP_READY_FOR_CONNECTING:
                if (mReadyForLamp == null) {
                    mReadyForLamp = new ConnectionLampReady();
                }
                fr = mReadyForLamp;
                break;
            case STEP_LAMP_SELECT_AP:
            case STEP_LAMP_CHANGE_AP:
                if (mSelectApLamp == null) {
                    mSelectApLamp = new ConnectionLampSelectAP();
                }
                fr = mSelectApLamp;
                break;
            case STEP_LAMP_INPUT_PASSWORD:
                if (mInputPasswordLamp == null) {
                    mInputPasswordLamp = new ConnectionLampInputApPassword();
                }
                fr = mInputPasswordLamp;
                break;
            case STEP_LAMP_ADD_NETWORK:
                if (mAddNewNetworkLamp == null) {
                    mAddNewNetworkLamp = new ConnectionLampAddNewNetwork();
                }
                fr = mAddNewNetworkLamp;
                break;
            case STEP_LAMP_SELECT_SECURITY:
                if (mSelectApSecurityTypeLamp == null) {
                    mSelectApSecurityTypeLamp = new ConnectionLampSelectApSecurity();
                }
                fr = mSelectApSecurityTypeLamp;
                break;
            case STEP_LAMP_COMPLETED:
                if (mCompletedLamp == null) {
                    mCompletedLamp = new ConnectionLampCompleted();
                }
                fr = mCompletedLamp;
                break;
            case STEP_SELECT_PACKAGE:
                if (mSelectPackageFragment == null) {
                    mSelectPackageFragment = new ConnectionSelectPackageFragment();
                }
                fr = mSelectPackageFragment;
                break;
            case STEP_MONIT_PACKAGE_PREPARE_DIAPER_SENSOR:
                if (mPrepareMonitPackageDiaperSensor == null) {
                    mPrepareMonitPackageDiaperSensor = new ConnectionMonitPackagePrepareDiaperSensor();
                }
                fr = mPrepareMonitPackageDiaperSensor;
                break;
            case STEP_MONIT_PACKAGE_PREPARE_HUB:
                if (mPrepareMonitPackageHub == null) {
                    mPrepareMonitPackageHub = new ConnectionMonitPackagePrepareHub();
                }
                fr = mPrepareMonitPackageHub;
                break;
            case STEP_MONIT_PACKAGE_PUT_SENSOR_TO_HUB:
                if (mPutMonitPackageDiaperSensorToHub == null) {
                    mPutMonitPackageDiaperSensorToHub = new ConnectionMonitPackagePutSensor();
                }
                fr = mPutMonitPackageDiaperSensorToHub;
                break;
            case STEP_MONIT_PACKAGE_HUB_SELECT_AP:
                if (mSelectApMonitPackageHub == null) {
                    mSelectApMonitPackageHub = new ConnectionMonitPackageHubSelectAP();
                }
                fr = mSelectApMonitPackageHub;
                break;
            case STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD:
                if (mInputPasswordMonitPackageHub == null) {
                    mInputPasswordMonitPackageHub = new ConnectionMonitPackageHubInputApPassword();
                }
                fr = mInputPasswordMonitPackageHub;
                break;
            case STEP_MONIT_PACKAGE_HUB_ADD_NETWORK:
                if (mAddNewNetworkMonitPackageHub == null) {
                    mAddNewNetworkMonitPackageHub = new ConnectionMonitPackageHubAddNewNetwork();
                }
                fr = mAddNewNetworkMonitPackageHub;
                break;
            case STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY:
                if (mSelectApSecurityTypeMonitPackageHub == null) {
                    mSelectApSecurityTypeMonitPackageHub = new ConnectionMonitPackageHubSelectApSecurity();
                }
                fr = mSelectApSecurityTypeMonitPackageHub;
                break;
            case STEP_MONIT_PACKAGE_INFO:
                if (mCompletedMonitPackageInfo == null) {
                    mCompletedMonitPackageInfo = new ConnectionMonitPackageInfo();
                }
                fr = mCompletedMonitPackageInfo;
                break;
            case STEP_MONIT_PACKAGE_HOW_ATTACH_SENSOR:
                if (mCompletedMonitPackageHowToAttachSensor == null) {
                    mCompletedMonitPackageHowToAttachSensor = new ConnectionMonitPackageHowToAttachSensor();
                }
                fr = mCompletedMonitPackageHowToAttachSensor;
                break;
        }

        if (fr != null) {
            try {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                if (mCurrentShowingContentIndex != STEP_SELECT_DEVICE) {
                    if (mCurrentShowingContentIndex < idx) {
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                    } else if (mCurrentShowingContentIndex > idx) {
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right, R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                    }
                }

                mCurrentShowingContentIndex = idx;
                fragmentTransaction.replace(R.id.fragment_connection, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } catch (IllegalStateException e) {

            }
        }
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG, "onDestroy");
        Iterator<DeviceBLEConnection> itr = ConnectionManager.getDeviceBLEConnectionList().values().iterator();
        while (itr.hasNext()) {
            DeviceBLEConnection bleConnection = itr.next();
            if (bleConnection != null) {
                bleConnection.setManuallyConnected(false);
            }
        }
        mConnectionMgr.updateDeviceFullStatusFromCloud(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
    }

    public void setFragmentHandler(Handler handler) {
        if (DBG) Log.d(TAG, "setFragmentHandler");
        mFragmentHandler = handler;
    }

    public void updateView() {
        if (DBG) Log.d(TAG, "updateView : " + mCurrentShowingContentIndex);
        switch(mCurrentShowingContentIndex) {
            case STEP_SELECT_DEVICE:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                orderIndicatorBar.showIndicatorBar(false);
                break;
            case STEP_MONIT_READY_FOR_CONNECTING:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                orderIndicatorBar.initialize();
                orderIndicatorBar.setIndicatorCount(3);
                orderIndicatorBar.setCurrentItem(1);
                orderIndicatorBar.showIndicatorBar(true);
                break;
            case STEP_MONIT_BABY_INFO:
                btnToolbarRight.setText(getString(R.string.btn_register));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBabyInfoForMonit.isValidInformation()) {
                            String babyName = mBabyInfoForMonit.getBabyName();
                            int babySex = mBabyInfoForMonit.getBabySex();
                            int babyEating = mBabyInfoForMonit.getBabyEating();
                            String babyBirthdayYYMMDD = mBabyInfoForMonit.getBabyBirthdayStringYYMMDD();
                            if (babyBirthdayYYMMDD == null || babyBirthdayYYMMDD.length() > 6) {
                                babyBirthdayYYMMDD = babyBirthdayYYMMDD.substring(0, 6);
                            }
                            sendBabyInfo(babyName, babyBirthdayYYMMDD, babySex, babyEating);
                            showFragment(STEP_MONIT_HOW_TO_ATTACH_SENSOR);

                            _initSensorAlarmStatus();
                        }
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                orderIndicatorBar.setCurrentItem(2);
                break;
            case STEP_MONIT_COMPLETED:
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
                        if (DBG) Log.d(TAG, "sensor ver: " + mConnectingDiaperSensor.firmwareVersion + " / " + mPreferenceMgr.getDiaperSensorVersion());
                        if (new VersionManager(mContext).checkDiaperSensorFwUpdateAvailable(mConnectingDiaperSensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion())) {
                            try {
                                mDlgSensorFirmwareUpdate.show();
                            } catch (Exception e) {

                            }
                        } else {
                            try {
                                mDlgConnectHub.show();
                            } catch (Exception e) {

                            }
                        }
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                orderIndicatorBar.setCurrentItem(3);

                break;
            case STEP_MONIT_HOW_TO_ATTACH_SENSOR:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setVisibility(View.GONE);
                orderIndicatorBar.showIndicatorBar(false);
                break;
            case STEP_ELDERLY_SENSOR_READY_FOR_CONNECTING:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                orderIndicatorBar.initialize();
                orderIndicatorBar.setIndicatorCount(3);
                orderIndicatorBar.setCurrentItem(1);
                orderIndicatorBar.showIndicatorBar(true);
                break;
            case STEP_ELDERLY_SENSOR_INFO:
                btnToolbarRight.setText(getString(R.string.btn_register));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mInfoForElderlyMonit.isValidInformation()) {
                            String name = mInfoForElderlyMonit.getName();
                            int gender = mInfoForElderlyMonit.getGender();
                            int eating = -1;
                            String birthdayYYMMDD = mInfoForElderlyMonit.getBirthdayStringYYMMDD();
                            if (birthdayYYMMDD == null || birthdayYYMMDD.length() > 6) {
                                birthdayYYMMDD = birthdayYYMMDD.substring(0, 6);
                            }
                            sendElderlyInfo(name, birthdayYYMMDD, gender, eating);
                            showFragment(STEP_ELDERLY_SENSOR_HOW_TO_ATTACH_SENSOR);

                            _initElderlySensorAlarmStatus();
                        }
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                orderIndicatorBar.setCurrentItem(2);
                break;
            case STEP_ELDERLY_SENSOR_COMPLETED:
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
                        if (DBG) Log.d(TAG, "sensor ver: " + mConnectingElderlyDiaperSensor.firmwareVersion + " / " + mPreferenceMgr.getDiaperSensorVersion());
                        if (new VersionManager(mContext).checkDiaperSensorFwUpdateAvailable(mConnectingElderlyDiaperSensor.firmwareVersion, mPreferenceMgr.getDiaperSensorVersion())) {
                            try {
                                mDlgSensorFirmwareUpdate.show();
                            } catch (Exception e) {

                            }
                        } else {
                            try {
                                mDlgConnectHub.show();
                            } catch (Exception e) {

                            }
                        }
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                orderIndicatorBar.setCurrentItem(3);

                break;
            case STEP_ELDERLY_SENSOR_HOW_TO_ATTACH_SENSOR:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setVisibility(View.GONE);
                orderIndicatorBar.showIndicatorBar(false);
                break;
            case STEP_HUB_READY_FOR_CONNECTING:
                mConnectingSensorForHubInfo = null;
                mConnectedHubInfo = null;
                mConnectedLampInfo = null;
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                btnToolbarRight.setVisibility(View.GONE);
                orderIndicatorBar.setIndicatorCount(4);
                orderIndicatorBar.setCurrentItem(1);
                orderIndicatorBar.showIndicatorBar(true);
                break;
            case STEP_HUB_PUT_SENSOR_TO_HUB:
                // 허브에 센서가 꽂히게 되면, 허브와 센서 BLE연결이 끊어질 것이고,
                // 이때 폰과 연결되어서 바로 AP설정이 가능해야 하므로, reconnectBleDevice()함수를 호출한다.
                if (mConnectionMgr != null) {
                    mConnectionMgr.reconnectBleDevice();
                }
                if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                    orderIndicatorBar.setIndicatorCount(3);
                    orderIndicatorBar.setCurrentItem(1);
                    orderIndicatorBar.showIndicatorBar(true);
                    btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    btnToolbarRight.setVisibility(View.GONE);
                    tvToolbarTitle.setText(getString(R.string.setting_wireless_network_change_title));
                    mPutSensorToHub.setTitle(getString(R.string.setting_wireless_network_change_title));
                    mPutSensorToHub.setButtonName(getString(R.string.btn_next));
                } else {
                    orderIndicatorBar.setCurrentItem(2);
                    btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFragment(STEP_HUB_READY_FOR_CONNECTING);
                        }
                    });
                    mPutSensorToHub.setTitle(getString(R.string.connection_hub_put_sensor_title));
                }
                break;
            case STEP_HUB_SELECT_AP:
                if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                            showFragment(STEP_HUB_PUT_SENSOR_TO_HUB);
                        } else {
                            showFragment(STEP_HUB_READY_FOR_CONNECTING);
                        }
                    }
                });
                btnToolbarRight.setVisibility(View.GONE);
                break;
            case STEP_HUB_INPUT_PASSWORD:
                if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_HUB_SELECT_AP);
                    }
                });
                btnToolbarRight.setText(getString(R.string.btn_connect));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTargetApPassword(mInputPasswordHub.getPassword());
                        sendTargetApInfo();
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                break;
            case STEP_HUB_ADD_NETWORK:
                if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedHubApInfo.name = mAddNewNetworkHub.getApName();
                        mSelectedHubApInfo.password = mAddNewNetworkHub.getApPassword();
                        mSelectedHubApInfo.securityType = mAddNewNetworkHub.getApSecurityType() == NetworkSecurityType.NONE ? 0 : 1; // OPEN(0) Secured(1)
                        mSelectedHubApInfo.index = PARAM_FOR_NEW_NETWORK;
                        sendTargetApInfo();
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_HUB_SELECT_AP);
                    }
                });
                break;
            case STEP_HUB_SELECT_SECURITY:
                if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarRight.setText(getString(R.string.btn_ok));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_HUB_ADD_NETWORK);
                        setTargetApInfoSecurity(mSelectApSecurityTypeHub.getSelectedSecurityType());
                        mAddNewNetworkHub.setApSecurityType(mSelectApSecurityTypeHub.getSelectedSecurityType());
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_HUB_ADD_NETWORK);
                    }
                });
                mSelectApSecurityTypeHub.setSelectedSecurityType(mAddNewNetworkHub.getApSecurityType());

                break;
            case STEP_HUB_COMPLETED:
                final DeviceAQMHub hub = ConnectionManager.getDeviceAQMHub(mConnectedHubDeviceId);
                if (hub != null) {
                    if (DBG) Log.d(TAG, "completed hub : " + hub.toString());
                    hub.setApName(mSelectedHubApInfo.name);
                    hub.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
                } else {
                    if (DBG) Log.e(TAG, "completed hub : NULL(" + mConnectedHubDeviceId + ")");
                }

                if (mStartStep == STEP_HUB_PUT_SENSOR_TO_HUB) {
                    orderIndicatorBar.setCurrentItem(3); // 3/3
                    mConnectionMgr.getUserInfoFromCloud(); // 연결직후 cid 업데이트가 안되어 기기초기화 메뉴 대신 기기삭제 메뉴가 나옴
                    btnToolbarRight.setText(getString(R.string.btn_done));
                    btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    btnToolbarRight.setVisibility(View.VISIBLE);
                    btnToolbarLeft.setVisibility(View.GONE);
                    mCompletedHub.showHubSettingView(false);

                    String connectedApName = "";
                    if (mSelectedHubApInfo.name != null) {
                        if (mSelectedHubApInfo.name.length() > 20) {
                            connectedApName = "\"" + mSelectedHubApInfo.name.substring(0, 20) + "...\"";
                        } else {
                            connectedApName = "\"" + mSelectedHubApInfo.name + "\"";
                        }
                    }
                    mCompletedHub.setDescriptionContents(getString(R.string.connection_hub_selected_ap_name) + connectedApName);
                } else {
                    orderIndicatorBar.setCurrentItem(4); // 4/4
                    mCompletedHub.showHubSettingView(true);
                    btnToolbarRight.setText(getString(R.string.btn_done));
                    btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mCompletedHub.isCompleted() == false) {
                                return;
                            }

                            sendHubInfo(hub,
                                    mCompletedHub.getName(),
                                    mCompletedHub.getMaxTemperature(),
                                    mCompletedHub.getMinTemperature(),
                                    mCompletedHub.getMaxHumidity(),
                                    mCompletedHub.getMinHumidity());

                            // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
                            checkHubFirmwareVersion();
                        }
                    });
                    btnToolbarRight.setVisibility(View.VISIBLE);
                    btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFragment(STEP_SELECT_DEVICE);
                        }
                    });
                }
                break;
            case STEP_LAMP_READY_FOR_CONNECTING:
                mConnectingSensorForHubInfo = null;
                mConnectedHubInfo = null;
                mConnectedLampInfo = null;
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                btnToolbarRight.setVisibility(View.GONE);
                orderIndicatorBar.setIndicatorCount(4);
                orderIndicatorBar.setCurrentItem(1);
                orderIndicatorBar.showIndicatorBar(true);
                break;
            case STEP_LAMP_SELECT_AP:
            case STEP_LAMP_CHANGE_AP:
                if (mStartStep == STEP_LAMP_CHANGE_AP || mCurrentShowingContentIndex == STEP_LAMP_CHANGE_AP) {
                    mConnectingSensorForHubInfo = null;
                    mConnectedHubInfo = null;
                    mConnectingLamp = ConnectionManager.mRegisteredLampList.get(mHubDeviceId);
                    if (mConnectingLamp == null) {
                        if (DBG) Log.d(TAG, "connectingLamp NULL");
                    } else {
                        if (DBG) Log.d(TAG, "connectingLamp : " + mConnectingLamp.toString());
                        mConnectedLampInfo = new DeviceInfo(
                                mConnectingLamp.deviceId,
                                mConnectingLamp.cloudId,
                                DeviceType.LAMP,
                                mConnectingLamp.name,
                                mConnectingLamp.btmacAddress,
                                mConnectingLamp.serial,
                                mConnectingLamp.firmwareVersion,
                                mConnectingLamp.advertisingName,
                                true,
                                true,
                                true,
                                true,
                                true);
                    }

                    btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    btnToolbarRight.setVisibility(View.GONE);
                    orderIndicatorBar.setIndicatorCount(3);
                    orderIndicatorBar.setCurrentItem(1);
                    orderIndicatorBar.showIndicatorBar(true);
                } else {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                    btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFragment(STEP_LAMP_READY_FOR_CONNECTING);
                        }
                    });
                    btnToolbarRight.setVisibility(View.GONE);
                }
                break;
            case STEP_LAMP_INPUT_PASSWORD:
                if (mStartStep == STEP_LAMP_CHANGE_AP) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_LAMP_SELECT_AP);
                    }
                });
                btnToolbarRight.setText(getString(R.string.btn_connect));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTargetApPassword(mInputPasswordLamp.getPassword());
                        sendTargetApInfoToLamp();
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                break;
            case STEP_LAMP_ADD_NETWORK:
                if (mStartStep == STEP_LAMP_CHANGE_AP) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedHubApInfo.name = mAddNewNetworkLamp.getApName();
                        mSelectedHubApInfo.password = mAddNewNetworkLamp.getApPassword();
                        mSelectedHubApInfo.securityType = mAddNewNetworkLamp.getApSecurityType() == NetworkSecurityType.NONE ? 0 : 1; // OPEN(0) Secured(1)
                        mSelectedHubApInfo.index = PARAM_FOR_NEW_NETWORK;
                        sendTargetApInfoToLamp();
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_LAMP_SELECT_AP);
                    }
                });
                break;
            case STEP_LAMP_SELECT_SECURITY:
                if (mStartStep == STEP_LAMP_CHANGE_AP) {
                    orderIndicatorBar.setCurrentItem(2); // 2/3
                } else {
                    orderIndicatorBar.setCurrentItem(3); // 3/4
                }
                btnToolbarRight.setText(getString(R.string.btn_ok));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DBG) Log.d(TAG, "select security from lamp: " + mSelectApSecurityTypeLamp.getSelectedSecurityType());
                        mAddNewNetworkLamp.setApSecurityType(mSelectApSecurityTypeLamp.getSelectedSecurityType());
                        setTargetApInfoSecurity(mSelectApSecurityTypeLamp.getSelectedSecurityType());
                        showFragment(STEP_LAMP_ADD_NETWORK);
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_LAMP_ADD_NETWORK);
                    }
                });
                mSelectApSecurityTypeLamp.setSelectedSecurityType(mAddNewNetworkLamp.getApSecurityType());
                break;
            case STEP_LAMP_COMPLETED:
                final DeviceLamp lamp = ConnectionManager.getDeviceLamp(mConnectedLampInfo.deviceId);
                if (lamp != null) {
                    if (DBG) Log.d(TAG, "completed lamp : " + lamp.toString());
                    lamp.setApName(mSelectedHubApInfo.name);
                    lamp.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
                } else {
                    if (DBG) Log.e(TAG, "completed lamp : NULL(" + mConnectedLampInfo.deviceId + ")");
                }

                if (mStartStep == STEP_LAMP_CHANGE_AP) {
                    orderIndicatorBar.setCurrentItem(3); // 3/3
                    mConnectionMgr.getUserInfoFromCloud(); // 연결직후 cid 업데이트가 안되어 기기초기화 메뉴 대신 기기삭제 메뉴가 나옴
                    btnToolbarRight.setText(getString(R.string.btn_done));
                    btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    btnToolbarRight.setVisibility(View.VISIBLE);
                    btnToolbarLeft.setVisibility(View.GONE);
                    mCompletedLamp.showLampSettingView(false);

                    String connectedApName = "";
                    if (mSelectedHubApInfo.name != null) {
                        if (mSelectedHubApInfo.name.length() > 20) {
                            connectedApName = "\"" + mSelectedHubApInfo.name.substring(0, 20) + "...\"";
                        } else {
                            connectedApName = "\"" + mSelectedHubApInfo.name + "\"";
                        }
                    }
                    mCompletedHub.setDescriptionContents(getString(R.string.connection_hub_selected_ap_name) + connectedApName);
                } else {
                    orderIndicatorBar.setCurrentItem(4); // 4/4
                    mCompletedLamp.showLampSettingView(true);
                    btnToolbarRight.setText(getString(R.string.btn_done));
                    btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mCompletedLamp.isCompleted() == false) {
                                return;
                            }

                            sendLampInfo(lamp,
                                    mCompletedLamp.getName(),
                                    mCompletedLamp.getMaxTemperature(),
                                    mCompletedLamp.getMinTemperature(),
                                    mCompletedLamp.getMaxHumidity(),
                                    mCompletedLamp.getMinHumidity());

                            // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
                            if (DBG) Log.d(TAG, "lamp ver: " + mConnectedLampInfo.firmwareVersion + " / " + mPreferenceMgr.getLampVersion());
                            if (new VersionManager(mContext).checkUpdateAvailable(mConnectedLampInfo.firmwareVersion, mPreferenceMgr.getLampVersion())) {
                                try {
                                    mDlgLampFirmwareUpdate.show();
                                } catch (Exception e) {

                                }
                            } else {
                                finish();
                            }
                        }
                    });
                    btnToolbarRight.setVisibility(View.VISIBLE);
                    btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFragment(STEP_SELECT_DEVICE);
                        }
                    });
                }
                break;

            case STEP_SELECT_PACKAGE:
                orderIndicatorBar.showIndicatorBar(false);
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;

            case STEP_MONIT_PACKAGE_PREPARE_DIAPER_SENSOR:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_PACKAGE);
                    }
                });
                orderIndicatorBar.initialize();
                orderIndicatorBar.setIndicatorCount(5);
                orderIndicatorBar.setCurrentItem(1);
                orderIndicatorBar.showIndicatorBar(true);
                break;

            case STEP_MONIT_PACKAGE_PREPARE_HUB:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_PREPARE_DIAPER_SENSOR);
                    }
                });
                orderIndicatorBar.setCurrentItem(2);
                orderIndicatorBar.showIndicatorBar(true);
                break;

            case STEP_MONIT_PACKAGE_PUT_SENSOR_TO_HUB:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_PREPARE_HUB);
                    }
                });
                orderIndicatorBar.setCurrentItem(3);
                orderIndicatorBar.showIndicatorBar(true);
                break;
            case STEP_MONIT_PACKAGE_HUB_SELECT_AP:
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_PUT_SENSOR_TO_HUB);
                    }
                });
                orderIndicatorBar.setCurrentItem(4);
                orderIndicatorBar.showIndicatorBar(true);
                break;
            case STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD:
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_HUB_SELECT_AP);
                    }
                });
                orderIndicatorBar.setCurrentItem(4);
                orderIndicatorBar.showIndicatorBar(true);
                btnToolbarRight.setText(getString(R.string.btn_connect));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTargetApPassword(mInputPasswordMonitPackageHub.getPassword());
                        sendTargetApInfo();
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                break;
            case STEP_MONIT_PACKAGE_HUB_ADD_NETWORK:
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_HUB_SELECT_AP);
                    }
                });
                orderIndicatorBar.setCurrentItem(4);
                orderIndicatorBar.showIndicatorBar(true);
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedHubApInfo.name = mAddNewNetworkMonitPackageHub.getApName();
                        mSelectedHubApInfo.password = mAddNewNetworkMonitPackageHub.getApPassword();
                        mSelectedHubApInfo.securityType = mAddNewNetworkMonitPackageHub.getApSecurityType() == NetworkSecurityType.NONE ? 0 : 1; // OPEN(0) Secured(1)
                        mSelectedHubApInfo.index = PARAM_FOR_NEW_NETWORK;
                        sendTargetApInfo();
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                break;

            case STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY:
                orderIndicatorBar.setCurrentItem(4);
                orderIndicatorBar.showIndicatorBar(true);
                btnToolbarRight.setText(getString(R.string.btn_ok));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_HUB_ADD_NETWORK);
                        setTargetApInfoSecurity(mSelectApSecurityTypeMonitPackageHub.getSelectedSecurityType());
                        mAddNewNetworkMonitPackageHub.setApSecurityType(mSelectApSecurityTypeMonitPackageHub.getSelectedSecurityType());
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_MONIT_PACKAGE_HUB_ADD_NETWORK);
                    }
                });
                mSelectApSecurityTypeMonitPackageHub.setSelectedSecurityType(mAddNewNetworkMonitPackageHub.getApSecurityType());
                break;
            case STEP_MONIT_PACKAGE_INFO:
                final DeviceAQMHub hub2 = ConnectionManager.getDeviceAQMHub(mConnectedHubDeviceId);
                if (hub2 != null) {
                    if (DBG) Log.d(TAG, "completed hub : " + hub2.toString());
                    hub2.setApName(mSelectedHubApInfo.name);
                    hub2.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
                } else {
                    if (DBG) Log.e(TAG, "completed hub : NULL(" + mConnectedHubDeviceId + ")");
                }

                orderIndicatorBar.setCurrentItem(5); // 4/4
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCompletedMonitPackageInfo.isCompleted() == false) {
                            return;
                        }

                        String babyName = mCompletedMonitPackageInfo.getBabyName();
                        int babySex = mCompletedMonitPackageInfo.getBabySex();
                        int babyEating = mCompletedMonitPackageInfo.getBabyEating();
                        String babyBirthdayYYMMDD = mCompletedMonitPackageInfo.getBabyBirthdayStringYYMMDD();
                        if (babyBirthdayYYMMDD == null || babyBirthdayYYMMDD.length() > 6) {
                            babyBirthdayYYMMDD = babyBirthdayYYMMDD.substring(0, 6);
                        }
                        sendBabyInfo(babyName, babyBirthdayYYMMDD, babySex, babyEating);
                        sendHubInfo(hub2,
                                mCompletedMonitPackageInfo.getRoomName(),
                                mCompletedMonitPackageInfo.getMaxTemperature(),
                                mCompletedMonitPackageInfo.getMinTemperature(),
                                mCompletedMonitPackageInfo.getMaxHumidity(),
                                mCompletedMonitPackageInfo.getMinHumidity());

                        showFragment(STEP_MONIT_PACKAGE_HOW_ATTACH_SENSOR);
                    }
                });
                btnToolbarRight.setVisibility(View.VISIBLE);
                btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFragment(STEP_SELECT_DEVICE);
                    }
                });
                break;
            case STEP_MONIT_PACKAGE_HOW_ATTACH_SENSOR:
                orderIndicatorBar.showIndicatorBar(false);
                btnToolbarRight.setVisibility(View.GONE);
                btnToolbarLeft.setVisibility(View.GONE);
                break;
        }
    }

    public void checkSensorFirmwareVersion() {
        // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
        String currSensorFwVersion = null;
        if (mConnectingDiaperSensor != null) {
            currSensorFwVersion = mConnectingDiaperSensor.firmwareVersion;
        }
        if (DBG) Log.d(TAG, "sensor ver: " + currSensorFwVersion + " / " + mPreferenceMgr.getDiaperSensorVersion() + " / " + mPreferenceMgr.getDiaperSensorForceVersion());
        boolean updateSensor = new VersionManager(mContext).checkUpdateAvailable(currSensorFwVersion, mPreferenceMgr.getDiaperSensorVersion());
        boolean updateSensorForce = new VersionManager(mContext).checkUpdateAvailable(currSensorFwVersion, mPreferenceMgr.getDiaperSensorForceVersion());

        if (DBG) Log.d(TAG, "update sensor version: latest/force: " + updateSensor + "/" + updateSensorForce);

        if (updateSensorForce) {
            try {
                mDlgSensorSecurityPatchUpdate.show();
            } catch (Exception e) {

            }
        } else if (updateSensor) {
            try {
                mDlgSensorFirmwareUpdate.show();
            } catch (Exception e) {

            }
        } else {
            finish();
        }
    }

    public void checkHubFirmwareVersion() {
        // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
        String currHubFwVersion = null;
        if (mConnectedHubInfo != null) {
            currHubFwVersion = mConnectedHubInfo.firmwareVersion;
        }
        if (DBG) Log.d(TAG, "hub ver: " + currHubFwVersion + " / " + mPreferenceMgr.getHubVersion() + " / " + mPreferenceMgr.getHubForceVersion());
        boolean updateHub = new VersionManager(mContext).checkUpdateAvailable(currHubFwVersion, mPreferenceMgr.getHubVersion());
        boolean updateHubForce = new VersionManager(mContext).checkUpdateAvailable(currHubFwVersion, mPreferenceMgr.getHubForceVersion());

        if (DBG) Log.d(TAG, "update hub version: latest/force: " + updateHub + "/" + updateHubForce);

        if (updateHubForce) {
            try {
                mDlgHubSecurityPatchUpdate.show();
            } catch (Exception e) {

            }
        } else if (updateHub) {
            try {
                mDlgHubFirmwareUpdate.show();
            } catch (Exception e) {

            }
        } else {
            finish();
        }
    }

    public void checkDeviceFirmwareVersion() {
        // 펌웨어 버전 비교, 최신버전이 아니면 업데이트 띄우기
        String currSensorFwVersion = null;
        String currHubFwVersion = null;
        if (mConnectingDiaperSensor != null) {
            currSensorFwVersion = mConnectingDiaperSensor.firmwareVersion;
        }
        if (mConnectedHubInfo != null) {
            currHubFwVersion = mConnectedHubInfo.firmwareVersion;
        }
        if (DBG) Log.d(TAG, "sensor ver: " + currSensorFwVersion + " / " + mPreferenceMgr.getDiaperSensorVersion() + " / " + mPreferenceMgr.getDiaperSensorForceVersion());
        if (DBG) Log.d(TAG, "hub ver: " + currHubFwVersion + " / " + mPreferenceMgr.getHubVersion() + " / " + mPreferenceMgr.getHubForceVersion());
        boolean updateSensor = new VersionManager(mContext).checkUpdateAvailable(currSensorFwVersion, mPreferenceMgr.getDiaperSensorVersion());
        boolean updateHub = new VersionManager(mContext).checkUpdateAvailable(currHubFwVersion, mPreferenceMgr.getHubVersion());

        boolean updateSensorForce = new VersionManager(mContext).checkUpdateAvailable(currSensorFwVersion, mPreferenceMgr.getDiaperSensorForceVersion());
        boolean updateHubForce = new VersionManager(mContext).checkUpdateAvailable(currHubFwVersion, mPreferenceMgr.getHubForceVersion());

        if (DBG) Log.d(TAG, "update sensor version: latest/force: " + updateSensor + "/" + updateSensorForce);
        if (DBG) Log.d(TAG, "update hub version: latest/force: " + updateHub + "/" + updateHubForce);

        if (updateSensorForce || updateHubForce) {
            try {
                mDlgPackageSecurityPatchUpdate.show();
            } catch (Exception e) {

            }
        } else if (updateSensor || updateHub) {
            try {
                mDlgPackageLatestUpdate.show();
            } catch (Exception e) {

            }
        } else {
            finish();
        }
    }

    public void closeApConnectionDialog() {
        // Connection Dialog 닫기
        mHandler.removeMessages(MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS);
        if (mDlgHubApConnectionProcessing != null && mDlgHubApConnectionProcessing.isShowing()) {
            mDlgHubApConnectionProcessing.setProgress(100);
            mDlgHubApConnectionProcessing.dismiss();
        }
    }

    /**
     *  hasBLEConnectedSensor
     *  현재 BLE로 연결된 센서가 있는지 확인하는 함수
     *  허브 제품 등록시, BLE연결 센서가 있으면 센서 삽입 애니메이션으로 넘어가며, 없으면 센서 연결 화면으로 유도함
     */
    public boolean hasBLEConnectedSensor() {
        boolean hasConnectedSensor = false;
        for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
            if (sensor == null) continue;

            if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                hasConnectedSensor = true;
                break;
            }
        }
        return hasConnectedSensor;
    }

    /**
     *  sendStartWifiScan
     *  센서를 통해 허브에 Wi-Fi 스캔 시작 명령을 보낼 때 호출하는 함수
     *  어떤 센서가 허브에 연결된지 모르기 때문에, 일단 BLE로 연결된 센서에는 모두 Wi-Fi 스캔 시작 명령을 보낸다.
     */
    public void sendStartWifiScan() {
        if (mConnectingSensorForHubInfo != null) {
            DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mConnectingSensorForHubInfo.deviceId, mConnectingSensorForHubInfo.type);
            if (bleConnection != null) {
                if (DBG) Log.d(TAG, "sendStartWifiScan : " + bleConnection.toString());
                bleConnection.startWifiScan();
            }
        }
    }

    /**
     *  sendStartWifiScanToLamp
     *  수유등에 Wi-Fi 스캔 시작 명령을 보낼 때 호출하는 함수
     */
    public void sendStartWifiScanToLamp() {
        if (mConnectedLampInfo == null) return;
        DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mConnectedLampInfo.deviceId, DeviceType.LAMP);
        if (bleConnection != null) {
            if (DBG) Log.d(TAG, "sendStartWifiScan(Lamp) : " + bleConnection.toString());
            bleConnection.startWifiScan();
        }
    }

    /**
     *  setDiaperSensorCloudIdToServer
     *  기저귀센서 인터넷 연결 완료 후, 정보 입력페이지 진입 전에 Cloud ID 등록
     */
    public void setDiaperSensorCloudIdToServer() {
        if (mConnectingDiaperSensor != null) {
            if (DBG) Log.d(TAG, "setDiaperSensorCloudIdToServer : " + mConnectingDiaperSensor.toString());
            mServerQueryMgr.setCloudId(
                    mConnectingDiaperSensor.type,
                    mConnectingDiaperSensor.deviceId,
                    mConnectingDiaperSensor.getEnc(),
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mHandler.obtainMessage(ConnectionManager.MSG_SET_CLOUD_ID_TO_CLOUD, DeviceType.DIAPER_SENSOR, (int)mConnectingDiaperSensor.deviceId).sendToTarget();
                            }
                        }
                    });
        }
    }

    /**
     *  setElderlyDiaperSensorCloudIdToServer
     *  기저귀센서 인터넷 연결 완료 후, 정보 입력페이지 진입 전에 Cloud ID 등록
     */
    public void setElderlyDiaperSensorCloudIdToServer() {
        if (mConnectingElderlyDiaperSensor != null) {
            if (DBG) Log.d(TAG, "setElderlyDiaperSensorCloudIdToServer : " + mConnectingElderlyDiaperSensor.toString());
            mServerQueryMgr.setCloudId(
                    mConnectingElderlyDiaperSensor.type,
                    mConnectingElderlyDiaperSensor.deviceId,
                    mConnectingElderlyDiaperSensor.getEnc(),
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mHandler.obtainMessage(ConnectionManager.MSG_SET_CLOUD_ID_TO_CLOUD, DeviceType.ELDERLY_DIAPER_SENSOR, (int)mConnectingElderlyDiaperSensor.deviceId).sendToTarget();
                            }
                        }
                    });
        }
    }

    /**
     *  setHubCloudIdToServer
     *  허브 인터넷 연결 완료 후, 정보 입력페이지 진입 전에 Cloud ID 등록
     */
    public void setHubCloudIdToServer() {
        if (mConnectedHubInfo != null) {
            if (DBG) Log.d(TAG, "setHubCloudIdToServer : " + mConnectedHubInfo.toString());
            mServerQueryMgr.setCloudId(
                    mConnectedHubInfo.type,
                    mConnectedHubInfo.deviceId,
                    mConnectedHubInfo.getEnc(),
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mHandler.obtainMessage(ConnectionManager.MSG_SET_CLOUD_ID_TO_CLOUD, DeviceType.AIR_QUALITY_MONITORING_HUB, (int)mConnectedHubInfo.deviceId).sendToTarget();
                            }
                        }
                    });
        }
        /*
        if (mConnectingSensorForHubInfo != null) {
            DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mConnectingSensorForHubInfo.deviceId, mConnectingSensorForHubInfo.type);
            if (bleConnection != null) {
                if (DBG) Log.d(TAG, "setHubCloudIdToServer : " + bleConnection.toString());
                bleConnection.setHubCloudIdToServer(new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            mHandler.obtainMessage(ConnectionManager.MSG_SET_CLOUD_ID_TO_CLOUD, DeviceType.AIR_QUALITY_MONITORING_HUB, -1).sendToTarget();
                        }
                    }
                });
            }
        }
        */
    }

    public void setLampCloudIdToServer() {
        if (mConnectedLampInfo != null) {
            if (DBG) Log.d(TAG, "setLampCloudIdToServer : " + mConnectedLampInfo.toString());
            mServerQueryMgr.setCloudId(
                    mConnectedLampInfo.type,
                    mConnectedLampInfo.deviceId,
                    mConnectedLampInfo.getEnc(),
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                mHandler.obtainMessage(ConnectionManager.MSG_SET_CLOUD_ID_TO_CLOUD, DeviceType.LAMP, (int)mConnectedLampInfo.deviceId).sendToTarget();
                            }
                        }
                    });
        }
    }

    /**
     *  sendTargetApInfo
     *  사용자가 입력한 AP 정보를 허브에 보내기 위한 함수
     *  AP 정보를 허브에 보내기 전에 UI를 활성화 시키고 보냄
     *  DeviceBLEConnection 객체를 통해 허브에 보내도록 되어있음
     */
    public void sendTargetApInfo() {
        startWaitHubApConnection();
        if (mConnectingSensorForHubInfo != null) {
            DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mConnectingSensorForHubInfo.deviceId, mConnectingSensorForHubInfo.type);
            if (bleConnection != null) {
                if (DBG) Log.d(TAG, "sendTargetApInfo : " + bleConnection.toString() + " / " + mSelectedHubApInfo.name + " / " + mSelectedHubApInfo.securityType + " / " + mSelectedHubApInfo.index + " / " + mSelectedHubApInfo.password);
                bleConnection.setApInfo(mSelectedHubApInfo);
            }
        }
    }

    /**
     *  sendTargetApInfoToLamp
     *  사용자가 입력한 AP 정보를 수유등에 보내기 위한 함수
     *  AP 정보를 허브에 보내기 전에 UI를 활성화 시키고 보냄
     *  DeviceBLEConnection 객체를 통해 허브에 보내도록 되어있음
     */
    public void sendTargetApInfoToLamp() {
        startWaitHubApConnection();
        if (mConnectedLampInfo != null) {
            DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mConnectedLampInfo.deviceId, DeviceType.LAMP);
            if (bleConnection != null) {
                if (DBG) Log.d(TAG, "sendTargetApInfo : " + bleConnection.toString() + " / " + mSelectedHubApInfo.name + " / " + mSelectedHubApInfo.securityType + " / " + mSelectedHubApInfo.index + " / " + mSelectedHubApInfo.password);
                bleConnection.setApInfo(mSelectedHubApInfo);
            }
        }
    }

    /**
     *  sendCheckHubStatus
     *  센서를 허브에 꽂은 뒤, 센서를 통해 허브와 연결 되면, 허브의 Device ID등 기본 정보를 읽어오도록
     *  DeviceBLEConnection 객체에 명령을 내리는 함수
     *  실제 CloudID, DeviceID 발급 등은 DeviceBLEConnection 에서 진행함.
     */
    public void sendCheckHubStatus() {
        if (DBG) Log.d(TAG, "sendCheckHubStatus : " + mHubDeviceId);
        boolean sensorDetected = false;

        // 1초에 한번씩 수행됨
        // 따라서 일단 checkHubDeviceInfo(), 이후 타겟이 정해지면 registerHubDevice()

        if (mHubDeviceId == -1) { // 허브 신규등록
            for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
                if (sensor == null) continue;

                if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED && sensor.getOperationStatus() >= DeviceStatus.OPERATION_HUB_NO_CHARGE) {
                    sensorDetected = true;
                    if (DBG) Log.d(TAG, "sensorDetected");

                    DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensor.deviceId, sensor.type);
                    if (bleConnection != null) {
                        if ((bleConnection.getHubDeviceInfo() == null)
                                || (bleConnection.getHubDeviceInfo().serial == null)
                                || (bleConnection.getHubDeviceInfo().serial.length() < 11)) {
                            if (DBG) Log.e(TAG, "bleConnection satisfied 1");
                            bleConnection.checkSensorStatus();
                            bleConnection.checkHubDeviceInfo(false);
                        } else if ((bleConnection.getHubDeviceInfo() != null)
                                && (bleConnection.getHubDeviceInfo().deviceId > 0)) {
                            if (bleConnection.getHubDeviceInfo().cloudId == 0) { // 최초등록
                                if (DBG) Log.e(TAG, "bleConnection satisfied 2");
                            } else { // 이미 연결
                                if (DBG) Log.e(TAG, "bleConnection satisfied 3");
                            }
                            bleConnection.checkHubDeviceInfo(true);
                            break;
                        } else {
                            if (DBG) Log.e(TAG, "bleConnection not satisfied: " + bleConnection.getHubDeviceInfo().serial + "(" + bleConnection.getHubDeviceInfo().serial.length() + ") / " + bleConnection.getHubDeviceInfo().deviceId + " / " + bleConnection.getHubDeviceInfo().cloudId);
                        }
                    } else {
                        if (DBG) Log.e(TAG, "bleConnection NULL");
                    }
                }
            }
            for (DeviceElderlyDiaperSensor sensor : ConnectionManager.mRegisteredElderlyDiaperSensorList.values()) {
                if (sensor == null) continue;

                if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED && sensor.getOperationStatus() >= DeviceStatus.OPERATION_HUB_NO_CHARGE) {
                    sensorDetected = true;
                    if (DBG) Log.d(TAG, "sensorDetected");

                    DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensor.deviceId, sensor.type);
                    if (bleConnection != null) {
                        if ((bleConnection.getHubDeviceInfo() == null)
                                || (bleConnection.getHubDeviceInfo().serial == null)
                                || (bleConnection.getHubDeviceInfo().serial.length() < 11)) {
                            if (DBG) Log.e(TAG, "bleConnection satisfied 1");
                            bleConnection.checkSensorStatus();
                            bleConnection.checkHubDeviceInfo(false);
                        } else if ((bleConnection.getHubDeviceInfo() != null)
                                && (bleConnection.getHubDeviceInfo().deviceId > 0)) {
                            if (bleConnection.getHubDeviceInfo().cloudId == 0) { // 최초등록
                                if (DBG) Log.e(TAG, "bleConnection satisfied 2");
                            } else { // 이미 연결
                                if (DBG) Log.e(TAG, "bleConnection satisfied 3");
                            }
                            bleConnection.checkHubDeviceInfo(true);
                            break;
                        } else {
                            if (DBG) Log.e(TAG, "bleConnection not satisfied: " + bleConnection.getHubDeviceInfo().serial + "(" + bleConnection.getHubDeviceInfo().serial.length() + ") / " + bleConnection.getHubDeviceInfo().deviceId + " / " + bleConnection.getHubDeviceInfo().cloudId);
                        }
                    } else {
                        if (DBG) Log.e(TAG, "bleConnection NULL");
                    }
                }
            }
            if (sensorDetected == false) {
                if (DBG) Log.d(TAG, "sendCheckHubStatus1");
                if (ConnectionManager.getDeviceBLEConnectionList() != null) {
                    // Force scan
                    mConnectionMgr.requestForceLeScan();
                    /*
                    for (DeviceBLEConnection connection : ConnectionManager.getDeviceBLEConnectionList().values()) {
                        if (connection != null) connection.connect();
                    }
                    */
                }
            }
        } else { // 기존 허브 AP변경
            if (mTargetBleConnection == null) {
                for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
                    if (sensor == null) continue;
                    if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED && sensor.getOperationStatus() >= DeviceStatus.OPERATION_HUB_NO_CHARGE) {
                        DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensor.deviceId, sensor.type);
                        if (bleConnection != null) {
                            if ((bleConnection.getHubDeviceInfo() == null)
                                    || (bleConnection.getHubDeviceInfo().serial == null)
                                    || (bleConnection.getHubDeviceInfo().serial.length() < 11)) {
                                bleConnection.checkSensorStatus();
                                bleConnection.checkHubDeviceInfo(false);
                            } else if ((bleConnection.getHubDeviceInfo() != null)
                                    && (mHubDeviceId == bleConnection.getHubDeviceInfo().deviceId)) {
                                if (DBG) Log.d(TAG, "set TargetBLEConnection : " + bleConnection.toString());
                                mTargetBleConnection = bleConnection;
                                sensorDetected = true;
                                break;
                            }
                        }
                    }
                }
                for (DeviceElderlyDiaperSensor sensor : ConnectionManager.mRegisteredElderlyDiaperSensorList.values()) {
                    if (sensor == null) continue;
                    if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED && sensor.getOperationStatus() >= DeviceStatus.OPERATION_HUB_NO_CHARGE) {
                        DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensor.deviceId, sensor.type);
                        if (bleConnection != null) {
                            if ((bleConnection.getHubDeviceInfo() == null)
                                    || (bleConnection.getHubDeviceInfo().serial == null)
                                    || (bleConnection.getHubDeviceInfo().serial.length() < 11)) {
                                bleConnection.checkSensorStatus();
                                bleConnection.checkHubDeviceInfo(false);
                            } else if ((bleConnection.getHubDeviceInfo() != null)
                                    && (mHubDeviceId == bleConnection.getHubDeviceInfo().deviceId)) {
                                if (DBG) Log.d(TAG, "set TargetBLEConnection : " + bleConnection.toString());
                                mTargetBleConnection = bleConnection;
                                sensorDetected = true;
                                break;
                            }
                        }
                    }
                }
            } else {
                sensorDetected =  true;
            }

            if (mTargetBleConnection != null) {
                if (DBG) Log.d(TAG, "through : " + mTargetBleConnection.toString() + " / " + sensorDetected);
                if (sensorDetected) {
                    if (DBG) Log.d(TAG, "sendCheckHubStatus3");
                    mTargetBleConnection.checkHubDeviceInfo(true);
                } else {
                    if (DBG) Log.d(TAG, "sendCheckHubStatus4");
                    mTargetBleConnection.connect();
                }
            }
        }
    }

    /**
     *  sendLampInfo
     *  사용자가 입력한 허브정보를 인터넷에 전송하는 함수
     */
    public void sendLampInfo(final DeviceLamp lamp, String name, float maxTemperature, float minTemperature, final float maxHumidity, final float minHumidity) {
        if (lamp == null) return;

        if ((maxHumidity <= minHumidity) || (maxTemperature <= minTemperature)) {
            showToast(mContext.getString(R.string.toast_invalid_min_max_range));
            return;
        }

        mPreferenceMgr.setDeviceName(DeviceType.LAMP, lamp.deviceId, name);
        mServerQueryMgr.setDeviceName(DeviceType.LAMP,
                lamp.deviceId,
                lamp.serial.substring(lamp.serial.length() - 4),
                name,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

                        } else {
                            if (DBG) Log.e(TAG, "setDeviceName Failed");
                        }
                    }
                });

        if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
            maxTemperature = UnitConvertUtil.getCelsiusFromFahrenheit(maxTemperature);
            minTemperature = UnitConvertUtil.getCelsiusFromFahrenheit(minTemperature);
        }
        final float convertedMaxTemperature = maxTemperature;
        final float convertedMinTemperature = minTemperature;

        mServerQueryMgr.setAlarmThreshold(
                mConnectedLampInfo.type,
                mConnectedLampInfo.deviceId,
                mConnectedLampInfo.getEnc(),
                maxTemperature,
                minTemperature,
                maxHumidity,
                minHumidity,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            lamp.setMaxTemperature(convertedMaxTemperature);
                            lamp.setMinTemperature(convertedMinTemperature);
                            lamp.setMaxHumidity(maxHumidity);
                            lamp.setMinHumidity(minHumidity);
                        }
                    }
                });
    }


    /**
     *  sendHubInfo
     *  사용자가 입력한 허브정보를 인터넷에 전송하는 함수
     */
    public void sendHubInfo(final DeviceAQMHub hub, String name, float maxTemperature, float minTemperature, final float maxHumidity, final float minHumidity) {
        if (hub == null) return;

        if ((maxHumidity <= minHumidity) || (maxTemperature <= minTemperature)) {
            showToast(mContext.getString(R.string.toast_invalid_min_max_range));
            return;
        }

        mPreferenceMgr.setDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, hub.deviceId, name);
        mServerQueryMgr.setDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB,
                hub.deviceId,
                hub.getEnc(),
                name,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

                        } else {
                            if (DBG) Log.e(TAG, "setDeviceName Failed");
                        }
                    }
                });

        if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
            FirebaseAnalyticsManager.getInstance(mContext).sendHubSettingTemperatureScale(hub.deviceId, "F");
            maxTemperature = UnitConvertUtil.getCelsiusFromFahrenheit(maxTemperature);
            minTemperature = UnitConvertUtil.getCelsiusFromFahrenheit(minTemperature);
        } else {
            FirebaseAnalyticsManager.getInstance(mContext).sendHubSettingTemperatureScale(hub.deviceId, "C");
        }
        final float convertedMaxTemperature = maxTemperature;
        final float convertedMinTemperature = minTemperature;

        mServerQueryMgr.setAlarmThreshold(
                mConnectedHubInfo.type,
                mConnectedHubInfo.deviceId,
                mConnectedHubInfo.getEnc(),
                maxTemperature,
                minTemperature,
                maxHumidity,
                minHumidity,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            FirebaseAnalyticsManager.getInstance(mContext).sendHubSettingTemperatureRange(mConnectedHubInfo.deviceId, UnitConvertUtil.getFahrenheitFromCelsius(convertedMinTemperature), UnitConvertUtil.getFahrenheitFromCelsius(convertedMaxTemperature));
                            FirebaseAnalyticsManager.getInstance(mContext).sendHubSettingHumidityRange(mConnectedHubInfo.deviceId, minHumidity, maxHumidity);
                            hub.setMaxTemperature(convertedMaxTemperature);
                            hub.setMinTemperature(convertedMinTemperature);
                            hub.setMaxHumidity(maxHumidity);
                            hub.setMinHumidity(minHumidity);
                        }
                    }
                });
    }

    /**
     *  sendBabyInfo
     *  사용자가 선택한 아기정보를 BLE로 전송하고 Cloud서버에 전송하는 함수
     */
    public void sendBabyInfo(String name, String birthdayYYMMDD, int sex, int eating) {
        if (DBG) Log.d(TAG, "sendBabyInfo : " + name + " / " + birthdayYYMMDD + " / " + sex);
        if (mConnectingDiaperSensor != null) {
            DeviceBLEConnection conn = ConnectionManager.getDeviceBLEConnection(mConnectingDiaperSensor.deviceId, mConnectingDiaperSensor.type);
            if (conn != null) {
                conn.setBabyInfo(name, birthdayYYMMDD, sex, eating);
            }
            mPreferenceMgr.setDeviceName(DeviceType.DIAPER_SENSOR, mConnectingDiaperSensor.deviceId, name);
            showProgressBar(true);
            mServerQueryMgr.setBabyInfo(DeviceType.DIAPER_SENSOR,
                    mConnectingDiaperSensor.deviceId,
                    mConnectingDiaperSensor.serial.substring(mConnectingDiaperSensor.serial.length() - 4),
                    name,
                    birthdayYYMMDD,
                    sex,
                    eating,
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            showProgressBar(false);
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

                            } else {
                                if (DBG) Log.e(TAG, "setBabyInfo Failed");
                            }
                        }
                    });
        }
    }

    /**
     *  sendElderlyInfo
     *  사용자가 선택한 환자정보를 BLE로 전송하고 Cloud서버에 전송하는 함수
     */
    public void sendElderlyInfo(String name, String birthdayYYMMDD, int sex, int eating) {
        if (DBG) Log.d(TAG, "sendElderlyInfo : " + name + " / " + birthdayYYMMDD + " / " + sex);
        if (mConnectingElderlyDiaperSensor != null) {
            DeviceBLEConnection conn = ConnectionManager.getDeviceBLEConnection(mConnectingElderlyDiaperSensor.deviceId, mConnectingElderlyDiaperSensor.type);
            if (conn != null) {
                conn.setBabyInfo(name, birthdayYYMMDD, sex, eating);
            }
            mPreferenceMgr.setDeviceName(DeviceType.ELDERLY_DIAPER_SENSOR, mConnectingElderlyDiaperSensor.deviceId, name);
            showProgressBar(true);
            mServerQueryMgr.setBabyInfo(DeviceType.ELDERLY_DIAPER_SENSOR,
                    mConnectingElderlyDiaperSensor.deviceId,
                    mConnectingElderlyDiaperSensor.serial.substring(mConnectingElderlyDiaperSensor.serial.length() - 4),
                    name,
                    birthdayYYMMDD,
                    sex,
                    eating,
                    new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            showProgressBar(false);
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

                            } else {
                                if (DBG) Log.e(TAG, "setBabyInfo Failed");
                            }
                        }
                    });
        }
    }

    /**
     *  setTargetApInfo
     *  사용자가 선택한 AP정보를 저장하기 위한 함수
     *  OPEN AP를 선택시에는 비밀번호 없이 바로 BLE로 전송해야 하므로 비밀번호 저장 함수와는 구분
     */
    public void setTargetApInfo(HubApInfo hubApInfo) {
        mSelectedHubApInfo = hubApInfo;
        if (mCurrentShowingContentIndex == STEP_LAMP_SELECT_AP || mCurrentShowingContentIndex == STEP_LAMP_CHANGE_AP) {
            if (mInputPasswordLamp != null) {
                mInputPasswordLamp.setApSSID(hubApInfo.name);
            }
        } else if (mCurrentShowingContentIndex == STEP_MONIT_PACKAGE_HUB_SELECT_AP) {
            if (mInputPasswordMonitPackageHub != null) {
                mInputPasswordMonitPackageHub.setApSSID(hubApInfo.name);
            }
        } else {
            if (mInputPasswordHub != null) {
                mInputPasswordHub.setApSSID(hubApInfo.name);
            }
        }
    }

    /**
     *  setTargetApInfoSecurity
     *  사용자가 선택한 AP의 암호화타입을 저장하기 위한 함수
     */
    public void setTargetApInfoSecurity(int securityType) {
        mSelectedHubApInfo.securityType = securityType;
    }

    /**
     *  setTargetApPassword
     *  사용자가 입력한 AP 비밀번호를 App 내부에 저장하기 위한 함수
     *  비밀번호까지 저장한 뒤 AP이름, 비밀번호, 암호화타입을 BLE로 전송함
     */
    public void setTargetApPassword(String password) {
        mSelectedHubApInfo.password = password;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN:
                if (resultCode != Activity.RESULT_OK) {
                    showToast(getString(R.string.toast_need_to_enable_bluetooth));
                    finish();
                }
                break;
            case REQUEST_CODE_ALLOW_PERMISSIONS:
                //
                break;
        }
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        tvToolbarTitle = ((TextView)findViewById(R.id.tv_toolbar_title));
        tvToolbarTitle.setText(getString(R.string.title_connection));
    }

    /**
     *  startWaitHubApConnection
     *  허브 무선네트워크 AP설정 후 AP연결에 대한 응답을 기다리기 전 UI 개시 함수
     */
    public void startWaitHubApConnection() {
        mHubApConnectionWaitSeconds = 0;
        try {
            switch (mCurrentShowingContentIndex) {
                case STEP_LAMP_SELECT_AP:
                case STEP_LAMP_INPUT_PASSWORD:
                case STEP_LAMP_ADD_NETWORK:
                case STEP_LAMP_SELECT_SECURITY:
                    mDlgHubApConnectionProcessing.setContents(getString(R.string.dialog_contents_lamp_ap_connecting));
                    break;
                case STEP_HUB_SELECT_AP:
                case STEP_HUB_INPUT_PASSWORD:
                case STEP_HUB_ADD_NETWORK:
                case STEP_HUB_SELECT_SECURITY:
                    mDlgHubApConnectionProcessing.setContents(getString(R.string.dialog_contents_hub_ap_connecting));
                    break;
                case STEP_MONIT_PACKAGE_HUB_SELECT_AP:
                case STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD:
                case STEP_MONIT_PACKAGE_HUB_ADD_NETWORK:
                case STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY:
                    mDlgHubApConnectionProcessing.setContents(getString(R.string.dialog_contents_hub_ap_connecting));
                default:
                    mDlgHubApConnectionProcessing.setContents(getString(R.string.dialog_contents_hub_ap_connecting));
                    break;
            }
            mDlgHubApConnectionProcessing.show();
        } catch (Exception e) {

        }
        mHandler.sendEmptyMessage(MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS);
    }

    @Override
    public void onBackPressed() {
        btnToolbarLeft.callOnClick();
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        orderIndicatorBar = new OrderIndicatorBar(mContext,
                findViewById(android.R.id.content),
                3,
                R.drawable.bg_indicator_focused,
                R.drawable.bg_indicator_passed,
                R.drawable.bg_indicator_not_passed,
                R.color.colorIndicatorLinePassed,
                R.color.colorIndicatorLine,
                (int)getResources().getDimension(R.dimen.indicator_focused_size),
                (int)getResources().getDimension(R.dimen.indicator_not_focused_size));

        mSelectDeviceFragment = new ConnectionSelectDeviceFragment();
        mReadyForMonit = new ConnectionMonitReady();
        mBabyInfoForMonit = new ConnectionMonitBabyInfo();
        mCompletedMonit = new ConnectionMonitCompleted();
        mHowToAttachMonit = new ConnectionMonitHowToAttachSensor();
        mPutSensorToHub = new ConnectionHubPutSensor();
        mSelectApHub = new ConnectionHubSelectAP();
        mInputPasswordHub = new ConnectionHubInputApPassword();
        mAddNewNetworkHub = new ConnectionHubAddNewNetwork();
        mCompletedHub = new ConnectionHubCompleted();
        mSelectApSecurityTypeHub = new ConnectionHubSelectApSecurity();
        mReadyForLamp = new ConnectionLampReady();
        mSelectApLamp = new ConnectionLampSelectAP();
        mInputPasswordLamp = new ConnectionLampInputApPassword();
        mAddNewNetworkLamp = new ConnectionLampAddNewNetwork();
        mCompletedLamp = new ConnectionLampCompleted();
        mSelectApSecurityTypeLamp = new ConnectionLampSelectApSecurity();
        mPrepareMonitPackageDiaperSensor = new ConnectionMonitPackagePrepareDiaperSensor();
        mPrepareMonitPackageHub = new ConnectionMonitPackagePrepareHub();
        mPutMonitPackageDiaperSensorToHub = new ConnectionMonitPackagePutSensor();
        mSelectApMonitPackageHub = new ConnectionMonitPackageHubSelectAP();
        mInputPasswordMonitPackageHub = new ConnectionMonitPackageHubInputApPassword();
        mAddNewNetworkMonitPackageHub = new ConnectionMonitPackageHubAddNewNetwork();
        mSelectApSecurityTypeMonitPackageHub = new ConnectionMonitPackageHubSelectApSecurity();
        mCompletedMonitPackageInfo = new ConnectionMonitPackageInfo();
        mCompletedMonitPackageHowToAttachSensor = new ConnectionMonitPackageHowToAttachSensor();

        if (mDlgHubApConnectionProcessing == null) {
            mDlgHubApConnectionProcessing = new ProgressHorizontalDialog(
                    ConnectionActivity.this,
                    getString(R.string.dialog_contents_hub_ap_connecting),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            mHandler.removeMessages(MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS);
                            mDlgHubApConnectionProcessing.dismiss();
                        }
                    });
        }

        if (mDlgHubApConnectionFailed == null) {
            if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                mDlgHubApConnectionFailed = new SimpleDialog(
                        ConnectionActivity.this,
                        "[Code204]",
                        getString(R.string.dialog_contents_hub_ap_connection_failed),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDlgHubApConnectionFailed.dismiss();
                            }
                        });
                mDlgHubApConnectionFailed.showHelpButton(true);
                mDlgHubApConnectionFailed.setContentsGravity(Gravity.CENTER);
                mDlgHubApConnectionFailed.setHelpButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ConnectionActivity)mContext).showHelpContents(11, 26);
                    }
                });
                mDlgHubApConnectionFailed.setContentsGravity(Gravity.LEFT);
            } else {
                mDlgHubApConnectionFailed = new SimpleDialog(
                        ConnectionActivity.this,
                        getString(R.string.dialog_contents_hub_ap_connection_failed),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDlgHubApConnectionFailed.dismiss();
                            }
                        });
                mDlgHubApConnectionFailed.setContentsGravity(Gravity.CENTER);
            }
        }

        if (mDlgSensorFirmwareUpdate == null) {
            mDlgSensorFirmwareUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgSensorFirmwareUpdate.dismiss();
                                mDlgConnectHub.show();
                            } catch (Exception e) {

                            }
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgSensorFirmwareUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            mPreferenceMgr.setNeedHubRegistrationDialog(true);
                            Intent intent = new Intent(ConnectionActivity.this, FirmwareUpdateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("targetDeviceId", mConnectingDiaperSensor.deviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgHubFirmwareUpdate == null) {
            mDlgHubFirmwareUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgHubFirmwareUpdate.dismiss();
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgHubFirmwareUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            Intent intent = new Intent(ConnectionActivity.this, HubFirmwareUpdateActivity.class);
                            intent.putExtra("targetDeviceId", mConnectedHubDeviceId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgLampFirmwareUpdate == null) {
            mDlgLampFirmwareUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgLampFirmwareUpdate.dismiss();
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgLampFirmwareUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            Intent intent = new Intent(ConnectionActivity.this, LampFirmwareUpdateActivity.class);
                            intent.putExtra("targetDeviceId", mConnectedHubDeviceId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgPackageSecurityPatchUpdate == null) {
            mDlgPackageSecurityPatchUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update_force),
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgPackageSecurityPatchUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            Intent intent = new Intent(ConnectionActivity.this, PackageFirmwareUpdateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("targetSensorDeviceId", mConnectingDiaperSensor.deviceId);
                            intent.putExtra("targetHubDeviceId", mConnectedHubDeviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgPackageLatestUpdate == null) {
            mDlgPackageLatestUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgPackageLatestUpdate.dismiss();
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgPackageLatestUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            Intent intent = new Intent(ConnectionActivity.this, PackageFirmwareUpdateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("targetSensorDeviceId", mConnectingDiaperSensor.deviceId);
                            intent.putExtra("targetHubDeviceId", mConnectedHubDeviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgSensorSecurityPatchUpdate == null) {
            mDlgSensorSecurityPatchUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update_force),
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgSensorSecurityPatchUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            mPreferenceMgr.setNeedHubRegistrationDialog(true);
                            Intent intent = new Intent(ConnectionActivity.this, FirmwareUpdateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("targetDeviceId", mConnectingDiaperSensor.deviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgHubSecurityPatchUpdate == null) {
            mDlgHubSecurityPatchUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update_force),
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgHubSecurityPatchUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            Intent intent = new Intent(ConnectionActivity.this, HubFirmwareUpdateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("targetHubDeviceId", mConnectedHubDeviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgHubFirmwareUpdate == null) {
            mDlgHubFirmwareUpdate = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.contents_need_firmware_update),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgHubFirmwareUpdate.dismiss();
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                mDlgHubFirmwareUpdate.dismiss();
                            } catch (Exception e) {

                            }
                            Intent intent = new Intent(ConnectionActivity.this, HubFirmwareUpdateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("targetHubDeviceId", mConnectedHubDeviceId);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
        }

        if (mDlgConnectHub == null) {
            mDlgConnectHub = new SimpleDialog(
                    ConnectionActivity.this,
                    getString(R.string.dialog_contents_ask_for_connectint_hub),
                    getString(R.string.btn_no),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgConnectHub.dismiss();
                            finish();
                        }
                    },
                    getString(R.string.btn_yes),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgConnectHub.dismiss();
                            showFragment(STEP_HUB_READY_FOR_CONNECTING);
                        }
                    });
        }
    }

    public void allowGuestManualConnection(boolean allow) {
        if (mGuestDeviceInfo == null) {
            if (DBG) Log.e(TAG, "allowGuestManualConnection mGuestDeviceInfo NULL");
            return;
        }
        if (allow) {
            mServerQueryMgr.requestBecomeCloudMember(mGuestDeviceInfo.cloudId, new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errCode, String data) {
                    if (InternetErrorCode.SUCCEEDED.equals(errCode) ||
                            InternetErrorCode.ERR_GROUP_MEMBER_ALREADY_EXISTED.equals(errCode)) {
                        mConnectionMgr.allowGuestManualConnection(true, mGuestDeviceInfo);
                    } else if (InternetErrorCode.ERR_GROUP_MEMBER_COUNT_EXCEEDED.equals(errCode)) {
                        showToast(mContext.getString(R.string.toast_invite_group_member_exceeded));
                        mConnectionMgr.allowGuestManualConnection(false, mGuestDeviceInfo);
                    } else {
                        showToast(mContext.getString(R.string.toast_invite_group_member_failed));
                        mConnectionMgr.allowGuestManualConnection(false, mGuestDeviceInfo);
                    }
                }
            });
        } else {
            mConnectionMgr.allowGuestManualConnection(false, mGuestDeviceInfo);
        }
    }

    public void allowGuestHubConnection(long cloudId) {
        mServerQueryMgr.requestBecomeCloudMember(cloudId, new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (InternetErrorCode.SUCCEEDED.equals(errCode) ||
                        InternetErrorCode.ERR_GROUP_MEMBER_ALREADY_EXISTED.equals(errCode)) {
                    showFragment(ConnectionActivity.STEP_HUB_SELECT_AP);
                } else if (InternetErrorCode.ERR_GROUP_MEMBER_COUNT_EXCEEDED.equals(errCode)) {
                    showToast(mContext.getString(R.string.toast_invite_group_member_exceeded));
                } else {
                    showToast(mContext.getString(R.string.toast_invite_group_member_failed));
                }
            }
        });
    }

    public DeviceAQMHub getConnectedHubDevice() {
        return ConnectionManager.getDeviceAQMHub(mConnectedHubDeviceId);
    }

    public DeviceLamp getConnectedLampDevice() {
        return ConnectionManager.getDeviceLamp(mConnectedLampInfo.deviceId);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SHOW_FRAGMENT:
                    int contentIdx = msg.arg1;
                    _showFragment(contentIdx);
                    break;
                case ConnectionManager.MSG_SCAN_FINISHED:
                    int foundDeviceCount = msg.arg1;
                    int discoveringTimeMs = msg.arg2;
                    ArrayList<BluetoothDevice> foundList = (ArrayList<BluetoothDevice>)msg.obj;
                    if (DBG) Log.d(TAG, "ConnectionManager.MSG_SCAN_FINISHED : " + foundDeviceCount + " / " + discoveringTimeMs);
                    if (mFragmentHandler != null) {
                        mFragmentHandler.obtainMessage(ConnectionManager.MSG_SCAN_FINISHED, foundDeviceCount, discoveringTimeMs, foundList).sendToTarget();
                    }
                    break;
                case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
                    int state = msg.arg1;
                    DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
                    if (deviceInfo != null) {
                        if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + deviceInfo.btmacAddress + "] " + state);
                        if (mFragmentHandler != null) {
                            mFragmentHandler.obtainMessage(ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE, state, -1, deviceInfo).sendToTarget();
                        }
                    }
                    break;
                case ConnectionManager.MSG_HUB_WIFI_SCAN_LIST:
                    HubApInfo apInfo = (HubApInfo)msg.obj;
                    apInfo.isConnected = false;
                    apInfo.currentSelected = false;

                    if (mHubDeviceId != -1) {
                        if (DBG) Log.d(TAG, "Connected Hub deviceId : " + mHubDeviceId);
                        DeviceAQMHub hubDevice = ConnectionManager.getDeviceAQMHub(mHubDeviceId);
                        if (hubDevice != null) {
                            if (DBG) Log.d(TAG, "Connected Hub Status : " + hubDevice.getApName() + " / " + apInfo.name + " / " + hubDevice.getConnectionState());
                            if (hubDevice.getApName() != null && hubDevice.getApName().equals(apInfo.name)) {
                                apInfo.currentSelected = true;
                            }

                            if (hubDevice.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
                                apInfo.isConnected = true;
                            } else {
                                apInfo.isConnected = false;
                            }
                        }
                    }

                    if (mFragmentHandler != null) {
                        mFragmentHandler.obtainMessage(ConnectionManager.MSG_HUB_WIFI_SCAN_LIST, apInfo).sendToTarget();
                    }
                    break;
                case ConnectionManager.MSG_BLE_MANUALLY_CONNECTED:
                    int state2 = msg.arg1;
                    DeviceInfo deviceInfo2 = (DeviceInfo)msg.obj;

                    if (deviceInfo2.type == DeviceType.DIAPER_SENSOR) {
                        mConnectingDiaperSensor = ConnectionManager.mRegisteredDiaperSensorList.get(deviceInfo2.deviceId);

                        if (DBG) Log.d(TAG, "MSG_BLE_MANUALLY_CONNECTED(SENSOR) : [" + deviceInfo2.deviceId + "] " + state2 + " / " + deviceInfo2.btmacAddress);
                        if (mFragmentHandler != null) {
                            mFragmentHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUALLY_CONNECTED, state2, -1, deviceInfo2).sendToTarget();
                        }

                        if (mCurrentShowingContentIndex == STEP_MONIT_PACKAGE_PUT_SENSOR_TO_HUB) {
                            // Fragment에서 허브 연결함
                        } else {
                            setDiaperSensorCloudIdToServer();
                            if (mCurrentShowingContentIndex == STEP_MONIT_READY_FOR_CONNECTING) {
                                if (mGuestDeviceInfo == null) {
                                    showFragment(ConnectionActivity.STEP_MONIT_BABY_INFO);
                                } else {
                                    showFragment(ConnectionActivity.STEP_MONIT_COMPLETED);
                                }
                            }
                            mGuestDeviceInfo = null;
                        }
                    } else if (deviceInfo2.type == DeviceType.LAMP) {
                        mConnectingLamp = ConnectionManager.mRegisteredLampList.get(deviceInfo2.deviceId);
                        mConnectedLampInfo = deviceInfo2;
                        if (DBG) Log.d(TAG, "MSG_BLE_MANUALLY_CONNECTED(LAMP) : [" + deviceInfo2.deviceId + "] " + state2 + " / " + deviceInfo2.btmacAddress);
                        if (mFragmentHandler != null) {
                            mFragmentHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUALLY_CONNECTED, state2, -1, deviceInfo2).sendToTarget();
                        }

                        if (mCurrentShowingContentIndex == STEP_LAMP_READY_FOR_CONNECTING) {
                            if (mGuestDeviceInfo == null) {
                                showFragment(ConnectionActivity.STEP_LAMP_SELECT_AP);
                            } else {
                                showFragment(ConnectionActivity.STEP_LAMP_COMPLETED);
                            }
                        }
                        mGuestDeviceInfo = null;
                    } else if (deviceInfo2.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
                        mConnectingElderlyDiaperSensor = ConnectionManager.mRegisteredElderlyDiaperSensorList.get(deviceInfo2.deviceId);

                        if (DBG) Log.d(TAG, "MSG_BLE_MANUALLY_CONNECTED(SENSOR) : [" + deviceInfo2.deviceId + "] " + state2 + " / " + deviceInfo2.btmacAddress);
                        if (mFragmentHandler != null) {
                            mFragmentHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUALLY_CONNECTED, state2, -1, deviceInfo2).sendToTarget();
                        }

                        if (mCurrentShowingContentIndex == STEP_MONIT_PACKAGE_PUT_SENSOR_TO_HUB) {
                            // Fragment에서 허브 연결함
                        } else {
                            setElderlyDiaperSensorCloudIdToServer();
                            if (mCurrentShowingContentIndex == STEP_ELDERLY_SENSOR_READY_FOR_CONNECTING) {
                                if (mGuestDeviceInfo == null) {
                                    showFragment(ConnectionActivity.STEP_ELDERLY_SENSOR_INFO);
                                } else {
                                    showFragment(ConnectionActivity.STEP_ELDERLY_SENSOR_COMPLETED);
                                }
                            }
                            mGuestDeviceInfo = null;
                        }
                    }
                    break;
                case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST:
                    mGuestDeviceInfo = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_BLE_MANUAL_CONNECTION_GUEST : " + mGuestDeviceInfo.toString());
                    if (mFragmentHandler != null) {
                        mFragmentHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST, mGuestDeviceInfo).sendToTarget();
                    }
                    break;

                case ConnectionManager.MSG_HUB_CONNECTED_WITH_SENSOR:
                    DeviceInfo sensorDeviceInfoConnectedWithHub = (DeviceInfo)msg.obj;
                    if (sensorDeviceInfoConnectedWithHub == null) break;

                    DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensorDeviceInfoConnectedWithHub.deviceId, sensorDeviceInfoConnectedWithHub.type);
                    if (bleConnection == null) break;
                    mConnectedHubInfo = bleConnection.getHubDeviceInfo();
                    if (mConnectedHubInfo == null) break;

                    long connectedHubDeviceId = mConnectedHubInfo.deviceId;

                    if (DBG) Log.d(TAG, "MSG_HUB_CONNECTED_WITH_SENSOR : " + sensorDeviceInfoConnectedWithHub.toString() + " / " + connectedHubDeviceId + " / " + mConnectedHubInfo);

                    if ((ConnectionManager.mRegisteredAQMHubList.get(connectedHubDeviceId) == null) // 신규등록
                            || (mHubDeviceId == connectedHubDeviceId)) { // AP변경
                        mConnectingSensorForHubInfo = sensorDeviceInfoConnectedWithHub;
                        mConnectedHubDeviceId = connectedHubDeviceId;
                        if (DBG) Log.d(TAG, "register it");

                        if (mFragmentHandler != null) {
                            mFragmentHandler.obtainMessage(ConnectionManager.MSG_HUB_CONNECTED_WITH_SENSOR, mConnectingSensorForHubInfo).sendToTarget();
                        }
                    } else if (ConnectionManager.mRegisteredAQMHubList.get(connectedHubDeviceId) != null) { // 내 Device인 경우 AP변경
                        mConnectingSensorForHubInfo = sensorDeviceInfoConnectedWithHub;
                        mConnectedHubDeviceId = connectedHubDeviceId;
                        if (DBG) Log.e(TAG, "already registered, but owner");

                        if (mFragmentHandler != null) {
                            mFragmentHandler.obtainMessage(ConnectionManager.MSG_HUB_CONNECTED_WITH_SENSOR, mConnectingSensorForHubInfo).sendToTarget();
                        }
                    } else { // 이미 등록되어있는 경우
                        if (DBG) Log.e(TAG, "already registered");
                    }
                    break;

                case ConnectionManager.MSG_SENSOR_BABY_INFO_UPDATED:
                    // Todo
                    break;

                case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT:
                    int state3 = msg.arg1;
                    int reason = msg.arg2;
                    if (mFragmentHandler != null) {
                        mFragmentHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT, state3, reason).sendToTarget();
                    }
                    mGuestDeviceInfo = null;
                    break;

                case MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS:
                    removeMessages(MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS);
                    if (mHubApConnectionWaitSeconds <= TIME_HUB_AP_CONNECTION_WAIT_SEC) {
                        if (mDlgHubApConnectionProcessing != null && mDlgHubApConnectionProcessing.isShowing()) {
                            mDlgHubApConnectionProcessing.setProgress((mHubApConnectionWaitSeconds * 100) / TIME_HUB_AP_CONNECTION_WAIT_SEC);
                        }
                        mHubApConnectionWaitSeconds++;
                        this.sendEmptyMessageDelayed(MSG_REFRESH_HUB_AP_CONNECTION_PROGRESS, 1000);
                    } else {
                        if (ConnectionActivity.this.isFinishing() || ConnectionActivity.this.isDestroyed()) {
                            break;
                        }
                        if (mDlgHubApConnectionProcessing != null && mDlgHubApConnectionProcessing.isShowing()) {
                            mDlgHubApConnectionProcessing.dismiss();
                        }
                        if (mDlgHubApConnectionFailed != null) {
                            try {
                                mDlgHubApConnectionFailed.show();
                            } catch (Exception e) {

                            }
                        }
                    }
                    break;

                case ConnectionManager.MSG_HUB_WIFI_CONNECTION_STATE_CHANGE:
                    int apConnectionStatus = msg.arg1;
                    if (DBG) Log.d(TAG, "MSG_HUB_WIFI_CONNECTION_STATE_CHANGE : " + apConnectionStatus);

                    // 허브 연결 완료시, UserInfo 수신 후 Completed 페이지로 넘어감
                    if (apConnectionStatus == 1) {
                        // 허브 인터넷 연결 완료 후, 정보입력 페이지로 넘어가기 전에 setHubCloudIdToServer 진행
                        isWaitingForSettingDeviceCloudId = true;
                        switch (mCurrentShowingContentIndex) {
                            case STEP_LAMP_SELECT_AP:
                            case STEP_LAMP_INPUT_PASSWORD:
                            case STEP_LAMP_ADD_NETWORK:
                            case STEP_LAMP_SELECT_SECURITY:
                                setLampCloudIdToServer();
                                break;
                            case STEP_HUB_SELECT_AP:
                            case STEP_HUB_INPUT_PASSWORD:
                            case STEP_HUB_ADD_NETWORK:
                            case STEP_HUB_SELECT_SECURITY:
                                setHubCloudIdToServer();
                                break;
                            case STEP_MONIT_PACKAGE_HUB_SELECT_AP:
                            case STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD:
                            case STEP_MONIT_PACKAGE_HUB_ADD_NETWORK:
                            case STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY:
                                if (DBG) Log.d(TAG, "set cloud id for sensor/hub");
                                setDiaperSensorCloudIdToServer();
                                setHubCloudIdToServer();
                                break;
                            default:
                                break;
                        }

                    }
                    break;

                case ConnectionManager.MSG_SET_CLOUD_ID_TO_CLOUD:
                    int deviceType = msg.arg1;
                    if (DBG) Log.d(TAG, "MSG_SET_CLOUD_ID_TO_CLOUD : " + deviceType + " / " + isWaitingForSettingDeviceCloudId);
                    if (isWaitingForSettingDeviceCloudId) {
                        if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB || deviceType == DeviceType.LAMP) {
                            isWaitingForHubCompletedPage = true;
                            mConnectionMgr.getUserInfoFromCloud();
                            mConnectionMgr.updateDeviceFullStatusFromCloud(null);
                        }
                    }
                    break;

                case ConnectionManager.MSG_SET_USER_INFO_DATA_FROM_CLOUD:
                    if (DBG) Log.d(TAG, "MSG_SET_USER_INFO_DATA_FROM_CLOUD : " + isWaitingForHubCompletedPage);
                    if (isWaitingForHubCompletedPage) {
                        isWaitingForHubCompletedPage = false;
                        // UserInfo 수신 후 Completed 페이지로 넘어감
                        switch (mCurrentShowingContentIndex) {
                            case STEP_LAMP_SELECT_AP:
                            case STEP_LAMP_INPUT_PASSWORD:
                            case STEP_LAMP_ADD_NETWORK:
                            case STEP_LAMP_SELECT_SECURITY:
                                showFragment(STEP_LAMP_COMPLETED);
                                break;
                            case STEP_HUB_SELECT_AP:
                            case STEP_HUB_INPUT_PASSWORD:
                            case STEP_HUB_ADD_NETWORK:
                            case STEP_HUB_SELECT_SECURITY:
                                showFragment(STEP_HUB_COMPLETED);
                                break;
                            case STEP_MONIT_PACKAGE_HUB_SELECT_AP:
                            case STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD:
                            case STEP_MONIT_PACKAGE_HUB_ADD_NETWORK:
                            case STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY:
                                showFragment(STEP_MONIT_PACKAGE_INFO);
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (DBG) Log.e(TAG, "not waiting for hub completed page");
                    }
                    break;
            }
        }
    };

    private void _next() {
        finish();
    }

    public void showHelpContents(int boardType, int boardId) {
        Intent intent = new Intent(ConnectionActivity.this, NoticeActivity.class);
        intent.putExtra("boardType", boardType);
        intent.putExtra("boardId", boardId);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void _initSensorAlarmStatus(final int deviceType, final long deviceId, final int notiType, final boolean enabled) {
        mServerQueryMgr.setDeviceAlarmStatus(deviceType, deviceId, notiType, enabled,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            if (DBG) Log.d(TAG, "Set alarm status succeeded: " + deviceType+  " / " + deviceId + " / " + notiType + " / " + enabled);
                            mPreferenceMgr.setDeviceAlarmEnabled(
                                    deviceType,
                                    deviceId,
                                    notiType,
                                    enabled);
                        } else {
                            if (DBG) Log.d(TAG, "Set alarm status failed");
                        }
                    }
                });
    }

    private void _initSensorAlarmStatus() {
        if (Configuration.BETA_TEST_MODE) {
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.DEVICE_ALL, true);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.DIAPER_SOILED, false);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, false);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.FART_DETECTED, false);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.MOVEMENT_DETECTED, false);
        } else {
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.DEVICE_ALL, true);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.DIAPER_SOILED, false);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, true);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
            _initSensorAlarmStatus(mConnectingDiaperSensor.type, mConnectingDiaperSensor.deviceId, NotificationType.MOVEMENT_DETECTED, false);
        }
    }

    private void _initElderlySensorAlarmStatus() {
        if (Configuration.BETA_TEST_MODE) {
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.DEVICE_ALL, true);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.PEE_DETECTED, true);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.POO_DETECTED, true);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.FART_DETECTED, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.MOVEMENT_DETECTED, false);
        } else {
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.DEVICE_ALL, true);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, true);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.PEE_DETECTED, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.POO_DETECTED, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.FART_DETECTED, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
            _initSensorAlarmStatus(mConnectingElderlyDiaperSensor.type, mConnectingElderlyDiaperSensor.deviceId, NotificationType.MOVEMENT_DETECTED, false);
        }

    }
}
