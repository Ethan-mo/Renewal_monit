package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationRadio extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidationRadio";

	private Button btnItem1, btnItem2;
	private int mSelectedRadioIndex;

	public ValidationRadio(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public ValidationRadio(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationRadio(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_radio, this, false);
		addView(v);

		tvTitle = (TextView) v.findViewById(R.id.tv_validation_radio_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_radio_warning);
		btnItem1 = (Button) v.findViewById(R.id.btn_validation_radio_item1);
		btnItem1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectItem(1);
				if (mClickListener != null) {
					mClickListener.onClick(v);
				}
			}
		});

		btnItem2 = (Button) v.findViewById(R.id.btn_validation_radio_item2);
		btnItem2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectItem(2);
				if (mClickListener != null) {
					mClickListener.onClick(v);
				}
			}
		});

		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_radio_checked);
		vUnderline = v.findViewById(R.id.v_validation_radio_underline);
		tvWarning.setVisibility(View.GONE);
	}

	private void _setView() {
		selectItem(mSelectedRadioIndex);
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

		btnItem1.setText(
				typedArray.getString(R.styleable.ValidationWidget_textItem1));
		btnItem2.setText(
				typedArray.getString(R.styleable.ValidationWidget_textItem2));

		if ("item1".equals(typedArray.getString(R.styleable.ValidationWidget_defaultSelected))) {
			selectItem(1);
		} else if ("item2".equals(typedArray.getString(R.styleable.ValidationWidget_defaultSelected))) {
			selectItem(2);
		} else {
			btnItem1.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
			btnItem2.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
		}

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);
	}

	public void selectItem(int idx) {
		mSelectedRadioIndex = idx;
		switch(idx) {
			case 1:
				btnItem1.setSelected(true);
				btnItem1.setTextColor(getContext().getResources().getColor(R.color.colorTextSelected));
				btnItem2.setSelected(false);
				btnItem2.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
				setValid(true);
				break;
			case 2:
				btnItem1.setSelected(false);
				btnItem1.setTextColor(getContext().getResources().getColor(R.color.colorTextNotSelected));
				btnItem2.setSelected(true);
				btnItem2.setTextColor(getContext().getResources().getColor(R.color.colorTextSelected));
				setValid(true);
				break;
		}
		if (mValidationUpdateListener != null) {
			mValidationUpdateListener.updateValidation();
		}
	}

	public void setWarning(String warning) {
		tvWarning.setText(warning);
	}

	public int getSelectedRadioIndex() {
		return mSelectedRadioIndex;
	}

	public void setTitleItem1(String title) {
		btnItem1.setText(title);
	}

	public void setTitleItem2(String title) {
		btnItem2.setText(title);
	}
}