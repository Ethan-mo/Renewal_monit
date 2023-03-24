package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import goodmonit.monit.com.kao.constants.Configuration;

public class SettingButtonSwitch extends SettingButton {
	private static final String TAG = Configuration.BASE_TAG + "SettingButton";

	public SettingButtonSwitch(Context context) {
		super(context);
		_initView();
	}

	public SettingButtonSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public SettingButtonSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		tvContent.setVisibility(View.GONE);
		ivRightDirection.setVisibility(View.GONE);
		switchButton.setVisibility(View.VISIBLE);
		switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isEnabled && mOnClickListener != null) {
					mOnClickListener.onClick(buttonView);
				}
			}
		});

		rctnWholeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isEnabled && mOnClickListener != null) {
					boolean isChecked = switchButton.isChecked();
					switchButton.setChecked(!isChecked);
				}
			}
		});
	}
	public void setChecked(boolean checked) {
		switchButton.setChecked(checked);
	}
	public boolean isChecked() {
		return switchButton.isChecked();
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
		if (mOnClickListener == null) {
			switchButton.setClickable(false);
		} else {
			switchButton.setClickable(true);
		}
	}
}