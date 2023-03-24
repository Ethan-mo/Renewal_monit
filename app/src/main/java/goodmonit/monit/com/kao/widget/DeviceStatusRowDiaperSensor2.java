package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class DeviceStatusRowDiaperSensor2 extends DeviceStatusRowWidget {
	private static final String TAG = Configuration.BASE_TAG + "DiaperRow2";
	private static final boolean DBG = Configuration.DBG;

	private boolean isConnected = false;
	private int mDeviceConnectionType = DeviceConnectionState.DISCONNECTED;

	private int mStatus = DeviceStatus.OPERATION_IDLE;
	private int mMovement = DeviceStatus.MOVEMENT_NO_MOVEMENT;
	private int mDetectedStatus = DeviceStatus.DETECT_NONE;
	private int mAnimationIndex = 1;
	private int sleepStatus = -1;

	public DeviceStatusRowDiaperSensor2(Context context) {
		super(context);
		_setView();
	}

	public DeviceStatusRowDiaperSensor2(Context context, AttributeSet attrs) {
		super(context, attrs);
		_setView();
	}

	public DeviceStatusRowDiaperSensor2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_setView();
	}

	private void _setView() {
		isConnected = true;
		setConnected(false);
		setOperationStatus(DeviceStatus.OPERATION_SENSING);
		setBatteryStatus(100, false);
		setMovementStatus(DeviceStatus.MOVEMENT_NO_MOVEMENT, false);
		setDiaperStatus(DeviceStatus.DETECT_NONE);

		tvTitle.setText(getContext().getString(R.string.device_type_diaper_sensor));
		tvStatus1Text.setText("");
		tvStatus2Text.setText("");
		tvStatus3Text.setText("");

		tvStatus1Content.setText(getContext().getString(R.string.device_sensor_diaper_status));
		tvStatus2Content.setText(getContext().getString(R.string.device_sensor_voc));
		tvStatus3Content.setText(getContext().getString(R.string.device_sensor_movement));

		tvDescription.setText(getContext().getString(R.string.device_sensor_disconnected));
		btnReconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDeviceId > 0) {
					DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mDeviceId, DeviceType.DIAPER_SENSOR);
					if (bleConnection != null) {
						bleConnection.requestForceLeScan();
						bleConnection.connect();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								btnReconnect.setText(mContext.getString(R.string.btn_connect));
							}
						}, 3000);
						btnReconnect.setText(mContext.getString(R.string.btn_connecting));
					} else {
						if (DBG) Log.e(TAG, "bleConnection NULL");
					}
				}
			}
		});
	}

	public void setTemperature(float temperature) {
		tvStatus1Content.setText(temperature + "℃");
	}

	public void setHumidity(float humidity) {
		tvStatus2Content.setText(humidity + "%");
	}

	public void setVoc(float voc) {
		tvStatus3Content.setText(voc + "");
	}

	public void setConnectionType(int deviceConnectionState) {
		if (mDeviceConnectionType != deviceConnectionState) {
			mDeviceConnectionType = deviceConnectionState;
			switch (deviceConnectionState) {
				case DeviceConnectionState.DISCONNECTED:
					ivConnectionType.setBackgroundResource(0);
					break;
				case DeviceConnectionState.WIFI_CONNECTED:
					ivConnectionType.setBackgroundResource(R.drawable.device_connection_wifi);
					break;
				case DeviceConnectionState.BLE_CONNECTED:
					ivConnectionType.setBackgroundResource(R.drawable.device_connection_bluetooth);
					break;
			}
		}
	}

	public void showNewMark(boolean show) {
		if (show) {
			ivNewMark.setVisibility(View.VISIBLE);
		} else {
			ivNewMark.setVisibility(View.GONE);
		}
	}

	public void showAlarmMark(boolean show) {
		if (show) {
			ivStatus1New.setVisibility(View.VISIBLE);
		} else {
			ivStatus1New.setVisibility(View.GONE);
		}
	}

	public void setConnected(boolean connected) {
		if (isConnected == connected) return;

		if (DBG) Log.d(TAG, "setConnected : " + connected);
		isConnected = connected;
		if (isConnected) {
			ivConnectionType.setVisibility(View.VISIBLE);
			lctnDeviceStatusDashboard.setVisibility(View.VISIBLE);
            lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
			tvDescription.setVisibility(View.GONE);
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor_connected);
			if (Configuration.CERTIFICATE_MODE) {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
			} else {
				ivStatus1Icon.setBackgroundResource(0);
				ivStatus2Icon.setBackgroundResource(0);
				ivStatus3Icon.setBackgroundResource(0);
			}
			setBatteryStatus(100, false);
			btnReconnect.setVisibility(View.GONE);
		} else {
			ivConnectionType.setVisibility(View.GONE);
			setConnectionType(DeviceConnectionState.DISCONNECTED);
			tvTitle.setText(getContext().getString(R.string.device_sensing_diaper));
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor_disconnected);
			lctnDeviceStatusDashboard.setVisibility(View.GONE);
            lctnDeviceBatteryPower.setVisibility(View.GONE);
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvDescription.setText(mContext.getString(R.string.device_sensor_disconnected));
			btnReconnect.setVisibility(View.VISIBLE);
			ivNewMark.setVisibility(View.GONE);
			ivStatus1New.setVisibility(View.GONE);
			ivStatus2New.setVisibility(View.GONE);
			ivStatus3New.setVisibility(View.GONE);
			lctnDeviceBatteryPower.setVisibility(View.GONE);
			ivBatteryChargingPower.setVisibility(View.GONE);
			tvBatteryChargingPower.setVisibility(View.GONE);
		}
	}

	public void setBatteryStatus(int batteryPower, boolean isCharging) {
        if (isCharging) {
            if (batteryPower == 100) {
                tvDescription.setText(mContext.getString(R.string.device_sensor_diaper_status_charged_detail));
                ivBatteryChargingPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charged_row);
            } else {
                tvDescription.setText(mContext.getString(R.string.device_sensor_diaper_status_charging_detail));
                ivBatteryChargingPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charging_row);
            }
            tvBatteryChargingPower.setText(batteryPower + "%");
			lctnDeviceBatteryPower.setVisibility(View.GONE);
        } else {
        	if (isConnected) {
				lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
			}
            tvBatteryPower.setText(batteryPower + "");
            if (batteryPower >= 96) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100_row);
            } else if (batteryPower >= 90) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_9x_row);
            } else if (batteryPower >= 80) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_8x_row);
            } else if (batteryPower >= 70) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_7x_row);
            } else if (batteryPower >= 60) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_6x_row);
            } else if (batteryPower >= 50) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_5x_row);
            } else if (batteryPower >= 40) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_4x_row);
            } else if (batteryPower >= 30) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_3x_row);
            } else if (batteryPower >= 20) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_2x_row);
            } else if (batteryPower > 0) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_1x_row);
            } else if (batteryPower == 0) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_0_row);
            }
        }
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setOperationStatus(int status) {
		mStatus = status;

		if (isConnected) {
			switch (status) {
				case DeviceStatus.OPERATION_IDLE:
					lctnDeviceStatusDashboard.setVisibility(View.GONE);
					lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
					ivBatteryChargingPower.setVisibility(View.GONE);
					tvBatteryChargingPower.setVisibility(View.GONE);
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					tvDescription.setText(R.string.device_sensor_diaper_status_idle_detail);
					break;
				case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
				case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
				case DeviceStatus.OPERATION_CABLE_CHARGING:
				case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				case DeviceStatus.OPERATION_HUB_CHARGING:
					lctnDeviceStatusDashboard.setVisibility(View.GONE);
					lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
					ivBatteryChargingPower.setVisibility(View.VISIBLE);
					tvBatteryChargingPower.setVisibility(View.VISIBLE);
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					break;
				case DeviceStatus.OPERATION_GAS_DETECTED:
				case DeviceStatus.OPERATION_AVOID_SENSING:
				case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
				case DeviceStatus.OPERATION_DEBUG_CHARGING:
				case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
				case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
				case DeviceStatus.OPERATION_SENSING:
					lctnDeviceStatusDashboard.setVisibility(View.VISIBLE);
					lctnDeviceBatteryPower.setVisibility(View.GONE);
					ivBatteryChargingPower.setVisibility(View.GONE);
					tvBatteryChargingPower.setVisibility(View.GONE);
					tvDescription.setVisibility(View.GONE);
					break;
			}
		}
	}

	public void setVocStatus(float vocValue) {
		if (DBG) Log.d(TAG, "setVocStatus: " + vocValue);
		switch (mStatus) {
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_voc_deactivated);
				tvStatus2Content.setText("-");
				break;
			case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
			case DeviceStatus.OPERATION_DEBUG_CHARGING:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
			case DeviceStatus.OPERATION_GAS_DETECTED:
			case DeviceStatus.OPERATION_AVOID_SENSING:
			case DeviceStatus.OPERATION_SENSING:
			case DeviceStatus.OPERATION_IDLE:
				ivStatus2Icon.setBackgroundResource(DeviceStatus.getDiaperSensorVocIconResource(vocValue));
				tvStatus2Content.setText(getResources().getString(DeviceStatus.getDiaperSensorVocStringResource(vocValue)));
				tvStatus2Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperSensorVocStringColorResource(vocValue)));
				break;
		}
	}

	public void setMovementStatus(final int movement, final boolean isSleeping) {
		mMovement = movement;

		switch (mStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
				tvStatus3Content.setText("-");
				break;
			case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
			case DeviceStatus.OPERATION_DEBUG_CHARGING:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
			case DeviceStatus.OPERATION_GAS_DETECTED:
			case DeviceStatus.OPERATION_AVOID_SENSING:
			case DeviceStatus.OPERATION_SENSING:
			case DeviceStatus.OPERATION_IDLE:
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
				tvStatus3Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
				if (isSleeping) {
					tvStatus3Content.setText(getContext().getString(DeviceStatus.getMovementStringResource(DeviceStatus.MOVEMENT_SLEEP)));
				} else {
					tvStatus3Content.setText(getContext().getString(DeviceStatus.getMovementStringResource(movement)));
				}
				break;

		}
	}

	public void setSleepStatus(int status) {
		sleepStatus = status;

		tvTitle.setText(getContext().getString(R.string.device_type_diaper_sensor) + "(" + sleepStatus +")");
	}

	public void setDiaperScore(int score) {
		switch (mStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
				tvStatus1Content.setText("-");
				break;
			case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
			case DeviceStatus.OPERATION_DEBUG_CHARGING:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
			case DeviceStatus.OPERATION_GAS_DETECTED:
			case DeviceStatus.OPERATION_AVOID_SENSING:
			case DeviceStatus.OPERATION_SENSING:
			case DeviceStatus.OPERATION_IDLE:
				ivStatus1Icon.setBackgroundResource(DeviceStatus.getDiaperScoreIconResource(score));
				tvStatus1Content.setText(mContext.getString(DeviceStatus.getDiaperScoreStringResource(score)));
				tvStatus1Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperScoreColorResource(score)));
				break;
		}
	}

	public void setDiaperStatus(int detectedStatus) {
		mDetectedStatus = detectedStatus;
		switch (mStatus) {
			case DeviceStatus.OPERATION_IDLE:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				return;
		}
		switch (detectedStatus) {
			case DeviceStatus.DETECT_ABNORMAL:
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated_phase2);
				tvStatus1Content.setText(mContext.getString(DeviceStatus.getDiaperScoreStringResource(0)));
				break;
		}
	}
}