package goodmonit.monit.com.kao.connection.Hub;

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
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.ProgressHorizontalDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class ConnectionHubPutSensor extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "PutSensor";
	private static final boolean DBG = Configuration.DBG;

	private static final int STEP1_CHECK_SENSOR		= 1;
	private static final int STEP2_PUT_SENSOR		= 2;
	private int mStep;

	private static final int MSG_CHANGE_ANIMATION	= 1;
	private static final int MSG_REFRESH_PROGRESS 	= 2;
	private static final long CHANGE_ANIMATION_INTERVAL_MS = 1000;
	public static final long TIME_HUB_CONNECTION_WITH_SENSOR_TIME_OUT_SEC	= 10;

	private int mAnimationIndex;

	private Button btnConnect, btnHelp;

	private ProgressHorizontalDialog mDlgProcessing;
	private SimpleDialog mDlgConnectionFailed, mDlgNotDetected, mDlgInternetConnection, mDlgGuest, mDlgInitializeHub;
	private int scanSeconds;
	private ImageView ivAnimation;
	private TextView tvTitle, tvDetail;
	private DeviceInfo mHubInfo;
	private String mTitle, mButtonName;
	private boolean isConnectingHubFound = false;
	private DeviceInfo mGuestDeviceInfo;

	private ValidationManager mValidationMgr;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_put_sensor, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mValidationMgr = new ValidationManager(getContext());
		mScreenInfo = new ScreenInfo(701);
		mAnimationIndex = 0;
        _initView(view);

		setView(STEP2_PUT_SENSOR);
        return view;
    }

	private void _initView(View v) { // View
		btnConnect = (Button)v.findViewById(R.id.btn_connection_hub_put_sensor_connect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mStep == STEP1_CHECK_SENSOR) {
					//vsAnimation.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_from_right));
					//vsAnimation.showNext();
					((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_READY_FOR_CONNECTING);
				} else if (mStep == STEP2_PUT_SENSOR) {
					if (ServerManager.isInternetConnected()) {
						_startConnect();
					} else {
						if (mDlgInternetConnection != null) {
							try {
								mDlgInternetConnection.show();
							} catch (Exception e) {

							}
						}
					}
				}
			}
		});

		btnHelp = (Button)v.findViewById(R.id.btn_connection_hub_put_sensor_help);
		btnHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 20);
			}
		});

		ivAnimation = (ImageView)v.findViewById(R.id.iv_connection_hub_put_sensor_animation);
		tvDetail = (TextView)v.findViewById(R.id.tv_connection_hub_put_sensor_detail);
		tvTitle = (TextView)v.findViewById(R.id.tv_connection_hub_put_sensor_title);
    }

	private void setView(int step) {
		mStep = step;
		mAnimationIndex = 0;
		switch (step) {
			case STEP1_CHECK_SENSOR:
				tvDetail.setText(getString(R.string.connection_hub_put_sensor_detail_step1));
				btnConnect.setText(getString(R.string.btn_connect_monit_sensor));
				ivAnimation.setImageResource(R.drawable.ani_hub_put_sensor1);
				break;
			case STEP2_PUT_SENSOR:
				tvDetail.setText(getString(R.string.connection_hub_put_sensor_detail_step2));
				btnConnect.setText(getString(R.string.connection_start_connect));
				mHandler.sendEmptyMessage(MSG_CHANGE_ANIMATION);
				break;
		}
	}

	private void _startConnect() {
		isConnectingHubFound = false;
		try {
			mDlgProcessing.show();
		} catch (Exception e) {

		}
		scanSeconds = 0;
		mHandler.sendEmptyMessage(MSG_REFRESH_PROGRESS);
	}

    @Override
	public void onPause() {
    	super.onPause();
		mHandler.removeMessages(MSG_CHANGE_ANIMATION);
    	if (DBG) Log.i(TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		((ConnectionActivity)mMainActivity).setFragmentHandler(mHandler);

		/*
		if (((ConnectionActivity)mMainActivity).hasBLEConnectedSensor() == true) {
			setView(STEP2_PUT_SENSOR);
		} else {
			if (DBG) Log.i(TAG, "No BLE Sensor");
			setView(STEP1_CHECK_SENSOR);
		}
		*/

		if (mDlgProcessing == null) {
			mDlgProcessing = new ProgressHorizontalDialog(
					mMainActivity,
					getString(R.string.dialog_contents_scanning),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mHandler.removeMessages(MSG_REFRESH_PROGRESS);
							mDlgProcessing.dismiss();
						}
					});
		}

		if (mDlgConnectionFailed == null) {
			mDlgConnectionFailed = new SimpleDialog(
					mMainActivity,
					getString(R.string.dialog_contents_failed_connection),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgConnectionFailed.dismiss();
						}
					},
					getString(R.string.btn_try_again),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgConnectionFailed.dismiss();
							_startConnect();
						}
					});
		}

		if (mDlgInternetConnection == null) {
			mDlgInternetConnection = new SimpleDialog(
					mMainActivity,
					getString(R.string.dialog_contents_need_internet_connection),
					//getString(R.string.dialog_contents_failed_connection),
					getString(R.string.btn_ok),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgInternetConnection.dismiss();
						}
					});
		}

		if (mDlgNotDetected == null) {
			mDlgNotDetected = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_HUB_NOT_FOUND + "]",
					getString(R.string.dialog_contents_not_detected_hub),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgNotDetected.dismiss();
						}
					},
					getString(R.string.btn_try_again),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgNotDetected.dismiss();
							_startConnect();
						}
					});
			mDlgNotDetected.setContentsGravity(Gravity.LEFT);
		}
		mDlgNotDetected.showHelpButton(true);
		mDlgNotDetected.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 19);
			}
		});

		if (mDlgGuest == null) {

			mDlgGuest = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_HUB_ALREADY_REGISTERED + "]",
					getString(R.string.dialog_contents_hub_already_registered) + mPreferenceMgr.getShortId(),
					getString(R.string.btn_device_initialize),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgGuest.dismiss();
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
							mDlgGuest.dismiss();
							if (mDlgConnectionFailed != null && !mDlgConnectionFailed.isShowing()) {
								try {
									mDlgConnectionFailed.show();
								} catch (Exception e) {

								}
							}
							((ConnectionActivity)mMainActivity).allowGuestManualConnection(false);
						}
					});

			mDlgGuest.setButtonColor(
					getResources().getColor(R.color.colorTextWarning),
					getResources().getColor(R.color.colorTextPrimary));

			mDlgGuest.setContentsGravity(Gravity.LEFT);
		}
		mDlgGuest.showHelpButton(true);
		mDlgGuest.setHelpButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 27);
			}
		});

		if (mDlgInitializeHub == null) {
			mDlgInitializeHub = new SimpleDialog(mContext,
					getString(R.string.dialog_contents_sensor_already_registered_init)+mPreferenceMgr.getShortId(), //기기 초기화시에 ~~~
					getString(R.string.btn_cancel), // 취소버튼
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgInitializeHub.dismiss(); // 취소 버튼을 눌렀을 때 발생하는 이벤트
						}
					},
					getString(R.string.btn_device_initialize), //초기화 버튼
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String inputShortId = mDlgInitializeHub.getInputText(); // 멤버 아이디를 기입하는 인풋
							if (inputShortId.length() > 1 && mValidationMgr.isValidShortId(inputShortId)) {  // 길이가 1보다 크고, ~~~
								if(inputShortId.equalsIgnoreCase(mPreferenceMgr.getShortId()))
								{
									((ConnectionActivity)mMainActivity).mServerQueryMgr.initDevice(
										mHubInfo.type, // int type
										mHubInfo.deviceId, // long type
										mHubInfo.getEnc(), // string type
										new ServerManager.ServerResponseListener() {
											@Override
											public void onReceive(int responseCode, String errCode, String data) {
												if (InternetErrorCode.SUCCEEDED.equals(errCode)) { // 만약 error 코드가 succeeded라면,
													((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_hub_initialize_succeeded)); // 허브 초기화에 성공하였습니다.
													mHubInfo = null;
													mDlgInitializeHub.dismiss();
												} else {
													((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_hub_initialize_failed)); // 허브 초기화에 실패하였습니다.
												}
											}
										});
								} else {
									((ConnectionActivity)mMainActivity).showToast(getString(R.string.warning_not_match_short_id)); // 회원 코드가 일치하지 않습니다.
								}
								/*
								mServerQueryMgr.checkSerialNumber(
										mHubInfo.type,
										mHubInfo.deviceId,
										msg,
										new ServerManager.ServerResponseListener() {
											@Override
											public void onReceive(int responseCode, String errCode, String data) {
												if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
													mServerQueryMgr.initDevice(
															mHubInfo.type,
															mHubInfo.deviceId,
															mHubInfo.getEnc(),
															new ServerManager.ServerResponseListener() {
																@Override
																public void onReceive(int responseCode, String errCode, String data) {
																	if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
																		((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_hub_initialize_succeeded));
																		mHubInfo = null;
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
										});*/
							}
							else {
								((ConnectionActivity)mMainActivity).showToast(getString(R.string.group_warning_invalid_short_id));// 올바른 회원코드 양식이 아닙니다.
							}
						}
					});
			mDlgInitializeHub.setContentsGravity(Gravity.LEFT);
			mDlgInitializeHub.setInputMode(true);
			mDlgInitializeHub.setButtonColor(
					getResources().getColor(R.color.colorTextPrimary),
					getResources().getColor(R.color.colorTextWarning));
		}

		if (mTitle != null) {
			tvTitle.setText(mTitle);
		}

		if (mButtonName != null) {
			btnConnect.setText(mButtonName);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
		if (mDlgGuest != null && mDlgGuest.isShowing()) {
			mDlgGuest.dismiss();
		}
		if (mDlgConnectionFailed != null && mDlgConnectionFailed.isShowing()) {
			mDlgConnectionFailed.dismiss();
		}
		if (mDlgInternetConnection != null && mDlgInternetConnection.isShowing()) {
			mDlgInternetConnection.dismiss();
		}
		if (mDlgNotDetected != null && mDlgNotDetected.isShowing()) {
			mDlgNotDetected.dismiss();
		}
		if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
			mDlgProcessing.dismiss();
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_PROGRESS:
					if (scanSeconds <= TIME_HUB_CONNECTION_WITH_SENSOR_TIME_OUT_SEC) {
						if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
							mDlgProcessing.setProgress((int)TIME_HUB_CONNECTION_WITH_SENSOR_TIME_OUT_SEC * scanSeconds);
						}
						if (scanSeconds % 3 == 0) {
							((ConnectionActivity) mMainActivity).sendCheckHubStatus();
						}
						scanSeconds++;
						this.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS, 1000);
					} else {
						if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
							try {
								mDlgProcessing.dismiss();
							} catch (IllegalArgumentException e) {

							}
						}
						if (mDlgNotDetected != null) {
							try {
								mDlgNotDetected.show();
								mConnectionMgr.sendDeviceNotFound(DeviceType.AIR_QUALITY_MONITORING_HUB);
							} catch (Exception e) {

							}
						}
					}
					break;
				case MSG_CHANGE_ANIMATION:
					if (mStep == STEP1_CHECK_SENSOR) {

					} else if (mStep == STEP2_PUT_SENSOR) {
						switch(mAnimationIndex % 4) {
							case 0:
								if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
									ivAnimation.setImageResource(R.drawable.ani_hub_kc_put_sensor2);
								} else {
									ivAnimation.setImageResource(R.drawable.ani_hub_put_sensor2);
								}
								break;
							case 1:
								if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
									ivAnimation.setImageResource(R.drawable.ani_hub_kc_put_sensor3);
								} else {
									ivAnimation.setImageResource(R.drawable.ani_hub_put_sensor3);
								}
								break;
							case 2:
								if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
									ivAnimation.setImageResource(R.drawable.ani_hub_kc_put_sensor4);
								} else {
									ivAnimation.setImageResource(R.drawable.ani_hub_put_sensor4);
								}
								break;
							case 3:
								ivAnimation.setImageResource(R.color.colorTransparent);
								break;
						}
						mAnimationIndex++;
					}
					this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS);
					break;

				case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST:
					mGuestDeviceInfo = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_BLE_MANUAL_CONNECTION_GUEST : " + mGuestDeviceInfo.toString());

					if (mGuestDeviceInfo != null) {
						removeMessages(MSG_REFRESH_PROGRESS);
						if (mDlgGuest != null && !mDlgGuest.isShowing()) {
							try {
								mDlgGuest.show();
								NotificationMessage msgGuest = new NotificationMessage(NotificationType.SYSTEM_DEVICE_ALREADY_REGISTERED, DeviceType.AIR_QUALITY_MONITORING_HUB, mGuestDeviceInfo.deviceId, "aid:" + mPreferenceMgr.getAccountId() + "/did:" + mGuestDeviceInfo.deviceId, System.currentTimeMillis());
								ServerQueryManager.getInstance(mContext).setNotificationFeedback(msgGuest, null);
							} catch (Exception e) {

							}
						}
					}

					break;
				case ConnectionManager.MSG_HUB_CONNECTED_WITH_SENSOR:
					if (isConnectingHubFound) break;
					else isConnectingHubFound = true;
					//if (DBG) Log.d(TAG, "MSG_HUB_CONNECTED_WITH_SENSOR : " + mConnectingSensorForHubInfo.toString() + " / " + mConnectedHubDeviceId);
					DeviceInfo sensorInfo = (DeviceInfo)msg.obj;
					if (sensorInfo != null) {
						if (DBG) Log.d(TAG, "MSG_HUB_CONNECTED_WITH_SENSOR : " + sensorInfo.toString());
						DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(sensorInfo.deviceId, sensorInfo.type);
						if (bleConnection != null) {
							mHubInfo = bleConnection.getHubDeviceInfo();
						}
					}
					if (mHubInfo != null) {
						removeMessages(MSG_REFRESH_PROGRESS);
						mDlgProcessing.setProgress(100);
						try {
							mDlgProcessing.dismiss();
						} catch (IllegalArgumentException e) {

						}

						if (mHubInfo.cloudId > 0 && mHubInfo.cloudId != mPreferenceMgr.getAccountId()) {
							boolean isAlreadyInvitedCloud = false;
							for (Group group : UserInfoManager.getInstance(mContext).getGroupList()) {
								if (group == null) continue;

								if (group.getGroupInfo().cloudId == mHubInfo.cloudId) {
									isAlreadyInvitedCloud = true;
									break;
								}
							}
							if (DBG) Log.d(TAG, "Guest : " + mHubInfo.cloudId + " / " + mPreferenceMgr.getAccountId() + " / " + isAlreadyInvitedCloud);
							if (isAlreadyInvitedCloud) {
								// 이미 초대된 그룹이라면, 다이얼로그 건너뜀
                                if (DBG) Log.d(TAG, "Hub registered now2");
                                ((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_HUB_SELECT_AP);
							} else {
								if ((mDlgGuest != null && !mDlgGuest.isShowing())
										&& (mDlgInitializeHub != null && !mDlgInitializeHub.isShowing())) {
									try {
										mDlgGuest.show();
									} catch (Exception e) {

									}
								}
							}
						} else {
							if (DBG) Log.d(TAG, "Hub registered now");
							((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_HUB_SELECT_AP);
						}
					} else {
						if (DBG) Log.e(TAG, "Hub info NULL");
					}
					break;
			}
		}
	};

	public void setTitle(String title) {
		mTitle = title;
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}

	public void setButtonName(String name) {
		mButtonName = name;
		if (btnConnect != null) {
			btnConnect.setText(name);
		}
	}
}
