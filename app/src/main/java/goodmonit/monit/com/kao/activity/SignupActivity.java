package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.signup.SignupContent1;
import goodmonit.monit.com.kao.signup.SignupContent2;
import goodmonit.monit.com.kao.signup.SignupContent3;
import goodmonit.monit.com.kao.signup.SignupContent4;
import goodmonit.monit.com.kao.widget.OrderIndicatorBar;

public class SignupActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Signup";
    private static final boolean DBG = Configuration.DBG;

    public static final int MSG_SHOW_WARNING_MESSAGE                   = 1;
    public static final int MSG_SHOW_FRAGMENT                          = 3;

    /** UI Resources */
    // Indicator
    private OrderIndicatorBar orderIndicatorBar;

    private SignupContent1 mSignupContent1;
    private SignupContent2 mSignupContent2;
    private SignupContent3 mSignupContent3;
    private SignupContent4 mSignupContent4;


    /** Control */
    private ValidationManager mValidationMgr;
    private int mCurrentShowingContentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        _setToolBar();

        mContext = this;
        mValidationMgr = new ValidationManager(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mPreferenceMgr = PreferenceManager.getInstance(this);

        _initView();
        mCurrentShowingContentIndex = getIntent().getIntExtra("startContent", SignInState.STEP_SIGN_UP);
        showFragment(mCurrentShowingContentIndex);
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        tvToolbarTitle = ((TextView)findViewById(R.id.tv_toolbar_title));
        tvToolbarTitle.setText(getString(R.string.title_signup));

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = null;
        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            case Configuration.APP_KC_HUGGIES_X_MONIT:
            case Configuration.APP_MONIT_X_KAO:
                intent = new Intent(SignupActivity.this, SigninActivity.class);
                break;
//            case Configuration.APP_MONIT_X_HUGGIES:
//                intent = new Intent(SignupActivity.this, YKSigninActivity.class);
//                break;
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        mSignupContent1 = new SignupContent1();
        mSignupContent2 = new SignupContent2();
        mSignupContent3 = new SignupContent3();
        mSignupContent4 = new SignupContent4();

        orderIndicatorBar = new OrderIndicatorBar(mContext,
                findViewById(android.R.id.content),
                4,
                R.drawable.bg_indicator_focused,
                R.drawable.bg_indicator_passed,
                R.drawable.bg_indicator_not_passed,
                R.color.colorIndicatorLinePassed,
                R.color.colorIndicatorLine,
                (int)getResources().getDimension(R.dimen.indicator_focused_size),
                (int)getResources().getDimension(R.dimen.indicator_not_focused_size));


    }

    public void confirmSignup1(final String email, final String passwordMD5) {
        String recommender = null;
        String productNumber = null;
        /*
        if (vrCode.getSelectedRadioIndex() == 1) {
            recommender = vrCode.getExtraInputText();
        } else {
            productNumber = vrCode.getExtraInputText();
        }
        */
        showProgressBar(true);
        mServerQueryMgr.join1(email, passwordMD5,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errcode, String data) {
                        showProgressBar(false);
                        if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                            try {
                                JSONObject jObject = new JSONObject(data);
                                String token = jObject.getString(mServerQueryMgr.getParameter(4));
                                long accountId = jObject.getLong(mServerQueryMgr.getParameter(3));
                                String shortId = jObject.getString(mServerQueryMgr.getParameter(22));

                                if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
                                    mPreferenceMgr.setSigninState(SignInState.STEP_AUTHENTICATE_EMAIL);
                                    mPreferenceMgr.setSigninEmail(email);
                                    mPreferenceMgr.setSigninToken(token);
                                    mPreferenceMgr.setAccountId(accountId);
                                    mPreferenceMgr.setShortId(shortId);
                                    showFragment(SignInState.STEP_AUTHENTICATE_EMAIL);
                                } else {
                                    if (mSignupContent1 != null && mCurrentShowingContentIndex == SignInState.STEP_SIGN_UP) {
                                        mSignupContent1.showWarningMessage(errcode);
                                    }
                                }
                            } catch (JSONException e) {
                                if (DBG) Log.e(TAG, e.toString());
                            }

                            mServerQueryMgr.setPolicy(mSignupContent1.getAgreementStatus(), new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errcode, String data) {
                                    if (DBG) Log.d(TAG, "setPolicy : " + mSignupContent1.getAgreementStatus());
                                    if (responseCode == ServerManager.RESPONSE_CODE_OK) {

                                    } else {

                                    }
                                }
                            });
                        } else {
                            showCommunicationErrorDialog(responseCode);
                        }
                    }});
    }

    public void sendAuthenticationEmail() {
        showProgressBar(true);
        mServerQueryMgr.resendAuthEmail(new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errcode, String data) {
                showProgressBar(false);
                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                    if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
                        showToast(getString(R.string.toast_sent_an_authentication_email));
                    } else {
                        showToast(getString(R.string.toast_failed_to_send_an_email));
                    }
                } else {
                    showCommunicationErrorDialog(responseCode);
                }
            }
        });
    }

    private void confirmSignup2() {
        showProgressBar(true);
        mServerQueryMgr.join2(new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errcode, String data) {
                        showProgressBar(false);
                        if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                            if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
                                mPreferenceMgr.setSigninState(SignInState.STEP_MORE_INFO);
                                showFragment(SignInState.STEP_MORE_INFO);
                            } else {
                                if (mSignupContent2 != null && mCurrentShowingContentIndex == SignInState.STEP_AUTHENTICATE_EMAIL) {
                                    mSignupContent2.showWarningMessage(InternetErrorCode.ERR_NOT_AUTHENTICATED);
                                }
                            }
                        } else {
                            showCommunicationErrorDialog(responseCode);
                        }
                    }});
    }

    private void confirmSignup3(final String nickname, final String yyyymm, final int sex) {
        showProgressBar(true);
        mServerQueryMgr.join3(nickname, yyyymm, sex,
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errcode, String data) {
                        showProgressBar(false);
                        if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                            if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
                                mPreferenceMgr.setSigninState(SignInState.STEP_WELCOME);
                                showFragment(SignInState.STEP_WELCOME);
                            } else {
                                mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_WARNING_MESSAGE, errcode));
                            }
                        } else {
                            showCommunicationErrorDialog(responseCode);
                        }
                    }});
    }

    private void confirmSignup4() {
        mPreferenceMgr.setSigninState(SignInState.STEP_COMPLETED);
        _next();
    }

    public void showFragment(int contentIdx) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_FRAGMENT, contentIdx, -1));
    }

    private void _showFragment(int idx) {
        Fragment fr = null;

        switch(idx) {
            case SignInState.STEP_SIGN_UP:
                if (mSignupContent1 == null) {
                    mSignupContent1 = new SignupContent1();
                }
                fr = mSignupContent1;
                orderIndicatorBar.setCurrentItem(1);
                btnToolbarRight.setText(getString(R.string.btn_signup_short));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSignupContent1 != null && mCurrentShowingContentIndex == SignInState.STEP_SIGN_UP) {
                            mSignupContent1.hideKeyboard();
                            if (mSignupContent1.isValidInformation()) {
                                confirmSignup1(mSignupContent1.getEmailString(), mSignupContent1.getPasswordMD5());
                            }
                        }
                    }
                });
                break;
            case SignInState.STEP_AUTHENTICATE_EMAIL:
                if (mSignupContent2 == null) {
                    mSignupContent2 = new SignupContent2();
                }
                fr = mSignupContent2;
                orderIndicatorBar.setCurrentItem(2);
                btnToolbarRight.setText(getString(R.string.btn_next));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmSignup2();
                    }
                });
                break;
            case SignInState.STEP_MORE_INFO:
                if (mSignupContent3 == null) {
                    mSignupContent3 = new SignupContent3();
                }
                fr = mSignupContent3;
                orderIndicatorBar.setCurrentItem(3);
                btnToolbarRight.setText(getString(R.string.btn_next));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSignupContent3 != null && mCurrentShowingContentIndex == SignInState.STEP_MORE_INFO) {
                            mSignupContent3.hideKeyboard();
                            if (mSignupContent3.isValidInformation()) {
                                String yyyymm = mSignupContent3.getBirthday();
                                if (yyyymm.length() > 6) {
                                    yyyymm = yyyymm.substring(0, 6);
                                }
                                confirmSignup3(mSignupContent3.getNickName(), yyyymm, mSignupContent3.getSex());
                            }
                        }
                    }
                });
                break;
            case SignInState.STEP_WELCOME:
                if (mSignupContent4 == null) {
                    mSignupContent4 = new SignupContent4();
                }
                fr = mSignupContent4;
                orderIndicatorBar.setCurrentItem(4);
                btnToolbarRight.setText(getString(R.string.btn_done));
                btnToolbarRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSignupContent4 != null && mCurrentShowingContentIndex == SignInState.STEP_WELCOME) {
                            confirmSignup4();
                        }
                    }
                });
                break;
        }

        if (fr != null) {
            try {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                if (mCurrentShowingContentIndex != SignInState.STEP_SIGN_UP) {
                    if (mCurrentShowingContentIndex < idx) {
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                    } else if (mCurrentShowingContentIndex > idx) {
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right, R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                    }
                }

                mCurrentShowingContentIndex = idx;
                fragmentTransaction.replace(R.id.fragment_signup, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } catch (IllegalStateException e) {

            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SHOW_FRAGMENT:
                    int contentIdx = msg.arg1;
                    _showFragment(contentIdx);
                    break;
            }
        }
    };

    private void _next() {
        Intent intent = null;
        if (Configuration.LIGHT_VERSION) {
            intent = new Intent(this, MainLightActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
