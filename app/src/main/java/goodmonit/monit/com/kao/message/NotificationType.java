package goodmonit.monit.com.kao.message;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class NotificationType {
    public static final int NONE = 0;
    public static final int PEE_DETECTED = 1;
    public static final int POO_DETECTED = 2;
    public static final int ABNORMAL_DETECTED = 3;
    public static final int DIAPER_CHANGED = 4;
    public static final int FART_DETECTED = 5;
    public static final int DIAPER_DETACHMENT_DETECTED = 6;
    public static final int DIAPER_ATTACHMENT_DETECTED = 7;
    public static final int MOVEMENT_DETECTED = 8;

    public static final int DIAPER_INITIALIZATION_DETECTED = 12;

    public static final int LOW_TEMPERATURE = 21;
    public static final int HIGH_TEMPERATURE = 22;
    public static final int LOW_HUMIDITY = 23;
    public static final int HIGH_HUMIDITY = 24;
    public static final int VOC_WARNING = 25;

    public static final int LOW_BATTERY = 41;
    public static final int DISCONNECTED = 42;
    public static final int CONNECTED = 43;
    public static final int HUB_DISCONNECTED = 44;
    public static final int HUB_CONNECTED = 45;
    public static final int SENSOR_LONG_DISCONNECTED = 46;

    public static final int UPDATE_FULL_DATA = 50;
    public static final int UPDATE_CLOUD_DATA = 51;
    public static final int UPDATE_NOTIFICATION_EDIT_DATA = 52;

    public static final int MY_CLOUD_INVITE     = 60;
    public static final int MY_CLOUD_DELETE     = 61;
    public static final int MY_CLOUD_LEAVE      = 62;
    public static final int MY_CLOUD_REQUEST    = 63;
    public static final int OTHER_CLOUD_INVITED = 64;
    public static final int OTHER_CLOUD_DELETED = 65;
    public static final int OTHER_CLOUD_LEAVE   = 66;
    public static final int OTHER_CLOUD_REQUEST = 67;
    public static final int CLOUD_INIT_DEVICE   = 68;
    public static final int OAUTH_LOGIN_SUCCESS = 70;

    public static final int BABY_SLEEP          = 80;

    public static final int BABY_FEEDING        = 90;
    public static final int BABY_FEEDING_NURSED_BREAST_MILK     = 91;
    public static final int BABY_FEEDING_BOTTLE_BREAST_MILK     = 92;
    public static final int BABY_FEEDING_BOTTLE_FORMULA_MILK    = 93;
    public static final int BABY_FEEDING_BABY_FOOD              = 94;

    public static final int DEVICE_ALL = 100;

    public static final int DIAPER_NEED_TO_CHANGE = 110;
    public static final int DIAPER_SOILED = 111;

    public static final int DIAPER_AUTO_SLLEP_MONITOR = 120;

    public static final int CHAT_USER_INPUT     = 200;
    public static final int CHAT_DATE_LINE      = 201;
    public static final int CHAT_USER_FEEDBACK  = 202;
    public static final int SYSTEM_CUSTOM_PUSH  = 203;
    public static final int SYSTEM_DEVICE_NOT_FOUND             = 204;
    public static final int SYSTEM_DEVICE_ALREADY_REGISTERED    = 205;
    public static final int SYSTEM_TOO_SHORT_FULL_DISCOVERY     = 206;
    public static final int SYSTEM_LE_SCAN_FAILED               = 207;
    public static final int SYSTEM_FW_UPDATE_FAILED             = 208;
    public static final int SYSTEM_DEVICE_SCAN_LIST             = 209;

    public static int getStringResource(int notitype) {
        switch(notitype) {
            case NotificationType.PEE_DETECTED:
                return R.string.device_sensor_diaper_status_pee_detail;
            case NotificationType.POO_DETECTED:
                return R.string.device_sensor_diaper_status_poo_detail;
            case NotificationType.ABNORMAL_DETECTED:
                return R.string.device_sensor_diaper_status_abnormal_detail;
            case NotificationType.FART_DETECTED:
                return R.string.device_sensor_diaper_status_fart_detail;
            case NotificationType.DIAPER_CHANGED:
                return R.string.notification_diaper_changed;
            case NotificationType.CONNECTED:
                return R.string.device_sensor_operation_connected_detail;
            case NotificationType.DISCONNECTED:
                return R.string.device_sensor_operation_disconnected_detail;
            case NotificationType.HUB_CONNECTED:
                return R.string.device_sensor_operation_connected_detail;
            case NotificationType.HUB_DISCONNECTED:
                return R.string.device_sensor_operation_disconnected_detail;
            case NotificationType.LOW_BATTERY:
                return R.string.notification_low_battery_detail;
            case NotificationType.LOW_TEMPERATURE:
                return R.string.notification_environment_low_temperature_title;
            case NotificationType.HIGH_TEMPERATURE:
                return R.string.notification_environment_high_temperature_title;
            case NotificationType.LOW_HUMIDITY:
                return R.string.notification_environment_low_humidity_title;
            case NotificationType.HIGH_HUMIDITY:
                return R.string.notification_environment_high_humidity_title;
            case NotificationType.VOC_WARNING:
                return R.string.notification_environment_bad_voc_title;
            case NotificationType.MY_CLOUD_INVITE:
                return R.string.group_message_my_group_invite;
            case NotificationType.MY_CLOUD_DELETE:
                return R.string.group_message_my_group_delete;
            case NotificationType.MY_CLOUD_LEAVE:
                return R.string.group_message_my_group_leave;
            case NotificationType.MY_CLOUD_REQUEST:
                return R.string.group_message_my_group_request;
            case NotificationType.OTHER_CLOUD_INVITED:
                return R.string.group_message_other_group_invited;
            case NotificationType.OTHER_CLOUD_DELETED:
                return R.string.group_message_other_group_deleted;
            case NotificationType.OTHER_CLOUD_LEAVE:
                return R.string.group_message_other_group_leave;
            case NotificationType.OTHER_CLOUD_REQUEST:
                return R.string.group_message_other_group_request;
            case NotificationType.CLOUD_INIT_DEVICE:
                return R.string.group_message_init_device;
            case NotificationType.BABY_SLEEP:
                return R.string.notification_sleep_start;
            case NotificationType.BABY_FEEDING_BABY_FOOD:
                return R.string.notification_feeding_baby_food;
            case NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK:
                return R.string.notification_feeding_bottle_breast_milk;
            case NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK:
                return R.string.notification_feeding_bottle_formula_milk;
            case NotificationType.BABY_FEEDING_NURSED_BREAST_MILK:
                return R.string.notification_feeding_nursed_breast_milk;
            case NotificationType.DIAPER_NEED_TO_CHANGE:
                return R.string.notification_diaper_status_check_diaper;
            case NotificationType.DIAPER_DETACHMENT_DETECTED:
            default:
                return R.string.app_name;
        }
    }

    public static int getIconResource(int notitype) {
        switch(notitype) {
            // 센서관련
            case NotificationType.CONNECTED:
                return R.drawable.ic_sensor_operation_activated;
            case NotificationType.DISCONNECTED:
                return R.drawable.ic_sensor_operation_deactivated;
            case NotificationType.PEE_DETECTED:
                //return R.drawable.ic_notification_diaper_pee;
                return R.drawable.ic_diary_diaper_clean;
            case NotificationType.POO_DETECTED:
                //return R.drawable.ic_notification_diaper_poo;
                return R.drawable.ic_diary_diaper_clean;
            case NotificationType.FART_DETECTED:
                return R.drawable.ic_notification_diaper_fart;
            case NotificationType.ABNORMAL_DETECTED:
                return R.drawable.ic_notification_filter_abnormal_detected_activated;
            case NotificationType.DIAPER_CHANGED:
                return R.drawable.ic_notification_diaper_changed;

            // 허브 관련
            case NotificationType.HUB_CONNECTED:
                return R.drawable.bg_environment_score_100;
            case NotificationType.HUB_DISCONNECTED:
                return R.drawable.bg_environment_score_deactivated;
            case NotificationType.LOW_TEMPERATURE:
                if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                    return R.drawable.ic_environment_temperature_warning_low_notification_kc;
                } else {
                    return R.drawable.ic_notification_aqmhub_temperature;
                }
            case NotificationType.HIGH_TEMPERATURE:
                if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                    return R.drawable.ic_environment_temperature_warning_high_notification_kc;
                } else {
                    return R.drawable.ic_notification_aqmhub_temperature;
                }
            case NotificationType.LOW_HUMIDITY:
                if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                    return R.drawable.ic_environment_humidity_warning_low_notification_kc;
                } else {
                    return R.drawable.ic_notification_aqmhub_humidity;
                }
            case NotificationType.HIGH_HUMIDITY:
                if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                    return R.drawable.ic_environment_humidity_warning_high_notification_kc;
                } else {
                    return R.drawable.ic_notification_aqmhub_humidity;
                }
            case NotificationType.VOC_WARNING:
                return R.drawable.ic_environment_voc_activated;
            case NotificationType.MY_CLOUD_INVITE:
            case NotificationType.MY_CLOUD_REQUEST:
            case NotificationType.OTHER_CLOUD_INVITED:
            case NotificationType.OTHER_CLOUD_REQUEST:
                return R.drawable.ic_notification_group_add;
            case NotificationType.MY_CLOUD_DELETE:
            case NotificationType.MY_CLOUD_LEAVE:
            case NotificationType.OTHER_CLOUD_DELETED:
            case NotificationType.OTHER_CLOUD_LEAVE:
                return R.drawable.ic_notification_group_delete;
            case NotificationType.CLOUD_INIT_DEVICE:
                return R.drawable.ic_notification_group_init_device;
            case NotificationType.CHAT_USER_FEEDBACK:
                return R.drawable.ic_notification_user_comment;
            case NotificationType.CHAT_USER_INPUT:
                return R.drawable.ic_notification_user_comment;
            case NotificationType.DIAPER_DETACHMENT_DETECTED:
                return R.drawable.ic_sensor_diaper_warning_abnormal;
            case NotificationType.BABY_SLEEP:
                return R.drawable.ic_diary_sleep;
            case NotificationType.BABY_FEEDING_BABY_FOOD:
                return R.drawable.ic_diary_feeding_baby_food;
            case NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK:
                return R.drawable.ic_diary_feeding_bottle_breast_milk;
            case NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK:
                return R.drawable.ic_diary_feeding_bottle_formula_milk;
            case NotificationType.BABY_FEEDING_NURSED_BREAST_MILK:
                return R.drawable.ic_diary_feeding_nursed_breast_milk;
            case NotificationType.DIAPER_NEED_TO_CHANGE:
                return R.drawable.ic_diary_diaper_clean;
            default:
                return R.drawable.ic_notification_logo;
        }
    }
}