package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.security.InvalidParameterException;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

/**
 * Created by Jake on 2017-05-08.
 */

public class FirebaseAnalyticsManager {
    private static final String TAG = Configuration.BASE_TAG + "FirebaseMgr";

    public static final String TAG_ACCOUNT_TERMS_AND_CONDITIONS = "account_terms_conditions";
    public static final String TAG_ACCOUNT_PRIVACY_POLICY = "account_privacy_policy";
    public static final String TAG_ACCOUNT_CUSTOMER_SUPPORT = "account_customer_support";

    public static final String TAG_GROUP_INVITATION = "group";

    public static final String TAG_HUB_SETTING_TEMPERATURE_SCALE = "hub_setting_temperature_scale";
    public static final String TAG_HUB_SETTING_ALARM_ENABLED = "hub_setting_alarm_enabled";
    public static final String TAG_HUB_SETTING_TEMPERATURE_RANGE = "hub_setting_temperature_range";
    public static final String TAG_HUB_SETTING_HUMIDITY_RANGE = "hub_setting_humidity_range";
    public static final String TAG_HUB_SETTING_LED_INDICATOR_ENABLED = "hub_setting_led_indicator_enabled";
    public static final String TAG_HUB_SETTING_LED_INDICATOR_ENABLED_TIME = "hub_setting_led_indicator_enabled_time";

    public static final String TAG_SENSOR_ALARM_PEE_DETECTED = "sensor_alarm_pee_detected";
    public static final String TAG_SENSOR_ALARM_POO_DETECTED = "sensor_alarm_poo_detected";
    public static final String TAG_SENSOR_ALARM_FART_DETECTED = "sensor_alarm_fart_detected";
    public static final String TAG_SENSOR_ALARM_DIAPER_CHANGED = "sensor_alarm_diaper_changed";

    public static final String TAG_SENSOR_GRAPH_DIAPER_BUTTON_WEEKLY = "sensor_graph_diaper_button_weekly";
    public static final String TAG_SENSOR_GRAPH_DIAPER_BUTTON_MONTHLY = "sensor_graph_diaper_button_monthly";
    public static final String TAG_SENSOR_GRAPH_PEE_BUTTON_WEEKLY = "sensor_graph_pee_button_weekly";
    public static final String TAG_SENSOR_GRAPH_PEE_BUTTON_MONTHLY = "sensor_graph_pee_button_monthly";
    public static final String TAG_SENSOR_GRAPH_POO_BUTTON_WEEKLY = "sensor_graph_poo_button_weekly";
    public static final String TAG_SENSOR_GRAPH_POO_BUTTON_MONTHLY = "sensor_graph_poo_button_monthly";
    public static final String TAG_SENSOR_GRAPH_FART_BUTTON_WEEKLY = "sensor_graph_fart_button_weekly";
    public static final String TAG_SENSOR_GRAPH_FART_BUTTON_MONTHLY = "sensor_graph_fart_button_monthly";

    public static final String TAG_SENSOR_SETTING_ALARM_ENABLED = "sensor_setting_alarm_enabled";
    public static final String TAG_SENSOR_SETTING_PEE_ALARM_ENABLED = "sensor_setting_pee_alarm_enabled";
    public static final String TAG_SENSOR_SETTING_POO_ALARM_ENABLED = "sensor_setting_poo_alarm_enabled";
    public static final String TAG_SENSOR_SETTING_FART_ALARM_ENABLED = "sensor_setting_fart_alarm_enabled";
    public static final String TAG_SENSOR_SETTING_CONNECTION_ALARM_ENABLED = "sensor_setting_connection_alarm_enabled";

    public static final String TAG_HUB_ALARM_HIGH_TEMPERATURE_DETECTED = "hub_alarm_high_temperature_detected";
    public static final String TAG_HUB_ALARM_LOW_TEMPERATURE_DETECTED = "hub_alarm_low_temperature_detected";
    public static final String TAG_HUB_ALARM_HIGH_HUMIDITY_DETECTED = "hub_alarm_high_humidity_detected";
    public static final String TAG_HUB_ALARM_LOW_HUMIDITY_DETECTED = "hub_alarm_low_humidity_detected";

    public static final String TAG_HUB_GRAPH_TEMPERATURE = "hub_graph_temperature";
    public static final String TAG_HUB_GRAPH_HUMIDITY = "hub_graph_humidity";

    public static final String CATEGORY_ACCOUNT_ID = "accountid_";
    public static final String CATEGORY_HUB_ID = "hubid_";
    public static final String CATEGORY_SENSOR_ID = "sensorid_";

    private static FirebaseAnalyticsManager mInstance;
    private static FirebaseAnalytics mFirebaseAnalytics;
    private static Context mContext;

    public static FirebaseAnalyticsManager getInstance(Context context) {
        if (context == null) {
            throw new InvalidParameterException("null Context");
        }

        if (mInstance == null) {
            mInstance = new FirebaseAnalyticsManager(context);
        }

        return mInstance;
    }

    private FirebaseAnalyticsManager(Context context) {
        mContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void sendCustomerSupport(long accountId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_ACCOUNT_ID + accountId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_ACCOUNT_CUSTOMER_SUPPORT, bundle);
    }

    public void sendPrivacyPolicy(long accountId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_ACCOUNT_ID + accountId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_ACCOUNT_PRIVACY_POLICY, bundle);
    }

    public void sendTermsAndConditions(long accountId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_ACCOUNT_ID + accountId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_ACCOUNT_TERMS_AND_CONDITIONS, bundle);
    }

    public void sendGroupInvitation(long accountId, String name, String membershipCode) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_ACCOUNT_ID + accountId, name + "_" + membershipCode);
        mFirebaseAnalytics.logEvent(TAG_GROUP_INVITATION, bundle);
    }

    public void sendHubSettingTemperatureScale(long hubId, String scale) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, scale);
        mFirebaseAnalytics.logEvent(TAG_HUB_SETTING_TEMPERATURE_SCALE, bundle);
    }

    public void sendHubSettingAlarmEnabled(long hubId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_HUB_SETTING_ALARM_ENABLED, bundle);
    }

    public void sendHubSettingTemperatureRange(long hubId, float low, float high) {
        low = (int)(low * 100) / 100.0f;
        high = (int)(high * 100) / 100.0f;
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, low + "-" + high);
        mFirebaseAnalytics.logEvent(TAG_HUB_SETTING_TEMPERATURE_RANGE, bundle);
    }

    public void sendHubSettingHumidityRange(long hubId, float low, float high) {
        low = (int)(low * 100) / 100.0f;
        high = (int)(high * 100) / 100.0f;
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, low + "-" + high);
        mFirebaseAnalytics.logEvent(TAG_HUB_SETTING_HUMIDITY_RANGE, bundle);
    }

    public void sendHubSettingLedIndicatorEnabled(long hubId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_HUB_SETTING_LED_INDICATOR_ENABLED, bundle);
    }

    public void sendHubSettingLedIndicatorEnabledTime(long hubId, String from, String to) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, from + "-" + to);
        mFirebaseAnalytics.logEvent(TAG_HUB_SETTING_LED_INDICATOR_ENABLED_TIME, bundle);
    }

    public void sendHubAlarmHighTemperatureDetected(long hubId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_HUB_ALARM_HIGH_TEMPERATURE_DETECTED, bundle);
    }

    public void sendHubAlarmLowTemperatureDetected(long hubId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_HUB_ALARM_LOW_TEMPERATURE_DETECTED, bundle);
    }

    public void sendHubAlarmHighHumidityDetected(long hubId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_HUB_ALARM_HIGH_HUMIDITY_DETECTED, bundle);
    }

    public void sendHubAlarmLowHumidityDetected(long hubId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_HUB_ALARM_LOW_HUMIDITY_DETECTED, bundle);
    }

    public void sendHubGraphTemperature(long hubId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_HUB_GRAPH_TEMPERATURE, bundle);
    }

    public void sendHubGraphHumidity(long hubId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_HUB_ID + hubId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_HUB_GRAPH_HUMIDITY, bundle);
    }

    public void sendSensorAlarmPeeDetected(long sensorId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_ALARM_PEE_DETECTED, bundle);
    }

    public void sendSensorAlarmPooDetected(long sensorId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_ALARM_POO_DETECTED, bundle);
    }

    public void sendSensorAlarmFartDetected(long sensorId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_ALARM_FART_DETECTED, bundle);
    }

    public void sendSensorAlarmDiaperChanged(long sensorId, long utcTimeMs) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcTimeMs));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_ALARM_DIAPER_CHANGED, bundle);
    }

    public void sendSensorGraphDiaperWeekly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_DIAPER_BUTTON_WEEKLY, bundle);
    }

    public void sendSensorGraphDiaperMonthly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_DIAPER_BUTTON_MONTHLY, bundle);
    }

    public void sendSensorGraphPeeWeekly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_PEE_BUTTON_WEEKLY, bundle);
    }

    public void sendSensorGraphPeeMonthly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_PEE_BUTTON_MONTHLY, bundle);
    }

    public void sendSensorGraphPooWeekly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_POO_BUTTON_WEEKLY, bundle);
    }

    public void sendSensorGraphPooMonthly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_POO_BUTTON_MONTHLY, bundle);
    }

    public void sendSensorGraphFartWeekly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_FART_BUTTON_WEEKLY, bundle);
    }

    public void sendSensorGraphFartMonthly(long sensorId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(System.currentTimeMillis()));
        mFirebaseAnalytics.logEvent(TAG_SENSOR_GRAPH_FART_BUTTON_MONTHLY, bundle);
    }

    public void sendSensorSettingAlarmEnabled(long sensorId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_SENSOR_SETTING_ALARM_ENABLED, bundle);
    }

    public void sendSensorSettingPeeAlarmEnabled(long sensorId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_SENSOR_SETTING_PEE_ALARM_ENABLED, bundle);
    }

    public void sendSensorSettingPooAlarmEnabled(long sensorId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_SENSOR_SETTING_POO_ALARM_ENABLED, bundle);
    }

    public void sendSensorSettingFartAlarmEnabled(long sensorId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_SENSOR_SETTING_FART_ALARM_ENABLED, bundle);
    }

    public void sendSensorSettingConnectionAlarmEnabled(long sensorId, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalyticsManager.CATEGORY_SENSOR_ID + sensorId, enabled ? "true" : "false");
        mFirebaseAnalytics.logEvent(TAG_SENSOR_SETTING_CONNECTION_ALARM_ENABLED, bundle);
    }
}