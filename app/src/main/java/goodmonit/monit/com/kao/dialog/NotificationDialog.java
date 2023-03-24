package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class NotificationDialog extends Dialog {
	private static final boolean DBG = Configuration.DBG;
	private static final String TAG = Configuration.BASE_TAG + "NotificationDlg";

	private Context mContext;
	private NotificationMessage mNotificationMsg;
	private String mBtnName;
	private View.OnClickListener mBtnListener, mBtnDeleteListener, mBtnWrongAlarmListener;
	private Button btnNegative, btnWrongAlarm, btnDelete;
	private TextView tvNotificationDescription, tvNotificationTime, tvWrongAlarm;
	private ImageView ivNotificationIcon;
	private boolean isWrongAlarm;

    public NotificationDialog(Context context,
							  NotificationMessage msg,
							  String btnName,
							  View.OnClickListener btnListener,
							  View.OnClickListener btnDeleteListener,
							  View.OnClickListener btnWrongAlarmListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
		mNotificationMsg = msg;
		mBtnName = btnName;
		mBtnListener = btnListener;
		mBtnDeleteListener = btnDeleteListener;
		mBtnWrongAlarmListener = btnWrongAlarmListener;

		isWrongAlarm = false;

		if (mNotificationMsg != null && "2".equals(mNotificationMsg.extra)) {
			isWrongAlarm = true;
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.dialog_notification);
        _initView();

		setNotification(mNotificationMsg);
		setButtonName(mBtnName);
		setButtonListener(mBtnListener);
		setWrongAlarm(isWrongAlarm);
    }

    public void setNotification(NotificationMessage msg) {
		mNotificationMsg = msg;
		if (ivNotificationIcon != null) {
			ivNotificationIcon.setBackgroundResource(NotificationType.getIconResource(mNotificationMsg.notiType));
		}
		if (tvNotificationDescription != null) {
			tvNotificationDescription.setText(NotificationType.getStringResource(mNotificationMsg.notiType));
		}
		if (tvNotificationTime != null) {
			tvNotificationTime.setText(DateTimeUtil.getDateTimeString(mNotificationMsg.timeMs, "ko"));
		}
		if (mNotificationMsg != null) {
			if ("2".equals(mNotificationMsg.extra)) {
				setWrongAlarm(true);
			} else {
				setWrongAlarm(false);
			}
		}
	}

	public void setButtonName(String btnName) {
		mBtnName = btnName;
		if (btnNegative != null) {
			btnNegative.setText(mBtnName);
		}
	}

	public void setButtonListener(View.OnClickListener listener) {
		mBtnListener = listener;
		if (btnNegative != null) {
			btnNegative.setOnClickListener(mBtnListener);
		}
	}

	public void setWrongAlarm(boolean wrong) {
		isWrongAlarm = wrong;
		if (isWrongAlarm) {
			if (tvWrongAlarm != null) {
				tvWrongAlarm.setVisibility(View.VISIBLE);
			}
			if (btnWrongAlarm != null) {
				btnWrongAlarm.setBackgroundResource(R.drawable.ic_report_wrong_alarm_enabled);
			}
		} else {
			if (tvWrongAlarm != null) {
				tvWrongAlarm.setVisibility(View.INVISIBLE);
			}
			if (btnWrongAlarm != null) {
				btnWrongAlarm.setBackgroundResource(R.drawable.ic_report_wrong_alarm_enabled);
			}
		}
	}

	public NotificationMessage getEditedNotificationMessage() {
		return mNotificationMsg;
	}

	private void _initView() {
		tvNotificationTime = (TextView)findViewById(R.id.tv_dialog_notification_time);
		ivNotificationIcon = (ImageView)findViewById(R.id.iv_dialog_notification_icon);
		tvNotificationDescription = (TextView)findViewById(R.id.tv_dialog_notification_description);
		tvWrongAlarm = (TextView)findViewById(R.id.tv_dialog_notification_wrong_alarm);
		btnNegative = (Button)findViewById(R.id.btn_dialog_notification_negative);
		btnNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		btnWrongAlarm = (Button)findViewById(R.id.btn_dialog_notification_report_wrong_alarm);
		btnWrongAlarm.setOnClickListener(mBtnWrongAlarmListener);
		btnDelete = (Button)findViewById(R.id.btn_dialog_notification_delete);
		btnDelete.setOnClickListener(mBtnDeleteListener);
	}

	@Override
	public void onBackPressed() {
		if (btnNegative != null) {
			btnNegative.callOnClick();
		}
    }

	@Override
	public void setOnCancelListener(OnCancelListener listener) {
		super.setOnCancelListener(listener);
	}
}
