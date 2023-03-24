package goodmonit.monit.com.kao.devices;

/**
 * Created by Jake on 2017-06-15.
 */

public class SensorValueType {
    public static final char TEMPERATURE         = 't'; // float
    public static final char HUMIDITY            = 'h'; // float
    public static final char VOC                 = 'v'; // float
    public static final char BATTERY             = 'b'; // integer [0~200, 100이상은 충전중]
    public static final char TOUCH               = 'o'; // float
    public static final char ACCELERATION        = 'a'; // float

    public static final char CO2                 = 'c'; // float
    public static final char RAW_GAS             = 'r'; // float
    public static final char COMP_GAS            = 'g'; // float
    public static final char PRESSURE            = 'p'; // float
    public static final char ETHANOL             = 'e'; // float

    public static final char X_AXIS              = 'x'; // float
    public static final char Y_AXIS              = 'y'; // float
    public static final char Z_AXIS              = 'z'; // float

    public static final char MOVEMENT_STATUS     = 'M'; // int
    public static final char DIAPER_STATUS       = 'D'; // int
    public static final char OPERATION_STATUS    = 'O'; // int

    public static final char LAMP_POWER          = 'P';  // integer [1: on, 0: off]
    public static final char LAMP_BRIGHT         = 'B';  // integer [1: bright, 2: brighter, 3: much brighter]
    public static final char LAMP_COLOR          = 'C';  // integer [3700~6000]

    public static final char NAME                = 'N';
    public static final char DEVICE_ID           = 'I';
    public static final char CLOUD_ID            = 'L';
}