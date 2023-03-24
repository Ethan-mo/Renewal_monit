package goodmonit.monit.com.kao.connection.Lamp;

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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.ProgressHorizontalDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class ConnectionLampReady extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "ReadyLamp";
	private static final boolean DBG = Configuration.DBG;

	private static final int STEP1_POWER_ON			= 1;
	private static final int STEP2_CHECK_LED		= 2;

	private static final int MSG_CHANGE_ANIMATION	= 1;
	private static final int MSG_REFRESH_PROGRESS 	= 2;
	private static final long CHANGE_ANIMATION_INTERVAL_MS = 1000;
	private int mAnimationIndex;
	private int mStep = 0;
	private boolean mManualConnected = false;
	private boolean mManualConnectCancelled = false;

	private Button btnConnect;
	private ProgressHorizontalDialog mDlgProcessing;
	private SimpleDialog mDlgConnectionFailed, mDlgNotDetected, mDlgInternetConnection, mDlgGuest, mDlgInitializeLamp;
	private int scanSeconds;

	private ImageView ivAnimationStep1, ivAnimationStep2;
	private TextView tvTitle, tvDetail;
	private DeviceInfo mLampInfo;
	private String mTitle, mButtonName;
	private ViewSwitcher vsAnimation;
	private DeviceInfo mGuestDeviceInfo;

	private ValidationManager mValidationMgr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_lamp_ready, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mScreenInfo = new ScreenInfo(701); // 수정필요
        _initView(view);
		setView(STEP1_POWER_ON);

		int available = ConnectionManager.checkBluetoothStatus();
		if (available == ConnectionManager.STATE_DISABLED || available == ConnectionManager.STATE_UNAVAILABLE) {
			Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(btEnableIntent, ConnectionActivity.REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN);
		}
        return view;
    }

	private void _initView(View v) {
		btnConnect = (Button)v.findViewById(R.id.btn_connection_lamp_start_connect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mConnectionMgr.checkLocationStatus()) { // Need to enable Location before BLE connection
					((ConnectionActivity)getActivity()).checkLocation();
					return;
				}

				if (mStep == STEP1_POWER_ON) {
					vsAnimation.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_from_right));
					vsAnimation.showNext();
					setView(STEP2_CHECK_LED);
				} else if (mStep == STEP2_CHECK_LED) {
					if (Configuration.NO_INTERNET) {
						_startConnect();
					} else {
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
			}
		});
		ivAnimationStep1 = (ImageView)v.findViewById(R.id.iv_connection_lamp_ready_animation_step1);
		ivAnimationStep2 = (ImageView)v.findViewById(R.id.iv_connection_lamp_ready_animation_step2);
		vsAnimation = (ViewSwitcher)v.findViewById(R.id.vs_connection_lamp_ready_animation);
		tvDetail = (TextView)v.findViewById(R.id.tv_connection_lamp_ready_detail);


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

		if (mDlgConnectionFailed == null) {
			mDlgConnectionFailed = new SimpleDialog(
					mContext,
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
					mContext,
					getString(R.string.dialog_contents_need_internet_connection),
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
					"[Code" + ConnectionActivity.CODE_HELP_LAMP_NOT_FOUND + "]",
					getString(R.string.dialog_contents_not_detected_lamp),
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

		if (mDlgGuest == null) { //이부분이 램프에서 초기화 과정을 거쳐야하는
			mDlgGuest = new SimpleDialog(
					mContext,"[Code" + ConnectionActivity.CODE_HELP_HUB_ALREADY_REGISTERED + "]",
					getString(R.string.dialog_contents_already_registered_invite_request) + mPreferenceMgr.getShortId(),
					getString(R.string.btn_device_initialize),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgGuest.dismiss();
							if (mDlgConnectionFailed != null && !mDlgInitializeLamp.isShowing()) {
								try {
									mDlgInitializeLamp.show();
								} catch (Exception e) {

								}
							}
							((ConnectionActivity)mMainActivity).allowGuestManualConnection(false);
						}
					}, getString(R.string.btn_close),
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

		if (mDlgInitializeLamp == null) {
			mDlgInitializeLamp = new SimpleDialog(mContext,
					getString(R.string.dialog_contents_sensor_already_registered_init)+mPreferenceMgr.getShortId(), //기기 초기화시에 ~~~
					getString(R.string.btn_cancel), // 취소버튼
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgInitializeLamp.dismiss(); // 취소 버튼을 눌렀을 때 발생하는 이벤트
						}
					},
					getString(R.string.btn_device_initialize), //초기화 버튼
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String inputShortId = mDlgInitializeLamp.getInputText(); // 멤버 아이디를 기입하는 인풋
							if (inputShortId.length() > 1 && mValidationMgr.isValidShortId(inputShortId)) {  // 길이가 1보다 크고, ~~~
								if(inputShortId.equalsIgnoreCase(mPreferenceMgr.getShortId()))
								{
									((ConnectionActivity)mMainActivity).mServerQueryMgr.initDevice(
											mLampInfo.type, // int type
											mLampInfo.deviceId, // long type
											mLampInfo.getEnc(), // string type
											new ServerManager.ServerResponseListener() {
												@Override
												public void onReceive(int responseCode, String errCode, String data) {
													if (InternetErrorCode.SUCCEEDED.equals(errCode)) { // 만약 error 코드가 succeeded라면,
														((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_lamp_initialize_succeeded)); // 허브 초기화에 성공하였습니다.
														mLampInfo = null;
														mDlgInitializeLamp.dismiss();
													} else {
														((ConnectionActivity)mMainActivity).showToast(getString(R.string.toast_lamp_initialize_failed)); // 허브 초기화에 실패하였습니다.
													}
												}
											});
								} else {
									((ConnectionActivity)mMainActivity).showToast(getString(R.string.warning_not_match_short_id)); // 회원 코드가 일치하지 않습니다.
								}
							}
							else {
								((ConnectionActivity)mMainActivity).showToast(getString(R.string.group_warning_invalid_short_id));// 올바른 회원코드 양식이 아닙니다.
							}
						}
					});
			mDlgInitializeLamp.setContentsGravity(Gravity.LEFT);
			mDlgInitializeLamp.setInputMode(true);
			mDlgInitializeLamp.setButtonColor(
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

	private void setView(int step) {
		mStep = step;
		mAnimationIndex = 0;
		switch (step) {
			case STEP1_POWER_ON:
				tvDetail.setText(getString(R.string.connection_lamp_ready_detail_step1));
				btnConnect.setText(getString(R.string.btn_next));
				break;
			case STEP2_CHECK_LED:
				tvDetail.setText(getString(R.string.connection_lamp_ready_detail_step2));
				btnConnect.setText(getString(R.string.connection_start_connect));
				break;
		}
	}

	private void _startConnect() {
		mManualConnected = false;
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
		mConnectionMgr.manualConnectLamp();
	}

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_PROGRESS:
					removeMessages(MSG_REFRESH_PROGRESS);
					if (scanSeconds < (int)(ConnectionManager.TIME_BLE_MANUAL_SCAN_TIME_OUT_SEC + ConnectionManager.TIME_BLE_MANUAL_CONNECTION_TIME_OUT_SEC)) {
						if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
							mDlgProcessing.setProgress((int)((100.0 / (ConnectionManager.TIME_BLE_MANUAL_SCAN_TIME_OUT_SEC + ConnectionManager.TIME_BLE_MANUAL_CONNECTION_TIME_OUT_SEC - 1)) * scanSeconds));
						}
						scanSeconds++;
						this.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS, 1000);
					}
					break;
				case ConnectionManager.MSG_SCAN_FINISHED:
					int foundDeviceCount = msg.arg1;
					if (DBG) Log.d(TAG, "ConnectionManager.MSG_SCAN_FINISHED : " + foundDeviceCount);

					if (foundDeviceCount == 0) {
						mDlgProcessing.dismiss();
						if (!mManualConnected && !mManualConnectCancelled) {
							mDlgNotDetected.setContents(getString(R.string.dialog_contents_not_detected_lamp));
							try {
								mDlgNotDetected.show();
								mConnectionMgr.sendDeviceNotFound(DeviceType.LAMP);
							} catch (Exception e) {

							}
						} else {

						}
						break;
					}

					mDlgProcessing.setContents(getString(R.string.dialog_contents_connecting));
					break;
				case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
					int state = msg.arg1;
					DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
					break;
				case ConnectionManager.MSG_BLE_MANUALLY_CONNECTED:
					if (mDlgConnectionFailed != null && mDlgConnectionFailed.isShowing()) {
						if (DBG) Log.e(TAG, "Dismiss connection failed dialog");
						mDlgConnectionFailed.dismiss();
					}
					mManualConnected = true;
					removeMessages(MSG_REFRESH_PROGRESS);
					int state2 = msg.arg1;
					DeviceInfo deviceInfo2 = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_BLE_MANUALLY_CONNECTED : [" + deviceInfo2.deviceId + "] " + state2 + " / " + deviceInfo2.btmacAddress);
					if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
						mDlgProcessing.setProgress(100);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mDlgProcessing.dismiss();
							}
						}, 500);
					}
					break;

				case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST:
					mGuestDeviceInfo = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_BLE_MANUAL_CONNECTION_GUEST : " + mGuestDeviceInfo.toString());
					if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
						mDlgProcessing.dismiss();
					}
					if (mDlgGuest != null && !mDlgGuest.isShowing()) {
						try {
							mDlgGuest.show();
							NotificationMessage msgGuest = new NotificationMessage(NotificationType.SYSTEM_DEVICE_ALREADY_REGISTERED, DeviceType.LAMP, mGuestDeviceInfo.deviceId, "aid:" + mPreferenceMgr.getAccountId() + "/did:" + mGuestDeviceInfo.deviceId, System.currentTimeMillis());
							ServerQueryManager.getInstance(mContext).setNotificationFeedback(msgGuest, null);
						} catch (Exception e) {

						}
					}
					break;
				case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT:
					int reason = msg.arg2;
					if (DBG) Log.e(TAG, "MSG_BLE_MANUAL_CONNECTION_TIME_OUT : " + reason);
					if (reason < 10) { // BLE 데이터를 제대로 못받음 : 센서 전원을 껐다가 다시 연결해주세요, 5 : Serial Number받았고 getDeviceId 수행함
						if (mDlgConnectionFailed != null) {
							mDlgConnectionFailed.setContents(getString(R.string.dialog_contents_failed_connection_reason_ble));
						}
					} else if (reason == 10 || reason == 12) { // BLE 데이터는 모두 받은상태로 GetDeviceId 또는 GetCloudId에서 TIMEOUT
						if (mDlgConnectionFailed != null) {
							mDlgConnectionFailed.setContents(getString(R.string.dialog_contents_failed_connection_reason_internet));
						}
					} else { // 100 : 기본 메시지
						if (mDlgConnectionFailed != null) {
							mDlgConnectionFailed.setContents(getString(R.string.dialog_contents_failed_connection));
						}
					}
					if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
						mDlgProcessing.dismiss();
					}
					if (mDlgConnectionFailed != null && !mDlgConnectionFailed.isShowing() && !mManualConnected && !mManualConnectCancelled) {
						try {
							mDlgConnectionFailed.show();
						} catch (Exception e) {

						}
					}
					break;

				case MSG_CHANGE_ANIMATION:
					if (mStep == STEP1_POWER_ON) {
						switch(mAnimationIndex % 3) {
							case 0:
								ivAnimationStep1.setImageResource(R.drawable.ani_lamp_ready1);
								this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS);
								break;
							case 1:
								ivAnimationStep1.setImageResource(R.drawable.ani_lamp_ready2);
								this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS);
								break;
							case 2:
								ivAnimationStep1.setImageResource(R.color.colorTransparent);
								this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS / 2);
								break;
						}
					} else if (mStep == STEP2_CHECK_LED) {
						switch(mAnimationIndex % 2) {
							case 0:
								ivAnimationStep2.setImageResource(R.drawable.ani_lamp_ready3);
								break;
							case 1:
								ivAnimationStep2.setImageResource(R.drawable.ani_lamp_ready4);
								break;
						}
						this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS / 2);
					}
					mAnimationIndex++;
					break;
			}
		}
	};

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
		mHandler.removeMessages(MSG_CHANGE_ANIMATION);
		mHandler.removeMessages(MSG_REFRESH_PROGRESS);
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
