package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.regex.Pattern;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class SigninEditText extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "SigninEditText";

	private LinearLayout ctnSigninEditBackground;
	private EditText etText;
	private ImageButton ibtnShowPassword;
	private ImageButton ibtnClear;
	private ImageView ivIcon;

	private boolean isPassword;

	public SigninEditText(Context context) {
		super(context);
		_initView();
		_setView();
	}

    public SigninEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
		_initView();
        _getAttrs(attrs);
		_setView();
    }

	public SigninEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	public EditText getEditTextView() {
		return etText;
	}

	public void setText(String text) {
		etText.setText(text);
		if (etText.getText().toString().length() > 0) {
			if (isPassword) {
				ibtnShowPassword.setVisibility(View.VISIBLE);
			} else {
				ibtnClear.setVisibility(View.VISIBLE);
			}
		} else {
			ibtnClear.setVisibility(View.GONE);
			if (isPassword) {
				ibtnShowPassword.setVisibility(View.GONE);
			}
		}
	}

	public String getText() {
		return etText.getText().toString();
	}

	private void _setView() {
		ibtnShowPassword.setVisibility(View.GONE);
		ibtnClear.setVisibility(View.GONE);
		if (isPassword) {
			_showPassword(false);
		}
		//etText.setFilters(new InputFilter[] {filter});
	}

	/**
	 * Filter for English, number, special characters
	 */
	private InputFilter filter = new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			Pattern ps = Pattern.compile("^[a-zA-Z0-9!”#$%&’()*+,-./:;<=>?@[\\]^_`{|}~]]");
			if (!ps.matcher(source).matches()) {
				return "";
			}
			return null;
		}
	};

	private void _showPassword(boolean show) {
		if (show) {
			etText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			etText.setTransformationMethod(null);
		} else {
			etText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
			etText.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		etText.setSelection(etText.length());
	}

    private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_signin_edittext, this, false);
		addView(v);
		ctnSigninEditBackground = (LinearLayout) v.findViewById(R.id.ctn_signin_edittext);
		etText = (EditText) v.findViewById(R.id.et_signin_edittext_text);
		ibtnShowPassword = (ImageButton) v.findViewById(R.id.ibtn_signin_edittext_show);
		ibtnClear = (ImageButton) v.findViewById(R.id.ibtn_signin_edittext_clear);
		ivIcon = (ImageView) v.findViewById(R.id.iv_signin_edittext_icon);
		isPassword = false;
		ibtnShowPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.isSelected()) {
					v.setSelected(false);
					_showPassword(false);
				} else {
					v.setSelected(true);
					_showPassword(true);
				}
			}
		});

		ibtnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				etText.setText("");
			}
		});

		etText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable edit) {
				String s = edit.toString();
				if (s.length() > 0) {
					if (isPassword) {
						ibtnShowPassword.setVisibility(View.VISIBLE);
					} else {
						ibtnClear.setVisibility(View.VISIBLE);
					}
				} else {
					ibtnClear.setVisibility(View.GONE);
					if (isPassword) {
						ibtnShowPassword.setVisibility(View.GONE);
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		ibtnClear.setVisibility(View.GONE);
		ibtnShowPassword.setVisibility(View.GONE);
	}

	private void _getAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SigninEditText);
		_setTypeArray(typedArray);
	}

	private void _getAttrs(AttributeSet attrs, int defStyle) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SigninEditText, defStyle, 0);
		_setTypeArray(typedArray);
	}

    private void _setTypeArray(TypedArray typedArray) {
		ivIcon.setBackgroundResource(
				typedArray.getResourceId(R.styleable.SigninEditText_categoryIcon, R.drawable.ic_signin_user));

		ibtnClear.setBackgroundResource(
				typedArray.getResourceId(R.styleable.SigninEditText_clearIcon, R.drawable.ic_edittext_clear));

		ibtnShowPassword.setBackgroundResource(
				typedArray.getResourceId(R.styleable.SigninEditText_showIcon, R.drawable.btn_edittext_show_password_white));

		etText.setHint(
				typedArray.getString(R.styleable.SigninEditText_hint));

		etText.setHintTextColor(
				typedArray.getColor(R.styleable.SigninEditText_hintColor, Color.WHITE));

		etText.setText(
				typedArray.getString(R.styleable.SigninEditText_text));

		etText.setTextColor(
				typedArray.getColor(R.styleable.SigninEditText_textColor, Color.WHITE));

		etText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				typedArray.getDimensionPixelSize(R.styleable.SigninEditText_textSize, 10));

		isPassword = typedArray.getBoolean(R.styleable.SigninEditText_password, false);
    }

	public void setBackground(int resId) {
		if (ctnSigninEditBackground != null) {
			ctnSigninEditBackground.setBackgroundResource(resId);
		}
	}

    public void setCategoryIcon(int resId) {
		if (ivIcon != null) {
			ivIcon.setBackgroundResource(resId);
		}
	}

	public void setClearIcon(int resId) {
		if (ibtnClear != null) {
			ibtnClear.setBackgroundResource(resId);
		}
	}

	public void setShowPasswordIcon(int resId) {
		if (ibtnShowPassword != null) {
			ibtnShowPassword.setBackgroundResource(resId);
		}
	}

	public void setHintTextColor(int color) {
		if (etText != null) {
			etText.setHintTextColor(color);
		}
	}

	public void setTextColor(int color) {
		if (etText != null) {
			etText.setTextColor(color);
		}
	}
}