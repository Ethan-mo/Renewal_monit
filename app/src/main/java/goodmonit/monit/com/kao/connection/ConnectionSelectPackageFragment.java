package goodmonit.monit.com.kao.connection;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class ConnectionSelectPackageFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SelectPackage";
	private static final boolean DBG = Configuration.DBG;

	private RelativeLayout btnPackageReady;
	private SimpleDialog mDlgRegisterAnother;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_select_package, container, false);
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
        _initView(view);

		int available = ConnectionManager.checkBluetoothStatus();
		if (available == ConnectionManager.STATE_DISABLED || available == ConnectionManager.STATE_UNAVAILABLE) {
			Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(btEnableIntent, ConnectionActivity.REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN);
		}
        return view;
    }

	private void _initView(View v) {
		btnPackageReady = (RelativeLayout)v.findViewById(R.id.rctn_connection_select_device_monit_package);
		btnPackageReady.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMainActivity != null) {
					if (ConnectionManager.getDeviceBLEConnectionList().values().size() > 0) {
						if (mDlgRegisterAnother == null) {
							mDlgRegisterAnother = new SimpleDialog(
									mMainActivity,
									getString(R.string.connection_already_registered_sensor),
									getString(R.string.btn_no),
									new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											mDlgRegisterAnother.dismiss();
										}
									},
									getString(R.string.btn_yes),
									new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_PREPARE_DIAPER_SENSOR);
											mDlgRegisterAnother.dismiss();
										}
									});
						}
                        try {
                            mDlgRegisterAnother.show();
                        } catch (Exception e) {

                        }
					} else {
						((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_PREPARE_DIAPER_SENSOR);
					}
				}
			}
		});
    }

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume : " + ConnectionManager.getBleConnectedDeviceCount());
		mMainActivity = getActivity();
		((ConnectionActivity)mMainActivity).updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
