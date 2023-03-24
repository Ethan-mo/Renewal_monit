package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.SensorGraphInfo;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class GraphViewSleeping extends View {
	private static final String TAG = Configuration.BASE_TAG + "GraphViewS";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	public static final int GRAPH_TYPE_MOVEMENT = 1;

	public static final int GRAPH_MODE_NO_DATA		= 1;
	public static final int GRAPH_MODE_SAME_DATA	= 2;
	public static final int GRAPH_MODE_NORMAL_DATA	= 3;

	private static final float SEPARATE_SECTION = 9;

	private static final int DEFAULT_SEPARATE_SECTION_FOR_10SEC = 8642;

	private static final int DEFAULT_GRAPH_CAP_HEIGHT = 30;

	private int mGraphType = GRAPH_TYPE_MOVEMENT;

	/**
	 * Related to draw object
	 */
	private Paint mGuideLinePaint, mGuideLineDashedPaint, mTimePaint, mSelectedTimePaint, mSelectedLineGraphPaint, mBackgroundGraphPaint;
	private Paint mDeepSleepSelectedPaint, mSleepSelectedPaint, mDeepSleepDeselectedPaint, mSleepDeselectedPaint, mDisconnectedPaint, mMovePaint;
	private float mTimeTextSize;

	/**
	 * Related to Coordinations
	 */
	private int mWidth, mHeight;
	private int mMaxGraphHeight, mMinGraphHeight;
	private int mMaxLineHeight, mMinLineHeight;
	private int mTouchedX, mTouchedDayIndex;
	private float mWidthBetweenGuideLine;
	private float mGraphStartX, mGraphEndX;
	private float mGraphWidth;
	private ArrayList<Float> mUpperCapHeight;
	private ArrayList<Float> mLowerCapHeight;

	/**
	 * Related to Values
	 */
	private double mMaxValue, mMinValue;
	private long mBeginTimeMs;

	private ArrayList<ArrayList<Integer>> mValues;
	private TextView tvNoData;
	private TextView tvTotalSleepTime, tvAwakeCount, tvDeepSleepTime, tvSleepScore, tvMovementCount;
	private RelativeLayout rctnDetailSection;
	private TextView tvDetailDate;

	private int mGraphMode = GRAPH_MODE_NORMAL_DATA;

	// Graph Value Data
	private ArrayList<Integer> mTotalCountDeepSleepSec;
	private ArrayList<Integer> mTotalCountSleepSec;
	private ArrayList<Integer> mTotalCountConvertedSleepSec;
	private ArrayList<Integer> mTotalCountAwake;

	// Graph Days
	private String[] mStrDays;
	private int[] mDays;
	private int mDayCount = 7;

	public GraphViewSleeping(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public GraphViewSleeping(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public void init() {
		mTouchedX = -1;
		mTouchedDayIndex = -1;

		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTouchedX = (int) event.getX();

				if ((mTouchedX > mGraphStartX) && (mTouchedX < mWidth - mGraphStartX)) {
					mTouchedDayIndex = (int)((mTouchedX - mGraphStartX) / mWidthBetweenGuideLine);
					if (DBG) Log.d(TAG, "onTouch: " + mTouchedX + " / " + mTouchedDayIndex);
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

		mValues = new ArrayList<ArrayList<Integer>>();
		mTotalCountDeepSleepSec = new ArrayList<>();
		mTotalCountSleepSec = new ArrayList<>();
		mTotalCountConvertedSleepSec = new ArrayList<>();
		mTotalCountAwake = new ArrayList<>();
		mUpperCapHeight = new ArrayList<>();
		mLowerCapHeight = new ArrayList<>();

		/*
		 * Guide Line Paint
		 */
		mGuideLinePaint = new Paint();
		mGuideLinePaint.setStyle(Paint.Style.STROKE);
		mGuideLinePaint.setColor(Color.GRAY);
		mGuideLinePaint.setStrokeWidth(1);
		mGuideLinePaint.setAntiAlias(true);

		mGuideLineDashedPaint = new Paint();
		mGuideLineDashedPaint.setStyle(Paint.Style.STROKE);
		mGuideLineDashedPaint.setColor(Color.GRAY);
		mGuideLineDashedPaint.setStrokeWidth(1);
		mGuideLineDashedPaint.setAntiAlias(true);

		/*
		 * Time Paint
		 */
		mTimeTextSize = 12 * getResources().getDisplayMetrics().scaledDensity;
		mTimePaint = new Paint();
		mTimePaint.setColor(getResources().getColor(R.color.colorTextPrimaryLight));
		mTimePaint.setTextSize(mTimeTextSize);
		mTimePaint.setTextAlign(Align.CENTER);
		mTimePaint.setAntiAlias(true);

		mSelectedTimePaint = new Paint();
		mSelectedTimePaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));
		mSelectedTimePaint.setTextSize(mTimeTextSize);
		mSelectedTimePaint.setTextAlign(Align.CENTER);
		mSelectedTimePaint.setAntiAlias(true);

		/*
		 * Line Graph Paint
		 */
		mSelectedLineGraphPaint = new Paint();
		mSelectedLineGraphPaint.setStyle(Paint.Style.STROKE);
		mSelectedLineGraphPaint.setAntiAlias(true);
		mSelectedLineGraphPaint.setColor(getResources().getColor(R.color.colorGraphSleepSelectedLine));

		mDeepSleepSelectedPaint = new Paint();
		mDeepSleepSelectedPaint.setStyle(Paint.Style.STROKE);
		mDeepSleepSelectedPaint.setAntiAlias(true);
		mDeepSleepSelectedPaint.setColor(getResources().getColor(R.color.colorGraphDeepSleepSelected));

		mSleepSelectedPaint = new Paint();
		mSleepSelectedPaint.setStyle(Paint.Style.STROKE);
		mSleepSelectedPaint.setAntiAlias(true);
		mSleepSelectedPaint.setColor(getResources().getColor(R.color.colorGraphSleepSelected));

		mDeepSleepDeselectedPaint = new Paint();
		mDeepSleepDeselectedPaint.setStyle(Paint.Style.STROKE);
		mDeepSleepDeselectedPaint.setAntiAlias(true);
		mDeepSleepDeselectedPaint.setColor(getResources().getColor(R.color.colorGraphDeepSleepDeselected));

		mSleepDeselectedPaint = new Paint();
		mSleepDeselectedPaint.setStyle(Paint.Style.STROKE);
		mSleepDeselectedPaint.setAntiAlias(true);
		mSleepDeselectedPaint.setColor(getResources().getColor(R.color.colorGraphSleepDeselected));
		mSleepDeselectedPaint.setStrokeWidth(10);

		mDisconnectedPaint = new Paint();
		mDisconnectedPaint.setStyle(Paint.Style.STROKE);
		mDisconnectedPaint.setAntiAlias(true);
		mDisconnectedPaint.setColor(getResources().getColor(R.color.colorGreyTransparent));
		mDisconnectedPaint.setStrokeWidth(10);

		/*
		 * Background Graph Paint
		 */
		mBackgroundGraphPaint = new Paint();
		mBackgroundGraphPaint.setStyle(Paint.Style.STROKE);
		mBackgroundGraphPaint.setAntiAlias(true);
		mBackgroundGraphPaint.setColor(getResources().getColor(R.color.colorGreyTransparent));
	}

	public void setDayTotalSleepTimeTextView(TextView tv) {
		tvTotalSleepTime = tv;
	}

	public void setDayTotalAwakeCountTimeTextView(TextView tv) {
		tvAwakeCount = tv;
	}

	public void setDayMovementCountTextView(TextView tv) {
		tvMovementCount = tv;
	}

	public void setDayDeepSleepTimeTextView(TextView tv) {
		tvDeepSleepTime = tv;
	}

	public void setDaySleepScoreTextView(TextView tv) {
		tvSleepScore = tv;
	}

	public void setDetailSection(RelativeLayout layout) {
		rctnDetailSection = layout;
	}

	public void setDetailDateTextView(TextView tv) {
		tvDetailDate = tv;
	}

	public void setNoDataTextView(TextView tv) {
		tvNoData = tv;
	}

	public void setBeginTimeMs(long timeMs) {
		mBeginTimeMs = timeMs;
	}

	// 가로축 Day값 설정(일자만 포함)
	public void setDays(int[] days, String[] strDays) {
		if (DBG) Log.d(TAG, "setDays size : " + days.length + " / " + strDays.length);
		mDays = days;
		mStrDays = strDays;
		mDayCount = days.length;
		mGraphStartX = (int)(mWidth / SEPARATE_SECTION);
		mGraphEndX = mWidth - mGraphStartX;
		mWidthBetweenGuideLine = (int)((mWidth - mGraphStartX * 2) / mDayCount);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		mMaxGraphHeight = (int)(mHeight * 0.05);
		mMinGraphHeight = (int)(mHeight * 0.95);
		mMaxLineHeight = (int)(mMinGraphHeight * 0.05);
		mMinLineHeight = (int)(mMinGraphHeight * 0.95);
		mGraphStartX = (int)(mWidth / SEPARATE_SECTION);
		mGraphEndX = mWidth - mGraphStartX;
		mWidthBetweenGuideLine = (int)((mWidth - mGraphStartX * 2) / mDayCount);
		mGraphWidth = mWidth - mGraphStartX * 2;

		/*
		if (DBG) Log.d(TAG, "onSizeChagned : " + mWidth + " / "
				+ mHeight + " / "
				+ mWidthBetweenGuideLine + " / "
				+ mGraphWidth + " / "
				+ mWidthBetweenDot + " / "
				+ mMinGraphHeight + " / "
				+ mMaxLineHeight + " / "
				+ mMinLineHeight);
		*/
	}

	public void setValues(ArrayList<SensorGraphInfo> sensorGraphInfoList) {
		mTouchedX = -1;
		mTouchedDayIndex = -1;

		if (sensorGraphInfoList == null) return;

		if (mValues == null) mValues = new ArrayList<>();
		mValues.clear();
		mTotalCountSleepSec.clear();
		mTotalCountDeepSleepSec.clear();
		mTotalCountAwake.clear();
		mTotalCountConvertedSleepSec.clear();

		boolean hasData = false;
		for (int day = 0; day < sensorGraphInfoList.size(); day++) {
			mValues.add(sensorGraphInfoList.get(day).sleepingValues);
			mTotalCountDeepSleepSec.add(sensorGraphInfoList.get(day).deepSleepTimeSec);
			mTotalCountSleepSec.add(sensorGraphInfoList.get(day).sleepTimeSec);
			mTotalCountConvertedSleepSec.add(sensorGraphInfoList.get(day).convertedSleepTimeSec);
			mTotalCountAwake.add(sensorGraphInfoList.get(day).cntMovementDetected);

			if (sensorGraphInfoList.get(day).convertedSleepTimeSec > 0) {
				hasData = true;
			}
		}

		if (mValues == null) {
			if (DBG) Log.d(TAG, "setValues: " + sensorGraphInfoList.size() + " -> NULL");
		} else {
			if (DBG) Log.d(TAG, "setValues: " + sensorGraphInfoList.size() + " -> " + mValues.size());
		}
		if (mValues == null) {
			mGraphMode = GRAPH_MODE_NO_DATA;
		} else if (!hasData) { // 데이터가 없는 경우
			mGraphMode = GRAPH_MODE_NO_DATA;
		} else if (mMaxValue == mMinValue) { // 모든 값이 동일한 경우
			mGraphMode = GRAPH_MODE_SAME_DATA;
		} else {
			mGraphMode = GRAPH_MODE_NORMAL_DATA;
			if (tvDeepSleepTime != null) {
				//tvDeepSleepTime.setText(mAverageMovementLevel + "");
			}
		}

		if (mGraphMode == GRAPH_MODE_NO_DATA) {
			if (tvTotalSleepTime != null) {
				tvTotalSleepTime.setText("-");
			}
			if (tvMovementCount != null) {
				tvMovementCount.setText("-");
			}
			if (tvDeepSleepTime != null) {
				tvDeepSleepTime.setText("-");
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw GuideLine
		_drawGuideLine(canvas);

		switch(mGraphMode) {
			case GRAPH_MODE_NO_DATA:
				tvNoData.setVisibility(View.VISIBLE);
				rctnDetailSection.setVisibility(View.GONE);
				break;
			case GRAPH_MODE_SAME_DATA:
			case GRAPH_MODE_NORMAL_DATA:
				tvNoData.setVisibility(View.GONE);
				if (mValues != null) {
					_drawSelectedInfo(canvas);
					if (mValues.size() > 0) {
						_drawLineGraph(canvas);
					}
				}
				break;
		}
	}

	private void _drawLineGraph(Canvas canvas) {
		float beforeHeight = 0;
		float nowHeight = 0;
		float nowWidth;

		int curr;
		ArrayList<Integer> values;
		for (int day = 0; day < mValues.size(); day++) {
			values = mValues.get(day);
			if (values == null) {
				continue;
			}

			Path disconnectedPath = new Path();
			Path movePath = new Path();
			Path deepSleepPath = new Path();
			Path sleepPath = new Path();

			nowWidth = mGraphStartX + (mWidthBetweenGuideLine * day) + (mWidthBetweenGuideLine / 2);

			beforeHeight = -1;
			mUpperCapHeight.clear();
			mLowerCapHeight.clear();
			boolean findUpperCap = true;
			boolean findLowerCap = false;
			float lowerCapHeight = -1;
			int cntLowerCapAvailable = 0;
			if (DBG) Log.d(TAG, "setValues: " + day + " / " + values.size());
			for (int idx = 0; idx < values.size(); idx++) {
				curr = values.get(idx);

				nowHeight = mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / (float)DEFAULT_SEPARATE_SECTION_FOR_10SEC * idx;

				if (beforeHeight == -1) {
					beforeHeight = nowHeight;
				}

				switch(curr) {
					case DeviceStatus.MOVEMENT_SLEEP:
						if (findUpperCap) {
							findUpperCap = false;
							findLowerCap = true;
							lowerCapHeight = -1;
							mUpperCapHeight.add(beforeHeight);
						}
						cntLowerCapAvailable = 0;

						sleepPath.moveTo(nowWidth, beforeHeight);
						sleepPath.lineTo(nowWidth, nowHeight);
						break;
					case DeviceStatus.MOVEMENT_DEEP_SLEEP:
						if (findUpperCap) {
							findUpperCap = false;
							findLowerCap = true;
							lowerCapHeight = -1;
							mUpperCapHeight.add(beforeHeight);
						}
						cntLowerCapAvailable = 0;

						deepSleepPath.moveTo(nowWidth, beforeHeight);
						deepSleepPath.lineTo(nowWidth, nowHeight);
						break;
					case DeviceStatus.MOVEMENT_NOT_USING:
					case DeviceStatus.MOVEMENT_DISCONNECTED:
					case DeviceStatus.MOVEMENT_NO_MOVEMENT:
						if (lowerCapHeight == -1) {
							lowerCapHeight = nowHeight;
						}
						cntLowerCapAvailable++;
						if (findLowerCap) {
							if (cntLowerCapAvailable == 60) {
								findLowerCap = false;
								findUpperCap = true;
								mLowerCapHeight.add(beforeHeight);
							}
						}

						disconnectedPath.moveTo(nowWidth, beforeHeight);
						disconnectedPath.lineTo(nowWidth, nowHeight);
						break;
					default:
						if (lowerCapHeight == -1) {
							lowerCapHeight = nowHeight;
						}
						cntLowerCapAvailable++;
						if (findLowerCap) {
							if (cntLowerCapAvailable == 60) {
								findLowerCap = false;
								findUpperCap = true;
								mLowerCapHeight.add(beforeHeight);
							}
						}

						movePath.moveTo(nowWidth, beforeHeight);
						movePath.lineTo(nowWidth, nowHeight);
						break;
				}

				beforeHeight = nowHeight;
			}

			if (findLowerCap) {
				mLowerCapHeight.add(beforeHeight);
			}

			if (mTouchedDayIndex == -1) {
				mDeepSleepSelectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
				canvas.drawPath(deepSleepPath, mDeepSleepSelectedPaint);

				mSleepSelectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
				canvas.drawPath(sleepPath, mSleepSelectedPaint);
			} else {
				if (mTouchedDayIndex == day) {
					mDeepSleepSelectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
					canvas.drawPath(deepSleepPath, mDeepSleepSelectedPaint);

					mSleepSelectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
					canvas.drawPath(sleepPath, mSleepSelectedPaint);
				} else {
					mDeepSleepDeselectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
					canvas.drawPath(deepSleepPath, mDeepSleepDeselectedPaint);

					mSleepDeselectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
					canvas.drawPath(sleepPath, mSleepDeselectedPaint);
				}
			}

			float capHeight = DEFAULT_GRAPH_CAP_HEIGHT;
			for (float upperHeight: mUpperCapHeight) {
				if (DBG) Log.d(TAG, "upper height: " + upperHeight);
				Drawable d = getResources().getDrawable(R.drawable.bg_upper_radius_cap_white, null);
				d.setBounds((int)(mGraphStartX + mWidthBetweenGuideLine * day + mWidthBetweenGuideLine / 2 - mWidthBetweenGuideLine * 0.3f - 2)
						, (int)upperHeight
						, (int)(mGraphStartX + mWidthBetweenGuideLine * day + mWidthBetweenGuideLine / 2 + mWidthBetweenGuideLine * 0.3f + 2)
						, (int)(upperHeight + capHeight));
				d.draw(canvas);
			}
			for (float lowerHeight: mLowerCapHeight) {
				if (DBG) Log.d(TAG, "lower height: " + lowerHeight);
				Drawable d = getResources().getDrawable(R.drawable.bg_lower_radius_cap_white, null);
				d.setBounds((int)(mGraphStartX + mWidthBetweenGuideLine * day + mWidthBetweenGuideLine / 2 - mWidthBetweenGuideLine * 0.3f - 2)
						, (int)(lowerHeight - capHeight)
						, (int)(mGraphStartX + mWidthBetweenGuideLine * day + mWidthBetweenGuideLine / 2 + mWidthBetweenGuideLine * 0.3f + 2)
						, (int)(lowerHeight));
				d.draw(canvas);
			}
		}
	}

	private void _drawSelectedInfo(Canvas canvas) {
		if (mTouchedX > -1) {
			if (mDayCount < 10) {
				mSelectedLineGraphPaint.setStrokeWidth(1);
			} else {
				mSelectedLineGraphPaint.setStrokeWidth(1);
//				Rect textBounds = new Rect();
//				String time = String.format("12시");
//				mSelectedTimePaint.getTextBounds(time, 0, time.length(), textBounds);
//				canvas.drawText(mStrDays[mTouchedDayIndex], mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex), mMaxLineHeight - textBounds.height() / 2, mSelectedTimePaint);
			}
			Path path = new Path();
			path.moveTo(mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex) + (mWidthBetweenGuideLine / 2), mMaxLineHeight - 5);
			path.lineTo(mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex) + (mWidthBetweenGuideLine / 2), mMinLineHeight + 5);
			canvas.drawPath(path, mSelectedLineGraphPaint);

			int totalDeepSleepHour = (mTotalCountDeepSleepSec.get(mTouchedDayIndex) / 60) / 60;
			int totalDeepSleepMinute = (mTotalCountDeepSleepSec.get(mTouchedDayIndex) / 60) % 60;
			String deepSleepTime = "";
			if (totalDeepSleepHour == 0) {
				deepSleepTime = totalDeepSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			} else {
				deepSleepTime = totalDeepSleepHour + getResources().getString(R.string.time_elapsed_hour) + " " + totalDeepSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			}

			int totalSleepHour = (mTotalCountSleepSec.get(mTouchedDayIndex) / 60) / 60;
			int totalSleepMinute = (mTotalCountSleepSec.get(mTouchedDayIndex) / 60) % 60;
			String sleepTime = "";
			if (totalSleepHour == 0) {
				sleepTime = totalSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			} else {
				sleepTime = totalSleepHour + getResources().getString(R.string.time_elapsed_hour) + " " + totalSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			}

			int totalConvertedSleepHour = (mTotalCountConvertedSleepSec.get(mTouchedDayIndex) / 60) / 60;
			int totalConvertedSleepMinute = (mTotalCountConvertedSleepSec.get(mTouchedDayIndex) / 60) % 60;
			String convertedSleepTime = "";
			if (totalConvertedSleepHour == 0) {
				convertedSleepTime = totalConvertedSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			} else {
				convertedSleepTime = totalConvertedSleepHour + getResources().getString(R.string.time_elapsed_hour) + " " + totalConvertedSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			}

			if (tvTotalSleepTime != null) {
				tvTotalSleepTime.setText(convertedSleepTime);
			}

			if (tvAwakeCount != null) {
				tvAwakeCount.setText(mTotalCountAwake.get(mTouchedDayIndex) + "");
			}

			/*
			if (tvDeepSleepTime != null) {
				tvDeepSleepTime.setText(deepSleepTime);
			}

			if (tvSleepScore != null) {
				tvSleepScore.setText((int)(mTotalCountDeepSleepSec.get(mTouchedDayIndex) * 100.0f / mTotalCountSleepSec.get(mTouchedDayIndex)) + "");
			}
			*/

			long todayMs = mBeginTimeMs + (long)mTouchedDayIndex * 1000 * 60 * 60 * 24;
			tvDetailDate.setText(DateTimeUtil.getDateStringWithDay(todayMs, Locale.getDefault().getLanguage()));

			rctnDetailSection.setVisibility(View.VISIBLE);
		} else {
			if (tvTotalSleepTime != null) {
				tvTotalSleepTime.setText("-");
			}

			if (tvDeepSleepTime != null) {
				tvDeepSleepTime.setText("-");
			}

			if (tvSleepScore != null) {
				tvSleepScore.setText("-");
			}
			rctnDetailSection.setVisibility(View.GONE);
		}
	}

	private void _drawGuideLine(Canvas canvas) {
		Rect textBounds = new Rect();
		String time = String.format("12시");
		mTimePaint.getTextBounds(time, 0, time.length(), textBounds);

		Path path = new Path();
		for (int i = 0; i <= 24; i++) {
			if (i % 4 == 0) {
				canvas.drawText(String.format("%02d"+getResources().getString(R.string.time_hour), i), mGraphStartX - textBounds.width(), mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / 24.0f * i + textBounds.height() / 2, mTimePaint);

				// Horizontal Line
				path.moveTo(mGraphStartX - textBounds.width() / 3, mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / 24.0f * i);
				path.lineTo(mGraphStartX + mWidthBetweenGuideLine * mDayCount + textBounds.width() / 3, mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / 24.0f * i);
			}
		}
		mGuideLinePaint.setStrokeWidth(1);
		canvas.drawPath(path, mGuideLinePaint);

		// Vertical Line
		path.moveTo(mGraphStartX, mMaxLineHeight);
		path.lineTo(mGraphStartX, mMinLineHeight + textBounds.height() * 1.0f);
		mGuideLinePaint.setStrokeWidth(1);
		canvas.drawPath(path, mGuideLinePaint);

		// Vertical Line(Dashed)
		if (mDayCount < 10) {
			for (int i = 0; i <= mDayCount; i++) {
				path.moveTo(mGraphStartX + mWidthBetweenGuideLine * i, mMaxLineHeight);
				path.lineTo(mGraphStartX + mWidthBetweenGuideLine * i, mMinLineHeight + textBounds.height() * 1.5f);
				if (i < mDayCount) {
					canvas.drawText(mStrDays[i], mGraphStartX + (mWidthBetweenGuideLine * i) + (mWidthBetweenGuideLine / 2), mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) + textBounds.height() * 2, mTimePaint);
				}
			}
		} else {
			for (int i = 0; i <= mDayCount; i++) {
				if (i % 5 == 0) {
					// Vertical Line
					path.moveTo(mGraphStartX + mWidthBetweenGuideLine * i, mMaxLineHeight);
					path.lineTo(mGraphStartX + mWidthBetweenGuideLine * i, mMinLineHeight + textBounds.height() * 1.0f);
					if (i < mDayCount) {
						canvas.drawText(mStrDays[i], mGraphStartX + (mWidthBetweenGuideLine * i) + (mWidthBetweenGuideLine / 2), mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) + textBounds.height() * 2, mTimePaint);
					}
				}
			}
		}

		mGuideLineDashedPaint.setStrokeWidth(1);
		mGuideLineDashedPaint.setPathEffect(new DashPathEffect(new float[]{10, 5, 10, 5}, 0));
		canvas.drawPath(path, mGuideLineDashedPaint);
	}
}