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
import goodmonit.monit.com.kao.devices.SensorGraphInfo;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class GraphViewDiaper extends View {
	private static final String TAG = Configuration.BASE_TAG + "GraphViewD";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	public static final int GRAPH_TYPE_DIAPER = 1;

	public static final int GRAPH_MODE_NO_DATA		= 1;
	public static final int GRAPH_MODE_SAME_DATA	= 2;
	public static final int GRAPH_MODE_NORMAL_DATA	= 3;

	private static final float SEPARATE_SECTION = 9;

	private static final int DEFAULT_SEPARATE_SECTION_FOR_DIAPER_CHANGED_COUNT = 24;

	private static final int DEFAULT_GRAPH_CAP_HEIGHT = 30;

	private int mGraphType = GRAPH_TYPE_DIAPER;

	/**
	 * Related to draw object
	 */
	private Paint mGuideLinePaint, mGuideLineDashedPaint, mTimePaint, mSelectedTimePaint, mSelectedLineGraphPaint, mBackgroundGraphPaint;
	private Paint mDiaperChangedSelectedPaint, mDiaperChangedDeselectedPaint;
	private Paint mDiaperChangedSelectedTextPaint, mDiaperChangedDeselectedTextPaint;
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
	private TextView tvDayTotalDiaperChanged, tvDayTotalPeeCount, tvDayTotalPooCount, tvDayTotalSoiledCount;
	private RelativeLayout rctnDetailSection;
	private TextView tvDetailDate;

	private int mGraphMode = GRAPH_MODE_NORMAL_DATA;

	// Graph Days
	private String[] mStrDays;
	private int[] mDays;
	private int mDayCount = 7;

	public GraphViewDiaper(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public GraphViewDiaper(Context context, AttributeSet attrs) {
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
		mSelectedLineGraphPaint.setColor(getResources().getColor(R.color.colorDiaryTextDiaper));

		mDiaperChangedSelectedPaint = new Paint();
		mDiaperChangedSelectedPaint.setStyle(Paint.Style.STROKE);
		mDiaperChangedSelectedPaint.setAntiAlias(true);
		mDiaperChangedSelectedPaint.setColor(getResources().getColor(R.color.colorDiaryTextDiaper));

		mDiaperChangedDeselectedPaint = new Paint();
		mDiaperChangedDeselectedPaint.setStyle(Paint.Style.STROKE);
		mDiaperChangedDeselectedPaint.setAntiAlias(true);
		mDiaperChangedDeselectedPaint.setColor(getResources().getColor(R.color.colorGraphDeepSleepDeselected));

		mDiaperChangedSelectedTextPaint = new Paint();
		mDiaperChangedSelectedTextPaint.setColor(getResources().getColor(R.color.colorDiaryTextDiaper));
		mDiaperChangedSelectedTextPaint.setTextSize(mTimeTextSize);
		mDiaperChangedSelectedTextPaint.setTextAlign(Align.CENTER);
		mDiaperChangedSelectedTextPaint.setAntiAlias(true);

		mDiaperChangedDeselectedTextPaint = new Paint();
		mDiaperChangedDeselectedTextPaint.setColor(getResources().getColor(R.color.colorGraphDeepSleepDeselected));
		mDiaperChangedDeselectedTextPaint.setTextSize(mTimeTextSize);
		mDiaperChangedDeselectedTextPaint.setTextAlign(Align.CENTER);
		mDiaperChangedDeselectedTextPaint.setAntiAlias(true);

		/*
		 * Background Graph Paint
		 */
		mBackgroundGraphPaint = new Paint();
		mBackgroundGraphPaint.setStyle(Paint.Style.STROKE);
		mBackgroundGraphPaint.setAntiAlias(true);
		mBackgroundGraphPaint.setColor(getResources().getColor(R.color.colorGreyTransparent));
	}

	public void setDayTotalDiaperChangedTextView(TextView tv) {
		tvDayTotalDiaperChanged =  tv;
	}

	public void setDayTotalPeeCountTextView(TextView tv) {
		tvDayTotalPeeCount =  tv;
	}

	public void setDayTotalPooCountTextView(TextView tv) {
		tvDayTotalPooCount =  tv;
	}

	public void setDayTotalSoiledCountTextView(TextView tv) {
		tvDayTotalSoiledCount =  tv;
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

	public void setValues(ArrayList<SensorGraphInfo> graphInfoList) {
		mTouchedX = -1;
		mTouchedDayIndex = -1;

		if (graphInfoList == null) return;

		if (mValues == null) mValues = new ArrayList<>();
		mValues.clear();

		boolean hasData = false;
		mMaxValue = -999;
		mMinValue = 999;
		int cnt = 0;
		int cntDiaperChanged = 0;
		int cntPeeDetected = 0;
		int cntPooDetected = 0;
		int cntSoiledDetected = 0;

		for (int day = 0; day < graphInfoList.size(); day++) {
			ArrayList<Integer> values = new ArrayList<>();
			if (graphInfoList.get(day) == null) {
				cntDiaperChanged = 0;
				cntPeeDetected = 0;
				cntPooDetected = 0;
				cntSoiledDetected = 0;
			} else {
				cntDiaperChanged = graphInfoList.get(day).cntDiaperChanged;
				cntPeeDetected = graphInfoList.get(day).cntPeeDetected;
				cntPooDetected = graphInfoList.get(day).cntPooDetected;
				cntSoiledDetected = graphInfoList.get(day).cntSoiledDetected;
			}

			cnt += cntDiaperChanged + cntPeeDetected + cntPooDetected;

			values.add(cntDiaperChanged);
			values.add(cntPeeDetected);
			values.add(cntPooDetected);
			values.add(cntSoiledDetected);

			mValues.add(values);
			if (DBG) Log.d(TAG, "setValues add: " + cntDiaperChanged + " / " + cntPeeDetected + " / " + cntPooDetected + " / " + cntSoiledDetected + " / " + day + " / " + graphInfoList.size());

			if (mMaxValue < cntDiaperChanged) {
				mMaxValue = cntDiaperChanged;
			}

			if (mMinValue > cntDiaperChanged) {
				mMinValue = cntDiaperChanged;
			}

			if (mMaxValue > 0) {
				hasData = true;
			}
		}

		if (mValues == null) {
			mGraphMode = GRAPH_MODE_NO_DATA;
		} else if (!hasData) { // 데이터가 없는 경우
			mGraphMode = GRAPH_MODE_NO_DATA;
		} else if (mMaxValue == mMinValue) { // 모든 값이 동일한 경우
			mGraphMode = GRAPH_MODE_SAME_DATA;
		} else {
			mGraphMode = GRAPH_MODE_NORMAL_DATA;
		}

		if (mValues == null) {
			if (DBG) Log.d(TAG, "setValues: " + graphInfoList.size() + " -> NULL / " + mGraphMode);
		} else {
			if (DBG) Log.d(TAG, "setValues: " + graphInfoList.size() + " -> " + mValues.size() + " / " + mMaxValue + " / " + mMinValue + " / " + mGraphMode);
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

		Rect textBounds = new Rect();
		String time = String.format("12");
		mDiaperChangedDeselectedTextPaint.getTextBounds(time, 0, time.length(), textBounds);

		for (int day = 0; day < mValues.size(); day++) {
			values = mValues.get(day);
			if (values == null) {
				continue;
			}

			Path diaperChangedPath = new Path();

			nowWidth = mGraphStartX + (mWidthBetweenGuideLine * day) + (mWidthBetweenGuideLine / 2);

			mUpperCapHeight.clear();
			mLowerCapHeight.clear();

			// 하루하루 그래프 그리기
			int dayTotalDiaperChanged = values.get(0);
			int dayTotalPeeDetected = values.get(1);
			int dayTotalPooDetected = values.get(2);
			int dayTotalSoiledDetected = values.get(3);

			// 그래프 상단
			nowHeight = mMinLineHeight - 2 - (mMinLineHeight - mMaxLineHeight) / (float)DEFAULT_SEPARATE_SECTION_FOR_DIAPER_CHANGED_COUNT * dayTotalDiaperChanged; // 상단
			// 그래프 하단
			beforeHeight = mMinLineHeight - 2;

			if (DBG) Log.d(TAG, "setValues: " + beforeHeight + " / " + nowHeight + " / " + dayTotalDiaperChanged);

			// 0보다 큰 데이터만 그래프 그리기
			if (dayTotalDiaperChanged > 0) {
				mUpperCapHeight.add(nowHeight);
				mLowerCapHeight.add(beforeHeight);
			}
			diaperChangedPath.moveTo(nowWidth, beforeHeight);
			diaperChangedPath.lineTo(nowWidth, nowHeight);

			if (mTouchedDayIndex == -1) {
				mDiaperChangedSelectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
				canvas.drawPath(diaperChangedPath, mDiaperChangedSelectedPaint);
				if (mDayCount < 10) {
					canvas.drawText(dayTotalDiaperChanged + "", nowWidth, nowHeight - textBounds.height() / 2, mDiaperChangedSelectedTextPaint);
				}
			} else {
				if (mTouchedDayIndex == day) {
					mDiaperChangedSelectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
					canvas.drawPath(diaperChangedPath, mDiaperChangedSelectedPaint);
					if (mDayCount < 10) {
						canvas.drawText(dayTotalDiaperChanged + "", nowWidth, nowHeight - textBounds.height() / 2, mDiaperChangedSelectedTextPaint);
					}
				} else {
					mDiaperChangedDeselectedPaint.setStrokeWidth(mWidthBetweenGuideLine * 0.5f);
					canvas.drawPath(diaperChangedPath, mDiaperChangedDeselectedPaint);
					if (mDayCount < 10) {
						canvas.drawText(dayTotalDiaperChanged + "", nowWidth, nowHeight - textBounds.height() / 2, mDiaperChangedDeselectedTextPaint);
					}
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
//				String time = String.format("12");
//				mSelectedTimePaint.getTextBounds(time, 0, time.length(), textBounds);
//				canvas.drawText(mStrDays[mTouchedDayIndex], mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex), mMaxLineHeight - textBounds.height() / 2, mSelectedTimePaint);
			}
			Path path = new Path();
			path.moveTo(mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex) + (mWidthBetweenGuideLine / 2), mMaxLineHeight - 5);
			path.lineTo(mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex) + (mWidthBetweenGuideLine / 2), mMinLineHeight + 5);
			canvas.drawPath(path, mSelectedLineGraphPaint);

			int cntDiaperChanged = mValues.get(mTouchedDayIndex).get(0);
			int cntPeeDetected = mValues.get(mTouchedDayIndex).get(1);
			int cntPooDetected = mValues.get(mTouchedDayIndex).get(2);
			int cntSoiledDetected = mValues.get(mTouchedDayIndex).get(3);
			rctnDetailSection.setVisibility(View.VISIBLE);

			long todayMs = mBeginTimeMs + (long)mTouchedDayIndex * 1000 * 60 * 60 * 24;
			tvDetailDate.setText(DateTimeUtil.getDateStringWithDay(todayMs, Locale.getDefault().getLanguage()));
			tvDayTotalDiaperChanged.setText(cntDiaperChanged + "");
			tvDayTotalPeeCount.setText(cntPeeDetected + "");
			tvDayTotalPooCount.setText(cntPooDetected + "");
			tvDayTotalSoiledCount.setText(cntSoiledDetected + "");
		} else {
			rctnDetailSection.setVisibility(View.GONE);
		}
	}

	private void _drawGuideLine(Canvas canvas) {
		Rect textBounds = new Rect();
		String time = String.format("12");
		mTimePaint.getTextBounds(time, 0, time.length(), textBounds);

		Path path = new Path();
		for (int i = 0; i <= DEFAULT_SEPARATE_SECTION_FOR_DIAPER_CHANGED_COUNT; i++) {
			if (i % 5 == 0) {
				canvas.drawText(String.format("%d", i), mGraphStartX - textBounds.width(), mMinLineHeight - (mMinLineHeight - mMaxLineHeight) / (DEFAULT_SEPARATE_SECTION_FOR_DIAPER_CHANGED_COUNT + 0.0f) * i + textBounds.height() / 2, mTimePaint);

				// Horizontal Line
				path.moveTo(mGraphStartX - textBounds.width() / 3, mMinLineHeight - (mMinLineHeight - mMaxLineHeight) / (DEFAULT_SEPARATE_SECTION_FOR_DIAPER_CHANGED_COUNT + 0.0f) * i);
				path.lineTo(mGraphStartX + mWidthBetweenGuideLine * mDayCount + textBounds.width() / 3, mMinLineHeight - (mMinLineHeight - mMaxLineHeight) / (DEFAULT_SEPARATE_SECTION_FOR_DIAPER_CHANGED_COUNT + 0.0f) * i);
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