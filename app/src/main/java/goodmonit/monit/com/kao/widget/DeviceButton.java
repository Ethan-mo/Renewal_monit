package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceType;

public class DeviceButton extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "DeviceBtn";

	private Context mContext;
    private RelativeLayout rctnDeviceButton;
    private ImageView ivDeviceIcon;
    private TextView tvDeviceType, tvDeviceName;
    private boolean isSelected;

    private String mDeviceName;
    private int mDeviceType;
    private long mDeviceId;

	public DeviceButton(Context context) {
		super(context);
        mContext = context;
		_initView();
		_setView();
	}

	public DeviceButton(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext = context;
		_initView();
		_setView();
	}

	public DeviceButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mContext = context;
		_initView();
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_device_button, this, false);
		addView(v);

		rctnDeviceButton = (RelativeLayout)findViewById(R.id.rctn_widget_device_button);
		ivDeviceIcon = (ImageView)findViewById(R.id.iv_widget_device_button_icon);
		tvDeviceType = (TextView)findViewById(R.id.tv_widget_device_button_title);
        tvDeviceName = (TextView)findViewById(R.id.tv_widget_device_button_contents);

        rctnDeviceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelected = !isSelected;
                rctnDeviceButton.setSelected(isSelected);
            }
        });
	}

	private void _setView() {
		setSelected(false);
	}

	public void setSelected(boolean selected) {
	    isSelected = selected;
	    rctnDeviceButton.setSelected(selected);
    }

    public void setDeviceId(long deviceId) {
	    mDeviceId = deviceId;
    }

	public void setDeviceName(String name) {
		mDeviceName = name;
		if (tvDeviceName != null) {
		    tvDeviceName.setText(mDeviceName);
        }
	}

    public void setDeviceType(int type) {
        mDeviceType = type;
        if (tvDeviceType != null) {
            switch (mDeviceType) {
                case DeviceType.DIAPER_SENSOR:
                    ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor);
                    tvDeviceType.setText(mContext.getString(R.string.device_type_diaper_sensor));
                    break;
                case DeviceType.AIR_QUALITY_MONITORING_HUB:
                    ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_aqmhub);
                    tvDeviceType.setText(mContext.getString(R.string.device_type_hub));
                    break;
                case DeviceType.LAMP:
                    ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_lamp);
                    tvDeviceType.setText(mContext.getString(R.string.device_type_lamp));
                    break;
            }
        }
    }

    public boolean isSelected() {
	    return isSelected;
    }

    public long getDeviceId() {
	    return mDeviceId;
    }

    public int getDeviceType() {
        return mDeviceType;
    }
}