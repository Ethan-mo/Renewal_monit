package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceInfo;

public class GroupSharedListItem extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "SharedItem";

	private Context mContext;
	private TextView tvGroupLeaderId, tvGroupLeaderShortId, tvDeviceListEmpty;
	private Button btnGroupLeave;
	private LinearLayout lctnSharedDeviceList;
	private ArrayList<DeviceInfo> mDeviceInfoList;

	public GroupSharedListItem(Context context) {
		super(context);
		mContext = context;
		_initView();
	}

	public GroupSharedListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_initView();
	}

	public GroupSharedListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		_initView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_group_shared_list_item, this, false);
		addView(v);

		tvGroupLeaderId = (TextView)v.findViewById(R.id.tv_group_shared_list_id);
		tvGroupLeaderShortId = (TextView)v.findViewById(R.id.tv_group_shared_list_short_id);
		btnGroupLeave = (Button)v.findViewById(R.id.btn_group_shared_list_leave);
		lctnSharedDeviceList = (LinearLayout)v.findViewById(R.id.lctn_group_shared_list_device_list);
		tvDeviceListEmpty = (TextView)v.findViewById(R.id.tv_group_shared_list_device_list_empty);
	}

	public void setGroupLeaderId(String nick) {
		tvGroupLeaderId.setText(nick);
	}

	public void setGroupLeaderShortId(String shortId) {
		tvGroupLeaderShortId.setText(shortId);
	}

	public void removeAllDeviceList() {
		lctnSharedDeviceList.removeAllViews();
	}

	public void setDeviceList(ArrayList<DeviceInfo> deviceInfoList) {
		mDeviceInfoList = deviceInfoList;
		if (mDeviceInfoList != null) {
			if (mDeviceInfoList.size() > 0) {
				tvDeviceListEmpty.setVisibility(View.GONE);
			}
			GroupDetailShareDevice shareDeviceList = null;
			for (int i = 0; i < deviceInfoList.size(); i++) {
				DeviceInfo deviceInfo = deviceInfoList.get(i);
				if (i % 2 == 0) {
					shareDeviceList = new GroupDetailShareDevice(mContext);
					lctnSharedDeviceList.addView(shareDeviceList);
					shareDeviceList.setLeftDeviceType(deviceInfo.type);
					shareDeviceList.setLeftDeviceName(deviceInfo.name);
				} else {
					shareDeviceList.setRightDeviceType(deviceInfo.type);
					shareDeviceList.setRightDeviceName(deviceInfo.name);
				}
			}
		}
	}

	public void setOnLeaveListener(OnClickListener listener) {
		btnGroupLeave.setOnClickListener(listener);
	}
}