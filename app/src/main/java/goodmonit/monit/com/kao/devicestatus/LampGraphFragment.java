package goodmonit.monit.com.kao.devicestatus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceLampActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceLamp;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.devices.LampGraphInfo;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.EnvironmentGraphView;
import goodmonit.monit.com.kao.widget.GraphViewEnvironment;

public class LampGraphFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "LampGraph";
	private static final boolean DBG = Configuration.DBG;

	private static final int VIEW_GRAPH_TEMPERATURE     = 1;
	private static final int VIEW_GRAPH_HUMIDITY  		= 2;
	private static final int VIEW_GRAPH_VOC			  	= 3;

	private Button btnTabTemperature, btnTabHumidity, btnTabVoc;
	private Button btnPrev, btnNext;
	private TextView tvDate, tvDay;
	private TextView tvGraphMaxValue, tvGraphMinValue, tvNoData;
	private LinearLayout lctnDuration;

	private RelativeLayout rctnAverage1, rctnAverage2, rctnAverage3;
	private TextView tvAverageTitle1, tvAverageTitle2, tvAverageTitle3;
	private TextView tvAverageContent1, tvAverageContent2, tvAverageContent3;
	private TextView tvAverageContentScale1, tvAverageContentScale2, tvAverageContentScale3;

	private RelativeLayout rctnDetailSection;
	private TextView tvDetailDate;
	private RelativeLayout rctnDetail1, rctnDetail2, rctnDetail3, rctnDetail4, rctnDetailGraph;
	private TextView tvDetailTitle1, tvDetailTitle2, tvDetailTitle3, tvDetailTitle4;
	private TextView tvDetailContent1, tvDetailContent2, tvDetailContent3, tvDetailContent4;
	private TextView tvDetailContentScale1, tvDetailContentScale2, tvDetailContentScale3, tvDetailContentScale4;

	/** UI Resources */
	private RelativeLayout rctnGraph;

	private int mCurrentViewIndex;

	private long mTodayLocalTimeMs, mCurrentLocalTimeMs;

	private DeviceLamp mLamp;
	private ArrayList<LampGraphInfo> mGraphInfoList = new ArrayList<>();
	private ArrayList<Double> mGraphTemperatureList = new ArrayList<>();
	private ArrayList<Double> mGraphHumidityList = new ArrayList<>();
	private ArrayList<Double> mGraphVocList = new ArrayList<>();
	private ArrayList<Double> mGraphScoreList = new ArrayList<>();

	private GraphViewEnvironment gvGraph;
	private EnvironmentCheckManager mEnvironmentMgr;

	private int mTemperatureUnit = 0; // Celcius
	private String mStrTemperatureUnit; // Celcius

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_lamp_graph, container, false);

		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mDatabaseMgr = DatabaseManager.getInstance(mContext);
		mEnvironmentMgr = new EnvironmentCheckManager(mContext);
		mScreenInfo = new ScreenInfo(1102);
		mMainActivity = getActivity();
        _initView(view);

		mCurrentViewIndex = VIEW_GRAPH_TEMPERATURE;
		_selectTabButton(mCurrentViewIndex);

		long utcNow = System.currentTimeMillis();
		long localNow = DateTimeUtil.convertUTCToLocalTimeMs(utcNow);
		mCurrentLocalTimeMs = DateTimeUtil.getDayBeginMillis(localNow);
		mTodayLocalTimeMs = DateTimeUtil.getDayBeginMillis(localNow);

		mLamp = ((DeviceLampActivity)mMainActivity).getLampObject();

		updateView();

        return view;
    }

	private void _initView(View v) {
		btnPrev = (Button)v.findViewById(R.id.btn_graph_prev);
		btnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentLocalTimeMs -= DateTimeUtil.ONE_DAY_MILLIS;
				updateView();
			}
		});

		btnNext = (Button)v.findViewById(R.id.btn_graph_next);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentLocalTimeMs += DateTimeUtil.ONE_DAY_MILLIS;
				updateView();
			}
		});

		tvDate = (TextView)v.findViewById(R.id.tv_graph_date);
		tvDay = (TextView)v.findViewById(R.id.tv_graph_day);

		lctnDuration = (LinearLayout)v.findViewById(R.id.lctn_graph_duration);

		rctnAverage1 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_average1);
		rctnAverage2 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_average2);
		rctnAverage3 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_average3);

		tvAverageTitle1 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average1_title);
		tvAverageTitle2 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average2_title);
		tvAverageTitle3 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average3_title);

		tvAverageContent1 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average1_content);
		tvAverageContent2 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average2_content);
		tvAverageContent3 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average3_content);

		tvAverageContentScale1 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average1_content_scale);
		tvAverageContentScale2 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average2_content_scale);
		tvAverageContentScale3 = (TextView)v.findViewById(R.id.tv_graph_detail_information_average3_content_scale);

		rctnDetailSection = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information);
		rctnDetailSection.setVisibility(View.GONE);

		tvDetailDate = (TextView)v.findViewById(R.id.tv_graph_detail_information_date);

		rctnDetailGraph = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_detail_graph);
		rctnDetail1 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_detail1);
		rctnDetail2 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_detail2);
		rctnDetail3 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_detail3);
		rctnDetail4 = (RelativeLayout)v.findViewById(R.id.rctn_graph_detail_information_detail4);

		tvDetailTitle1 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail1_title);
		tvDetailTitle2 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail2_title);
		tvDetailTitle3 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail3_title);
		tvDetailTitle4 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail4_title);

		tvDetailContent1 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail1_content);
		tvDetailContent2 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail2_content);
		tvDetailContent3 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail3_content);
		tvDetailContent4 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail4_content);

		tvDetailContentScale1 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail1_content_scale);
		tvDetailContentScale2 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail2_content_scale);
		tvDetailContentScale3 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail3_content_scale);
		tvDetailContentScale4 = (TextView)v.findViewById(R.id.tv_graph_detail_information_detail4_content_scale);

		tvGraphMaxValue = (TextView)v.findViewById(R.id.tv_graph_max_value);
		tvGraphMinValue = (TextView)v.findViewById(R.id.tv_graph_min_value);
		tvNoData = (TextView)v.findViewById(R.id.tv_graph_no_data);

		btnTabTemperature = (Button)v.findViewById(R.id.btn_graph_tab_temperature);
		btnTabTemperature.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_selectTabButton(VIEW_GRAPH_TEMPERATURE);
				updateView();
			}
		});

		btnTabHumidity = (Button)v.findViewById(R.id.btn_graph_tab_humidity);
		btnTabHumidity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_selectTabButton(VIEW_GRAPH_HUMIDITY);
				updateView();
			}
		});

		btnTabVoc = (Button)v.findViewById(R.id.btn_graph_tab_voc);
		btnTabVoc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_selectTabButton(VIEW_GRAPH_VOC);
				updateView();
			}
		});
		btnTabVoc.setVisibility(View.GONE);

		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			btnTabVoc.setVisibility(View.GONE);
			btnTabTemperature.setText("");
			btnTabHumidity.setText("");
			btnTabVoc.setText("");
		}

		rctnGraph = (RelativeLayout)v.findViewById(R.id.rctn_graph);
		gvGraph = (GraphViewEnvironment) v.findViewById(R.id.gv_environment_graph);
		gvGraph.setCurrentTimeTextView(tvDetailDate);
		gvGraph.setCurrentValueTextView(tvDetailContent1);
		gvGraph.setAverageValueTextView(tvAverageContent2);
		gvGraph.setMaxValueTextView(tvGraphMaxValue);
		gvGraph.setMinValueTextView(tvGraphMinValue);
		gvGraph.setNoDataTextView(tvNoData);
		gvGraph.setDetailSection(rctnDetailSection);
		gvGraph.setDetailDateTextView(tvDetailDate);
    }

	private void _selectTabButton(int position) {
		mCurrentViewIndex = position;
		btnTabTemperature.setSelected(false);
		btnTabHumidity.setSelected(false);
		btnTabVoc.setSelected(false);
		btnTabTemperature.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
		btnTabHumidity.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
		btnTabVoc.setTextColor(getResources().getColor(R.color.colorTextNotSelected));

		switch (position) {
			case VIEW_GRAPH_TEMPERATURE:
				btnTabTemperature.setSelected(true);
				btnTabTemperature.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
				break;
			case VIEW_GRAPH_HUMIDITY:
				btnTabHumidity.setSelected(true);
				btnTabHumidity.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
				break;
			case VIEW_GRAPH_VOC:
				btnTabVoc.setSelected(true);
				btnTabVoc.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
				break;
		}
	}

    public void updateView() {
		if (DBG) Log.d(TAG, "updateView Start");
		if (mLamp == null) return;

		_updateDate();

		long utcBeginSec = DateTimeUtil.convertLocalToUTCTimeMs(mCurrentLocalTimeMs) / 1000;
		long utcEndSec = utcBeginSec + DateTimeUtil.ONE_DAY_MILLIS / 1000 - 1;

		_loadGraphData(utcBeginSec, utcEndSec);

		if (mCurrentViewIndex == VIEW_GRAPH_TEMPERATURE) {
			_updatePreference();
			_updateTemperatureGraph();

		} else if (mCurrentViewIndex == VIEW_GRAPH_HUMIDITY) {
			_updateHumidityGraph();

		}

        gvGraph.setBeginTimeMs(DateTimeUtil.convertLocalToUTCTimeMs(mCurrentLocalTimeMs));
        gvGraph.invalidate();
	}

	private void _updatePreference() {
		mStrTemperatureUnit = PreferenceManager.getInstance(mContext).getTemperatureScale();
		if (mStrTemperatureUnit.equals(mContext.getString(R.string.unit_temperature_celsius))) {
			mTemperatureUnit = 0;
		} else {
			mTemperatureUnit = 1;
		}
	}

	private void _loadGraphData(long utcBeginSec, long utcEndSec) {
		if (mLamp == null) return;
		mGraphInfoList = mDatabaseMgr.getLampGraphInfoList(mLamp.deviceId, utcBeginSec, utcEndSec);
		if (DBG) Log.d(TAG, "loadGraphData : " + utcBeginSec + " / " + DateTimeUtil.getNotConvertedDateTime(utcBeginSec * 1000)
				+ " ~ " + utcEndSec + " / " + DateTimeUtil.getNotConvertedDateTime(utcEndSec * 1000)
				+ " / " + mGraphInfoList.size());

		mGraphScoreList.clear();
		mGraphTemperatureList.clear();
		mGraphHumidityList.clear();
		mGraphVocList.clear();

		double temperature = -999;
		double humidity = -999;
		double voc = -999;
		double score = -999;
		long timeSec = 0;

		// 초기화
		for (int i = 0; i < 6 * 24; i++) {
			mGraphScoreList.add(score);
			mGraphTemperatureList.add(temperature);
			mGraphHumidityList.add(humidity);
			mGraphVocList.add(voc);
		}

		int idx = 0;
		for (int i = 0; i < mGraphInfoList.size(); i++) {
			idx = (int)(mGraphInfoList.get(i).timeSec - utcBeginSec) / (60 * 10);
			score = mGraphInfoList.get(i).score;
			temperature = mGraphInfoList.get(i).temperature;
			humidity = mGraphInfoList.get(i).humidity;
			voc = mGraphInfoList.get(i).voc;

			if (score != -999) {
				mGraphScoreList.set(idx, score);
			}
			if (temperature != -999) {
				temperature = temperature / 100.0;
				if (mTemperatureUnit == 0) { // Celcius
					temperature = (int)(temperature * 10) / 10.0;
					mGraphTemperatureList.set(idx, temperature);
				} else { // Fahrenheit
					temperature = UnitConvertUtil.getFahrenheitFromCelsius((float)temperature);
					mGraphTemperatureList.set(idx, temperature);
				}
			}
			if (humidity != -999) {
				humidity = humidity / 100.0;
				humidity = (int)(humidity * 10) / 10.0;
				mGraphHumidityList.set(idx, humidity);
			}
			if (voc != -999 && voc != -1) {
				mGraphVocList.set(idx, voc / 100.0);
			}
		}
	}

	private void _updateTemperatureGraph() {
		lctnDuration.setVisibility(View.GONE);

		rctnDetailSection.setBackgroundColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
		rctnDetailGraph.setVisibility(View.GONE);
		rctnAverage1.setVisibility(View.VISIBLE);
		rctnAverage2.setVisibility(View.VISIBLE);
		rctnAverage3.setVisibility(View.GONE);
		rctnDetail1.setVisibility(View.VISIBLE);
		rctnDetail2.setVisibility(View.GONE);
		rctnDetail3.setVisibility(View.GONE);
		rctnDetail4.setVisibility(View.GONE);

		tvGraphMaxValue.setVisibility(View.VISIBLE);
		tvGraphMinValue.setVisibility(View.VISIBLE);

		tvAverageTitle1.setText(R.string.lamp_environment_graph_temperature_current);
		tvAverageTitle2.setText(R.string.lamp_environment_graph_temperature_average);
		tvAverageTitle2.setOnClickListener(null);
		tvAverageTitle3.setOnClickListener(null);

		tvDetailTitle1.setText(R.string.lamp_environment_graph_temperature_selected);
		tvDetailTitle1.setOnClickListener(null);
		tvDetailTitle2.setOnClickListener(null);
		tvDetailTitle3.setOnClickListener(null);
		tvDetailTitle4.setOnClickListener(null);

		tvAverageContentScale1.setVisibility(View.VISIBLE);
		tvAverageContentScale2.setVisibility(View.VISIBLE);
		tvAverageContentScale3.setVisibility(View.INVISIBLE);
		tvAverageContentScale1.setText(mStrTemperatureUnit);
		tvAverageContentScale2.setText(mStrTemperatureUnit);

		tvDetailContentScale1.setVisibility(View.VISIBLE);
		tvDetailContentScale2.setVisibility(View.INVISIBLE);
		tvDetailContentScale3.setVisibility(View.INVISIBLE);
		tvDetailContentScale4.setVisibility(View.INVISIBLE);
		tvDetailContentScale1.setText(mStrTemperatureUnit);

		// 평균 구하기
		double lastTemperature = 0;
		double sumTemperature = 0;
		int cntTemperature = 0;
		for (int i = 0; i < mGraphTemperatureList.size(); i++) {
			if (mGraphTemperatureList.get(i) != -999) {
				cntTemperature++;
				lastTemperature = mGraphTemperatureList.get(i);
				sumTemperature += lastTemperature;
			}
		}
		float avgTemerature = (float)(sumTemperature / cntTemperature);
		avgTemerature = (int)(avgTemerature * 10) / 10.0f;

        if (lastTemperature >= mLamp.getMaxTemperature() || lastTemperature <= mLamp.getMinTemperature()) {
            tvAverageContent1.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
        } else {
            tvAverageContent1.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
        }
        tvAverageContent1.setText(lastTemperature + "");

        if (avgTemerature >= mLamp.getMaxTemperature() || avgTemerature <= mLamp.getMinTemperature()) {
            tvAverageContent2.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
        } else {
            tvAverageContent2.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
        }
        tvAverageContent2.setText(avgTemerature + "");

		gvGraph.setGraphType(EnvironmentGraphView.GRAPH_TYPE_TEMPERATURE);
		gvGraph.setTemperatureThreshold(mLamp.getMaxTemperature(), mLamp.getMinTemperature());
		gvGraph.setValues(mGraphTemperatureList);
	}

	private void _updateHumidityGraph() {
		lctnDuration.setVisibility(View.GONE);

		rctnDetailSection.setBackgroundColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
		rctnDetailGraph.setVisibility(View.GONE);
		rctnAverage1.setVisibility(View.VISIBLE);
		rctnAverage2.setVisibility(View.VISIBLE);
		rctnAverage3.setVisibility(View.GONE);
		rctnDetail1.setVisibility(View.VISIBLE);
		rctnDetail2.setVisibility(View.GONE);
		rctnDetail3.setVisibility(View.GONE);
		rctnDetail4.setVisibility(View.GONE);

		tvGraphMaxValue.setVisibility(View.VISIBLE);
		tvGraphMinValue.setVisibility(View.VISIBLE);

		tvAverageTitle1.setText(R.string.lamp_environment_graph_humidity_current);
		tvAverageTitle2.setText(R.string.lamp_environment_graph_humidity_average);
		tvAverageTitle2.setOnClickListener(null);
		tvAverageTitle3.setOnClickListener(null);

		tvDetailTitle1.setText(R.string.lamp_environment_graph_humidity_selected);
		tvDetailTitle1.setOnClickListener(null);
		tvDetailTitle2.setOnClickListener(null);
		tvDetailTitle3.setOnClickListener(null);
		tvDetailTitle4.setOnClickListener(null);

		tvAverageContentScale1.setVisibility(View.VISIBLE);
		tvAverageContentScale2.setVisibility(View.VISIBLE);
		tvAverageContentScale3.setVisibility(View.INVISIBLE);
		tvAverageContentScale1.setText("%");
		tvAverageContentScale2.setText("%");

		tvDetailContentScale1.setVisibility(View.VISIBLE);
		tvDetailContentScale2.setVisibility(View.INVISIBLE);
		tvDetailContentScale3.setVisibility(View.INVISIBLE);
		tvDetailContentScale4.setVisibility(View.INVISIBLE);
		tvDetailContentScale1.setText("%");

		// 평균 구하기
		double lastHumidity = 0;
		double sumHumidity = 0;
		int cntHumidity = 0;
		for (int i = 0; i < mGraphHumidityList.size(); i++) {
			if (mGraphHumidityList.get(i) != -999) {
				cntHumidity++;
				lastHumidity = mGraphHumidityList.get(i);
				sumHumidity += lastHumidity;
			}

		}
		float avgHumidity = (float)(sumHumidity / cntHumidity);
		avgHumidity = (int)(avgHumidity * 10) / 10.0f;

        if (lastHumidity >= mLamp.getMaxHumidity() || lastHumidity <= mLamp.getMinHumidity()) {
            tvAverageContent1.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
        } else {
            tvAverageContent1.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
        }
        tvAverageContent1.setText(lastHumidity + "");

        if (avgHumidity >= mLamp.getMaxHumidity() || avgHumidity <= mLamp.getMinHumidity()) {
            tvAverageContent2.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
        } else {
            tvAverageContent2.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
        }
        tvAverageContent2.setText(avgHumidity + "");

		gvGraph.setGraphType(EnvironmentGraphView.GRAPH_TYPE_HUMIDITY);
		gvGraph.setHumidityThreshold(mLamp.getMaxHumidity(), mLamp.getMinHumidity());
		gvGraph.setValues(mGraphHumidityList);
	}

	private void _updateDate() {
		tvDate.setText(DateTimeUtil.getDateString(mCurrentLocalTimeMs, Locale.getDefault().getLanguage()));
		long diff = (mTodayLocalTimeMs - mCurrentLocalTimeMs) / DateTimeUtil.ONE_DAY_MILLIS;
		if (diff == 0) {
			tvDay.setText(getString(R.string.hub_graph_today));
		} else {
			tvDay.setText(getString(R.string.hub_graph_ago, diff + ""));
		}
		if (mTodayLocalTimeMs == mCurrentLocalTimeMs) {
			btnNext.setVisibility(View.GONE);
		} else {
			btnNext.setVisibility(View.VISIBLE);
		}

		if (mTodayLocalTimeMs - DateTimeUtil.ONE_DAY_MILLIS * 30 >= mCurrentLocalTimeMs) {
			btnPrev.setVisibility(View.GONE);
		} else {
			btnPrev.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		mLamp = ((DeviceLampActivity)mMainActivity).getLampObject();
		((DeviceLampActivity)mMainActivity).updateNewMark();
		updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
