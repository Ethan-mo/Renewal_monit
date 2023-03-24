package goodmonit.monit.com.kao.devicestatus;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceSensorActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class DiaperStatus2Fragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "DiaperFrgmt2";
	private static final boolean DBG = Configuration.DBG;

	private static final int MSG_REFRESH_VIEW 			= 1;
	private static final int REFRESH_VIEW_INTERVAL_SEC 	= 1;

	private LinearLayout lctnSleepSensingMode;

	private ImageView ivBabyStatusIcon;
	private TextView tvBabyStatus;

	private Button btnStatusItem1Icon;
	private TextView tvStatusItem1Title, tvStatusItem1Content, tvStatusItem1ContentExtra;

	private Button btnStatusItem2Icon;
	private TextView tvStatusItem2Title, tvStatusItem2Content, tvStatusItem2ContentExtra;

	private Button btnStatusItem3Icon;
	private TextView tvStatusItem3Title, tvStatusItem3Content, tvStatusItem3ContentExtra;

	private Button btnSensingSleepingModeIcon;
	private TextView tvSensingSleepingMode;
	private Button btnSensingSleepingMode;

	private Button btnDiaperChanged, btnDiaperChangedElapsedTime;

	private Button btnSensorOperationStatus;
	private TextView tvSensorOperationStatus;

	private ImageView ivSensorBatteryPower;
	private TextView tvSensorBatteryPower;

	private RelativeLayout rctnSensingValue;
	private TextView tvSensingValue;
	private Button btnSensingValueClose;

	private Button btnStatusInit;

	private boolean isConnected = false;
	private boolean isCharging = false;
	private DeviceDiaperSensor mMonitSensor;
	private VersionManager mVersionMgr;
	private UserInfoManager mUserInfoMgr;

	private int mOperationStatus, mDiaperStatus, mBatteryPower, mMovementStatus, mDiaperScore;

	private int mCntRawValueTouch = 0;

	/* Related to Sleeping */
	private boolean isSleeping;
	private int mSleepingIndex;
	private int mAnimationIndex = 0;

	private SimpleDialog mDlgDiaper, mDlgVoc, mDlgMovement, mDlgSleeping, mDlgSensorOperation;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_diaper_sensor_status2, container, false);
		mContext = inflater.getContext();
		mVersionMgr = new VersionManager(mContext);
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(901);
		mUserInfoMgr = UserInfoManager.getInstance(mContext);
        _initView(view);
		isConnected = true;
		setConnected(false);
        return view;
    }

	private void _initView(View v) {
		mDlgDiaper = new SimpleDialog(mContext,
				getString(R.string.guide_sensor_diaper_status_title),
				getString(R.string.guide_sensor_diaper_status_contents),
				getString(R.string.btn_close),
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mDlgDiaper.dismiss();
					}
				});
		mDlgDiaper.setContentsGravity(Gravity.LEFT);

		mDlgVoc = new SimpleDialog(mContext,
                getString(R.string.guide_sensor_voc_avg_title),
                getString(R.string.guide_sensor_voc_avg_contents),
				getString(R.string.btn_close),
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mDlgVoc.dismiss();
					}
				});
		mDlgVoc.setContentsGravity(Gravity.LEFT);

		mDlgMovement = new SimpleDialog(mContext,
                getString(R.string.guide_sensor_moving_title),
                getString(R.string.guide_sensor_moving_contents),
				getString(R.string.btn_close),
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mDlgMovement.dismiss();
					}
				});
		mDlgMovement.setContentsGravity(Gravity.LEFT);

		mDlgSleeping = new SimpleDialog(mContext,
                getString(R.string.guide_sensor_sleep_mode_title),
                getString(R.string.guide_sensor_sleep_mode_contents),
				getString(R.string.btn_close),
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mDlgSleeping.dismiss();
					}
				});
		mDlgSleeping.setContentsGravity(Gravity.LEFT);

		mDlgSensorOperation = new SimpleDialog(mContext,
                getString(R.string.guide_sensor_operation_title),
                getString(R.string.guide_sensor_operation_contents),
				getString(R.string.btn_close),
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mDlgSensorOperation.dismiss();
					}
				});
		mDlgSensorOperation.setContentsGravity(Gravity.LEFT);

		ivBabyStatusIcon = (ImageView)v.findViewById(R.id.iv_device_detail_status_baby_status);
		ivBabyStatusIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCntRawValueTouch++;
				if (mCntRawValueTouch > 30) {
					mCntRawValueTouch = 0;
					rctnSensingValue.setVisibility(View.VISIBLE);
				}
			}
		});

		tvBabyStatus = (TextView)v.findViewById(R.id.tv_device_detail_status_baby_status);

		btnSensingSleepingModeIcon = (Button)v.findViewById(R.id.btn_device_detail_status_sensing_mode);
		btnSensingSleepingModeIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgSleeping != null && !mDlgSleeping.isShowing()) {
					mDlgSleeping.show();
				}
			}
		});

		lctnSleepSensingMode = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_sensing_mode);
		tvSensingSleepingMode = (TextView)v.findViewById(R.id.tv_device_detail_status_sensing_mode_description);
		btnSensingSleepingMode = (Button)v.findViewById(R.id.btn_device_detail_status_sensing_mode_enabled);
		btnSensingSleepingMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean enabled = mPreferenceMgr.getSleepingEnabled(mMonitSensor.deviceId);
				if (enabled) { // 수면 Enabled일 경우
					((DeviceSensorActivity)mMainActivity).showSleepDateTimeDialog(false);
					//mPreferenceMgr.setSleepingEnabled(mMonitSensor.deviceId, false);
					//mPreferenceMgr.setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, mMonitSensor.deviceId, NotificationType.MOVEMENT_DETECTED, false);
				} else {
					((DeviceSensorActivity)mMainActivity).showSleepDateTimeDialog(true);
					//mPreferenceMgr.setSleepingEnabled(mMonitSensor.deviceId, true);
					//mPreferenceMgr.setSleepingStartTimeMs(mMonitSensor.deviceId, System.currentTimeMillis());
				}
				refreshView();
			}
		});

		btnSensorOperationStatus = (Button)v.findViewById(R.id.btn_device_detail_status_sensor_operation_status);
		btnSensorOperationStatus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgSensorOperation != null && !mDlgSensorOperation.isShowing()) {
					mDlgSensorOperation.show();
				}
			}
		});
		tvSensorOperationStatus = (TextView)v.findViewById(R.id.tv_device_detail_status_sensor_operation_status);

		btnStatusItem1Icon = (Button)v.findViewById(R.id.btn_device_detail_status_item1_icon);
		btnStatusItem1Icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgDiaper != null && !mDlgDiaper.isShowing()) {
					mDlgDiaper.show();
				}
			}
		});
		btnStatusItem2Icon = (Button)v.findViewById(R.id.btn_device_detail_status_item2_icon);
		btnStatusItem2Icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgVoc != null && !mDlgVoc.isShowing()) {
					mDlgVoc.show();
				}
			}
		});
		btnStatusItem3Icon = (Button)v.findViewById(R.id.btn_device_detail_status_item3_icon);
		btnStatusItem3Icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgMovement != null && !mDlgMovement.isShowing()) {
					mDlgMovement.show();
				}
			}
		});

		tvStatusItem1Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_title);
		tvStatusItem2Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_title);
		tvStatusItem3Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_title);

		tvStatusItem1Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_content);
		tvStatusItem2Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_content);
		tvStatusItem3Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_content);

		tvStatusItem1ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_content_extra);
		tvStatusItem2ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_content_extra);
		tvStatusItem3ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_content_extra);
		tvStatusItem1ContentExtra.setVisibility(View.GONE);
		tvStatusItem2ContentExtra.setVisibility(View.GONE);
		tvStatusItem3ContentExtra.setVisibility(View.GONE);

		btnDiaperChanged = (Button)v.findViewById(R.id.btn_device_detail_status_change_diaper);
		btnDiaperChanged.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).showDateTimeDialog();
				refreshView();
			}
		});

		btnDiaperChangedElapsedTime = (Button)v.findViewById(R.id.btn_device_detail_status_change_diaper_elapsed_time);
		btnDiaperChangedElapsedTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((DeviceSensorActivity)getActivity()).showFragment(DeviceSensorActivity.VIEW_DIAPER_NOTIFICATION);
				((DeviceSensorActivity)getActivity()).selectTabButton(DeviceSensorActivity.VIEW_DIAPER_NOTIFICATION);
			}
		});

		ivSensorBatteryPower = (ImageView)v.findViewById(R.id.iv_device_detail_status_sensor_battery_power);
        tvSensorBatteryPower = (TextView)v.findViewById(R.id.tv_device_detail_status_sensor_battery_power);

		rctnSensingValue = (RelativeLayout)v.findViewById(R.id.rctn_device_detail_status_sensing_value);
		btnSensingValueClose = (Button)v.findViewById(R.id.btn_device_detail_status_sensing_value_close);
		btnSensingValueClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				rctnSensingValue.setVisibility(View.GONE);
			}
		});
		tvSensingValue = (TextView)v.findViewById(R.id.tv_device_detail_status_sensing_value);

		btnStatusInit = (Button)v.findViewById(R.id.btn_device_detail_status_initialize);
		btnStatusInit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).initDiaperStatus();
				refreshView();
			}
		});
    }

	private String[] mDebuggingText = new String[100];
	private int mDebuggingTextIdx = 0;
	public void addDebuggingText(String text) {
		mDebuggingText[mDebuggingTextIdx++ % 100] = text;
		if (tvSensingValue == null || tvSensingValue.getVisibility() != View.VISIBLE) {
			return;
		}

		String debuggingText = "";
		int cnt = 0;
		while(cnt < 100) {
			String line = mDebuggingText[(mDebuggingTextIdx + cnt) % 100];
			if (line != null) {
				debuggingText += line + "\n";
			}
			cnt++;
		}
		tvSensingValue.setText(debuggingText);
	}

    public void refreshView() {
		if (mMonitSensor == null) {
			return;
		}
		mMonitSensor.updateDiaperStatus();
		try {
			if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED ||
					mMonitSensor.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
				setConnected(true);

				setOperationStatus(mMonitSensor.getOperationStatus());
				setBatteryStatus(mMonitSensor.getBatteryPower());
				setVocStatus(mMonitSensor.getVocAvg());
				setPassedTimeFromDiaperChanged(mPreferenceMgr.getLatestDiaperChangedTimeSec(mMonitSensor.deviceId) * 1000);
				setMovementStatus(mMonitSensor.getMovementStatus());
				setDiaperScore(mMonitSensor.getDiaperScore());
				setDiaperStatus(mMonitSensor.getDiaperStatus());
				//setBabyStatus();
			} else {
				setConnected(false);
				setPassedTimeFromDiaperChanged(mPreferenceMgr.getLatestDiaperChangedTimeSec(mMonitSensor.deviceId) * 1000);
			}

			if (Configuration.MONIT_AUTO_SLEEP_DETECTION) {
				if (mPreferenceMgr.getAutoSleepingDetectionEnabled(mMonitSensor.deviceId)) {
					lctnSleepSensingMode.setVisibility(View.GONE);
					int currentSleepingStatus = mPreferenceMgr.getDiaperSensorCurrentSleepingLevel(mMonitSensor.deviceId);
					boolean sleeping = false;
					if(DBG) Log.d(TAG, "currentSleepingStatus : "+currentSleepingStatus);
					switch (currentSleepingStatus) {
						case DeviceStatus.MOVEMENT_SLEEP:
						case DeviceStatus.MOVEMENT_DEEP_SLEEP:
							sleeping = true;
							break;
					}
					isSleeping = sleeping;
				} else {
					lctnSleepSensingMode.setVisibility(View.VISIBLE);
					setSleepingMode(mPreferenceMgr.getSleepingEnabled(mMonitSensor.deviceId), mPreferenceMgr.getSleepingStartTimeMs(mMonitSensor.deviceId));
				}
			} else {
				setSleepingMode(mPreferenceMgr.getSleepingEnabled(mMonitSensor.deviceId), mPreferenceMgr.getSleepingStartTimeMs(mMonitSensor.deviceId));
			}
		} catch(IllegalStateException ex){
			if (DBG) Log.e(TAG, "refreshView exception");
		}
	}

    public void setConnected(boolean connected) {
		if (isConnected == connected) return;
		if (isAdded() == false) return;

		isConnected = connected;

		// 연결되었을 때,
		if (connected) {
			// Baby Activity
			ivBabyStatusIcon.setImageResource(R.drawable.ic_sensor_movement_activated);
			//tvBabyStatus.setText("");

			btnSensingSleepingModeIcon.setBackgroundResource(R.drawable.ic_diary_sleep_oval_activated);

			btnStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
			//tvStatusItem1Content.setText("");

			btnStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_voc_good);
			//tvStatusItem2Content.setText("");

			btnStatusItem3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
			//tvStatusItem3Content.setText("");

			// Sensor Operation
			btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
			tvSensorOperationStatus.setText(getString(R.string.device_sensor_operation_connected));

			// Battery Power
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100_row);
			//tvSensorBatteryPower.setText("");
		} else {
			// Baby Activity
			ivBabyStatusIcon.setImageResource(R.drawable.ic_sensor_movement_deactivated);
			tvBabyStatus.setText(getString(R.string.device_sensor_baby_talk_disconnected)); // 끊어짐

			btnSensingSleepingModeIcon.setBackgroundResource(R.drawable.ic_diary_sleep_oval_deactivated);

			btnStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_deactivated);
			tvStatusItem1Content.setText("");

			btnStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_voc_deactivated);
			tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
			tvStatusItem2Content.setText("");

			btnStatusItem3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
			tvStatusItem3Content.setText("");

			// Sensor Operation
			btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_deactivated);
			tvSensorOperationStatus.setText(getString(R.string.device_sensor_operation_disconnected));

			// Battery Power
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_deactivated_row);
			tvSensorBatteryPower.setText("");
		}
	}

	public void setBabyStatus() {
		if (isConnected) {
			tvBabyStatus.setText("");
		}
	}

	public void setBatteryStatus(int batteryPower) {
		mBatteryPower = batteryPower;

		switch (mOperationStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				isCharging = true;
				break;
			default:
				isCharging = false;
				break;
		}

		if (isCharging && batteryPower == 100) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charged_row);
		} else if (isCharging) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charging_row);
		} else if (batteryPower == 100) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100_row);
		} else if (batteryPower >= 90) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_9x_row);
		} else if (batteryPower >= 80) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_8x_row);
		} else if (batteryPower >= 70) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_7x_row);
		} else if (batteryPower >= 60) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_6x_row);
		} else if (batteryPower >= 50) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_5x_row);
		} else if (batteryPower >= 40) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_4x_row);
		} else if (batteryPower >= 30) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_3x_row);
		} else if (batteryPower >= 20) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_2x_row);
		} else if (batteryPower > 0) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_1x_row);
		} else if (batteryPower == 0) {
			ivSensorBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_0_row);
		}

		tvSensorBatteryPower.setText(batteryPower + "%");
	}

	public void setOperationStatus(int operation) {
		mOperationStatus = operation;
		switch(operation) {
			case DeviceStatus.OPERATION_IDLE:
				tvSensorOperationStatus.setText(getString(R.string.device_sensor_diaper_status_idle_main_screen));
				btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_idle);
				break;
			case DeviceStatus.OPERATION_SENSING:
				tvSensorOperationStatus.setText(getString(R.string.device_sensor_diaper_status_sensing_detail));
				btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				break;
			case DeviceStatus.OPERATION_GAS_DETECTED:
				tvSensorOperationStatus.setText(getString(R.string.device_sensor_diaper_status_sensing_detail));
				btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				break;
			case DeviceStatus.OPERATION_AVOID_SENSING:
				tvSensorOperationStatus.setText(getString(R.string.device_sensor_diaper_status_sensing_detail));
				btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				break;
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGING:
				tvSensorOperationStatus.setText(getString(R.string.device_sensor_operation_charging));
				btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				break;
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				tvSensorOperationStatus.setText(getString(R.string.device_sensor_operation_fully_charged));
				btnSensorOperationStatus.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				break;
			default:
				break;
		}
	}

    public void setMovementStatus(final int movement) {
		mMovementStatus = movement; // 보여주지는 않지만 현재변경된 값은 기억하고 있다가 보여줄때 그값을 보여줌

		mAnimationIndex = (mAnimationIndex + 1) % 4;
		if(DBG) Log.d(TAG, "mMonitSensor.getOperationStatus()) : "+mMonitSensor.getOperationStatus()+"\nisSleeping : " + isSleeping);
		switch (mMonitSensor.getOperationStatus()) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				tvStatusItem3Content.setText("-");
				ivBabyStatusIcon.setImageResource(DeviceStatus.getMovementIcon(DeviceStatus.MOVEMENT_NO_MOVEMENT, mAnimationIndex));
				break;

			default:
				if (isSleeping) { // 수면모드일 경우, 수면으로 표시
					tvStatusItem3Content.setText(DeviceStatus.getMovementStringResource(DeviceStatus.MOVEMENT_SLEEP));
					ivBabyStatusIcon.setImageResource(DeviceStatus.getMovementIcon(DeviceStatus.MOVEMENT_SLEEP, mAnimationIndex));
				} else {
					tvStatusItem3Content.setText(DeviceStatus.getMovementStringResource(mMovementStatus));
					ivBabyStatusIcon.setImageResource(DeviceStatus.getMovementIcon(movement, mAnimationIndex));
					switch (movement % 4) {
						case 0: // 3,4,7,8,11,12는 좀더 빠르게 애니메이션 움직이기(0.5초씩)
						case 3:
							if (movement > 0) {
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										ivBabyStatusIcon.setImageResource(DeviceStatus.getMovementIcon(movement, mAnimationIndex + 1));
									}
								}, 500);
							}
							break;
					}
				}
				break;
		}
	}

	public void setVocStatus(float vocValue) {
		switch (mOperationStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				btnStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_voc_good);
				tvStatusItem2Content.setText("-");
				tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextGrey));
				break;
			default:
				btnStatusItem2Icon.setBackgroundResource(DeviceStatus.getDiaperSensorVocIconResource(vocValue));
				tvStatusItem2Content.setText(getResources().getString(DeviceStatus.getDiaperSensorVocStringResource(vocValue)));
				if (vocValue == 0) {
					tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				} else {
					tvStatusItem2Title.setTextColor(getResources().getColor(DeviceStatus.getDiaperSensorVocStringColorResource(vocValue)));
				}
				tvStatusItem2Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperSensorVocStringColorResource(vocValue)));
				break;
		}
	}

	public void setDiaperScore(int score) {
		mDiaperScore = score;
		switch (mOperationStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				btnStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setText("-");
				tvBabyStatus.setText(getString(R.string.device_sensor_diaper_status_charged_detail));
				break;
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				btnStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setText("-");
				tvBabyStatus.setText(getString(R.string.device_sensor_diaper_status_charging_detail));
				break;
			default:
				btnStatusItem1Icon.setBackgroundResource(DeviceStatus.getDiaperScoreIconResource(score));
				if (score >= 90) {
					tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				} else {
					tvStatusItem1Title.setTextColor(getResources().getColor(DeviceStatus.getDiaperScoreColorResource(score)));
				}
				tvStatusItem1Content.setText(mContext.getString(DeviceStatus.getDiaperScoreStringResource(score)));
				tvStatusItem1Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperScoreColorResource(score)));
				tvBabyStatus.setText(DeviceStatus.getDiaperScoreBabyFeelingStringResource(score));
				break;
		}
	}

	public void setDiaperStatus(int diaperStatus) {
		mDiaperStatus = diaperStatus;
		switch (mOperationStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				btnStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setText("-");
				tvBabyStatus.setText(getString(R.string.device_sensor_diaper_status_charged_detail));
				break;
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				btnStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextGrey));
				tvStatusItem1Content.setText("-");
				tvBabyStatus.setText(getString(R.string.device_sensor_diaper_status_charging_detail));
				break;
			default:
				switch (diaperStatus) {
					case DeviceStatus.DETECT_ABNORMAL:
						btnStatusItem1Icon.setBackgroundResource(DeviceStatus.getDiaperScoreIconResource(0));
						tvStatusItem1Title.setTextColor(getResources().getColor(DeviceStatus.getDiaperScoreColorResource(0)));
						tvStatusItem1Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperScoreColorResource(0)));
						tvStatusItem1Content.setText(mContext.getString(DeviceStatus.getDiaperScoreStringResource(0)));
						tvBabyStatus.setText(DeviceStatus.getDiaperScoreBabyFeelingStringResource(0));
						break;
				}
				break;
		}
	}

	public void setPassedTimeFromDiaperChanged(long utcTimeMs) {
		if (DBG) Log.d(TAG, "setPassedTimeFromDiaperChanged : " + System.currentTimeMillis() + " / " + utcTimeMs + " / " + (System.currentTimeMillis() - utcTimeMs) + " / " + ((System.currentTimeMillis() - utcTimeMs) / 1000));
		int min = (int)((System.currentTimeMillis() - utcTimeMs) / 1000 / 60);
    	try {
			if (utcTimeMs == -1 || utcTimeMs == 0) {
				btnDiaperChangedElapsedTime.setText(getString(R.string.device_sensor_diaper_never_changed));
				return;
			}

			String timeString = DateTimeUtil.getElapsedTimeString(mContext, min);
			if (min <= 90) {
				btnDiaperChangedElapsedTime.setTextColor(getResources().getColor(R.color.colorTextPrimary));
			} else if (min <= 180) {
				btnDiaperChangedElapsedTime.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
			} else {
				btnDiaperChangedElapsedTime.setTextColor(getResources().getColor(R.color.colorTextWarning));
			}
			btnDiaperChangedElapsedTime.setText(getString(R.string.device_sensor_the_latest_time_diaper_changed, timeString));
		} catch (IllegalStateException e) {

		}
	}

	public void setSleepingMode(boolean enabled, long utcTimeMs) {
		if (DBG) Log.d(TAG, "setSleepingMode : " + enabled + " / " + utcTimeMs + " / " + (System.currentTimeMillis() - utcTimeMs) + " / " + ((System.currentTimeMillis() - utcTimeMs) / 1000 / 60));
		isSleeping = enabled;
		btnSensingSleepingMode.setSelected(enabled);
		if (enabled) {
			int min = (int)((System.currentTimeMillis() - utcTimeMs) / 1000 / 60);
			String beginTimeString = DateTimeUtil.getLocalTimeStringFromUtcTimestamp(utcTimeMs);
			String elapsedTimeString = DateTimeUtil.getElapsedTimeString(mContext, min);

			if (isConnected && !isCharging) {
				tvSensingSleepingMode.setText(getString(R.string.device_sensor_sleeping_sensing_mode));
			} else {
				tvSensingSleepingMode.setText(getString(R.string.device_sensor_sleeping_sensing_mode_disconnected));
			}
			tvSensingSleepingMode.append("\n" +	beginTimeString + "~ (" + getString(R.string.device_sensor_sleeping_elapsed_time, elapsedTimeString) + ")");

		} else {
			if (isConnected && !isCharging) {
				tvSensingSleepingMode.setText(getString(R.string.device_sensor_activity_sensing_mode));
			} else {
				tvSensingSleepingMode.setText(getString(R.string.device_sensor_activity_sensing_mode_disconnected));
			}
		}
	}

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
		mHandler.removeMessages(MSG_REFRESH_VIEW);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		((DeviceSensorActivity)mMainActivity).updateNewMark();
		mMonitSensor = ((DeviceSensorActivity)mMainActivity).getDiaperSensorObject();
		if (mMonitSensor == null) {
			if (DBG) Log.e(TAG, "Object NULL");
            ((DeviceSensorActivity)mMainActivity).finish();
            return;
		}
		btnSensingSleepingMode.setSelected(mPreferenceMgr.getSleepingEnabled(mMonitSensor.deviceId));

		mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_VIEW:
					mHandler.removeMessages(MSG_REFRESH_VIEW);
					mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, REFRESH_VIEW_INTERVAL_SEC * 1000L);
					refreshView();
					break;
			}
		}
	};
}