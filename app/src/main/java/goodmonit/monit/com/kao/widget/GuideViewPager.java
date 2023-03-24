package goodmonit.monit.com.kao.widget;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class GuideViewPager extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "ViewPager";
	private static final boolean DBG = Configuration.DBG;

	private static final int MAX_VIEW_COUNT = 10;
    private Context mContext;

	private Button btnPrev, btnNext;
	private ArrayList<Integer> mImageResIdList;
	private Button[] btnIndicator;

	private ViewPager mViewPager;
	private PagerAdapter mViewPagerAdapter;

	private int mCurrentViewPageIdx;
	private LayoutInflater mLayoutInflater;

	public GuideViewPager(Context context) {
		super(context);
		mContext = context;
		init();
	}

    public GuideViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
		init();
    }

	public void init() {
		mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = mLayoutInflater.inflate(R.layout.widget_guide_viewpager, this, false);
		addView(v);

		mImageResIdList = new ArrayList<>();
		btnIndicator =  new Button[MAX_VIEW_COUNT];

		mViewPager = (ViewPager)findViewById(R.id.vp_guide_viewpager_images);
		btnPrev = (Button)findViewById(R.id.btn_guide_viewpager_prev);
		btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentViewPageIdx > 0) {
					mCurrentViewPageIdx--;
					mViewPager.setCurrentItem(mCurrentViewPageIdx);
				}
			}
		});
		btnNext = (Button)findViewById(R.id.btn_guide_viewpager_next);
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentViewPageIdx < mImageResIdList.size() - 1) {
					mCurrentViewPageIdx++;
					mViewPager.setCurrentItem(mCurrentViewPageIdx);
				}
			}
		});
		btnIndicator[0] = (Button)findViewById(R.id.btn_guide_viewpager_indicator1);
		btnIndicator[1] = (Button)findViewById(R.id.btn_guide_viewpager_indicator2);
		btnIndicator[2] = (Button)findViewById(R.id.btn_guide_viewpager_indicator3);
		btnIndicator[3] = (Button)findViewById(R.id.btn_guide_viewpager_indicator4);
		btnIndicator[4] = (Button)findViewById(R.id.btn_guide_viewpager_indicator5);
		btnIndicator[5] = (Button)findViewById(R.id.btn_guide_viewpager_indicator6);
		btnIndicator[6] = (Button)findViewById(R.id.btn_guide_viewpager_indicator7);
		btnIndicator[7] = (Button)findViewById(R.id.btn_guide_viewpager_indicator8);
		btnIndicator[8] = (Button)findViewById(R.id.btn_guide_viewpager_indicator9);
		btnIndicator[9] = (Button)findViewById(R.id.btn_guide_viewpager_indicator10);

		mViewPagerAdapter = new PagerAdapter() {
			@Override
			public int getCount() {
				return mImageResIdList.size();
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				if (DBG) Log.d(TAG, "instantiateItem : " + position);
				ImageView v = new ImageView(mContext);
				v.setImageResource(mImageResIdList.get(position));
				container.addView(v);
				return v;
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View)object);
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view.equals(object);
			}
		};

		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {;}

			@Override
			public void onPageSelected(int position) {
				if (DBG) Log.d(TAG, "onPageSelected : " + position);
				setCurrentIndicator(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {;}
		});
	}

	public void setCurrentViewPageIdx(int idx) {
		if (DBG) Log.d(TAG, "setCurrentViewPageIdx : " + idx);
		mCurrentViewPageIdx = idx;
		mViewPager.setCurrentItem(idx);
	}

	public int getCurrentViewPageIdx() {
		return mCurrentViewPageIdx;
	}

	public int getViewCount() {
		return mImageResIdList.size();
	}

	public void setCurrentIndicator(int idx) {
		//if (DBG) Log.d(TAG, "setCurrentIndicator : " + idx);
		for (int i = 0; i < MAX_VIEW_COUNT; i++) {
			if (DBG) Log.d(TAG, "setCurrentIndicator : " + i + " / " + idx + " / " + mImageResIdList.size());
			if (i < mImageResIdList.size()) {
				btnIndicator[i].setVisibility(View.VISIBLE);
				if (i == idx) {
					btnIndicator[i].setSelected(true);
				} else {
					btnIndicator[i].setSelected(false);
				}
			} else {
				btnIndicator[i].setVisibility(View.GONE);
			}
		}

		if (idx >= mImageResIdList.size() - 1) {
			btnNext.setVisibility(View.GONE);
		} else {
			btnNext.setVisibility(View.VISIBLE);
		}

		if (idx <= 0) {
			btnPrev.setVisibility(View.GONE);
		} else {
			btnPrev.setVisibility(View.VISIBLE);
		}

	}

	public void addViewPage(int imageResId) {
		mImageResIdList.add(imageResId);
        mViewPagerAdapter.notifyDataSetChanged();
	}

}