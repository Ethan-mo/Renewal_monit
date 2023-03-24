package goodmonit.monit.com.kao.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.dialog.ProgressCircleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class SensorStatusFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SensorFragment";
	private static final boolean DBG = Configuration.DBG;

    private static final int MSG_UPDATE_VIEW	= 1;

	/* Intent request code */
	private static final int REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN				= 1;


	/* EasyConnecting */
	private boolean mIsEasyConnecting;
	private BluetoothDevice mEasyConnectingDevice;
	private HashMap<Integer, String> mEasyConnectingCandidates;

	/* UI */
	private Button btnAdd;
	private ListView lvSensorStatusRowList;
	private TextView tvListEmptyView;
	//private SensorStatusRowListAdapter mRowAdapter;
	private ProgressCircleDialog mDlgProcessing;
	private BluetoothAdapter mBluetoothAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_sensor_status, container, false);

		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		//mRowAdapter = new SensorStatusRowListAdapter(getActivity());
		mConnectionMgr = ConnectionManager.getInstance(mHandler);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (ConnectionManager.checkBluetoothStatus() == ConnectionManager.STATE_DISABLED) {
			Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(btEnableIntent, REQUEST_CODE_ENABLE_BLUETOOTH_FOR_SCAN);
		}

        _initView(view);
        return view;
    }

	private void _initView(View v) {
		tvListEmptyView = (TextView) v.findViewById(R.id.tv_frmt_sensor_status_empty_view);
		lvSensorStatusRowList = (ListView)v.findViewById(R.id.lv_frmt_sensor_status_row_list);
		//lvSensorStatusRowList.setAdapter(mRowAdapter);
		lvSensorStatusRowList.setEmptyView(tvListEmptyView);
		btnAdd = (Button) v.findViewById(R.id.btn_frmt_sensor_status_add);
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ConnectionActivity.class);
				startActivity(intent);
			}
		});

		mDlgProcessing = new ProgressCircleDialog(
				getActivity(),
				"센서를 찾고있습니다. 센서 전원을 켜주세요.",
				"취소",
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mConnectionMgr.isDiscovering()) {
							mConnectionMgr.cancelDiscovery();
						}
						mDlgProcessing.dismiss();
					}
				});

		//mRowAdapter.notifyDataSetChanged();
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
		mPreferenceMgr.setLatestForegroundFragmentId(ID_SENSOR_STATUS);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_UPDATE_VIEW:
					refreshView();
					sendEmptyMessageDelayed(MSG_UPDATE_VIEW, 1000);
					break;
				case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
					int state = msg.arg1;
					DeviceInfo deviceInfo = (DeviceInfo)msg.obj;
					//DeviceMonitSensor monitSensor = (DeviceMonitSensor)ConnectionManager.getDevice(DeviceType.DIAPER_SENSOR, deviceInfo.deviceId);
					//String address = monitSensor.getMacAddress();

					if (state == DeviceConnectionState.BLE_CONNECTED) {
						refreshView();
						if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
							mDlgProcessing.dismiss();
						}
						mEasyConnectingDevice = null;
					} else if (state == DeviceConnectionState.DISCONNECTED) {
						refreshView();
						if (mEasyConnectingDevice != null) {
							String easyConnectDeviceAddress = mEasyConnectingDevice.getAddress();
							if (easyConnectDeviceAddress != null){// && easyConnectDeviceAddress.equals(address)) {
								if (DBG) Log.i(TAG, "easyconnect device disconnected");
								if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
									mDlgProcessing.dismiss();
								}
								mEasyConnectingDevice = null;
								break;
							}
							//if (DBG) Log.i(TAG, "other device disconnected");
							break;
						}
					}
					break;
				case ConnectionManager.MSG_SCAN_RESULT:
					BluetoothDevice device = (BluetoothDevice)msg.obj;
					int rssi = msg.arg1;
					if (DBG) Log.d(TAG, "MSG_SCAN_RESULT : " + rssi + " / " + device.getName() + " / " + device.getAddress() + " / " + device.getType());
					if (mIsEasyConnecting) {
						if (rssi > -50) {
							mEasyConnectingDevice = device;
							//_connectEasyConnectDevice(device);
							mConnectionMgr.cancelDiscovery();
						} else {
							mEasyConnectingCandidates.put(rssi, device.getAddress());
						}
					}
					break;

				case ConnectionManager.MSG_SCAN_FINISHED:
					if (DBG) Log.e(TAG, "MSG_SCAN_FINISHED");
					if (mIsEasyConnecting) {
						mIsEasyConnecting = false;
						if (mEasyConnectingDevice == null) {
							if (mEasyConnectingCandidates.size() == 0) { // Can not find easy connect target
								if (DBG) Log.i(TAG, "no candidates");
								if (mDlgProcessing != null && mDlgProcessing.isShowing()) {
									mDlgProcessing.dismiss();
								}
							} else {
								//_connectEasyConnectCandidate();
							}
						}
					}
					break;
			}
		}
	};

	public void refreshView() {
		//if (mRowAdapter.getCount() > 0) {
		//	mRowAdapter.notifyDataSetChanged();
		//}
	}


	private void _startEasyConnect() {
		if (DBG) Log.d(TAG, "_startEasyConnect");
		mIsEasyConnecting = true;
		if (mEasyConnectingCandidates == null) {
			mEasyConnectingCandidates = new HashMap<Integer, String>();
		} else {
			mEasyConnectingCandidates.clear();
		}
        try {
            mDlgProcessing.show();
        } catch (Exception e) {

        }
		if (mConnectionMgr.isDiscovering()) {
			mConnectionMgr.cancelDiscovery();
		}
		mConnectionMgr.startDiscovery();
	}

}
