package goodmonit.monit.com.kao.devices;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.message.NotificationType;

public class DeviceStatus {
    // DiaperSensor SensingMode
	public static final int MODE_DIAPER_SENSING             = 1;
    public static final int MODE_ENVIRONMENT_SENSING        = 2;

    public static final int DIAPER_SENSOR_OPERATION         = 0;
    public static final int DIAPER_SENSOR_MOVEMENT          = 1;
    public static final int DIAPER_SENSOR_DIAPER_STATUS     = 2;

    // DiaperSensor Operation
    public static final int OPERATION_SENSING               = 0;
    public static final int OPERATION_IDLE                  = 4;
    public static final int OPERATION_GAS_DETECTED          = 8;
    public static final int OPERATION_AVOID_SENSING         = 12;

    public static final int OPERATION_CABLE_NO_CHARGE       = 16;
    public static final int OPERATION_CABLE_CHARGING        = 17;
    public static final int OPERATION_CABLE_CHARGED_FULLY   = 18;
    public static final int OPERATION_CABLE_CHARGED_ERROR   = 19;

    public static final int OPERATION_HUB_NO_CHARGE         = 32;
    public static final int OPERATION_HUB_CHARGING          = 33;
    public static final int OPERATION_HUB_CHARGED_FULLY     = 34;
    public static final int OPERATION_HUB_CHARGED_ERROR     = 35;

    public static final int OPERATION_DEBUG_NO_CHARGE       = 48;
    public static final int OPERATION_DEBUG_CHARGING        = 49;
    public static final int OPERATION_DEBUG_CHARGED_FULLY   = 50;
    public static final int OPERATION_DEBUG_CHARGED_ERROR   = 51;

    public static final int OPERATION_STRAP_CONNECTED       = 64;
    public static final int OPERATION_STRAP_CONNECTED_IDLE  = 68;
    public static final int OPERATION_STRAP_CONNECTED_SENSING1 = 72;
    public static final int OPERATION_STRAP_CONNECTED_SENSING2 = 76;

    // DiaperSensor Movement
    public static final int MOVEMENT_DISCONNECTED           = -1; // 연결 끊김
    public static final int MOVEMENT_NO_MOVEMENT            = 0;  // 움직임 없음(z축 수평아님)
    public static final int MOVEMENT_SLEEP                  = 13; // 수면
    public static final int MOVEMENT_DEEP_SLEEP             = 14; // 깊은잠
    public static final int MOVEMENT_NOT_USING              = 15; // 사용안함(z축 수평: -1.05 ~ -0.95, 0.95 ~ 1.05)

    // DiaperSensor Detection
    public static final int DETECT_NONE             = 0;
    public static final int DETECT_PEE      	    = 1;
    public static final int DETECT_POO	            = 2;
    public static final int DETECT_ABNORMAL		    = 3;
    public static final int DETECT_FART			    = 5;
    public static final int DETECT_DIAPER_DETACHED	= 6;
    public static final int DETECT_DIAPER_ATTACHED	= 7;
    public static final int DIAPER_CHANGED_BY_USER	= 100;
    public static final int DETECT_CHECK_DIAPER     = 110;

    // Hub Bright
    public static final int BRIGHT_OFF 	    = 0;
    public static final int BRIGHT_ON1 	    = 16;
    public static final int BRIGHT_ON2 	    = 102;
    public static final int BRIGHT_ON3	    = 204;
    public static final int BRIGHT_ON4 	    = 511;
    public static final int BRIGHT_ON5	    = 1023;

    public static final int LAMP_POWER_OFF 	= 0;
    public static final int LAMP_POWER_ON 	= 1;

    // Hub Sensor Attached
    public static final int SENSOR_DETACHED 	= 0;

    public static final int BETA_DISABLED 	= 0;
    public static final int BETA_ENABLED 	= 1;

    public static final int SENSITIVITY_LOWEST  = 1;
    public static final int SENSITIVITY_LOWER   = 3;
    public static final int SENSITIVITY_NORMAL  = 5;
    public static final int SENSITIVITY_HIGHER  = 7;
    public static final int SENSITIVITY_HIGHEST = 9;

    public static final int THRESHOLD_TOUCH_LEVEL4 = 750;
    public static final int THRESHOLD_TOUCH_LEVEL3 = 700;
    public static final int THRESHOLD_TOUCH_LEVEL2 = 600;
    public static final int THRESHOLD_TOUCH_LEVEL1 = 500;

    public static int getDiaperStatusFromNotificationType(int notiType) {
        switch(notiType) {
            case NotificationType.PEE_DETECTED:
                return DETECT_PEE;
            case NotificationType.POO_DETECTED:
                return DETECT_POO;
            case NotificationType.FART_DETECTED:
                return DETECT_FART;
            case NotificationType.ABNORMAL_DETECTED:
                return DETECT_ABNORMAL;
            case NotificationType.DIAPER_CHANGED:
                return DETECT_NONE;
            case NotificationType.DIAPER_NEED_TO_CHANGE:
                return DETECT_CHECK_DIAPER;
            default:
                return DETECT_NONE;
        }
    }

    public static boolean isUpdatableDiaperStatus(int currentDiaperStatus, int updateDiaperStatus) {
        boolean updatable = false;

        switch (currentDiaperStatus) {
            case DeviceStatus.DETECT_NONE:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: updatable = true; break;
                    case DeviceStatus.DETECT_POO: updatable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: updatable = true; break;
                    case DeviceStatus.DETECT_FART: updatable = true; break;
                }
                break;
            case DeviceStatus.DETECT_PEE:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: break;
                    case DeviceStatus.DETECT_POO: updatable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: updatable = true; break;
                    case DeviceStatus.DETECT_FART: updatable = true; break;
                }
                break;
            case DeviceStatus.DETECT_POO:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: break;
                    case DeviceStatus.DETECT_POO: break;
                    case DeviceStatus.DETECT_ABNORMAL: updatable = true; break;
                    case DeviceStatus.DETECT_FART: break;
                }
                break;
            case DeviceStatus.DETECT_ABNORMAL:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: break;
                    case DeviceStatus.DETECT_POO: break;
                    case DeviceStatus.DETECT_ABNORMAL: break;
                    case DeviceStatus.DETECT_FART: break;
                }
                break;
            case DeviceStatus.DETECT_FART:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: updatable = true; break;
                    case DeviceStatus.DETECT_POO: updatable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: updatable = true; break;
                    case DeviceStatus.DETECT_FART: break;
                }
                break;
            default:
                break;
        }
        return updatable;
    }

    public static boolean isNotificationAvailableDiaperStatus(int currentDiaperStatus, int updateDiaperStatus) {
        boolean notifiable = false;

        switch (currentDiaperStatus) {
            case DeviceStatus.DETECT_NONE:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: notifiable = true; break;
                    case DeviceStatus.DETECT_POO: notifiable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: notifiable = true; break;
                    case DeviceStatus.DETECT_FART: notifiable = true; break;
                    case DeviceStatus.DETECT_CHECK_DIAPER: notifiable = true; break;
                }
                break;
            case DeviceStatus.DETECT_PEE:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: notifiable = true; break;
                    case DeviceStatus.DETECT_POO: notifiable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: notifiable = true; break;
                    case DeviceStatus.DETECT_FART: notifiable = true; break;
                    case DeviceStatus.DETECT_CHECK_DIAPER: notifiable = true; break;
                }
                break;
            case DeviceStatus.DETECT_POO:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: notifiable = true; break;
                    case DeviceStatus.DETECT_POO: notifiable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: notifiable = true; break;
                    case DeviceStatus.DETECT_FART: notifiable = true; break;
                    case DeviceStatus.DETECT_CHECK_DIAPER: notifiable = true; break;
                }
                break;
            case DeviceStatus.DETECT_ABNORMAL:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: notifiable = true; break;
                    case DeviceStatus.DETECT_POO: notifiable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: notifiable = true; break;
                    case DeviceStatus.DETECT_FART: notifiable = true; break;
                    case DeviceStatus.DETECT_CHECK_DIAPER: notifiable = true; break;
                }
                break;
            case DeviceStatus.DETECT_FART:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: notifiable = true; break;
                    case DeviceStatus.DETECT_POO: notifiable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: notifiable = true; break;
                    case DeviceStatus.DETECT_FART: notifiable = true; break;
                    case DeviceStatus.DETECT_CHECK_DIAPER: notifiable = true; break;
                }
                break;
            case DeviceStatus.DETECT_CHECK_DIAPER:
                switch(updateDiaperStatus) {
                    case DeviceStatus.DETECT_NONE: break;
                    case DeviceStatus.DETECT_PEE: notifiable = true; break;
                    case DeviceStatus.DETECT_POO: notifiable = true; break;
                    case DeviceStatus.DETECT_ABNORMAL: notifiable = true; break;
                    case DeviceStatus.DETECT_FART: notifiable = true; break;
                    case DeviceStatus.DETECT_CHECK_DIAPER: notifiable = true; break;
                }
                break;
            default:
                break;
        }
        return notifiable;
    }

    public static boolean isSensorCharging(int operationStatus) {
        switch (operationStatus) {
            case DeviceStatus.OPERATION_CABLE_CHARGED_ERROR:
            case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
            case DeviceStatus.OPERATION_CABLE_CHARGING:
            case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
            case DeviceStatus.OPERATION_HUB_CHARGED_ERROR:
            case DeviceStatus.OPERATION_HUB_NO_CHARGE:
            case DeviceStatus.OPERATION_HUB_CHARGING:
            case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
                return true;
            default:
                return false;
        }
    }

    public static int getSensorOperationImageResource(int operationStatus) {
        switch (operationStatus) {
            case DeviceStatus.OPERATION_IDLE:
                return R.drawable.ic_sensor_operation_idle;
            case DeviceStatus.OPERATION_GAS_DETECTED:
                return R.drawable.ic_sensor_operation_analyzing;
            case DeviceStatus.OPERATION_AVOID_SENSING:
                return R.drawable.ic_sensor_operation_analyzing;
            case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
            case DeviceStatus.OPERATION_CABLE_CHARGING:
            case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
            case DeviceStatus.OPERATION_HUB_NO_CHARGE:
            case DeviceStatus.OPERATION_HUB_CHARGING:
            case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
                return R.drawable.ic_sensor_operation_activated;
            case DeviceStatus.OPERATION_SENSING:
            default:
                return R.drawable.ic_sensor_operation_activated;
        }
    }

    public static int getSensorOperationStringResource(int operationStatus) {
        switch (operationStatus) {
            case DeviceStatus.OPERATION_IDLE:
                return R.string.device_sensor_operation_idle;
            case DeviceStatus.OPERATION_GAS_DETECTED:
                return R.string.device_sensor_operation_analyzing;
            case DeviceStatus.OPERATION_AVOID_SENSING:
                return R.string.device_sensor_operation_analyzing;
            case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
            case DeviceStatus.OPERATION_CABLE_CHARGING:
            case DeviceStatus.OPERATION_HUB_NO_CHARGE:
            case DeviceStatus.OPERATION_HUB_CHARGING:
                return R.string.device_sensor_operation_charging;
            case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
            case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
                return R.string.device_sensor_operation_fully_charged;
            case DeviceStatus.OPERATION_SENSING:
            default:
                return R.string.device_sensor_operation_sensing;
        }
    }


    public static int getDiaperStatusImageResource(int diaperStatus) {
        switch (diaperStatus) {
            case DeviceStatus.DETECT_PEE:
                return R.drawable.ic_sensor_diaper_warning_pee;
            case DeviceStatus.DETECT_POO:
                return R.drawable.ic_sensor_diaper_warning_poo;
            case DeviceStatus.DETECT_ABNORMAL:
                return R.drawable.ic_sensor_diaper_warning_abnormal;
            case DeviceStatus.DETECT_FART:
                return R.drawable.ic_sensor_diaper_warning_fart;
            case DeviceStatus.DETECT_NONE:
            default:
                return R.drawable.ic_sensor_diaper_activated;
        }
    }

    public static int getDiaperStatusStringResource(int diaperStatus) {
        switch (diaperStatus) {
            case DeviceStatus.DETECT_PEE:
                return R.string.device_sensor_diaper_status_pee;
            case DeviceStatus.DETECT_POO:
                return R.string.device_sensor_diaper_status_poo;
            case DeviceStatus.DETECT_ABNORMAL:
                return R.string.device_sensor_diaper_status_abnormal;
            case DeviceStatus.DETECT_FART:
                return R.string.device_sensor_diaper_status_fart;
            case DeviceStatus.DETECT_NONE:
            default:
                return R.string.device_sensor_diaper_status_normal;
        }
    }

    public static int getBatteryImageResource(int batteryPower, boolean isCharging) {
        if (isCharging && batteryPower == 100) {
            return R.drawable.ic_sensor_diaper_battery_charged;
        } else if (isCharging) {
            return R.drawable.ic_sensor_diaper_battery_charging;
        } else if (batteryPower == 100) {
            return R.drawable.ic_sensor_diaper_battery_100;
        } else if (batteryPower >= 90) {
            return R.drawable.ic_sensor_diaper_battery_9x;
        } else if (batteryPower >= 80) {
            return R.drawable.ic_sensor_diaper_battery_8x;
        } else if (batteryPower >= 70) {
            return R.drawable.ic_sensor_diaper_battery_7x;
        } else if (batteryPower >= 60) {
            return R.drawable.ic_sensor_diaper_battery_6x;
        } else if (batteryPower >= 50) {
            return R.drawable.ic_sensor_diaper_battery_5x;
        } else if (batteryPower >= 40) {
            return R.drawable.ic_sensor_diaper_battery_4x;
        } else if (batteryPower >= 30) {
            return R.drawable.ic_sensor_diaper_battery_3x;
        } else if (batteryPower >= 20) {
            return R.drawable.ic_sensor_diaper_battery_2x;
        } else if (batteryPower > 0) {
            return R.drawable.ic_sensor_diaper_battery_1x;
        } else {
            return R.drawable.ic_sensor_diaper_battery_0;
        }
    }

    public static int getMovementStringResource(int movementLevel) {
        switch (movementLevel) {
            case DeviceStatus.MOVEMENT_NOT_USING: // (15)
                return R.string.movement_not_using;
            case DeviceStatus.MOVEMENT_DISCONNECTED: // (-1)
                return R.string.movement_disconnected;
            case DeviceStatus.MOVEMENT_DEEP_SLEEP: // (14)
                return R.string.movement_deep_sleep;
            case DeviceStatus.MOVEMENT_SLEEP: // (13)
                return R.string.movement_sleep;
            case 0:
                return R.string.movement_not_moving;
            case 1:
            case 2:
            case 3:
            case 4:
                return R.string.device_sensor_movement_sleeping;
            case 5:
            case 6:
            case 7:
            case 8:
                return R.string.device_sensor_movement_crawling;
            case 9:
            case 10:
            case 11:
            case 12:
            default:
                return R.string.device_sensor_movement_running;
        }
    }

    public static int getMovementColorResource(int movementLevel) {
        switch(movementLevel) {
            case DeviceStatus.MOVEMENT_DISCONNECTED:
            case DeviceStatus.MOVEMENT_NOT_USING:
                return R.color.colorTextNotSelected;
            case DeviceStatus.MOVEMENT_SLEEP:
                return R.color.colorTextDiaperCategoryTransparent;
            case DeviceStatus.MOVEMENT_DEEP_SLEEP:
            case DeviceStatus.MOVEMENT_NO_MOVEMENT:
                return R.color.colorTextDiaperCategory;
            case 1:
            case 2:
            case 3:
            case 4:
                return R.color.colorTextWarningBlue;
            case 5:
            case 6:
            case 7:
            case 8:
                return R.color.colorTextWarningOrange;
            case 9:
            case 10:
            case 11:
            case 12:
            default:
                return R.color.colorTextWarning;
        }
    }

    public static int getMovementLevelFromValue(char charValue) {
        int value;
        switch (charValue) {
            case '0': value = DeviceStatus.MOVEMENT_NO_MOVEMENT; break;
            case '1': value = 1; break;
            case '2': value = 2; break;
            case '3': value = 3; break;
            case '4': value = 4; break;
            case '5': value = 5; break;
            case '6': value = 6; break;
            case '7': value = 7; break;
            case '8': value = 8; break;
            case '9': value = 9; break;
            case 'A': value = 10; break;
            case 'B': value = 11; break;
            case 'C': value = 12; break;
            case 'D': value = 13; break;
            case 'E': value = DeviceStatus.MOVEMENT_NOT_USING; break;
            case 'F': value = DeviceStatus.MOVEMENT_DISCONNECTED; break;
            default : value = DeviceStatus.MOVEMENT_DISCONNECTED; break;
        }
        return value;
    }

    public static int getMovementIcon(int movementLevel, int animationIndex) {
        switch (movementLevel) {
            case 0:
                if (animationIndex % 2 == 0) {
                    return R.drawable.ic_sensor_movement_none1;
                } else {
                    return R.drawable.ic_sensor_movement_none2;
                }
            case 1:
            case 2:
            case 3:
            case 4:
                if (animationIndex % 2 == 0) {
                    return R.drawable.ic_sensor_movement_crawling1;
                } else {
                    return R.drawable.ic_sensor_movement_crawling2;
                }
            case 5:
            case 6:
            case 7:
            case 8:
                if (animationIndex % 2 == 0) {
                    return R.drawable.ic_sensor_movement_walking1;
                } else {
                    return R.drawable.ic_sensor_movement_walking2;
                }
            case 9:
            case 10:
            case 11:
            case 12:
                if (animationIndex % 2 == 0) {
                    return R.drawable.ic_sensor_movement_running1;
                } else {
                    return R.drawable.ic_sensor_movement_running2;
                }
            case DeviceStatus.MOVEMENT_DEEP_SLEEP: // (14)
            case DeviceStatus.MOVEMENT_SLEEP: // (13)
                if (animationIndex % 2 == 0) {
                    return R.drawable.ic_sensor_movement_sleep1;
                } else {
                    return R.drawable.ic_sensor_movement_sleep2;
                }
            case DeviceStatus.MOVEMENT_NOT_USING: // (15)
            case DeviceStatus.MOVEMENT_DISCONNECTED: // (-1)
            default:
                return 0;
        }
    }

    public static int getDiaperSensorVocStringColorResource(float voc) {
        if (voc <= 0) {
            return R.color.colorTextPrimary;
        } else if (voc <= 100) {
            return R.color.colorTextWarningOrange;
        } else if (voc <= 300) {
            return R.color.colorTextWarning;
        } else if (voc <= 1000) {
            return R.color.colorTextPrimary;
        } else {
            return R.color.colorTextPrimary;
        }
    }

    public static int getDiaperSensorVocIconResource(float voc) {
        if (voc <= 0) {
            return R.drawable.ic_sensor_voc_good;
        } else if (voc <= 100) {
            return R.drawable.ic_sensor_voc_warning_orange;
        } else if (voc <= 300) {
            return R.drawable.ic_sensor_voc_warning_red;
        } else if (voc <= 1000) {
            return R.drawable.ic_sensor_voc_warning_black;
        } else {
            return R.drawable.ic_sensor_voc_warning_black;
        }
    }

    public static int getDiaperSensorVocWidgetResource(float voc) {
        if (voc <= 0) {
            return R.drawable.bg_btn_oval_voc_clean;
        } else if (voc <= 100) {
            return R.drawable.bg_btn_oval_voc_warning1;
        } else if (voc <= 300) {
            return R.drawable.bg_btn_oval_voc_warning2;
        } else if (voc <= 1000) {
            return R.drawable.bg_btn_oval_voc_warning3;
        } else {
            return R.drawable.bg_btn_oval_voc_warning3;
        }
    }

    public static int getDiaperSensorVocStringResource(float voc) {
        if (voc == 0) {
            return R.string.device_environment_voc_avg_level0;
        } else if (voc <= 100) {
            return R.string.device_environment_voc_avg_level1;
        } else if (voc <= 300) {
            return R.string.device_environment_voc_avg_level2;
        } else if (voc <= 1000) {
            return R.string.device_environment_voc_avg_level3;
        } else {
            return R.string.device_environment_voc_avg_level4;
        }
    }
    /*
    public static int getDiaperSensorVocStringResource(float voc) {
        if (voc <= 100) {
            return R.string.device_environment_voc_good;
        } else if (voc <= 300) {
            return R.string.device_environment_voc_not_good;
        } else if (voc <= 1000) {
            return R.string.device_environment_voc_bad;
        } else {
            return R.string.device_environment_voc_very_bad;
        }
    }
    */
    public static int getDiaperScoreIconResource(int score) {
        if (score >= 90) {
            return R.drawable.ic_sensor_diaper_activated;
        } else if (score >= 60) {
            return R.drawable.ic_sensor_diaper_activated_phase1;
        } else {
            return R.drawable.ic_sensor_diaper_activated_phase2;
        }
    }

    public static int getDiaperScoreWidgetResource(int score) {
        if (score >= 90) {
            return R.drawable.bg_btn_oval_diaper_clean;
        } else if (score >= 60) {
            return R.drawable.bg_btn_oval_diaper_soiled;
        } else {
            return R.drawable.bg_btn_oval_diaper_warning;
        }
    }

    public static int getDiaperScoreColorResource(int score) {
        if (score >= 90) {
            return R.color.colorTextPrimary;
        } else if (score >= 60) {
            return R.color.colorTextWarningOrange;
        } else {
            return R.color.colorTextWarning;
        }
    }

    public static int getDiaperScoreStringResource(int score) {
        if (score >= 90) {
            return R.string.device_sensor_diaper_status_normal;
        } else if (score >= 60) {
            return R.string.device_sensor_diaper_status_soiled;
        } else {
            return R.string.device_sensor_diaper_status_check_diaper;
        }
    }

    public static int getDiaperScoreBabyFeelingStringResource(int score) {
        if (score >= 90) {
            return R.string.device_sensor_baby_status_good_detail;
        } else if (score >= 60) {
            return R.string.device_sensor_baby_status_good_detail;
        } else {
            return R.string.device_sensor_baby_status_check_diaper_detail;
        }
    }
}