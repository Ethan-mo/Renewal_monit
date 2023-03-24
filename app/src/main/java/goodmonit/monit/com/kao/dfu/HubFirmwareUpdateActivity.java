package goodmonit.monit.com.kao.dfu;

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

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.BaseActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class HubFirmwareUpdateActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "DFU";
    private static final boolean DBG = Configuration.DBG;

    private final int MSG_OTA_UPDATE_PROGRESS       = 1;
    private final int MSG_OTA_UPDATE_COMPLETED      = 2;
    private int mOtaUpdateProgress = 0;

    private SimpleDialog mDlgUpdateCompleted, mDlgUpdateFailed, mDlgStay;
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

    private long mConnectedDeviceId;
    private DeviceAQMHub mHubDevice;
    private int mOTAMode;
    private String prevFirmwareVersion;
    private boolean isOTAUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        _setToolBar();

        mContext = this;
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(mContext);
        mServerQueryMgr = ServerQueryManager.getInstance(mContext);

        _setAQMHub();

        _initView();
    }

    private void _setAQMHub() {
        mConnectedDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        mHubDevice = ConnectionManager.getDeviceAQMHub(mConnectedDeviceId);
        if (mHubDevice != null) {
            if (DBG) Log.d(TAG, "targetDevice : [" + mHubDevice.deviceId + "] " + mHubDevice.name);
        } else {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedDeviceId);
            finish();
            return;
        }
    }

    private void checkUpdateAvailable() {
        if (DBG) Log.d(TAG, "checkUpdateAvailable");
        VersionManager versionMgr = new VersionManager(mContext);
        tvCurrentVersion.setText(getString(R.string.current_version) + " : " + mHubDevice.getDeviceInfo().firmwareVersion);
        tvLatestVersion.setText(getString(R.string.latest_version) + " : " + mPreferenceMgr.getHubVersion());
        if (versionMgr.checkUpdateAvailable(mHubDevice.getDeviceInfo().firmwareVersion, mPreferenceMgr.getHubVersion()) || Configuration.MASTER) {
            rctnLatestVersion.setVisibility(View.GONE);
            rctnUpdateAvailable.setVisibility(View.VISIBLE);
            rctnOnUpdate.setVisibility(View.GONE);
            tvUpdateDescription.setVisibility(View.VISIBLE);
            if (versionMgr.checkUpdateAvailable(mHubDevice.getDeviceInfo().firmwareVersion, mPreferenceMgr.getHubForceVersion())) {
                // If current hub version is lower than hub security patched version, show another description
                tvUpdateDescription.setText(getString(R.string.dfu_update_available_description_force));
            } else {
                tvUpdateDescription.setText(getString(R.string.dfu_update_available_description));
            }
        } else {
            rctnLatestVersion.setVisibility(View.VISIBLE);
            rctnUpdateAvailable.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG, "onDestroy");
        mHandler.removeMessages(MSG_OTA_UPDATE_PROGRESS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");

        checkUpdateAvailable();

        if (isOTAUpdating) {
            _showProgressBar();
        } else {
            if ((mHubDevice.getConnectionState() == DeviceConnectionState.DISCONNECTED)) {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG, "onPause");
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
        if (btnUpdate != null && (btnUpdate.isSelected() || !btnUpdate.isEnabled())) {
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

                } else {
                    mOTAMode = 0;
                    _doOTAUpdate();
                    btnUpdate.setSelected(!btnUpdate.isSelected());
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
                getString(R.string.dfu_status_completed_msg),
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
                getString(R.string.dfu_status_aborted_msg),
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
    }



    private void _doOTAUpdate() {
        isOTAUpdating = true;
        mServerQueryMgr.OTAUpdateDevice(
                DeviceType.AIR_QUALITY_MONITORING_HUB,
                mHubDevice.deviceId,
                mHubDevice.getEnc(),
                mOTAMode,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            if (DBG) Log.d(TAG, "OTA Update Start");
                            mOtaUpdateProgress = 0;
                            prevFirmwareVersion = mHubDevice.firmwareVersion;
                            mHandler.sendEmptyMessage(MSG_OTA_UPDATE_PROGRESS);
                        } else {
                            if (DBG) Log.d(TAG, "OTA Update Failed");
                        }
                    }
                });

        _showProgressBar();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_OTA_UPDATE_COMPLETED:
                    removeMessages(MSG_OTA_UPDATE_COMPLETED);
                    mOtaUpdateProgress = mOtaUpdateProgress + 3;
                    if ((int)(mOtaUpdateProgress / 3.3) > 100) {
                        btnUpdate.setEnabled(true);
                        btnUpdate.setText(R.string.btn_update);
                        tvStatus.setText(getString(R.string.dfu_status_completed_msg));
                        tvUploadStatus.setText("(100%)");
                        if (mDlgUpdateCompleted != null && !mDlgUpdateCompleted.isShowing()) {
                            try {
                                mDlgUpdateCompleted.show();
                            } catch (Exception e) {

                            }
                        }
                    } else {
                        tvUploadStatus.setText("(" + (int)(mOtaUpdateProgress / 3.3) + "%)");
                        mHandler.sendEmptyMessageDelayed(MSG_OTA_UPDATE_COMPLETED, 100);
                    }
                    break;
                case MSG_OTA_UPDATE_PROGRESS:
                    removeMessages(MSG_OTA_UPDATE_PROGRESS);
                    if (DBG) Log.d(TAG, "MSG_OTA_UPDATE_PROGRESS : " + mOtaUpdateProgress + "/300 -> " + (mOtaUpdateProgress / 3) + "% / " + prevFirmwareVersion + " / " + mHubDevice.firmwareVersion);
                    mOtaUpdateProgress++;
                    if (mOtaUpdateProgress < 10) {
                        tvStatus.setText(getString(R.string.dfu_update_waiting));
                        tvUploadStatus.setText("");
                        mHandler.sendEmptyMessageDelayed(MSG_OTA_UPDATE_PROGRESS, 1000);
                    } else if (mOtaUpdateProgress < 330) {
                        tvStatus.setText(getString(R.string.dfu_status_uploading));
                        tvUploadStatus.setText("(" + (int)(mOtaUpdateProgress / 3.3) + "%)");
                        if (mOtaUpdateProgress % 10 == 0) {
                            mConnectionMgr.getUserInfoFromCloud(); // 10초마다 허브 펌웨어 버전가져오기
                        }
                        //if (new VersionManager(mContext).checkUpdateAvailable(prevFirmwareVersion, mHubDevice.firmwareVersion)) {
                        if (prevFirmwareVersion != null && !prevFirmwareVersion.equals(mHubDevice.firmwareVersion)) {
                            if (DBG) Log.d(TAG, "Update Succeeded : " + prevFirmwareVersion + " -> " + mHubDevice.firmwareVersion);
                            mHandler.sendEmptyMessage(MSG_OTA_UPDATE_COMPLETED);
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_OTA_UPDATE_PROGRESS, 1000);
                        }

                    } else {
                        if (DBG) Log.e(TAG, "Update Failed : " );
                        mOtaUpdateProgress = 0;
                        if (mDlgUpdateFailed != null && !mDlgUpdateFailed.isShowing()) {
                            try {
                                mDlgUpdateFailed.show();
                            } catch (Exception e) {

                            }
                        }
                    }
                    break;
                case ConnectionManager.MSG_WIFI_CONNECTION_STATE_CHANGE:
                    removeMessages(MSG_OTA_UPDATE_PROGRESS);
                    final int wifiConnectionState = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
                    if (DBG) Log.d(TAG, "MSG_WIFI_CONNECTION_STATE_CHANGE : [" + deviceInfo.type + " / " + deviceInfo.deviceId + "/" + mConnectedDeviceId + "] " + wifiConnectionState + " / " + deviceInfo.firmwareVersion);
                    if (deviceInfo.type == DeviceType.AIR_QUALITY_MONITORING_HUB && mConnectedDeviceId == deviceInfo.deviceId) {
                        if (wifiConnectionState == DeviceConnectionState.WIFI_CONNECTED) {
                            //if ((new VersionManager(mContext)).checkUpdateAvailable(prevFirmwareVersion, deviceInfo.firmwareVersion)) {
                            if (prevFirmwareVersion != null && !prevFirmwareVersion.equals(deviceInfo.firmwareVersion)) {
                                btnUpdate.setEnabled(true);
                                btnUpdate.setText(R.string.btn_update);
                                // 업데이트 성공
                                if (DBG) Log.d(TAG, "Update Succeeded : " + prevFirmwareVersion + " -> " + deviceInfo.firmwareVersion);
                                tvStatus.setText(getString(R.string.dfu_status_completed_msg));
                                tvUploadStatus.setText("(100%)");
                                if (mDlgUpdateCompleted != null && !mDlgUpdateCompleted.isShowing()) {
                                    try {
                                        mDlgUpdateCompleted.show();
                                    } catch (Exception e) {

                                    }
                                }
                            } else {
                                btnUpdate.setEnabled(true);
                                btnUpdate.setText(R.string.btn_update);
                                // 업데이트 실패
                                if (DBG) Log.e(TAG, "Update Failed : " + prevFirmwareVersion + " -> " + deviceInfo.firmwareVersion);
                                if (mDlgUpdateFailed != null && !mDlgUpdateFailed.isShowing()) {
                                    try {
                                        mDlgUpdateFailed.show();
                                    } catch (Exception e) {

                                    }
                                }
                            }
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_OTA_UPDATE_PROGRESS, 1000);
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
        tvUploadStatus.setText("");
        btnUpdate.setEnabled(false);
        btnUpdate.setText(R.string.dfu_status_uploading);
    }

    private void _clearUI() {
        btnUpdate.setEnabled(true);
        btnUpdate.setSelected(false);
        btnUpdate.setText(R.string.btn_update);
    }
}
