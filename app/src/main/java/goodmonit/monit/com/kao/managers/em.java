package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import goodmonit.monit.com.kao.constants.Configuration;

/**
 * Created by Jake on 2017-05-08.
 */

public class em {
    private static final String TAG = Configuration.BASE_TAG + "em";
    private static final boolean DBG = Configuration.DBG;

    private static em mInstance;
    private Context mContext;
    private String mKeyIndex;
    private String mEncryptionKey;
    private String mInitialVector;
    private String mLocalKeyIndex;
    private String mLocalEncryptionKey;
    private String mLocalInitialVector;
    private boolean isReady;

    public static em getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new em(context);
        }
        return mInstance;
    }

    public em(Context context) {
        mContext = context;
        checkLocalAppData();
    }

    public boolean isReady() {
        return isReady;
    }

    public void setAppData(String data) {
        if (isValid(data)) {
            mEncryptionKey = getA();
            mInitialVector = getB();
            mKeyIndex = getC();
            isReady = true;
            if (DBG) Log.d(TAG, "setAppData: " + data + " / enc: " + mEncryptionKey + " / IV: " + mInitialVector + " / ki: " + mKeyIndex);
        }
    }

    public String genLocalAppData(boolean newData) {
        return genKey(newData);
    }

    public void setLocalAppData(String data) {
        if (isValidLocal(data)) {
            mLocalEncryptionKey = getLA();
            mLocalInitialVector = getLB();
            mLocalKeyIndex = getLC();
            if (DBG) Log.d(TAG, "setLocalAppData: " + data + " / enc: " + mLocalEncryptionKey + " / IV: " + mLocalInitialVector + " / ki: " + mLocalKeyIndex);
        }
    }

    public String getEncryptedString(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,	IllegalBlockSizeException, BadPaddingException {
        if (str == null) throw new IllegalArgumentException();
        byte[] utf8bytes = str.getBytes("UTF-8");
        if (utf8bytes.length == 0) throw new IllegalArgumentException();
        byte[] aesbytes = getE(utf8bytes);
        if (aesbytes.length == 0) throw new IllegalArgumentException();
        String jniAesString = new String(aesbytes, "UTF-8");
        return jniAesString;
    }

    public String getDecryptedString(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IllegalArgumentException {
        if (str == null) throw new IllegalArgumentException();
        byte[] base64bytes = Base64.decode(str, 0);
        if (base64bytes.length == 0) throw new IllegalArgumentException();
        byte[] aesbytes = getD(base64bytes);
        if (aesbytes.length == 0) throw new IllegalArgumentException();
        String jnidec = new String(aesbytes, "UTF-8");
        return jnidec;
    }

    public String getLocalEncryptedString(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,	IllegalBlockSizeException, BadPaddingException {
        if (str == null) throw new IllegalArgumentException();
        byte[] utf8bytes = str.getBytes("UTF-8");
        if (utf8bytes.length == 0) throw new IllegalArgumentException();
        byte[] aesbytes = getLE(utf8bytes);
        if (aesbytes.length == 0) throw new IllegalArgumentException();
        String jniAesString = new String(aesbytes, "UTF-8");
        return jniAesString;
    }

    public String getLocalDecryptedString(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IllegalArgumentException {
        if (str == null) throw new IllegalArgumentException();
        byte[] base64bytes = Base64.decode(str, 0);
        if (base64bytes.length == 0) throw new IllegalArgumentException();
        byte[] aesbytes = getLD(base64bytes);
        if (aesbytes.length == 0) throw new IllegalArgumentException();
        String jnidec = new String(aesbytes, "UTF-8");
        return jnidec;
    }

    public void checkLocalAppData() {
        PreferenceManager prefMgr = PreferenceManager.getInstance(mContext);
        // 로컬 키가 있는지 확인
        String appData = prefMgr.getLocalAppData();

        if (appData == null) {
            // 만약 이전에 사용중이던 앱이면, 기존 키 가져오기
            // 처음 시작된 앱이면 랜덤생성 후 저장
            if (prefMgr.isAppFirstLaunched() == null) {
                appData = genLocalAppData(true);
            } else {
                appData = genLocalAppData(false);
            }
            if (DBG) Log.e(TAG, "AppData not existed, generate AppData: " + appData);
            prefMgr.setLocalAppData(appData);
        } else {
            if (DBG) Log.e(TAG, "AppData: " + appData);
        }
        setLocalAppData(appData);
    }

    private native String genKey(boolean newData);
    private native boolean isValid(String data);
    private native String getA(); // getEncryptionKey
    private native String getB(); // getInitialVector
    private native String getC(); // getEncryptionKeyIndex
    private native boolean isValidLocal(String data);
    private native String getLA(); // getEncryptionKey
    private native String getLB(); // getInitialVector
    private native String getLC(); // getEncryptionKeyIndex
    private native byte[] getD(byte[] data); // getDecryptedString
    private native byte[] getE(byte[] data); // getEncryptedString+KeyIndex
    private native byte[] getLD(byte[] data); // getDecryptedString
    private native byte[] getLE(byte[] data); // getEncryptedString
    static {
        System.loadLibrary("em-lib");
    }
}