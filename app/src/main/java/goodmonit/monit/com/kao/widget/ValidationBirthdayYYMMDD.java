package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

import static java.lang.System.currentTimeMillis;

public class ValidationBirthdayYYMMDD extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidBirthday";

	private WheelNotationDatePicker wdpWheelView;
	private View vDividerExpand;

	private TextView tvTextContents;
	private int mYear, mMonth, mDay;
	private boolean showDay = true;
	private String mLocale;

	public ValidationBirthdayYYMMDD(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public ValidationBirthdayYYMMDD(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationBirthdayYYMMDD(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		mLocale = Locale.getDefault().getLanguage();

		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_birthday, this, false);
		addView(v);

		tvTextContents = (TextView)v.findViewById(R.id.tv_validation_birthday_text);
		tvTextContents.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showWheelPicker(wdpWheelView.getVisibility() == View.GONE);
				if (mClickListener != null) {
					mClickListener.onClick(v);
				}
			}
		});
		tvTextContents.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimaryLight));

		tvTitle = (TextView) v.findViewById(R.id.tv_validation_birthday_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_birthday_warning);
		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_birthday_checked);
		vUnderline = v.findViewById(R.id.v_validation_birthday_underline);
		tvWarning.setVisibility(View.GONE);

		vDividerExpand = v.findViewById(R.id.v_validation_birthday_expand);

		wdpWheelView = (WheelNotationDatePicker)v.findViewById(R.id.wdp_validation_birthday);
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
                updateTextView();
			}
		});

		wdpWheelView.setVisibility(View.GONE);
		vDividerExpand.setVisibility(View.GONE);
	}

	public void updateTextView() {
        if (showDay) {
            switch(DateTimeUtil.getDateNotationType(mLocale)) {
                case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
                    setText(mYear + mContext.getString(R.string.time_year_short) + " " + mMonth + mContext.getString(R.string.time_month_short) + " " + mDay + mContext.getString(R.string.time_day_short));
                    break;
                case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
                    setText(DateTimeUtil.getMonthString(mMonth) + " " + mDay + ", " + mYear);
                    break;
                case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
                    setText(mDay + " " + DateTimeUtil.getMonthString(mMonth) + ", " + mYear);
                    break;
            }
        } else {
            switch(DateTimeUtil.getDateNotationType(mLocale)) {
                case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
                    setText(mYear + mContext.getString(R.string.time_year_short) + " " + mMonth + mContext.getString(R.string.time_month_short));
                    break;
                case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
                    setText(DateTimeUtil.getMonthString(mMonth) + ", " + mYear);
                    break;
                case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
                    setText(DateTimeUtil.getMonthString(mMonth) + ", " + mYear);
                    break;
            }
        }
	}

	public void showDay(boolean show) {
		showDay = show;
		wdpWheelView.showDay(show);
	}

	public void setBirthDayYYMMDD(String yyMMdd) {
		int year, month, day;
		if (yyMMdd == null || yyMMdd.length() != 6) {
			long now = System.currentTimeMillis();
			yyMMdd = DateFormat.format("yyMMdd", now).toString();
		}

		year = Integer.parseInt(yyMMdd.substring(0, 2));
		month = Integer.parseInt(yyMMdd.substring(2, 4));
		day = Integer.parseInt(yyMMdd.substring(4, 6));

		wdpWheelView.setYear(year);
		wdpWheelView.setMonth(month);
		wdpWheelView.setDay(day);

		mYear = 2000 + year;
		mMonth = month;
		mDay = day;

		updateTextView();
	}
	/*
	public void setBirthday(String birthdayYYYYMMDD) {
		int year, month, day;
		if (birthdayYYYYMMDD == null || birthdayYYYYMMDD.length() < 6) {
			long now = System.currentTimeMillis();
			birthdayYYYYMMDD = DateFormat.format("yyMMdd", now).toString();
		}

		if (!birthdayYYYYMMDD.startsWith("20")) {
			birthdayYYYYMMDD = "20" + birthdayYYYYMMDD;
		}

		if (birthdayYYYYMMDD.length() < 8) {
			birthdayYYYYMMDD = birthdayYYYYMMDD + "01";
		}

		year = Integer.parseInt(birthdayYYYYMMDD.substring(0, 4));
		month = Integer.parseInt(birthdayYYYYMMDD.substring(4, 6));
		day = Integer.parseInt(birthdayYYYYMMDD.substring(6));
		wdpWheelView.setYear(year);
		wdpWheelView.setMonth(month);
		wdpWheelView.setDay(day);

		mYear = year;
		mMonth = month;
		mDay = day;

		//setText(mYear + mContext.getString(R.string.time_year_short) + " " + mMonth + mContext.getString(R.string.time_month_short) + " " + mDay + mContext.getString(R.string.time_day_short));

		setText(mYear + mContext.getString(R.string.time_year_short) + " " + DateTimeUtil.getMonthString(mMonth) + " " + mDay + mContext.getString(R.string.time_day_short));
	}
	*/
	private void _setView() {
		showUnderLine(showUnderline);
	}

	private void _getAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValidationWidget);
		_setTypeArray(typedArray);
	}

	private void _getAttrs(AttributeSet attrs, int defStyle) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValidationWidget, defStyle, 0);
		_setTypeArray(typedArray);
	}

	private void _setTypeArray(TypedArray typedArray) {
		tvTitle.setText(
				typedArray.getString(R.styleable.ValidationWidget_textTitle));
		tvWarning.setText(
				typedArray.getString(R.styleable.ValidationWidget_textWarning));
		tvTextContents.setText(
				typedArray.getString(R.styleable.ValidationWidget_textContents));

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);
	}

	public void setText(String text) {
		tvTextContents.setText(text);
		tvTextContents.setTextColor(getContext().getResources().getColor(R.color.colorTextPositive));
		if (mValidationUpdateListener != null) {
			mValidationUpdateListener.updateValidation();
		} else {
			if (text == null || text.length() == 0) {
				setValid(false);
			} else {
				setValid(true);
			}
		}
	}

	public void setBirthdayFromYear(int fromYear) {
		wdpWheelView.setWheelViewData(fromYear);
	}

	public String getText() {
		return tvTextContents.getText().toString();
	}

	public String getSelectedDateStringYYYYMMDD() {
		String dateString = "";

		if (mYear < 100) {
			dateString = "20" + mYear;
		} else {
			dateString = "" + mYear;
		}

		if (mMonth < 10) {
			dateString += "0" + mMonth;
		} else {
			dateString += mMonth;
		}

		if (mDay < 10) {
			dateString += "0" + mDay;
		} else {
			dateString += mDay;
		}

		return dateString;
	}

	public String getSelectedDateStringYYMMDD() {
		String dateString = "";

		if (mYear > 100) {
			dateString = "" + (mYear % 100);
		} else {
			dateString = "" + mYear;
		}

		if (mMonth < 10) {
			dateString += "0" + mMonth;
		} else {
			dateString += mMonth;
		}

		if (mDay < 10) {
			dateString += "0" + mDay;
		} else {
			dateString += mDay;
		}

		return dateString;
	}

	public TextView getTextView() {
		return tvTextContents;
	}

	public void setOnClickListener(OnClickListener listener) {
		tvTextContents.setOnClickListener(listener);
	}

	public void showWheelPicker(boolean show) {
		if (show) {
			wdpWheelView.setVisibility(View.VISIBLE);
			vDividerExpand.setVisibility(View.VISIBLE);
			wdpWheelView.onExpanded();
		} else {
			wdpWheelView.setVisibility(View.GONE);
			vDividerExpand.setVisibility(View.GONE);
			wdpWheelView.onCollapsed();
		}
	}
}