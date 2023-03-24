package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.TitlebarDatePicker;
import goodmonit.monit.com.kao.widget.TitlebarTimePicker;

public class DiaperInputDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "DateTimeDlg";
    private static final boolean DBG = Configuration.DBG;

	private TextView tvTitle, tvContents;
	private Button btnLeft, btnRight;
	private View.OnClickListener listenerLeftBtn, listenerRightBtn;
	private TitlebarTimePicker wtpTitlebarTimePicker;
	private TitlebarDatePicker wdpTitlebarDatePicker;

	private String mLeftBtnName, mRightBtnName;

	private long mDateTimeUtcMs = -1;
	private String mTitle, mContents;
	private int mSelectedIndex = 2;
	private NotificationMessage mMessage;

	private boolean showModeSelect;
	private LinearLayout lctnModeSelect;
	private Button btnModeItem1, btnModeItem2, btnModeItem3, btnModeItem4;

    public DiaperInputDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
	}

	public DiaperInputDialog(Context context,
                             String leftButtonName, View.OnClickListener leftButtonListener,
                             String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	public DiaperInputDialog(Context context, String title,
                             String leftButtonName, View.OnClickListener leftButtonListener,
                             String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	public DiaperInputDialog(Context context, String title, String contents,
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
         
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.dialog_input_diaper);
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
		lctnModeSelect = (LinearLayout)findViewById(R.id.lctn_dialog_mode_select);
		btnModeItem1 = (Button)findViewById(R.id.btn_dialog_mode_select_item1);
		btnModeItem1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btnModeItem1.setSelected(true);
				btnModeItem2.setSelected(false);
				btnModeItem3.setSelected(false);
				btnModeItem4.setSelected(false);
			}
		});
		btnModeItem1.setVisibility(View.GONE);
		btnModeItem2 = (Button)findViewById(R.id.btn_dialog_mode_select_item2);
		btnModeItem2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btnModeItem1.setSelected(false);
				btnModeItem2.setSelected(true);
				btnModeItem3.setSelected(false);
				btnModeItem4.setSelected(false);
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
			}
		});

		if (mSelectedIndex == 1) {
			btnModeItem1.callOnClick();
		} else if (mSelectedIndex == 2) {
			btnModeItem2.callOnClick();
		} else if (mSelectedIndex == 3) {
			btnModeItem3.callOnClick();
		} else if (mSelectedIndex == 4) {
			btnModeItem4.callOnClick();
		}

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

	public int getSelectedMode() {
    	if (btnModeItem1.isSelected()) {
    		return 1;	// Nothing
		} else if (btnModeItem2.isSelected()) {
			return 2;	// Pee
		} else if (btnModeItem3.isSelected()) {
			return 3;	// Poo
		} else if (btnModeItem4.isSelected()) {
			return 4;	// Mixed
		} else {
    		return -1;
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
    	// 기본 Clean은 옵션에서 제외
    	if (idx == 1) idx = 2;
    	mSelectedIndex = idx;
    	if (mSelectedIndex == 1) {
    		if (btnModeItem1 != null) {
				btnModeItem1.callOnClick();
			}
		} else if (mSelectedIndex == 2) {
			if (btnModeItem2 != null) {
				btnModeItem2.callOnClick();
			}
		} else if (mSelectedIndex == 3) {
			if (btnModeItem3 != null) {
				btnModeItem3.callOnClick();
			}
		} else if (mSelectedIndex == 4) {
			if (btnModeItem4 != null) {
				btnModeItem4.callOnClick();
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
