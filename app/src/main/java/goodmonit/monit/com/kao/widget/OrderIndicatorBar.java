package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class OrderIndicatorBar {
	private static final String TAG = Configuration.BASE_TAG + "OrderIndicatorBar";

	private RelativeLayout mIndicatorBarView;
	private int mIndicatorCount;
	private Context mContext;
	private TextView[] tvIndicator;
	private ImageView[] ivPath;

	private int drawableFocused, drawablePassed, drawableNotPassed;
	private int colorPassedPath, colorNotPassedPath;
	private int sizeFocused, sizeDefault;
	private ViewGroup.LayoutParams lpFocused, lpDefault;

	public OrderIndicatorBar(Context context, View v, int count, int focusedRes, int passedRes, int notPassedRes, int passedPathColor, int notPassedPathColor, int focusedResSize, int defaultResSize) {
		mContext = context;
		mIndicatorCount = count;
		tvIndicator = new TextView[5];
		ivPath = new ImageView[5];
		drawableFocused = focusedRes;
		drawablePassed = passedRes;
		drawableNotPassed = notPassedRes;
		colorPassedPath = passedPathColor;
		colorNotPassedPath = notPassedPathColor;

		sizeFocused = focusedResSize;
		sizeDefault = defaultResSize;

		setView(v);
	}

	public void setView(View v) {
		mIndicatorBarView = (RelativeLayout)v.findViewById(R.id.rctn_order_indicator_bar);
		tvIndicator[0] = (TextView)v.findViewById(R.id.tv_order_indicator1);
		tvIndicator[1] = (TextView)v.findViewById(R.id.tv_order_indicator2);
		tvIndicator[2] = (TextView)v.findViewById(R.id.tv_order_indicator3);
		tvIndicator[3] = (TextView)v.findViewById(R.id.tv_order_indicator4);
		tvIndicator[4] = (TextView)v.findViewById(R.id.tv_order_indicator5);

		lpFocused = tvIndicator[0].getLayoutParams();
		lpFocused.width = sizeFocused;
		lpFocused.height = sizeFocused;
		lpDefault = tvIndicator[1].getLayoutParams();
		lpDefault.width = sizeDefault;
		lpDefault.height = sizeDefault;

		ivPath[0] = (ImageView)v.findViewById(R.id.iv_order_indicator_path1);
		ivPath[1] = (ImageView)v.findViewById(R.id.iv_order_indicator_path1);
		ivPath[2] = (ImageView)v.findViewById(R.id.iv_order_indicator_path2);
		ivPath[3] = (ImageView)v.findViewById(R.id.iv_order_indicator_path3);
		ivPath[4] = (ImageView)v.findViewById(R.id.iv_order_indicator_path4);

		initialize();

		setIndicatorCount(mIndicatorCount);
	}

	public void showIndicatorBar(boolean show) {
		if (show) {
			mIndicatorBarView.setVisibility(View.VISIBLE);
		} else {
			mIndicatorBarView.setVisibility(View.GONE);
		}
	}

	public void setIndicatorCount(int count) {
		mIndicatorCount = count;
		for (int i = 0; i < 5; i++) {
			if (i >= mIndicatorCount) {
				tvIndicator[i].setVisibility(View.GONE);
				ivPath[i].setVisibility(View.GONE);
			} else {
				tvIndicator[i].setVisibility(View.VISIBLE);
				ivPath[i].setVisibility(View.VISIBLE);
			}
		}
	}

	public void initialize() {
		for (int i = 0; i < 5; i++) {
			tvIndicator[i].setBackgroundResource(drawableNotPassed);
			ivPath[i].setBackgroundResource(R.color.colorIndicatorLine);
		}
		tvIndicator[0].setBackgroundResource(drawableFocused);
	}

	public void setCurrentItem(int currentItem) {
		if (currentItem > mIndicatorCount) {
			currentItem = mIndicatorCount;
		}

		for (int i = 0; i < 5; i++) {

			if (i == currentItem - 1) {
				if (i == mIndicatorCount - 1) {
					tvIndicator[i].setBackgroundResource(R.drawable.bg_indicator_completed);
					tvIndicator[i].setLayoutParams(lpFocused);
					tvIndicator[i].setText("");
					ivPath[i].setBackgroundResource(colorPassedPath);
				} else {
					tvIndicator[i].setBackgroundResource(drawableFocused);
					tvIndicator[i].setLayoutParams(lpFocused);
					tvIndicator[i].setText(currentItem + "");
					ivPath[i].setBackgroundResource(colorPassedPath);
				}
			} else if (i < currentItem - 1) {
				tvIndicator[i].setBackgroundResource(drawablePassed);
				tvIndicator[i].setLayoutParams(lpDefault);
				tvIndicator[i].setText("");
				ivPath[i].setBackgroundResource(colorPassedPath);
			} else {
				tvIndicator[i].setBackgroundResource(drawableNotPassed);
				tvIndicator[i].setLayoutParams(lpDefault);
				tvIndicator[i].setText("");
				ivPath[i].setBackgroundResource(colorNotPassedPath);
			}
		}

	}
}