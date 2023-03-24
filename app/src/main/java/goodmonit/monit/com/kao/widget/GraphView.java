package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class GraphView extends View {
	private static final String TAG = Configuration.BASE_TAG + "GraphView";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	public static final int GRAPH_TYPE_BAR 			= 1;
	public static final int GRAPH_TYPE_LINE 		= 2;

	private static final int GUIDE_LINE_DIVIDER_COUNT	= 5;

	private TextView tvNoData;

	private int mGraphViewWidth, mGraphViewHeight;
	private int mGraphMarginStart, mGraphMarginTop, mGraphMarginBottom, mGraphMarginEnd;
	private int mGraphBottomY, mGraphTopY, mGraphStartX, mGraphEndX, mGraphTopHeight;
	private int[] mPosX, mPosY;

	// BarGraph
	private Paint mBarGraphPaint;
	private float mBarWidth, mBarEmptySpaceWidth;
	private int mBarColor, mBarColorSelected;

	// LineGraph
	private Paint mLineGraphPaint, mPointPaint, mInnerPointPaint;
	private int mLineColor;
	private float mLineGraphThickness;
	private int mPointSize, mPointRadius, mInnerPointRadius;

	// Graph Text
	private Paint mTextPaintXAxis, mTextPaintYAxis, mTextPaintInbox;
	private int mTextColorXAxis, mTextColorYAxis, mTextColorInbox;
	private float mTextSizeXAxis, mTextSizeYAxis, mTextSizeInbox;

	// Graph GuideLine
	private Paint mGuideLinePaint;
	private int mGuideLineColor;
	private float mGuideLineThickness, mGuideLineBoldThickness;

	// Graph Value Data
	private double[] mGraphValueList;
	private int mMaxGraphValue;

	// Graph Days
	private String[] mStrDays;
	private int[] mDays;
	private int mDayCount = 7;

	private int mGraphType = GRAPH_TYPE_BAR;

	// Graph Touch
	private int mTouchedX = -1;
	private int mSelectedIdx = -1;

	// 그래프가 몇개가 한꺼번에 그려지는지 확인하기 위함
	private int showGraphCount;

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		setTypes(context, attrs);
		showGraphCount = 0;

		mTouchedX = -1;
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTouchedX = (int) event.getX();

				if ((mTouchedX > mGraphStartX) && (mTouchedX < mGraphEndX)) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							invalidate();
							return true;
						case MotionEvent.ACTION_MOVE:
							invalidate();
							return true;
					}
				}
				// Including ACTION_UP
				mTouchedX = -1;
				mSelectedIdx = -1;
				invalidate();
				return false;
			}
		});
	}

	public void setGraphViewMargin(int top, int bottom, int start, int end) {
		mGraphMarginTop = top;
		mGraphMarginBottom = bottom;
		mGraphMarginStart = start;
		mGraphMarginEnd = end;
	}

	public void showGraph(int type, boolean show) {
		showGraphCount = 1;
	}

	public void setNoDataTextView(TextView nodata) {
		tvNoData = nodata;
	}

	private void setTypes(Context context, AttributeSet attrs) {
		TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.GraphView);

		mGraphMarginTop = (int)types.getDimension(R.styleable.GraphView_graphMarginTop, 200);
		mGraphMarginBottom = (int)types.getDimension(R.styleable.GraphView_graphMarginBottom, 100);
		mGraphMarginStart = (int)types.getDimension(R.styleable.GraphView_graphMarginStart, 200);
		mGraphMarginEnd = (int)types.getDimension(R.styleable.GraphView_graphMarginEnd, 200);

		mLineColor = types.getColor(R.styleable.GraphView_graphLineColor, Color.BLACK);
		mLineGraphThickness = types.getDimension(R.styleable.GraphView_graphLineThickness, 1);

		mGuideLineColor = types.getColor(R.styleable.GraphView_graphGuideLineColor, Color.BLACK);
		mGuideLineThickness = types.getDimension(R.styleable.GraphView_graphGuideLineThickness, 1);
		mGuideLineBoldThickness = types.getDimension(R.styleable.GraphView_graphGuideLineBoldThickness, 2);

		mBarWidth = types.getDimension(R.styleable.GraphView_graphBarWidth, 0);
		mBarColor = types.getColor(R.styleable.GraphView_graphBarColor, Color.BLACK);
		mBarColorSelected = types.getColor(R.styleable.GraphView_graphBarSelectedColor, Color.BLACK);

		mTextSizeXAxis = types.getDimension(R.styleable.GraphView_graphXAxisTextSize, 15);
		mTextSizeYAxis = types.getDimension(R.styleable.GraphView_graphYAxisTextSize, 15);
		mTextSizeInbox = types.getDimension(R.styleable.GraphView_graphInboxTextSize, 15);

		mTextColorXAxis = types.getColor(R.styleable.GraphView_graphXAxisTextColor, Color.BLACK);
		mTextColorYAxis = types.getColor(R.styleable.GraphView_graphYAxisTextColor, Color.BLACK);
		mTextColorInbox = types.getColor(R.styleable.GraphView_graphInboxTextColor, Color.BLACK);

        /*
         * Point Paint
         */
		mPointPaint = new Paint();
		mPointPaint.setColor(types.getColor(R.styleable.GraphView_graphPointColor, Color.BLACK));
		mPointSize = (int)types.getDimension(R.styleable.GraphView_graphPointSize, 6);
		mPointRadius = mPointSize / 2;

		mInnerPointPaint = new Paint();
		mInnerPointPaint.setColor(Color.WHITE);
		mInnerPointRadius = mPointRadius / 2;

        /*
         * Box Graph Paint
         */
		mBarGraphPaint = new Paint();
		mBarGraphPaint.setStyle(Paint.Style.FILL);
		mBarGraphPaint.setAntiAlias(true);

        /*
         * Line Graph Paint
         */
		mLineGraphPaint = new Paint();
		mLineGraphPaint.setStyle(Paint.Style.STROKE);
		mLineGraphPaint.setAntiAlias(true);

        /*
         * Guide Line Paint
         */
		mGuideLinePaint = new Paint();
		mGuideLinePaint.setStyle(Paint.Style.STROKE);
		mGuideLinePaint.setColor(mGuideLineColor);
		mGuideLinePaint.setStrokeWidth(mGuideLineThickness);
		mGuideLinePaint.setAntiAlias(true);

        /*
         * Text Paint
         */
		mTextPaintInbox = new Paint();
		mTextPaintInbox.setColor(mTextColorInbox);
		mTextPaintInbox.setTextSize(mTextSizeInbox);
		mTextPaintInbox.setTextAlign(Align.CENTER);

		mTextPaintXAxis = new Paint();
		mTextPaintXAxis.setColor(mTextColorXAxis);
		mTextPaintXAxis.setTextSize(mTextSizeXAxis);
		mTextPaintXAxis.setTextAlign(Align.CENTER);

		mTextPaintYAxis = new Paint();
		mTextPaintYAxis.setColor(mTextColorYAxis);
		mTextPaintYAxis.setTextSize(mTextSizeYAxis);
		mTextPaintYAxis.setTextAlign(Align.RIGHT);
	}

	// 가로축 Day값 설정(일자만 포함)
	public void setDays(int[] days, String[] strDays) {
		if (DBG) Log.d(TAG, "setDays size : " + days.length + " / " + strDays.length);
		mDays = days;
		mStrDays = strDays;
		mDayCount = days.length;
	}

	// 세로축 Value값 설정
	public void setValues(double[] values) {
		mGraphValueList = values;
	}

	public void setValues(ArrayList<Double> values) {
		if (DBG) Log.d(TAG, "setValueList size : " + values.size());
		mGraphValueList = new double[values.size()];
		for (int i = 0; i < values.size(); i++) {
			mGraphValueList[i] = values.get(i);
		}
	}

	// Bar그래프인지 Line그래프인지 설정
	public void setGraphType(int type) {
		mGraphType = type;
	}

	private void _setCoordination() {
		int width = getWidth();
		int height = getHeight();
		int valueCount = mGraphValueList.length;
		float gapX, gapY;

		mGraphStartX = mGraphMarginStart;
		mGraphEndX = width - mGraphMarginEnd;
		mGraphTopY = mGraphMarginTop;
		mGraphBottomY = height - mGraphMarginBottom;

		mGraphViewWidth = mGraphEndX - mGraphStartX;
		mGraphViewHeight = mGraphBottomY - mGraphTopY;

		if (mGraphType == GRAPH_TYPE_LINE) { // line그래프와 bar그래프가 달라져야 함
			// Point X좌표
			mPosX = new int[valueCount];
			gapX = (float)(mGraphViewWidth) / (valueCount - 1);
			for (int i = 0; i < valueCount; i++) {
				int x = (int) (mGraphMarginStart + (i * gapX));
				mPosX[i] = x;
			}

			// Point Y좌표
			mPosY = new int [GUIDE_LINE_DIVIDER_COUNT];
			gapY = (float)(mGraphViewHeight) / (GUIDE_LINE_DIVIDER_COUNT - 1);
			for(int i = 0 ; i < GUIDE_LINE_DIVIDER_COUNT; i++) {
				mPosY[i] = (int)(height - (mGraphMarginBottom + (i * gapY)));
			}
			mGraphTopHeight = mGraphBottomY - ((mGraphBottomY - mGraphMarginTop) * 7/8);

		} else if (mGraphType == GRAPH_TYPE_BAR) {
			// Bar StartX좌표
			mPosX = new int[valueCount];
			//if (mBarWidth == 0) {
				mBarWidth = (float)(mGraphViewWidth) / (valueCount * 2 + 1);
				mBarEmptySpaceWidth = mBarWidth;
			//} else {
			//	mBarEmptySpaceWidth = (mGraphViewWidth - mBarWidth * valueCount) / (valueCount + 1);
			//}

			gapX = mBarWidth + mBarEmptySpaceWidth;
			for (int i = 0; i < valueCount; i++) {
				int x = (int)(mGraphStartX + mBarEmptySpaceWidth + (i * gapX));
				mPosX[i] = x;
			}

			// Bar TopY좌표
			mMaxGraphValue = 23;
			mPosY = new int[valueCount];
			gapY = (float)(mGraphViewHeight) / mMaxGraphValue;
			for(int i = 0 ; i < valueCount; i++) {
				if (mGraphValueList[i] > mMaxGraphValue) {
					mPosY[i] = (int)(mGraphBottomY - gapY * mMaxGraphValue);
				} else {
					mPosY[i] = (int) (mGraphBottomY - gapY * mGraphValueList[i]);
				}
			}
			mGraphTopHeight = mGraphBottomY - ((mGraphBottomY - mGraphMarginTop) * 7/8);
		}

		//if (DBG) Log.i(TAG, "setMargin [start]" + mGraphMarginStart + " [end]" + mGraphMarginEnd + " [top]" + mGraphMarginTop + " [bottom]" + mGraphMarginBottom);
		//if (DBG) Log.i(TAG, "setCoordination  [w]" + mGraphViewWidth + " [h]" + mGraphViewHeight + " [startx]" + mGraphStartX + " [endx]" + mGraphEndX + " [topy]" + mGraphTopY + " [bottomy]" + mGraphBottomY + " [toph]" + mGraphTopHeight + " [bar]" + mBarWidth + " [gap]" + mBarEmptySpaceWidth);
	}

	private void _drawGuideVerticalLine(Canvas canvas) {
		if (mGraphType == GRAPH_TYPE_LINE) {
			// Draw vertical line
			mGuideLinePaint.setStrokeWidth(mGuideLineThickness);
			Path path = new Path();
			path.addRect(mGraphStartX, mGraphTopY, mGraphEndX, mGraphBottomY, Path.Direction.CW);
			for (int i = 0; i < mDayCount; i++) {
				path.moveTo(mPosX[i], mGraphTopY);
				path.lineTo(mPosX[i], mGraphBottomY);
			}
			canvas.drawPath(path, mGuideLinePaint);

			// Set path bold vertical line
			if (mDayCount > 20) {
				for (int i = 0; i < mDayCount; i = i + 5) {
					path.moveTo(mPosX[i], mGraphTopY);
					path.lineTo(mPosX[i], mGraphBottomY);
				}
			}
			canvas.drawPath(path, mGuideLinePaint);
		} else if (mGraphType == GRAPH_TYPE_BAR) {
			// Empty
		}
	}

	private void _drawGuideSelectedVertical(Canvas canvas) {

		if (mGraphType == GRAPH_TYPE_LINE) {
			// Empty
		} else if (mGraphType == GRAPH_TYPE_BAR) {
			if (mTouchedX > -1) {
				mGuideLinePaint.setStrokeWidth(mGuideLineThickness);
				Path path = new Path();
				path.moveTo(mTouchedX, mGraphBottomY);
				path.lineTo(mTouchedX, mGraphTopY);
				canvas.drawPath(path, mGuideLinePaint);
			}
		}
	}

	private void _drawGuideHorizontalLine(Canvas canvas) {
		if (mGraphType == GRAPH_TYPE_LINE) {
			// Draw horizontal line
			mGuideLinePaint.setStrokeWidth(mGuideLineThickness);
			Path path = new Path();
			path.addRect(mPosX[0], mPosY[0], mPosX[mDayCount - 1], mPosY[GUIDE_LINE_DIVIDER_COUNT - 1], Path.Direction.CW);
			for (int i = 0; i < GUIDE_LINE_DIVIDER_COUNT; i++) {
				path.moveTo(mPosX[0], mPosY[i]);
				path.lineTo(mPosX[mDayCount - 1], mPosY[i]);
			}
			canvas.drawPath(path, mGuideLinePaint);
		} else if (mGraphType == GRAPH_TYPE_BAR) {
			// Draw horizontal line
			mGuideLinePaint.setStrokeWidth(mGuideLineThickness);
			Path path = new Path();
			int gapY = mGraphViewHeight / mMaxGraphValue;
			//path.addRect(mGraphStartX, mGraphTopY, mGraphEndX, mGraphBottomY, Path.Direction.CW);
			for (int i = 0; i < mMaxGraphValue; i = i + 5) {
				path.moveTo(mGraphStartX, (float)(mGraphBottomY - gapY * i));
				path.lineTo(mGraphEndX, (float)(mGraphBottomY - gapY * i));
			}
			canvas.drawPath(path, mGuideLinePaint);
		}
	}

	private void _drawXAxisText(Canvas canvas) {
		if (mGraphType == GRAPH_TYPE_LINE) {
			mBarWidth = 0;
		}

		int start = 0;
		for(int i = 0 ; i < mDayCount ; i++) {
			if (mDayCount < 20) {
				canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, (float)(mGraphBottomY + (int)(mTextSizeXAxis * 1.5)), mTextPaintXAxis);
			} else {
				if (i == mSelectedIdx) {
					canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, (float)(mGraphBottomY + (int)(mTextSizeXAxis * 1.5)), mTextPaintXAxis);
				} else {
					if (i == 0 && (mSelectedIdx > 2 || mSelectedIdx == -1)) {
						canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, (float)(mGraphBottomY + (int)(mTextSizeXAxis * 1.5)), mTextPaintXAxis);
					} else if (i == mDayCount - 1 && mSelectedIdx < mDayCount - 3) {
						canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, (float)(mGraphBottomY + (int)(mTextSizeXAxis * 1.5)), mTextPaintXAxis);
					} else if (i == mDayCount / 3 && (mSelectedIdx > mDayCount / 3 + 2 || mSelectedIdx < mDayCount / 3 - 2)) {
						canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, (float)(mGraphBottomY + (int)(mTextSizeXAxis * 1.5)), mTextPaintXAxis);
					} else if (i == mDayCount / 3 * 2 && (mSelectedIdx > mDayCount / 3 * 2 + 2 || mSelectedIdx < mDayCount / 3 * 2 - 2)) {
						canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, (float)(mGraphBottomY + (int)(mTextSizeXAxis * 1.5)), mTextPaintXAxis);
					}
				}

				/*
				if (start % 5 == 0) {
					canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, mGraphBottomY + (int)(mTextSizeXAxis * 1.5), mTextPaintXAxis);
				} else {
					// month Last day
					if (i < mDayCount - 1 && mDays[i + 1] == 1) {
						canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, mGraphBottomY + (int)(mTextSizeXAxis * 1.5), mTextPaintXAxis);
						start = 0;
					}
					// First day
					if (mDays[i] == 1) {
						canvas.drawText(mStrDays[i], mPosX[i] + mBarWidth / 2, mGraphBottomY + (int)(mTextSizeXAxis * 1.5), mTextPaintXAxis);
						start = 0;
					}
				}
				*/
				start++;
			}
		}
		//canvas.drawText(mDays[mDayCount - 1] + "", mPosX[mDayCount - 1], mGraphBottomY + (int)(mTextSizeXAxis * 1.5), mTextPaintXAxis);
	}

	private void _drawYAxisText(Canvas canvas) {
		if (mGraphType == GRAPH_TYPE_LINE) {
			// Empty
		} else if (mGraphType == GRAPH_TYPE_BAR) {
			int gapY = mGraphViewHeight / mMaxGraphValue;
			for (int i = 0; i < mMaxGraphValue; i = i + 5) {
				canvas.drawText(i + "", (int)(mGraphStartX * 0.9), (float)(mGraphBottomY - gapY * i), mTextPaintYAxis);
			}
		}
	}

	private void _drawInboxText(Canvas canvas, double value[]) {
		if (mGraphType == GRAPH_TYPE_LINE) {
			mTextPaintInbox.setTextAlign(Align.RIGHT);
			double max = -999;
			for(int i = 0 ; i < value.length ; i++) {
				if (max < value[i]) {
					max = value[i];
				}
			}
			double maxValue = max * 8/7;

			// Bottom value 0 is not needed, Start from 1
			for(int i = 1 ; i < GUIDE_LINE_DIVIDER_COUNT ; i++) {
				double textValue = maxValue * i / (GUIDE_LINE_DIVIDER_COUNT - 1);
				textValue = (int)(textValue * 10) / 10.0;
				canvas.drawText(textValue + "", mGraphStartX - mTextSizeInbox, mPosY[i] + mTextSizeInbox / 2, mTextPaintInbox);
			}
		} else if (mGraphType == GRAPH_TYPE_BAR) {
			if (mGraphValueList.length > 20) {
				if (mSelectedIdx > -1) {
					canvas.drawText((int)value[mSelectedIdx] + "", mPosX[mSelectedIdx] + mBarWidth / 2, mPosY[mSelectedIdx] - mTextSizeInbox / 2, mTextPaintInbox);
				}
			} else {
				for (int i = 0; i < value.length; i++) {
					canvas.drawText((int) value[i] + "", mPosX[i] + mBarWidth / 2, mPosY[i] - mTextSizeInbox / 2, mTextPaintInbox);
				}
			}
		}
	}

	private void _drawUnitText(Canvas canvas, int type) {
		mTextPaintInbox.setTextAlign(Align.LEFT);
		String unit = "(min)";
		/*
		switch(type) {
			case DailyData.TYPE_BREAST_MILK_LEFT:
			case DailyData.TYPE_BREAST_MILK_RIGHT:
				unit = "(" + mContext.getString(R.string.time_elapsed_hour) + ")";
				break;
			case DailyData.TYPE_BREAST_MILK_PUMPED:
			case DailyData.TYPE_MILK:
				unit = "(" + PreferenceManager.getInstance(mContext).getAmountUnit() + ")";
				break;
			case DailyData.TYPE_FOOD:
				unit = "(" + PreferenceManager.getInstance(mContext).getAmountUnit() + ")";
				break;
			case DailyData.TYPE_SLEEPING:
				unit = "(" + mContext.getString(R.string.time_elapsed_hour) + ")";
				break;
			case DailyData.TYPE_DIAPER:
				unit = "(" + mContext.getString(R.string.history_times) + ")";
				break;
			case DailyData.TYPE_HEIGHT:
				unit = "(" + PreferenceManager.getInstance(mContext).getLengthUnit() + ")";
				break;
			case DailyData.TYPE_WEIGHT:
				unit = "(" + PreferenceManager.getInstance(mContext).getWeightUnit() + ")";
				break;
		}
		*/

		canvas.drawText(unit, mGraphStartX, mGraphTopY - mTextSizeInbox, mTextPaintInbox);
	}
	private int[] _convertHeight(double value[]) {
		double max = -999;
		for (int i = 0; i < value.length; i++) {
			if (max < value[i]) {
				max = value[i];
			}
		}

		int[] height = new int[mDayCount];
		for (int i = 0; i < mDayCount; i++) {
			double portion = value[i] / max;
			if (portion > 1) {
				portion = 1;
			}
			height[i] = mGraphBottomY - (int)((mGraphBottomY - mGraphTopHeight) * portion);
		}

		return height;
	}

	private void _drawLineGraph(Canvas canvas, int type, int[] posY) {
		int color = mLineColor;
		//int color = DailyDataIcon.getIconColorResource(mContext, type);

		mLineGraphPaint.setColor(color);
		mLineGraphPaint.setStrokeWidth(mLineGraphThickness);
		mPointPaint.setColor(color);

		float pointRadius = mPointRadius;
		float innerPointRadius = mInnerPointRadius;
		if (mDayCount > 20) {
			pointRadius = mPointRadius / 2.0f;
			innerPointRadius = mInnerPointRadius / 2.0f;
			mLineGraphPaint.setStrokeWidth(mLineGraphThickness / 2.0f);
		}

		for (int i = 0; i < posY.length; i++) {
			if (i < posY.length - 1) {
				canvas.drawLine(mPosX[i], posY[i], mPosX[i + 1], posY[i + 1], mLineGraphPaint);
			}
			canvas.drawCircle(mPosX[i], posY[i], pointRadius, mPointPaint);
			canvas.drawCircle(mPosX[i], posY[i], innerPointRadius, mInnerPointPaint);
		}

	}

	private void _drawBarGraph(Canvas canvas, int type, int[] posY) {
		int color = mBarColor;
		//int color = DailyDataIcon.getIconColorResource(mContext, type);

		mBarGraphPaint.setColor(color);
		for (int i = 0; i < mGraphValueList.length; i++) {
			if (mSelectedIdx == i) {
				mBarGraphPaint.setColor(mBarColorSelected);
			} else {
				mBarGraphPaint.setColor(mBarColor);
			}
			canvas.drawRect(mPosX[i], mPosY[i], mPosX[i] + mBarWidth, mGraphBottomY, mBarGraphPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw GuideLine
		_setCoordination();
		_drawGuideVerticalLine(canvas);
		_drawGuideHorizontalLine(canvas);
		_drawGuideSelectedVertical(canvas);
		_drawXAxisText(canvas);
		_drawYAxisText(canvas);

		double sum = 0;
		for (double d : mGraphValueList) {
			sum += d;
		}
		if (mGraphValueList == null || mGraphValueList.length == 0 || sum == 0) {
			tvNoData.setVisibility(View.VISIBLE);
			return;
		} else {
			tvNoData.setVisibility(View.GONE);
		}

		int graphCategory = 0;
		switch(mGraphType) {
			case GRAPH_TYPE_BAR:
				if (mTouchedX == -1) {
					mSelectedIdx = -1;
				} else {
					mSelectedIdx = (int)((mTouchedX - mGraphStartX - mBarEmptySpaceWidth / 2) / (mBarWidth + mBarEmptySpaceWidth));
					if (mSelectedIdx >= mGraphValueList.length) {
						mSelectedIdx = -1;
					}
				}
				if (DBG) Log.d(TAG, "selected idx : " + mSelectedIdx);
				_drawBarGraph(canvas, graphCategory, _convertHeight(mGraphValueList));
				//if (showGraphCount == 1) {
					_drawInboxText(canvas, mGraphValueList);
					//_drawUnitText(canvas, graphCategory);
				//}
				break;
			case GRAPH_TYPE_LINE:
				_drawLineGraph(canvas, graphCategory, _convertHeight(mGraphValueList));
				if (showGraphCount == 1) {
					_drawInboxText(canvas, mGraphValueList);
					_drawUnitText(canvas, graphCategory);
				}
				break;
		}
	}
}