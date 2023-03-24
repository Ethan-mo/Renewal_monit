package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class DeviceStatusRowWidget extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "DeviceStatus";

	protected Context mContext;
	protected int mThemeColor, mUnavailableColor;
	protected TextView tvTitle;
	protected TextView tvDeviceName;
	protected TextView ivDeviceIcon;
	protected TextView tvDescription;
	protected LinearLayout lctnDeviceStatusDashboard, lctnDeviceStatusItem1, lctnDeviceStatusItem2, lctnDeviceStatusItem3, lctnDeviceBatteryPower;
	protected RelativeLayout rctnTitlebar;
	protected ImageView ivDeviceIconBackground;
	protected ImageView ivStatus1New, ivStatus2New, ivStatus3New;
	protected TextView tvStatus1Text, tvStatus2Text, tvStatus3Text;
	protected TextView tvStatus1TextUnit, tvStatus2TextUnit, tvStatus3TextUnit;
	protected ImageView ivStatus1Icon, ivStatus2Icon, ivStatus3Icon;
	protected ImageView ivConnectionType, ivNewMark, ivBatteryPower, ivBatteryChargingPower;
	protected TextView tvStatus1Content, tvStatus2Content, tvStatus3Content;
	protected String mDeviceName;
	protected long mDeviceId;
	protected PreferenceManager mPreferenceMgr;
	protected View vUnderlineOtherCategory, vUnderlineSameCategory;
	protected Button btnReconnect;
	protected TextView tvBatteryPower, tvBatteryChargingPower;

	public DeviceStatusRowWidget(Context context) {
		super(context);
		_initView();
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
	}

	public DeviceStatusRowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
	}

	public DeviceStatusRowWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_device_status_row, this, false);
		addView(v);

		setBackgroundResource(R.drawable.bg_btn_white_darklight_selector);

		mThemeColor = getContext().getResources().getColor(R.color.colorTextLampCategory);
		mUnavailableColor = getContext().getResources().getColor(R.color.colorTextDeviceDisconnected);

		btnReconnect = (Button)v.findViewById(R.id.btn_device_status_reconnect);

		tvTitle = (TextView)v.findViewById(R.id.tv_device_status_title);
		tvDeviceName = (TextView)v.findViewById(R.id.tv_device_status_name);
		tvDescription = (TextView)v.findViewById(R.id.tv_device_status_description);

		lctnDeviceStatusDashboard = (LinearLayout)v.findViewById(R.id.lctn_device_status_dashboard);

		ivDeviceIcon = (TextView)v.findViewById(R.id.tv_device_status_device_icon);
		ivDeviceIconBackground = (ImageView)v.findViewById(R.id.iv_device_status_device_icon_background);

		lctnDeviceStatusItem1 = (LinearLayout)v.findViewById(R.id.lctn_device_status_item1);
		tvStatus1Text = (TextView)v.findViewById(R.id.tv_device_status_text1);
		tvStatus1Text.setText("");
		tvStatus1TextUnit = (TextView)v.findViewById(R.id.tv_device_status_text1_unit);
		tvStatus1TextUnit.setText("");
		tvStatus1Content = (TextView)v.findViewById(R.id.tv_device_status_content1);
		tvStatus1Content.setText("");
		ivStatus1Icon = (ImageView)v.findViewById(R.id.iv_device_status_icon1);
		ivStatus1Icon.setBackgroundResource(0);
		ivStatus1New = (ImageView)v.findViewById(R.id.iv_device_status_icon1_new);

		lctnDeviceStatusItem2 = (LinearLayout)v.findViewById(R.id.lctn_device_status_item2);
		tvStatus2Text = (TextView)v.findViewById(R.id.tv_device_status_text2);
		tvStatus2Text.setText("");
		tvStatus2TextUnit = (TextView)v.findViewById(R.id.tv_device_status_text2_unit);
		tvStatus2TextUnit.setText("");
		tvStatus2Content = (TextView)v.findViewById(R.id.tv_device_status_content2);
		tvStatus2Content.setText("");
		ivStatus2Icon = (ImageView)v.findViewById(R.id.iv_device_status_icon2);
		ivStatus2Icon.setBackgroundResource(0);
		ivStatus2New = (ImageView)v.findViewById(R.id.iv_device_status_icon2_new);

		lctnDeviceStatusItem3 = (LinearLayout)v.findViewById(R.id.lctn_device_status_item3);
		tvStatus3Text = (TextView)v.findViewById(R.id.tv_device_status_text3);
		tvStatus3Text.setText("");
		tvStatus3TextUnit = (TextView)v.findViewById(R.id.tv_device_status_text3_unit);
		tvStatus3TextUnit.setText("");
		tvStatus3Content = (TextView)v.findViewById(R.id.tv_device_status_content3);
		tvStatus3Content.setText("");
		ivStatus3Icon = (ImageView)v.findViewById(R.id.iv_device_status_icon3);
		ivStatus3Icon.setBackgroundResource(0);
		ivStatus3New = (ImageView)v.findViewById(R.id.iv_device_status_icon3_new);

		ivConnectionType = (ImageView)v.findViewById(R.id.iv_device_status_connection_type);
		ivConnectionType.setBackgroundResource(0);

		lctnDeviceBatteryPower = (LinearLayout)v.findViewById(R.id.lctn_device_status_battery_power);

		ivBatteryPower = (ImageView)v.findViewById(R.id.iv_device_status_battery_power);
		ivBatteryPower.setBackgroundResource(0);

		tvBatteryPower = (TextView)v.findViewById(R.id.tv_device_status_battery_power);

		ivBatteryChargingPower = (ImageView)v.findViewById(R.id.iv_device_status_battery_charging_power);
		tvBatteryChargingPower = (TextView)v.findViewById(R.id.tv_device_status_battery_charging_power);

		ivNewMark = (ImageView)v.findViewById(R.id.iv_device_status_new_mark);

		rctnTitlebar = (RelativeLayout)v.findViewById(R.id.rctn_device_status_titlebar);
		vUnderlineOtherCategory = v.findViewById(R.id.v_device_status_underline_other_category);
		vUnderlineSameCategory = v.findViewById(R.id.v_device_status_underline_same_category);
		vUnderlineSameCategory.setVisibility(View.GONE);
	}

	public void setTitle(String title) {
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}

	public void setDeviceIcon(int resId) {
		if (ivDeviceIcon != null) {
			ivDeviceIcon.setBackgroundResource(resId);
		}
	}

	public void setDeviceName(String name) {
		mDeviceName = name;
		if (tvDeviceName != null) {
			tvDeviceName.setText(name);
		}
	}

	public void setDescription(String description) {
		if (tvDescription != null) {
			tvDescription.setText(description);
		}
	}

	public void showDescription(boolean show) {
		if (tvDescription != null) {
			if (show) {
				tvDescription.setVisibility(View.VISIBLE);
			} else {
				tvDescription.setVisibility(View.GONE);
			}
		}
	}

	public void setStatus1Text(String text) {
		if (tvStatus1Text != null) {
			tvStatus1Text.setText(text);
		}
	}

	public void setStatus2Text(String text) {
		if (tvStatus2Text != null) {
			tvStatus2Text.setText(text);
		}
	}

	public void setStatus3Text(String text) {
		if (tvStatus3Text != null) {
			tvStatus3Text.setText(text);
		}
	}

	public void setStatus1Icon(int resId) {
		if (ivStatus1Icon != null) {
			ivStatus1Icon.setBackgroundResource(resId);
		}
	}

	public void setStatus2Icon(int resId) {
		if (ivStatus2Icon != null) {
			ivStatus2Icon.setBackgroundResource(resId);
		}
	}

	public void setStatus3Icon(int resId) {
		if (ivStatus3Icon != null) {
			ivStatus3Icon.setBackgroundResource(resId);
		}
	}

	public void setStatus1Content(String text) {
		if (tvStatus1Content != null) {
			tvStatus1Content.setText(text);
		}
	}

	public void setStatus2Content(String text) {
		if (tvStatus2Content != null) {
			tvStatus2Content.setText(text);
		}
	}

	public void setStatus3Content(String text) {
		if (tvStatus3Content != null) {
			tvStatus3Content.setText(text);
		}
	}

	public void setDeviceId(long deviceId) {
		mDeviceId = deviceId;
	}

	public long getDeviceId() {
		return mDeviceId;
	}

	public String getDeviceName() {
		return mDeviceName;
	}

	public void showTitlebar(boolean show) {
		if (show) {
			rctnTitlebar.setVisibility(View.VISIBLE);
		} else {
			rctnTitlebar.setVisibility(View.GONE);
		}
	}

	public void setOtherCategory(boolean other) {
		if (other) {
			vUnderlineSameCategory.setVisibility(View.GONE);
			vUnderlineOtherCategory.setVisibility(View.VISIBLE);
		} else {
			vUnderlineSameCategory.setVisibility(View.VISIBLE);
			vUnderlineOtherCategory.setVisibility(View.GONE);
		}
	}

	public void setDeviceStatusItem3Visible(boolean visible) {
		if (lctnDeviceStatusItem3 != null) {
			if (visible) {
				lctnDeviceStatusItem3.setVisibility(View.VISIBLE);
			} else {
				lctnDeviceStatusItem3.setVisibility(View.GONE);
			}
		}
	}
}