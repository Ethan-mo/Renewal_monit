package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;

/**
 * Created by Jake on 2017-05-24.
 */

public class VersionManager {
    private static final String TAG = Configuration.BASE_TAG + "Version";
    private static final boolean DBG = Configuration.DBG;

    private Context mContext;
    private PreferenceManager mPreferenceMgr;
    private ServerQueryManager mServerQueryMgr;

    public VersionManager(Context context) {
        mContext = context;

        mPreferenceMgr = PreferenceManager.getInstance(context);
        mServerQueryMgr = ServerQueryManager.getInstance(context);
    }

    public void setLocalAppVersion() {
        PackageInfo pi = null;
        String packageVersion;
        try {
            pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            packageVersion = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            packageVersion = "1.0.0";
        }
        if (DBG) Log.d(TAG, "local ver : " + packageVersion);
        mPreferenceMgr.setLocalVersion(packageVersion);
    }

    /*
     * Deprecated
     * Raw파일 이름 가져오기
     */
    /*
    private void updateLatestDiaperSensorFirmwareVersion() {
        String latestVersion = "0.1.0";
        int resId = 0;
        for (Field field : R.raw.class.getFields()) {
            try {
                String fileName = field.getName();
                if (fileName == null) continue;
                if (fileName.contains("fw_diaper_sensor")) {
                    String[] version = fileName.split("_");
                    if (version.length < 6) continue;
                    latestVersion = version[3] + "." + version[4] + "." + version[5];
                    resId = field.getInt(field);
                }
            } catch (Exception e) {
                if (DBG) Log.e(TAG, "Raw Asset: " + e.toString());
            }

        }
        mPreferenceMgr.setDiaperSensorVersion(latestVersion);
        mPreferenceMgr.setDiaperSensorFwResId(resId);
        if (DBG) Log.d(TAG, "getLatestDiaperSensorFirmwareVersion : " + latestVersion + " / " + Integer.toHexString(resId));
    }
    */

    public boolean supportDiaperSensorFwUpdate(String fwversion) {
        if (fwversion == null) return false;
        String[] version = fwversion.split("\\.");
        if (version.length < 3) return false;
        if (Integer.parseInt(version[0]) == 0 && Integer.parseInt(version[1]) < 4) return false;  // FW버전 0.4.0 미만은 업데이트 불가
        return true;
    }

    public boolean supportDiaperSensitivity(String fwversion) {
        if (fwversion == null) return false;
        String[] version = fwversion.split("\\.");
        if (version.length < 3) return false;
        return checkSupportAvailable("1.0.3", fwversion); // 1.0.3 이상인 경우에만 민감도 설정 가능하게 하기
    }

    public boolean supportDiaperSensorHeatingTime(String fwversion) {
        if (fwversion == null) return false;
        String[] version = fwversion.split("\\.");
        if (version.length < 3) return false;
        return checkSupportAvailable("1.4.0", fwversion); // 1.4.0 이상인 경우에만 HeatingTimeMs 설정 가능하게 하기
    }

    public boolean revisedConnectionFalseNegativeAlert(String fwversion) {
        if (fwversion == null) return false;
        String[] version = fwversion.split("\\.");
        if (version.length < 3) return false;
        return checkSupportAvailable("1.3.5", fwversion); // 1.3.5 이상인 경우에만 HeatingTimeMs 설정 가능하게 하기
    }

    public boolean supportLampSetting(String fwversion) {
        if (fwversion == null) return false;
        return checkSupportAvailable("1.2.5", fwversion); // 1.2.5 이상인 경우에만 수유등 컨트롤하기
    }

    public boolean supportUpdatedLampMonitoring(String fwversion) {
        if (fwversion == null) return false;
        return checkSupportAvailable("1.1.0", fwversion); // 1.1.0 이상인 경우에만 수유등 컨트롤하기
    }

    public boolean supportMovementShowing(String fwversion) {
        if (fwversion == null) return false;
        return checkSupportAvailable("1.0.3", fwversion); // 1.0.3 이상인 경우에만 움직임 보여주기
    }

    public boolean checkDiaperSensorFwUpdateAvailable(String currentVersion, String latestVersion) {
        if (!supportDiaperSensorFwUpdate(currentVersion)) return false;
        return checkUpdateAvailable(currentVersion, latestVersion);
    }

    public boolean checkSupportAvailable(String currentVersion, String latestVersion) {
        if (DBG) Log.d(TAG, "checkSupportAvailable " + currentVersion + " / " + latestVersion);
        if (currentVersion.equals(latestVersion)) {
            return true;
        } else {
            return checkUpdateAvailable(currentVersion, latestVersion);
        }
    }

    public boolean checkUpdateAvailable(String currentVersion, String latestVersion) {
        if (DBG) Log.d(TAG, "checkUpdateAvailable " + currentVersion + " / " + latestVersion);
        boolean updateAvailable = false;
        if (currentVersion == null || latestVersion == null) {
            return updateAvailable;
        }
        if (!currentVersion.equals(latestVersion)) {
            String[] currentVersionCodeArray = currentVersion.split("\\.");
            String[] latestVersionCodeArray = latestVersion.split("\\.");

            int maxLength = currentVersionCodeArray.length;
            if (latestVersionCodeArray.length > maxLength) {
                maxLength = latestVersionCodeArray.length;
            }

            int appVersionDigit, marketVersionDigit;
            for (int i = 0; i < maxLength; i++){
                try {
                    appVersionDigit = Integer.parseInt(currentVersionCodeArray[i]);
                } catch (Exception e) {
                    appVersionDigit = 0;
                }
                try {
                    marketVersionDigit = Integer.parseInt(latestVersionCodeArray[i]);
                } catch (Exception e) {
                    marketVersionDigit = 0;
                }

                if (appVersionDigit > marketVersionDigit) {
                    updateAvailable = false;
                    break;
                } else if (appVersionDigit < marketVersionDigit) {
                    if (DBG) Log.d(TAG, "Update!!!");
                    updateAvailable = true;
                    break;
                }
            }
        }
        return updateAvailable;
    }
}
