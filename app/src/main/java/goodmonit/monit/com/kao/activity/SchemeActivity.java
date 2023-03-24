package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Set;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.PushManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;

public class SchemeActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Scheme";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_SIGNIN_SUCCEEDED               = 1;
    private static final int MSG_SIGNIN_FAILED_SERVER_ERROR     = 2;
    private static final int MSG_SIGNIN_FAILED_INVALID_DATA     = 3;
    private static final int MSG_SIGNIN_FAILED_JSON_EXCEPTION   = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme);

        showProgressBar(true);
        mServerQueryMgr = ServerQueryManager.getInstance(mContext);
        mPreferenceMgr = PreferenceManager.getInstance(mContext);
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String paramFrom = null;
            String paramSiteCode = null;
            switch (Configuration.APP_MODE) {
                case Configuration.APP_GLOBAL: // Playground 에서 진입
                case Configuration.APP_KC_HUGGIES_X_MONIT:
//                case Configuration.APP_MONIT_X_KAO:
//                    paramFrom = uri.getQueryParameter(mServerQueryMgr.getParameter(73));
//                    if (paramFrom != null && paramFrom.toLowerCase().equals(mServerQueryMgr.getParameter(106))) {
//                        if (DBG) Log.d(TAG, "launch monit global");
//                        // 이미 실행되어있다면, 메인으로 바로 호출
//                        // 아니라면 처음부터 실행
//                        // 서버에 횟수 저장
//                        Intent signinIntent = new Intent(this, SplashActivity.class);
//                        startActivity(signinIntent);
//                        overridePendingTransition(0, 0);
//                        ServerQueryManager.getInstance(mContext).setChannelEvent(null);
//                        showProgressBar(false);
//                    }
//                    break;
                case Configuration.APP_MONIT_X_KAO:
                    _getScheme(uri.toString());
                    break;
            }
        }
    }

    private void _getScheme(String url) {
        if (DBG) Log.d(TAG, "_getScheme: " + url);
        if (url.contains(mServerQueryMgr.getParameter(144))) {
            if (DBG) Log.d(TAG, "orig: " + url);
            if (DBG) Log.d(TAG, "replace # to &");
            url = url.replace(mServerQueryMgr.getParameter(144), mServerQueryMgr.getParameter(145));
            if (DBG) Log.d(TAG, "rev: " + url);
        }
        Uri uri = Uri.parse(url);
        Set<String> paramNames = uri.getQueryParameterNames();
        LinkedHashMap<String, String> urimap = new LinkedHashMap<>();

        for (String paramName : paramNames) {
            if (DBG) Log.d(TAG, "URI Param: [" + paramName + "]");
            if (DBG) Log.d(TAG, "->" + uri.getQueryParameter(paramName));
            urimap.put(paramName, uri.getQueryParameter(paramName));
        }

        String paramFrom = uri.getQueryParameter(mServerQueryMgr.getParameter(73));
        String code = uri.getQueryParameter(mServerQueryMgr.getParameter(143));
        String idToken = uri.getQueryParameter(mServerQueryMgr.getParameter(141));
        String accessToken = uri.getQueryParameter(mServerQueryMgr.getParameter(142));
        if (DBG) Log.d(TAG, "params : " + uri.toString() + " -> \n[FROM]\n" + paramFrom + "\n[CODE]\n" + code + "\n[ID_TOKEN]\n" + idToken + "\n[ACCESS_TOKEN]\n" + accessToken);

        signinOAuth2(idToken, accessToken);
    }

    public void signinOAuth2(String idToken, String accessToken) {
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            if (DBG) Log.i(TAG, "Signin OAuth2.0");
            showProgressBar(true);
            mServerQueryMgr.YKOAuth2SignIn(idToken, accessToken, new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errCode, String data) {
                    showProgressBar(false);
                    if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            try {
                                JSONObject jObject = new JSONObject(data);
                                String email = jObject.getString(mServerQueryMgr.getParameter(12));
                                int myAccountId = jObject.getInt(mServerQueryMgr.getParameter(3));
                                String myToken = jObject.getString(mServerQueryMgr.getParameter(4));
                                String myNickname = jObject.getString(mServerQueryMgr.getParameter(25));
                                String myBirthday = jObject.getString(mServerQueryMgr.getParameter(17));
                                String shortId = jObject.getString(mServerQueryMgr.getParameter(22));
                                int mySex = jObject.getInt(mServerQueryMgr.getParameter(18));

                                mPreferenceMgr.setSigninEmail(email);
                                mPreferenceMgr.setShortId(shortId);
                                mPreferenceMgr.setAccountId(myAccountId);
                                mPreferenceMgr.setSigninToken(myToken);
                                mPreferenceMgr.setSigninState(SignInState.STEP_COMPLETED);

                                mPreferenceMgr.setProfileBirthday(myBirthday);
                                mPreferenceMgr.setProfileNickname(myNickname);
                                mPreferenceMgr.setProfileSex(mySex);
                                mPreferenceMgr.setNeedToUpdatePushToken(PushManager.PUSH_SERVICE_TYPE);
                                mHandler.sendEmptyMessage(MSG_SIGNIN_SUCCEEDED);
                            } catch (JSONException e) {
                                if (DBG) Log.e(TAG, "JSONException : " + e);
                                mHandler.sendEmptyMessage(MSG_SIGNIN_FAILED_JSON_EXCEPTION);
                            }
                        } else {
                            mHandler.sendEmptyMessage(MSG_SIGNIN_FAILED_INVALID_DATA);
                            if (DBG) Log.e(TAG, "errCode : " + errCode);
                        }
                    } else {
                        if (DBG) Log.e(TAG, "responseCode : " + responseCode);
                        mHandler.sendEmptyMessage(MSG_SIGNIN_FAILED_SERVER_ERROR);
                    }
                    showProgressBar(false);
                }
            });
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
                switch (msg.what) {
                    case MSG_SIGNIN_SUCCEEDED:
                        showToast(getString(R.string.toast_signin_succeeded));
                        Intent intent = null;
                        if (Configuration.LIGHT_VERSION) {
                            intent = new Intent(mContext, MainLightActivity.class);
                        } else {
                            intent = new Intent(mContext, MainActivity.class);
                        }
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
