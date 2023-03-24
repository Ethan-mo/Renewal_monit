package goodmonit.monit.com.kao.connection.ElderlySensor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.dialog.ProgressCircleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.widget.ValidationBirthdayYYMMDD;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class ConnectionElderlySensorInfo extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "BabyInfo";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;
	private Button btnHelp;
	private ProgressCircleDialog mDlgProcessing;
	private ValidationEditText vetName;
	private ValidationRadio vrGender;
	private ValidationBirthdayYYMMDD vtvBirthday;

	private ValidationManager mValidationMgr;
	private ScrollView svBabyInfo;

	private String mName;
	private String mBirthdayYYYYMM;
	private String mBirthdayYYMMDD;
	private int mGender;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_elderly_info, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mValidationMgr = new ValidationManager(inflater.getContext());
		mScreenInfo = new ScreenInfo(602);
        _initView(view);
        return view;
    }

	private void _initView(View v) {
		svBabyInfo = (ScrollView)v.findViewById(R.id.sv_connection_monit_baby_info);

		btnHelp = (Button)v.findViewById(R.id.btn_connection_monit_baby_info_help);
		btnHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(12, 21);
			}
		});

		vetName = (ValidationEditText)v.findViewById(R.id.vet_input_elderly_info_name);
		vetName.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vetName.setValid(mValidationMgr.isValidBabyname(vetName.getText()));
			}
		});

		vrGender = (ValidationRadio)v.findViewById(R.id.vr_input_elderly_info_gender);
		vrGender.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vrGender.setValid(true);
			}
		});
		vrGender.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_hideKeyboard();
			}
		});

		vtvBirthday = (ValidationBirthdayYYMMDD)v.findViewById(R.id.vtv_input_elderly_info_bday);
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
				_hideKeyboard();
			}
		});
		vtvBirthday.showDay(false);

		if (mGender != -1) {
			if (vetName != null) {
				vetName.setText(mName);
			}
			if (vrGender != null) {
				vrGender.selectItem(mGender == 1 ? 1 : 2);
			}
			if (vtvBirthday != null) {
				vtvBirthday.setBirthDayYYMMDD(mBirthdayYYMMDD);
			}
			if (vtvBirthday != null) {
				vtvBirthday.showWheelPicker(true);
			}
		}
    }

	private void _hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(vetName.getWindowToken(), 0);
	}

    public void setName(String name) {
		mName = name;
	}

	public void setBirthdayYYMMDD(String yyMMdd) {
		mBirthdayYYYYMM = yyMMdd;
	}

	public void setGender(int gender) {
		mGender = gender;
	}

    public boolean isValidInformation() {
		if (!vetName.isValid()) {
			vetName.showWarning(true, 1000);
		}
		if (!vrGender.isValid()) {
			vrGender.showWarning(true, 1000);
		}
		if (!vtvBirthday.isValid()) {
			vtvBirthday.showWarning(true, 1000);
		}

		return vetName.isValid() && vrGender.isValid() && vtvBirthday.isValid();
	}

    public String getName() {
		return vetName.getText();
	}

	public int getGender() {
		if (vrGender.getSelectedRadioIndex() == 2) { // Girl
			return 0;
		} else {
			return 1;
		}
	}

	public String getBirthdayStringYYMMDD() {
		return vtvBirthday.getSelectedDateStringYYMMDD();
	}

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what) {
				case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
					int state = msg.arg1;
					DeviceInfo deviceInfo = (DeviceInfo)msg.obj;

					if (deviceInfo != null) {
						if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "] " + state + " / " + deviceInfo.btmacAddress);

						if (state == DeviceConnectionState.BLE_CONNECTED) {
							if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
								mDlgProcessing.dismiss();
							}
							//_showContent(STEP_SETTING_COMPLETED);
						}
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
		((ConnectionActivity)mMainActivity).updateView();
		((ConnectionActivity)mMainActivity).setFragmentHandler(mHandler);

		if (mDlgProcessing == null) {
			mDlgProcessing = new ProgressCircleDialog(
                    mMainActivity,
					getString(R.string.dialog_contents_scanning),
					getString(R.string.btn_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mDlgProcessing.dismiss();
						}
					});
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
