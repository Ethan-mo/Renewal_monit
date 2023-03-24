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

public class DeviceStatusRowDiaperSensor extends DeviceStatusRowWidget {
	private static final String TAG = Configuration.BASE_TAG + "DiaperRow";
	private static final boolean DBG = Configuration.DBG;

	private static final boolean SHOW_BATTERY = true;

	private boolean isConnected = false;
	private int mDeviceConnectionType = DeviceConnectionState.DISCONNECTED;

	private int mStatus = DeviceStatus.OPERATION_IDLE;
	private int mMovement = DeviceStatus.MOVEMENT_NO_MOVEMENT;
	private int mDetectedStatus = DeviceStatus.DETECT_NONE;

	public DeviceStatusRowDiaperSensor(Context context) {
		super(context);
		_setView();
	}

	public DeviceStatusRowDiaperSensor(Context context, AttributeSet attrs) {
		super(context, attrs);
		_setView();
	}

	public DeviceStatusRowDiaperSensor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_setView();
	}

	private void _setView() {
		isConnected = true;
		setConnected(false);
		setOperationStatus(DeviceStatus.OPERATION_SENSING);
		if (SHOW_BATTERY) {
			setBatteryStatus(100, false);
		} else {
			setMovementStatus(DeviceStatus.MOVEMENT_NO_MOVEMENT);
		}
		setDiaperStatus(DeviceStatus.DETECT_NONE);

		tvTitle.setText(getContext().getString(R.string.device_type_diaper_sensor));
		tvStatus1Text.setText(getContext().getString(R.string.device_sensor_operation));
		tvStatus2Text.setText(getContext().getString(R.string.device_sensor_diaper_status));
		if (!SHOW_BATTERY) {
			tvStatus3Text.setText(getContext().getString(R.string.device_sensor_movement));
		}
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
			ivStatus2New.setVisibility(View.VISIBLE);
		} else {
			ivStatus2New.setVisibility(View.GONE);
		}
	}

	public void setConnected(boolean connected) {
		if (isConnected == connected) return;

		if (DBG) Log.d(TAG, "setConnected : " + connected);
		isConnected = connected;
		if (isConnected) {
			ivConnectionType.setVisibility(View.VISIBLE);
			lctnDeviceStatusDashboard.setVisibility(View.VISIBLE);
			tvDescription.setVisibility(View.GONE);
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor_connected);
			if (Configuration.CERTIFICATE_MODE) {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
			} else {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				if (SHOW_BATTERY) {
					setBatteryStatus(100, false);
				} else {
					ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
				}
			}
			btnReconnect.setVisibility(View.GONE);
		} else {
			ivConnectionType.setVisibility(View.GONE);
			setConnectionType(DeviceConnectionState.DISCONNECTED);
			tvTitle.setText(getContext().getString(R.string.device_sensing_diaper));
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor_disconnected);
			lctnDeviceStatusDashboard.setVisibility(View.GONE);
			tvDescription.setVisibility(View.VISIBLE);
			btnReconnect.setVisibility(View.VISIBLE);
			ivNewMark.setVisibility(View.GONE);
			ivStatus1New.setVisibility(View.GONE);
			ivStatus2New.setVisibility(View.GONE);
			ivStatus3New.setVisibility(View.GONE);
		}
	}

	public void setBatteryStatus(int batteryPower, boolean isCharging) {
		if (isCharging && batteryPower == 100) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charged);
		} else if (isCharging) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charging);
		} else if (batteryPower >= 96) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100);
		} else if (batteryPower >= 90) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_9x);
		} else if (batteryPower >= 80) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_8x);
		} else if (batteryPower >= 70) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_7x);
		} else if (batteryPower >= 60) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_6x);
		} else if (batteryPower >= 50) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_5x);
		} else if (batteryPower >= 40) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_4x);
		} else if (batteryPower >= 30) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_3x);
		} else if (batteryPower >= 20) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_2x);
		} else if (batteryPower > 0) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_1x);
		} else if (batteryPower == 0) {
			ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_0);
		}

		tvStatus3Content.setText(batteryPower + "%");
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setOperationStatus(int status) {
		mStatus = status;
		switch (status) {
			case DeviceStatus.OPERATION_IDLE:
				tvStatus1Content.setText(getContext().getString(R.string.device_sensor_operation_idle));
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_operation_idle);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				setDiaperStatus(mDetectedStatus);
                if (!SHOW_BATTERY) {
                    ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
                    setMovementStatus(mMovement);
                }
				break;
			case DeviceStatus.OPERATION_GAS_DETECTED:
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_operation_analyzing);
				tvStatus1Content.setText(getContext().getString(R.string.device_sensor_operation_analyzing));
				break;
			case DeviceStatus.OPERATION_AVOID_SENSING:
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_operation_analyzing);
				tvStatus1Content.setText(getContext().getString(R.string.device_sensor_operation_analyzing) + "!");
				break;
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				tvStatus1Content.setText(getContext().getString(R.string.device_sensor_operation_charging));
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_deactivated);
				tvStatus2Content.setText("");
                if (!SHOW_BATTERY) {
                    ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
                    tvStatus3Content.setText("");
                }
				break;
			case DeviceStatus.OPERATION_SENSING:
				tvStatus1Content.setText(getContext().getString(R.string.device_sensor_operation_sensing));
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_operation_activated);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				setDiaperStatus(mDetectedStatus);
                if (!SHOW_BATTERY) {
                    setMovementStatus(mMovement);
                    ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
                }
				break;
		}
	}

	public void setMovementStatus(int movement) {
		mMovement = movement;

		if (SHOW_BATTERY) {
			return;
		}
        if (mStatus >= DeviceStatus.OPERATION_CABLE_NO_CHARGE && mStatus < DeviceStatus.OPERATION_DEBUG_NO_CHARGE) {
			return;
		}

		tvStatus3Content.setText(DeviceStatus.getMovementStringResource(movement));
	}

	public void setDiaperStatus(int detectedStatus) {

		mDetectedStatus = detectedStatus;

		if (mStatus >= DeviceStatus.OPERATION_CABLE_NO_CHARGE && mStatus < DeviceStatus.OPERATION_DEBUG_NO_CHARGE) {
			return;
		}
		switch (detectedStatus) {
			case DeviceStatus.DETECT_NONE:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated);
				tvStatus2Content.setText(getContext().getString(R.string.device_sensor_diaper_status_normal));
				break;
			case DeviceStatus.DETECT_PEE:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_warning_pee);
				tvStatus2Content.setText(getContext().getString(R.string.device_sensor_diaper_status_pee));
				break;
			case DeviceStatus.DETECT_POO:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_warning_poo);
				tvStatus2Content.setText(getContext().getString(R.string.device_sensor_diaper_status_poo));
				break;
			case DeviceStatus.DETECT_ABNORMAL:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_warning_abnormal);
				tvStatus2Content.setText(getContext().getString(R.string.device_sensor_diaper_status_abnormal));
				break;
			case DeviceStatus.DETECT_FART:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_warning_fart);
				tvStatus2Content.setText(getContext().getString(R.string.device_sensor_diaper_status_fart));
				break;
		}
	}
}