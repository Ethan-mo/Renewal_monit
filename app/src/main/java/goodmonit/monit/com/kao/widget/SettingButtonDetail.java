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

public class SettingButtonDetail extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "SettingButtonDetail";

	protected RelativeLayout rctnWholeButton;
	protected LinearLayout lctnMoreInfo, lctnGroupIcon;
	protected OnClickListener mOnClickListener;
	protected TextView tvTitle, tvContent, tvDetail;
	protected ImageView ivIcon, ivRightDirection;
	protected View vDividerSameCategory, vDividerOtherCategory;
	protected boolean isEnabled;

	public SettingButtonDetail(Context context) {
		super(context);
		_initView();
	}

	public SettingButtonDetail(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public SettingButtonDetail(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_setting_button_detail, this, false);
		addView(v);

		rctnWholeButton = (RelativeLayout) v.findViewById(R.id.rctn_widget_setting_detail_button);
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
		tvTitle = (TextView) v.findViewById(R.id.tv_widget_setting_detail_button_title);
		tvDetail = (TextView) v.findViewById(R.id.tv_widget_setting_detail_button_detail);
		tvContent = (TextView) v.findViewById(R.id.tv_widget_setting_detail_button_content);
		ivRightDirection = (ImageView) v.findViewById(R.id.iv_widget_setting_detail_button_right_direction);
		vDividerSameCategory = v.findViewById(R.id.v_widget_setting_detail_button_same_category_divider);
		vDividerOtherCategory = v.findViewById(R.id.v_widget_setting_detail_button_other_category_divider);
		vDividerSameCategory.setVisibility(View.GONE);
		ivIcon = (ImageView)v.findViewById(R.id.iv_widget_setting_detail_button_icon);
		ivIcon.setVisibility(View.GONE);

		lctnMoreInfo = (LinearLayout)v.findViewById(R.id.lctn_widget_setting_detail_button_more_info);
		lctnGroupIcon = (LinearLayout)v.findViewById(R.id.lctn_widget_setting_detail_button_group_icon);
		isEnabled = true;
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
		if (isEnabled) {
			tvTitle.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimary));
			tvDetail.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
			tvContent.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));
			ivRightDirection.setBackgroundResource(R.drawable.ic_direction_right_black_light);
		} else {
			tvTitle.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			tvDetail.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			tvContent.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			ivRightDirection.setBackgroundResource(R.drawable.ic_direction_right_deactivated);
		}
	}

	public void setTitle(String title) {
		if (title == null) {
			tvTitle.setVisibility(View.GONE);
		} else {
			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(title);
		}
	}

	public void setDetail(String detail) {
		if (detail == null) {
			tvDetail.setVisibility(View.GONE);
		} else {
			tvDetail.setVisibility(View.VISIBLE);
			tvDetail.setText(detail);
		}
	}

	public void setContent(String content) {
		tvContent.setText(content);
	}

	public void setIcon(int iconRes) {
		if (iconRes == 0) {
			ivIcon.setVisibility(View.GONE);
			return;
		}
		ivIcon.setImageResource(iconRes);
		ivIcon.setVisibility(View.VISIBLE);
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

	public void showGroupIcon(boolean show) {
		if (show) {
			lctnGroupIcon.setVisibility(View.VISIBLE);
			lctnMoreInfo.setVisibility(View.GONE);
		} else {
			lctnGroupIcon.setVisibility(View.GONE);
			lctnMoreInfo.setVisibility(View.VISIBLE);
		}
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

}