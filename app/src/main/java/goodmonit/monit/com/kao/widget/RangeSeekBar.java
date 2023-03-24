package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class RangeSeekBar extends FrameLayout {
	private static final String TAG = Configuration.BASE_TAG + "RangeSeekbar";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	private int mMinValue = 0;
	private int mMaxValue = 100;
	private ImageView leftThumb, rightThumb;
	private View view;
	private View leftBar, rightBar, middleBar;
	private TextView tvLeftThumb, tvRightThumb;
	private TextView tvLeftThumbScale, tvRightThumbScale;
	private TextView tvLeftThumbIndicator, tvRightThumbIndicator;
	private LinearLayout.LayoutParams leftBarLayoutParams, rightBarLayoutParams, middleBarLayoutParams;
	private LinearLayout llRangeSeekbar;

	private int mLeftThumbPos = 0;
	private int mRightThumbPos = 100;
	private String mStrScale = "";
	private boolean hasDrawn = false;
	private boolean doUpdateView = false;

	private OnRangeSeekBarListener mListener;

	public static class OnRangeSeekBarListener {
		public void onValueChanged(int mMinValue, int mMaxValue, int leftPos, int rightPos) {
		}
	}

	public RangeSeekBar(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		this.mContext = context;
		this.view = inflate(getContext(), R.layout.widget_range_seekbar, null);
		_initView(view);
		addView(this.view);
	}

	public RangeSeekBar(Context context) {
		super(context);
		this.mContext = context;
		this.view = inflate(getContext(), R.layout.widget_range_seekbar, null);
		_initView(view);
		addView(this.view);
	}

	private void _initView(View v) {
		leftThumb = (ImageView)v.findViewById(R.id.left_thumb);
		rightThumb = (ImageView)v.findViewById(R.id.right_thumb);
		tvRightThumb = (TextView)v.findViewById(R.id.tv_range_max);
		tvRightThumbScale = (TextView)v.findViewById(R.id.tv_range_max_scale);
		tvRightThumbIndicator = (TextView)v.findViewById(R.id.tv_range_max_indicator);
		tvLeftThumb = (TextView)v.findViewById(R.id.tv_range_min);
		tvLeftThumbScale = (TextView)v.findViewById(R.id.tv_range_min_scale);
		tvLeftThumbIndicator = (TextView)v.findViewById(R.id.tv_range_min_indicator);
		leftBar = v.findViewById(R.id.left_bar);
		rightBar = v.findViewById(R.id.right_bar);
		middleBar = v.findViewById(R.id.middle_bar);
		leftBarLayoutParams = (LinearLayout.LayoutParams)leftBar.getLayoutParams();
		rightBarLayoutParams = (LinearLayout.LayoutParams)rightBar.getLayoutParams();
		middleBarLayoutParams = (LinearLayout.LayoutParams)middleBar.getLayoutParams();
		llRangeSeekbar = (LinearLayout)v.findViewById(R.id.ll_range_seekbar);

		leftThumb.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int diff = mMaxValue - mMinValue;
				if(diff < 0){
					diff = 100;
					mMinValue = 0;
					mMaxValue = 100;
				}
				float width = llRangeSeekbar.getWidth();
				float gap = leftThumb.getWidth();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					leftThumb.bringToFront();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					leftBarLayoutParams.weight += event.getX()/width;
					middleBarLayoutParams.weight = 1 - (leftBarLayoutParams.weight + rightBarLayoutParams.weight);

					int tempMaxValue = mRightThumbPos;
					int tempMinValue = (int)(diff*leftBarLayoutParams.weight + mMinValue);
					if(tempMinValue > tempMaxValue) {
						tempMinValue = tempMaxValue;
					}
					if(tempMinValue < mMinValue) {
						tempMinValue = mMinValue;
					}

					mLeftThumbPos = tempMinValue;
					if (mListener != null) {
						mListener.onValueChanged(mMinValue, mMaxValue, mLeftThumbPos, mRightThumbPos);
					}

					checkCollision();

					if (middleBarLayoutParams.weight > gap/width && leftBarLayoutParams.weight >= 0) {
						leftBar.setLayoutParams(leftBarLayoutParams);
						middleBar.setLayoutParams(middleBarLayoutParams);
					} else {
						if (leftBarLayoutParams.weight < 0) {
							leftBarLayoutParams.weight = 0;
							middleBarLayoutParams.weight = 1 - (rightBarLayoutParams.weight + leftBarLayoutParams.weight);
						} else {
							middleBarLayoutParams.weight = gap/width + (tempMaxValue - tempMinValue)/(1.0f*diff);
							leftBarLayoutParams.weight = 1 - (middleBarLayoutParams.weight + rightBarLayoutParams.weight);
						}
						leftBar.setLayoutParams(leftBarLayoutParams);
						middleBar.setLayoutParams(middleBarLayoutParams);
					}
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					return true;
				} else {
					return false;
				}
			}
		});

		rightThumb.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int diff = mMaxValue - mMinValue;
				if(diff < 0){
					diff = 100;
					mMinValue = 0;
					mMaxValue = 100;
				}
				float width = llRangeSeekbar.getWidth();
				float gap = leftThumb.getWidth();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					rightThumb.bringToFront();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					rightBarLayoutParams.weight -= (event.getX()/width);
					middleBarLayoutParams.weight = 1 - (rightBarLayoutParams.weight + leftBarLayoutParams.weight);

					int tempMinValue = mLeftThumbPos;
					int tempMaxValue = (int)(diff*(1 - rightBarLayoutParams.weight) + mMinValue);
					if(tempMaxValue < tempMinValue){
						tempMaxValue = tempMinValue;
					}
					if(tempMaxValue > mMaxValue){
						tempMaxValue = mMaxValue;
					}

					mRightThumbPos = tempMaxValue;
					if (mListener != null) {
						mListener.onValueChanged(mMinValue, mMaxValue, mLeftThumbPos, mRightThumbPos);
					}

					checkCollision();

					if (middleBarLayoutParams.weight > gap/width && rightBarLayoutParams.weight >= 0) {
						rightBar.setLayoutParams(rightBarLayoutParams);
						middleBar.setLayoutParams(middleBarLayoutParams);
					} else {
						if (rightBarLayoutParams.weight < 0) {
							rightBarLayoutParams.weight = 0;
							middleBarLayoutParams.weight = 1 - (rightBarLayoutParams.weight + leftBarLayoutParams.weight);
						} else {
							middleBarLayoutParams.weight = gap/width + (tempMaxValue - tempMinValue)/(1.0f*diff);
							rightBarLayoutParams.weight = 1 - (leftBarLayoutParams.weight + middleBarLayoutParams.weight);
						}
						rightBar.setLayoutParams(rightBarLayoutParams);
						middleBar.setLayoutParams(middleBarLayoutParams);
					}
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	public void checkCollision() {
		if (mRightThumbPos - mLeftThumbPos < (mMaxValue - mMinValue) / 10 * 2) {
			if (tvLeftThumb.getVisibility() == View.VISIBLE) {
				tvLeftThumb.setVisibility(View.INVISIBLE);
				tvLeftThumbScale.setVisibility(View.INVISIBLE);
				tvLeftThumbIndicator.setVisibility(View.INVISIBLE);
				tvRightThumb.setVisibility(View.INVISIBLE);
				tvRightThumbScale.setVisibility(View.INVISIBLE);
				tvRightThumbIndicator.setVisibility(View.INVISIBLE);
			}
		} else {
			if (tvLeftThumb.getVisibility() != View.VISIBLE) {
				tvLeftThumb.setVisibility(View.VISIBLE);
				tvLeftThumbScale.setVisibility(View.VISIBLE);
				tvLeftThumbIndicator.setVisibility(View.VISIBLE);
				tvRightThumb.setVisibility(View.VISIBLE);
				tvRightThumbScale.setVisibility(View.VISIBLE);
				tvRightThumbIndicator.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
        hasDrawn = true;
		if (doUpdateView) {
			doUpdateView = false;
			_updateView();
		}
	}

	private void _updateView() {
		if (DBG) Log.d(TAG, "_updateView : " + mMaxValue + " / " + mMinValue + " / " + mLeftThumbPos + " / " + mRightThumbPos);
		int diff = mMaxValue - mMinValue;
		if(diff < 0){
			diff = 100;
			mMinValue = 0;
			mMaxValue = 100;
		}
		float width = llRangeSeekbar.getWidth();
		float gap = leftThumb.getWidth();

		leftBarLayoutParams.weight = (mLeftThumbPos - mMinValue) / (float)diff;
		rightBarLayoutParams.weight = (mMaxValue - mRightThumbPos) / (float)diff;
		middleBarLayoutParams.weight = 1 - (leftBarLayoutParams.weight + rightBarLayoutParams.weight);

		if (DBG) Log.d(TAG, "weight: " + leftBarLayoutParams.weight + " / " + rightBarLayoutParams.weight + " / " + middleBarLayoutParams.weight);

		checkCollision();

		leftBar.setLayoutParams(leftBarLayoutParams);
		middleBar.setLayoutParams(middleBarLayoutParams);
		rightBar.setLayoutParams(rightBarLayoutParams);

		/*
		if (middleBarLayoutParams.weight > gap/width && leftBarLayoutParams.weight >= 0) {
			leftBar.setLayoutParams(leftBarLayoutParams);
			middleBar.setLayoutParams(middleBarLayoutParams);
		} else {
			if (leftBarLayoutParams.weight < 0) {
				leftBarLayoutParams.weight = 0;
				middleBarLayoutParams.weight = 1 - (rightBarLayoutParams.weight + leftBarLayoutParams.weight);
			} else {
				middleBarLayoutParams.weight = gap/width + (mRightThumbPos - mLeftThumbPos) / (1.0f * diff);
				leftBarLayoutParams.weight = 1 - (middleBarLayoutParams.weight + rightBarLayoutParams.weight);
			}
			leftBar.setLayoutParams(leftBarLayoutParams);
			middleBar.setLayoutParams(middleBarLayoutParams);
		}

		if (middleBarLayoutParams.weight > gap/width && rightBarLayoutParams.weight >= 0) {
			rightBar.setLayoutParams(rightBarLayoutParams);
			middleBar.setLayoutParams(middleBarLayoutParams);
		} else {
			if (rightBarLayoutParams.weight < 0) {
				rightBarLayoutParams.weight = 0;
				middleBarLayoutParams.weight = 1 - (rightBarLayoutParams.weight + leftBarLayoutParams.weight);
			} else {
				middleBarLayoutParams.weight = gap/width + (mRightThumbPos - mLeftThumbPos) / (1.0f * diff);
				rightBarLayoutParams.weight = 1 - (leftBarLayoutParams.weight + middleBarLayoutParams.weight);
			}
			rightBar.setLayoutParams(rightBarLayoutParams);
			middleBar.setLayoutParams(middleBarLayoutParams);
		}
		*/
	}

	public void updateView() {
	    if (hasDrawn == false) {
            doUpdateView = true;
        } else {
	        _updateView();
        }
	}

	public int getMinValue() {
		return this.mMinValue;
	}

	public void setMinValue(int mMinValue) {
		this.mMinValue = mMinValue;
		this.mLeftThumbPos = mMinValue;
	}

	public int getMaxValue() {
		return this.mMaxValue;
	}

	public void setMaxValue(int mMaxValue) {
		this.mMaxValue = mMaxValue;
		this.mRightThumbPos = mMaxValue;
	}

	public void setLeftThumbTextView(String text) {
		if (tvLeftThumb != null) {
			tvLeftThumb.setText(text);
		}
	}

	public void setRightThumbTextView(String text) {
		if (tvRightThumb != null) {
			tvRightThumb.setText(text);
		}
	}

	public void setLeftThumbScaleTextView(String text) {
		if (tvLeftThumbScale != null) {
			tvLeftThumbScale.setText(text);
		}
	}

	public void setRightThumbScaleTextView(String text) {
		if (tvRightThumbScale != null) {
			tvRightThumbScale.setText(text);
		}
	}

	public int getLeftThumbPos() {
		return this.mLeftThumbPos;
	}

	public void setLeftThumbPos(int mLeftThumbPos) {
		this.mLeftThumbPos = mLeftThumbPos;
	}

	public int getRightThumbPos() {
		return this.mRightThumbPos;
	}

	public void setRightThumbPos(int mRightThumbPos) {
		this.mRightThumbPos = mRightThumbPos;
	}

	public void setOnValueChangedListener(OnRangeSeekBarListener listener) {
		this.mListener = listener;
	}
}