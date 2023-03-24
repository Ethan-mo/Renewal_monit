package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.Pushy;
import goodmonit.monit.com.kao.activity.NuguActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceElderlyDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.PushMessage;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class PushManager {
	private static final String TAG = Configuration.BASE_TAG + "PushMgr";
	private static final boolean DBG = Configuration.DBG;

	public static final int PUSH_INIT = -1;
	public static final int PUSH_FCM = 0;
	public static final int PUSH_PUSHY = 1;
	public static final int PUSH_SERVICE_TYPE = PUSH_FCM;

	private static Context mContext;
	private static PushManager mInstance;
	private static PreferenceManager mPreferenceMgr;
	private static NotiManager mNotiMgr;
	private static DebugManager mDebugMgr;
	private ConnectionManager mConnectionMgr;
	private sm mStringMgr;
	private boolean alreadyStartService = false;

	public static PushManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PushManager(context);
		} else {
			mContext = context;
		}
		return mInstance;
	}

	public PushManager(Context context) {
		mContext = context;
		mStringMgr = new sm();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mNotiMgr = NotiManager.getInstance(mContext);
		mDebugMgr = DebugManager.getInstance(mContext);
		mConnectionMgr = ConnectionManager.getInstance();

		if (ConnectionManager.getInstance(null) == null) {
			if (DBG) Log.e(TAG, "start service1");
			alreadyStartService = true;
			mContext.startService(new Intent(mContext, ConnectionManager.class));
		}
	}

	public void checkPushUpdated() {

		if (PUSH_SERVICE_TYPE == PUSH_FCM) {
			// FCM 확인
			String token = FirebaseInstanceId.getInstance().getToken();
			if (token != null && !token.equals(mPreferenceMgr.getPushToken())) {
				if (DBG) Log.i(TAG, "Token Updated");
				mPreferenceMgr.setPushToken(token);
				mPreferenceMgr.setNeedToUpdatePushToken(PUSH_FCM);
			}
		} else if (PUSH_SERVICE_TYPE == PUSH_PUSHY) {
			// PUSHY 확인
			new RegisterForPushNotificationsAsync().execute();
		}

		// 업데이트 된 경우 서버에 전송
		if (mPreferenceMgr.getNeedToUpdatePushToken() > PUSH_INIT) {
			ServerQueryManager.getInstance(mContext).updatePushToken();
		}
	}

	private class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Exception> {
		protected Exception doInBackground(Void... params) {
			try {
				// Assign a unique token to this device
				String deviceToken = Pushy.register(mContext);

				String savedToken = mPreferenceMgr.getPushToken();
				if (deviceToken != null) {
					if (!savedToken.equals(deviceToken)) {
						mPreferenceMgr.setPushToken(deviceToken);
						mPreferenceMgr.setNeedToUpdatePushToken(PUSH_PUSHY);
						ServerQueryManager.getInstance(mContext).updatePushToken(); // PushyType
					}
				}
				Pushy.listen(mContext);

				// Log it for debugging purposes
				if (DBG) Log.d(TAG, "Pushy device token: " + deviceToken);

				// Send the token to your backend server via an HTTP GET request
				//new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();
			}
			catch (Exception exc) {
				// Return exc to onPostExecute
				return exc;
			}

			// Success
			return null;
		}

		@Override
		protected void onPostExecute(Exception exc) {
			// Failed?
			if (exc != null) {
				// Show error as toast message
				if (DBG) Log.e(TAG, "pushy failed");
				return;
			}

			// Succeeded, do something to alert the user
		}
	}

	public void onMessageReceived(String msg) {
		if (msg == null || msg.length() == 0) return;
		long now = System.currentTimeMillis();

		mConnectionMgr = ConnectionManager.getInstance();

		int notiType = 0;
		int deviceType = 0;
		long deviceId = 0;
		String extra = null;
		long timeMs = 0;

		try {
			JSONObject jobj = new JSONObject(msg);
			notiType = jobj.optInt(mStringMgr.getParameter(38), -1);
			deviceType = jobj.optInt(mStringMgr.getParameter(28), -1);
			deviceId = jobj.optLong(mStringMgr.getParameter(26), -1);
			extra = jobj.optString(mStringMgr.getParameter(37), null);
			timeMs = jobj.optLong(mStringMgr.getParameter(15), now);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (DBG) Log.d(TAG, "received : " + notiType + " / " + deviceType + " / " + deviceId + " / " + extra + " / " + timeMs);
        //if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("push: " + notiType + " / " + deviceType + " / " + deviceId + " / " + extra + " / " + timeMs);

		if (Configuration.FAST_DETECTION) {
			if (DBG) Log.e(TAG, "FAST DETECTION, ignore push");
            //if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr1");
			return;
		}

		if (notiType == -1 || deviceType == -1 || deviceId == -1) {
            //if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr2");
			return;
		}

		// 푸시서버 연결문제로 밀렸다가 한꺼번에 들어오는 이슈있음.
		// 3초 이내에 들어오는 알람은 폭탄메시지로 간주하고 무시
		if (now - mPreferenceMgr.getLatestPushDataTime(notiType) > 3 * 1000) {
			mPreferenceMgr.setLatestPushDataTime(notiType, now);
		} else {
			if (DBG) Log.e(TAG, "avoid bomb messages : " + notiType + " / " + (now - mPreferenceMgr.getLatestPushDataTime(notiType)));
            //if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr3");
			return;
		}

		PushMessage pushMsg = new PushMessage();
		pushMsg.notiType = notiType;
		pushMsg.deviceId = deviceId;
		pushMsg.deviceType = deviceType;
		pushMsg.extra = extra;
		pushMsg.timeMs = timeMs;
		pushMsg.ignoreLatestComparison = false;
		if (notiType == NotificationType.DIAPER_CHANGED) { // 기저귀 교체는 이전 시간으로 설정이 가능하므로 ignoreLatestComparison true로 설정
			pushMsg.ignoreLatestComparison = true;
		}
		handlePushMessage(pushMsg);
	}

	public void handleDiaperSensorPushMessage(PushMessage pushMsg) {
		int notiType = pushMsg.notiType;
		int deviceType = pushMsg.deviceType;
		long deviceId = pushMsg.deviceId;
		String extra = pushMsg.extra;
		long timeMs = pushMsg.timeMs;
		boolean ignoreLatestComparison = pushMsg.ignoreLatestComparison; // 기저귀 교체는 이전 시간으로 설정이 가능하므로 ignoreLatestComparison true로 설정
		String deviceName = mPreferenceMgr.getDeviceName(deviceType, deviceId);
		if (DBG) Log.d(TAG, "handleDiaperSensorPushMessage : " + timeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(timeMs) + ") / "+ notiType + " / " + deviceType + " / " + deviceId + " / " + deviceName + " / " + extra);

		int updateDiaperStatus = DeviceStatus.getDiaperStatusFromNotificationType(notiType);
		int currentDiaperStatus = mPreferenceMgr.getDeviceStatus(DeviceType.DIAPER_SENSOR, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, DeviceStatus.DETECT_NONE);
		long latestDiaperStatusUpdatedTimeSec = mPreferenceMgr.getLatestDiaperStatusUpdatedTimeSec(deviceId);
		if (DBG) Log.d(TAG, "DiaperUpdatedTime : " + latestDiaperStatusUpdatedTimeSec);

		DeviceDiaperSensor sensor = ConnectionManager.getDeviceDiaperSensor(deviceId);
		//if (sensor != null) sensor.updateDiaperStatus();

		// 1. Notification 띄우기
		switch(notiType) {
			case NotificationType.SENSOR_LONG_DISCONNECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
					&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.SENSOR_LONG_DISCONNECTED)) {
					mNotiMgr.notifySensorLongDisconnected(deviceId, deviceName, timeMs);
				}
				break;

			case NotificationType.DIAPER_DETACHMENT_DETECTED: // 기저귀 교체 알람은 Notification 없이 알람탭에 들어가야 함
				//NotificationMessage msgDiaperChangeDetected = new NotificationMessage(NotificationType.DIAPER_DETACHMENT_DETECTED, DeviceType.DIAPER_SENSOR, deviceId, "", timeMs);
				//if (msgDiaperChangeDetected.insertDB(mContext) > 0) {
				//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
				//} else {
				//	if (DBG) Log.e(TAG, "insert error");
				//}
				break;

			case NotificationType.MOVEMENT_DETECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.MOVEMENT_DETECTED)) {
					mNotiMgr.notifyMovementDetected(deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Movement detected alarm disabled");
				}
				break;

			case NotificationType.CONNECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.CONNECTED)
						&& Configuration.MASTER) {
					//mNotiMgr.notifyConnected(DeviceType.DIAPER_SENSOR, deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Connection detected alarm disabled");
				}
				break;

			case NotificationType.DISCONNECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DISCONNECTED)
						&& Configuration.MASTER) {
					//mNotiMgr.notifyDisconnected(DeviceType.DIAPER_SENSOR, deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Disconnection detected alarm disabled");
				}
				break;

			case NotificationType.LOW_BATTERY:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.LOW_BATTERY)) {
					mNotiMgr.notifyLowBatteryPower(deviceId, deviceName, timeMs);
				} else {
					//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr7");
				}
				break;

			case NotificationType.CLOUD_INIT_DEVICE:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.CLOUD_INIT_DEVICE)) {
					mNotiMgr.notifyDeviceInitialized(deviceId, deviceName, timeMs);
				}

				switch (Configuration.APP_MODE) {
					case Configuration.APP_GLOBAL:
					//case Configuration.APP_MONIT_X_HUGGIES:
					case Configuration.APP_MONIT_X_KAO:
						mPreferenceMgr.setDeviceName(DeviceType.DIAPER_SENSOR, deviceId, DeviceInfo.DIAPER_SENSOR_BASE_NAME);
						break;
					case Configuration.APP_KC_HUGGIES_X_MONIT:
						mPreferenceMgr.setDeviceName(DeviceType.DIAPER_SENSOR, deviceId, DeviceInfo.KC_DIAPER_SENSOR_BASE_NAME);
						break;
				}
				break;

			case NotificationType.DIAPER_CHANGED:
 				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)) {
					// 별도로 끌수 없음
					mNotiMgr.notifyDiaperChanged(deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Diaper changed alarm disabled");
				}
 				break;

			case NotificationType.PEE_DETECTED:
			case NotificationType.POO_DETECTED:
			case NotificationType.ABNORMAL_DETECTED:
			case NotificationType.FART_DETECTED:
			case NotificationType.DIAPER_NEED_TO_CHANGE:
				if ((latestDiaperStatusUpdatedTimeSec < timeMs / 1000)
						|| ignoreLatestComparison) {
					if (DBG) Log.d(TAG, "show notification time satisfied : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000) + " / " + ignoreLatestComparison);

					/*
					NotificationMessage msg = new NotificationMessage(notiType, DeviceType.DIAPER_SENSOR, deviceId, "", timeMs);
					if (msg.insertDB(mContext) > 0) {
						if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					*/
						switch(notiType) {
							case NotificationType.PEE_DETECTED:
								FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPeeDetected(deviceId, timeMs);
								break;
							case NotificationType.POO_DETECTED:
								FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPooDetected(deviceId, timeMs);
								break;
							case NotificationType.FART_DETECTED:
								FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmFartDetected(deviceId, timeMs);
								break;
						}
					//}
				} else {
					if (DBG) Log.d(TAG, "previous detected : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000));
					break;
				}

				// 충전중이면 Notification 띄우지 않기(DB에 저장만하기)
				if (sensor != null && DeviceStatus.isSensorCharging(sensor.getOperationStatus()) == true) {
					if (DBG) Log.d(TAG, "charging now : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000));
					break;
				}

				// Notification 받을 수 있으면 띄우기
				if (DeviceStatus.isNotificationAvailableDiaperStatus(currentDiaperStatus, updateDiaperStatus)) {
					if (DBG) Log.d(TAG, "notifiable diaper status: " + currentDiaperStatus + " -> " + updateDiaperStatus);
					if (notiType == NotificationType.PEE_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.PEE_DETECTED)) {
							mNotiMgr.notifyPeeDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Pee detected alarm disabled");
						}

						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_SOILED)) {
							mNotiMgr.notifyDiaperSoiledPeeDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Pee detected alarm disabled");
						}

					} else if (notiType == NotificationType.POO_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.POO_DETECTED)) {
							mNotiMgr.notifyPooDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Poo detected alarm disabled");
						}

						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_SOILED)) {
							mNotiMgr.notifyDiaperSoiledPooDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Poo detected alarm disabled");
						}

					} else if (notiType == NotificationType.ABNORMAL_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)) {
							// 별도로 끌수 없음
							mNotiMgr.notifyAbnormalDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Abnormal detected alarm disabled");
						}

					} else if (notiType == NotificationType.FART_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.FART_DETECTED)) {
							mNotiMgr.notifyFartDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Fart detected alarm disabled");
						}
					} else if (notiType == NotificationType.DIAPER_NEED_TO_CHANGE) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_NEED_TO_CHANGE)) {
							mNotiMgr.notifyCheckDiaper(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "CheckDiaper detected alarm disabled");
						}
					}
				} else {
					if (DBG) Log.d(TAG, "NOT notifiable diaper status: " + currentDiaperStatus + " -> " + updateDiaperStatus);
				}
				break;
			default:
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr8: " + notiType);
				break;
		}

		// 2. 상태 변경이 가능한지 확인 후 View 상태 업데이트
		if ((latestDiaperStatusUpdatedTimeSec < timeMs / 1000)
			|| ignoreLatestComparison) {
			if (DBG)  Log.d(TAG, "view update time satisfied : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000));

			if (DeviceStatus.isUpdatableDiaperStatus(currentDiaperStatus, updateDiaperStatus)) {
				// 상태 변경
				mPreferenceMgr.setDeviceStatus(deviceType, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, updateDiaperStatus);
				mPreferenceMgr.setLatestDiaperStatusUpdatedTimeSec(deviceId, timeMs / 1000);
				if (updateDiaperStatus != currentDiaperStatus) { // 같은 소변 알람인 경우 감지시간은 먼저 왔던 알람시간이 우선 됨
					mPreferenceMgr.setLatestDiaperDetectedTimeMs(deviceId, timeMs);
				}
			} else {
				if (DBG) Log.e(TAG, "not updateable status : " + currentDiaperStatus + "->" + updateDiaperStatus);
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr6: " + currentDiaperStatus + "/" + updateDiaperStatus);
			}
		} else {
			if (DBG) Log.e(TAG, "not latest status : " + mPreferenceMgr.getLatestDiaperStatusUpdatedTimeSec(deviceId) + " / " + timeMs / 1000);
			//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr5");
		}
	}


	public void handleElderlyDiaperSensorPushMessage(PushMessage pushMsg) {
		int notiType = pushMsg.notiType;
		int deviceType = pushMsg.deviceType;
		long deviceId = pushMsg.deviceId;
		String extra = pushMsg.extra;
		long timeMs = pushMsg.timeMs;
		boolean ignoreLatestComparison = pushMsg.ignoreLatestComparison; // 기저귀 교체는 이전 시간으로 설정이 가능하므로 ignoreLatestComparison true로 설정
		String deviceName = mPreferenceMgr.getDeviceName(deviceType, deviceId);
		if (DBG) Log.d(TAG, "handleDiaperSensorPushMessage : " + timeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(timeMs) + ") / "+ notiType + " / " + deviceType + " / " + deviceId + " / " + deviceName + " / " + extra);

		int updateDiaperStatus = DeviceStatus.getDiaperStatusFromNotificationType(notiType);
		int currentDiaperStatus = mPreferenceMgr.getDeviceStatus(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, DeviceStatus.DETECT_NONE);
		long latestDiaperStatusUpdatedTimeSec = mPreferenceMgr.getLatestDiaperStatusUpdatedTimeSec(deviceId);
		if (DBG) Log.d(TAG, "DiaperUpdatedTime : " + latestDiaperStatusUpdatedTimeSec);

		DeviceElderlyDiaperSensor sensor = ConnectionManager.getDeviceElderlyDiaperSensor(deviceId);
		//if (sensor != null) sensor.updateDiaperStatus();

		// 1. Notification 띄우기
		switch(notiType) {
			case NotificationType.SENSOR_LONG_DISCONNECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.SENSOR_LONG_DISCONNECTED)) {
					mNotiMgr.notifySensorLongDisconnected(deviceId, deviceName, timeMs);
				}
				break;

			case NotificationType.DIAPER_DETACHMENT_DETECTED: // 기저귀 교체 알람은 Notification 없이 알람탭에 들어가야 함
				//NotificationMessage msgDiaperChangeDetected = new NotificationMessage(NotificationType.DIAPER_DETACHMENT_DETECTED, DeviceType.DIAPER_SENSOR, deviceId, "", timeMs);
				//if (msgDiaperChangeDetected.insertDB(mContext) > 0) {
				//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
				//} else {
				//	if (DBG) Log.e(TAG, "insert error");
				//}
				break;

			case NotificationType.MOVEMENT_DETECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.MOVEMENT_DETECTED)) {
					mNotiMgr.notifyMovementDetected(deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Movement detected alarm disabled");
				}
				break;

			case NotificationType.CONNECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.CONNECTED)
						&& Configuration.MASTER) {
					//mNotiMgr.notifyConnected(DeviceType.DIAPER_SENSOR, deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Connection detected alarm disabled");
				}
				break;

			case NotificationType.DISCONNECTED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DISCONNECTED)
						&& Configuration.MASTER) {
					//mNotiMgr.notifyDisconnected(DeviceType.DIAPER_SENSOR, deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Disconnection detected alarm disabled");
				}
				break;

			case NotificationType.LOW_BATTERY:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.LOW_BATTERY)) {
					mNotiMgr.notifyLowBatteryPower(deviceId, deviceName, timeMs);
				} else {
					//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr7");
				}
				break;

			case NotificationType.CLOUD_INIT_DEVICE:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
						&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.CLOUD_INIT_DEVICE)) {
					mNotiMgr.notifyDeviceInitialized(deviceId, deviceName, timeMs);
				}

				switch (Configuration.APP_MODE) {
					case Configuration.APP_GLOBAL:
					//case Configuration.APP_MONIT_X_HUGGIES:
					case Configuration.APP_MONIT_X_KAO:
						mPreferenceMgr.setDeviceName(DeviceType.DIAPER_SENSOR, deviceId, DeviceInfo.DIAPER_SENSOR_BASE_NAME);
						break;
					case Configuration.APP_KC_HUGGIES_X_MONIT:
						mPreferenceMgr.setDeviceName(DeviceType.DIAPER_SENSOR, deviceId, DeviceInfo.KC_DIAPER_SENSOR_BASE_NAME);
						break;
				}
				break;

			case NotificationType.DIAPER_CHANGED:
				if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)) {
					// 별도로 끌수 없음
					mNotiMgr.notifyDiaperChanged(deviceId, deviceName, timeMs);
				} else {
					if (DBG) Log.e(TAG, "Diaper changed alarm disabled");
				}
				break;

			case NotificationType.PEE_DETECTED:
			case NotificationType.POO_DETECTED:
			case NotificationType.ABNORMAL_DETECTED:
			case NotificationType.FART_DETECTED:
			case NotificationType.DIAPER_NEED_TO_CHANGE:
				if ((latestDiaperStatusUpdatedTimeSec < timeMs / 1000)
						|| ignoreLatestComparison) {
					if (DBG) Log.d(TAG, "show notification time satisfied : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000) + " / " + ignoreLatestComparison);

					/*
					NotificationMessage msg = new NotificationMessage(notiType, DeviceType.DIAPER_SENSOR, deviceId, "", timeMs);
					if (msg.insertDB(mContext) > 0) {
						if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					*/
					switch(notiType) {
						case NotificationType.PEE_DETECTED:
							FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPeeDetected(deviceId, timeMs);
							break;
						case NotificationType.POO_DETECTED:
							FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmPooDetected(deviceId, timeMs);
							break;
						case NotificationType.FART_DETECTED:
							FirebaseAnalyticsManager.getInstance(mContext).sendSensorAlarmFartDetected(deviceId, timeMs);
							break;
					}
					//}
				} else {
					if (DBG) Log.d(TAG, "previous detected : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000));
					break;
				}

				// 충전중이면 Notification 띄우지 않기(DB에 저장만하기)
				if (sensor != null && DeviceStatus.isSensorCharging(sensor.getOperationStatus()) == true) {
					if (DBG) Log.d(TAG, "charging now : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000));
					break;
				}

				// Notification 받을 수 있으면 띄우기
				if (DeviceStatus.isNotificationAvailableDiaperStatus(currentDiaperStatus, updateDiaperStatus)) {
					if (DBG) Log.d(TAG, "notifiable diaper status: " + currentDiaperStatus + " -> " + updateDiaperStatus);
					if (notiType == NotificationType.PEE_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_SOILED)) {
							mNotiMgr.notifyElderlyDiaperSoiledPeeDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Pee detected alarm disabled");
						}

					} else if (notiType == NotificationType.POO_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.POO_DETECTED)) {
							mNotiMgr.notifyPooDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Poo detected alarm disabled");
						}

						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_SOILED)) {
							mNotiMgr.notifyDiaperSoiledPooDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Pee detected alarm disabled");
						}

					} else if (notiType == NotificationType.ABNORMAL_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)) {
							// 별도로 끌수 없음
							mNotiMgr.notifyAbnormalDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Abnormal detected alarm disabled");
						}

					} else if (notiType == NotificationType.FART_DETECTED) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.FART_DETECTED)) {
							mNotiMgr.notifyFartDetected(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "Fart detected alarm disabled");
						}
					} else if (notiType == NotificationType.DIAPER_NEED_TO_CHANGE) {
						if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)
								&& mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DIAPER_NEED_TO_CHANGE)) {
							mNotiMgr.notifyCheckDiaper(deviceId, deviceName, timeMs);
						} else {
							if (DBG) Log.e(TAG, "CheckDiaper detected alarm disabled");
						}
					}
				} else {
					if (DBG) Log.d(TAG, "NOT notifiable diaper status: " + currentDiaperStatus + " -> " + updateDiaperStatus);
				}
				break;
			default:
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr8: " + notiType);
				break;
		}

		// 2. 상태 변경이 가능한지 확인 후 View 상태 업데이트
		if ((latestDiaperStatusUpdatedTimeSec < timeMs / 1000)
				|| ignoreLatestComparison) {
			if (DBG)  Log.d(TAG, "view update time satisfied : " + latestDiaperStatusUpdatedTimeSec + " / " + (timeMs / 1000));

			if (DeviceStatus.isUpdatableDiaperStatus(currentDiaperStatus, updateDiaperStatus)) {
				// 상태 변경
				mPreferenceMgr.setDeviceStatus(deviceType, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, updateDiaperStatus);
				mPreferenceMgr.setLatestDiaperStatusUpdatedTimeSec(deviceId, timeMs / 1000);
				if (updateDiaperStatus != currentDiaperStatus) { // 같은 소변 알람인 경우 감지시간은 먼저 왔던 알람시간이 우선 됨
					mPreferenceMgr.setLatestDiaperDetectedTimeMs(deviceId, timeMs);
				}
			} else {
				if (DBG) Log.e(TAG, "not updateable status : " + currentDiaperStatus + "->" + updateDiaperStatus);
				//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr6: " + currentDiaperStatus + "/" + updateDiaperStatus);
			}
		} else {
			if (DBG) Log.e(TAG, "not latest status : " + mPreferenceMgr.getLatestDiaperStatusUpdatedTimeSec(deviceId) + " / " + timeMs / 1000);
			//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr5");
		}
	}

	public void handleAQMHubPushMessage(PushMessage pushMsg) {
		int notiType = pushMsg.notiType;
		int deviceType = pushMsg.deviceType;
		long deviceId = pushMsg.deviceId;
		String extra = pushMsg.extra;
		long timeMs = pushMsg.timeMs;
		boolean ignoreLatestComparison = pushMsg.ignoreLatestComparison;
		String deviceName = mPreferenceMgr.getDeviceName(deviceType, deviceId);
		if (DBG) Log.d(TAG, "handleAQMHubPushMessage : " + timeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(timeMs) + ") / "+ notiType + " / " + deviceType + " / " + deviceId + " / " + deviceName + " / " + extra);

		if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)) {
			NotificationMessage msg;
			switch (notiType) {
				case NotificationType.HIGH_TEMPERATURE:
					//msg = new NotificationMessage(NotificationType.HIGH_TEMPERATURE, DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
						FirebaseAnalyticsManager.getInstance(mContext).sendHubAlarmHighTemperatureDetected(deviceId, timeMs);
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyHighTemperature(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.LOW_TEMPERATURE:
					//msg = new NotificationMessage(NotificationType.LOW_TEMPERATURE, DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
						FirebaseAnalyticsManager.getInstance(mContext).sendHubAlarmLowTemperatureDetected(deviceId, timeMs);
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyLowTemperature(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.HIGH_HUMIDITY:
					//msg = new NotificationMessage(NotificationType.HIGH_HUMIDITY, DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
						FirebaseAnalyticsManager.getInstance(mContext).sendHubAlarmHighHumidityDetected(deviceId, timeMs);
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyHighHumidity(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.LOW_HUMIDITY:
					//msg = new NotificationMessage(NotificationType.LOW_HUMIDITY, DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
						FirebaseAnalyticsManager.getInstance(mContext).sendHubAlarmLowHumidityDetected(deviceId, timeMs);
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					//mNotiMgr.notifyLowHumidity(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.VOC_WARNING:
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) break; // KC는 VOC관련 피처 제거
					//msg = new NotificationMessage(NotificationType.VOC_WARNING, DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					//mNotiMgr.notifyBadVoc(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.CLOUD_INIT_DEVICE:
					mNotiMgr.notifyDeviceInitialized(deviceId, deviceName, timeMs);

					switch (Configuration.APP_MODE) {
						case Configuration.APP_GLOBAL:
						//case Configuration.APP_MONIT_X_HUGGIES:
						case Configuration.APP_MONIT_X_KAO:
							mPreferenceMgr.setDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, DeviceInfo.AQMHUB_BASE_NAME);
							break;
						case Configuration.APP_KC_HUGGIES_X_MONIT:
							mPreferenceMgr.setDeviceName(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, DeviceInfo.KC_AQMHUB_BASE_NAME);
							break;
					}
					break;
				default:
					//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr9: " + notiType);
					break;
			}
		} else {
			//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr10");
		}
	}

	public void handleLampPushMessage(PushMessage pushMsg) {
		int notiType = pushMsg.notiType;
		int deviceType = pushMsg.deviceType;
		long deviceId = pushMsg.deviceId;
		String extra = pushMsg.extra;
		long timeMs = pushMsg.timeMs;
		boolean ignoreLatestComparison = pushMsg.ignoreLatestComparison;
		String deviceName = mPreferenceMgr.getDeviceName(deviceType, deviceId);
		if (DBG) Log.d(TAG, "handleLampPushMessage : " + timeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(timeMs) + ") / "+ notiType + " / " + deviceType + " / " + deviceId + " / " + deviceName + " / " + extra);

		if (mPreferenceMgr.getDeviceAlarmEnabled(deviceType, deviceId, NotificationType.DEVICE_ALL)) {
			NotificationMessage msg;
			switch (notiType) {
				case NotificationType.HIGH_TEMPERATURE:
					//msg = new NotificationMessage(NotificationType.HIGH_TEMPERATURE, DeviceType.LAMP, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyHighTemperature(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.LOW_TEMPERATURE:
					//msg = new NotificationMessage(NotificationType.LOW_TEMPERATURE, DeviceType.LAMP, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyLowTemperature(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.HIGH_HUMIDITY:
					//msg = new NotificationMessage(NotificationType.HIGH_HUMIDITY, DeviceType.LAMP, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyHighHumidity(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.LOW_HUMIDITY:
					//msg = new NotificationMessage(NotificationType.LOW_HUMIDITY, DeviceType.LAMP, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyLowHumidity(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.VOC_WARNING:
					//msg = new NotificationMessage(NotificationType.VOC_WARNING, DeviceType.LAMP, deviceId, "", timeMs);
					//if (msg.insertDB(mContext) > 0) {
					//	if (mConnectionMgr != null) mConnectionMgr.sendNotificationUpdatedMessage();
					//}
					mNotiMgr.notifyBadVoc(deviceType, deviceId, deviceName, timeMs);
					break;
				case NotificationType.CLOUD_INIT_DEVICE:
					mNotiMgr.notifyDeviceInitialized(deviceId, deviceName, timeMs);

					switch (Configuration.APP_MODE) {
						case Configuration.APP_GLOBAL:
						//case Configuration.APP_MONIT_X_HUGGIES:
						case Configuration.APP_MONIT_X_KAO:
							mPreferenceMgr.setDeviceName(DeviceType.LAMP, deviceId, DeviceInfo.LAMP_BASE_NAME);
							break;
						case Configuration.APP_KC_HUGGIES_X_MONIT:
							mPreferenceMgr.setDeviceName(DeviceType.LAMP, deviceId, DeviceInfo.LAMP_BASE_NAME);
							break;
					}
					break;
				default:
					//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr9: " + notiType);
					break;
			}
		} else {
			//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr10");
		}
	}

	public void handlePushMessage(PushMessage pushMsg) {
		int notiType = pushMsg.notiType;
		int deviceType = pushMsg.deviceType;
		long deviceId = pushMsg.deviceId;
		String extra = pushMsg.extra;
		long timeMs = pushMsg.timeMs;
		boolean ignoreLatestComparison = pushMsg.ignoreLatestComparison;
		String deviceName = mPreferenceMgr.getDeviceName(deviceType, deviceId);
		if (DBG) Log.d(TAG, "handlePushMessage : " + timeMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(timeMs) + ") / "+ notiType + " / " + deviceType + " / " + deviceId + " / " + deviceName + " / " + extra);
		//mDebugMgr.saveDebuggingLog("msg: " + notiType + " / " + deviceType + " / " + deviceId + " / " + extra + " / " + timeMs);

		switch (notiType) {
			case NotificationType.UPDATE_NOTIFICATION_EDIT_DATA:
				if (DBG) Log.d(TAG, "Push UpdateNotificationEdit(" + deviceType + "/" + deviceId + ")");
				if (mConnectionMgr != null) {
					mConnectionMgr.getNotificationFromCloudV2(deviceType, (int) deviceId);
					mConnectionMgr.updateDeviceFullStatusFromCloud(null);
				}
				break;
			case NotificationType.UPDATE_FULL_DATA:
				if (DBG) Log.d(TAG, "Push UpdateDeviceFullStatus");
				if (mConnectionMgr != null) {
					mConnectionMgr.updateDeviceFullStatusFromCloud(null);
				}
				break;
			case NotificationType.UPDATE_CLOUD_DATA:
			case NotificationType.CLOUD_INIT_DEVICE:
				if (DBG) Log.d(TAG, "Push UpdateUserInfo");
				if (mConnectionMgr != null) {
					mConnectionMgr.getCloudNotificationFromCloudV2();
					mConnectionMgr.getUserInfoFromCloud();
				}
				break;
			case NotificationType.OAUTH_LOGIN_SUCCESS:
				if (DBG) Log.d(TAG, "Push OAUTH_LOGIN_SUCCESS");
				Intent intent = new Intent();
				intent.setAction(NuguActivity.BROADCAST_MESSAGE_AUTH_COMPLETED);
				mContext.sendBroadcast(intent);
				break;
			default:
				if (deviceType == DeviceType.DIAPER_SENSOR && deviceId > 0) {
					handleDiaperSensorPushMessage(pushMsg);
					if (mConnectionMgr != null) {
						mConnectionMgr.getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, deviceId);
					}
				} else if (deviceType == DeviceType.AIR_QUALITY_MONITORING_HUB && deviceId > 0) {
					handleAQMHubPushMessage(pushMsg);
					if (mConnectionMgr != null) {
						mConnectionMgr.getNotificationFromCloudV2(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId);
					}
				} else if (deviceType == DeviceType.LAMP && deviceId > 0) {
					handleLampPushMessage(pushMsg);
					if (mConnectionMgr != null) {
						mConnectionMgr.getNotificationFromCloudV2(DeviceType.LAMP, deviceId);
					}
				} else if (deviceType == DeviceType.ELDERLY_DIAPER_SENSOR && deviceId > 0) {
					handleElderlyDiaperSensorPushMessage(pushMsg);
					if (mConnectionMgr != null) {
						mConnectionMgr.getNotificationFromCloudV2(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId);
					}
				} else if (deviceType == DeviceType.SYSTEM) { // Custom Push
					if (mNotiMgr != null) {
						mNotiMgr.notifyCustomPush(extra, timeMs);
					}
				} else {
					//if (Configuration.LOGGING) mDebugMgr.saveDebuggingLog("perr11: " + deviceType + "/" + deviceId);
				}
				break;
		}

		if (mConnectionMgr == null) {
			if (alreadyStartService == false) {
				if (ConnectionManager.getInstance() == null) {
					if (DBG) Log.e(TAG, "start service2");
					mContext.startService(new Intent(mContext, ConnectionManager.class));
				}
			}
		}
	}
}