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

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.SignupActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.widget.ValidationBirthdayYYMMDD;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class SignupContent3 extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "Signup3";
	private static final boolean DBG = Configuration.DBG;

	private ValidationManager mValidationMgr;
	private ValidationEditText vetNickname;
	private ValidationRadio vrSex;
	private ValidationBirthdayYYMMDD vtvBirthday;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_signup3, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mValidationMgr = new ValidationManager(mContext);
		mScreenInfo = new ScreenInfo(203);

		_initView(view);

        return view;
    }

	private void _initView(View v) {
		vetNickname = (ValidationEditText)v.findViewById(R.id.vet_activity_signup_nickname);
		vetNickname.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vetNickname.setValid(mValidationMgr.isValidNickname(vetNickname.getText()));
			}
		});

		vrSex = (ValidationRadio)v.findViewById(R.id.vr_activity_signup_gender);
		vrSex.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vrSex.setValid(true);
			}
		});

		vtvBirthday = (ValidationBirthdayYYMMDD)v.findViewById(R.id.vtv_activity_signup_birthday);
		vtvBirthday.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vtvBirthday.setValid(true);
			}
		});
		vtvBirthday.setBirthdayFromYear(1900);
		vtvBirthday.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
			}
		});
		vtvBirthday.showDay(false);
    }

	public boolean isValidInformation() {
		String nickname = vetNickname.getText();
		String bday = vtvBirthday.getSelectedDateStringYYYYMMDD();
		int gender = vrSex.getSelectedRadioIndex() == 1 ? 1 : 0;

		boolean valid = true;
		if (DBG) Log.d(TAG, "_checkSignUpContent3! > " + nickname + " / " + bday + " / " + gender);
		if (!vetNickname.isValid()) {
			vetNickname.setWarning(getString(R.string.account_warning_nickname));
			vetNickname.showWarning(true, 1000);
			vetNickname.setValid(false);
			valid = false;
		}

		if (!vtvBirthday.isValid()) {
			vtvBirthday.setWarning(getString(R.string.account_warning_birthday));
			vtvBirthday.showWarning(true, 1000);
			vtvBirthday.setValid(false);
			valid = false;
		}

		if (!vrSex.isValid()) {
			vrSex.setWarning(getString(R.string.account_warning_gender));
			vrSex.showWarning(true, 1000);
			vrSex.setValid(false);
			valid = false;
		}

		return valid;
	}

	public String getNickName() {
		return vetNickname.getText();
	}

	public String getBirthday() {
		return vtvBirthday.getSelectedDateStringYYYYMMDD();
	}

	public int getSex() {
		return vrSex.getSelectedRadioIndex() == 1 ? 1 : 0;
	}

	public void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(vetNickname.getWindowToken(), 0);
	}

	public void showWarningMessage(String errcode) {
		mHandler.obtainMessage(SignupActivity.MSG_SHOW_WARNING_MESSAGE, errcode).sendToTarget();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case SignupActivity.MSG_SHOW_WARNING_MESSAGE:

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
