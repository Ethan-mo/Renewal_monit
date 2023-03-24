package goodmonit.monit.com.kao.connection.Hub;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class ConnectionHubInputApPassword extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "InputPw";
	private static final boolean DBG = Configuration.DBG;
	private TextView tvSSID;
	private EditText etPassword;
	private Button btnShowPassword;

	private String mApSSID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_input_ap_password, container, false);
        mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(703);

        _initView(view);

		if (mApSSID != null) {
			if (mApSSID.length() > 20) {
				tvSSID.append("\"" + mApSSID.substring(0, 20) + "...\"");
			} else {
				tvSSID.append("\"" + mApSSID + "\"");
			}
		}

		_showPassword(false);
        return view;
    }

    public void setApSSID(String ssid) {
		mApSSID = ssid;
	}

	public String getPassword() {
		return etPassword.getText().toString();
	}

	private void _initView(View v) {
		tvSSID = (TextView)v.findViewById(R.id.tv_connection_hub_input_password_detail);
		etPassword = (EditText)v.findViewById(R.id.et_connection_hub_input_ap_password);
		btnShowPassword = (Button)v.findViewById(R.id.btn_connection_hub_input_ap_password_show);
		btnShowPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnShowPassword.isSelected()) {
					btnShowPassword.setSelected(false);
					_showPassword(false);
				} else {
					btnShowPassword.setSelected(true);
					_showPassword(true);
				}
			}
		});
		btnShowPassword.setVisibility(View.GONE);

		etPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

			@Override
			public void afterTextChanged(Editable s) {
				String text = s.toString();
				if (text.length() > 0) {
					btnShowPassword.setVisibility(View.VISIBLE);
				} else {
					btnShowPassword.setVisibility(View.GONE);
				}
			}
		});
    }

	private void _showPassword(boolean show) {
		if (show) {
			etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			etPassword.setTransformationMethod(null);
		} else {
			etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
			etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		etPassword.setSelection(etPassword.length());
	}

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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
