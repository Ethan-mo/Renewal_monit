package goodmonit.monit.com.kao.connection.Package;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.NetworkSecurityType;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class ConnectionMonitPackageHubAddNewNetwork extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "AddNewNetwork";
	private static final boolean DBG = Configuration.DBG;

	private RelativeLayout rctnInputPassword, rctnSecurityType;
	private TextView tvSelectedApSecurity;
	private EditText etApName, etApPassword;
	private Button btnShowPassword;
	private int mSelectedApSecurity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_add_new_network, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(705);

        _initView(view);

		_showPassword(false);
        return view;
    }

	private void _initView(View v) {
		tvSelectedApSecurity = (TextView)v.findViewById(R.id.tv_connection_hub_add_new_network_security_value);
		etApName = (EditText)v.findViewById(R.id.et_connection_hub_add_new_network_name);
		etApPassword = (EditText)v.findViewById(R.id.et_connection_hub_add_new_network_input_ap_password);
		rctnInputPassword = (RelativeLayout)v.findViewById(R.id.rctn_connection_hub_add_new_network_input_password);
		rctnSecurityType = (RelativeLayout)v.findViewById(R.id.rctn_connection_hub_add_new_network_security);
		rctnSecurityType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_hideKeyboard();
				((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_HUB_SELECT_SECURITY);
			}
		});
		btnShowPassword = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_input_ap_password_show);
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


		etApPassword.addTextChangedListener(new TextWatcher() {
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

	private void _hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etApName.getWindowToken(), 0);
	}

	private void _showPassword(boolean show) {
		if (show) {
			etApPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			etApPassword.setTransformationMethod(null);
		} else {
			etApPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
			etApPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		etApPassword.setSelection(etApPassword.length());
	}

    private void _updateView() {
		switch(mSelectedApSecurity) {
			case NetworkSecurityType.NONE:
				tvSelectedApSecurity.setText(getString(R.string.connection_hub_network_security_none));
				rctnInputPassword.setVisibility(View.GONE);
				break;
			case NetworkSecurityType.WEP:
				tvSelectedApSecurity.setText(getString(R.string.connection_hub_network_security_wep));
				rctnInputPassword.setVisibility(View.VISIBLE);
				break;
			case NetworkSecurityType.WPA:
				tvSelectedApSecurity.setText(getString(R.string.connection_hub_network_security_wpa));
				rctnInputPassword.setVisibility(View.VISIBLE);
				break;
			case NetworkSecurityType.WPA2:
				tvSelectedApSecurity.setText(getString(R.string.connection_hub_network_security_wpa2));
				rctnInputPassword.setVisibility(View.VISIBLE);
				break;
			case NetworkSecurityType.WPA_TKIP:
				tvSelectedApSecurity.setText(getString(R.string.connection_hub_network_security_wpa_tkip));
				rctnInputPassword.setVisibility(View.VISIBLE);
				break;
			case NetworkSecurityType.WPA2_TKIP:
				tvSelectedApSecurity.setText(getString(R.string.connection_hub_network_security_wpa2_tkip));
				rctnInputPassword.setVisibility(View.VISIBLE);
				break;
		}
	}

	public String getApName() {
		return etApName.getText().toString();
	}

	public String getApPassword() {
		return etApPassword.getText().toString();
	}

	public int getApSecurityType() {
		return mSelectedApSecurity;
	}

    public void setApSecurityType(int securityType) {
		mSelectedApSecurity = securityType;
		_updateView();
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
		_updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
