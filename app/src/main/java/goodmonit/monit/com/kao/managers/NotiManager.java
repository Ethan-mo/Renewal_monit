package goodmonit.monit.com.kao.managers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.MainActivity;
import goodmonit.monit.com.kao.activity.MainLightActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.message.NotificationType;

public class NotiManager {
	private static final String TAG = Configuration.BASE_TAG + "NotiMgr";
	private static final boolean DBG = Configuration.DBG;

	public static final int BASE_MONIT_NOTIFICATION = R.string.app_name;
    public static final String MONIT_NOTIFICATION_CHANNEL_ID = "goodmonit_notification";
    public static final String MONIT_NOTIFICATION_CHANNEL_NAME = "goodmonit_notification_name";

	public static int mNotificationTotalCount = 0;
	
	private static NotiManager mInstance;
	private PreferenceManager mPreferenceMgr;
	private Context mContext;

	private NotificationManager mSystemNotiManager;
	private AudioManager mAudioMgr;
	
	public static NotiManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new NotiManager(context);
		} else {
			mInstance.setContext(context);
		}
		return mInstance;
	}

	public NotiManager(Context context) {
		mContext = context;
		mSystemNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mAudioMgr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
	}

	private void setContext(Context context) {
		mContext = context;
	}

	public void notifyElderlyDiaperSoiledPeeDetected(long deviceId, String title, long timeMs) {
		String message = mContext.getString(R.string.notification_diaper_status_diaper_soiled_title);
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_SOILED, title, message, timeMs);
	}

	public void notifyDiaperSoiledPeeDetected(long deviceId, String title, long timeMs) {
		String message = mContext.getString(R.string.notification_diaper_status_diaper_soiled_title);
		if (Configuration.B2B_MODE) {
			message = message + "(" + mContext.getString(R.string.device_sensor_diaper_status_pee) + ")";
		}
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_SOILED, title, message, timeMs);
	}

	public void notifyDiaperSoiledPooDetected(long deviceId, String title, long timeMs) {
		String message = mContext.getString(R.string.notification_diaper_status_diaper_soiled_title);
		if (Configuration.B2B_MODE) {
			message = message + "(" + mContext.getString(R.string.device_sensor_diaper_status_poo) + ")";
		}
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_SOILED, title, message, timeMs);
	}

	public void notifyPeeDetected(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.PEE_DETECTED, title, mContext.getString(R.string.notification_diaper_status_pee_detected_detail), timeMs);
	}

	public void notifyPooDetected(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.POO_DETECTED, title, mContext.getString(R.string.notification_diaper_status_poo_detected_detail), timeMs);
	}

	public void notifyAbnormalDetected(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.ABNORMAL_DETECTED, title, mContext.getString(R.string.notification_diaper_status_max_voc_detected_detail), timeMs);
	}

	public void notifyDiaperChanged(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_CHANGED, title, mContext.getString(R.string.notification_diaper_status_diaper_changed_detail), timeMs);
	}

	public void notifyFartDetected(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.POO_DETECTED, title, mContext.getString(R.string.notification_diaper_status_fart_detected_detail), timeMs);
	}

	public void notifyCheckDiaper(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, title, mContext.getString(R.string.notification_diaper_status_check_diaper), timeMs);
	}

	public void notifyLowBatteryPower(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.SYSTEM_CUSTOM_PUSH, title, mContext.getString(R.string.notification_low_battery_detail), timeMs);
	}

	public void notifyDeviceInitialized(long deviceId, String title, long timeMs) {
		showMessageNotification(0, 0, NotificationType.SYSTEM_CUSTOM_PUSH, title, mContext.getString(R.string.notification_init_device_description), timeMs);
	}

	public void notifyHighTemperature(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.HIGH_TEMPERATURE, title, mContext.getString(R.string.notification_environment_high_temperature_detail), timeMs);
	}

	public void notifyLowTemperature(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.HIGH_TEMPERATURE, title, mContext.getString(R.string.notification_environment_low_temperature_detail), timeMs);
	}

	public void notifyHighHumidity(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.HIGH_HUMIDITY, title, mContext.getString(R.string.notification_environment_high_humidity_detail), timeMs);
	}

	public void notifyLowHumidity(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.HIGH_HUMIDITY, title, mContext.getString(R.string.notification_environment_low_humidity_detail), timeMs);
	}

	public void notifyBadVoc(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.VOC_WARNING, title, mContext.getString(R.string.notification_environment_bad_voc_detail), timeMs);
	}

	public void notifyCustomPush(String extra, long timeMs) {
		if (DBG) Log.d(TAG, "notifyCustomPush: " + extra);
		if (extra == null) return;
		String title = "";
		String contents = "";
		String extras[] = extra.split("<>");
		if (extras.length == 2) {
			title = extras[0];
			contents = extras[1];
		} else {
			if (DBG) Log.d(TAG, "not enough length: " + extras.length + " / " + extras[0]);
			return;
		}
		showMessageNotification(DeviceType.SYSTEM, 0, NotificationType.SYSTEM_CUSTOM_PUSH, title, contents, timeMs);
	}

	public void notifyConnected(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.CONNECTED, title, mContext.getString(R.string.device_sensor_operation_connected), timeMs);
	}

	public void notifyDisconnected(int deviceType, long deviceId, String title, long timeMs) {
		showMessageNotification(deviceType, deviceId, NotificationType.CONNECTED, title, mContext.getString(R.string.device_sensor_operation_disconnected), timeMs);
	}

	public void notifySensorLongDisconnected(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, title, mContext.getString(R.string.notification_diaper_sensor_long_disconnected_detail), timeMs);
	}

	public void notifyBetatestInputAlarm(long deviceId) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.SYSTEM_CUSTOM_PUSH, "20분마다 기저귀를 체크해주세요.", "피드백메뉴에서 이상없음/소변/대변을 체크를 해주세요.", System.currentTimeMillis());
	}

	public void notifyDiaperStatusCheckAlarm(long deviceId, int type) {
		if (type == 10) {
			showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.SYSTEM_CUSTOM_PUSH, "기저귀 용변유무 확인필요", "기저귀 교체 10분이 지났습니다. 알람메시지를 눌러 기저귀 상태를 입력해주세요.", System.currentTimeMillis());
		} else if (type == 40) {
			showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.SYSTEM_CUSTOM_PUSH, "기저귀 용변유무 확인필요", "기저귀 교체 40분이 지났습니다. 알람메시지를 눌러 기저귀 상태를 입력해주세요.", System.currentTimeMillis());
		}
	}

	public void notifyMovementDetected(long deviceId, String title, long timeMs) {
		showMessageNotification(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED, title, mContext.getString(R.string.notification_diaper_status_movement_detected_detail), timeMs);
	}

	public void notifyConnectionFailedAlarm(int deviceType, long deviceId) {
		long now = System.currentTimeMillis();
		if (mPreferenceMgr != null) {
			if (now - mPreferenceMgr.getLatestGattConnectionFailedAlarmTime() < 10 * 60 * 1000) { // 10분 이내에 알람 안보내기
				return;
			} else {
				String title = "센서 연결 실패";
				String detail = "센서와 연결이 실패하고 있습니다. 비행기 모드를 On/Off 해주시고, 모닛 앱을 강제종료 후 재시작해주세요. 문제가 지속적으로 발생하는 경우 스마트폰 재부팅이 필요합니다.";
				showMessageNotification(deviceType, deviceId, NotificationType.SYSTEM_CUSTOM_PUSH, title, detail, now);
			}
			mPreferenceMgr.setLatestGattConnectionFailedAlarmTime(now);
		}
	}

	public void cancelMessageNotification() {
		mSystemNotiManager.cancel(R.string.app_name);
	}

	public void cancelMessageNotification(int deviceType, long deviceId) {
		int notifyId = BASE_MONIT_NOTIFICATION + (int)deviceId * 10 + deviceType;
		mSystemNotiManager.cancel(mContext.getString(R.string.app_name), notifyId);
	}

	public void showMessageNotification(int deviceType, long deviceId, int notiType, String title, String detail, long timeMs) {
		if (mSystemNotiManager == null) {
			mSystemNotiManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		}

		long latestNotificationShownTime = mPreferenceMgr.getLatestNotificationShownTimeMs(deviceType, deviceId, notiType);
		if (latestNotificationShownTime > 0 && latestNotificationShownTime >= timeMs) {
			if (DBG) Log.d(TAG, "already shown notification: " + deviceType + " / " + deviceId + " / " + notiType + " / " + timeMs + " / " + latestNotificationShownTime);
			return;
		}
		mPreferenceMgr.setLatestNotificationShownTimeMs(deviceType, deviceId, notiType, timeMs);

        mNotificationTotalCount++;
		Intent intent = null;
		if (Configuration.LIGHT_VERSION) {
			intent = new Intent(mContext, MainLightActivity.class);
		} else {
			intent = new Intent(mContext, MainActivity.class);
		}
		intent.putExtra("mode", MainLightActivity.MODE_SHOW_DEVICE_DETAIL_PAGE);
		intent.putExtra("deviceType", deviceType);
		intent.putExtra("deviceId", deviceId);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		intent.putExtra(BaseFragment.START_FRAGMENT, BaseFragment.ID_MESSAGE);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				mContext, 0,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(mContext);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setTicker(mContext.getString(R.string.notification_title));
		builder.setWhen(timeMs);
		builder.setShowWhen(true);
		builder.setContentTitle(title);
		builder.setContentText(detail);
		builder.setStyle(new Notification.BigTextStyle().bigText(detail));
		builder.setContentIntent(pendingIntent);
		builder.setAutoCancel(true);
		builder.setPriority(Notification.PRIORITY_HIGH);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(MONIT_NOTIFICATION_CHANNEL_ID);
		}
        Notification noti = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 오레오부터는 NotificationChannel이 있어야함
            if (mSystemNotiManager.getNotificationChannel(MONIT_NOTIFICATION_CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(MONIT_NOTIFICATION_CHANNEL_ID, MONIT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(mContext.getString(R.string.app_name));

				if (!Configuration.MONIT_ELDERLY_TEST) {
					if (mPreferenceMgr.getProfileSex() == 0) {
						notificationChannel.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.girl_mommy), null);
					} else {
						notificationChannel.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.girl_daddy), null);
					}
				}
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{0, 250, 250, 250});
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                notificationChannel.setShowBadge(true);

                if (DBG) Log.d(TAG, "createNotificationChannel: " + MONIT_NOTIFICATION_CHANNEL_ID);
                mSystemNotiManager.createNotificationChannel(notificationChannel);
            }
        } else {
            if (mAudioMgr != null) {
                int valueMode =  mAudioMgr.getRingerMode();
                switch(valueMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        if (DBG) Log.d(TAG, "audio: RINGER_MODE_NORMAL");
                        if (mPreferenceMgr.getProfileSex() == 0) {
                            noti.sound = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.girl_mommy);
                        } else {
                            noti.sound = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.girl_daddy);
                        }
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        if (DBG) Log.d(TAG, "audio: RINGER_MODE_SILENT");
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        if (DBG) Log.d(TAG, "audio: RINGER_MODE_VIBRATE");
                        noti.defaults |= Notification.DEFAULT_VIBRATE;
                        break;
                    default:
                        if (DBG) Log.e(TAG, "audio: default");
                        break;
                }
            } else {
                noti.defaults |= Notification.DEFAULT_VIBRATE;
            }
        }

        int notifyId = BASE_MONIT_NOTIFICATION + (int)deviceId * 10000 + notiType * 10 + deviceType;
		mSystemNotiManager.notify(mContext.getString(R.string.app_name), notifyId, noti);

		Intent intentBadge = new Intent(MainActivity.BROADCAST_MSG_UPDATE_TAB_BADGE);
		intentBadge.putExtra(MainActivity.EXTRA_TAB_ID, BaseFragment.ID_MESSAGE);
		intentBadge.putExtra(MainActivity.EXTRA_SHOW_BADGE, true);
		intentBadge.putExtra(MainActivity.EXTRA_BADGE_TEXT, "" + mNotificationTotalCount);
		mContext.sendBroadcast(intentBadge);
	}
}
