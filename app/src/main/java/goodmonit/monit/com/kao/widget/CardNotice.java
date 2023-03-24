package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class CardNotice extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "CardNotice";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;
	private ImageView ivIcon;
	private TextView tvDescription;
	private Button btnRight, btnLeft;

	private int mIconImageResId;
	private String mDescription, mLeftButtonName, mRightButtonName;
	private View.OnClickListener mLeftListener, mRightListener;

	public CardNotice(Context context) {
		super(context);
		_initView();
		mContext = context;
	}

	public CardNotice(Context context,
					  int iconImageResId,
					  String description) {
		super(context);
		mIconImageResId = iconImageResId;
		mDescription = description;
		_initView();
		mContext = context;
	}

	public CardNotice(Context context,
					  int iconImageResId,
					  String description,
					  String btnName,
					  OnClickListener listener) {
		super(context);
		mIconImageResId = iconImageResId;
		mDescription = description;
		mRightButtonName = btnName;
		mRightListener = listener;
		_initView();
		mContext = context;
	}

	public CardNotice(Context context,
					  int iconImageResId,
					  String description,
					  String leftButtonName,
					  OnClickListener leftButtonListener,
					  String rightButtonName,
					  OnClickListener rightButtonListener) {
		super(context);

		mIconImageResId = iconImageResId;
		mDescription = description;
		mLeftButtonName = leftButtonName;
		mLeftListener = leftButtonListener;
		mRightButtonName = rightButtonName;
		mRightListener = rightButtonListener;
		_initView();
		mContext = context;
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_card_notice, this, false);
		addView(v);

		ivIcon = (ImageView)v.findViewById(R.id.iv_widget_card_notice_icon);
		tvDescription = (TextView)v.findViewById(R.id.tv_widget_card_notice_description);
		btnLeft = (Button)v.findViewById(R.id.btn_widget_card_notice_left);
		btnRight = (Button)v.findViewById(R.id.btn_widget_card_notice_right);

		if (mIconImageResId == 0) {
			ivIcon.setVisibility(View.GONE);
		} else {
			ivIcon.setImageResource(mIconImageResId);
		}

		tvDescription.setText(mDescription);

		if (mLeftButtonName == null) {
			btnLeft.setVisibility(GONE);
		} else {
			btnLeft.setText(mLeftButtonName);
			btnLeft.setOnClickListener(mLeftListener);
		}

		if (mRightButtonName == null) {
			btnRight.setVisibility(GONE);
		} else {
			btnRight.setText(mRightButtonName);
			btnRight.setOnClickListener(mRightListener);
		}
	}

	public void setLeftButton(String name, OnClickListener listener) {
		if (btnLeft != null) {
			btnLeft.setVisibility(View.VISIBLE);
			btnLeft.setText(name);
			btnLeft.setOnClickListener(listener);
		}
	}

	public void setRightButton(String name, OnClickListener listener) {
		if (btnRight != null) {
			btnRight.setVisibility(View.VISIBLE);
			btnRight.setText(name);
			btnRight.setOnClickListener(listener);
		}
	}

	public void setDescription(String description) {
		if (tvDescription != null) {
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setText(description);
		}
	}

	public void setIcon(int imgResId) {
		if (ivIcon != null) {
			ivIcon.setVisibility(View.VISIBLE);
			ivIcon.setImageResource(imgResId);
		}
	}
}