package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
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
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.SensorGraphInfo;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class GraphViewMovement extends View {
	private static final String TAG = Configuration.BASE_TAG + "GraphViewS";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	public static final int GRAPH_TYPE_MOVEMENT = 1;

	public static final int GRAPH_MODE_NO_DATA		= 1;
	public static final int GRAPH_MODE_SAME_DATA	= 2;
	public static final int GRAPH_MODE_NORMAL_DATA	= 3;

	private static final float SEPARATE_SECTION = 9;

	private static final int DEFAULT_SEPARATE_SECTION_FOR_MOVEMENT = 14;
	private static final int DEFAULT_POINT_RADIUS = 5;
	private static final int DEFAULT_LINE_GRAPH_WIDTH = 5;
	private static final int DEFAULT_SELECTED_DATE_VERTICAL_LINE_WIDTH = 2;

	private static final int DEFAULT_GRAPH_CAP_HEIGHT = 30;

	private int mGraphType = GRAPH_TYPE_MOVEMENT;

	/**
	 * Related to draw object
	 */
	private Paint mGuideLinePaint, mGuideLineDashedPaint, mTimePaint, mSelectedTimePaint, mSelectedLineGraphPaint, mBackgroundGraphPaint;
	private Paint mMovementLevelPaint, mMovementLevelTextPaint, mMovementLevelPointPaint;
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

	private ArrayList<SensorGraphInfo> mSensorGraphInfoList;
	private ArrayList<ArrayList<Integer>> mValues;
	private TextView tvNoData;
	private TextView tvAverageMovementLevel, tvAverageMovementString;
	private RelativeLayout rctnDetailSection;
	private TextView tvDetailDate;

	private int mGraphMode = GRAPH_MODE_NORMAL_DATA;

	// Graph Value Data
	private ArrayList<Integer> mTotalMovementLevelValue;
	private ArrayList<Integer> mTotalCountMovementLevel;
	private ArrayList<Float> mAverageMovementLevelValue;

	// Graph Days
	private String[] mStrDays;
	private int[] mDays;
	private int mDayCount = 7;

	private GraphViewMovementDetail gvDetailMovement;
	private RelativeLayout rctnDetailGraphSection;

	public GraphViewMovement(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public GraphViewMovement(Context context, AttributeSet attrs) {
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
		mTotalMovementLevelValue = new ArrayList<>();
		mTotalCountMovementLevel = new ArrayList<>();
		mAverageMovementLevelValue = new ArrayList<>();
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
		mSelectedLineGraphPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));

		mMovementLevelPaint = new Paint();
		mMovementLevelPaint.setStyle(Paint.Style.STROKE);
		mMovementLevelPaint.setAntiAlias(true);
		mMovementLevelPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));

		mMovementLevelPointPaint = new Paint();
		mMovementLevelPointPaint.setAntiAlias(true);
		mMovementLevelPointPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));

		mMovementLevelTextPaint = new Paint();
		mMovementLevelTextPaint.setAntiAlias(true);
		mMovementLevelTextPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));
		mMovementLevelTextPaint.setTextSize(mTimeTextSize);
		mMovementLevelTextPaint.setTextAlign(Align.CENTER);
		mMovementLevelTextPaint.setAntiAlias(true);

		/*
		 * Background Graph Paint
		 */
		mBackgroundGraphPaint = new Paint();
		mBackgroundGraphPaint.setStyle(Paint.Style.STROKE);
		mBackgroundGraphPaint.setAntiAlias(true);
		mBackgroundGraphPaint.setColor(getResources().getColor(R.color.colorGreyTransparent));
	}

	public void setDayAverageMovementLevelTextView(TextView tv) {
		tvAverageMovementLevel = tv;
	}

	public void setDayAverageMovementStringTextView(TextView tv) {
		tvAverageMovementString = tv;
	}

	public void setDetailSection(RelativeLayout layout) {
		rctnDetailSection = layout;
	}

	public void setDetailDateTextView(TextView tv) {
		tvDetailDate = tv;
	}

	public void setDetailGraphSection(RelativeLayout layout) {
		rctnDetailGraphSection = layout;
	}

	public void setDetailGraph(GraphViewMovementDetail gv) {
		gvDetailMovement = gv;
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
		mTotalMovementLevelValue.clear();
		mTotalCountMovementLevel.clear();
		mAverageMovementLevelValue.clear();

		boolean hasData = false;
		for (int day = 0; day < sensorGraphInfoList.size(); day++) {
			mValues.add(sensorGraphInfoList.get(day).movementValues);
			mTotalCountMovementLevel.add(sensorGraphInfoList.get(day).cntMovementLevel);
			mTotalMovementLevelValue.add(sensorGraphInfoList.get(day).sumMovementLevel);
			mAverageMovementLevelValue.add(sensorGraphInfoList.get(day).avgMovementLevel);

			if (sensorGraphInfoList.get(day).sumMovementLevel > 0) {
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
		}

		if (mGraphMode == GRAPH_MODE_NO_DATA) {
			if (tvAverageMovementLevel != null) {
				tvAverageMovementLevel.setText("-");
			}
			if (tvAverageMovementString != null) {
				tvAverageMovementString.setText("-");
			}
			rctnDetailSection.setVisibility(View.GONE);
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
				if (mAverageMovementLevelValue != null) {
					_drawSelectedInfo(canvas);
					if (mAverageMovementLevelValue.size() > 0) {
						_drawLineGraph(canvas);
					}
				}
				break;
		}
	}

	private void _drawLineGraph(Canvas canvas) {
		float beforeHeight = -1;
		float nowHeight = 0;
		float beforeWidth = -1;
		float nowWidth;

		float curr;
		Path movementPath = new Path();

		Rect textBounds = new Rect();
		String time = String.format("12시");
		mMovementLevelTextPaint.getTextBounds(time, 0, time.length(), textBounds);

		for (int day = 0; day < mAverageMovementLevelValue.size(); day++) {
			curr = mAverageMovementLevelValue.get(day);

			nowWidth = mGraphStartX + (mWidthBetweenGuideLine * day) + (mWidthBetweenGuideLine / 2);
			nowHeight = mMinLineHeight - ((mMinLineHeight - mMaxLineHeight) / (float)DEFAULT_SEPARATE_SECTION_FOR_MOVEMENT * curr);

			if (beforeWidth == -1) {
				beforeWidth = nowWidth;
			}
			if (beforeHeight == -1) {
				beforeHeight = nowHeight;
			}

			movementPath.moveTo(beforeWidth, beforeHeight);
			movementPath.lineTo(nowWidth, nowHeight);

			if (mDayCount < 10) {
				RectF oval = new RectF(nowWidth - DEFAULT_POINT_RADIUS, nowHeight - DEFAULT_POINT_RADIUS, nowWidth + DEFAULT_POINT_RADIUS, nowHeight + DEFAULT_POINT_RADIUS);
				canvas.drawOval(oval, mMovementLevelPointPaint);
				canvas.drawText(curr + "", nowWidth, nowHeight - textBounds.height(), mMovementLevelTextPaint);
			}

			beforeHeight = nowHeight;
			beforeWidth = nowWidth;
		}

		mMovementLevelPaint.setStrokeWidth(DEFAULT_LINE_GRAPH_WIDTH);
		canvas.drawPath(movementPath, mMovementLevelPaint);
	}

	private void _drawSelectedInfo(Canvas canvas) {
		if (mTouchedX > -1) {
			if (mDayCount < 10) {
				mSelectedLineGraphPaint.setStrokeWidth(DEFAULT_SELECTED_DATE_VERTICAL_LINE_WIDTH);
			} else {
				mSelectedLineGraphPaint.setStrokeWidth(DEFAULT_SELECTED_DATE_VERTICAL_LINE_WIDTH);
//				Rect textBounds = new Rect();
//				String time = String.format("12시");
//				mSelectedTimePaint.getTextBounds(time, 0, time.length(), textBounds);
//				canvas.drawText(mStrDays[mTouchedDayIndex], mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex), mMaxLineHeight - textBounds.height() / 2, mSelectedTimePaint);
			}
			Path path = new Path();
			path.moveTo(mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex) + (mWidthBetweenGuideLine / 2), mMaxLineHeight - 5);
			path.lineTo(mGraphStartX + (mWidthBetweenGuideLine * mTouchedDayIndex) + (mWidthBetweenGuideLine / 2), mMinLineHeight + 5);
			canvas.drawPath(path, mSelectedLineGraphPaint);

			float movementLevel = mAverageMovementLevelValue.get(mTouchedDayIndex);
			if (tvAverageMovementLevel != null) {
				tvAverageMovementLevel.setText(movementLevel + "");
			}

			if (tvAverageMovementString != null) {
				tvAverageMovementString.setText(DeviceStatus.getMovementStringResource((int)movementLevel));
			}

			long todayMs = mBeginTimeMs + (long)mTouchedDayIndex * 1000 * 60 * 60 * 24;
			tvDetailDate.setText(DateTimeUtil.getDateStringWithDay(todayMs, Locale.getDefault().getLanguage()));

			rctnDetailGraphSection.setVisibility(View.VISIBLE);
			gvDetailMovement.setValues(mValues.get(mTouchedDayIndex));
			gvDetailMovement.invalidate();

			rctnDetailSection.setVisibility(View.VISIBLE);
		} else {
			if (tvAverageMovementLevel != null) {
				tvAverageMovementLevel.setText("-");
			}

			if (tvAverageMovementString != null) {
				tvAverageMovementString.setText("-");
			}

			rctnDetailSection.setVisibility(View.GONE);
		}
	}

	private void _drawGuideLine(Canvas canvas) {
		Rect textBounds = new Rect();
		String time = String.format("12시");
		mTimePaint.getTextBounds(time, 0, time.length(), textBounds);

		Path path = new Path();
		for (int i = 0; i <= DEFAULT_SEPARATE_SECTION_FOR_MOVEMENT; i++) {
			if (i % 4 == 0) {
				canvas.drawText(mContext.getString(DeviceStatus.getMovementStringResource(i)), mGraphStartX - textBounds.width(), mMinLineHeight - (mMinLineHeight - mMaxLineHeight) / (float)DEFAULT_SEPARATE_SECTION_FOR_MOVEMENT * i + textBounds.height() / 2, mTimePaint);

				// Horizontal Line
				path.moveTo(mGraphStartX - textBounds.width() / 3, mMinLineHeight - (mMinLineHeight - mMaxLineHeight) / (float)DEFAULT_SEPARATE_SECTION_FOR_MOVEMENT * i);
				path.lineTo(mGraphStartX + mWidthBetweenGuideLine * mDayCount + textBounds.width() / 3, mMinLineHeight - (mMinLineHeight - mMaxLineHeight) / (float)DEFAULT_SEPARATE_SECTION_FOR_MOVEMENT * i);
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