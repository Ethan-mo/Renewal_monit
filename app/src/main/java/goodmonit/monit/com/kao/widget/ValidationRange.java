package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationRange extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidRange";
	private static final boolean DBG = Configuration.DBG;

	private TextView tvTextContents;
	private RangeSeekBar rsbRange;
	private String mScale = "";
	private RangeSeekBar.OnRangeSeekBarListener mListener;

	public ValidationRange(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public ValidationRange(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationRange(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_range, this, false);
		addView(v);

		tvTextContents = (TextView)v.findViewById(R.id.tv_validation_range_text);
		tvTextContents.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));

		tvTitle = (TextView) v.findViewById(R.id.tv_validation_range_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_range_warning);
		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_range_checked);
		vUnderline = v.findViewById(R.id.v_validation_range_underline);
		tvWarning.setVisibility(View.GONE);

		rsbRange = (RangeSeekBar)v.findViewById(R.id.rsb_validation_range);
		rsbRange.setOnValueChangedListener(new RangeSeekBar.OnRangeSeekBarListener() {
			@Override
			public void onValueChanged(int minVal, int maxVal, int leftPos, int rightPos) {
				//if (DBG) Log.d(TAG, "changed: " + minVal + " / " + maxVal + " / " + leftPos + " / " + rightPos);
				if (mListener != null) {
					mListener.onValueChanged(minVal, maxVal, leftPos, rightPos);
				}
			}
		});
	}

	public void setMinValue(int min) {
		rsbRange.setMinValue(min);
	}

	public void setMaxValue(int max) {
		rsbRange.setMaxValue(max);
	}

	public void updateView() {
		rsbRange.updateView();
	}

	public void setLeftThumbPos(int pos) {
		rsbRange.setLeftThumbPos(pos);
	}

	public void setLeftThumbTextView(String text) {
		rsbRange.setLeftThumbTextView(text);
	}

	public void setRightThumbPos(int pos) {
		rsbRange.setRightThumbPos(pos);
	}

	public void setRightThumbTextView(String text) {
		rsbRange.setRightThumbTextView(text);
	}

	public void setScale(String scale) {
		mScale = scale;
		rsbRange.setLeftThumbScaleTextView(scale);
		rsbRange.setRightThumbScaleTextView(scale);
	}

	private void _setView() {
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
		tvTextContents.setHint(
				typedArray.getString(R.styleable.ValidationWidget_textHint));
		tvTextContents.setText(
				typedArray.getString(R.styleable.ValidationWidget_textContents));

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);
	}

	public void setText(String text) {
		tvTextContents.setText(text);
		tvTextContents.setTextColor(getContext().getResources().getColor(R.color.colorTextPositive));
		if (mValidationUpdateListener != null) {
			mValidationUpdateListener.updateValidation();
		} else {
			if (text == null) {
				setValid(false);
			} else {
				setValid(true);
			}
		}
	}

	public void initialize() {
		tvTextContents.setText("");
		tvTextContents.setTextColor(getContext().getResources().getColor(R.color.colorTextPositive));
		if (ivValidationChecked != null) {
			ivValidationChecked.setBackgroundResource(R.drawable.ic_validation_check_default);
		}
		isValid = false;
	}

	public String getText() {
		return tvTextContents.getText().toString();
	}

	public TextView getTextView() {
		return tvTextContents;
	}

	public void setOnValueChangedListener(RangeSeekBar.OnRangeSeekBarListener listener) {
		mListener = listener;
	}

	public void setOnClickListener(OnClickListener listener) {
		tvTextContents.setOnClickListener(listener);
	}
}