package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationTextView extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidTextView";

	private LinearLayout lctnAgreement;
	private TextView tvTextContents;
	private Button btnAgreement;
	private boolean showAgreement = false;

	public ValidationTextView(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public ValidationTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_textview, this, false);
		addView(v);

		tvTextContents = (TextView)v.findViewById(R.id.tv_validation_textview_text);
		tvTitle = (TextView) v.findViewById(R.id.tv_validation_textview_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_textview_warning);
		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_textview_checked);
		vUnderline = v.findViewById(R.id.v_validation_textview_underline);
		tvWarning.setVisibility(View.GONE);

		lctnAgreement = (LinearLayout)v.findViewById(R.id.lctn_validation_textview_agreement);
		btnAgreement = (Button)v.findViewById(R.id.btn_validation_textview_agreement);
		btnAgreement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnAgreement.isSelected()) {
					_setAgreementSelected(false);
					setValid(false);
				} else {
					_setAgreementSelected(true);
					setValid(true);
				}
				if (mValidationUpdateListener != null) {
					mValidationUpdateListener.updateValidation();
				}
			}
		});

		_setAgreementSelected(false);
		setValid(false);
	}

	private void _setAgreementSelected(boolean selected) {
		if (selected) {
			btnAgreement.setSelected(true);
			btnAgreement.setTextColor(getContext().getResources().getColor(R.color.colorTextSelected));
		} else {
			btnAgreement.setSelected(false);
			btnAgreement.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimary));
		}
	}

	private void _setView() {
		showUnderLine(showUnderline);
		if (showAgreement) {
			lctnAgreement.setVisibility(View.VISIBLE);
		} else {
			lctnAgreement.setVisibility(View.GONE);
		}
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
		tvTextContents.setText(
				typedArray.getString(R.styleable.ValidationWidget_textContents));

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);
		showAgreement = typedArray.getBoolean(R.styleable.ValidationWidget_showAgreement, false);
	}

	public void setText(String text) {
		tvTextContents.setText(text);
		if (mValidationUpdateListener != null) {
			mValidationUpdateListener.updateValidation();
		} else {
			if (text == null || text.length() == 0) {
				setValid(false);
			} else {
				setValid(true);
			}
		}
	}

	public String getText() {
		return tvTextContents.getText().toString();
	}

	public TextView getTextView() {
		return tvTextContents;
	}

	public boolean isShowingAgreement() {
		return showAgreement;
	}

	public boolean isAgreed() {
		return btnAgreement.isSelected();
	}

	public void setOnClickListener(OnClickListener listener) {
		tvTextContents.setOnClickListener(listener);
	}
}