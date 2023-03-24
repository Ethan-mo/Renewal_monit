package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.sm;

public class YKSigninActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "YKSignin";
    private static final boolean DBG = Configuration.DBG;

    private Button btnSignin, btnSignup, btnMonitSignin;
    private ImageView ivLogo;
    private int mTouchCount;
    private sm mStringMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_signin_yk);
        mContext = this;
        mStringMgr = new sm();
        mTouchCount = 0;
        _initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");

        if (PreferenceManager.getInstance(mContext).getAppClosed() == true) {
            PreferenceManager.getInstance(mContext).setAppClosed(false);
            //finish(); 웹뷰사용시 주석처리, 브라우저사용시 주석해제
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void _initView() {
        ivLogo = (ImageView)findViewById(R.id.iv_activity_signin_logo);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTouchCount++;
                if (mTouchCount > 30) {
                    if (btnMonitSignin != null) {
                        btnMonitSignin.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        btnSignin = (Button)findViewById(R.id.btn_activity_signin_yk_signin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 웹뷰 띄우기
                Intent intent = new Intent(mContext, YKWebviewActivity.class);
                if (Configuration.ENABLE_YK_OAUTH2) {
                    intent.putExtra(YKWebviewActivity.YKWebViewMode, YKWebviewActivity.WEBVIEW_MODE_SIGNIN_OAUTH2);
                } else {
                    intent.putExtra(YKWebviewActivity.YKWebViewMode, YKWebviewActivity.WEBVIEW_MODE_SIGNIN);
                }
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri uri = null;
//                if (Configuration.RELEASE_SERVER) {
//                    uri = Uri.parse(mStringMgr.getParameter(260));
//                } else {
//                    uri = Uri.parse(mStringMgr.getParameter(258));
//                }
//                intent.setData(uri);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
            }
        });

        btnMonitSignin = (Button)findViewById(R.id.btn_activity_signin_monit_signin);
        btnMonitSignin.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // 웹뷰 띄우기
                 Intent intent = new Intent(YKSigninActivity.this, SigninActivity.class);
                 startActivity(intent);
                 overridePendingTransition(0, 0);
                 finish();
             }
         });
        btnMonitSignin.setVisibility(View.GONE);

        btnSignup = (Button)findViewById(R.id.btn_activity_signin_yk_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = null;
                if (Configuration.RELEASE_SERVER) {
                    uri = Uri.parse(mStringMgr.getParameter(261));

                } else {
                    uri = Uri.parse(mStringMgr.getParameter(259));
                }
                intent.setData(uri);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}
