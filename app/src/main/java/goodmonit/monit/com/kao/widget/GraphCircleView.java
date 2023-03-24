package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.managers.MovementManager;

public class GraphCircleView extends View {
	private static final String TAG = Configuration.BASE_TAG + "GraphCView";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	public static final int GRAPH_MODE_NO_DATA		= 1;
	public static final int GRAPH_MODE_NORMAL_DATA	= 2;
	private int mGraphMode = GRAPH_MODE_NORMAL_DATA;

	private static final int TICK_FOR_24_HOUR = 6 * 60 * 24; // 10초에 1개
	private static final float ANGLE_FOR_1TICK = 1.0f / TICK_FOR_24_HOUR * 360;

	private int mGraphViewWidth, mGraphViewHeight;
	private int mGraphMarginStart, mGraphMarginTop, mGraphMarginBottom, mGraphMarginEnd;
	private int mGraphBottomY, mGraphTopY, mGraphStartX, mGraphEndX, mGraphTopHeight;

	// ArcGraph
	private Paint mArcSleepingGraphPaint;
	private Paint mArcDeepSleepingGraphPaint;
	private Paint mArcDeactivatedGraphPaint;

	// Graph GuideLine
	private Paint mGuideLinePaint, mTimePaint;
	private int mGuideLineColor;
	private float mGuideLineThickness, mGuideLineBoldThickness;
	private float mTimeTextSize;

	// Graph Value Data
	private int mTotalCountDeepSleepSec;
	private int mTotalCountSleepSec;

	private double mMaxValue, mMinValue;
	private int mMaxIndex, mMinIndex;
	private long mBeginTimeMs;

	private ArrayList<Integer> mValues;
	private TextView tvNoData;
	private TextView tvSleepingTimeValue, tvDeepSleepingTimeValue, tvSleepingScoreValue;
	private LinearLayout lctnSleepingScore;

	// Graph Touch
	private int mTouchedX = -1;
	private int mTouchedY = -1;

	public GraphCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mValues = new ArrayList<Integer>();
		setTypes(context, attrs);

		mTouchedX = -1;
		mTouchedY = -1;
	}

	public void setGraphViewMargin(int top, int bottom, int start, int end) {
		mGraphMarginTop = top;
		mGraphMarginBottom = bottom;
		mGraphMarginStart = start;
		mGraphMarginEnd = end;
	}

	private void setTypes(Context context, AttributeSet attrs) {
		TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.GraphView);

		mGraphMarginTop = (int)types.getDimension(R.styleable.GraphView_graphMarginTop, 200);
		mGraphMarginBottom = (int)types.getDimension(R.styleable.GraphView_graphMarginBottom, 100);
		mGraphMarginStart = (int)types.getDimension(R.styleable.GraphView_graphMarginStart, 200);
		mGraphMarginEnd = (int)types.getDimension(R.styleable.GraphView_graphMarginEnd, 200);

		mGuideLineColor = types.getColor(R.styleable.GraphView_graphGuideLineColor, Color.BLACK);
		mGuideLineThickness = types.getDimension(R.styleable.GraphView_graphGuideLineThickness, 1);
		mGuideLineBoldThickness = types.getDimension(R.styleable.GraphView_graphGuideLineBoldThickness, 2);

        /*
         * Arc Graph Paint
         */
		mArcSleepingGraphPaint = new Paint();
		mArcSleepingGraphPaint.setStyle(Paint.Style.FILL);
		mArcSleepingGraphPaint.setAntiAlias(false);
		mArcSleepingGraphPaint.setColor(getResources().getColor(R.color.colorMovementSleepingGraph));

		mArcDeepSleepingGraphPaint = new Paint();
		mArcDeepSleepingGraphPaint.setStyle(Paint.Style.FILL);
		mArcDeepSleepingGraphPaint.setAntiAlias(false);
		mArcDeepSleepingGraphPaint.setColor(getResources().getColor(R.color.colorMovementDeepSleepingGraph));

		/*
		 * Arc Graph Paint
		 */
		mArcDeactivatedGraphPaint = new Paint();
		mArcDeactivatedGraphPaint.setStyle(Paint.Style.FILL);
		mArcDeactivatedGraphPaint.setAntiAlias(false);
		mArcDeactivatedGraphPaint.setColor(getResources().getColor(R.color.colorMovementMovingGraph));

        /*
         * Guide Line Paint
         */
		mGuideLinePaint = new Paint();
		mGuideLinePaint.setStyle(Paint.Style.STROKE);
		mGuideLinePaint.setColor(mGuideLineColor);
		mGuideLinePaint.setStrokeWidth(mGuideLineThickness);
		mGuideLinePaint.setAntiAlias(false);

		/*
		 * Time Paint
		 */
		mTimeTextSize = 12 * getResources().getDisplayMetrics().scaledDensity;
		mTimePaint = new Paint();
		mTimePaint.setColor(getResources().getColor(R.color.colorTextPrimaryLight));
		mTimePaint.setTextSize(mTimeTextSize);
		mTimePaint.setTextAlign(Paint.Align.LEFT);
		mTimePaint.setAntiAlias(false);
	}

	public void setSleepingTimeTextView(TextView tv) {
		tvSleepingTimeValue = tv;
	}

	public void setNoDataTextView(TextView tv) {
		tvNoData = tv;
	}

	public void setDeepSleepingTimeTextView(TextView tv) {
		tvDeepSleepingTimeValue = tv;
	}

	public void setSleepingScoreTextView(TextView tv) {
		tvSleepingScoreValue = tv;
	}

	public void setSleepingScoreContainer(LinearLayout lctn) {
		lctnSleepingScore = lctn;
	}

	public void setValues(ArrayList<Integer> movementLevelList) {
		if (movementLevelList == null) return;
		if (DBG) Log.d(TAG, "setValues Start");
		mTotalCountDeepSleepSec = 0;
		mTotalCountSleepSec = 0;

		mMaxValue = -999;
		mMinValue = 999;
		mMaxIndex = 0;
		mMinIndex = 0;

		if (mValues == null) mValues = new ArrayList<>();
		mValues.clear();

		MovementManager movementMgr = new MovementManager();
		movementMgr.setMovementData(movementLevelList, null);

		mValues = movementMgr.getConvertedMovementData();
		mMaxValue = movementMgr.getMaxMovementLevel();
		mMaxIndex = movementMgr.getMaxMovementLevelIndex();
		mMinValue = movementMgr.getMinMovementLevel();
		mMinIndex = movementMgr.getMinMovementLevelIndex();

		mTotalCountDeepSleepSec = movementMgr.getTotalDeepSleepSec();
		int totalDeepSleepHour = (mTotalCountDeepSleepSec / 60) / 60;
		int totalDeepSleepMinute = (mTotalCountDeepSleepSec / 60) % 60;

		mTotalCountSleepSec = movementMgr.getTotalSleepSec();
		int totalSleepHour = (mTotalCountSleepSec / 60) / 60;
		int totalSleepMinute = (mTotalCountSleepSec / 60) % 60;

		if (mValues == null) {
			if (DBG) Log.d(TAG, "setValues: " + movementLevelList.size() + " -> NULL");
			mGraphMode = GRAPH_MODE_NO_DATA;

			if (tvSleepingTimeValue != null) {
				tvSleepingTimeValue.setText("-");
			}

			if (tvDeepSleepingTimeValue != null) {
				tvDeepSleepingTimeValue.setText("-");
			}

			if (tvSleepingScoreValue != null) {
				tvSleepingScoreValue.setText("-");
			}

		} else {
			if (DBG) Log.d(TAG, "setValues: " + movementLevelList.size() + " -> " + mValues.size());
			mGraphMode = GRAPH_MODE_NORMAL_DATA;

			String deepSleepTime = "";
			if (totalSleepHour == 0) {
				deepSleepTime = totalDeepSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			} else {
				deepSleepTime = totalDeepSleepHour + getResources().getString(R.string.time_elapsed_hour) + totalDeepSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			}

			String sleepTime = "";
			if (totalSleepHour == 0) {
				sleepTime = totalSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			} else {
				sleepTime = totalSleepHour + getResources().getString(R.string.time_elapsed_hour) + totalSleepMinute + getResources().getString(R.string.time_elapsed_minute);
			}

			if (tvSleepingTimeValue != null) {
				tvSleepingTimeValue.setText(sleepTime);
			}

			if (tvDeepSleepingTimeValue != null) {
				tvDeepSleepingTimeValue.setText(deepSleepTime);
			}

			if (tvSleepingScoreValue != null) {
				tvSleepingScoreValue.setText((int)(mTotalCountDeepSleepSec * 100.0f / mTotalCountSleepSec) + "");
			}
		}

		if (DBG) Log.d(TAG, "setValues End");
	}

	private void _setCoordination() {
		int width = getWidth();
		int height = getHeight();

		float diff, space;
		if (width > height) {

			diff = width - height;
			space = height / 8;

			mGraphBottomY = (int)(height - space);
			mGraphTopY = (int)space;
			mGraphStartX = (int)(diff / 2 + space);
			mGraphEndX = (int)(width - diff / 2 - space);
		} else {

			diff = height - width;
			space = width / 8;

			mGraphBottomY = (int)(height - diff / 2 - space);
			mGraphTopY = (int)(diff / 2 + space);
			mGraphStartX = (int)space;
			mGraphEndX = (int)(width - space);
		}

	}

	private void _drawGuideLine(Canvas canvas) {
		int lineWidth = 20;
		int lineMargin = 10;

		final float ANGLE_FOR_1TICK = 360 / 24.0f;

		float angle = 0;
		final float radius = (mGraphEndX - mGraphStartX) / 2.0f;
		final float CENTER_X = mGraphStartX + radius;
		final float CENTER_Y = mGraphTopY + radius;

		float startX, startY, endX, endY;
		float textX, textY;
		double radian;

		for (int i = 0; i < 24; i++) {
			radian = Math.toRadians(angle);
			startX = (float)(CENTER_X + (radius + lineMargin) * Math.sin(radian));
			startY = (float)(CENTER_Y - (radius + lineMargin) * Math.cos(radian));
			endX = (float)(CENTER_X + (radius + lineMargin + lineWidth) * Math.sin(radian));
			endY = (float)(CENTER_Y - (radius + lineMargin + lineWidth) * Math.cos(radian));

			if (i % 2 == 0) {
				Rect textBounds = new Rect();
				String time = String.format("%02d", i);
				mTimePaint.getTextBounds(time, 0, time.length(), textBounds);

				textX = (float)(CENTER_X + (radius + lineMargin * 4 + lineWidth) * Math.sin(radian));
				textY = (float)(CENTER_Y - (radius + lineMargin * 4 + lineWidth) * Math.cos(radian));
				textX -= textBounds.width() / 2;
				textY += textBounds.height() / 2;
				canvas.drawText(time, textX, textY, mTimePaint);
			}
			canvas.drawLine(startX, startY, endX, endY, mGuideLinePaint); // 00
			angle += ANGLE_FOR_1TICK;
		}



//
//		canvas.drawLine(startX, startY, endX, endY, mGuideLinePaint); // 00
//
//
//		canvas.drawLine(mGraphEndX + lineMargin, mGraphTopY + (mGraphBottomY - mGraphTopY) / 2.0f, mGraphEndX + lineMargin + lineWidth, mGraphTopY + (mGraphBottomY - mGraphTopY) / 2.0f, mGuideLinePaint); // 06
//
//		canvas.drawLine(mGraphStartX + (mGraphEndX - mGraphStartX) / 2.0f, mGraphBottomY + lineMargin, mGraphStartX + (mGraphEndX - mGraphStartX) / 2, mGraphBottomY + lineMargin + lineWidth, mGuideLinePaint); // 12
//
//		canvas.drawLine(mGraphStartX - lineMargin, mGraphTopY + (mGraphBottomY - mGraphTopY) / 2.0f, mGraphStartX - lineMargin - lineWidth, mGraphTopY + (mGraphBottomY - mGraphTopY) / 2.0f, mGuideLinePaint); // 18

	}

	private void _drawArcGraph(Canvas canvas, ArrayList<Integer> values) {
		if (values == null) return;

		float currAngle = 270;
		for (int i = 0; i < values.size(); i++) {
			if (currAngle >= 360) currAngle = currAngle - 360;

			//if (DBG) Log.d(TAG, "draw : " + i + " / " + mValues.get(i) + " / " + currAngle);

			switch(values.get(i)) {
				case DeviceStatus.MOVEMENT_SLEEP:
					canvas.drawArc(mGraphStartX, mGraphTopY, mGraphEndX, mGraphBottomY, currAngle, ANGLE_FOR_1TICK, true, mArcSleepingGraphPaint);
					break;
				case DeviceStatus.MOVEMENT_DEEP_SLEEP:
					canvas.drawArc(mGraphStartX, mGraphTopY, mGraphEndX, mGraphBottomY, currAngle, ANGLE_FOR_1TICK, true, mArcDeepSleepingGraphPaint);
					break;
				case DeviceStatus.MOVEMENT_NOT_USING:
				case DeviceStatus.MOVEMENT_DISCONNECTED:
				case DeviceStatus.MOVEMENT_NO_MOVEMENT:
					break;
				default:
					canvas.drawArc(mGraphStartX, mGraphTopY, mGraphEndX, mGraphBottomY, currAngle, ANGLE_FOR_1TICK, true, mArcDeactivatedGraphPaint);
					break;
			}

			currAngle = currAngle + ANGLE_FOR_1TICK;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (DBG) Log.d(TAG, "onDraw Start");
		switch(mGraphMode) {
			case GRAPH_MODE_NO_DATA:
				tvNoData.setVisibility(View.VISIBLE);
				lctnSleepingScore.setVisibility(View.GONE);
				break;
			case GRAPH_MODE_NORMAL_DATA:
				tvNoData.setVisibility(View.GONE);
				lctnSleepingScore.setVisibility(View.VISIBLE);
				break;
		}

		// Draw GuideLine
		_setCoordination();
		_drawGuideLine(canvas);
		_drawArcGraph(canvas, mValues);
		if (DBG) Log.d(TAG, "onDraw End");
	}
}