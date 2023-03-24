package goodmonit.monit.com.kao.services;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import goodmonit.monit.com.kao.MonitApplication;
import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.UserInfo.UserInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.devices.ConnectionLog;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
import goodmonit.monit.com.kao.devices.CurrentSensorLog;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceElderlyDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.ElderlySensingData;
import goodmonit.monit.com.kao.devices.HubApInfo;
import goodmonit.monit.com.kao.devices.HubGraphInfo;
import goodmonit.monit.com.kao.devices.LampGraphInfo;
import goodmonit.monit.com.kao.devices.MovementGraphInfo;
import goodmonit.monit.com.kao.devices.SensingData;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.DebugManager;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.NotiManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.em;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class ConnectionManager extends Service {
    private static final String TAG = Configuration.BASE_TAG + "ConnectionMgr";
	private static final boolean DBG = Configuration.DBG;

	// Constants that indicate the current connection state
    public static final int STATE_UNAVAILABLE	= -99; // Bluetooth is not supported
    public static final int STATE_DISABLED		= -1; // Bluetooth is supported but not enabled
    public static final int STATE_DISCONNECTED	= 1;  // now listening for incoming connections
    public static final int STATE_SCANNING 		= 2;  // now scanning remote devices
    public static final int STATE_CONNECTING	= 3;  // now connecting to a remote device
    public static final int STATE_CONNECTED 	= 4;  // now connected to a remote device

	// Message types sent from the BluetoothChatService Handler
	/**
	 * related to message
	 */
    private static final int BASE									= R.string.app_name;
    public static final int MSG_REGISTERED_DEVICE_UPDATE			= BASE + 1;
    public static final int MSG_BLE_CONNECTION_STATE_CHANGE 		= BASE + 2;
	public static final int MSG_BLE_MANUALLY_CONNECTED				= BASE + 3;
	public static final int MSG_CONNECTION_ERROR 					= BASE + 4;
	public static final int MSG_WIFI_CONNECTION_STATE_CHANGE 		= BASE + 5;
	public static final int MSG_BLE_MANUAL_CONNECTION_TIME_OUT		= BASE + 6;
	public static final int MSG_BLE_MANUAL_CONNECTION_GUEST			= BASE + 7;
	public static final int MSG_BLE_GATT_CONNECTION_ERROR			= BASE + 8; // Gatt Connection이 실패하는 경우 앱 강제종료 필요

    public static final int MSG_SCAN_RESULT							= BASE + 9;
	public static final int MSG_SCAN_FINISHED						= BASE + 10;
    public static final int MSG_LEGACY_SCAN_FINISHED				= BASE + 11;
	public static final int MSG_LE_SCAN_FINISHED					= BASE + 12;

	public static final int MSG_SENSOR_VALUE_UPDATED				= BASE + 20;
	public static final int MSG_LAMP_VALUE_UPDATED					= BASE + 21;
	public static final int MSG_SENSOR_BABY_INFO_UPDATED			= BASE + 22;
	public static final int MSG_SET_DEVICE_DATA						= BASE + 23;
	public static final int MSG_INIT_DEVICE							= BASE + 24;
	public static final int MSG_HUB_CONNECTED_WITH_SENSOR  			= BASE + 25;
	public static final int MSG_HUB_WIFI_CONNECTION_STATE_CHANGE 	= BASE + 26;
	public static final int MSG_HUB_WIFI_SCAN_LIST				 	= BASE + 27;

	//public static final int MSG_GET_DEVICE_ID_FROM_CLOUD	 		= BASE + 30;
	//public static final int MSG_UPDATE_DEVICE_STATUS_FROM_CLOUD		= BASE + 35;

	public static final int MSG_GET_DEVICE_STATUS_FROM_CLOUD		= BASE + 40;
	public static final int MSG_BETA_TEST_INPUT_ALARM				= BASE + 41;
	public static final int MSG_SET_USER_INFO_DATA_FROM_CLOUD		= BASE + 42;
	public static final int MSG_SET_DEVICE_STATUS_TO_CLOUD			= BASE + 43;
	public static final int MSG_SET_DIAPER_SENSING_DATA_TO_CLOUD	= BASE + 44;
	public static final int MSG_SET_CLOUD_ID_TO_CLOUD			 	= BASE + 45;
	public static final int MSG_CHECK_INVALID_TOKEN				 	= BASE + 46;
	public static final int MSG_SET_ELDERLY_DIAPER_SENSING_DATA_TO_CLOUD	= BASE + 47;

	public static final int MSG_UPDATE_SCREEN_DEVICE_OBJECT_VIEW	= BASE + 50;

	public static final int MSG_START_SCAN_FOR_RECONNECT			= BASE + 51;
	public static final int MSG_STOP_SCAN_FOR_RECONNECT				= BASE + 52;

	public static final int MSG_NOTIFICATION_MESSAGE_UPDATED		= BASE + 60;
	public static final int MSG_USER_INFO_UPDATED					= BASE + 61;

	public static final int MSG_NOTIFICATION_MESSAGE_RECEIVED		= BASE + 63;
	public static final int MSG_HUB_GRAPH_DATA_RECEIVED				= BASE + 64;
	public static final int MSG_LAMP_GRAPH_DATA_RECEIVED			= BASE + 65;
	public static final int MSG_MOVEMENT_GRAPH_DATA_RECEIVED		= BASE + 66;
	public static final int MSG_SLEEP_GRAPH_DATA_RECEIVED			= BASE + 67;

	public static final int MSG_DISABLE_FAST_DETECTION				= BASE + 70;
	private int countScanForReconnect = 0;
	private long nextScanInterval = 0;
	//public static final int MSG_UPLOAD_BETA_LOG_TO_SERVER			= BASE + 42;

	/**
	 * related to time
	 */
	public static final long TIME_GET_DEVICE_STATUS_FROM_CLOUD_SEC		= 10;
	public static final long TIME_SET_DEVICE_STATUS_FROM_CLOUD_SEC		= 30;
	public static final long TIME_GET_DEVICE_STATUS_FROM_CLOUD_FOR_CERT_SEC		= 1;
	public static final long TIME_SET_DEVICE_STATUS_FROM_CLOUD_FOR_CERT_SEC		= 1;
	public static final long TIME_BLE_MANUAL_SCAN_TIME_OUT_SEC			= 7;
	public static final long TIME_BLE_LEGACY_SCAN_TIME_OUT_SEC			= 3;
	public static final long TIME_BLE_LE_SCAN_TIME_OUT_SEC				= 3;

	public static final long TIME_BLE_MANUAL_CONNECTION_TIME_OUT_SEC	= 20;
	public static final long TIME_BETA_TEST_INPUT_ALARM_PERIOD_MIN		= 20;

	// Reconnect
	public static final long TIME_BLE_BACKGROUND_SCAN_TIME_OUT_SEC_FOR_FOREGROUND_APP 		= 3; // 3초간 스캔
	public static final long TIME_BLE_BACKGROUND_SCAN_TIME_OUT_SEC_FOR_BACKGROUND_APP		= 3; // 3초간 스캔

	public static final long TIME_MAX_BLE_BACKGROUND_SCAN_PERIOD_SEC_FOR_FOREGROUND_APP 	= 5;// 스캔시작 후 다음 스캔시작까지 10초, 스캔시간이 3초이면 스캔 종료후 7초 뒤에 다시 스캔 시작
	public static final long TIME_MAX_BLE_BACKGROUND_SCAN_PERIOD_SEC_FOR_BACKGROUND_APP 	= 30;// 스캔시작 후 다음 스캔시작까지 10초, 스캔시간이 3초이면 스캔 종료후 7초 뒤에 다시 스캔 시작

	public static final int MAX_COUNT_GET_DEVICE_STATUS_FROM_CLOUD_DONE		= 60;
	private int countGetDeviceStatusDone = 0;

    private Context mContext;

	private boolean isDiscovering = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private PreferenceManager mPrefManager;
    
    private static ConnectionManager mConnectionManager;
    private static Handler mUpperLayerHandler;

	private ServerQueryManager mServerQueryMgr;
	private DatabaseManager mDatabaseMgr;
	private DebugManager mDebugMgr;
	private AutoReconnectManager mReconnectMgr;
	private UserInfoManager mUserInfoMgr;
	private DeviceInfo mLatestInitDeviceInfo;
	private NotificationManager mNotificationMgr;

    private boolean isManuallyStopConnecting = false;
    private boolean isFullDiscoveryForAutoReconnect = false;

	//public static HashMap<String, DeviceAirQualityMonitoringHub> mRegisteredAirQualityMonitoringHub = new HashMap<>();
	//public static HashMap<String, DeviceMonitSensor> mRegisteredMonitSensor = new HashMap<>();
	//public static HashMap<String, Device> mRegisteredDevices = new HashMap<>();
	private DeviceBLEConnection mManuallyConnectingDevice;

	// Device 종류 2가지로 나누어짐
	// 1. View를 위한
	public static HashMap<Long, DeviceAQMHub> mRegisteredAQMHubList = new HashMap<>();
	public static HashMap<Long, DeviceDiaperSensor> mRegisteredDiaperSensorList = new HashMap<>();
	public static HashMap<Long, DeviceElderlyDiaperSensor> mRegisteredElderlyDiaperSensorList = new HashMap<>();
	public static HashMap<Long, DeviceLamp> mRegisteredLampList = new HashMap<>();
	public static ArrayList<Long> mDeviceInfoListFromCloud = new ArrayList<>(); // DeviceId * 10 + type
	// 2. BLE Connection을 위한
	private static HashMap<Long, DeviceBLEConnection> mRegisteredBleDeviceList = new HashMap<>();

	public static ConnectionLog mConnectionLog = new ConnectionLog();

	private static HashMap<Long, Boolean> mGetNotificationTable = new HashMap<>();
	private static HashMap<Long, Boolean> mGetCloudNotificationTable = new HashMap<>();
	private static HashMap<Long, Boolean> mGetNotificationEditTable = new HashMap<>();
	private static HashMap<Long, Boolean> mGetHubGraphTable = new HashMap<>();
	private static HashMap<Long, Boolean> mGetMovementGraphTable = new HashMap<>();
	private static HashMap<Long, Boolean> mGetLampGraphTable = new HashMap<>();
	private static HashMap<Long, Boolean> mGetSleepGraphTable = new HashMap<>();

	@Override
	public void onCreate() {
		if (DBG) Log.i(TAG, "onCreate");
		mConnectionManager = this;
		mContext = this;

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

		mPrefManager = PreferenceManager.getInstance(mContext);

		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mDatabaseMgr = DatabaseManager.getInstance(mContext);
		mDebugMgr = DebugManager.getInstance(mContext);
		mUserInfoMgr = UserInfoManager.getInstance(mContext);
		mReconnectMgr = new AutoReconnectManager();
		mNotificationMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		_loadDeviceInfo();

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
		}
		//filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
		registerReceiver(mReceiver, filter);

		reconnectBleDevice();

		startGetDeviceStatusFromCloud();
		startSetDeviceStatusToCloud();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		return START_STICKY;
	}

    @Override
    public void onDestroy() {
        if (DBG) Log.i(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
    }

    public static ConnectionManager getInstance(Handler handler) {
    	mUpperLayerHandler = handler;
    	if (DBG) Log.d(TAG, "getInstance(" + handler + ") : " + mConnectionManager);
    	return mConnectionManager;
    }

	public static ConnectionManager getInstance() {
		if (DBG) Log.d(TAG, "getInstance() : " + mConnectionManager);
		return mConnectionManager;
	}

    private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder {
    	public ConnectionManager getService(Context context, Handler handler) {
    		mContext = context;
    		mUpperLayerHandler = handler;
    		return ConnectionManager.this;
    	}
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
	    return super.onUnbind(intent); 
	}

	/**
	 *  _loadDeviceInfo
	 *  Service 최초 구동시, 저장된 DeviceInfo 리스트 불러오기, BLE연결 시도해야하는 Device 리스트 불러오기
	 */
	private void _loadDeviceInfo() {
		ArrayList<DeviceInfo> deviceInfoList = mDatabaseMgr.getDeviceInfoList();

		// 1. View를 위한 객체
		if (deviceInfoList == null) {
			return;
		}
		if (DBG) Log.i(TAG, "_loadDeviceInfo : " + deviceInfoList.size());

		for (DeviceInfo deviceInfo : deviceInfoList) {
			switch (deviceInfo.type) {
				case DeviceType.DIAPER_SENSOR:
					DeviceDiaperSensor sensor = new DeviceDiaperSensor(mContext, deviceInfo);
					sensor.hasBleConnected = true;
					mRegisteredDiaperSensorList.put(deviceInfo.deviceId, sensor);
					ConnectionManager.putDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type, new DeviceBLEConnection(mContext, deviceInfo, mHandler, true));
					break;
				case DeviceType.AIR_QUALITY_MONITORING_HUB:
					mRegisteredAQMHubList.put(deviceInfo.deviceId, new DeviceAQMHub(mContext, deviceInfo));
					break;
				case DeviceType.LAMP:
					DeviceLamp lamp = new DeviceLamp(mContext, deviceInfo);
					mRegisteredLampList.put(deviceInfo.deviceId, lamp);
					ConnectionManager.putDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type, new DeviceBLEConnection(mContext, deviceInfo, mHandler, true));
					break;
				case DeviceType.ELDERLY_DIAPER_SENSOR:
					DeviceElderlyDiaperSensor elderlyDiaperSennsor = new DeviceElderlyDiaperSensor(mContext, deviceInfo);
					elderlyDiaperSennsor.hasBleConnected = true;
					mRegisteredElderlyDiaperSensorList.put(deviceInfo.deviceId, elderlyDiaperSennsor);
					ConnectionManager.putDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type, new DeviceBLEConnection(mContext, deviceInfo, mHandler, true));
					break;
			}
		}
		printRegisteredDeviceList();
	}

	public static DeviceDiaperSensor getDeviceDiaperSensor(long deviceId) {
		return mRegisteredDiaperSensorList.get(deviceId);
	}

	public static DeviceElderlyDiaperSensor getDeviceElderlyDiaperSensor(long deviceId) {
		return mRegisteredElderlyDiaperSensorList.get(deviceId);
	}

	public static DeviceAQMHub getDeviceAQMHub(long deviceId) {
		return mRegisteredAQMHubList.get(deviceId);
	}

	public static DeviceLamp getDeviceLamp(long deviceId) {
		return mRegisteredLampList.get(deviceId);
	}

	public static HashMap<Long, DeviceBLEConnection> getDeviceBLEConnectionList() {
		return mRegisteredBleDeviceList;
	}

	public static DeviceBLEConnection getDeviceBLEConnection(long deviceId, int deviceType) {
		return mRegisteredBleDeviceList.get(deviceId * 10 + deviceType);
	}

	public static void putDeviceBLEConnection(long deviceId, int deviceType, DeviceBLEConnection bleConnection) {
		mRegisteredBleDeviceList.put(deviceId * 10 + deviceType, bleConnection);
	}

	public static void removeDeviceBLEConnection(DeviceBLEConnection bleConnection) {
		mRegisteredBleDeviceList.remove(bleConnection);
	}

	public static void removeDeviceBLEConnection(long deviceId, int deviceType) {
		mRegisteredBleDeviceList.remove(deviceId * 10 + deviceType);
	}

	public void sendNotificationUpdatedMessage() {
		mHandler.sendEmptyMessage(MSG_NOTIFICATION_MESSAGE_UPDATED);
	}

	/**
	 *  clearRegisteredDevices
	 *  로그아웃, 회원탈퇴 시 연결 모두 종료 후 목록에서 삭제
	 */
	public void clearRegisteredDevices() {
		if (DBG) Log.i(TAG, "clearDeviceInfo");
		if (mRegisteredBleDeviceList != null) {
			for (Map.Entry<Long, DeviceBLEConnection> entry : mRegisteredBleDeviceList.entrySet()) {
				DeviceBLEConnection bleConnection = entry.getValue();
				if (bleConnection != null) {
					bleConnection.close();
				}
			}
		}

		if (mRegisteredBleDeviceList != null) {
			mRegisteredBleDeviceList.clear();
		}
		if (mRegisteredDiaperSensorList != null) {
			mRegisteredDiaperSensorList.clear();
		}
		if (mRegisteredAQMHubList != null) {
			mRegisteredAQMHubList.clear();
		}
		if (mRegisteredElderlyDiaperSensorList != null) {
			mRegisteredElderlyDiaperSensorList.clear();
		}
		if (mDatabaseMgr != null) {
			mDatabaseMgr.initDeviceDB();
		}
	}

	/**
	 *  matchCloudDataWithViewObject
	 *  Cloud에서 받아오는 정보대로 ViewObject를 처리함
	 */

	public void compareCloudWithDeviceViewObject() {
		if (DBG) Log.i(TAG, "compareCloudWithDeviceViewObject");

        // 1. Sensor, Lamp BLEConnection, DB 삭제
        Iterator<DeviceBLEConnection> itr = ConnectionManager.mRegisteredBleDeviceList.values().iterator();
		try {
			while (itr.hasNext()) {
				DeviceBLEConnection bleConnection = itr.next();
				if (bleConnection != null) {
					// 패키지 기기 등록시 센서->허브 순으로 등록하면서 setCloudId 전에 삭제될 수가 있으므로 등록시에는 삭제되지 않도록 막아야 함
					if (bleConnection.isManuallyConnected()) {
						if (DBG) Log.d(TAG, "manually connected: " + bleConnection.toString());
						continue;
					}
					if (bleConnection.getDeviceInfo().type == DeviceType.DIAPER_SENSOR) {
						if (!mDeviceInfoListFromCloud.contains(bleConnection.getDeviceInfo().deviceId * 10 + DeviceType.DIAPER_SENSOR)) {
							bleConnection.unregister();
							itr.remove();
						}
					} else if (bleConnection.getDeviceInfo().type == DeviceType.LAMP) {
						if (!mDeviceInfoListFromCloud.contains(bleConnection.getDeviceInfo().deviceId * 10 + DeviceType.LAMP)) {
							bleConnection.unregister();
							itr.remove();
						}
					} else if (bleConnection.getDeviceInfo().type == DeviceType.ELDERLY_DIAPER_SENSOR) {
						if (!mDeviceInfoListFromCloud.contains(bleConnection.getDeviceInfo().deviceId * 10 + DeviceType.ELDERLY_DIAPER_SENSOR)) {
							bleConnection.unregister();
							itr.remove();
						}
					}
				}
			}
		} catch (ConcurrentModificationException e) {
			if (DBG) Log.e(TAG, "Remove BLEConnection exception : " + e.toString());
		}

        // 2. Sensor ObjectView, Message 삭제
        Iterator<DeviceDiaperSensor> itrDiaperSensor = ConnectionManager.mRegisteredDiaperSensorList.values().iterator();
		try {
			while (itrDiaperSensor.hasNext()) {
				DeviceDiaperSensor diaperSensor = itrDiaperSensor.next();

				if (diaperSensor == null) continue;

				// 패키지 기기 등록시 센서->허브 순으로 등록하면서 setCloudId 전에 삭제될 수가 있으므로 등록시에는 삭제되지 않도록 막아야 함
				DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(diaperSensor.deviceId, DeviceType.DIAPER_SENSOR);
				if (bleConnection != null && bleConnection.isManuallyConnected()) {
					if (DBG) Log.d(TAG, "manually connected: " + bleConnection.toString());
					continue;
				}

				if (!mDeviceInfoListFromCloud.contains(diaperSensor.getDeviceInfo().deviceId * 10 + DeviceType.DIAPER_SENSOR)) {
					if (DBG) Log.d(TAG, "remove DiaperSensor(" + diaperSensor.getDeviceInfo().deviceId + ")");
					mDatabaseMgr.deleteNotificationMessages(DeviceType.DIAPER_SENSOR, diaperSensor.getDeviceInfo().deviceId);
					mDatabaseMgr.deleteMovementGraphInfoDB(diaperSensor.getDeviceInfo().deviceId);
					mPrefManager.initDiaperSensorPreference(diaperSensor.getDeviceInfo().deviceId);
					itrDiaperSensor.remove();
				}
			}
		} catch (ConcurrentModificationException e) {
			if (DBG) Log.e(TAG, "Remove Sensor object exception : " + e.toString());
		}

		// 3. Lamp ObjectView, Message 삭제
		Iterator<DeviceLamp> itrLamp = ConnectionManager.mRegisteredLampList.values().iterator();
		try {
			while (itrLamp.hasNext()) {
				DeviceLamp lamp = itrLamp.next();
				if (!mDeviceInfoListFromCloud.contains(lamp.getDeviceInfo().deviceId * 10 + DeviceType.LAMP)) {
					mDatabaseMgr.deleteNotificationMessages(DeviceType.LAMP, lamp.getDeviceInfo().deviceId);
					itrLamp.remove();
				}
			}
		} catch (ConcurrentModificationException e) {
			if (DBG) Log.e(TAG, "Remove Sensor object exception : " + e.toString());
		}

		// 4. Elderly Sensor ObjectView, Message 삭제
		Iterator<DeviceElderlyDiaperSensor> itrElderlyDiaperSensor = ConnectionManager.mRegisteredElderlyDiaperSensorList.values().iterator();
		try {
			while (itrElderlyDiaperSensor.hasNext()) {
				DeviceElderlyDiaperSensor diaperSensor = itrElderlyDiaperSensor.next();

				if (diaperSensor == null) continue;

				// 패키지 기기 등록시 센서->허브 순으로 등록하면서 setCloudId 전에 삭제될 수가 있으므로 등록시에는 삭제되지 않도록 막아야 함
				DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(diaperSensor.deviceId, DeviceType.ELDERLY_DIAPER_SENSOR);
				if (bleConnection != null && bleConnection.isManuallyConnected()) {
					if (DBG) Log.d(TAG, "manually connected: " + bleConnection.toString());
					continue;
				}

				if (!mDeviceInfoListFromCloud.contains(diaperSensor.getDeviceInfo().deviceId * 10 + DeviceType.ELDERLY_DIAPER_SENSOR)) {
					if (DBG) Log.d(TAG, "remove DiaperSensor(" + diaperSensor.getDeviceInfo().deviceId + ")");
					mDatabaseMgr.deleteNotificationMessages(DeviceType.ELDERLY_DIAPER_SENSOR, diaperSensor.getDeviceInfo().deviceId);
					mDatabaseMgr.deleteMovementGraphInfoDB(diaperSensor.getDeviceInfo().deviceId);
					mPrefManager.initDiaperSensorPreference(diaperSensor.getDeviceInfo().deviceId);
					itrElderlyDiaperSensor.remove();
				}
			}
		} catch (ConcurrentModificationException e) {
			if (DBG) Log.e(TAG, "Remove Sensor object exception : " + e.toString());
		}

        // 5. 클라우드에서 받아온 데이터에는 없는데 Hub 리스트에 있는 데이터 삭제
        Iterator<DeviceAQMHub> itrAQMHub = ConnectionManager.mRegisteredAQMHubList.values().iterator();
		try {
			while (itrAQMHub.hasNext()) {
				DeviceAQMHub hub = itrAQMHub.next();
				if (!mDeviceInfoListFromCloud.contains(hub.getDeviceInfo().deviceId * 10 + DeviceType.AIR_QUALITY_MONITORING_HUB)) {
					mDatabaseMgr.deleteNotificationMessages(DeviceType.AIR_QUALITY_MONITORING_HUB, hub.getDeviceInfo().deviceId);
					itrAQMHub.remove();
				}
			}
		} catch (ConcurrentModificationException e) {
			if (DBG) Log.e(TAG, "Remove Hub object exception : " + e.toString());
		}
	}

	/**
	 *  leaveGroup
	 *  기기 지우기 및 그룹탈퇴 시 호출하는 함수로,
	 *  해당하는 기기(들)의 ObjectView 와 BLE Connection, Message관련 내용 삭제
	 */
	public void leaveGroup(long cloudId) {
		if (DBG) Log.d(TAG, "leaveGroup : " + cloudId);

        // 1. Sensor BLEConnection, DB 삭제
        Iterator<DeviceBLEConnection> itr = ConnectionManager.mRegisteredBleDeviceList.values().iterator();
		try {
			while (itr.hasNext()) {
				DeviceBLEConnection bleConnection = itr.next();
				if (bleConnection.getDeviceInfo().cloudId == cloudId) {
					bleConnection.unregister();
					itr.remove();
				}
			}
		} catch(ConcurrentModificationException e) {
			if (DBG) Log.d(TAG, "ConcurrentModificationException[1] : " + e);
		}

		// 2. Sensor ObjectView, Message 삭제
        Iterator<DeviceDiaperSensor> itrDiaperSensor = ConnectionManager.mRegisteredDiaperSensorList.values().iterator();
		try {
			while (itrDiaperSensor.hasNext()) {
				DeviceDiaperSensor diaperSensor = itrDiaperSensor.next();
				if (diaperSensor.getDeviceInfo().cloudId == cloudId) {
					mDatabaseMgr.deleteNotificationMessages(DeviceType.DIAPER_SENSOR, diaperSensor.getDeviceInfo().deviceId);
					mDatabaseMgr.deleteMovementGraphInfoDB(diaperSensor.getDeviceInfo().deviceId);
					mPrefManager.initDiaperSensorPreference(diaperSensor.getDeviceInfo().deviceId);
					itrDiaperSensor.remove();
				}
			}
		} catch(ConcurrentModificationException e) {
			if (DBG) Log.d(TAG, "ConcurrentModificationException[2] : " + e);
		}

		// 3. Hub ObjectView, Message 삭제
        Iterator<DeviceAQMHub> itrAQMHub = ConnectionManager.mRegisteredAQMHubList.values().iterator();
		try {
			while (itrAQMHub.hasNext()) {
				DeviceAQMHub hub = itrAQMHub.next();
				if (hub.getDeviceInfo().cloudId == cloudId) {
					mDatabaseMgr.deleteNotificationMessages(DeviceType.AIR_QUALITY_MONITORING_HUB, hub.getDeviceInfo().deviceId);
					mPrefManager.initAQMHubPreference(hub.getDeviceInfo().deviceId);
					itrAQMHub.remove();
				}
			}
		} catch(ConcurrentModificationException e) {
			if (DBG) Log.d(TAG, "ConcurrentModificationException[3] : " + e);
		}

		// 4. Lamp ObjectView, Message 삭제
		Iterator<DeviceLamp> itrLamp = ConnectionManager.mRegisteredLampList.values().iterator();
		try {
			while (itrLamp.hasNext()) {
				DeviceLamp lamp = itrLamp.next();
				if (lamp.getDeviceInfo().cloudId == cloudId) {
					mDatabaseMgr.deleteNotificationMessages(DeviceType.LAMP, lamp.getDeviceInfo().deviceId);
					mPrefManager.initLampPreference(lamp.getDeviceInfo().deviceId);
					itrLamp.remove();
				}
			}
		} catch(ConcurrentModificationException e) {
			if (DBG) Log.d(TAG, "ConcurrentModificationException[2] : " + e);
		}

		// 5. Elderly Diaper Sensor ObjectView, Message 삭제
		Iterator<DeviceElderlyDiaperSensor> itrElderlyDiaperSensor = ConnectionManager.mRegisteredElderlyDiaperSensorList.values().iterator();
		try {
			while (itrElderlyDiaperSensor.hasNext()) {
				DeviceElderlyDiaperSensor diaperSensor = itrElderlyDiaperSensor.next();
				if (diaperSensor.getDeviceInfo().cloudId == cloudId) {
					mDatabaseMgr.deleteNotificationMessages(DeviceType.ELDERLY_DIAPER_SENSOR, diaperSensor.getDeviceInfo().deviceId);
					mDatabaseMgr.deleteMovementGraphInfoDB(diaperSensor.getDeviceInfo().deviceId);
					mPrefManager.initElderlyDiaperSensorPreference(diaperSensor.getDeviceInfo().deviceId);
					itrElderlyDiaperSensor.remove();
				}
			}
		} catch(ConcurrentModificationException e) {
			if (DBG) Log.d(TAG, "ConcurrentModificationException[2] : " + e);
		}

		printRegisteredDeviceList();
	}

	/**
	 *  removeRegisteredDevice
	 *  기기 초기화/삭제 시 목록에서 삭제
	 */
	public void removeRegisteredDevice(int type, long deviceId) {
		if (DBG) Log.d(TAG, "removeRegisteredDevice : " + type + " / " + deviceId);
		// BLE Connection 목록에서 삭제
		DeviceBLEConnection bleConnection = getDeviceBLEConnection(deviceId, type);
		if (bleConnection != null) {
			bleConnection.close();
			removeDeviceBLEConnection(deviceId, type);
		}

		if (type == DeviceType.DIAPER_SENSOR) {
			mRegisteredDiaperSensorList.remove(deviceId);
		} else if (type == DeviceType.AIR_QUALITY_MONITORING_HUB) {
			mRegisteredAQMHubList.remove(deviceId);
		} else if (type == DeviceType.LAMP) {
			mRegisteredLampList.remove(deviceId);
		} else if (type == DeviceType.ELDERLY_DIAPER_SENSOR) {
			mRegisteredElderlyDiaperSensorList.remove(deviceId);
		}
		printRegisteredDeviceList();
	}

	public void printRegisteredDeviceList() {
		{
			if (DBG) Log.d(TAG, "=====DiaperSensorList=====");
			for (DeviceDiaperSensor sensor : mRegisteredDiaperSensorList.values()) {
				if (DBG) Log.d(TAG, sensor.toString());
			}

			if (DBG) Log.d(TAG, "=====AQMHubList=====");
			for (DeviceAQMHub hub : mRegisteredAQMHubList.values()) {
				if (DBG) Log.d(TAG, hub.toString());
			}

			if (DBG) Log.d(TAG, "=====LAMPList=====");
			for (DeviceLamp lamp : mRegisteredLampList.values()) {
				if (DBG) Log.d(TAG, lamp.toString());
			}

			if (DBG) Log.d(TAG, "=====ElderlyDiaperSensorList=====");
			for (DeviceElderlyDiaperSensor sensor : mRegisteredElderlyDiaperSensorList.values()) {
				if (DBG) Log.d(TAG, sensor.toString());
			}

			if (DBG) Log.d(TAG, "=====BLEConnectionList=====");
			for (DeviceBLEConnection bleconnection : mRegisteredBleDeviceList.values()) {
				if (DBG) Log.d(TAG, bleconnection.toString());
			}
		}
	}

	/**
	 *  removeRegisteredDevice
	 *  기기 초기화/삭제 시 목록에서 삭제
	 */
	public void removeRegisteredBleConnectionDevice(int type, long deviceId) {
		// BLE Connection 목록에서 삭제

	}

	/**
	 *  getRegisteredDeviceTotalCount
	 *  전체 등록된 Device의 총 갯수구하기
	 */
	public static int getRegisteredDeviceTotalCount() {
		return mRegisteredDiaperSensorList.size() + mRegisteredAQMHubList.size() + mRegisteredLampList.size() + mRegisteredElderlyDiaperSensorList.size();
	}

	/**
	 *  getUserInfoFromCloud
	 *  클라우드로부터 1.내정보, 2.Group멤버정보, 3.디바이스정보 를 받아와 처리한다.
	 */
	public void getUserInfoFromCloud() {
		mServerQueryMgr.getUserInfo(new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errCode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
					mHandler.obtainMessage(MSG_SET_USER_INFO_DATA_FROM_CLOUD, data).sendToTarget();
				}
			}
		});
	}

	/**
	 *  _setUserInfoFromCloud
	 *  클라우드로부터 1.내정보, 2.Group멤버정보, 3.디바이스정보 를 받아와 처리한다.
	 *  기존 getUserInfoFromCloud 에서 모두 처리하려 했으나,
	 *  java.lang.RuntimeException: Cant create handler inside thread that has not called Looper.prepare()
	 *  에러 발생으로 핸들러를 통해 별도의 내부 함수를 호출한다.
	 *  내부적으로 붙일 BLE센서, 붙이지 않을 BLE센서
	 */
	private void _setUserInfoFromCloud(String data) {
		if (data == null) return;
		try {
			JSONObject jObject = new JSONObject(data);

			// 1. 내정보 저장
			String myNickname = jObject.getString(mServerQueryMgr.getParameter(16));
			String myBirthday = jObject.getString(mServerQueryMgr.getParameter(17));
			int mySex = jObject.getInt(mServerQueryMgr.getParameter(18));
			String myShortId = jObject.getString(mServerQueryMgr.getParameter(22));
            String myEmail = jObject.getString(mServerQueryMgr.getParameter(12));

			mPrefManager.setProfileNickname(myNickname);
			mPrefManager.setProfileBirthday(myBirthday);
			mPrefManager.setProfileSex(mySex);
			mPrefManager.setShortId(myShortId);

			// 2. Group멤버 저장 갱신
			mUserInfoMgr.initUserInfoList();
			mUserInfoMgr.initGroupList();
			String member = jObject.getString(mServerQueryMgr.getParameter(48));

			ArrayList<Long> cloudList = new ArrayList<Long>();
			if (member != null) {
				JSONArray jarr = new JSONArray(member);
				for (int i = 0; i < jarr.length(); i++) {
					JSONObject jobj = jarr.getJSONObject(i);
					long cloudId = jobj.getLong(mServerQueryMgr.getParameter(29));
					long accountId = jobj.getLong(mServerQueryMgr.getParameter(3));
					String nickname = jobj.getString(mServerQueryMgr.getParameter(16));
					String email = jobj.getString(mServerQueryMgr.getParameter(12));
					String shortid = jobj.getString(mServerQueryMgr.getParameter(22));
					int familyType = jobj.getInt(mServerQueryMgr.getParameter(23));

					mUserInfoMgr.addUserInfo(new UserInfo(accountId, cloudId, email, nickname, shortid, familyType));
				}
			}

			// 3. Device정보 저장
			String device = jObject.getString(mServerQueryMgr.getParameter(10));
			if (device != null) {
				JSONArray jarr = new JSONArray(device);
				for (int i = 0; i < jarr.length(); i++) {
					JSONObject jobj = jarr.getJSONObject(i);
					int type = jobj.optInt(mServerQueryMgr.getParameter(28), 0);
					long cloudId = jobj.optLong(mServerQueryMgr.getParameter(29), 0);
					long deviceId = jobj.optLong(mServerQueryMgr.getParameter(26), 0);
					String name = jobj.optString(mServerQueryMgr.getParameter(25), null);
					String serial = jobj.optString(mServerQueryMgr.getParameter(44), null);
					String macAddr = jobj.optString(mServerQueryMgr.getParameter(45), null);
					String firmwareVersion = jobj.optString(mServerQueryMgr.getParameter(30), null);
					String alarmEnabled = jobj.optString(mServerQueryMgr.getParameter(46), null);
					String advName = jobj.optString(mServerQueryMgr.getParameter(156), null);
					HashMap<String, String> alarmHashmap = new HashMap<>();
					if (alarmEnabled != null) {
						String[] alarmEnabledSplit = alarmEnabled.split("/");
						if (alarmEnabledSplit != null) {
							for (String alm : alarmEnabledSplit) {
								String[] almSplit = alm.split(",");
								if (almSplit == null || almSplit.length != 2) continue;
								alarmHashmap.put(almSplit[0], almSplit[1]);
							}
						}
					}
					// 초기화하는 도중, 서버로부터 데이터를 가져와서 세팅하는 이슈발생
					if (mLatestInitDeviceInfo != null && mLatestInitDeviceInfo.type == type && mLatestInitDeviceInfo.deviceId == deviceId) {
						continue;
					}

					if (type == DeviceType.DIAPER_SENSOR) {
						DeviceDiaperSensor sensor = ConnectionManager.getDeviceDiaperSensor(deviceId);
						if (sensor == null) {
							if (DBG) Log.d(TAG, "Add new diaper sensor from server : " + deviceId);
							sensor = new DeviceDiaperSensor(mContext);
							mRegisteredDiaperSensorList.put(deviceId, sensor);
						} else {
							if (DBG) Log.d(TAG, "Already have local diaper sensor, update it : " + deviceId);
						}
						sensor.deviceId = deviceId;
						sensor.cloudId = cloudId;
						sensor.firmwareVersion = firmwareVersion;
						if (name !=  null) {
							sensor.setName(name);
							mPrefManager.setDeviceName(DeviceType.DIAPER_SENSOR, deviceId, name);
						}
						sensor.serial = serial;
						if (serial != null && serial.length() > 10 && mPrefManager.getDeviceSerialNumber(DeviceType.DIAPER_SENSOR, deviceId) == null) {
							if (DBG) Log.d(TAG, "set sensor serial number: " + deviceId + " / " + serial);
							mPrefManager.setDeviceSerialNumber(DeviceType.DIAPER_SENSOR, deviceId, serial);
						}

						// 알람 정리
						Iterator<String> itr = alarmHashmap.keySet().iterator();
						int notiType;
						while (itr.hasNext()) {
							String key = itr.next();
							try {
								notiType = Integer.parseInt(key);
							} catch (NumberFormatException e) {
								notiType = -1;
							}

							boolean enabled = "1".equals(alarmHashmap.get(key));
							mPrefManager.setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, notiType, enabled);
							if (DBG) Log.d (TAG, "sensor alarm setting: " + deviceId + " / " + notiType + " / " + enabled);
						}

						if (macAddr != null) {
							String[] tokenMac = macAddr.split(":");
							if (tokenMac.length == 6) {
								advName = "MONIT_Diaper(" + tokenMac[5] + tokenMac[4] + tokenMac[3] + ")";
							}
						}

						DeviceInfo deviceInfo = new DeviceInfo(deviceId, cloudId, DeviceType.DIAPER_SENSOR, name, macAddr, serial, firmwareVersion, advName, true, true, true, true, true);
						if (deviceInfo.insertDB(mContext) == -1) {
							if (DBG) Log.d(TAG, "Already DB inserted device : " + deviceInfo.toString());
						}
						if (getDeviceBLEConnection(deviceId, DeviceType.DIAPER_SENSOR) == null) {
							if (DBG) Log.d(TAG, "Add Ble Connection Device : " + deviceInfo.toString());
							DeviceBLEConnection bleConn = new DeviceBLEConnection(mContext, deviceInfo, mHandler, true);
							putDeviceBLEConnection(deviceId, DeviceType.DIAPER_SENSOR, bleConn);
						}
					} else if (type == DeviceType.AIR_QUALITY_MONITORING_HUB) {
						DeviceAQMHub hub = ConnectionManager.getDeviceAQMHub(deviceId);
						if (hub == null) {
							if (DBG) Log.d(TAG, "Add new hub from server : " + deviceId);
							hub = new DeviceAQMHub(mContext);
							mRegisteredAQMHubList.put(deviceId, hub);
						} else {
							if (DBG) Log.d(TAG, "Already have local hub, update it : " + deviceId);
						}
						hub.deviceId = deviceId;
						hub.cloudId = cloudId;
						hub.firmwareVersion = firmwareVersion;
						if (name !=  null) {
							hub.setName(name);
							mPrefManager.setDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, name);
						}
						hub.serial = serial;
						if (serial != null && serial.length() > 10 && mPrefManager.getDeviceSerialNumber(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId) == null) {
							if (DBG) Log.d(TAG, "set hub serial number: " + deviceId + " / " + serial);
							mPrefManager.setDeviceSerialNumber(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, serial);
						}
						// 알람 정리
						Iterator<String> itr = alarmHashmap.keySet().iterator();
						int notiType;
						while (itr.hasNext()) {
							String key = itr.next();
							try {
								notiType = Integer.parseInt(key);
							} catch (NumberFormatException e) {
								notiType = -1;
							}

							boolean enabled = "1".equals(alarmHashmap.get(key));
							mPrefManager.setDeviceAlarmEnabled(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, notiType, enabled);
							if (DBG) Log.d (TAG, "hub alarm setting: " + deviceId + " / " + notiType + " / " + enabled);
						}
					} else if (type == DeviceType.LAMP) {
						DeviceLamp lamp = ConnectionManager.getDeviceLamp(deviceId);
						if (lamp == null) {
							if (DBG) Log.d(TAG, "Add new lamp from server : " + deviceId);
							lamp = new DeviceLamp(mContext);
							mRegisteredLampList.put(deviceId, lamp);
						} else {
							if (DBG) Log.d(TAG, "Already have local lamp, update it : " + deviceId);
						}
						lamp.deviceId = deviceId;
						lamp.cloudId = cloudId;
						lamp.firmwareVersion = firmwareVersion;
						if (name !=  null) {
							lamp.setName(name);
							mPrefManager.setDeviceName(DeviceType.LAMP, deviceId, name);
						}
						lamp.serial = serial;
						if (serial != null && serial.length() > 10 && mPrefManager.getDeviceSerialNumber(DeviceType.LAMP, deviceId) == null) {
							if (DBG) Log.d(TAG, "set lamp serial number: " + deviceId + " / " + serial);
							mPrefManager.setDeviceSerialNumber(DeviceType.LAMP, deviceId, serial);
						}
						// 알람 정리
						Iterator<String> itr = alarmHashmap.keySet().iterator();
						int notiType;
						while (itr.hasNext()) {
							String key = itr.next();
							try {
								notiType = Integer.parseInt(key);
							} catch (NumberFormatException e) {
								notiType = -1;
							}

							boolean enabled = "1".equals(alarmHashmap.get(key));
							mPrefManager.setDeviceAlarmEnabled(DeviceType.LAMP, deviceId, notiType, enabled);
							if (DBG) Log.d (TAG, "lamp alarm setting: " + deviceId + " / " + notiType + " / " + enabled);
						}

						if (macAddr != null) {
							String[] tokenMac = macAddr.split(":");
							if (tokenMac.length == 6) {
								advName = "MONIT_Lamp(" + tokenMac[5] + tokenMac[4] + tokenMac[3] + ")";
							}
						}

						DeviceInfo deviceInfo = new DeviceInfo(deviceId, cloudId, DeviceType.LAMP, name, macAddr, serial, firmwareVersion, advName, true, true, true, true, true);
						if (deviceInfo.insertDB(mContext) == -1) {
							if (DBG) Log.d(TAG, "Already DB inserted device : " + deviceInfo.toString());
						}
						if (getDeviceBLEConnection(deviceId, DeviceType.LAMP) == null) {
							if (DBG) Log.d(TAG, "Add Ble Connection Device : " + deviceInfo.toString());
							DeviceBLEConnection bleConn = new DeviceBLEConnection(mContext, deviceInfo, mHandler, true);
							putDeviceBLEConnection(deviceId, DeviceType.LAMP, bleConn);
						}
					} else if (type == DeviceType.ELDERLY_DIAPER_SENSOR) {
						DeviceElderlyDiaperSensor sensor = ConnectionManager.getDeviceElderlyDiaperSensor(deviceId);
						if (sensor == null) {
							if (DBG) Log.d(TAG, "Add new diaper sensor from server : " + deviceId);
							sensor = new DeviceElderlyDiaperSensor(mContext);
							mRegisteredElderlyDiaperSensorList.put(deviceId, sensor);
						} else {
							if (DBG) Log.d(TAG, "Already have local diaper sensor, update it : " + deviceId);
						}
						sensor.deviceId = deviceId;
						sensor.cloudId = cloudId;
						sensor.firmwareVersion = firmwareVersion;
						if (name !=  null) {
							sensor.setName(name);
							mPrefManager.setDeviceName(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, name);
						}
						sensor.serial = serial;
						if (serial != null && serial.length() > 10 && mPrefManager.getDeviceSerialNumber(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId) == null) {
							if (DBG) Log.d(TAG, "set sensor serial number: " + deviceId + " / " + serial);
							mPrefManager.setDeviceSerialNumber(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, serial);
						}

						// 알람 정리
						Iterator<String> itr = alarmHashmap.keySet().iterator();
						int notiType;
						while (itr.hasNext()) {
							String key = itr.next();
							try {
								notiType = Integer.parseInt(key);
							} catch (NumberFormatException e) {
								notiType = -1;
							}

							boolean enabled = "1".equals(alarmHashmap.get(key));
							mPrefManager.setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, notiType, enabled);
							if (DBG) Log.d (TAG, "elderly sensor alarm setting: " + deviceId + " / " + notiType + " / " + enabled);
						}

						if (macAddr != null) {
							String[] tokenMac = macAddr.split(":");
							if (tokenMac.length == 6) {
								advName = "MONIT_Elderly(" + tokenMac[5] + tokenMac[4] + tokenMac[3] + ")";
							}
						}

						DeviceInfo deviceInfo = new DeviceInfo(deviceId, cloudId, DeviceType.ELDERLY_DIAPER_SENSOR, name, macAddr, serial, firmwareVersion, advName, true, true, true, true, true);
						if (deviceInfo.insertDB(mContext) == -1) {
							if (DBG) Log.d(TAG, "Already DB inserted device : " + deviceInfo.toString());
						}
						if (getDeviceBLEConnection(deviceId, DeviceType.ELDERLY_DIAPER_SENSOR) == null) {
							if (DBG) Log.d(TAG, "Add Ble Connection Device : " + deviceInfo.toString());
							DeviceBLEConnection bleConn = new DeviceBLEConnection(mContext, deviceInfo, mHandler, true);
							putDeviceBLEConnection(deviceId, DeviceType.ELDERLY_DIAPER_SENSOR, bleConn);
						}
					}
				}
			}

		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		mUserInfoMgr.refreshGroupList();
		mLatestInitDeviceInfo = null;
		if (mUpperLayerHandler != null) {
			mUpperLayerHandler.obtainMessage(MSG_USER_INFO_UPDATED).sendToTarget();
		}
	}

	/**
	 *  _setDeviceDataFromCloud
	 *  Cloud 로 부터 받아온 데이터를 파싱하여 각 ViewObject에 맞게 넣기
	 */
	private void _setDeviceDataFromCloud(String data) {
		if (DBG) Log.i(TAG, "_setDeviceDataFromCloud");
		boolean addedNewDevice = false;

		try {
			JSONObject wholeObj = new JSONObject(data);
			JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(11));
			for (int i = 0; i < jarr.length(); i++) {
				JSONObject jobj = jarr.getJSONObject(i);
				int type = jobj.optInt(mServerQueryMgr.getParameter(28), 0);
				long deviceId = jobj.optLong(mServerQueryMgr.getParameter(26), 0);
				long cloudId = jobj.optLong(mServerQueryMgr.getParameter(29), 0);

				// 초기화하는 도중, 서버로부터 데이터를 가져와서 세팅하는 이슈발생
				if (mLatestInitDeviceInfo != null && mLatestInitDeviceInfo.type == type && mLatestInitDeviceInfo.deviceId == deviceId) {
					continue;
				}

				mDeviceInfoListFromCloud.add(deviceId * 10 + type);
				if (type == DeviceType.DIAPER_SENSOR && deviceId != 0) {
					DeviceDiaperSensor sensor = getDeviceDiaperSensor(deviceId);
					DeviceBLEConnection bleSensor = getDeviceBLEConnection(deviceId, type);
					/*
					if (bleSensor != null) {
						// BLE로 직접받고 있더라도, 기저귀 초기화, 기저귀 교체, 이름변경 등 바꿔줘야하므로 업데이트 시킴
                        // 직접 받고 있으므로 업데이트 시킬 필요가 없음 But, 연결이 끊어진 상태라면 직접 받아야함
                        if (bleSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                            if (DBG) Log.d(TAG, "ignore update sensor data from did : " + deviceId);
                            continue;
                        }
					}
					*/

					if (sensor == null) {
						sensor = new DeviceDiaperSensor(mContext);
						sensor.deviceId = deviceId;
						if (DBG) Log.d(TAG, "Add new diaper sensor from server : " + deviceId);
						mRegisteredDiaperSensorList.put(deviceId, sensor);
						addedNewDevice = true;
					} else {
						if (DBG) Log.d(TAG, "Already have local diaper sensor, update it : " + deviceId);
					}
					if (cloudId > 0) {
						sensor.cloudId = cloudId;
					}

					if (bleSensor != null) {
						if (DBG) Log.d(TAG, "diaper sensor connection ble: " + bleSensor.getConnectionState() + " / object: " + bleSensor.getConnectionState());
						if (bleSensor.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) { // BLE연결이 아니라면
							if (jobj.optInt(mServerQueryMgr.getParameter(54), DeviceConnectionState.WIFI_CONNECTED) == DeviceConnectionState.DISCONNECTED) {
								// Wi-Fi로 받는 데이터이므로 Wi-Fi 접속만 신경쓰면 됨
								if (sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED) {
									if (mUpperLayerHandler != null) {
										mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.DISCONNECTED, -1, sensor.getDeviceInfo()).sendToTarget();
									}
								}
								sensor.setConnectionState(DeviceConnectionState.DISCONNECTED);
								sensor.setConnectionId(-1);
								bleSensor.setConnectionState(DeviceConnectionState.DISCONNECTED);
							} else {
								// Wi-Fi로 받는 데이터이므로 Wi-Fi 접속만 신경쓰면 됨
								if (sensor.getConnectionState() != DeviceConnectionState.WIFI_CONNECTED) {
									if (mUpperLayerHandler != null) {
										mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.WIFI_CONNECTED, -1, sensor.getDeviceInfo()).sendToTarget();
									}
								}
								sensor.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
								sensor.setConnectionId(jobj.optLong(mServerQueryMgr.getParameter(119), -1));
								bleSensor.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
								if (Configuration.CERTIFICATE_MODE) {
									sensor.setTemperature(jobj.optInt(mServerQueryMgr.getParameter(39), -1) / 100.0f);
									sensor.setHumidity(jobj.optInt(mServerQueryMgr.getParameter(40), -1) / 100.0f);
									sensor.setVoc(jobj.optInt(mServerQueryMgr.getParameter(41), -1) / 100.0f);
								}
							}
						} else {
							sensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
						}
					}

//					if (Configuration.BETA_TEST_MODE) {
//						// 베타테스트 모드이고, 충전중이 아니면 Baseline 알람 울리기
//						// 앱이 background여도 FCM메시지로 인해 10분에 한번씩 호출되어 여기 있어도 됨
//						if (jobj.optInt(mServerQueryMgr.getParameter(50), -1) < DeviceStatus.OPERATION_CABLE_NO_CHARGE) {
//							// 이 센서가 내 센서일때만 보내기
//							if (sensor.cloudId == mPrefManager.getAccountId()) {
//								sensor.checkDiaperStatusForBaseLine();
//							} else {
//								if (DBG) Log.d(TAG, "Not my device");
//							}
//						}
//					}

					if (!Configuration.CERTIFICATE_MODE) {
						sensor.setDiaperScore(jobj.optInt(mServerQueryMgr.getParameter(153), -1));
						sensor.setBatteryPower(jobj.optInt(mServerQueryMgr.getParameter(49), -1));
						sensor.setOperationStatus(jobj.optInt(mServerQueryMgr.getParameter(50), -1));
						sensor.setMovementStatus(jobj.optInt(mServerQueryMgr.getParameter(51), -1));
						sensor.setVocAvg(jobj.optInt(mServerQueryMgr.getParameter(154), -1) / 100.0f);
						int sleep =jobj.optInt(mServerQueryMgr.getParameter(167), -1);
						sensor.setSleepStatus(sleep);
						if(DBG) Log.d(TAG, "sleep : "+sensor+"cpp string : "+mServerQueryMgr.getParameter(167) );
						mPrefManager.setDiaperSensorCurrentSleepingLevel(sensor.deviceId, sleep);

						if (!Configuration.FAST_DETECTION) {
							// 센서 Status가 변경되었다면, Status를 변경하기
							int diaperStatus = jobj.optInt(mServerQueryMgr.getParameter(52), -1);
							long diaperUpdatedTimeSec = jobj.optLong(mServerQueryMgr.getParameter(53), 0);
							if (mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) < diaperUpdatedTimeSec) {
								if (DBG) Log.e(TAG, "Pending Push : " + sensor.deviceId + " / " + mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) + " / " + diaperUpdatedTimeSec);
								//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("Pending Push : " + mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) + " / " + diaperUpdatedTimeSec);

								if (diaperStatus == DeviceStatus.DETECT_NONE) {
									sensor.initDetectedStatus(diaperUpdatedTimeSec * 1000);
								} else {
									sensor.setDiaperStatus(diaperStatus, diaperUpdatedTimeSec * 1000);
								}
							}
						}
					}

					String name = jobj.optString(mServerQueryMgr.getParameter(25), null);
					if (name !=  null) {
						sensor.setName(name);
						mPrefManager.setDeviceName(DeviceType.DIAPER_SENSOR, deviceId, name);
					}
					sensor.setBabyBirthdayYYMMDD(jobj.optString(mServerQueryMgr.getParameter(17), null));
					sensor.setBabySex(jobj.optInt(mServerQueryMgr.getParameter(18), -1));
					sensor.setBabyEating(jobj.optInt(mServerQueryMgr.getParameter(112), -1));
					sensor.setSensitivity(jobj.optInt(mServerQueryMgr.getParameter(47), -1));

				} else if (type == DeviceType.AIR_QUALITY_MONITORING_HUB && deviceId != 0) {
					DeviceAQMHub hub = getDeviceAQMHub(deviceId);
					if (hub == null) {
						hub = new DeviceAQMHub(mContext);
						hub.deviceId = deviceId;
						if (DBG) Log.d(TAG, "Add new hub from server : " + deviceId);
						mRegisteredAQMHubList.put(deviceId, hub);
						addedNewDevice = true;
					} else {
						if (DBG) Log.d(TAG, "Already have local hub, update it : " + deviceId);
					}

					if (jobj.optInt(mServerQueryMgr.getParameter(54), DeviceConnectionState.WIFI_CONNECTED) == DeviceConnectionState.DISCONNECTED) {
						if (hub.getConnectionState() != DeviceConnectionState.DISCONNECTED) {
							if (mUpperLayerHandler != null) {
								mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.DISCONNECTED, -1, hub.getDeviceInfo()).sendToTarget();
							}
						}
						hub.setConnectionState(DeviceConnectionState.DISCONNECTED);
					} else {
						if (hub.getConnectionState() != DeviceConnectionState.WIFI_CONNECTED) {
							if (mUpperLayerHandler != null) {
								mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.WIFI_CONNECTED, -1, hub.getDeviceInfo()).sendToTarget();
							}
						}
						hub.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
					}
					if (cloudId > 0) {
						hub.cloudId = cloudId;
					}

					int lampPower = jobj.optInt(mServerQueryMgr.getParameter(55), -1);
					int brightLevel = jobj.optInt(mServerQueryMgr.getParameter(56), -1);

					String hubfirmware = jobj.optString(mServerQueryMgr.getParameter(30), null);
					if (hubfirmware != null) {
						hub.firmwareVersion = hubfirmware;
					}
					hub.setLampPower(lampPower);
					hub.setBrightLevel(brightLevel);
					hub.setColorTemperature(jobj.optInt(mServerQueryMgr.getParameter(57), -1));
					hub.setSensorAttached(jobj.optInt(mServerQueryMgr.getParameter(58), -1));
					hub.setTemperature(jobj.optInt(mServerQueryMgr.getParameter(59), -1));
					hub.setHumidity(jobj.optInt(mServerQueryMgr.getParameter(60), -1));
					hub.setVoc(jobj.optInt(mServerQueryMgr.getParameter(61), -1));
					String name = jobj.optString(mServerQueryMgr.getParameter(25), null);
					if (name !=  null) {
						hub.setName(name);
						mPrefManager.setDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, name);
					}
					hub.setName(jobj.optString(mServerQueryMgr.getParameter(25), null));
					hub.setApName(jobj.optString(mServerQueryMgr.getParameter(62), null));
					hub.setApSecurity(jobj.optInt(mServerQueryMgr.getParameter(63), -1));
					hub.setMaxTemperature(jobj.optInt(mServerQueryMgr.getParameter(33), -1));
					hub.setMinTemperature(jobj.optInt(mServerQueryMgr.getParameter(34), -1));
					hub.setMaxHumidity(jobj.optInt(mServerQueryMgr.getParameter(35), -1));
					hub.setMinHumidity(jobj.optInt(mServerQueryMgr.getParameter(36), -1));
					hub.setBetaStatus(jobj.optInt(mServerQueryMgr.getParameter(64), -1));

					String ledOnTime = jobj.optString(mServerQueryMgr.getParameter(31), null);
					String ledOffTime = jobj.optString(mServerQueryMgr.getParameter(32), null);

					if (ledOnTime != null && ledOffTime != null) {
						try {
							Date ledOnOffDate = new Date();
							ledOnOffDate.setHours(Integer.parseInt(ledOnTime.substring(0, 2)));
							ledOnOffDate.setMinutes(Integer.parseInt(ledOnTime.substring(2, 4)));
							long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(ledOnOffDate.getTime());
							ledOnOffDate.setTime(utcTimeMs);
							int convertedHour = ledOnOffDate.getHours();
							int convertedMinute = ledOnOffDate.getMinutes();
							ledOnTime = String.format(Locale.getDefault(), "%02d%02d", convertedHour, convertedMinute);

							ledOnOffDate.setHours(Integer.parseInt(ledOffTime.substring(0, 2)));
							ledOnOffDate.setMinutes(Integer.parseInt(ledOffTime.substring(2, 4)));
							utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(ledOnOffDate.getTime());
							ledOnOffDate.setTime(utcTimeMs);
							convertedHour = ledOnOffDate.getHours();
							convertedMinute = ledOnOffDate.getMinutes();
							ledOffTime = String.format(Locale.getDefault(), "%02d%02d", convertedHour, convertedMinute) + ledOffTime.substring(4);
							if (DBG) Log.d(TAG, "converted : " + deviceId + " / " + ledOnTime + " ~ " + ledOffTime);
						} catch (Exception e) {
							if (DBG) Log.e(TAG, "Exception : " + e);
							ledOnTime = "0700";
							ledOffTime = "21000";
						}
						hub.setLedOnTime(ledOnTime);
						hub.setLedOffTime(ledOffTime);
					}

					// 수유등 ON Timer
					String lampOnTime = jobj.optString(mServerQueryMgr.getParameter(126), null);
					if (lampOnTime != null) {
						try {
							Date date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(lampOnTime);
							long lampOnUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
							if (lampOnUtcTimeMs == 0 || lampOnUtcTimeMs > System.currentTimeMillis()) {
								mPrefManager.setLampOnTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, hub.deviceId, lampOnUtcTimeMs);
							}

							if (DBG) Log.d(TAG, "Lamp onTime: " + lampOnTime + "(" + lampOnUtcTimeMs + ")");
						} catch (Exception e) {
							if (DBG) Log.e(TAG, "Exception : " + e);
						}
					}

					// 수유등 Off Timer
					String lampOffTime = jobj.optString(mServerQueryMgr.getParameter(125), null);
					if (lampOffTime != null) {
						try {
							// 수유등 OFF Timer
							Date date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(lampOffTime);
							long lampOffUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
							if (lampOffUtcTimeMs == 0 || lampOffUtcTimeMs > System.currentTimeMillis()) {
								mPrefManager.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, hub.deviceId, lampOffUtcTimeMs);
							}

							if (DBG) Log.d(TAG, "Lamp offTime: " + lampOffTime + "(" + lampOffUtcTimeMs + ")");
						} catch (Exception e) {
							if (DBG) Log.e(TAG, "Exception : " + e);
						}
					}
				} else if (type == DeviceType.LAMP && deviceId != 0) {
					DeviceLamp lamp = getDeviceLamp(deviceId);
					DeviceBLEConnection bleLamp = getDeviceBLEConnection(deviceId, type);

					if (lamp == null) {
						lamp = new DeviceLamp(mContext);
						lamp.deviceId = deviceId;
						if (DBG) Log.d(TAG, "Add new lamp from server : " + deviceId);
						mRegisteredLampList.put(deviceId, lamp);
						addedNewDevice = true;
					} else {
						if (DBG) Log.d(TAG, "Already have local lamp, update it : " + deviceId);
					}

					if (cloudId > 0) {
						lamp.cloudId = cloudId;
					}

					if (bleLamp != null) {
						long where = jobj.optLong(mServerQueryMgr.getParameter(119), -1);
						if (DBG) Log.d(TAG, "lamp connection ble: " + bleLamp.getConnectionState() + " / object: " + lamp.getConnectionState() + " / " + where);

						// 직접연결이 안된 상태이면 false로 설정(블루투스 연결시 타이머 비활성화, 펌웨어 업데이트 불가)
						switch (jobj.optInt(mServerQueryMgr.getParameter(54), DeviceConnectionState.DISCONNECTED)) {
							case 0:
								bleLamp.setServerDirectConnectionState(false);
								lamp.setServerDirectConnectionState(false);
								break;
							case 1:
								bleLamp.setServerDirectConnectionState(false);
								lamp.setServerDirectConnectionState(false);
								break;
							case 2:
								bleLamp.setServerDirectConnectionState(true);
								lamp.setServerDirectConnectionState(true);
								break;
						}

						if (bleLamp.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) { // BLE연결이 아니라면
							if (jobj.optInt(mServerQueryMgr.getParameter(54), DeviceConnectionState.WIFI_CONNECTED) == DeviceConnectionState.DISCONNECTED) {
								if (lamp.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
									// Wi-Fi로 받는 데이터이므로 Wi-Fi 접속만 신경쓰면 됨
									if (mUpperLayerHandler != null) {
										mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.DISCONNECTED, -1, lamp.getDeviceInfo()).sendToTarget();
									}
									if (DBG) Log.d(TAG, "Set lamp connection status : DISCONNECTED / " + lamp.getConnectionState());
									lamp.setConnectionState(DeviceConnectionState.DISCONNECTED);
									lamp.setConnectionId(-1);
									bleLamp.setConnectionState(DeviceConnectionState.DISCONNECTED);
								}
							} else {
								if (lamp.getConnectionState() == DeviceConnectionState.DISCONNECTED) {
									// Wi-Fi로 받는 데이터이므로 Wi-Fi 접속만 신경쓰면 됨
									if (mUpperLayerHandler != null) {
										mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.WIFI_CONNECTED, -1, lamp.getDeviceInfo()).sendToTarget();
									}
									if (DBG) Log.d(TAG, "Set lamp connection status : WIFI_CONNECTED / " + lamp.getConnectionState());
									lamp.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
									lamp.setConnectionId(where);
									bleLamp.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
								}
							}
						} else {
							lamp.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							lamp.setConnectionId(where);
						}
					}

					String lampfirmware = jobj.optString(mServerQueryMgr.getParameter(30), null);
					if (lampfirmware != null) {
						lamp.firmwareVersion = lampfirmware;
					}
					lamp.setLampPower(jobj.optInt(mServerQueryMgr.getParameter(55), 0));
					lamp.setBrightLevel(jobj.optInt(mServerQueryMgr.getParameter(56), 0));
					lamp.setColorTemperature(jobj.optInt(mServerQueryMgr.getParameter(57), -1));
					lamp.setSensorAttached(jobj.optInt(mServerQueryMgr.getParameter(58), -1));
					lamp.setTemperature(jobj.optInt(mServerQueryMgr.getParameter(59), -1));
					lamp.setHumidity(jobj.optInt(mServerQueryMgr.getParameter(60), -1));
					lamp.setVoc(jobj.optInt(mServerQueryMgr.getParameter(61), -1));
					String name = jobj.optString(mServerQueryMgr.getParameter(25), null);
					if (name !=  null) {
						lamp.setName(name);
						mPrefManager.setDeviceName(DeviceType.LAMP, deviceId, name);
					}
					lamp.setApName(jobj.optString(mServerQueryMgr.getParameter(62), null));
					lamp.setApSecurity(jobj.optInt(mServerQueryMgr.getParameter(63), -1));
					lamp.setMaxTemperature(jobj.optInt(mServerQueryMgr.getParameter(33), -1));
					lamp.setMinTemperature(jobj.optInt(mServerQueryMgr.getParameter(34), -1));
					lamp.setMaxHumidity(jobj.optInt(mServerQueryMgr.getParameter(35), -1));
					lamp.setMinHumidity(jobj.optInt(mServerQueryMgr.getParameter(36), -1));
					lamp.setBetaStatus(jobj.optInt(mServerQueryMgr.getParameter(64), -1));

					String ledOnTime = jobj.optString(mServerQueryMgr.getParameter(31), null);
					String ledOffTime = jobj.optString(mServerQueryMgr.getParameter(32), null);

					if (ledOnTime != null && ledOffTime != null) {
						try {
							Date ledOnOffDate = new Date();
							ledOnOffDate.setHours(Integer.parseInt(ledOnTime.substring(0, 2)));
							ledOnOffDate.setMinutes(Integer.parseInt(ledOnTime.substring(2, 4)));
							long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(ledOnOffDate.getTime());
							ledOnOffDate.setTime(utcTimeMs);
							int convertedHour = ledOnOffDate.getHours();
							int convertedMinute = ledOnOffDate.getMinutes();
							ledOnTime = String.format(Locale.getDefault(), "%02d%02d", convertedHour, convertedMinute);

							ledOnOffDate.setHours(Integer.parseInt(ledOffTime.substring(0, 2)));
							ledOnOffDate.setMinutes(Integer.parseInt(ledOffTime.substring(2, 4)));
							utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(ledOnOffDate.getTime());
							ledOnOffDate.setTime(utcTimeMs);
							convertedHour = ledOnOffDate.getHours();
							convertedMinute = ledOnOffDate.getMinutes();
							ledOffTime = String.format(Locale.getDefault(), "%02d%02d", convertedHour, convertedMinute) + ledOffTime.substring(4);
							if (DBG) Log.d(TAG, "converted : " + deviceId + " / " + ledOnTime + " ~ " + ledOffTime);
						} catch (Exception e) {
							if (DBG) Log.e(TAG, "Exception : " + e);
							ledOnTime = "0700";
							ledOffTime = "21000";
						}
						lamp.setLedOnTime(ledOnTime);
						lamp.setLedOffTime(ledOffTime);
					}

					// 수유등 ON Timer
					String lampOnTime = jobj.optString(mServerQueryMgr.getParameter(126), null);
					if (lampOnTime != null) {
						try {
							Date date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(lampOnTime);
							long lampOnUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
							if (lampOnUtcTimeMs == 0 || lampOnUtcTimeMs > System.currentTimeMillis()) {
								mPrefManager.setLampOnTimerTargetMs(DeviceType.LAMP, lamp.deviceId, lampOnUtcTimeMs);
							}

							if (DBG) Log.d(TAG, "Lamp onTime: " + lampOnTime + "(" + lampOnUtcTimeMs + ")");
						} catch (Exception e) {
							if (DBG) Log.e(TAG, "Exception : " + e);
						}
					}

					// 수유등 Off Timer
					String lampOffTime = jobj.optString(mServerQueryMgr.getParameter(125), null);
					if (lampOffTime != null) {
						try {
							// 수유등 OFF Timer
							Date date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(lampOffTime);
							long lampOffUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
							if (lampOffUtcTimeMs == 0 || lampOffUtcTimeMs > System.currentTimeMillis()) {
								mPrefManager.setLampOffTimerTargetMs(DeviceType.LAMP, lamp.deviceId, lampOffUtcTimeMs);
							}

							if (DBG) Log.d(TAG, "Lamp offTime: " + lampOffTime + "(" + lampOffUtcTimeMs + ")");
						} catch (Exception e) {
							if (DBG) Log.e(TAG, "Exception : " + e);
						}
					}
				} else if (type == DeviceType.ELDERLY_DIAPER_SENSOR && deviceId != 0) {
					DeviceElderlyDiaperSensor sensor = getDeviceElderlyDiaperSensor(deviceId);
					DeviceBLEConnection bleSensor = getDeviceBLEConnection(deviceId, type);
					/*
					if (bleSensor != null) {
						// BLE로 직접받고 있더라도, 기저귀 초기화, 기저귀 교체, 이름변경 등 바꿔줘야하므로 업데이트 시킴
                        // 직접 받고 있으므로 업데이트 시킬 필요가 없음 But, 연결이 끊어진 상태라면 직접 받아야함
                        if (bleSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
                            if (DBG) Log.d(TAG, "ignore update sensor data from did : " + deviceId);
                            continue;
                        }
					}
					*/

					if (sensor == null) {
						sensor = new DeviceElderlyDiaperSensor(mContext);
						sensor.deviceId = deviceId;
						if (DBG) Log.d(TAG, "Add new elderly diaper sensor from server : " + deviceId);
						mRegisteredElderlyDiaperSensorList.put(deviceId, sensor);
						addedNewDevice = true;
					} else {
						if (DBG) Log.d(TAG, "Already have local elderly diaper sensor, update it : " + deviceId);
					}
					if (cloudId > 0) {
						sensor.cloudId = cloudId;
					}

					if (bleSensor != null) {
						if (DBG) Log.d(TAG, "diaper sensor connection ble: " + bleSensor.getConnectionState() + " / object: " + bleSensor.getConnectionState());
						if (bleSensor.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) { // BLE연결이 아니라면
							if (jobj.optInt(mServerQueryMgr.getParameter(54), DeviceConnectionState.WIFI_CONNECTED) == DeviceConnectionState.DISCONNECTED) {
								// Wi-Fi로 받는 데이터이므로 Wi-Fi 접속만 신경쓰면 됨
								if (sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED) {
									if (mUpperLayerHandler != null) {
										mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.DISCONNECTED, -1, sensor.getDeviceInfo()).sendToTarget();
									}
								}
								sensor.setConnectionState(DeviceConnectionState.DISCONNECTED);
								sensor.setConnectionId(-1);
								bleSensor.setConnectionState(DeviceConnectionState.DISCONNECTED);
							} else {
								// Wi-Fi로 받는 데이터이므로 Wi-Fi 접속만 신경쓰면 됨
								if (sensor.getConnectionState() != DeviceConnectionState.WIFI_CONNECTED) {
									if (mUpperLayerHandler != null) {
										mUpperLayerHandler.obtainMessage(MSG_WIFI_CONNECTION_STATE_CHANGE, DeviceConnectionState.WIFI_CONNECTED, -1, sensor.getDeviceInfo()).sendToTarget();
									}
								}
								sensor.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
								sensor.setConnectionId(jobj.optLong(mServerQueryMgr.getParameter(119), -1));
								bleSensor.setConnectionState(DeviceConnectionState.WIFI_CONNECTED);
								if (Configuration.CERTIFICATE_MODE) {
									sensor.setTemperature(jobj.optInt(mServerQueryMgr.getParameter(39), -1) / 100.0f);
									sensor.setHumidity(jobj.optInt(mServerQueryMgr.getParameter(40), -1) / 100.0f);
									sensor.setVoc(jobj.optInt(mServerQueryMgr.getParameter(41), -1) / 100.0f);
								}
							}
						} else {
							sensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
						}
					}

//					if (Configuration.BETA_TEST_MODE) {
//						// 베타테스트 모드이고, 충전중이 아니면 Baseline 알람 울리기
//						// 앱이 background여도 FCM메시지로 인해 10분에 한번씩 호출되어 여기 있어도 됨
//						if (jobj.optInt(mServerQueryMgr.getParameter(50), -1) < DeviceStatus.OPERATION_CABLE_NO_CHARGE) {
//							// 이 센서가 내 센서일때만 보내기
//							if (sensor.cloudId == mPrefManager.getAccountId()) {
//								sensor.checkDiaperStatusForBaseLine();
//							} else {
//								if (DBG) Log.d(TAG, "Not my device");
//							}
//						}
//					}

					if (!Configuration.CERTIFICATE_MODE) {
						sensor.setDiaperScore(jobj.optInt(mServerQueryMgr.getParameter(153), -1));
						sensor.setBatteryPower(jobj.optInt(mServerQueryMgr.getParameter(49), -1));
						sensor.setOperationStatus(jobj.optInt(mServerQueryMgr.getParameter(50), -1));
						sensor.setMovementStatus(jobj.optInt(mServerQueryMgr.getParameter(51), -1));
						sensor.setVocAvg(jobj.optInt(mServerQueryMgr.getParameter(154), -1) / 100.0f);
						sensor.setStrapBatteryPower(jobj.optInt(mServerQueryMgr.getParameter(166), -1) / 100);

						if (!Configuration.FAST_DETECTION) {
							// 센서 Status가 변경되었다면, Status를 변경하기
							int diaperStatus = jobj.optInt(mServerQueryMgr.getParameter(52), -1);
							long diaperUpdatedTimeSec = jobj.optLong(mServerQueryMgr.getParameter(53), 0);
							if (mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) < diaperUpdatedTimeSec) {
								if (DBG) Log.e(TAG, "Pending Push : " + sensor.deviceId + " / " + mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) + " / " + diaperUpdatedTimeSec);
								//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("Pending Push : " + mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) + " / " + diaperUpdatedTimeSec);

								if (diaperStatus == DeviceStatus.DETECT_NONE) {
									sensor.initDetectedStatus(diaperUpdatedTimeSec * 1000);
								} else {
									sensor.setDiaperStatus(diaperStatus, diaperUpdatedTimeSec * 1000);
								}
							}
						}
					}

					if (sensor.getConnectionState() != DeviceConnectionState.BLE_CONNECTED && sensor.getConnectionState() != DeviceConnectionState.DISCONNECTED) {
						int operation = jobj.optInt(mServerQueryMgr.getParameter(50), -1);

						if (operation == DeviceStatus.OPERATION_STRAP_CONNECTED) {
							sensor.setStrapAttached(true);
						} else {
							sensor.setStrapAttached(true);
						}

						float[] multiTouch = new float[9];
						multiTouch[0] = jobj.optInt(mServerQueryMgr.getParameter(157), -1);
						multiTouch[1] = jobj.optInt(mServerQueryMgr.getParameter(158), -1);
						multiTouch[2] = jobj.optInt(mServerQueryMgr.getParameter(159), -1);
						multiTouch[3] = jobj.optInt(mServerQueryMgr.getParameter(160), -1);
						multiTouch[4] = jobj.optInt(mServerQueryMgr.getParameter(161), -1);
						multiTouch[5] = jobj.optInt(mServerQueryMgr.getParameter(162), -1);
						multiTouch[6] = jobj.optInt(mServerQueryMgr.getParameter(163), -1);
						multiTouch[7] = jobj.optInt(mServerQueryMgr.getParameter(164), -1);
						multiTouch[8] = jobj.optInt(mServerQueryMgr.getParameter(165), -1);
						sensor.setMultiTouch(multiTouch);

						String strTouch = "";
						for (int ii = 0; ii < multiTouch.length; ii++) {
							strTouch += multiTouch[ii] + ", ";
						}
						sensor.deviceId = deviceId;
						if (DBG) Log.d(TAG, "STRAP_STATUS(WIFI) [" + sensor.deviceId  + "] " + strTouch + " / oper: " + operation);
					}

					sensor.setTemperature(jobj.optInt(mServerQueryMgr.getParameter(39), -1) / 100);
					sensor.setHumidity(jobj.optInt(mServerQueryMgr.getParameter(40), -1) / 100);
					sensor.setVoc(jobj.optInt(mServerQueryMgr.getParameter(41), -1) / 100);

					String name = jobj.optString(mServerQueryMgr.getParameter(25), null);
					if (name !=  null) {
						sensor.setName(name);
						mPrefManager.setDeviceName(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, name);
					}
					sensor.setBabyBirthdayYYMMDD(jobj.optString(mServerQueryMgr.getParameter(17), null));
					sensor.setBabySex(jobj.optInt(mServerQueryMgr.getParameter(18), -1));
					sensor.setBabyEating(jobj.optInt(mServerQueryMgr.getParameter(112), -1));
					sensor.setSensitivity(jobj.optInt(mServerQueryMgr.getParameter(47), -1));

				}
			}
			if (addedNewDevice) {
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_UPDATE_SCREEN_DEVICE_OBJECT_VIEW).sendToTarget();
				}
			}
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		mLatestInitDeviceInfo = null;
	}

	/**
	 *  updateDeviceStatusFromCloud
	 *  10초에 한번씩 현재기기정보를 가져오기 위한 함수
	 */
	public void updateDeviceStatusFromCloud(final ServerManager.ServerResponseListener listener) {
		if (mPrefManager.getSigninEmail() == null ||
				mPrefManager.getSigninToken() == null ||
				mPrefManager.getSigninState() != SignInState.STEP_COMPLETED) {
			return;
		}
		mServerQueryMgr.getDeviceStatus(new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
					_setDeviceDataFromCloud(data);
				} else if (InternetErrorCode.ERR_INVALID_TOKEN.equals(errcode)) {
					//UserInfoManager.getInstance(mContext).signout();
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_CHECK_INVALID_TOKEN).sendToTarget();
					}
				}
				if (listener != null) {
					listener.onReceive(responseCode, errcode, data);
				}
			}
		});
	}

	/**
	 *  updateDeviceFullStatusFromCloud
	 *  10분에 한번씩 현재기기정보를 가져오기 위한 함수
	 */
	public void updateDeviceFullStatusFromCloud(final ServerManager.ServerResponseListener listener) {
		if (mPrefManager == null ||
				mPrefManager.getSigninEmail() == null ||
				mPrefManager.getSigninToken() == null ||
				mPrefManager.getSigninState() != SignInState.STEP_COMPLETED) {
			return;
		}

		if (mServerQueryMgr == null) return;
		mServerQueryMgr.getDeviceFullStatus(new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
					mDeviceInfoListFromCloud.clear(); // Cloud에서 받아오는 데이터들 저장하기 위함
					_setDeviceDataFromCloud(data);
                    compareCloudWithDeviceViewObject(); // Cloud에서 받아온 데이터와 로컬에 있는 데이터를 비교
				} else if (InternetErrorCode.ERR_INVALID_TOKEN.equals(errcode)) {
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_CHECK_INVALID_TOKEN).sendToTarget();
					}
				}
				if (listener != null) {
					listener.onReceive(responseCode, errcode, data);
				}
			}
		});
	}

	/**
	 *  updateDeviceStatusToCloud
	 *  디바이스 상태를 클라우드에 보내주어야 할때 사용 30초에 한번씩 Full Sync를 맞추기 위함
	 *  BLE 연결된 상태 정보만 보내줘야 하는데, 막 끊어졌을 때에도 보내야 함
	 */
	public void updateDeviceStatusToCloud() {
		if (DBG) Log.i(TAG, "updateDeviceStatusToCloud");
		String updateData = "[";
		final ArrayList<Long> arrDiaperStatusUploadSensorList = new ArrayList<>();
		for (DeviceBLEConnection conn : mRegisteredBleDeviceList.values()) {
			if (conn == null) continue;
			DeviceDiaperSensor sensor = getDeviceDiaperSensor(conn.getDeviceInfo().deviceId);
			if (sensor == null || sensor.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) continue;

			try {
				JSONObject jobj = new JSONObject();
				jobj.put(mServerQueryMgr.getParameter(28), DeviceType.DIAPER_SENSOR);
				jobj.put(mServerQueryMgr.getParameter(26), sensor.deviceId);
				jobj.put(mServerQueryMgr.getParameter(27), sensor.getEnc());

				jobj.put(mServerQueryMgr.getParameter(50), sensor.getOperationStatus());
				jobj.put(mServerQueryMgr.getParameter(54), (sensor.getConnectionState() == DeviceConnectionState.DISCONNECTED) ? 0 : 1);

				if (!Configuration.FAST_DETECTION) {
					if (sensor.getDiaperStatus() != DeviceStatus.DETECT_NONE) {
						if (DBG) Log.e(TAG, "updated: " + mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) + " / uploaded: " + mPrefManager.getLatestSensorDiaperStatusUploadTimeSec(sensor.deviceId));
						if (mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId) > mPrefManager.getLatestSensorDiaperStatusUploadTimeSec(sensor.deviceId)) {
							jobj.put(mServerQueryMgr.getParameter(52), sensor.getDiaperStatus());
							jobj.put(mServerQueryMgr.getParameter(15), mPrefManager.getLatestDiaperStatusUpdatedTimeSec(sensor.deviceId));
							jobj.put(mServerQueryMgr.getParameter(41), (int) (sensor.getVoc() * 100));
							arrDiaperStatusUploadSensorList.add(sensor.deviceId);
						} else {
							if (DBG) Log.e(TAG, "already sent diaper status");
						}
					} else {
						if (DBG) Log.e(TAG, "diaperStatus == 0");
					}
				}

				jobj.put(mServerQueryMgr.getParameter(51), sensor.getMovementStatus());
				jobj.put(mServerQueryMgr.getParameter(49), sensor.getBatteryPower() * 100);

				if (DBG) Log.i(TAG, "ChangedValue : " + jobj.toString());
				if (updateData.length() > 2) {
					updateData += ", " + jobj.toString();
				} else {
					updateData += jobj.toString();
				}
			} catch (JSONException e) {
				if (DBG) Log.e(TAG, e.toString());
			} catch (NullPointerException e) {
				if (DBG) Log.e(TAG, e.toString());
			}
		}

		if (updateData.equals("[")) {
			return;
		} else {
			updateData += "]";
		}
		mServerQueryMgr.setDeviceStatus(updateData, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					if (errcode.equals(InternetErrorCode.SUCCEEDED)) {
						if (DBG) Log.d(TAG, data);
						for (long deviceId: arrDiaperStatusUploadSensorList) {
							mPrefManager.setLatestSensorDiaperStatusUploadTimeSec(deviceId, mPrefManager.getLatestDiaperStatusUpdatedTimeSec(deviceId));
							if (DBG) Log.e(TAG, "updated upload time: " + deviceId + " / " + mPrefManager.getLatestSensorDiaperStatusUploadTimeSec(deviceId));
						}
					}
				}
			}
		});
	}

	/**
	 *  updateDeviceLampBrightLevel
	 *  허브/수유등 밝기단계를 클라우드에 보내주어야 할때 사용
	 */
	public void updateDeviceLampBrightLevel(final int deviceType, final long deviceId, final int brightLevel) {
		if (DBG) Log.i(TAG, "updateDeviceLampBrightLevel: " + deviceType + " / " + deviceId + " / " + brightLevel);

		String deviceEnc = null;
		if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
			DeviceAQMHub hub = getDeviceAQMHub(deviceId);
			if (hub == null || hub.getEnc() == null) {
				if (DBG) Log.e(TAG, "hub null");
				return;
			}
			deviceEnc = hub.getEnc();
		} else if (deviceType == DeviceType.LAMP) {
			DeviceLamp lamp = getDeviceLamp(deviceId);
			if (lamp == null || lamp.getEnc() == null) {
				if (DBG) Log.e(TAG, "lamp null");
				return;
			}
			deviceEnc = lamp.getEnc();
		}
		if (deviceEnc == null) {
			if (DBG) Log.e(TAG, "enc null");
			return;
		}

		mServerQueryMgr.setLampBrightess(deviceType, deviceId, deviceEnc, brightLevel, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
						if (DBG) Log.d(TAG, data);
						if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
							getDeviceAQMHub(deviceId).setBrightLevel(brightLevel);
						} else if (deviceType == DeviceType.LAMP) {
							getDeviceLamp(deviceId).setBrightLevel(brightLevel);
						}
					}
				}
			}
		});
	}

	/**
	 *  updateDeviceLampPower
	 *  허브/수유등 점등여부를 클라우드에 보내주어야 할때 사용
	 */
	public void updateDeviceLampPower(final int deviceType, final long deviceId, final int power) {
		if (DBG) Log.i(TAG, "updateDeviceLampPower: " + deviceType + " / " + deviceId + " / " + power);

		String deviceEnc = null;
		int brightnessTemp = -1;
		if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
			DeviceAQMHub hub = getDeviceAQMHub(deviceId);
			if (hub == null || hub.getEnc() == null) {
				if (DBG) Log.e(TAG, "hub null");
				return;
			}
			deviceEnc = hub.getEnc();
			brightnessTemp = hub.getBrightLevel();
		} else if (deviceType == DeviceType.LAMP) {
			DeviceLamp lamp = getDeviceLamp(deviceId);
			if (lamp == null || lamp.getEnc() == null) {
				if (DBG) Log.e(TAG, "lamp null");
				return;
			}
			deviceEnc = lamp.getEnc();
			brightnessTemp = lamp.getBrightLevel();
		}
		if (deviceEnc == null) {
			if (DBG) Log.e(TAG, "enc null");
			return;
		}

		final int brightnessFinal = brightnessTemp;
		final String deviceEncFinal = deviceEnc;

		mServerQueryMgr.setLampPower(deviceType, deviceId, deviceEncFinal, power, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
						if (DBG) Log.d(TAG, data);
						if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
							DeviceAQMHub hub = getDeviceAQMHub(deviceId);
							if (hub == null || hub.getEnc() == null) {
								if (DBG) Log.e(TAG, "hub null");
								return;
							}
							hub.setLampPower(power);

						} else if (deviceType == DeviceType.LAMP) {
							DeviceLamp lamp = getDeviceLamp(deviceId);
							if (lamp == null || lamp.getEnc() == null) {
								if (DBG) Log.e(TAG, "lamp null");
								return;
							}
							lamp.setLampPower(power);
						}

                        if (DBG) Log.i(TAG, "current status: " + power + " / " + brightnessFinal);
						// 혹시 power는 on인데, 밝기가 0인 경우
						if (power == DeviceStatus.LAMP_POWER_ON && brightnessFinal == DeviceStatus.BRIGHT_OFF) {
							mServerQueryMgr.setLampBrightess(deviceType, deviceId, deviceEncFinal, DeviceStatus.BRIGHT_ON1, new ServerManager.ServerResponseListener() {
								@Override
								public void onReceive(int responseCode, String errcode, String data) {
									if (responseCode == ServerManager.RESPONSE_CODE_OK) {
										if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
											if (DBG) Log.d(TAG, data);
											if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
												getDeviceAQMHub(deviceId).setBrightLevel(DeviceStatus.BRIGHT_ON1);
											} else if (deviceType == DeviceType.LAMP) {
												getDeviceLamp(deviceId).setBrightLevel(DeviceStatus.BRIGHT_ON1);
											}
										}
									}
								}
							});
						}
					}
				}
			}
		});
	}

	/**
	 *  updateLampOffTimer
	 *  수유등 꺼짐 타이머 설정시
	 */
	public void updateDeviceLampOffTimer(int deviceType, long deviceId, long utcTimeMs) {
		if (DBG) Log.i(TAG, "updateAQMHubLampOffTimer: " + deviceType + " / " + deviceId + " / " + utcTimeMs);

		String deviceEnc = null;
		if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
			DeviceAQMHub hub = getDeviceAQMHub(deviceId);
			if (hub == null || hub.getEnc() == null) {
				if (DBG) Log.e(TAG, "hub null");
				return;
			}
			deviceEnc = hub.getEnc();
		} else if (deviceType == DeviceType.LAMP) {
			DeviceLamp lamp = getDeviceLamp(deviceId);
			if (lamp == null || lamp.getEnc() == null) {
				if (DBG) Log.e(TAG, "lamp null");
				return;
			}
			deviceEnc = lamp.getEnc();
		}
		if (deviceEnc == null) {
			if (DBG) Log.e(TAG, "enc null");
			return;
		}

		mServerQueryMgr.setLampOffTimer(deviceType, deviceId, deviceEnc, utcTimeMs, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
						if (DBG) Log.d(TAG, data);

					}
				}
			}
		});
	}

	/**
	 *  updateDeviceLampBrightLevelBle
	 *  허브/수유등 밝기단계를 BLE로 보내주어야 할때 사용
	 */
	public void updateDeviceLampBrightLevelBle(int deviceType, long deviceId, int brightLevel) {
		if (DBG) Log.i(TAG, "updateDeviceLampBrightLevelBle: " + deviceType + " / " + deviceId + " / " + brightLevel);

		DeviceBLEConnection bleConnection = getDeviceBLEConnection(deviceId, deviceType);

		if (bleConnection == null) {
			if (DBG) Log.e(TAG, "NOT found device");
			return;
		}

		if (bleConnection.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) {
			if (DBG) Log.e(TAG, "NOT BLE connected device(" + bleConnection.getConnectionState() + ")");
			return;
		}

		bleConnection.setLampBrightControl(brightLevel);
	}

	/**
	 *  updateDeviceLampPowerBle
	 *  허브/수유등 전원을 BLE로 보내주어야 할때 사용
	 */
	public void updateDeviceLampPowerBle(int deviceType, long deviceId, int power) {
		if (DBG) Log.i(TAG, "updateDeviceLampPowerBle: " + deviceType + " / " + deviceId + " / " + power);

		DeviceBLEConnection bleConnection = getDeviceBLEConnection(deviceId, deviceType);

		if (bleConnection == null) {
			if (DBG) Log.e(TAG, "NOT found device");
			return;
		}

		if (bleConnection.getConnectionState() != DeviceConnectionState.BLE_CONNECTED) {
			if (DBG) Log.e(TAG, "NOT BLE connected device(" + bleConnection.getConnectionState() + ")");
			return;
		}

		bleConnection.setLampPowerControl(power);
	}

	/**
	 *  startConnectionSensorToCloud
	 *  센서가 연결되자 마자 서버에 데이터를 전송
	 */
	public void startConnectionSensorToCloud(DeviceDiaperSensor sensor) {
		if (sensor == null) {
			return;
		}

		String updateData = null;
		try {
			JSONObject jobj = new JSONObject();
			jobj.put(mServerQueryMgr.getParameter(28), DeviceType.DIAPER_SENSOR);
			jobj.put(mServerQueryMgr.getParameter(26), sensor.deviceId);
			jobj.put(mServerQueryMgr.getParameter(27), sensor.getEnc());
			jobj.put(mServerQueryMgr.getParameter(49), sensor.getBatteryPower() * 100);
			jobj.put(mServerQueryMgr.getParameter(51), sensor.getMovementStatus());
			//jobj.put(mServerQueryMgr.getParameter(52), sensor.getDiaperStatus()); // dps 1인 상태에서 추가로 보내면 바로 알람이 오게됨
			jobj.put(mServerQueryMgr.getParameter(50), sensor.getOperationStatus());
			jobj.put(mServerQueryMgr.getParameter(39), (int)(sensor.getTemperature() * 100));
			jobj.put(mServerQueryMgr.getParameter(40), (int)(sensor.getHumidity() * 100));
			jobj.put(mServerQueryMgr.getParameter(41), (int)(sensor.getVoc() * 100));
			jobj.put(mServerQueryMgr.getParameter(30), sensor.firmwareVersion);
			jobj.put(mServerQueryMgr.getParameter(65), 2);
			updateData = "[" + jobj.toString() + "]";
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		mServerQueryMgr.startConnectionSensor(updateData, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					/* Prevent static analysis
					if (errcode == InternetErrorCode.SUCCEEDED) {

					} else {

					}
					*/
				}
			}
		});
	}

	public void startConnectionElderlyDiaperSensorToCloud(DeviceElderlyDiaperSensor sensor) {
		if (sensor == null) {
			return;
		}

		String updateData = null;
		try {
			JSONObject jobj = new JSONObject();
			jobj.put(mServerQueryMgr.getParameter(28), DeviceType.ELDERLY_DIAPER_SENSOR);
			jobj.put(mServerQueryMgr.getParameter(26), sensor.deviceId);
			jobj.put(mServerQueryMgr.getParameter(27), sensor.getEnc());
			jobj.put(mServerQueryMgr.getParameter(49), sensor.getBatteryPower() * 100);
			jobj.put(mServerQueryMgr.getParameter(51), sensor.getMovementStatus());
			//jobj.put(mServerQueryMgr.getParameter(52), sensor.getDiaperStatus()); // dps 1인 상태에서 추가로 보내면 바로 알람이 오게됨
			jobj.put(mServerQueryMgr.getParameter(50), sensor.getOperationStatus());
			jobj.put(mServerQueryMgr.getParameter(39), (int)(sensor.getTemperature() * 100));
			jobj.put(mServerQueryMgr.getParameter(40), (int)(sensor.getHumidity() * 100));
			jobj.put(mServerQueryMgr.getParameter(41), (int)(sensor.getVoc() * 100));
			jobj.put(mServerQueryMgr.getParameter(30), sensor.firmwareVersion);
			jobj.put(mServerQueryMgr.getParameter(65), 2);
			updateData = "[" + jobj.toString() + "]";
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		mServerQueryMgr.startConnectionSensor(updateData, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					/* Prevent static analysis
					if (errcode == InternetErrorCode.SUCCEEDED) {

					} else {

					}
					*/
				}
			}
		});
	}

	public void getNotificationEditFromCloud(final int deviceType, final long deviceId) {
		if (mGetNotificationEditTable == null) return;
		if (mGetNotificationEditTable.containsKey(deviceId * 10 + deviceType) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getNotificationFromCloud: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetNotificationEditTable.put(deviceId * 10 + deviceType, true);
        mServerQueryMgr.getNotificationEdit(deviceType, deviceId, new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                    try {
						long maxUtcTimeMs = -1;
                        JSONObject wholeObj = new JSONObject(data);
                        JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(66));
                        for (int i = 0; i < jarr.length(); i++) {
                            JSONObject jobj = jarr.getJSONObject(i);
							long server_noti_id = jobj.optLong(mServerQueryMgr.getParameter(117), -1);
							int edit_type = jobj.optInt(mServerQueryMgr.getParameter(118), -1);
                            String edit_extra = jobj.optString(mServerQueryMgr.getParameter(37), null); // Notification 수정 내용
                            String edit_time = jobj.optString(mServerQueryMgr.getParameter(15), null); // Notification 수정 시간
							String edit_created = jobj.optString(mServerQueryMgr.getParameter(121), null); // 수정이 만들어진 시간(GetNotificationEdit 패킷 호출시 같이 보내는 가장 최근 업데이트 한 시간)
                            if (server_noti_id == -1 || edit_type == -1 || edit_created == null)
                                continue;

                            try {
								// Time이 UTC로 들어옴
                                Date date_created_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(edit_created);
                                long createdUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
                                if (createdUtcTimeMs > maxUtcTimeMs) {
                                    maxUtcTimeMs = createdUtcTimeMs;
                                }

								// Time이 UTC로 들어옴
								Date date_edit_time = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(edit_time);
								long editUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_edit_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴

                                mDatabaseMgr.modifyNotificationMessage(server_noti_id, edit_type, edit_extra, editUtcTimeMs);
                            } catch (ParseException e) {

                            }
                        }
                        if (mPrefManager.getLatestNotificationEditTimeMs(deviceType, deviceId) < maxUtcTimeMs) {
                            mPrefManager.setLatestNotificationEditTimeMs(deviceType, deviceId, maxUtcTimeMs);
                        }
                    } catch (JSONException e) {
                        if (DBG) Log.e(TAG, e.toString());
                    } catch (NullPointerException e) {
                        if (DBG) Log.e(TAG, e.toString());
                    }
                    if (mPrefManager.getLatestNotificationEditTimeMs(deviceType, deviceId) == 0) {
						mPrefManager.setLatestNotificationEditTimeMs(deviceType, deviceId, System.currentTimeMillis() - DateTimeUtil.ONE_DAY_MILLIS);
					}
                    //if (DBG) mDatabaseMgr.checkNotificationMessageDB(deviceType, deviceId);
                }

				mGetNotificationEditTable.remove(deviceId * 10 + deviceType);
                sendNotificationUpdatedMessage();

                if (mUpperLayerHandler != null) {
                    mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, 0, 0).sendToTarget();
                }
            }
        });
    }

	public void getNotificationFromCloud(final int deviceType, final long deviceId) {
		mServerQueryMgr.getNotification(deviceType, deviceId, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errCode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
					ArrayList<NotificationMessage> notificationList = new ArrayList<NotificationMessage>();
					int cntNotificationList = 0;
					long maxUtcTimeMs = -1;
					try {
						JSONObject wholeObj = new JSONObject(data);
						JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(66));
						for (int i = 0; i < jarr.length(); i++) {
							JSONObject jobj = jarr.getJSONObject(i);
							int noti_type = jobj.optInt(mServerQueryMgr.getParameter(38), -1);
							int device_type = jobj.optInt(mServerQueryMgr.getParameter(28), -1);
							int device_id = jobj.optInt(mServerQueryMgr.getParameter(26), -1);
							String extra = jobj.optString(mServerQueryMgr.getParameter(37), null);
							String time = jobj.optString(mServerQueryMgr.getParameter(15), null);
							long server_noti_id = jobj.optLong(mServerQueryMgr.getParameter(117), -1);

							if (server_noti_id == -1 || noti_type == -1 || device_type == -1 || device_id == -1 || time == null)
								continue;

							try {
								// Time이 UTC로 들어옴
								Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time);
								long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()); // +9로 실제 UTC로 변경
								if (utcTimeMs > maxUtcTimeMs) {
									maxUtcTimeMs = utcTimeMs;
								}
								notificationList.add(new NotificationMessage(noti_type, device_type, device_id, extra, utcTimeMs, server_noti_id));
							} catch (ParseException e) {

							}
						}

						for (int i = 0; i < notificationList.size(); i++) {
							NotificationMessage msg = notificationList.get(i);
							if (msg.insertDB(mContext) > 0) {
								switch(msg.notiType) {
									case NotificationType.PEE_DETECTED:
										FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPeeDetected(deviceId, msg.timeMs);
										break;
									case NotificationType.POO_DETECTED:
										FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPooDetected(deviceId, msg.timeMs);
										break;
									case NotificationType.FART_DETECTED:
										FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmFartDetected(deviceId, msg.timeMs);
										break;
									case NotificationType.DIAPER_CHANGED:
										FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmDiaperChanged(deviceId, msg.timeMs);
										break;
								}
								cntNotificationList++;
							} else {
								msg.updateDB(mContext);
							}
							if (mUpperLayerHandler != null) {
								mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, i, notificationList.size()).sendToTarget();
							}
						}

						if (mPrefManager.getLatestNotificationTimeMs(deviceType, deviceId) < maxUtcTimeMs) {
							mPrefManager.setLatestNotificationTimeMs(deviceType, deviceId, maxUtcTimeMs);
						}
					} catch (JSONException e) {
						if (DBG) Log.e(TAG, e.toString());
					} catch (NullPointerException e) {
						if (DBG) Log.e(TAG, e.toString());
					}

					if (mPrefManager.getLatestNotificationTimeMs(deviceType, deviceId) == 0) {
						mPrefManager.setLatestNotificationTimeMs(deviceType, deviceId, System.currentTimeMillis() + 1);
					}

					if (cntNotificationList > 0) {
						sendNotificationUpdatedMessage();
					}
				}

				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, 0, 0).sendToTarget();
				}

				getNotificationEditFromCloud(deviceType, deviceId);
			}
		});
	}

	public void getCloudNotificationFromCloud() {
		mServerQueryMgr.getCloudNotification(new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errCode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
					ArrayList<NotificationMessage> notificationList = new ArrayList<NotificationMessage>();
					int cntNotificationList = 0;
					long maxUtcTimeMs = -1;
					try {
						JSONObject wholeObj = new JSONObject(data);
						JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(66));
						for (int i = 0; i < jarr.length(); i++) {
							JSONObject jobj = jarr.getJSONObject(i);
							int noti_type = jobj.optInt(mServerQueryMgr.getParameter(38), -1);
							int device_type = jobj.optInt(mServerQueryMgr.getParameter(28), -1);
							int device_id = jobj.optInt(mServerQueryMgr.getParameter(26), -1);
							String extra = jobj.optString(mServerQueryMgr.getParameter(37), null);
							String time = jobj.optString(mServerQueryMgr.getParameter(15), null);
							long server_noti_id = jobj.optLong(mServerQueryMgr.getParameter(117), -1);

							if (server_noti_id == -1 || noti_type == -1 || device_type == -1 || device_id == -1 || time == null)
								continue;

							try {
								Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time);
								long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()); // +9로 실제 UTC로 변경
								if (utcTimeMs > maxUtcTimeMs) {
									maxUtcTimeMs = utcTimeMs;
								}

								//DB에 넣어야함
								NotificationMessage msg = new NotificationMessage(noti_type, device_type, device_id, extra, utcTimeMs, server_noti_id);
								if (msg.insertDB(mContext) > 0)  {
									cntNotificationList++;
								}
							} catch (ParseException e) {

							}
						}

						for (int i = 0; i < notificationList.size(); i++) {
							NotificationMessage msg = notificationList.get(i);
							if (msg.insertDB(mContext) > 0) {
								cntNotificationList++;
							} else {
								msg.updateDB(mContext);
							}
							if (mUpperLayerHandler != null) {
								mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, i, notificationList.size()).sendToTarget();
							}
						}

						if (mPrefManager.getLatestNotificationTimeMs(0, 0) < maxUtcTimeMs) {
							mPrefManager.setLatestNotificationTimeMs(0, 0, maxUtcTimeMs);
						}
					} catch (JSONException e) {
						if (DBG) Log.e(TAG, e.toString());
					} catch (NullPointerException e) {
						if (DBG) Log.e(TAG, e.toString());
					}
					if (cntNotificationList > 0) {
						sendNotificationUpdatedMessage();
					}
				}
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, 0, 0).sendToTarget();
				}
			}
		});
	}

	/*
	 * getNotificationFromCloudV2
	 * Get Notification from Server
	 */
	public void getNotificationFromCloudV2(final int deviceType, final long deviceId) {
		if (mGetNotificationTable == null) return;
		if (mGetNotificationTable.containsKey(deviceId * 10 + deviceType) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getNotificationFromCloud: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetNotificationTable.put(deviceId * 10 + deviceType, true);
		mServerQueryMgr.getNotification(deviceType, deviceId, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errCode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
					ArrayList<NotificationMessage> notificationList = new ArrayList<NotificationMessage>();
					long maxUtcTimeMs = -1;
					try {
						JSONObject wholeObj = new JSONObject(data);
						JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(66));

						// 수면 관련 Notification 확인
						long latestSleepStartTimeMs = 0;
						String latestSleepEndExtra = null;

						// 기저귀 교체 Notification 확인
						long latestDiaperChangedTimeMs = 0;

						for (int i = 0; i < jarr.length(); i++) {
							JSONObject jobj = jarr.getJSONObject(i);
							int noti_type = jobj.optInt(mServerQueryMgr.getParameter(38), -1);
							int device_type = jobj.optInt(mServerQueryMgr.getParameter(28), -1);
							int device_id = jobj.optInt(mServerQueryMgr.getParameter(26), -1);
							String extra = jobj.optString(mServerQueryMgr.getParameter(37), null);
							String time = jobj.optString(mServerQueryMgr.getParameter(15), null);
							long server_noti_id = jobj.optLong(mServerQueryMgr.getParameter(117), -1);

							if (server_noti_id == -1 || noti_type == -1 || device_type == -1 || device_id == -1 || time == null)
								continue;

							try {
								// Time이 UTC로 들어옴
								Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time);
								long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()); // +9로 실제 UTC로 변경
								if (utcTimeMs > maxUtcTimeMs) {
									maxUtcTimeMs = utcTimeMs;
								}
								NotificationMessage msg = new NotificationMessage(noti_type, device_type, device_id, extra, utcTimeMs, server_noti_id);
								notificationList.add(msg);

								// 수면 시작/종료 확인 후 수면모드 동기화
								if (noti_type == NotificationType.BABY_SLEEP) {
									if (latestSleepStartTimeMs == 0) {
										latestSleepStartTimeMs = utcTimeMs;
										latestSleepEndExtra = extra;
									} else if (latestSleepStartTimeMs < utcTimeMs) {
										latestSleepStartTimeMs = utcTimeMs;
										latestSleepEndExtra = extra;
									}
								} else if (noti_type == NotificationType.DIAPER_CHANGED) {
									if (latestDiaperChangedTimeMs == 0) {
										latestDiaperChangedTimeMs = utcTimeMs;
									} else if (latestDiaperChangedTimeMs < utcTimeMs) {
										latestDiaperChangedTimeMs = utcTimeMs;
									}
								}
							} catch (ParseException e) {

							}
						}

						mDatabaseMgr.insertNotificationMessageList(deviceType, deviceId, notificationList);

						if (mUpperLayerHandler != null) {
							mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, notificationList.size(), notificationList.size()).sendToTarget();
						}

						if (mPrefManager.getLatestNotificationTimeMs(deviceType, deviceId) < maxUtcTimeMs) {
							mPrefManager.setLatestNotificationTimeMs(deviceType, deviceId, maxUtcTimeMs);
						}

						// 수면모드 동기화
						if (latestSleepStartTimeMs > 0) {
							mPrefManager.setSleepingStartTimeMs(deviceId, latestSleepStartTimeMs);
							if (latestSleepEndExtra == null || latestSleepEndExtra.equals("-")) {
								mPrefManager.setSleepingEnabled(deviceId, true);
								mPrefManager.setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED, true);
							} else {
								mPrefManager.setSleepingEnabled(deviceId, false);
								mPrefManager.setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED, false);
							}
						}

						// 최신 기저귀 교체 동기화
						if (latestDiaperChangedTimeMs > 0) {
							mPrefManager.setLatestDiaperChangedTimeSec(deviceId, latestDiaperChangedTimeMs / 1000);
						}

					} catch (JSONException e) {
						if (DBG) Log.e(TAG, e.toString());
					} catch (NullPointerException e) {
						if (DBG) Log.e(TAG, e.toString());
					}

					if (mPrefManager.getLatestNotificationTimeMs(deviceType, deviceId) == 0) {
						mPrefManager.setLatestNotificationTimeMs(deviceType, deviceId, System.currentTimeMillis() + 1);
					}
				}
				mGetNotificationTable.remove(deviceId * 10 + deviceType);
				/*
				NotificationEdit에서 처리 가능
				sendNotificationUpdatedMessage();
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, 0, 0).sendToTarget();
				}
				*/

				getNotificationEditFromCloud(deviceType, deviceId);
			}
		});
	}

	public void getCloudNotificationFromCloudV2() {
		if (mGetCloudNotificationTable == null) return;
		if (mGetCloudNotificationTable.containsKey(DeviceType.SYSTEM) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getCloudNotificationFromCloudV2: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetCloudNotificationTable.put((long)DeviceType.SYSTEM, true);
		mServerQueryMgr.getCloudNotification(new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errCode, String data) {
				if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
					ArrayList<NotificationMessage> notificationList = new ArrayList<NotificationMessage>();
					int cntNotificationList = 0;
					long maxUtcTimeMs = -1;
					try {
						JSONObject wholeObj = new JSONObject(data);
						JSONArray jarr = wholeObj.getJSONArray(mServerQueryMgr.getParameter(66));
						for (int i = 0; i < jarr.length(); i++) {
							JSONObject jobj = jarr.getJSONObject(i);
							int noti_type = jobj.optInt(mServerQueryMgr.getParameter(38), -1);
							int device_type = jobj.optInt(mServerQueryMgr.getParameter(28), -1);
							int device_id = jobj.optInt(mServerQueryMgr.getParameter(26), -1);
							String extra = jobj.optString(mServerQueryMgr.getParameter(37), null);
							String time = jobj.optString(mServerQueryMgr.getParameter(15), null);
							long server_noti_id = jobj.optLong(mServerQueryMgr.getParameter(117), -1);

							if (server_noti_id == -1 || noti_type == -1 || device_type == -1 || device_id == -1 || time == null)
								continue;

							try {
								Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time);
								long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()); // +9로 실제 UTC로 변경
								if (utcTimeMs > maxUtcTimeMs) {
									maxUtcTimeMs = utcTimeMs;
								}

								//DB에 넣어야함
								NotificationMessage msg = new NotificationMessage(noti_type, device_type, device_id, extra, utcTimeMs, server_noti_id);
								if (msg.insertDB(mContext) > 0)  {
									cntNotificationList++;
								}
							} catch (ParseException e) {

							}
						}

						cntNotificationList = mDatabaseMgr.insertCloudNotificationMessageList(notificationList);

						if (mUpperLayerHandler != null) {
							mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, notificationList.size(), notificationList.size()).sendToTarget();
						}
//						for (int i = 0; i < notificationList.size(); i++) {
//							NotificationMessage msg = notificationList.get(i);
//							if (msg.insertDB(mContext) > 0) {
//								cntNotificationList++;
//							} else {
//								msg.updateDB(mContext);
//							}
//							if (mUpperLayerHandler != null) {
//								mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, i, notificationList.size()).sendToTarget();
//							}
//						}

						if (mPrefManager.getLatestNotificationTimeMs(0, 0) < maxUtcTimeMs) {
							mPrefManager.setLatestNotificationTimeMs(0, 0, maxUtcTimeMs);
						}
					} catch (JSONException e) {
						if (DBG) Log.e(TAG, e.toString());
					} catch (NullPointerException e) {
						if (DBG) Log.e(TAG, e.toString());
					}
					if (cntNotificationList > 0) {
						sendNotificationUpdatedMessage();
					}
				}
				mGetCloudNotificationTable.remove((long)DeviceType.SYSTEM);
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_NOTIFICATION_MESSAGE_RECEIVED, 0, 0).sendToTarget();
				}
			}
		});
	}

	public void getSleepGraphList(final DeviceDiaperSensor diaperSensor) {
		_getSleepGraphList(diaperSensor, true);
	}

	public void _getSleepGraphList(final DeviceDiaperSensor diaperSensor, final boolean backgroundPolling) {
		if (mGetSleepGraphTable == null || diaperSensor == null) return;
		if (mGetSleepGraphTable.containsKey(diaperSensor.deviceId * 10 + DeviceType.DIAPER_SENSOR) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getSleepGraphList: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetSleepGraphTable.put(diaperSensor.deviceId * 10 + DeviceType.DIAPER_SENSOR, true);

		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestSleepGraphUpdatedTimeSec(diaperSensor.deviceId) * 1000);
		if (DBG) Log.d(TAG, "LatestGetMovementGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestSleepGraphUpdatedTimeSec(diaperSensor.deviceId) + ")");
		mServerQueryMgr.getSleepGraphList(
				diaperSensor.deviceId,
				startTimeStamp,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						long total = 0L;
						long now = 0L;
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							ArrayList<MovementGraphInfo> movementGraphInfoList = new ArrayList<MovementGraphInfo>();
							try {
								JSONObject jobj = new JSONObject(data);
								String startTime = jobj.getString(mServerQueryMgr.getParameter(15));
								String value = jobj.getString(mServerQueryMgr.getParameter(51));
								int count = jobj.getInt(mServerQueryMgr.getParameter(127));

								long utcTimeSec = 0;
								try {
									Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(startTime); // 로컬시간을 UTC로 변경하는 줄 알고 -9로 변경됨
									utcTimeSec = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()) / 1000; // +9로 실제 UTC로 변경
								} catch (ParseException e) {

								}

								if (count > 0) {
									MovementGraphInfo movementGraphInfo = new MovementGraphInfo();
									movementGraphInfo.deviceId = diaperSensor.deviceId;
									movementGraphInfo.startUtcTimeMs = utcTimeSec * 1000;
									movementGraphInfo.value = value;
									movementGraphInfo.created = System.currentTimeMillis();
									movementGraphInfo.count = count;

									movementGraphInfoList.add(movementGraphInfo);

									utcTimeSec += (count - 1) * 10;

									mDatabaseMgr.insertMovementGraphInfoList(movementGraphInfoList);
									//mPrefManager.setLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId, utcTimeSec - 60 * 10);
									mPrefManager.setLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId, utcTimeSec);
									//if (DBG) Log.d(TAG, "setLatestMovementGraphUpdatedTimeSec: utc(" + (utcTimeSec - 60 * 10) + ")");
									if (DBG) Log.d(TAG, "setLatestMovementGraphUpdatedTimeSec: utc(" + (DateTimeUtil.getNotConvertedDateTime(utcTimeSec)) + ")");
								}

								if (mUpperLayerHandler != null) {
									if (backgroundPolling) {
										mUpperLayerHandler.obtainMessage(MSG_MOVEMENT_GRAPH_DATA_RECEIVED, movementGraphInfoList.size(), movementGraphInfoList.size()).sendToTarget();
									}
								}

							} catch (JSONException e) {
								if (DBG) Log.e(TAG, e.toString());
								if (backgroundPolling) {
									_sendFinishedGetMovementGraphMessage(diaperSensor);
								}
							} catch (NullPointerException e) {
								if (DBG) Log.e(TAG, e.toString());
								if (backgroundPolling) {
									_sendFinishedGetMovementGraphMessage(diaperSensor);
								}
							}
						}
						mGetMovementGraphTable.remove(diaperSensor.deviceId * 10 + DeviceType.DIAPER_SENSOR);
						if (backgroundPolling) {
							_sendFinishedGetMovementGraphMessage(diaperSensor);
						}
					}
				});
	}

	public void getMovementGraphList(final DeviceDiaperSensor diaperSensor) {
		_getMovementGraphList(diaperSensor, true);
	}

	public void _getMovementGraphList(final DeviceDiaperSensor diaperSensor, final boolean backgroundPolling) {
		if (mGetMovementGraphTable == null || diaperSensor == null) return;
		if (mGetMovementGraphTable.containsKey(diaperSensor.deviceId * 10 + DeviceType.DIAPER_SENSOR) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getMovementGraphList: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetMovementGraphTable.put(diaperSensor.deviceId * 10 + DeviceType.DIAPER_SENSOR, true);

		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId) * 1000);
		if (DBG) Log.d(TAG, "LatestGetMovementGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId) + ")");
		mServerQueryMgr.getMovementGraphList(
				diaperSensor.deviceId,
				startTimeStamp,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						long total = 0L;
						long now = 0L;
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							ArrayList<MovementGraphInfo> movementGraphInfoList = new ArrayList<MovementGraphInfo>();
							try {
								JSONObject jobj = new JSONObject(data);
								String startTime = jobj.getString(mServerQueryMgr.getParameter(15));
								String value = jobj.getString(mServerQueryMgr.getParameter(51));
								int count = jobj.getInt(mServerQueryMgr.getParameter(127));

								long utcTimeSec = 0;
								try {
									Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(startTime); // 로컬시간을 UTC로 변경하는 줄 알고 -9로 변경됨
									utcTimeSec = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()) / 1000; // +9로 실제 UTC로 변경
								} catch (ParseException e) {

								}

								if (count > 0) {
									MovementGraphInfo movementGraphInfo = new MovementGraphInfo();
									movementGraphInfo.deviceId = diaperSensor.deviceId;
									movementGraphInfo.startUtcTimeMs = utcTimeSec * 1000;
									movementGraphInfo.value = value;
									movementGraphInfo.created = System.currentTimeMillis();
									movementGraphInfo.count = count;

									movementGraphInfoList.add(movementGraphInfo);

									utcTimeSec += (count - 1) * 10;

									mDatabaseMgr.insertMovementGraphInfoList(movementGraphInfoList);
									//mPrefManager.setLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId, utcTimeSec - 60 * 10);
									mPrefManager.setLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId, utcTimeSec);
									//if (DBG) Log.d(TAG, "setLatestMovementGraphUpdatedTimeSec: utc(" + (utcTimeSec - 60 * 10) + ")");
									if (DBG) Log.d(TAG, "setLatestMovementGraphUpdatedTimeSec: utc(" + (DateTimeUtil.getNotConvertedDateTime(utcTimeSec)) + ")");
								}

								if (mUpperLayerHandler != null) {
									if (backgroundPolling) {
										mUpperLayerHandler.obtainMessage(MSG_MOVEMENT_GRAPH_DATA_RECEIVED, movementGraphInfoList.size(), movementGraphInfoList.size()).sendToTarget();
									}
								}

							} catch (JSONException e) {
								if (DBG) Log.e(TAG, e.toString());
								if (backgroundPolling) {
									_sendFinishedGetMovementGraphMessage(diaperSensor);
								}
							} catch (NullPointerException e) {
								if (DBG) Log.e(TAG, e.toString());
								if (backgroundPolling) {
									_sendFinishedGetMovementGraphMessage(diaperSensor);
								}
							}
						}
						mGetMovementGraphTable.remove(diaperSensor.deviceId * 10 + DeviceType.DIAPER_SENSOR);
						if (backgroundPolling) {
							_sendFinishedGetMovementGraphMessage(diaperSensor);
						}
					}
				});
	}
	public void getHubGraphList(final DeviceAQMHub hub) {
		if (mGetHubGraphTable == null || hub == null) return;
		if (mGetHubGraphTable.containsKey(hub.deviceId * 10 + DeviceType.AIR_QUALITY_MONITORING_HUB) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getHubGraphList: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetHubGraphTable.put(hub.deviceId * 10 + DeviceType.AIR_QUALITY_MONITORING_HUB, true);

		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestHubGraphUpdatedTimeSec(hub.deviceId) * 1000);
		if (DBG) Log.d(TAG, "LatestGetHubGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestHubGraphUpdatedTimeSec(hub.deviceId) + ")");
		//final EnvironmentCheckManager environmentCheckMgr = new EnvironmentCheckManager(mContext);
		//environmentCheckMgr.setTemperatureThreshold(hub.getMinTemperature(), hub.getMaxTemperature());
		//environmentCheckMgr.setHumidityThreshold(hub.getMinHumidity(), hub.getMaxHumidity());
		mServerQueryMgr.getHubGraphList(
				hub.deviceId,
				startTimeStamp,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						long total = 0L;
						long now = 0L;
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							ArrayList<HubGraphInfo> hubGraphInfoList = new ArrayList<HubGraphInfo>();
							try {
								JSONObject wholeObj = new JSONObject(data);
								String time = wholeObj.optString(mServerQueryMgr.getParameter(15), null);
								String temperatureString = wholeObj.optString(mServerQueryMgr.getParameter(39), null);
								String humidityString = wholeObj.optString(mServerQueryMgr.getParameter(40), null);
								String vocString = wholeObj.optString(mServerQueryMgr.getParameter(41), null);

								if (time == null || time.length() == 0) {
									if (DBG) Log.e(TAG, "time no data");
									throw new NullPointerException();
								}

								String[] tem = null;
								String[] hum = null;
								String[] voc = null;
								if (temperatureString != null) {
									tem = temperatureString.split(",");
								}
								if (humidityString != null) {
									hum = humidityString.split(",");
								}
								if (vocString != null) {
									voc = vocString.split(",");
								}

								int minLength = 9999;
								if (tem != null && minLength > tem.length) minLength = tem.length;
								if (hum != null && minLength > hum.length) minLength = hum.length;
								if (voc != null && minLength > voc.length) minLength = voc.length;
								if (DBG) Log.d(TAG, "minLength : " + minLength);

								long utcTimeSec = 0;
								try {
									Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time); // 로컬시간을 UTC로 변경하는 줄 알고 -9로 변경됨
									utcTimeSec = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()) / 1000; // +9로 실제 UTC로 변경
								} catch (ParseException e) {

								}

								if (tem != null && tem.length > 0 &&
										hum != null && hum.length > 0 &&
										voc != null && voc.length > 0) {
									for (int i = 0; i < minLength; i++) {
										if (tem[i].length() == 0 || hum[i].length() == 0 || voc[i].length() == 0) continue;
										HubGraphInfo hubGraphInfo = new HubGraphInfo();
										hubGraphInfo.deviceId = hub.deviceId;
										hubGraphInfo.temperature = Integer.parseInt(tem[i]);
										hubGraphInfo.humidity = Integer.parseInt(hum[i]);
										hubGraphInfo.voc = Integer.parseInt(voc[i]);
										if (hubGraphInfo.temperature == -1 || hubGraphInfo.humidity == -1) {
											// 세가지 값중 하나라도 초기값이라면, 연결이 안된 것이라고 판단할 수 있음
											hubGraphInfo.temperature = -999;
											hubGraphInfo.humidity = -999;
											hubGraphInfo.voc = -999;
											hubGraphInfo.score = -999;
										} else {
											//hubGraphInfo.score = environmentCheckMgr.calculateScore(hubGraphInfo.temperature / 100, hubGraphInfo.humidity / 100, hubGraphInfo.voc / 100);
										}

										hubGraphInfo.timeSec = utcTimeSec;

										//if (DBG) Log.d(TAG, "[" + i + "] utc " + DateTimeUtil.getNotConvertedDateTime(utcTimeSec * 1000) + " / " + tem[i] + " / " + hum[i] + " / " + voc[i] + " => " + hubGraphInfo.score);

										hubGraphInfoList.add(hubGraphInfo);
										utcTimeSec += 60 * 10;
									}
								}

								mDatabaseMgr.insertHubGraphInfoList(hubGraphInfoList);
								mUpperLayerHandler.obtainMessage(MSG_HUB_GRAPH_DATA_RECEIVED, hubGraphInfoList.size(), hubGraphInfoList.size()).sendToTarget();
//								long idx;
//								for (int i = 0; i < hubGraphInfoList.size(); i++) {
//									HubGraphInfo info = hubGraphInfoList.get(i);
//									idx = mDatabaseMgr.insertDB(info);
//									if (mUpperLayerHandler != null) {
//										if (i == hubGraphInfoList.size() - 1) {
//											_sendFinishedGetHubGraphMessage(hub);
//										} else {
//											mUpperLayerHandler.obtainMessage(MSG_HUB_GRAPH_DATA_RECEIVED, i, hubGraphInfoList.size()).sendToTarget();
//										}
//									}
//								}

								if (hub != null) {
									mPrefManager.setLatestHubGraphUpdatedTimeSec(hub.deviceId, utcTimeSec - 60 * 10);
								}
								if (DBG) Log.d(TAG, "setLatestHubGraphUpdatedTimeSec: utc(" + (utcTimeSec - 60 * 10) + ")");
							} catch (JSONException e) {
								if (DBG) Log.e(TAG, e.toString());
								_sendFinishedGetHubGraphMessage(hub);
							} catch (NullPointerException e) {
								if (DBG) Log.e(TAG, e.toString());
								_sendFinishedGetHubGraphMessage(hub);
							}
						}
						mGetHubGraphTable.remove(hub.deviceId * 10 + DeviceType.AIR_QUALITY_MONITORING_HUB);
						_sendFinishedGetHubGraphMessage(hub);
					}
				});
	}

	public void getLampGraphList(final DeviceLamp lamp) {
		if (mGetLampGraphTable == null) return;
		if (mGetLampGraphTable.containsKey(lamp.deviceId * 10 + DeviceType.LAMP) == true) { // is getting notification?
			if (DBG) Log.e(TAG, "getLampGraphList: Duplicated, skip");
			return;
		}

		if (mServerQueryMgr == null) return;
		mGetLampGraphTable.put(lamp.deviceId * 10 + DeviceType.LAMP, true);

		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestHubGraphUpdatedTimeSec(lamp.deviceId) * 1000);
		if (DBG) Log.d(TAG, "LatestGetLampGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestHubGraphUpdatedTimeSec(lamp.deviceId) + ")");
		//final EnvironmentCheckManager environmentCheckMgr = new EnvironmentCheckManager(mContext);
		//environmentCheckMgr.setTemperatureThreshold(hub.getMinTemperature(), hub.getMaxTemperature());
		//environmentCheckMgr.setHumidityThreshold(hub.getMinHumidity(), hub.getMaxHumidity());
		mServerQueryMgr.getLampGraphList(
				lamp.deviceId,
				startTimeStamp,
				new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						long total = 0L;
						long now = 0L;
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
							ArrayList<LampGraphInfo> lampGraphInfoList = new ArrayList<LampGraphInfo>();
							try {
								JSONObject wholeObj = new JSONObject(data);
								String time = wholeObj.optString(mServerQueryMgr.getParameter(15), null);
								String temperatureString = wholeObj.optString(mServerQueryMgr.getParameter(39), null);
								String humidityString = wholeObj.optString(mServerQueryMgr.getParameter(40), null);

								if (time == null || time.length() == 0) {
									if (DBG) Log.e(TAG, "time no data");
									throw new NullPointerException();
								}

								String[] tem = null;
								String[] hum = null;
								if (temperatureString != null) {
									tem = temperatureString.split(",");
								}
								if (humidityString != null) {
									hum = humidityString.split(",");
								}

								int minLength = 9999;
								if (tem != null && minLength > tem.length) minLength = tem.length;
								if (hum != null && minLength > hum.length) minLength = hum.length;
								if (DBG) Log.d(TAG, "minLength : " + minLength);

								long utcTimeSec = 0;
								try {
									Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(time); // 로컬시간을 UTC로 변경하는 줄 알고 -9로 변경됨
									utcTimeSec = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()) / 1000; // +9로 실제 UTC로 변경
								} catch (ParseException e) {

								}

								if (tem != null && tem.length > 0 &&
										hum != null && hum.length > 0) {
									for (int i = 0; i < minLength; i++) {
										if (tem[i].length() == 0 || hum[i].length() == 0) continue;
										LampGraphInfo lampGraphInfo = new LampGraphInfo();
										lampGraphInfo.deviceId = lamp.deviceId;
										lampGraphInfo.temperature = Integer.parseInt(tem[i]);
										lampGraphInfo.humidity = Integer.parseInt(hum[i]);
										if (lampGraphInfo.temperature == -1 || lampGraphInfo.humidity == -1) {
											// 세가지 값중 하나라도 초기값이라면, 연결이 안된 것이라고 판단할 수 있음
											lampGraphInfo.temperature = -999;
											lampGraphInfo.humidity = -999;
										} else {
											//hubGraphInfo.score = environmentCheckMgr.calculateScore(hubGraphInfo.temperature / 100, hubGraphInfo.humidity / 100, hubGraphInfo.voc / 100);
										}

										lampGraphInfo.timeSec = utcTimeSec;

										if (DBG) Log.d(TAG, "[" + i + "] utc " + DateTimeUtil.getNotConvertedDateTime(utcTimeSec * 1000) + " / " + tem[i] + " / " + hum[i] + " => " + lampGraphInfo.score);

										lampGraphInfoList.add(lampGraphInfo);
										utcTimeSec += 60 * 10;
									}
								}

								mDatabaseMgr.insertLampGraphInfoList(lampGraphInfoList);
								mUpperLayerHandler.obtainMessage(MSG_LAMP_GRAPH_DATA_RECEIVED, lampGraphInfoList.size(), lampGraphInfoList.size()).sendToTarget();

								if (lamp != null) {
									mPrefManager.setLatestLampGraphUpdatedTimeSec(lamp.deviceId, utcTimeSec - 60 * 10);
								}
								if (DBG) Log.d(TAG, "setLatestLampGraphUpdatedTimeSec: utc(" + (utcTimeSec - 60 * 10) + ")");
							} catch (JSONException e) {
								if (DBG) Log.e(TAG, e.toString());
								_sendFinishedGetLampGraphMessage(lamp);
							} catch (NullPointerException e) {
								if (DBG) Log.e(TAG, e.toString());
								_sendFinishedGetLampGraphMessage(lamp);
							}
						}
						mGetLampGraphTable.remove(lamp.deviceId * 10 + DeviceType.LAMP);
						_sendFinishedGetLampGraphMessage(lamp);
					}
				});
	}

	private void _sendFinishedGetMovementGraphMessage(DeviceDiaperSensor diaperSensor) {
		if (mUpperLayerHandler != null) {
			mUpperLayerHandler.obtainMessage(MSG_MOVEMENT_GRAPH_DATA_RECEIVED, 0, 0).sendToTarget();
		}
		if (diaperSensor == null) return;
		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId) * 1000);
		if (DBG) Log.d(TAG, "FinishedGetMovementGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestMovementGraphUpdatedTimeSec(diaperSensor.deviceId) + ")");
	}

	private void _sendFinishedGetHubGraphMessage(DeviceAQMHub hub) {
		if (mUpperLayerHandler != null) {
			mUpperLayerHandler.obtainMessage(MSG_HUB_GRAPH_DATA_RECEIVED, 0, 0).sendToTarget();
		}
		if (hub == null) return;
		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestHubGraphUpdatedTimeSec(hub.deviceId) * 1000);
		if (DBG) Log.d(TAG, "FinishedGetHubGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestHubGraphUpdatedTimeSec(hub.deviceId) + ")");
	}

	private void _sendFinishedGetLampGraphMessage(DeviceLamp lamp) {
		if (mUpperLayerHandler != null) {
			mUpperLayerHandler.obtainMessage(MSG_LAMP_GRAPH_DATA_RECEIVED, 0, 0).sendToTarget();
		}
		if (lamp == null) return;
		String startTimeStamp = DateTimeUtil.getNotConvertedDateTime(mPrefManager.getLatestLampGraphUpdatedTimeSec(lamp.deviceId) * 1000);
		if (DBG) Log.d(TAG, "FinishedGetLampGraph: utc(" + startTimeStamp + ") / utc(" + mPrefManager.getLatestLampGraphUpdatedTimeSec(lamp.deviceId) + ")");
	}


	public void initDeviceStatusToCloud(DeviceInfo deviceInfo) {
		mHandler.obtainMessage(MSG_INIT_DEVICE, deviceInfo).sendToTarget();
	}

	public void startGetDeviceStatusFromCloud() {
		countGetDeviceStatusDone = 0;
		mHandler.sendEmptyMessage(MSG_GET_DEVICE_STATUS_FROM_CLOUD);
	}

	public void startSetDeviceStatusToCloud() {
		mHandler.sendEmptyMessage(MSG_SET_DEVICE_STATUS_TO_CLOUD);
	}

	public void stopSetDeviceStatusToCloud() {
		mHandler.removeMessages(MSG_SET_DEVICE_STATUS_TO_CLOUD);
	}

	public void stopGetDeviceStatusFromCloud() {
		mHandler.removeMessages(MSG_GET_DEVICE_STATUS_FROM_CLOUD);
	}

	public void startBetaTestInputAlarm() {
		mHandler.sendEmptyMessage(MSG_BETA_TEST_INPUT_ALARM);
	}

	public void stopBetaTestInputAlarm() {
		mHandler.removeMessages(MSG_BETA_TEST_INPUT_ALARM);
	}

	public void setFastDetectionAlarm(boolean enable) {
		if (DBG) Log.d(TAG, "setFastDetectionAlarm: " + enable);
		mHandler.removeMessages(MSG_DISABLE_FAST_DETECTION);
		if (enable) {
			Configuration.FAST_DETECTION = true;
			mHandler.sendEmptyMessageDelayed(MSG_DISABLE_FAST_DETECTION, 60 * 1000);
		} else {
			Configuration.FAST_DETECTION = false;
		}
	}

	public void sendFakeAlert(int notitype, int deviceType, long deviceId, String enc, long timeSec) {
		String updateData = null;
		try {
			JSONObject jobj = new JSONObject();
			jobj.put(mServerQueryMgr.getParameter(28), deviceType);
			jobj.put(mServerQueryMgr.getParameter(26), deviceId);
			jobj.put(mServerQueryMgr.getParameter(27), enc);
			if (timeSec == 0) timeSec = System.currentTimeMillis() / 1000;
			jobj.put(mServerQueryMgr.getParameter(15), timeSec);

			switch(notitype) {
				case NotificationType.PEE_DETECTED:
					jobj.put(mServerQueryMgr.getParameter(52), 1);
					break;
				case NotificationType.POO_DETECTED:
					jobj.put(mServerQueryMgr.getParameter(52), 2);
					break;
				case NotificationType.FART_DETECTED:
					jobj.put(mServerQueryMgr.getParameter(52), 5);
					break;
			}

			updateData = "[" + jobj.toString() + "]";
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (updateData == null) return;

		mServerQueryMgr.setDeviceStatus(updateData, new ServerManager.ServerResponseListener() {
			@Override
			public void onReceive(int responseCode, String errcode, String data) {
				if (responseCode == ServerManager.RESPONSE_CODE_OK) {
					if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
						if (DBG) Log.d(TAG, data);
					}
				}
			}
		});
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//if (DBG) Log.d(TAG, "handleMessage : " + msg.what);

			switch(msg.what) {
				case MSG_SET_USER_INFO_DATA_FROM_CLOUD:
					_setUserInfoFromCloud((String) msg.obj);
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_SET_USER_INFO_DATA_FROM_CLOUD, msg.obj).sendToTarget();
					}
					break;
				case MSG_GET_DEVICE_STATUS_FROM_CLOUD:
					this.removeMessages(MSG_GET_DEVICE_STATUS_FROM_CLOUD);
					if (Configuration.CERTIFICATE_MODE && em.getInstance(mContext).isReady()) {
						this.sendEmptyMessageDelayed(MSG_GET_DEVICE_STATUS_FROM_CLOUD, TIME_GET_DEVICE_STATUS_FROM_CLOUD_FOR_CERT_SEC * 1000);
					} else {
						this.sendEmptyMessageDelayed(MSG_GET_DEVICE_STATUS_FROM_CLOUD, TIME_GET_DEVICE_STATUS_FROM_CLOUD_SEC * 1000);
					}

					if (!em.getInstance(mContext).isReady()) {
						mServerQueryMgr.getAppData(new ServerManager.ServerResponseListener() {
							@Override
							public void onReceive(int responseCode, String errCode, String data) {
								if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
									em.getInstance(mContext).setAppData(ServerManager.getStringFromJSONObj(data, mServerQueryMgr.getParameter(80)));
								} else {

								}
							}
						});
						break;
					}

					// Background일 때,
					if (MonitApplication.isBackground) {
						if (DBG) Log.d(TAG, "DO NOT CHECK FROM CLOUD");
					} else {
						if (countGetDeviceStatusDone == 0) {
							updateDeviceFullStatusFromCloud(null);
						} else {
							if (mRegisteredDiaperSensorList.size() + mRegisteredAQMHubList.size() + mRegisteredLampList.size() + mRegisteredElderlyDiaperSensorList.size() > getBleConnectedDeviceCount()) {
								updateDeviceStatusFromCloud(null);
							} else {
								if (DBG) Log.d(TAG, "DO NOT CHECK FROM CLOUD : " + mRegisteredDiaperSensorList.size() + " / " + mRegisteredAQMHubList.size() + " / " + mRegisteredLampList.size() +  " / " + mRegisteredElderlyDiaperSensorList.size() + " / " +  getBleConnectedDeviceCount());
							}
						}
						countGetDeviceStatusDone = (countGetDeviceStatusDone + 1) % MAX_COUNT_GET_DEVICE_STATUS_FROM_CLOUD_DONE;
					}

					// 센서 움직임 가져오기
//					for (DeviceDiaperSensor sensor: mRegisteredDiaperSensorList.values()) {
//						if (sensor != null) {
//							_getMovementGraphList(sensor, false);
//						}
//					}
					break;

				case MSG_SET_DEVICE_STATUS_TO_CLOUD:
					this.removeMessages(MSG_SET_DEVICE_STATUS_TO_CLOUD);
					if (Configuration.CERTIFICATE_MODE && em.getInstance(mContext).isReady()) {
						this.sendEmptyMessageDelayed(MSG_SET_DEVICE_STATUS_TO_CLOUD, TIME_SET_DEVICE_STATUS_FROM_CLOUD_FOR_CERT_SEC * 1000);
					} else {
						this.sendEmptyMessageDelayed(MSG_SET_DEVICE_STATUS_TO_CLOUD, TIME_SET_DEVICE_STATUS_FROM_CLOUD_SEC * 1000);
					}

					// 데이터 보내는 것은 Background 여도 상관없이 보내야함
					// 나중에 BLE패킷을 받으면 업데이트 하는 걸로 해야함
					if (getBleConnectedDeviceCount() > 0) {
						updateDeviceStatusToCloud();
					} else {
						if (DBG) Log.d(TAG, "NO SET DEVICE STATUS : NO BLE CONNECTED");
					}
					break;

				case MSG_SET_DIAPER_SENSING_DATA_TO_CLOUD:
					final SensingData sensingData = (SensingData)msg.obj;
					Log.d(TAG, "[zuo] send it: " + sensingData.timeMs);
					mServerQueryMgr.setDiaperSensingLog(
							sensingData.deviceId,
							sensingData.temperature,
							sensingData.humidity,
							sensingData.voc,
							sensingData.capacitance,
							sensingData.acceleration,
							sensingData.sensorstatus,
							sensingData.movementlevel,
							sensingData.ethanol,
							sensingData.co2,
							sensingData.pressure,
							sensingData.compgas,
							sensingData.timeMs,
							new ServerManager.ServerResponseListener() {
								@Override
								public void onReceive(int responseCode, String errCode, String data) {
									if (responseCode == ServerManager.RESPONSE_CODE_OK) {
										if (DBG) Log.d(TAG, "send diaper sensing log: " + sensingData.timeMs);

										ArrayList<SensingData> sensingDataList = mDatabaseMgr.getSensingDataList();
										if (sensingDataList != null && sensingDataList.size() > 0) {
											for (final SensingData sensingData2 : sensingDataList) {
												if (DBG) Log.d(TAG, "send diaper sensing log2: " + sensingData2.timeMs + " / " + sensingDataList.size());
												mServerQueryMgr.setDiaperSensingLog(
														sensingData2.deviceId,
														sensingData2.temperature,
														sensingData2.humidity,
														sensingData2.voc,
														sensingData2.capacitance,
														sensingData2.acceleration,
														sensingData2.sensorstatus,
														sensingData2.movementlevel,
														sensingData2.ethanol,
														sensingData2.co2,
														sensingData2.pressure,
														sensingData2.compgas,
														sensingData2.timeMs,
														new ServerManager.ServerResponseListener() {
															@Override
															public void onReceive(int responseCode, String errCode, String data) {
																if (responseCode == ServerManager.RESPONSE_CODE_OK) {
																	if (DBG) Log.d(TAG, "receive succeeded delete it2: " + sensingData2.timeMs);
																	mDatabaseMgr.deleteDB(sensingData2);
																} else {
																	if (DBG) Log.d(TAG, "receive failed retry it2: " + sensingData2.timeMs);
																}
															}
														});
											}
										}
									} else {
										// 보내는데 실패하면 DB에 넣기
										if (DBG) Log.d(TAG, "receive failed insert it: " + sensingData.timeMs);
										mDatabaseMgr.insertDB(sensingData);
									}
								}
							});
					break;

				case MSG_SET_ELDERLY_DIAPER_SENSING_DATA_TO_CLOUD:
					final ElderlySensingData elderlySensingData = (ElderlySensingData)msg.obj;
					mServerQueryMgr.setElderlyDiaperSensingLog(
							elderlySensingData.deviceId,
							elderlySensingData.temperature,
							elderlySensingData.humidity,
							elderlySensingData.voc,
							elderlySensingData.capacitance,
							elderlySensingData.acceleration,
							elderlySensingData.sensorstatus,
							elderlySensingData.movementlevel,
							elderlySensingData.ethanol,
							elderlySensingData.co2,
							elderlySensingData.pressure,
							elderlySensingData.compgas,
							elderlySensingData.touch_ch1,
							elderlySensingData.touch_ch2,
							elderlySensingData.touch_ch3,
							elderlySensingData.touch_ch4,
							elderlySensingData.touch_ch5,
							elderlySensingData.touch_ch6,
							elderlySensingData.touch_ch7,
							elderlySensingData.touch_ch8,
							elderlySensingData.touch_ch9,
							elderlySensingData.timeMs,
							new ServerManager.ServerResponseListener() {
								@Override
								public void onReceive(int responseCode, String errCode, String data) {
									boolean succeeded = false;
									if (responseCode == ServerManager.RESPONSE_CODE_OK) {
										succeeded = true;

										ArrayList<ElderlySensingData> sensingDataList = mDatabaseMgr.getElderlySensingDataList();
										if (sensingDataList != null && sensingDataList.size() > 0) {
											for (final ElderlySensingData sensingData2 : sensingDataList) {
												mServerQueryMgr.setElderlyDiaperSensingLog(
														sensingData2.deviceId,
														sensingData2.temperature,
														sensingData2.humidity,
														sensingData2.voc,
														sensingData2.capacitance,
														sensingData2.acceleration,
														sensingData2.sensorstatus,
														sensingData2.movementlevel,
														sensingData2.ethanol,
														sensingData2.co2,
														sensingData2.pressure,
														sensingData2.compgas,
														sensingData2.touch_ch1,
														sensingData2.touch_ch2,
														sensingData2.touch_ch3,
														sensingData2.touch_ch4,
														sensingData2.touch_ch5,
														sensingData2.touch_ch6,
														sensingData2.touch_ch7,
														sensingData2.touch_ch8,
														sensingData2.touch_ch9,
														sensingData2.timeMs,
														new ServerManager.ServerResponseListener() {
															@Override
															public void onReceive(int responseCode, String errCode, String data) {
																if (responseCode == ServerManager.RESPONSE_CODE_OK) {
																	mDatabaseMgr.deleteDB(sensingData2);
																}
															}
														});
											}
										}
									}

									if (succeeded == false) {
										// 보내는데 실패하면 DB에 넣기
										mDatabaseMgr.insertDB(elderlySensingData);
									}
								}
							});
					break;
				case MSG_BLE_MANUALLY_CONNECTED:
					removeMessages(MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
					DeviceInfo deviceInfo = (DeviceInfo) msg.obj;
					if (deviceInfo == null) break;

					if (DBG) Log.i(TAG, "Manually connected " + deviceInfo.name + " / " + deviceInfo.btmacAddress);

					if (deviceInfo.type == DeviceType.DIAPER_SENSOR) {
						DeviceDiaperSensor sensor = getDeviceDiaperSensor(deviceInfo.deviceId);
						if (sensor != null) {
							sensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
						} else {
							if (DBG) Log.i(TAG, "add sensor view object : " + deviceInfo.deviceId);
							sensor = new DeviceDiaperSensor(mContext, deviceInfo);
							sensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							mRegisteredDiaperSensorList.put(deviceInfo.deviceId, sensor);
						}
						DeviceBLEConnection conn = getDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type);
						if (conn != null) {
							if (DBG) Log.i(TAG, "sync view object : " + deviceInfo.deviceId);
							conn.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							conn.syncViewObject();
						}
						startConnectionSensorToCloud(sensor);
					} else if (deviceInfo.type == DeviceType.AIR_QUALITY_MONITORING_HUB) {
						DeviceAQMHub hub = getDeviceAQMHub(deviceInfo.deviceId);
						if (hub != null) {
							hub.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
						} else {
							hub = new DeviceAQMHub(mContext, deviceInfo);
							hub.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							mRegisteredAQMHubList.put(deviceInfo.deviceId, hub);
						}
					} else if (deviceInfo.type == DeviceType.LAMP) {
						DeviceLamp lamp = getDeviceLamp(deviceInfo.deviceId);
						if (lamp != null) {
							lamp.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
						} else {
							if (DBG) Log.i(TAG, "add lamp view object : " + deviceInfo.deviceId);
							lamp = new DeviceLamp(mContext, deviceInfo);
							lamp.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							mRegisteredLampList.put(deviceInfo.deviceId, lamp);
						}
						DeviceBLEConnection conn = getDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type);
						if (conn != null) {
							if (DBG) Log.i(TAG, "sync view object : " + deviceInfo.deviceId);
							conn.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							conn.syncViewObject();
						}
						//startConnectionLampToCloud(lamp);
					} else if (deviceInfo.type == DeviceType.ELDERLY_DIAPER_SENSOR) {
						DeviceElderlyDiaperSensor sensor = getDeviceElderlyDiaperSensor(deviceInfo.deviceId);
						if (sensor != null) {
							sensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
						} else {
							if (DBG) Log.i(TAG, "add elderly sensor view object : " + deviceInfo.deviceId);
							sensor = new DeviceElderlyDiaperSensor(mContext, deviceInfo);
							sensor.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							mRegisteredElderlyDiaperSensorList.put(deviceInfo.deviceId, sensor);
						}
						DeviceBLEConnection conn = getDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type);
						if (conn != null) {
							if (DBG) Log.i(TAG, "sync view object : " + deviceInfo.deviceId);
							conn.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
							conn.syncViewObject();
						}
						startConnectionElderlyDiaperSensorToCloud(sensor);
					}
					deviceInfo.hasBleConnected = true;
					deviceInfo.insertDB(mContext);
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_BLE_MANUALLY_CONNECTED, DeviceConnectionState.BLE_CONNECTED, -1, deviceInfo).sendToTarget();
					} else {
						if (DBG) Log.e(TAG, "mUpperLayerHandler NULL");
					}
					break;
				case MSG_BLE_CONNECTION_STATE_CHANGE:
					int state = msg.arg1;
					DeviceInfo deviceInfo2 = (DeviceInfo) msg.obj;
					if (deviceInfo2.type == DeviceType.DIAPER_SENSOR) {
						DeviceDiaperSensor sensor = getDeviceDiaperSensor(deviceInfo2.deviceId);
						if (sensor != null) {
							sensor.setConnectionState(state);
							if (state == DeviceConnectionState.BLE_CONNECTED) {
								startConnectionSensorToCloud(sensor);
							}
						}
					} else if (deviceInfo2.type == DeviceType.AIR_QUALITY_MONITORING_HUB) {
						DeviceAQMHub hub = getDeviceAQMHub(deviceInfo2.deviceId);
						if (hub != null) {
							hub.setConnectionState(state);
						}
					} else if (deviceInfo2.type == DeviceType.LAMP) {
						DeviceLamp lamp = getDeviceLamp(deviceInfo2.deviceId);
						if (lamp != null) {
							lamp.setConnectionState(state);
						}
					}

					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_BLE_CONNECTION_STATE_CHANGE, state, -1, deviceInfo2).sendToTarget();
					} else {
						if (DBG) Log.e(TAG, "mUpperLayerHandler NULL");
					}
					break;

				case MSG_LEGACY_SCAN_FINISHED:
					if (DBG) Log.d(TAG, "MSG_LEGACY_SCAN_FINISHED");
					removeMessages(MSG_LEGACY_SCAN_FINISHED);
					cancelDiscovery();

					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							if (DBG) Log.d(TAG, "Stop LeScan");
							if (mBluetoothLeScanner == null) {
								mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
							}
							mBluetoothLeScanner.stopScan(mManualScanCallback);
							_doAfterDiscovering();
						}
					}, (TIME_BLE_LE_SCAN_TIME_OUT_SEC - 1) * 1000);
					if (mBluetoothLeScanner == null) {
						mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
					}
					mLeDiscoveryStartTimeMs = System.currentTimeMillis();
					if (DBG) Log.d(TAG, "Start LeScan");
					mBluetoothLeScanner.startScan(mManualScanCallback);
					break;

				case MSG_BLE_MANUAL_CONNECTION_TIME_OUT:
					int state3 = msg.arg1;
					int reason = msg.arg2;
					DeviceInfo deviceInfo3 = (DeviceInfo) msg.obj;

					mServerQueryMgr.setSensorConnectionLog(mConnectionLog.toString(), null);
					mConnectionLog.initialize();

					if (mManuallyConnectingDevice != null) {
						if (DBG) Log.e(TAG, "MSG_BLE_MANUAL_CONNECTION_TIME_OUT : " + deviceInfo3.btmacAddress);
						mManuallyConnectingDevice.initialize();
						mManuallyConnectingDevice = null;
						if (mUpperLayerHandler != null) {
							mUpperLayerHandler.obtainMessage(MSG_BLE_MANUAL_CONNECTION_TIME_OUT, state3, reason, deviceInfo3).sendToTarget();
						} else {
							if (DBG) Log.e(TAG, "mUpperLayerHandler NULL");
						}
					}
					break;

				case MSG_BLE_GATT_CONNECTION_ERROR:
					if (DBG) Log.d(TAG, "MSG_BLE_GATT_CONNECTION_ERROR");
					DeviceInfo deviceInfoGattErr = (DeviceInfo) msg.obj;
					if (deviceInfoGattErr != null) {
						NotiManager notiManager = NotiManager.getInstance(mContext);
						notiManager.notifyConnectionFailedAlarm(deviceInfoGattErr.type, deviceInfoGattErr.deviceId);
					}
					break;

				case MSG_SET_DEVICE_DATA:
					if (DBG) Log.d(TAG, "MSG_SET_DEVICE_DATA");
					//if (Configuration.ALLOW_SAVE_LOCAL_HISTORY_FILE) {
						CurrentSensorLog log = (CurrentSensorLog)msg.obj;
						if (log.id > 0 && mDebugMgr != null) mDebugMgr.saveEssentialHistoryFile(log);
					//}
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_SET_DEVICE_DATA, msg.arg1, msg.arg2, msg.obj).sendToTarget();
					}
					break;

				case MSG_SENSOR_VALUE_UPDATED:
					final int deviceId2 = msg.arg1;
					final CurrentSensorValue currSensorValue = (CurrentSensorValue)msg.obj;
					if (DBG) Log.d(TAG, "[" + deviceId2 + "] " + currSensorValue.toString());
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_SENSOR_VALUE_UPDATED, deviceId2, -1, currSensorValue).sendToTarget();
					} else {
						if (DBG) Log.e(TAG, "mUpperLayerHandler NULL");
					}
					if (Configuration.ALLOW_SAVE_LOCAL_HISTORY_FILE) {
						if (mDebugMgr != null) mDebugMgr.saveEssentialHistoryFile(currSensorValue);
					}
					break;

				case MSG_LAMP_VALUE_UPDATED:
					final int deviceId3 = msg.arg1;
					final CurrentLampValue currLampValue = (CurrentLampValue)msg.obj;
					if (DBG) Log.d(TAG, "[" + deviceId3 + "] " + currLampValue.toString());
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_LAMP_VALUE_UPDATED, deviceId3, -1, currLampValue).sendToTarget();
					} else {
						if (DBG) Log.e(TAG, "mUpperLayerHandler NULL");
					}
					break;

				case MSG_CONNECTION_ERROR:
					int errorCode = msg.arg1;
					DeviceInfo errDeviceInfo = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_CONNECTION_ERROR : [" + errDeviceInfo.deviceId + "] " + errDeviceInfo.name + " / " + errDeviceInfo.btmacAddress + " / " + errorCode);
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_CONNECTION_ERROR, errorCode, -1).sendToTarget();
					}
					break;

				case MSG_INIT_DEVICE:
					final DeviceInfo initTarget = (DeviceInfo)msg.obj;
					mLatestInitDeviceInfo = initTarget;
					ServerQueryManager.getInstance(mContext).initDevice(
							initTarget.type,
							initTarget.deviceId,
                            initTarget.getEnc(),
							new ServerManager.ServerResponseListener() {
								@Override
								public void onReceive(int responseCode, String errCode, String data) {
									if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
										if (DBG) Log.d(TAG, "initDevice succeeded : " + initTarget.type + " / " + initTarget.deviceId);
									} else {
										if (DBG) Log.d(TAG, "initDevice failed : " + initTarget.type + " / " + initTarget.deviceId);
									}
								}
							});
					break;

				case MSG_HUB_CONNECTED_WITH_SENSOR:
					int operationStatus = msg.arg1;
					DeviceInfo sensorInfo = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_HUB_CONNECTED_WITH_SENSOR : " + sensorInfo.toString());
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_HUB_CONNECTED_WITH_SENSOR, operationStatus, -1, sensorInfo).sendToTarget();
					}
					break;

				case MSG_HUB_WIFI_CONNECTION_STATE_CHANGE:
					int apConnectionStatus = msg.arg1;
					if (DBG) Log.d(TAG, "MSG_HUB_WIFI_CONNECTION_STATE_CHANGE : " + apConnectionStatus);
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_HUB_WIFI_CONNECTION_STATE_CHANGE, apConnectionStatus, -1).sendToTarget();
					}
					break;

				case MSG_HUB_WIFI_SCAN_LIST:
					HubApInfo apInfo = (HubApInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_HUB_WIFI_SCAN_LIST : " + apInfo.index + " / " + apInfo.securityType + " / " + apInfo.rssi + " / " + apInfo.name);
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_HUB_WIFI_SCAN_LIST, apInfo).sendToTarget();
					}
					break;

				case MSG_START_SCAN_FOR_RECONNECT:
					removeMessages(MSG_START_SCAN_FOR_RECONNECT);
					boolean forceScan = msg.arg1 == 1 ? true : false;
					if (DBG) Log.d(TAG, "MSG_SCAN_FOR_RECONNECT : " + mRegisteredBleDeviceList.size() + " / " + getBleConnectedDeviceCount() + " / " + mPrefManager.getSigninState() + " / " + forceScan);
					if (MonitApplication.isBackground) { // App이 Background일때만 최대 30초에 한번 3초동안 스캔
						countScanForReconnect++;
						nextScanInterval = countScanForReconnect * 3L;
						if (nextScanInterval > TIME_MAX_BLE_BACKGROUND_SCAN_PERIOD_SEC_FOR_BACKGROUND_APP) {
							nextScanInterval = TIME_MAX_BLE_BACKGROUND_SCAN_PERIOD_SEC_FOR_BACKGROUND_APP;
						}
						if (countScanForReconnect > 6) {
							countScanForReconnect = 0;
							forceScan = true;
						}
					} else { // App이 Foreground일때는 5초에 한번 3초동안 스캔
						countScanForReconnect++;
						nextScanInterval = TIME_MAX_BLE_BACKGROUND_SCAN_PERIOD_SEC_FOR_FOREGROUND_APP;
						if (countScanForReconnect > 12) { // Foreground일때 1분에 한번씩 강제스캔
							countScanForReconnect = 0;
							forceScan = true;
						}
					}

					mHandler.sendEmptyMessageDelayed(MSG_START_SCAN_FOR_RECONNECT, nextScanInterval * 1000);

					boolean needScan = false;
					// 혹시 GATT Disconnected 이벤트 받지 못하고 끊어진 센서가 있는지 확인
					for (DeviceBLEConnection bleConnection : mRegisteredBleDeviceList.values()) {
						if (bleConnection == null) continue;
						bleConnection.checkBLEConnection();

						if (!bleConnection.isLeScanFound) {
							needScan = true;
                            if (DBG) Log.d(TAG, "not found : " + bleConnection.getDeviceInfo().deviceId);
						} else {
                            if (DBG) Log.d(TAG, "already found : " + bleConnection.getDeviceInfo().deviceId);
                        }
					}

					if (needScan || forceScan) {
						// Reconnect를 위한 LE Scan 조건
						// 1. 로그인이 되어있는 상태
						// 2. Ble나 Wi-Fi 로 연결된 기기 갯수보다 BLE로 연결되어야 기기 갯수가 많으면 SCAN 시작
						if ((mPrefManager.getSigninState() == SignInState.STEP_COMPLETED) &&
								//(mRegisteredBleDeviceList.size() > getBleWifiConnectedDeviceCount())) {
								(mRegisteredBleDeviceList.size() > getBleConnectedDeviceCount())) {
							if (DBG) Log.d(TAG, "startLeScan : " + countScanForReconnect + " / force: " + forceScan);
							if (forceScan) {
								mReconnectMgr.setDiscoveryScan();
								mReconnectMgr.startLeScan();
							} else {
								mReconnectMgr.startLeScan();
							}
						} else {
                            if (DBG) Log.e(TAG, "NOT startLeScan count : " + mRegisteredBleDeviceList.size() + " / " + getBleConnectedDeviceCount());
                        }
					} else {
                        if (DBG) Log.e(TAG, "NOT startLeScan flag : " + needScan + " / " + forceScan);
                    }
					break;
				case MSG_STOP_SCAN_FOR_RECONNECT:
					removeMessages(MSG_STOP_SCAN_FOR_RECONNECT);
					if (DBG) Log.d(TAG, "MSG_STOP_SCAN_FOR_RECONNECT");
					if (isFullDiscoveryForAutoReconnect) {
						mBluetoothAdapter.cancelDiscovery();
					} else {
						mReconnectMgr.stopLeScan();
					}
					break;
				case MSG_BETA_TEST_INPUT_ALARM:
					removeMessages(MSG_BETA_TEST_INPUT_ALARM);
					NotiManager.getInstance(mContext).notifyBetatestInputAlarm(0);
					sendEmptyMessageDelayed(MSG_BETA_TEST_INPUT_ALARM, TIME_BETA_TEST_INPUT_ALARM_PERIOD_MIN * 1000 * 60);
					break;
				case MSG_NOTIFICATION_MESSAGE_UPDATED:
					removeMessages(MSG_NOTIFICATION_MESSAGE_UPDATED);
					if (DBG) Log.d(TAG, "MSG_NOTIFICATION_MESSAGE_UPDATED");
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.sendEmptyMessage(MSG_NOTIFICATION_MESSAGE_UPDATED);
					}
					break;
				case MSG_BLE_MANUAL_CONNECTION_GUEST:
					removeMessages(MSG_BLE_MANUAL_CONNECTION_TIME_OUT);
					DeviceInfo guestDeviceInfo = (DeviceInfo)msg.obj;
					if (DBG) Log.d(TAG, "MSG_BLE_MANUAL_CONNECTION_GUEST : " + guestDeviceInfo.toString());
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_BLE_MANUAL_CONNECTION_GUEST, guestDeviceInfo).sendToTarget();
					}
					break;
				case MSG_DISABLE_FAST_DETECTION:
					Configuration.FAST_DETECTION = false;
					break;
			}
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null) return;
				short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
				if (DBG) Log.d(TAG, "BluetoothDevice.ACTION_FOUND : " + device.getName() + " / " + device.getAddress() + " / " + device.describeContents() + " / " + device.getUuids() + " / " + device.getBondState() + " / " + rssi);

				if (isFullDiscoveryForAutoReconnect) {
					mReconnectMgr.addScannedDevice(device);
				} else {
					_addScannedDeviceList(device, rssi);
				}

			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				if (state == BluetoothAdapter.STATE_ON) {
					if (DBG) Log.d(TAG, "Bluetooth On : " + state);
					if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("BT ON");
					reconnectBleDevice();
				} else {
					if (DBG) Log.d(TAG, "Bluetooth Off : " + state);
					if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("BT OFF");
					mHandler.removeMessages(MSG_START_SCAN_FOR_RECONNECT);
					for (DeviceBLEConnection conn : mRegisteredBleDeviceList.values()) {
						if (conn == null) continue;
						conn.isLeScanFound = false;
						conn.close();
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if (DBG) Log.d(TAG, "BluetoothDevice.ACTION_DISCOVERY_FINISHED: " + isFullDiscoveryForAutoReconnect);
				long discoveryTimeMs = System.currentTimeMillis() - mFullDiscoveryStartTimeMs;
				if (discoveryTimeMs < 1000) {
					if (DBG) Log.d(TAG, "Too short discovery, need to do LEScan");
					sendTooShortFullDiscoveryMessage(discoveryTimeMs);
				}
				if (isFullDiscoveryForAutoReconnect) {
					isFullDiscoveryForAutoReconnect = false;
					mReconnectMgr.checkDeviceList();
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null) return;
				int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				if (DBG) Log.d(TAG, "BluetoothDevice.ACTION_BOND_STATE_CHANGED : " + device.getName() + " / " + device.getAddress() + " / " + device.describeContents() + " / " + device.getUuids() + " / " + device.getBondState());
				if (state == BluetoothDevice.BOND_BONDED) {
					if (DBG) Log.i(TAG, "Bonded");
				} else if (state == BluetoothDevice.BOND_BONDING) {
					if (DBG) Log.i(TAG, "Bonding");
				} else if (state == BluetoothDevice.BOND_NONE) {
					if (DBG) Log.i(TAG, "None");
				} else {
					if (DBG) Log.i(TAG, "else");
				}
			} else if (PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED.equals(action)) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
					if (pm.isDeviceIdleMode()) {
						if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("IDLE enter");
						if (DBG) Log.d(TAG, "PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED Enter");
					} else {
						if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("IDLE exit");
						if (DBG) Log.d(TAG, "PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED Exit");
					}
				}
			}

			/*else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
        		try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
	                    String name = device.getName();
	            		String addr = device.getAddress();
	                    if (isValidDevice(name, addr) == DeviceType.DIAPER_SENSOR) {
	                    	if (BluetoothDevice.DEVICE_TYPE_LE == device.getType()) {
	                    		int pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", DEFAULT_CONNECTION_PIN_CODE);
	                            //the pin in case you need to accept for an specific pin
	                            if (DBG) Log.d(TAG, "BluetoothDevice.ACTION_PAIRING_REQUEST : " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 123456));
	                            byte[] pinBytes;
	                            pinBytes = ("" + pin).getBytes("UTF-8");
	                            device.setPin(pinBytes);
	                            //device.setPairingConfirmation(true);
	                    	} else {
	                    		if (DBG) Log.d(TAG, "BluetoothDevice.ACTION_PAIRING_REQUEST not applied");
	                    	}
	                    }
                    }
	            } catch (Exception e) {
	                if (DBG) Log.e(TAG, "Auto pairing failed");
	            }
			}
	         */
		}
	};

	//region "Connection"
	private boolean isNewDeviceConnection = false;
	private HashMap<Integer, BluetoothDevice> mConnectingCandidateDevice;
	private ArrayList<BluetoothDevice> mScannedDeviceList;
	private ArrayList<String> mScannedDeviceMacList;
	private int mFindDeviceType;

	private void _addScannedDeviceList(BluetoothDevice device, int rssi) {
		if (device != null && isNewDeviceConnection) {
			String name = device.getName();
			String addr = device.getAddress();
			int deviceType = isValidDevice(name, addr);
			if (deviceType == mFindDeviceType) {
				boolean newDevice = true;
				for (DeviceBLEConnection conn : mRegisteredBleDeviceList.values()) {
					if (conn == null || conn.getDeviceInfo() == null) continue;
					if (device.getAddress().equals(conn.getDeviceInfo().btmacAddress)) {
						newDevice = false;
						if (DBG) Log.d(TAG, "scanned but duplicated");
						break;
					}
				}
				if (newDevice) {
					if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("add scan device: " + device.getAddress());
					mConnectingCandidateDevice.put(rssi, device);
					if (mConnectionLog.isManualConnection) {
						mConnectionLog.addScannedDevice(device.getName());
					}
				}
			}
			if (!mScannedDeviceMacList.contains(device.getAddress())) {
				mScannedDeviceList.add(device);
				mScannedDeviceMacList.add(device.getAddress());
			}
		}
	}

	public void manualConnectDiaperSensor() {
		mConnectionLog.initialize();
		mConnectionLog.isManualConnection = true;
		mConnectionLog.initialize();
		mFindDeviceType = DeviceType.DIAPER_SENSOR;
		isNewDeviceConnection = true;
		_manualDiscoverDevice();
	}

	public void manualConnectLamp() {
		mConnectionLog.initialize();
		mConnectionLog.isManualConnection = true;
		mConnectionLog.initialize();
		mFindDeviceType = DeviceType.LAMP;
		isNewDeviceConnection = true;
		_manualDiscoverDevice();
	}

	public void manualConnectElderlySensor() {
		mConnectionLog.initialize();
		mConnectionLog.isManualConnection = true;
		mConnectionLog.initialize();
		mFindDeviceType = DeviceType.ELDERLY_DIAPER_SENSOR;
		isNewDeviceConnection = true;
		_manualDiscoverDevice();
	}

	public void allowGuestManualConnection(boolean allow, DeviceInfo deviceInfo) {
		if (allow) {
			if (DBG) Log.d(TAG, "allowGuestManualConnection : " + mManuallyConnectingDevice.getDeviceInfo().deviceId + " / " + deviceInfo.deviceId);
			putDeviceBLEConnection(deviceInfo.deviceId, deviceInfo.type, mManuallyConnectingDevice);
			mHandler.obtainMessage(MSG_BLE_MANUALLY_CONNECTED, deviceInfo).sendToTarget();
		} else {
			mHandler.obtainMessage(MSG_BLE_MANUAL_CONNECTION_TIME_OUT, DeviceConnectionState.DISCONNECTED, 100, deviceInfo).sendToTarget(); // bleInitStep 100
		}
	}

	public void manualConnectAQMHub() {
		mFindDeviceType = DeviceType.AIR_QUALITY_MONITORING_HUB;
		isNewDeviceConnection = true;
		_manualDiscoverDevice();
	}

	private void _manualDiscoverDevice() {
		if (mConnectingCandidateDevice == null) {
			mConnectingCandidateDevice = new HashMap<Integer, BluetoothDevice>();
		} else {
			mConnectingCandidateDevice.clear();
		}

		if (mScannedDeviceList == null) {
			mScannedDeviceList = new ArrayList<>();
		} else {
			mScannedDeviceList.clear();
		}

		if (mScannedDeviceMacList == null) {
			mScannedDeviceMacList = new ArrayList<>();
		} else {
			mScannedDeviceMacList.clear();
		}

		if (isDiscovering()) {
			cancelDiscovery();
		}
		startDiscovery();
		// When devices are found, it puts into mConnectingCandidateDevice by _addScannedDeviceList()
	}

	private void _connectCandidateDevice() {
        if (mConnectingCandidateDevice.size() == 0) {
			if (mConnectingCandidateDevice.size() == 0) {
				mServerQueryMgr.setSensorConnectionLog(mConnectionLog.toString(), null);
				mConnectionLog.initialize();
			}
			return;
		}
        if (DBG) Log.i(TAG, "_connectCandidateDevice");

		BluetoothDevice candidateDevice = null;

        Iterator<Integer> itr = mConnectingCandidateDevice.keySet().iterator();
        int low = -999;
        while (itr.hasNext()) {
            int rssi = itr.next();
            if (rssi > low) {
				candidateDevice = mConnectingCandidateDevice.get(rssi);
				low = rssi;
            }
        }

        if (candidateDevice != null) {
			int status = checkBluetoothStatus();
			if (status == STATE_DISABLED || status == STATE_UNAVAILABLE) return;
			String name = null;
			switch (mFindDeviceType) {
				case DeviceType.DIAPER_SENSOR:
					name = DeviceInfo.DIAPER_SENSOR_BASE_NAME;
					switch (Configuration.APP_MODE) {
						case Configuration.APP_GLOBAL:
						//case Configuration.APP_MONIT_X_HUGGIES:
						case Configuration.APP_MONIT_X_KAO:
							name = DeviceInfo.DIAPER_SENSOR_BASE_NAME;
							break;
						case Configuration.APP_KC_HUGGIES_X_MONIT:
							name = DeviceInfo.KC_DIAPER_SENSOR_BASE_NAME;
							break;
					}
					DeviceInfo deviceInfo = new DeviceInfo(0, 0, mFindDeviceType, name, candidateDevice.getAddress(), null, null, candidateDevice.getName(), true, true, true, true, true);
					mManuallyConnectingDevice = new DeviceBLEConnection(mContext, deviceInfo, mHandler, false);
					if (DBG) Log.d(TAG, "set sensor leScanFound true");
					mManuallyConnectingDevice.isLeScanFound = true;
					mManuallyConnectingDevice.manualConnect();
					break;

				case DeviceType.LAMP:
					name = DeviceInfo.LAMP_BASE_NAME;
					DeviceInfo deviceInfoLamp = new DeviceInfo(0, 0, mFindDeviceType, name, candidateDevice.getAddress(), null, null, candidateDevice.getName(), true, true, true, true, true);
					mManuallyConnectingDevice = new DeviceBLEConnection(mContext, deviceInfoLamp, mHandler, false);
					if (DBG) Log.d(TAG, "set lamp leScanFound true");
					mManuallyConnectingDevice.isLeScanFound = true;
					mManuallyConnectingDevice.manualConnect();
					break;

				case DeviceType.AIR_QUALITY_MONITORING_HUB:
					break;

				case DeviceType.ELDERLY_DIAPER_SENSOR:
					name = DeviceInfo.ELDERLY_DIAPER_SENSOR_BASE_NAME;
					DeviceInfo deviceInfoElderlyDiaperSensor = new DeviceInfo(0, 0, mFindDeviceType, name, candidateDevice.getAddress(), null, null, candidateDevice.getName(), true, true, true, true, true);
					mManuallyConnectingDevice = new DeviceBLEConnection(mContext, deviceInfoElderlyDiaperSensor, mHandler, false);
					if (DBG) Log.d(TAG, "set lamp leScanFound true");
					mManuallyConnectingDevice.isLeScanFound = true;
					mManuallyConnectingDevice.manualConnect();
					break;
			}
        }
    }

	public void disconnect(long deviceId, int deviceType) {
		DeviceBLEConnection conn = getDeviceBLEConnection(deviceId, deviceType);
		if (conn != null) {
			conn.disconnect();
		}
	}
	//endregion

	//region "Discover"
	public void manualCancelDiscovery() {
    	if (mBluetoothAdapter == null) return;

        if (mBluetoothAdapter.isDiscovering()) {
			if (DBG) Log.d(TAG, "Manually Stop Connection");
            isManuallyStopConnecting = true;
			cancelDiscovery();
        }
    }

	public void cancelDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
		isDiscovering = false;
        if (DBG) Log.d(TAG, "cancelDiscovery");
	}
    
    public boolean isDiscovering() {
    	if (mBluetoothAdapter == null) return false;
    	return mBluetoothAdapter.isDiscovering();
    }

	private ScanCallback mManualScanCallback =
			new ScanCallback() {
				@Override
				public void onScanResult(int callbackType, ScanResult result) {
					//if (DBG) Log.d(TAG, "onScanResult: " + result.getDevice().getName());
					_processResult(result);
				}

				@Override
				public void onBatchScanResults(List<ScanResult> results) {
					long discoveryTimeMs = System.currentTimeMillis() - mLeDiscoveryStartTimeMs;
					if (DBG) Log.d(TAG, "onBatchScanResults: " + results.size() + " / " + discoveryTimeMs);
					for (ScanResult result : results) {
						_processResult(result);
					}
				}

				@Override
				public void onScanFailed(int errorCode) {
					if (DBG) Log.d(TAG, "onScanFailed: " + errorCode);
					sendLeDiscoveryFailed();
				}

				private void _processResult(final ScanResult result) {
					final BluetoothDevice device = result.getDevice();
					final int rssi = result.getRssi();
					if (DBG) Log.d(TAG, "manualLeScanCallback : " + device.getName() + " / " + device.getAddress() + " / " + device.describeContents() + " / " + device.getUuids() + " / " + device.getBondState() + " / " + rssi);
					_addScannedDeviceList(device, rssi);
				}
			};
	private long mFullDiscoveryStartTimeMs = 0;
	private long mLeDiscoveryStartTimeMs = 0;

    public void startDiscovery() {
    	if (DBG) Log.d(TAG, "startDiscovery");
		if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("startDiscovery");
        if (mBluetoothAdapter == null) return;

		cancelDiscovery();
		isManuallyStopConnecting = false;
		mFullDiscoveryStartTimeMs = System.currentTimeMillis();
        mBluetoothAdapter.startDiscovery(); // Full Scan
		isDiscovering = true;
		mHandler.removeMessages(MSG_LEGACY_SCAN_FINISHED);
		mHandler.sendEmptyMessageDelayed(MSG_LEGACY_SCAN_FINISHED, TIME_BLE_LEGACY_SCAN_TIME_OUT_SEC * 1000);
    }

    private void _doAfterDiscovering() {
		if (isManuallyStopConnecting) {
			if (DBG) Log.d(TAG, "ManualCancel");
			if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("ManualCancel");
			isManuallyStopConnecting = false;
		} else {
			if (isNewDeviceConnection) {
				isNewDeviceConnection = false;

				if (mUpperLayerHandler != null) {
					Message msg = mUpperLayerHandler.obtainMessage(MSG_SCAN_FINISHED);
					msg.arg1 = mConnectingCandidateDevice.size();
					msg.arg2 = (int)(System.currentTimeMillis() - mFullDiscoveryStartTimeMs);
					final ArrayList<BluetoothDevice> foundList = mScannedDeviceList;
					msg.obj = foundList;
					mUpperLayerHandler.sendMessage(msg);
				} else {
					if (DBG) Log.e(TAG, "mUpperLayerHandler NULL");
				}
				_connectCandidateDevice();
			} else {
				if (DBG) Log.d(TAG, "Auto connecting");
			}
		}
	}
	//endregion

	public void write(long deviceId, int deviceType, byte[] data) {
		if (DBG) Log.d(TAG, "write bytes [" + deviceId + " / " + deviceType + "] : " + data);
		DeviceBLEConnection conn = getDeviceBLEConnection(deviceId, deviceType);
		if (conn != null) {
			conn.write(data);
		}
	}

	public static int getBleConnectedDeviceCount() {
		int count = 0;
		for (DeviceDiaperSensor conn : mRegisteredDiaperSensorList.values()) {
			if (conn.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
				count++;
			}
		}
		for (DeviceElderlyDiaperSensor conn : mRegisteredElderlyDiaperSensorList.values()) {
			if (conn.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
				count++;
			}
		}
		return count;
	}

	public static int getBleWifiConnectedDeviceCount() {
		int count = 0;
		for (DeviceDiaperSensor conn : mRegisteredDiaperSensorList.values()) {
			if (conn.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
				count++;
			} else if (conn.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
				// 허브에 꽂혀서 충전중인 센서는 BLE연결이 가능
				if (conn.getOperationStatus() < DeviceStatus.OPERATION_HUB_NO_CHARGE) {
					count++;
				}
			}
		}
		return count;
	}

    public static int checkBluetoothStatus() {
    	BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter() ;
        if (adapter == null) {
        	return STATE_UNAVAILABLE;
        }
        if (adapter.isEnabled()) {
        	return STATE_DISCONNECTED;
        } else {
        	return STATE_DISABLED;
        }
    }

	public boolean checkLocationStatus() {
		LocationManager locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
		boolean gpsEnabled = false;
		boolean networkEnabled = false;

		try {
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {;}
		try {
			networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {;}

		if (DBG) Log.d(TAG, "checkLocationStatus: " + (gpsEnabled & networkEnabled));
		return gpsEnabled & networkEnabled;
	}

    public static int isValidDevice(String name, String address) {
    	if (name == null || address == null) {
    		return -1;
    	}
    	name = name.toLowerCase();
    	if (address.length() != 17) { // 11:22:33:44:55:66
    		return -1;
    	}

		if (name.contains("monit_diaper")) { // 11:22:33:44:55:66
			if (DBG) Log.d(TAG, "Diaper Sensor FOUND : " + address);
			return DeviceType.DIAPER_SENSOR;
		}
		if (name.contains("monit_elderly")) { // 11:22:33:44:55:66
			if (DBG) Log.d(TAG, "Elderly Sensor FOUND : " + address);
			return DeviceType.ELDERLY_DIAPER_SENSOR;
		}
		if (name.contains("monit_lamp")) { // 11:22:33:44:55:66
			if (DBG) Log.d(TAG, "Lamp FOUND : " + address);
			return DeviceType.LAMP;
		}
    	return -1;
    }

	public void requestForceLeScan() {
		mHandler.obtainMessage(MSG_START_SCAN_FOR_RECONNECT, 1, 0).sendToTarget();
	}

	public void reconnectBleDevice() {
		countScanForReconnect = 0;
		//mHandler.obtainMessage(MSG_START_SCAN_FOR_RECONNECT, 0, 0).sendToTarget();
		mHandler.obtainMessage(MSG_START_SCAN_FOR_RECONNECT, 1, 0).sendToTarget();
		/*
		for (DeviceBLEConnection conn : ConnectionManager.mRegisteredBleDeviceList.values()) {
			conn.startAutoConnect();
		}
		*/
	}

	public void sendTooShortFullDiscoveryMessage(long discoveryTimeMs) {
		String deviceInfo = Build.MANUFACTURER + "/" + Build.MODEL + "/" + android.os.Build.VERSION.RELEASE;
		NotificationMessage msgNotFound = new NotificationMessage(NotificationType.SYSTEM_TOO_SHORT_FULL_DISCOVERY, DeviceType.DIAPER_SENSOR, 0, "aid: " + mPrefManager.getAccountId() + "/scan: " + discoveryTimeMs + "/device: " + deviceInfo, System.currentTimeMillis());
		ServerQueryManager.getInstance(mContext).setNotificationFeedback(msgNotFound, null);
	}

	public void sendLeDiscoveryFailed() {
		String deviceInfo = Build.MANUFACTURER + "/" + Build.MODEL + "/" + android.os.Build.VERSION.RELEASE;
		NotificationMessage msgNotFound = new NotificationMessage(NotificationType.SYSTEM_LE_SCAN_FAILED, DeviceType.DIAPER_SENSOR, 0, "aid: " + mPrefManager.getAccountId() + "/device: " + deviceInfo, System.currentTimeMillis());
		ServerQueryManager.getInstance(mContext).setNotificationFeedback(msgNotFound, null);
	}

	public void sendDeviceNotFound(int deviceType) {
		String deviceInfo = Build.MANUFACTURER + "/" + Build.MODEL + "/" + android.os.Build.VERSION.RELEASE;
		NotificationMessage msgNotFound = new NotificationMessage(NotificationType.SYSTEM_DEVICE_NOT_FOUND, deviceType, 0, "aid: " + mPrefManager.getAccountId() + "/device: " + deviceInfo, System.currentTimeMillis());
		ServerQueryManager.getInstance(mContext).setNotificationFeedback(msgNotFound, null);
	}

	public void sendFwUpdateFailed(int deviceType, long deviceId, String errorCode) {
		String deviceInfo = Build.MANUFACTURER + "/" + Build.MODEL + "/" + android.os.Build.VERSION.RELEASE;
		NotificationMessage msgNotFound = new NotificationMessage(NotificationType.SYSTEM_FW_UPDATE_FAILED, deviceType, 0, "aid: " + mPrefManager.getAccountId() + "/dtype: " + deviceType + "/did: " + deviceId + "/err: " + errorCode + "/device: " + deviceInfo, System.currentTimeMillis());
		ServerQueryManager.getInstance(mContext).setNotificationFeedback(msgNotFound, null);
	}

	class AutoReconnectManager {
		public boolean isScanning = false;
		private ArrayList<String> mScannedDeviceMacAddr = new ArrayList<>();
		private long scanStartTimeMs = 0;
		private long scanningTimeMs = 0;
		private int cntNoScanResult = 0;
		private static final int FORCE_DISCOVERY_SCAN_FOR_NO_SCAN_RESULT_COUNT = 5;

		public AutoReconnectManager() {

		}

		public void startLeScan() {
			mScannedDeviceMacAddr.clear();
			if (cntNoScanResult < FORCE_DISCOVERY_SCAN_FOR_NO_SCAN_RESULT_COUNT) {
				_startLeScan();
			} else {
				if (!mBluetoothAdapter.isDiscovering()) {
					if (DBG) Log.d(TAG, "startDiscovery(LeScan)");
					cntNoScanResult = 0;
					isFullDiscoveryForAutoReconnect = true;
					mBluetoothAdapter.startDiscovery();
					mHandler.sendEmptyMessageDelayed(MSG_STOP_SCAN_FOR_RECONNECT, TIME_BLE_BACKGROUND_SCAN_TIME_OUT_SEC_FOR_BACKGROUND_APP * 1000);
				} else {
					if (DBG) Log.d(TAG, "isDiscovering");
				}
			}
		}

		public void setDiscoveryScan() {
			// 주기적인 startLeScan에 의해서 Discovery동작
			cntNoScanResult = FORCE_DISCOVERY_SCAN_FOR_NO_SCAN_RESULT_COUNT;
		}

		private void _startLeScan() {
			try {
				if (DBG) Log.d(TAG, "startLeScan");
				if (isScanning) {
					isScanning = false;
					if (mBluetoothLeScanner == null) {
						mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
					}
					mBluetoothLeScanner.stopScan(mScanCallback);
				}

				//if (DBG) Log.d(TAG, "startLeScan");
				//mDebugMgr.saveDebuggingLog("AutoReconnect startLeScan");
				scanStartTimeMs = System.currentTimeMillis();

				if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
					isScanning = true;
					//mBluetoothAdapter.startLeScan(mLeScanCallback);
					if (mBluetoothLeScanner == null) {
						mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
					}
					mBluetoothLeScanner.startScan(mScanCallback);
				}
			} catch (Exception e) {
				if (DBG) Log.e(TAG, "startLeScan Exception occurred: " + e.toString());
			}
			// Background 일때는 3초간 스캔, Foreground 일때는 1초간 스캔
			if (MonitApplication.isBackground) {
				mHandler.sendEmptyMessageDelayed(MSG_STOP_SCAN_FOR_RECONNECT, TIME_BLE_BACKGROUND_SCAN_TIME_OUT_SEC_FOR_BACKGROUND_APP * 1000);
			} else {
				mHandler.sendEmptyMessageDelayed(MSG_STOP_SCAN_FOR_RECONNECT, TIME_BLE_BACKGROUND_SCAN_TIME_OUT_SEC_FOR_FOREGROUND_APP * 1000);
			}
		}

		public void stopLeScan() {
			_stopLeScan();
		}

		private void _stopLeScan() {
			if (isScanning) {
				isScanning = false;
				if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
					try {
						//mBluetoothAdapter.stopLeScan(mLeScanCallback);

						if (mBluetoothLeScanner == null) {
							mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
						}

						mBluetoothLeScanner.stopScan(mScanCallback);
					} catch(Exception e) {

					}
				}
				scanningTimeMs = System.currentTimeMillis() - scanStartTimeMs;
				if (mScannedDeviceMacAddr.size() == 0) {
					cntNoScanResult++;
				}
				if (DBG) Log.d(TAG, "auto stopLeScan " + scanningTimeMs + ", " + mScannedDeviceMacAddr.size() + ", " + cntNoScanResult);

				if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("stop scan " + scanningTimeMs + " / " + mScannedDeviceMacAddr.size());
				checkDeviceList();
			}
		}

		public void checkDeviceList() {
			for (String addr : mScannedDeviceMacAddr) {
				if (addr == null) continue;
                if (DBG) Log.d(TAG, "Scanned addr: " + addr);

				for (DeviceBLEConnection conn : mRegisteredBleDeviceList.values()) {
					if (conn == null) continue;

                    //if (DBG) Log.d(TAG, "  Compare addr: " + conn.getDeviceInfo().btmacAddress);
					if (addr.equals(conn.getDeviceInfo().btmacAddress)) {
						if (DBG) Log.d(TAG, "Matched : " + conn.getDeviceInfo().btmacAddress);
						if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("matched");
						conn.isLeScanFound = true;
						conn.directConnect();
						//conn.startAutoConnect();
						break;
					}
				}
			}
		}

		public void addScannedDevice(final BluetoothDevice device) {
			if (!mScannedDeviceMacAddr.contains(device.getAddress())) {
				//if (DBG) Log.d(TAG, "Scanned: " + device.getName() + " / " + device.getAddress());
				mScannedDeviceMacAddr.add(device.getAddress());
			}
		}

		private BluetoothAdapter.LeScanCallback mLeScanCallback =
				new BluetoothAdapter.LeScanCallback() {
					@Override
					public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
						addScannedDevice(device);
					}
				};

		private ScanCallback mScanCallback = new ScanCallback() {
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				//if (DBG) Log.d(TAG, "onScanResult: " + result.getDevice().getName());
				_processResult(result);
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				//if (DBG) Log.d(TAG, "onBatchScanResults: " + results.size());
				for (ScanResult result : results) {
					_processResult(result);
				}
			}

			@Override
			public void onScanFailed(int errorCode) {
				//if (DBG) Log.d(TAG, "onScanFailed: " + errorCode);
			}

			private void _processResult(final ScanResult result) {
				final BluetoothDevice device = result.getDevice();
				addScannedDevice(device);
			}
		};
	}
}