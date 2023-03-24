package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.widget.ValidationBirthdayYYMMDD;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class ForgotPasswordActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "ForgetPasswd";

    private ValidationEditText vetEmail;
    private ValidationBirthdayYYMMDD vtvBirthday;
    private ValidationRadio vrGender;
    private ValidationManager mValidationMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        _setToolBar();

        mContext = this;
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mValidationMgr = new ValidationManager(this);
        mScreenInfo = new ScreenInfo(301);

        _initView();
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle.setText(getString(R.string.title_forgot_password));
        btnToolbarRight.setText(getString(R.string.btn_find));

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
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vetEmail.getWindowToken(), 0);
        finish();
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(vetEmail.getWindowToken(), 0);

                boolean valid = true;
                if (!vetEmail.isValid()) {
                    vetEmail.showWarning(true, 1000);
                    vetEmail.setValid(false);
                    valid = false;
                }
                /*
                if (!vtvBirthday.isValid()) {
                    vtvBirthday.showWarning(true, 1000);
                    vtvBirthday.setValid(false);
                    valid = false;
                }

                if (!vrGender.isValid()) {
                    vrGender.showWarning(true, 1000);
                    vrGender.setValid(false);
                    valid = false;
                }
                */

                if (valid == false) return;

                showProgressBar(true);
                /*
                mServerQueryMgr.findPasswd(vetEmail.getText(), vtvBirthday.getSelectedDateString(), vrGender.getSelectedRadioIndex(),
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                showProgressBar(false);
                                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        showToast(getString(R.string.toast_sent_new_password_email));
                                        finish();
                                    } else if (InternetErrorCode.ERR_SENT_FORGOT_PW_EMAIL.equals(errCode)) {
                                        showToast(getString(R.string.toast_failed_to_send_an_email));
                                    } else { // InternetErrorCode.ERR_INVALID_FORGOT_PW_INFO.equals(errCode)
                                        showToast(getString(R.string.toast_invalid_user_info));
                                    }
                                } else {
                                    showCommunicationErrorDialog(responseCode);
                                }
                            }
                        });
                        */
                mServerQueryMgr.resetPassword(vetEmail.getText(),
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                showProgressBar(false);
                                if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                    if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        showToast(getString(R.string.toast_sent_new_password_email));
                                        finish();
                                    } else {
                                        showToast(getString(R.string.toast_invalid_user_info));
                                    }
                                } else {
                                    showCommunicationErrorDialog(responseCode);
                                }
                            }
                        });
            }
        });

        vetEmail = (ValidationEditText)findViewById(R.id.vet_activity_forgot_password_email);
        vetEmail.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
            @Override
            public void updateValidation() {
                vetEmail.setValid(mValidationMgr.isValidEmail(vetEmail.getText()));
            }
        });

        vtvBirthday = (ValidationBirthdayYYMMDD) findViewById(R.id.vtv_activity_forgot_password_birthday);
        vtvBirthday.setBirthdayFromYear(1900);
        vtvBirthday.addOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _hideKeyboard();
            }
        });

        vrGender = (ValidationRadio)findViewById(R.id.vr_activity_forgot_password_gender);

        vtvBirthday.setVisibility(View.GONE);
        vrGender.setVisibility(View.GONE);
    }

    private void _hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vetEmail.getWindowToken(), 0);
    }
}
