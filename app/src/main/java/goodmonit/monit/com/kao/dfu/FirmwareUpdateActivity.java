package goodmonit.monit.com.kao.dfu;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.net.HttpURLConnection;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.BaseActivity;
import goodmonit.monit.com.kao.activity.GuideDirectConnectionActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerDfuManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.services.ConnectionManager;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class FirmwareUpdateActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "DFU";
    private static final boolean DBG = Configuration.DBG;

    private static final int REQCODE_BLE_DIRECT_CONNECTION  = 1;

    private static final int MSG_DO_OTA_UPDATE              = 1;
    private static final int STEP_READY                     = 0;
    private static final int STEP_DOWNLOAD_DFU_FILE         = 1;
    private static final int STEP_ENTER_DFU_MODE            = 2;
    private static final int STEP_START_DFU                 = 3;
    private static final int STEP_UPLOAD_FILE               = 4;
    private static final int STEP_COMPLETE_DFU              = 5;

    public static final int MSG_FIRMWARE_DOWNLOAD_PROGRESS  = 100;
    public static final int MSG_UPDATE_CANCELLED            = 101;

    public static final int ERR_CODE_DFU_ABORTED            = 190;
    public static final int ERR_CODE_DFU_FILE_DOWNLOAD_URL  = 191;
    public static final int ERR_CODE_DFU_FILE_DOWNLOAD      = 192;
    private static int mDfuStep = STEP_READY;

    private long mConnectedSensorDeviceId;
    private DeviceBLEConnection mMonitSensor;

    private String mTargetDeviceName;
    private String mTargetMacAddress;
    private String mFileName = ServerDfuManager.FIRMWARE_FILE_NAME;
    private int mOTAMode;

    private SimpleDialog mDlgUpdateCompleted, mDlgUpdateFailed, mDlgStay, mDlgLowBattery;

    /** related to DFU */
    private static final String DATA_DEVICE_NAME = "MONIT_BL";
    private static final String DATA_FW_FILE_NAME = "fw_file_name";
    private static final String DATA_ADV_NAME = "device_adv_name";
    private static final String DATA_MAC_ADDR = "device_mac_addr";
    private static final String DATA_STATUS = "status";
    private static final String DATA_DFU_COMPLETED = "dfu_completed";
    private static final String DATA_DFU_ERROR = "dfu_error";
    private static final String DATA_DFU_STEP = "dfu_step";

    private TextView tvStatus;
    private TextView tvUploadStatus;
    private Button btnUpdate;
    private Button btnUpdateLowerVersion, btnUpdateHigherVersion, btnUpdateOTATestVersion, btnUpdate3rdParty;
    private TextView tvCurrentVersion, tvLatestVersion;
    private RelativeLayout rctnLatestVersion;
    private RelativeLayout rctnUpdateAvailable;
    private RelativeLayout rctnOnUpdate;
    private TextView tvUpdateDescription;
    private ImageView ivLogo;

    private BluetoothDevice mSelectedDevice;
    //private String mFilePath;
    //private Uri mFileStreamUri;
    //private String mInitFilePath;
    //private Uri mInitFileStreamUri;
    //private int mFileType;
    //private int mFileTypeTmp; // This value is being used when user is selecting a file not to overwrite the old value (in case he/she will cancel selecting file)
    private boolean mStatusOk;
    /** Flag set to true in {@link #onRestart()} and to false in {@link #onPause()}. */
    private boolean mResumed;
    /** Flag set to true if DFU operation was completed while {@link #mResumed} was false. */
    private boolean mDfuCompleted;
    /** The error message received from DFU service while {@link #mResumed} was false. */
    private String mDfuError;
    private boolean mEnterDfuMode;
    private boolean mDfuCancelled;
    private int mLatestErrorCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        _setToolBar();

        mContext = this;
        mConnectedSensorDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(mContext);
        mServerQueryMgr = ServerQueryManager.getInstance(mContext);
        mEnterDfuMode = false;
        mMonitSensor = ConnectionManager.getDeviceBLEConnection(mConnectedSensorDeviceId, DeviceType.DIAPER_SENSOR);
        mOTAMode = 0;

        if (mMonitSensor == null) {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedSensorDeviceId);
            finish();
            return;
        }
        setTargetDeviceInformation(mMonitSensor.getDeviceInfo().btmacAddress);

        _initView();

        if (isDfuServiceRunning()) {
            mStatusOk = true;
            _showProgressBar();
        }
        // restore saved state
        //mFileType = DfuService.TYPE_AUTO; // Default
        if (savedInstanceState != null) {
            mTargetDeviceName = savedInstanceState.getString(DATA_ADV_NAME);
            mTargetMacAddress = savedInstanceState.getString(DATA_MAC_ADDR);
            mStatusOk = mStatusOk || savedInstanceState.getBoolean(DATA_STATUS);
            //btnUpdate.setEnabled(mSelectedDevice != null && mStatusOk);
            mDfuCompleted = savedInstanceState.getBoolean(DATA_DFU_COMPLETED);
            mDfuError = savedInstanceState.getString(DATA_DFU_ERROR);
            mFileName = savedInstanceState.getString(DATA_FW_FILE_NAME);
            mDfuStep = savedInstanceState.getInt(DATA_DFU_STEP);
        }

        if (DBG) Log.d(TAG, "targetDevice : " + mTargetDeviceName + " / " + mTargetMacAddress);
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);

        if (mMonitSensor.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) {
            Intent intent = new Intent(FirmwareUpdateActivity.this, GuideDirectConnectionActivity.class);
            intent.putExtra("targetDeviceId", mConnectedSensorDeviceId);
            startActivityForResult(intent, REQCODE_BLE_DIRECT_CONNECTION);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DBG) Log.i(TAG, "onActivityResult : " + requestCode + " / " + mMonitSensor.getConnectionState());
        switch (requestCode) {
            case REQCODE_BLE_DIRECT_CONNECTION:
                if (mMonitSensor.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) {
                    finish();
                }
                break;
        }
    }

    private void checkUpdateAvailable() {
        if (DBG) Log.d(TAG, "checkUpdateAvailable");
        VersionManager versionMgr = new VersionManager(mContext);
        tvCurrentVersion.setText(getString(R.string.current_version) + " : " + mMonitSensor.getDeviceInfo().firmwareVersion);
        tvLatestVersion.setText(getString(R.string.latest_version) + " : " + mPreferenceMgr.getDiaperSensorVersion());
        if (versionMgr.checkUpdateAvailable(mMonitSensor.getDeviceInfo().firmwareVersion, mPreferenceMgr.getDiaperSensorVersion()) || Configuration.MASTER || Configuration.DEVELOPER) {
            rctnLatestVersion.setVisibility(View.GONE);
            rctnUpdateAvailable.setVisibility(View.VISIBLE);
            rctnOnUpdate.setVisibility(View.GONE);
            tvUpdateDescription.setVisibility(View.VISIBLE);

            if (versionMgr.checkUpdateAvailable(mMonitSensor.getDeviceInfo().firmwareVersion, mPreferenceMgr.getDiaperSensorForceVersion())) {
                // If current diaper sensor version is lower than hub security patched version, show another description
                tvUpdateDescription.setText(getString(R.string.dfu_update_available_description_force));
            } else {
                tvUpdateDescription.setText(getString(R.string.dfu_update_available_description));
            }
        } else {
            rctnLatestVersion.setVisibility(View.VISIBLE);
            rctnUpdateAvailable.setVisibility(View.GONE);
        }
    }

    private boolean setTargetDeviceInformation(String prevMacAddress) {
        if (prevMacAddress == null) return false;
        String mac[] = prevMacAddress.split(":");
        if (mac == null || mac.length < 6) return false;

        int lastMac = Integer.parseInt(mac[5], 16) + 1;
        mac[5] = Integer.toHexString(lastMac).toUpperCase();
        if (mac[5].length() == 1) {
            mac[5] = "0" + mac[5];
        }

        mTargetMacAddress = mac[0] + ":" + mac[1] + ":" + mac[2] + ":" + mac[3] + ":" + mac[4] + ":" + mac[5];
        mTargetDeviceName = DATA_DEVICE_NAME + "(" + mac[5] + mac[4] + mac[3] + ")";
        return true;
    }

    private void _initUpdate(Intent intent) {
        if (DBG) Log.i(TAG, "_initUpdate");

        final String name = mTargetDeviceName;
        final String address = mTargetMacAddress;
        final String overwrittenName = intent.getStringExtra(DfuService.EXTRA_DEVICE_NAME);
        final String path = intent.getStringExtra(DfuService.EXTRA_FILE_PATH);
        final String initPath = intent.getStringExtra(DfuService.EXTRA_INIT_FILE_PATH);
        final String finalName = overwrittenName == null ? ((name != null) ? name : "Not available") : overwrittenName;
        final int type = intent.getIntExtra(DfuService.EXTRA_FILE_TYPE, DfuService.TYPE_AUTO);
        final boolean keepBond = intent.getBooleanExtra(DfuService.EXTRA_KEEP_BOND, false);
        final boolean forceDfu = false;
        final boolean enablePRNs = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        final String value = String.valueOf(DfuServiceInitiator.DEFAULT_PRN_VALUE);
        int numberOfPackets;
        try {
            numberOfPackets = Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            numberOfPackets = DfuServiceInitiator.DEFAULT_PRN_VALUE;
        }
        String filePath = mContext.getFilesDir().getPath() + "/" + mFileName;

        // Start DFU service with data provided in the intent
        /*
        final Intent service = new Intent(this, DfuService.class);
        service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, address);
        service.putExtra(DfuService.EXTRA_DEVICE_NAME, finalName);
        String filePath = mContext.getFilesDir().getPath() + "/" + mFileName;
        //String filePath = Environment.getExternalStorageDirectory().getPath() + "/monit/" + mFileName;
        service.putExtra(DfuService.EXTRA_FILE_PATH, filePath);
        service.putExtra(DfuService.EXTRA_FILE_TYPE, type);
        //service.putExtra(DfuService.EXTRA_FILE_RES_ID, mFwResId);
        service.putExtra(DfuService.EXTRA_KEEP_BOND, keepBond);
        service.putExtra(DfuService.EXTRA_UNSAFE_EXPERIMENTAL_BUTTONLESS_DFU, true);
        if (DBG) Log.i(TAG, "startService");
        startService(service);
        */

        final DfuServiceInitiator starter = new DfuServiceInitiator(address)
                .setDeviceName(finalName)
                .setKeepBond(keepBond)
                .setForceDfu(forceDfu)
                .setPacketsReceiptNotificationsEnabled(enablePRNs)
                .setPacketsReceiptNotificationsValue(numberOfPackets)
                //.setPrepareDataObjectDelay(400)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);

        starter.setZip(null, filePath);
        starter.setScope(DfuServiceInitiator.SCOPE_APPLICATION | DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS);

        starter.start(this, DfuService.class);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DATA_ADV_NAME, mTargetDeviceName);
        outState.putString(DATA_MAC_ADDR, mTargetMacAddress);
        outState.putString(DATA_DFU_ERROR, mDfuError);
        outState.putBoolean(DATA_STATUS, mStatusOk);
        outState.putBoolean(DATA_DFU_COMPLETED, mDfuCompleted);
        outState.putString(DATA_FW_FILE_NAME, mFileName);
        outState.putInt(DATA_DFU_STEP, mDfuStep);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG, "onDestroy");
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume(" + mDfuStep + ")");
        mResumed = true;
        checkUpdateAvailable();
        if (mDfuCompleted) {
            onTransferCompleted();
        }

        if (mDfuError != null) {
            showErrorMessage(mDfuError);
        }

        if (mDfuCompleted || mDfuError != null) {
            // if this activity is still open and upload process was completed, cancel the notification
            final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(DfuService.NOTIFICATION_ID);
            mDfuCompleted = false;
            mDfuError = null;
        }

        if (mMonitSensor.getConnectionState() == DeviceConnectionState.DISCONNECTED) {
            if (mDfuStep == STEP_READY) {
                finish();
            } else {
                _showProgressBar();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG, "onPause");
        mResumed = false;
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getString(R.string.title_firmware_update));

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
        if (btnUpdate != null && btnUpdate.isSelected()) {
            // 업데이트 중에만 Stay 다이얼로그 띄움
            if (mDlgStay != null && !mDlgStay.isShowing()) {
                try {
                    mDlgStay.show();
                } catch (Exception e) {

                }
            }
        } else {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
        }
    }

    private void _initView() {
        ivLogo = (ImageView)findViewById(R.id.iv_firmware_update_logo);
        if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
            ivLogo.setImageResource(R.drawable.logo_kchuggies);
        } else {
            ivLogo.setImageResource(R.drawable.ic_logo_green);
        }

        btnUpdateLowerVersion = (Button)findViewById(R.id.btn_firmware_update_lower_version);
        btnUpdateHigherVersion = (Button)findViewById(R.id.btn_firmware_update_higher_version);
        btnUpdateOTATestVersion = (Button)findViewById(R.id.btn_firmware_update_ota_test_version);
        btnUpdate3rdParty = (Button)findViewById(R.id.btn_firmware_update_3rd_party_test_version);
        btnUpdateLowerVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOTAMode = 1;
                _doOTAUpdate();
            }
        });
        btnUpdateHigherVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOTAMode = 2;
                _doOTAUpdate();
            }
        });
        btnUpdateOTATestVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOTAMode = 32;
                _doOTAUpdate();
            }
        });
        btnUpdate3rdParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOTAMode = 128;
                _doOTAUpdate();
            }
        });

        if (Configuration.DEVELOPER) {
            btnUpdateOTATestVersion.setVisibility(View.VISIBLE);
        } else {
            btnUpdateOTATestVersion.setVisibility(View.GONE);
        }

        if (Configuration.DEVELOPER_3RD_PARTY) {
            btnUpdate3rdParty.setVisibility(View.VISIBLE);
        } else {
            btnUpdate3rdParty.setVisibility(View.GONE);
        }

        if (Configuration.MASTER) {
            btnUpdateLowerVersion.setVisibility(View.VISIBLE);
            btnUpdateHigherVersion.setVisibility(View.VISIBLE);
            btnUpdateOTATestVersion.setVisibility(View.VISIBLE);
            btnUpdate3rdParty.setVisibility(View.VISIBLE);
        }

        btnUpdate = (Button) findViewById(R.id.btn_firmware_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnUpdate.isSelected()) { // cancel
                    _cancelOTAUpdate();
                } else {
                    int batteryPower = mConnectionMgr.mRegisteredDiaperSensorList.get(mConnectedSensorDeviceId).getBatteryPower();
                    if (DBG) Log.d(TAG, "battery power : " + batteryPower);
                    if (batteryPower < 30) {
                        if (mDlgLowBattery != null && !mDlgLowBattery.isShowing()) {
                            try {
                                mDlgLowBattery.show();
                            } catch (Exception e) {

                            }
                        }
                    } else {
                        mOTAMode = 0;
                        _doOTAUpdate();
                    }
                }
            }
        });

        tvCurrentVersion = (TextView) findViewById(R.id.tv_firmware_update_current_version);
        tvLatestVersion = (TextView) findViewById(R.id.tv_firmware_update_latest_version);

        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        tvStatus = (TextView) findViewById(R.id.tv_firmware_update_status);
        tvUploadStatus = (TextView) findViewById(R.id.tv_firmware_upload_status);
        rctnLatestVersion = (RelativeLayout) findViewById(R.id.rctn_firmware_update_latest_version);
        rctnUpdateAvailable = (RelativeLayout) findViewById(R.id.rctn_firmware_update_available_version);
        rctnOnUpdate = (RelativeLayout) findViewById(R.id.rctn_firmware_update_on_update);

        rctnLatestVersion.setVisibility(View.VISIBLE);
        tvUpdateDescription = (TextView) findViewById(R.id.tv_firmware_update_description);
        rctnUpdateAvailable.setVisibility(View.GONE);
        rctnOnUpdate.setVisibility(View.GONE);

        mDlgUpdateCompleted = new SimpleDialog(mContext,
                getString(R.string.dfu_update_completed),
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgUpdateCompleted.dismiss();
                        finish();
                    }
                }
        );

        mDlgUpdateFailed = new SimpleDialog(mContext,
                getString(R.string.dfu_update_failed),
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgUpdateFailed.dismiss();
                        finish();
                    }
                }
        );

        mDlgStay = new SimpleDialog(mContext,
                getString(R.string.dialog_dfu_stay_this_screen),
                getString(R.string.btn_no),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgStay.dismiss();
                    }
                },
                getString(R.string.btn_yes),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgStay.dismiss();
                        finish();
                        overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                    }
                });

        mDlgLowBattery = new SimpleDialog(mContext,
                getString(R.string.dialog_contents_dfu_low_battery),
                getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgLowBattery.dismiss();
                    }
                });
    }

    private void _cancelOTAUpdate() {
        mDfuCancelled = true;
        tvStatus.setText(getString(R.string.dfu_status_aborting));
        tvUploadStatus.setText("");
        btnUpdate.setText(getString(R.string.dfu_status_aborting));
        btnUpdate.setClickable(false);
        btnUpdate.setSelected(false);
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
        manager.sendBroadcast(pauseAction);
        if (DBG) Log.d(TAG, "Cancel Update");
    }

    private void _enterDfuMode() {
        tvStatus.setText(R.string.dfu_status_switching_to_dfu);
        tvUploadStatus.setText("");
        mEnterDfuMode = true;
        mMonitSensor.enterDFUMode();
    }

    private void _downloadFirmwareFile() {
        tvStatus.setText(R.string.dfu_status_downloading);
        tvUploadStatus.setText("");
//
//        final FileDownloadManager fileDownloader = new FileDownloadManager(mContext);
//        fileDownloader.setDownloadListener(new FileDownloadManager.FileDownloadListener() {
//            @Override
//            public void onSucceeded(String filename) {
//                if (DBG) Log.d(TAG, "onSucceeded : " + filename);
//                if (mDfuCancelled) {
//                    onUploadCanceled(ERR_CODE_DFU_ABORTED);
//                } else {
//                    mDfuStep = STEP_ENTER_DFU_MODE;
//                    mHandler.sendEmptyMessage(MSG_DO_OTA_UPDATE);
//                }
//            }
//            @Override
//            public void onFailed(String filename) {
//                onUploadCanceled(ERR_CODE_DFU_FILE_DOWNLOAD);
//                if (DBG) Log.d(TAG, "onFailed : " + filename);
//            }
//        });
//        fileDownloader.setProgressHandler(mHandler);
//
//        mServerQueryMgr.getSensorFW(
//                mMonitSensor.getDeviceInfo().deviceId,
//                mMonitSensor.getDeviceInfo().getEnc(),
//                mOTAMode,
//                new ServerManager.ServerResponseListener() {
//                    @Override
//                    public void onReceive(int responseCode, String errCode, String data) {
//                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
//                            String downloadUrl = ServerManager.getJArrayFromJSONObj(data, mServerQueryMgr.getParameter(67));
//                            mFileName = ServerManager.getJArrayFromJSONObj(data, mServerQueryMgr.getParameter(68));
//                            if (DBG) Log.d(TAG, "filename : " + mFileName);
//                            if (DBG) Log.d(TAG, "url : " + downloadUrl);
//                            fileDownloader.setDownloadFileName(mFileName);
//                            fileDownloader.setDownloadUrl(downloadUrl);
//                            fileDownloader.execute();
//                        } else {
//                            onUploadCanceled(ERR_CODE_DFU_FILE_DOWNLOAD_URL);
//                        }
//                    }
//                });

        mServerQueryMgr.getSensorFW2(
                mMonitSensor.getDeviceInfo().deviceId,
                mMonitSensor.getDeviceInfo().getEnc(),
                mOTAMode,
                new ServerDfuManager.ServerDfuResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode) {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                if (mDfuCancelled) {
                                    onUploadCanceled(ERR_CODE_DFU_ABORTED);
                                } else {
                                    mDfuStep = STEP_ENTER_DFU_MODE;
                                    mHandler.sendEmptyMessage(MSG_DO_OTA_UPDATE);
                                }
                            } else {
                                onUploadCanceled(ERR_CODE_DFU_FILE_DOWNLOAD);
                            }
                        } else {
                            onUploadCanceled(ERR_CODE_DFU_ABORTED);
                        }
                    }

                    @Override
                    public void onProgress(int percentage, int totalBytes) {
                        mHandler.obtainMessage(MSG_FIRMWARE_DOWNLOAD_PROGRESS, percentage, totalBytes).sendToTarget();
                    }
                });

    }

    private void _doOTAUpdate() {
        _showProgressBar();
        btnUpdate.setSelected(true);
        mDfuCancelled = false;
        mDfuStep = STEP_DOWNLOAD_DFU_FILE;
        mHandler.sendEmptyMessage(MSG_DO_OTA_UPDATE);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_DO_OTA_UPDATE:
                    switch (mDfuStep) {
                        case STEP_DOWNLOAD_DFU_FILE:
                            _downloadFirmwareFile();
                            break;
                        case STEP_ENTER_DFU_MODE:
                            _enterDfuMode();
                            break;
                        case STEP_START_DFU:
                            _showProgressBar();
                            final Intent intent = getIntent();
                            _initUpdate(intent);
                            break;
                    }

                    break;
                case MSG_UPDATE_CANCELLED:
                    int errorCode = msg.arg1;
                    clearUI(false);
                    if (mDlgUpdateFailed != null && !mDlgUpdateFailed.isShowing()) {
                        mDlgUpdateFailed.setContents(getString(R.string.dfu_update_failed) + "\n(" + mDfuStep + " / " +  errorCode + ")");
                        try {
                            mDlgUpdateFailed.show();
                        } catch (Exception e) {

                        }
                    }
                    mDfuStep = STEP_READY;
                    //showToast(getString(R.string.dfu_status_aborted));
                    break;
                case MSG_FIRMWARE_DOWNLOAD_PROGRESS:
                    int percentage = msg.arg1;
                    int total = msg.arg2;
                    tvUploadStatus.setText("(" + percentage + "%)");
                    if (DBG) Log.d(TAG, "progress : " + percentage + " / " + total);
                    break;
                case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
                    final int state = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "] " + state + " / " + mDfuStep);

                    if (deviceInfo.deviceId == mMonitSensor.getDeviceInfo().deviceId) {
                        if (state == DeviceConnectionState.DISCONNECTED) {
                            if (mDfuCancelled) {
                                onUploadCanceled(ERR_CODE_DFU_ABORTED);
                            } else {
                                if (mEnterDfuMode) {
                                    mEnterDfuMode = false;
                                    mDfuStep = STEP_START_DFU;
                                    mHandler.sendEmptyMessage(MSG_DO_OTA_UPDATE);
                                } else {
                                    // DFU모드 진입도 못하고 연결이 끊어진 상태. 업데이트를 할 수 없습니다.
                                }
                            }
                        } else {
                            // 펌웨어 업데이트 완료 후 센서 자동 재부팅이 되고 연결 되면, 펌웨어 업데이트 실패했다고 뜨고 있음
                            if (DBG) Log.d(TAG, "step: " + mDfuStep + " / errCode: " + mLatestErrorCode);
                            if (mDfuStep != STEP_READY && mDfuStep != STEP_COMPLETE_DFU) {
                                onUploadCanceled(mLatestErrorCode);
                            }
                        }
                    }
                    break;
            }
        }
    };

    private void _showProgressBar() {
        rctnLatestVersion.setVisibility(View.GONE);
        tvUpdateDescription.setVisibility(View.GONE);
        rctnUpdateAvailable.setVisibility(View.VISIBLE);
        rctnOnUpdate.setVisibility(View.VISIBLE);

        tvStatus.setVisibility(View.VISIBLE);
        tvUploadStatus.setVisibility(View.VISIBLE);
        btnUpdate.setText(R.string.btn_cancel);
    }

    private void onTransferCompleted() {
        clearUI(false);
        mMonitSensor.getDeviceInfo().firmwareVersion = mPreferenceMgr.getDiaperSensorVersion();
        if (mDlgUpdateCompleted != null && !mDlgUpdateCompleted.isShowing()) {
            try {
                mDlgUpdateCompleted.show();
            } catch (Exception e) {

            }
        }
        showToast(getString(R.string.dfu_status_completed));
    }

    public void onUploadCanceled(int errorCode) {
        mHandler.obtainMessage(MSG_UPDATE_CANCELLED, errorCode, 1).sendToTarget();
    }

    private void showErrorMessage(final String message) {
        clearUI(false);
        showToast("Upload failed: " + message);
    }

    private void clearUI(final boolean clearDevice) {
        checkUpdateAvailable();
        btnUpdate.setClickable(true);
        btnUpdate.setText(R.string.btn_update);
        if (clearDevice) {
            mSelectedDevice = null;
        }
        // Application may have lost the right to these files if Activity was closed during upload (grant uri permission). Clear file related values.
        mStatusOk = false;
    }

    private boolean isDfuServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DfuService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The progress listener receives events from the DFU Service.
     * If is registered in onCreate() and unregistered in onDestroy() so methods here may also be called
     * when the screen is locked or the app went to the background. This is because the UI needs to have the
     * correct information after user comes back to the activity and this information can't be read from the service
     * as it might have been killed already (DFU completed or finished with error).
     */
    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onDeviceConnecting : " + deviceAddress);
            tvStatus.setText(getString(R.string.dfu_status_connecting));
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onDfuProcessStarting : " + deviceAddress);
            mDfuStep = STEP_START_DFU;
            tvStatus.setText(R.string.dfu_status_starting);
            tvUploadStatus.setText("");
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onEnablingDfuMode : " + deviceAddress);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onFirmwareValidating : " + deviceAddress);
            tvUploadStatus.setText("");
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onDeviceDisconnecting : " + deviceAddress);
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onDfuCompleted : " + deviceAddress);
            mDfuStep = STEP_COMPLETE_DFU;
            tvStatus.setText(R.string.dfu_status_completed);
            tvUploadStatus.setText("");
            if (mResumed) {
                // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onTransferCompleted();

                        // if this activity is still open and upload process was completed, cancel the notification
                        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 200);
            } else {
                // Save that the DFU process has finished
                mDfuCompleted = true;
            }
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            if (DBG) Log.d(TAG, "onDfuAborted : " + deviceAddress);
            tvStatus.setText(getString(R.string.dfu_status_aborted));
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onUploadCanceled(ERR_CODE_DFU_ABORTED);

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            if (DBG) Log.d(TAG, "onProgressChanged : " + deviceAddress + " / " + percent + " / " + speed + " / " + avgSpeed + " / " + currentPart + " / " + partsTotal);
            mDfuStep = STEP_UPLOAD_FILE;
            tvStatus.setText(R.string.dfu_status_uploading);
            tvUploadStatus.setText("(" + percent + "%)");
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            if (DBG) Log.d(TAG, "onError : " + deviceAddress + " / " + error + " / " + errorType + " / " + message + " / " + (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED));
            mConnectionMgr.sendFwUpdateFailed(DeviceType.DIAPER_SENSOR, mMonitSensor.getDeviceInfo().deviceId, error + " / " + errorType + " / " + message);
            mLatestErrorCode = error;
            if (mResumed) {
                //if (DBG) showErrorMessage(message);
                // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if this activity is still open and upload process was completed, cancel the notification
                        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 200);
            }

            if (mDfuCancelled || (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED)) {
                mDfuError = message;
                onUploadCanceled(mLatestErrorCode);
            } else {
                if (DBG) Log.d(TAG, "current step : " + mDfuStep);
                mHandler.sendEmptyMessage(MSG_DO_OTA_UPDATE);
            }
        }
    };
}
