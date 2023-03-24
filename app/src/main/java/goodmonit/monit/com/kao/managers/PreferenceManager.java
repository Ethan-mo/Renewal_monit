package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.IllegalFormatException;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.message.NotificationType;

public class PreferenceManager {
	private static final String TAG = Configuration.BASE_TAG + "Pref";
	private static final boolean DBG = Configuration.DBG;
	private static final String PREFERENCE_NAME				= "MonitPreferences";
	private static PreferenceManager mPreferenceManager;
	private Context mContext;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor;
	private sm mStringMgr;

	public static PreferenceManager getInstance(Context context) {
		if (context == null) {
			throw new InvalidParameterException("null Context");
		}

		if (mPreferenceManager == null) {
			mPreferenceManager = new PreferenceManager(context);
		}

		return mPreferenceManager;
	}

	public PreferenceManager(Context context) {
		mContext = context;
		mPrefs = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mPrefsEditor = mPrefs.edit();
		mStringMgr = new sm();
	}

	private void _putValue(final String key, final String value) {
		_putValue(key, value, true);
	}
	private void _putValue(final String key, final String value, boolean encrypt) {
		String encryptedValue = null;
		String decryptedValue = "";
		if (encrypt) {
			try {
				encryptedValue = em.getInstance(mContext).getLocalEncryptedString(value);
				decryptedValue = em.getInstance(mContext).getLocalDecryptedString(encryptedValue);
			} catch (NoSuchAlgorithmException e) {
				if (DBG) Log.e(TAG, "NoSuchAlgorithmException");
			} catch (InvalidKeyException e) {
				if (DBG) Log.e(TAG, "InvalidKeyException");
			} catch (InvalidAlgorithmParameterException e) {
				if (DBG) Log.e(TAG, "InvalidAlgorithmParameterException");
			} catch (NoSuchPaddingException e) {
				if (DBG) Log.e(TAG, "NoSuchPaddingException");
			} catch (BadPaddingException e) {
				if (DBG) Log.e(TAG, "BadPaddingException");
			} catch (UnsupportedEncodingException e) {
				if (DBG) Log.e(TAG, "UnsupportedEncodingException");
			} catch (IllegalBlockSizeException e) {
				if (DBG) Log.e(TAG, "IllegalBlockSizeException");
			} catch (IllegalArgumentException e) {
				if (DBG) Log.e(TAG, "IllegalArgumentException");
			}
		} else {
			encryptedValue = value;
		}
		//if (DBG) Log.d(TAG, "putValue : " + key + " / " + value + "-> " + encryptedValue + "(" + decryptedValue + ")");
		mPrefsEditor.putString(key, encryptedValue);
		mPrefsEditor.commit();
	}
	private void _putValue(final String key, final int value) {
		_putValue(key, value + "");
		mPrefsEditor.commit();
	}
	private void _putValue(final String key, final long value) {
		_putValue(key, value + "");
		mPrefsEditor.commit();
	}
	private void _putValue(final String key, final float value) {
		_putValue(key, value + "");
		mPrefsEditor.commit();
	}
	private void _putValue(final String key, final boolean value) {
		if (value) {
			_putValue(key, "1");
		} else {
			_putValue(key, "0");
		}
		mPrefsEditor.commit();
	}

	private String _getValue(final String key, final String defaultValue) {
		return _getValue(key, defaultValue, true);
	}
	private String _getValue(final String key, final String defaultValue, boolean decrypt) {
		String decryptedValue = defaultValue;
		String encryptedValue = mPrefs.getString(key, defaultValue);
		if (decrypt) {
			try {
				decryptedValue = em.getInstance(mContext).getLocalDecryptedString(encryptedValue);
			} catch (NoSuchAlgorithmException e) {
				if (DBG) Log.e(TAG, "NoSuchAlgorithmException : " + key);
				_putValue(key, defaultValue);
			} catch (InvalidKeyException e) {
				if (DBG) Log.e(TAG, "InvalidKeyException : " + key);
				_putValue(key, defaultValue);
			} catch (InvalidAlgorithmParameterException e) {
				if (DBG) Log.e(TAG, "InvalidAlgorithmParameterException : " + key);
				_putValue(key, defaultValue);
			} catch (NoSuchPaddingException e) {
				if (DBG) Log.e(TAG, "NoSuchPaddingException : " + key);
				_putValue(key, defaultValue);
			} catch (BadPaddingException e) {
				if (DBG) Log.e(TAG, "BadPaddingException : " + key);
				_putValue(key, defaultValue);
			} catch (UnsupportedEncodingException e) {
				if (DBG) Log.e(TAG, "UnsupportedEncodingException : " + key);
				_putValue(key, defaultValue);
			} catch (IllegalBlockSizeException e) {
				if (DBG) Log.e(TAG, "IllegalBlockSizeException : " + key);
				_putValue(key, defaultValue);
			} catch (IllegalArgumentException e) {
				if (DBG) Log.e(TAG, "IllegalArgumentException : " + key);
				_putValue(key, defaultValue);
			}
		} else {
			decryptedValue = encryptedValue;
		}
		//if (DBG) Log.d(TAG, "getValue : " + key + " / " + encryptedValue + " -> " + decryptedValue);
		return decryptedValue;
	}
	private int _getValue(final String key, final int defaultValue) {
		String value = _getValue(key, null);
		int ret = defaultValue;
		try {
			ret = Integer.parseInt(value);
		} catch (IllegalFormatException e) {
			if (DBG) Log.e(TAG, "IllegalFormatException : " + key);
			_putValue(key, defaultValue);
		} catch (NumberFormatException e) {
			if (DBG) Log.e(TAG, "NumberFormatException : " + key);
			_putValue(key, defaultValue);
		}
		return ret;
	}
	private long _getValue(final String key, final long defaultValue) {
		String value = _getValue(key, null);
		long ret = defaultValue;
		try {
			ret = Long.parseLong(value);
		} catch (IllegalFormatException e) {
			if (DBG) Log.e(TAG, "IllegalFormatException : " + key);
			_putValue(key, defaultValue);
		} catch (NumberFormatException e) {
			if (DBG) Log.e(TAG, "NumberFormatException : " + key);
			_putValue(key, defaultValue);
		}
		return ret;
	}
	private float _getValue(final String key, final float defaultValue) {
		String value = _getValue(key, null);
		float ret = defaultValue;
		try {
			ret = Float.parseFloat(value);
		} catch (IllegalFormatException e) {
			if (DBG) Log.e(TAG, "IllegalFormatException : " + key);
			_putValue(key, defaultValue);
		} catch (NumberFormatException e) {
			if (DBG) Log.e(TAG, "NumberFormatException : " + key);
			_putValue(key, defaultValue);
		}
		return ret;
	}
	private boolean _getValue(final String key, final boolean defaultValue) {
		String value = _getValue(key, defaultValue ? "1" : "0");
		boolean ret = defaultValue;

		if ("1".equals(value)) ret = true;
		else if ("0".equals(value)) ret = false;

		return ret;
	}

	/**
	 * Check device version and market version
	 */
	public void setDiaperSensorVersion(String version) { _putValue(mStringMgr.getParameter(500), version); }
	public String getDiaperSensorVersion() { return _getValue(mStringMgr.getParameter(500), "1.0.0"); }
	public void setHubVersion(String version) { _putValue(mStringMgr.getParameter(501), version); }
	public String getHubVersion() { return _getValue(mStringMgr.getParameter(501), "1.0.0"); }
	public void setLampVersion(String version) { _putValue(mStringMgr.getParameter(552), version); }
	public String getLampVersion() { return _getValue(mStringMgr.getParameter(552), "1.0.0"); }
	public void setMarketVersion(String version) { _putValue(mStringMgr.getParameter(502), version); }
	public String getMarketVersion() { return _getValue(mStringMgr.getParameter(502), "1.0.0"); }
	public void setLocalVersion(String version) { _putValue(mStringMgr.getParameter(503), version); }
	public String getLocalVersion() { return _getValue(mStringMgr.getParameter(503), "1.0.0"); }
	public void setLatestInstalledVersion(String version) { _putValue(mStringMgr.getParameter(504), version); }
	public String getLatestInstalledVersion() { return _getValue(mStringMgr.getParameter(504), "1.0.0"); }
	public void setDiaperSensorForceVersion(String version) { _putValue(mStringMgr.getParameter(555), version); }
	public String getDiaperSensorForceVersion() { return _getValue(mStringMgr.getParameter(555), "1.0.0"); }
	public void setHubForceVersion(String version) { _putValue(mStringMgr.getParameter(556), version); }
	public String getHubForceVersion() { return _getValue(mStringMgr.getParameter(556), "1.0.0"); }
	public void setLampForceVersion(String version) { _putValue(mStringMgr.getParameter(557), version); }
	public String getLampForceVersion() { return _getValue(mStringMgr.getParameter(557), "1.0.0"); }
	/**
	 * Check account information
	 */
	public void setSelectedBabyId(int id) { _putValue(mStringMgr.getParameter(517), id); }
	public int getSelectedBabyId() { return _getValue(mStringMgr.getParameter(517), 0); }
	public void setProfileNickname(String nickname) { _putValue(mStringMgr.getParameter(514), nickname); }
	public String getProfileNickname() { return _getValue(mStringMgr.getParameter(514), null); }
	public void setProfileSex(int sex) { _putValue(mStringMgr.getParameter(516), sex); }
	public int getProfileSex() { return _getValue(mStringMgr.getParameter(516), 0); }
	public void setProfileBirthday(String birthday) { _putValue(mStringMgr.getParameter(515), birthday); }
	public String getProfileBirthday() { return _getValue(mStringMgr.getParameter(515), null); }
	public void setCloudId(String id) { _putValue(mStringMgr.getParameter(512), id); }
	public String getCloudId() { return _getValue(mStringMgr.getParameter(512), null); }
	public void setShortId(String id) { _putValue(mStringMgr.getParameter(513), id); }
	public String getShortId() { return _getValue(mStringMgr.getParameter(513), null); }
	public void setAccountId(long id) { _putValue(mStringMgr.getParameter(505), id); }
	public long getAccountId() { return _getValue(mStringMgr.getParameter(505), 0L); }
	public void setSigninToken(String token) { _putValue(mStringMgr.getParameter(506), token); }
	public String getSigninToken() { return _getValue(mStringMgr.getParameter(506), null); }
	public void setSigninEmail(String email) { _putValue(mStringMgr.getParameter(507), email); }
	public String getSigninEmail() { return _getValue(mStringMgr.getParameter(507), null); }
	public void setSigninState(int state) { _putValue(mStringMgr.getParameter(508), state); }
	public int getSigninState() { return _getValue(mStringMgr.getParameter(508), 0); }
	public void setPushToken(String token) { _putValue(mStringMgr.getParameter(509), token); }
	public String getPushToken() { return _getValue(mStringMgr.getParameter(509), null); }
	public void setPushId(String token) { _putValue(mStringMgr.getParameter(510), token); }
	public String getPushId() { return _getValue(mStringMgr.getParameter(510), null); }
	public void setNeedToUpdatePushToken(int type) { _putValue(mStringMgr.getParameter(511), type); }
	public int getNeedToUpdatePushToken() { return _getValue(mStringMgr.getParameter(511), -1); }

	/**
	 * Latest Notification time
	 */
	public void setLatestNotificationTimeMs(int deviceType, long deviceId, long timeMs) { _putValue(mStringMgr.getParameter(518) + deviceType + "_" + deviceId, timeMs); }
	public long getLatestNotificationTimeMs(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(518) + deviceType + "_" + deviceId, 0L); }
	public void setLatestNotificationEditTimeMs(int deviceType, long deviceId, long timeMs) { _putValue(mStringMgr.getParameter(542) + deviceType + "_" + deviceId, timeMs); }
	public long getLatestNotificationEditTimeMs(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(542) + deviceType + "_" + deviceId, 0L); }
	public void setLatestSavedNotificationIndex(int deviceType, long deviceId, int type, long index) { _putValue(mStringMgr.getParameter(519) + deviceType + "_" + deviceId + "_" + type, index); }
    public long getLatestSavedNotificationIndex(int deviceType, long deviceId, int type) { return _getValue(mStringMgr.getParameter(519) + deviceType + "_" + deviceId + "_" + type, 0L); }
	public void setLatestCheckedNotificationIndex(int deviceType, long deviceId, long index) { _putValue(mStringMgr.getParameter(520) + deviceType + "_" + deviceId, index); }
	public long getLatestCheckedNotificationIndex(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(520) + deviceType + "_" + deviceId, 0L); }

	/**
	 * Latest Diaper Status time
	 */
	public void setLatestDiaperDetectedTimeMs(long deviceId, long timeMs) { _putValue(mStringMgr.getParameter(521) + deviceId, timeMs); }
	public long getLatestDiaperDetectedTimeMs(long deviceId) { return _getValue(mStringMgr.getParameter(521) + deviceId, 0L); }
	public void setLatestDiaperStatusUpdatedTimeSec(long deviceId, long timeSec) { _putValue(mStringMgr.getParameter(522) + deviceId, timeSec); }
	public long getLatestDiaperStatusUpdatedTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(522) + deviceId, 0L); }
	public void setLatestDiaperChangedTimeSec(long deviceId, long timeSec) { _putValue(mStringMgr.getParameter(560) + deviceId, timeSec); }
	public long getLatestDiaperChangedTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(560) + deviceId, 0L); }

	/**
	 * Latest HubGraph updated time
	 */
	public void setLatestHubGraphUpdatedTimeSec(long deviceId, long timeSec) { _putValue(mStringMgr.getParameter(523) + deviceId, timeSec); }
	public long getLatestHubGraphUpdatedTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(523) + deviceId, 0L); }

	/**
	 * Latest LampGraph updated time
	 */
	public void setLatestLampGraphUpdatedTimeSec(long deviceId, long timeSec) { _putValue(mStringMgr.getParameter(553) + deviceId, timeSec); }
	public long getLatestLampGraphUpdatedTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(553) + deviceId, 0L); }

	/**
	 * Latest MovementGraph updated time
	 */
	public void setLatestMovementGraphUpdatedTimeSec(long deviceId, long timeSec) { _putValue(mStringMgr.getParameter(566) + deviceId, timeSec); }
	public long getLatestMovementGraphUpdatedTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(566) + deviceId, 0L); }

	/**
	 * Latest SleepGraph updated time
	 */
	public void setLatestSleepGraphUpdatedTimeSec(long deviceId, long timeSec) { _putValue(mStringMgr.getParameter(568) + deviceId, timeSec); }
	public long getLatestSleepGraphUpdatedTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(568) + deviceId, 0L); }

	/**
	 * DiaperSensor Sleeping level
	 */
	public void setDiaperSensorCurrentSleepingLevel(long deviceId, int level) { _putValue(mStringMgr.getParameter(567) + deviceId, level); }
	public int getDiaperSensorCurrentSleepingLevel(long deviceId) { return _getValue(mStringMgr.getParameter(567) + deviceId, DeviceStatus.MOVEMENT_NO_MOVEMENT); }

	/**
	 *  Device Alarm On/Off
	 */
	public void setDeviceAlarmEnabled(int deviceType, long deviceId, int notiType, boolean enabled) {_putValue(mStringMgr.getParameter(531) + deviceType + "_" + deviceId + "_" + notiType, enabled);}
	public boolean getDeviceAlarmEnabled(int deviceType, long deviceId, int notiType) {
		if (deviceType == DeviceType.DIAPER_SENSOR)
		{
			if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) { // 한국은 방귀 DEFAULT OFF 설정
				switch (notiType) {
					case NotificationType.FART_DETECTED:
						return _getValue(mStringMgr.getParameter(531) + deviceType + "_" + deviceId + "_" + notiType, false);
				}
			}

			if (notiType == NotificationType.MOVEMENT_DETECTED) { // 움직임 알람은 DEFAULT OFF
				return _getValue(mStringMgr.getParameter(531) + deviceType + "_" + deviceId + "_" + notiType, false);
			}
		}

		return _getValue(mStringMgr.getParameter(531) + deviceType + "_" + deviceId + "_" + notiType, true);
	}

	/**
	 *  Device Info
	 */
	public void setDeviceName(int deviceType, long deviceId, String name) {_putValue(mStringMgr.getParameter(532) + deviceType + "_" + deviceId, name);}
	public String getDeviceName(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(532) + deviceType + "_" + deviceId, "Monit");}
	public void setDeviceSerialNumber(int deviceType, long deviceId, String serial) {_putValue(mStringMgr.getParameter(540) + deviceType + "_" + deviceId, serial);}
	public String getDeviceSerialNumber(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(540) + deviceType + "_" + deviceId, null);}

	/**
	 *  MonitDeviceStatus
	 */
	public void setDeviceStatus(int deviceType, long deviceId, int statusType, int statusValue) {_putValue(mStringMgr.getParameter(533) + deviceType + "_" + deviceId + "_" + statusType, statusValue);}
	public int getDeviceStatus(int deviceType, long deviceId, int statusType, int defaultValue) { return _getValue(mStringMgr.getParameter(533) + deviceType + "_" + deviceId + "_" + statusType, defaultValue);}

	/**
	 * Data Unit
	 */
	public void setTemperatureScale(String scale) { _putValue(mStringMgr.getParameter(524), scale); }
	public String getTemperatureScale() {
		if ("en".equals(Locale.getDefault().getLanguage())) {
			return _getValue(mStringMgr.getParameter(524), mContext.getString(R.string.unit_temperature_fahrenheit));
		} else {
			return _getValue(mStringMgr.getParameter(524), mContext.getString(R.string.unit_temperature_celsius));
		}
	}


	public void setLatestForegroundFragmentId(int fragmentId) { _putValue(mStringMgr.getParameter(525), fragmentId); }
	public int getLatestForegroundFragmentId() { return _getValue(mStringMgr.getParameter(525), BaseFragment.ID_SENSOR_STATUS); }

	/**
	 *  Push message
	 */
	public void setLatestPushDataTime(int type, long timeMs) {_putValue(mStringMgr.getParameter(526) + type, timeMs);}
	public long getLatestPushDataTime(int type) { return _getValue(mStringMgr.getParameter(526) + type, 0L);}

	/**
	 *  Gatt Connection Failed Alarm
	 */
	public void setLatestGattConnectionFailedAlarmTime(long timeMs) {_putValue(mStringMgr.getParameter(527), timeMs);}
	public long getLatestGattConnectionFailedAlarmTime() { return _getValue(mStringMgr.getParameter(527), 0L);}

	/**
	 *  Force Close Warning
	 */
	public void setDoNotShowDialog(int dlgId, boolean show) {_putValue(mStringMgr.getParameter(528) + dlgId, show);}
	public boolean getDoNotShowDialog(int dlgId) { return _getValue(mStringMgr.getParameter(528) + dlgId, false);}

	/**
	 *  Hub Registration Dialog
	 */
	public void setNeedHubRegistrationDialog(boolean show) {_putValue(mStringMgr.getParameter(541), show);}
	public boolean getNeedHubRegistrationDialog() { return _getValue(mStringMgr.getParameter(541), false);}

	/**
	 *  Invalid token
	 */
	public void setInvalidTokenReceived(boolean isInvalidToken) {_putValue(mStringMgr.getParameter(529), isInvalidToken);}
	public boolean getInvalidTokenReceived() { return _getValue(mStringMgr.getParameter(529), false);}

	/**
	 *  DemoInfo
	 */
	public void setDemoInfoThreshold(float threshold) {_putValue(mStringMgr.getParameter(534), threshold);}
	public float getDemoInfoThreshold() { return _getValue(mStringMgr.getParameter(534), 0.5f);}

	public void setDemoInfoCount(int count) {_putValue(mStringMgr.getParameter(535), count);}
	public int getDemoInfoCount() { return _getValue(mStringMgr.getParameter(535), 3);}

	public void setDemoInfoIgnoreDelaySec(float delaySec) {_putValue(mStringMgr.getParameter(536), delaySec);}
	public float getDemoInfoIgnoreDelaySec() { return _getValue(mStringMgr.getParameter(536), 30);}

	public void setDemoInfoAlarmDelaySec(float delaySec) {_putValue(mStringMgr.getParameter(537), delaySec);}
	public float getDemoInfoAlarmDelaySec() { return _getValue(mStringMgr.getParameter(537), 0);}

	/**
	 *  Policy
	 */
	public void setPolicyAgreed(long accountId, int policyType, int agreed) {_putValue(mStringMgr.getParameter(538) + "_" + accountId + "_" + policyType, agreed);}
	public int getPolicyAgreed(long accountId, int policyType) {return _getValue(mStringMgr.getParameter(538) + "_" + accountId + "_" + policyType, 0);}
	public void setPolicySetTime(long accountId, int policyType, String time) {_putValue(mStringMgr.getParameter(539) + "_" + accountId + "_" + policyType, time);}
	public String getPolicySetTime(long accountId, int policyType) { return _getValue(mStringMgr.getParameter(539) + "_" + accountId + "_" + policyType, null);}

	/**
	 *  Nugu
	 */
	public void setOtpAuthToken(String token) { _putValue(mStringMgr.getParameter(543), token);}
	public String getOtpAuthToken() { return _getValue(mStringMgr.getParameter(543), null);}
	public void setOtpAuthTokenExpiredUtcTimeStampMs(long expiredUtcTimeMs) { _putValue(mStringMgr.getParameter(544), expiredUtcTimeMs);}
	public long getOtpAuthTokenExpiredUtcTimeStampMs() { return _getValue(mStringMgr.getParameter(544), 0L);}

	/**
	 *  LampTimer
	 */
	public void setLampOffTimerTargetMs(int deviceType, long deviceId, long utcTimeMs) { _putValue(mStringMgr.getParameter(545) + "_" + deviceType + "_" + deviceId, utcTimeMs);}
	public long getLampOffTimerTargetMs(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(545) + "_" + deviceType + "_" + deviceId, 0L);}
	public void setLampOnTimerTargetMs(int deviceType, long deviceId, long utcTimeMs) { _putValue(mStringMgr.getParameter(546) + "_" + deviceType + "_" + deviceId, utcTimeMs);}
	public long getLampOnTimerTargetMs(int deviceType, long deviceId) { return _getValue(mStringMgr.getParameter(546) + "_" + deviceType + "_" + deviceId, 0L);}

	/**
	 *  Tooltip Box
	 */
	public void setDoNotShowTooltipBox(String description, boolean doNotShow) { _putValue(mStringMgr.getParameter(547) + description, doNotShow);}
	public boolean getDoNotShowTooltipBox(String description) { return _getValue(mStringMgr.getParameter(547) + description, false);} // 한번도 열어보지 않은 Tooltip Box는 doNotShow가 False임(보여줘야 함)

	/**
	 *  Widget
	 */
	public void setWidgetRefreshPeriodMin(int widgetId, int periodMin) { _putValue(mStringMgr.getParameter(548) + widgetId, periodMin);}
	public int getWidgetRefreshPeriodMin(int widgetId) { return _getValue(mStringMgr.getParameter(548) + widgetId, 0);}
	public void setWidgetShowDeviceInfo(int widgetId, int index, long deviceInfo) { _putValue(mStringMgr.getParameter(549) + widgetId + "_" + index, deviceInfo);}
	public long getWidgetShowDeviceInfo(int widgetId, int index) { return _getValue(mStringMgr.getParameter(549) + widgetId + "_" + index, -1);}

	/**
	 * Latest Sensor Movement Graph updated time
	 */
	//public void setLatestSensorMovementGraphUpdatedUtcTimeMs(long deviceId, long utcTimeMs) { _putValue(mStringMgr.getParameter(550) + deviceId, utcTimeMs); }
	//public long getLatestSensorMovementGraphUpdatedUtcTimeMs(long deviceId) { return _getValue(mStringMgr.getParameter(550) + deviceId, 0L); }

	/**
	 * Latest Sensor Diaper Status Upload Time Sec
	 */
	public void setLatestSensorDiaperStatusUploadTimeSec(long deviceId, long utcTimeSec) { _putValue(mStringMgr.getParameter(551) + deviceId, utcTimeSec); }
	public long getLatestSensorDiaperStatusUploadTimeSec(long deviceId) { return _getValue(mStringMgr.getParameter(551) + deviceId, 0L); }

	/**
	 * Diaper status check to analyze baseline
	 */
	public void setDiaperCheckNotificationShown(long deviceId, int elapsedMinutes, boolean shown) { _putValue(mStringMgr.getParameter(563) + deviceId + "_" + elapsedMinutes, shown); }
	public boolean getDiaperCheckNotificationShown(long deviceId, int elapsedMinutes) { return _getValue(mStringMgr.getParameter(563) + deviceId + "_" + elapsedMinutes, false); }

	/**
	 * Notification Filter
	 */
	public void setDeviceNotificationFilterSelected(int deviceType, long deviceId, int notiType, boolean selected) { _putValue(mStringMgr.getParameter(554) + deviceType + "_" + deviceId + "_" + notiType, selected); }
	public boolean getDeviceNotificationFilterSelected(int deviceType, long deviceId, int notiType) {
		if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) { // 한국은 방귀 DEFAULT OFF 설정
			switch (notiType) {
				case NotificationType.FART_DETECTED:
					return _getValue(mStringMgr.getParameter(554) + deviceType + "_" + deviceId + "_" + notiType, false);
			}
		}
		return _getValue(mStringMgr.getParameter(554) + deviceType + "_" + deviceId + "_" + notiType, true);
	}

	/**
	 * LocalAppData
	 */
	public void setLocalAppData(String appData) { _putValue(mStringMgr.getParameter(152), appData, false); }
	public String getLocalAppData() { return _getValue(mStringMgr.getParameter(152), null, false); }

	/**
	 * Sleeping
	 */
	public void setSleepingStartTimeMs(long deviceId, long timeMs) { _putValue(mStringMgr.getParameter(558) + deviceId, timeMs); }
	public long getSleepingStartTimeMs(long deviceId) { return _getValue(mStringMgr.getParameter(558) + deviceId, 0L); }
	public void setSleepingEnabled(long deviceId, boolean enabled) { _putValue(mStringMgr.getParameter(561) + deviceId, enabled); }
	public boolean getSleepingEnabled(long deviceId) { return _getValue(mStringMgr.getParameter(561) + deviceId, false); }
	public void setAutoSleepingDetectionEnabled(long deviceId, boolean enabled) { _putValue(mStringMgr.getParameter(565) + deviceId, enabled); }
	public boolean getAutoSleepingDetectionEnabled(long deviceId) { return _getValue(mStringMgr.getParameter(565) + deviceId, true); }

	/**
	 * Feeding
	 */
	public void setLatestFeedingAmount(int feedingType, int amount) { _putValue(mStringMgr.getParameter(562) + feedingType, amount); }
	public int getLatestFeedingAmount(int feedingType, int defaultValue) { return _getValue(mStringMgr.getParameter(562) + feedingType, defaultValue); }

	public void initDiaperSensorPreference(long deviceId) {
		setLatestDiaperStatusUpdatedTimeSec(deviceId, 0);
		setLatestDiaperDetectedTimeMs(deviceId, 0);
		setLatestCheckedNotificationIndex(DeviceType.DIAPER_SENSOR, deviceId, 0);
		setLatestSavedNotificationIndex(DeviceType.DIAPER_SENSOR, deviceId, 0, 0);
		setLatestNotificationTimeMs(DeviceType.DIAPER_SENSOR, deviceId, 0);
		setLatestMovementGraphUpdatedTimeSec(deviceId, 0);

		// Default ON
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL, true);
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, true); // Default On
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DIAPER_SOILED, false); // Default On
		setAutoSleepingDetectionEnabled(deviceId, true);

		// Default Off
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.PEE_DETECTED, false); // Default Off
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.POO_DETECTED, false); // Default Off
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.FART_DETECTED, false); // Default Off
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.CONNECTED, false);
		setDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED, false);
		setDeviceStatus(DeviceType.DIAPER_SENSOR, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, DeviceStatus.DETECT_NONE);
		setDeviceSerialNumber(DeviceType.DIAPER_SENSOR, deviceId, null);
	}

	public void initElderlyDiaperSensorPreference(long deviceId) {
		setLatestDiaperStatusUpdatedTimeSec(deviceId, 0);
		setLatestDiaperDetectedTimeMs(deviceId, 0);
		setLatestCheckedNotificationIndex(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, 0);
		setLatestSavedNotificationIndex(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, 0, 0);
		setLatestNotificationTimeMs(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, 0);
		setLatestMovementGraphUpdatedTimeSec(deviceId, 0);

		// Default ON
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL, true);
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.DIAPER_NEED_TO_CHANGE, true); // Default Off
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.DIAPER_SOILED, false); // Default Off

		// Default Off
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.PEE_DETECTED, false); // Default Off
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.POO_DETECTED, false); // Default Off
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.FART_DETECTED, false); // Default Off
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.SENSOR_LONG_DISCONNECTED, false);
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.CONNECTED, false);
		setDeviceAlarmEnabled(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED, false);
		setDeviceStatus(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, DeviceStatus.DETECT_NONE);
		setDeviceSerialNumber(DeviceType.ELDERLY_DIAPER_SENSOR, deviceId, null);
	}

	public void initAQMHubPreference(long deviceId) {
		setLatestHubGraphUpdatedTimeSec(deviceId, 0);
		setLatestNotificationTimeMs(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, 0);
		setLatestCheckedNotificationIndex(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, 0);
		setLatestSavedNotificationIndex(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, 0, 0);
		setLatestSavedNotificationIndex(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, 1, 0);
		setLatestSavedNotificationIndex(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, 2, 0);
		setDeviceSerialNumber(DeviceType.AIR_QUALITY_MONITORING_HUB, deviceId, null);
	}

	public void initLampPreference(long deviceId) {
		setLatestHubGraphUpdatedTimeSec(deviceId, 0);
		setLatestNotificationTimeMs(DeviceType.LAMP, deviceId, 0);
		setLatestCheckedNotificationIndex(DeviceType.LAMP, deviceId, 0);
		setLatestSavedNotificationIndex(DeviceType.LAMP, deviceId, 0, 0);
		setLatestSavedNotificationIndex(DeviceType.LAMP, deviceId, 1, 0);
		setLatestSavedNotificationIndex(DeviceType.LAMP, deviceId, 2, 0);
		setDeviceSerialNumber(DeviceType.LAMP, deviceId, null);
	}

	public String isAppFirstLaunched() { return _getValue(mStringMgr.getParameter(506), null, false); }

	public boolean getAppClosed() { return _getValue(mStringMgr.getParameter(559), false); }
	public void setAppClosed(boolean closed) { _putValue(mStringMgr.getParameter(559), closed); }

	// Check duplicated notification
	public void setLatestNotificationShownTimeMs(int deviceType, long deviceId, int notiType, long utcTimeMs) { _putValue(mStringMgr.getParameter(564) + deviceType + "_" + deviceId + "_" + notiType, utcTimeMs);}
	public long getLatestNotificationShownTimeMs(int deviceType, long deviceId, int notiType) { return _getValue(mStringMgr.getParameter(564) + deviceType + "_" + deviceId + "_" + notiType, 0);}
}