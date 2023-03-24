package goodmonit.monit.com.kao.signup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.SignupActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.Policy;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.widget.ValidationAgreementPolicy;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class SignupContent1 extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "Signup1";
	private static final boolean DBG = Configuration.DBG;

	private ValidationManager mValidationMgr;
	private ValidationEditText vetEmail, vetPassword;
	private ValidationAgreementPolicy vapTermsOfUse, vapPrivacy, vap3rdPartyDataProvide;
	private ValidationRadio vrEuCitizen;
	private TextView tvAgreementDescription, tvAccountTitle, tvAccountDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_signup1, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mConnectionMgr = ConnectionManager.getInstance();
		mValidationMgr = new ValidationManager(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(201);

		_initView(view);

        return view;
    }

	private void _initView(View v) {
		tvAccountTitle = (TextView)v.findViewById(R.id.tv_activity_signup_account_title);
		tvAccountDescription = (TextView)v.findViewById(R.id.tv_activity_signup_account_description);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			tvAccountTitle.setText(R.string.signup_step1_title_kc);
			tvAccountDescription.setText(R.string.signup_step1_detail_kc);
		} else {
			tvAccountTitle.setText(R.string.signup_step1_title);
			tvAccountDescription.setText(R.string.signup_step1_detail);
		}

		vetEmail = (ValidationEditText)v.findViewById(R.id.vet_activity_signup_email);
		vetEmail.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vetEmail.setWarning(getString(R.string.account_warning_email));
				vetEmail.setValid(mValidationMgr.isValidEmail(vetEmail.getText()));
			}
		});

		vetPassword = (ValidationEditText)v.findViewById(R.id.vet_activity_signup_password);
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

		tvAgreementDescription = (TextView)v.findViewById(R.id.tv_activity_signup_agreement_description);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			tvAgreementDescription.setText(R.string.agreement_description_kc);
		} else {
			tvAgreementDescription.setText(R.string.agreement_description_gdpr);
		}

		vapTermsOfUse = (ValidationAgreementPolicy)v.findViewById(R.id.vap_activity_signup_terms_of_use);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			vapTermsOfUse.setShowUrl(mServerQueryMgr.getParameter(392));
		} else {
			if ("ko".equals(Locale.getDefault().getLanguage())) {
				vapTermsOfUse.setShowUrl(mServerQueryMgr.getParameter(320));
			} else {
				vapTermsOfUse.setShowUrl(mServerQueryMgr.getParameter(321));
			}
		}
		vapTermsOfUse.setPolicyName("* " + getString(R.string.agreement_service_goodmonit));
		vapTermsOfUse.showUnderline(true);

		vapPrivacy = (ValidationAgreementPolicy)v.findViewById(R.id.vap_activity_signup_privacy);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			vapPrivacy.setShowUrl(mServerQueryMgr.getParameter(393));
		} else {
			if ("ko".equals(Locale.getDefault().getLanguage())) {
				vapPrivacy.setShowUrl(mServerQueryMgr.getParameter(340));
			} else {
				vapPrivacy.setShowUrl(mServerQueryMgr.getParameter(341));
			}
		}
		vapPrivacy.setPolicyName("* " + getString(R.string.agreement_privacy_goodmonit));
		vapPrivacy.showUnderline(false);

		vap3rdPartyDataProvide = (ValidationAgreementPolicy)v.findViewById(R.id.vap_activity_signup_3rd_party_data_provide);
		vap3rdPartyDataProvide.setShowUrl(mServerQueryMgr.getParameter(381));
		vap3rdPartyDataProvide.setPolicyName("* " + getString(R.string.agreement_disclosure));
		vap3rdPartyDataProvide.showUnderline(false);
		if (Configuration.APP_MODE == Configuration.APP_MONIT_X_KAO) {
			vap3rdPartyDataProvide.setVisibility(View.VISIBLE);
		}

		vrEuCitizen = (ValidationRadio)v.findViewById(R.id.vr_activity_signup_eu_citizen);
		vrEuCitizen.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vrEuCitizen.setValid(true);
			}
		});
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			// KC 앱은 EU Citizen여부가 Default False
			vrEuCitizen.selectItem(2);
		}
		vrEuCitizen.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int selected = vrEuCitizen.getSelectedRadioIndex();
				if (selected == 1) { // yes, GDPR
					vapPrivacy.setPolicyName("* " + getString(R.string.agreement_privacy_gdpr_goodmonit));
					vapPrivacy.setShowUrl(mServerQueryMgr.getParameter(342));
				} else { // no
					vapPrivacy.setPolicyName("* " + getString(R.string.agreement_privacy_goodmonit));
					if ("ko".equals(Locale.getDefault().getLanguage())) {
						vapPrivacy.setShowUrl(mServerQueryMgr.getParameter(340));
					} else {
						vapPrivacy.setShowUrl(mServerQueryMgr.getParameter(341));
					}
				}
				vapTermsOfUse.setSelected(false);
				vapPrivacy.setSelected(false);
			}
		});

		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			vrEuCitizen.setVisibility(View.GONE);
		} else {
			vrEuCitizen.setVisibility(View.VISIBLE);
		}
    }

	public boolean isValidInformation() {
		boolean valid = true;
		if (!vetEmail.isValid()) {
			vetEmail.setWarning(getString(R.string.account_warning_email));
			vetEmail.showWarning(true, 1000);
			vetEmail.setValid(false);
			valid = false;
		}

		if (!vetPassword.isValid()) {
			vetPassword.setWarning(getString(R.string.account_warning_password));
			vetPassword.showWarning(true, 1000);
			vetPassword.setValid(false);
			valid = false;
		}

		if (Configuration.APP_MODE == Configuration.APP_GLOBAL) {
			if (!vrEuCitizen.isValid()) {
				vrEuCitizen.setWarning(getString(R.string.account_warning_code));
				vrEuCitizen.showWarning(true, 1000);
				vrEuCitizen.setValid(false);
				valid = false;
			}
		}

		if (!vapTermsOfUse.isAgreed()) {
			vapTermsOfUse.setWarning(getString(R.string.account_warning_agreement));
			vapTermsOfUse.showWarning(true, 1000);
			vapTermsOfUse.setSelected(false);
			valid = false;
		}

		if (!vapPrivacy.isAgreed()) {
			vapPrivacy.setWarning(getString(R.string.account_warning_agreement));
			vapPrivacy.showWarning(true, 1000);
			vapPrivacy.setSelected(false);
			valid = false;
		}

		if (Configuration.APP_MODE == Configuration.APP_MONIT_X_KAO) {
			if (!vap3rdPartyDataProvide.isAgreed()) {
				vap3rdPartyDataProvide.setWarning(getString(R.string.account_warning_agreement));
				vap3rdPartyDataProvide.showWarning(true, 1000);
				vap3rdPartyDataProvide.setSelected(false);
				valid = false;
			}
		}

		return valid;
	}

	public String getAgreementStatus() {
		String updateData = null;

		try {
			JSONObject jobjTermsOfUse = new JSONObject();

			if ("ko".equals(Locale.getDefault().getLanguage())) {
				jobjTermsOfUse.put(mServerQueryMgr.getParameter(20), Policy.TERMS_OF_USE_KR);
			} else {
				jobjTermsOfUse.put(mServerQueryMgr.getParameter(20), Policy.TERMS_OF_USE_GLOBAL);
			}
			jobjTermsOfUse.put(mServerQueryMgr.getParameter(98), (vapTermsOfUse.isAgreed() ? 1 : 0));

			JSONObject jobjPrivacy = new JSONObject();
			if (vrEuCitizen.getSelectedRadioIndex() == 1) { // GDPR
				jobjPrivacy.put(mServerQueryMgr.getParameter(20), Policy.PRIVACY_GDPR);
			} else {
				if ("ko".equals(Locale.getDefault().getLanguage())) {
					jobjPrivacy.put(mServerQueryMgr.getParameter(20), Policy.PRIVACY_KR);
				} else {
					jobjPrivacy.put(mServerQueryMgr.getParameter(20), Policy.PRIVACY_GLOBAL);
				}
			}
			jobjPrivacy.put(mServerQueryMgr.getParameter(98), (vapPrivacy.isAgreed() ? 1 : 0));

			updateData = "[" + jobjTermsOfUse.toString() + "," + jobjPrivacy.toString();

			if (Configuration.APP_MODE == Configuration.APP_MONIT_X_KAO) {
				JSONObject jobj3rdParty = new JSONObject();
				jobj3rdParty.put(mServerQueryMgr.getParameter(20), Policy.PROVIDE_3RD_PARTY_KAO);
				jobj3rdParty.put(mServerQueryMgr.getParameter(98), (vap3rdPartyDataProvide.isAgreed() ? 1 : 0));
				updateData += "," + jobj3rdParty.toString();
			}
			updateData += "]";
		} catch (JSONException e) {
			if (DBG) Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			if (DBG) Log.e(TAG, e.toString());
		}

		return updateData;
	}

	public String getEmailString() {
		return vetEmail.getText();
	}

	public String getPasswordMD5() {
		return ServerManager.getMD5Encryption(vetPassword.getText());
	}

	public void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(vetEmail.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(vetPassword.getWindowToken(), 0);
	}

	public void showWarningMessage(String errcode) {
		mHandler.obtainMessage(SignupActivity.MSG_SHOW_WARNING_MESSAGE, errcode).sendToTarget();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case SignupActivity.MSG_SHOW_WARNING_MESSAGE:
					String errcode = (String)msg.obj;
					if (InternetErrorCode.ERR_DUPLICATED_EMAIL.equals(errcode)) {
						vetEmail.setWarning(getString(R.string.account_warning_duplicated_email));
						vetEmail.showWarning(true, 1000);
						vetEmail.setValid(false);
					} else if (InternetErrorCode.ERR_LEAVE_EMAIL.equals(errcode)) {
						vetEmail.setWarning(getString(R.string.account_warning_leave_email));
						vetEmail.showWarning(true, 1000);
						vetEmail.setValid(false);
					} else if (InternetErrorCode.ERR_SENT_EMAIL.equals(errcode)) {
						vetEmail.setWarning(getString(R.string.account_warning_failed_send_email));
						vetEmail.showWarning(true, 1000);
						vetEmail.setValid(false);
					}
					break;
			}
		}
	};

	@Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
