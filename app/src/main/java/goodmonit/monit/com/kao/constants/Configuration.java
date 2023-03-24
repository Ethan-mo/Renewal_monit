package goodmonit.monit.com.kao.constants;

import android.util.Log;

public class Configuration {
    public static final String BASE_TAG = "MonitKAO/";
    public static final String TAG = BASE_TAG + "Configuration";

    public static final boolean RELEASED			= true;
    public static final boolean DBG				    = !RELEASED;
    public static final boolean LIGHT_VERSION 	    = true;
    public static final boolean CHECK_UPDATE    	= true;
    public static final boolean RELEASE_SERVER      = true;
    public static final boolean ENABLE_YK_OAUTH2    = true;
    public static final boolean MONIT20             = true;
    public static final boolean MONIT20_HUB         = true;
    public static final boolean MONIT_ELDERLY_TEST  = false;
    public static final boolean MONIT_AUTO_SLEEP_DETECTION  = true;

    public static final int OS_ANDROID			    = 1;

    public static final int APP_GLOBAL			    = 0;
    public static final int APP_MONIT_X_HUGGIES	    = 1;
    public static final int APP_KC_HUGGIES_X_MONIT	= 2;
    public static final int APP_MONIT_X_KAO     	= 1;
    public static final int APP_MODE               	= APP_MONIT_X_KAO;

    /**
     * 데모용 Flag
     * FAST_DETECTION = TRUE => 5초간 습도증가에 소변검출
     */
    public static boolean FAST_DETECTION		    = false;

    /**
     * 상세로깅모드 Flag
     * LOGGING = TRUE => 상세로깅모드 Enable
     */
    public static boolean LOGGING				= !RELEASED;

    /**
     * 개발자모드 Flag
     * 펌웨어 업데이트 별도 가능, 소변/대변 Fake알람 가능
     * DEVELOPER = TRUE => 센서 펌웨어 업데이트 기능 ON
     */
    public static boolean DEVELOPER			= false;

    /**
     * 마스터모드 Flag
     * MASTER = TRUE => 모드 Enable
     */
    public static boolean MASTER			    = !RELEASED;

    /**
     * 인증용 Flag
     * CERTIFICATE_MODE = TRUE => 인터넷 안됨과 동일
     * SHOW_DEBUGGING_MENU = TRUE => 1초에 한번씩 들어오는 데이터를 확인할 수 있음
     */
    public static final boolean CERTIFICATE_MODE	= false;

    /**
     * 인터넷 안되는 지역에 갈 때, 샘플 Flag
     */
    public static final boolean NO_INTERNET	    = false;

    /**
     * 베타테스트용 Flag
     * BETA_TEST_MODE = TRUE : 1초에 한번씩 데이터 받아오도록 센서에 Polling메시지 전송
     * ALLOW_SAVE_LOCAL_HISTORY_FILE = TRUE : 1초에 한번씩 들어오는 데이터를 파일로 저장
     * SHOW_DATA_MINING_MENU = TRUE : 테스트 시작/종료, 20분에 한번씩 기저귀 소변/대변/이상없음 체크, 사용자 문구 입력하도록 판넬 띄우기
     * ALLOW_DIAPER_DETECT_FEEDBACK = TRUE : 기저귀센서 메시지를 선택해 맞음/틀림/모름/삭제 버튼을 입력할 수 있음
     */
    public static boolean BETA_TEST_MODE	            = false;
    public static boolean ALLOW_SAVE_LOCAL_HISTORY_FILE	= false;
    public static boolean SHOW_DATA_MINING_MENU	        = false;
    public static boolean ALLOW_DIAPER_DETECT_FEEDBACK	= false;

    /**
     * 수유등 등 신규제품 테스트용 Flag
     */
    public static boolean NEW_PRODUCT_MODE	            = false;

    /**
     * 3rd Party 업체 테스트용 업데이트 Flag
     */
    public static boolean DEVELOPER_3RD_PARTY           = false;

    /**
     * B2B모드 Flag
     */
    public static boolean B2B_MODE                      = false;

    public static void setRunningMode(int mode) {
        String log = "";
        if ((mode & 1) == 1) { // Master Flag
            Configuration.MASTER = true;
            log += "M";
        } else {
            Configuration.MASTER = false;
        }

        if ((mode & 2) == 2) {
            Configuration.BETA_TEST_MODE = true;
            Configuration.ALLOW_SAVE_LOCAL_HISTORY_FILE = false;
            Configuration.ALLOW_DIAPER_DETECT_FEEDBACK = true;
            Configuration.SHOW_DATA_MINING_MENU = true;
            log += "B";
        } else {
            Configuration.BETA_TEST_MODE = false;
            Configuration.ALLOW_SAVE_LOCAL_HISTORY_FILE = false;
            Configuration.ALLOW_DIAPER_DETECT_FEEDBACK = false;
            Configuration.SHOW_DATA_MINING_MENU = false;
        }

        if ((mode & 4) == 4) {
            //Configuration.SHOW_DATA_MINING_MENU = true;
            log += "D";
        } else {
            //Configuration.SHOW_DATA_MINING_MENU = false;
        }

        if ((mode & 8) == 8) {
            Configuration.FAST_DETECTION = true;
            log += "F";
        } else {
            Configuration.FAST_DETECTION = false;
        }

        if ((mode & 16) == 16) {
            Configuration.LOGGING = true;
            log += "L";
        } else {
            Configuration.LOGGING = false;
        }

        if ((mode & 32) == 32) {
            Configuration.DEVELOPER = true;
            log += "D";
        } else {
            Configuration.DEVELOPER = false;
        }

        if ((mode & 64) == 64) {
            Configuration.NEW_PRODUCT_MODE = true;
            log += "N";
        } else {
            Configuration.NEW_PRODUCT_MODE = false;
        }

        if ((mode & 128) == 128) {
            Configuration.DEVELOPER_3RD_PARTY = true;
            log += "3D";
        } else {
            Configuration.DEVELOPER_3RD_PARTY = false;
        }

        if ((mode & 256) == 256) {
            Configuration.B2B_MODE = true;
            log += "B";
        } else {
            Configuration.B2B_MODE = false;
        }
        Log.d(TAG, "mode: " + log);
    }
}