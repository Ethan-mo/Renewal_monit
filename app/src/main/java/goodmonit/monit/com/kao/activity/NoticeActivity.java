package goodmonit.monit.com.kao.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
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

import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;

public class NoticeActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Notice";
    private static final boolean DBG = Configuration.DBG;

    private WebView mWebView;
    private SimpleDialog mDlg_SslError;

    private int mBoardType;
    private int mBoardId;

    private String initialLoadingURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_notice);
        mPreferenceMgr = PreferenceManager.getInstance(mContext);
        mServerQueryMgr = ServerQueryManager.getInstance(mContext);
        mScreenInfo = new ScreenInfo(102);

        mBoardType = getIntent().getIntExtra("boardType", 0);
        mBoardId = getIntent().getIntExtra("boardId", 0);

        if (mBoardType > 0 && mBoardId > 0) {
            if (Configuration.RELEASE_SERVER) {
                initialLoadingURL = mServerQueryMgr.getParameter(390);
            } else {
                initialLoadingURL = mServerQueryMgr.getParameter(391);
            }
            initialLoadingURL += mServerQueryMgr.getParameter(123) + "=" + mBoardType;
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(124) + "=" + mBoardId;
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(6) + "=" + Configuration.OS_ANDROID;
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(7) + "=" + Configuration.APP_MODE;
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(5) + "=" + Locale.getDefault().getLanguage();
        } else {
            if (Configuration.RELEASE_SERVER) {
                initialLoadingURL = mServerQueryMgr.getParameter(390);
            } else {
                initialLoadingURL = mServerQueryMgr.getParameter(391);
            }
            initialLoadingURL += mServerQueryMgr.getParameter(123) + "=1";
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(6) + "=" + Configuration.OS_ANDROID;
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(7) + "=" + Configuration.APP_MODE;
            initialLoadingURL += "&" + mServerQueryMgr.getParameter(5) + "=" + Locale.getDefault().getLanguage();
        }

        _setToolBar();
        _initView();

        loadInitialUrlPage();
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        tvToolbarTitle = ((TextView) findViewById(R.id.tv_toolbar_title));
        tvToolbarTitle.setText(getString(R.string.notice_title));

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

    public void loadInitialUrlPage() {
        showProgressBar(true);
        mWebView.loadUrl(initialLoadingURL);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBoardType == 10) {
            tvToolbarTitle.setText(getString(R.string.help));
        } else if (mBoardType == 11) {
            tvToolbarTitle.setText(getString(R.string.help));
        } else if (mBoardType == 12) {
            tvToolbarTitle.setText(getString(R.string.help));
        } else {
            tvToolbarTitle.setText(getString(R.string.notice_title));
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            overridePendingTransition(0, 0);
            finish();
        }
    }

    private void _setProgress(int progress) {
        if (DBG) Log.d(TAG, "progress : " + progress);
    }

    private void _initView() {

        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
        mWebView = (WebView) findViewById(R.id.webview_notice);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebContentsDebuggingEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                _setProgress(progress * 100);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap b) {
                if (DBG) Log.i(TAG, "onPageStarted : " + url);
                showProgressBar(true);
            }

            // 페이지 읽기가 끝났을 때의 동작을 설정한다
            @Override
            public void onPageFinished(WebView view, String url) {
                showProgressBar(false);
                if (DBG) Log.i(TAG, "onPageFinished : " + url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (DBG) Log.i(TAG, "onLoadResource : " + url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (DBG)
                    Log.i(TAG, "shouldOverrideUrlLoading : " + request.getUrl().toString());
                view.loadUrl(request.getUrl().toString());
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
                if (DBG) Log.e(TAG, "onReceivedSslError : " + error.getUrl() + " / " + error.getPrimaryError() + " / " + error.getCertificate() + " / " + error.toString());
                if (mDlg_SslError == null) {
                    mDlg_SslError = new SimpleDialog(NoticeActivity.this,
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
    }
}
