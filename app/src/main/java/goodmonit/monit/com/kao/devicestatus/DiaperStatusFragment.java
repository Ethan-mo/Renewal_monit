package goodmonit.monit.com.kao.devicestatus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceSensorActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.VersionManager;

public class DiaperStatusFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "DiaperFragment";
	private static final boolean DBG = Configuration.DBG;

	private static final int MSG_REFRESH_VIEW 			= 1;
	private static final int REFRESH_VIEW_INTERVAL_SEC 	= 1;

	private static boolean SHOW_MOVEMENT = false;

	private RelativeLayout rctnStatusBackground;
	private TextView tvDebugPanel;
	private int touchDebugPanel;
	private ImageView ivStatusIcon, ivStatusIconExtra;

	private ImageView ivStatusItem1Icon;
	private TextView tvStatusItem1Title, tvStatusItem1Content, tvStatusItem1ContentExtra;

	private ImageView ivStatusItem2Icon;
	private TextView tvStatusItem2Title, tvStatusItem2Content, tvStatusItem2ContentExtra;

	private ImageView ivStatusItem3Icon;
	private TextView tvStatusItem3Title, tvStatusItem3Content, tvStatusItem3ContentExtra;

	private TextView tvStatusDescription, tvStatusDescriptionExtra;
	private Button btnStatusChanged, btnStatusInit;

	//private RelativeLayout rctnChangedDiaper;
	//private TextView tvDetectionType;
	private TextView tvPassedTime;
	private TextView tvWhereConnected;

	private boolean isConnected = false;
	private DeviceDiaperSensor mMonitSensor;
	private VersionManager mVersionMgr;
	private UserInfoManager mUserInfoMgr;

	private int mMovementStatus, mOperationStatus, mDiaperStatus;
	private static final int CHANGING_DIAPER_STATUS_SEC = 3;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_diaper_sensor_status, container, false);
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
		rctnStatusBackground = (RelativeLayout)v.findViewById(R.id.rctn_device_detail_status_background);
		touchDebugPanel = 0;
		rctnStatusBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				touchDebugPanel++;
				if (touchDebugPanel > 30) {
					touchDebugPanel = 0;
					tvDebugPanel.setVisibility(View.VISIBLE);
				}
			}
		});
		tvDebugPanel = (TextView)v.findViewById(R.id.tv_debug_panel);
		tvDebugPanel.setMovementMethod(new ScrollingMovementMethod());
		tvDebugPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				touchDebugPanel++;
				if (touchDebugPanel > 30) {
					touchDebugPanel = 0;
					tvDebugPanel.setVisibility(View.GONE);
				}
			}
		});

		ivStatusIcon = (ImageView)v.findViewById(R.id.iv_device_detail_status_icon);
		ivStatusIconExtra = (ImageView)v.findViewById(R.id.iv_device_detail_status_icon_extra);

		ivStatusItem1Icon = (ImageView)v.findViewById(R.id.iv_device_detail_status_item1_icon);
		ivStatusItem2Icon = (ImageView)v.findViewById(R.id.iv_device_detail_status_item2_icon);
		ivStatusItem3Icon = (ImageView)v.findViewById(R.id.iv_device_detail_status_item3_icon);

		tvStatusItem1Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_title);
		tvStatusItem2Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_title);
		tvStatusItem3Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_title);

		tvStatusItem1Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_content);
		tvStatusItem2Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_content);
		tvStatusItem3Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_content);

		tvStatusItem1ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_content_extra);
		tvStatusItem2ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_content_extra);
		tvStatusItem3ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_content_extra);

		tvStatusDescription = (TextView)v.findViewById(R.id.tv_device_detail_status_description);
		tvStatusDescriptionExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_description_extra);
		btnStatusChanged = (Button)v.findViewById(R.id.btn_device_detail_status_change_diaper);
		btnStatusChanged.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).showDateTimeDialog();
				refreshView();
			}
		});

		btnStatusInit = (Button)v.findViewById(R.id.btn_device_detail_status_initialize);
		btnStatusInit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).initDiaperStatus();
				refreshView();
			}
		});

		tvPassedTime = (TextView) v.findViewById(R.id.tv_device_detail_status_passed_time);

		tvWhereConnected = (TextView) v.findViewById(R.id.tv_device_detail_status_where_conn);
		if (Configuration.MASTER) tvWhereConnected.setVisibility(View.VISIBLE);
    }

	private String[] mDebuggingText = new String[100];
	private int mDebuggingTextIdx = 0;
	public void addDebuggingText(String text) {
		mDebuggingText[mDebuggingTextIdx++ % 100] = text;
		if (tvDebugPanel == null || tvDebugPanel.getVisibility() != View.VISIBLE) {
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
		tvDebugPanel.setText(debuggingText);
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

				switch (mMonitSensor.getOperationStatus()) {
					case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
					case DeviceStatus.OPERATION_CABLE_CHARGING:
					case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
					case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
					case DeviceStatus.OPERATION_HUB_CHARGING:
					case DeviceStatus.OPERATION_HUB_NO_CHARGE:
						setBatteryStatus(mMonitSensor.getBatteryPower(), true);
						break;
					default:
						setBatteryStatus(mMonitSensor.getBatteryPower(), false);
						break;
				}

				setDiaperStatus(mMonitSensor.getDiaperStatus());
				setOperationStatus(mMonitSensor.getOperationStatus());
				if (SHOW_MOVEMENT) {
					setMovementStatus(mMonitSensor.getMovementStatus());
				} else {
					setAlarmCountStatus(DatabaseManager.getInstance(mContext).getTodayDiaperAlarmCount(mMonitSensor.deviceId));
				}
				setPassedTimeFromDetection(mMonitSensor.getDiaperDetectedTimeMs());
				if (mMonitSensor.getDiaperStatus() == DeviceStatus.DETECT_NONE) {
					btnStatusInit.setVisibility(View.GONE);
				} else {
					btnStatusInit.setVisibility(View.VISIBLE);
				}

				long whereConnId = mMonitSensor.getConnectionId();	// 1의자리에 Type이 있는 값
				int whereConnType = (int)(mMonitSensor.getConnectionId() % 10);
				if (whereConnId > 0) {
					whereConnId = whereConnId / 10; // Type이외의 값을 ID로 전환
					if (whereConnType == 0) { // 스마트폰에 붙었을 때,
						String nickName = "";
						if (mUserInfoMgr != null) nickName = mUserInfoMgr.getUserNickname(whereConnId);
						tvWhereConnected.setText("BLE Connected with Phone\n" + nickName + "("+ whereConnId + ")");
					} else if (whereConnType == 2) { // 허브에 붙었을 때
						String hubName = "";
						if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
						tvWhereConnected.setText("BLE Connected with Hub\n" + hubName + "("+ whereConnId + ")");
					} else if (whereConnType == 3) { // 허브에 붙었을 때
						String hubName = "";
						if (mPreferenceMgr != null) hubName = mPreferenceMgr.getDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, whereConnId);
						tvWhereConnected.setText("Charging on Hub\n" + hubName + "("+ whereConnId + ")");
					}
				} else {
					tvWhereConnected.setText("");
				}
			} else {
				setConnected(false);
			}
		} catch(IllegalStateException ex){
			if (DBG) Log.e(TAG, "refreshView exception");
		}
	}

    public void setConnected(boolean connected) {
		if (isConnected == connected) return;
		if (isAdded() == false) return;

		isConnected = connected;
		if (connected) {
			rctnStatusBackground.setBackgroundResource(0);
			ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_normal);
			ivStatusIconExtra.setBackgroundResource(0);
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100);

			if (mOperationStatus == DeviceStatus.OPERATION_GAS_DETECTED || mOperationStatus == DeviceStatus.OPERATION_AVOID_SENSING) {
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_analyzing);
			} else if (mOperationStatus == DeviceStatus.OPERATION_IDLE) {
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_idle);
			} else {
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
			}

			if (SHOW_MOVEMENT) {
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
			} else {
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_sensor_alarm_count_activated);
			}

			tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
			tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
			tvStatusItem1Title.setText(getString(R.string.device_sensor_battery_power));
			tvStatusItem2Title.setText(getString(R.string.device_sensor_operation));
			if (SHOW_MOVEMENT) {
				tvStatusItem3Title.setText(getString(R.string.device_sensor_movement));
			} else {
				tvStatusItem3Title.setText(getString(R.string.device_sensor_alarm_count));
			}

			tvStatusItem1Content.setVisibility(View.VISIBLE);
			tvStatusItem2Content.setVisibility(View.VISIBLE);
			tvStatusItem3Content.setVisibility(View.VISIBLE);
			tvStatusItem1ContentExtra.setVisibility(View.VISIBLE);
			tvStatusItem1ContentExtra.setText("%");

			tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
			tvStatusDescriptionExtra.setText("");
			btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
			if (mDiaperStatus == DeviceStatus.DETECT_NONE) {
				btnStatusInit.setVisibility(View.GONE);
			} else {
				btnStatusInit.setVisibility(View.VISIBLE);
			}
		} else {
			rctnStatusBackground.setBackgroundResource(0);
			ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_deactivated);
			ivStatusIconExtra.setBackgroundResource(0);
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_deactivated);
			ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_deactivated);
			if (SHOW_MOVEMENT) {
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
			} else {
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_sensor_alarm_count_deactivated);
			}

			tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));

			tvStatusItem1Content.setVisibility(View.INVISIBLE);
			tvStatusItem2Content.setVisibility(View.INVISIBLE);
			tvStatusItem3Content.setVisibility(View.INVISIBLE);
			tvStatusItem1ContentExtra.setVisibility(View.INVISIBLE);

			tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
			tvStatusDescription.setText(getString(R.string.device_sensor_disconnected_title));
			tvStatusDescriptionExtra.setText(getString(R.string.device_sensor_disconnected_detail));
			tvPassedTime.setVisibility(View.GONE);
			btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			btnStatusInit.setVisibility(View.GONE);
		}
	}

	/* Deprecated */
    public void setSensorValue(CurrentSensorValue value) {
		try {
			setBatteryStatus(value.battery, false);
			setOperationStatus(value.status_operation);
			setMovementStatus(value.status_movement);

			if (value.count_poo_detected > 0) {
				setDiaperStatus(DeviceStatus.DETECT_POO);
			} else if (value.count_abnormal_detected > 0) {
				setDiaperStatus(DeviceStatus.DETECT_ABNORMAL);
			} else if (value.count_pee_detected > 0) {
				setDiaperStatus(DeviceStatus.DETECT_PEE);
			} else {
				setDiaperStatus(DeviceStatus.DETECT_NONE);
			}
		} catch (IllegalStateException ex) {
			if (DBG) Log.e(TAG, "setSensorValue exception");
		}
	}

	public void setBatteryStatus(int batteryPower, boolean isCharging) {
		if (isCharging && batteryPower == 100) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charged);
		} else if (isCharging) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charging);
		} else if (batteryPower == 100) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100);
		} else if (batteryPower >= 90) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_9x);
		} else if (batteryPower >= 80) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_8x);
		} else if (batteryPower >= 70) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_7x);
		} else if (batteryPower >= 60) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_6x);
		} else if (batteryPower >= 50) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_5x);
		} else if (batteryPower >= 40) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_4x);
		} else if (batteryPower >= 30) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_3x);
		} else if (batteryPower >= 20) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_2x);
		} else if (batteryPower > 0) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_1x);
		} else if (batteryPower == 0) {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_0);
		}

		tvStatusItem1Content.setText(batteryPower + "");
	}

	public void setOperationStatus(int operation) {
		mOperationStatus = operation;
		switch(operation) {
			case DeviceStatus.OPERATION_IDLE:
				tvStatusItem2Content.setText(getString(R.string.device_sensor_operation_idle));
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_idle);
				break;
			case DeviceStatus.OPERATION_SENSING:
				tvStatusItem2Content.setText(getString(R.string.device_sensor_operation_sensing));
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				break;
			case DeviceStatus.OPERATION_GAS_DETECTED:
				tvStatusItem2Content.setText(getContext().getString(R.string.device_sensor_operation_analyzing));
				tvStatusDescriptionExtra.setText(getContext().getString(R.string.device_sensor_diaper_status_analyzing));
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_analyzing);
				break;
			case DeviceStatus.OPERATION_AVOID_SENSING:
				tvStatusItem2Content.setText(getContext().getString(R.string.device_sensor_operation_analyzing) + "!");
				tvStatusDescriptionExtra.setText(getContext().getString(R.string.device_sensor_diaper_status_analyzing));
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_analyzing);
				break;
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				tvStatusItem2Content.setText(getString(R.string.device_sensor_operation_charging));
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				setDiaperStatus(DeviceStatus.DETECT_NONE);
				break;
			default:
				break;
		}
	}

	public void setAlarmCountStatus(int count) {
		tvStatusItem3Content.setText(count + "");
	}

    public void setMovementStatus(int movement) {
		mMovementStatus = movement; // 보여주지는 않지만 현재변경된 값은 기억하고 있다가 보여줄때 그값을 보여줌
		//if (mOperationStatus != DeviceStatus.OPERATION_IDLE &&  mOperationStatus != DeviceStatus.OPERATION_SENSING) {
		//	return;
		//}
		Log.d(TAG, "movement : "+movement);

		switch(movement) {
			//case DeviceStatus.MOVEMENT_SLEEP:
			case 0:
			case 1:
			case 2:
				tvStatusItem3Content.setText(getString(R.string.device_sensor_movement_sleeping) + "(" + movement + ")");
				break;
			//case DeviceStatus.MOVEMENT_CRAWL:
			case 3:
			case 4:
			case 5:
			case 6:
				tvStatusItem3Content.setText(getString(R.string.device_sensor_movement_crawling) + "(" + movement + ")");
				break;
			//case DeviceStatus.MOVEMENT_RUN:
			case 7:
			case 8:
			case 9:
			case 10:
				tvStatusItem3Content.setText(getString(R.string.device_sensor_movement_running) + "(" + movement + ")");
				break;
			default:
				break;
		}
		tvStatusItem3Content.setText("" + movement);
	}

	public void setDiaperStatus(int status) {
		mDiaperStatus = status;
		switch(status) {
			case DeviceStatus.DETECT_NONE:
				rctnStatusBackground.setBackgroundResource(0);
				ivStatusIconExtra.setBackgroundResource(0);
				ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_normal);
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusDescription.setText(getString(R.string.device_sensor_diaper_status_no_pee_or_poo));
					if (System.currentTimeMillis() - mMonitSensor.getDiaperChangedButtonPressedTimeMs() < CHANGING_DIAPER_STATUS_SEC * 1000) {
						btnStatusChanged.setText(getString(R.string.btn_diaper_sensor_diaper_changed));
						btnStatusChanged.setTextColor(getResources().getColor(R.color.colorPrimary));
					} else {
						btnStatusChanged.setText(getString(R.string.btn_diaper_sensor_dry_diaper));
						btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextGrey));
					}
				} else {
					tvStatusDescription.setText(getString(R.string.device_sensor_diaper_status_normal_detail));
				}
				tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
				tvStatusDescriptionExtra.setText("");
				tvPassedTime.setVisibility(View.GONE);
				break;
			case DeviceStatus.DETECT_PEE:
				rctnStatusBackground.setBackgroundResource(R.drawable.bg_device_detail_diaper_warning);
				ivStatusIconExtra.setBackgroundResource(R.drawable.ic_device_detail_diaper_pee);
				ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_warning);
				tvStatusDescription.setText(getString(R.string.device_sensor_diaper_status_pee_detail));
				tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				tvStatusDescriptionExtra.setText(getString(R.string.device_sensor_diaper_status_change));
				tvPassedTime.setVisibility(View.VISIBLE);

				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					btnStatusChanged.setText(getString(R.string.btn_diaper_sensor_change_diaper));
					btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}
				break;
			case DeviceStatus.DETECT_POO:
				rctnStatusBackground.setBackgroundResource(R.drawable.bg_device_detail_diaper_warning);
				ivStatusIconExtra.setBackgroundResource(R.drawable.ic_device_detail_diaper_poo);
				ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_warning);
				tvStatusDescription.setText(getString(R.string.device_sensor_diaper_status_poo_detail));
				tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				tvStatusDescriptionExtra.setText(getString(R.string.device_sensor_diaper_status_change));
				tvPassedTime.setVisibility(View.VISIBLE);
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					btnStatusChanged.setText(getString(R.string.btn_diaper_sensor_change_diaper));
					btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}
				break;
			case DeviceStatus.DETECT_ABNORMAL:
				rctnStatusBackground.setBackgroundResource(R.drawable.bg_device_detail_diaper_warning);
				ivStatusIconExtra.setBackgroundResource(R.drawable.ic_device_detail_diaper_abnormal);
				ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_warning);
				tvStatusDescription.setText(getString(R.string.device_sensor_diaper_status_abnormal_detail));
				tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				tvStatusDescriptionExtra.setText(getString(R.string.device_sensor_diaper_status_change));
				tvPassedTime.setVisibility(View.VISIBLE);
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					btnStatusChanged.setText(getString(R.string.btn_diaper_sensor_change_diaper));
					btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}
				break;
			case DeviceStatus.DETECT_FART:
				rctnStatusBackground.setBackgroundResource(R.drawable.bg_device_detail_diaper_warning);
				ivStatusIconExtra.setBackgroundResource(R.drawable.ic_device_detail_diaper_fart);
				ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_warning);
				tvStatusDescription.setText(getString(R.string.device_sensor_diaper_status_fart_detail));
				tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				tvStatusDescriptionExtra.setText(getString(R.string.device_sensor_diaper_status_change));
				tvPassedTime.setVisibility(View.VISIBLE);
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					btnStatusChanged.setText(getString(R.string.btn_diaper_sensor_change_diaper));
					btnStatusChanged.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}
				break;
			default:
				break;
		}

		switch(mOperationStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				rctnStatusBackground.setBackgroundResource(0);
				ivStatusIcon.setBackgroundResource(R.drawable.ic_device_detail_diaper_deactivated);
				break;
			default:
				break;
		}
	}

	public void setPassedTimeFromDetection(long utcTimeMs) {
		if (DBG) Log.d(TAG, "setPassedTimeFromDetection : " + System.currentTimeMillis() + " / " + utcTimeMs + " / " + (System.currentTimeMillis() - utcTimeMs) + " / " + ((System.currentTimeMillis() - utcTimeMs) / 1000));
		int min = (int)((System.currentTimeMillis() - utcTimeMs) / 1000 / 60);
		try {
			tvPassedTime.setText(getString(R.string.device_sensor_detect_passed_time, min));
		} catch (IllegalStateException e) {

		}
	}

	public void setDiaperSensorDeviceInfo(DeviceDiaperSensor sensor) {
		if (DBG) Log.d(TAG, "setDiaperSensorDeviceInfo: " + sensor.toString());
		mMonitSensor = sensor;
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
			mMainActivity.finish();
		} else {
			String firmwareVersion = mMonitSensor.firmwareVersion;
			if (mVersionMgr.supportMovementShowing(firmwareVersion) && Configuration.MASTER) {
				SHOW_MOVEMENT = true;
				//setConnected(!isConnected);
			}
		}

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