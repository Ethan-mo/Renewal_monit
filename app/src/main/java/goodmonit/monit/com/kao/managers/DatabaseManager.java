package goodmonit.monit.com.kao.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.ElderlySensingData;
import goodmonit.monit.com.kao.devices.HubGraphInfo;
import goodmonit.monit.com.kao.devices.LampGraphInfo;
import goodmonit.monit.com.kao.devices.MovementGraphInfo;
import goodmonit.monit.com.kao.devices.SensingData;
import goodmonit.monit.com.kao.devices.SensorGraphInfo;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class DatabaseManager {
	private static final String TAG = Configuration.BASE_TAG + "DatabaseManager";
	private static final boolean DBG = Configuration.DBG;

	private static final String DB_NAME = "monit.db";
	private static final String TABLE_NAME_NOTIFICATION = "notification";
	private static final String TABLE_NAME_HUB_GRAPH = "hubgraph";
	private static final String TABLE_NAME_LAMP_GRAPH = "lampgraph";
	private static final String TABLE_NAME_MOVEMENT_GRAPH = "movementgraph";
	private static final String TABLE_NAME_DEVICE_ENC = "device_enc";
	private static final String TABLE_NAME_DEVICE = "device";
	private static final String TABLE_NAME_SENSOR_DATA = "sensordata2";
	private static final String TABLE_NAME_SCREEN_ANALYTICS = "screenanalytics";
	private static final String TABLE_NAME_ELDERLY_SENSOR_DATA = "elderlysensordata";

	private static final int DB_VERSION = 12;
	
	private static DatabaseManager mDatabaseManager;
	private static Context mContext;
	private static DatabaseHelper mDBHelper;
	private SQLiteDatabase mSQLiteDB;
	private em mEncryptionMgr;
	
	public static DatabaseManager getInstance(Context context) {
		if (context == null) {
			throw new InvalidParameterException("null Context");
		}
		
		if (mDatabaseManager == null) {
			mDatabaseManager = new DatabaseManager(context);
		}
		
		//if (DBG) Log.d(TAG, "getInstance : " + mDatabaseManager);
		return mDatabaseManager;
	}
	
	public DatabaseManager(Context context) {
		if (DBG) Log.d(TAG, "create DatabaseManager");
		mContext = context;
		mDBHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
		mEncryptionMgr = new em(mContext);
		mSQLiteDB = mDBHelper.getWritableDatabase();
	}

	private ContentValues _getContentValues(HubGraphInfo info) {
		if (info == null) {
			return null;
		}
		ContentValues values = new ContentValues();
		values.put("device_id", info.deviceId);
		values.put("temperature", info.temperature);
		values.put("humidity", info.humidity);
		values.put("voc", info.voc);
		values.put("score", info.score);
		values.put("created", info.timeSec);
		return values;
	}

	private ContentValues _getContentValues(LampGraphInfo info) {
		if (info == null) {
			return null;
		}
		ContentValues values = new ContentValues();
		values.put("device_id", info.deviceId);
		values.put("temperature", info.temperature);
		values.put("humidity", info.humidity);
		values.put("voc", info.voc);
		values.put("score", info.score);
		values.put("created", info.timeSec);
		return values;
	}

	private ContentValues _getContentValues(NotificationMessage msg) {
		if (msg == null) {
			return null;
		}
		ContentValues values = new ContentValues();
		values.put("noti_type", msg.notiType);
		values.put("device_type", msg.deviceType);
		values.put("device_id", msg.deviceId);
		values.put("extra", msg.extra);
		values.put("created", msg.timeMs);
		values.put("server_noti_id", msg.serverNotiId);
		return values;
	}

	private ContentValues _getContentValues(SensingData info) {
		if (info == null) {
			return null;
		}

		ContentValues values = new ContentValues();
		values.put("device_id", info.deviceId);
		values.put("temperature", info.temperature);
		values.put("humidity", info.humidity);
		values.put("voc", info.voc);
		values.put("capacitance", info.capacitance);
		values.put("acceleration", info.acceleration);
		values.put("sensorstatus", info.sensorstatus);
		values.put("movementlevel", info.movementlevel);
		values.put("ethanol", info.ethanol);
		values.put("co2", info.co2);
		values.put("pressure", info.pressure);
		values.put("compgas", info.compgas);
		values.put("timeMs", info.timeMs);
		return values;
	}

	private ContentValues _getContentValues(ElderlySensingData info) {
		if (info == null) {
			return null;
		}

		ContentValues values = new ContentValues();
		values.put("device_id", info.deviceId);
		values.put("temperature", info.temperature);
		values.put("humidity", info.humidity);
		values.put("voc", info.voc);
		values.put("capacitance", info.capacitance);
		values.put("acceleration", info.acceleration);
		values.put("sensorstatus", info.sensorstatus);
		values.put("movementlevel", info.movementlevel);
		values.put("ethanol", info.ethanol);
		values.put("co2", info.co2);
		values.put("pressure", info.pressure);
		values.put("compgas", info.compgas);
		values.put("touchch1", info.touch_ch1);
		values.put("touchch2", info.touch_ch2);
		values.put("touchch3", info.touch_ch3);
		values.put("touchch4", info.touch_ch4);
		values.put("touchch5", info.touch_ch5);
		values.put("touchch6", info.touch_ch6);
		values.put("touchch7", info.touch_ch7);
		values.put("touchch8", info.touch_ch8);
		values.put("touchch9", info.touch_ch9);
		values.put("timeMs", info.timeMs);
		return values;
	}

	//region "NotificationMessage"
	public long insertDB(NotificationMessage msg) {
		if (msg == null) {
			return -1;
		}
		if (DBG) Log.d(TAG, "insertDB : " + msg.toString());

		Cursor c = null;
		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			//c = mSQLiteDB.rawQuery("SELECT * FROM notification WHERE noti_type=" + msg.notiType + " AND device_type=" + msg.deviceType + " AND device_id=" + msg.deviceId + " AND created=" + msg.timeMs, null);
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "noti_type=? AND  device_type=? AND device_id=? AND created=?";
			String[] selectionArgs = {msg.notiType + "", msg.deviceType + "", msg.deviceId + "", msg.timeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			c.moveToFirst();
			while(!c.isAfterLast()) {
				if (DBG) Log.d(TAG, "message already existed : " + msg.toString());
				c.close();
				updateDB(msg);
				return -1;
			}

			ContentValues cv = _getContentValues(msg);
			long regId = mSQLiteDB.insert(TABLE_NAME_NOTIFICATION, null, cv);
			//checkNotificationMessageDB();
			if (msg.deviceType == DeviceType.DIAPER_SENSOR) {
				switch(msg.notiType) {
					case NotificationType.PEE_DETECTED:
					case NotificationType.POO_DETECTED:
					case NotificationType.FART_DETECTED:
					case NotificationType.ABNORMAL_DETECTED:
					case NotificationType.DIAPER_CHANGED:
					case NotificationType.CHAT_USER_FEEDBACK:
					case NotificationType.CHAT_USER_INPUT:
					case NotificationType.BABY_SLEEP:
					case NotificationType.BABY_FEEDING_BABY_FOOD:
					case NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK:
					case NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK:
					case NotificationType.BABY_FEEDING_NURSED_BREAST_MILK:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 0, regId);
						break;
				}
			} else if (msg.deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
				switch(msg.notiType) {
					case NotificationType.HIGH_TEMPERATURE:
					case NotificationType.LOW_TEMPERATURE:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 0, regId);
						break;
					case NotificationType.HIGH_HUMIDITY:
					case NotificationType.LOW_HUMIDITY:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 1, regId);
						break;
					case NotificationType.VOC_WARNING:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 2, regId);
						break;
				}
			} else if (msg.deviceType == DeviceType.SYSTEM) {
				switch(msg.notiType) {
					case NotificationType.MY_CLOUD_INVITE:
					case NotificationType.MY_CLOUD_DELETE:
					case NotificationType.MY_CLOUD_LEAVE:
					case NotificationType.MY_CLOUD_REQUEST:
					case NotificationType.OTHER_CLOUD_INVITED:
					case NotificationType.OTHER_CLOUD_DELETED:
					case NotificationType.OTHER_CLOUD_LEAVE:
					case NotificationType.OTHER_CLOUD_REQUEST:
					case NotificationType.CLOUD_INIT_DEVICE:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(DeviceType.SYSTEM, 0, 0, regId);
						break;
				}
			}
			if (c != null) c.close();
			return regId;
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			if (c != null) c.close();
			return -1;
		}
	}

	public int insertNotificationMessageList(int deviceType, long deviceId, ArrayList<NotificationMessage> msgList) {
		if (msgList == null) {
			return -1;
		}
		if (DBG) Log.d(TAG, "insertDeviceNotificationMessageList Start : " + msgList.size());

		HashMap<String, String> existedHashMap = new HashMap<>(); // 중복체크를 위한 기존 저장된 메시지 해시맵
		ArrayList<NotificationMessage> existedMsgList; // 중복확인을 위한 기존 저장된 메시지 리스트
		ArrayList<NotificationMessage> insertAvailableMsgList = new ArrayList<>(); // 저장해야 할 메시지 중 중복 제거되어 삽입가능한 메시지 리스트
		ArrayList<NotificationMessage> insertSucceededMsgList = new ArrayList<>(); // 삽입이 완료된 메시지 리스트
		ArrayList<Long> insertSucceededMsgIdList = new ArrayList<>(); // 삽입이 완료된 메시지ID 리스트

		// Insert 원하는 시작/종료시간 확인
		long beginMs = -1;
		long endMs = -1;
		for (NotificationMessage msg : msgList) {
			if (beginMs == -1) beginMs = msg.timeMs;
			if (endMs == -1) endMs = msg.timeMs;

			if (msg.timeMs < beginMs) beginMs = msg.timeMs;
			if (msg.timeMs > endMs) endMs = msg.timeMs;
		}
		if (DBG) Log.d(TAG, "begin/end: " + beginMs + " / " + endMs);

		existedMsgList = getNotificationMessages(deviceType, deviceId, beginMs, endMs);
		if (existedMsgList == null) {
            if (DBG) Log.d(TAG, "existedMsgList NULL");
			insertAvailableMsgList = msgList;
		} else {
            // 중복확인을 위한 기존 메시지 해시맵 구성
            for (NotificationMessage msg : existedMsgList) {
            	String key = msg.notiType + "," + msg.deviceType +"," + msg.deviceId + "," + msg.timeMs;
                existedHashMap.put(key, "1");
            }
            if (DBG) Log.d(TAG, "existedMsgList: " + existedMsgList.size());

			for (NotificationMessage msg: msgList) {
				String key = msg.notiType + "," + msg.deviceType +"," + msg.deviceId + "," + msg.timeMs;
				if (existedHashMap.containsKey(key)) {
					if (DBG) Log.d(TAG, "Duplicated Msg: " + key);
				} else {
					insertAvailableMsgList.add(msg);
				}
			}
		}
		if (DBG) Log.d(TAG, "insertAvailableMsgList: " + insertAvailableMsgList.size());

		// 중복제거된 메시지 삽입
		try {
			mSQLiteDB = mDBHelper.getWritableDatabase();

			long regId = 0;
			mSQLiteDB.beginTransaction();
			for (NotificationMessage msg : insertAvailableMsgList) {
				ContentValues cv = _getContentValues(msg);
				regId = mSQLiteDB.insert(TABLE_NAME_NOTIFICATION, null, cv);
				if (regId == -1) {
					if (DBG) Log.e(TAG, "insertDBList failed: " + msg.toString());
				} else {
					msg.msgId = regId;
					insertSucceededMsgList.add(msg);
				}
			}
			mSQLiteDB.setTransactionSuccessful();
			mSQLiteDB.endTransaction();

			if (DBG) Log.d(TAG, "insertDeviceNotificationMessageList End : " + insertSucceededMsgList.size());
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}

		// DB입력 후 후속작업
		for (NotificationMessage msg : insertSucceededMsgList) {
			if (msg.deviceType == DeviceType.DIAPER_SENSOR) {
				switch(msg.notiType) {
					case NotificationType.PEE_DETECTED:
					case NotificationType.POO_DETECTED:
					case NotificationType.FART_DETECTED:
					case NotificationType.ABNORMAL_DETECTED:
					case NotificationType.DIAPER_CHANGED:
					case NotificationType.CHAT_USER_FEEDBACK:
					case NotificationType.CHAT_USER_INPUT:
					case NotificationType.BABY_SLEEP:
					case NotificationType.BABY_FEEDING_BABY_FOOD:
					case NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK:
					case NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK:
					case NotificationType.BABY_FEEDING_NURSED_BREAST_MILK:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 0, msg.msgId);
						break;
				}
				switch(msg.notiType) {
					case NotificationType.PEE_DETECTED:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPeeDetected(msg.deviceId, msg.timeMs);
						break;
					case NotificationType.POO_DETECTED:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPooDetected(msg.deviceId, msg.timeMs);
						break;
					case NotificationType.FART_DETECTED:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmFartDetected(msg.deviceId, msg.timeMs);
						break;
					case NotificationType.DIAPER_CHANGED:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmDiaperChanged(msg.deviceId, msg.timeMs);
						break;
				}
			} else if (msg.deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB) {
				switch(msg.notiType) {
					case NotificationType.HIGH_TEMPERATURE:
					case NotificationType.LOW_TEMPERATURE:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 0, msg.msgId);
						break;
					case NotificationType.HIGH_HUMIDITY:
					case NotificationType.LOW_HUMIDITY:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 1, msg.msgId);
						break;
					case NotificationType.VOC_WARNING:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(msg.deviceType, msg.deviceId, 2, msg.msgId);
						break;
				}
			} else if (msg.deviceType == DeviceType.SYSTEM) {
				switch(msg.notiType) {
					case NotificationType.MY_CLOUD_INVITE:
					case NotificationType.MY_CLOUD_DELETE:
					case NotificationType.MY_CLOUD_LEAVE:
					case NotificationType.MY_CLOUD_REQUEST:
					case NotificationType.OTHER_CLOUD_INVITED:
					case NotificationType.OTHER_CLOUD_DELETED:
					case NotificationType.OTHER_CLOUD_LEAVE:
					case NotificationType.OTHER_CLOUD_REQUEST:
					case NotificationType.CLOUD_INIT_DEVICE:
						PreferenceManager.getInstance(mContext).setLatestSavedNotificationIndex(DeviceType.SYSTEM, 0, 0, msg.msgId);
						break;
				}
			}
		}
		return insertSucceededMsgList.size();
	}

	public int insertCloudNotificationMessageList(ArrayList<NotificationMessage> msgList) {
		if (msgList == null) {
			return -1;
		}
		return insertNotificationMessageList(DeviceType.SYSTEM, 0, msgList);
	}

	public int updateDB(NotificationMessage msg) {
		if (msg == null) {
			return -1;
		}
		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			ContentValues cv = _getContentValues(msg);
			int res = mSQLiteDB.update(TABLE_NAME_NOTIFICATION, cv, "noti_type=? AND device_type=? AND device_id=? AND created=?", new String[]{msg.notiType + "", msg.deviceType + "", msg.deviceId + "", msg.timeMs + ""});
			if (res == 0) {
				if (DBG) Log.e(TAG, "updateDB failed[1]: " + msg.toString());

				int res2 = mSQLiteDB.update(TABLE_NAME_NOTIFICATION, cv, "server_noti_id=?", new String[]{msg.serverNotiId + ""});
				if (res2 == 0) {
					if (DBG) Log.e(TAG, "updateDB failed[2]: " + msg.toString());
					return -1;
				} else {
					if (DBG) Log.d(TAG, "updateDB succeeded[2]: " + msg.toString());
				}
			} else {
				if (DBG) Log.d(TAG, "updateDB succeeded[1]: " + msg.toString());
			}
		} catch (Exception e) {
			if (DBG) Log.e(TAG, "updateDB failed: " + msg.toString());
			return -1;
		}
		//checkNotificationMessageDB();
		return 1;
	}

	public int deleteDB(NotificationMessage msg) {
		if (msg == null) {
			return -1;
		}
		if (DBG) Log.d(TAG, "deleteDB : " + msg.toString());
		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			mSQLiteDB.delete(TABLE_NAME_NOTIFICATION, "noti_type=? AND device_type=? AND device_id=? AND created=?", new String[]{msg.notiType + "", msg.deviceType + "", msg.deviceId + "", msg.timeMs + ""});
			//checkNotificationMessageDB();
			return 1;
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			return -1;
		}
	}

	public int modifyNotificationMessage(long serverNotiId, int editType, String editExtra, long editTimeUtcMs) {
		if (DBG) Log.d(TAG, "modifyNotificationMessage: " + serverNotiId + " / " + editType + " / " + editExtra + " / " + editTimeUtcMs);
		NotificationMessage msg = null;
		int ret = -1;
		String table = TABLE_NAME_NOTIFICATION;
		String[] columns = null;
		String selection = "server_noti_id=?";
		String[] selectionArgs = {serverNotiId + ""};
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String limit = null;
		Cursor tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		tc.moveToFirst();
		while(!tc.isAfterLast()) {
			long msgId = tc.getLong(0);
			int noti_type = tc.getInt(1);
			int device_type = tc.getInt(2);
			long device_id = tc.getLong(3);
			String extra = tc.getString(4);
			long created = tc.getLong(5);
			long server_noti_id = tc.getLong(6);
			msg = new NotificationMessage(msgId, noti_type, device_type, device_id, extra, created, server_noti_id);
			if (DBG) Log.d(TAG,"  found existed message: " + msg.toString());
			tc.moveToNext();
		}
		tc.close();
		if (msg == null) {
			if (DBG) Log.e(TAG, "modifyNotificationMessage Failed");
			return -1;
		}
		if (editType == 1) { // remove
			ret = deleteDB(msg);
			if (ret == -1) if (DBG) Log.d(TAG, "remove failed: " + msg.toString());
			else if (DBG) Log.d(TAG, "remove succeeded: " + msg.toString());
		} else if (editType == 2) { // edit
			if (DBG) Log.d(TAG, "modify from msg: " + msg.toString());
			msg.extra = editExtra;
			msg.timeMs = editTimeUtcMs;
			ret = updateDB(msg);
			if (ret == -1) if (DBG) Log.d(TAG, "modify failed: " + msg.toString());
			else if (DBG) Log.d(TAG, "modify succeeded: " + msg.toString());
		}
		return ret;
	}

	public void checkNotificationMessageDB() {
		if (DBG) Log.d(TAG, "=============== Notification Message Database Values ===============");
		String table = TABLE_NAME_NOTIFICATION;
		String[] columns = null;
		String selection = null;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String limit = null;
		Cursor tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		tc.moveToFirst();
		while(!tc.isAfterLast()) {
			long msgId = tc.getLong(0);
			int notiType = tc.getInt(1);
			int deviceType = tc.getInt(2);
			long deviceId = tc.getLong(3);
			String extra = tc.getString(4);
			long created = tc.getLong(5);
			long serverNotiId = tc.getLong(6);
			NotificationMessage msg = new NotificationMessage(msgId, notiType, deviceType, deviceId, extra, created, serverNotiId);

			if (DBG) Log.d(TAG, msg.toString());
			tc.moveToNext();
		}
		tc.close();
	}

	public void checkNotificationMessageDB(int deviceType, long deviceId) {
		if (DBG) Log.d(TAG, "=============== Notification Message Database Values (" + deviceType + " / " + deviceId + ") ===============");
		String table = TABLE_NAME_NOTIFICATION;
		String[] columns = null;
		String selection = "device_type=? AND device_id>=?";
		String[] selectionArgs = {deviceType + "", deviceId + ""};
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String limit = null;
		Cursor tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		tc.moveToFirst();
		while(!tc.isAfterLast()) {
			long msgId = tc.getLong(0);
			int noti_type = tc.getInt(1);
			int device_type = tc.getInt(2);
			long device_id = tc.getLong(3);
			String extra = tc.getString(4);
			long created = tc.getLong(5);
			long serverNotiId = tc.getLong(6);
			NotificationMessage msg = new NotificationMessage(msgId, noti_type, device_type, device_id, extra, created, serverNotiId);

			if (DBG) Log.d(TAG, msg.toString());
			tc.moveToNext();
		}
		tc.close();
	}

	public int deleteNotificationMessages(int type, long deviceId) {
		if (deviceId < 0) {
			return -1;
		}

		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			// 초기화 메시지는 삭제하지 않음
			mSQLiteDB.delete(TABLE_NAME_NOTIFICATION, "device_type=? AND device_id=? AND noti_type !=?", new String[]{type + "", deviceId + "", NotificationType.CLOUD_INIT_DEVICE + ""});
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			return -1;
		}
		return 1;
	}

	public ArrayList<NotificationMessage> getNotificationMessages(int deviceType, long deviceId, long beginMs, long endMs) {
		ArrayList<NotificationMessage> messages = new ArrayList<NotificationMessage>();
		Cursor tc = null;
		try {
			mSQLiteDB = mDBHelper.getReadableDatabase();
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created>=? AND created<=?";
			String[] selectionArgs = {deviceType + "", deviceId + "", beginMs + "", endMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created asc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int notiType = tc.getInt(1);
				int device_type = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);
				long serverNotiId = tc.getLong(6);
				messages.add(new NotificationMessage(msgId, notiType, device_type, device_id, extra, created, serverNotiId));
				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return messages;
	}

	public int getTodayDiaperAlarmCount(long deviceId) {
		int alarmCount = 0;
		long now = DateTimeUtil.convertUTCToLocalTimeMs(System.currentTimeMillis());
		long dayBeginTimeMs = DateTimeUtil.getDayBeginMillis(now);
		long dayEndTimeMs = DateTimeUtil.getDayEndMillis(now);

		//String query = "SELECT * FROM notification where device_type=" + DeviceType.DIAPER_SENSOR + " AND device_id=" + deviceId + " AND created>=" + dayBeginTimeMs + " AND created<=" + dayEndTimeMs + " order by created desc";

		ArrayList<NotificationMessage> messages = new ArrayList<NotificationMessage>();
		Cursor tc = null;
		try {
			//mSQLiteDB = mDBHelper.getReadableDatabase();
			//tc = mSQLiteDB.rawQuery(query, null);
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created>=? AND created<=?";
			String[] selectionArgs = {DeviceType.DIAPER_SENSOR + "", deviceId + "", dayBeginTimeMs + "", dayEndTimeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();

			while(!tc.isAfterLast()) {
				int notiType = tc.getInt(1);
				switch(notiType) {
					case NotificationType.PEE_DETECTED:
					case NotificationType.POO_DETECTED:
					case NotificationType.FART_DETECTED:
					case NotificationType.ABNORMAL_DETECTED:
						alarmCount++;
						break;

				}
				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return alarmCount;
	}

	public ArrayList<SensorGraphInfo> getSensorGraphInfoList(long deviceId, long beginMs, long endMs) {
		int days = (int)((endMs - beginMs) / DateTimeUtil.ONE_DAY_MILLIS) + 1;
		if (DBG) Log.d(TAG, "getSensorGraphInfoList : " + beginMs + "~" + endMs + "=>" + days + "days");
		//String query = "SELECT * FROM notification where created>=" + beginMs + " and created <=" + endMs + " and device_type=" + DeviceType.DIAPER_SENSOR + " and device_id=" + deviceId + " order by created asc";

		int[] cntDiaperChanged = new int[days];
		int[] cntPeeDetected = new int[days];
		int[] cntPooDetected = new int[days];
		int[] cntFartDetected = new int[days];
		for (int i = 0; i < days; i++) {
			cntDiaperChanged[i] = 0;
			cntPeeDetected[i] = 0;
			cntPooDetected[i] = 0;
			cntFartDetected[i] = 0;
		}

		int idx = 0;
		Cursor tc = null;
		try {
			//mSQLiteDB = mDBHelper.getReadableDatabase();
			//tc = mSQLiteDB.rawQuery(query, null);

			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created>=? AND created<=?";
			String[] selectionArgs = {DeviceType.DIAPER_SENSOR + "", deviceId + "", beginMs + "", endMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int noti_type = tc.getInt(1);
				int device_type = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);

				idx = (int)((created - beginMs) / DateTimeUtil.ONE_DAY_MILLIS);
				if (DBG) Log.d(TAG, "noti : " + noti_type + " / " + created + " / " + idx);

				switch(noti_type) {
					case NotificationType.DIAPER_CHANGED:
						cntDiaperChanged[idx]++;
						break;
					case NotificationType.PEE_DETECTED:
						cntPeeDetected[idx]++;
						break;
					case NotificationType.POO_DETECTED:
						cntPooDetected[idx]++;
						break;
					case NotificationType.FART_DETECTED:
						cntFartDetected[idx]++;
						break;
				}
				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}

		ArrayList<SensorGraphInfo> sensorGraphInfoList = new ArrayList<>();
		for (int i = 0; i < days; i++) {
			SensorGraphInfo info = new SensorGraphInfo();
			info.cntDiaperChanged = cntDiaperChanged[i];
			info.cntPeeDetected = cntPeeDetected[i];
			info.cntPooDetected = cntPooDetected[i];
			info.cntFartDetected = cntFartDetected[i];
			sensorGraphInfoList.add(info);
		}

		if (tc != null) tc.close();
		return sensorGraphInfoList;
	}

	public ArrayList<SensorGraphInfo> getDiaperChangedInfoList(long deviceId, long beginMs, long endMs) {
		int days = (int)((endMs - beginMs) / DateTimeUtil.ONE_DAY_MILLIS) + 1;
		if (DBG) Log.d(TAG, "getDiaperChangedInfoList : " + beginMs + "~" + endMs + "=>" + days + "days");

		int[] cntDiaperChanged = new int[days];
		int[] cntPeeDetected = new int[days];
		int[] cntPooDetected = new int[days];
		int[] cntSoiledDetected = new int[days];
		for (int i = 0; i < days; i++) {
			cntDiaperChanged[i] = 0;
			cntPeeDetected[i] = 0;
			cntPooDetected[i] = 0;
			cntSoiledDetected[i] = 0;
		}

		int idx = 0;
		Cursor tc = null;
		try {
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created>=? AND created<=?";
			String[] selectionArgs = {DeviceType.DIAPER_SENSOR + "", deviceId + "", beginMs + "", endMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int noti_type = tc.getInt(1);
				int device_type = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);

				idx = (int)((created - beginMs) / DateTimeUtil.ONE_DAY_MILLIS);
				if (DBG) Log.d(TAG, "noti : " + noti_type + " / " + created + " / " + idx);

				if (noti_type == NotificationType.DIAPER_CHANGED) {
					if ("1".equals(extra)) { // Clean
						cntDiaperChanged[idx]++;
					} else if ("2".equals(extra)) { // Pee
						cntDiaperChanged[idx]++;
						cntPeeDetected[idx]++;
					} else if ("3".equals(extra)) { // Poo
						cntDiaperChanged[idx]++;
						cntPooDetected[idx]++;
					} else if ("4".equals(extra)) { // Pee/Poo Mixed
						cntDiaperChanged[idx]++;
						cntPeeDetected[idx]++;
						cntPooDetected[idx]++;
					} else {
						cntDiaperChanged[idx]++;
					}
				} else if (noti_type == NotificationType.DIAPER_NEED_TO_CHANGE) {
					cntSoiledDetected[idx]++;
				}

				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}

		if (tc != null) tc.close();

		ArrayList<SensorGraphInfo> sensorGraphInfoList = new ArrayList<>();
		for (int j = 0; j < days; j++) {
			SensorGraphInfo info = new SensorGraphInfo();
			info.cntDiaperChanged = cntDiaperChanged[j];
			info.cntPeeDetected = cntPeeDetected[j];
			info.cntPooDetected = cntPooDetected[j];
			info.cntSoiledDetected = cntSoiledDetected[j];
			sensorGraphInfoList.add(info);
		}

		return sensorGraphInfoList;
	}

	public ArrayList<NotificationMessage> getDiaperSensorMessages(int deviceType, long deviceId, long utcTimeMs, int limit) {
		ArrayList<Integer> allList = new ArrayList<>();
		allList.add(NotificationType.PEE_DETECTED);
		allList.add(NotificationType.POO_DETECTED);
		allList.add(NotificationType.FART_DETECTED);
		allList.add(NotificationType.ABNORMAL_DETECTED);
		allList.add(NotificationType.DIAPER_CHANGED);
		if (Configuration.BETA_TEST_MODE) {
			allList.add(NotificationType.DIAPER_DETACHMENT_DETECTED);
		}
		return getDiaperSensorMessages(deviceType, deviceId, allList, utcTimeMs, limit);
	}

	public ArrayList<NotificationMessage> getDiaperSensorMessagesV2(int deviceType, long deviceId, long utcTimeMs, int limit) {
		ArrayList<Integer> allList = new ArrayList<>();
		if (PreferenceManager.getInstance(mContext).getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_SOILED)) {
			allList.add(NotificationType.PEE_DETECTED);
		}
		if (PreferenceManager.getInstance(mContext).getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_SOILED)) {
			allList.add(NotificationType.POO_DETECTED);
		}
		if (PreferenceManager.getInstance(mContext).getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.FART_DETECTED)) {
			allList.add(NotificationType.FART_DETECTED);
		}
		if (PreferenceManager.getInstance(mContext).getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_NEED_TO_CHANGE)) {
			allList.add(NotificationType.DIAPER_NEED_TO_CHANGE);
		}
		allList.add(NotificationType.DIAPER_CHANGED);
		allList.add(NotificationType.BABY_SLEEP);
		allList.add(NotificationType.BABY_FEEDING_BABY_FOOD);
		allList.add(NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK);
		allList.add(NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK);
		allList.add(NotificationType.BABY_FEEDING_NURSED_BREAST_MILK);
		return getDiaperSensorMessages(deviceType, deviceId, allList, utcTimeMs, limit);
	}

	public NotificationMessage getRecentDiaperChangedMessages(long deviceId) {
		Cursor tc = null;
		long now = System.currentTimeMillis();
		NotificationMessage msg = null;
		try {
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created<? AND noti_type=?";
			String[] selectionArgs = {DeviceType.DIAPER_SENSOR + "", deviceId + "", now + "", NotificationType.DIAPER_CHANGED + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = "1";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			tc.moveToFirst();

			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int notiType = tc.getInt(1);
				int deviceType = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);
				long serverNotiId = tc.getLong(6);
				msg = new NotificationMessage(msgId, notiType, deviceType, device_id, extra, created, serverNotiId);
				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return msg;
	}

	public ArrayList<NotificationMessage> getDiaperSensorMessages(int deviceType, long deviceId, ArrayList<Integer> types, long utcTimeMs, int countMessages) {
		ArrayList<NotificationMessage> messages = new ArrayList<NotificationMessage>();
		if (DBG) Log.d(TAG, "getDiaperSensorMessages: " + utcTimeMs + " / " + countMessages);

		Cursor tc = null;
		try {
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created<?";
			String[] selectionArgs = {deviceType + "", deviceId + "", utcTimeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = countMessages + "";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();

			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int notiType = tc.getInt(1);
				int device_type = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);
				long serverNotiId = tc.getLong(6);
				NotificationMessage msg = new NotificationMessage(msgId, notiType, device_type, device_id, extra, created, serverNotiId);

				if (DBG) Log.d(TAG, "found: " + msg.toString());
				switch(notiType) {
					case NotificationType.CHAT_USER_INPUT:
					case NotificationType.CHAT_USER_FEEDBACK:
					case NotificationType.DIAPER_DETACHMENT_DETECTED:
						if (Configuration.BETA_TEST_MODE) { // 베타테스트모드일 때에만 CHAT_USER_INPUT 추가
							messages.add(msg);
						}
						break;
					default:
					//case NotificationType.PEE_DETECTED:
					//case NotificationType.POO_DETECTED:
					//case NotificationType.FART_DETECTED:
					//case NotificationType.DIAPER_CHANGED:
					//case NotificationType.ABNORMAL_DETECTED:
					//case NotificationType.CONNECTED:
					//case NotificationType.DISCONNECTED:
						for (int type : types) {
							if (type == notiType) {
								messages.add(msg);
								break;
							}
						}
						break;
				}
				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return messages;
	}

	public ArrayList<NotificationMessage> getAQMHubMessages(long deviceId, long utcTimeMs, int countMessages) {
		ArrayList<NotificationMessage> messages = new ArrayList<NotificationMessage>();
		Cursor tc = null;
		try {
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created<?";
			String[] selectionArgs = {DeviceType.AIR_QUALITY_MONITORING_HUB + "", deviceId + "", utcTimeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = countMessages + "";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int notiType = tc.getInt(1);
				int deviceType = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);
				long serverNotiId = tc.getLong(6);
				NotificationMessage msg = new NotificationMessage(msgId, notiType, deviceType, device_id, extra, created, serverNotiId);

				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					switch (notiType) {
						case NotificationType.LOW_TEMPERATURE:
						case NotificationType.HIGH_TEMPERATURE:
						case NotificationType.LOW_HUMIDITY:
						case NotificationType.HIGH_HUMIDITY:
							messages.add(msg);
							break;
					}
				} else {
					switch (notiType) {
						case NotificationType.LOW_TEMPERATURE:
						case NotificationType.HIGH_TEMPERATURE:
						case NotificationType.LOW_HUMIDITY:
						case NotificationType.HIGH_HUMIDITY:
						case NotificationType.VOC_WARNING:
							//case NotificationType.CONNECTED:
							//case NotificationType.DISCONNECTED:
							//case NotificationType.CHAT_USER_INPUT:
							//case NotificationType.CHAT_USER_FEEDBACK:
							messages.add(msg);
							break;
					}
				}

				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return messages;
	}

	public ArrayList<NotificationMessage> getLampMessages(long deviceId, long utcTimeMs, int countMessages) {
		ArrayList<NotificationMessage> messages = new ArrayList<NotificationMessage>();
		Cursor tc = null;
		try {
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "device_type=? AND device_id=? AND created<?";
			String[] selectionArgs = {DeviceType.LAMP + "", deviceId + "", utcTimeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = countMessages + "";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int notiType = tc.getInt(1);
				int deviceType = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);
				long serverNotiId = tc.getLong(6);
				NotificationMessage msg = new NotificationMessage(msgId, notiType, deviceType, device_id, extra, created, serverNotiId);

				switch(notiType) {
					case NotificationType.LOW_TEMPERATURE:
					case NotificationType.HIGH_TEMPERATURE:
					case NotificationType.LOW_HUMIDITY:
					case NotificationType.HIGH_HUMIDITY:
					case NotificationType.VOC_WARNING:
						//case NotificationType.CONNECTED:
						//case NotificationType.DISCONNECTED:
						//case NotificationType.CHAT_USER_INPUT:
						//case NotificationType.CHAT_USER_FEEDBACK:
						messages.add(msg);
						break;
				}

				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return messages;
	}

	public ArrayList<NotificationMessage> getCloudMessages(long utcTimeMs, int countMessages) {
		ArrayList<NotificationMessage> messages = new ArrayList<NotificationMessage>();
		Cursor tc = null;
		try {
			String table = TABLE_NAME_NOTIFICATION;
			String[] columns = null;
			String selection = "created<?";
			String[] selectionArgs = {utcTimeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = countMessages + "";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long msgId = tc.getLong(0);
				int notiType = tc.getInt(1);
				int deviceType = tc.getInt(2);
				long device_id = tc.getLong(3);
				String extra = tc.getString(4);
				long created = tc.getLong(5);
				long serverNotiId = tc.getLong(6);
				NotificationMessage msg = new NotificationMessage(msgId, notiType, deviceType, device_id, extra, created, serverNotiId);

				switch(notiType) {
					case NotificationType.MY_CLOUD_INVITE:
					case NotificationType.MY_CLOUD_DELETE:
					case NotificationType.MY_CLOUD_LEAVE:
					case NotificationType.MY_CLOUD_REQUEST:
					case NotificationType.OTHER_CLOUD_INVITED:
					case NotificationType.OTHER_CLOUD_DELETED:
					case NotificationType.OTHER_CLOUD_LEAVE:
					case NotificationType.OTHER_CLOUD_REQUEST:
					case NotificationType.CLOUD_INIT_DEVICE:
						messages.add(msg);
						break;

				}
				tc.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return messages;
	}
	//endregion

	//region "HubGraph"
	public long insertHubGraphInfoList(ArrayList<HubGraphInfo> hubGraphInfoList) {
		if (hubGraphInfoList == null) {
			return -1;
		}
		int cntInsertedHubGraphInfo = 0;
		long regId = 0;
		if (DBG) Log.d(TAG, "insertHubGraphInfoList Start : " + hubGraphInfoList.size());
		try {
			mSQLiteDB = mDBHelper.getWritableDatabase();
			mSQLiteDB.beginTransaction();
			for (HubGraphInfo hubGraphInfo : hubGraphInfoList) {
				ContentValues cv = _getContentValues(hubGraphInfo);
				regId = mSQLiteDB.insert(TABLE_NAME_HUB_GRAPH, null, cv);

				if (regId == -1) {
					if (DBG) Log.e(TAG, "insertDBList failed: " + hubGraphInfo.toString());
				} else {
					cntInsertedHubGraphInfo++;
				}
			}
			mSQLiteDB.setTransactionSuccessful();
			mSQLiteDB.endTransaction();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			cntInsertedHubGraphInfo = -1;
		}
		if (DBG) Log.d(TAG, "insertHubGraphInfoList End : " + cntInsertedHubGraphInfo);
		return cntInsertedHubGraphInfo;
	}

	public long insertDB(HubGraphInfo hubGraphInfo) {
		if (hubGraphInfo == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			// 중복체크
			//c = mSQLiteDB.rawQuery("SELECT * FROM hubgraph WHERE device_id=" +  + " AND created=" + , null);

			String table = TABLE_NAME_HUB_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND created=?";
			String[] selectionArgs = {hubGraphInfo.deviceId + "", hubGraphInfo.timeSec + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			c.moveToFirst();
			while(!c.isAfterLast()) {
				if (DBG) Log.d(TAG, "graph already existed : " + hubGraphInfo.timeSec);
				c.close();
				return -1;
			}

			ContentValues cv = _getContentValues(hubGraphInfo);
			regId = mSQLiteDB.insert(TABLE_NAME_HUB_GRAPH, null, cv);
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (c != null) c.close();
		//checkProfileDB();
		return regId;
	}

	public ArrayList<HubGraphInfo> getHubGraphInfoList(long deviceId, long fromSec, long toSec) {
		if (DBG) Log.d(TAG, "getHubGraphInfoList : " + fromSec + "~" + toSec);
		String query = "SELECT * FROM hubgraph where created>=" + fromSec + " and created <=" + toSec + " and device_id=" + deviceId + " order by created asc";

		ArrayList<HubGraphInfo> hubGraphInfoList = new ArrayList<>();
		Cursor tc = null;
		try {
			//mSQLiteDB = mDBHelper.getReadableDatabase();
			//tc = mSQLiteDB.rawQuery(query, null);

			String table = TABLE_NAME_HUB_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND created>=? AND created<=?";
			String[] selectionArgs = {deviceId + "", fromSec + "", toSec + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created asc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long id = tc.getLong(0);
				int temperature = tc.getInt(2);
				int humidity = tc.getInt(3);
				int voc = tc.getInt(4);
				int score = tc.getInt(5);
				long created = tc.getLong(6);

				tc.moveToNext();

				HubGraphInfo info = new HubGraphInfo();
				info.deviceId = deviceId;
				info.temperature = temperature;
				info.humidity = humidity;
				info.voc = voc;
				info.score = score;
				info.timeSec = created;

				hubGraphInfoList.add(info);

				if (DBG) Log.d(TAG, "get : " + info.temperature + " / " + info.humidity + " / " + info.voc + " / " + info.timeSec);
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return hubGraphInfoList;
	}
	//endregion

	//region "LampGraph"
	public long insertLampGraphInfoList(ArrayList<LampGraphInfo> lampGraphInfoList) {
		if (lampGraphInfoList == null) {
			return -1;
		}
		int cntInsertedLampGraphInfo = 0;
		long regId = 0;
		if (DBG) Log.d(TAG, "insertLampGraphInfoList Start : " + lampGraphInfoList.size());
		try {
			mSQLiteDB = mDBHelper.getWritableDatabase();
			mSQLiteDB.beginTransaction();
			for (LampGraphInfo lampGraphInfo : lampGraphInfoList) {
				ContentValues cv = _getContentValues(lampGraphInfo);
				regId = mSQLiteDB.insert(TABLE_NAME_LAMP_GRAPH, null, cv);

				if (regId == -1) {
					if (DBG) Log.e(TAG, "insertDBList failed: " + lampGraphInfo.toString());
				} else {
					cntInsertedLampGraphInfo++;
				}
			}
			mSQLiteDB.setTransactionSuccessful();
			mSQLiteDB.endTransaction();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			cntInsertedLampGraphInfo = -1;
		}
		if (DBG) Log.d(TAG, "insertLampGraphInfoList End : " + cntInsertedLampGraphInfo);
		return cntInsertedLampGraphInfo;
	}

	public long insertDB(LampGraphInfo lampGraphInfo) {
		if (lampGraphInfo == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			// 중복체크
			//c = mSQLiteDB.rawQuery("SELECT * FROM hubgraph WHERE device_id=" +  + " AND created=" + , null);

			String table = TABLE_NAME_LAMP_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND created=?";
			String[] selectionArgs = {lampGraphInfo.deviceId + "", lampGraphInfo.timeSec + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			c.moveToFirst();
			while(!c.isAfterLast()) {
				if (DBG) Log.d(TAG, "graph already existed : " + lampGraphInfo.timeSec);
				c.close();
				return -1;
			}

			ContentValues cv = _getContentValues(lampGraphInfo);
			regId = mSQLiteDB.insert(TABLE_NAME_LAMP_GRAPH, null, cv);
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (c != null) c.close();
		//checkProfileDB();
		return regId;
	}

	public ArrayList<LampGraphInfo> getLampGraphInfoList(long deviceId, long fromSec, long toSec) {
		if (DBG) Log.d(TAG, "getLampGraphInfoList : " + fromSec + "~" + toSec);
		String query = "SELECT * FROM lampgraph where created>=" + fromSec + " and created <=" + toSec + " and device_id=" + deviceId + " order by created asc";

		ArrayList<LampGraphInfo> lampGraphInfoList = new ArrayList<>();
		Cursor tc = null;
		try {
			//mSQLiteDB = mDBHelper.getReadableDatabase();
			//tc = mSQLiteDB.rawQuery(query, null);

			String table = TABLE_NAME_LAMP_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND created>=? AND created<=?";
			String[] selectionArgs = {deviceId + "", fromSec + "", toSec + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created asc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			tc.moveToFirst();
			while(!tc.isAfterLast()) {
				long id = tc.getLong(0);
				int temperature = tc.getInt(2);
				int humidity = tc.getInt(3);
				int voc = tc.getInt(4);
				int score = tc.getInt(5);
				long created = tc.getLong(6);

				tc.moveToNext();

				LampGraphInfo info = new LampGraphInfo();
				info.deviceId = deviceId;
				info.temperature = temperature;
				info.humidity = humidity;
				info.voc = voc;
				info.score = score;
				info.timeSec = created;

				lampGraphInfoList.add(info);

				if (DBG) Log.d(TAG, "get : " + info.temperature + " / " + info.humidity + " / " + info.voc + " / " + info.timeSec);
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (tc != null) tc.close();
		return lampGraphInfoList;
	}
	//endregion

	//region "MovementGraph"
	public long insertMovementGraphInfoList(ArrayList<MovementGraphInfo> movementGraphInfoList) {
		if (movementGraphInfoList == null) {
			return -1;
		}
		int cntInsertedMovementGraphInfo = 0;
		long regId = 0;
		if (DBG) Log.d(TAG, "insertMovementGraphInfoList Start : " + movementGraphInfoList.size());
		try {
			mSQLiteDB = mDBHelper.getWritableDatabase();
			mSQLiteDB.beginTransaction();
			for (MovementGraphInfo movementGraphInfo : movementGraphInfoList) {
				ContentValues cv = _getContentValues(movementGraphInfo);
				regId = mSQLiteDB.insert(TABLE_NAME_MOVEMENT_GRAPH, null, cv);

				if (regId == -1) {
					if (DBG) Log.e(TAG, "insertDBList failed: " + movementGraphInfoList.toString());
				} else {
					cntInsertedMovementGraphInfo++;
				}
			}
			mSQLiteDB.setTransactionSuccessful();
			mSQLiteDB.endTransaction();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			cntInsertedMovementGraphInfo = -1;
		}
		if (DBG) Log.d(TAG, "insertMovementGraphInfoList End : " + cntInsertedMovementGraphInfo);
		return cntInsertedMovementGraphInfo;
	}

	public long insertDB(MovementGraphInfo movementGraphInfo) {
		if (movementGraphInfo == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
		try {
			String table = TABLE_NAME_MOVEMENT_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND starttime=?";
			String[] selectionArgs = {movementGraphInfo.deviceId + "", movementGraphInfo.startUtcTimeMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "created desc";
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			c.moveToFirst();
			while(!c.isAfterLast()) { // 존재하면,
				checkMovementGraphInfoDB();
				if (DBG) Log.d(TAG, "graph already existed : " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(movementGraphInfo.startUtcTimeMs));
				c.close();
				return -1;
			}

			ContentValues cv = _getContentValues(movementGraphInfo);
			regId = mSQLiteDB.insert(TABLE_NAME_MOVEMENT_GRAPH, null, cv);

			if (DBG) checkMovementGraphInfoDB();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
		if (c != null) c.close();
		return regId;
	}

	public void deleteMovementGraphInfoDB(long deviceId) {
		try {
			mSQLiteDB.delete(TABLE_NAME_MOVEMENT_GRAPH, "device_id=?", new String[]{deviceId + ""});
			if (DBG) Log.d(TAG, "deleteMovementDB(" + deviceId + ")");
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
	}

	public void checkMovementGraphInfoDB() {
		if (DBG) Log.d(TAG, "=============== Movement Values ===============");
		Cursor tc = null;
		try {
			String table = TABLE_NAME_MOVEMENT_GRAPH;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = "starttime asc";
			String limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			tc.moveToFirst();
			while (!tc.isAfterLast()) {
				long id = tc.getLong(0);
				long device_id = tc.getLong(1);
				long startUtcTimeMs = tc.getLong(2);
				String value = tc.getString(3);
				int count = tc.getInt(4);
				long created = tc.getLong(5);
				String log = device_id + " / " + startUtcTimeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startUtcTimeMs) + ") ~ " + (startUtcTimeMs + count * 10 * 1000) + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp((startUtcTimeMs + count * 10 * 1000)) + ") / " + count;
				if (DBG) Log.d(TAG, log);
				tc.moveToNext();
			}
			tc.close();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
		}
	}

	private ContentValues _getContentValues(MovementGraphInfo movementGraphInfo) {
		if (movementGraphInfo == null) {
			return null;
		}
		ContentValues values = new ContentValues();
		try {
			values.put("device_id", movementGraphInfo.deviceId + "");
			values.put("starttime", movementGraphInfo.startUtcTimeMs + "");
			values.put("value", movementGraphInfo.value + "");
			values.put("count", movementGraphInfo.count + "");
			values.put("created", movementGraphInfo.created + "");
		} catch (Exception e) {
			if (DBG) e.printStackTrace();
		}

		return values;
	}

	public ArrayList<Integer> getMovementGraphInfo(long deviceId, long beginMs, long endMs) {
		int days = (int)((endMs - beginMs) / DateTimeUtil.ONE_DAY_MILLIS) + 1;
		if (DBG) Log.d(TAG, "getMovementGraphInfo : " + deviceId + " / " + beginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(beginMs) + ") ~ " + endMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(endMs) + ")" + "=>" + days + "days");

		ArrayList<Integer> movementGraphInfo = new ArrayList<Integer>();
		int idx = 0;
		Cursor tc = null;
		//try {
			// 1. 중간에 짤렸을 수 있으므로 시작 시간 이전으로 가장 최근 1개 데이터 읽어오기
			String table = TABLE_NAME_MOVEMENT_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND starttime<?";
			String[] selectionArgs = {deviceId + "", beginMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "starttime desc";
			String limit = "1";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			MovementGraphInfo prevMovementInfo = null;
			tc.moveToFirst();
			while (!tc.isAfterLast()) {
				long id = tc.getLong(0);
				long device_id = tc.getLong(1);
				long startUtcTimeMs = tc.getLong(2);
				String value = tc.getString(3);
				int count = tc.getInt(4);
				long created = tc.getLong(5);

				prevMovementInfo = new MovementGraphInfo();
				prevMovementInfo.deviceId = device_id;
				prevMovementInfo.startUtcTimeMs = startUtcTimeMs;
				prevMovementInfo.value = value.toUpperCase();
				prevMovementInfo.count = count;
				prevMovementInfo.created = created;

				tc.moveToNext();
			}
			tc.close();

			ArrayList<Integer> valueList = new ArrayList<>();
			if (prevMovementInfo != null) {
				if (DBG) Log.d(TAG, "get data from prev row: " + prevMovementInfo.deviceId + " / " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(prevMovementInfo.startUtcTimeMs) + " / " + prevMovementInfo.count + " / " + prevMovementInfo.value);
				String movementValue = prevMovementInfo.value;
				char curr = '0';
				char prev = '0';
				char duplicatedChar = '0';
				boolean findDuplicatedCountNumber = false;
				String duplicatedCount = "";
				for (int i = 0; i < movementValue.length(); i++) {
					if (i >= prevMovementInfo.count) break;

					curr = movementValue.charAt(i);

					if (curr == '(') {
						findDuplicatedCountNumber = true;
						duplicatedChar = prev;
						continue;
					} else if (curr == ')') {
						// 괄호안에 숫자 개수만큼 중복 숫자를 리스트에 넣기
						int cnt = Integer.parseInt(duplicatedCount);
						int movementLevel = DeviceStatus.getMovementLevelFromValue(duplicatedChar);
						for (int j = 0; j < cnt - 1; j++) {
							valueList.add(movementLevel);
						}
						duplicatedCount = "";
						findDuplicatedCountNumber = false;
					} else if (findDuplicatedCountNumber == true) {
						duplicatedCount += curr;
					} else {
						valueList.add(DeviceStatus.getMovementLevelFromValue(curr));
					}
					prev = curr;
				}

				if (valueList != null && valueList.size() > 0) {
					// 부족한 수량 추가
					int insufficientCount = valueList.size() - 1;
					for (int k = 0; k < prevMovementInfo.count - insufficientCount; k++) {
						valueList.add(0);
					}

					//String valueString = "";
					//for (int k = 0; k < valueList.size(); k++) {
						//valueString += valueList.get(k);
					//}
					//Log.d(TAG, "valueString: " + valueString);

					//valueString = "";
					int cntAdded = 0;
					long startAddingTimeUtcMs = 0;
					long endAddingTimeUtcMs = 0;
					for (int j = 0; j < valueList.size() - 1; j++) {
						// beginMs 보다 크거나 같다면 반환리스트에 추가할 것
						if (prevMovementInfo.startUtcTimeMs + 1000 * 10 * j >= beginMs) {
							if (startAddingTimeUtcMs == 0) startAddingTimeUtcMs = prevMovementInfo.startUtcTimeMs + 1000 * 10 * j;
							endAddingTimeUtcMs = prevMovementInfo.startUtcTimeMs + 1000 * 10 * j;
							cntAdded++;
							movementGraphInfo.add(valueList.get(j));
							//valueString += valueList.get(j);
						}
						if (prevMovementInfo.startUtcTimeMs + 1000 * 10 * j > endMs) {
							if (DBG) Log.e(TAG, "    break: " + (prevMovementInfo.startUtcTimeMs + 1000 * 10 * j) + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp((prevMovementInfo.startUtcTimeMs + 1000 * 10 * j)) + ")");
							break;
						}
					}

					if (DBG) Log.d(TAG, "    add data" + beginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(beginMs) + ")"
							+ " / stime: " + startAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startAddingTimeUtcMs) + ")"
							+ " / etime: " + endAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(endAddingTimeUtcMs) + ")"
							+ " / count: " + cntAdded
							//+ " / string: " + valueString
					);
				}
			} else {
				if (DBG) Log.e(TAG, "get data from prev row: " + deviceId + " / NULL");
			}

			// 실제 데이터 읽어오기
			tc = null;
			table = TABLE_NAME_MOVEMENT_GRAPH;
			columns = null;
			selection = "device_id=? AND starttime>=? AND starttime<=?";
			selectionArgs = new String[]{deviceId + "", beginMs + "", endMs + ""};
			groupBy = null;
			having = null;
			orderBy = "starttime asc";
			limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			MovementGraphInfo info = null;
			ArrayList<MovementGraphInfo> movementInfoList = new ArrayList<>();
			tc.moveToFirst();
			while (!tc.isAfterLast()) {
				long id = tc.getLong(0);
				long device_id = tc.getLong(1);
				long startUtcTimeMs = tc.getLong(2);
				String value = tc.getString(3);
				int count = tc.getInt(4);
				long created = tc.getLong(5);

				info = new MovementGraphInfo();
				info.deviceId = device_id;
				info.startUtcTimeMs = startUtcTimeMs;
				info.value = value.toUpperCase();
				info.count = count;
				info.created = created;
				movementInfoList.add(info);
				tc.moveToNext();
			}
			tc.close();

			// 끊어져있다가 데이터가 있는 경우(15시 이후 데이터 있는 경우), 앞쪽에 끊어진 부분을 채워줘야함
			if (movementInfoList.size() > 0 && movementGraphInfo.size() == 0) {
				long startUtcTimeMs = movementInfoList.get(0).startUtcTimeMs;
				if (startUtcTimeMs > beginMs) {
					int cntAdded = 0;
					for (int j = 0; j < 8640; j++) {
						if (beginMs + j * 10 * 1000 >= startUtcTimeMs - 10 * 1000) break;
						cntAdded++;
						movementGraphInfo.add(DeviceStatus.MOVEMENT_DISCONNECTED);
					}
					if (DBG) Log.d(TAG, "    fill disconnected data: " + deviceId
							+ " / " + startUtcTimeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startUtcTimeMs - 10 * 1000) + ") ~ "
							+ (startUtcTimeMs - 10 * 1000 + cntAdded * 10 * 1000) + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp((startUtcTimeMs - 10 * 1000 + cntAdded * 10 * 1000)) + ")");
				} else {
					if (DBG) Log.d(TAG, "do not fill disconnected");
				}
			}

			for (MovementGraphInfo graphInfo : movementInfoList) {
				if (DBG) Log.d(TAG, "get data from row: " + graphInfo.deviceId + " / " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(graphInfo.startUtcTimeMs) + " / " + graphInfo.count + " / " + graphInfo.value);
				if (valueList == null) {
					valueList = new ArrayList<>();
				} else {
					valueList.clear();
				}

				String movementValue = graphInfo.value;
				char curr = '0';
				char prev = '0';
				char duplicatedChar = '0';
				boolean findDuplicatedCountNumber = false;
				String duplicatedCount = "";
				for (int i = 0; i < movementValue.length(); i++) {
					curr = movementValue.charAt(i);

					if (curr == '(') {
						findDuplicatedCountNumber = true;
						duplicatedChar = prev;
						continue;
					} else if (curr == ')') {
						// 괄호안에 숫자 개수만큼 중복 숫자를 리스트에 넣기
						int cnt = Integer.parseInt(duplicatedCount);
						int movementLevel = DeviceStatus.getMovementLevelFromValue(duplicatedChar);
						for (int j = 0; j < cnt - 1; j++) {
							valueList.add(movementLevel);
						}
						duplicatedCount = "";
						findDuplicatedCountNumber = false;
					} else if (findDuplicatedCountNumber == true) {
						duplicatedCount += curr;
					} else {
						valueList.add(DeviceStatus.getMovementLevelFromValue(curr));
					}
					prev = curr;
				}


				String valueString = "";
				if (valueList != null && valueList.size() > 0) {
					// 부족한 수량 추가
					int insufficientCount = valueList.size() - 1;
					for (int k = 0; k < graphInfo.count - insufficientCount; k++) {
						valueList.add(0);
					}

					int cntAdded = 0;
					long startAddingTimeUtcMs = 0;
					long endAddingTimeUtcMs = 0;
					for (int j = 0; j < valueList.size() - 1; j++) {
						if ((graphInfo.startUtcTimeMs + 1000 * 10 * j >= beginMs)
								&& (graphInfo.startUtcTimeMs + 1000 * 10 * j <= endMs)) {
							if (startAddingTimeUtcMs == 0) startAddingTimeUtcMs = graphInfo.startUtcTimeMs + 1000 * 10 * j;
							endAddingTimeUtcMs = graphInfo.startUtcTimeMs + 1000 * 10 * j;
							cntAdded++;
							movementGraphInfo.add(valueList.get(j));
							valueString += valueList.get(j);
						}
					}

					if (DBG) Log.d(TAG, "    add data " + beginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(beginMs) + ")"
							+ " / stime: " + startAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startAddingTimeUtcMs) + ")"
							+ " / etime: " + endAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(endAddingTimeUtcMs) + ")"
							+ " / count: " + cntAdded
							+ " / string: " + valueString
					);
				}
			}

			if (movementInfoList.size() == 0) {
				if (DBG) Log.e(TAG, "get data from row: " + deviceId + " / NULL");
			}
//		} catch (Exception e) {
//			if (DBG) Log.e(TAG, "Exception: " + e.toString());
//		}

		return movementGraphInfo;
	}

	public ArrayList<SensorGraphInfo> getSleepingGraphInfo(long deviceId, long beginMs, long endMs) {
		int days = (int)((endMs - beginMs) / DateTimeUtil.ONE_DAY_MILLIS) + 1;
		if (DBG) Log.d(TAG, "getSleepingGraphInfo : " + deviceId + " / " + beginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(beginMs) + ") ~ " + endMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(endMs) + ")" + "=>" + days + "days");

		SensorGraphInfo[] sleepingGraphInfo = new SensorGraphInfo[days];
		for (int i = 0; i < days; i++) {
			sleepingGraphInfo[i] = new SensorGraphInfo();
		}
		long totalBeginMs = beginMs;
		long totalEndMs = endMs;

		for (int day = 0; day < days; day++) {
			beginMs = totalBeginMs + DateTimeUtil.ONE_DAY_MILLIS * day;
			endMs = totalBeginMs + DateTimeUtil.ONE_DAY_MILLIS * (day + 1) - 1;

			ArrayList<Integer> movementGraphInfo = new ArrayList<Integer>();
			int idx = 0;
			Cursor tc = null;
			//try {
			// 1. 중간에 짤렸을 수 있으므로 시작 시간 이전으로 가장 최근 1개 데이터 읽어오기
			String table = TABLE_NAME_MOVEMENT_GRAPH;
			String[] columns = null;
			String selection = "device_id=? AND starttime<?";
			String[] selectionArgs = {deviceId + "", beginMs + ""};
			String groupBy = null;
			String having = null;
			String orderBy = "starttime desc";
			String limit = "1";
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			MovementGraphInfo prevMovementInfo = null;
			tc.moveToFirst();
			while (!tc.isAfterLast()) {
				long id = tc.getLong(0);
				long device_id = tc.getLong(1);
				long startUtcTimeMs = tc.getLong(2);
				String value = tc.getString(3);
				int count = tc.getInt(4);
				long created = tc.getLong(5);

				prevMovementInfo = new MovementGraphInfo();
				prevMovementInfo.deviceId = device_id;
				prevMovementInfo.startUtcTimeMs = startUtcTimeMs;
				prevMovementInfo.value = value.toUpperCase();
				prevMovementInfo.count = count;
				prevMovementInfo.created = created;

				tc.moveToNext();
			}
			tc.close();

			ArrayList<Integer> valueList = new ArrayList<>();
			if (prevMovementInfo != null) {
				if (DBG) Log.d(TAG, "get data from prev row: " + prevMovementInfo.deviceId + " / " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(prevMovementInfo.startUtcTimeMs) + " / " + prevMovementInfo.count + " / " + prevMovementInfo.value);
				String movementValue = prevMovementInfo.value;
				char curr = '0';
				char prev = '0';
				char duplicatedChar = '0';
				boolean findDuplicatedCountNumber = false;
				String duplicatedCount = "";
				for (int i = 0; i < movementValue.length(); i++) {
					if (i >= prevMovementInfo.count) break;

					curr = movementValue.charAt(i);

					if (curr == '(') {
						findDuplicatedCountNumber = true;
						duplicatedChar = prev;
						continue;
					} else if (curr == ')') {
						// 괄호안에 숫자 개수만큼 중복 숫자를 리스트에 넣기
						int cnt = Integer.parseInt(duplicatedCount);
						int movementLevel = DeviceStatus.getMovementLevelFromValue(duplicatedChar);
						for (int j = 0; j < cnt - 1; j++) {
							valueList.add(movementLevel);
						}
						duplicatedCount = "";
						findDuplicatedCountNumber = false;
					} else if (findDuplicatedCountNumber == true) {
						duplicatedCount += curr;
					} else {
						valueList.add(DeviceStatus.getMovementLevelFromValue(curr));
					}
					prev = curr;
				}

				if (valueList != null && valueList.size() > 0) {
					// 부족한 수량 추가
					int insufficientCount = valueList.size() - 1;
					for (int k = 0; k < prevMovementInfo.count - insufficientCount; k++) {
						valueList.add(0);
					}
					//String valueString = "";
					//for (int k = 0; k < valueList.size(); k++) {
					//valueString += valueList.get(k);
					//}
					//Log.d(TAG, "valueString: " + valueString);

					//valueString = "";
					int cntAdded = 0;
					long startAddingTimeUtcMs = 0;
					long endAddingTimeUtcMs = 0;
					for (int j = 0; j < valueList.size() - 1; j++) {
						// beginMs 보다 크거나 같다면 반환리스트에 추가할 것
						if (prevMovementInfo.startUtcTimeMs + 1000 * 10 * j >= beginMs) {
							if (startAddingTimeUtcMs == 0) startAddingTimeUtcMs = prevMovementInfo.startUtcTimeMs + 1000 * 10 * j;
							endAddingTimeUtcMs = prevMovementInfo.startUtcTimeMs + 1000 * 10 * j;
							cntAdded++;
							movementGraphInfo.add(valueList.get(j));
							//valueString += valueList.get(j);
						}
						if (prevMovementInfo.startUtcTimeMs + 1000 * 10 * j > endMs) {
							if (DBG) Log.e(TAG, "    break: " + (prevMovementInfo.startUtcTimeMs + 1000 * 10 * j) + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp((prevMovementInfo.startUtcTimeMs + 1000 * 10 * j)) + ")");
							break;
						}
					}

					if (DBG) Log.d(TAG, "    add data" + beginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(beginMs) + ")"
									+ " / stime: " + startAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startAddingTimeUtcMs) + ")"
									+ " / etime: " + endAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(endAddingTimeUtcMs) + ")"
									+ " / count: " + cntAdded
							//+ " / string: " + valueString
					);
				}
			} else {
				if (DBG) Log.e(TAG, "get data from prev row: " + deviceId + " / NULL");
			}

			// 실제 데이터 읽어오기
			tc = null;
			table = TABLE_NAME_MOVEMENT_GRAPH;
			columns = null;
			selection = "device_id=? AND starttime>=? AND starttime<=?";
			selectionArgs = new String[]{deviceId + "", beginMs + "", endMs + ""};
			groupBy = null;
			having = null;
			orderBy = "starttime asc";
			limit = null;
			tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			MovementGraphInfo info = null;
			ArrayList<MovementGraphInfo> movementInfoList = new ArrayList<>();
			tc.moveToFirst();
			while (!tc.isAfterLast()) {
				long id = tc.getLong(0);
				long device_id = tc.getLong(1);
				long startUtcTimeMs = tc.getLong(2);
				String value = tc.getString(3);
				int count = tc.getInt(4);
				long created = tc.getLong(5);

				info = new MovementGraphInfo();
				info.deviceId = device_id;
				info.startUtcTimeMs = startUtcTimeMs;
				info.value = value.toUpperCase();
				info.count = count;
				info.created = created;
				movementInfoList.add(info);
				tc.moveToNext();
			}
			tc.close();

			// 끊어져있다가 데이터가 있는 경우(15시 이후 데이터 있는 경우), 앞쪽에 끊어진 부분을 채워줘야함
			if (movementInfoList.size() > 0 && movementGraphInfo.size() == 0) {
				long startUtcTimeMs = movementInfoList.get(0).startUtcTimeMs;
				if (startUtcTimeMs > beginMs) {
					int cntAdded = 0;
					for (int j = 0; j < 8640; j++) {
						if (beginMs + j * 10 * 1000 >= startUtcTimeMs - 10 * 1000) break;
						cntAdded++;
						movementGraphInfo.add(DeviceStatus.MOVEMENT_DISCONNECTED);
					}
					if (DBG) Log.d(TAG, "    fill disconnected data: " + deviceId
							+ " / " + startUtcTimeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startUtcTimeMs - 10 * 1000) + ") ~ "
							+ (startUtcTimeMs - 10 * 1000 + cntAdded * 10 * 1000) + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp((startUtcTimeMs - 10 * 1000 + cntAdded * 10 * 1000)) + ")");
				} else {
					if (DBG) Log.d(TAG, "do not fill disconnected");
				}
			}

			for (MovementGraphInfo graphInfo : movementInfoList) {
				if (DBG) Log.d(TAG, "get data from row: " + graphInfo.deviceId + " / " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(graphInfo.startUtcTimeMs) + " / " + graphInfo.count + " / " + graphInfo.value);
				if (valueList == null) {
					valueList = new ArrayList<>();
				} else {
					valueList.clear();
				}

				String movementValue = graphInfo.value;
				char curr = '0';
				char prev = '0';
				char duplicatedChar = '0';
				boolean findDuplicatedCountNumber = false;
				String duplicatedCount = "";
				for (int i = 0; i < movementValue.length(); i++) {
					curr = movementValue.charAt(i);

					if (curr == '(') {
						findDuplicatedCountNumber = true;
						duplicatedChar = prev;
						continue;
					} else if (curr == ')') {
						// 괄호안에 숫자 개수만큼 중복 숫자를 리스트에 넣기
						int cnt = Integer.parseInt(duplicatedCount);
						int movementLevel = DeviceStatus.getMovementLevelFromValue(duplicatedChar);
						for (int j = 0; j < cnt - 1; j++) {
							valueList.add(movementLevel);
						}
						duplicatedCount = "";
						findDuplicatedCountNumber = false;
					} else if (findDuplicatedCountNumber == true) {
						duplicatedCount += curr;
					} else {
						valueList.add(DeviceStatus.getMovementLevelFromValue(curr));
					}
					prev = curr;
				}

				String valueString = "";
				if (valueList != null && valueList.size() > 0) {
					// 부족한 수량 추가
					int insufficientCount = valueList.size() - 1;
					for (int k = 0; k < graphInfo.count - insufficientCount; k++) {
						valueList.add(0);
					}

					int cntAdded = 0;
					long startAddingTimeUtcMs = 0;
					long endAddingTimeUtcMs = 0;
					for (int j = 0; j < valueList.size() - 1; j++) {
						if ((graphInfo.startUtcTimeMs + 1000 * 10 * j >= beginMs)
								&& (graphInfo.startUtcTimeMs + 1000 * 10 * j <= endMs)) {
							if (startAddingTimeUtcMs == 0) startAddingTimeUtcMs = graphInfo.startUtcTimeMs + 1000 * 10 * j;
							endAddingTimeUtcMs = graphInfo.startUtcTimeMs + 1000 * 10 * j;
							cntAdded++;
							movementGraphInfo.add(valueList.get(j));
							valueString += valueList.get(j);
						}
					}

					if (DBG) Log.d(TAG, "    add data " + beginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(beginMs) + ")"
							+ " / stime: " + startAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(startAddingTimeUtcMs) + ")"
							+ " / etime: " + endAddingTimeUtcMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(endAddingTimeUtcMs) + ")"
							+ " / count: " + cntAdded
							+ " / string: " + valueString
					);
				}
			}

			// 모두 채운 후 빈 곳은 Disconnected로 채움
			int graphInfoSize = movementGraphInfo.size();
			for (int iter = graphInfoSize; iter < 8640; iter++) {
				movementGraphInfo.add(DeviceStatus.MOVEMENT_DISCONNECTED);
			}

			if (movementInfoList.size() == 0) {
				if (DBG) Log.e(TAG, "get data from row: " + deviceId + " / NULL" + " / " + graphInfoSize + " added");
			}

			sleepingGraphInfo[day].movementValues = movementGraphInfo;
//		} catch (Exception e) {
//			if (DBG) Log.e(TAG, "Exception: " + e.toString());
//		}
		}

		// 수면 데이터 가져오기
		ArrayList<Long> sleepBeginTimeMsList = new ArrayList<>();
		ArrayList<Long> sleepEndTimeMsList = new ArrayList<>();

		// 1. 중간에 짤렸을 수 있으므로 시작 시간 이전으로 가장 최근 1개 데이터 읽어오기
		Cursor tc = null;
		String table = TABLE_NAME_NOTIFICATION;
		String[] columns = null;
		String selection = "noti_type=? AND device_type=? AND device_id=? AND created<?";
		String[] selectionArgs = {NotificationType.BABY_SLEEP + "", DeviceType.DIAPER_SENSOR + "", deviceId + "", totalBeginMs + ""};
		String groupBy = null;
		String having = null;
		String orderBy = "created desc";
		String limit = "1";

		tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

		NotificationMessage latestSleepMessage = null;
		// 가장 최근 Sleep 메시지 확인
		tc.moveToFirst();
		while (!tc.isAfterLast()) {
			long msgId = tc.getLong(0);
			int noti_type = tc.getInt(1);
			int device_type = tc.getInt(2);
			long device_id = tc.getLong(3);
			String extra = tc.getString(4);
			long created = tc.getLong(5);
			long server_noti_id = tc.getLong(6);
			latestSleepMessage = new NotificationMessage(msgId, noti_type, device_type, device_id, extra, created, server_noti_id);

			if (DBG) Log.d(TAG, "Sleep found(1): " + created + " / " + extra);
			// 가장 최근 Sleep메시지가 없으면 넘어감
			// 가장 최근 Sleep메시지가 있으면, 종료시간을 파악한 뒤 종료시간이 걸치게 되면 그 때만큼 시간을 추가해줌
			// 종료시간이 걸치지 않으면 그대로 넘어감
			if (latestSleepMessage != null) {
				if ((latestSleepMessage != null) && !("-".equals(latestSleepMessage.extra))) {
					try {
						Date date_created_time = new SimpleDateFormat(ServerQueryManager.getInstance(mContext).getParameter(1)).parse(latestSleepMessage.extra);
						long endSleepUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴

						if (endSleepUtcTimeMs > totalBeginMs) {
							sleepBeginTimeMsList.add(totalBeginMs);
							sleepEndTimeMsList.add(endSleepUtcTimeMs);
							if (DBG) Log.d(TAG, "Sleep Added(Start): " + sleepBeginTimeMsList + " ~ " + sleepEndTimeMsList);
						}
					} catch (Exception e) {
						if (DBG) Log.d(TAG, "Exception: " + e.toString());
					}
				}
			}
			tc.moveToNext();
		}
		tc.close();

		// 실제 데이터 읽어오기
		tc = null;
		table = TABLE_NAME_NOTIFICATION;
		columns = null;
		selection = "noti_type=? AND device_type=? AND device_id=? AND created>=? AND created<=?";
		selectionArgs = new String[] {NotificationType.BABY_SLEEP + "", DeviceType.DIAPER_SENSOR + "", deviceId + "", totalBeginMs + "", totalEndMs + ""};
		groupBy = null;
		having = null;
		orderBy = "created asc";
		limit = null;
		tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		tc.moveToFirst();
		while (!tc.isAfterLast()) {
			long msgId = tc.getLong(0);
			int noti_type = tc.getInt(1);
			int device_type = tc.getInt(2);
			long device_id = tc.getLong(3);
			String extra = tc.getString(4);
			long created = tc.getLong(5);
			long server_noti_id = tc.getLong(6);
			latestSleepMessage = new NotificationMessage(msgId, noti_type, device_type, device_id, extra, created, server_noti_id);

			if (DBG) Log.d(TAG, "Sleep found(2): " + created + " / " + extra);
			if (latestSleepMessage != null) {
				if ((latestSleepMessage != null) && !("-".equals(latestSleepMessage.extra))) {
					try {
						Date date_created_time = new SimpleDateFormat(ServerQueryManager.getInstance(mContext).getParameter(1)).parse(latestSleepMessage.extra);
						long endSleepUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date_created_time.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴

						// 끝나는 시간이 end시간보다 크면
						// end시간을 끝나는 시간으로 설정
						if (endSleepUtcTimeMs > totalEndMs) {
							sleepBeginTimeMsList.add(created);
							sleepEndTimeMsList.add(totalEndMs);
							if (DBG) Log.d(TAG, "Sleep Added(End): " + created + " ~ " + totalEndMs);
						} else {
							sleepBeginTimeMsList.add(created);
							sleepEndTimeMsList.add(endSleepUtcTimeMs);
							if (DBG) Log.d(TAG, "Sleep Added(Normal): " + created + " ~ " + endSleepUtcTimeMs);
						}
					} catch (Exception e) {
						if (DBG) Log.d(TAG, "Exception: " + e.toString());
					}
				}
			}
			tc.moveToNext();
		}
		tc.close();

		if (DBG) Log.d(TAG, "Check list size: " + sleepBeginTimeMsList.size() + " / " + sleepEndTimeMsList.size());

		StringBuilder sbNoneSleepForDays = new StringBuilder();
		for (int day = 0; day < days; day++) {
			for (int i = 0; i < 60 * 24; i++) {
				sbNoneSleepForDays.append('0');
			}
		}
		for (int i = 0; i < sleepBeginTimeMsList.size(); i++) {
			int beginTimeSec = (int)(sleepBeginTimeMsList.get(i) / 1000);
			int endTimeSec = (int)(sleepEndTimeMsList.get(i) / 1000);

			int startIdx = (int)(beginTimeSec - totalBeginMs / 1000) / 60; // 60초마다 index 1증가하므로
			for (int j = beginTimeSec; j < endTimeSec; j = j + 60) {
				sbNoneSleepForDays.setCharAt(startIdx, '1');
				startIdx++;
			}
		}

		for (int day = 0; day < days; day++) {
			int startIdx = day * 60 * 24;
			int endIdx = startIdx + 60 * 24;
			String strDay = sbNoneSleepForDays.substring(startIdx, endIdx);

			ArrayList<Integer> sleepForADay = new ArrayList<>();
			for (int i = 0; i < strDay.length(); i++) {
				if (strDay.charAt(i) == '0') {
					sleepForADay.add(0);
					sleepForADay.add(0);
					sleepForADay.add(0);
					sleepForADay.add(0);
					sleepForADay.add(0);
					sleepForADay.add(0);
				} else if (strDay.charAt(i) == '1') {
					sleepForADay.add(1);
					sleepForADay.add(1);
					sleepForADay.add(1);
					sleepForADay.add(1);
					sleepForADay.add(1);
					sleepForADay.add(1);
				}
			}
			if (DBG) Log.d(TAG, "insert day/data: " + day + " / " + sleepForADay.size());
			sleepingGraphInfo[day].sleepingValues = sleepForADay;
		}

		// 움직임 알람 가져오기
		tc = null;
		table = TABLE_NAME_NOTIFICATION;
		columns = null;
		selection = "noti_type=? AND device_type=? AND device_id=? AND created>=? AND created<=?";
		selectionArgs = new String[] {NotificationType.MOVEMENT_DETECTED + "", DeviceType.DIAPER_SENSOR + "", deviceId + "", totalBeginMs + "", totalEndMs + ""};
		groupBy = null;
		having = null;
		orderBy = "created asc";
		limit = null;
		tc = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		tc.moveToFirst();
		while (!tc.isAfterLast()) {
			long msgId = tc.getLong(0);
			int noti_type = tc.getInt(1);
			int device_type = tc.getInt(2);
			long device_id = tc.getLong(3);
			String extra = tc.getString(4);
			long created = tc.getLong(5);
			long server_noti_id = tc.getLong(6);

			int day = (int)((created - totalBeginMs) / 1000) / 60 / 60 / 24;
			sleepingGraphInfo[day].cntMovementDetected++;
			tc.moveToNext();
		}
		tc.close();

		ArrayList<SensorGraphInfo> sensorGraphInfoList = new ArrayList<>();
		for (int day = 0; day < days; day++) {
			SensorGraphInfo info = new SensorGraphInfo();
			info.movementValues = sleepingGraphInfo[day].movementValues;
			info.sleepingValues = sleepingGraphInfo[day].sleepingValues;
			info.cntMovementDetected = sleepingGraphInfo[day].cntMovementDetected;
			if (DBG) Log.d(TAG, "count day/data: " + day + " / " + sleepingGraphInfo[day].movementValues.size() + " / " + sleepingGraphInfo[day].sleepingValues.size() + " / " + sleepingGraphInfo[day].cntMovementDetected);
			sensorGraphInfoList.add(info);
		}

		return sensorGraphInfoList;
	}
	//endregion

	//region "Device"
	public void checkDeviceDB() {
		Cursor c = null;
		if (DBG) Log.d(TAG, "=============== Device Database Values ===============");
		try {
			//mSQLiteDB = mDBHelper.getReadableDatabase();
			//c = mSQLiteDB.rawQuery("SELECT * FROM device_enc", null);

			String table = TABLE_NAME_DEVICE_ENC;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			c.moveToFirst();
			String strTemp;
			String log = "";
			while (!c.isAfterLast()) {
				log = "";
				strTemp = c.getString(1);
				log += "[" + mEncryptionMgr.getLocalDecryptedString(strTemp) + "] ";
				strTemp = c.getString(2);
				log += " / " + mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(3);
				log += " / " + mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(4);
				log += " / " + mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(5);
				log += " / " + mEncryptionMgr.getLocalDecryptedString(strTemp);

				if (DBG) Log.d(TAG, log);
				c.moveToNext();
			}
		} catch (Exception e) {
			if (DBG) Log.e(TAG, "Exception : " + e);
		}

		if (c != null) c.close();
	}

	public ArrayList<DeviceInfo> getDeviceInfoList() {
		ArrayList<DeviceInfo> infoList = new ArrayList<>();

		Cursor c = null;
		try {
            //mSQLiteDB = mDBHelper.getReadableDatabase();
			//c = mSQLiteDB.rawQuery("SELECT * FROM device_enc", null);

			if (DBG) Log.d(TAG, "[getRegisteredDeviceInfo]");
			String table = TABLE_NAME_DEVICE_ENC;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

            if (c == null) return null;
			c.moveToFirst();
			String strTemp;
			while(!c.isAfterLast()) {
				strTemp = c.getString(1);
				long deviceId = Long.parseLong(mEncryptionMgr.getLocalDecryptedString(strTemp));
				strTemp = c.getString(2);
				long cloudId = Long.parseLong(mEncryptionMgr.getLocalDecryptedString(strTemp));
				strTemp = c.getString(3);
				int type = Integer.parseInt(mEncryptionMgr.getLocalDecryptedString(strTemp));
				strTemp = c.getString(4);
				String name = mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(5);
				String macaddr = mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(6);
				String serial = mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(7);
				String firmware = mEncryptionMgr.getLocalDecryptedString(strTemp);
				strTemp = c.getString(8);
				boolean alarm1 = mEncryptionMgr.getLocalDecryptedString(strTemp) == "1" ? true : false;
				strTemp = c.getString(9);
				boolean alarm2 = mEncryptionMgr.getLocalDecryptedString(strTemp) == "1" ? true : false;
				strTemp = c.getString(10);
				boolean alarm3 = mEncryptionMgr.getLocalDecryptedString(strTemp) == "1" ? true : false;
				strTemp = c.getString(11);
				boolean alarm4 = mEncryptionMgr.getLocalDecryptedString(strTemp) == "1" ? true : false;
				strTemp = c.getString(12);
				boolean alarm5 = mEncryptionMgr.getLocalDecryptedString(strTemp) == "1" ? true : false;

				DeviceInfo info = new DeviceInfo(deviceId, cloudId, type, name, macaddr, serial, firmware, null, alarm1, alarm2, alarm3, alarm4, alarm5);
				infoList.add(info);
				if (DBG) Log.d(TAG, info.toString());
				c.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, "Exception : " + e);
			if (c != null) c.close();
			return null;
		}
		if (c != null) c.close();

		return infoList;
	}

	public void moveDatabase() {
		Cursor c = null;
		try {
			//mSQLiteDB = mDBHelper.getReadableDatabase();
			//c = mSQLiteDB.rawQuery("SELECT * FROM device", null);
			if (DBG) Log.d(TAG, "[moveDatabase]");
			String table = TABLE_NAME_DEVICE;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			if (c == null) return;
			c.moveToFirst();
			while(!c.isAfterLast()) {
				long deviceId = c.getLong(1);
				long cloudId = c.getLong(2);
				int type = c.getInt(3);
				String name = c.getString(4);
				String macaddr = c.getString(5);
				String serial = c.getString(6);
				String firmware = c.getString(7);
				boolean alarm1 = c.getInt(8) == 1;
				boolean alarm2 = c.getInt(9) == 1;
				boolean alarm3 = c.getInt(10) == 1;
				boolean alarm4 = c.getInt(11) == 1;
				boolean alarm5 = c.getInt(12) == 1;

				DeviceInfo info = new DeviceInfo(deviceId, cloudId, type, name, macaddr, serial, firmware, null, alarm1, alarm2, alarm3, alarm4, alarm5);

				insertDB(info);
				if (DBG) Log.d(TAG, info.toString());
				c.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, "Exception : " + e);
		}
		if (c != null) c.close();
	}

	private ContentValues _getContentValues(DeviceInfo deviceInfo) {
		if (deviceInfo == null) {
			return null;
		}
		ContentValues values = new ContentValues();
		try {
			values.put("deviceid", mEncryptionMgr.getLocalEncryptedString(deviceInfo.deviceId + ""));
			values.put("cloudid", mEncryptionMgr.getLocalEncryptedString(deviceInfo.cloudId + ""));
			values.put("type", mEncryptionMgr.getLocalEncryptedString(deviceInfo.type + ""));
			values.put("name", mEncryptionMgr.getLocalEncryptedString(deviceInfo.name));
			values.put("macaddr", mEncryptionMgr.getLocalEncryptedString(deviceInfo.btmacAddress));
			values.put("serial", mEncryptionMgr.getLocalEncryptedString(deviceInfo.serial));
			values.put("firmware", mEncryptionMgr.getLocalEncryptedString(deviceInfo.firmwareVersion));
			values.put("alarm1", mEncryptionMgr.getLocalEncryptedString((deviceInfo.enabledAlarm1 ? 1 : 0) + ""));
			values.put("alarm2", mEncryptionMgr.getLocalEncryptedString((deviceInfo.enabledAlarm2 ? 1 : 0) + ""));
			values.put("alarm3", mEncryptionMgr.getLocalEncryptedString((deviceInfo.enabledAlarm3 ? 1 : 0) + ""));
			values.put("alarm4", mEncryptionMgr.getLocalEncryptedString((deviceInfo.enabledAlarm4 ? 1 : 0) + ""));
			values.put("alarm5", mEncryptionMgr.getLocalEncryptedString((deviceInfo.enabledAlarm5 ? 1 : 0) + ""));
			values.put("lastupdate", mEncryptionMgr.getLocalEncryptedString(System.currentTimeMillis() + ""));
		} catch (Exception e) {
			if (DBG) e.printStackTrace();
		}

		return values;
	}

	public int deleteDB(DeviceInfo deviceInfo) {
		if (deviceInfo == null) {
			return -1;
		}

		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			//String sql = "delete from  WHERE deviceid = \"" + mEncryptionMgr.getLocalEncryptedString(deviceInfo.deviceId + "") + "\" and type = \"" + mEncryptionMgr.getLocalEncryptedString(deviceInfo.type + "") + "\"";
			//mSQLiteDB.execSQL(sql);
			mSQLiteDB.delete(TABLE_NAME_DEVICE_ENC, "deviceid=? AND type=?", new String[]{mEncryptionMgr.getLocalEncryptedString(deviceInfo.deviceId + ""), mEncryptionMgr.getLocalEncryptedString(deviceInfo.type + "")});
			if (DBG) Log.d(TAG, "deleteDB : ");
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			return -1;
		}

		checkDeviceDB();
		return 1;
	}

	public int insertDB(DeviceInfo deviceInfo) {
		if (deviceInfo == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
        try {
            //mSQLiteDB = mDBHelper.getWritableDatabase();
            //c = mSQLiteDB.rawQuery("SELECT * FROM device_enc WHERE deviceid = \"" + mEncryptionMgr.getLocalEncryptedString(deviceInfo.deviceId + "") + "\" AND type = \"" + mEncryptionMgr.getLocalEncryptedString(deviceInfo.type + "") + "\"", null);

			String table = TABLE_NAME_DEVICE_ENC;
			String[] columns = null;
			String selection = "deviceid=? AND type=?";
			String[] selectionArgs = {mEncryptionMgr.getLocalEncryptedString(deviceInfo.deviceId + ""), mEncryptionMgr.getLocalEncryptedString(deviceInfo.type + "")};
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			c.moveToFirst();
            while(!c.isAfterLast()) {
                c.close();
				checkDeviceDB();
				if (DBG) Log.d(TAG, "deviceInfo already existed : " + deviceInfo.toString());
                return -1;
            }
            ContentValues cv = _getContentValues(deviceInfo);
            regId = mSQLiteDB.insert(TABLE_NAME_DEVICE_ENC, null, cv);
            if (DBG) Log.d(TAG, "deviceInfo insert succeeded : " + deviceInfo.toString());
        } catch (Exception e) {
			if (DBG) {
				Log.d(TAG, "deviceInfo insert failed : " + e.toString());
				e.printStackTrace();
			}
        }
        if (c != null) c.close();
        checkDeviceDB();
        return (int)regId;
	}

	public int updateDB(DeviceInfo deviceInfo) {
		if (deviceInfo == null) {
			return -1;
		}
		try {
			//mSQLiteDB = mDBHelper.getWritableDatabase();
			mSQLiteDB.update(TABLE_NAME_DEVICE_ENC, _getContentValues(deviceInfo), "type=? AND deviceid=?", new String[]{mEncryptionMgr.getLocalEncryptedString(deviceInfo.type + ""), mEncryptionMgr.getLocalEncryptedString(deviceInfo.deviceId + "")});
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			return -1;
		}
		checkDeviceDB();
		return 1;
	}

	public void initDeviceDB() {
		if (DBG) Log.e(TAG, "initDeviceDB");
		//mSQLiteDB = mDBHelper.getWritableDatabase();
		mSQLiteDB.delete(TABLE_NAME_DEVICE_ENC, null, null);
	}
	//endregion

	private ContentValues _getContentValues(ScreenInfo screenInfo) {
		if (screenInfo == null) {
			return null;
		}
		ContentValues values = new ContentValues();
		try {
			values.put("screentype", screenInfo.screenType);
			values.put("intype", screenInfo.inType);
			values.put("outtype", screenInfo.outType);
			values.put("intime", screenInfo.inUtcTimeStampMs);
			values.put("outtime", screenInfo.outUtcTimeStampMs);
		} catch (Exception e) {
			if (DBG) e.printStackTrace();
		}

		return values;
	}

	public int insertDB(ScreenInfo screenInfo) {
		if (screenInfo == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
		try {
			ContentValues cv = _getContentValues(screenInfo);
			regId = mSQLiteDB.insert(TABLE_NAME_SCREEN_ANALYTICS, null, cv);
			if (DBG) Log.d(TAG, TABLE_NAME_SCREEN_ANALYTICS + " insert succeeded : " + screenInfo.toString());
		} catch (Exception e) {
			if (DBG) {
				Log.d(TAG, TABLE_NAME_SCREEN_ANALYTICS + " insert failed : " + e.toString());
				e.printStackTrace();
			}
		}
		if (c != null) c.close();
		return (int)regId;
	}

	public int deleteDB(ScreenInfo screenInfo) {
		try {
			mSQLiteDB.delete(TABLE_NAME_SCREEN_ANALYTICS, "_id =?", new String[]{ screenInfo.id + "" });
			if (DBG) Log.d(TAG, "deleteDB succeeded ");
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			return -1;
		}
		return 1;
	}

	public int initScreenInfo() {
		try {
			mSQLiteDB.delete(TABLE_NAME_SCREEN_ANALYTICS, "outtime <=?", new String[]{ System.currentTimeMillis() + "" });
			if (DBG) Log.d(TAG, "initDB succeeded ");
		} catch (Exception e) {
			if (DBG) Log.e(TAG, e.toString());
			return -1;
		}
		return 1;
	}

	public ArrayList<ScreenInfo> getScreenInfo() {
		ArrayList<ScreenInfo> infoList = new ArrayList<>();

		Cursor c = null;
		try {
			String table = TABLE_NAME_SCREEN_ANALYTICS;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			if (c == null) return null;
			c.moveToFirst();
			while(!c.isAfterLast()) {
				int id = c.getInt(0);
				int screenType = c.getInt(1);
				int inType = c.getInt(2);
				int outType = c.getInt(3);
				long inTimeMs = c.getLong(4);
				long outTimeMs = c.getLong(5);

				ScreenInfo info = new ScreenInfo(id, screenType, inType, outType, inTimeMs, outTimeMs);
				infoList.add(info);
				if (DBG) Log.d(TAG, info.toString());
				c.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, "Exception : " + e);
			if (c != null) c.close();
			return null;
		}
		if (c != null) c.close();

		return infoList;
	}

	public int deleteDB(SensingData data) {
		try {
			mSQLiteDB.delete(TABLE_NAME_SENSOR_DATA, "timeMs=?", new String[]{ data.timeMs + "" });
			if (DBG) Log.d(TAG, "deleteDB succeeded : " + data.timeMs);
		} catch (Exception e) {
			if (DBG) Log.e(TAG, "deleteDB failed : " + e.toString());
			return -1;
		}
		return 1;
	}

	public int deleteDB(ElderlySensingData data) {
		try {
			mSQLiteDB.delete(TABLE_NAME_ELDERLY_SENSOR_DATA, "timeMs=?", new String[]{ data.timeMs + "" });
			if (DBG) Log.d(TAG, "deleteDB succeeded : " + data.timeMs);
		} catch (Exception e) {
			if (DBG) Log.e(TAG, "deleteDB failed : " + e.toString());
			return -1;
		}
		return 1;
	}

	public int insertDB(SensingData data) {
		if (data == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
		try {
			ContentValues cv = _getContentValues(data);
			regId = mSQLiteDB.insert(TABLE_NAME_SENSOR_DATA, null, cv);
			if (DBG) Log.d(TAG, TABLE_NAME_SENSOR_DATA + " insert succeeded : " + data.toString());
		} catch (Exception e) {
			if (DBG) {
				Log.d(TAG, TABLE_NAME_SENSOR_DATA + " insert failed : " + e.toString());
				e.printStackTrace();
			}
		}
		if (c != null) c.close();
		return (int)regId;
	}

	public int insertDB(ElderlySensingData data) {
		if (data == null) {
			return -1;
		}
		long regId = -1;
		Cursor c = null;
		try {
			ContentValues cv = _getContentValues(data);
			regId = mSQLiteDB.insert(TABLE_NAME_ELDERLY_SENSOR_DATA, null, cv);
			if (DBG) Log.d(TAG, TABLE_NAME_ELDERLY_SENSOR_DATA + " insert succeeded : " + data.toString());
		} catch (Exception e) {
			if (DBG) {
				Log.d(TAG, TABLE_NAME_ELDERLY_SENSOR_DATA + " insert failed : " + e.toString());
				e.printStackTrace();
			}
		}
		if (c != null) c.close();
		return (int)regId;
	}

	public ArrayList<SensingData> getSensingDataList() {
		ArrayList<SensingData> infoList = new ArrayList<>();

		Cursor c = null;
		try {
			String table = TABLE_NAME_SENSOR_DATA;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = "2";
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			if (c == null) return null;
			c.moveToFirst();
			while(!c.isAfterLast()) {
				int id = c.getInt(0);
				int deviceId = c.getInt(1);
				String temperature = c.getString(2);
				String humidity = c.getString(3);
				String voc = c.getString(4);
				String capacitance = c.getString(5);
				String acceleration = c.getString(6);
				String sensorstatus = c.getString(7);
				String movementlevel = c.getString(8);
				String ethanol = c.getString(9);
				String co2 = c.getString(10);
				String pressure = c.getString(11);
				String compgas = c.getString(12);
				long timeMs = c.getLong(13);

				SensingData info = new SensingData();
				info.deviceId = deviceId;
				info.temperature = temperature;
				info.humidity = humidity;
				info.voc = voc;
				info.capacitance = capacitance;
				info.acceleration = acceleration;
				info.sensorstatus = sensorstatus;
				info.movementlevel = movementlevel;
				info.ethanol = ethanol;
				info.co2 = co2;
				info.pressure = pressure;
				info.compgas = compgas;
				info.timeMs = timeMs;
				infoList.add(info);
				if (DBG) Log.d(TAG, info.toString());
				c.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, "Exception : " + e);
			if (c != null) c.close();
			return null;
		}
		if (c != null) c.close();

		return infoList;
	}

	public ArrayList<ElderlySensingData> getElderlySensingDataList() {
		ArrayList<ElderlySensingData> infoList = new ArrayList<>();

		Cursor c = null;
		try {
			String table = TABLE_NAME_ELDERLY_SENSOR_DATA;
			String[] columns = null;
			String selection = null;
			String[] selectionArgs = null;
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			c = mSQLiteDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			if (c == null) return null;
			c.moveToFirst();
			while(!c.isAfterLast()) {
				int id = c.getInt(0);
				int deviceId = c.getInt(1);
				String temperature = c.getString(2);
				String humidity = c.getString(3);
				String voc = c.getString(4);
				String capacitance = c.getString(5);
				String acceleration = c.getString(6);
				String sensorstatus = c.getString(7);
				String movementlevel = c.getString(8);
				String ethanol = c.getString(9);
				String co2 = c.getString(10);
				String pressure = c.getString(11);
				String compgas = c.getString(12);
				String touchch1 = c.getString(13);
				String touchch2 = c.getString(14);
				String touchch3 = c.getString(15);
				String touchch4 = c.getString(16);
				String touchch5 = c.getString(17);
				String touchch6 = c.getString(18);
				String touchch7 = c.getString(19);
				String touchch8 = c.getString(20);
				String touchch9 = c.getString(21);
				long timeMs = c.getLong(22);

				ElderlySensingData info = new ElderlySensingData();
				info.deviceId = deviceId;
				info.temperature = temperature;
				info.humidity = humidity;
				info.voc = voc;
				info.capacitance = capacitance;
				info.acceleration = acceleration;
				info.sensorstatus = sensorstatus;
				info.movementlevel = movementlevel;
				info.ethanol = ethanol;
				info.co2 = co2;
				info.pressure = pressure;
				info.compgas = compgas;
				info.touch_ch1 = touchch1;
				info.touch_ch2 = touchch2;
				info.touch_ch3 = touchch3;
				info.touch_ch4 = touchch4;
				info.touch_ch5 = touchch5;
				info.touch_ch6 = touchch6;
				info.touch_ch7 = touchch7;
				info.touch_ch8 = touchch8;
				info.touch_ch9 = touchch9;
				info.timeMs = timeMs;
				infoList.add(info);
				if (DBG) Log.d(TAG, info.toString());
				c.moveToNext();
			}
		} catch(Exception e) {
			if (DBG) Log.e(TAG, "Exception : " + e);
			if (c != null) c.close();
			return null;
		}
		if (c != null) c.close();

		return infoList;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		private final String SQL_CREATE_TABLE_REGISTERED_DEVICE_ENC = "CREATE TABLE device_enc (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"deviceid STRING, " +
				"cloudid STRING, " +
				"type STRING, " +
				"name STRING, " +
				"macaddr STRING, " +
				"serial STRING, " +
				"firmware STRING, " +
				"alarm1 STRING, " +
				"alarm2 STRING, " +
				"alarm3 STRING, " +
				"alarm4 STRING, " +
				"alarm5 STRING, " +
				"lastupdate STRING)";

		private final String SQL_CREATE_TABLE_NOTIFICATION = "CREATE TABLE notification (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"noti_type INTEGER, " +
				"device_type INTEGER, " +
				"device_id INTEGER, " +
				"extra STRING, " +
				"created INTEGER," +
				"server_noti_id INTEGER" +
				")";

		private final String SQL_CREATE_TABLE_HUB_GRAPH = "CREATE TABLE hubgraph (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"device_id INTEGER, " +
				"temperature INTEGER, " +
				"humidity INTEGER, " +
				"voc INTEGER, " +
				"score INTEGER, " +
				"created INTEGER" +
				")";

		private final String SQL_CREATE_TABLE_SCREEN_ANALYTICS = "CREATE TABLE screenanalytics (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"screentype INTEGER, " +
				"intype INTEGER, " +
				"outtype INTEGER, " +
				"intime INTEGER, " +
				"outtime INTEGER, " +
				"created INTEGER" +
				")";

		private final String SQL_CREATE_TABLE_MOVEMENT_GRAPH = "CREATE TABLE movementgraph (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"device_id INTEGER, " +
				"starttime INTEGER, " +
				"value STRING, " +
				"count INTEGER, " +
				"created INTEGER" +
				")";

		private final String SQL_CREATE_TABLE_LAMP_GRAPH = "CREATE TABLE lampgraph (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"device_id INTEGER, " +
				"temperature INTEGER, " +
				"humidity INTEGER, " +
				"voc INTEGER, " +
				"score INTEGER, " +
				"created INTEGER" +
				")";

		private final String SQL_CREATE_TABLE_SENSOR_DATA2 = "CREATE TABLE sensordata2 (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"device_id INTEGER, " +
				"temperature STRING, " +
				"humidity STRING, " +
				"voc STRING, " +
				"capacitance STRING, " +
				"acceleration STRING," +
				"sensorstatus STRING," +
				"movementlevel STRING," +
				"ethanol STRING," +
				"co2 STRING," +
				"pressure STRING," +
				"compgas STRING," +
				"timeMs INTEGER" +
				")";

		private final String SQL_CREATE_TABLE_ELDERLY_SENSOR_DATA = "CREATE TABLE elderlysensordata (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"device_id INTEGER, " +
				"temperature STRING, " +
				"humidity STRING, " +
				"voc STRING, " +
				"capacitance STRING, " +
				"acceleration STRING," +
				"sensorstatus STRING," +
				"movementlevel STRING," +
				"ethanol STRING," +
				"co2 STRING," +
				"pressure STRING," +
				"compgas STRING," +
				"touchch1 STRING," +
				"touchch2 STRING," +
				"touchch3 STRING," +
				"touchch4 STRING," +
				"touchch5 STRING," +
				"touchch6 STRING," +
				"touchch7 STRING," +
				"touchch8 STRING," +
				"touchch9 STRING," +
				"timeMs INTEGER" +
				")";

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (DBG) Log.i(TAG, "onCreate DB");
			db.execSQL(SQL_CREATE_TABLE_REGISTERED_DEVICE_ENC);
			db.execSQL(SQL_CREATE_TABLE_NOTIFICATION);
			db.execSQL(SQL_CREATE_TABLE_HUB_GRAPH);
			db.execSQL(SQL_CREATE_TABLE_SCREEN_ANALYTICS);
			db.execSQL(SQL_CREATE_TABLE_MOVEMENT_GRAPH);
			db.execSQL(SQL_CREATE_TABLE_LAMP_GRAPH);
			db.execSQL(SQL_CREATE_TABLE_SENSOR_DATA2);
			db.execSQL(SQL_CREATE_TABLE_ELDERLY_SENSOR_DATA);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (DBG) Log.i(TAG, "onUpgrade : " + oldVersion + " > " + newVersion);
			if (oldVersion < 3) {
				db.execSQL(SQL_CREATE_TABLE_NOTIFICATION);
			}
			if (oldVersion < 4) {
				db.execSQL(SQL_CREATE_TABLE_HUB_GRAPH);
			}
			if (oldVersion < 5) {
				db.execSQL(SQL_CREATE_TABLE_REGISTERED_DEVICE_ENC);
				db.execSQL("drop table if exists device");
				db.execSQL("drop table if exists userinfo");
				db.execSQL("drop table if exists groupinfo");
			}
			if (oldVersion < 6) { // server_noti_id 추가
				db.execSQL("ALTER TABLE notification ADD COLUMN server_noti_id INT");
			}
			if (oldVersion < 7) {
				db.execSQL(SQL_CREATE_TABLE_SCREEN_ANALYTICS);
			}
			if (oldVersion < 8) {
				db.execSQL(SQL_CREATE_TABLE_MOVEMENT_GRAPH);
			}
			if (oldVersion < 9) {
				db.execSQL(SQL_CREATE_TABLE_LAMP_GRAPH);
			}
			if (oldVersion < 11) {
				db.execSQL(SQL_CREATE_TABLE_SENSOR_DATA2);
			}
			if (oldVersion < 12) {
				db.execSQL(SQL_CREATE_TABLE_ELDERLY_SENSOR_DATA);
			}
		}
	}
}