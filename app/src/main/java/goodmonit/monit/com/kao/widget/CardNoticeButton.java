package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class CardNoticeButton extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "CardNotice";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;
	private LinearLayout lctnButton;
	private ImageView ivIcon;
	private TextView tvDescription;

	private int mIconImageResId;
	private String mDescription;
	private OnClickListener mListener;

	public CardNoticeButton(Context context) {
		super(context);
		_initView();
		mContext = context;
	}

	public CardNoticeButton(Context context,
                            int iconImageResId,
                            String description) {
		super(context);
		mIconImageResId = iconImageResId;
		mDescription = description;
		_initView();
		mContext = context;
	}

	public CardNoticeButton(Context context,
                            int iconImageResId,
                            String description,
                            OnClickListener listener) {
		super(context);
		mIconImageResId = iconImageResId;
		mDescription = description;
		mListener = listener;
		_initView();
		mContext = context;
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_card_notice_button, this, false);
		addView(v);

		lctnButton = (LinearLayout)v.findViewById(R.id.lctn_widget_card_notice_button);
		ivIcon = (ImageView)v.findViewById(R.id.iv_widget_card_notice_button_icon);
		tvDescription = (TextView)v.findViewById(R.id.tv_widget_card_notice_button_description);

//		if (mIconImageResId == 0) {
//			ivIcon.setVisibility(View.GONE);
//		} else {
//			ivIcon.setImageResource(mIconImageResId);
//		}

		tvDescription.setText(mDescription);

		if (mListener != null) {
			lctnButton.setOnClickListener(mListener);
		}
	}

	public void setOnClickListener(OnClickListener listener) {
		mListener = listener;
		if (lctnButton != null) {
			lctnButton.setOnClickListener(listener);
		}
	}

	public void setDescription(String description) {
		if (tvDescription != null) {
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setText(description);
		}
	}

	public void setIcon(int imgResId) {
		mIconImageResId = imgResId;
		if (ivIcon != null) {
			ivIcon.setVisibility(View.VISIBLE);
			ivIcon.setImageResource(imgResId);
		}
	}
}