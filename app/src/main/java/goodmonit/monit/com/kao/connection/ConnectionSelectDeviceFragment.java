package goodmonit.monit.com.kao.connection;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class ConnectionSelectDeviceFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SelectDevice";
	private static final boolean DBG = Configuration.DBG;

	private RelativeLayout btnMonitReady, btnHubReady, btnLampReady, btnElderlyDiaperSensorReady;
	private Button btnHubNecessity;
	private ImageView ivHubIcon, ivHubMore;
	private TextView tvHubText;
	private TextView tvDescription;
	private SimpleDialog mDlgRegisterAnother;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_select_device, container, false);
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
		btnMonitReady = (RelativeLayout)v.findViewById(R.id.rctn_connection_select_device_monit);
		btnMonitReady.setOnClickListener(new View.OnClickListener() {
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
											((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_READY_FOR_CONNECTING);
											mDlgRegisterAnother.dismiss();
										}
									});
						}
                        try {
                            mDlgRegisterAnother.show();
                        } catch (Exception e) {

                        }
					} else {
						((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_READY_FOR_CONNECTING);
					}
				}
			}
		});

		btnHubReady = (RelativeLayout)v.findViewById(R.id.rctn_connection_select_device_hub);
		ivHubIcon = (ImageView)v.findViewById(R.id.iv_connection_select_device_item2);
		tvHubText = (TextView)v.findViewById(R.id.tv_connection_select_device_item2);
		ivHubMore = (ImageView)v.findViewById(R.id.iv_connection_select_device_more2);

		tvDescription = (TextView)v.findViewById(R.id.tv_connection_select_device_description);
		tvDescription.setText(getString(R.string.device_monit_hub_connection_condition) + "\n" + getString(R.string.device_monit_hub_lamp_differentiation));
		tvDescription.setVisibility(View.VISIBLE);

		btnLampReady = (RelativeLayout)v.findViewById(R.id.rctn_connection_select_device_lamp);
		btnLampReady.setVisibility(View.VISIBLE);
		btnLampReady.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMainActivity != null) {
					((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_LAMP_READY_FOR_CONNECTING);
				}
			}
		});

		btnElderlyDiaperSensorReady = (RelativeLayout) v.findViewById(R.id.rctn_connection_select_device_elderly_diaper_sensor);
		btnElderlyDiaperSensorReady.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMainActivity != null) {
					((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_ELDERLY_SENSOR_READY_FOR_CONNECTING);
				}
			}
		});

		if (Configuration.NEW_PRODUCT_MODE) {
			btnElderlyDiaperSensorReady.setVisibility(View.VISIBLE);
		} else {
			btnElderlyDiaperSensorReady.setVisibility(View.GONE);
		}
        /*
		btnHubNecessity = (Button)v.findViewById(R.id.btn_connection_select_device_hub_necessity);
		btnHubNecessity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMainActivity != null) {
					((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_HUB_NECESSITY);
				}
			}
		});
		*/
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

		if (ConnectionManager.getBleConnectedDeviceCount() == 0) {
			ivHubIcon.setImageResource(R.drawable.ic_device_connection_aqmhub_deactivated);
			tvHubText.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			ivHubMore.setImageResource(R.drawable.ic_direction_right_deactivated);
			btnHubReady.setBackgroundResource(R.color.colorWhite);
			btnHubReady.setOnClickListener(null);
		} else {
			ivHubIcon.setImageResource(R.drawable.ic_device_connection_aqmhub_activated);
			tvHubText.setTextColor(getResources().getColor(R.color.colorTextPrimary));
			ivHubMore.setImageResource(R.drawable.ic_direction_right_black_light);
			btnHubReady.setBackgroundResource(R.drawable.bg_btn_white_darklight_selector);
			btnHubReady.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mMainActivity != null) {
						((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_HUB_READY_FOR_CONNECTING);
					}
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
