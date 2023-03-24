package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class SimpleDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "SimpleDialog";

	private static final int DIALOG_MODE_CONTENTS_WITH_SINGLE_BUTTON				= 1;
	private static final int DIALOG_MODE_CONTENTS_WITH_DOUBLE_BUTTONS				= 2;
	private static final int DIALOG_MODE_CONTENTS_WITH_TRIPLE_BUTTONS				= 3;
	private static final int DIALOG_MODE_CONTENTS_WITH_SINGLE_BUTTON_AND_TITLE		= 4;
	private static final int DIALOG_MODE_CONTENTS_WITH_DOUBLE_BUTTONS_AND_TITLE		= 5;
	private static final int DIALOG_MODE_CONTENTS_WITH_TRIPLE_BUTTONS_AND_TITLE		= 6;
	private int mDialogMode;

	private TextView tvTitle, tvContents;
	private Button btnLeft, btnRight, btnCenter;
	private View vDividerCenterBtn, vDividerRightBtn;
	private View.OnClickListener listenerLeftBtn, listenerRightBtn, listenerCenterBtn, listenerHelpBtn;
	private LinearLayout ctnContents, ctnExtraContents;
	private EditText etInputText;
    private int mLeftBtnColor = -1, mRightBtnColor = -1, mCenterBtnColor = -1;
	private String mInputText, mHintText;
	private int mContentsGravity = Gravity.CENTER;
	private boolean isInputMode;
	private boolean showHelpButton = false;
	private Button btnHelp;

	private String mTitle, mContents, mLeftBtnName, mRightBtnName, mCenterBtnName;

    public SimpleDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
	}
    
    public SimpleDialog(Context context, String contents,
						String btnName, View.OnClickListener btnListener) {
    	super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mDialogMode = DIALOG_MODE_CONTENTS_WITH_SINGLE_BUTTON;
		mContents = contents;
		mLeftBtnName = btnName;
		listenerLeftBtn = btnListener;
		setInputMode(false);
	}

	public SimpleDialog(Context context, String contents,
						String leftButtonName, View.OnClickListener leftButtonListener,
						String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mDialogMode = DIALOG_MODE_CONTENTS_WITH_DOUBLE_BUTTONS;
		mContents = contents;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
		setInputMode(false);
	}

	public SimpleDialog(Context context, String contents,
						String leftButtonName, View.OnClickListener leftButtonListener,
						String centerButtonName, View.OnClickListener centerButtonListener,
						String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mDialogMode = DIALOG_MODE_CONTENTS_WITH_TRIPLE_BUTTONS;
		mContents = contents;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		mCenterBtnName = centerButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
		listenerCenterBtn = centerButtonListener;
		setInputMode(false);
	}

	public SimpleDialog(Context context, String title, String contents,
						String btnName, View.OnClickListener btnListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mDialogMode = DIALOG_MODE_CONTENTS_WITH_SINGLE_BUTTON_AND_TITLE;
		mTitle = title;
		mContents = contents;
		mLeftBtnName = btnName;
		listenerLeftBtn = btnListener;
		setInputMode(false);
	}

	public SimpleDialog(Context context, String title, String contents,
						String leftButtonName, View.OnClickListener leftButtonListener,
						String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mDialogMode = DIALOG_MODE_CONTENTS_WITH_DOUBLE_BUTTONS_AND_TITLE;
		mTitle = title;
		mContents = contents;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
		setInputMode(false);
	}

	public SimpleDialog(Context context, String title, String contents,
						String leftButtonName, View.OnClickListener leftButtonListener,
						String centerButtonName, View.OnClickListener centertButtonListener,
						String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mDialogMode = DIALOG_MODE_CONTENTS_WITH_TRIPLE_BUTTONS_AND_TITLE;
		mTitle = title;
		mContents = contents;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		mCenterBtnName = centerButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
		listenerCenterBtn = centertButtonListener;
		setInputMode(false);
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.dialog_simple);
        _initView();

		setTitle(mTitle);
		setContents(mContents);

		switch(mDialogMode) {
			case DIALOG_MODE_CONTENTS_WITH_SINGLE_BUTTON:
				tvTitle.setVisibility(View.GONE);
				tvContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.font_13_5sp));
				tvContents.setLineSpacing(getContext().getResources().getDimension(R.dimen.font_6_5sp), 1);
				setButtonName(mLeftBtnName);
				setButtonListener(listenerLeftBtn);
				if (mLeftBtnColor != -1) {
					setButtonColor(mLeftBtnColor);
				}
				vDividerRightBtn.setVisibility(View.GONE);
				btnRight.setVisibility(View.GONE);
				vDividerCenterBtn.setVisibility(View.GONE);
				btnCenter.setVisibility(View.GONE);
				break;
			case DIALOG_MODE_CONTENTS_WITH_SINGLE_BUTTON_AND_TITLE:
				setButtonName(mLeftBtnName);
				setButtonListener(listenerLeftBtn);
				if (mLeftBtnColor != -1) {
					setButtonColor(mLeftBtnColor);
				}
				vDividerRightBtn.setVisibility(View.GONE);
				btnRight.setVisibility(View.GONE);
				vDividerCenterBtn.setVisibility(View.GONE);
				btnCenter.setVisibility(View.GONE);
				break;
			case DIALOG_MODE_CONTENTS_WITH_DOUBLE_BUTTONS:
				tvTitle.setVisibility(View.GONE);
				tvContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.font_13_5sp));
				tvContents.setLineSpacing(getContext().getResources().getDimension(R.dimen.font_6_5sp), 1);
				setButtonName(mLeftBtnName, mRightBtnName);
				setButtonListener(listenerLeftBtn, listenerRightBtn);
				if (mLeftBtnColor != -1) {
					setButtonColor(mLeftBtnColor, mRightBtnColor);
				}
				vDividerCenterBtn.setVisibility(View.GONE);
				btnCenter.setVisibility(View.GONE);
				break;
			case DIALOG_MODE_CONTENTS_WITH_DOUBLE_BUTTONS_AND_TITLE:
				setButtonName(mLeftBtnName, mRightBtnName);
				setButtonListener(listenerLeftBtn, listenerRightBtn);
				if (mLeftBtnColor != -1) {
					setButtonColor(mLeftBtnColor, mRightBtnColor);
				}
				vDividerCenterBtn.setVisibility(View.GONE);
				btnCenter.setVisibility(View.GONE);
				break;
			case DIALOG_MODE_CONTENTS_WITH_TRIPLE_BUTTONS:
				tvTitle.setVisibility(View.GONE);
				tvContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.font_13_5sp));
				tvContents.setLineSpacing(getContext().getResources().getDimension(R.dimen.font_6_5sp), 1);
				setButtonName(mLeftBtnName, mCenterBtnName, mRightBtnName);
				setButtonListener(listenerLeftBtn, listenerCenterBtn, listenerRightBtn);
				if (mLeftBtnColor != -1) {
					setButtonColor(mLeftBtnColor, mCenterBtnColor, mRightBtnColor);
				}
				break;
			case DIALOG_MODE_CONTENTS_WITH_TRIPLE_BUTTONS_AND_TITLE:
				setButtonName(mLeftBtnName, mCenterBtnName, mRightBtnName);
				setButtonListener(listenerLeftBtn, listenerCenterBtn, listenerRightBtn);
				if (mLeftBtnColor != -1) {
					setButtonColor(mLeftBtnColor, mCenterBtnColor, mRightBtnColor);
				}
				break;
		}
		if (isInputMode && mHintText != null) {
			etInputText.setHint(mHintText);
		}
		tvContents.setGravity(mContentsGravity);

		if (showHelpButton) {
			btnHelp.setVisibility(View.VISIBLE);
		} else {
			btnHelp.setVisibility(View.GONE);
		}
		btnHelp.setOnClickListener(listenerHelpBtn);
    }

    public void setContentsGravity(int gravity) {
		mContentsGravity = gravity;
	}

    public void setInputMode(boolean input) {
		isInputMode = input;
	}

	public void setHintText(String text) {
		mHintText = text;
	}

	public void setInputText(String text) {
		mInputText = text;
		if (etInputText != null) {
			etInputText.setText("");
		}
	}

	public String getInputText() {
		return etInputText.getText().toString();
	}

	private void _initView() {
		ctnContents = (LinearLayout)findViewById(R.id.ctn_dialog_contents);
		ctnExtraContents = (LinearLayout)findViewById(R.id.ctn_dialog_extra_contents);
		tvTitle = (TextView)findViewById(R.id.tv_dialog_title);
		tvContents = (TextView)findViewById(R.id.tv_dialog_contents);
		btnLeft = (Button)findViewById(R.id.btn_dialog_left);
		btnCenter = (Button)findViewById(R.id.btn_dialog_center);
		btnRight = (Button)findViewById(R.id.btn_dialog_right);
		vDividerCenterBtn = findViewById(R.id.v_dialog_divider_btn_center);
		vDividerRightBtn = findViewById(R.id.v_dialog_divider_btn_right);
		etInputText = (EditText)findViewById(R.id.et_dialog_extra_input);
		btnHelp = (Button)findViewById(R.id.btn_dialog_help);

		if (isInputMode) {
			ctnExtraContents.setVisibility(View.VISIBLE);
			etInputText.setText(mInputText);
			etInputText.setSelection(etInputText.length());
		} else {
			ctnExtraContents.setVisibility(View.GONE);
		}
    }

	public void setTitle(String title) {
		mTitle = title;
		if (tvTitle != null) {
			tvTitle.setText(mTitle);
		}
	}

	public void setContents(String contents) {
		mContents = contents;
		if (tvContents != null) {
			tvContents.setText(mContents);
		}
	}

	public void setButtonName(String singleName) {
		mLeftBtnName = singleName;
		if (btnLeft != null) {
			btnLeft.setText(mLeftBtnName);
		}
	}

	public void setButtonName(String leftBtnName, String rightBtnName) {
		mLeftBtnName = leftBtnName;
		mRightBtnName = rightBtnName;
		if (btnLeft != null) {
			btnLeft.setText(mLeftBtnName);
		}
		if (btnRight != null) {
			btnRight.setText(mRightBtnName);
		}
	}

	public void setButtonName(String leftBtnName, String centerBtnName, String rightBtnName) {
		mLeftBtnName = leftBtnName;
		mCenterBtnName = centerBtnName;
		mRightBtnName = rightBtnName;
		if (btnLeft != null) {
			btnLeft.setText(mLeftBtnName);
		}
		if (btnCenter != null) {
			btnCenter.setText(mCenterBtnName);
		}
		if (btnRight != null) {
			btnRight.setText(mRightBtnName);
		}
	}

	public void setButtonColor(int btnColor) {
		mLeftBtnColor = btnColor;
		if (btnLeft != null) {
			btnLeft.setTextColor(mLeftBtnColor);
		}
	}

	public void setButtonColor(int leftBtnColor, int rightBtnColor) {
		mLeftBtnColor = leftBtnColor;
		mRightBtnColor = rightBtnColor;
		if (btnLeft != null) {
			btnLeft.setTextColor(mLeftBtnColor);
		}
		if (btnRight != null) {
			btnRight.setTextColor(mRightBtnColor);
		}
	}

	public void setButtonColor(int leftBtnColor, int centerBtnColor, int rightBtnColor) {
		mLeftBtnColor = leftBtnColor;
		mCenterBtnColor = centerBtnColor;
		mRightBtnColor = rightBtnColor;
		if (btnLeft != null) {
			btnLeft.setTextColor(mLeftBtnColor);
		}
		if (btnCenter != null) {
			btnCenter.setTextColor(mCenterBtnColor);
		}
		if (btnRight != null) {
			btnRight.setTextColor(mRightBtnColor);
		}
	}

	public void setButtonListener(View.OnClickListener singleBtnListener) {
		listenerLeftBtn = singleBtnListener;
		if (btnLeft != null) {
			btnLeft.setOnClickListener(listenerLeftBtn);
		}
	}

	public void setButtonListener(View.OnClickListener leftBtnListener, View.OnClickListener rightBtnListener) {
		listenerLeftBtn = leftBtnListener;
		listenerRightBtn = rightBtnListener;
		if (btnLeft != null) {
			btnLeft.setOnClickListener(listenerLeftBtn);
		}
		if (btnRight != null) {
			btnRight.setOnClickListener(listenerRightBtn);
		}
	}

	public void setButtonListener(View.OnClickListener leftBtnListener, View.OnClickListener centerBtnListener, View.OnClickListener rightBtnListener) {
		listenerLeftBtn = leftBtnListener;
		listenerRightBtn = rightBtnListener;
		listenerCenterBtn = centerBtnListener;
		if (btnLeft != null) {
			btnLeft.setOnClickListener(listenerLeftBtn);
		}
		if (btnCenter != null) {
			btnCenter.setOnClickListener(listenerCenterBtn);
		}
		if (btnRight != null) {
			btnRight.setOnClickListener(listenerRightBtn);
		}
	}

	public void showHelpButton(boolean show) {
		showHelpButton = show;
	}

	public void setHelpButtonListener(View.OnClickListener listener) {
		listenerHelpBtn = listener;
	}

	@Override
	public void onBackPressed() {
    }

	@Override
	public void setOnCancelListener(OnCancelListener listener) {
		super.setOnCancelListener(listener);
	}
}
