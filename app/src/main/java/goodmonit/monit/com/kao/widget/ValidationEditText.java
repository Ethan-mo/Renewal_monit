package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Pattern;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.ValidationManager;

public class ValidationEditText extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidEditText";

	private EditText etText;
	private ImageButton ibtnShowPassword, ibtnClear;
	private boolean isPassword = false;

	public ValidationEditText(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public ValidationEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_edittext, this, false);
		addView(v);
		etText = (EditText) v.findViewById(R.id.et_validation_edittext_text);
		ibtnShowPassword = (ImageButton) v.findViewById(R.id.ibtn_validation_edittext_show);
		ibtnClear = (ImageButton) v.findViewById(R.id.ibtn_validation_edittext_clear);
		tvTitle = (TextView) v.findViewById(R.id.tv_validation_edittext_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_edittext_warning);
		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_edittext_checked);
		vUnderline = v.findViewById(R.id.v_validation_edittext_underline);

		ibtnShowPassword.setVisibility(View.GONE);
		ibtnClear.setVisibility(View.GONE);
		tvWarning.setVisibility(View.GONE);

		ibtnShowPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.isSelected()) {
					v.setSelected(false);
					showPassword(false);
				} else {
					v.setSelected(true);
					showPassword(true);
				}
			}
		});

		ibtnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				etText.setText("");
				tvWarning.setVisibility(View.GONE);
			}
		});
		//etText.setFilters(new InputFilter[] {filter});

		etText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

			@Override
			public void afterTextChanged(Editable s) {
				String text = s.toString();
				if (text.length() > 0) {
					showExtraButton(true);
				} else {
					showExtraButton(false);
				}
				if (mValidationUpdateListener != null) {
					mValidationUpdateListener.updateValidation();
				}
			}
		});
	}

	private void _setView() {
		if (isPassword()) {
			showPassword(false);
		}
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
		etText.setHint(
				typedArray.getString(R.styleable.ValidationWidget_textHint));

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);

		String inputType = typedArray.getString(R.styleable.ValidationWidget_inputType);
		if (inputType != null) {
			inputType = inputType.toLowerCase();
		}
		if ("password".equals(inputType)) {
			setPasswordMode(true);
		} else {
			setPasswordMode(false);
		}
	}

	public void setText(String text) {
		etText.setText(text);
		if (etText.getText().toString().length() > 0) {
			ibtnClear.setVisibility(View.VISIBLE);
			if (isPassword()) {
				ibtnShowPassword.setVisibility(View.VISIBLE);
			}
		} else {
			ibtnClear.setVisibility(View.GONE);
			if (isPassword()) {
				ibtnShowPassword.setVisibility(View.GONE);
			}
		}
	}

	public EditText getEditTextView() {
		return etText;
	}

	public void setHint(String text) {
		etText.setHint(text);
	}

	public String getText() {
		return etText.getText().toString();
	}

	public void setPasswordMode(boolean password) {
		isPassword = password;
	}

	public boolean isPassword() {
		return isPassword;
	}

	public void addTextChangedListener(TextWatcher watcher) {
		etText.addTextChangedListener(watcher);
	}

	public void showExtraButton(boolean show) {
		if (show) {
			if (isPassword) {
				ibtnShowPassword.setVisibility(View.VISIBLE);
			} else {
				ibtnClear.setVisibility(View.VISIBLE);
			}
		} else {
			if (isPassword) {
				ibtnShowPassword.setVisibility(View.GONE);
			} else {
				ibtnClear.setVisibility(View.GONE);
			}
			showWarning(false);
		}
	}

	/**
	 * Filter for English, number, special characters
	 */
	private InputFilter filter= new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			Pattern ps = Pattern.compile("^[a-zA-Z0-9" + ValidationManager.VALID_SPECIAL_CHARACTERS + "]");
			if (!ps.matcher(source).matches()) {
				return "";
			}
			return null;
		}
	};

	public void showPassword(boolean show) {
		if (show) {
			etText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			etText.setTransformationMethod(null);
		} else {
			etText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
			etText.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		etText.setSelection(etText.length());
	}
}