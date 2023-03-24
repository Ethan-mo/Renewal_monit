package goodmonit.monit.com.kao.devices;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import goodmonit.monit.com.kao.constants.BlePacketType;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.managers.DebugManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class DeviceBLEConnection {
	private static final String TAG = Configuration.BASE_TAG + "BleConn";
	private static final boolean DBG = Configuration.DBG;

	private static final long KEEP_ALIVE_PERIOD_SEC		 	= 10;

	protected static final int MSG_CONNECTION_STATE_CHANGED   	= 1;
	protected static final int MSG_ESTABLISH_LOGICAL_CONNECTION	= 7;
	protected static final int MSG_START_AUTO_POLLING			= 8;

	private boolean isConnected = false;
	private boolean isServerDirectConnected = false;
	protected PreferenceManager mPreferenceMgr;
	protected DebugManager mDebugMgr;
	protected GattManager mGattMgr;
	protected BlePacketManager mPacketMgr;

	protected DeviceInfo mDeviceInfo;
	protected DeviceInfo mHubDeviceInfo;
	protected Context mContext;

	protected VersionManager mVersionMgr;

	private DiaperStatusInfo mDiaperStatusInfo;

	protected int currBleInitStep;
	protected boolean isManualConnect = false;
	protected int mConnectionState;
	protected Handler mConnectionManagerHandler;
	protected boolean isRemoved = false;

	// GATT에 AutoConnection = true 로 연결하게되면, Bluetooth On 후 Scan이 한번이라도 되어 해당 Device를 찾았을때 자동으로 붙임
	// 불가피하게 연결이 끊어지는 경우가 발생할 수 있으며, bluetooth 강제 off 등이 발생하면 초기화 해주어야함
	public boolean isLeScanFound = false;

	public int mDisconnectedReason = -1;

	private static final int MAX_COUNT_GATHER_SENSING_DATA = 100;
	private static final int MAX_COUNT_FLUSH_SENSING_DATA = 30;
	private int[] mTemperatureList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mHumidityList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mVocList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mAccelerationList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mSensorStatusList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mMovementLevelList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mEthanolList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mCo2List = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mPressureList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mCompGasList = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh1 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh2 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh3 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh4 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh5 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh6 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh7 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh8 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int[] mTouchCh9 = new int[MAX_COUNT_GATHER_SENSING_DATA];
	private int mGatherSensingDataIndex = 0;
	private int mGatherElderlySensingDataIndex = -1;

	private ServerQueryManager mServerQueryMgr;
	private CurrentSensorLog mCurrentSensorLog;

	public DeviceBLEConnection(Context context, DeviceInfo info, Handler upperLayerHandler, boolean startConnect) {
		_init(context, info.deviceId, info.cloudId, info.type, info.name, info.btmacAddress, info.serial, info.firmwareVersion, info.advertisingName, info.enabledAlarm1, info.enabledAlarm2, info.enabledAlarm3, info.enabledAlarm4, info.enabledAlarm5);
		mConnectionManagerHandler = upperLayerHandler;

		connect();
	}

	private void _init(Context context, long deviceId, long cloudId, int deviceType, String deviceName, String deviceMacAddr, String serial, String firmware, String advertisingName, boolean alarm1, boolean alarm2, boolean alarm3, boolean alarm4, boolean alarm5) {
		mDeviceInfo = new DeviceInfo(deviceId, cloudId, deviceType, deviceName, deviceMacAddr, serial, firmware, advertisingName, alarm1, alarm2, alarm3, alarm4, alarm5);
		mContext = context;
		mConnectionState = DeviceConnectionState.DISCONNECTED;
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mDebugMgr = DebugManager.getInstance(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mPacketMgr = BlePacketManager.getInstance();
		mVersionMgr = new VersionManager(mContext);
		mGattMgr = new GattManager(mContext, mDeviceInfo, mHandler);
		mDiaperStatusInfo = new DiaperStatusInfo();
		mCurrentSensorLog = new CurrentSensorLog();
	}

	public DeviceInfo getDeviceInfo() {
		return mDeviceInfo;
	}

	public DeviceInfo getHubDeviceInfo() {
		return mHubDeviceInfo;
	}

	public void manualConnect() {
		if (DBG) Log.d(TAG, "ManualConnect");

		if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("MANUAL CONN");
		isManualConnect = true;
		isLeScanFound = true;
		connect();
	}

	public boolean isManuallyConnected() {
		return isManualConnect;
	}

	public void setManuallyConnected(boolean manuallyConnected) {
		isManualConnect = manuallyConnected;
	}

	public void requestForceLeScan() {
		mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_START_SCAN_FOR_RECONNECT, 1, 0).sendToTarget();
	}

	public void connect() {
		if (isLeScanFound == false) { // 자동연결시에는 의미 없음
			if (DBG) Log.e(TAG, "NOT FOUND YET");
			if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("NOT FOUND");
			return;
		}

		if (mGattMgr == null) {
			if (DBG) Log.e(TAG, "GattMgr NULL");
			if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("GattMgr NULL");
			return;
		}

		// 원래 ServiceDiscovered 에서 Timeout 메시지를 보냈었는데,
		// GattCallBack이 불리지 않는 경우가 있음, ManualConnect시에 Timeout자체를 못받음
		mHandler.removeMessages(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
		if (isManualConnect) {
			mHandler.sendEmptyMessageDelayed(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT, ConnectionManager.TIME_BLE_MANUAL_CONNECTION_TIME_OUT_SEC * 1000);
		}

		mGattMgr.connectManually();
	}

	public int getConnectionState() {
		return mConnectionState;
	}

	public void setServerDirectConnectionState(boolean serverDirectConnected) {
		isServerDirectConnected = serverDirectConnected;
	}

	public boolean getServerDirectConnectionState() {
		return isServerDirectConnected;
	}

	public void setConnectionState(int connectionState) {
		if (DBG) Log.d(TAG, "setConnectionState : " + connectionState);
		mConnectionState = connectionState;
	}

	public void disconnect() {
		if (mGattMgr != null) {
			mGattMgr.disconnect();
			isLeScanFound = false;
			_setConnected(false); // Gatt Disconnected Callback을 타지 않을 가능성때문에 직접 설정
		} else {
			if (DBG) Log.e(TAG, "Gatt NULL");
		}
	}

	public void close() {
		if (mGattMgr != null) {
			mGattMgr.disconnect();
			mGattMgr.close();
			isLeScanFound = false;
			_setConnected(false); // Gatt Disconnected Callback을 타지 않을 가능성때문에 직접 설정
		} else {
			if (DBG) Log.e(TAG, "Gatt NULL");
		}
	}

	public void write(byte[] data) {
		if (mGattMgr != null) {
			mGattMgr.write(data);
		}
	}

	public void directConnect() {
		int bluetoothStatus = ConnectionManager.checkBluetoothStatus();
		if (bluetoothStatus != ConnectionManager.STATE_DISABLED && bluetoothStatus != ConnectionManager.STATE_UNAVAILABLE) {
			if (mConnectionState == DeviceConnectionState.DISCONNECTED) {
				if (DBG) Log.i(TAG, "disconnected => connect[" + mDeviceInfo.deviceId + "] " + mDeviceInfo.name + " / " + mDeviceInfo.btmacAddress + " / " + mConnectionState);
			} else if (mConnectionState == DeviceConnectionState.WIFI_CONNECTED) {
				//if (mDeviceViewObject == null) {
				//	mDeviceViewObject = ConnectionManager.getDeviceDiaperSensor(getDeviceInfo().deviceId);
				//}
				//if ((mDeviceViewObject != null) && (mDeviceViewObject.getOperationStatus() >= DeviceStatus.OPERATION_HUB_NO_CHARGE)) { // Wi-Fi 연결일때는 허브에 충전중일때만 붙이는 상황
				if (DBG) Log.i(TAG, "wificonnected => connect[" + mDeviceInfo.deviceId + "] " + mDeviceInfo.name + " / " + mDeviceInfo.btmacAddress + " / " + mConnectionState);
				//}
			}
			connect();
		} else {
			if (DBG) Log.i(TAG, "bluetoothStatus DISABLED");
		}
	}

	public void checkBLEConnection() {
		if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR || mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
			if (mGattMgr != null) mGattMgr.checkBleConnection(30);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (DBG) Log.d(TAG, "handleMessage : " + msg.what);
			switch(msg.what) {
				case MSG_START_AUTO_POLLING:
					removeMessages(MSG_START_AUTO_POLLING);
					//sendAutoPollingEnabled(true);
					break;

				case ConnectionManager.MSG_BLE_GATT_CONNECTION_ERROR:
					if (DBG) Log.d(TAG, "MSG_BLE_GATT_CONNECTION_ERROR");
					if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("GATT CONN ERR");
					close();
					break;

				case ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT:
					if (DBG) Log.d(TAG, "ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT : " + isManualConnect + " / " + currBleInitStep);
					if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("MANUAL CONN T/O : " + isManualConnect + " / " + currBleInitStep);
					if (isManualConnect) {
						if (mConnectionManagerHandler != null) {
							mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT, DeviceConnectionState.DISCONNECTED, currBleInitStep , mDeviceInfo).sendToTarget();
						} else {
							if (DBG) Log.e(TAG, "BLE_CONNECTION_TIME_OUT NOT SENT");
						}
						close();
						isManualConnect = false;
					}
					break;

				case GattManager.MSG_GATT_BLE_RECEIVED_DATA:
					removeMessages(GattManager.MSG_GATT_BLE_RECEIVED_DATA);

					byte[] data = (byte[])msg.obj;
					if (data == null) break;

					if (mPacketMgr.isLongPacket(data)) { // Long Packet
						_analyzeData(data);
					} else { // Short Packet
						if (data.length > 4) { // chunk byte[] every 4 bytes
							int length = 0;
							byte[] chunkedData = new byte[4];
							while (length < data.length) {
								chunkedData[0] = data[length];
								chunkedData[1] = data[length + 1];
								chunkedData[2] = data[length + 2];
								chunkedData[3] = data[length + 3];
								length += 4;
								_analyzeData(chunkedData);
							}
						} else {
							_analyzeData(data);
						}
					}
					break;

				case MSG_CONNECTION_STATE_CHANGED:
					int state = msg.arg1;
					if (DBG) Log.d(TAG, "MSG_CONNECTION_STATE_CHANGED: " + state);
					_sendConnectionStateMessage(state);
					break;

				case GattManager.MSG_GATT_BLE_CONNECTION_STATE_CHANGE:
					int connectionState = msg.arg1;
					if (connectionState == DeviceConnectionState.BLE_CONNECTED) {
						switch(mDeviceInfo.type) {
							case DeviceType.DIAPER_SENSOR:
								if (mDiaperStatusInfo == null) {
									mDiaperStatusInfo = new DiaperStatusInfo();
								} else {
									mDiaperStatusInfo.initDiaperStatusInfo();
								}
								getSensorDeviceInfo();
								break;
							case DeviceType.LAMP:
								getLampDeviceInfo();
								break;
							case DeviceType.ELDERLY_DIAPER_SENSOR:
								if (mDiaperStatusInfo == null) {
									mDiaperStatusInfo = new DiaperStatusInfo();
								} else {
									mDiaperStatusInfo.initDiaperStatusInfo();
								}
								getElderlySensorDeviceInfo();
								break;
						}
					} else {
						_flushSensingData();
						mDisconnectedReason = msg.arg2;
						isLeScanFound = false;
					}
					if (DBG) Log.d(TAG, "MSG_GATT_BLE_CONNECTION_STATE_CHANGE: " + connectionState + " / " + mDisconnectedReason);
					setConnectionState(connectionState);
					_sendConnectionStateMessage(connectionState);
					break;
			}
		}
	};

	public void sendDiaperStatusDetectedTime(int detectionType) {
		if (DBG) Log.d(TAG, "sendDiaperStatusChangedTime: " + detectionType);
		int[] packets = new int[1];
		switch(detectionType) {
			case DeviceStatus.DETECT_PEE:
				packets[0] = BlePacketType.LATEST_PEE_DETECTION_TIME;
				mGattMgr.write(mPacketMgr.getRequestPacket(packets));
				break;
			case DeviceStatus.DETECT_POO:
				packets[0] = BlePacketType.LATEST_POO_DETECTION_TIME;
				mGattMgr.write(mPacketMgr.getRequestPacket(packets));
				break;
			case DeviceStatus.DETECT_ABNORMAL:
				packets[0] = BlePacketType.LATEST_ABNORMAL_DETECTION_TIME;
				mGattMgr.write(mPacketMgr.getRequestPacket(packets));
				break;
			case DeviceStatus.DETECT_FART:
				packets[0] = BlePacketType.LATEST_FART_DETECTION_TIME;
				mGattMgr.write(mPacketMgr.getRequestPacket(packets));
				break;
			case DeviceStatus.DETECT_DIAPER_DETACHED:
				packets[0] = BlePacketType.LATEST_DETACHMENT_DETECTION_TIME;
				mGattMgr.write(mPacketMgr.getRequestPacket(packets));
				break;
			case DeviceStatus.DETECT_DIAPER_ATTACHED:
				packets[0] = BlePacketType.LATEST_ATTACHMENT_DETECTION_TIME;
				mGattMgr.write(mPacketMgr.getRequestPacket(packets));
				break;
		}
	}

	public void sendDebugCommand(String command) {
		if (DBG) Log.d(TAG, "debugCommand: " + command);
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.DEBUG_COMMAND, command));
	}

	public void blink() {
		if (DBG) Log.d(TAG, "blink");
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.LED_CONTROL));
	}

	public void enterDFUMode() {
		if (DBG) Log.d(TAG, "enter dfu");
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.ENTER_DFU, 1));
	}

	public void startWifiScan() {
		if (DBG) Log.d(TAG, "start wifi scan");
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.HUB_WIFI_SCAN));
	}

	public void setSensitivity(int sensitivity) {
		if (DBG) Log.d(TAG, "setSensitivity: " + sensitivity);
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.SENSITIVITY, sensitivity));
	}

	public void setLampBrightControl(int bright) {
		if (DBG) Log.d(TAG, "setLampBrightControl: " + bright);
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.LAMP_BRIGHT_CTRL, bright));
	}

	public void setLampPowerControl(int power) {
		if (DBG) Log.d(TAG, "setLampPowerControl: " + power);
		if (power == DeviceStatus.LAMP_POWER_OFF) { // 전원 끄기
			mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.LAMP_BRIGHT_CTRL, 10001));
		} else { // 전원 켜기
			mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.LAMP_BRIGHT_CTRL, 10002));
		}
	}

	public void sendAutoPollingEnabled(boolean enabled) {
		if (DBG) Log.d(TAG, "sendAutoPollingEnabled: " + enabled);

		if (mVersionMgr.revisedConnectionFalseNegativeAlert(mDeviceInfo.firmwareVersion)) {
			mGattMgr.write(mPacketMgr.getAutoPollingPacket(enabled, new int[]{BlePacketType.TEMPERATURE, BlePacketType.HUMIDITY, BlePacketType.VOC, BlePacketType.TOUCH, BlePacketType.ACCELERATION, BlePacketType.ETHANOL, BlePacketType.CO2, BlePacketType.PRESSURE, BlePacketType.DIAPER_STATUS_COUNT, BlePacketType.COMPENSATED_GAS}));
		} else {
			mGattMgr.write(mPacketMgr.getAutoPollingPacket(enabled, new int[]{BlePacketType.TEMPERATURE, BlePacketType.HUMIDITY, BlePacketType.VOC, BlePacketType.TOUCH, BlePacketType.ACCELERATION, BlePacketType.ETHANOL, BlePacketType.CO2, BlePacketType.PRESSURE, BlePacketType.RAW_GAS, BlePacketType.COMPENSATED_GAS}));
		}
	}

	byte[] mNameBytes;
	byte[] mApNameBytes;
	byte[] mApPasswordBytes;

	int tempBatttery;
	float tempTemperature;
	float tempHumidity;
	float tempVoc;
	float tempTouch;
	float tempAcceleration;
	float tempZAxis;
	float tempEthanol;
	float tempCo2;
	float tempPressure;
	float tempRawGas;
	float tempCompGas;
	int tempStrapBattery;
	float[] tempMultiTouch = new float[9];

	String tempBabyBirthdayYYMMDD;
	int tempBabyEating;
	int tempBabySex;
	int tempSensitivity;
	int tempOperationStatus;
	int tempMovementStatus;
	int[] tempSensorStatus;

	private void _analyzeData(byte[] data) {
		if (data == null) return;
		byte type = data[0];

		switch (type) {
			case BlePacketType.DEVICE_ID:
				mDeviceInfo.deviceId = mPacketMgr.getDeviceId(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "DEVICE_ID : " + mDeviceInfo.deviceId);
				if (ConnectionManager.mConnectionLog.isManualConnection) {
					ConnectionManager.mConnectionLog.bleStep = 1;
				}
				currBleInitStep = 1;
				if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step1");
				break;
			case BlePacketType.CLOUD_ID:
				mDeviceInfo.cloudId = mPacketMgr.getCloudId(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "CLOUD_ID : " + mDeviceInfo.cloudId);
				break;
			case BlePacketType.FIRMWARE_VERSION:
				mDeviceInfo.firmwareVersion = mPacketMgr.getFirmwareVersion(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "FIRMWARE_VERSION : " + mDeviceInfo.firmwareVersion);

				if (mVersionMgr.supportDiaperSensitivity(mDeviceInfo.firmwareVersion) && mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
					if (DBG) Log.i(TAG, "req sensitivity");
					mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.SENSITIVITY}));
				}
				break;
			case BlePacketType.HUB_WIFI_SCAN:
				HubApInfo tempWifiScanAPInfo = mPacketMgr.getHubApInfo(data);
				if (mConnectionManagerHandler != null) {
					mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_HUB_WIFI_SCAN_LIST, tempWifiScanAPInfo).sendToTarget();
				}
				break;
			case BlePacketType.DIAPER_PENDING_INFO:
				if (mVersionMgr.revisedConnectionFalseNegativeAlert(mDeviceInfo.firmwareVersion) == false) {
					int diaperStatusPendingInfo = data[1];
					int diaperStatusPendingSec = ((0xFF & data[3]) << 8) | 0xFF & data[2];
					long nowUtcMs = System.currentTimeMillis();
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "DIAPER_PENDING_INFO : dps: " + diaperStatusPendingInfo + ", dtime(sec): " + diaperStatusPendingSec + ", now(sec): " + (nowUtcMs / 1000));

					if (diaperStatusPendingInfo != DeviceStatus.DETECT_NONE) {
						_updatePendingDiaperStatus(diaperStatusPendingInfo, nowUtcMs, diaperStatusPendingSec);
					}
				}
				break;
			case BlePacketType.SENSOR_STATUS:
				if (ConnectionManager.mConnectionLog.isManualConnection) {
					ConnectionManager.mConnectionLog.bleStep = 4;
				}
				currBleInitStep = 4;

				tempSensorStatus = mPacketMgr.getSensorStatus(data);

				// 움직임 확인(tempSensorStatus[0])
				if (tempMovementStatus != tempSensorStatus[0]) {
					if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "SENSOR_STATUS changed movement : " + tempMovementStatus + "->" + tempSensorStatus[0]);
					tempMovementStatus = tempSensorStatus[0];
					mServerQueryMgr.setDeviceMovementStatus(mDeviceInfo.type,
							mDeviceInfo.deviceId,
							mDeviceInfo.getEnc(),
							tempMovementStatus,
							null);
				}

				// 기저귀 상태 확인(tempSensorStatus[1])
				if (mVersionMgr.revisedConnectionFalseNegativeAlert(mDeviceInfo.firmwareVersion) == false) {
					if (tempSensorStatus[1] != DeviceStatus.DETECT_NONE) {
						_updateDiaperStatus(tempSensorStatus[1], System.currentTimeMillis());
					}
				}
				/*
				switch(tempSensorStatus[2])
				{
					case DeviceStatus.OPERATION_HUB_CHARGING + 4:
					case DeviceStatus.OPERATION_HUB_CHARGING + 8:
					case DeviceStatus.OPERATION_HUB_CHARGING + 12:
						tempSensorStatus[2] = DeviceStatus.OPERATION_HUB_CHARGING;
						break;
					case DeviceStatus.OPERATION_HUB_CHARGED_FULLY + 4:
					case DeviceStatus.OPERATION_HUB_CHARGED_FULLY + 8:
					case DeviceStatus.OPERATION_HUB_CHARGED_FULLY + 12:
						tempSensorStatus[2] = DeviceStatus.OPERATION_HUB_CHARGED_FULLY;
						break;
					case DeviceStatus.OPERATION_CABLE_CHARGING + 4:
					case DeviceStatus.OPERATION_CABLE_CHARGING + 8:
					case DeviceStatus.OPERATION_CABLE_CHARGING + 12:
						tempSensorStatus[2] = DeviceStatus.OPERATION_CABLE_CHARGING;
						break;
					case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY + 4:
					case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY + 8:
					case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY + 12:
						tempSensorStatus[2] = DeviceStatus.OPERATION_CABLE_CHARGED_FULLY;
						break;
				}
				*/

				// 센서상태 확인(tempSensorStatus[2])
				// [0000]00[00]
				switch ((tempSensorStatus[2] >> 4)) {
					case 1: // 케이블
						switch (tempSensorStatus[2] & 3) {
							case 1: // 충전중
								tempSensorStatus[2] = DeviceStatus.OPERATION_CABLE_CHARGING;
								break;
							case 2: // 완충
								tempSensorStatus[2] = DeviceStatus.OPERATION_CABLE_CHARGED_FULLY;
								break;
							default: // 기타
								tempSensorStatus[2] = DeviceStatus.OPERATION_CABLE_NO_CHARGE;
								break;
						}
						break;
					case 2: // 허브
						switch (tempSensorStatus[2] & 3) {
							case 1: // 충전중
								tempSensorStatus[2] = DeviceStatus.OPERATION_HUB_CHARGING;
								break;
							case 2: // 완충
								tempSensorStatus[2] = DeviceStatus.OPERATION_HUB_CHARGED_FULLY;
								break;
							default: // 기타
								tempSensorStatus[2] = DeviceStatus.OPERATION_HUB_NO_CHARGE;
								break;
						}
						break;
					case 4: // 스트랩
						if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + " STRAP Connected: " + tempSensorStatus[2]);
						//tempSensorStatus[2] = DeviceStatus.OPERATION_STRAP_CONNECTED;
						break;
					case 3: // 디버깅
					default:
						switch ((tempSensorStatus[2] >> 2) & 3) {
							case 1: // 4 -> IDLE
								tempSensorStatus[2] = DeviceStatus.OPERATION_IDLE;
								break;
							case 2: // 8 -> Analyzing
								tempSensorStatus[2] = DeviceStatus.OPERATION_GAS_DETECTED;
								break;
							case 3: // 12 -> Peak found
								tempSensorStatus[2] = DeviceStatus.OPERATION_AVOID_SENSING;
								break;
							case 0:
							default:
								tempSensorStatus[2] = DeviceStatus.OPERATION_SENSING;
								break;
						}
						break;
				}

				if (tempOperationStatus != tempSensorStatus[2]) {
					if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "SENSOR_STATUS changed operation : " + tempOperationStatus + "->" + tempSensorStatus[2]);
					tempOperationStatus = tempSensorStatus[2];
					mServerQueryMgr.setDeviceOperationStatus(mDeviceInfo.type,
							mDeviceInfo.deviceId,
							mDeviceInfo.getEnc(),
							tempOperationStatus,
							null);
				}

				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "SENSOR_STATUS : " + tempMovementStatus + " / " + "(" + tempSensorStatus[1] + ")" + " / " + tempOperationStatus);
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step4 : " + tempMovementStatus + " / " + tempDiaperStatus + " / " + tempOperationStatus);
				break;

			case BlePacketType.BATTERY:
				tempBatttery = mPacketMgr.getBatteryValue(data);
				if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
					if (mDeviceViewObject == null) {
						mDeviceViewObject = ConnectionManager.getDeviceDiaperSensor(mDeviceInfo.deviceId);
					}
					if ((mDeviceViewObject != null)
							&& (mDeviceViewObject.getBatteryPower() != tempBatttery)
							&& (getConnectionState() == DeviceConnectionState.BLE_CONNECTED)) {
						if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "BATTERY changed value : " + mDeviceViewObject.getBatteryPower() + "->" + tempBatttery);
						mServerQueryMgr.setDeviceBatteryPower(mDeviceInfo.type,
								mDeviceInfo.deviceId,
								mDeviceInfo.getEnc(),
								tempBatttery,
								null);
					}
				} else if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
					if (mDeviceViewObjectElderlyDiaperSensor == null) {
						mDeviceViewObjectElderlyDiaperSensor = ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId);
					}
					if ((mDeviceViewObjectElderlyDiaperSensor != null)
							&& (mDeviceViewObjectElderlyDiaperSensor.getBatteryPower() != tempBatttery)
							&& (getConnectionState() == DeviceConnectionState.BLE_CONNECTED)) {
						if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "BATTERY changed value : " + mDeviceViewObjectElderlyDiaperSensor.getBatteryPower() + "->" + tempBatttery);
						mServerQueryMgr.setDeviceBatteryPower(mDeviceInfo.type,
								mDeviceInfo.deviceId,
								mDeviceInfo.getEnc(),
								tempBatttery,
								null);
					}
				}

				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "BATTERY : " + tempBatttery);
				break;

			case BlePacketType.TEMPERATURE:
				tempTemperature = mPacketMgr.getTemperatureValue(data);
				if (mDeviceInfo.type == DeviceType.LAMP && new VersionManager(mContext).supportUpdatedLampMonitoring(mDeviceInfo.firmwareVersion)) {
					// 수유등 1.1.0 미만 버전에서는 25.5도일때, BLE 255000 -> getTemperatureValue 2550 으로 값 가져옴
					// 수유등 1.1.0 이상 버전에서는 25.5도일때, BLE 2550 -> getTemperatureValue 25.5 으로 값 가져옴
					// 따라서 서버에 업로드하기 위해 2550으로 만들필요 있음
					tempTemperature = tempTemperature * 100;
				}
				if (DBG) Log.i(TAG, "TEMPERATURE : " + tempTemperature);
				if (mDeviceInfo.type == DeviceType.LAMP) {
					// 아무것도 안함
				} else {
					mTemperatureList[mGatherSensingDataIndex] = (int) (tempTemperature * 100);
				}
				break;
			case BlePacketType.HUMIDITY:
				tempHumidity = mPacketMgr.getHumidityValue(data);
				if (mDeviceInfo.type == DeviceType.LAMP && new VersionManager(mContext).supportUpdatedLampMonitoring(mDeviceInfo.firmwareVersion)) {
					tempHumidity = tempHumidity * 100;
				}
				if (DBG) Log.i(TAG, "HUMIDITY : " + tempHumidity);
				if (mDeviceInfo.type == DeviceType.LAMP) {
					// 10초에 한번 BLE로 들어오는 온도, 습도 값 전달
					_updateLampMonitoringStatus(tempTemperature, tempHumidity);
				} else {
					mHumidityList[mGatherSensingDataIndex] = (int) (tempHumidity * 100);
				}
				break;
			case BlePacketType.VOC:
				tempVoc = mPacketMgr.getVocValue(data);
				//if (DBG) Log.i(TAG, "VOC : " + tempVoc);
				mMovementLevelList[mGatherSensingDataIndex] = tempMovementStatus;
				mSensorStatusList[mGatherSensingDataIndex] = tempOperationStatus;
				mVocList[mGatherSensingDataIndex] = (int)(tempVoc * 100);
				break;
			case BlePacketType.TOUCH:
				tempTouch = mPacketMgr.getTouchValue(data);
				mTouchList[mGatherSensingDataIndex] = (int)(tempTouch);
				break;
			case BlePacketType.ACCELERATION:
				tempAcceleration = mPacketMgr.getAccelerationValue(data);
				tempZAxis = mPacketMgr.getZaxisFromAccelerationValue(data);
				mAccelerationList[mGatherSensingDataIndex] = mPacketMgr.getUnsignedIntegerValue(data);
				break;
			case BlePacketType.SENSITIVITY:
				tempSensitivity = mPacketMgr.getSensitivity(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "SENSITIVITY : " + tempSensitivity);
				break;
			case BlePacketType.BABY_INFO:
				String tempYYMMDD = mPacketMgr.getBabyBirthday(data);
				tempBabyBirthdayYYMMDD = tempYYMMDD;
				tempBabySex = mPacketMgr.getBabySex(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "BABY_INFO : " + tempBabyBirthdayYYMMDD + " / " + tempBabySex);
				break;
			case BlePacketType.ETHANOL:
				tempEthanol = mPacketMgr.getEthanolValue(data);
				mEthanolList[mGatherSensingDataIndex] = (int)tempEthanol;

				if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
					if (mGatherElderlySensingDataIndex + 1 < mGatherSensingDataIndex) {
						if (mDeviceViewObjectElderlyDiaperSensor != null) {
							mDeviceViewObjectElderlyDiaperSensor.setStrapAttached(false);
						}
					}
				}
				_setSensingData();
				break;
			case BlePacketType.CO2:
				tempCo2 = mPacketMgr.getCo2Value(data);
				mCo2List[mGatherSensingDataIndex] = (int)tempCo2;
				break;
			case BlePacketType.PRESSURE:
				tempPressure = mPacketMgr.getPressureValue(data);
				mPressureList[mGatherSensingDataIndex] = (int)tempPressure;
				break;
			case BlePacketType.RAW_GAS:
				tempRawGas = mPacketMgr.getRawGasValue(data);
				break;
			case BlePacketType.COMPENSATED_GAS:
				tempCompGas = mPacketMgr.getCompensatedGasValue(data);
				mCompGasList[mGatherSensingDataIndex] = (int)tempCompGas;
				break;
			case BlePacketType.ELDERLY_STRAP_CAPACITANCE_STATUS:
				if (mDeviceViewObjectElderlyDiaperSensor == null) {
					mDeviceViewObjectElderlyDiaperSensor = ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId);
				}

				if ((mDeviceViewObjectElderlyDiaperSensor != null)
						&& (mDeviceViewObjectElderlyDiaperSensor.getStrapAttached() == false)) {
					if (DBG) Log.d(TAG, "need to ask strap battery");
					mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.ELDERLY_STRAP_BATTERY_STATUS}));
				}

				tempMultiTouch = mPacketMgr.getMultiTouchValue(data);

				String strStrapValues = "";
				for (int i = 0; i < tempMultiTouch.length; i++) {
					strStrapValues += (int)(tempMultiTouch[i]) + ", ";
				}

				if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "STRAP_STATUS : " + strStrapValues);

				mGatherElderlySensingDataIndex = mGatherSensingDataIndex;
				mTouchCh1[mGatherSensingDataIndex] = (int)tempMultiTouch[0];
				mTouchCh2[mGatherSensingDataIndex] = (int)tempMultiTouch[1];
				mTouchCh3[mGatherSensingDataIndex] = (int)tempMultiTouch[2];
				mTouchCh4[mGatherSensingDataIndex] = (int)tempMultiTouch[3];
				mTouchCh5[mGatherSensingDataIndex] = (int)tempMultiTouch[4];
				mTouchCh6[mGatherSensingDataIndex] = (int)tempMultiTouch[5];
				mTouchCh7[mGatherSensingDataIndex] = (int)tempMultiTouch[6];
				mTouchCh8[mGatherSensingDataIndex] = (int)tempMultiTouch[7];
				mTouchCh9[mGatherSensingDataIndex] = (int)tempMultiTouch[8];
				if (mDeviceViewObjectElderlyDiaperSensor != null) {
					mDeviceViewObjectElderlyDiaperSensor.setStrapAttached(true);
					mDeviceViewObjectElderlyDiaperSensor.setMultiTouch(tempMultiTouch);
				}
				break;
			case BlePacketType.ELDERLY_STRAP_BATTERY_STATUS:
				tempStrapBattery = mPacketMgr.getStrapBatteryValue(data);
				if (mDeviceViewObjectElderlyDiaperSensor == null) {
					mDeviceViewObjectElderlyDiaperSensor = ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId);
				}
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " STRAP BATTERY changed value : " + tempStrapBattery);
				if (mDeviceViewObjectElderlyDiaperSensor != null) {
					mDeviceViewObjectElderlyDiaperSensor.setStrapAttached(true);
					mDeviceViewObjectElderlyDiaperSensor.setStrapBatteryPower(tempStrapBattery);
				}
				mServerQueryMgr.setDeviceStrapBatteryPower(DeviceType.ELDERLY_DIAPER_SENSOR,
						mDeviceInfo.deviceId,
						mDeviceInfo.getEnc(),
						tempStrapBattery,
						null);
				break;
			case BlePacketType.DEVICE_NAME:
				if (ConnectionManager.mConnectionLog.isManualConnection) {
					ConnectionManager.mConnectionLog.bleStep = 2;
				}
				currBleInitStep = 2;
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step2");
				if (data[1] == 1) {
					byte[] nameBytes = new byte[data.length - 4];
					for (int i = 4; i < data.length; i++) {
						nameBytes[i - 4] = data[i];
					}
					//mDeviceInfo.name = mPacketMgr.getName(nameBytes);
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "DEVICE_NAME : [" + mDeviceInfo.name + "]");
				} else {
					int pktIdx = data[2];
					if (pktIdx == 1) { // Initialize
						mNameBytes = new byte[BlePacketManager.MAX_BYTE_LENGTH_NAME];
						for (int i = 0; i < BlePacketManager.MAX_BYTE_LENGTH_NAME; i++) {
							mNameBytes[i] = 0;
						}
					}
					int queueIdx = (pktIdx - 1) * 16;
					for (int i = 4; i < data.length; i++) {
						mNameBytes[queueIdx++] = data[i];
					}
					if (pktIdx == 2) { // Finish
						//mDeviceInfo.name = mPacketMgr.getName(mNameBytes);
						if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "DEVICE_NAME : [" + mDeviceInfo.name + "]");
					}
				}
				break;
			case BlePacketType.SERIAL_NUMBER:
				if (ConnectionManager.mConnectionLog.isManualConnection) {
					ConnectionManager.mConnectionLog.bleStep = 5;
				}
				currBleInitStep = 5;
				mDeviceInfo.serial = mPacketMgr.getSerialNumber(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "SERIAL_NUMBER : " + mDeviceInfo.serial);
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step5");

				if (Configuration.NO_INTERNET) {
					_setConnected(true);
				} else {
					if (isManualConnect || mDeviceInfo.deviceId < 1) { // 수동연결이거나, deviceId 가 없으면 인터넷 연결
						_getDeviceIdFromServer();
					} else {
						_getDeviceIdFromServer();
						_setConnected(true);
					}
				}
				break;
			case BlePacketType.MAC_ADDRESS:
				if (ConnectionManager.mConnectionLog.isManualConnection) {
					ConnectionManager.mConnectionLog.bleStep = 3;
				}
				currBleInitStep = 3;
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step3");
				mDeviceInfo.btmacAddress = mPacketMgr.getMacAddress(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "MAC_ADDRESS : " + mDeviceInfo.btmacAddress);
				break;

			case BlePacketType.HUB_AP_NAME:
				if (data[1] == 1) {
					byte[] nameBytes = new byte[data.length - 4];
					for (int i = 4; i < data.length; i++) {
						nameBytes[i - 4] = data[i];
					}
					String apName = mPacketMgr.getName(nameBytes);
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_AP_NAME : [" + apName + "]");
				} else {
					int pktIdx = data[2];
					if (pktIdx == 1) { // Initialize
						mApNameBytes = new byte[BlePacketManager.MAX_BYTE_LENGTH_AP_NAME];
						for (int i = 0; i < BlePacketManager.MAX_BYTE_LENGTH_AP_NAME; i++) {
							mApNameBytes[i] = 0;
						}
					}
					int queueIdx = (pktIdx - 1) * 16;
					for (int i = 4; i < data.length; i++) {
						mApNameBytes[queueIdx++] = data[i];
					}
					if (pktIdx == 2) { // Finish
						String apName = mPacketMgr.getName(mApNameBytes);
						if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_AP_NAME : [" + apName + "]");
					}
				}
				break;

			case BlePacketType.HUB_AP_PASSWORD:
				if (data[1] == 1) {
					byte[] nameBytes = new byte[data.length - 4];
					for (int i = 4; i < data.length; i++) {
						nameBytes[i - 4] = data[i];
					}
					String apPassword = mPacketMgr.getName(nameBytes);
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_AP_PASSWORD : [" + apPassword + "]");
				} else {
					int pktIdx = data[2];
					if (pktIdx == 1) { // Initialize
						mApPasswordBytes = new byte[BlePacketManager.MAX_BYTE_LENGTH_AP_NAME];
						for (int i = 0; i < BlePacketManager.MAX_BYTE_LENGTH_AP_NAME; i++) {
							mApPasswordBytes[i] = 0;
						}
					}
					int queueIdx = (pktIdx - 1) * 16;
					for (int i = 4; i < data.length; i++) {
						mApPasswordBytes[queueIdx++] = data[i];
					}
					if (pktIdx == 2) { // Finish
						String apPassword = mPacketMgr.getName(mApPasswordBytes);
						if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_AP_PASSWORD : [" + apPassword + "]");
					}
				}
				break;

			case BlePacketType.HUB_AP_CONNECTION_STATUS:
				int connectionStatus = mPacketMgr.getHubApConnectionStatus(data);
				if (mConnectionManagerHandler != null) {
					mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_HUB_WIFI_CONNECTION_STATE_CHANGE, connectionStatus, -1).sendToTarget();
				}
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_AP_CONNECTION_STATUS : " + connectionStatus);
				break;

			case BlePacketType.HUB_DEVICE_ID:
				mHubDeviceInfo = new DeviceInfo();
				mHubDeviceInfo.type = 2;
				mHubDeviceInfo.deviceId = mPacketMgr.getDeviceId(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_DEVICE_ID : " + mHubDeviceInfo.deviceId);
				break;
			case BlePacketType.HUB_CLOUD_ID:
				mHubDeviceInfo.cloudId = mPacketMgr.getCloudId(data);
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_CLOUD_ID : " + mHubDeviceInfo.cloudId);
				break;

			case BlePacketType.HUB_FIRMWARE_VERSION:
				String tempFWVersion = mPacketMgr.getFirmwareVersion(data);
				if (tempFWVersion != null && tempFWVersion.length() > 3) {
					mHubDeviceInfo.firmwareVersion = tempFWVersion;
				}
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_FIRMWARE_VERSION : " + mHubDeviceInfo.firmwareVersion);
				break;

			case BlePacketType.HUB_MAC_ADDRESS:
				String tempMac = mPacketMgr.getMacAddress(data);
				if (tempMac != null && tempMac.length() > 6) {
					mHubDeviceInfo.btmacAddress = tempMac;
				}
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_MAC_ADDRESS : " + mHubDeviceInfo.btmacAddress);
				break;

			case BlePacketType.HUB_SERIAL_NUMBER:
				String tempSerial = mPacketMgr.getSerialNumber(data);
				if (tempSerial != null && tempSerial.length() > 7) {
					mHubDeviceInfo.serial = tempSerial;
				}

				if (mHubDeviceInfo.serial != null) {
					_getHubDeviceIdFromServer();
				}
				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "HUB_SERIAL_NUMBER : " + mHubDeviceInfo.serial);
				break;

			case BlePacketType.DIAPER_STATUS_COUNT:
				if (mDiaperStatusInfo == null) mDiaperStatusInfo = new DiaperStatusInfo();

				int[] diaperStatusCount = mPacketMgr.getDiaperStatusCount(data);

				if (diaperStatusCount != null) {
					if (DBG)
						Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "DIAPER_STATUS_COUNT(" + data + ") / "
								+ "Pee: " + diaperStatusCount[0] + " / "
								+ "Poo: " + diaperStatusCount[1] + " / "
								+ "Abnormal: " + diaperStatusCount[2] + " / "
								+ "Fart: " + diaperStatusCount[3] + " / "
								+ "Detachment: " + diaperStatusCount[4] + " / "
								+ "Attachment: " + diaperStatusCount[5] + " / ");
				}

				mDiaperStatusInfo.setDiaperStatusInfo(diaperStatusCount);

				if (mDiaperStatusInfo.isPeeDetected()) {
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " PEE DETECTED(" + mDiaperStatusInfo.getPeeCount() + ")");
					sendDiaperStatusDetectedTime(DeviceStatus.DETECT_PEE);
				} else {
					mDiaperStatusInfo.ignoreInitialSetting[0] = false;
				}

				if (mDiaperStatusInfo.isPooDetected()) {
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " POO DETECTED(" + mDiaperStatusInfo.getPooCount() + ")");
					sendDiaperStatusDetectedTime(DeviceStatus.DETECT_POO);
				} else {
					mDiaperStatusInfo.ignoreInitialSetting[1] = false;
				}

				if (mDiaperStatusInfo.isAbnormalDetected()) {
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " ABNORMAL DETECTED(" + mDiaperStatusInfo.getAbnormalCount() + ")");
					sendDiaperStatusDetectedTime(DeviceStatus.DETECT_ABNORMAL);
				} else {
					mDiaperStatusInfo.ignoreInitialSetting[2] = false;
				}

				if (mDiaperStatusInfo.isFartDetected()) {
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " FART DETECTED(" + mDiaperStatusInfo.getFartCount() + ")");
					sendDiaperStatusDetectedTime(DeviceStatus.DETECT_FART);
				} else {
					mDiaperStatusInfo.ignoreInitialSetting[3] = false;
				}

				if (mDiaperStatusInfo.isDetachmentDetected()) {
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " DETACHMENT DETECTED(" + mDiaperStatusInfo.getDetachmentCount() + ")");
					sendDiaperStatusDetectedTime(DeviceStatus.DETECT_DIAPER_DETACHED);
				} else {
					mDiaperStatusInfo.ignoreInitialSetting[4] = false;
				}

				if (mDiaperStatusInfo.isAttachmentDetected()) {
					if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " ATTACHMENT DETECTED(" + mDiaperStatusInfo.getAttachmentCount() + ")");
					sendDiaperStatusDetectedTime(DeviceStatus.DETECT_DIAPER_ATTACHED);
				} else {
					mDiaperStatusInfo.ignoreInitialSetting[5] = false;
				}
				break;

			case BlePacketType.LATEST_PEE_DETECTION_TIME:
			case BlePacketType.LATEST_POO_DETECTION_TIME:
			case BlePacketType.LATEST_ABNORMAL_DETECTION_TIME:
			case BlePacketType.LATEST_FART_DETECTION_TIME:
			case BlePacketType.LATEST_DETACHMENT_DETECTION_TIME:
			case BlePacketType.LATEST_ATTACHMENT_DETECTION_TIME:
				if (mDiaperStatusInfo == null) mDiaperStatusInfo = new DiaperStatusInfo();

				int detectionType = mPacketMgr.getDiaperStatusDetectionType(data);
				long detectionUtcTimeSec = mPacketMgr.getDiaperStatusDetectionTime(data);
				mDiaperStatusInfo.setDiaperStatusDetectionTime(detectionType, detectionUtcTimeSec);

				if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + " CHANGED DIAPER STATUS(" + detectionType + " / " + detectionUtcTimeSec + ")");
				// 앱을 켜고 맨 처음 연결시에는 앱에서 직접 알람 안주고 서버로 올림
				switch(detectionType) {
					case DeviceStatus.DETECT_PEE:
						if (mDiaperStatusInfo.ignoreInitialSetting[0] == false) {
							_updateDiaperStatus(detectionType, detectionUtcTimeSec * 1000);
						} else {
							if(DBG) Log.i(TAG, "ignore first pee detection");
							mDiaperStatusInfo.ignoreInitialSetting[0] = false;
							_updateDeviceDiaperStatus(detectionType, detectionUtcTimeSec);
						}
						break;
					case DeviceStatus.DETECT_POO:
						if (mDiaperStatusInfo.ignoreInitialSetting[1] == false) {
							_updateDiaperStatus(detectionType, detectionUtcTimeSec * 1000);
						} else {
							if(DBG) Log.i(TAG, "ignore first poo detection");
							mDiaperStatusInfo.ignoreInitialSetting[1] = false;
							_updateDeviceDiaperStatus(detectionType, detectionUtcTimeSec);
						}
						break;
					case DeviceStatus.DETECT_ABNORMAL:
						if (mDiaperStatusInfo.ignoreInitialSetting[2] == false) {
							_updateDiaperStatus(detectionType, detectionUtcTimeSec * 1000);
						} else {
							if(DBG) Log.i(TAG, "ignore first abnormal detection");
							mDiaperStatusInfo.ignoreInitialSetting[2] = false;
							_updateDeviceDiaperStatus(detectionType, detectionUtcTimeSec);
						}
						break;
					case DeviceStatus.DETECT_FART:
						if (mDiaperStatusInfo.ignoreInitialSetting[3] == false) {
							_updateDiaperStatus(detectionType, detectionUtcTimeSec * 1000);
						} else {
							if(DBG) Log.i(TAG, "ignore first fart detection");
							mDiaperStatusInfo.ignoreInitialSetting[3] = false;
						}
						break;
					case DeviceStatus.DETECT_DIAPER_DETACHED:
						if (mDiaperStatusInfo.ignoreInitialSetting[4] == false) {
							//_updateDiaperStatus(detectionType, detectionUtcTimeSec * 1000);
						} else {
							if(DBG) Log.i(TAG, "ignore first detachment detection");
							mDiaperStatusInfo.ignoreInitialSetting[4] = false;
							_updateDeviceDiaperStatus(detectionType, detectionUtcTimeSec);
						}
						break;
					case DeviceStatus.DETECT_DIAPER_ATTACHED:
						if (mDiaperStatusInfo.ignoreInitialSetting[5] == false) {
							//_updateDiaperStatus(detectionType, detectionUtcTimeSec * 1000);
						} else {
							if(DBG) Log.i(TAG, "ignore first attachment detection");
							mDiaperStatusInfo.ignoreInitialSetting[5] = false;
							_updateDeviceDiaperStatus(detectionType, detectionUtcTimeSec);
						}
						break;
				}
				break;
		}
		_updateViewObject(type);
	}

	private void _updateDiaperStatus(int diaperStatus, long timeMs) {
		if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "_updateDiaperStatus: " + diaperStatus + " / " + timeMs);

		if (!Configuration.FAST_DETECTION) {
			if (diaperStatus != DeviceStatus.DETECT_NONE) {
				if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "DiaperStatus changed BLE : " + diaperStatus);

				// 서버에 보내기
				_updateDeviceDiaperStatus(diaperStatus, timeMs / 1000);

				// 화면 업데이트
				if (mDeviceViewObject != null) {
					mDeviceViewObject.setDiaperStatus(diaperStatus, timeMs);
				}
			}
		}
	}

	private void _updatePendingDiaperStatus(int diaperStatus, long timeMs, int diffSec) {
		if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "_updatePendingDiaperStatus: " + diaperStatus + " / " + timeMs + " / " + diffSec);

		if (!Configuration.FAST_DETECTION) {
			if (diaperStatus != DeviceStatus.DETECT_NONE) {
				if (DBG) Log.d(TAG, "[" + mDeviceInfo.deviceId + "]" + "DiaperStatus changed BLE : " + diaperStatus);

				// 서버에 보내기
				_updateDeviceDiaperStatus(diaperStatus, timeMs / 1000, diffSec);

				// 화면 업데이트
				if (mDeviceViewObject != null) {
					mDeviceViewObject.setDiaperStatus(diaperStatus, timeMs - diffSec * 1000);
				}
			}
		}
	}

	private void _setSensingData() {
		// 1초마다 센서 디버그 메시지 출력 & 로그 저장
		if (mConnectionManagerHandler != null) {
			mCurrentSensorLog.timeSec = System.currentTimeMillis() / 1000;
			mCurrentSensorLog.type = mDeviceInfo.type;
			mCurrentSensorLog.id = mDeviceInfo.deviceId;
			mCurrentSensorLog.status = tempOperationStatus;
			mCurrentSensorLog.battery = tempBatttery;
			if (mDeviceViewObject != null) {
				mCurrentSensorLog.sex = mDeviceViewObject.getBabySex();
				mCurrentSensorLog.months = mDeviceViewObject.getBabyMonths();
			}
			//mCurrentSensorLog.data = String.format(Locale.getDefault(), "T%2.2f,H%2.2f,V%4.1f,T%5.1f,A%1.3f,Z%1.2f,S%d,M%d,%.1f,%.1f,%.1f,%.1f,%.1f", tempTemperature, tempHumidity, tempVoc, tempTouch, tempAcceleration, tempZAxis, tempOperationStatus, tempMovementStatus, tempEthanol, tempCo2, tempPressure, tempCompGas, tempRawGas);
			mCurrentSensorLog.data = String.format(Locale.getDefault(), "t%2.2f,h%2.2f,v%4.1f,r%5.1f,c%5.1f,a%1.3f,z%1.2f,s%d,m%d", tempTemperature, tempHumidity, tempVoc, (tempTouch % 10000) * 10, (tempTouch / 10000) * 10, tempAcceleration, tempZAxis, tempOperationStatus, tempMovementStatus);
			mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_SET_DEVICE_DATA, mDeviceInfo.type, (int)mDeviceInfo.deviceId, mCurrentSensorLog).sendToTarget();
		}

		// 30초간 데이터 저장 후 서버에 전송
		if (DBG) Log.i(TAG, "[" + mDeviceInfo.deviceId + "]" + "[T] " + tempTemperature + ", [H] " + tempHumidity + ", [V] " + tempVoc + ", [C] " + tempTouch + ", [A] " + tempAcceleration + ", [S] " + tempOperationStatus + ", [M] " + tempMovementStatus + ", [E] " + tempEthanol + ", [C] " + tempCo2 + ", [P] " + tempPressure + ", [O] " + tempCompGas + ", [Idx] " + mGatherSensingDataIndex);

		switch (tempOperationStatus) { // 충전중이거나, 끊어졌을때에는 이전까지 데이터를 업로드 해야함
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGING:
				_flushSensingData();
				break;
			default:
				if (mGatherSensingDataIndex >= MAX_COUNT_FLUSH_SENSING_DATA - 1) { // Flush 데이터가 차면 보내기
					_flushSensingData();
				} else {
					mGatherSensingDataIndex++;
				}
				break;
		}
	}

	private void _flushSensingData() {
		if (mGatherSensingDataIndex <= 0) return;

		if (DBG) Log.d(TAG, "flushSensingData: " + mGatherSensingDataIndex);

		if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
			SensingData sensingData = new SensingData();
			sensingData.deviceId = mDeviceInfo.deviceId;
			sensingData.timeMs = System.currentTimeMillis();

			sensingData.temperature = "" + mTemperatureList[mGatherSensingDataIndex];
			sensingData.humidity = "" + mHumidityList[mGatherSensingDataIndex];
			sensingData.voc = "" + mVocList[mGatherSensingDataIndex];
			sensingData.capacitance = "" + mTouchList[mGatherSensingDataIndex];
			sensingData.acceleration = "" + mAccelerationList[mGatherSensingDataIndex];
			sensingData.sensorstatus = "" + mSensorStatusList[mGatherSensingDataIndex];
			sensingData.movementlevel = "" + mMovementLevelList[mGatherSensingDataIndex];
			sensingData.ethanol = "" + mEthanolList[mGatherSensingDataIndex];
			sensingData.co2 = "" + mCo2List[mGatherSensingDataIndex];
			sensingData.pressure = "" + mPressureList[mGatherSensingDataIndex];
			sensingData.compgas = "" + mCompGasList[mGatherSensingDataIndex];

			for (int i = mGatherSensingDataIndex - 1; i >= 0; i--) {
				sensingData.temperature += "," + mTemperatureList[i];
				sensingData.humidity += "," + mHumidityList[i];
				sensingData.voc += "," + mVocList[i];
				sensingData.capacitance += "," + mTouchList[i];
				sensingData.acceleration += "," + mAccelerationList[i];
				sensingData.sensorstatus += "," + mSensorStatusList[i];
				sensingData.movementlevel += "," + mMovementLevelList[i];
				sensingData.ethanol += "," + mEthanolList[i];
				sensingData.co2 += "," + mCo2List[i];
				sensingData.pressure += "," + mPressureList[i];
				sensingData.compgas += "," + mCompGasList[i];
			}

			if (mConnectionManagerHandler != null) {
				mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_SET_DIAPER_SENSING_DATA_TO_CLOUD, sensingData).sendToTarget();
			}
		} else if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
			ElderlySensingData sensingData = new ElderlySensingData();
			sensingData.deviceId = mDeviceInfo.deviceId;
			sensingData.timeMs = System.currentTimeMillis();

			sensingData.temperature = "" + mTemperatureList[mGatherSensingDataIndex];
			sensingData.humidity = "" + mHumidityList[mGatherSensingDataIndex];
			sensingData.voc = "" + mVocList[mGatherSensingDataIndex];
			sensingData.capacitance = "" + mTouchList[mGatherSensingDataIndex];
			sensingData.acceleration = "" + mAccelerationList[mGatherSensingDataIndex];
			sensingData.sensorstatus = "" + mSensorStatusList[mGatherSensingDataIndex];
			sensingData.movementlevel = "" + mMovementLevelList[mGatherSensingDataIndex];
			sensingData.ethanol = "" + mEthanolList[mGatherSensingDataIndex];
			sensingData.co2 = "" + mCo2List[mGatherSensingDataIndex];
			sensingData.pressure = "" + mPressureList[mGatherSensingDataIndex];
			sensingData.compgas = "" + mCompGasList[mGatherSensingDataIndex];
			sensingData.touch_ch1 = "" + mTouchCh1[mGatherSensingDataIndex];
			sensingData.touch_ch2 = "" + mTouchCh2[mGatherSensingDataIndex];
			sensingData.touch_ch3 = "" + mTouchCh3[mGatherSensingDataIndex];
			sensingData.touch_ch4 = "" + mTouchCh4[mGatherSensingDataIndex];
			sensingData.touch_ch5 = "" + mTouchCh5[mGatherSensingDataIndex];
			sensingData.touch_ch6 = "" + mTouchCh6[mGatherSensingDataIndex];
			sensingData.touch_ch7 = "" + mTouchCh7[mGatherSensingDataIndex];
			sensingData.touch_ch8 = "" + mTouchCh8[mGatherSensingDataIndex];
			sensingData.touch_ch9 = "" + mTouchCh9[mGatherSensingDataIndex];

			for (int i = mGatherSensingDataIndex - 1; i >= 0; i--) {
				sensingData.temperature += "," + mTemperatureList[i];
				sensingData.humidity += "," + mHumidityList[i];
				sensingData.voc += "," + mVocList[i];
				sensingData.capacitance += "," + mTouchList[i];
				sensingData.acceleration += "," + mAccelerationList[i];
				sensingData.sensorstatus += "," + mSensorStatusList[i];
				sensingData.movementlevel += "," + mMovementLevelList[i];
				sensingData.ethanol += "," + mEthanolList[i];
				sensingData.co2 += "," + mCo2List[i];
				sensingData.pressure += "," + mPressureList[i];
				sensingData.compgas += "," + mCompGasList[i];
				sensingData.touch_ch1 += "," + mTouchCh1[i];
				sensingData.touch_ch2 += "," + mTouchCh2[i];
				sensingData.touch_ch3 += "," + mTouchCh3[i];
				sensingData.touch_ch4 += "," + mTouchCh4[i];
				sensingData.touch_ch5 += "," + mTouchCh5[i];
				sensingData.touch_ch6 += "," + mTouchCh6[i];
				sensingData.touch_ch7 += "," + mTouchCh7[i];
				sensingData.touch_ch8 += "," + mTouchCh8[i];
				sensingData.touch_ch9 += "," + mTouchCh9[i];
			}

			if (mConnectionManagerHandler != null) {
				mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_SET_ELDERLY_DIAPER_SENSING_DATA_TO_CLOUD, sensingData).sendToTarget();
			}
		}

		mGatherSensingDataIndex = 0;
	}

	private DeviceDiaperSensor mDeviceViewObject;
	private DeviceLamp mDeviceViewObjectLamp;
	private DeviceElderlyDiaperSensor mDeviceViewObjectElderlyDiaperSensor;
	private void _updateViewObject(int type) {
		if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
			if (mDeviceViewObject == null) {
				mDeviceViewObject = ConnectionManager.getDeviceDiaperSensor(mDeviceInfo.deviceId);
			}

			if (mDeviceViewObject != null) {
				switch (type) {
					// DeviceInfo
					case BlePacketType.DEVICE_ID:
					case BlePacketType.CLOUD_ID:
					case BlePacketType.FIRMWARE_VERSION:
					case BlePacketType.DEVICE_NAME:
					case BlePacketType.SERIAL_NUMBER:
					case BlePacketType.MAC_ADDRESS:
						mDeviceViewObject.deviceId = mDeviceInfo.deviceId;
						mDeviceViewObject.cloudId = mDeviceInfo.cloudId;
						mDeviceViewObject.firmwareVersion = mDeviceInfo.firmwareVersion;
						mDeviceViewObject.name = mDeviceInfo.name;
						mDeviceViewObject.serial = mDeviceInfo.serial;
						mDeviceViewObject.btmacAddress = mDeviceInfo.btmacAddress;
						if (mDeviceViewObject.updateDB(mContext) == -1) {
							if (DBG) Log.e(TAG, "updateDB Failed");
						}
						break;

					// ViewObject
					case BlePacketType.SENSOR_STATUS:
						mDeviceViewObject.setMovementStatus(tempMovementStatus);
						mDeviceViewObject.setOperationStatus(tempOperationStatus);
						//mDeviceViewObject.setDiaperStatus(tempDiaperStatus); Notification 때문에 BLE로 받자마자 갱신
						break;
					case BlePacketType.SENSITIVITY:
						mDeviceViewObject.setSensitivity(tempSensitivity);
						mServerQueryMgr.setSensorSensitivity(
								getDeviceInfo().deviceId,
								getDeviceInfo().getEnc(),
								tempSensitivity,
								null);
						break;
					case BlePacketType.BATTERY:
						mDeviceViewObject.setBatteryPower(tempBatttery);
						break;
					case BlePacketType.TEMPERATURE:
						mDeviceViewObject.setTemperature(tempTemperature);
						break;
					case BlePacketType.HUMIDITY:
						mDeviceViewObject.setHumidity(tempHumidity);
						break;
					case BlePacketType.VOC:
						mDeviceViewObject.setVoc(tempVoc);
						break;
					case BlePacketType.TOUCH:
						mDeviceViewObject.setCapacity(tempTouch);
						break;
					case BlePacketType.ACCELERATION:
						mDeviceViewObject.setAcceleration(tempAcceleration);
						break;
					case BlePacketType.BABY_INFO:
						mDeviceViewObject.setBabyBirthdayYYMMDD(tempBabyBirthdayYYMMDD);
						mDeviceViewObject.setBabySex(tempBabySex);
						mDeviceViewObject.setBabyEating(tempBabyEating);
						break;
					case BlePacketType.HUB_AP_NAME:

						break;
					case BlePacketType.HUB_AP_CONNECTION_STATUS:

						break;
					case BlePacketType.HUB_AP_SECURITY:

						break;
				}
			}
		} else if (mDeviceInfo.type == DeviceType.LAMP) {
			if (mDeviceViewObjectLamp == null) {
				mDeviceViewObjectLamp = ConnectionManager.getDeviceLamp(mDeviceInfo.deviceId);
			}
			if (mDeviceViewObjectLamp != null) {
				switch (type) {
					// DeviceInfo
					case BlePacketType.DEVICE_ID:
					case BlePacketType.CLOUD_ID:
					case BlePacketType.FIRMWARE_VERSION:
					case BlePacketType.DEVICE_NAME:
					case BlePacketType.SERIAL_NUMBER:
					case BlePacketType.MAC_ADDRESS:
						mDeviceViewObjectLamp.deviceId = mDeviceInfo.deviceId;
						mDeviceViewObjectLamp.cloudId = mDeviceInfo.cloudId;
						mDeviceViewObjectLamp.firmwareVersion = mDeviceInfo.firmwareVersion;
						mDeviceViewObjectLamp.name = mDeviceInfo.name;
						mDeviceViewObjectLamp.serial = mDeviceInfo.serial;
						mDeviceViewObjectLamp.btmacAddress = mDeviceInfo.btmacAddress;
						if (mDeviceViewObjectLamp.updateDB(mContext) == -1) {
							if (DBG) Log.e(TAG, "updateDB Failed");
						}
						break;
					case BlePacketType.TEMPERATURE:
						mDeviceViewObjectLamp.setTemperature(tempTemperature);
						break;
					case BlePacketType.HUMIDITY:
						mDeviceViewObjectLamp.setHumidity(tempHumidity);
						break;
					case BlePacketType.VOC:
						mDeviceViewObjectLamp.setVoc(tempVoc);
						break;
				}
			}
		} else if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
			if (mDeviceViewObjectElderlyDiaperSensor == null) {
				mDeviceViewObjectElderlyDiaperSensor = ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId);
			}

			if (mDeviceViewObjectElderlyDiaperSensor != null) {
				switch (type) {
					// DeviceInfo
					case BlePacketType.DEVICE_ID:
					case BlePacketType.CLOUD_ID:
					case BlePacketType.FIRMWARE_VERSION:
					case BlePacketType.DEVICE_NAME:
					case BlePacketType.SERIAL_NUMBER:
					case BlePacketType.MAC_ADDRESS:
						mDeviceViewObjectElderlyDiaperSensor.deviceId = mDeviceInfo.deviceId;
						mDeviceViewObjectElderlyDiaperSensor.cloudId = mDeviceInfo.cloudId;
						mDeviceViewObjectElderlyDiaperSensor.firmwareVersion = mDeviceInfo.firmwareVersion;
						mDeviceViewObjectElderlyDiaperSensor.name = mDeviceInfo.name;
						mDeviceViewObjectElderlyDiaperSensor.serial = mDeviceInfo.serial;
						mDeviceViewObjectElderlyDiaperSensor.btmacAddress = mDeviceInfo.btmacAddress;
						if (mDeviceViewObjectElderlyDiaperSensor.updateDB(mContext) == -1) {
							if (DBG) Log.e(TAG, "updateDB Failed");
						}
						break;

					// ViewObject
					case BlePacketType.SENSOR_STATUS:
						mDeviceViewObjectElderlyDiaperSensor.setMovementStatus(tempMovementStatus);
						mDeviceViewObjectElderlyDiaperSensor.setOperationStatus(tempOperationStatus);
						//mDeviceViewObject.setDiaperStatus(tempDiaperStatus); Notification 때문에 BLE로 받자마자 갱신
						break;
					case BlePacketType.SENSITIVITY:
						mDeviceViewObjectElderlyDiaperSensor.setSensitivity(tempSensitivity);
						mServerQueryMgr.setSensorSensitivity(
								getDeviceInfo().deviceId,
								getDeviceInfo().getEnc(),
								tempSensitivity,
								null);
						break;
					case BlePacketType.BATTERY:
						mDeviceViewObjectElderlyDiaperSensor.setBatteryPower(tempBatttery);
						break;
					case BlePacketType.TEMPERATURE:
						mDeviceViewObjectElderlyDiaperSensor.setTemperature(tempTemperature);
						break;
					case BlePacketType.HUMIDITY:
						mDeviceViewObjectElderlyDiaperSensor.setHumidity(tempHumidity);
						break;
					case BlePacketType.VOC:
						mDeviceViewObjectElderlyDiaperSensor.setVoc(tempVoc);
						break;
					case BlePacketType.TOUCH:
						mDeviceViewObjectElderlyDiaperSensor.setCapacity(tempTouch);
						break;
					case BlePacketType.ACCELERATION:
						mDeviceViewObjectElderlyDiaperSensor.setAcceleration(tempAcceleration);
						break;
					case BlePacketType.ELDERLY_STRAP_BATTERY_STATUS:
						mDeviceViewObjectElderlyDiaperSensor.setStrapAttached(true);
						mDeviceViewObjectElderlyDiaperSensor.setMultiTouch(tempMultiTouch);
						mDeviceViewObjectElderlyDiaperSensor.setStrapBatteryPower(tempStrapBattery);
						break;
					case BlePacketType.ELDERLY_STRAP_CAPACITANCE_STATUS:
						mDeviceViewObjectElderlyDiaperSensor.setStrapAttached(true);
						mDeviceViewObjectElderlyDiaperSensor.setStrapBatteryPower(tempStrapBattery);
						break;
					case BlePacketType.BABY_INFO:
						mDeviceViewObjectElderlyDiaperSensor.setBabyBirthdayYYMMDD(tempBabyBirthdayYYMMDD);
						mDeviceViewObjectElderlyDiaperSensor.setBabySex(tempBabySex);
						mDeviceViewObjectElderlyDiaperSensor.setBabyEating(tempBabyEating);
						break;
					case BlePacketType.HUB_AP_NAME:

						break;
					case BlePacketType.HUB_AP_CONNECTION_STATUS:

						break;
					case BlePacketType.HUB_AP_SECURITY:

						break;
				}
			}
		}
	}

	public void syncViewObject() {
		if (DBG) Log.d(TAG, "syncViewObject");

		if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
			if (mDeviceViewObject == null) {
				mDeviceViewObject = ConnectionManager.getDeviceDiaperSensor(mDeviceInfo.deviceId);
			}
			if (mDeviceViewObject != null) {
				if (DBG) Log.d(TAG, "syncViewObject start");
				mDeviceViewObject.deviceId = mDeviceInfo.deviceId;
				mDeviceViewObject.cloudId = mDeviceInfo.cloudId;
				mDeviceViewObject.firmwareVersion = mDeviceInfo.firmwareVersion;
				mDeviceViewObject.name = mDeviceInfo.name;
				mDeviceViewObject.serial = mDeviceInfo.serial;
				mDeviceViewObject.btmacAddress = mDeviceInfo.btmacAddress;

				mDeviceViewObject.setMovementStatus(tempMovementStatus);
				mDeviceViewObject.setOperationStatus(tempOperationStatus);
				//mDeviceViewObject.setDiaperStatus(tempDiaperStatus);

				mDeviceViewObject.setBatteryPower(tempBatttery);
				mDeviceViewObject.setTemperature(tempTemperature);
				mDeviceViewObject.setHumidity(tempHumidity);
				mDeviceViewObject.setVoc(tempVoc);
				mDeviceViewObject.setCapacity(tempTouch);
			}
		} else if (mDeviceInfo.type == DeviceType.LAMP) {
			if (mDeviceViewObjectLamp == null) {
				mDeviceViewObjectLamp = ConnectionManager.getDeviceLamp(mDeviceInfo.deviceId);
			}
			if (mDeviceViewObjectLamp != null) {
				if (DBG) Log.d(TAG, "syncViewObject start");
				mDeviceViewObjectLamp.deviceId = mDeviceInfo.deviceId;
				mDeviceViewObjectLamp.cloudId = mDeviceInfo.cloudId;
				mDeviceViewObjectLamp.firmwareVersion = mDeviceInfo.firmwareVersion;
				mDeviceViewObjectLamp.name = mDeviceInfo.name;
				mDeviceViewObjectLamp.serial = mDeviceInfo.serial;
				mDeviceViewObjectLamp.btmacAddress = mDeviceInfo.btmacAddress;
			}
		} else if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
			if (mDeviceViewObjectElderlyDiaperSensor == null) {
				mDeviceViewObjectElderlyDiaperSensor = ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId);
			}
			if (mDeviceViewObjectElderlyDiaperSensor != null) {
				if (DBG) Log.d(TAG, "syncViewObject start");
				mDeviceViewObjectElderlyDiaperSensor.deviceId = mDeviceInfo.deviceId;
				mDeviceViewObjectElderlyDiaperSensor.cloudId = mDeviceInfo.cloudId;
				mDeviceViewObjectElderlyDiaperSensor.firmwareVersion = mDeviceInfo.firmwareVersion;
				mDeviceViewObjectElderlyDiaperSensor.name = mDeviceInfo.name;
				mDeviceViewObjectElderlyDiaperSensor.serial = mDeviceInfo.serial;
				mDeviceViewObjectElderlyDiaperSensor.btmacAddress = mDeviceInfo.btmacAddress;

				mDeviceViewObjectElderlyDiaperSensor.setMovementStatus(tempMovementStatus);
				mDeviceViewObjectElderlyDiaperSensor.setOperationStatus(tempOperationStatus);
				//mDeviceViewObject.setDiaperStatus(tempDiaperStatus);

				mDeviceViewObjectElderlyDiaperSensor.setBatteryPower(tempBatttery);
				mDeviceViewObjectElderlyDiaperSensor.setTemperature(tempTemperature);
				mDeviceViewObjectElderlyDiaperSensor.setHumidity(tempHumidity);
				mDeviceViewObjectElderlyDiaperSensor.setVoc(tempVoc);
				mDeviceViewObjectElderlyDiaperSensor.setCapacity(tempTouch);
			}
		}
	}

	public void setDeviceId(long id) {
		if (DBG) Log.d(TAG, "setDeviceId : " + mDeviceInfo.deviceId);
		mDeviceInfo.deviceId = id;
		mGattMgr.write(mPacketMgr.makePacketFor3ByteValue(BlePacketType.DEVICE_ID, id));
		_updateViewObject(BlePacketType.DEVICE_ID);
	}

	public void setCertMode() {
		if (DBG) Log.d(TAG, "setCertMode");
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.CERT));
	}

	public void restartLamp() {
		if (DBG) Log.d(TAG, "restartLamp");
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.RESET));
	}

	public void setSerialNumber(String serial) {
		if (serial == null || serial.length() < 11) return;
		if (DBG) Log.d(TAG, "setSerialNumber : " + mDeviceInfo.serial);
		mDeviceInfo.serial = serial;
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.SERIAL_NUMBER, serial));
		_updateViewObject(BlePacketType.SERIAL_NUMBER);
	}

	public void setCloudId(long id) {
		if (DBG) Log.d(TAG, "setCloudId : " + mDeviceInfo.cloudId);
		mDeviceInfo.cloudId = id;
		mGattMgr.write(mPacketMgr.makePacketFor3ByteValue(BlePacketType.CLOUD_ID, id));
		_updateViewObject(BlePacketType.CLOUD_ID);
	}

	public void setName(String name) {
		if (DBG) Log.d(TAG, "setName : " + mDeviceInfo.name);
		mDeviceInfo.name = name;
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.DEVICE_NAME, name));
		_updateViewObject(BlePacketType.DEVICE_NAME);
	}

	public void setBabyInfo(String name, String birthdayYYMMDD, int sex, int eating) {
		mDeviceInfo.name = name;
		tempBabyBirthdayYYMMDD = birthdayYYMMDD;
		tempBabySex = sex;
		tempBabyEating = eating;
		String tempYYMMDD = birthdayYYMMDD;
		if (tempYYMMDD.length() != 6) {
			tempYYMMDD = "180101";
		}

		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.DEVICE_NAME, name));
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.BABY_INFO, tempYYMMDD + sex));

		_updateViewObject(BlePacketType.DEVICE_NAME);
		_updateViewObject(BlePacketType.BABY_INFO);

		if (DBG) Log.d(TAG, "setBabyInfo : " + mDeviceInfo.name + " / " + tempYYMMDD + " / " + tempBabySex);
	}

	public void setApInfo(HubApInfo hubApInfo) {
		if (DBG) Log.d(TAG, "setApInfo : " + hubApInfo.name + " / " + hubApInfo.securityType + " / " + hubApInfo.index);

		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.HUB_AP_NAME, hubApInfo.name));
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.HUB_AP_PASSWORD, hubApInfo.password));
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.HUB_AP_SECURITY, hubApInfo.securityType, hubApInfo.index));
	}

	public void setHeatingDurationTimeMs(long timeMs) {
		if (DBG) Log.d(TAG, "setHeatingDurationTimeMs : " + timeMs);
		mGattMgr.write(mPacketMgr.makePacketFor3ByteValue(BlePacketType.HEATING_DURATION_TIME, timeMs));
	}

	public void setUtcTimeInfo(long utcTimeSec) {
		if (DBG) Log.d(TAG, "setUtcTimeInfo : " + utcTimeSec);
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.UTC_TIME_INFO, utcTimeSec));
	}

	public void checkSensorStatus() {
		if (DBG) Log.d(TAG, "checkSensorStatus");
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.SENSOR_STATUS, BlePacketType.BATTERY, BlePacketType.TEMPERATURE, BlePacketType.HUMIDITY, BlePacketType.VOC}));
	}

	public void checkElderlySensorStrapBatteryPower() {
		if (DBG) Log.d(TAG, "checkElderlySensorStrapBatteryPower");
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.ELDERLY_STRAP_BATTERY_STATUS}));
	}

	/**
	 *  getSensorDeviceInfo
	 *  센서의 Device ID 발급/확인을 위한 기본 정보를 요청함
	 */
	public void getSensorDeviceInfo() {
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.DEVICE_ID, BlePacketType.CLOUD_ID, BlePacketType.FIRMWARE_VERSION, BlePacketType.BABY_INFO}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.DEVICE_NAME}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.MAC_ADDRESS}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.SENSOR_STATUS, BlePacketType.BATTERY, BlePacketType.TEMPERATURE, BlePacketType.HUMIDITY, BlePacketType.VOC}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.SERIAL_NUMBER}));
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.DATE_INFO, System.currentTimeMillis()));
		sendAutoPollingEnabled(true);
	}

	/**
	 *  getElderlySensorDeviceInfo
	 *  성인용 기저귀 센서의 Device ID 발급/확인을 위한 기본 정보를 요청함
	 */
	public void getElderlySensorDeviceInfo() {
		if (DBG) Log.d(TAG, "getElderlySensorDeviceInfo");
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.DEVICE_ID, BlePacketType.CLOUD_ID, BlePacketType.FIRMWARE_VERSION, BlePacketType.BABY_INFO}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.DEVICE_NAME}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.MAC_ADDRESS}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.SENSOR_STATUS, BlePacketType.BATTERY, BlePacketType.TEMPERATURE, BlePacketType.HUMIDITY, BlePacketType.VOC}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.SERIAL_NUMBER}));
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.DATE_INFO, System.currentTimeMillis()));
		sendAutoPollingEnabled(true);
	}

	/**
	 *  getLampDeviceInfo
	 *  수유등의 Device ID 발급/확인을 위한 기본 정보를 요청함
	 */
	public void getLampDeviceInfo() {
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.DEVICE_ID, BlePacketType.CLOUD_ID, BlePacketType.FIRMWARE_VERSION}));
		//mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.MAC_ADDRESS}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[] {BlePacketType.SERIAL_NUMBER}));
	}

	/**
	 *  checkHubDeviceInfo
	 *  연결된 센서를 통해 허브의 Device ID 발급/확인을 위한 기본 정보를 요청함
	 *  @param register: 허브 Device ID 확인 후 바로 허브 등록
	 */
	private boolean doRegisterHubDevice = false;
	public void checkHubDeviceInfo(boolean register) {
		if (DBG) Log.d(TAG, "checkHubDeviceInfo : " + register);
		if (doRegisterHubDevice == false) {
			doRegisterHubDevice = register;
		}
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.HUB_DEVICE_ID, BlePacketType.HUB_CLOUD_ID, BlePacketType.HUB_FIRMWARE_VERSION, BlePacketType.HUB_AP_SECURITY}));
		//write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.HUB_DEVICE_NAME}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.HUB_MAC_ADDRESS}));
		//write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.HUB_AP_NAME}));
		mGattMgr.write(mPacketMgr.getRequestPacket(new int[]{BlePacketType.HUB_SERIAL_NUMBER}));
	}

	/**
	 *  initialize
	 *  연결된 센서를 초기화시키며, 추가 연결이 되지 않도록 unregister를 수행함
	 *  단, unregister 수행시 init패킷이 센서에 send된 후 연결을 끊을 수 있도록 1초 뒤에 unregister 수행
	 */
	public void initialize() {
		if (DBG) Log.e(TAG, "initialize[" + mDeviceInfo.deviceId + "]" + mDeviceInfo.name + " / " + mDeviceInfo.btmacAddress);
		mGattMgr.write(mPacketMgr.getCommandPacket(BlePacketType.INITIALIZE));
		ConnectionManager.mRegisteredDiaperSensorList.remove(mDeviceInfo.deviceId);
		ConnectionManager.removeDeviceBLEConnection(mDeviceInfo.deviceId, mDeviceInfo.type);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				unregister();
			}
		}, 1000);
	}

	/**
	 *  unregister
	 *  Gatt를 Close하며, App이 관리하고 있는 DeviceList에서 삭제함
	 */
	public void unregister() {
		if (DBG) Log.e(TAG, "unregister[" + mDeviceInfo.deviceId + "]" + mDeviceInfo.name + " / " + mDeviceInfo.btmacAddress);
		isRemoved = true;
		close();
		if (mDeviceInfo.deleteDB(mContext) == 1) {
			if (DBG) Log.e(TAG, "delete DB succeeded");
		} else {
			if (DBG) Log.e(TAG, "delete DB failed");
		}
		//mRegisteredBleDeviceList.remove(mDeviceInfo.deviceId);
		//ConnectionManager.mRegisteredDiaperSensorList.remove(mDeviceInfo.deviceId);
	}

	public void setConnected(boolean connected) {
		_setConnected(connected);
	}

	/**
	 *  _setConnected
	 *  센서의 Device ID, Cloud ID 발급/확인 후 최종 Connected 결정을 내리게 되며, AutoConnect를 멈춤
	 */
	private void _setConnected(boolean connected) {
		//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("setConnected : " + connected);
		if (connected) {
			if (DBG) Log.d(TAG, "Connected " + mDeviceInfo.type + " / " + mDeviceInfo.deviceId + " / " + mDeviceInfo.cloudId + " / " + mDeviceInfo.name + " / " + mDeviceInfo.serial + " / " + mDeviceInfo.btmacAddress + " / " + mDeviceInfo.firmwareVersion);
			if (mHandler != null) {
				// Time out 관련 초기화
				mHandler.removeMessages(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
				mHandler.removeMessages(MSG_CONNECTION_STATE_CHANGED);
				if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR || mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
					mHandler.sendEmptyMessageDelayed(MSG_START_AUTO_POLLING, 3 * 1000L);
				}
			}

			// Set Connected
			mConnectionState = DeviceConnectionState.BLE_CONNECTED;
			if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
				if (mDeviceViewObject != null) {
					mDeviceViewObject.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
				}
			} else if (mDeviceInfo.type == DeviceType.LAMP) {
				if (mDeviceViewObjectLamp != null) {
					mDeviceViewObjectLamp.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
				}
			} else if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
				if (mDeviceViewObjectElderlyDiaperSensor != null) {
					mDeviceViewObjectElderlyDiaperSensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
				}
			}

			if (!isConnected) {
				if (isManualConnect) {
					ConnectionManager.putDeviceBLEConnection(mDeviceInfo.deviceId, mDeviceInfo.type, this);
					if (mConnectionManagerHandler != null) {
						Message msg = mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUALLY_CONNECTED);
						msg.arg1 = DeviceConnectionState.BLE_CONNECTED;
						msg.obj = mDeviceInfo;
						mConnectionManagerHandler.sendMessage(msg);
					} else {
						if (DBG) Log.e(TAG, "sendConnectionStateMessageDelayed connectionManagerHandler NULL");
					}
				} else {
					if (mHandler != null) {
						mHandler.obtainMessage(MSG_CONNECTION_STATE_CHANGED, DeviceConnectionState.BLE_CONNECTED, -1).sendToTarget();
					}
				}
				isConnected = true;

				mServerQueryMgr.setDeviceConnectionState(
						mDeviceInfo.type,
						mDeviceInfo.deviceId,
						mDeviceInfo.getEnc(),
						mConnectionState,
						-1,
						null);
				if (isRemoved) {
					mHandler = null;
				}
			} else {
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("already : " + mConnectionState + " / " + isConnected);
			}
		} else {
			if (isConnected) {
				// Set Disconnected
				mConnectionState = DeviceConnectionState.DISCONNECTED;

				if (mDeviceInfo.type == DeviceType.DIAPER_SENSOR) {
					if (mDeviceViewObject != null) {
						mDeviceViewObject.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
					}
				} else if (mDeviceInfo.type == DeviceType.LAMP) {
					if (mDeviceViewObjectLamp != null) {
						mDeviceViewObjectLamp.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
					}
				} else if (mDeviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
					if (mDeviceViewObjectElderlyDiaperSensor != null) {
						mDeviceViewObjectElderlyDiaperSensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
					}
				}
				if (mHandler != null) {
					mHandler.obtainMessage(MSG_CONNECTION_STATE_CHANGED, DeviceConnectionState.DISCONNECTED, -1).sendToTarget();
				}
				isConnected = false;

				mServerQueryMgr.setDeviceConnectionState(
						mDeviceInfo.type,
						mDeviceInfo.deviceId,
						mDeviceInfo.getEnc(),
						mConnectionState,
						mDisconnectedReason,
						null);
			} else {
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("already : " + mConnectionState + " / " + isConnected);
			}
		}
	}

	/**
	 *  _sendHubConnected
	 *  센서를 통해 허브의 Device ID, Cloud ID 등을 전달받아 발급/확인을 완료한 뒤 호출하는 함수로
	 *  허브와 스마트폰의 연결완료 여부를 상위레벨인 ConnectionManager로 전달함
	 */
	private void _sendHubConnected() {
		if (mConnectionManagerHandler != null) {
			mConnectionManagerHandler.obtainMessage(
					ConnectionManager.MSG_HUB_CONNECTED_WITH_SENSOR,
					(tempOperationStatus >= DeviceStatus.OPERATION_HUB_NO_CHARGE) ? 1 : 0,
					-1, mDeviceInfo).sendToTarget();
		}
	}

	private void _getDeviceIdFromServer() {
		mServerQueryMgr.getDeviceId(
				mDeviceInfo.type,
				mDeviceInfo.deviceId,
				mDeviceInfo.serial,
				mDeviceInfo.btmacAddress,
				mDeviceInfo.name,
				mDeviceInfo.firmwareVersion,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						if (ConnectionManager.mConnectionLog.isManualConnection) {
							ConnectionManager.mConnectionLog.GetDeviceIdEcd = errCode;
						}
						currBleInitStep = 10;
						//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step10");
						if (InternetErrorCode.SUCCEEDED.equals(errCode) || InternetErrorCode.ERR_ALREADY_REGISTERED_PRODUCT.equals(errCode)) {
							currBleInitStep = 11;
							switch(mDeviceInfo.type) {
								case DeviceType.DIAPER_SENSOR:
									//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step11");
									long deviceId = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(26));
									long heatingTimeMs = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(128), -1);
									if (mVersionMgr.supportDiaperSensorHeatingTime(mDeviceInfo.firmwareVersion)) {
										if (heatingTimeMs != -1) {
											setHeatingDurationTimeMs(heatingTimeMs);
										}
									}

									if (mVersionMgr.revisedConnectionFalseNegativeAlert(mDeviceInfo.firmwareVersion)) {
										setUtcTimeInfo(System.currentTimeMillis() / 1000);
									}

									mPreferenceMgr.setDeviceSerialNumber(DeviceType.DIAPER_SENSOR, deviceId, mDeviceInfo.serial);
									if (ConnectionManager.mConnectionLog.isManualConnection) {
										ConnectionManager.mConnectionLog.deviceId = deviceId;
									}
									if (deviceId > 0) {
										mDeviceInfo.deviceId = deviceId;
										setDeviceId(deviceId);
									}
									//_setConnected(true); Cloud ID까지 확인하고 연결시킴
									_updateDeviceFWVersion();
									_getCloudIdFromServer();
									break;
								case DeviceType.LAMP:
									long lampDeviceId = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(26));
									String serial = ServerManager.getStringFromJSONObj(data, mServerQueryMgr.getParameter(44));
									mPreferenceMgr.setDeviceSerialNumber(DeviceType.LAMP, lampDeviceId, mDeviceInfo.serial);
									if (ConnectionManager.mConnectionLog.isManualConnection) {
										ConnectionManager.mConnectionLog.deviceId = lampDeviceId;
									}
									if (lampDeviceId > 0) {
										mDeviceInfo.deviceId = lampDeviceId;
										setDeviceId(lampDeviceId);
									}
									if (serial != null && serial.length() == 11) {
										mDeviceInfo.serial = serial;
										setCertMode();
										setSerialNumber(serial);
										restartLamp();
									}
									_updateDeviceFWVersion();
									_getCloudIdFromServer();
									break;
                                case DeviceType.ELDERLY_DIAPER_SENSOR:
                                    //if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step11");
                                    long deviceId2 = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(26));
                                    long heatingTimeMs2 = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(128), -1);
                                    if (mVersionMgr.supportDiaperSensorHeatingTime(mDeviceInfo.firmwareVersion)) {
                                        if (heatingTimeMs2 != -1) {
                                            setHeatingDurationTimeMs(heatingTimeMs2);
                                        }
                                    }

                                    if (mVersionMgr.revisedConnectionFalseNegativeAlert(mDeviceInfo.firmwareVersion)) {
                                        setUtcTimeInfo(System.currentTimeMillis() / 1000);
                                    }

                                    mPreferenceMgr.setDeviceSerialNumber(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId2, mDeviceInfo.serial);
                                    if (ConnectionManager.mConnectionLog.isManualConnection) {
                                        ConnectionManager.mConnectionLog.deviceId = deviceId2;
                                    }
                                    if (deviceId2 > 0) {
                                        mDeviceInfo.deviceId = deviceId2;
                                        setDeviceId(deviceId2);
                                    }
                                    //_setConnected(true); Cloud ID까지 확인하고 연결시킴
                                    _updateDeviceFWVersion();
                                    _getCloudIdFromServer();
                                    break;
							}
						} else {
							if (DBG) Log.d(TAG, "getDeviceId FAILED : " + responseCode + " / " + errCode);
							//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("resp : " + responseCode + " / " + errCode);
						}
					}
				});
	}

	private void _getHubDeviceIdFromServer() {
		mServerQueryMgr.getDeviceId(
				mHubDeviceInfo.type,
				mHubDeviceInfo.deviceId,
				mHubDeviceInfo.serial,
				mHubDeviceInfo.btmacAddress,
				mHubDeviceInfo.name,
				mHubDeviceInfo.firmwareVersion,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("get hub did");
						if (InternetErrorCode.SUCCEEDED.equals(errCode) || InternetErrorCode.ERR_ALREADY_REGISTERED_PRODUCT.equals(errCode)) {
							long deviceId = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(26));
							mPreferenceMgr.setDeviceSerialNumber(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, mHubDeviceInfo.serial);
							if (DBG) Log.d(TAG, "getHubDeviceId : " + deviceId);
							if (deviceId > 0) {
								mHubDeviceInfo.deviceId = deviceId;
								mGattMgr.write(mPacketMgr.makePacketFor3ByteValue(BlePacketType.HUB_DEVICE_ID, deviceId));
								//_sendHubConnected(); Cloud ID를 받아온 뒤 그룹 추가 여부도 확인하고 Hub Connected가 되어야함
								_getHubCloudIdFromServer();
							}
						} else {
							if (DBG) Log.d(TAG, "getHubDeviceId FAILED : " + responseCode + " / " + errCode);
							//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("resp : " + responseCode + " / " + errCode);
						}
					}
				});
	}

	/*
	public void setDiaperSensorCloudIdToServer(final ServerManager.ServerResponseListener listener) {
		mServerQueryMgr.setCloudId(
				mDeviceInfo.type,
				mDeviceInfo.deviceId,
				mDeviceInfo.getEnc(),
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						if (listener != null) {
							listener.onReceive(responseCode, errCode, data);
						}
						if (ConnectionManager.mConnectionLog.isManualConnection) {
							ConnectionManager.mConnectionLog.SetCloudIdEcd = errCode;
						}
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
						}
					}
				});
	}


	public void setLampCloudIdToServer(final ServerManager.ServerResponseListener listener) {
		mServerQueryMgr.setCloudId(
				mDeviceInfo.type,
				mDeviceInfo.deviceId,
				mDeviceInfo.getEnc(),
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						if (listener != null) {
							listener.onReceive(responseCode, errCode, data);
						}
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
						}
					}
				});
	}

	public void setHubCloudIdToServer(final ServerManager.ServerResponseListener listener) {
		mServerQueryMgr.setCloudId(
				mHubDeviceInfo.type,
				mHubDeviceInfo.deviceId,
				mHubDeviceInfo.getEnc(),
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						if (listener != null) {
							listener.onReceive(responseCode, errCode, data);
						}
						//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("set hub cid");
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							long accountId = mPreferenceMgr.getAccountId();
							mGattMgr.write(mPacketMgr.makePacketFor3ByteValue(BlePacketType.HUB_CLOUD_ID, accountId));
						} else {
							//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("resp : " + responseCode + " / " + errCode);
						}
					}
				});
	}
	*/

	private void _getHubCloudIdFromServer() {
		mServerQueryMgr.getCloudId(
				mHubDeviceInfo.type,
				mHubDeviceInfo.deviceId,
				mHubDeviceInfo.getEnc(),
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("get hub cid");
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							long cloudId = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(29));
							if (DBG) Log.d(TAG, "getHubCloudId : " + cloudId + " / " + doRegisterHubDevice);
							if (doRegisterHubDevice) {
								if (cloudId == 0) { // 허브 cloudId == 0(초기화상태)에서만 CloudId Set
									mHubDeviceInfo.cloudId = mPreferenceMgr.getAccountId();
									//_setHubCloudIdToServer();
								} else {
									mHubDeviceInfo.cloudId = cloudId;
								}
								mGattMgr.write(mPacketMgr.makePacketFor3ByteValue(BlePacketType.HUB_CLOUD_ID, mHubDeviceInfo.cloudId));
								_sendHubConnected();
								/*
								if ((cloudId == 0) || (mHubDeviceInfo.cloudId == 0)) { // 서버 혹은 디바이스에 세팅이 안되어 있는 경우
									mHubDeviceInfo.cloudId = cloudId;
									mDebugMgr.saveDebuggingLog("getHubCloudIdFromServer set hub cloud id : " + mHubDeviceInfo.deviceId);
									_setHubCloudIdToServer();
								}
								*/
								doRegisterHubDevice = false;
							} else {
								mHubDeviceInfo.cloudId = cloudId;
							}
						} else {
							//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("resp : " + responseCode + " / " + errCode);
						}
					}
				});
	}

	private void _updateDeviceFWVersion() {
		mServerQueryMgr.updateFWVersion(
				mDeviceInfo.type,
				mDeviceInfo.deviceId,
				mDeviceInfo.getEnc(),
				mDeviceInfo.firmwareVersion,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						/* Prevent static analysis
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

						} else {

						}
						*/
					}
				});
	}

	private void _getCloudIdFromServer() {
		mServerQueryMgr.getCloudId(
				mDeviceInfo.type,
				mDeviceInfo.deviceId,
				mDeviceInfo.getEnc(),
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						if (ConnectionManager.mConnectionLog.isManualConnection) {
							ConnectionManager.mConnectionLog.GetCloudIdEcd = errCode;
						}
						currBleInitStep = 12;
						//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step12");
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							currBleInitStep = 13;
							switch(mDeviceInfo.type) {
								case DeviceType.DIAPER_SENSOR:
									//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step13");
									long cloudId = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(29));
									// 수동연결시, 다른 계정 기기라면 그룹원으로 초대되도록 유도
									if (isManualConnect && cloudId > 0 && cloudId != mPreferenceMgr.getAccountId()) {
										mDeviceInfo.cloudId = cloudId;
										setCloudId(cloudId);
										if (mHandler != null) {
											mHandler.removeMessages(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
										}
										if (mConnectionManagerHandler != null) {
											mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST, mDeviceInfo).sendToTarget();
										}

									} else {
										if (cloudId == 0) { // 서버에 Cloud ID 가 등록되어 있지 않은 경우
											cloudId = mPreferenceMgr.getAccountId();
											//setDiaperSensorCloudIdToServer();
										}
										setCloudId(cloudId);
										_setConnected(true);
									}
									break;
								case DeviceType.LAMP:
									//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step13");
									long lampCloudId = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(29));
									// 수동연결시, 다른 계정 기기라면 그룹원으로 초대되도록 유도
									if (isManualConnect && lampCloudId > 0 && lampCloudId != mPreferenceMgr.getAccountId()) {
										mDeviceInfo.cloudId = lampCloudId;
										setCloudId(lampCloudId);
										if (mHandler != null) {
											mHandler.removeMessages(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
										}
										if (mConnectionManagerHandler != null) {
											mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST, mDeviceInfo).sendToTarget();
										}

									} else {
										if (lampCloudId == 0) { // 서버에 Cloud ID 가 등록되어 있지 않은 경우
											lampCloudId = mPreferenceMgr.getAccountId();
											//setLampCloudIdToServer(null);
										}
										setCloudId(lampCloudId);
										_setConnected(true);
									}
									break;
								case DeviceType.ELDERLY_DIAPER_SENSOR:
									//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("step13");
									long cloudId2 = ServerManager.getLongFromJSONObj(data, mServerQueryMgr.getParameter(29));
									// 수동연결시, 다른 계정 기기라면 그룹원으로 초대되도록 유도
									if (isManualConnect && cloudId2 > 0 && cloudId2 != mPreferenceMgr.getAccountId()) {
										mDeviceInfo.cloudId = cloudId2;
										setCloudId(cloudId2);
										if (mHandler != null) {
											mHandler.removeMessages(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
										}
										if (mConnectionManagerHandler != null) {
											mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_BLE_MANUAL_CONNECTION_GUEST, mDeviceInfo).sendToTarget();
										}

									} else {
										if (cloudId2 == 0) { // 서버에 Cloud ID 가 등록되어 있지 않은 경우
											cloudId2 = mPreferenceMgr.getAccountId();
											//setDiaperSensorCloudIdToServer();
										}
										setCloudId(cloudId2);
										_setConnected(true);
									}
									break;
							}

						} else {
							//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("resp : " + responseCode + " / " + errCode);
						}
					}
				});
	}

	private void _updateDeviceDiaperStatus(int diaperStatus, long timeSec) {
		_updateDeviceDiaperStatus(diaperStatus, timeSec, -1);
	}

	private void _updateDeviceDiaperStatus(int diaperStatus, long timeSec, int diffTimeSec) {
		String updateData = null;
		if (timeSec == 0) timeSec = System.currentTimeMillis() / 1000;

		try {
			JSONObject jobj = new JSONObject();
			jobj.put(mServerQueryMgr.getParameter(28), getDeviceInfo().type);
			jobj.put(mServerQueryMgr.getParameter(26), getDeviceInfo().deviceId);
			jobj.put(mServerQueryMgr.getParameter(27), getDeviceInfo().getEnc());
			jobj.put(mServerQueryMgr.getParameter(15), timeSec); // time

			if (mVersionMgr.revisedConnectionFalseNegativeAlert(mDeviceInfo.firmwareVersion) == true) {
				if (mDiaperStatusInfo != null) {
					jobj.put(mServerQueryMgr.getParameter(129), mDiaperStatusInfo.detectionCount[0]);
					jobj.put(mServerQueryMgr.getParameter(130), mDiaperStatusInfo.detectionCount[1]);
					jobj.put(mServerQueryMgr.getParameter(131), mDiaperStatusInfo.detectionCount[2]);
					jobj.put(mServerQueryMgr.getParameter(132), mDiaperStatusInfo.detectionCount[3]);
					jobj.put(mServerQueryMgr.getParameter(133), mDiaperStatusInfo.detectionCount[4]);
					jobj.put(mServerQueryMgr.getParameter(134), mDiaperStatusInfo.detectionCount[5]);

					jobj.put(mServerQueryMgr.getParameter(135), mDiaperStatusInfo.latestDetectionUtcTimeSec[0]);
					jobj.put(mServerQueryMgr.getParameter(136), mDiaperStatusInfo.latestDetectionUtcTimeSec[1]);
					jobj.put(mServerQueryMgr.getParameter(137), mDiaperStatusInfo.latestDetectionUtcTimeSec[2]);
					jobj.put(mServerQueryMgr.getParameter(138), mDiaperStatusInfo.latestDetectionUtcTimeSec[3]);
					jobj.put(mServerQueryMgr.getParameter(139), mDiaperStatusInfo.latestDetectionUtcTimeSec[4]);
					jobj.put(mServerQueryMgr.getParameter(140), mDiaperStatusInfo.latestDetectionUtcTimeSec[5]);
				} else {
					if (DBG) Log.e(TAG, "diaper status info null");
				}
			} else {
				jobj.put(mServerQueryMgr.getParameter(52), diaperStatus); // dps
				if (diffTimeSec > 0) {
					jobj.put(mServerQueryMgr.getParameter(53), diffTimeSec); // dtime(pending diff time sec)
				}
				jobj.put(mServerQueryMgr.getParameter(41), (int) (tempVoc * 100));
			}
			updateData = "[" + jobj.toString() + "]";
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (updateData == null) return;

		mServerQueryMgr.setDeviceStatus(updateData, null);
	}

	private void _updateLampMonitoringStatus(float temperature, float humidity) {
		String updateData = null;
		long timeSec = System.currentTimeMillis() / 1000;
		if (DBG) Log.d(TAG, "_updateLampMonitoringStatus: " + temperature + " / " + humidity);

		try {
			JSONObject jobj = new JSONObject();
			jobj.put(mServerQueryMgr.getParameter(28), DeviceType.LAMP);
			jobj.put(mServerQueryMgr.getParameter(26), getDeviceInfo().deviceId);
			jobj.put(mServerQueryMgr.getParameter(27), getDeviceInfo().getEnc());
			jobj.put(mServerQueryMgr.getParameter(15), timeSec); // time

			jobj.put(mServerQueryMgr.getParameter(39), (int)(temperature));
			jobj.put(mServerQueryMgr.getParameter(40), (int)(humidity));

			updateData = "[" + jobj.toString() + "]";
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (updateData == null) return;

		mServerQueryMgr.setDeviceStatus(updateData, null);
	}

	private void _sendConnectionStateMessage(int state) {
		_sendConnectionStateMessageDelayed(state, 0);
	}

	private void _sendConnectionStateMessageDelayed(int state, long delayed) {
		if (mConnectionManagerHandler != null) {
			Message msg = mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE);
			msg.arg1 = state;
			msg.obj = mDeviceInfo;
			mConnectionManagerHandler.sendMessageDelayed(msg, delayed);
		} else {
			if (DBG) Log.e(TAG, "sendConnectionStateMessageDelayed connectionManagerHandler NULL");
		}

		if (state == DeviceConnectionState.BLE_CONNECTED || state == DeviceConnectionState.DISCONNECTED) {
			if (ConnectionManager.getDeviceDiaperSensor(mDeviceInfo.deviceId) != null) {
				ConnectionManager.getDeviceDiaperSensor(mDeviceInfo.deviceId).setConnectionState(state);
			}
			if (ConnectionManager.getDeviceLamp(mDeviceInfo.deviceId) != null) {
				ConnectionManager.getDeviceLamp(mDeviceInfo.deviceId).setConnectionState(state);
			}
			if (ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId) != null) {
				ConnectionManager.getDeviceElderlyDiaperSensor(mDeviceInfo.deviceId).setConnectionState(state);
			}
		}
	}

	private void _sendErrorMessage(int code) {
		if (mConnectionManagerHandler != null) {
			Message msg = mConnectionManagerHandler.obtainMessage(ConnectionManager.MSG_CONNECTION_ERROR);
			msg.arg1 = code;
			msg.obj = mDeviceInfo;
			mConnectionManagerHandler.sendMessage(msg);
		} else {
			if (DBG) Log.e(TAG, "sendErrorMessage connectionManagerHandler NULL");
		}
	}

	public String toString() {
		return mDeviceInfo.toString();
	}
}