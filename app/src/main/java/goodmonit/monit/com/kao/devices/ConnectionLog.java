package goodmonit.monit.com.kao.devices;

public class ConnectionLog {
	public boolean isManualConnection;
	public boolean connectionSucceeded;
	public String scannedDeviceName;
	public int scannedDeviceCount;
	public int connected;
	public int bleStep;
	public String GetDeviceIdEcd;
	public long deviceId;
	public String GetCloudIdEcd;
	public String SetCloudIdEcd;
	public String startConnResp;

	public ConnectionLog() {
		initialize();
	}

	public void initialize() {
		isManualConnection = false;
		connectionSucceeded = false;
		scannedDeviceName = null;
		scannedDeviceCount = -1;
		connected = -1;
		bleStep = -1;
		GetDeviceIdEcd = "-1";
		deviceId = -1;
		GetCloudIdEcd = "-1";
		SetCloudIdEcd = "-1";
		startConnResp = "1";
	}

	public void addScannedDevice(String name) {
		scannedDeviceName += name + "/";
	}

	public String toString() {
		return scannedDeviceName + "," + scannedDeviceCount + "," + connected + "," + bleStep + "," + GetDeviceIdEcd + "," + deviceId  + "," + GetCloudIdEcd + "," + SetCloudIdEcd + "," + startConnResp;
	}
}