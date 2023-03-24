package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class Wheel12HourTimePicker extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "12HourPicker";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;
	private WheelView wvHour, wvMinute, wvAmPm;
	private View mView;
	private View vSelectionDividerTop, vSelectionDividerBottom;
	private int mHour, mMinute, mAmPm;
	private TextView tvItem1, tvItem2, tvItem3;
	private String mItemTitle1, mItemTitle2, mItemTitle3;
	private LayoutInflater mInflater;
	private WheelView.OnWheelViewListener mOnSelectedTimeListener;

	private boolean showUnit = true;
	private boolean isExpanded = false;
	private boolean isEnabled = true;
	private final String TIME_VALUE_FORMAT = "%02d";

	public Wheel12HourTimePicker(Context context) {
		super(context);
		mContext = context;
		_initView();
		setTime(0, 0);
	}

	public Wheel12HourTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_getAttrs(attrs);
		_initView();
		setTime(0, 0);
	}

	public Wheel12HourTimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_getAttrs(attrs, defStyle);
		_initView();
		setTime(0, 0);
	}

	private void _getAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WheelViewWidget);
		_setTypeArray(typedArray);
	}

	private void _getAttrs(AttributeSet attrs, int defStyle) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WheelViewWidget, defStyle, 0);
		_setTypeArray(typedArray);
	}

	private void _setTypeArray(TypedArray typedArray) {
		mItemTitle1 = typedArray.getString(R.styleable.WheelViewWidget_wvItem1);
		mItemTitle2 = typedArray.getString(R.styleable.WheelViewWidget_wvItem2);
		mItemTitle3 = typedArray.getString(R.styleable.WheelViewWidget_wvItem3);
	}

	private void _initView() {
		mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = mInflater.inflate(R.layout.widget_wheel_time_picker_5_rows, this, true);

		tvItem1 = (TextView)mView.findViewById(R.id.tv_widget_wheel_time_picker_item1_unit);
		tvItem2 = (TextView)mView.findViewById(R.id.tv_widget_wheel_time_picker_item2_unit);
		tvItem3 = (TextView)mView.findViewById(R.id.tv_widget_wheel_time_picker_item3_unit);

		tvItem1.setVisibility(View.GONE);
		tvItem2.setVisibility(View.GONE);
		tvItem3.setVisibility(View.GONE);

		wvHour = (WheelView) mView.findViewById(R.id.wv_widget_wheel_time_picker_item1);
		wvHour.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mHour = Integer.parseInt(item);
				if (mOnSelectedTimeListener != null) {
					mOnSelectedTimeListener.onSelectedTime(mHour, mMinute, mAmPm);
				}
			}
		});

		wvMinute = (WheelView) mView.findViewById(R.id.wv_widget_wheel_time_picker_item2);
		wvMinute.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mMinute = Integer.parseInt(item);
				if (mOnSelectedTimeListener != null) {
					mOnSelectedTimeListener.onSelectedTime(mHour, mMinute, mAmPm);
				}
			}
		});

		wvAmPm = (WheelView) mView.findViewById(R.id.wv_widget_wheel_time_picker_item3);
		wvAmPm.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mAmPm = selectedIndex;
				if (mOnSelectedTimeListener != null) {
					mOnSelectedTimeListener.onSelectedTime(mHour, mMinute, mAmPm);
				}
			}
		});

		vSelectionDividerTop = mView.findViewById(R.id.v_widget_wheel_time_picker_divider_top);
		vSelectionDividerBottom = mView.findViewById(R.id.v_widget_wheel_time_picker_divider_bottom);

		setWheelViewData();
	}

	public void showSelectionDivider(boolean show) {
		if (show) {
			vSelectionDividerTop.setVisibility(View.VISIBLE);
			vSelectionDividerBottom.setVisibility(View.VISIBLE);
		} else {
			vSelectionDividerTop.setVisibility(View.GONE);
			vSelectionDividerBottom.setVisibility(View.GONE);
		}
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
		wvHour.setEnabled(enabled);
		wvMinute.setEnabled(enabled);
		wvAmPm.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void onExpanded() {
		isExpanded = true;
		String strHour = String.format(TIME_VALUE_FORMAT, mHour);
		String strMinute = String.format(TIME_VALUE_FORMAT, mMinute);

		wvHour.setSelection(strHour);
		wvMinute.setSelection(strMinute);
		if (mAmPm == DateTimeUtil.HOUR_AM) {
			wvAmPm.setSelection(DateTimeUtil.HOUR_AM);
		} else {
			wvAmPm.setSelection(DateTimeUtil.HOUR_PM);
		}

		if (DBG) Log.d(TAG, "onExpanded : " + strHour + " / " + strMinute + " / " + mAmPm);
	}

	public void onCollapsed() {
		isExpanded = false;
		if (DBG) Log.d(TAG, "onCollapsed : " + mHour + " / " + mMinute + " / " + mAmPm);
	}

	public void setWheelViewData() {
		ArrayList<String> hourList = new ArrayList<>();
		for (int i = 1; i <= 12; i++) {
			hourList.add(String.format(TIME_VALUE_FORMAT, i));
		}
		wvHour.setItems(hourList);
		wvHour.setSelection(mHour);

		ArrayList<String> minuteList = new ArrayList<>();
		for (int i = 0; i < 60*3; i++) {
			minuteList.add(String.format(TIME_VALUE_FORMAT, i%60));
		}
		wvMinute.setItems(minuteList);
		wvMinute.setSelection(mMinute);

		ArrayList<String> secondList = new ArrayList<>();
		secondList.add(mContext.getString(R.string.time_am));
		secondList.add(mContext.getString(R.string.time_pm));
		wvAmPm.setItems(secondList);
		wvAmPm.setSelection(0);

		if (DBG) Log.d(TAG, "_initTimerWheelViewData : " + mHour + " / " + mMinute + " / " + mAmPm);
	}

	public void setOnSelectedTimeListener(WheelView.OnWheelViewListener listener) {
		mOnSelectedTimeListener = listener;
	}

	public void setTime(int hour, int minute) {
		setHour(hour);
		setMinute(minute);

		if (DBG) Log.d(TAG, "setTime: " + mHour + " / " + mMinute + " / " + mAmPm);

		wvHour.setSelection(mHour);
		wvMinute.setSelection(mMinute);
		wvAmPm.setSelection(mAmPm);

		if (isExpanded) {
			onExpanded();
		}
	}

	public void setTime(long utcTimeMs) {
		Date time = new Date(utcTimeMs);
		setTime(time.getHours(), time.getMinutes());
	}

	public void setHour(int hour) {
		mHour = hour;
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
	}

	public void setMinute(int minute) {
		mMinute = minute;
	}

	public void setAmPm(int ampm) {
		mAmPm = ampm;
	}
	
	public View getView() {
		return mView;
	}
}
