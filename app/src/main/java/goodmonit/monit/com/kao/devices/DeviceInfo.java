package goodmonit.monit.com.kao.devices;

import android.content.Context;

import goodmonit.monit.com.kao.managers.DatabaseManager;

public class DeviceInfo {
	public static final String DIAPER_SENSOR_BASE_NAME = "Monit Diaper Sensor";
	public static final String AQMHUB_BASE_NAME = "Monit Hub";
	public static final String LAMP_BASE_NAME = "Monit Nursing Lamp";
	public static final String ELDERLY_DIAPER_SENSOR_BASE_NAME = "Monit Elderly Diaper Sensor";

	public static final String KC_DIAPER_SENSOR_BASE_NAME = "Huggies-Monit Sensor";
	public static final String KC_AQMHUB_BASE_NAME = "Huggies-Monit Hub";

	public int type;
	public long deviceId;
	public long cloudId;
	public String name;
	public String btmacAddress;
	public String advertisingName;
	public String serial;
	public String firmwareVersion;
	public boolean enabledAlarm1;
	public boolean enabledAlarm2;
	public boolean enabledAlarm3;
	public boolean enabledAlarm4;
	public boolean enabledAlarm5;
	public boolean hasBleConnected;
	protected int connectionState;
	protected long connectionId;

	public DeviceInfo() {
		type = 0;
		deviceId = 0;
		cloudId = 0;
		name = "DeviceName";
		btmacAddress = null;
		serial = null;
		firmwareVersion = "1.0.0";
		enabledAlarm1 = true;
		enabledAlarm2 = true;
		enabledAlarm3 = true;
		enabledAlarm4 = true;
		enabledAlarm5 = true;
		connectionState = DeviceConnectionState.DISCONNECTED;
		connectionId = -1;
		hasBleConnected = false;
	}

	public DeviceInfo(long _deviceId, long _cloudId, int _type, String _name, String _btmacAddress, String _serial, String _firmwareVersion, String _advertisingName, boolean _alarm1, boolean _alarm2, boolean _alarm3, boolean _alarm4, boolean _alarm5) {
		deviceId = _deviceId;
		cloudId = _cloudId;
		type = _type;
		name = _name;
		btmacAddress = _btmacAddress;
        serial = _serial;
        firmwareVersion = _firmwareVersion == null ? "1.0.0" : _firmwareVersion;
		enabledAlarm1 = _alarm1;
		enabledAlarm2 = _alarm2;
		enabledAlarm3 = _alarm3;
		enabledAlarm4 = _alarm4;
		enabledAlarm5 = _alarm5;
		connectionState = DeviceConnectionState.DISCONNECTED;
		connectionId = -1;
		advertisingName = _advertisingName;
	}

	public DeviceInfo getDeviceInfo() {
		return this;
	}

	public void setName(String _name) {
		if (_name != null) {
			name = _name;
		}
	}

	public String getName() {
		return name;
	}

	public String getEnc() {
		if (serial == null || serial.length() < 7) {
			return null;
		} else {
			return serial.substring(7);
		}
	}

	public void setAdvertisingName(String _name) {
		if (_name != null) {
			advertisingName = _name;
		}
	}

	public String getAdvertisingName() {
		return advertisingName;
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

	public String toString() {
		return "t:" + type + "/d:" + deviceId + "/c:" + cloudId + "/n:" + name + "/m:" + btmacAddress + "/s:" + serial +  "/con:" + connectionState + " / " + enabledAlarm1 + " / " + enabledAlarm2 + " / " + enabledAlarm3 + " / " + enabledAlarm4 +" / " + enabledAlarm5;
	}
}
