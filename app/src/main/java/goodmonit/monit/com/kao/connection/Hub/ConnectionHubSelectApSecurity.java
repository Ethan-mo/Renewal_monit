package goodmonit.monit.com.kao.connection.Hub;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.NetworkSecurityType;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class ConnectionHubSelectApSecurity extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SelectSecurity";
	private static final boolean DBG = Configuration.DBG;

	private Button btnNONE, btnWEP, btnWPA, btnWPA2, btnWPATKIP, btnWPA2TKIP;

	private int mSelectedSecurityType = NetworkSecurityType.NONE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_add_new_network_security, container, false);
        mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(706);
        _initView(view);

		selectSecurityButton(mSelectedSecurityType);
        return view;
    }

	private void _initView(View v) {
		btnNONE = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_security_none);
		btnNONE.setOnClickListener(mListener);
		btnWEP = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_security_wep);
		btnWEP.setOnClickListener(mListener);
		btnWPA = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_security_wpa);
		btnWPA.setOnClickListener(mListener);
		btnWPA2 = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_security_wpa2);
		btnWPA2.setOnClickListener(mListener);
		btnWPATKIP = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_security_wpa_tkip);
		btnWPATKIP.setOnClickListener(mListener);
		btnWPA2TKIP = (Button)v.findViewById(R.id.btn_connection_hub_add_new_network_security_wpa2_tkip);
		btnWPA2TKIP.setOnClickListener(mListener);
    }

    public void selectSecurityButton(int securityType) {
		mSelectedSecurityType = securityType;
		btnNONE.setSelected(false);
		btnWEP.setSelected(false);
		btnWPA.setSelected(false);
		btnWPA2.setSelected(false);
		btnWPATKIP.setSelected(false);
		btnWPA2TKIP.setSelected(false);
		switch (mSelectedSecurityType) {
			case NetworkSecurityType.NONE:
				btnNONE.setSelected(true);
				mSelectedSecurityType = NetworkSecurityType.NONE;
				break;
			case NetworkSecurityType.WEP:
				btnWEP.setSelected(true);
				mSelectedSecurityType = NetworkSecurityType.WEP;
				break;
			case NetworkSecurityType.WPA:
				btnWPA.setSelected(true);
				mSelectedSecurityType = NetworkSecurityType.WPA;
				break;
			case NetworkSecurityType.WPA2:
				btnWPA2.setSelected(true);
				mSelectedSecurityType = NetworkSecurityType.WPA2;
				break;
			case NetworkSecurityType.WPA_TKIP:
				btnWPATKIP.setSelected(true);
				mSelectedSecurityType = NetworkSecurityType.WPA_TKIP;
				break;
			case NetworkSecurityType.WPA2_TKIP:
				btnWPA2TKIP.setSelected(true);
				mSelectedSecurityType = NetworkSecurityType.WPA2_TKIP;
				break;
		}
	}

	private View.OnClickListener mListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.btn_connection_hub_add_new_network_security_none:
					selectSecurityButton(NetworkSecurityType.NONE);
					break;
				case R.id.btn_connection_hub_add_new_network_security_wep:
					selectSecurityButton(NetworkSecurityType.WEP);
					break;
				case R.id.btn_connection_hub_add_new_network_security_wpa:
					selectSecurityButton(NetworkSecurityType.WPA);
					break;
				case R.id.btn_connection_hub_add_new_network_security_wpa2:
					selectSecurityButton(NetworkSecurityType.WPA2);
					break;
				case R.id.btn_connection_hub_add_new_network_security_wpa_tkip:
					selectSecurityButton(NetworkSecurityType.WPA_TKIP);
					break;
				case R.id.btn_connection_hub_add_new_network_security_wpa2_tkip:
					selectSecurityButton(NetworkSecurityType.WPA2_TKIP);
					break;

			}
		}
	};

	public void setSelectedSecurityType(int selectedSecurityType) {
		mSelectedSecurityType = selectedSecurityType;
	}

	public int getSelectedSecurityType() {
		return mSelectedSecurityType;
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
		selectSecurityButton(mSelectedSecurityType);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
