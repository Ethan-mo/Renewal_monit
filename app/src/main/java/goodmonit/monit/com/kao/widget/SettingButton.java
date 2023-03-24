package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class SettingButton extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "SettingButton";

	protected RelativeLayout rctnWholeButton;
	protected OnClickListener mOnClickListener;
	protected TextView tvTitle, tvContent, tvTitleDepth2, tvWarning, tvDescription, tvDescriptionDepth2;
	protected ImageView ivRightDirection;
	protected ImageView ivNewMark;
	protected Switch switchButton;
	protected View vDividerSameCategory, vDividerOtherCategory;
	protected RelativeLayout rctnWheelView;
	protected WheelView wvSelection;
	protected TextView tvWheelViewExtra;
	protected boolean isEnabled;
	protected boolean hasDescription;
	protected int mDepth;

	public SettingButton(Context context) {
		super(context);
		initView();
	}

	public SettingButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public SettingButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public void initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_setting_button, this, false);
		addView(v);

		rctnWholeButton = (RelativeLayout) v.findViewById(R.id.rctn_widget_setting_button);
		rctnWholeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnClickListener != null) {
					if (isEnabled) {
						mOnClickListener.onClick(v);
					}
				}
			}
		});
		tvTitle = (TextView) v.findViewById(R.id.tv_widget_setting_button_title);
		tvTitleDepth2 = (TextView) v.findViewById(R.id.tv_widget_setting_button_title_depth2);
		tvDescription = (TextView) v.findViewById(R.id.tv_widget_setting_button_description);
		tvDescriptionDepth2 = (TextView) v.findViewById(R.id.tv_widget_setting_button_description_depth2);
		tvContent = (TextView) v.findViewById(R.id.tv_widget_setting_button_content);
		tvWarning = (TextView) v.findViewById(R.id.tv_widget_setting_button_warning);
		ivRightDirection = (ImageView) v.findViewById(R.id.iv_widget_setting_button_right_direction);
		switchButton = (Switch) v.findViewById(R.id.switch_widget_setting);
		vDividerSameCategory = v.findViewById(R.id.v_widget_setting_button_same_category_divider);
		vDividerOtherCategory = v.findViewById(R.id.v_widget_setting_button_other_category_divider);
		ivNewMark = (ImageView)v.findViewById(R.id.iv_widget_setting_button_new_mark);

		tvTitleDepth2.setVisibility(View.GONE);
		vDividerSameCategory.setVisibility(View.GONE);
		switchButton.setVisibility(View.GONE);
		tvWarning.setVisibility(View.GONE);
		isEnabled = true;
		hasDescription = false;
		mDepth = 1;

		rctnWheelView = (RelativeLayout)v.findViewById(R.id.rctn_widget_setting_wheelview);
		wvSelection = (WheelView)v.findViewById(R.id.wv_widget_setting_button_item);
		tvWheelViewExtra = (TextView)v.findViewById(R.id.tv_widget_setting_button_wheelview_item_extra);
		rctnWheelView.setVisibility(View.GONE);
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
		if (isEnabled) {
			tvTitle.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimary));
			tvTitleDepth2.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimary));
			tvDescription.setTextColor(getContext().getResources().getColor(R.color.colorTextGrey));
			tvDescriptionDepth2.setTextColor(getContext().getResources().getColor(R.color.colorTextGrey));
			tvContent.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
			ivRightDirection.setBackgroundResource(R.drawable.ic_direction_right_black_light);
			switchButton.setEnabled(true);
		} else {
			tvTitle.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			tvTitleDepth2.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			tvDescription.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			tvDescriptionDepth2.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			tvContent.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			ivRightDirection.setBackgroundResource(R.drawable.ic_direction_right_deactivated);
			switchButton.setEnabled(false);
		}
	}

	public void setDepth(int depth) {
		mDepth = depth;
		switch (depth) {
			case 1:
				tvTitle.setVisibility(View.VISIBLE);
				tvTitleDepth2.setVisibility(View.GONE);
				if (hasDescription) {
					tvDescription.setVisibility(View.VISIBLE);
					tvDescriptionDepth2.setVisibility(View.GONE);
				}
				break;
			case 2:
				tvTitle.setVisibility(View.GONE);
				tvTitleDepth2.setVisibility(View.VISIBLE);
				if (hasDescription) {
					tvDescriptionDepth2.setVisibility(View.VISIBLE);
					tvDescription.setVisibility(View.GONE);
				}
				break;
		}
	}

	public void setClickable(boolean clickable) {
		if (clickable) {
			rctnWholeButton.setBackgroundResource(R.drawable.bg_btn_white_darklight_selector);
		} else {
			rctnWholeButton.setBackgroundResource(R.color.colorWhite);
		}
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
		tvTitleDepth2.setText(title);
		setDepth(mDepth);
	}

	public void setDescription(String description) {
		hasDescription = true;
		tvDescription.setText(description);
		tvDescriptionDepth2.setText(description);
		setDepth(mDepth);
	}

	public void setContent(String content) {
		tvContent.setText(content);
	}

	public void showDirection(boolean show) {
		if (show) {
			ivRightDirection.setVisibility(View.VISIBLE);
		} else {
			ivRightDirection.setVisibility(View.GONE);
		}
	}

	public void setDividerForOtherCategory(boolean set) {
		if (set) {
			vDividerOtherCategory.setVisibility(View.VISIBLE);
			vDividerSameCategory.setVisibility(View.GONE);
		} else {
			vDividerOtherCategory.setVisibility(View.GONE);
			vDividerSameCategory.setVisibility(View.VISIBLE);
		}
	}

	public void showNewMark(boolean show) {
		if (show) {
			ivNewMark.setVisibility(View.VISIBLE);
		} else {
			ivNewMark.setVisibility(View.GONE);
		}
	}

	public void setBackgroundResource(int resId) {
		rctnWholeButton.setBackgroundResource(resId);
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

}