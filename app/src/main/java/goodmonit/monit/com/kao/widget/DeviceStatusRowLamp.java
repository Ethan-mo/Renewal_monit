package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.util.UnitConvertUtil;

public class DeviceStatusRowLamp extends DeviceStatusRowWidget {
	private static final String TAG = Configuration.BASE_TAG + "LampStatus";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;

	private boolean isConnected = false;
	private int mDeviceConnectionType = DeviceConnectionState.DISCONNECTED;

	private boolean isSensorAttached = false;

	private boolean isTemperatureWarning = false;
	private boolean isHumidityWarning = false;
	private boolean isVocWarning = false;

	private int mScore = 90;

	public DeviceStatusRowLamp(Context context) {
		super(context);
		mContext = context;
		_setView();
	}

	public DeviceStatusRowLamp(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_setView();
	}

	public DeviceStatusRowLamp(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		_setView();
	}

	private void _setView() {
		tvTitle.setText(getContext().getString(R.string.device_type_lamp));

		tvStatus1Content.setText(getContext().getString(R.string.device_environment_temperature));
		tvStatus2Content.setText(getContext().getString(R.string.device_environment_humidity));
		tvStatus3Content.setText(getContext().getString(R.string.device_environment_voc));
		tvStatus1Text.setVisibility(View.VISIBLE);
		tvStatus2Text.setVisibility(View.VISIBLE);
		tvStatus3Text.setVisibility(View.VISIBLE);
		tvStatus1Text.setText("");
		tvStatus2Text.setText("");
		tvStatus3Text.setText("");

		tvStatus1TextUnit.setText(mPreferenceMgr.getTemperatureScale());
		tvStatus2TextUnit.setText("%");
		tvStatus1TextUnit.setVisibility(View.VISIBLE);
		tvStatus2TextUnit.setVisibility(View.VISIBLE);
		ivStatus1Icon.setBackgroundResource(0);
		ivStatus2Icon.setBackgroundResource(0);
		ivStatus3Icon.setBackgroundResource(0);
		ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
		ivDeviceIcon.setText("");
		tvDescription.setText("");
		ivConnectionType.setBackgroundResource(R.drawable.device_connection_wifi);

		ivStatus3Icon.setBackgroundResource(0);
		tvStatus3Text.setTextColor(getResources().getColor(R.color.colorTextDeviceDisconnected));
		tvStatus3Text.setText("-");
		lctnDeviceStatusItem3.setVisibility(View.GONE);

		isConnected = true;
		setConnected(false);
	}

	public void setConnected(boolean connected) {
		if (isConnected == connected) return;

		if (DBG) Log.d(TAG, "setConnected : " + connected);
		isConnected = connected;
		if (isConnected) {
			ivConnectionType.setVisibility(View.VISIBLE);
			lctnDeviceStatusDashboard.setVisibility(View.VISIBLE);
			tvDescription.setVisibility(View.GONE);

			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
			//ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
			//ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
		} else {
			ivConnectionType.setVisibility(View.GONE);
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_disconnected);
			ivDeviceIconBackground.setBackgroundResource(0);

			lctnDeviceStatusDashboard.setVisibility(View.GONE);
			ivNewMark.setVisibility(View.GONE);
			ivStatus1New.setVisibility(View.GONE);
			ivStatus2New.setVisibility(View.GONE);
			ivStatus3New.setVisibility(View.GONE);
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setText(getContext().getString(R.string.device_lamp_disconnected));
		}
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

	public boolean isConnected() {
		return isConnected;
	}

	public void setTemperature(float celsius) {
		if (mPreferenceMgr.getTemperatureScale().equals(mContext.getString(R.string.unit_temperature_fahrenheit))) {
			tvStatus1Text.setText(UnitConvertUtil.getFahrenheitFromCelsius(celsius) + "");
		} else {
			tvStatus1Text.setText(celsius + "");
		}
	}

	public void setHumidity(float humidity) {
		tvStatus2Text.setText(humidity + "");
	}

	public void setTemperatureWarning(int warning) {
		if (warning == EnvironmentCheckManager.NORMAL) {
			isTemperatureWarning = false;
			tvStatus1Text.setTextColor(getContext().getResources().getColor(R.color.colorTextEnvironmentCategory));
		} else if (warning == EnvironmentCheckManager.HIGH) {
			isTemperatureWarning = true;
			tvStatus1Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
		} else {
			isTemperatureWarning = true;
			tvStatus1Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
		}
	}

	public void setHumidityWarning(int warning) {
		if (warning == EnvironmentCheckManager.NORMAL) {
			isHumidityWarning = false;
			tvStatus2Text.setTextColor(getContext().getResources().getColor(R.color.colorTextEnvironmentCategory));
		} else if (warning == EnvironmentCheckManager.HIGH) {
			isHumidityWarning = true;
			tvStatus2Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
		} else {
			isHumidityWarning = true;
			tvStatus2Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
		}
	}

	public void showTemperatureAlarmMark(boolean show) {
		if (show) {
			ivStatus1New.setVisibility(View.VISIBLE);
		} else {
			ivStatus1New.setVisibility(View.GONE);
		}
	}
	public void showHumidityAlarmMark(boolean show) {
		if (show) {
			ivStatus2New.setVisibility(View.VISIBLE);
		} else {
			ivStatus2New.setVisibility(View.GONE);
		}
	}
	public void showVocAlarmMark(boolean show) {
		if (show) {
			ivStatus3New.setVisibility(View.VISIBLE);
		} else {
			ivStatus3New.setVisibility(View.GONE);
		}
	}

	public void showNewMark(boolean show) {
		if (show) {
			ivNewMark.setVisibility(View.VISIBLE);
		} else {
			ivNewMark.setVisibility(View.GONE);
		}
	}

	public void setBrightLevel(int power, int brightLevel) {
		if (power == DeviceStatus.LAMP_POWER_OFF) {
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
		} else {
			if (brightLevel == DeviceStatus.BRIGHT_OFF) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON5) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON4) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON3) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON2) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON1) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
			} else {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
			}
		}
	}
}