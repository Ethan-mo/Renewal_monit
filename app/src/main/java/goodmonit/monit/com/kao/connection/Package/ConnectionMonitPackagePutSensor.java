package goodmonit.monit.com.kao.connection.Package;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.UserInfo.Group;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.ProgressHorizontalDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class ConnectionMonitPackagePutSensor extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "PkgConn";
	private static final boolean DBG = Configuration.DBG;

	private static final int STEP1_CHECK_SENSOR		= 1;
	private static final int STEP2_PUT_SENSOR		= 2;

	private static final int MSG_CHANGE_ANIMATION	= 1;
	private static final int MSG_REFRESH_PROGRESS 	= 2;
	private static final int MSG_CHECK_HUB_CONNECTION_VIA_SENSOR 	= 3;

	private static final long CHANGE_ANIMATION_INTERVAL_MS = 1000;
	private int mAnimationIndex;
	private int mStep = 0;
	private boolean mManualConnected = false;
	private DeviceInfo mManuallyConnectedDevice = null;
	private boolean mManualConnectCancelled = false;

	private Button btnConnect, btnHelp;
	private ProgressHorizontalDialog mDlgProcessing;
	private SimpleDialog mDlgInitializeSensor, mDlgInitializeHub;

	private SimpleDialog mDlgDiaperSensorNotDetected;
	private SimpleDialog mDlgDiaperSensorConnectionFailed;
	private SimpleDialog mDlgInternetNotConnected;
	private SimpleDialog mDlgDiaperSensorAlreadyRegistered;

	private SimpleDialog mDlgHubAlreadyRegistered;
	private SimpleDialog mDlgHubNotDetected;

	private int scanSeconds;
	private boolean findHub = false;

	private ImageView ivAnimation;
	private TextView tvDetail;
	private DeviceInfo mGuestDeviceInfo;
    private DeviceInfo mGuestHubDeviceInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_monit_package_put_sensor_to_hub, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mConnectionMgr = ConnectionManager.getInstance();
		mScreenInfo = new ScreenInfo(601);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mDatabaseMgr = DatabaseManager.getInstance(mContext);
        _initView(view);
		setView(STEP2_PUT_SENSOR);

		int available = ConnectionManager.checkBluetoothStatus();
		if (available == ConnectionManager.STATE_DISABLED || available == ConnectionManager.STATE_UNAVAILABLE) {
			Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(btEnableIntent, ConnectionActivity.REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN);
		}
        return view;
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DBG) Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);
		switch (requestCode) {
			case ConnectionActivity.REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN:
				if (resultCode == Activity.RESULT_OK) {
				} else {
					((ConnectionActivity)mContext).showToast(getString(R.string.toast_need_to_enable_bluetooth));
				}
				break;
		}
	}

	private void _initView(View v) {
		btnConnect = (Button)v.findViewById(R.id.btn_connection_monit_package_put_sensor_to_hub_start_connect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ConnectionManager.checkBluetoothStatus() == ConnectionManager.STATE_DISABLED) {
					Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(btEnableIntent, ConnectionActivity.REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN);
				} else {
					if (ServerManager.isInternetConnected()) {
						_startConnect();
					} else {
						if (mDlgInternetNotConnected != null) {
							try {
								mDlgInternetNotConnected.show();
							} catch (Exception e) {

							}
						}
					}
				}
			}
		});

		btnHelp = (Button)v.findViewById(R.id.btn_connection_monit_package_put_sensor_to_hub_help);
		btnHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 20);
			}
		});
		ivAnimation = (ImageView)v.findViewById(R.id.iv_connection_monit_package_put_sensor_to_hub_animation);
		tvDetail = (TextView)v.findViewById(R.id.tv_connection_monit_package_put_sensor_to_hub_detail);

		if (mDlgProcessing == null) {
			mDlgProcessing = new ProgressHorizontalDialog(
					mContext,
					getString(R.string.dialog_contents_scanning),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							if (mConnectionMgr.isDiscovering()) {
								if (DBG) Log.d(TAG, "Processing canceled");
								mConnectionMgr.manualCancelDiscovery();
							}
							mManualConnectCancelled = true;
							mDlgProcessing.dismiss();
						}
					});
		}


		if (mDlgDiaperSensorNotDetected == null) {
			mDlgDiaperSensorNotDetected = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_DIAPER_SENSOR_NOT_FOUND + "]",
					getString(R.string.dialog_contents_not_detected_monit),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgDiaperSensorNotDetected.dismiss();
						}
					},
					getString(R.string.btn_try_again),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgDiaperSensorNotDetected.dismiss();
							_startConnect();
						}
					});
			mDlgDiaperSensorNotDetected.setContentsGravity(Gravity.LEFT);
		}
		mDlgDiaperSensorNotDetected.showHelpButton(true);
		mDlgDiaperSensorNotDetected.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(10, 25);
			}
		});

		if (mDlgDiaperSensorConnectionFailed == null) {
			mDlgDiaperSensorConnectionFailed = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_DIAPER_SENSOR_NOT_CONNECTED + "]",
					getString(R.string.dialog_contents_failed_connection),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgDiaperSensorConnectionFailed.dismiss();
						}
					},
					getString(R.string.btn_try_again),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgDiaperSensorConnectionFailed.dismiss();
							_startConnect();
						}
					});
			mDlgDiaperSensorConnectionFailed.setContentsGravity(Gravity.LEFT);
		}
		mDlgDiaperSensorConnectionFailed.showHelpButton(true);
		mDlgDiaperSensorConnectionFailed.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(10, 28);
			}
		});

		if (mDlgInternetNotConnected == null) {
			mDlgInternetNotConnected = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_DIAPER_SENSOR_NOT_CONNECTED + "]",
					getString(R.string.dialog_contents_need_internet_connection),
					getString(R.string.btn_ok),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgInternetNotConnected.dismiss();
						}
					});
			mDlgInternetNotConnected.setContentsGravity(Gravity.LEFT);
		}
		mDlgInternetNotConnected.showHelpButton(true);
		mDlgInternetNotConnected.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(10, 28);
			}
		});

		if (mDlgDiaperSensorAlreadyRegistered == null) {
			mDlgDiaperSensorAlreadyRegistered = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_DIAPER_SENSOR_ALREADY_REGISTERED + "]",
					getString(R.string.dialog_contents_sensor_already_registered) + mPreferenceMgr.getShortId(),
					getString(R.string.btn_device_initialize),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgDiaperSensorAlreadyRegistered.dismiss();
							if (mDlgInitializeSensor != null && !mDlgInitializeSensor.isShowing()) {
								try {
									mDlgInitializeSensor.show();
								} catch (Exception e) {

								}
							}
							((ConnectionActivity)mMainActivity).allowGuestManualConnection(false);
						}
					},
					getString(R.string.btn_close),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mGuestDeviceInfo = null;
							mDlgDiaperSensorAlreadyRegistered.dismiss();
							if (mDlgDiaperSensorConnectionFailed != null && !mDlgDiaperSensorConnectionFailed.isShowing()) {
								try {
									mDlgDiaperSensorConnectionFailed.show();
								} catch (Exception e) {

								}
							}
							((ConnectionActivity)mMainActivity).allowGuestManualConnection(false);
						}
					});

			mDlgDiaperSensorAlreadyRegistered.setButtonColor(
					getResources().getColor(R.color.colorTextWarning),
					getResources().getColor(R.color.colorTextPrimary));

			mDlgDiaperSensorAlreadyRegistered.setContentsGravity(Gravity.LEFT);
		}
		mDlgDiaperSensorAlreadyRegistered.showHelpButton(true);
		mDlgDiaperSensorAlreadyRegistered.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(10, 24);
			}
		});

		if (mDlgHubNotDetected == null) {
			mDlgHubNotDetected = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_HUB_NOT_FOUND + "]",
					getString(R.string.dialog_contents_not_detected_hub),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgHubNotDetected.dismiss();
						}
					},
					getString(R.string.btn_try_again),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgHubNotDetected.dismiss();
							_startConnect();
						}
					});
			mDlgHubNotDetected.setContentsGravity(Gravity.LEFT);
		}
		mDlgHubNotDetected.showHelpButton(true);
		mDlgHubNotDetected.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 19);
			}
		});

		if (mDlgHubAlreadyRegistered == null) {
			mDlgHubAlreadyRegistered = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_HUB_ALREADY_REGISTERED + "]",
					getString(R.string.dialog_contents_hub_already_registered) + mPreferenceMgr.getShortId(),
					getString(R.string.btn_device_initialize),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgHubAlreadyRegistered.dismiss();
							if (mDlgInitializeHub != null && !mDlgInitializeHub.isShowing()) {
								try {
									mDlgInitializeHub.show();
								} catch (Exception e) {

								}
							}
							((ConnectionActivity)mMainActivity).allowGuestManualConnection(false);
						}
					},
					getString(R.string.btn_close),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mGuestDeviceInfo = null;
							mDlgHubAlreadyRegistered.dismiss();
							if (mDlgDiaperSensorConnectionFailed != null && !mDlgDiaperSensorConnectionFailed.isShowing()) {
								try {
									mDlgDiaperSensorConnectionFailed.show();
								} catch (Exception e) {

								}
							}
							((ConnectionActivity)mMainActivity).allowGuestManualConnection(false);
						}
					});

			mDlgHubAlreadyRegistered.setButtonColor(
					getResources().getColor(R.color.colorTextWarning),
					getResources().getColor(R.color.colorTextPrimary));

			mDlgHubAlreadyRegistered.setContentsGravity(Gravity.LEFT);
		}
		mDlgHubAlreadyRegistered.showHelpButton(true);
		mDlgHubAlreadyRegistered.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 27);
			}
		});


		if (mDlgInitializeSensor == null) {
			mDlgInitializeSensor = new SimpleDialog(mContext,
					getString(R.string.dialog_contents_sensor_initialize_with_serialnumber),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgInitializeSensor.dismiss();
						}
					},
					getString(R.string.btn_device_initialize),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String msg = mDlgInitializeSensor.getInputText();
							if (msg.length() > 1) {
								mServerQueryMgr.checkSerialNumber(
										mGuestDeviceInfo.type,
										mGuestDeviceInfo.deviceId,
										msg,
										new ServerManager.ServerResponseListener() {
											@Override
											public void onReceive(int responseCode, String errCode, String data) {
												if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
													mServerQueryMgr.initDevice(
															mGuestDeviceInfo.type,
															mGuestDeviceInfo.deviceId,
															mGuestDeviceInfo.getEnc(),
															new ServerManager.ServerResponseListener() {
																@Override
																public void onReceive(int responseCode, String errCode, String data) {
																	if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
																		((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_sensor_initialize_succeeded));
																		mGuestDeviceInfo = null;
																		mDlgInitializeSensor.dismiss();
																	} else {
																		((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_sensor_initialize_failed));
																	}
																}
															});
												} else {
													((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_sensor_initialize_wrong_serialnumber));
												}
											}
										});
							}
						}
					});
			mDlgInitializeSensor.setContentsGravity(Gravity.LEFT);
			mDlgInitializeSensor.setInputMode(true);
			mDlgInitializeSensor.setButtonColor(
					getResources().getColor(R.color.colorTextPrimary),
					getResources().getColor(R.color.colorTextWarning));
		}

		if (mDlgInitializeHub == null) {
			mDlgInitializeHub = new SimpleDialog(mContext,
					getString(R.string.dialog_contents_hub_initialize_with_serialnumber),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgInitializeHub.dismiss();
						}
					},
					getString(R.string.btn_device_initialize),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String msg = mDlgInitializeHub.getInputText();
							if (msg.length() > 1) {
								mServerQueryMgr.checkSerialNumber(
                                        mGuestHubDeviceInfo.type,
                                        mGuestHubDeviceInfo.deviceId,
										msg,
										new ServerManager.ServerResponseListener() {
											@Override
											public void onReceive(int responseCode, String errCode, String data) {
												if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
													mServerQueryMgr.initDevice(
                                                            mGuestHubDeviceInfo.type,
                                                            mGuestHubDeviceInfo.deviceId,
                                                            mGuestHubDeviceInfo.getEnc(),
															new ServerManager.ServerResponseListener() {
																@Override
																public void onReceive(int responseCode, String errCode, String data) {
																	if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
																		((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_hub_initialize_succeeded));
                                                                        mGuestHubDeviceInfo = null;
																		mDlgInitializeHub.dismiss();
																	} else {
																		((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_hub_initialize_failed));
																	}
																}
															});
												} else {
													((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_hub_initialize_wrong_serialnumber));
												}
											}
										});
							}
						}
					});
			mDlgInitializeHub.setContentsGravity(Gravity.LEFT);
			mDlgInitializeHub.setInputMode(true);
			mDlgInitializeHub.setButtonColor(
					getResources().getColor(R.color.colorTextPrimary),
					getResources().getColor(R.color.colorTextWarning));
		}
    }

    private void setView(int step) {
		mStep = step;
		mAnimationIndex = 0;
		switch (step) {
			case STEP1_CHECK_SENSOR:

				break;
			case STEP2_PUT_SENSOR:
				tvDetail.setText(getString(R.string.connection_package_put_sensor_detail));
				btnConnect.setText(getString(R.string.connection_start_connect));
				mHandler.sendEmptyMessage(MSG_CHANGE_ANIMATION);
				break;
		}
	}

	private void _startConnect() {
		mManualConnected = false;
		mManuallyConnectedDevice = null;
		mManualConnectCancelled = false;
		if (mDlgProcessing != null) {
			try {
				mDlgProcessing.setContents(getString(R.string.dialog_contents_scanning));
				mDlgProcessing.show();
			} catch (Exception e) {

			}
		}
		scanSeconds = 0;
		mHandler.sendEmptyMessage(MSG_REFRESH_PROGRESS);
		if (mConnectionMgr == null) {
			mConnectionMgr = ConnectionManager.getInstance();
		}
		mConnectionMgr.manualConnectDiaperSensor();
	}

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_PROGRESS:
					removeMessages(MSG_REFRESH_PROGRESS);
					if (DBG) Log.e(TAG, "MSG_REFRESH_PROGRESS: " + scanSeconds + " / " + findHub);
					if (scanSeconds < (int)(ConnectionManager.TIME_BLE_MANUAL_SCAN_TIME_OUT_SEC + ConnectionManager.TIME_BLE_MANUAL_CONNECTION_TIME_OUT_SEC)) {
						if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
							mDlgProcessing.setProgress((int)((100.0 / (ConnectionManager.TIME_BLE_MANUAL_SCAN_TIME_OUT_SEC + ConnectionManager.TIME_BLE_MANUAL_CONNECTION_TIME_OUT_SEC - 1)) * scanSeconds));
						}
						if (findHub) {
							if (scanSeconds % 3 == 0) {
								((ConnectionActivity) mMainActivity).sendCheckHubStatus();
							}
						}
						scanSeconds++;
						this.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS, 1000);
					} else {
						findHub = false;
						if (mManuallyConnectedDevice != null) {
							DeviceBLEConnection bleConnection = mConnectionMgr.getDeviceBLEConnection(mManuallyConnectedDevice.deviceId, DeviceType.DIAPER_SENSOR);
							if (bleConnection != null) {
								ConnectionManager.mRegisteredDiaperSensorList.remove(mManuallyConnectedDevice.deviceId);
								bleConnection.unregister();
								ConnectionManager.removeDeviceBLEConnection(mManuallyConnectedDevice.deviceId, mManuallyConnectedDevice.type);
								mManuallyConnectedDevice = null;
							} else {
								if (DBG) Log.e(TAG, "bleConnection NULL");
							}
						}

						if (mManualConnected == true) { // 센서가 연결되었을 때,
							if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
								mDlgProcessing.dismiss();
							}
							if (mDlgHubNotDetected != null && !mDlgHubNotDetected.isShowing()) {
								mDlgHubNotDetected.show();
							}
						} else {
							if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
								mDlgProcessing.dismiss();
							}
							if (mDlgDiaperSensorConnectionFailed != null && !mDlgDiaperSensorConnectionFailed.isShowing()) {
								mDlgDiaperSensorConnectionFailed.show();
							}
						}
					}
					break;

				case MSG_CHECK_HUB_CONNECTION_VIA_SENSOR:
					removeMessages(MSG_CHECK_HUB_CONNECTION_VIA_SENSOR);
                    if (DBG) Log.d(TAG, "MSG_CHECK_HUB_CONNECTION_VIA_SENSOR");
					if (scanSeconds % 3 == 0) {
						((ConnectionActivity) mMainActivity).sendCheckHubStatus();
					}
					this.sendEmptyMessageDelayed(MSG_CHECK_HUB_CONNECTION_VIA_SENSOR, 1000);
					break;

				case ConnectionManager.MSG_SCAN_FINISHED:
					int foundDeviceCount = msg.arg1;
					if (DBG) Log.d(TAG, "ConnectionManager.MSG_SCAN_FINISHED : " + foundDeviceCount);

					if (foundDeviceCount == 0) {
						// 만약, 이미 연결된 센서가 있다면 확인이 필요함
						boolean foundBleConnectedSensor = false;
						for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
							if (sensor == null) continue;
							if (sensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED && sensor.getOperationStatus() >= DeviceStatus.OPERATION_HUB_NO_CHARGE) {
								foundBleConnectedSensor = true;
								break;
							}
						}

						if (foundBleConnectedSensor == false) {
							if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
								mDlgProcessing.dismiss();
							}
							if (!mManualConnected && !mManualConnectCancelled) {
								try {
									mDlgDiaperSensorNotDetected.show();
								} catch (Exception e) {

								}
								removeMessages(MSG_REFRESH_PROGRESS);
							} else {

							}
							break;
						} else {
							findHub = true;
						}
					}

					mDlgProcessing.setContents(getString(R.string.dialog_contents_connecting));
					break;

				case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
					int state = msg.arg1;
					DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
					break;

				case ConnectionManager.MSG_BLE_MANUALLY_CONNECTED:
					if (mDlgDiaperSensorConnectionFailed != null && mDlgDiaperSensorConnectionFailed.isShowing()) {
						if (DBG) Log.e(TAG, "Dismiss connection failed dialog");
						mDlgDiaperSensorConnectionFailed.dismiss();
					}
					mManualConnected = true;
					int state2 = msg.arg1;
					mManuallyConnectedDevice = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_BLE_MANUALLY_CONNECTED : [" + mManuallyConnectedDevice.deviceId + "] " + state2 + " / " + mManuallyConnectedDevice.btmacAddress);

					// 센서 연결 후 허브 연결 확인
					this.sendEmptyMessage(MSG_CHECK_HUB_CONNECTION_VIA_SENSOR);
					break;

				case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST:
					mGuestDeviceInfo = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_BLE_MANUAL_CONNECTION_GUEST : " + mGuestDeviceInfo.toString());

					if (mManuallyConnectedDevice != null) {
						DeviceBLEConnection bleConnection = mConnectionMgr.getDeviceBLEConnection(mManuallyConnectedDevice.deviceId, DeviceType.DIAPER_SENSOR);
						if (bleConnection != null) {
							ConnectionManager.mRegisteredDiaperSensorList.remove(mManuallyConnectedDevice.deviceId);
							bleConnection.unregister();
							ConnectionManager.removeDeviceBLEConnection(mManuallyConnectedDevice.deviceId, mManuallyConnectedDevice.type);
							mManuallyConnectedDevice = null;
						} else {
							if (DBG) Log.e(TAG, "bleConnection NULL");
						}
					}

					if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
						mDlgProcessing.dismiss();
					}
					if (mGuestDeviceInfo != null) {
						removeMessages(MSG_REFRESH_PROGRESS);
						if (mGuestDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
							if (mDlgDiaperSensorAlreadyRegistered != null && !mDlgDiaperSensorAlreadyRegistered.isShowing()) {
								try {
									mDlgDiaperSensorAlreadyRegistered.show();
								} catch (Exception e) {

								}
							}
						} else if (mGuestDeviceInfo.type == DeviceType.AIR_QUALITY_MONITORING_HUB) {
							if (mDlgHubAlreadyRegistered != null && !mDlgHubAlreadyRegistered.isShowing()) {
								try {
									mDlgHubAlreadyRegistered.show();
								} catch (Exception e) {

								}
							}
						}
					}

					break;
				case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT:
					int reason = msg.arg2;
					if (DBG) Log.e(TAG, "MSG_BLE_MANUAL_CONNECTION_TIME_OUT : " + reason);
					if (mManuallyConnectedDevice != null) {
						DeviceBLEConnection bleConnection = mConnectionMgr.getDeviceBLEConnection(mManuallyConnectedDevice.deviceId, DeviceType.DIAPER_SENSOR);
						if (bleConnection != null) {
							ConnectionManager.mRegisteredDiaperSensorList.remove(mManuallyConnectedDevice.deviceId);
							bleConnection.unregister();
							ConnectionManager.removeDeviceBLEConnection(mManuallyConnectedDevice.deviceId, mManuallyConnectedDevice.type);
							mManuallyConnectedDevice = null;
						} else {
							if (DBG) Log.e(TAG, "bleConnection NULL");
						}
					}

					if ((mDlgInitializeSensor != null && mDlgInitializeSensor.isShowing() == false)
							&& (mDlgInitializeHub != null && mDlgInitializeHub.isShowing() == false)
							&& (mDlgDiaperSensorNotDetected != null && mDlgDiaperSensorNotDetected.isShowing() == false)) {
						if (reason < 10) { // BLE 데이터를 제대로 못받음 : 센서 전원을 껐다가 다시 연결해주세요, 5 : Serial Number받았고 getDeviceId 수행함
							if (mDlgDiaperSensorConnectionFailed != null) {
								mDlgDiaperSensorConnectionFailed.setContents(getString(R.string.dialog_contents_failed_connection_reason_ble));
							}
						} else if (reason == 10 || reason == 12) { // BLE 데이터는 모두 받은상태로 GetDeviceId 또는 GetCloudId에서 TIMEOUT
							if (mDlgDiaperSensorConnectionFailed != null) {
								mDlgDiaperSensorConnectionFailed.setContents(getString(R.string.dialog_contents_failed_connection_reason_internet));
							}
						} else { // 100 : 기본 메시지
							if (mDlgDiaperSensorConnectionFailed != null) {
								mDlgDiaperSensorConnectionFailed.setContents(getString(R.string.dialog_contents_failed_connection));
							}
						}
						if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
							mDlgProcessing.dismiss();
						}
						if (mDlgDiaperSensorConnectionFailed != null && !mDlgDiaperSensorConnectionFailed.isShowing() && !mManualConnected && !mManualConnectCancelled) {
							try {
								mDlgDiaperSensorConnectionFailed.show();
							} catch (Exception e) {

							}
						}
					}
					break;

				case MSG_CHANGE_ANIMATION:
					removeMessages(MSG_CHANGE_ANIMATION);
					if (mStep == STEP1_CHECK_SENSOR) {

					} else if (mStep == STEP2_PUT_SENSOR) {
						switch(mAnimationIndex % 4) {
							case 0:
								ivAnimation.setImageResource(R.drawable.ani_hub_kc_put_sensor2);
								break;
							case 1:
								ivAnimation.setImageResource(R.drawable.ani_hub_kc_put_sensor3);
								break;
							case 2:
								ivAnimation.setImageResource(R.drawable.ani_hub_kc_put_sensor4);
								break;
							case 3:
								ivAnimation.setImageResource(R.color.colorTransparent);
								break;
						}
						mAnimationIndex++;
					}
					this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS);
					break;

				case ConnectionManager.MSG_HUB_CONNECTED_WITH_SENSOR:
					removeMessages(MSG_CHECK_HUB_CONNECTION_VIA_SENSOR);
					DeviceInfo sensorInfo = (DeviceInfo)msg.obj;
					DeviceInfo hubInfo = null;
					if (sensorInfo != null) {
						if (DBG) Log.d(TAG, "MSG_HUB_CONNECTED_WITH_SENSOR : " + sensorInfo.toString());
						DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensorInfo.deviceId, sensorInfo.type);
						if (bleConnection != null) {
							hubInfo = bleConnection.getHubDeviceInfo();
						}
					}
					if (hubInfo != null) {
						removeMessages(MSG_REFRESH_PROGRESS);
						mDlgProcessing.setProgress(100);
						try {
							mDlgProcessing.dismiss();
						} catch (IllegalArgumentException e) {

						}

						if (hubInfo.cloudId > 0 && hubInfo.cloudId != mPreferenceMgr.getAccountId()) {
							boolean isAlreadyInvitedCloud = false;
							for (Group group : UserInfoManager.getInstance(mContext).getGroupList()) {
								if (group == null) continue;

								if (group.getGroupInfo().cloudId == hubInfo.cloudId) {
									isAlreadyInvitedCloud = true;
									break;
								}
							}
							if (DBG) Log.d(TAG, "Guest : " + hubInfo.cloudId + " / " + mPreferenceMgr.getAccountId() + " / " + isAlreadyInvitedCloud);
							if (isAlreadyInvitedCloud) {
								// 이미 초대된 그룹이라면, 다이얼로그 건너뜀
								if (DBG) Log.d(TAG, "Hub registered now2");
								((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_HUB_SELECT_AP);
							} else {
								if (mManuallyConnectedDevice != null) {
									DeviceBLEConnection bleConnection = mConnectionMgr.getDeviceBLEConnection(mManuallyConnectedDevice.deviceId, DeviceType.DIAPER_SENSOR);
									if (bleConnection != null) {
										ConnectionManager.mRegisteredDiaperSensorList.remove(mManuallyConnectedDevice.deviceId);
										bleConnection.unregister();
										ConnectionManager.removeDeviceBLEConnection(mManuallyConnectedDevice.deviceId, mManuallyConnectedDevice.type);
										mManuallyConnectedDevice = null;
									} else {
										if (DBG) Log.e(TAG, "bleConnection NULL");
									}
								}

                                mGuestHubDeviceInfo = hubInfo;
								if ((mDlgHubAlreadyRegistered != null && !mDlgHubAlreadyRegistered.isShowing())
										&& (mDlgInitializeHub != null && !mDlgInitializeHub.isShowing())) {
									try {
										mDlgHubAlreadyRegistered.show();
									} catch (Exception e) {

									}
								}
							}
						} else {
							if (DBG) Log.d(TAG, "Hub registered now");
							((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_HUB_SELECT_AP);
						}
					} else {
						if (DBG) Log.e(TAG, "Hub info NULL");
					}
					break;
			}
		}
	};

    @Override
	public void onPause() {
    	super.onPause();
		mHandler.removeMessages(MSG_CHANGE_ANIMATION);
		mHandler.removeMessages(MSG_REFRESH_PROGRESS);
    	if (DBG) Log.i(TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		((ConnectionActivity)mMainActivity).updateView();
		((ConnectionActivity)mMainActivity).setFragmentHandler(mHandler);
		mAnimationIndex = 0;
		mHandler.sendEmptyMessage(MSG_CHANGE_ANIMATION);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
