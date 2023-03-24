package goodmonit.monit.com.kao.util;

import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;

public class LogUtil {
    public static void d(String tag, String text) {
        if (Configuration.LOGGING == true) {
            Log.d(tag, text);
        }
    }

    public static void i(String tag, String text) {
        if (Configuration.LOGGING == true) {
            Log.i(tag, text);
        }
    }

    public static void e(String tag, String text) {
        if (Configuration.LOGGING == true) {
            Log.e(tag, text);
        }
    }
}