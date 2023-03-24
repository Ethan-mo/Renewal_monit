package goodmonit.monit.com.kao.devices;

import android.text.format.DateFormat;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import goodmonit.monit.com.kao.constants.BlePacketType;
import goodmonit.monit.com.kao.constants.Configuration;

public class BlePacketManager {
	private static final String TAG = Configuration.BASE_TAG + "BlePacket";
	private static final boolean DBG = Configuration.DBG;

	private static BlePacketManager mBlePacketMgr;
	public static final int MAX_BYTE_LENGTH_IN_A_PACKET = 20;
	public static final int MAX_BYTE_LENGTH_NAME = 24;
	public static final int MAX_BYTE_LENGTH_AP_NAME = 32;
	public static final int MAX_BYTE_LENGTH_AP_PASSWORD = 64;

	public static BlePacketManager getInstance() {
		if (mBlePacketMgr == null) {
			mBlePacketMgr = new BlePacketManager();
		}

		return mBlePacketMgr;
	}

	public BlePacketManager() {
		// empty
	}

	public byte[] getRequestPacket(int packetType[]) {
		byte[] pkt = new byte[packetType.length + 4];
		int cnt = 0;
		pkt[cnt++] = BlePacketType.REQUEST;
		pkt[cnt++] = (byte)0;
		pkt[cnt++] = (byte)0;
		pkt[cnt++] = (byte)0;

		for (int type : packetType) {
			pkt[cnt++] = (byte)type;
		}
		return pkt;
	}

	public byte[] getAutoPollingPacket(boolean enable, int packetType[]) {
		byte[] pkt = new byte[packetType.length + 4];
		int cnt = 0;
		pkt[cnt++] = BlePacketType.AUTO_POLLING;
		pkt[cnt++] = (byte)1;
		pkt[cnt++] = (byte)1;
		if (enable) {
			pkt[cnt++] = (byte)1;
		} else {
			pkt[cnt++] = (byte)0;
		}

		for (int type : packetType) {
			pkt[cnt++] = (byte)type;
		}
		return pkt;
	}

	public byte[] getCommandPacket(byte type) {
		return getCommandPacket(type, 0);
	}

	public byte[] getCommandPacket(byte type, long data) {
		byte[] pkts;
		switch(type) {
			case BlePacketType.DATE_INFO:
				String yymmddString = DateFormat.format("yyMMdd", data).toString();
				int year = Integer.parseInt(yymmddString.substring(0, 2));
				int month = Integer.parseInt(yymmddString.substring(2, 4));
				int day = Integer.parseInt(yymmddString.substring(4, 6));
				pkts = new byte[4];
				pkts[0] = BlePacketType.DATE_INFO;
				pkts[1] = (byte)year;
				pkts[2] = (byte)month;
				pkts[3] = (byte)day;
				break;

			case BlePacketType.UTC_TIME_INFO:
				int utcTimeSec = (int)data;
				pkts = new byte[8];
				pkts[0] = BlePacketType.UTC_TIME_INFO;
				pkts[1] = (byte)0x01;
				pkts[2] = (byte)0x01;
				pkts[3] = (byte)0x00;
				pkts[4] = (byte)(utcTimeSec);
				pkts[5] = (byte)(utcTimeSec >> 8);
				pkts[6] = (byte)(utcTimeSec >> 16);
				pkts[7] = (byte)(utcTimeSec >> 24);
				break;
			default:
				pkts = new byte[4];
				break;
		}
		return pkts;
	}

	public byte[] getCommandPacket(byte type, String data) {
		byte[] pkts;
		switch(type) {
			case BlePacketType.BABY_INFO:
				long longData = 0;
				try {
					longData = Long.parseLong(data);
				} catch (Exception e) {
					longData = 0;
				}
				pkts = makePacketFor3ByteValue(BlePacketType.BABY_INFO, longData);
				break;
			case BlePacketType.SERIAL_NUMBER:
				byte[] byteSerialData = null;
				if (data == null || data.length() == 0) {
					data = "";
				}
				try {
					byteSerialData = data.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					byteSerialData = data.getBytes();
				}

				pkts = new byte[byteSerialData.length + 5];
				pkts[0] = type;
				pkts[1] = (byte)0x01;
				pkts[2] = (byte)0x01;
				pkts[3] = (byte)0x00;
				for (int i = 0; i < byteSerialData.length; i++) {
					pkts[4 + i] = byteSerialData[i];
				}
				pkts[byteSerialData.length + 4] = (byte)0x00;
				break;
			case BlePacketType.DEVICE_NAME:
			case BlePacketType.HUB_AP_NAME:
			case BlePacketType.HUB_AP_PASSWORD:
			case BlePacketType.DEBUG_COMMAND:
				byte[] byteData = null;
				if (data == null || data.length() == 0) {
					data = "";
				}
				try {
					byteData = data.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					byteData = data.getBytes();
				}

				if (byteData.length <= 16) {
					pkts = new byte[byteData.length + 4];
					pkts[0] = type;
					pkts[1] = (byte)0x01;
					pkts[2] = (byte)0x01;
					pkts[3] = (byte)0x00;
					for (int i = 0; i < byteData.length; i++) {
						pkts[4+i] = byteData[i];
					}
				} else {
					int idx = 0;
					int pktTotal = (int)Math.ceil(byteData.length / 16.0);
					pkts = new byte[byteData.length + 4 * pktTotal];
					for (int i = 0; i < pktTotal; i++) {
						pkts[i * MAX_BYTE_LENGTH_IN_A_PACKET] = type;
						pkts[i * MAX_BYTE_LENGTH_IN_A_PACKET + 1] = (byte)pktTotal;
						pkts[i * MAX_BYTE_LENGTH_IN_A_PACKET + 2] = (byte)(i + 1);
						pkts[i * MAX_BYTE_LENGTH_IN_A_PACKET + 3] = 0;

						for (int j = 0; j < 16; j++) {
							if (idx >= byteData.length) {
								break;
							}
							pkts[i * MAX_BYTE_LENGTH_IN_A_PACKET + 4 + j] = byteData[idx];
							idx++;
						}
					}
					if (DBG) Log.i(TAG, "long packets : " + idx + " / " + pktTotal + " / " + pkts.length);
				}

				String log = "";
				for (byte b : pkts) {
					int bb = 0xFF & b;
					log += bb + " ";
				}

				if (DBG) Log.d(TAG, "getCommandPackets : " + type + "(" + pkts.length + ") : " + log);
				break;
			default:
				pkts = new byte[4];
				break;
		}
		return pkts;
	}

	public byte[] getCommandPacket(byte type, int extra, int extra2) {
		byte[] pkts = new byte[4];
		switch (type) {
			case BlePacketType.HUB_AP_SECURITY:
				// Security Type * 100 + 연결할AP아이디
				pkts = new byte[4];
				pkts[0] = BlePacketType.HUB_AP_SECURITY;
				pkts[1] = (byte)(extra & 0x000000ff);
				pkts[2] = (byte)(extra2 & 0x000000ff);
				pkts[3] = (byte)0x00;
				break;
		}
		return pkts;
	}

	public byte[] getCommandPacket(byte type, int extra) {
		byte[] pkts;
		switch(type) {
			case BlePacketType.LED_CONTROL: // Green LED Blink for 5 secs
				pkts = new byte[4];
				pkts[0] = BlePacketType.LED_CONTROL;
				pkts[1] = (byte)0x01;
				pkts[2] = (byte)0x00;
				pkts[3] = (byte)0x00;
				break;
			case BlePacketType.INITIALIZE:
				pkts = new byte[4];
				pkts[0] = BlePacketType.INITIALIZE;
				pkts[1] = (byte)0x55;
				pkts[2] = (byte)0xAA;
				pkts[3] = (byte)0xFF;
				break;
			case BlePacketType.HUB_WIFI_SCAN:
				pkts = new byte[4];
				pkts[0] = BlePacketType.HUB_WIFI_SCAN;
				pkts[1] = (byte)0x00;
				pkts[2] = (byte)0x00;
				pkts[3] = (byte)0x00;
				break;
			case BlePacketType.KEEP_ALIVE:
				pkts = new byte[4];
				pkts[0] = BlePacketType.KEEP_ALIVE;
				pkts[1] = (byte)(extra & 0x000000ff);
				pkts[2] = (byte)0x00;
				pkts[3] = (byte)0x00;
				break;
			case BlePacketType.ENTER_DFU:
				pkts = new byte[4];
				pkts[0] = BlePacketType.ENTER_DFU;
				pkts[1] = (byte)0x01;
				pkts[2] = (byte)0x00;
				pkts[3] = (byte)0x00;
				break;
			case BlePacketType.SENSITIVITY:
				pkts = new byte[4];
				pkts[0] = BlePacketType.SENSITIVITY;
				pkts[1] = (byte)(extra & 0x000000ff);
				pkts[2] = (byte)0x00;
				pkts[3] = (byte)0x00;
				break;
			case BlePacketType.LAMP_BRIGHT_CTRL: // 전원 or 밝기 변경
				pkts = new byte[4];
				pkts[0] = BlePacketType.LAMP_BRIGHT_CTRL;
				if (extra == 10002) { // 전원 켜기
					pkts[1] = (byte)0xFF;
					pkts[2] = (byte)0xFF;
					pkts[3] = 1;
				} else if (extra == 10001) { // 전원 끄기
					pkts[1] = (byte)0xFF;
					pkts[2] = (byte)0xFF;
					pkts[3] = 0;
				} else { // 밝기 변경
					pkts[1] = (byte)extra;
					pkts[2] = (byte)(extra >> 8);
					pkts[3] = (byte)0xFF;
				}
				break;
			case BlePacketType.CERT:
				pkts = new byte[4];
				pkts[0] = BlePacketType.CERT;
				pkts[1] = (byte)0x55;
				pkts[2] = (byte)0xAA;
				pkts[3] = (byte)0xFF;
				break;
			case BlePacketType.RESET:
				pkts = new byte[4];
				pkts[0] = BlePacketType.RESET;
				pkts[1] = (byte)0x55;
				pkts[2] = (byte)0xAA;
				pkts[3] = (byte)0xFF;
				break;
			default:
				pkts = new byte[4];
				break;
		}
		return pkts;
	}

	public byte[] makePacketFor3ByteValue(byte type, long value) {
		byte[] packet = new byte[4];
		packet[0] = type;
		packet[1] = (byte) value;
		packet[2] = (byte) (value >> 8);
		packet[3] = (byte) (value >> 16);
		return packet;
	}

	private long _getUnsignedValue(byte[] packets) {
		return (((0xFF & packets[3]) << 16) | ((0xFF & packets[2]) << 8) | 0xFF & packets[1]);
	}

	private long _getSignedValue(byte[] packets) {
		if ((packets[3] >> 7) == 0x01) {
			return (long)((((0x7F & packets[3]) << 16) | ((0xFF & packets[2]) << 8) | 0xFF & packets[1]) * -1);
		} else {
			return (long)(((0x7F & packets[3]) << 16) | ((0xFF & packets[2]) << 8) | 0xFF & packets[1]);
		}
	}

	public boolean isLongPacket(byte[] packets) {
		return (packets[0] & 0xFF) >= 128;
	}

	public byte[] trimBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0) return null;

		int trimIdx = -1;
		for (int i = bytes.length - 1; i >= 0 ; i--) {
			if (bytes[i] != 0) {
				trimIdx = i;
				break;
			}
		}
		if (trimIdx == -1) return null;

		byte[] trimmedBytes = new byte[trimIdx + 1];
		for (int i = 0; i < trimIdx + 1; i++) {
			trimmedBytes[i] = bytes[i];
		}
		return trimmedBytes;
	}

	public long getDeviceId(byte[] packets) {
		if (packets.length < 4) return -1;
		return _getUnsignedValue(packets);
	}

	public long getCloudId(byte[] packets) {
		if (packets.length < 4) return -1;
		return _getUnsignedValue(packets);
	}

	public String getName(byte[] packets) {
		byte[] trimmedBytes = trimBytes(packets);
		if (trimmedBytes == null || trimmedBytes.length == 0) return "Monit";

		String name = "Monit";
		try {
			name = new String(trimmedBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
        }

		return name;
	}

	public String getHardwareVersion(byte[] packets) {
		if (packets.length < 4) return null;
		return (0xFF & packets[1]) + "." + (0xFF & packets[2]) + "." + (0xFF & packets[3]);
	}

	public String getFirmwareVersion(byte[] packets) {
		if (packets.length < 4) return null;
		return (0xFF & packets[1]) + "." + (0xFF & packets[2]) + "." + (0xFF & packets[3]);
	}

	public String getBabyBirthday(byte[] packets) {
		if (packets.length < 4) return null;
		long value = _getUnsignedValue(packets) / 10; // Separate birthday from sex
		String birthday = Long.toString(value);

		for (int i = 0; i < 6 - birthday.length(); i++) {
			birthday = "0" + birthday;
		}

		return birthday;
	}

	public int getBabySex(byte[] packets) {
		if (packets.length < 4) return -1;
		int sex = (int)(_getUnsignedValue(packets) % 10); // Separate birthday from sex
		return sex;
	}

	public String getSerialNumber(byte[] packets) {
		if (packets.length < 13) return null;
		StringBuilder sb = new StringBuilder();
		for (int i = 4; i < packets.length; i++) {
			if (packets[i] == 0x00) continue;
			sb.append((char)(0xFF & packets[i]));
		}
		return sb.toString();
	}

	public String getMacAddress(byte[] packets) {
		if (packets.length < 9) return null;

		String macAddress = "";
		for (int i = packets.length - 1; i >= 4; i--) {
			String byteString = Integer.toHexString(packets[i]);
			if (byteString.length() > 2) {
				byteString = byteString.substring(byteString.length() - 2);
			} else if (byteString.length() == 1) {
				byteString = "0" + byteString;
			}
			macAddress += ":" + byteString.toUpperCase();
		}
		return macAddress.substring(1);
	}

	public int[] getSensorStatus(byte[] packets) {
		if (packets.length < 4) return null;
		int[] status = new int[3];
		status[0] = (0xFF & packets[1]);
		status[1] = (0xFF & packets[2]);
		status[2] = (0xFF & packets[3]);
		return status;
	}

	public int[] getPendingDiaperInfo(byte[] packets) {
		if (packets.length < 4) return null;
		int[] pendingInfo = new int[2];
		pendingInfo[0] = (0xFF & packets[1]); // Info
		pendingInfo[1] = ((0xFF & packets[3]) << 8) | (0xFF & packets[2]);
		return pendingInfo;
	}

	public int getSensitivity(byte[] packets) {
		if (packets.length < 4) return 3;
		int sensitivity = (0xFF & packets[1]);
		return sensitivity;
	}

	public long getTouchValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets);
	}

	public float[] getMultiTouchValue(byte[] packets) {
		if (packets.length < 4) return null;
		float[] touch = new float[9];

		String strTouch = "";
		for (int i = 0; i < 9; i++) {
			touch[i] = ((0xFF & packets[(i + 1) * 2 + 1]) << 8) | 0xFF & packets[(i + 1) * 2];

			strTouch += touch[i] + ", ";
		}

		return touch;
	}

	public int getStrapBatteryValue(byte[] packets) {
		if (packets.length < 4) return 0;
		long battery = _getUnsignedValue(packets) / 100;
		return (int)battery;
	}

	public int getBatteryValue(byte[] packets) {
		if (packets.length < 4) return 0;
		long battery = _getUnsignedValue(packets) / 100;
		return (int)battery;
	}

	public int getHubApConnectionStatus(byte[] packets) {
		if (packets.length < 4) return 0;
		return (int)_getSignedValue(packets);
	}

	public HubApInfo getHubApInfo(byte[] packets) {
		HubApInfo apInfo = new HubApInfo();
		if (packets[1] == -1) {
			packets[3] = 1;
		}
		byte[] trimmedBytes = trimBytes(packets);
		if (trimmedBytes == null || trimmedBytes.length == 0) return null;

		apInfo.index = packets[1];
		apInfo.securityType = packets[2];
		apInfo.rssi = packets[3];

		byte[] name = new byte[trimmedBytes.length - 4];
		for (int i = 4; i < trimmedBytes.length; i++) {
			name[i - 4] = trimmedBytes[i];
		}

		try {
			apInfo.name = new String(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}

		return apInfo;
	}

	public float getXaxisValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getSignedValue(packets);
	}

	public float getYaxisValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getSignedValue(packets);
	}

	public float getZaxisValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getSignedValue(packets);
	}

	public int getUnsignedIntegerValue(byte[] packets) {
		if (packets.length < 4) return -1;
		return (int)_getUnsignedValue(packets);
	}

	public float getAccelerationValue(byte[] packets) {
		if (packets.length < 4) return -1;
		long accValue = _getUnsignedValue(packets);
		accValue = accValue % 10000;
		return accValue / (float)1000.0;
	}

	public float getZaxisFromAccelerationValue(byte[] packets) {
		if (packets.length < 4) return -1;
		long accValue = _getUnsignedValue(packets);
		accValue = accValue / 10000;
		accValue = accValue - 1000;
		return accValue / (float)100.0;
	}

	public float getTemperatureValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getSignedValue(packets) / (float)100.0;
	}

	public float getHumidityValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets) / (float)100.0;
	}

	public float getVocValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets) / (float)100.0;
	}

	public long getCo2Value(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets);
	}

	public long getRawGasValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets);
	}

	public long getCompensatedGasValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets);
	}

	public long getPressureValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets);
	}

	public long getEthanolValue(byte[] packets) {
		if (packets.length < 4) return 0;
		return _getUnsignedValue(packets);
	}

	public int[] getDiaperStatusCount(byte[] packets) {
		if (packets.length < 4) return null;
		int[] diaperStatusCount = new int[6];
		diaperStatusCount[0] = packets[1] & 0x0f;
		diaperStatusCount[1] = (packets[1] >> 4) & 0x0f;
		diaperStatusCount[2] = packets[2] & 0x0f;
		diaperStatusCount[3] = (packets[2] >> 4) & 0x0f;
		diaperStatusCount[4] = packets[3] & 0x0f;
		diaperStatusCount[5] = (packets[3] >> 4) & 0x0f;

		return diaperStatusCount;
	}

	public int getDiaperStatusDetectionType(byte[] packets) {
		if (packets.length < 8) return 0;
		return packets[3];
	}

	public long getDiaperStatusDetectionTime(byte[] packets) {
		if (packets.length < 8) return 0;
		long utcTimeSec = (((0xFF & packets[7]) << 24) | ((0xFF & packets[6]) << 16) | ((0xFF & packets[5]) << 8) | 0xFF & packets[4]);
		return utcTimeSec;
	}

	/**
	 * Check needed
	 */
	public void isAck(byte[] packets) {
		if (packets.length < 4) return;
		boolean isAck = packets[1] == 0;
		byte msgId = packets[2];

	}

}