package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class GraphViewEnvironment extends View {
	private static final String TAG = Configuration.BASE_TAG + "EnvView";
	private static final boolean DBG = Configuration.DBG;
    private Context mContext;

	public static final int GRAPH_TYPE_SCORE 		= 0;
	public static final int GRAPH_TYPE_TEMPERATURE 	= 1;
	public static final int GRAPH_TYPE_HUMIDITY 	= 2;
	public static final int GRAPH_TYPE_VOC 			= 3;

	public static final int GRAPH_MODE_NO_DATA		= 1;
	public static final int GRAPH_MODE_SAME_DATA	= 2;
	public static final int GRAPH_MODE_NORMAL_DATA	= 3;

	private static final int SEPARATE_SECTION = 14;
	private static final int DEFAULT_POINT_RADIUS = 8;

	private static final double DEFAULT_MIDDLE_TEMPERATURE_CELSIUS = 22.5;
	private static final double DEFAULT_MIDDLE_TEMPERATURE_FAHRENHEIT = 72.5;

	private int mGraphType = GRAPH_TYPE_SCORE;
	private float mMaxTemperature, mMinTemperature;
	private float mMaxHumidity, mMinHumidity;
	private float mMaxVoc;

	/**
	 * Related to draw object
	 */
	private Paint mGuideLinePaint, mTimePaint, mLineGraphPaint, mLineGraphWarningPaint, mSelectedPointPaint, mSelectedLineGraphPaint;
	private float mTimeTextSize;

	/**
	 * Related to Coordinations
	 */
	private int mWidth, mHeight;
	private int mMaxGraphHeight, mMinGraphHeight;
	private int mMaxLineHeight, mMinLineHeight;
	private int mTouchedX;
	private float mWidthBetweenGuideLine;
	private float mGraphWidth;
	private float mWidthBetweenMinute;
	private float mTemperatureHeightForPoint1;
	private float mHumidityHeightForPoint1;
	private float mVocHeightForPoint1;

	/**
	 * Related to Values
	 */
	private double mMaxValue, mMinValue;
	private int mMaxIndex, mMinIndex;
	private long mBeginTimeMs;

	private ArrayList<Double> mValues;
	private TextView tvMaxValue, tvMinValue, tvNoData;
	private TextView tvCurrentTime, tvCurrentValue, tvAverageValue;
	private RelativeLayout rctnDetailSection;
	private TextView tvDetailDate;

	private int mGraphMode = GRAPH_MODE_NORMAL_DATA;

	private int mCntTemperatureHeightSection = 650;
	private double mMiddleTemperatureValue = DEFAULT_MIDDLE_TEMPERATURE_CELSIUS;

	private int mCntHumidityHeightSection = 1000;
	private double mMiddleHumidityValue = 50.0;

	private int mCntVocHeightSection = 1000;

	public GraphViewEnvironment(Context context) {
		super(context);
		mContext = context;
		init();
	}

    public GraphViewEnvironment(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
		init();
    }

    public void init() {
		mTouchedX = -1;
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTouchedX = (int) event.getX();

				if ((mTouchedX > mWidthBetweenGuideLine) && (mTouchedX < mWidth - mWidthBetweenGuideLine)) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							invalidate();
							return true;
						case MotionEvent.ACTION_MOVE:
							invalidate();
							return true;
					}
				}
				return false;
			}
		});

		mValues = new ArrayList<Double>();

		/*
         * Guide Line Paint
         */
		mGuideLinePaint = new Paint();
		mGuideLinePaint.setStyle(Paint.Style.STROKE);
		mGuideLinePaint.setColor(Color.GRAY);
		mGuideLinePaint.setStrokeWidth(1);
		mGuideLinePaint.setAntiAlias(true);

        /*
         * Time Paint
         */
		mTimeTextSize = 12 * getResources().getDisplayMetrics().scaledDensity;
		mTimePaint = new Paint();
		mTimePaint.setColor(getResources().getColor(R.color.colorTextPrimaryLight));
		mTimePaint.setTextSize(mTimeTextSize);
		mTimePaint.setTextAlign(Align.CENTER);
		mTimePaint.setAntiAlias(true);

		/*
         * Line Graph Paint
         */
		mLineGraphPaint = new Paint();
		mLineGraphPaint.setStyle(Paint.Style.STROKE);
		mLineGraphPaint.setStrokeWidth(4);
		mLineGraphPaint.setAntiAlias(true);
		mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));

		mLineGraphWarningPaint = new Paint();
		mLineGraphWarningPaint.setStyle(Paint.Style.STROKE);
		mLineGraphWarningPaint.setStrokeWidth(4);
		mLineGraphWarningPaint.setAntiAlias(true);
		mLineGraphWarningPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));

		mSelectedLineGraphPaint = new Paint();
		mSelectedLineGraphPaint.setStyle(Paint.Style.STROKE);
		mSelectedLineGraphPaint.setAntiAlias(true);
		mSelectedLineGraphPaint.setColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
		mSelectedLineGraphPaint.setStrokeWidth(1);

		mSelectedPointPaint = new Paint();
		mSelectedPointPaint.setAntiAlias(true);
		mSelectedPointPaint.setColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
	}

	public void setTemperatureScale(int scale) {
		if (scale == 0) { // Celsius
			mMiddleTemperatureValue = DEFAULT_MIDDLE_TEMPERATURE_CELSIUS;
		} else if (scale == 1) { // Fahrenheit
			mMiddleTemperatureValue = DEFAULT_MIDDLE_TEMPERATURE_FAHRENHEIT;
		}
	}
	public void setCurrentTimeTextView(TextView tv) {
		tvCurrentTime = tv;
	}

	public void setCurrentValueTextView(TextView tv) {
		tvCurrentValue = tv;
	}

	public void setMaxValueTextView(TextView tv) {
		tvMaxValue = tv;
	}

	public void setMinValueTextView(TextView tv) {
		tvMinValue = tv;
	}

	public void setNoDataTextView(TextView tv) {
		tvNoData = tv;
	}

	public void setAverageValueTextView(TextView tv) {
		tvAverageValue = tv;
	}

	public void setDetailSection(RelativeLayout layout) {
		rctnDetailSection = layout;
	}

	public void setDetailDateTextView(TextView tv) {
		tvDetailDate = tv;
	}

	public void setBeginTimeMs(long timeMs) {
		mBeginTimeMs = timeMs;
	}

	public void setHumidityThreshold(float max, float min) {
		mMaxHumidity = max;
		mMinHumidity = min;
	}

	public void setTemperatureThreshold(float max, float min) {
		mMaxTemperature = max;
		mMinTemperature = min;
	}

	public void setVocThreshold(float bad) {
		mMaxVoc = 150;
	}

	public void setGraphType(int type) {
		mGraphType = type;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		mMaxGraphHeight = 0;
		mMinGraphHeight = (int)(mHeight * 0.9);
		mMaxLineHeight = (int)(mMinGraphHeight * 0.125);
		mMinLineHeight = (int)(mMinGraphHeight * 0.875);
		mWidthBetweenGuideLine = (int)(mWidth / SEPARATE_SECTION);
		mGraphWidth = mWidth - mWidthBetweenGuideLine * 2;
		mWidthBetweenMinute = mGraphWidth / mValues.size();
		/*
		if (DBG) Log.d(TAG, "onSizeChagned : " + mWidth + " / "
				+ mHeight + " / "
				+ mWidthBetweenGuideLine + " / "
				+ mGraphWidth + " / "
				+ mWidthBetweenMinute + " / "
				+ mMinGraphHeight + " / "
				+ mMaxLineHeight + " / "
				+ mMinLineHeight);
		*/
		mTemperatureHeightForPoint1 = (mMinLineHeight - mMaxLineHeight) * 1.0f / mCntTemperatureHeightSection;
		mHumidityHeightForPoint1 = (mMinLineHeight - mMaxLineHeight) * 1.0f / mCntHumidityHeightSection;
		mVocHeightForPoint1 = (mMinLineHeight - mMaxLineHeight) * 1.0f / mCntVocHeightSection;
	}

	public void setValues(ArrayList<Double> values) {
		mMaxValue = -999;
		mMinValue = 999;
		mMaxIndex = 0;
		mMinIndex = 0;
		mValues = values;

		int idx = -1;
		for (Double d : values) {
			idx++;
			if (d == -999) continue;
			if (d > mMaxValue) {
				mMaxValue = d;
				mMaxIndex = idx;
			}
			if (d < mMinValue) {
				mMinValue = d;
				mMinIndex = idx;
			}
		}

		if (mMaxValue == -999 || mMinValue == 999) { // 데이터가 없는 경우
			mGraphMode = GRAPH_MODE_NO_DATA;
			tvCurrentValue.setText("-");
			tvAverageValue.setText("-");
		} else if (mMaxValue == mMinValue) { // 모든 값이 동일한 경우
			mGraphMode = GRAPH_MODE_SAME_DATA;
		} else {
			mGraphMode = GRAPH_MODE_NORMAL_DATA;
		}

		mTouchedX = -1;
		rctnDetailSection.setVisibility(View.GONE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw GuideLine
		_drawVerticalGuideLine(canvas);
		_drawTimeText(canvas);

		switch(mGraphMode) {
			case GRAPH_MODE_NO_DATA:
				tvNoData.setVisibility(View.VISIBLE);
				tvMaxValue.setVisibility(View.GONE);
				tvMinValue.setVisibility(View.GONE);
				break;
			case GRAPH_MODE_SAME_DATA:
				tvNoData.setVisibility(View.GONE);
				tvMaxValue.setVisibility(View.VISIBLE);
				tvMinValue.setVisibility(View.GONE);
				if (mValues.size() > 0) {
					_drawLineGraph(canvas);
					_drawMinMaxValue();
					_drawSelectedInfo(canvas);
				}
				break;
			case GRAPH_MODE_NORMAL_DATA:
				tvNoData.setVisibility(View.GONE);
				tvMaxValue.setVisibility(View.VISIBLE);
				tvMinValue.setVisibility(View.VISIBLE);
				if (mValues.size() > 0) {
					_drawLineGraph(canvas);
					_drawMinMaxValue();
					_drawSelectedInfo(canvas);
				}
				break;
		}
	}

	private void _drawSelectedInfo(Canvas canvas) {
		if (mTouchedX > -1) {
			int idx = (int)((mTouchedX - mWidthBetweenGuideLine) / (mWidth - mWidthBetweenGuideLine * 2) * mValues.size());

			//int hour = idx / 6;
			//int min = (idx % 6) * 10;
			//canvas.drawText(mValues.get(idx) + "\n" + hour + ":" + min, mTouchedX, mMaxLineHeight / 2, mTimePaint);
			double curr = mValues.get(idx);

			updateCurrentValueTextView(curr);

			Path path = new Path();
			path.moveTo(mTouchedX, mMaxGraphHeight);
			path.lineTo(mTouchedX, mMinGraphHeight);
			canvas.drawPath(path, mSelectedLineGraphPaint);

			float nowHeight = 0;
			switch(mGraphType) { // 그래프 Y좌표
				case GRAPH_TYPE_TEMPERATURE:
					nowHeight = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleTemperatureValue - curr) * 10) * mTemperatureHeightForPoint1);
					break;
				case GRAPH_TYPE_HUMIDITY:
					nowHeight = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleHumidityValue - curr) * 10) * mHumidityHeightForPoint1);
					break;
				case GRAPH_TYPE_VOC:
					nowHeight = (float)(mMinLineHeight - curr * mVocHeightForPoint1);
					if (nowHeight < mMaxLineHeight) {
						nowHeight = mMaxLineHeight;
					}
					break;
				default:
					nowHeight = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (curr - mMinValue) / (mMaxValue - mMinValue)));
					break;
			}
			RectF oval = new RectF(mTouchedX - DEFAULT_POINT_RADIUS, nowHeight - DEFAULT_POINT_RADIUS, mTouchedX + DEFAULT_POINT_RADIUS, nowHeight + DEFAULT_POINT_RADIUS);
			canvas.drawOval(oval, mSelectedPointPaint);

			rctnDetailSection.setVisibility(View.VISIBLE);

			long nowMs = mBeginTimeMs + (long)idx * 1000 * 60 * 10; // 10분
			tvDetailDate.setText(DateTimeUtil.getDateTimeStringWithDay(nowMs, Locale.getDefault().getLanguage()));

		} else {
			rctnDetailSection.setVisibility(View.GONE);
		}
	}

	public void updateCurrentValueTextView(double curr) {
		if (curr == -999) {
			tvCurrentValue.setText("-");
			tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
		} else {
			tvCurrentValue.setText(curr + "");
			switch(mGraphType) {
				case GRAPH_TYPE_SCORE:
					if (curr < 50) {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
					} else if (curr < 70) {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow70));
					} else if (curr < 90) {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow90));
					} else {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
					}
					break;
				case GRAPH_TYPE_TEMPERATURE:
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
						if (curr >= mMaxTemperature) {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextWarning));
						} else if (curr <= mMinTemperature) {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextWarningBlue));
						} else {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorPrimary));
						}
					} else {
						if (curr >= mMaxTemperature || curr <= mMinTemperature) {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
						} else {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
						}
					}
					break;
				case GRAPH_TYPE_HUMIDITY:
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
						if (curr >= mMaxHumidity) {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
						} else if (curr <= mMinHumidity) {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
						} else {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorPrimary));
						}
					} else {
						if (curr >= mMaxHumidity || curr <= mMinHumidity) {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
						} else {
							tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
						}
					}
					break;
				case GRAPH_TYPE_VOC:
					if (curr > 300) {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
						tvCurrentValue.setText(getContext().getString(R.string.device_environment_voc_very_bad));
					} else if (curr > 150) {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow70));
						tvCurrentValue.setText(getContext().getString(R.string.device_environment_voc_not_good));
					} else if (curr > 50) {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow90));
						tvCurrentValue.setText(getContext().getString(R.string.device_environment_voc_normal));
					} else {
						tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
						tvCurrentValue.setText(getContext().getString(R.string.device_environment_voc_good));
					}
					break;
				default:
					tvCurrentValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
					break;
			}
		}
	}

	private void _drawLineGraph(Canvas canvas) {
		float beforeHeight = 0;
		float nowHeight = 0;

		// 그래프 시작 좌표 구하기
		switch(mGraphType) { // 그래프 시작 Y좌표
			case GRAPH_TYPE_TEMPERATURE:
				beforeHeight = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleTemperatureValue - mValues.get(0)) * 10) * mTemperatureHeightForPoint1);
				break;
			case GRAPH_TYPE_HUMIDITY:
				beforeHeight = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleHumidityValue - mValues.get(0)) * 10) * mHumidityHeightForPoint1);
				break;
			case GRAPH_TYPE_VOC:
				beforeHeight = (float)(mMinLineHeight - mValues.get(0) * mVocHeightForPoint1);
				if (beforeHeight < mMaxLineHeight) {
					beforeHeight = mMaxLineHeight;
				}
				break;
			default:
				beforeHeight = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (mValues.get(0) - mMinValue) / (mMaxValue - mMinValue)));
				break;
		}
		float width = mWidth / SEPARATE_SECTION;
		double prev = -1;

		Path normalPath = new Path();
		Path warningPath = new Path();
		Path greenPath = new Path();
		Path orangePath = new Path();
		for (Double curr : mValues) {
			if (curr == -999) {
				prev = -999;
				width += mWidthBetweenMinute;
				continue;
			}

			switch(mGraphType) { // 그래프 Y좌표
				case GRAPH_TYPE_TEMPERATURE:
					nowHeight = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleTemperatureValue - curr) * 10) * mTemperatureHeightForPoint1);
					break;
				case GRAPH_TYPE_HUMIDITY:
					nowHeight = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleHumidityValue - curr) * 10) * mHumidityHeightForPoint1);
					break;
				case GRAPH_TYPE_VOC:
					nowHeight = (float)(mMinLineHeight - curr * mVocHeightForPoint1);
					if (nowHeight < mMaxLineHeight) {
						nowHeight = mMaxLineHeight;
					}
					break;
				default:
					nowHeight = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (curr - mMinValue) / (mMaxValue - mMinValue)));
					break;
			}
			if (DBG) Log.d(TAG, " _drawLineGraph start : " + nowHeight + " / " + mMaxValue + " / " + mMinValue + " / " + curr);

			switch(mGraphType) {
				case GRAPH_TYPE_SCORE:
					if (curr < 50 || prev < 50 && prev > 0) {
						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
					} else if (curr < 70 || prev < 70 && prev > 0) {
						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow70));
					} else if (curr < 90 || prev < 90 && prev > 0) {
						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow90));
					} else {
						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
					}
					break;
				case GRAPH_TYPE_TEMPERATURE:
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
						if (curr >= mMaxTemperature || (prev >= mMaxTemperature && prev > 0)) {
							mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextWarning));
						} else if (curr <= mMinTemperature || (prev <= mMinTemperature && prev > 0)) {
							mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextWarningBlue));
						} else {
							mLineGraphPaint.setColor(getResources().getColor(R.color.colorPrimary));
						}
					} else {
						if (curr >= mMaxTemperature || curr <= mMinTemperature || (prev >= mMaxTemperature || prev <= mMinTemperature) && prev > 0) {
							warningPath.moveTo(width, beforeHeight);
							warningPath.lineTo(width + mWidthBetweenMinute, nowHeight);
							//mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
						} else {
							if (prev == -999) {
								normalPath.moveTo(width, nowHeight);
							} else {
								normalPath.moveTo(width, beforeHeight);
							}
							normalPath.lineTo(width + mWidthBetweenMinute, nowHeight);
							//mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
						}
					}
					break;
				case GRAPH_TYPE_HUMIDITY:
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
						if (curr >= mMaxHumidity || (prev >= mMaxHumidity && prev > 0)) {
							mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextWarningOrange));
						} else if (curr <= mMinHumidity || (prev <= mMinHumidity && prev > 0)) {
							mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextWarningOrange));
						} else {
							mLineGraphPaint.setColor(getResources().getColor(R.color.colorPrimary));
						}
					} else {
						if (curr >= mMaxHumidity || curr <= mMinHumidity || (prev >= mMaxHumidity || prev <= mMinHumidity) && prev > 0) {
							warningPath.moveTo(width, beforeHeight);
							warningPath.lineTo(width + mWidthBetweenMinute, nowHeight);
							//mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
						} else {
							if (prev == -999) {
								normalPath.moveTo(width, nowHeight);
							} else {
								normalPath.moveTo(width, beforeHeight);
							}
							normalPath.lineTo(width + mWidthBetweenMinute, nowHeight);
							//mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
						}
					}
					break;
				case GRAPH_TYPE_VOC:
					if (curr > 300) {
						if (prev == -999) {
							warningPath.moveTo(width, nowHeight);
						} else {
							warningPath.moveTo(width, beforeHeight);
						}
						warningPath.lineTo(width + mWidthBetweenMinute, nowHeight);
					} else if (curr > 150) {
						if (prev == -999) {
							orangePath.moveTo(width, nowHeight);
						} else {
							orangePath.moveTo(width, beforeHeight);
						}
						orangePath.lineTo(width + mWidthBetweenMinute, nowHeight);
					} else if (curr > 50) {
						if (prev == -999) {
							greenPath.moveTo(width, nowHeight);
						} else {
							greenPath.moveTo(width, beforeHeight);
						}
						greenPath.lineTo(width + mWidthBetweenMinute, nowHeight);
					} else {
						if (prev == -999) {
							normalPath.moveTo(width, nowHeight);
						} else {
							normalPath.moveTo(width, beforeHeight);
						}
						normalPath.lineTo(width + mWidthBetweenMinute, nowHeight);
					}
//					if (curr >= mMaxVoc || prev >= mMaxVoc && prev > 0) {
//						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
//					} else {
//						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
//					}
					break;
				default:
//					mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
					break;
			}

			width += mWidthBetweenMinute;

			prev = curr;
			beforeHeight = nowHeight;
		}

		switch(mGraphType) {
			case GRAPH_TYPE_SCORE:
				break;
			case GRAPH_TYPE_TEMPERATURE:
				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
				canvas.drawPath(normalPath, mLineGraphPaint);

				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
				canvas.drawPath(warningPath, mLineGraphPaint);
				break;
			case GRAPH_TYPE_HUMIDITY:
				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
				canvas.drawPath(normalPath, mLineGraphPaint);

				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
				canvas.drawPath(warningPath, mLineGraphPaint);
				break;
			case GRAPH_TYPE_VOC:
				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow100));
				canvas.drawPath(normalPath, mLineGraphPaint);

				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow70));
				canvas.drawPath(orangePath, mLineGraphPaint);

				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow50));
				canvas.drawPath(warningPath, mLineGraphPaint);

				mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextScoreBelow90));
				canvas.drawPath(greenPath, mLineGraphPaint);
				break;
			default:
				break;
		}

	}

	private void _drawMinMaxValue() {
		if (mGraphMode == GRAPH_MODE_NO_DATA) {
			return;
		}

		tvMaxValue.setText(mMaxValue + "");
		tvMinValue.setText(mMinValue + "");

		float maxValueX = mWidthBetweenGuideLine + (mMaxIndex + 1) * mWidthBetweenMinute - tvMaxValue.getWidth() / 2.0f;
		float minValueX = mWidthBetweenGuideLine + (mMinIndex + 1) * mWidthBetweenMinute - tvMaxValue.getWidth() / 2.0f;
		float maxValueY = 0;
		float minValueY = 0;

		switch(mGraphType) {
			case GRAPH_TYPE_TEMPERATURE:
				maxValueY = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleTemperatureValue - mValues.get(mMaxIndex)) * 10) * mTemperatureHeightForPoint1);
				minValueY = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleTemperatureValue - mValues.get(mMinIndex)) * 10) * mTemperatureHeightForPoint1);
				break;
			case GRAPH_TYPE_HUMIDITY:
				maxValueY = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleHumidityValue - mValues.get(mMaxIndex)) * 10) * mHumidityHeightForPoint1);
				minValueY = (float)((mMinLineHeight - mMaxLineHeight) / 2.0f + ((mMiddleHumidityValue - mValues.get(mMinIndex)) * 10) * mHumidityHeightForPoint1);
				break;
			case GRAPH_TYPE_VOC:
				maxValueY = (float)(mMinLineHeight - mValues.get(mMaxIndex) * mVocHeightForPoint1);
				minValueY = (float)(mMinLineHeight - mValues.get(mMinIndex) * mVocHeightForPoint1);
				if (maxValueY < mMaxLineHeight) {
					maxValueY = mMaxLineHeight;
				}
				break;
			default:
				maxValueY = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (mValues.get(mMaxIndex) - mMinValue) / (mMaxValue - mMinValue)));
				minValueY = mMinLineHeight;
				break;
		}

		if (mGraphMode == GRAPH_MODE_SAME_DATA) {
			tvMaxValue.setX(maxValueX);
			tvMaxValue.setY(maxValueY - tvMaxValue.getHeight() - 5);
			tvMinValue.setVisibility(View.GONE);
		} else if (mGraphMode == GRAPH_MODE_NORMAL_DATA) {
			tvMaxValue.setX(maxValueX);
			tvMaxValue.setY(maxValueY - tvMaxValue.getHeight() - 5);
			tvMinValue.setVisibility(View.VISIBLE);
			tvMinValue.setX(minValueX);
			tvMinValue.setY(minValueY + 5);
		}

		switch(mGraphType) {
			case GRAPH_TYPE_SCORE:
				if (mMaxValue < 50) {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_red);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
				} else if (mMaxValue < 70) {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_yellow);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
				} else if (mMaxValue < 90) {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_green);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
				} else {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_blue);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
				}

				if (mMinValue < 50) {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_red);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
				} else if (mMinValue < 70) {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_yellow);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
				} else if (mMinValue < 90) {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_green);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
				} else {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_blue);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
				}

				break;
			case GRAPH_TYPE_TEMPERATURE:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					if (mMaxValue >= mMaxTemperature || mMaxValue <= mMinTemperature) {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_red);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextWarning));
					} else {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_green);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
					}

					if (mMinValue >= mMaxTemperature || mMinValue <= mMinTemperature) {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_blue);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextWarningBlue));
					} else {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_green);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
					}
				} else {
					if (mMaxValue >= mMaxTemperature || mMaxValue <= mMinTemperature) {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_red);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					} else {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_blue);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					}

					if (mMinValue >= mMaxTemperature || mMinValue <= mMinTemperature) {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_red);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					} else {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_blue);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					}
				}

				break;
			case GRAPH_TYPE_HUMIDITY:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					if (mMaxValue >= mMaxHumidity || mMaxValue <= mMinHumidity) {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_yellow);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextWarningOrange));
					} else {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_green);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
					}

					if (mMinValue >= mMaxHumidity || mMinValue <= mMinHumidity) {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_yellow);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextWarningOrange));
					} else {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_green);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
					}
				} else {
					if (mMaxValue >= mMaxHumidity || mMaxValue <= mMinHumidity) {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_red);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					} else {
						tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_blue);
						tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					}

					if (mMinValue >= mMaxHumidity || mMinValue <= mMinHumidity) {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_red);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					} else {
						tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_blue);
						tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					}
				}
				break;
			case GRAPH_TYPE_VOC:
				if (mMaxValue > 300) {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_red);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					tvMaxValue.setText(getContext().getString(R.string.device_environment_voc_very_bad));
				} else if (mMaxValue > 150) {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_yellow);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
					tvMaxValue.setText(getContext().getString(R.string.device_environment_voc_not_good));
				} else if (mMaxValue > 50) {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_green);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
					tvMaxValue.setText(getContext().getString(R.string.device_environment_voc_normal));
				} else {
					tvMaxValue.setBackgroundResource(R.drawable.bg_graph_max_value_blue);
					tvMaxValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					tvMaxValue.setText(getContext().getString(R.string.device_environment_voc_good));
				}

				if (mMinValue > 300) {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_red);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					tvMinValue.setText(getContext().getString(R.string.device_environment_voc_very_bad));
				} else if (mMinValue > 150) {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_yellow);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
					tvMinValue.setText(getContext().getString(R.string.device_environment_voc_not_good));
				} else if (mMinValue > 50) {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_green);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
					tvMinValue.setText(getContext().getString(R.string.device_environment_voc_normal));
				} else {
					tvMinValue.setBackgroundResource(R.drawable.bg_graph_min_value_blue);
					tvMinValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					tvMinValue.setText(getContext().getString(R.string.device_environment_voc_good));
				}
				break;
		}
	}

	private void _drawVerticalGuideLine(Canvas canvas) {

		// Thick line
		Path path = new Path();
		for (int i = 0; i < 13; i++) {
			if (i % 3 == 0) {
				path.moveTo(mWidthBetweenGuideLine * (i + 1), mMaxGraphHeight);
				path.lineTo(mWidthBetweenGuideLine * (i + 1), mMinGraphHeight);
			}
		}
		mGuideLinePaint.setStrokeWidth(1);
		mGuideLinePaint.setColor(getResources().getColor(R.color.colorGraphThickGuideLine));
		canvas.drawPath(path, mGuideLinePaint);

		// Thin line
		for (int i = 0; i < 13; i++) {
			if (i % 3 != 0) {
				path.moveTo(mWidthBetweenGuideLine * (i + 1), mMaxGraphHeight);
				path.lineTo(mWidthBetweenGuideLine * (i + 1), mMinGraphHeight);
			}
		}
		mGuideLinePaint.setStrokeWidth(0.5f);
		canvas.drawPath(path, mGuideLinePaint);
	}

	private void _drawTimeText(Canvas canvas) {
		final float height = (float)(mMinGraphHeight + (int)(mTimeTextSize * 1.5));
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			canvas.drawText("Midnight", mWidthBetweenGuideLine * 1, height, mTimePaint);
			canvas.drawText("6AM", mWidthBetweenGuideLine * 4, height, mTimePaint);
			canvas.drawText("Noon", mWidthBetweenGuideLine * 7, height, mTimePaint);
			canvas.drawText("6PM", mWidthBetweenGuideLine * 10, height, mTimePaint);
			canvas.drawText("Midnight", mWidthBetweenGuideLine * 13, height, mTimePaint);
		} else {
			canvas.drawText("00", mWidthBetweenGuideLine * 1, height, mTimePaint);
			canvas.drawText("06", mWidthBetweenGuideLine * 4, height, mTimePaint);
			canvas.drawText("12", mWidthBetweenGuideLine * 7, height, mTimePaint);
			canvas.drawText("18", mWidthBetweenGuideLine * 10, height, mTimePaint);
			canvas.drawText("24", mWidthBetweenGuideLine * 13, height, mTimePaint);
		}
	}
}