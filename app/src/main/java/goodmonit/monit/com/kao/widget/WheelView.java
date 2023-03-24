package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

/**
 * Created by Jake on 2017-07-03.
 */
public class WheelView extends ScrollView {
    public static final String TAG = Configuration.BASE_TAG + "WheelView";
    private static final boolean DBG = Configuration.DBG;

    public static class OnWheelViewListener {
        public void onValueChanged(int selectedIndex, String item) {
        }

        public void onSelectedDate(int year, int month, int day) {
        }

        public void onSelectedTime(int hour, int minute, int second) {
        }

        public void onExpanded() {
        }

        public void onCollapsed() {
        }
    }

    private static final int DEFAULT_OFFSET = 2;
    private static final int DEFAULT_DISPLAY_COUNT = 5;

    private Context mContext;
    private LinearLayout views;

    private List<String> items;
    private int mDisplayItemCount = DEFAULT_DISPLAY_COUNT;
    private int mOffset = DEFAULT_OFFSET;
    private int selectedIndex = 1;
    private int mItemHeight = 0;
    private int initialY;

    private float mDefaultTextSize;
    private int mDefaultTextColor;
    private float mScrollTo;

    private float mSelectedTextSize;
    private int mSelectedTextColor;

    private Runnable scrollerTask;
    private int newCheck = 50;

    private boolean isEnabled = true;

    public WheelView(Context mContext) {
        super(mContext);
        init(mContext);
    }

    public WheelView(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);
        _setAttrs(attrs);
        init(mContext);
    }

    public WheelView(Context mContext, AttributeSet attrs, int defStyle) {
        super(mContext, attrs, defStyle);
        _setAttrs(attrs, defStyle);
        init(mContext);
    }

    private void _setAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WheelViewWidget);
        _setTypeArray(typedArray);
    }

    private void _setAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WheelViewWidget, defStyle, 0);
        _setTypeArray(typedArray);
    }

    private void _setTypeArray(TypedArray typedArray) {
        if (typedArray == null) return;

        mItemHeight = typedArray.getDimensionPixelSize(R.styleable.WheelViewWidget_wvRowHeight, 50);
        mDisplayItemCount = typedArray.getInteger(R.styleable.WheelViewWidget_wvRowCount, DEFAULT_DISPLAY_COUNT);
        mOffset = mDisplayItemCount / 2;
        mDefaultTextColor = typedArray.getColor(R.styleable.WheelViewWidget_wvDefaultTextColor, getResources().getColor(R.color.colorTextPrimaryLight));
        mSelectedTextColor = typedArray.getColor(R.styleable.WheelViewWidget_wvSelectedTextColor, getResources().getColor(R.color.colorTextPrimary));
        mDefaultTextSize = typedArray.getDimensionPixelSize(R.styleable.WheelViewWidget_wvDefaultTextSize, 13);
        mSelectedTextSize = typedArray.getDimensionPixelSize(R.styleable.WheelViewWidget_wvSelectedTextSize, 13);
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> list) {
        if (null == items) {
            items = new ArrayList<String>();
        }
        items.clear();
        items.addAll(list);

        for (int i = 0; i < mOffset; i++) {
            items.add(0, "");
            items.add("");
        }

        initData();
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    private void init(Context context) {
        this.mContext = context;
        this.setVerticalScrollBarEnabled(false);
        this.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        views = new LinearLayout(mContext);
        views.setOrientation(LinearLayout.VERTICAL);
        views.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.addView(views);

        scrollerTask = new Runnable() {
            public void run() {
                int newY = getScrollY();
                if (initialY - newY == 0) { // stopped
                    final int remainder = initialY % mItemHeight;
                    final int divided = initialY / mItemHeight;
                    if (remainder == 0) {
                        selectedIndex = divided + mOffset;
                        onSelectedCallBack();
                    } else {
                        if (remainder > mItemHeight / 2) {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    WheelView.this.smoothScrollTo(0, initialY - remainder + mItemHeight);
                                    selectedIndex = divided + mOffset + 1;
                                    onSelectedCallBack();
                                }
                            });
                        } else {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    WheelView.this.smoothScrollTo(0, initialY - remainder);
                                    selectedIndex = divided + mOffset;
                                    onSelectedCallBack();
                                }
                            });
                        }
                    }
                } else {
                    initialY = getScrollY();
                    WheelView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };

        WheelView.this.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                WheelView.this.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        //WheelView.this.getViewTreeObserver().addOnGlobalLayoutListener(mListenerForScroll);
    }

    public void startScrollerTask() {
        initialY = getScrollY();
        this.postDelayed(scrollerTask, newCheck);
    }

    private void initData() {
        views.removeAllViews();
        for (String item : items) {
            views.addView(createView(item));
        }

        refreshItemView(0);
    }

    public void setDefaultTextSize(float size) {
        mDefaultTextSize = size;
    }

    public void setDefaultTextColor(int color) {
        mDefaultTextColor = color;
    }

    public void setSelectedTextSize(float size) {
        mSelectedTextSize = size;
    }

    public void setSelectedTextColor(int color) {
        mSelectedTextColor = color;
    }

    private TextView createView(String item) {
        NotoTextView tv = new NotoTextView(mContext);
        tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight));
        tv.setSingleLine(true);
        tv.setText(item);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(mDefaultTextColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mDefaultTextSize);
        return tv;
    }

    public void setEnabled(boolean enabled) {
        if (DBG) Log.d(TAG, "setEnabled: " + enabled + ", selectedIdx: " + selectedIndex);
        isEnabled = enabled;

        int childSize = views.getChildCount();
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                continue;
            }
            if (isEnabled) {
                if (selectedIndex == i) {
                    itemView.setTextColor(mSelectedTextColor);
                    itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mSelectedTextSize);
                } else {
                    itemView.setTextColor(mDefaultTextColor);
                    itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mDefaultTextSize);
                }
            } else {
                itemView.setTextColor(mDefaultTextColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mDefaultTextSize);
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (!isEnabled) {
            if (DBG) Log.e(TAG, "onScrollChanged: disabled");
            return;
        }
        refreshItemView(t);

        if (t > oldt) {
            scrollDirection = SCROLL_DIRECTION_DOWN;
        } else {
            scrollDirection = SCROLL_DIRECTION_UP;
        }
    }

    private void refreshItemView(int y) {
        int position = y / mItemHeight + mOffset;
        int remainder = y % mItemHeight;
        int divided = y / mItemHeight;

        if (remainder == 0) {
            position = divided + mOffset;
        } else {
            if (remainder > mItemHeight / 2) {
                position = divided + mOffset + 1;
            }
        }

        int childSize = views.getChildCount();
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                return;
            }
            if (position == i) {
                itemView.setTextColor(mSelectedTextColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mSelectedTextSize);
            } else {
                itemView.setTextColor(mDefaultTextColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mDefaultTextSize);
            }
        }
    }

    private int scrollDirection = -1;
    private static final int SCROLL_DIRECTION_UP = 0;
    private static final int SCROLL_DIRECTION_DOWN = 1;

    private int viewWidth;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (DBG) Log.d(TAG, "onSizeChanged : " + mScrollTo);
        viewWidth = w;
        WheelView.this.smoothScrollTo(0, (int)mScrollTo);
    }

    private void onSelectedCallBack() {
        if (null != onWheelViewListener) {
            if (selectedIndex < items.size()) {
                onWheelViewListener.onValueChanged(selectedIndex - mOffset, items.get(selectedIndex));
            }
        }
    }

    public void setSelection(String item) {
        if (item == null) return;
        int position = 0;
        for (int i = 0; i < items.size(); i++) {
            if (item.equals(items.get(i))) {
                position = i;
                break;
            }
        }
        setSelection(position - mOffset);
    }

    public void setSelection(int position) {
        final int p = position;
        selectedIndex = p + mOffset;
        this.post(new Runnable() {
            @Override
            public void run() {
                mScrollTo = (float)(p * mItemHeight);
                WheelView.this.smoothScrollTo(0, p * mItemHeight);
            }
        });

        for (int i = 0; i < views.getChildCount(); i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                return;
            }
            if (selectedIndex == i) {
                itemView.setTextColor(mSelectedTextColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mSelectedTextSize);
            } else {
                itemView.setTextColor(mDefaultTextColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) mDefaultTextSize);
            }
        }
    }

    public String getSelectedItem() {
        return items.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex - mOffset;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
            case MotionEvent.ACTION_UP:
                startScrollerTask();
                break;
            case MotionEvent.ACTION_DOWN:
                if (isEnabled == false) return false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isEnabled == false) return false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    private OnWheelViewListener onWheelViewListener;

    public OnWheelViewListener getOnWheelViewListener() {
        return onWheelViewListener;
    }

    public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
        this.onWheelViewListener = onWheelViewListener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        WheelView.this.smoothScrollTo(0, (int)mScrollTo);
    }
}