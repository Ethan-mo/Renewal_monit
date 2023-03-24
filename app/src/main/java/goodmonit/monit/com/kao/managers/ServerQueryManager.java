package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class ServerQueryManager {
    public static final String TAG = Configuration.BASE_TAG + "ServerMgr";
    private static final boolean DBG = Configuration.DBG;

    private static ServerQueryManager mServerQueryManager;
    private Context mContext;

    private ServerManager mServerMgr;
    private ServerDfuManager mServerDfuMgr;
    private PreferenceManager mPreferenceMgr;
    private DatabaseManager mDatabaseMgr;
    private sm mStringMgr;
    private int mServerBaseAddr, mServerStartAddr;

    public static ServerQueryManager getInstance(Context context) {
        if (mServerQueryManager == null) {
            mServerQueryManager = new ServerQueryManager(context);
        }
        return mServerQueryManager;
    }

    public ServerQueryManager(Context context) {
        mContext = context;
        mServerMgr = ServerManager.getInstance(context);
        mServerDfuMgr = ServerDfuManager.getInstance(context);
        mPreferenceMgr = PreferenceManager.getInstance(context);
        mDatabaseMgr = DatabaseManager.getInstance(context);
        mStringMgr = new sm();

//        if (Configuration.RELEASE_SERVER) {
//            mServerStartAddr = 200;
//            mServerBaseAddr = 201;
//        } else {
//            mServerStartAddr = 204;
//            mServerBaseAddr = 205;
//        }

        mServerStartAddr = 200;
        mServerBaseAddr = 201;

    }

    public void init(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1000));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(5), Locale.getDefault().getLanguage());
        params.put(getParameter(6), Configuration.OS_ANDROID + "");
        params.put(getParameter(7), Configuration.APP_MODE + "");
        params.put(getParameter(8), mPreferenceMgr.getLocalVersion());
        params.put(getParameter(9), DateFormat.format(getParameter(1), System.currentTimeMillis()).toString());
        String deviceInfo = Build.MANUFACTURER + "/" + Build.MODEL + "/" + android.os.Build.VERSION.RELEASE;
        params.put(getParameter(10), deviceInfo);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setSensorConnectionLog(String data, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1002));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(11), "\"" + data + "\"");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getLatestInfo(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1001));
        params.put(getParameter(6), Configuration.OS_ANDROID + "");
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void signIn(String email, String password, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1003));
        params.put(getParameter(12), email);
        params.put(getParameter(13), password);
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void YKSignIn(String userId, long signinTime, String token, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1004));
        params.put(getParameter(14), userId);
        params.put(getParameter(15), "" + signinTime);
        params.put(getParameter(4), "" + token);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void YKOAuth2SignIn(String idToken, String accessToken, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1070));
        params.put(getParameter(141), idToken);
        params.put(getParameter(142), accessToken);
        if (Configuration.RELEASE_SERVER) {
            params.put(getParameter(8), "2");
        } else {
            params.put(getParameter(8), "12");
        }
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void join1(String email, String password, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1005));
        params.put(getParameter(12), email);
        params.put(getParameter(13), password);
        params.put(getParameter(5), Locale.getDefault().getLanguage());
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void join2(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1006));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void join3(String nickname, String yyyymm, int gender, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1007));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(16), nickname);
        params.put(getParameter(17), yyyymm);
        params.put(getParameter(18), "" + gender);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void signout(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1008));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void leave(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1009));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void resendAuthEmail(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1010));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(5), Locale.getDefault().getLanguage());
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void updatePushToken() {
        String pushToken = mPreferenceMgr.getPushToken();
        String pushId = mPreferenceMgr.getPushId();
        if (pushToken == null) {
            return;
        }

        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1011));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(19), pushToken);
        params.put(getParameter(20), mPreferenceMgr.getNeedToUpdatePushToken() + "");
        params.put(getParameter(21), pushId);
        mServerMgr.sendPostMethod(url, params, new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                        if (DBG) Log.d(TAG, "Update PushToken Succeeded");
                        mPreferenceMgr.setNeedToUpdatePushToken(PushManager.PUSH_INIT);
                    } else {
                        if (DBG) Log.d(TAG, "Update PushToken Failed");
                    }
                }
            }});
    }

    public void resetPassword(String email, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1050));
        params.put(getParameter(12), email);
        params.put(getParameter(5), Locale.getDefault().getLanguage());
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void changePasswordV2(String oldPassword, String newPassword, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1073));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(13), newPassword);
        params.put(getParameter(148), oldPassword);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void changeNickname(String nickname, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1014));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(16), nickname);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getUserInfo(final ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1015));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());

        mServerMgr.sendPostMethod(url, params, new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errCode, String data) {
                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                    if (listener != null) {
                        listener.onReceive(responseCode, errCode, data);
                    }
                }
            }
        });
    }

    public void inviteCloudMember(String shortId, int familyType, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1016));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(22), shortId);
        params.put(getParameter(23), familyType + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void deleteCloudMember(long targetId, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1017));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(24), targetId + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void leaveCloud(long targetId, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1018));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(24), targetId + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getDeviceId(int type, long deviceId, String serial, String macaddr,  String name, String firmware, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1019));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(44), serial);
        params.put(getParameter(45), macaddr);
        params.put(getParameter(30), firmware);
        params.put(getParameter(25), name);
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getCloudId(int type, long deviceId, String enc, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1020));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setCloudId(int type, long deviceId, String enc, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1021));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDeviceName(int type, long did, String enc, String name, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1022));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), did + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(25), name);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDeviceAlarmStatus(int deviceType, long deviceId, int notiType, boolean alarmEnabled, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1023));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(46), notiType + "," + (alarmEnabled == true ? "1" : "0"));
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDeviceAlarmStatusCommon(int deviceType, long deviceId, int notiType, boolean alarmEnabled, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1079));
        params.put(getParameter(3), mPreferenceMgr.getAccountId() + "");
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(46), notiType + "," + (alarmEnabled == true ? "1" : "0"));
        mServerMgr.sendPostMethod(url, params, listener);
    }
    public void getDeviceStatus(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1024));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void requestBecomeCloudMember(long cloudId, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1025));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(29), cloudId + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getDeviceFullStatus(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1026));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getNotification(int deviceType, long deviceId, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1027));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        String dateTime = null;
        if ((mPreferenceMgr.getLatestNotificationTimeMs(deviceType, deviceId) == 0)
                || (mPreferenceMgr.getLatestNotificationEditTimeMs(deviceType, deviceId) == 0)) { // 가져왔던 데이터가 없는 경우 isfull로 처음부터 땡겨오기
            params.put(getParameter(120), "1");
            dateTime = getParameter(82);
        } else {
            params.put(getParameter(120), "0");
            dateTime = DateTimeUtil.getNotConvertedDateTime(mPreferenceMgr.getLatestNotificationTimeMs(deviceType, deviceId));
            if (dateTime == null) {
                dateTime = getParameter(82);
            }
        }
        params.put(getParameter(15), dateTime);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getNotificationEdit(int deviceType, long deviceId, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1066));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        String dateTime = null;
        if (mPreferenceMgr.getLatestNotificationEditTimeMs(deviceType, deviceId) == 0) {
            dateTime = getParameter(82);
        } else {
            dateTime = DateTimeUtil.getNotConvertedDateTime(mPreferenceMgr.getLatestNotificationEditTimeMs(deviceType, deviceId));
            if (dateTime == null) {
                dateTime = getParameter(82);
            }
        }
        params.put(getParameter(15), dateTime);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getCloudNotification(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1028));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        String dateTime = null;
        if (mPreferenceMgr.getLatestNotificationTimeMs(0, 0) == 0) {
            dateTime = getParameter(82);
        } else {
            dateTime = DateTimeUtil.getNotConvertedDateTime(mPreferenceMgr.getLatestNotificationTimeMs(0, 0));
            if (dateTime == null) {
                dateTime = getParameter(82);
            }
        }
        params.put(getParameter(15), dateTime);

        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void initDevice(int type, long deviceId, String enc, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1029));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void OTAUpdateDevice(int type, long deviceId, String enc, int mode, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1030));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(85), mode + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDiaperChanged(int type, long deviceId, String enc, long utcTimeMs, String extra, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1031));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(37), extra);

        //String utcTimeString = DateFormat.format(getParameter(1), DateTimeUtil.convertLocalToUTCTimeMs(utcTimeMs)).toString();

        String utcTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs);
        params.put(getParameter(15), utcTimeString);
        if (DBG) Log.d(TAG, "setDiaperChanged : " + utcTimeString);

        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void updateFWVersion(int type, long deviceId, String enc, String fwv, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1032));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(30), fwv);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void initDiaperStatus(int type, long deviceId, String enc, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1033));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void startConnectionSensor(String updateData, ServerManager.ServerResponseListener listener) {
        if (updateData == null) {
            return;
        }

        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1034));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(11), updateData);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setLedOnOffTime(int type, long deviceId, String enc, String onTimeHHmm, String offTimeHHmm, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1035));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(31), onTimeHHmm);
        params.put(getParameter(32), offTimeHHmm);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setAlarmThreshold(int type, long deviceId, String enc, float tmax, float tmin, float hmax, float hmin, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1036));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        if (tmax != -1) {
            params.put(getParameter(33), (tmax * 100) + "");
        }
        if (tmin != -1) {
            params.put(getParameter(34), (tmin * 100) + "");
        }
        if (hmax != -1) {
            params.put(getParameter(35), (hmax * 100) + "");
        }
        if (hmin != -1) {
            params.put(getParameter(36), (hmin * 100) + "");
        }
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getNotice(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1058));
        params.put(getParameter(5), Locale.getDefault().getLanguage());
        params.put(getParameter(6), Configuration.OS_ANDROID + "");
        params.put(getParameter(7), Configuration.APP_MODE + "");
        params.put(getParameter(8), mPreferenceMgr.getLocalVersion());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getMaintenance(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1059));
        params.put(getParameter(6), Configuration.OS_ANDROID + "");
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getMaintenanceNotice(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1060));
        params.put(getParameter(5), Locale.getDefault().getLanguage());
        params.put(getParameter(6), Configuration.OS_ANDROID + "");
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getSensorFW(long deviceId, String enc, int mode, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1038));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), "" + deviceId);
        params.put(getParameter(27), enc);
        params.put(getParameter(85), mode + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getSensorFW2(long deviceId, String enc, int mode, ServerDfuManager.ServerDfuResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1074));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), "" + deviceId);
        params.put(getParameter(27), enc);
        params.put(getParameter(85), mode + "");
        mServerDfuMgr.sendPostMethod(url, params, listener);
    }

    public void setSensorSensitivity(long deviceId, String enc, int sensitivity, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1039));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), "" + deviceId);
        params.put(getParameter(27), enc);
        params.put(getParameter(47), "" + sensitivity);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getAppData(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerStartAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1040));
        params.put(getParameter(6), Configuration.OS_ANDROID + "");
        params.put(getParameter(7), Configuration.APP_MODE + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDeviceStatus(String updateData, ServerManager.ServerResponseListener listener) {
        if (updateData == null) {
            return;
        }

        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1041));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(11), updateData);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getHubGraphList(long deviceId, String time, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1042));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(15), time);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getLampGraphList(long deviceId, String time, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1071));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(15), time);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void uploadFile(String fileName, ServerManager.ServerResponseListener listener) {
        //String url = getParameter(202);
        //mServerMgr.uploadFile(url, fileName, listener);
    }

    public void setBabyInfo(int type, long deviceId, String enc, String name, String birthdayYYMMDD, int sex, int eating, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1043));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(25), name);
        params.put(getParameter(17), birthdayYYMMDD);
        params.put(getParameter(18), sex + "");
        params.put(getParameter(112), eating + "");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setNotificationFeedback(NotificationMessage msg, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1044));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(38), msg.notiType + "");
        params.put(getParameter(28), msg.deviceType + "");
        params.put(getParameter(26), msg.deviceId + "");
        params.put(getParameter(37), msg.extra);
        params.put(getParameter(15), DateTimeUtil.getNotConvertedDateTime(msg.timeMs));
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void modifyNotification(NotificationMessage msg, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1065));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(117), msg.serverNotiId + "");
        params.put(getParameter(118), "2"); // Modify
        params.put(getParameter(37), msg.extra);
        params.put(getParameter(15), DateTimeUtil.getNotConvertedDateTime(msg.timeMs));
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void removeNotification(NotificationMessage msg, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        if (msg.serverNotiId > 0) {
            params.put(getParameter(2), getParameter(1065));
            params.put(getParameter(117), msg.serverNotiId + "");
            params.put(getParameter(118), "1"); // Remove
        } else {
            params.put(getParameter(2), getParameter(1055));
            params.put(getParameter(38), msg.notiType + "");
            params.put(getParameter(28), msg.deviceType + "");
            params.put(getParameter(26), msg.deviceId + "");
            params.put(getParameter(15), DateTimeUtil.getNotConvertedDateTime(msg.timeMs));
        }
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDiaperSensingLog(long deviceId, String temperature, String humidity, String voc, String capacitance, String acceleration, String sensorstatus, String movementlevel, String ethanol, String co2, String pressure, String compgas, long timeMs, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1045));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(28), DeviceType.DIAPER_SENSOR + "");
        params.put(getParameter(39), temperature);
        params.put(getParameter(40), humidity);
        params.put(getParameter(41), voc);
        params.put(getParameter(42), capacitance);
        params.put(getParameter(43), acceleration);
        params.put(getParameter(104), sensorstatus);
        params.put(getParameter(105), movementlevel);
        params.put(getParameter(113), ethanol);
        params.put(getParameter(114), co2);
        params.put(getParameter(115), pressure);
        params.put(getParameter(116), compgas);
        params.put(getParameter(15), DateTimeUtil.getNotConvertedDateTime(timeMs));
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setElderlyDiaperSensingLog(long deviceId, String temperature, String humidity, String voc, String capacitance, String acceleration, String sensorstatus, String movementlevel, String ethanol, String co2, String pressure, String compgas, String touchch1, String touchch2, String touchch3, String touchch4, String touchch5, String touchch6, String touchch7, String touchch8, String touchch9, long timeMs, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1045));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(28), DeviceType.ELDERLY_DIAPER_SENSOR + "");
        params.put(getParameter(39), temperature);
        params.put(getParameter(40), humidity);
        params.put(getParameter(41), voc);
        params.put(getParameter(42), capacitance);
        params.put(getParameter(43), acceleration);
        params.put(getParameter(104), sensorstatus);
        params.put(getParameter(105), movementlevel);
        params.put(getParameter(113), ethanol);
        params.put(getParameter(114), co2);
        params.put(getParameter(115), pressure);
        params.put(getParameter(116), compgas);
        params.put(getParameter(157), touchch1);
        params.put(getParameter(158), touchch2);
        params.put(getParameter(159), touchch3);
        params.put(getParameter(160), touchch4);
        params.put(getParameter(161), touchch5);
        params.put(getParameter(162), touchch6);
        params.put(getParameter(163), touchch7);
        params.put(getParameter(164), touchch8);
        params.put(getParameter(165), touchch9);
        params.put(getParameter(15), DateTimeUtil.getNotConvertedDateTime(timeMs));
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getPolicyList(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1046));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setPolicyInsertList(String type, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1047));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(20), type);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setPolicyDeleteList(String type, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1048));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(20), "" + type);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setPolicyDeleteAll(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1049));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setAppInfo(String temunit, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1052));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        if (temunit != null && (temunit.equals("f") || temunit.equals("F"))) {
            temunit = "F";
        } else {
            temunit = "C";
        }
        params.put(getParameter(87), temunit);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getSensorGraphAverage(long beginUtcMs, long endUtcMs, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1053));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());

        beginUtcMs = DateTimeUtil.convertLocalToUTCTimeMs(beginUtcMs);
        endUtcMs = DateTimeUtil.convertLocalToUTCTimeMs(endUtcMs);

        String beginUtcString = DateFormat.format(getParameter(1), beginUtcMs).toString();
        String endUtcString = DateFormat.format(getParameter(1), endUtcMs).toString();
        if (DBG) Log.d(TAG, "getSensorGraphAverage : " + beginUtcString + " ~ " + endUtcString);

        params.put(getParameter(88), beginUtcString);
        params.put(getParameter(89), endUtcString);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getDemoInfo(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1054));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(6), "1"); // Android
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setPolicy(String typeArray, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1056));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(11), typeArray);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getPolicy(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1057));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void initDeviceOwner(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1061));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setAccountActiveUser(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1062));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setChannelEvent(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1063));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(107), "1"); // 채널링Type: 플레이그라운드(1)
        params.put(getParameter(108), "1"); // 이벤트Type: 일반접속시(1)
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setScreenAnalytics(int viewType, int startEvent, int endEvent, String startTime, String endTime, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1064));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(109), "" + viewType);   // View타입
        params.put(getParameter(110), "" + startEvent); // 시작이벤트Type: startApp(1), foreground(2)
        params.put(getParameter(111), "" + endEvent);   // 종료이벤트Type: terminate(1), background(2)
        params.put(getParameter(88), startTime);        // 뷰를 시작한 시간(yyMMdd-hhmmss)
        params.put(getParameter(89), endTime);          // 뷰를 종료한 시간(yyMMdd-hhmmss)
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getAuthToken(ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1067));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getLampOffTimerInfo(int deviceType, long deviceId, String enc, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1068));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), "" + deviceType);
        params.put(getParameter(26), "" + deviceId);
        params.put(getParameter(27), enc);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getMovementGraphList(long deviceId, String lastestSavedTime, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1069));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), "" + deviceId);
        params.put(getParameter(15), lastestSavedTime);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void getSleepGraphList(long deviceId, String lastestSavedTime, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1078));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), "" + deviceId);
        params.put(getParameter(15), lastestSavedTime);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setDeviceBatteryPower(int deviceType, long deviceId, String enc, int batteryPower, ServerManager.ServerResponseListener listener) {
        String updateData = null;
        long timeSec = System.currentTimeMillis() / 1000;
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(getParameter(28), deviceType);
            jobj.put(getParameter(26), deviceId);
            jobj.put(getParameter(27), enc);
            jobj.put(getParameter(15), timeSec); // time
            jobj.put(getParameter(49), batteryPower * 100);
            updateData = "[" + jobj.toString() + "]";
        } catch (JSONException e) {
            if (DBG) Log.e(TAG, e.toString());
        } catch (NullPointerException e) {
            if (DBG) Log.e(TAG, e.toString());
        }
        if (updateData == null) return;

        setDeviceStatus(updateData, listener);
    }

    public void setDeviceStrapBatteryPower(int deviceType, long deviceId, String enc, int strapBatteryPower, ServerManager.ServerResponseListener listener) {
        String updateData = null;
        long timeSec = System.currentTimeMillis() / 1000;
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(getParameter(28), deviceType);
            jobj.put(getParameter(26), deviceId);
            jobj.put(getParameter(27), enc);
            jobj.put(getParameter(15), timeSec); // time
            jobj.put(getParameter(166), strapBatteryPower * 100);
            updateData = "[" + jobj.toString() + "]";
        } catch (JSONException e) {
            if (DBG) Log.e(TAG, e.toString());
        } catch (NullPointerException e) {
            if (DBG) Log.e(TAG, e.toString());
        }
        if (updateData == null) return;

        setDeviceStatus(updateData, listener);
    }

    public void setDeviceMovementStatus(int deviceType, long deviceId, String enc, int movementStatus, ServerManager.ServerResponseListener listener) {
        String updateData = null;
        long timeSec = System.currentTimeMillis() / 1000;
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(getParameter(28), deviceType);
            jobj.put(getParameter(26), deviceId);
            jobj.put(getParameter(27), enc);
            jobj.put(getParameter(15), timeSec); // time
            jobj.put(getParameter(51), movementStatus);
            updateData = "[" + jobj.toString() + "]";
        } catch (JSONException e) {
            if (DBG) Log.e(TAG, e.toString());
        } catch (NullPointerException e) {
            if (DBG) Log.e(TAG, e.toString());
        }
        if (updateData == null) return;

        setDeviceStatus(updateData, listener);
    }

    public void setDeviceOperationStatus(int deviceType, long deviceId, String enc, int operationStatus, ServerManager.ServerResponseListener listener) {
        String updateData = null;
        long timeSec = System.currentTimeMillis() / 1000;
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(getParameter(28), deviceType);
            jobj.put(getParameter(26), deviceId);
            jobj.put(getParameter(27), enc);
            jobj.put(getParameter(15), timeSec); // time
            jobj.put(getParameter(50), operationStatus);
            updateData = "[" + jobj.toString() + "]";
        } catch (JSONException e) {
            if (DBG) Log.e(TAG, e.toString());
        } catch (NullPointerException e) {
            if (DBG) Log.e(TAG, e.toString());
        }
        if (updateData == null) return;

        setDeviceStatus(updateData, listener);
    }

    public void setDeviceConnectionState(int deviceType, long deviceId, String enc, int connectionState, int reason, ServerManager.ServerResponseListener listener) {
        String updateData = null;
        long timeSec = System.currentTimeMillis() / 1000;
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(getParameter(28), deviceType);
            jobj.put(getParameter(26), deviceId);
            jobj.put(getParameter(27), enc);
            jobj.put(getParameter(15), timeSec); // time

            if (connectionState == DeviceConnectionState.DISCONNECTED) {
                jobj.put(getParameter(54), 0);
                jobj.put(getParameter(83), "a" + reason);
            } else {
                jobj.put(getParameter(54), 1);
            }
            updateData = "[" + jobj.toString() + "]";
        } catch (JSONException e) {
            if (DBG) Log.e(TAG, e.toString());
        } catch (NullPointerException e) {
            if (DBG) Log.e(TAG, e.toString());
        }
        if (updateData == null) return;

        setDeviceStatus(updateData, listener);
    }

    public void checkSerialNumber(int type, long deviceId, String srl, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1072));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), type + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(44), srl);
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setSleepMode(long deviceId, String enc, boolean isStart, long utcTimeMs, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1076));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(155), isStart ? "1" : "0");
        String utcTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs);
        params.put(getParameter(15), utcTimeString);
        if (DBG) Log.d(TAG, "setSleepMode : " + utcTimeString);

        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setLampBrightess(int deviceType, long deviceId, String enc, int brightLevel, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1077));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(56), brightLevel + "");
        params.put(getParameter(57), "200");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setLampPower(int deviceType, long deviceId, String enc, int power, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1077));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        params.put(getParameter(55), power + "");
        params.put(getParameter(57), "200");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public void setLampOffTimer(int deviceType, long deviceId, String enc, long utcTimeMs, ServerManager.ServerResponseListener listener) {
        String url = getParameter(mServerBaseAddr);
        HashMap<String, String> params = new HashMap<>();
        params.put(getParameter(2), getParameter(1077));
        params.put(getParameter(3), "" + mPreferenceMgr.getAccountId());
        params.put(getParameter(4), mPreferenceMgr.getSigninToken());
        params.put(getParameter(28), deviceType + "");
        params.put(getParameter(26), deviceId + "");
        params.put(getParameter(27), enc);
        String strUtcTime = DateTimeUtil.getNotConvertedDateTime(utcTimeMs);
        String strNowUtcTime = DateTimeUtil.getNotConvertedDateTime(System.currentTimeMillis());
        if (DBG) Log.i(TAG, "updateAQMHubLampOffTimer: " + strNowUtcTime + "->" + strUtcTime);
        params.put(getParameter(125), strUtcTime);
        params.put(getParameter(57), "200");
        mServerMgr.sendPostMethod(url, params, listener);
    }

    public String getParameter(int type) {
        return mStringMgr.getParameter(type);
    }
}