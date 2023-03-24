package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import goodmonit.monit.com.kao.constants.Configuration;

public class SettingButtonWheelView extends SettingButton {
	private static final String TAG = Configuration.BASE_TAG + "SettingButton";

	private WheelView.OnWheelViewListener mOnSelectedListener;
	private WheelView.OnWheelViewListener mOnExpandListener;
	public SettingButtonWheelView(Context context) {
		super(context);
		_initView();
	}

	public SettingButtonWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public SettingButtonWheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rctnWheelView.getVisibility() == View.VISIBLE) {
					expandWheelView(false);
				} else {
					expandWheelView(true);
				}
			}
		};
		ivRightDirection.setVisibility(View.GONE);
		switchButton.setVisibility(View.GONE);
		rctnWheelView.setVisibility(View.GONE);
		wvSelection.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int idx, String item) {
				if (mOnSelectedListener != null) {
					mOnSelectedListener.onValueChanged(idx, item);
				}
			}
		});
		expandWheelView(false);
	}

	public void expandWheelView(boolean expand) {
		if (expand) {
			rctnWheelView.setVisibility(View.VISIBLE);
			if (mOnExpandListener != null) {
				mOnExpandListener.onExpanded();
			}
		} else {
			rctnWheelView.setVisibility(View.GONE);
			if (mOnExpandListener != null) {
				mOnExpandListener.onCollapsed();
			}
		}
	}

	public void setOnExpandListener(WheelView.OnWheelViewListener listener) {
		mOnExpandListener = listener;
	}

	public void selectItem(String item) {
		wvSelection.setSelection(item);
	}

	public void selectItem(int idx) {
		wvSelection.setSelection(idx);
	}

	public void setItems(List<String> values) {
		wvSelection.setItems(values);
	}

	public void setOnSelectedListener(WheelView.OnWheelViewListener listener) {
		mOnSelectedListener = listener;
	}

	public void setExtraText(String extra) {
		tvWheelViewExtra.setText(extra);
	}
}