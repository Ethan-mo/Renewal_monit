package goodmonit.monit.com.kao.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.Policy;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.TooltipBoxManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.SettingButton;
import goodmonit.monit.com.kao.widget.SettingButtonWarning;
import goodmonit.monit.com.kao.widget.SettingTextDivider;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class SettingAccountActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "SettingAccount";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_SHOW_VIEW              = 1;

    private static final int VIEW_MAIN                  = 1;
    private static final int VIEW_CHANGE_PASSWORD       = 2;
    private static final int VIEW_CHANGE_NICKNAME       = 3;

    private int mCurrentViewIndex;

    private LinearLayout lctnButtonList;
    private SettingButton btnChangePassword, btnNickname, btnShortId, btnGender, btnBirthday;
    private SettingButton btnPrivacy, btnTerms, btnCollect, btnProvide3rdParty;
    private SettingButton btnSupport, btnFeedback, btnNotice, btnInitTooltip;
    private SettingButton btnAppVersion;
    private SettingButton btnNugu, btnGoogleAssistant;
    private SettingButtonWarning btnSignout, btnLeave;

    private ValidationEditText vetPassword, vetCurrentPassword;
    private ValidationEditText vetNickname;
    private ValidationManager mValidationMgr;

    private SimpleDialog mDlgSignoutConfirmation, mDlgLeaveConfirmation;

    private UserInfoManager mUserInfoMgr;
    private PreferenceManager mPreferenceMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        _setToolBar();

        mContext = this;
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mUserInfoMgr = UserInfoManager.getInstance(this);
        mValidationMgr = new ValidationManager(this);
        mScreenInfo = new ScreenInfo(401);

        mUserInfoMgr.refreshGroupList();

        _initView();

        mCurrentViewIndex = VIEW_MAIN;
        _showSettingView(mCurrentViewIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getString(R.string.title_account).toUpperCase());

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
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

    @Override
    public void onBackPressed() {
        if (mCurrentViewIndex == VIEW_MAIN) {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
        } else {
            btnToolbarLeft.callOnClick();
        }
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);
        lctnButtonList = (LinearLayout) findViewById(R.id.lctn_setting_list);
    }

    private void _showSettingView(int viewIndex) {
        mHandler.obtainMessage(MSG_SHOW_VIEW, viewIndex, -1).sendToTarget();
    }

    private void _showSettingMainView() {
        mCurrentViewIndex = VIEW_MAIN;
        tvToolbarTitle.setText(getString(R.string.title_setting));
        btnToolbarRight.setVisibility(View.GONE);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
            }
        });

        _initSettingMain();
        lctnButtonList.removeAllViews();

        SettingTextDivider dividerAccount = new SettingTextDivider(this);
        lctnButtonList.addView(dividerAccount);
        lctnButtonList.addView(btnShortId);
        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            case Configuration.APP_KC_HUGGIES_X_MONIT:
            case Configuration.APP_MONIT_X_KAO:
                dividerAccount.setTitle(getString(R.string.account).toUpperCase() + " : " + mPreferenceMgr.getSigninEmail());
                lctnButtonList.addView(btnChangePassword);
                lctnButtonList.addView(btnNickname);
                lctnButtonList.addView(btnGender);
                lctnButtonList.addView(btnBirthday);
                break;
//            case Configuration.APP_MONIT_X_HUGGIES:
//                dividerAccount.setTitle(getString(R.string.account).toUpperCase());
//                lctnButtonList.addView(btnNickname);
//                break;
        }

        SettingTextDivider dividerLegal = new SettingTextDivider(this);
        dividerLegal.setTitle(getString(R.string.legal_notice).toUpperCase());
        lctnButtonList.addView(dividerLegal);
        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            case Configuration.APP_KC_HUGGIES_X_MONIT:
                lctnButtonList.addView(btnTerms);
                lctnButtonList.addView(btnPrivacy);
                btnPrivacy.setDividerForOtherCategory(true);
                //lctnButtonList.addView(btnCollect);
                break;
                //여긴 잘 모르겠음
//            case Configuration.APP_MONIT_X_HUGGIES:
//                lctnButtonList.addView(btnTerms);
//                //lctnButtonList.addView(btnWarranty);
//                lctnButtonList.addView(btnPrivacy);
//                lctnButtonList.addView(btnCollect);
//                lctnButtonList.addView(btnProvide3rdParty);
//                break;
            case Configuration.APP_MONIT_X_KAO:
                lctnButtonList.addView(btnTerms);
                lctnButtonList.addView(btnPrivacy);
                lctnButtonList.addView(btnProvide3rdParty);
                break;
        }

        SettingTextDivider dividerHelp = new SettingTextDivider(this);
        dividerHelp.setTitle(getString(R.string.help).toUpperCase());
        lctnButtonList.addView(dividerHelp);
        lctnButtonList.addView(btnNotice);
        lctnButtonList.addView(btnSupport);
        if (Configuration.DEVELOPER) {
            lctnButtonList.addView(btnInitTooltip);
        }
        //lctnButtonList.addView(btnFeedback);

        SettingTextDivider divider3rdParty = new SettingTextDivider(this);
        divider3rdParty.setTitle(getString(R.string.account_setup_header_nugu).toUpperCase());
        if (Configuration.APP_MODE == Configuration.APP_MONIT_X_HUGGIES) {
            lctnButtonList.addView(divider3rdParty);
            lctnButtonList.addView(btnNugu);
            lctnButtonList.addView(btnGoogleAssistant);
        }

        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnAppVersion);

        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(btnSignout);

        switch (Configuration.APP_MODE) {
            case Configuration.APP_GLOBAL:
            case Configuration.APP_KC_HUGGIES_X_MONIT:
            case Configuration.APP_MONIT_X_KAO:
                lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
                lctnButtonList.addView(btnLeave);
                break;
//            case Configuration.APP_MONIT_X_HUGGIES:
//                break;
        }

        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider_footer, null));
        _hideKeyboard();
    }

    private void _initSettingMain() {
        if (mDlgSignoutConfirmation == null) {
            mDlgSignoutConfirmation = new SimpleDialog(SettingAccountActivity.this,
                    getString(R.string.dialog_contents_sign_out_confirm),
                    getString(R.string.dialog_contents_sign_out_confirm_description),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgSignoutConfirmation.dismiss();
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgSignoutConfirmation.dismiss();
                            mServerQueryMgr.signout(new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    mUserInfoMgr.signout();
                                    finish();
                                }
                            });
                        }
                    });
            mDlgSignoutConfirmation.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        if (mDlgLeaveConfirmation == null) {
            mDlgLeaveConfirmation = new SimpleDialog(SettingAccountActivity.this,
                    getString(R.string.dialog_contents_leave_confirm),
                    getString(R.string.dialog_contents_leave_confirm_description),
                    getString(R.string.btn_cancel),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgLeaveConfirmation.dismiss();
                        }
                    },
                    getString(R.string.btn_ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgLeaveConfirmation.dismiss();
                            mServerQueryMgr.leave(new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    mUserInfoMgr.leave();
                                    finish();
                                }
                            });
                        }
                    });
            mDlgLeaveConfirmation.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        if (btnChangePassword == null) {
            btnChangePassword = new SettingButton(this);
            btnChangePassword.setTitle(getString(R.string.account_change_password));
            btnChangePassword.setContent("");
            btnChangePassword.setDividerForOtherCategory(false);
            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _showSettingView(VIEW_CHANGE_PASSWORD);
                    }
            });
        }

        if (btnNickname == null) {
            btnNickname = new SettingButton(this);
            btnNickname.setTitle(getString(R.string.account_nickname));

            switch (Configuration.APP_MODE) {
                case Configuration.APP_GLOBAL:
                case Configuration.APP_KC_HUGGIES_X_MONIT:
                case Configuration.APP_MONIT_X_KAO:
                    btnNickname.setDividerForOtherCategory(false);
                    break;
//                case Configuration.APP_MONIT_X_HUGGIES:
//                    btnNickname.setDividerForOtherCategory(true);
//                    break;
            }

            btnNickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _showSettingView(VIEW_CHANGE_NICKNAME);
                }
            });
        }
        btnNickname.setContent(mPreferenceMgr.getProfileNickname());

        if (btnShortId == null) {
            btnShortId = new SettingButton(this);
            btnShortId.setTitle(getString(R.string.account_shortid));
            btnShortId.setDividerForOtherCategory(false);
            btnShortId.setOnClickListener(null);
            btnShortId.showDirection(false);
            btnShortId.setClickable(false);
            btnShortId.setContent(mPreferenceMgr.getShortId());
        }

        if (btnGender == null) {
            btnGender = new SettingButton(this);
            btnGender.setTitle(getString(R.string.account_gender));
            btnGender.setContent(
                    mPreferenceMgr.getProfileSex() == 0 ? getString(R.string.gender_female) : getString(R.string.gender_male));
            btnGender.setDividerForOtherCategory(false);
            btnGender.showDirection(false);
            btnGender.setClickable(false);
        }

        if (btnBirthday == null) {
            btnBirthday = new SettingButton(this);
            btnBirthday.setTitle(getString(R.string.account_birthday));
            btnBirthday.setContent(DateTimeUtil.getDateStringYYYYMM(mPreferenceMgr.getProfileBirthday(), Locale.getDefault().getLanguage()));
            btnBirthday.setDividerForOtherCategory(true);
            btnBirthday.showDirection(false);
            btnBirthday.setClickable(false);
        }

        if (btnPrivacy == null) {
            btnPrivacy = new SettingButton(this);
            if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GDPR) != -1) {
                btnPrivacy.setTitle(getString(R.string.agreement_privacy_gdpr_goodmonit));
            } else {
                btnPrivacy.setTitle(getString(R.string.agreement_privacy_goodmonit));
            }
            btnPrivacy.setContent("");
            btnPrivacy.setDividerForOtherCategory(false);
            btnPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = null;
                    if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                        url = mServerQueryMgr.getParameter(393);
                        FirebaseAnalyticsManager.getInstance(mContext).sendPrivacyPolicy(mPreferenceMgr.getAccountId());
                    } else if (Configuration.APP_MODE == Configuration.APP_MONIT_X_KAO) {
                        url = mServerQueryMgr.getParameter(341);
                        FirebaseAnalyticsManager.getInstance(mContext).sendPrivacyPolicy(mPreferenceMgr.getAccountId());
                    } else {
                        if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_KR) != -1)
                                || (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PRIVACY_KR) != -1)) {
                            url = mServerQueryMgr.getParameter(340);
                        } else if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GLOBAL) != -1) {
                            url = mServerQueryMgr.getParameter(341);
                        } else if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GDPR) != -1) {
                            url = mServerQueryMgr.getParameter(342);
                        } else {
                            url = mServerQueryMgr.getParameter(341);
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        if (btnTerms == null) {
            btnTerms = new SettingButton(this);
            btnTerms.setTitle(getString(R.string.legal_terms));
            btnTerms.setContent("");
            btnTerms.setDividerForOtherCategory(false);
            btnTerms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = null;
                    if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                        url = mServerQueryMgr.getParameter(392);
                        FirebaseAnalyticsManager.getInstance(mContext).sendTermsAndConditions(mPreferenceMgr.getAccountId());
                    } else if (Configuration.APP_MODE == Configuration.APP_MONIT_X_KAO) {
                        url = mServerQueryMgr.getParameter(321);
                        FirebaseAnalyticsManager.getInstance(mContext).sendTermsAndConditions(mPreferenceMgr.getAccountId());
                    } else {
                        if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_KR) != -1)
                                || (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_TERMS_OF_USE_KR) != -1)) {
                            url = mServerQueryMgr.getParameter(320);
                        } else if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_GLOBAL) != -1) {
                            url = mServerQueryMgr.getParameter(321);
                        } else {
                            url = mServerQueryMgr.getParameter(321);
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        if (btnCollect == null) {
            btnCollect = new SettingButton(this);
            btnCollect.setTitle(getString(R.string.legal_collect_privacy));
            btnCollect.setContent("");
            btnCollect.setDividerForOtherCategory(false);
            btnCollect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = null;
                    url = mServerQueryMgr.getParameter(360);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        if (btnProvide3rdParty == null) {
            btnProvide3rdParty = new SettingButton(this);
            btnProvide3rdParty.setTitle(getString(R.string.legal_provide_3rd_party));
            btnProvide3rdParty.setContent("");
            btnProvide3rdParty.setDividerForOtherCategory(true);
            btnProvide3rdParty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = null;
                    if (Configuration.APP_MODE == Configuration.APP_MONIT_X_KAO) {
                        url = mServerQueryMgr.getParameter(381);
                    } else {
                        url = mServerQueryMgr.getParameter(380);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        if (btnSupport == null) {
            btnSupport = new SettingButton(this);
            btnSupport.setTitle(getString(R.string.help_support));
            btnSupport.setContent("");
            btnSupport.setDividerForOtherCategory(true);
            btnSupport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = null;
                    if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
                        url = mServerQueryMgr.getParameter(394);
                        FirebaseAnalyticsManager.getInstance(mContext).sendCustomerSupport(mPreferenceMgr.getAccountId());
                    } else {
                        if ("ko".equals(Locale.getDefault().getLanguage())) {
                            url = mServerQueryMgr.getParameter(300);
                        } else {
                            url = mServerQueryMgr.getParameter(301);
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        if (btnNotice == null) {
            btnNotice = new SettingButton(this);
            btnNotice.setTitle(getString(R.string.notice_title));
            btnNotice.setContent("");
            btnNotice.setDividerForOtherCategory(true);
            btnNotice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingAccountActivity.this, NoticeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (btnFeedback == null) {
            btnFeedback = new SettingButton(this);
            btnFeedback.setTitle(getString(R.string.help_feedback));
            btnFeedback.setContent("");
            btnFeedback.setDividerForOtherCategory(true);
            btnFeedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        if (btnInitTooltip == null) {
            btnInitTooltip = new SettingButton(this);
            btnInitTooltip.setTitle("Init tooltip");
            btnInitTooltip.setContent("");
            btnInitTooltip.setDividerForOtherCategory(true);
            btnInitTooltip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TooltipBoxManager tooltipMgr = new TooltipBoxManager(mContext);
                    tooltipMgr.initPreferences();
                    showToast("Initialized");
                }
            });
        }

        if (btnSignout == null) {
            btnSignout = new SettingButtonWarning(this);
            btnSignout.setWarning(getString(R.string.btn_signout));
            btnSignout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDlgSignoutConfirmation != null) {
                        try {
                            mDlgSignoutConfirmation.show();
                        } catch (Exception e) {

                        }
                    }
                }
            });
        }

        if (btnLeave == null) {
            btnLeave = new SettingButtonWarning(this);
            btnLeave.setWarning(getString(R.string.btn_leave));
            btnLeave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDlgLeaveConfirmation != null) {
                        try {
                            mDlgLeaveConfirmation.show();
                        } catch (Exception e) {

                        }
                    }
                }
            });
        }

        if (btnAppVersion == null) {
            btnAppVersion = new SettingButton(this);
            btnAppVersion.setTitle(getString(R.string.setting_device_app_version));
            btnAppVersion.setDividerForOtherCategory(true);
            btnAppVersion.showDirection(false);
            btnAppVersion.setContent(mPreferenceMgr.getLocalVersion());
            btnAppVersion.setClickable(false);
        }

        if (btnNugu == null) {
            btnNugu = new SettingButton(this);
            btnNugu.setTitle(getString(R.string.nugu_title));
            btnNugu.setDividerForOtherCategory(true);
            btnNugu.showDirection(true);
            btnNugu.setClickable(true);
            btnNugu.setContent("");
            btnNugu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingAccountActivity.this, NuguActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
        }

        if (btnGoogleAssistant == null) {
            btnGoogleAssistant = new SettingButton(this);
            btnGoogleAssistant.setTitle(getString(R.string.assistant_title));
            btnGoogleAssistant.setDividerForOtherCategory(true);
            btnGoogleAssistant.showDirection(true);
            btnGoogleAssistant.setClickable(true);
            btnGoogleAssistant.setContent("");
            btnGoogleAssistant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingAccountActivity.this, GoogleAssistantActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                }
            });
        }
    }

    private void _showSettingChangePassword() {
        _initSettingChangePassword();
        mCurrentViewIndex = VIEW_CHANGE_PASSWORD;
        tvToolbarTitle.setText(getString(R.string.account_change_password));
        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vetPassword.isValid()) {
                    vetPassword.setWarning(getString(R.string.account_warning_password));
                    vetPassword.setValid(false);
                    return;
                } else if (!vetCurrentPassword.isValid()) {
                    vetCurrentPassword.showWarning(true);
                    vetCurrentPassword.setValid(false);
                } else {
                    String newPasswordMD5 = ServerManager.getMD5Encryption(vetPassword.getText());
                    String oldPasswordMD5 = ServerManager.getMD5Encryption(vetCurrentPassword.getText());
                    showProgressBar(true);
                    mServerQueryMgr.changePasswordV2(oldPasswordMD5, newPasswordMD5, new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            showProgressBar(false);
                            if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    showToast(getString(R.string.toast_change_password_succeeded));
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromInputMethod(vetPassword.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                                    _showSettingView(VIEW_MAIN);
                                } else if (InternetErrorCode.ERR_WRONG_CURRENT_PASSWORD.equals(errCode)) {
                                    ((Activity)mContext).runOnUiThread(new Runnable() {
                                        public void run() {
                                            vetCurrentPassword.setWarning(getString(R.string.account_current_password_not_match));
                                            vetCurrentPassword.showWarning(true);
                                            vetCurrentPassword.setValid(false);
                                        }
                                    });
                                } else {
                                    showToast(getString(R.string.toast_change_password_failed));
                                }
                            } else {
                                showCommunicationErrorDialog(responseCode);
                            }
                        }
                    });
                }
            }
        });
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromInputMethod(vetPassword.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                _showSettingView(VIEW_MAIN);
            }
        });

        vetPassword.showWarning(false);
        vetCurrentPassword.showWarning(false);

        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(vetCurrentPassword);
        lctnButtonList.addView(vetPassword);

        SettingTextDivider dividerPassword = new SettingTextDivider(this);
        dividerPassword.setTitle(getString(R.string.account_password_description));
        lctnButtonList.addView(dividerPassword);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(vetPassword, InputMethodManager.SHOW_IMPLICIT);
    }

    private void _initSettingChangePassword() {
        if (vetPassword == null) {
            vetPassword = new ValidationEditText(this);
            vetPassword.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    switch (mValidationMgr.isValidPassword(vetPassword.getText())) {
                        case ValidationManager.PASSWORD_AVAILABLE:
                            vetPassword.showWarning(false);
                            vetPassword.setValid(true);
                            break;
                        case ValidationManager.PASSWORD_NO_UPPERCASE_ALPHABET:
                            vetPassword.setWarning(getString(R.string.account_warning_password_no_alphabet_uppercase));
                            vetPassword.showWarning(true);
                            vetPassword.setValid(false);
                            break;
                        case ValidationManager.PASSWORD_NO_LOWERCASE_ALPHABET:
                            vetPassword.setWarning(getString(R.string.account_warning_password_no_alphabet_lowercase));
                            vetPassword.showWarning(true);
                            vetPassword.setValid(false);
                            break;
                        case ValidationManager.PASSWORD_NO_DIGIT:
                            vetPassword.setWarning(getString(R.string.account_warning_password_digit));
                            vetPassword.showWarning(true);
                            vetPassword.setValid(false);
                            break;
                        case ValidationManager.PASSWORD_NO_NUMBER:
                            vetPassword.setWarning(getString(R.string.account_warning_password_no_number));
                            vetPassword.showWarning(true);
                            vetPassword.setValid(false);
                            break;
                        case ValidationManager.PASSWORD_NO_SPECIAL_CHARACTER:
                            vetPassword.setWarning(getString(R.string.account_warning_password_no_special_character));
                            vetPassword.showWarning(true);
                            vetPassword.setValid(false);
                            break;
                    }
                }
            });
            vetPassword.setTitle(getString(R.string.account_new_password));
            vetPassword.setHint(getString(R.string.account_new_password_hint));
            vetPassword.setWarning(getString(R.string.account_warning_password));
            vetPassword.setPasswordMode(true);
            vetPassword.showPassword(false);
            vetPassword.showUnderLine(true);
        }
        vetPassword.setText("");
        vetPassword.showWarning(false);

        if (vetCurrentPassword == null) {
            vetCurrentPassword = new ValidationEditText(this);
            vetCurrentPassword.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    switch (mValidationMgr.isValidPassword(vetCurrentPassword.getText())) {
                        case ValidationManager.PASSWORD_NO_DIGIT:
                            vetCurrentPassword.setWarning(getString(R.string.account_warning_password_digit));
                            vetCurrentPassword.showWarning(true);
                            vetCurrentPassword.setValid(false);
                            break;
                        default:
                            vetCurrentPassword.showWarning(false);
                            vetCurrentPassword.setValid(true);
                            break;
                    }
                }
            });
            vetCurrentPassword.setTitle(getString(R.string.account_current_password));
            vetCurrentPassword.setHint(getString(R.string.account_current_password_hint));
            vetCurrentPassword.setWarning(getString(R.string.account_current_password_not_match));
            vetCurrentPassword.setPasswordMode(true);
            vetCurrentPassword.showPassword(false);
            vetCurrentPassword.showUnderLine(true);
        }
        vetCurrentPassword.setText("");
        vetCurrentPassword.showWarning(false);
    }

    private void _showSettingChangeNickname() {
        _initSettingChangeNickname();

        mCurrentViewIndex = VIEW_CHANGE_NICKNAME;
        tvToolbarTitle.setText(getString(R.string.account_change_nickname));
        btnToolbarRight.setVisibility(View.VISIBLE);
        btnToolbarRight.setText(getString(R.string.btn_save));
        btnToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vetNickname.isValid()) {
                    vetNickname.setWarning(getString(R.string.account_warning_nickname));
                    vetNickname.setValid(false);
                    return;
                } else {
                    final String nickname = vetNickname.getText();
                    if (nickname.equals(mPreferenceMgr.getProfileNickname())) {
                        showToast(getString(R.string.toast_change_same_nickname));
                        return;
                    }

                    showProgressBar(true);
                    mServerQueryMgr.changeNickname(nickname, new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            showProgressBar(false);
                            if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    showToast(getString(R.string.toast_change_nickname_succeeded));
                                    _showSettingView(VIEW_MAIN);
                                    mPreferenceMgr.setProfileNickname(nickname);
                                } else {
                                    showToast(getString(R.string.toast_change_nickname_failed));
                                }
                            } else {
                                showCommunicationErrorDialog(responseCode);
                            }
                        }
                    });
                }
            }
        });
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showSettingView(VIEW_MAIN);
            }
        });

        lctnButtonList.removeAllViews();
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
        lctnButtonList.addView(vetNickname);
        lctnButtonList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider_nomargin, null));

        //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(vetNickname.getWindowToken(), 0);
        //imm.showSoftInput(vetNickname, InputMethodManager.SHOW_IMPLICIT);
    }

    private void _initSettingChangeNickname() {
        if (vetNickname == null) {
            vetNickname = new ValidationEditText(this);
            vetNickname.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
                @Override
                public void updateValidation() {
                    vetNickname.setValid(mValidationMgr.isValidNickname(vetNickname.getText()));
                }
            });
            vetNickname.setTitle(getString(R.string.account_nickname));
            vetNickname.setHint(getString(R.string.account_warning_nickname));
            vetNickname.setWarning(getString(R.string.account_warning_nickname));
            vetNickname.showUnderLine(false);
        }
        vetNickname.setText(mPreferenceMgr.getProfileNickname());
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SHOW_VIEW:
                    int viewIdx = msg.arg1;
                    mCurrentViewIndex = viewIdx;
                    switch (viewIdx) {
                        case VIEW_MAIN:
                            _showSettingMainView();
                            break;
                        case VIEW_CHANGE_PASSWORD:
                            _showSettingChangePassword();
                            break;
                        case VIEW_CHANGE_NICKNAME:
                            _showSettingChangeNickname();
                            break;
                    }
                    break;
            }
        }
    };

    private void _hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (vetNickname != null) {
            imm.hideSoftInputFromWindow(vetNickname.getWindowToken(), 0);
        }
        if (vetPassword != null) {
            imm.hideSoftInputFromWindow(vetPassword.getWindowToken(), 0);
        }
    }
}
