package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Set;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.PushManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;

public class YKWebviewActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "YKWebview";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_SIGNIN_SUCCEEDED               = 1;
    private static final int MSG_SIGNIN_FAILED_SERVER_ERROR     = 2;
    private static final int MSG_SIGNIN_FAILED_INVALID_DATA     = 3;
    private static final int MSG_SIGNIN_FAILED_JSON_EXCEPTION   = 4;

    public static final String YKWebViewMode = "webview_mode";
    public static final int WEBVIEW_MODE_SIGNIN = 1;
    public static final int WEBVIEW_MODE_SIGNIN_OAUTH2 = 2;
    public static final int WEBVIEW_MODE_SIGNUP_OAUTH2 = 3;

    private WebView mWebView;
    private int mWebViewMode;
    private String initialLoadingURL;
    private SimpleDialog mDlg_LoginFailed;
    private SimpleDialog mDlg_LoginSucceeded;
    private SimpleDialog mDlg_SslError;
    private SimpleDialog mDlg_NotSupported;
    private String mSignInID;
    private long mSignInTimeMs;
    private String mSignInToken;

    private int mSignInServer = 251;
    private int mSignUpServer = 251;
    private int mSignInSucceededServer = 253;

    private boolean ssoRedirectKakao = false;
    private boolean ssoRedirectNaver = false;
    private boolean ssoRedirectFacebook = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_webview_yk);

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(mContext);
        mServerQueryMgr = ServerQueryManager.getInstance(mContext);
        mScreenInfo = new ScreenInfo(102);
        mWebViewMode = getIntent().getIntExtra(YKWebViewMode, 0);

        if (mWebViewMode == WEBVIEW_MODE_SIGNIN) {
            if (Configuration.RELEASE_SERVER) {
                mSignInServer = 251;
                mSignInSucceededServer = 253;
            } else {
                mSignInServer = 255;
                mSignInSucceededServer = 257;
            }
            initialLoadingURL = mServerQueryMgr.getParameter(mSignInServer);
        } else if (mWebViewMode == WEBVIEW_MODE_SIGNIN_OAUTH2) {
            if (Configuration.RELEASE_SERVER) {
                mSignInServer = 260;
                mSignInSucceededServer = 253;
            } else {
                mSignInServer = 258;
                mSignInSucceededServer = 262;
            }
            initialLoadingURL = mServerQueryMgr.getParameter(mSignInServer);
        } else if (mWebViewMode == WEBVIEW_MODE_SIGNUP_OAUTH2) {
            if (Configuration.RELEASE_SERVER) {
                mSignUpServer = 261;
                mSignInSucceededServer = 253;
            } else {
                mSignUpServer = 259;
                mSignInSucceededServer = 262;
            }
            initialLoadingURL = mServerQueryMgr.getParameter(mSignUpServer);
        }

        _setToolBar();
        _initView();

        loadInitialUrlPage();
    }

    private void _setToolBar() {
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
            tvToolbarTitle = ((TextView) findViewById(R.id.tv_toolbar_title));
            if (mWebViewMode == WEBVIEW_MODE_SIGNIN || mWebViewMode == WEBVIEW_MODE_SIGNIN_OAUTH2) {
                tvToolbarTitle.setText(getString(R.string.momq_yk_signin_title));
            }

            btnToolbarRight.setVisibility(View.GONE);
            btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
            btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
            btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    public void loadInitialUrlPage() {
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            showProgressBar(true);
            mWebView.loadUrl(initialLoadingURL);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
    }

    @Override
    public void onBackPressed() {
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                Intent intent = new Intent(mContext, YKSigninActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        }
    }

    private void _setProgress(int progress) {
        if (DBG) Log.d(TAG, "progress : " + progress);
    }

    private void _initView() {
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
            mWebView = (WebView) findViewById(R.id.webview_yk_signin);
            mDlg_LoginFailed = new SimpleDialog(
                    mContext,
                    getString(R.string.toast_signin_failed),
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlg_LoginFailed.dismiss();
                            loadInitialUrlPage();
                        }
                    }
            );

            mDlg_LoginSucceeded = new SimpleDialog(
                    mContext,
                    getString(R.string.toast_signin_succeeded),
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlg_LoginSucceeded.dismiss();
                            loadInitialUrlPage();
                        }
                    }
            );

            // 캐시 삭제
            mWebView.clearCache(true);
            mWebView.clearHistory();
            clearCookies(mContext);

            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            mWebView.setWebContentsDebuggingEnabled(true);
            mWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    _setProgress(progress * 100);
                }
            });

            mWebView.addJavascriptInterface(new JavaScriptPasser(this), "Goodmonit");
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, Bitmap b) {
                    if (DBG) Log.i(TAG, "onPageStarted : " + url);
                    showProgressBar(true);
                    if (url != null) {
                        if (url.toLowerCase().contains(mServerQueryMgr.getParameter(mSignInSucceededServer).toLowerCase())) {
                            mWebView.stopLoading();
                        } else {
                            if (DBG) Log.e(TAG, "NOT Matched : " + url);
                        }
                    }
                }

                // 페이지 읽기가 끝났을 때의 동작을 설정한다
                @Override
                public void onPageFinished(WebView view, String url) {
                    showProgressBar(false);
                    if (DBG) Log.i(TAG, "onPageFinished : " + url);

                    if (url != null) {
                        if ((url.startsWith(mServerQueryMgr.getParameter(263)) || url.startsWith(mServerQueryMgr.getParameter(264))) && ssoRedirectKakao == false) {
                            if (DBG) Log.d(TAG, "Kakao login");
                            ssoRedirectKakao = true;
                            view.loadUrl(mServerQueryMgr.getParameter(269));

                        } else if ((url.startsWith(mServerQueryMgr.getParameter(265)) || url.startsWith(mServerQueryMgr.getParameter(266))) && ssoRedirectFacebook == false) {
                            if (DBG) Log.d(TAG, "Facebook login");
                            ssoRedirectFacebook = true;
                            view.loadUrl(mServerQueryMgr.getParameter(269));

                        } else if ((url.startsWith(mServerQueryMgr.getParameter(267)) || url.startsWith(mServerQueryMgr.getParameter(268))) && ssoRedirectNaver == false) {
                            if (DBG) Log.d(TAG, "Naver login");
                            ssoRedirectNaver = true;
                            view.loadUrl(mServerQueryMgr.getParameter(269));

                        } else if (url.startsWith(mServerQueryMgr.getParameter(270)) || url.startsWith(mServerQueryMgr.getParameter(271)) || url.startsWith(mServerQueryMgr.getParameter(272))) {
                            if (mDlg_NotSupported == null) {
                                mDlg_NotSupported = new SimpleDialog(YKWebviewActivity.this,
                                        "아이디찾기, 비밀번호찾기, 회원가입은 PC에서 MomQ 사이트에 접속해서 이용해주세요",
                                        getString(R.string.btn_close),
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDlg_NotSupported.dismiss();
                                            }
                                        }
                                );
                            }
                            if (mDlg_NotSupported != null && !mDlg_NotSupported.isShowing()) {
                                try {
                                    mDlg_NotSupported.show();
                                } catch (Exception e) {

                                }
                            }
                            if (mWebView.canGoBack()) {
                                mWebView.goBack();
                            }
                        }
                    }
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    if (DBG) Log.i(TAG, "onLoadResource : " + url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (DBG) Log.i(TAG, "shouldOverrideUrlLoading : " + request.getUrl().toString());
                    String url = request.getUrl().toString();
                    if (url != null) {
                        if (url.startsWith(mServerQueryMgr.getParameter(146))) {
                            _getScheme(url);
                            //view.loadUrl(request.getUrl().toString());
                        } else {
                            view.loadUrl(request.getUrl().toString());
                        }
                    } else {
                        view.loadUrl(request.getUrl().toString());
                    }
                    return true;
                }

                @Override
                public void onPageCommitVisible(WebView view, String url) {
                    super.onPageCommitVisible(view, url);
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    if (DBG) Log.i(TAG, "onReceivedError");
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    super.onReceivedHttpError(view, request, errorResponse);
                    if (DBG) Log.i(TAG, "onReceivedHttpError");
                }

                @Override
                public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                    super.onFormResubmission(view, dontResend, resend);
                }

                @Override
                public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                    super.doUpdateVisitedHistory(view, url, isReload);
                }

                @Override
                public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                    if (DBG)
                        Log.e(TAG, "onReceivedSslError : " + error.getUrl() + " / " + error.getPrimaryError() + " / " + error.getCertificate() + " / " + error.toString());
                    if (mDlg_SslError == null) {
                        mDlg_SslError = new SimpleDialog(YKWebviewActivity.this,
                                getString(R.string.dialog_contents_webview_invalid_ssl_error),
                                getString(R.string.btn_no),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDlg_SslError.dismiss();
                                        handler.cancel();
                                    }
                                },
                                getString(R.string.btn_yes),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDlg_SslError.dismiss();
                                        handler.proceed();
                                    }
                                }
                        );
                        mDlg_SslError.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mDlg_SslError.dismiss();
                            }
                        });
                    }
                    if (!mDlg_SslError.isShowing()) {
                        try {
                            mDlg_SslError.show();
                        } catch (Exception e) {

                        }
                    }
                }

                @Override
                public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                    super.onReceivedClientCertRequest(view, request);
                    if (DBG) Log.i(TAG, "onReceivedClientCertRequest");
                }

                @Override
                public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm);
                    if (DBG) Log.i(TAG, "onReceivedHttpAuthRequest");
                }

                @Override
                public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                    return super.shouldOverrideKeyEvent(view, event);
                }

                @Override
                public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                    super.onUnhandledKeyEvent(view, event);
                }

                @Override
                public void onScaleChanged(WebView view, float oldScale, float newScale) {
                    super.onScaleChanged(view, oldScale, newScale);
                }

                @Override
                public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
                    super.onReceivedLoginRequest(view, realm, account, args);
                    if (DBG) Log.i(TAG, "onReceivedLoginRequest");
                }
            });

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message,
                                         final JsResult result) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle(getString(R.string.momq_alert))
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok,
                                    new AlertDialog.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.confirm();
                                        }
                                    })
                            .setCancelable(false)
                            .create()
                            .show();
                    return true;
                }

                @Override
                public boolean onJsConfirm(WebView view, String url, String message,
                                           final JsResult result) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle(getString(R.string.btn_ok))
                            .setMessage(message)
                            .setPositiveButton(getString(R.string.btn_yes),
                                    new AlertDialog.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.confirm();
                                        }
                                    })
                            .setNegativeButton(getString(R.string.btn_no),
                                    new AlertDialog.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.cancel();
                                        }
                                    })
                            .setCancelable(false)
                            .create()
                            .show();
                    return true;
                }
            });
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

    public void signinSucceeded() {
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            if (DBG) Log.i(TAG, "Signin Succeeded");

            showProgressBar(true);
            mServerQueryMgr.YKSignIn(mSignInID, mSignInTimeMs, mSignInToken, new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errCode, String data) {
                    showProgressBar(false);
                    if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            try {
                                JSONObject jObject = new JSONObject(data);
                                int myAccountId = jObject.getInt(mServerQueryMgr.getParameter(3));
                                String myToken = jObject.getString(mServerQueryMgr.getParameter(4));
                                String myNickname = jObject.getString(mServerQueryMgr.getParameter(25));
                                String myBirthday = jObject.getString(mServerQueryMgr.getParameter(17));
                                String shortId = jObject.getString(mServerQueryMgr.getParameter(22));
                                int mySex = jObject.getInt(mServerQueryMgr.getParameter(18));

                                mPreferenceMgr.setSigninEmail(mSignInID);
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
                }
            });
        }
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

                                mPreferenceMgr.setInvalidTokenReceived(false);
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
                }
            });
        }
    }

    public Handler mHandler = new Handler() {
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
                    case MSG_SIGNIN_FAILED_INVALID_DATA:
                        mDlg_LoginFailed.setContents(getString(R.string.toast_signin_failed) + "[Code: 400]");
                        try {
                            mDlg_LoginFailed.show();
                        } catch (Exception e) {

                        }
                        //showToast("로그인 정보가 잘못되었습니다.");
                        break;
                    case MSG_SIGNIN_FAILED_SERVER_ERROR:
                        mDlg_LoginFailed.setContents(getString(R.string.toast_signin_failed) + "[Code: 401]");
                        try {
                            mDlg_LoginFailed.show();
                        } catch (Exception e) {

                        }
                        //showToast("서버오류로 로그인이 실패했습니다.");
                        break;
                    case MSG_SIGNIN_FAILED_JSON_EXCEPTION:
                        mDlg_LoginFailed.setContents(getString(R.string.toast_signin_failed) + "[Code: 402]");
                        try {
                            mDlg_LoginFailed.show();
                        } catch (Exception e) {

                        }
                        //showToast("서버에서 받아온 데이터가 유효하지 않습니다.");
                        break;
                }

            }
        }
    };

    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (DBG) Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            if (DBG) Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public class JavaScriptPasser {
        private Context c;
        public JavaScriptPasser(Context context) {
            c = context;
        }

        @JavascriptInterface
        public void signin(String id, String token) {
            mSignInID = id;
            mSignInTimeMs = System.currentTimeMillis() / 1000;
            mSignInToken = token;
            if (DBG) Log.d(TAG, "signin: " + mSignInID + " / " + mSignInTimeMs + " / " + mSignInToken);
            signinSucceeded();
        }

        @JavascriptInterface
        public void signin(String id) {
            mSignInID = id;
            mSignInTimeMs = System.currentTimeMillis() / 1000;
            if (DBG) Log.d(TAG, "signin: " + mSignInID + " / " + mSignInTimeMs);
            signinSucceeded();
        }

        @JavascriptInterface
        public void openurl(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

        @JavascriptInterface
        public void closeWebView(String siteCode) {
            if (DBG) Log.d(TAG, "closeWebView: " + siteCode);
            if (siteCode == null) return;

            if ("yksso".equals(siteCode.toLowerCase())) {
                Intent intent = new Intent(mContext, YKSigninActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        }
    }
}
