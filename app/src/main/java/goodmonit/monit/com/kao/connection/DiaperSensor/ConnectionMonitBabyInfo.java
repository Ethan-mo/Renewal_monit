package goodmonit.monit.com.kao.connection.DiaperSensor;

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
import goodmonit.monit.com.kao.widget.ValidationImageButtons;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class ConnectionMonitBabyInfo extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "BabyInfo";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;
	private Button btnHelp;
	private ProgressCircleDialog mDlgProcessing;
	private ValidationEditText vetBabyName;
	private ValidationRadio vrBabySex;
	private ValidationBirthdayYYMMDD vtvBabyBirthday;
	private ValidationImageButtons vibBabyEating;

	private ValidationManager mValidationMgr;
	private ScrollView svBabyInfo;

	private String mBabyName;
	private String mBabyBirthdayYYYYMM;
	private String mBabyBirthdayYYMMDD;
	private int mBabySex;
	private int mBabyEating;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_monit_babyinfo, container, false);
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

		vetBabyName = (ValidationEditText)v.findViewById(R.id.vet_input_baby_info_babyname);
		vetBabyName.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vetBabyName.setValid(mValidationMgr.isValidBabyname(vetBabyName.getText()));
			}
		});

		vrBabySex = (ValidationRadio)v.findViewById(R.id.vr_input_baby_info_babysex);
		vrBabySex.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vrBabySex.setValid(true);
			}
		});
		vrBabySex.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_hideKeyboard();
			}
		});

		vtvBabyBirthday = (ValidationBirthdayYYMMDD)v.findViewById(R.id.vtv_input_baby_info_babybday);
		vtvBabyBirthday.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vtvBabyBirthday.setValid(true);
			}
		});
		vtvBabyBirthday.setBirthdayFromYear(2019);
		vtvBabyBirthday.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_hideKeyboard();
			}
		});
		vtvBabyBirthday.showDay(false);

		vibBabyEating = (ValidationImageButtons) v.findViewById(R.id.vib_input_baby_info_eating);
		vibBabyEating.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				if (vibBabyEating.getSelectedImageButtonIdx() == 0) {
					vibBabyEating.setValid(false);
				} else {
					vibBabyEating.setValid(true);
				}
			}
		});
		vibBabyEating.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_hideKeyboard();
			}
		});

		if (mBabySex != -1) {
			if (vetBabyName != null) {
				vetBabyName.setText(mBabyName);
			}
			if (vrBabySex != null) {
				vrBabySex.selectItem(mBabySex == 1 ? 1 : 2);
			}
			if (vtvBabyBirthday != null) {
				vtvBabyBirthday.setBirthDayYYMMDD(mBabyBirthdayYYMMDD);
			}
			if (vtvBabyBirthday != null) {
				vtvBabyBirthday.showWheelPicker(true);
			}
			if (vibBabyEating != null) {
				vibBabyEating.selectItem(mBabyEating);
			}
		}
    }

	private void _hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(vetBabyName.getWindowToken(), 0);
	}

    public void setBabyName(String name) {
		mBabyName = name;
	}

	public void setBabyBirthdayYYMMDD(String yyMMdd) {
		mBabyBirthdayYYYYMM = yyMMdd;
	}

	public void setBabySex(int sex) {
		mBabySex = sex;
	}

	public void setBabyEating(int eating) {
		mBabyEating = eating;
	}

    public boolean isValidInformation() {
		if (!vetBabyName.isValid()) {
			vetBabyName.showWarning(true, 1000);
		}
		if (!vrBabySex.isValid()) {
			vrBabySex.showWarning(true, 1000);
		}
		if (!vtvBabyBirthday.isValid()) {
			vtvBabyBirthday.showWarning(true, 1000);
		}
		if (!vibBabyEating.isValid()) {
			vibBabyEating.showWarning(true, 1000);
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					if (DBG) Log.d(TAG, "move focus");
					int vTop = vibBabyEating.getTop();
					svBabyInfo.smoothScrollTo(0, vTop);
				}
			});
		}

		return vetBabyName.isValid() && vrBabySex.isValid() && vtvBabyBirthday.isValid() && vibBabyEating.isValid();
	}

    public String getBabyName() {
		return vetBabyName.getText();
	}

	public int getBabySex() {
		if (vrBabySex.getSelectedRadioIndex() == 2) { // Girl
			return 0;
		} else {
			return 1;
		}
	}

	public int getBabyEating() {
		return vibBabyEating.getSelectedImageButtonIdx();
	}

	public String getBabyBirthdayStringYYMMDD() {
		return vtvBabyBirthday.getSelectedDateStringYYMMDD();
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
