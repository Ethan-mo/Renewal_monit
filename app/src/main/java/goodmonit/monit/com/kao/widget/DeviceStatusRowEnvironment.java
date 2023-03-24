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

public class DeviceStatusRowEnvironment extends DeviceStatusRowWidget {
	private static final String TAG = Configuration.BASE_TAG + "Environment";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;

	private boolean isConnected = false;
	private int mDeviceConnectionType = DeviceConnectionState.DISCONNECTED;

	private boolean isSensorAttached = false;

	private boolean isTemperatureWarning = false;
	private boolean isHumidityWarning = false;
	private boolean isVocWarning = false;

	private int mScore = 90;

	public DeviceStatusRowEnvironment(Context context) {
		super(context);
		mContext = context;
		_setView();
	}

	public DeviceStatusRowEnvironment(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_setView();
	}

	public DeviceStatusRowEnvironment(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		_setView();
	}

	private void _setView() {
		//setConnected(false);
		//setTemperature((float)23.5);
		//setHumidity((float)45);
		//setVocStatus(getContext().getString(R.string.device_environment_voc_good));

		tvTitle.setText(getContext().getString(R.string.device_type_hub));
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			tvStatus1Text.setText(getContext().getString(R.string.device_environment_temperature));
			tvStatus2Text.setText(getContext().getString(R.string.device_environment_humidity));
			tvStatus3Text.setText(getContext().getString(R.string.device_environment_voc));
			tvStatus1Content.setText("");
			tvStatus2Content.setText("");
			tvStatus3Content.setText("");
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_kc);
		} else {
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
		}
		ivDeviceIcon.setText("");
		tvDescription.setText("");
		ivConnectionType.setBackgroundResource(R.drawable.device_connection_wifi);

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
			// 밝기 설정시 변경가능
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_kc);
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
			} else {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
				//ivStatus1Icon.setBackgroundResource(0);
				//ivStatus2Icon.setBackgroundResource(0);
				//ivStatus3Icon.setBackgroundResource(0);
				//tvStatus1Text.setText("");
				//tvStatus2Text.setText("");
				//tvStatus3Text.setText("");
			}
			//setEnvironmentScore(mScore);

			setSensorAttached(isSensorAttached);
		} else {
			ivConnectionType.setVisibility(View.GONE);
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_disconnected);
			ivDeviceIconBackground.setBackgroundResource(0);
			//ivDeviceIcon.setText("");
			//ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_deactivated);

			lctnDeviceStatusDashboard.setVisibility(View.GONE);
			ivNewMark.setVisibility(View.GONE);
			ivStatus1New.setVisibility(View.GONE);
			ivStatus2New.setVisibility(View.GONE);
			ivStatus3New.setVisibility(View.GONE);
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setText(getContext().getString(R.string.device_hub_disconnected));
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

	public void setSensorAttached(boolean attached) {
		isSensorAttached = attached;
		if (isSensorAttached) {
			ivStatus3Icon.setBackgroundResource(0);
			tvStatus3Text.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
			tvStatus3Text.setText("");
			lctnDeviceStatusItem3.setVisibility(View.VISIBLE);
		} else {
			ivStatus3Icon.setBackgroundResource(0);
			tvStatus3Text.setTextColor(getResources().getColor(R.color.colorTextDeviceDisconnected));
			tvStatus3Text.setText("-");
			lctnDeviceStatusItem3.setVisibility(View.GONE);
			//tvStatus3Content.setText("");
		}
	}

	/*
	public void setEnvironmentScore(int score) {
		if (!isConnected) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_deactivated);
			return;
		}
		mScore = score;
		if (score == 100) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_100);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
		} else if (score >= 90) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_9x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
		} else if (score >= 80) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_8x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
		} else if (score >= 70) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_7x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
		} else if (score >= 60) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_6x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
		} else if (score >= 50) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_5x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
		} else if (score >= 40) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_4x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
		} else if (score >= 30) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_3x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
		} else if (score >= 20) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_2x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
		} else if (score >= 10) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_1x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
		} else if (score >= 0) {
			ivDeviceIcon.setBackgroundResource(R.drawable.bg_environment_score_x);
			ivDeviceIcon.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
		}
		ivDeviceIcon.setText(score + "");
	}
	*/

	public void setTemperature(float celsius) {
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			if (mPreferenceMgr.getTemperatureScale().equals(mContext.getString(R.string.unit_temperature_fahrenheit))) {
				tvStatus1Content.setText(UnitConvertUtil.getFahrenheitFromCelsius(celsius) + mPreferenceMgr.getTemperatureScale());
			} else {
				tvStatus1Content.setText(celsius + mPreferenceMgr.getTemperatureScale());
			}
		} else {
			if (mPreferenceMgr.getTemperatureScale().equals(mContext.getString(R.string.unit_temperature_fahrenheit))) {
				tvStatus1Text.setText(UnitConvertUtil.getFahrenheitFromCelsius(celsius) + "");
			} else {
				tvStatus1Text.setText(celsius + "");
			}
		}
	}

	public void setHumidity(float humidity) {
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			tvStatus2Content.setText(humidity + "%");
		} else {
			tvStatus2Text.setText(humidity + "");
		}
	}

	public void setVocStatus(String status) {
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			return;
		} else {
			//tvStatus3Content.setText(status);
			tvStatus3Text.setText(status);
		}
	}

	public void setTemperatureWarning(int warning) {
		if (warning == EnvironmentCheckManager.NORMAL) {
			isTemperatureWarning = false;
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated_kc);
				tvStatus1Content.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
			} else {
				//ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				//tvStatus1Content.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
				tvStatus1Text.setTextColor(getContext().getResources().getColor(R.color.colorTextEnvironmentCategory));
			}
		} else if (warning == EnvironmentCheckManager.HIGH) {
			isTemperatureWarning = true;
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning_high_kc);
				tvStatus1Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
			} else {
				//ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning);
				//tvStatus1Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
				tvStatus1Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
			}
		} else {
			isTemperatureWarning = true;
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning_low_kc);
				tvStatus1Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarningBlue));
			} else {
				//ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning);
				//tvStatus1Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
				tvStatus1Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
			}
		}
	}

	public void setHumidityWarning(int warning) {
		if (warning == EnvironmentCheckManager.NORMAL) {
			isHumidityWarning = false;
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated_kc);
				tvStatus2Content.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
			} else {
				//ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
				//tvStatus2Content.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
				tvStatus2Text.setTextColor(getContext().getResources().getColor(R.color.colorTextEnvironmentCategory));
			}
		} else if (warning == EnvironmentCheckManager.HIGH) {
			isHumidityWarning = true;
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning_high_kc);
				tvStatus2Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarningOrange));
			} else {
				//ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning);
				//tvStatus2Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
				tvStatus2Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
			}
		} else {
			isHumidityWarning = true;
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning_low_kc);
				tvStatus2Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarningOrange));
			} else {
				//ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning);
				//tvStatus2Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
				tvStatus2Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
			}
		}
	}

	public void setVocWarning(boolean warning) {
		isVocWarning = warning;
		if (warning) {
			//ivStatus3Icon.setBackgroundResource(R.drawable.ic_environment_voc_warning);
			//tvStatus3Content.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
			tvStatus3Text.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
		} else {
			//ivStatus3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
			//tvStatus3Content.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
			tvStatus3Text.setTextColor(getContext().getResources().getColor(R.color.colorTextEnvironmentCategory));
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
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_kc);
			} else {
				ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
			}
		} else {
			if (brightLevel == DeviceStatus.BRIGHT_OFF) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected);
				}
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON5) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
				}
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON4) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
				}
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON3) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
				}
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON2) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
				}
			} else if (brightLevel >= DeviceStatus.BRIGHT_ON1) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
				}
			} else {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on_kc);
				} else {
					ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub_connected_lamp_on);
				}
			}
		}
	}
}