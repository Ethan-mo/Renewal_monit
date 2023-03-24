package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.TitlebarDatePicker;
import goodmonit.monit.com.kao.widget.TitlebarTimePicker;

public class SleepInputDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "DateTimeDlg";
    private static final boolean DBG = Configuration.DBG;

	private TextView tvTitle, tvContents;
	private Button btnLeft, btnRight;
	private View.OnClickListener listenerLeftBtn, listenerRightBtn;
	private TitlebarTimePicker wtpTitlebarTimePickerStart;
	private TitlebarDatePicker wdpTitlebarDatePickerStart;
	private TitlebarTimePicker wtpTitlebarTimePickerEnd;
	private TitlebarDatePicker wdpTitlebarDatePickerEnd;

	private String mLeftBtnName, mRightBtnName;

	private long mDateTimeUtcMs = -1;
	private String mTitle, mContents;
	private int mSelectedIndex = 1;
	private NotificationMessage mMessage;

    public SleepInputDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
	}

	public SleepInputDialog(Context context,
                            String leftButtonName, View.OnClickListener leftButtonListener,
                            String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	public SleepInputDialog(Context context, String title,
                            String leftButtonName, View.OnClickListener leftButtonListener,
                            String rightButtonName, View.OnClickListener rightButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mLeftBtnName = leftButtonName;
		mRightBtnName = rightButtonName;
		listenerLeftBtn = leftButtonListener;
		listenerRightBtn = rightButtonListener;
	}

	public SleepInputDialog(Context context, String title, String contents,
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

        setContentView(R.layout.dialog_input_sleeping);
        _initView();

		setStartDateTimeUtcMs(mDateTimeUtcMs);
		setEndDateTimeUtcMs(mDateTimeUtcMs + 1000 * 60 * 60);
    }

	private void _initView() {
		tvTitle = (TextView)findViewById(R.id.tv_dialog_title);
		tvContents = (TextView)findViewById(R.id.tv_dialog_contents);
		btnLeft = (Button)findViewById(R.id.btn_dialog_left);
		btnRight = (Button)findViewById(R.id.btn_dialog_right);
		wtpTitlebarTimePickerStart = (TitlebarTimePicker)findViewById(R.id.wtp_dialog_time_picker);
		wdpTitlebarDatePickerStart = (TitlebarDatePicker) findViewById(R.id.wdp_dialog_date_picker);

		wtpTitlebarTimePickerEnd = (TitlebarTimePicker)findViewById(R.id.wtp_dialog_time_picker2);
		wdpTitlebarDatePickerEnd = (TitlebarDatePicker) findViewById(R.id.wdp_dialog_date_picker2);

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

	public void setStartDateTimeUtcMs(long utcMs) {
    	if (utcMs == -1) {
    		mDateTimeUtcMs = System.currentTimeMillis();
		} else {
    		mDateTimeUtcMs = utcMs;
		}

    	if (wtpTitlebarTimePickerStart != null) {
			wtpTitlebarTimePickerStart.setTime(utcMs);
		}

    	if (wdpTitlebarDatePickerStart != null) {
			wdpTitlebarDatePickerStart.setTime(utcMs);
		}
	}

	public void setEndDateTimeUtcMs(long utcMs) {
		if (wtpTitlebarTimePickerEnd != null) {
			wtpTitlebarTimePickerEnd.setTime(utcMs);
		}

		if (wdpTitlebarDatePickerEnd != null) {
			wdpTitlebarDatePickerEnd.setTime(utcMs);
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

	public long getStartDateTimeUtcMs() {
		String yyyyMMdd = "20" + wdpTitlebarDatePickerStart.getSelectedDateStringYYMMDD();
		String HHmmss = wtpTitlebarTimePickerStart.getSelectedTimeStringHHMM() + ((mDateTimeUtcMs / 1000) % 60);
		long utcMs = DateTimeUtil.getUtcTimeStampFromLocalString(yyyyMMdd + HHmmss, "yyyyMMddHHmmss");
		if (DBG) Log.d(TAG, "getStartDateTimeUtcMs: " + yyyyMMdd + "-" + HHmmss + " -> " + utcMs);
		return utcMs;
	}

	public long getEndDateTimeUtcMs() {
		String yyyyMMdd = "20" + wdpTitlebarDatePickerEnd.getSelectedDateStringYYMMDD();
		String HHmmss = wtpTitlebarTimePickerEnd.getSelectedTimeStringHHMM() + ((mDateTimeUtcMs / 1000) % 60);
		long utcMs = DateTimeUtil.getUtcTimeStampFromLocalString(yyyyMMdd + HHmmss, "yyyyMMddHHmmss");
		if (DBG) Log.d(TAG, "getEndDateTimeUtcMs: " + yyyyMMdd + "-" + HHmmss + " -> " + utcMs);
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

	public void expandEndTime(boolean expand) {
    	if (wtpTitlebarTimePickerEnd != null) {
			wtpTitlebarTimePickerEnd.setExpanded(expand);
		}
        if (wdpTitlebarDatePickerEnd != null) {
			wdpTitlebarDatePickerEnd.setExpanded(expand);
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
