package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import goodmonit.monit.com.kao.constants.Configuration;

public class SettingButtonWarning extends SettingButton {
	private static final String TAG = Configuration.BASE_TAG + "SettingButton";

	public SettingButtonWarning(Context context) {
		super(context);
		_initView();
	}

	public SettingButtonWarning(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public SettingButtonWarning(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		tvWarning.setVisibility(View.VISIBLE);
		tvTitle.setVisibility(View.GONE);
		tvContent.setVisibility(View.GONE);
		ivRightDirection.setVisibility(View.GONE);

		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
			}
		});
	}

	public void setWarning(String warning) {
		tvWarning.setText(warning);
	}
}