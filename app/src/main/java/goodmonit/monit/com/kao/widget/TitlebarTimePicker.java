package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class TitlebarTimePicker extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "TBTimePicker";
	private static final boolean DBG = Configuration.DBG;

	private Wheel12HourTimePicker wtpWheelView;
	private TextView tvTitle, tvContents;
	private ImageView ivDirection;
	private View emptyDivider;
	private LinearLayout lctnTitleBar;

	private int mHour, mMinute, mAmPm;
	private boolean isExpanded = false;

	public TitlebarTimePicker(Context context) {
		super(context);
		_initView();
	}

	public TitlebarTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public TitlebarTimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	public void setExpanded(boolean expanded) {
		isExpanded = expanded;
		if (isExpanded) {
			emptyDivider.setVisibility(View.VISIBLE);
			ivDirection.setBackgroundResource(R.drawable.ic_direction_up_black);
			wtpWheelView.setVisibility(View.VISIBLE);
			wtpWheelView.onExpanded();
		} else {
			emptyDivider.setVisibility(View.GONE);
			ivDirection.setBackgroundResource(R.drawable.ic_direction_down_black);
			wtpWheelView.setVisibility(View.GONE);
			wtpWheelView.onCollapsed();
		}
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_titlebar_time_picker, this, false);
		addView(v);

		tvTitle = (TextView)v.findViewById(R.id.tv_titlebar_time_picker_title);
		tvContents = (TextView)v.findViewById(R.id.tv_titlebar_time_picker_contents);
		ivDirection = (ImageView)v.findViewById(R.id.iv_titlebar_time_picker_direction);
		emptyDivider = (View)v.findViewById(R.id.v_titlebar_time_picker_empty_divider);
		lctnTitleBar = (LinearLayout)v.findViewById(R.id.lctn_titlebar_time_picker_title_bar);
		lctnTitleBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setExpanded(!isExpanded);
			}
		});

		wtpWheelView = (Wheel12HourTimePicker)v.findViewById(R.id.wdp_titlebar_time_picker);
		wtpWheelView.setOnSelectedTimeListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onSelectedTime(int hour, int minute, int ampm) {
				if (DBG) Log.d(TAG, "onSelectedTime: " + hour + ":" + minute + ":" + ampm);
				mHour = hour;
				mMinute = minute;
				mAmPm = ampm;
				updateTitlebar();
			}
		});
		wtpWheelView.setTime(System.currentTimeMillis());
		setExpanded(true);
	}

	public void updateTitlebar() {
		String strAmPm = "";
		if (mAmPm == DateTimeUtil.HOUR_AM) {
			strAmPm = mContext.getString(R.string.time_am);
		} else {
			strAmPm = mContext.getString(R.string.time_pm);
		}
		setContents(String.format("%02d:%02d ", mHour, mMinute) + strAmPm);
	}

	public void setTime(int hour, int minute) {
		if (DBG) Log.d(TAG, "setTime: " + hour + " / " + minute);

		mHour = hour;
		mMinute = minute;

		if (mHour == 0 || mHour == 24) {
			mAmPm = DateTimeUtil.HOUR_AM;
			mHour = 12;
		} else if (mHour == 12){
			mAmPm = DateTimeUtil.HOUR_PM;
			mHour = 12;
		} else if (mHour <= 11) {
			mAmPm = DateTimeUtil.HOUR_AM;
		} else {
			mAmPm = DateTimeUtil.HOUR_PM;
			mHour = mHour - 12;
		}
		if (DBG) Log.d(TAG, "set12Hour: " + mHour + " / " + mMinute + " / " + mAmPm);
		wtpWheelView.setTime(hour, minute);
		updateTitlebar();
	}

	public void setTime(long utcTimeMs) {
		Date time = new Date(utcTimeMs);
		setTime(time.getHours(), time.getMinutes());
	}

	public void setTitle(String title) {
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}

	public void setContents(String contents) {
		if (tvContents != null) {
			tvContents.setText(contents);
		}
	}

	public String getSelectedTimeStringHHMM() {
		String timeString = "";

		if (mAmPm == DateTimeUtil.HOUR_AM) {
			if (mHour == 12) {
				mHour = 0;
			}
		} else {
			if (mHour == 12) {
				mHour = 12;
			} else {
				mHour += 12;
			}

			if (mHour > 23) mHour -= 12;
		}

		timeString = String.format("%02d%02d", mHour, mMinute);
		return timeString;
	}
}