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
import java.util.Calendar;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class WheelNotationDatePicker extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "NotationPicker";
	private static final boolean DBG = Configuration.DBG;

	private Context mContext;
	private WheelView wvItem1, wvItem2, wvItem3;
	private WheelView wvYear, wvMonth, wvDay;
	private View mView;
	private int mYear, mMonth, mDay;
	private int mMaxYear, mMaxDay;
	private TextView tvItem1, tvItem2, tvItem3;
	private String mItemTitle1, mItemTitle2, mItemTitle3;
	private LayoutInflater mInflater;
	private WheelView.OnWheelViewListener mOnSelectedDateListener;
	private int mYearFrom;
	private ArrayList<String> day31List = new ArrayList<>();
	private ArrayList<String> day29List = new ArrayList<>();
	private ArrayList<String> day28List = new ArrayList<>();
	private ArrayList<String> day30List = new ArrayList<>();
	private boolean showDay = true;
	private int mNotationType;

	public WheelNotationDatePicker(Context context) {
		super(context);
		mContext = context;
		_initView();
		setTime(System.currentTimeMillis());
	}

	public WheelNotationDatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_getAttrs(attrs);
		_initView();
		setTime(System.currentTimeMillis());
	}

	public WheelNotationDatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_getAttrs(attrs, defStyle);
		_initView();
		setTime(System.currentTimeMillis());
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
		mNotationType = DateTimeUtil.getDateNotationType(Locale.getDefault().getLanguage());

		mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = mInflater.inflate(R.layout.widget_wheel_date_picker_5_rows, this, true);

		tvItem1 = (TextView)mView.findViewById(R.id.tv_widget_wheel_date_picker_item1_unit);
		tvItem2 = (TextView)mView.findViewById(R.id.tv_widget_wheel_date_picker_item2_unit);
		tvItem3 = (TextView)mView.findViewById(R.id.tv_widget_wheel_date_picker_item3_unit);

		wvItem1 = (WheelView) mView.findViewById(R.id.wv_widget_wheel_date_picker_item1);
		wvItem2 = (WheelView) mView.findViewById(R.id.wv_widget_wheel_date_picker_item2);
		wvItem3 = (WheelView) mView.findViewById(R.id.wv_widget_wheel_date_picker_item3);

		switch(mNotationType) {
			case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
				wvYear = wvItem1;
				wvMonth = wvItem2;
				wvDay = wvItem3;
				tvItem1.setText(getContext().getString(R.string.time_year_short));
				tvItem2.setText(getContext().getString(R.string.time_month_short));
				tvItem3.setText(getContext().getString(R.string.time_day_short));
				break;
			case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
				wvYear = wvItem3;
				wvMonth = wvItem1;
				wvDay = wvItem2;
				tvItem3.setText(getContext().getString(R.string.time_year_short));
				tvItem1.setText(getContext().getString(R.string.time_month_short));
				tvItem2.setText(getContext().getString(R.string.time_day_short));
				break;
			case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
				wvYear = wvItem3;
				wvMonth = wvItem2;
				wvDay = wvItem1;
				tvItem3.setText(getContext().getString(R.string.time_year_short));
				tvItem2.setText(getContext().getString(R.string.time_month_short));
				tvItem1.setText(getContext().getString(R.string.time_day_short));
				break;
		}

		wvYear.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mYear = mYearFrom + selectedIndex;
				_updateWheelViewDay();
				if (mOnSelectedDateListener != null) {
					mOnSelectedDateListener.onSelectedDate(mYear, mMonth, mDay);
				}
			}
		});

		wvMonth.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mMonth = selectedIndex + 1;
				_updateWheelViewDay();
				if (mOnSelectedDateListener != null) {
					mOnSelectedDateListener.onSelectedDate(mYear, mMonth, mDay);
				}
			}
		});

		wvDay.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onValueChanged(int selectedIndex, String item) {
				mDay = selectedIndex + 1;
				_updateWheelViewDay();
				if (mOnSelectedDateListener != null) {
					mOnSelectedDateListener.onSelectedDate(mYear, mMonth, mDay);
				}
			}
		});

		tvItem3.setVisibility(View.GONE);
		tvItem2.setVisibility(View.GONE);
		tvItem1.setVisibility(View.GONE);

		setWheelViewData(2000);
	}

	public void showDay(boolean show) {
		showDay = show;
		if (showDay) {
			wvDay.setVisibility(View.VISIBLE);
		} else {
			wvDay.setVisibility(View.GONE);
		}
	}

	public void onExpanded() {
		if (DBG) Log.d(TAG, "onExpanded : " + mYear + " / " + mMonth + " / " + mDay);
		//String strYear = mContext.getString(R.string.time_year_short);
		String strYear = "";
		if (mYear < 100) {
			wvYear.setSelection("20" + mYear + strYear);
		} else {
			wvYear.setSelection(mYear + strYear);
		}
		wvMonth.setSelection(mMonth - 1);
		wvDay.setSelection(mDay - 1);
	}

	public void onCollapsed() {
		if (DBG) Log.d(TAG, "onCollapsed : " + mYear + " / " + mMonth + " / " + mDay);
	}

	public void setWheelViewData(int fromYear) {
		int to = mMaxYear;
		mYearFrom = fromYear;
		int from = fromYear;

		//String strYear = mContext.getString(R.string.time_year_short);
		String strYear = "";
		ArrayList<String> yearList = new ArrayList<>();
		for (int i = from; i <= to; i++) {
			yearList.add(i + strYear);
		}
		wvYear.setItems(yearList);
		wvYear.setSelection(mYear);

		ArrayList<String> monthList = new ArrayList<>();
		for (int i = 1; i <= 12; i++) {
			monthList.add(DateTimeUtil.getMonthString(i));
		}
		wvMonth.setItems(monthList);
		wvMonth.setSelection(mMonth);

		//String strDay = mContext.getString(R.string.time_day_short);
		String strDay = "";
		day28List.clear();
		day29List.clear();
		day30List.clear();
		day31List.clear();
		for (int i = 1; i <= 31; i++) {
			if (i <= 28) day28List.add(i + strDay);
			if (i <= 29) day29List.add(i + strDay);
			if (i <= 30) day30List.add(i + strDay);
			day31List.add(i + strDay);
		}

		if (mMaxDay == 28) {
			wvDay.setItems(day28List);
		} else if (mMaxDay == 29) {
			wvDay.setItems(day29List);
		} else if (mMaxDay == 30) {
			wvDay.setItems(day30List);
		} else {
			wvDay.setItems(day31List);
		}

		wvDay.setSelection(mDay);

		if (DBG) Log.d(TAG, "_initWheelViewData : " + mYear + " / " + mMonth + " / " + mDay);
	}

	public void setOnSelectedDateListener(WheelView.OnWheelViewListener listener) {
		mOnSelectedDateListener = listener;
	}

	public void setTime(long time) {
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH) + 1;
		mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

		mMaxYear = mYear + 1;
		mCalendar.set(Calendar.MONTH, mMonth);
		mCalendar.set(Calendar.DAY_OF_MONTH, 0);
		mMaxDay = mCalendar.get(Calendar.DAY_OF_MONTH);
	}

	public void setYear(int year) {
		mYear = year;
	}

	public void setMonth(int month) {
		mMonth = month;
	}

	public void setDay(int day) {
		mDay = day;
	}

	private void _updateWheelViewDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, mYear);
		calendar.set(Calendar.MONTH, mMonth);
		calendar.set(Calendar.DAY_OF_MONTH, 0);

		int maxDay = calendar.get(Calendar.DAY_OF_MONTH);

		if (mMaxDay == maxDay) {
			if (DBG) Log.d(TAG, "_updateWheelViewDay RETURN : " + maxDay + " / " + mYear + " / " + mMonth + " / " + mDay);
			return;
		}
		if (DBG) Log.d(TAG, "_updateWheelViewDay " + mMaxDay + ":" + maxDay + " / " + mYear + " / " + mMonth + " / " + mDay);

		if (maxDay == 28) {
			wvDay.setItems(day28List);
		} else if (maxDay == 29) {
			wvDay.setItems(day29List);
		} else if (maxDay == 30) {
			wvDay.setItems(day30List);
		} else {
			wvDay.setItems(day31List);
		}

		if (mDay > maxDay) {
			wvDay.setSelection(maxDay);
			mDay = maxDay;
		} else {
			wvDay.setSelection(mDay - 1);
		}
		mMaxDay = maxDay;
	}
	
	public View getView() {
		return mView;
	}

	public long getDateTimeMs() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, mYear);
		calendar.set(Calendar.MONTH, mMonth - 1);
		calendar.set(Calendar.DAY_OF_MONTH, mDay);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
}
