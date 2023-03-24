package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.dialog.ProgressCircleDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.managers.em;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class SplashActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Splash";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_ANIMATION_STOP         = 1;
    private static final int MSG_SHOW_TIMEOUT_DIALOG    = 2;
    private static final int MSG_VERSION_CHECK_FINISHED = 3;
    private static final int MSG_SESSION_CHECK_FINISHED = 4;

    private static final long SPLASH_SHOWING_TIME_MS = 2000;

    private ImageView ivSplashAnimation;
    private VersionManager mAppVersionMgr;

    private SimpleDialog mDlgSessionTimeout, mDlgException;
    private ProgressCircleDialog mDlgSelectNotification;
    private SimpleDialog mDlgUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DBG) Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_splash);

        mContext = this;
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mPreferenceMgr = PreferenceManager.getInstance(this);

        if (ConnectionManager.getInstance(null) == null) {
            if (DBG) Log.e(TAG, "start service4");
            startService(new Intent(this, ConnectionManager.class));
        }

        _initView();
        _showSplash();
    }

    public void checkLatestVersion() {
        //showProgressBar(true);
        mAppVersionMgr = new VersionManager(this);
        mAppVersionMgr.setLocalAppVersion();

        mServerQueryMgr.getLatestInfo(new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errcode, String data) {
                //showProgressBar(false);
                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                    try {
                        JSONObject jObject = new JSONObject(data);
                        String appVersion = jObject.getString(mServerQueryMgr.getParameter(77));
                        String diaperSensorVersion = jObject.getString(mServerQueryMgr.getParameter(78));
                        String hubVersion = jObject.getString(mServerQueryMgr.getParameter(79));
                        String lampVersion = jObject.getString(mServerQueryMgr.getParameter(147));
                        String diaperSensorForceVersion = jObject.getString(mServerQueryMgr.getParameter(149));
                        String hubForceVersion = jObject.getString(mServerQueryMgr.getParameter(150));
                        String lampForceVersion = jObject.getString(mServerQueryMgr.getParameter(151));
                        mPreferenceMgr.setMarketVersion(appVersion);
                        mPreferenceMgr.setDiaperSensorVersion(diaperSensorVersion);
                        mPreferenceMgr.setHubVersion(hubVersion);
                        mPreferenceMgr.setLampVersion(lampVersion);
                        mPreferenceMgr.setDiaperSensorForceVersion(diaperSensorForceVersion);
                        mPreferenceMgr.setHubForceVersion(hubForceVersion);
                        mPreferenceMgr.setLampForceVersion(lampForceVersion);
                    } catch (JSONException e) {
                        if (DBG) Log.e(TAG, e.toString());
                    }
                    mHandler.sendEmptyMessage(MSG_VERSION_CHECK_FINISHED);
                } else if (responseCode == ServerManager.ERROR_CODE_TIMEOUT_EXCEPTION ||
                        responseCode == ServerManager.ERROR_CODE_IO_EXCEPTION) {
                    mHandler.obtainMessage(MSG_SHOW_TIMEOUT_DIALOG, 0, 0).sendToTarget();
                    if (DBG) Log.e(TAG, "responseCode(" + responseCode + ")");
                } else {
                    mHandler.sendEmptyMessage(MSG_VERSION_CHECK_FINISHED);
                    if (DBG) Log.e(TAG, "responseCode(" + responseCode + ")");
                }
            }
        });

        mServerQueryMgr.getDemoInfo(new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errcode, String data) {
                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                    try {
                        JSONObject jObject = new JSONObject(data);
                        int threshold = jObject.getInt(mServerQueryMgr.getParameter(94));
                        int count = jObject.getInt(mServerQueryMgr.getParameter(95));
                        double alarmDelaySec = jObject.getDouble(mServerQueryMgr.getParameter(96));
                        double ignoreDelaySec = jObject.getDouble(mServerQueryMgr.getParameter(97));
                        mPreferenceMgr.setDemoInfoThreshold(threshold / 100.0f);
                        mPreferenceMgr.setDemoInfoCount(count);
                        mPreferenceMgr.setDemoInfoIgnoreDelaySec((float)ignoreDelaySec);
                        mPreferenceMgr.setDemoInfoAlarmDelaySec((float)alarmDelaySec);
                    } catch (JSONException e) {
                        if (DBG) Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ANIMATION_STOP:
                    ivSplashAnimation.setVisibility(View.GONE);
                    break;
                case MSG_VERSION_CHECK_FINISHED:
                    if (Configuration.NO_INTERNET) {
                        mPreferenceMgr.setSigninState(SignInState.STEP_COMPLETED);
                        mPreferenceMgr.setSigninEmail("mom@goodmonit.com");
                        mPreferenceMgr.setProfileBirthday("19800101");
                        mPreferenceMgr.setProfileNickname("Monit");
                        mPreferenceMgr.setShortId("ABC123");
                        mPreferenceMgr.setProfileSex(0);
                        _next();
                        break;
                    }

                    mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
                    if (mPreferenceMgr.getSigninEmail() != null && mPreferenceMgr.getSigninToken() != null) {
                        _checkSession();
                    } else {
                        _next();
                    }
                    break;

                case MSG_SESSION_CHECK_FINISHED:
                    _next();
                    break;

                case MSG_SHOW_TIMEOUT_DIALOG:
                    if (mDlgSessionTimeout == null) {
                        mDlgSessionTimeout = new SimpleDialog(SplashActivity.this,
                                getString(R.string.dialog_contents_err_communication_with_server, ServerManager.ERROR_CODE_TIMEOUT_EXCEPTION + ""),
                                getString(R.string.btn_finish),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDlgSessionTimeout.dismiss();
                                        finish();
                                    }
                                },
                                getString(R.string.btn_try_again),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDlgSessionTimeout.dismiss();
                                        _checkSession();
                                    }
                                }
                                );
                    }
                    try {
                        mDlgSessionTimeout.show();
                    } catch (Exception e) {

                    }
                    break;
            }
        }
    };

    private void _next() {
        if (mHandler.hasMessages(MSG_ANIMATION_STOP)) {
            mHandler.sendEmptyMessageDelayed(MSG_SESSION_CHECK_FINISHED, 300);
            return;
        }

        //showProgressBar(false);
        if (!Configuration.CHECK_UPDATE || Configuration.CERTIFICATE_MODE) {
            if (DBG) Log.e(TAG, "ignore update");
        } else if (mAppVersionMgr.checkUpdateAvailable(mPreferenceMgr.getLocalVersion(), mPreferenceMgr.getMarketVersion())) {
            String contents = "";
            switch (Configuration.APP_MODE) {
                case Configuration.APP_GLOBAL:
                case Configuration.APP_KC_HUGGIES_X_MONIT:
                //case Configuration.APP_MONIT_X_HUGGIES:
                case Configuration.APP_MONIT_X_KAO:
                    contents = getString(R.string.dialog_contents_update_available);
                    mDlgUpdate = new SimpleDialog(this,
                            contents,
                            getString(R.string.btn_finish),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    finish();
                                }
                            },
                            getString(R.string.btn_update),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    final String appPackageName = getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                    finish();
                                }
                            });
                    try {
                        mDlgUpdate.show();
                    } catch (Exception e) {

                    }
                    break;
            }
            return;
        }

        int signinState = mPreferenceMgr.getSigninState();
        if (DBG) Log.d(TAG, "signinState : " + signinState);

        Intent intent = null;
        switch(signinState) {
            case SignInState.STEP_SIGN_IN:
                switch (Configuration.APP_MODE) {
                    case Configuration.APP_GLOBAL:
                    case Configuration.APP_KC_HUGGIES_X_MONIT:
                    case Configuration.APP_MONIT_X_KAO:
                        intent = new Intent(this, SigninActivity.class);
                        break;
//                    case Configuration.APP_MONIT_X_HUGGIES:
//                        intent = new Intent(this, YKSigninActivity.class);
//                        break;
                }
                break;
            case SignInState.STEP_AUTHENTICATE_EMAIL:
            case SignInState.STEP_MORE_INFO:
            case SignInState.STEP_WELCOME:
                intent = new Intent(this, SignupActivity.class);
                intent.putExtra("startContent", signinState);
                break;
            case SignInState.STEP_COMPLETED:
                if (Configuration.LIGHT_VERSION) {
                    intent = new Intent(this, MainLightActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                break;
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        overridePendingTransition(0, 0);
        finish();
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
        ivSplashAnimation = (ImageView) findViewById(R.id.iv_activity_splash_logo);
    }

    private void _showSplash() {
        ivSplashAnimation.setVisibility(View.GONE);
        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            case Configuration.APP_MONIT_X_KAO:
                ivSplashAnimation.setImageResource(R.drawable.global_splash);
                break;
            case Configuration.APP_KC_HUGGIES_X_MONIT:
                ivSplashAnimation.setImageResource(R.drawable.kc_splash);
                break;
//            case Configuration.APP_MONIT_X_HUGGIES:
//                ivSplashAnimation.setImageResource(R.drawable.yk_splash);
//                break;
        }

        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);

        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {;}

            @Override
            public void onAnimationEnd(Animation animation) {
//                Animation fadeout = AnimationUtils.loadAnimation(mContext, R.anim.anim_fade_out);
//                ivSplashAnimation.setAnimation(fadeout);
//                ivSplashAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {;}
        });
        ivSplashAnimation.setAnimation(fadein);
        ivSplashAnimation.setVisibility(View.VISIBLE);

        mHandler.sendEmptyMessageDelayed(MSG_ANIMATION_STOP, SPLASH_SHOWING_TIME_MS);
    }

/*
    private AnimationManager mAnimationMgr;
    private void _showMonitSplash() {
        mAnimationMgr = new AnimationManager(ivSplashAnimation);
        int[] animationResource = new int[28];
        int start = R.drawable.splash_001;
        for (int i = 0; i < 28; i++) {
            animationResource[i] = start + i;
        }
        mAnimationMgr.addAllFrames(animationResource, 100);
        mAnimationMgr.start();

        mHandler.sendEmptyMessageDelayed(MSG_ANIMATION_STOP, 3000);
    }
*/
    private void _checkSession() {
        //showProgressBar(true);
        String token = mPreferenceMgr.getSigninToken();
        if (DBG) Log.d(TAG, "checkSession : " + token);
        if (token != null) {
            mServerQueryMgr.init(new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errcode, String data) {
                    //showProgressBar(false);
                    if (DBG) Log.d(TAG, "init : " + responseCode + " / " + errcode + " / " + data);
                    if (responseCode == ServerManager.RESPONSE_CODE_OK && InternetErrorCode.SUCCEEDED.equals(errcode)) {
                        int step = ServerManager.getIntFromJSONObj(data, mServerQueryMgr.getParameter(69));
                        switch(step) {
                            case SignInState.STEP_AUTHENTICATE_EMAIL:
                                mPreferenceMgr.setSigninState(SignInState.STEP_AUTHENTICATE_EMAIL);
                                break;
                            case SignInState.STEP_MORE_INFO:
                                mPreferenceMgr.setSigninState(SignInState.STEP_MORE_INFO);
                                break;
                            case SignInState.STEP_COMPLETED:
                                mPreferenceMgr.setSigninState(SignInState.STEP_COMPLETED);
                                break;
                            case SignInState.STEP_WELCOME:
                                mPreferenceMgr.setSigninState(SignInState.STEP_WELCOME);
                                break;
                            default:
                                mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
                                break;
                        }

                        int runningMode = ServerManager.getIntFromJSONObj(data, mServerQueryMgr.getParameter(70));
                        Configuration.setRunningMode(runningMode);
                        mHandler.sendEmptyMessage(MSG_SESSION_CHECK_FINISHED);
                    } else if (responseCode == ServerManager.RESPONSE_CODE_OK && InternetErrorCode.ERR_INVALID_TOKEN.equals(errcode)) {
                        if (DBG) Log.d(TAG, "InvalidTokenReceived");
                        mPreferenceMgr.setInvalidTokenReceived(false);
                        showToast(mContext.getString(R.string.toast_invalid_user_session));
                        UserInfoManager.getInstance(mContext).signout();
                        Intent intent = null;

                        switch (Configuration.APP_MODE) {
                            case Configuration.APP_GLOBAL:
                            case Configuration.APP_KC_HUGGIES_X_MONIT:
                            case Configuration.APP_MONIT_X_KAO:
                                intent = new Intent(SplashActivity.this, SigninActivity.class);
                                break;
//                            case Configuration.APP_MONIT_X_HUGGIES:
//                                intent = new Intent(SplashActivity.this, YKSigninActivity.class);
//                                break;
                        }

                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else if (responseCode == ServerManager.ERROR_CODE_TIMEOUT_EXCEPTION ||
                            responseCode == ServerManager.ERROR_CODE_IO_EXCEPTION) {
                        mHandler.obtainMessage(MSG_SHOW_TIMEOUT_DIALOG, 0, 0).sendToTarget();
                    } else {
                        showToast(getString(R.string.toast_invalid_user_session));
                        mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
                        mHandler.sendEmptyMessage(MSG_SESSION_CHECK_FINISHED);
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         if (DBG) Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
        if (!em.getInstance(mContext).isReady()) {
            if (DBG) Log.e(TAG, "em not ready");
            mServerQueryMgr.getAppData(new ServerManager.ServerResponseListener() {
                @Override
                public void onReceive(int responseCode, String errCode, String data) {
                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                        em.getInstance(mContext).setAppData(ServerManager.getStringFromJSONObj(data, mServerQueryMgr.getParameter(80)));
                    }
                    checkLatestVersion();
                }
            });
        } else {
            checkLatestVersion();
        }
    }
}