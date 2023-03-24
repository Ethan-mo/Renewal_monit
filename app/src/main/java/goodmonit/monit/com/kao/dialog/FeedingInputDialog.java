package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.FeedingType;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.TitlebarDatePicker;
import goodmonit.monit.com.kao.widget.TitlebarTimePicker;

public class FeedingInputDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "DateTimeDlg";
    private static final boolean DBG = Configuration.DBG;

    private Context mContext;
	private TextView tvTitle, tvContents;
	private Button btnLeft, btnRight;
	private View.OnClickListener listenerLeftBtn, listenerRightBtn;
	private TitlebarTimePicker wtpTitlebarTimePicker;
	private TitlebarDatePicker wdpTitlebarDatePicker;

	private String mLeftBtnName, mRightBtnName;

	private long mDateTimeUtcMs = -1;
	private String mTitle, mContents;
	private int mSelectedIndex = 1;
	private NotificationMessage mMessage;

	private Button btnModeItem1, btnModeItem2, btnModeItem3, btnModeItem4;
	private Button btnLessStep1, btnLessStep2, btnMoreStep1, btnMoreStep2;
	private EditText etExtraValue;
	private TextView tvExtraValueUnit;

    public FeedingInputDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
	}

	public FeedingInputDialog(Context context,
                              String leftButtonName, View.OnClickListener leftButtonListener,
                              String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	public FeedingInputDialog(Context context, String title,
                              String leftButtonName, View.OnClickListener leftButtonListener,
                              String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	public FeedingInputDialog(Context context, String title, String contents,
                              String leftButtonName, View.OnClickListener leftButtonListener,
                              String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mContents = contents;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mContext = getContext();
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.dialog_input_feeding);
        _initView();

		setDateTimeUtcMs(mDateTimeUtcMs);
    }

	private void _initView() {
		tvTitle = (TextView)findViewById(R.id.tv_dialog_title);
		tvContents = (TextView)findViewById(R.id.tv_dialog_contents);
		btnLeft = (Button)findViewById(R.id.btn_dialog_left);
		btnRight = (Button)findViewById(R.id.btn_dialog_right);
		wtpTitlebarTimePicker = (TitlebarTimePicker)findViewById(R.id.wtp_dialog_time_picker);
		wdpTitlebarDatePicker = (TitlebarDatePicker) findViewById(R.id.wdp_dialog_date_picker);
		etExtraValue = (EditText)findViewById(R.id.et_dialog_extra_input);
		tvExtraValueUnit = (TextView)findViewById(R.id.tv_dialog_extra_input_unit);

		btnLessStep1 = (Button)findViewById(R.id.btn_dialog_extra_input_less_step1);
		btnLessStep1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateExtraValue(-1);
			}
		});
		btnLessStep2 = (Button)findViewById(R.id.btn_dialog_extra_input_less_step2);
		btnLessStep2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateExtraValue(-2);
			}
		});
		btnMoreStep1 = (Button)findViewById(R.id.btn_dialog_extra_input_more_step1);
		btnMoreStep1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateExtraValue(1);
			}
		});
		btnMoreStep2 = (Button)findViewById(R.id.btn_dialog_extra_input_more_step2);
		btnMoreStep2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateExtraValue(2);
			}
		});

		btnModeItem1 = (Button)findViewById(R.id.btn_dialog_mode_select_item1);
		btnModeItem1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btnModeItem1.setSelected(true);
				btnModeItem2.setSelected(false);
				btnModeItem3.setSelected(false);
				btnModeItem4.setSelected(false);
				tvExtraValueUnit.setText("분");
			}
		});
		btnModeItem2 = (Button)findViewById(R.id.btn_dialog_mode_select_item2);
		btnModeItem2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btnModeItem1.setSelected(false);
				btnModeItem2.setSelected(true);
				btnModeItem3.setSelected(false);
				btnModeItem4.setSelected(false);
				tvExtraValueUnit.setText("ml/g");
			}
		});
		btnModeItem3 = (Button)findViewById(R.id.btn_dialog_mode_select_item3);
		btnModeItem3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btnModeItem1.setSelected(false);
				btnModeItem2.setSelected(false);
				btnModeItem3.setSelected(true);
				btnModeItem4.setSelected(false);
				tvExtraValueUnit.setText("ml/g");
			}
		});
		btnModeItem4 = (Button)findViewById(R.id.btn_dialog_mode_select_item4);
		btnModeItem4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btnModeItem1.setSelected(false);
				btnModeItem2.setSelected(false);
				btnModeItem3.setSelected(false);
				btnModeItem4.setSelected(true);
				tvExtraValueUnit.setText("g");
			}
		});

		setSelectedIndex(mSelectedIndex);
		setButtonName(mLeftBtnName, mRightBtnName);
		setButtonListener(listenerLeftBtn, listenerRightBtn);
		if (mTitle == null) {
			tvTitle.setVisibility(View.GONE);
		} else {
			tvTitle.setText(mTitle);
		}
		if (mContents == null) {
			tvContents.setVisibility(View.GONE);
		} else {
			tvContents.setText(mContents);
		}
    }

    public void updateExtraValue(int updateType) {
		String strExtra = etExtraValue.getText().toString();
		int intExtra = 0;
		try {
			intExtra = Integer.parseInt(strExtra);
		} catch (Exception e) {
			if (btnModeItem1.isSelected()) { // 1단계 감소시 모유수유 1분 감소
				intExtra = PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.NURSED_BREAST_MILK, FeedingType.NURSED_BREAST_MILK);
			} else if (btnModeItem2.isSelected()) {
				intExtra = PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_BREAST_MILK);
			} else if (btnModeItem3.isSelected()) {
				intExtra = PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.BOTTLE_FORMULA_MILK, FeedingType.BOTTLE_FORMULA_MILK);
			} else if (btnModeItem4.isSelected()) {
				intExtra = PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.BABY_FOOD, FeedingType.BABY_FOOD);
			}
		}

		if (updateType == -2) {
			if (btnModeItem1.isSelected()) { // 1단계 감소시 모유수유 1분 감소
				intExtra = intExtra - FeedingType.STEP2_VALUE_NURSED_BREAST_MILK;
			} else if (btnModeItem2.isSelected()) {
				intExtra = intExtra - FeedingType.STEP2_VALUE_BOTTLE_BREAST_MILK;
			} else if (btnModeItem3.isSelected()) {
				intExtra = intExtra - FeedingType.STEP2_VALUE_BOTTLE_FORMULA_MILK;
			} else if (btnModeItem4.isSelected()) {
				intExtra = intExtra - FeedingType.STEP2_VALUE_BABY_FOOD;
			}
		} else if (updateType == -1) {
			if (btnModeItem1.isSelected()) { // 1단계 감소시 모유수유 1분 감소
				intExtra = intExtra - FeedingType.STEP1_VALUE_NURSED_BREAST_MILK;
			} else if (btnModeItem2.isSelected()) {
				intExtra = intExtra - FeedingType.STEP1_VALUE_BOTTLE_BREAST_MILK;
			} else if (btnModeItem3.isSelected()) {
				intExtra = intExtra - FeedingType.STEP1_VALUE_BOTTLE_FORMULA_MILK;
			} else if (btnModeItem4.isSelected()) {
				intExtra = intExtra - FeedingType.STEP1_VALUE_BABY_FOOD;
			}
		} else if (updateType == 1) {
			if (btnModeItem1.isSelected()) { // 1단계 감소시 모유수유 1분 감소
				intExtra = intExtra + FeedingType.STEP1_VALUE_NURSED_BREAST_MILK;
			} else if (btnModeItem2.isSelected()) {
				intExtra = intExtra + FeedingType.STEP1_VALUE_BOTTLE_BREAST_MILK;
			} else if (btnModeItem3.isSelected()) {
				intExtra = intExtra + FeedingType.STEP1_VALUE_BOTTLE_FORMULA_MILK;
			} else if (btnModeItem4.isSelected()) {
				intExtra = intExtra + FeedingType.STEP1_VALUE_BABY_FOOD;
			}
		} else if (updateType == 2) {
			if (btnModeItem1.isSelected()) { // 1단계 감소시 모유수유 1분 감소
				intExtra = intExtra + FeedingType.STEP2_VALUE_NURSED_BREAST_MILK;
			} else if (btnModeItem2.isSelected()) {
				intExtra = intExtra + FeedingType.STEP2_VALUE_BOTTLE_BREAST_MILK;
			} else if (btnModeItem3.isSelected()) {
				intExtra = intExtra + FeedingType.STEP2_VALUE_BOTTLE_FORMULA_MILK;
			} else if (btnModeItem4.isSelected()) {
				intExtra = intExtra + FeedingType.STEP2_VALUE_BABY_FOOD;
			}
		}

		if (btnModeItem1.isSelected()) { // 1단계 감소시 모유수유 1분 감소
			PreferenceManager.getInstance(mContext).setLatestFeedingAmount(FeedingType.NURSED_BREAST_MILK, intExtra);
		} else if (btnModeItem2.isSelected()) {
			PreferenceManager.getInstance(mContext).setLatestFeedingAmount(FeedingType.BOTTLE_BREAST_MILK, intExtra);
		} else if (btnModeItem3.isSelected()) {
			PreferenceManager.getInstance(mContext).setLatestFeedingAmount(FeedingType.BOTTLE_FORMULA_MILK, intExtra);
		} else if (btnModeItem4.isSelected()) {
			PreferenceManager.getInstance(mContext).setLatestFeedingAmount(FeedingType.BABY_FOOD, intExtra);
		}
		etExtraValue.setText(intExtra + "");
	}

	public int getSelectedMode() {
    	if (btnModeItem1.isSelected()) {
    		return FeedingType.NURSED_BREAST_MILK;	// Nursed Breast milk
		} else if (btnModeItem2.isSelected()) {
			return FeedingType.BOTTLE_BREAST_MILK;	// Bottle Breast milk
		} else if (btnModeItem3.isSelected()) {
			return FeedingType.BOTTLE_FORMULA_MILK;	// Bottle Formula milk
		} else if (btnModeItem4.isSelected()) {
			return FeedingType.BABY_FOOD;	// Baby food
		} else {
    		return -1;
		}
	}

	public String getExtraValue() {
    	if (etExtraValue != null) {
    		return etExtraValue.getText().toString();
		} else {
    		return null;
		}
	}

	public void setExtraValue(String extraValue) {
		if (etExtraValue != null) {
			etExtraValue.setText(extraValue);
		}
	}

	public void setDateTimeUtcMs(long utcMs) {
    	if (utcMs == -1) {
    		mDateTimeUtcMs = System.currentTimeMillis();
		} else {
    		mDateTimeUtcMs = utcMs;
		}

    	if (wtpTitlebarTimePicker != null) {
			wtpTitlebarTimePicker.setTime(utcMs);
		}

    	if (wdpTitlebarDatePicker != null) {
			wdpTitlebarDatePicker.setTime(utcMs);
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

	public long getDateTimeUtcMs() {
		String yyyyMMdd = "20" + wdpTitlebarDatePicker.getSelectedDateStringYYMMDD();
		String HHmmss = wtpTitlebarTimePicker.getSelectedTimeStringHHMM() + ((mDateTimeUtcMs / 1000) % 60);
		long utcMs = DateTimeUtil.getUtcTimeStampFromLocalString(yyyyMMdd + HHmmss, "yyyyMMddHHmmss");
		if (DBG) Log.d(TAG, "getDateTimeUtcMs: " + yyyyMMdd + "-" + HHmmss + " -> " + utcMs);
		return utcMs;
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

	public void setSelectedIndex(int idx) {
		mSelectedIndex = idx;
		if (mSelectedIndex == FeedingType.NURSED_BREAST_MILK) {
			if (btnModeItem1 != null) {
				btnModeItem1.callOnClick();
				etExtraValue.setText(PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.NURSED_BREAST_MILK, FeedingType.DEFAULT_VALUE_NURSED_BREAST_MILK) + "");
			}
		} else if (mSelectedIndex == FeedingType.BOTTLE_BREAST_MILK) {
			if (btnModeItem2 != null) {
				btnModeItem2.callOnClick();
				etExtraValue.setText(PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.BOTTLE_BREAST_MILK, FeedingType.DEFAULT_VALUE_BOTTLE_BREAST_MILK) + "");
			}
		} else if (mSelectedIndex == FeedingType.BOTTLE_FORMULA_MILK) {
			if (btnModeItem3 != null) {
				btnModeItem3.callOnClick();
				etExtraValue.setText(PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.BOTTLE_FORMULA_MILK, FeedingType.DEFAULT_VALUE_BOTTLE_FORMULA_MILK) + "");
			}
		} else if (mSelectedIndex == FeedingType.BABY_FOOD) {
			if (btnModeItem4 != null) {
				btnModeItem4.callOnClick();
				etExtraValue.setText(PreferenceManager.getInstance(mContext).getLatestFeedingAmount(FeedingType.BABY_FOOD, FeedingType.DEFAULT_VALUE_BABY_FOOD) + "");
			}
		}
	}

	public void setNotificationData(NotificationMessage msg) {
		mMessage = msg;
	}

	public NotificationMessage getNotificationData() {
		return mMessage;
	}

	@Override
	public void onBackPressed() {
    }

	@Override
	public void setOnCancelListener(OnCancelListener listener) {
		super.setOnCancelListener(listener);
	}
}
