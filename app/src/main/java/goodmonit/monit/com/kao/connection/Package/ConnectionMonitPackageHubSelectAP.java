package goodmonit.monit.com.kao.connection.Package;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.NetworkSecurityType;
import goodmonit.monit.com.kao.devices.HubApInfo;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

import static goodmonit.monit.com.kao.activity.ConnectionActivity.PARAM_FOR_NEW_NETWORK;

public class ConnectionMonitPackageHubSelectAP extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "PkgSelectAP";
	private static final boolean DBG = Configuration.DBG;

	//private static final int MSG_SCAN_AP			= 1;
	private static final int MSG_SCAN_AP_TIME_OUT	= 2;

	private static final int SCAN_AP_TIME_OUT_SEC	= 8;

	private Button btnScanAp, btnScanRefresh;
	private TextView tvDetail, tvEmptyList, tvStatus, tvTitle;
	private ProgressBar pbProgress;

	private SimpleDialog mDlgFailedToGetScanList;

	private ListView lvWifiApList;
	private ScannedApListAdatper mAdapter;
	private RelativeLayout rctnScanList;
	private boolean receivedApInfo;
	private boolean receivedFromHub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_select_ap, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(702);

        _initView(view);

        return view;
    }

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_SCAN_AP_TIME_OUT:
					pbProgress.setVisibility(View.GONE);
					btnScanRefresh.setVisibility(View.VISIBLE);
					tvStatus.setText(getString(R.string.connection_hub_scan_network_list));
					mAdapter.addNewNetworkRow();
					mAdapter.notifyDataSetChanged();
					if (receivedApInfo && receivedFromHub) { // 센서를 통해 허브로부터 AP리스트 받은 경우
						tvTitle.setText(R.string.connection_hub_scan_network_list);
						tvDetail.setText(R.string.connection_hub_select_ap_detail);
					} else {
						if (mDlgFailedToGetScanList != null && !mDlgFailedToGetScanList.isShowing()) {
							try {
								mDlgFailedToGetScanList.show();
							} catch (Exception e) {

							}
						}
					}
					break;
				case ConnectionManager.MSG_HUB_WIFI_SCAN_LIST:
					HubApInfo apInfo = (HubApInfo)msg.obj;
					if (DBG) Log.e(TAG, "MSG_HUB_WIFI_SCAN_LIST: " + apInfo.index + " / " + apInfo.name);
					receivedApInfo = true;
					if (apInfo.rssi == 1) receivedFromHub = true;
					if (apInfo.index > -1) {
						mAdapter.addScanResult(apInfo);
						mAdapter.notifyDataSetChanged();
					}
					break;
			}
		}
	};

	private void startScan() {
		receivedApInfo = false;
		receivedFromHub = false;
		((ConnectionActivity)mMainActivity).sendStartWifiScan();
		mAdapter.clearScanResult();
		mAdapter.notifyDataSetChanged();
		rctnScanList.setVisibility(View.VISIBLE);
		tvStatus.setText(getString(R.string.connection_hub_scanning_status));
		pbProgress.setVisibility(View.VISIBLE);
		btnScanRefresh.setVisibility(View.GONE);
		mHandler.sendEmptyMessageDelayed(MSG_SCAN_AP_TIME_OUT, SCAN_AP_TIME_OUT_SEC * 1000L);
	}

	private void _initView(View v) {
		rctnScanList = (RelativeLayout)v.findViewById(R.id.rctn_connection_hub_scan_list);
		btnScanRefresh = (Button)v.findViewById(R.id.btn_connection_hub_scan_refresh);
		btnScanRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startScan();
			}
		});

		btnScanAp = (Button)v.findViewById(R.id.btn_connection_hub_scan_ap);
		btnScanAp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startScan();
				btnScanAp.setVisibility(View.GONE);
			}
		});
		tvTitle = (TextView)v.findViewById(R.id.tv_connection_hub_select_ap_title);
		tvDetail = (TextView)v.findViewById(R.id.tv_connection_hub_select_ap_detail);
		tvStatus = (TextView)v.findViewById(R.id.tv_connection_hub_scanned_ap_status);
		pbProgress = (ProgressBar)v.findViewById(R.id.pb_connection_hub_scanned_ap_progressing);
		lvWifiApList = (ListView)v.findViewById(R.id.lv_connection_hub_scanned_ap);
		lvWifiApList.setDivider(null);
		mAdapter = new ScannedApListAdatper(getContext());
		lvWifiApList.setAdapter(mAdapter);

		if (mDlgFailedToGetScanList == null) {
			mDlgFailedToGetScanList = new SimpleDialog(
					mContext,
					"[Code" + ConnectionActivity.CODE_HELP_HUB_AP_NOT_CONNECTED + "]",
					mContext.getString(R.string.dialog_connection_not_received_ap_info),
					mContext.getString(R.string.btn_ok),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDlgFailedToGetScanList.dismiss();
						}
					});
		}
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
		((ConnectionActivity)mMainActivity).setFragmentHandler(mHandler);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
		mHandler.removeMessages(MSG_SCAN_AP_TIME_OUT);
	}

	class ScannedApListAdatper extends BaseAdapter {
		private LayoutInflater mmInflater;
		private Context mmContext;
		private ArrayList<HubApInfo> mmHubApInfoList;

		public ScannedApListAdatper(Context c) {
			mmContext = c;
			mmInflater = (LayoutInflater)mmContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mmHubApInfoList = new ArrayList<>();
		}

		public void addNewNetworkRow() {
			HubApInfo newNetwork = new HubApInfo();
			newNetwork.name = getString(R.string.connection_hub_scanning_add_new_network);
			newNetwork.index = PARAM_FOR_NEW_NETWORK;
			newNetwork.securityType = NetworkSecurityType.NONE;
			newNetwork.rssi = PARAM_FOR_NEW_NETWORK;
			mmHubApInfoList.add(newNetwork);
		}

		public void clearScanResult() {
			mmHubApInfoList.clear();
		}

		public void addScanResult(HubApInfo apInfo) {
			if (apInfo.currentSelected) {
				mmHubApInfoList.add(0, apInfo);
			} else {
				mmHubApInfoList.add(apInfo);
			}
		}

		@Override
		public int getCount() {
			return mmHubApInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			final HubApInfo result = mmHubApInfoList.get(pos);
			ViewHolder viewHolder;

			TextView tvApName;
			ImageView ivSecurity;
			ImageView ivSignalPower;
			Button btnApConnected;
			TextView tvApConnected;

			if (convertView == null) {
				convertView = mmInflater.inflate(R.layout.widget_scanned_ap_list_row, parent, false);
				tvApName = (TextView)convertView.findViewById(R.id.tv_connection_scanned_ap_list_name);
				ivSecurity = (ImageView)convertView.findViewById(R.id.iv_connection_scanned_ap_list_security);
				ivSignalPower = (ImageView)convertView.findViewById(R.id.iv_connection_scanned_ap_list_signal_power);
				btnApConnected = (Button)convertView.findViewById(R.id.btn_connection_scanned_ap_list_name_connected);
				tvApConnected = (TextView)convertView.findViewById(R.id.tv_connection_scanned_ap_list_name_connected);

				viewHolder = new ViewHolder();
				viewHolder.tvApName = tvApName;
				viewHolder.ivSecurity = ivSecurity;
				viewHolder.ivSignalPower = ivSignalPower;
				viewHolder.btnApConnected = btnApConnected;
				viewHolder.tvApConnected = tvApConnected;
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				tvApName = viewHolder.tvApName;
				ivSecurity = viewHolder.ivSecurity;
				ivSignalPower = viewHolder.ivSignalPower;
				tvApConnected = viewHolder.tvApConnected;
				btnApConnected = viewHolder.btnApConnected;
			}

			if (result.currentSelected) {
				btnApConnected.setVisibility(View.VISIBLE);
				tvApConnected.setVisibility(View.VISIBLE);
				if (result.isConnected) {
					btnApConnected.setSelected(true);
					tvApConnected.setText(getString(R.string.setting_ap_info_connected));
					tvApConnected.setTextColor(getResources().getColor(R.color.colorPrimary));
				} else {
					btnApConnected.setSelected(false);
					tvApConnected.setText(getString(R.string.setting_ap_info_not_connected));
					tvApConnected.setTextColor(getResources().getColor(R.color.colorTextPrimaryLight));
				}
			} else {
				btnApConnected.setVisibility(View.GONE);
				tvApConnected.setVisibility(View.GONE);
			}

			byte[] byteName = null;
			try {
				byteName = result.name.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				byteName = result.name.getBytes();
			}

			if (byteName.length >= 15) {
				tvApName.setText(result.name + "...");
			} else {
				tvApName.setText(result.name);
			}

			if (result.securityType == NetworkSecurityType.NONE) {
				ivSecurity.setVisibility(View.GONE);
			} else {
				ivSecurity.setVisibility(View.VISIBLE);
			}

			if (result.rssi == 99) {
				ivSignalPower.setBackgroundResource(0);
			} else {
				switch (WifiManager.calculateSignalLevel(result.rssi, 4)) {
					case 4:
						ivSignalPower.setBackgroundResource(R.drawable.ic_wifi_signal_power_3);
						break;
					case 3:
						ivSignalPower.setBackgroundResource(R.drawable.ic_wifi_signal_power_3);
						break;
					case 2:
						ivSignalPower.setBackgroundResource(R.drawable.ic_wifi_signal_power_2);
						break;
					case 1:
						ivSignalPower.setBackgroundResource(R.drawable.ic_wifi_signal_power_1);
						break;
					case 0:
						ivSignalPower.setBackgroundResource(R.drawable.ic_wifi_signal_power_0);
						break;
				}
			}
			//ivSignalPower.setVisibility(View.INVISIBLE);

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((ConnectionActivity)mMainActivity).setTargetApInfo(result);
					if (result.index == PARAM_FOR_NEW_NETWORK) {
						((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_HUB_ADD_NETWORK);
					} else {
						if (result.securityType == NetworkSecurityType.NONE) {
							if (DBG) Log.d(TAG, "Non-secured AP");
							((ConnectionActivity) mMainActivity).sendTargetApInfo();
						} else {
							if (DBG) Log.d(TAG, "Secured AP");
							((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_MONIT_PACKAGE_HUB_INPUT_PASSWORD);
						}
					}
				}
			});

			return convertView;
		}
	}

	public class ViewHolder {
		public TextView tvApName;
		public ImageView ivSecurity;
		public ImageView ivSignalPower;
		public Button btnApConnected;
		public TextView tvApConnected;
	}
}
