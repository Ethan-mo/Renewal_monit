package goodmonit.monit.com.kao.devices;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.NotiManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.PushManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class DeviceElderlyDiaperSensor extends DeviceInfo {
    private static final String TAG = Configuration.BASE_TAG + "ElderlySensor";
    private static final boolean DBG = Configuration.DBG;

    public static final long REPEAT_NOTICE_TIME_DIAPER_MIN = 5;

    private DiaperCheckManager mDiaperCheckMgr;
    private DatabaseManager mDatabaseMgr;
    private Context mContext;

    public float currentTemperature;
    public float currentHumidity;
    public float currentVoc;
    public float currentVocAvg;
    public float currentCapacity;
    public float currentAcceleration;
    public float[] currentMultiTouch;
    public int[] currentMultiTouchLevel;
    public int currentContamination;
    public float lastContamination = -1;
    public int currentTouchDetectedCount = 0;
    public long latestTouchDetectedTimeMs = -1;

    public int sensitivity = DeviceStatus.SENSITIVITY_NORMAL;

    public int batteryPower;
    private int strapBatteryPower;
    private boolean isStrapAttached;

    public int movementStatus;
    public int operationStatus;
    public int diaperStatus;
    public int diaperScore;

    public String advertisingName;
    public String babyBirthdayYYYYMM;
    public String babyBirthdayYYMMDD;
    public int babySex;
    public int babyMonths;
    public int babyDdays;
    public int babyEating;

    public long mDiaperDetectedTimeMs = 0;      // 소변 다음 소변 알람이 왔을때, 먼저번 소변 시간을 기록하기 위한 변수
    public long mDiaperStatusUpdatedTimeMs = 0; // 가장 최근 변경된 시간을 알기위한 변수
    public long mDiaperChangedButtonPressedTimeMs = 0; // 가장 최근 기저귀 교체 버튼을 누른 시간
    public long mRecentDiaperChangedTimeMs = -1; // 가장 최근 기저귀를 교체한 시간
    public long mPreviousDiaperChangedTimeMs = -1; // 기저귀 Refreshment 체크를 위한 교체시간
    public PreferenceManager mPreferenceMgr;
    private long mMovementZeroStartMs = 0; // 움직임 감지용 시간 변수

    private int mCountNotification = 0;
    private int mConnectionId;  // Wi-Fi 연결시 허브did 또는 스마트폰Device에 연결된 aid

    // Soiled(Diaper Refreshment)
    private int cntPeeDetected = 0;
    private int cntPooDetected = 0;
    private int cntFartDetected = 0;
    private int cntAbnormalDetected = 0;

    public DeviceElderlyDiaperSensor(Context context) {
        super();
        mContext = context;
        mPreferenceMgr = PreferenceManager.getInstance(context);
        type = DeviceType.ELDERLY_DIAPER_SENSOR;
        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            //case Configuration.APP_MONIT_X_HUGGIES:
            case Configuration.APP_MONIT_X_KAO:
                name = ELDERLY_DIAPER_SENSOR_BASE_NAME;
                break;
            case Configuration.APP_KC_HUGGIES_X_MONIT:
                name = ELDERLY_DIAPER_SENSOR_BASE_NAME;
                break;
        }
        advertisingName = "monit_";

        deviceId = 0;
        cloudId = 0;
        initDeviceDiaperSensor();
    }

    public DeviceElderlyDiaperSensor(Context context, DeviceInfo info) {
        super(info.deviceId, info.cloudId, info.type, info.name, info.btmacAddress, info.serial, info.firmwareVersion, info.advertisingName, info.enabledAlarm1, info.enabledAlarm2, info.enabledAlarm3, info.enabledAlarm4, info.enabledAlarm5);
        mContext = context;
        mPreferenceMgr = PreferenceManager.getInstance(context);
        type = DeviceType.ELDERLY_DIAPER_SENSOR;
        if (name == null) {
            switch (Configuration.APP_MODE) {
                case Configuration.APP_GLOBAL:
                //case Configuration.APP_MONIT_X_HUGGIES:
                case Configuration.APP_MONIT_X_KAO:
                    name = ELDERLY_DIAPER_SENSOR_BASE_NAME;
                    break;
                case Configuration.APP_KC_HUGGIES_X_MONIT:
                    name = ELDERLY_DIAPER_SENSOR_BASE_NAME;
                    break;
            }
        }

        if (advertisingName == null) {
            advertisingName = "monit_";
        }

        initDeviceDiaperSensor();
    }

    public void initDeviceDiaperSensor() {
        currentTemperature = -1;
        currentHumidity = -1;
        currentVoc = -1;
        currentVocAvg = -1;
        currentCapacity = -1;
        currentAcceleration = -1;
        currentMultiTouch = new float[9];
        currentMultiTouchLevel = new int[9];
        diaperScore = 100;
        batteryPower = 0;
        isStrapAttached = false;

        movementStatus = DeviceStatus.MOVEMENT_NO_MOVEMENT;
        operationStatus = DeviceStatus.MODE_DIAPER_SENSING;
        diaperStatus = DeviceStatus.DETECT_NONE;

        connectionState = DeviceConnectionState.DISCONNECTED;
        mConnectionId = 0;

        if (Configuration.FAST_DETECTION) {
            mDiaperCheckMgr = new DiaperCheckManager(mContext);
        }
        mDatabaseMgr = DatabaseManager.getInstance(mContext);
        mDiaperDetectedTimeMs = mPreferenceMgr.getLatestDiaperDetectedTimeMs(deviceId);
        mDiaperStatusUpdatedTimeMs = mPreferenceMgr.getLatestDiaperStatusUpdatedTimeSec(deviceId) * 1000;
        getRecentDiaperChangedTimeMs();
    }

    public long getDiaperChangedButtonPressedTimeMs() {
        return mDiaperChangedButtonPressedTimeMs;
    }

    public long getRecentDiaperChangedTimeMs() {
        NotificationMessage msg = mDatabaseMgr.getRecentDiaperChangedMessages(deviceId);
        if (msg != null) {
            if (DBG) Log.d(TAG, "getRecentDiaperChangedTimeMs: " + msg.toString());
            if (mRecentDiaperChangedTimeMs < msg.timeMs) {
                mRecentDiaperChangedTimeMs = msg.timeMs;
            }
        } else {
            if (DBG) Log.d(TAG, "getRecentDiaperChangedTimeMs: NULL");
        }
        return mRecentDiaperChangedTimeMs;
    }

    public void setConnectionState(int state) {
        if (DBG) Log.e(TAG, "setConnectionState : " + state);
        if (connectionState != state) {
            if (state == DeviceConnectionState.BLE_CONNECTED) {
                if (Configuration.MASTER
                        && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL)
                        && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.CONNECTED)) {
                    //NotiManager.getInstance(mContext).notifyConnected(DeviceType.DIAPER_SENSOR, deviceId + (mCountNotification * 1000), "(" + deviceId + " / " + (mCountNotification++) + ")", System.currentTimeMillis());
                }
            } else if(state == DeviceConnectionState.WIFI_CONNECTED) {
                if (Configuration.MASTER
                        && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL)
                        && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.CONNECTED)) {
                    //NotiManager.getInstance(mContext).notifyConnected(DeviceType.DIAPER_SENSOR, deviceId + (mCountNotification * 1000), "(" + deviceId + " / " + (mCountNotification++) + ")", System.currentTimeMillis());
                }
            } else if (state == DeviceConnectionState.DISCONNECTED) {
                if (Configuration.MASTER
                        && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL)
                        && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.CONNECTED)) {
                    //NotiManager.getInstance(mContext).notifyDisconnected(DeviceType.DIAPER_SENSOR, deviceId + (mCountNotification * 1000), "(" + deviceId + " / " + (mCountNotification++) + ")", System.currentTimeMillis());
                }

                // 전체변수 초기화
                diaperStatus = DeviceStatus.DETECT_NONE;
                operationStatus = DeviceStatus.OPERATION_IDLE;
                movementStatus = DeviceStatus.MOVEMENT_NO_MOVEMENT;
            }
        }
        connectionState = state;
    }

    public int getConnectionState() {
        return connectionState;
    }

    public void setConnectionId(long connId) {
        connectionId = connId;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public float getTemperature() {
        return currentTemperature;
    }

    public float getHumidity() {
        return currentHumidity;
    }

    public float getVoc() {
        return currentVoc;
    }

    public float getVocAvg() {
        return currentVocAvg;
    }

    public float getCapacity() {
        return currentCapacity;
    }

    public float getAcceleration() {
        return currentAcceleration;
    }

    public float[] getMultiTouch() { return currentMultiTouch; }

    public int getTouchDetectedCount() {
        return currentTouchDetectedCount;
    }

    public long getTouchLatestDetectedTimeMs() {
        return latestTouchDetectedTimeMs;
    }

    public boolean isStrapAttached() { return isStrapAttached; }

    public void setTemperature(float value) {
        if (value != -1) {
            currentTemperature = (int) (value * 10) / (float) 10.0;
        }
    }

    public void setHumidity(float value) {
        if (value != -1) {
            currentHumidity = (int)(value * 10) / (float)10.0;
            if (Configuration.FAST_DETECTION) {
                // 3초간 증가하는지 확인 후
                // 알람주기
                if (mDiaperCheckMgr != null) {
                    if (DBG) Log.d(TAG, "current");
                    mDiaperCheckMgr.setDemoValue(currentHumidity, currentVoc);
                    int diaperStatus = mDiaperCheckMgr.checkDemo();
                    setDiaperStatus(diaperStatus, DeviceStatus.DETECT_NONE);
                } else {
                    mDiaperCheckMgr = new DiaperCheckManager(mContext);
                }
            }
        }
    }

    public void setVoc(float value) {
        if (value != -1) {
            currentVoc = (int) (value * 10) / (float) 10.0;
        }
    }

    public void setVocAvg(float value) {
        if (value != -1) {
            currentVocAvg = (int) (value * 10) / (float) 10.0;
        }
    }

    public void setCapacity(float value) {
        if (value != -1) {
            currentCapacity = (int) (value * 10) / (float) 10.0;
        }
    }

    public void setAcceleration(float value) {
        if (value != -1) {
            currentAcceleration = (int) (value * 1000) / (float) 1000.0;
        }
    }

    public void setMultiTouch(float[] touch) {
        if (DBG) Log.d(TAG, "setMultiTouch obj: " + touch[0] + " / " + touch[1] + " / " + touch[2]);
        if (touch != null) {
            for (int i = 0; i < currentMultiTouch.length; i++) {
                currentMultiTouch[i] = touch[i];
            }

            currentContamination = 0;
            for (int i = 0; i < touch.length; i++) {
                if (currentMultiTouch[i] > DeviceStatus.THRESHOLD_TOUCH_LEVEL4) {
                    currentMultiTouchLevel[i] = 3;
                    currentContamination += 3;
                } else if (currentMultiTouch[i] > DeviceStatus.THRESHOLD_TOUCH_LEVEL3) {
                    currentMultiTouchLevel[i] = 2;
                    currentContamination += 2;
                } else if (currentMultiTouch[i] > DeviceStatus.THRESHOLD_TOUCH_LEVEL2) {
                    currentMultiTouchLevel[i] = 1;
                    currentContamination += 1;
                } else {
                    currentMultiTouchLevel[i] = 0;
                    currentContamination += 0;
                }
            }

            if (DBG) Log.d(TAG, "setContamination: " + currentContamination + "/" + (3 * touch.length));

            currentContamination = (int)((currentContamination + 0.0f) / (3 * touch.length) * 100);

            if (currentContamination > lastContamination + 10) { // 퍼센트가 10프로 이상 올라가면 카운트 증가
                if (DBG) Log.d(TAG, "contamination detected by touch");
                currentTouchDetectedCount++;
                latestTouchDetectedTimeMs = System.currentTimeMillis();
            }

            lastContamination = currentContamination;
        }
    }

    public void initTouchDetectedInfo() {
        if (DBG) Log.d(TAG, "initTouchDetectedInfo");
        currentTouchDetectedCount = 0;
        latestTouchDetectedTimeMs = -1;
    }

    public int getContamination() {
        return currentContamination;
    }

    public void setStrapAttached(boolean attached) {
        if (DBG) Log.d(TAG, "setStrapAttached: " + attached);
        isStrapAttached = attached;
    }

    public boolean getStrapAttached() {
        return isStrapAttached;
    }

    public void setMovementStatus(int status) {
        if (status != -1) {
            movementStatus = status;
//            if (movementStatus == 0) { // MovementLevel이 0이면 시작시간 판단
//                if (mMovementZeroStartMs == 0) {
//                    mMovementZeroStartMs = System.currentTimeMillis();
//                    if (DBG) Log.d(TAG, "set MovementZeroStartSec: " + mMovementZeroStartMs);
//                }
//            } else {
//                if (movementStatus >= 2) { // 움직임이 레벨2이상 감지되면
//                    if (mMovementZeroStartMs > 0 && System.currentTimeMillis() - mMovementZeroStartMs >  3 * 60 * 1000) { // 3분 이상 0을 유지하다가 움직임이 감지되면 움직임 알람주기
//                        mMovementZeroStartMs = 0;
//                        if (mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.DEVICE_ALL)
//                                && mPreferenceMgr.getDeviceAlarmEnabled(DeviceType.DIAPER_SENSOR, deviceId, NotificationType.MOVEMENT_DETECTED)) {
//                            NotiManager.getInstance(mContext).notifyMovementDetected(deviceId + (mCountNotification * 1000), mPreferenceMgr.getDeviceName(DeviceType.DIAPER_SENSOR, deviceId), System.currentTimeMillis());
//                        }
//                    }
//                }
//            }
        }
    }

    public int getMovementStatus() {
        return movementStatus;
    }

    public void updateDiaperStatus() {
        mDiaperDetectedTimeMs = mPreferenceMgr.getLatestDiaperDetectedTimeMs(deviceId);
        mDiaperStatusUpdatedTimeMs = mPreferenceMgr.getLatestDiaperStatusUpdatedTimeSec(deviceId) * 1000;
        diaperStatus = mPreferenceMgr.getDeviceStatus(DeviceType.DIAPER_SENSOR, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, DeviceStatus.DETECT_NONE);
    }

    /**
     * 기저귀 상태 감지
     */
    public void setDiaperStatus(int status, long utcTimeMs) {
        if (utcTimeMs == 0) utcTimeMs = System.currentTimeMillis();
        if (DBG) Log.d(TAG, "setDiaperStatus : " + diaperStatus + " -> " + status + " / " + utcTimeMs);

        PushMessage pushMsg = new PushMessage();
        pushMsg.deviceType = DeviceType.DIAPER_SENSOR;
        pushMsg.deviceId = deviceId;
        pushMsg.extra = "";
        pushMsg.ignoreLatestComparison = false;

        switch(status) {
            case DeviceStatus.DETECT_PEE:
                pushMsg.notiType = NotificationType.PEE_DETECTED;
                cntPeeDetected++;
                break;
            case DeviceStatus.DETECT_POO:
                pushMsg.notiType = NotificationType.POO_DETECTED;
                cntPooDetected++;
                break;
            case DeviceStatus.DETECT_ABNORMAL:
                pushMsg.notiType = NotificationType.ABNORMAL_DETECTED;
                cntAbnormalDetected++;
                break;
            case DeviceStatus.DETECT_FART:
                pushMsg.notiType = NotificationType.FART_DETECTED;
                cntFartDetected++;
                break;
            case DeviceStatus.DETECT_DIAPER_DETACHED:
                //pushMsg.notiType = NotificationType.DIAPER_DETACHMENT_DETECTED; // 로컬에서 처리하지 않음
                pushMsg.notiType = NotificationType.NONE;
                break;
            case DeviceStatus.DETECT_DIAPER_ATTACHED:
                pushMsg.notiType = NotificationType.NONE;
                break;
            case DeviceStatus.DIAPER_CHANGED_BY_USER: // 실제 유저가 버튼을 누른 경우
                pushMsg.notiType = NotificationType.DIAPER_CHANGED;
                pushMsg.ignoreLatestComparison = true;

                cntPeeDetected = 0;
                cntPooDetected = 0;
                cntAbnormalDetected = 0;
                cntFartDetected = 0;
                break;
            default:
                // 로컬에서 처리하지 않고, 서버에서 받아와서 보여준다면 notiType를 NONE로 설정
                pushMsg.notiType = NotificationType.NONE;
                break;
        }
        if ((Configuration.FAST_DETECTION && status != DeviceStatus.DETECT_NONE) || !Configuration.FAST_DETECTION) {
            pushMsg.timeMs = utcTimeMs;
            PushManager.getInstance(mContext).handlePushMessage(pushMsg);
        }
    }

    /**
     * 기저귀 초기화
     */
    public void initDetectedStatus(long utcTimeMs) {
        if (DBG) Log.d(TAG, "initDetectedStatus");
        if (utcTimeMs == 0) utcTimeMs = System.currentTimeMillis();

        //if (mPreferenceMgr.getLatestDiaperDetectedTimeMs(deviceId) < utcTimeMs) { // 기저귀 상태 감지된 것보다 최신 시간이면, 초기화하기
            mPreferenceMgr.setLatestDiaperDetectedTimeMs(deviceId, 0);
            mPreferenceMgr.setDeviceStatus(DeviceType.DIAPER_SENSOR, deviceId, DeviceStatus.DIAPER_SENSOR_DIAPER_STATUS, DeviceStatus.DETECT_NONE);
            if (Configuration.FAST_DETECTION && mDiaperCheckMgr != null) {
                mDiaperCheckMgr.initDemoValue();
            }
            updateDiaperStatus();
        //}
    }

    /**
     * 기저귀 교체
     */
    public void setDiaperChanged(long utcTimeMs) {
        if (DBG) Log.d(TAG, "setDiaperChanged");

        // UI 업데이트
        initDetectedStatus(utcTimeMs);
        // 푸시알람 전송
        setDiaperStatus(DeviceStatus.DIAPER_CHANGED_BY_USER, utcTimeMs);
        if (utcTimeMs > mRecentDiaperChangedTimeMs) mRecentDiaperChangedTimeMs = utcTimeMs;
        mDiaperChangedButtonPressedTimeMs = System.currentTimeMillis();
        mPreferenceMgr.setDiaperCheckNotificationShown(deviceId, 1, false);
        mPreferenceMgr.setDiaperCheckNotificationShown(deviceId, 2, false);

        // 서버로 부터 받아오기 전에 바로 점수 초기화
        setDiaperScore(100);
    }

    public int getDiaperStatus() {
        return diaperStatus;
    }

    public void setOperationStatus(int status) {
        if (status != -1) {
            if (DBG) Log.d(TAG, "setOperationStatus " + status);
            operationStatus = status;
        }
    }

    public int getOperationStatus() {
        return operationStatus;
    }
/*
    public void setBabyBirthday(String yyyymm) {
        if (yyyymm != null) {
            if (!yyyymm.startsWith("20")) {
                yyyymm = "20" + yyyymm;
            }
            if (yyyymm.length() > 6) {
                yyyymm = yyyymm.substring(0, 6);
            }
            babyBirthdayYYYYMM = yyyymm;
            try {
                int birthYear = Integer.valueOf(babyBirthdayYYYYMM.substring(0, 4));
                int birthMonth = Integer.valueOf(babyBirthdayYYYYMM.substring(4, 6));

                Calendar c = Calendar.getInstance();
                int nowYear = c.get(Calendar.YEAR);
                int nowMonth = c.get(Calendar.MONTH) + 1;

                babyMonths = (nowYear - birthYear) * 12 + (nowMonth - birthMonth);
                if (babyMonths < 0) {
                    babyMonths = 0;
                }
            } catch (Exception e) {
                babyMonths = 0;
            }
        }
    }
    public String getBabyBirthdayYYYYMM() {
        return babyBirthdayYYYYMM;
    }
    public String getBabyBirthdayYYYYMM() {
        return babyBirthdayYYYYMM;
    }
*/

    public void setBabyBirthdayYYMMDD(String yyMMdd) {
        if (yyMMdd == null || yyMMdd.length() != 6) return;

        babyBirthdayYYMMDD = yyMMdd;
        try {
            int birthYear = Integer.valueOf(babyBirthdayYYMMDD.substring(0, 2));
            int birthMonth = Integer.valueOf(babyBirthdayYYMMDD.substring(2, 4));
            int birthDay = Integer.valueOf(babyBirthdayYYMMDD.substring(4, 6));

            Calendar c = Calendar.getInstance();
            int nowYear = c.get(Calendar.YEAR);
            int nowMonth = c.get(Calendar.MONTH) + 1;

            c.set(birthYear, birthMonth - 1, birthDay);
            babyDdays = (int)((System.currentTimeMillis() - c.getTimeInMillis()) / 1000 / 60 / 60 / 24);

            babyMonths = (nowYear - birthYear) * 12 + (nowMonth - birthMonth);
            if (babyMonths < 0) {
                babyMonths = 0;
            }
        } catch (Exception e) {
            babyDdays = 0;
            babyMonths = 0;
        }
    }

    public String getBabyBirthdayYYMMDD() {
        return babyBirthdayYYMMDD;
    }

    public int getBabyDdays() {
        return babyDdays;
    }

    public int getBabyMonths() {
        return babyMonths;
    }

    public void setBabySex(int sex) {
        if (sex != -1) {
            babySex = sex;
        }
    }

    public int getBabySex() {
        return babySex;
    }

    public void setBabyEating(int eating) {
        if (eating != -1) {
            babyEating = eating;
        }
    }

    public int getBabyEating() {
        return babyEating;
    }

    public void setAdvertisingName(String advName) {
        if (advName != null) {
            advertisingName = advName;
        }
    }

    public String getAdvertisingName() {
        return advertisingName;
    }

    public void setBatteryPower(int power) {
        if (power != -1) {
            if (power > 100) {
                batteryPower = power / 100;
            } else {
                batteryPower = power;
            }
        }
    }

    public void setStrapBatteryPower(int power) {
        if (power != -1) {
            if (power > 100) {
                strapBatteryPower = power / 100;
            } else {
                strapBatteryPower = power;
            }
        }
    }

    public int getBatteryPower() {
        return batteryPower;
    }

    public int getStrapBatteryPower() {
        return strapBatteryPower;
    }

    public int insertDB(Context c) {
        if (c == null) return -1;
        return mDatabaseMgr.insertDB(this);
    }

    public int updateDB(Context c) {
        if (c == null) return -1;
        return mDatabaseMgr.updateDB(this);
    }

    public int deleteDB(Context c) {
        if (c == null) return -1;
        return mDatabaseMgr.deleteDB(this);
    }

    public void setSensitivity(int value) {
        sensitivity = value;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public long getDiaperDetectedTimeMs() {
        return mDiaperDetectedTimeMs;
    }

    public void setDiaperScore(int score) {
        if (score < 0) {
            score = 0;
        }
        diaperScore = score;
    }

    public int getDiaperScore() {
        return diaperScore;
    }

    public int getTotalScore() {
        // Score 100: Good
        // Score 70: Not good
        // Score 50: Bad
        // Score 30: Very Bad

        int score = 100; // 좋음

        score -= (cntPeeDetected * 10); // 소변1회당 10점 차감
        score -= (cntPooDetected * 30); // 대변1회당 30점 차감
        score -= (cntAbnormalDetected * 0); // 이상1회당 0점 차감
        score -= (cntFartDetected * 0); // 방귀1회당 0점 차감

        // 가장최근 기저귀 교체시간에 따라 점수 차감
        int diaperWearingTimeMinute = 0;
        if (mRecentDiaperChangedTimeMs > 0) {
            diaperWearingTimeMinute = (int) ((System.currentTimeMillis() - mRecentDiaperChangedTimeMs) / 1000 / 60);
            if (diaperWearingTimeMinute >= 4 * 60) {  // 4시간 이상되면 60점 차감
                score -= 60;
            } else if (diaperWearingTimeMinute >= 3 * 60) { // 3시간 이상되면 50점 차감
                score -= 50;
            } else if (diaperWearingTimeMinute >= 2 * 60) { // 2시간 이상 되면되면 20점 차감
                score -= 20;
            }
        }

        // 60점 이하일 경우, 기저귀교체시점 알람(Abnormal, notitype 8)
        if (score <= 60) {
            if (mPreviousDiaperChangedTimeMs < mRecentDiaperChangedTimeMs) {
                setDiaperStatus(DeviceStatus.DETECT_ABNORMAL, System.currentTimeMillis());
                mPreviousDiaperChangedTimeMs = mRecentDiaperChangedTimeMs;
                if (DBG) Log.d(TAG, "prev value succeeded: " + mRecentDiaperChangedTimeMs);
            } else {
                if (DBG) Log.d(TAG, "prev value failed");
            }
        }

        if (score < 0) {
            score = 0;
        }

        if (DBG) Log.d(TAG, "getTotalScore[" + deviceId + "]: " + cntPeeDetected + " / " + cntPooDetected + " / " + cntAbnormalDetected + " / " + cntFartDetected + " / " + diaperWearingTimeMinute + " / " + score);
        return score;
    }

    public void checkDiaperStatusForBaseLine() {
        long now = System.currentTimeMillis();
        long recentDiaperChangedTimeMs = getRecentDiaperChangedTimeMs();
        if (DBG) Log.d(TAG, "checkDiaperStatusForBaseLine: " + now + " / " + recentDiaperChangedTimeMs + " / " + (now - recentDiaperChangedTimeMs));
        // 가장 최근 기저귀 교체시점이 40분이 넘어가면 Notification 만들기
        if (now - recentDiaperChangedTimeMs >= 40 * 60 * 1000) {
            if (mPreferenceMgr.getDiaperCheckNotificationShown(deviceId, 2) == false) {
                mPreferenceMgr.setDiaperCheckNotificationShown(deviceId, 2, true);
                final NotificationMessage notiMsg = new NotificationMessage(NotificationType.CHAT_USER_FEEDBACK, DeviceType.DIAPER_SENSOR, deviceId, "d40", now);
                ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                        notiMsg,
                        new ServerManager.ServerResponseListener(){
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    ConnectionManager.getInstance().getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, deviceId);
                                    NotiManager.getInstance(mContext).notifyDiaperStatusCheckAlarm(deviceId, 40);
                                }
                            }
                        });
            }
        } else if (now - recentDiaperChangedTimeMs >= 10 * 60 * 1000) { // 가장 최근 기저귀 교체시점이 10분이 넘어가면 Notification 만들기
            if (mPreferenceMgr.getDiaperCheckNotificationShown(deviceId, 1) == false) {
                mPreferenceMgr.setDiaperCheckNotificationShown(deviceId, 1, true);
                final NotificationMessage notiMsg = new NotificationMessage(NotificationType.CHAT_USER_FEEDBACK, DeviceType.DIAPER_SENSOR, deviceId, "d10", now);
                ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                        notiMsg,
                        new ServerManager.ServerResponseListener(){
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    ConnectionManager.getInstance().getNotificationFromCloudV2(DeviceType.DIAPER_SENSOR, deviceId);
                                    NotiManager.getInstance(mContext).notifyDiaperStatusCheckAlarm(deviceId, 10);
                                }
                            }
                        });
            }
        }
    }
}