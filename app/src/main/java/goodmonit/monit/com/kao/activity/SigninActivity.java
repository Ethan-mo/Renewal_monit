package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

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
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.widget.SigninEditText;

public class SigninActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Signin";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_SHOW_DIALOG                = 1;

    private static final int DIALOG_INVALID_SIGNIN_INPUT    = 1;
    private static final int DIALOG_INVALID_SIGNIN_RETURN   = 2;
    private static final int DIALOG_INVALID_SIGNIN_RETURN_LEAVE = 3;

    private SigninEditText letEmail, letPassword;
    private Button btnSignin, btnSignup, btnForgotPassword;
    private ImageView ivLogo;
    private LinearLayout lctnBackground;
    private View vDividerBetweenSignupAndForgotPassword;

    private SimpleDialog mDlgInvalidInput, mDlgInvalidReturn;
    private ValidationManager mValidationMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mContext = this;
        mValidationMgr = new ValidationManager(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mScreenInfo = new ScreenInfo(101);

        _initView();
        String savedEmail = mPreferenceMgr.getSigninEmail();
        if (savedEmail != null && savedEmail.length() > 0) {
            letEmail.setText(savedEmail);
        }

        letEmail.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(letEmail.getWindowToken(), 0);
            }
        }, 100);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        showFinishApplicationDialog();
    }

    private void _initView() {
        lctnBackground = (LinearLayout) findViewById(R.id.lctn_activity_signin_background);
        ivLogo = (ImageView) findViewById(R.id.iv_activity_signin_logo);
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
        btnSignin = (Button)findViewById(R.id.btn_activity_signin_signin);
        btnSignup = (Button)findViewById(R.id.btn_activity_signin_signup);
        btnForgotPassword = (Button)findViewById(R.id.btn_activity_signin_forgot_password);
        letEmail = (SigninEditText)findViewById(R.id.let_activity_signin_email);
        letPassword = (SigninEditText)findViewById(R.id.let_activity_signin_password);
        vDividerBetweenSignupAndForgotPassword = (View)findViewById(R.id.v_activity_signin_divider);

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _signin();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ForgotPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            case Configuration.APP_MONIT_X_KAO:
                lctnBackground.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                ivLogo.setImageResource(R.drawable.logo_white);
                btnSignin.setBackgroundResource(R.drawable.bg_btn_radius_darkgreen_solid_no_border);
                btnSignin.setTextColor(getResources().getColor(R.color.colorWhite));
                btnSignup.setTextColor(getResources().getColor(R.color.colorWhite));
                btnForgotPassword.setTextColor(getResources().getColor(R.color.colorWhite));
                vDividerBetweenSignupAndForgotPassword.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                letEmail.setBackground(R.drawable.bg_underline_white);
                letEmail.setCategoryIcon(R.drawable.ic_signin_user);
                letEmail.setClearIcon(R.drawable.ic_edittext_clear);
                letEmail.setShowPasswordIcon(R.drawable.btn_edittext_show_password_white);
                letEmail.setHintTextColor(getResources().getColor(R.color.colorWhite));
                letEmail.setTextColor(getResources().getColor(R.color.colorWhite));
                letPassword.setBackground(R.drawable.bg_underline_white);
                letPassword.setCategoryIcon(R.drawable.ic_signin_password);
                letPassword.setClearIcon(R.drawable.ic_edittext_clear);
                letPassword.setShowPasswordIcon(R.drawable.btn_edittext_show_password_white);
                letPassword.setHintTextColor(getResources().getColor(R.color.colorWhite));
                letPassword.setTextColor(getResources().getColor(R.color.colorWhite));
                break;
            case Configuration.APP_KC_HUGGIES_X_MONIT:
                lctnBackground.setBackgroundResource(R.drawable.kc_signin_background);
                ivLogo.setImageResource(R.drawable.logo_kchuggies);
                btnSignin.setBackgroundResource(R.drawable.bg_btn_radius_white_solid_shadow_border);
                btnSignin.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                btnSignup.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                btnForgotPassword.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                vDividerBetweenSignupAndForgotPassword.setBackgroundColor(getResources().getColor(R.color.colorTextPrimary));
                letEmail.setBackground(R.drawable.bg_underline_black);
                letEmail.setCategoryIcon(R.drawable.ic_signin_user_black);
                letEmail.setClearIcon(R.drawable.ic_edittext_clear_black);
                letEmail.setShowPasswordIcon(R.drawable.btn_edittext_show_password_black);
                letEmail.setHintTextColor(getResources().getColor(R.color.colorTextPrimary));
                letEmail.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                letPassword.setBackground(R.drawable.bg_underline_black);
                letPassword.setCategoryIcon(R.drawable.ic_signin_password_black);
                letPassword.setClearIcon(R.drawable.ic_edittext_clear_black);
                letPassword.setShowPasswordIcon(R.drawable.btn_edittext_show_password_black);
                letPassword.setHintTextColor(getResources().getColor(R.color.colorTextPrimary));
                letPassword.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SHOW_DIALOG:
                    _showDialog(msg.arg1, msg.arg2);
                    break;
            }
        }
    };

    private void _showDialog(int type, int extra) {
        switch (type) {
            case DIALOG_INVALID_SIGNIN_INPUT:
                mDlgInvalidInput = new SimpleDialog(
                        SigninActivity.this,
                        getString(R.string.dialog_contents_invalid_signin_input),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mDlgInvalidInput.dismiss();
                            }
                        });
                try {
                    mDlgInvalidInput.show();
                } catch (Exception e) {

                }
                break;

            case DIALOG_INVALID_SIGNIN_RETURN:
                mDlgInvalidReturn = new SimpleDialog(
                        SigninActivity.this,
                        getString(R.string.dialog_contents_invalid_signin_input),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mDlgInvalidReturn.dismiss();
                            }
                        });
                try {
                    mDlgInvalidReturn.show();
                } catch (Exception e) {

                }
                break;

            case DIALOG_INVALID_SIGNIN_RETURN_LEAVE:
                mDlgInvalidReturn = new SimpleDialog(
                        SigninActivity.this,
                        getString(R.string.dialog_contents_leave_account_error),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mDlgInvalidReturn.dismiss();
                            }
                        });
                try {
                    mDlgInvalidReturn.show();
                } catch (Exception e) {

                }
                break;
        }
    }

    private void _signin() {
        String email = letEmail.getText().toLowerCase();
        String password = letPassword.getText();

        if (!mValidationMgr.isValidEmail(email) || (mValidationMgr.isValidPassword(password) != ValidationManager.PASSWORD_AVAILABLE)) {
            _showDialog(DIALOG_INVALID_SIGNIN_INPUT, -1);
            return;
        }
        mPreferenceMgr.setSigninEmail(email);
        String passwordMD5 = ServerManager.getMD5Encryption(password);

        showProgressBar(true);
        mServerQueryMgr.signIn(email, passwordMD5, new ServerManager.ServerResponseListener() {
            @Override
            public void onReceive(int responseCode, String errcode, String data) {
                showProgressBar(false);
                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                    try {
                        JSONObject jObject = new JSONObject(data);
                        String token = jObject.getString(mServerQueryMgr.getParameter(4));
                        long accountId = jObject.getLong(mServerQueryMgr.getParameter(3));
                        int step = jObject.getInt(mServerQueryMgr.getParameter(69));
                        if (InternetErrorCode.ERR_INVALID_EMAIL.equals(errcode)) {
                            mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_DIALOG, DIALOG_INVALID_SIGNIN_RETURN, -1));
                            return;
                        } else if (InternetErrorCode.ERR_INVALID_PASSWORD.equals(errcode)) {
                            mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_DIALOG, DIALOG_INVALID_SIGNIN_RETURN, -1));
                            return;
                        } else if (InternetErrorCode.ERR_LEAVE_EMAIL.equals(errcode)) {
                            mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_DIALOG, DIALOG_INVALID_SIGNIN_RETURN_LEAVE, -1));
                            return;
                        } else if (InternetErrorCode.SUCCEEDED.equals(errcode)) {
                            mPreferenceMgr.setSigninToken(token);
                            mPreferenceMgr.setAccountId(accountId);
                            mPreferenceMgr.setSigninState(step);
                            mPreferenceMgr.setInvalidTokenReceived(false);
                            _next();
                        } else {
                            showCommunicationErrorDialog(errcode);
                        }
                    } catch (JSONException e) {
                        if (DBG) Log.e(TAG, e.toString());
                        showCommunicationErrorDialog(errcode);
                    }
                } else {
                    showCommunicationErrorDialog("" + responseCode);
                }
            }
        });
    }

    private void _next() {
        int signinState = mPreferenceMgr.getSigninState();
        if (DBG) Log.d(TAG, "signinState : " + signinState);

        Intent intent = null;
        switch(signinState) {
            case SignInState.STEP_SIGN_IN:
                intent = new Intent(this, SigninActivity.class);
                break;
            case SignInState.STEP_AUTHENTICATE_EMAIL:
            case SignInState.STEP_MORE_INFO:
            case SignInState.STEP_WELCOME:
                intent = new Intent(this, SignupActivity.class);
                intent.putExtra("startContent", signinState);
                break;
            case SignInState.STEP_COMPLETED:
                mPreferenceMgr.setNeedToUpdatePushToken(PushManager.PUSH_SERVICE_TYPE);
                if (Configuration.LIGHT_VERSION) {
                    intent = new Intent(this, MainLightActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                break;
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

}
