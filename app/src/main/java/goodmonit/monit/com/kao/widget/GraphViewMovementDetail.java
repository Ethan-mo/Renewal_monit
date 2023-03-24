package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceStatus;

public class GraphViewMovementDetail extends View {
	private static final String TAG = Configuration.BASE_TAG + "GraphView";
	private static final boolean DBG = Configuration.DBG;
    private Context mContext;

	public static final int GRAPH_MODE_NO_DATA		= 1;
	public static final int GRAPH_MODE_SAME_DATA	= 2;
	public static final int GRAPH_MODE_NORMAL_DATA	= 3;

	private static final float SEPARATE_SECTION = 9;

	private static final int DEFAULT_SEPARATE_SECTION_FOR_10SEC = 8642;

	/**
	 * Related to draw object
	 */
	private Paint mGuideLinePaint, mTimePaint, mSelectedTimePaint, mLineGraphPaint, mBackgroundGraphPaint;
	private float mTimeTextSize;

	/**
	 * Related to Coordinations
	 */
	private int mWidth, mHeight;
	private int mMaxGraphHeight, mMinGraphHeight;
	private int mMaxLineHeight, mMinLineHeight;
	private int mTouchedX;
	private float mWidthBetweenGuideLine;
	private float mGraphStartX, mGraphEndX;
	private float mGraphWidth;
	private float mWidthBetweenDot;

	/**
	 * Related to Values
	 */
	private double mMaxValue, mMinValue;
	private int mMaxIndex, mMinIndex;
	private long mBeginTimeMs;

	private ArrayList<Integer> mValues;

	private int mGraphMode = GRAPH_MODE_NORMAL_DATA;

	private float mAverageMovementLevel;

	public GraphViewMovementDetail(Context context) {
		super(context);
		mContext = context;
		init();
	}

    public GraphViewMovementDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
		init();
    }

    public void init() {
		mValues = new ArrayList<Integer>();

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
		mTimePaint.setTextAlign(Align.LEFT);
		mTimePaint.setAntiAlias(true);

		mSelectedTimePaint = new Paint();
		mSelectedTimePaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));
		mSelectedTimePaint.setTextSize(mTimeTextSize);
		mSelectedTimePaint.setTextAlign(Align.CENTER);
		mSelectedTimePaint.setAntiAlias(true);

		/*
         * Line Graph Paint
         */
		mLineGraphPaint = new Paint();
		mLineGraphPaint.setStyle(Paint.Style.STROKE);
		mLineGraphPaint.setAntiAlias(true);
		mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategory));

		/*
		 * Background Graph Paint
		 */
		mBackgroundGraphPaint = new Paint();
		mBackgroundGraphPaint.setStyle(Paint.Style.STROKE);
		mBackgroundGraphPaint.setAntiAlias(true);
		mBackgroundGraphPaint.setColor(getResources().getColor(R.color.colorGreyTransparent));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		mMaxGraphHeight = (int)(mHeight * 0.05);
		mMinGraphHeight = (int)(mHeight * 0.95);
		mMaxLineHeight = (int)(mMinGraphHeight * 0.05);
		mMinLineHeight = (int)(mMinGraphHeight * 0.90);
		mGraphStartX = (int)(mWidth / SEPARATE_SECTION);
		mGraphEndX = mWidth - mGraphStartX;
		mWidthBetweenGuideLine = mGraphStartX;
		mGraphWidth = mWidth - mWidthBetweenGuideLine * 2;
		mWidthBetweenDot = mGraphWidth / DEFAULT_SEPARATE_SECTION_FOR_10SEC;

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

	public void setValues(ArrayList<Integer> movementLevelList) {
		if (movementLevelList == null) return;

		mValues = movementLevelList;

		if (mValues == null) {
			if (DBG) Log.d(TAG, "setValues: " + movementLevelList.size() + " -> NULL");
		} else {
			if (DBG) Log.d(TAG, "setValues: " + movementLevelList.size() + " -> " + mValues.size());
		}

		if (mValues == null) {
			mGraphMode = GRAPH_MODE_NO_DATA;
		} else {
			mGraphMode = GRAPH_MODE_NORMAL_DATA;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw GuideLine
		_drawGuideLine(canvas);

		switch(mGraphMode) {
			case GRAPH_MODE_NO_DATA:
				this.setVisibility(View.GONE);
				break;
			case GRAPH_MODE_SAME_DATA:
			case GRAPH_MODE_NORMAL_DATA:
				if (mValues != null) {
					if (mValues.size() > 0) {
						_drawLineGraphV2(canvas);
					}
				}
				break;
		}
	}

	private void _drawLineGraphV2(Canvas canvas) {
		float beforeHeight = 0;
		float nowHeight = 0;
		float nowWidth;
		int prev = DeviceStatus.MOVEMENT_DISCONNECTED;
		boolean isContinuousGraph = false;

		// 선 24개 그리기

		float minHeight = -1;
		float maxHeight = -1;
		float avgHeight = -1;
		int integrationRange = 6 * 10; // 10분
		mBackgroundGraphPaint.setStrokeWidth(5);
		mLineGraphPaint.setStrokeWidth(5);
		int max = -999;
		int min = 999;
		int curr, cal;
		int sum = 0;
		int cntIdx = 0;
		float anchorWidth = -1;
		float anchorHeight = -1;
		float cubicX1 = -1;
		float cubicX2 = -1;
		float cubicY1 = -1;
		float cubicY2 = -1;
		float dividerWidth = (mWidth - mWidthBetweenGuideLine * 2) / (float)DEFAULT_SEPARATE_SECTION_FOR_10SEC * integrationRange;

		Path backgroundPath = new Path();
		Path avgLinePath = new Path();
		for (int idx = 0; idx < mValues.size(); idx++) {
			curr = mValues.get(idx);

			if (curr == DeviceStatus.MOVEMENT_NOT_USING || curr == DeviceStatus.MOVEMENT_DEEP_SLEEP || curr == DeviceStatus.MOVEMENT_SLEEP) {
				cal = 0;
			} else {
				cal = curr;
			}

			if (cal >= 0) {
				cntIdx++;
				sum = sum + cal;
				if (min > cal) min = cal;
				if (max < cal) max = cal;
			}

			if (idx % integrationRange != 0 || idx == 0) continue;

			if (min == 999 && max == -999) { // 전부 데이터가 끊어져있으면 그릴 필요없음
				anchorWidth = -1;
				anchorHeight = -1;
				continue;
			}

			nowWidth = mGraphStartX + dividerWidth * ((idx / integrationRange) - 1);
			avgHeight = (float) (mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (sum / (float)cntIdx) / 15));
			nowHeight = avgHeight;

			if (anchorWidth == -1 || anchorHeight == -1) {
				anchorWidth = nowWidth;
				anchorHeight = nowHeight;
				avgLinePath.moveTo(anchorWidth, anchorHeight);
			}

			cubicX1 = (nowWidth + anchorWidth) / 2.0f;
			cubicY1 = anchorHeight;

			cubicX2 = (nowWidth + anchorWidth) / 2.0f;
			cubicY2 = nowHeight;

			avgLinePath.cubicTo(cubicX1, cubicY1, cubicX2, cubicY2, nowWidth, nowHeight);

			backgroundPath.moveTo(anchorWidth, anchorHeight);
			backgroundPath.lineTo(nowWidth, nowHeight);

			max = -999;
			min = 999;
			cntIdx = 0;
			sum = 0;
			anchorWidth = nowWidth;
			anchorHeight = avgHeight;
		}

		canvas.drawPath(avgLinePath, mLineGraphPaint);
	}

	private void _drawLineGraph(Canvas canvas) {
		float beforeHeight = 0;
		float nowHeight = 0;
		float width = mGraphStartX;
		int prev = DeviceStatus.MOVEMENT_DISCONNECTED;
		boolean isContinuousGraph = false;

		int firstValue = mValues.get(0);
		if (firstValue == DeviceStatus.MOVEMENT_NOT_USING || firstValue == DeviceStatus.MOVEMENT_DEEP_SLEEP || firstValue == DeviceStatus.MOVEMENT_SLEEP) {
			beforeHeight = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (0 - mMinValue) / 15));
		} else {
			beforeHeight = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (firstValue - mMinValue) / 15));
		}

		// 각 점에 맞춰서 그래프 그리기
		float anchorWidth = width;
		float anchorHeight = -1;
		int integrationRange = 6;
		int max = -999;
		int min = 999;
		int curr;
		for (int idx = 0; idx < mValues.size(); idx++) {
//			curr = mValues.get(idx);

			if (max < mValues.get(idx)) {
				max = mValues.get(idx);
			}
			if (idx % integrationRange != 0) continue;
			curr = max;
			max = -999;

			if (curr == prev && idx < mValues.size() - 1) {
				// 같은경우, 아직 그리지 않고, 좌표를 지정함
				if (isContinuousGraph == false) {
					anchorWidth = width;
					anchorHeight = beforeHeight;
					isContinuousGraph = true;
					if (DBG) Log.d(TAG, "set isContinuousGraph i: " + idx + " / w: " + width + " / h: " + beforeHeight + " / v: " + curr);
				}
			} else {
				// 달라질경우, 이전까지를 한꺼번에 그림
				if (curr == DeviceStatus.MOVEMENT_NOT_USING || curr == DeviceStatus.MOVEMENT_DEEP_SLEEP || curr == DeviceStatus.MOVEMENT_SLEEP) {
					nowHeight = (float)(mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (0 - mMinValue) / 15));
				} else {
					nowHeight = (float) (mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) * (1 - (curr - mMinValue) / 15));
				}
				if (DBG) Log.d(TAG, "_drawLineGraph start w: " + anchorWidth + "-> " + width + " / h: " + anchorHeight + " / v: " +  + curr);

				if (prev != DeviceStatus.MOVEMENT_DISCONNECTED) { // 이전에 끊어져있었다면 그래프를 그릴 필요 없음(prev == DeviceStatus.MOVEMENT_DISCONNECTED)
					Path path = new Path();
					if (anchorHeight == -1) anchorHeight = beforeHeight;
					//if (anchorWidth == -1) anchorWidth = width - mWidthBetweenDot;
					if (anchorWidth == -1) anchorWidth = width - mWidthBetweenDot * integrationRange;

					path.moveTo(anchorWidth, anchorHeight);
					path.lineTo(width, anchorHeight);

					// 색지정
					mLineGraphPaint.setColor(getResources().getColor(DeviceStatus.getMovementColorResource(prev)));
					mBackgroundGraphPaint.setColor(getResources().getColor(DeviceStatus.getMovementColorResource(prev)));

					/*
					if (prev == DeviceStatus.MOVEMENT_DEEP_SLEEP) {
						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategoryTransparent));
					} else {
						mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextNotSelected));
					}
					*/
					canvas.drawPath(path, mLineGraphPaint);
					//canvas.drawRect(anchorWidth, anchorHeight, width, mMinLineHeight, mBackgroundGraphPaint);

					if (curr != DeviceStatus.MOVEMENT_DISCONNECTED) { // 지금 끊어졌다면, 이전까지 그래프를 그리고, 세로선 그래프를 그리지 말아야함
						Path verticalPath = new Path();
						verticalPath.moveTo(width, anchorHeight);
						verticalPath.lineTo(width, nowHeight);
						//mLineGraphPaint.setColor(getResources().getColor(R.color.colorTextNotSelected));
						//canvas.drawPath(verticalPath, mLineGraphPaint);
					}
				}

				isContinuousGraph = false;
				anchorHeight = -1;
				anchorWidth = -1;
			}

			// 좌표 업데이트
			width += mWidthBetweenDot * integrationRange;
			beforeHeight = nowHeight;
			prev = curr;
		}
	}

	private void _drawBackgroundBoxGraph(Canvas canvas) {
		float width = mWidth / SEPARATE_SECTION;
		int prev = DeviceStatus.MOVEMENT_DISCONNECTED;

		int continuousCount = 0;
		float startHorizontalPoint = -1;
		for (int curr : mValues) {
			if (prev == DeviceStatus.MOVEMENT_DEEP_SLEEP && curr == DeviceStatus.MOVEMENT_DEEP_SLEEP) {
				continuousCount++; // 10초에 1증가
				if (startHorizontalPoint == -1) {
					startHorizontalPoint = width;
				}
			} else {
				if (continuousCount > 0) {
					mBackgroundGraphPaint.setColor(getResources().getColor(R.color.colorTextDiaperCategoryTransparent));
					canvas.drawRect(startHorizontalPoint, mMaxLineHeight, width, mMinLineHeight, mBackgroundGraphPaint);
					continuousCount = 0;
					startHorizontalPoint = -1;
				}
			}

			prev = curr;
			width += mWidthBetweenDot;
		}
	}

	private void _drawGuideLine(Canvas canvas) {
		int maxVerticalValue = 12;
		int cntHorizontalLines = 4;
		int maxHorizontalValue = 24;

		Rect textBounds = new Rect();
		String time = String.format("12시");
		mTimePaint.getTextBounds(time, 0, time.length(), textBounds);

		Path path = new Path();
		for (int i = 0; i <= maxVerticalValue; i++) {
			if (i % cntHorizontalLines == 0) {
				if (i < maxVerticalValue) {
					canvas.drawText(mContext.getString(DeviceStatus.getMovementStringResource(maxVerticalValue - i)), mGraphStartX - textBounds.width(), mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / (float)maxVerticalValue * (i + 2) + textBounds.height() / 2, mSelectedTimePaint);
				}
				// Horizontal Line
				path.moveTo(mGraphStartX - textBounds.width() / 3, mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / (float)maxVerticalValue * i);
				path.lineTo(mGraphEndX + textBounds.width() / 3, mMaxLineHeight + (mMinLineHeight - mMaxLineHeight) / (float)maxVerticalValue * i);
			}
		}

		float dividerWidth = (mWidth - mWidthBetweenGuideLine * 2) / cntHorizontalLines;
		for (int i = 0; i <= cntHorizontalLines; i++) {
			path.moveTo(mGraphStartX + dividerWidth * i, mMaxLineHeight);
			path.lineTo(mGraphStartX + dividerWidth * i, mMinLineHeight + textBounds.height() * 1.5f);
			if (i < cntHorizontalLines) {
				canvas.drawText(String.format("%02d"+getResources().getString(R.string.time_hour), i * 6), mGraphStartX + (dividerWidth * i) + 10, mMinLineHeight + textBounds.height(), mTimePaint);
			}
		}

		mGuideLinePaint.setStrokeWidth(1);
		canvas.drawPath(path, mGuideLinePaint);
	}
}