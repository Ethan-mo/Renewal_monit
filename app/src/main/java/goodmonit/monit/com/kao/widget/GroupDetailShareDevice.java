package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceType;

public class GroupDetailShareDevice extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "GroupDetailShareDevice";

	private int mDeviceType1, mDeviceType2;
	private String mDeviceName1, mDeviceName2;

	private ImageView ivDeviceType1, ivDeviceType2;
	private TextView tvDeviceType1, tvDeviceType2;
	private TextView tvDeviceName1, tvDeviceName2;

	public GroupDetailShareDevice(Context context) {
		super(context);
		_initView();
	}

	public GroupDetailShareDevice(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public GroupDetailShareDevice(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_group_detail_share_device, this, false);
		addView(v);

		ivDeviceType1 = (ImageView)v.findViewById(R.id.iv_group_detail_share_device_icon1);
		ivDeviceType2 = (ImageView)v.findViewById(R.id.iv_group_detail_share_device_icon2);
		tvDeviceType1 = (TextView)v.findViewById(R.id.tv_group_detail_share_device_type1);
		tvDeviceType2 = (TextView)v.findViewById(R.id.tv_group_detail_share_device_type2);
		tvDeviceName1 = (TextView)v.findViewById(R.id.tv_group_detail_share_device_name1);
		tvDeviceName2 = (TextView)v.findViewById(R.id.tv_group_detail_share_device_name2);
	}

	public void setLeftDeviceType(int type) {
		mDeviceType1 = type;
		switch(mDeviceType1) {
			case DeviceType.DIAPER_SENSOR:
				if (ivDeviceType1 != null) ivDeviceType1.setBackgroundResource(R.drawable.ic_device_sensor);
				if (tvDeviceType1 != null) tvDeviceType1.setText(getContext().getString(R.string.device_type_diaper_sensor));
				break;
			case DeviceType.AIR_QUALITY_MONITORING_HUB:
				if (ivDeviceType1 != null) ivDeviceType1.setBackgroundResource(R.drawable.ic_device_aqmhub);
				if (tvDeviceType1 != null) tvDeviceType1.setText(getContext().getString(R.string.device_type_hub));
				break;
			case DeviceType.LAMP:
				if (ivDeviceType1 != null) ivDeviceType1.setBackgroundResource(R.drawable.ic_device_lamp);
				if (tvDeviceType1 != null) tvDeviceType1.setText(getContext().getString(R.string.device_type_lamp));
				break;
			case DeviceType.ELDERLY_DIAPER_SENSOR:
				if (ivDeviceType1 != null) ivDeviceType1.setBackgroundResource(R.drawable.ic_device_sensor);
				if (tvDeviceType1 != null) tvDeviceType1.setText(getContext().getString(R.string.device_type_elderly_diaper_sensor));
				break;
		}
	}

	public void setLeftDeviceName(String name){
		mDeviceName1 = name;
		if (tvDeviceName1 != null) tvDeviceName1.setText(mDeviceName1);
	}

	public void setRightDeviceType(int type) {
		mDeviceType2 = type;
		switch(mDeviceType2) {
			case DeviceType.DIAPER_SENSOR:
				if (ivDeviceType2 != null) ivDeviceType2.setBackgroundResource(R.drawable.ic_device_sensor);
				if (tvDeviceType2 != null) tvDeviceType2.setText(getContext().getString(R.string.device_type_diaper_sensor));
				break;
			case DeviceType.AIR_QUALITY_MONITORING_HUB:
				if (ivDeviceType2 != null) ivDeviceType2.setBackgroundResource(R.drawable.ic_device_aqmhub);
				if (tvDeviceType2 != null) tvDeviceType2.setText(getContext().getString(R.string.device_type_hub));
				break;
			case DeviceType.LAMP:
				if (ivDeviceType2 != null) ivDeviceType2.setBackgroundResource(R.drawable.ic_device_lamp);
				if (tvDeviceType2 != null) tvDeviceType2.setText(getContext().getString(R.string.device_type_lamp));
				break;
			case DeviceType.ELDERLY_DIAPER_SENSOR:
				if (ivDeviceType2 != null) ivDeviceType2.setBackgroundResource(R.drawable.ic_device_sensor);
				if (tvDeviceType2 != null) tvDeviceType2.setText(getContext().getString(R.string.device_type_elderly_diaper_sensor));
				break;
		}
	}

	public void setRightDeviceName(String name){
		mDeviceName2 = name;
		if (tvDeviceName2 != null) tvDeviceName2.setText(mDeviceName2);
	}
}