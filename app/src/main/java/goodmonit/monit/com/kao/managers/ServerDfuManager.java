package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;

public class ServerDfuManager {
    public static final String TAG = Configuration.BASE_TAG + "ServerDfuMgr";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_CONNECTION_SEND_GET_METHOD     = 1;
    private static final int MSG_CONNECTION_SEND_POST_METHOD    = 2;
    private static final int MSG_CONNECTION_SOCKET_TIMEOUT      = 3;
    private static final long SOCKET_TIMEOUT_MS                 = 30 * 1000L;

    public static final int ERROR_CODE_TIMEOUT_EXCEPTION        = 900;
    public static final int ERROR_CODE_MALFORMED_URL_EXCEPTION  = 901;
    public static final int ERROR_CODE_IO_EXCEPTION             = 902;
    public static final int ERROR_CODE_ENCRYPTION_EXCEPTION     = 903;
    public static final int ERROR_CODE_INVALID_PACKET_EXCEPTION = 904;

    public static final int RESPONSE_CODE_OK = 200;

    private static final String CHAR_BOUNDARY = "*****";
    private static final String CHAR_LINE_END = "\r\n";
    private static final String CHAR_TWO_HYPHENS = "--";

    private static ServerDfuManager mServerManager;
    private static Context mContext;

    private PreferenceManager mPreferenceMgr;
    private em mEncryptionMgr;
    private boolean enableHTTPSConnection;
    private boolean mAllowAnyCertificate = true;
    public static final String FIRMWARE_FILE_NAME = "dfu.zip";

    private class ServerRequest {
        public String url;
        public HashMap<String, String> params;
        public ServerDfuResponseListener listener;
        public ServerRequest(String Url, HashMap<String, String> Params, ServerDfuResponseListener Listener) {
            url = Url;
            params = Params;
            listener = Listener;
        }
    }
    private int mToken;
    private HashMap<String, ServerRequest> mServerRequest;

    public static class ServerDfuResponseListener {
        public void onReceive(int responseCode, String errCode) {}

        public void onProgress(int percentage, int totalBytes) {}
    }

    public static ServerDfuManager getInstance(Context context) {
        if (mServerManager == null) {
            mServerManager = new ServerDfuManager(context);
        } else {
            mContext = context;
        }
        return mServerManager;
    }

    public ServerDfuManager(Context context) {
        mContext = context;
        mPreferenceMgr = PreferenceManager.getInstance(context);
        mEncryptionMgr = em.getInstance(context);
        mToken = 0;
        mServerRequest = new HashMap<String, ServerRequest>();
    }

    private boolean isHTTPSURL(String url) {
        return url.contains("https:");
    }

    private Handler mSocketHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_CONNECTION_SOCKET_TIMEOUT:
                    int token = msg.arg1;
                    ServerRequest serverRequest = mServerRequest.get(token + "");
                    if (serverRequest != null && serverRequest.listener != null) {
                        serverRequest.listener.onReceive(ERROR_CODE_TIMEOUT_EXCEPTION, null);
                        mServerRequest.remove(token + "");
                    }
                    break;
            }
        }
    };

    private Handler mSendMethodHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_CONNECTION_SEND_POST_METHOD:
                    int token2 = msg.arg1;
                    mSocketHandler.sendMessageDelayed(mSocketHandler.obtainMessage(MSG_CONNECTION_SOCKET_TIMEOUT, token2, -1), SOCKET_TIMEOUT_MS);
                    _sendPostMethod(token2);
                    break;
            }
        }
    };

    private void _sendPostMethod(final int token) {
        ServerRequest request = mServerRequest.get(token + "");
        if (request == null) {
            return;
        }
        final String urlString = request.url;
        final HashMap<String, String> params = request.params;
        final ServerDfuResponseListener onReceiveListener = request.listener;

        Thread thread = new Thread() {
            @Override
            public void run() {
                int responseCode = ERROR_CODE_IO_EXCEPTION;
                String receivedData = null;
                String errCode = null;
                String sendString = null;
                boolean notReadyForConnectingServer = false;
                boolean firmware = false;
                try {
                    String pkt = params.get(ServerQueryManager.getInstance(mContext).getParameter(2));
                    if (mEncryptionMgr.isReady()) { // 이미 암호화 키가 있음에도 다시 start를 하는 경우,
                        if (pkt.equals(ServerQueryManager.getInstance(mContext).getParameter(1040))) {
                            sendString = _getJsonStringFromParams(params);
                        } else {
                            sendString = mEncryptionMgr.getEncryptedString(_getJsonStringFromParams(params));
                        }
                    } else {
                        if (pkt.equals(ServerQueryManager.getInstance(mContext).getParameter(1040))) {
                            if (DBG) Log.e(TAG, "Encryption NOT READY");
                            sendString = _getJsonStringFromParams(params);
                        } else {
                            if (DBG) Log.e(TAG, "Encryption NOT READY Requesting...");
                            notReadyForConnectingServer = true;
                        }
                    }
                    if (sendString == null) {
                        if (DBG) Log.e(TAG, "sendString null");
                        notReadyForConnectingServer = true;
                    }

                    if (notReadyForConnectingServer) {
                        if (onReceiveListener != null) {
                            onReceiveListener.onReceive(responseCode, errCode);
                            mServerRequest.remove(token + "");
                        } else {
                            if (DBG) Log.e(TAG, "Receive Listener NULL");
                        }
                        return;
                    }

                    String finalUrl = urlString;
                    URL url = new URL(finalUrl);

                    if (DBG) Log.i(TAG, "sendPostMethod : " + finalUrl);

                    HttpURLConnection httpURLConnection = null;

                    enableHTTPSConnection = isHTTPSURL(finalUrl);
                    if (enableHTTPSConnection) {
                        _trustAllHosts();
                        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                        httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String s, SSLSession sslSession) {
                                if (mAllowAnyCertificate) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
                        httpURLConnection = httpsURLConnection;
                    } else {
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                    }

                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout((int)SOCKET_TIMEOUT_MS);
                    httpURLConnection.setReadTimeout((int)SOCKET_TIMEOUT_MS);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(sendString);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    httpURLConnection.connect();

                    StringBuilder responseStringBuilder = new StringBuilder();
                    responseCode = httpURLConnection.getResponseCode();
                    int fileSize = 0;
                    String log = "";

                    if (pkt.equals(ServerQueryManager.getInstance(mContext).getParameter(1074))) {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            String filePath = mContext.getFilesDir().getPath() + "/" + FIRMWARE_FILE_NAME;
                            //String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FIRMWARE_FILE_NAME;
                            File file = new File(filePath);
                            try {
                                if (file.exists()) {
                                    if (file.delete()) {
                                        if (DBG) Log.d(TAG, "delete succeeded");
                                    } else {
                                        if (DBG) Log.d(TAG, "delete failed");
                                    }
                                }
                                file.createNewFile();
                            } catch (Exception e) {

                            }

                            InputStream inStream = httpURLConnection.getInputStream();
                            FileOutputStream outStream = new FileOutputStream(file);

                            byte[] buf = new byte[1024];
                            int len = 0;
                            int total = 0;
                            fileSize = httpURLConnection.getContentLength();
                            while ((len = inStream.read(buf)) > 0) {
                                outStream.write(buf, 0, len);
                                total += len;
                                onReceiveListener.onProgress((int)((total + 0.0) / fileSize * 100), total);
                            }
                            outStream.close();
                            inStream.close();
                            receivedData = "{\"" + ServerQueryManager.getInstance(mContext).getParameter(1074) + "\":" + InternetErrorCode.SUCCEEDED + "}";
                            errCode = InternetErrorCode.SUCCEEDED;
                        } else {
                            receivedData = "{\"" + ServerQueryManager.getInstance(mContext).getParameter(1074) + "\":" + InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED + "}";
                            errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                        }

                    } else {
                        responseCode = ERROR_CODE_INVALID_PACKET_EXCEPTION;
                        receivedData = null;
                        errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    }

                    if (DBG) Log.i(TAG, "response code : " + responseCode + " / " + errCode + " / result : " + receivedData + " / " + fileSize);
                } catch (MalformedURLException e) {
                    responseCode = ERROR_CODE_MALFORMED_URL_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "MalformedURLException : " + e.toString());
                } catch (IOException e) {
                    responseCode = ERROR_CODE_IO_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "IOException : " + e.toString());
                } catch (NoSuchAlgorithmException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "NoSuchAlgorithmException : " + e.toString());
                } catch (InvalidKeyException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "InvalidKeyException : " + e.toString());
                } catch (InvalidAlgorithmParameterException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "InvalidAlgorithmParameterException : " + e.toString());
                } catch (NoSuchPaddingException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "NoSuchPaddingException : " + e.toString());
                } catch (BadPaddingException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "BadPaddingException : " + e.toString());
                } catch (IllegalBlockSizeException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "IllegalBlockSizeException : " + e.toString());
                } catch (IllegalArgumentException e) {
                    responseCode = ERROR_CODE_ENCRYPTION_EXCEPTION;
                    errCode = InternetErrorCode.ERR_FIRMWARE_DOWNLOAD_FAILED;
                    receivedData = null;
                    e.printStackTrace();
                    if (DBG) Log.e(TAG, "IllegalBlockSizeException : " + e.toString());
                }

                if (InternetErrorCode.ERR_INVALID_TOKEN.equals(errCode)) {
                    if (DBG) Log.d(TAG, "set InvalidTokenReceived");
                    mPreferenceMgr.setInvalidTokenReceived(true);
                }

                if (onReceiveListener != null) {
                    onReceiveListener.onReceive(responseCode, errCode);
                    mServerRequest.remove(token + "");
                } else {
                    if (DBG) Log.e(TAG, "Receive Listener NULL");
                }
            }
        };
        thread.start();
    }

    public static String getStringFromJSONObj(String data, String key) {
        if (data == null) return null;

        String value = null;
        try {
            JSONObject jObject = new JSONObject(data);
            value = URLDecoder.decode(jObject.getString(key), "UTF-8");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return value;
    }

    public static int getIntFromJSONObj(String data, String key) {
        try {
            return Integer.parseInt(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getIntFromJSONObj(String data, String key, int defaultValue) {
        try {
            return Integer.parseInt(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getLongFromJSONObj(String data, String key) {
        try {
            return Long.parseLong(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static long getLongFromJSONObj(String data, String key, long defaultValue) {
        try {
            return Long.parseLong(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float getFloatFromJSONObj(String data, String key) {
        try {
            return Float.parseFloat(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static float getFloatFromJSONObj(String data, String key, float defaultValue) {
        try {
            return Float.parseFloat(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double getDoubleFromJSONObj(String data, String key) {
        try {
            return Double.parseDouble(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double getDoubleFromJSONObj(String data, String key, double defaultValue) {
        try {
            return Double.parseDouble(getStringFromJSONObj(data, key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getJArrayFromJSONObj(String data, String key) {
        String value = null;
        try {
            JSONObject jObject = new JSONObject(data);
            value = URLDecoder.decode(jObject.getString(key), "UTF-8");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return value;
    }

    private void _trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                if (mAllowAnyCertificate) {
                    if (DBG) Log.d(TAG, "ClientTrusted");
                } else {
                    throw new java.security.cert.CertificateException();
                }
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                if (mAllowAnyCertificate) {
                    if (DBG) Log.d(TAG, "ServerTrusted");
                } else {
                    throw new java.security.cert.CertificateException();
                }
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String _getStringFromParams(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            //if (DBG) Log.d(TAG, "String : " + entry.getKey() + " / " + entry.getValue());
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private String _getJsonStringFromParams(HashMap<String, String> params) throws UnsupportedEncodingException {
        JSONObject sObject = new JSONObject();
        String dataValue = null;
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey() == null) {
                    sObject.put("", TextUtils.htmlEncode(entry.getValue()));
                    //sObject.put("", URLEncoder.encode(entry.getValue(), "UTF-8"));
                } else if (entry.getValue() == null) {
                    sObject.put(TextUtils.htmlEncode(entry.getKey()), "");
                    //sObject.put(URLEncoder.encode(entry.getKey(), "UTF-8"), "");
                } else if ("data".equals(entry.getKey())) {
                    dataValue = entry.getValue();
                } else {
                    sObject.put(TextUtils.htmlEncode(entry.getKey()), TextUtils.htmlEncode(entry.getValue()));
                    //sObject.put(URLEncoder.encode(entry.getKey(), "UTF-8"), URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
            }
            if (DBG) Log.i(TAG, "json created : " + sObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String retString = sObject.toString();
        if (dataValue != null) {
            retString = retString.substring(0, retString.length() - 1) + ",\"data\": " + dataValue + "}";
            if (DBG) Log.i(TAG, "added dataValue : " + retString);
        }
        return retString;
    }

    private int _putNewServerRequest(String url, HashMap<String, String> params, ServerDfuResponseListener listener) {
        mToken++;
        if (mToken > 10000) {
            mToken = 0;
        }
        mServerRequest.put(mToken + "", new ServerRequest(url, params, listener));
        return mToken;
    }

    public void sendPostMethod(String url, HashMap<String, String> params, ServerDfuResponseListener listener) {
        final int token = _putNewServerRequest(url, params, listener);
        mSendMethodHandler.sendMessage(mSendMethodHandler.obtainMessage(MSG_CONNECTION_SEND_POST_METHOD, token, -1));
    }
}