package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;

public class SettingTextDivider extends LinearLayout {
	private TextView tvTitle;
	private View vMarginTop;
	private View vMarginBottom;
	private View vBottomDivider;

	public SettingTextDivider(Context context) {
		super(context);
		_initView();
	}

	public SettingTextDivider(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public SettingTextDivider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_setting_divider_text, this, false);
		addView(v);

		tvTitle = (TextView) v.findViewById(R.id.tv_widget_setting_text_divider_title);
		vMarginTop = v.findViewById(R.id.v_widget_setting_text_divider_margin_top);
		vMarginBottom = v.findViewById(R.id.v_widget_setting_text_divider_margin_bottom);
		vBottomDivider = v.findViewById(R.id.v_widget_setting_text_divider_bottom);
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	public void setPrimaryColor(boolean primary) {
		if (primary) {
			tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary));
		} else {
			tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimaryLight));
		}
	}

	public void showBottomDivider(boolean show) {
		if (show) {
			vBottomDivider.setVisibility(View.VISIBLE);
		} else {
			vBottomDivider.setVisibility(View.GONE);
		}
	}

	public void setTopGravity(boolean gravity) {
		if (gravity) {
			vMarginTop.setVisibility(View.GONE);
			vMarginBottom.setVisibility(View.VISIBLE);
		} else {
			vMarginTop.setVisibility(View.VISIBLE);
			vMarginBottom.setVisibility(View.GONE);
		}
	}

}