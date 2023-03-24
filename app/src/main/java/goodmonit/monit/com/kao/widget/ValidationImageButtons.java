package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationImageButtons extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidationBtns";

	private ImageButton btnItem1, btnItem2, btnItem3;
	private int mSelectedImageButtonIdx = 0;

	public ValidationImageButtons(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public ValidationImageButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationImageButtons(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_image_buttons, this, false);
		addView(v);

		tvTitle = (TextView) v.findViewById(R.id.tv_validation_image_buttons_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_image_buttons_warning);
		btnItem1 = (ImageButton) v.findViewById(R.id.btn_validation_image_buttons_item1);
		btnItem1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnItem1.isSelected()) {
					mSelectedImageButtonIdx -= 1;
					btnItem1.setSelected(false);
				} else {
					mSelectedImageButtonIdx += 1;
					btnItem1.setSelected(true);
				}

				if (mClickListener != null) {
					mClickListener.onClick(v);
				}
				if (mValidationUpdateListener != null) {
					mValidationUpdateListener.updateValidation();
				}
			}
		});

		btnItem2 = (ImageButton) v.findViewById(R.id.btn_validation_image_buttons_item2);
		btnItem2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnItem2.isSelected()) {
					mSelectedImageButtonIdx -= 2;
					btnItem2.setSelected(false);
				} else {
					mSelectedImageButtonIdx += 2;
					btnItem2.setSelected(true);
				}

				if (mClickListener != null) {
					mClickListener.onClick(v);
				}
				if (mValidationUpdateListener != null) {
					mValidationUpdateListener.updateValidation();
				}
			}
		});

		btnItem3 = (ImageButton) v.findViewById(R.id.btn_validation_image_buttons_item3);
		btnItem3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnItem3.isSelected()) {
					mSelectedImageButtonIdx -= 4;
					btnItem3.setSelected(false);
				} else {
					mSelectedImageButtonIdx += 4;
					btnItem3.setSelected(true);
				}

				if (mClickListener != null) {
					mClickListener.onClick(v);
				}
				if (mValidationUpdateListener != null) {
					mValidationUpdateListener.updateValidation();
				}
			}
		});

		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_image_buttons_checked);
		vUnderline = v.findViewById(R.id.v_validation_image_buttons_underline);
		tvWarning.setVisibility(View.GONE);
	}

	private void _setView() {
		selectItem(mSelectedImageButtonIdx);
		showUnderLine(showUnderline);
	}

	private void _getAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValidationWidget);
		_setTypeArray(typedArray);
	}

	private void _getAttrs(AttributeSet attrs, int defStyle) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValidationWidget, defStyle, 0);
		_setTypeArray(typedArray);
	}

	private void _setTypeArray(TypedArray typedArray) {
		tvTitle.setText(
				typedArray.getString(R.styleable.ValidationWidget_textTitle));
		tvWarning.setText(
				typedArray.getString(R.styleable.ValidationWidget_textWarning));

		int titleWidth = typedArray.getDimensionPixelSize(R.styleable.ValidationWidget_textTitleWidth, -1);
		if (titleWidth != -1) {
			ViewGroup.LayoutParams params = tvTitle.getLayoutParams();
			params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			tvTitle.setLayoutParams(params);
		}

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);
	}

	public void selectItem(int idx) {
		mSelectedImageButtonIdx = idx;
		if ((mSelectedImageButtonIdx & 1) == 1) {
			btnItem1.setSelected(true);
		}
		if ((mSelectedImageButtonIdx & 2) == 2) {
			btnItem2.setSelected(true);
		}
		if ((mSelectedImageButtonIdx & 4) == 4) {
			btnItem3.setSelected(true);
		}

		if (mValidationUpdateListener != null) {
			mValidationUpdateListener.updateValidation();
		}
	}

	public void setWarning(String warning) {
		tvWarning.setText(warning);
	}

	public int getSelectedImageButtonIdx() {
		return mSelectedImageButtonIdx;
	}

	public void setImageItem1(int imgRes) {
		btnItem1.setImageResource(imgRes);
	}

	public void setImageItem2(int imgRes) {
		btnItem2.setImageResource(imgRes);
	}

	public void setImageItem3(int imgRes) {
		btnItem3.setImageResource(imgRes);
	}

}