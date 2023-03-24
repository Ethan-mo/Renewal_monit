package goodmonit.monit.com.kao.constants;

/**
 * Created by Jake on 2017-05-08.
 */

public class BlePacketType {
    public static final byte DEVICE_ID			= (byte)0x01;
    public static final byte CLOUD_ID			= (byte)0x02;
    public static final byte HARDWARE_VERSION	= (byte)0x03;
    public static final byte FIRMWARE_VERSION	= (byte)0x04;
    public static final byte SENSOR_STATUS		= (byte)0x05;
    public static final byte LED_CONTROL		= (byte)0x06;
    public static final byte INITIALIZE			= (byte)0x07;
    public static final byte BABY_INFO		    = (byte)0x08;
    public static final byte DATE_INFO		    = (byte)0x09;

    public static final byte TOUCH				= (byte)0x10;
    public static final byte BATTERY			= (byte)0x11;
    public static final byte X_AXIS				= (byte)0x12;
    public static final byte Y_AXIS				= (byte)0x13;
    public static final byte Z_AXIS				= (byte)0x14;
    public static final byte ACCELERATION		= (byte)0x15;
    public static final byte TEMPERATURE		= (byte)0x20;
    public static final byte HUMIDITY			= (byte)0x21;
    public static final byte VOC				= (byte)0x22;
    public static final byte CO2				= (byte)0x23;
    public static final byte RAW_GAS			= (byte)0x24;
    public static final byte COMPENSATED_GAS	= (byte)0x25;
    public static final byte PRESSURE			= (byte)0x26;
    public static final byte ETHANOL			= (byte)0x27;
    public static final byte ACK				= (byte)0x30;
    public static final byte CERT				= (byte)0x50;
    public static final byte RESET				= (byte)0x0F;

    public static final byte REQUEST			= (byte)0x81;
    public static final byte AUTO_POLLING		= (byte)0x82;
    public static final byte PART_NUMBER		= (byte)0x90;
    public static final byte SERIAL_NUMBER		= (byte)0x91;
    public static final byte DEVICE_NAME		= (byte)0x92;
    public static final byte MAC_ADDRESS		= (byte)0x93;
    public static final byte UTC_TIME_INFO		= (byte)0x94;

    public static final byte HUB_DEVICE_ID			= (byte)0x40;
    public static final byte HUB_CLOUD_ID			= (byte)0x41;
    public static final byte HUB_FIRMWARE_VERSION	= (byte)0x42;
    public static final byte HUB_AP_SECURITY	    = (byte)0x43;
    public static final byte HUB_AP_CONNECTION_STATUS   = (byte)0x44;
    public static final byte KEEP_ALIVE			    = (byte)0x45;
    public static final byte ENTER_DFU			    = (byte)0x46;
    public static final byte SENSITIVITY		    = (byte)0x47;
    public static final byte DIAPER_PENDING_INFO    = (byte)0x48;
    public static final byte HEATING_DURATION_TIME  = (byte)0x49;
    public static final byte LAMP_BRIGHT_CTRL       = (byte)0x51;
    public static final byte ELDERLY_STRAP_BATTERY_STATUS      = (byte)0x52;

    public static final byte POO_DETECTION_TIME     = (byte)0x4A;
    public static final byte DIAPER_STATUS_COUNT    = (byte)0x4B;

    public static final byte HUB_AP_NAME		    = (byte)0xA0;
    public static final byte HUB_AP_PASSWORD	    = (byte)0xA1;
    public static final byte HUB_SERIAL_NUMBER	    = (byte)0xA2;
    public static final byte HUB_DEVICE_NAME	    = (byte)0xA3;
    public static final byte HUB_MAC_ADDRESS	    = (byte)0xA4;
    public static final byte HUB_WIFI_SCAN	        = (byte)0xA5;
    public static final byte DEBUG_COMMAND	        = (byte)0xA6;
    public static final byte LATEST_PEE_DETECTION_TIME  = (byte)0xA7;
    public static final byte LATEST_POO_DETECTION_TIME  = (byte)0xA8;
    public static final byte LATEST_ABNORMAL_DETECTION_TIME  = (byte)0xA9;
    public static final byte LATEST_FART_DETECTION_TIME  = (byte)0xAA;
    public static final byte LATEST_DETACHMENT_DETECTION_TIME  = (byte)0xAB;
    public static final byte LATEST_ATTACHMENT_DETECTION_TIME  = (byte)0xAC;

    public static final byte ELDERLY_STRAP_CAPACITANCE_STATUS  = (byte)0xB1;

    public static final byte ETC			        = (byte)0xAA;

}

