package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

import static java.lang.System.currentTimeMillis;

public class TitlebarDatePicker extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "TBDatePicker";
	private static final boolean DBG = Configuration.DBG;

	private WheelNotationDatePicker wdpWheelView;
	private TextView tvTitle, tvContents;
	private ImageView ivDirection;
	private View emptyDivider;
	private LinearLayout lctnTitleBar;

	private int mYear, mMonth, mDay;
	private boolean isExpanded = false;
	private String mLocale;

	public TitlebarDatePicker(Context context) {
		super(context);
		_initView();
	}

	public TitlebarDatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
	}

	public TitlebarDatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
	}

	public void setExpanded(boolean expanded) {
		isExpanded = expanded;
		if (isExpanded) {
			emptyDivider.setVisibility(View.VISIBLE);
			ivDirection.setBackgroundResource(R.drawable.ic_direction_up_black);
			wdpWheelView.setVisibility(View.VISIBLE);
			wdpWheelView.onExpanded();
		} else {
			emptyDivider.setVisibility(View.GONE);
			ivDirection.setBackgroundResource(R.drawable.ic_direction_down_black);
			wdpWheelView.setVisibility(View.GONE);
			wdpWheelView.onCollapsed();
		}
	}

	private void _initView() {
		mLocale = Locale.getDefault().getLanguage();

		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_titlebar_date_picker, this, false);
		addView(v);

		tvTitle = (TextView)v.findViewById(R.id.tv_titlebar_date_picker_title);
		tvContents = (TextView)v.findViewById(R.id.tv_titlebar_date_picker_contents);
		ivDirection = (ImageView)v.findViewById(R.id.iv_titlebar_date_picker_direction);
		emptyDivider = (View)v.findViewById(R.id.v_titlebar_date_picker_empty_divider);
		lctnTitleBar = (LinearLayout)v.findViewById(R.id.lctn_titlebar_date_picker_title_bar);
		lctnTitleBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setExpanded(!isExpanded);
			}
		});

		wdpWheelView = (WheelNotationDatePicker)v.findViewById(R.id.wdp_titlebar_date_picker);
		wdpWheelView.setWheelViewData(2000);
		wdpWheelView.setTime(currentTimeMillis());
		wdpWheelView.setOnSelectedDateListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onSelectedDate(int year, int month, int day) {
				if (year < 100) {
					year += 2000;
				}
				mYear = year;
				mMonth = month;
				mDay = day;
				updateTitlebar();
			}
		});

		setExpanded(false);
	}

	public void updateTitlebar() {
		switch(DateTimeUtil.getDateNotationType(mLocale)) {
			case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
				setContents(mYear + mContext.getString(R.string.time_year_short) + " " + mMonth + mContext.getString(R.string.time_month_short) + " " + mDay + mContext.getString(R.string.time_day_short));
				break;
			case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
				setContents(DateTimeUtil.getMonthString(mMonth) + " " + mDay + ", " + mYear);
				break;
			case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
				setContents(mDay + " " + DateTimeUtil.getMonthString(mMonth) + ", " + mYear);
				break;
		}
	}

	public void setDateYYMMDD(String yyMMdd) {
		int year, month, day;
		if (yyMMdd == null || yyMMdd.length() != 6) {
			long now = System.currentTimeMillis();
			yyMMdd = DateFormat.format("yyMMdd", now).toString();
		}

		year = Integer.parseInt(yyMMdd.substring(0, 2));
		month = Integer.parseInt(yyMMdd.substring(2, 4));
		day = Integer.parseInt(yyMMdd.substring(4, 6));

		if (DBG) Log.d(TAG, "setDate: " + year + " / " + month + " / " + day);

		wdpWheelView.setYear(year);
		wdpWheelView.setMonth(month);
		wdpWheelView.setDay(day);

		mYear = 2000 + year;
		mMonth = month;
		mDay = day;

		updateTitlebar();
	}

	public void setTime(long utcTimeMs) {
		Date time = new Date(utcTimeMs);
		int year = time.getYear();
		if (year > 100) {
			year = year % 100;
		}

		int month = time.getMonth() + 1;
		int day = time.getDate();

		String localDateYYMMDD = String.format("%02d%02d%02d", year, month, day);
		if (DBG) Log.d(TAG, "setTime: " + localDateYYMMDD);
		setDateYYMMDD(localDateYYMMDD);
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

	public String getSelectedDateStringYYYYMMDD() {
		String dateString = "";
		if (mYear < 100) {
			mYear += 2000;
		}
		dateString = String.format("%04d%02d%02d", mYear, mMonth, mDay);
		return dateString;
	}

	public String getSelectedDateStringYYMMDD() {
		String dateString = "";
		if (mYear > 100) {
			mYear = mYear % 100;
		}
		dateString = String.format("%02d%02d%02d", mYear, mMonth, mDay);
		return dateString;
	}
}