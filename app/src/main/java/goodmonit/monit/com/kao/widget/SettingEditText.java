package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class SettingEditText extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "SettingEditText";

	protected TextView tvTitle;
	protected EditText etText;
	protected Button btnClear;
	protected Button btnShowPassword;
	protected boolean isPassword = false;

	public SettingEditText(Context context) {
		super(context);
		_initView();
	}

	public SettingEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public SettingEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_setting_edittext, this, false);
		addView(v);

		tvTitle = (TextView) v.findViewById(R.id.tv_widget_setting_edittext_title);
		etText = (EditText) v.findViewById(R.id.et_widget_setting_edittext);
		btnClear = (Button) v.findViewById(R.id.btn_widget_setting_edittext_clear);
		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				etText.setText("");
			}
		});
		btnShowPassword = (Button)v.findViewById(R.id.btn_widget_setting_edittext_show_password);
		btnShowPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnShowPassword.isSelected()) {
					btnShowPassword.setSelected(false);
					_showPassword(false);
				} else {
					btnShowPassword.setSelected(true);
					_showPassword(true);
				}
			}
		});
		btnShowPassword.setVisibility(View.GONE);

		etText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 0) {
					if (isPassword) {
						btnShowPassword.setVisibility(View.VISIBLE);
					} else {
						btnClear.setVisibility(View.VISIBLE);
					}
				} else {
					btnClear.setVisibility(View.GONE);
					btnShowPassword.setVisibility(View.GONE);
				}
			}
		});
	}

	public void setText(String text) {
		etText.setText(text);
	}

	public void setHint(String text) {
		etText.setHint(text);
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	public String getText() {
		return etText.getText().toString();
	}

	private void _showPassword(boolean show) {
		if (etText == null) return;
		if (show) {
			etText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			etText.setTransformationMethod(null);
		} else {
			etText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
			etText.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		etText.setSelection(etText.length());
	}

	public void setPassword(boolean password) {
		isPassword = password;
		if (isPassword) {
			_showPassword(false);
		}
	}
}