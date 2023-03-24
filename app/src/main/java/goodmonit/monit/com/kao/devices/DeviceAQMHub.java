package goodmonit.monit.com.kao.devices;

import android.content.Context;
import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.DatabaseManager;

public class DeviceAQMHub extends DeviceInfo {
	private static final String TAG = Configuration.BASE_TAG + "AQMHub";
	private static final boolean DBG = Configuration.DBG;

	private EnvironmentCheckManager mEnvironmentManager;
	private Context mContext;

	private int lampPower;
	private int brightLevel;
	private int colorTemperature;

	private int sensorAttached;

	private float currentTemperature;
	private float currentHumidity;
	private float currentVoc;

	private String apName;
	private int apSecurity;

	private String ledOnTime_HHmm;
	private String ledOffTime_HHmm;

	private float maxTemperature;
	private float minTemperature;
	private float maxHumidity;
	private float minHumidity;

	private int betaStatus;
	private long remoteControlledUtcTimeMs;

	public DeviceAQMHub(Context context) {
		super();
		mContext = context;
		type = DeviceType.AIR_QUALITY_MONITORING_HUB;
		switch (Configuration.APP_MODE) {
			case Configuration.APP_GLOBAL:
			//case Configuration.APP_MONIT_X_HUGGIES:
			case Configuration.APP_MONIT_X_KAO:
				name = AQMHUB_BASE_NAME;
				break;
			case Configuration.APP_KC_HUGGIES_X_MONIT:
				name = KC_AQMHUB_BASE_NAME;
				break;
		}

		deviceId = 0;
		cloudId = 0;

		brightLevel = DeviceStatus.BRIGHT_OFF;
		lampPower = DeviceStatus.LAMP_POWER_OFF;
		colorTemperature = 0;

		currentTemperature = -1;
		currentHumidity = -1;
		currentVoc = -1;

		sensorAttached = 0;
		apName = null;
		apSecurity = -1;

		ledOnTime_HHmm = "0000";
		ledOffTime_HHmm = "0000";

		maxTemperature = 30;
		minTemperature = 20;

		maxHumidity = 60;
		minHumidity = 40;

		betaStatus = DeviceStatus.BETA_DISABLED;

		_setEnvironmentManager();
	}

	public DeviceAQMHub(Context context, DeviceInfo info) {
		super(info.deviceId, info.cloudId, info.type, info.name, info.btmacAddress, info.serial, info.firmwareVersion, info.advertisingName, info.enabledAlarm1, info.enabledAlarm2, info.enabledAlarm3, info.enabledAlarm4, info.enabledAlarm5);
		mContext = context;
		type = DeviceType.AIR_QUALITY_MONITORING_HUB;
		if (name == null) {
			switch (Configuration.APP_MODE) {
				case Configuration.APP_GLOBAL:
				//case Configuration.APP_MONIT_X_HUGGIES:
				case Configuration.APP_MONIT_X_KAO:
					name = AQMHUB_BASE_NAME;
					break;
				case Configuration.APP_KC_HUGGIES_X_MONIT:
					name = KC_AQMHUB_BASE_NAME;
					break;
			}
		}

		brightLevel = DeviceStatus.BRIGHT_OFF;
		lampPower = DeviceStatus.LAMP_POWER_OFF;
		colorTemperature = 0;

		currentTemperature = -1;
		currentHumidity = -1;
		currentVoc = -1;

		sensorAttached = 0;
		apName = null;
		apSecurity = -1;

		ledOnTime_HHmm = "0000";
		ledOffTime_HHmm = "0000";

		maxTemperature = 30;
		minTemperature = 20;

		maxHumidity = 60;
		minHumidity = 40;

		betaStatus = DeviceStatus.BETA_DISABLED;
		_setEnvironmentManager();
	}

	private void _setEnvironmentManager() {
		if (mEnvironmentManager == null) {
			mEnvironmentManager = new EnvironmentCheckManager(mContext);
		}
		mEnvironmentManager.setTemperatureThreshold(minTemperature, maxTemperature);
		mEnvironmentManager.setHumidityThreshold(minHumidity, maxHumidity);
	}

	public void setBetaStatus(int value) {
		if (value != -1) {
			betaStatus = value;
		}
	}

	public int getBetaStatus() {
		return betaStatus;
	}

	public void setMaxTemperature(float value) {
		if (value != -1) {
			if (value > 100) {
				maxTemperature = (int)(value / 10) / (float) 10.0;
			} else {
				maxTemperature = (int)(value * 10) / (float) 10.0;
			}

			mEnvironmentManager.setTemperatureThreshold(minTemperature, maxTemperature);
		}
	}

	public float getMaxTemperature() {
		return maxTemperature;
	}

	public void setMinTemperature(float value) {
		if (value != -1) {
			if (value > 100) {
				minTemperature = (int)(value / 10) / (float) 10.0;
			} else {
				minTemperature = (int)(value * 10) / (float) 10.0;
			}
			mEnvironmentManager.setTemperatureThreshold(minTemperature, maxTemperature);
		}
	}

	public float getMinTemperature() {
		return minTemperature;
	}

	public void setMaxHumidity(float value) {
		if (value != -1) {
			if (value > 100) {
				maxHumidity = (int)(value / 10) / (float) 10.0;
			} else {
				maxHumidity = (int)(value * 10) / (float) 10.0;
			}
			mEnvironmentManager.setHumidityThreshold(minHumidity, maxHumidity);
		}
	}

	public int getMaxHumidity() {
		return (int)maxHumidity;
	}

	public void setMinHumidity(float value) {
		if (value != -1) {
			if (value > 100) {
				minHumidity = (int)(value / 10) / (float) 10.0;
			} else {
				minHumidity = (int)(value * 10) / (float) 10.0;
			}
			mEnvironmentManager.setHumidityThreshold(minHumidity, maxHumidity);
		}
	}

	public int getMinHumidity() {
		return (int)minHumidity;
	}

	public void setLedOnTime(String HHmm) {
		if (HHmm != null) {
			ledOnTime_HHmm = HHmm;
		}
	}

	public String getLedOnTime() {
		return ledOnTime_HHmm;
	}

	public void setLedOffTime(String HHmm) {
		if (HHmm != null) {
			ledOffTime_HHmm = HHmm;
		}
	}

	public String getLedOffTime() {
		return ledOffTime_HHmm;
	}

	public void setApSecurity(int value) {
		if (value != -2) {
			apSecurity = value;
		}
	}

	public int getApSecurity() {
		return apSecurity;
	}

	public void setApName(String value) {
		if (value != null) {
			apName = value;
		}
	}

	public String getApName() {
		return apName;
	}

	public void setSensorAttached(int value) {
		if (value != -1) {
			sensorAttached = value;
		}
	}

	public int getSensorAttached() {
		return sensorAttached;
	}

	public void setConnectionState(int state) {
		connectionState = state;
	}

	public int getConnectionState() {
		return connectionState;
	}

	public float getTemperature() {
		return currentTemperature;
	}

	public float getHumidity() {
		return currentHumidity;
	}

	public float getVoc() {
		return currentVoc;
	}

	public void setTemperature(float value) {
		if (value != -1) {
			currentTemperature = (int)(value / 10) / (float)10.0;
			mEnvironmentManager.setTemperature(currentTemperature);
		}
	}

	public void setHumidity(float value) {
		if (value != -1) {
			currentHumidity = (int)(value / 10) / (float)10.0;
			mEnvironmentManager.setHumidity(currentHumidity);
		}
	}

	public void setVoc(float value) {
		if (value != -1) {
			currentVoc = (int)(value / 10) / (float)10.0;
			mEnvironmentManager.setVoc(currentVoc);
		}
	}

	public int getTemperatureStatus() {
		return mEnvironmentManager.getTemperatureStatus();
	}

	public int getHumidityStatus() {
		return mEnvironmentManager.getHumidityStatus();
	}

	public int getVocStatus() {
		return mEnvironmentManager.getVocStatus();
	}

	public int getScore() {
		return mEnvironmentManager.getScore();
	}

	public String getScoreDescription() {
		return mEnvironmentManager.getScoreDescription();
	}

	// 밝기를 앱에서 직접 설정한 경우
	public void setBrightLevelRemoteFromApp(int value) {
		if (value != -1) {
			remoteControlledUtcTimeMs = System.currentTimeMillis();
			brightLevel = value;
		}
	}

	public void setBrightLevel(int value) {
		if (value != -1) {
			if (remoteControlledUtcTimeMs > 0 && System.currentTimeMillis() - remoteControlledUtcTimeMs < 1000) {
				if (DBG) Log.e(TAG, "duplicated bright level setting before: " + brightLevel + ", set: " + value);
				// 앱에서 직접 1단계로 밝기를 설정하고 SetDeviceStatus가 서버로 전송되는 동시에,
				// GetDeviceStatus 패킷 응답으로 꺼짐을 받게되면,
				// 수유등은 실제로 켜지지만, 앱에서는 수유등UI 밝기가 1단계로 설정 되었다가 곧바로 꺼지는 현상 발생
				// Sync를 맞추기 위해, 앱에서 직접 밝기를 설정하는 경우, 1초 이내로 서버에서 응답받는 허브 밝기는 무시
			} else {
				brightLevel = value;
			}
		}
	}

	public int getBrightLevel() {
		return brightLevel;
	}

	public void setLampPower(int power) {
		lampPower = power;
	}

	public int getLampPower() {
		return lampPower;
	}

	public void setColorTemperature(int value) {
		if (value != -1) {
			colorTemperature = value;
		}
	}

	public int getColorTemperature() {
		return colorTemperature;
	}

	public int insertDB(Context c) {
		if (c == null) return -1;
		return DatabaseManager.getInstance(c).insertDB(this);
	}

	public int updateDB(Context c) {
		if (c == null) return -1;
		return DatabaseManager.getInstance(c).updateDB(this);
	}

	public int deleteDB(Context c) {
		if (c == null) return -1;
		return DatabaseManager.getInstance(c).deleteDB(this);
	}
}