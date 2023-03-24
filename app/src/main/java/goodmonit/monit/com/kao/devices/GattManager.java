package goodmonit.monit.com.kao.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.BleErrorCode;
import goodmonit.monit.com.kao.constants.BlePacketType;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class GattManager {
	private static final String TAG = Configuration.BASE_TAG + "GattMgr";
	private static final boolean DBG = Configuration.DBG;

	private static final int BASE									= R.string.app_name + 10000;
	public static final int MSG_GATT_BLE_CONNECTION_STATE_CHANGE	= BASE + 1;
	public static final int MSG_GATT_BLE_RECEIVED_DATA		 		= BASE + 2;
	public static final int MSG_GATT_BLE_WRITE_DATA		 			= BASE + 3;
	public static final int MSG_GATT_BLE_ACK_TIME_OUT				= BASE + 4;

	public static final int MSG_GATT_BLE_CONNECTION_ERROR			= BASE + 10; // Gatt Connect 시도 후 Service Discover 까지 30초

	private static final long TIME_OUT_GATT_BLE_CONNECTION_SEC 	= 20;
	private static final long TIME_OUT_GATT_BLE_ACK_WAIT_SEC 	= 3;

	private static final String RX_SERVICE_UUID = "20c10001-71bd-11e7-8cf7-a6006ad3dba0";
	private static final String RX_CHAR_UUID = "20c10002-71bd-11e7-8cf7-a6006ad3dba0";
	private static final String TX_CHAR_UUID = "20c10003-71bd-11e7-8cf7-a6006ad3dba0";
	private static final String NOTIFICATION_DESCRIPTION_UUID = "00002902-0000-1000-8000-00805f9b34fb";

	private BluetoothGatt mGatt;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothGattCharacteristic mWritableChar;
	private BluetoothGattCharacteristic mNotifyChar;
	private DeviceInfo mBluetoothDeviceInfo;

	private Context mContext;
	private Handler mUpperLayerHandler;
	private int mLastDisconnectedReason;
	private boolean isConnected;
	private int mCurrBleInitStep;

	// Write, Read Queue
	private ArrayList<byte[]> mWriteByteQueue;
	private ArrayList<byte[]> mReadByteQueue;

	private static final boolean sendNextPacketAfterCheckingAck = true;
	private byte mAckMsgId = 0x00;
	int mWriteTryCount = 0; // try MAX 3 times
	int mNAKCount = 0; // try MAX 3 times

	private long mLastBlePacketReceivedTimeMs = 0;

	// 연결끊김을 위한 View객체 컨트롤
	private DeviceDiaperSensor mDiaperSensorView = null;

	/**
	 * Gatt constructor
	 * Gatt연결, 연결해제, 데이터 송수신을 담당하며 상위 레이어 Handler로 해당 내용 전송
	 * @param context
	 * @param upperLayerHandler
	 */
	public GattManager(Context context, DeviceInfo deviceInfo, Handler upperLayerHandler) {
		mContext = context;
		mUpperLayerHandler = upperLayerHandler;
		mBluetoothDeviceInfo = deviceInfo;
		mWriteByteQueue = new ArrayList<>();
		mReadByteQueue = new ArrayList<>();
	}

	/**
	 * setBluetoothDevice
	 * BluetoothDevice 변경시 호출
	 * @param bluetoothDevice
	 */
	public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		mBluetoothDevice = bluetoothDevice;
	}

	/**
	 * isGattConnected
	 * Gatt의 물리적인 연결상태 확인, 연결뿐만아니라 Discover, 데이터 수신 까지 완료되면 물리적인 연결
	 * @return isConnected
	 */
	public boolean isGattConnected() {
		return isConnected;
	}

	/**
	 * getLastDisconnectedReason
	 * BluetoothDevice 변경시 호출
	 * @return mLastDisconnectedReason
	 */
	public int getLastDisconnectedReason() {
		return mLastDisconnectedReason;
	}

	private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mLastDisconnectedReason = -1;
				mCurrBleInitStep = 0;
				if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " Connected to GATT server.");
				gatt.discoverServices();

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				isConnected = false;
				mLastDisconnectedReason = status;
				mCurrBleInitStep = 0;
				if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " Disconnected from GATT server.(" + mLastDisconnectedReason + ", " + mCurrBleInitStep + ")");
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_GATT_BLE_CONNECTION_STATE_CHANGE, DeviceConnectionState.DISCONNECTED, mLastDisconnectedReason).sendToTarget();
				}
				_refreshDeviceCache();
			}
		}

		private void _refreshDeviceCache() {
			// from http://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android
			try {
				BluetoothGatt localBluetoothGatt = mGatt;
				Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
				if (localMethod != null) {
					boolean bool = (Boolean) localMethod.invoke(localBluetoothGatt, new Object[0]);
					if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " Gatt cache refresh successful: [" + bool + "]");
				}
			}
			catch (Exception localException) {
				if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " An exception occured while refreshing device : " + localException);
			}
			return;
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " onServicesDiscovered : " + status);
			mCurrBleInitStep = 2;
			if (status == BluetoothGatt.GATT_SUCCESS) {
				_checkGattServices(gatt, gatt.getServices());

				if (mWritableChar != null && mNotifyChar != null) {
					// Write / Notify 할 준비가 되면 Connected 로 수정
					mHandler.removeMessages(MSG_GATT_BLE_CONNECTION_ERROR);
					mCurrBleInitStep = 3;

					_initWrite();
					isConnected = true;
					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_GATT_BLE_CONNECTION_STATE_CHANGE, DeviceConnectionState.BLE_CONNECTED, -1).sendToTarget();
					}
				} else {
					if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " onServicesDiscovered FAILED");
				}
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			byte[] data = characteristic.getValue();
			if (DBG) {
				String log = "";
				for (byte b : data) {
					int bb = 0xFF & b;
					log += String.format(" %02X", bb);
				}
				Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " onCharacteristicChanged: " + log + "(" +  data.length + ")");
			}

			if (isConnected == false) {
				isConnected = true;
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_GATT_BLE_CONNECTION_STATE_CHANGE, DeviceConnectionState.BLE_CONNECTED, -1).sendToTarget();
				}
			}

			// 연결 끊김이슈 해결(View단에서 TRUE로 수정)
			if (mDiaperSensorView == null) {
				mDiaperSensorView = ConnectionManager.getDeviceDiaperSensor(mBluetoothDeviceInfo.deviceId);
			}
			if (mDiaperSensorView != null) {
				mDiaperSensorView.setConnectionState(DeviceConnectionState.BLE_CONNECTED);
			}

			mCurrBleInitStep = 4;
			mLastBlePacketReceivedTimeMs = System.currentTimeMillis();

			// 큐를 사용하지 않고 바로 처리하면 이전 데이터가 처리되면서 다음 데이터가 들어와서 이전 데이터가 처리되다가 중단되고 다음 데이터를 처리하는 상황 발생됨(말이 안되는데 실제 발생)
			mReadByteQueue.add(data);

			// 큐에 넣자마자 ReceivedData 데이터 전송
			mHandler.sendEmptyMessage(MSG_GATT_BLE_RECEIVED_DATA);
		}
	};

	private void _checkGattServices(BluetoothGatt gatt, List<BluetoothGattService> gattServices) {
		mWritableChar = null;
		mNotifyChar = null;
		for (BluetoothGattService gattService : gattServices) {
			if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " # GATT Service : " + gattService.getUuid().toString() + " / " + gattService.getType() + " / " + gattService.getInstanceId());

			for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
				if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " ## GATT Char : " + gattCharacteristic.getUuid() + " / " + gattCharacteristic.getValue() + " / " + gattCharacteristic.getInstanceId());
				for (BluetoothGattDescriptor gattDescriptor : gattCharacteristic.getDescriptors()) {
					if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " ### GATT Descriptor : " + gattDescriptor.getUuid() + " / " + gattDescriptor.getValue());
				}

				if (_isWritableCharacteristic(gattCharacteristic)) {
					mWritableChar = gattCharacteristic;

				} else if (_isNotificationCharacteristic(gattCharacteristic)) {
					mNotifyChar = gattCharacteristic;
					_setCharacteristicNotification(gatt, gattCharacteristic, true);
				}
			}
		}
	}

	private boolean _isWritableCharacteristic(BluetoothGattCharacteristic characteristic) {
		if(characteristic == null) return false;

		int prop = characteristic.getProperties();

		if((prop & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 || (prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
			if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " # Found writable characteristic");
			return true;
		} else {
			return false;
		}
	}

	private boolean _isNotificationCharacteristic(BluetoothGattCharacteristic characteristic) {
		if(characteristic == null) return false;

		int prop = characteristic.getProperties();

		if((prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
			if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " # Found notification characteristic");
			return true;
		} else {
			return false;
		}
	}

	private void _setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (gatt == null) return;

		if (UUID.fromString(TX_CHAR_UUID).equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(NOTIFICATION_DESCRIPTION_UUID));
			if (enabled) {
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			} else {
				descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			}
			gatt.writeDescriptor(descriptor);
			gatt.setCharacteristicNotification(characteristic, enabled);
		}
	}

	public void disconnect() {
		if (mGatt != null) {
			mGatt.disconnect();
		} else {
			if (DBG) Log.e(TAG, "Gatt NULL");
		}
	}

	public void close() {
		if (mGatt != null) {
			mGatt.disconnect();
			mGatt.close();
			mGatt = null;
		} else {
			if (DBG) Log.e(TAG, "Gatt NULL");
		}
	}

	public String getValidBluetoothDeviceMacAddress(String macAddr) {
		if (macAddr == null) return null;

		if (macAddr.length() < 17) { // 잘못 저장된 MAC Address복구
			String[] btmacToken = macAddr.split(":");
			String newBtMac = "";
			for (String str : btmacToken) {
				if (str.length() == 1) {
					str = "0" + str;
				}
				newBtMac += ":" + str;
			}
			macAddr = newBtMac.substring(1);
		}

		return macAddr;
	}

	public void checkBleConnection(int diffSec) {
		long now = System.currentTimeMillis();
		long diffMs = now - mLastBlePacketReceivedTimeMs;
		if (isConnected) {
			if (mLastBlePacketReceivedTimeMs > 0 && diffMs > diffSec * 1000) {
				if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + " checkBLEConnection No packet from GATT, Disconnect it, now:" + now + " / last:" + mLastBlePacketReceivedTimeMs);
				mLastBlePacketReceivedTimeMs = 999;
				close();
				isConnected = false;
				mCurrBleInitStep = 0;
				if (mUpperLayerHandler != null) {
					mUpperLayerHandler.obtainMessage(MSG_GATT_BLE_CONNECTION_STATE_CHANGE, DeviceConnectionState.DISCONNECTED, mLastDisconnectedReason).sendToTarget();
				}
			} else {
				if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + " checkBLEConnection latest packet : " + (now - mLastBlePacketReceivedTimeMs) / 1000);
			}
		} else {
			if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + " checkBLEConnection but disconnected");
		}
	}

	public void connectManually() {
		if (mGatt != null) { // Gatt가 이미 한번이라도 연결 되었으면, 자동연결할 것이므로 필요없음
			if (DBG) Log.e(TAG, "WAIT FOR AUTO CONNECT");
			return;
		}

		if (mBluetoothDevice == null) {
			if (mBluetoothDeviceInfo.btmacAddress == null) {
				_sendErrorMessage(BleErrorCode.BLE_CONNECT_ERR1);
				return;
			} else {
				String validMacAddr = getValidBluetoothDeviceMacAddress(mBluetoothDeviceInfo.btmacAddress);
				if (validMacAddr != null && !validMacAddr.equals(mBluetoothDeviceInfo.btmacAddress)) {
					mBluetoothDeviceInfo.btmacAddress = validMacAddr;
					if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " restore wrong bt mac : " + validMacAddr);
					mBluetoothDeviceInfo.updateDB(mContext);
				}
				mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mBluetoothDeviceInfo.btmacAddress);
			}

			if (mBluetoothDevice == null) {
				_sendErrorMessage(BleErrorCode.BLE_CONNECT_ERR2);
				return;
			}
		}

		if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " try connect");

		mCurrBleInitStep = 1;
		mGatt = mBluetoothDevice.connectGatt(mContext, true, gattCallback);
		if (mGatt == null) {
			if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " GATT NULL");
			_sendErrorMessage(BleErrorCode.BLE_CONNECT_ERR3);
			return;
		}

		Message message = new Message();
		message.what = MSG_GATT_BLE_CONNECTION_ERROR;
		message.obj = mBluetoothDeviceInfo;
		mHandler.sendMessageDelayed(message, TIME_OUT_GATT_BLE_CONNECTION_SEC * 1000);
	}

	private void _sendErrorMessage(int code) {
		if (mUpperLayerHandler != null) {
			Message msg = mUpperLayerHandler.obtainMessage(ConnectionManager.MSG_CONNECTION_ERROR);
			msg.arg1 = code;
			msg.obj = mBluetoothDeviceInfo;
			mUpperLayerHandler.sendMessage(msg);
		} else {
			if (DBG) Log.e(TAG, "sendErrorMessage connectionManagerHandler NULL");
		}
	}

	public void write(byte[] data) {
		if (!isConnected) {
			if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " write failed NOT CONNECTED");
			return;
		}

		if (data == null) return;
		byte[] logData;
		if (data.length > 20) {
			byte[] chunkedData = new byte[20];
			for (int i = 0; i < data.length; i++) {
				if (i % 20 == 0) {
					if (i > 0) {
						mWriteByteQueue.add(chunkedData);
					}
					chunkedData = new byte[20];
				}
				chunkedData[i % 20] = data[i];
			}
			logData = chunkedData;
			mWriteByteQueue.add(chunkedData); // Add Last chunkedData
		} else {
			logData = data;
			mWriteByteQueue.add(data);
		}

		String log = "";
		if (DBG) {
			for (byte b : logData) {
				int bb = 0xFF & b;
				log += String.format(" %02X", bb);
			}
		}

		if (mHandler != null) {
			if (sendNextPacketAfterCheckingAck) {
				if ((!mHandler.hasMessages(MSG_GATT_BLE_WRITE_DATA) || mWriteByteQueue.size() == 1) && (mAckMsgId == 0x00)) { // 최초 패킷 전송시
					mAckMsgId = BlePacketType.ETC;
					if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " write directly: " + log + " / " + isConnected);
					mHandler.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 50);
				} else {
					if (DBG) Log.d(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " write wait: " + log + " for " + String.format("%02X", mAckMsgId) + " / " + mHandler.hasMessages(MSG_GATT_BLE_WRITE_DATA) + " / " + mWriteByteQueue.size());
				}
			} else {
				mHandler.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 50);
			}
		}
	}

	private int _sendToDevice(byte[] data) {
		if (mGatt == null) {
			_sendErrorMessage(BleErrorCode.BLE_WRITE_ERR1);
			return BleErrorCode.BLE_WRITE_ERR1;
		}

		if (mWritableChar == null) {
			_sendErrorMessage(BleErrorCode.BLE_WRITE_ERR2);
			return BleErrorCode.BLE_WRITE_ERR2;
		}

		mWritableChar.setValue(data);

		boolean writeSucceeded = mGatt.writeCharacteristic(mWritableChar);

		String log = "";
		if (DBG) {
			for (byte b : data) {
				int bb = 0xFF & b;
				log += String.format(" %02X", bb);
			}
		}
		if (writeSucceeded) {
			if (DBG) Log.d(TAG, "write byte succeeded : " + log);
		} else {
			if (DBG) Log.e(TAG, "write byte failed : " + log);
			_sendErrorMessage(BleErrorCode.BLE_WRITE_ERR3);
			return BleErrorCode.BLE_WRITE_ERR3;
		}
		return 0;
	}

	private void _initWrite() {
		if (mHandler != null) {
			mHandler.removeMessages(MSG_GATT_BLE_WRITE_DATA);
		}
		mAckMsgId = 0x00;
		mNAKCount = 0;
		mWriteTryCount = 0;
		mWriteByteQueue.clear();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//if (DBG) Log.d(TAG, "handleMessage : " + msg.what);
			switch (msg.what) {
				case MSG_GATT_BLE_CONNECTION_ERROR:
					if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " MSG_GATT_BLE_CONNECTION_ERROR");
					close();

					if (mUpperLayerHandler != null) {
						mUpperLayerHandler.obtainMessage(MSG_GATT_BLE_CONNECTION_ERROR, mCurrBleInitStep, -1, mBluetoothDeviceInfo).sendToTarget();
					}
					break;

				case MSG_GATT_BLE_RECEIVED_DATA:
					removeMessages(MSG_GATT_BLE_RECEIVED_DATA);
					if (mReadByteQueue.size() > 0) {
						byte[] data = mReadByteQueue.get(0);
						mReadByteQueue.remove(0);
						if (data == null) break;

						if (sendNextPacketAfterCheckingAck) {
							byte type = data[0];
							// ACK/NAK에 대한 처리
							if ((mAckMsgId == type) // Req패킷에 대한 응답을 받았을 때, AckMsgId는 Req MsgId인 81이 아닌 Req에 실린 값으로 설정됨
									|| (BlePacketType.ACK == type)) { // Cmd패킷에 대한 응답은 ACK로 받게 됨

								// 1. ACK Timeout 제거
								mHandler.removeMessages(MSG_GATT_BLE_ACK_TIME_OUT);
								byte ackType = 0;        // 초기값 ACK로 세팅
								byte ackMsgId = type;    // 현재 받은 패킷으로 설정
								if (BlePacketType.ACK == type) {
									ackType = data[1];
									ackMsgId = data[2];
								}

								if (ackType == 0) { // ACK를 받았으면
									if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + "ACK " + String.format("%02X->%02X", (0xFF & mAckMsgId), (0xFF & ackMsgId)));
									if (mAckMsgId == ackMsgId) { // 가장 최근에 보낸 패킷에 대한 ACK인지 확인한 후
										// 관련변수 초기화
										mAckMsgId = 0x00;
										mNAKCount = 0;
										mWriteTryCount = 0;

										// 응답을 기다리던 패킷 삭제
										if (mWriteByteQueue.size() > 0) {
											mWriteByteQueue.remove(0);
											if (DBG) Log.d(TAG, "writeQueueSize1: " + mWriteByteQueue.size());
										}
										mHandler.sendEmptyMessage(MSG_GATT_BLE_WRITE_DATA);
									}
								} else if (ackType == 1) { // NACK를 받았으면
									if (DBG) Log.i(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + "NAK " + String.format("%02X->%02X", (0xFF & mAckMsgId), (0xFF & ackMsgId)));
									// If get NAK msg id related to latest write, send again
									if (mAckMsgId == ackMsgId) { // 가장 최근에 보낸 패킷에 대한 NACK인지 확인한 후
										mNAKCount++;
										if (mNAKCount > 3) { // 3회 이상이면 해당 패킷은 삭제
											if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + "NAK more than 3 times");
											// 관련변수 초기화
											mAckMsgId = 0x00;
											mNAKCount = 0;
											mWriteTryCount = 0;

											// 응답을 기다리던 패킷 삭제
											if (mWriteByteQueue.size() > 0) {
												mWriteByteQueue.remove(0);
												if (DBG) Log.d(TAG, "writeQueueSize2: " + mWriteByteQueue.size());
											}
										}
										mHandler.sendEmptyMessage(MSG_GATT_BLE_WRITE_DATA);
									}
								}
							}
						}

						if (mUpperLayerHandler != null) {
							mUpperLayerHandler.obtainMessage(MSG_GATT_BLE_RECEIVED_DATA, data).sendToTarget();
						}
					}

					if (mReadByteQueue.size() == 0) {
						// 큐에 remove함과 거의 동시에 add하는 상황 발생 가능
						// 이때는, 10초 뒤에 다시 데이터 확인하라고 DelayMessage 전송
						this.sendEmptyMessageDelayed(MSG_GATT_BLE_RECEIVED_DATA, 10 * 1000);
					} else {
						this.sendEmptyMessage(MSG_GATT_BLE_RECEIVED_DATA);
					}
					break;

				case MSG_GATT_BLE_WRITE_DATA:
					removeMessages(MSG_GATT_BLE_WRITE_DATA);
					if (mGatt == null) break;

					if (mWriteByteQueue.size() == 0) {
						if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + mBluetoothDeviceInfo.name + " / " + mBluetoothDeviceInfo.btmacAddress + " send MSG_WRITE_TO_DEVICE delay");
						this.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 10 * 1000); // 큐가 비었으면, 10초에 한번씩 확인할 것
						break;
					}

					byte[] sendBytes = mWriteByteQueue.get(0);
					if (sendBytes == null) { // Queue가 삭제되면서 바로 Add될 수 있으므로 null이 있을 수도 있음
						mWriteByteQueue.remove(0);
						if (DBG) Log.d(TAG, "writeQueueSize3: " + mWriteByteQueue.size());
						this.sendEmptyMessage(MSG_GATT_BLE_WRITE_DATA);
						break;
					}

					if (sendNextPacketAfterCheckingAck) {
						if (sendBytes[0] == BlePacketType.REQUEST) { // Request doesn't need ACK Packet
							mAckMsgId = sendBytes[4];
						} else {
							mAckMsgId = sendBytes[0];
						}
						if (DBG) Log.i(TAG, "Set ACK before send : " + String.format("%02X", mAckMsgId));
						if (_sendToDevice(sendBytes) == 0) {
							// 패킷 보내는게 성공되면, ACK 받기까지 3초간 기다림
							this.sendEmptyMessageDelayed(MSG_GATT_BLE_ACK_TIME_OUT, TIME_OUT_GATT_BLE_ACK_WAIT_SEC * 1000);
							break;
						} else {
							// 패킷 보내는게 실패하면, 아래로 떨어짐(ACK Timeout과 동일처리)
						}
					} else {
						// ACK 확인할 필요가 없으므로 곧바로 데이터 전송
						if (_sendToDevice(sendBytes) == 0) {
							this.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 50);
						} else {
							this.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 500);
						}
						break;
					}
					// Through
					// 패킷 보내는게 실패하거나, ACK TIME-OUT나는 경우
				case MSG_GATT_BLE_ACK_TIME_OUT:
					removeMessages(MSG_GATT_BLE_ACK_TIME_OUT);
					mWriteTryCount++;
					if (msg.what == MSG_GATT_BLE_ACK_TIME_OUT) {
						if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + "ACK Timeout , resend : " + String.format("%02X", mAckMsgId) + "(" + mWriteTryCount + ")");
					} else { // 패킷보내는게 실패한 경우
						if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + "Write failed, resend : " + String.format("%02X", mAckMsgId) + "(" + mWriteTryCount + ")");
					}

					if (mWriteTryCount >= 3) { // 3번 이후로는 다음 패킷 보내기
						if (DBG) Log.e(TAG, "[" + mBluetoothDeviceInfo.deviceId + "] " + "4 times failed");
						if (mWriteByteQueue.size() > 0) {
							mWriteByteQueue.remove(0);
							if (DBG) Log.d(TAG, "writeQueueSize4: " + mWriteByteQueue.size());
						}
						mWriteTryCount = 0;
						mNAKCount = 0;
						mAckMsgId = 0x00;
						close();
						break;
					}

					if (msg.what == MSG_GATT_BLE_ACK_TIME_OUT) {
						this.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 50);
					} else { // 패킷보내는게 실패한 경우
						this.sendEmptyMessageDelayed(MSG_GATT_BLE_WRITE_DATA, 500);
					}
					break;

			}
		}
	};
}