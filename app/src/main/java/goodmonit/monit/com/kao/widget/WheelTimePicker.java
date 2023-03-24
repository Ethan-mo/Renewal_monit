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

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class WheelTimePicker extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "WheelTimePicker";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;
	private WheelView wvHour, wvMinute, wvSecond;
	private View mView;
	private View vSelectionDividerTop, vSelectionDividerBottom;
	private int mHour, mMinute, mSecond;
	private TextView tvItem1, tvItem2, tvItem3;
	private String mItemTitle1, mItemTitle2, mItemTitle3;
	private LayoutInflater mInflater;
	private WheelView.OnWheelViewListener mOnSelectedTimeListener;
	private boolean showSecond = false;
	private boolean isExpanded = false;
	private boolean isEnabled = true;
	private final String TIME_VALUE_FORMAT = "%02d";

	public WheelTimePicker(Context context) {
		super(context);
		mContext = context;
		_initView();
		setTime(0, 0, 0);
	}

	public WheelTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_getAttrs(attrs);
		_initView();
		setTime(0, 0, 0);
	}

	public WheelTimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_getAttrs(attrs, defStyle);
		_initView();
		setTime(0, 0, 0);
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
		mView = mInflater.inflate(R.layout.widget_wheel_time_picker_3_rows, this, true);

		tvItem1 = (TextView)mView.findViewById(R.id.tv_widget_wheel_time_picker_item1_unit);
		tvItem2 = (TextView)mView.findViewById(R.id.tv_widget_wheel_time_picker_item2_unit);
		tvItem3 = (TextView)mView.findViewById(R.id.tv_widget_wheel_time_picker_item3_unit);

		tvItem1.setText(getContext().getString(R.string.time_hour_short));
		tvItem2.setText(getContext().getString(R.string.time_minute_short));
		tvItem3.setText(getContext().getString(R.string.time_second_short));

		wvHour = (WheelView) mView.findViewById(R.id.wv_widget_wheel_time_picker_item1);
		wvHour.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mHour = Integer.parseInt(item);
				if (mOnSelectedTimeListener != null) {
					mOnSelectedTimeListener.onSelectedTime(mHour, mMinute, mSecond);
				}
			}
		});

		wvMinute = (WheelView) mView.findViewById(R.id.wv_widget_wheel_time_picker_item2);
		wvMinute.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mMinute = Integer.parseInt(item);
				if (mOnSelectedTimeListener != null) {
					mOnSelectedTimeListener.onSelectedTime(mHour, mMinute, mSecond);
				}
			}
		});

		wvSecond = (WheelView) mView.findViewById(R.id.wv_widget_wheel_time_picker_item3);
		wvSecond.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mSecond = Integer.parseInt(item);
				if (mOnSelectedTimeListener != null) {
					mOnSelectedTimeListener.onSelectedTime(mHour, mMinute, mSecond);
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
		wvSecond.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void showSecond(boolean show) {
		showSecond = show;
		if (showSecond) {
			wvSecond.setVisibility(View.VISIBLE);
			tvItem3.setVisibility(View.VISIBLE);
		} else {
			wvSecond.setVisibility(View.GONE);
			tvItem3.setVisibility(View.GONE);
		}
	}

	public void onExpanded() {
		isExpanded = true;
		String strHour = String.format(TIME_VALUE_FORMAT, mHour);
		String strMinute = String.format(TIME_VALUE_FORMAT, mMinute);
		String strSecond = String.format(TIME_VALUE_FORMAT, mSecond);
		if (DBG) Log.d(TAG, "onExpanded : " + strHour + " / " + strMinute + " / " + strSecond);
		wvHour.setSelection(strHour);
		wvMinute.setSelection(strMinute);
		wvSecond.setSelection(strSecond);
	}

	public void onCollapsed() {
		isExpanded = false;
		if (DBG) Log.d(TAG, "onCollapsed : " + mHour + " / " + mMinute + " / " + mSecond);
	}

	public void setWheelViewData() {
		ArrayList<String> hourList = new ArrayList<>();
		for (int i = 0; i <= 23; i++) {
			hourList.add(String.format(TIME_VALUE_FORMAT, i));
		}
		wvHour.setItems(hourList);
		wvHour.setSelection(mHour);

		ArrayList<String> minuteList = new ArrayList<>();
		for (int i = 0; i <= 5; i++) {
			minuteList.add(String.format(TIME_VALUE_FORMAT, i * 10));
		}
		wvMinute.setItems(minuteList);
		wvMinute.setSelection(mMinute);

		ArrayList<String> secondList = new ArrayList<>();
		for (int i = 0; i <= 59; i++) {
			secondList.add(String.format(TIME_VALUE_FORMAT, i));
		}
		wvSecond.setItems(secondList);
		wvSecond.setSelection(mSecond);

		if (DBG) Log.d(TAG, "_initWheelViewData : " + mHour + " / " + mMinute + " / " + mSecond);
	}

	public void setOnSelectedTimeListener(WheelView.OnWheelViewListener listener) {
		mOnSelectedTimeListener = listener;
	}

	public void setTime(int hour, int minute, int second) {
		mHour = hour;
		mMinute = minute;
		mSecond = second;
		if (isExpanded) {
			onExpanded();
		}
	}

	public void setHour(int hour) {
		mHour = hour;
	}

	public void setMinute(int minute) {
		mMinute = minute;
	}

	public void setSecond(int second) {
		mSecond = second;
	}
	
	public View getView() {
		return mView;
	}

	public int getTimeTotalSecond() {
		return (mHour * 60 * 60) + (mMinute * 60) + (mSecond);
	}
}
