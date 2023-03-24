package goodmonit.monit.com.kao.devicestatus;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceEnvironmentActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.devices.HubGraphInfo;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.EnvironmentGraphView;

public class EnvironmentGraphFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "HubGraph";
	private static final boolean DBG = Configuration.DBG;

	private static final int VIEW_GRAPH_TEMPERATURE     = 1;
	private static final int VIEW_GRAPH_HUMIDITY  		= 2;
	private static final int VIEW_GRAPH_VOC			  	= 3;

	private Button btnTabTemperature, btnTabHumidity, btnTabVoc;
	private Button btnPrev, btnNext;
	private TextView tvDate, tvDay;
	private TextView tvTime, tvTimeValue, tvTimeValueUnit;
	private TextView tvAverage, tvAverageValue, tvAverageValueUnit;
	private TextView tvMaxValue, tvMinValue, tvNoData;

	/** UI Resources */
	private RelativeLayout rctnGraph;

	private int mCurrentViewIndex;

	private long mTodayLocalTimeMs, mCurrentLocalTimeMs;

	private DeviceAQMHub mHub;
	private ArrayList<HubGraphInfo> mHubGraphInfoList = new ArrayList<>();
	private ArrayList<Double> mHubGraphTemperatureList = new ArrayList<>();
	private ArrayList<Double> mHubGraphHumidityList = new ArrayList<>();
	private ArrayList<Double> mHubGraphVocList = new ArrayList<>();
	private ArrayList<Double> mHubGraphScoreList = new ArrayList<>();

	private EnvironmentGraphView gvGraph;
	private EnvironmentCheckManager mEnvironmentMgr;

	private int mTemperatureUnit = 0; // Celcius
	private String mStrTemperatureUnit; // Celcius

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_aqmhub_graph, container, false);

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

		mHub = ((DeviceEnvironmentActivity)mMainActivity).getAQMHubObject();

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
		tvTime = (TextView)v.findViewById(R.id.tv_graph_time);
		tvTimeValue = (TextView)v.findViewById(R.id.tv_graph_time_value);
		tvTimeValueUnit = (TextView)v.findViewById(R.id.tv_graph_time_value_unit);
		tvAverage = (TextView)v.findViewById(R.id.tv_graph_average);
		tvAverageValue = (TextView)v.findViewById(R.id.tv_graph_average_value);
		tvAverageValueUnit = (TextView)v.findViewById(R.id.tv_graph_average_value_unit);

		tvMaxValue = (TextView)v.findViewById(R.id.tv_environment_graph_max_value);
		tvMinValue = (TextView)v.findViewById(R.id.tv_environment_graph_min_value);
		tvNoData = (TextView)v.findViewById(R.id.tv_environment_graph_no_data);

		btnTabTemperature = (Button)v.findViewById(R.id.btn_graph_tab_temperature);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
            Drawable top = getResources().getDrawable(R.drawable.btn_tab_temperature_kc);
            int iconSize = (int)getResources().getDimension(R.dimen.graph_tab_icon_size);
			top.setBounds(0, 0, iconSize, iconSize);
            btnTabTemperature.setCompoundDrawables(null, top , null, null);
		}
		btnTabTemperature.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FirebaseAnalyticsManager.getInstance(mContext).sendHubGraphTemperature(mHub.deviceId);
				_selectTabButton(VIEW_GRAPH_TEMPERATURE);
				updateView();
			}
		});

		btnTabHumidity = (Button)v.findViewById(R.id.btn_graph_tab_humidity);
        if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			Drawable top = getResources().getDrawable(R.drawable.btn_tab_humidity_kc);
			int iconSize = (int)getResources().getDimension(R.dimen.graph_tab_icon_size);
			top.setBounds(0, 0, iconSize, iconSize);
			btnTabHumidity.setCompoundDrawables(null, top , null, null);
        }
		btnTabHumidity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FirebaseAnalyticsManager.getInstance(mContext).sendHubGraphHumidity(mHub.deviceId);
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

		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			btnTabVoc.setVisibility(View.GONE);
			btnTabTemperature.setText("");
			btnTabHumidity.setText("");
			btnTabVoc.setText("");
		}

		rctnGraph = (RelativeLayout)v.findViewById(R.id.rctn_graph);
		gvGraph = (EnvironmentGraphView)v.findViewById(R.id.gv_environment_graph);
		gvGraph.setCurrentTimeTextView(tvTime);
		gvGraph.setCurrentValueTextView(tvTimeValue);
		gvGraph.setMaxValueTextView(tvMaxValue);
		gvGraph.setMinValueTextView(tvMinValue);
		gvGraph.setNoDataTextView(tvNoData);
        gvGraph.setAverageValueTextView(tvAverageValue);
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
		_updatePreference();
		_updateDate();
		long utcBeginSec = DateTimeUtil.convertLocalToUTCTimeMs(mCurrentLocalTimeMs) / 1000;
		long utcEndSec = utcBeginSec + DateTimeUtil.ONE_DAY_MILLIS / 1000 - 1;

		_loadGraphData(utcBeginSec, utcEndSec);

		_updateUnit();
		_updateGraph();
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
		if (mHub == null) return;
		mHubGraphInfoList = mDatabaseMgr.getHubGraphInfoList(mHub.deviceId, utcBeginSec, utcEndSec);
		if (DBG) Log.d(TAG, "loadGraphData : " + utcBeginSec + " / " + DateTimeUtil.getNotConvertedDateTime(utcBeginSec * 1000)
				+ " ~ " + utcEndSec + " / " + DateTimeUtil.getNotConvertedDateTime(utcEndSec * 1000)
				+ " / " + mHubGraphInfoList.size());

		mHubGraphScoreList.clear();
		mHubGraphTemperatureList.clear();
		mHubGraphHumidityList.clear();
		mHubGraphVocList.clear();

		double temperature = -999;
		double humidity = -999;
		double voc = -999;
		double score = -999;
		long timeSec = 0;

		// 초기화
		for (int i = 0; i < 6 * 24; i++) {
			mHubGraphScoreList.add(score);
			mHubGraphTemperatureList.add(temperature);
			mHubGraphHumidityList.add(humidity);
			mHubGraphVocList.add(voc);
		}

		int idx = 0;
		for (int i = 0; i < mHubGraphInfoList.size(); i++) {
			idx = (int)(mHubGraphInfoList.get(i).timeSec - utcBeginSec) / (60 * 10);
			score = mHubGraphInfoList.get(i).score;
			temperature = mHubGraphInfoList.get(i).temperature;
			humidity = mHubGraphInfoList.get(i).humidity;
			voc = mHubGraphInfoList.get(i).voc;

			if (score != -999) {
				mHubGraphScoreList.set(idx, score);
			}
			if (temperature != -999) {
				temperature = temperature / 100.0;
				if (mTemperatureUnit == 0) { // Celcius
					temperature = (int)(temperature * 10) / 10.0;
					mHubGraphTemperatureList.set(idx, temperature);
				} else { // Fahrenheit
					temperature = UnitConvertUtil.getFahrenheitFromCelsius((float)temperature);
					mHubGraphTemperatureList.set(idx, temperature);
				}
			}
			if (humidity != -999) {
				humidity = humidity / 100.0;
				humidity = (int)(humidity * 10) / 10.0;
				mHubGraphHumidityList.set(idx, humidity);
			}
			if (voc != -999 && voc != -1) {
				mHubGraphVocList.set(idx, voc / 100.0);
			}
		}
	}

	private void _updateGraph() {
		if (mHub == null) return;
		//gvGraph.init();
		gvGraph.setBeginTimeMs(DateTimeUtil.convertLocalToUTCTimeMs(mCurrentLocalTimeMs));
		switch (mCurrentViewIndex) {
			case VIEW_GRAPH_TEMPERATURE:
				gvGraph.setGraphType(EnvironmentGraphView.GRAPH_TYPE_TEMPERATURE);
				gvGraph.setValues(mHubGraphTemperatureList);
				if (mTemperatureUnit == 0) { // Celcius
					gvGraph.setTemperatureScale(0);
					gvGraph.setTemperatureThreshold(mHub.getMaxTemperature(), mHub.getMinTemperature());
				} else { // Fahrenheit
					gvGraph.setTemperatureScale(1);
					gvGraph.setTemperatureThreshold(UnitConvertUtil.getFahrenheitFromCelsius(mHub.getMaxTemperature()), UnitConvertUtil.getFahrenheitFromCelsius(mHub.getMinTemperature()));
				}
				break;
			case VIEW_GRAPH_HUMIDITY:
				gvGraph.setGraphType(EnvironmentGraphView.GRAPH_TYPE_HUMIDITY);
				gvGraph.setValues(mHubGraphHumidityList);
				gvGraph.setHumidityThreshold(mHub.getMaxHumidity(), mHub.getMinHumidity());
				break;
			case VIEW_GRAPH_VOC:
				gvGraph.setGraphType(EnvironmentGraphView.GRAPH_TYPE_VOC);
				gvGraph.setValues(mHubGraphVocList);
				gvGraph.setVocThreshold(150);
				break;
		}
		gvGraph.invalidate();
	}

	private void _updateUnit() {
		if (mHub == null) return;

		double sum = 0;
		double cnt = 0;
		double avg = 0;
		double curr = -999;

		ArrayList<Double> graphInfoList = null;
		if (mCurrentViewIndex == VIEW_GRAPH_TEMPERATURE) {
			graphInfoList = mHubGraphTemperatureList;
		} else if (mCurrentViewIndex == VIEW_GRAPH_HUMIDITY) {
			graphInfoList = mHubGraphHumidityList;
		} else if (mCurrentViewIndex == VIEW_GRAPH_VOC) {
			graphInfoList = mHubGraphVocList;
		}

		if (graphInfoList == null) return;

		for (double value : graphInfoList) {
			if (value == -999) continue;
			curr = value;
			cnt++;
			sum += value;
		}
		if (cnt == 0) {
			avg = 0;
		} else {
			avg = sum / cnt;
			avg = ((int) avg * 100) / 100.0;
		}

		switch (mCurrentViewIndex) {
			case VIEW_GRAPH_TEMPERATURE:
				double maxTemperature = mHub.getMaxTemperature();
				double minTemperature = mHub.getMinTemperature();
				if (mTemperatureUnit == 1) { // Fahrenheit
					maxTemperature = UnitConvertUtil.getFahrenheitFromCelsius(mHub.getMaxTemperature());
					minTemperature = UnitConvertUtil.getFahrenheitFromCelsius(mHub.getMinTemperature());
				}

				if (curr != -999) {
					tvTimeValue.setText(curr + "");
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
						if (curr >= maxTemperature) {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextWarning));
						} else if (curr <= minTemperature) {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextWarningBlue));
						} else {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorPrimary));
						}
					} else {
						if (curr >= maxTemperature || curr <= minTemperature) {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
						} else {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
						}
					}
				}
				tvAverageValue.setText(avg + "");
				tvTimeValueUnit.setText(mStrTemperatureUnit);
				tvAverageValueUnit.setText(mStrTemperatureUnit);
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					if (avg >= maxTemperature) {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextWarning));
					} else if (avg <= minTemperature) {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextWarningBlue));
					} else {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorPrimary));
					}
				} else {
					if (avg >= maxTemperature || avg <= minTemperature) {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
					} else {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
					}
				}
				break;
			case VIEW_GRAPH_HUMIDITY:
				if (curr != -999) {
					tvTimeValue.setText(curr + "");
					if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
						if (curr >= mHub.getMaxHumidity()) {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
						} else if (curr <= mHub.getMinHumidity()) {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
						} else {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorPrimary));
						}
					} else {
						if (curr >= mHub.getMaxHumidity() || curr <= mHub.getMinHumidity()) {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
						} else {
							tvTimeValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
						}
					}
				}
				tvAverageValue.setText(avg + "");
				tvTimeValueUnit.setText("%");
				tvAverageValueUnit.setText("%");
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					if (avg >= mHub.getMaxHumidity()) {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
					} else if (avg <= mHub.getMinHumidity()) {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
					} else {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorPrimary));
					}
				} else {
					if (avg >= mHub.getMaxHumidity() || avg <= mHub.getMinHumidity()) {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow50));
					} else {
						tvAverageValue.setTextColor(getResources().getColor(R.color.colorTextScoreBelow100));
					}
				}
				break;
			case VIEW_GRAPH_VOC:
				if (curr != -999) {
					if (curr > 300) {
						tvTimeValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
						tvTimeValue.setText(getContext().getString(R.string.device_environment_voc_very_bad));
					} else if (curr > 150) {
						tvTimeValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
						tvTimeValue.setText(getContext().getString(R.string.device_environment_voc_not_good));
					} else if (curr > 50) {
						tvTimeValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
						tvTimeValue.setText(getContext().getString(R.string.device_environment_voc_normal));
					} else {
						tvTimeValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
						tvTimeValue.setText(getContext().getString(R.string.device_environment_voc_good));
					}
				} else {
					tvTimeValue.setText("-");
				}
				tvTimeValueUnit.setText("");
				tvAverageValueUnit.setText("");
				if (avg > 300) {
					tvAverageValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow50));
					tvAverageValue.setText(getContext().getString(R.string.device_environment_voc_very_bad));
				} else if (avg > 150) {
					tvAverageValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow70));
					tvAverageValue.setText(getContext().getString(R.string.device_environment_voc_not_good));
				} else if (avg > 50) {
					tvAverageValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow90));
					tvAverageValue.setText(getContext().getString(R.string.device_environment_voc_normal));
				} else {
					tvAverageValue.setTextColor(getContext().getResources().getColor(R.color.colorTextScoreBelow100));
					tvAverageValue.setText(getContext().getString(R.string.device_environment_voc_good));
				}
				break;
		}
	}

	private void _updateDate() {
		tvDate.setText(DateTimeUtil.getDateString(mCurrentLocalTimeMs, Locale.getDefault().getLanguage()));
		tvTime.setText(DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, System.currentTimeMillis()));
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

		if (mTodayLocalTimeMs - DateTimeUtil.ONE_DAY_MILLIS * 6 >= mCurrentLocalTimeMs) {
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
		((DeviceEnvironmentActivity)mMainActivity).updateNewMark();
		updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
