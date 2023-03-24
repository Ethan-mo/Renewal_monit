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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceSensorActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.SensorGraphInfo;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.GraphView;
import goodmonit.monit.com.kao.widget.MovementGraphView;

public class DiaperGraphFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SensorGraph";
	private static final boolean DBG = Configuration.DBG;

	private static final int DURATION_WEEK			= 1;
	private static final int DURATION_MONTH			= 2;

	private static final int VIEW_GRAPH_DIAPER		= 1;
	private static final int VIEW_GRAPH_PEE  		= 2;
	private static final int VIEW_GRAPH_POO			= 3;
	private static final int VIEW_GRAPH_FART		= 4;
	private static final int VIEW_GRAPH_MOVEMENT	= 5;

	private Button btnTabDiaper, btnTabPee, btnTabPoo, btnTabFart, btnTabMovement;
	private Button btnPrev, btnNext;
	private Button btnDurationWeek, btnDurationMonth;
	private TextView tvDate, tvDay;
	private TextView tvTime, tvTimeValue, tvTimeValueUnit;
	private TextView tvAverage, tvAverageValue, tvAverageValueUnit;
	private TextView tvNoData;
	private TextView tvMovementMaxValue, tvMovementMinValue;

	private int mPeerPeeDetectedAverage = 0;
	private int mPeerPooDetectedAverage = 0;
	private int mPeerFartDetectedAverage = 0;
	private int mPeerDiaperChangedAverage = 0;

	/** UI Resources */
	private RelativeLayout rctnGraph;
	private RelativeLayout rctnMovementGraph;
	private LinearLayout lctnDuration;

	private int mCurrentViewIndex;
	private int mCurrentDurationIndex;

	private long mTodayLocalTimeMs, mTodayUtcTimeMs;
	private long mCurrentLocalTimeMs, mCurrentUtcTimeMs;
	private long mBeginLocalTimeMs, mBeginUtcTimeMs;
	private long mEndLocalTimeMs, mEndUtcTimeMs;

	private DeviceDiaperSensor mMonitSensor;
	private ArrayList<SensorGraphInfo> mSensorGraphInfoList = new ArrayList<>();
	private ArrayList<Double> mSensorGraphDiaperChangedList = new ArrayList<>();
	private ArrayList<Double> mSensorGraphPeeDetectedList = new ArrayList<>();
	private ArrayList<Double> mSensorGraphPooDetectedList = new ArrayList<>();
	private ArrayList<Double> mSensorGraphFartDetectedList = new ArrayList<>();
	private ArrayList<Integer> mSensorMovementLevelList = new ArrayList<>();

	private GraphView gvGraph;
	private MovementGraphView gvMovementGraph;

	private long mBirthdayUtcMs, mBirthdayLocalMs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_diaper_graph, container, false);

		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mDatabaseMgr = DatabaseManager.getInstance(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(902);
		mMainActivity = getActivity();

		mMonitSensor = ((DeviceSensorActivity)mMainActivity).getDiaperSensorObject();
        _initView(view);

		mCurrentViewIndex = VIEW_GRAPH_DIAPER;
		mCurrentDurationIndex = DURATION_WEEK;

		_selectDurationButton(mCurrentDurationIndex);
		_selectTabButton(mCurrentViewIndex);

		long utcNow = System.currentTimeMillis();
		long localNow = DateTimeUtil.convertUTCToLocalTimeMs(utcNow);

		mCurrentLocalTimeMs = DateTimeUtil.getDayBeginMillis(localNow);
		mTodayLocalTimeMs = DateTimeUtil.getDayBeginMillis(localNow);

		String birthdayYYMMDD = null;
		if (mMonitSensor != null) {
			birthdayYYMMDD = mMonitSensor.getBabyBirthdayYYMMDD();
		}
		if (DBG) Log.d(TAG, "birthday : " + birthdayYYMMDD);
		if (birthdayYYMMDD == null || birthdayYYMMDD.equals("700101")) {
			mBirthdayUtcMs = System.currentTimeMillis();
			mBirthdayLocalMs = DateTimeUtil.convertUTCToLocalTimeMs(mBirthdayUtcMs);
		} else {
			int year = 2000 + Integer.parseInt(birthdayYYMMDD.substring(0, 2));
			int month = Integer.parseInt(birthdayYYMMDD.substring(2, 4));
			Calendar c = Calendar.getInstance();
			c.set(year, month - 1, 1, 0, 0, 1);
			mBirthdayUtcMs = c.getTimeInMillis();
			mBirthdayLocalMs = DateTimeUtil.convertUTCToLocalTimeMs(mBirthdayUtcMs);
		}
		updateView();
		updatePeerAverage();

        return view;
    }

	private void _initView(View v) {
		btnPrev = (Button)v.findViewById(R.id.btn_graph_prev);
		btnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
					mCurrentLocalTimeMs -= DateTimeUtil.ONE_DAY_MILLIS;
					updateView();
				} else {
					mCurrentLocalTimeMs = mBeginLocalTimeMs;
					updateView();
					updatePeerAverage();
				}
			}
		});

		btnNext = (Button)v.findViewById(R.id.btn_graph_next);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
					mCurrentLocalTimeMs += DateTimeUtil.ONE_DAY_MILLIS;
					updateView();
				} else {
					switch (mCurrentDurationIndex) {
						case DURATION_WEEK:
							mCurrentLocalTimeMs += DateTimeUtil.ONE_WEEK_MILLIS;
							break;
						case DURATION_MONTH:
							mCurrentLocalTimeMs += DateTimeUtil.ONE_MONTH_MILLIS;
							break;
					}
					updateView();
					updatePeerAverage();
				}
			}
		});

		btnDurationWeek = (Button)v.findViewById(R.id.btn_graph_duration_week);
		btnDurationWeek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mCurrentViewIndex) {
					case VIEW_GRAPH_DIAPER:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphDiaperWeekly(mMonitSensor.deviceId);
						break;
					case VIEW_GRAPH_PEE:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPeeWeekly(mMonitSensor.deviceId);
						break;
					case VIEW_GRAPH_POO:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPooWeekly(mMonitSensor.deviceId);
						break;
					case VIEW_GRAPH_FART:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphFartWeekly(mMonitSensor.deviceId);
						break;
				}
				_selectDurationButton(DURATION_WEEK);
				updateView();
				updatePeerAverage();
			}
		});

		btnDurationMonth = (Button)v.findViewById(R.id.btn_graph_duration_month);
		btnDurationMonth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mCurrentViewIndex) {
					case VIEW_GRAPH_DIAPER:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphDiaperMonthly(mMonitSensor.deviceId);
						break;
					case VIEW_GRAPH_PEE:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPeeMonthly(mMonitSensor.deviceId);
						break;
					case VIEW_GRAPH_POO:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPooMonthly(mMonitSensor.deviceId);
						break;
					case VIEW_GRAPH_FART:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphFartMonthly(mMonitSensor.deviceId);
						break;
				}
				_selectDurationButton(DURATION_MONTH);
				updateView();
				updatePeerAverage();
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

		tvNoData = (TextView)v.findViewById(R.id.tv_graph_no_data);

		btnTabDiaper = (Button)v.findViewById(R.id.btn_graph_tab_diaper);
		btnTabDiaper.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mCurrentDurationIndex) {
					case DURATION_WEEK:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphDiaperWeekly(mMonitSensor.deviceId);
						break;
					case DURATION_MONTH:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphDiaperMonthly(mMonitSensor.deviceId);
						break;
				}
				_selectTabButton(VIEW_GRAPH_DIAPER);
				updateView();
			}
		});

		btnTabPee = (Button)v.findViewById(R.id.btn_graph_tab_pee);
		btnTabPee.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mCurrentDurationIndex) {
					case DURATION_WEEK:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPeeWeekly(mMonitSensor.deviceId);
						break;
					case DURATION_MONTH:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPeeMonthly(mMonitSensor.deviceId);
						break;
				}
				_selectTabButton(VIEW_GRAPH_PEE);
				updateView();
			}
		});

		btnTabPoo = (Button)v.findViewById(R.id.btn_graph_tab_poo);
		btnTabPoo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mCurrentDurationIndex) {
					case DURATION_WEEK:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPooWeekly(mMonitSensor.deviceId);
						break;
					case DURATION_MONTH:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphPooMonthly(mMonitSensor.deviceId);
						break;
				}
				_selectTabButton(VIEW_GRAPH_POO);
				updateView();
			}
		});

		btnTabFart = (Button)v.findViewById(R.id.btn_graph_tab_fart);
		btnTabFart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mCurrentDurationIndex) {
					case DURATION_WEEK:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphFartWeekly(mMonitSensor.deviceId);
						break;
					case DURATION_MONTH:
						FirebaseAnalyticsManager.getInstance(mContext).sendSensorGraphFartMonthly(mMonitSensor.deviceId);
						break;
				}
				_selectTabButton(VIEW_GRAPH_FART);
				updateView();
			}
		});

		btnTabMovement = (Button)v.findViewById(R.id.btn_graph_tab_movement);
		btnTabMovement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_selectTabButton(VIEW_GRAPH_MOVEMENT);
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
			}
		});

		if (Configuration.MASTER) {
			btnTabFart.setVisibility(View.GONE);
		} else {
			btnTabMovement.setVisibility(View.GONE);
		}

		rctnGraph = (RelativeLayout)v.findViewById(R.id.rctn_graph);
		gvGraph = (GraphView)v.findViewById(R.id.gv_diaper_graph);
		gvGraph.setGraphType(GraphView.GRAPH_TYPE_BAR);
		gvGraph.setNoDataTextView(tvNoData);

		lctnDuration = (LinearLayout)v.findViewById(R.id.lctn_graph_duration);
		rctnMovementGraph = (RelativeLayout)v.findViewById(R.id.rctn_movement_graph);
		gvMovementGraph = (MovementGraphView)v.findViewById(R.id.gv_movement_graph);
		tvMovementMaxValue = (TextView)v.findViewById(R.id.tv_movement_graph_max_value);
		tvMovementMinValue = (TextView)v.findViewById(R.id.tv_movement_graph_min_value);

		gvMovementGraph.setCurrentTimeTextView(tvTime);
		gvMovementGraph.setCurrentValueTextView(tvTimeValue);
		gvMovementGraph.setMaxValueTextView(tvMovementMaxValue);
		gvMovementGraph.setMinValueTextView(tvMovementMinValue);
		gvMovementGraph.setNoDataTextView(tvNoData);
		//gvMovementGraph.setDeepSleepTimeTextView(tvAverageValue);
    }

	private void _selectDurationButton(int position) {
		mCurrentDurationIndex = position;

		for (int i = DURATION_WEEK; i <= DURATION_MONTH; i++) {
			if (i == position) {
				switch (i) {
					case DURATION_WEEK:
						btnDurationWeek.setSelected(true);
						btnDurationWeek.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
					case DURATION_MONTH:
						btnDurationMonth.setSelected(true);
						btnDurationMonth.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
				}
			} else {
				switch (i) {
					case DURATION_WEEK:
						btnDurationWeek.setSelected(false);
						btnDurationWeek.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
					case DURATION_MONTH:
						btnDurationMonth.setSelected(false);
						btnDurationMonth.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
				}
			}
		}
	}

	private void _selectTabButton(int position) {
		mCurrentViewIndex = position;

		for (int i = VIEW_GRAPH_DIAPER; i <= VIEW_GRAPH_MOVEMENT; i++) {
			if (i == position) {
				switch (i) {
					case VIEW_GRAPH_DIAPER:
						btnTabDiaper.setSelected(true);
						btnTabDiaper.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
					case VIEW_GRAPH_PEE:
						btnTabPee.setSelected(true);
						btnTabPee.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
					case VIEW_GRAPH_POO:
						btnTabPoo.setSelected(true);
						btnTabPoo.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
					case VIEW_GRAPH_FART:
						btnTabFart.setSelected(true);
						btnTabFart.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
					case VIEW_GRAPH_MOVEMENT:
						btnTabMovement.setSelected(true);
						btnTabMovement.setTextColor(getResources().getColor(R.color.colorTextDiaperCategory));
						break;
				}
			} else {
				switch (i) {
					case VIEW_GRAPH_DIAPER:
						btnTabDiaper.setSelected(false);
						btnTabDiaper.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
					case VIEW_GRAPH_PEE:
						btnTabPee.setSelected(false);
						btnTabPee.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
					case VIEW_GRAPH_POO:
						btnTabPoo.setSelected(false);
						btnTabPoo.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
					case VIEW_GRAPH_FART:
						btnTabFart.setSelected(false);
						btnTabFart.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
					case VIEW_GRAPH_MOVEMENT:
						btnTabMovement.setSelected(false);
						btnTabMovement.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
				}
			}
		}
	}

	public void updatePeerAverage() {
		long utcBeginMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mBeginLocalTimeMs));
		long utcEndMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mEndLocalTimeMs) + DateTimeUtil.ONE_DAY_MILLIS - 1);

		// 또래 평균 데이터 업데이트
		mServerQueryMgr.getSensorGraphAverage(utcBeginMs, utcEndMs, new ServerManager.ServerResponseListener(){
			@Override
			public void onReceive(int responseCode, String errCode, String data) {
				mPeerPeeDetectedAverage = 0;
				mPeerPooDetectedAverage = 0;
				mPeerFartDetectedAverage = 0;
				mPeerDiaperChangedAverage = 0;

				if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
					JSONObject jobj = null;
					try {
						jobj = new JSONObject(data);
						mPeerPeeDetectedAverage = jobj.optInt(mServerQueryMgr.getParameter(90), 0);
						mPeerPooDetectedAverage = jobj.optInt(mServerQueryMgr.getParameter(91), 0);
						mPeerFartDetectedAverage = jobj.optInt(mServerQueryMgr.getParameter(92), 0);
						mPeerDiaperChangedAverage = jobj.optInt(mServerQueryMgr.getParameter(93), 0);
					} catch (JSONException e) {
						if (DBG) Log.e(TAG,"JSONException : " + e.toString());
					}
				}

				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						switch (mCurrentViewIndex) {
							case VIEW_GRAPH_DIAPER:
								if (mPeerDiaperChangedAverage == 0) {
									tvTimeValue.setText("-");
								} else {
									tvTimeValue.setText(mPeerDiaperChangedAverage + "");
								}
								break;
							case VIEW_GRAPH_PEE:
								if (mPeerPeeDetectedAverage == 0) {
									tvTimeValue.setText("-");
								} else {
									tvTimeValue.setText(mPeerPeeDetectedAverage + "");
								}
								break;
							case VIEW_GRAPH_POO:
								if (mPeerPooDetectedAverage == 0) {
									tvTimeValue.setText("-");
								} else {
									tvTimeValue.setText(mPeerPooDetectedAverage + "");
								}
								break;
							case VIEW_GRAPH_FART:
								if (mPeerFartDetectedAverage == 0) {
									tvTimeValue.setText("-");
								} else {
									tvTimeValue.setText(mPeerFartDetectedAverage + "");
								}
								break;
						}
					}
				});
			}
		});
	}

    public void updateView() {
		_updateDate();

		long utcBeginMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mBeginLocalTimeMs));
		long utcEndMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mEndLocalTimeMs) + DateTimeUtil.ONE_DAY_MILLIS - 1);

		_loadGraphData(utcBeginMs, utcEndMs);
		//_updateUnit();

		if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
			tvTimeValueUnit.setText("");
			tvAverageValueUnit.setText("");
			_updateMovementGraph();
		} else {
			tvTimeValueUnit.setText(getString(R.string.sensor_graph_count));
			tvAverageValueUnit.setText(getString(R.string.sensor_graph_count));
			_updateGraph();
		}
	}

	private void _loadGraphData(long utcBeginMs, long utcEndMs) {
    	if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
			mSensorMovementLevelList.clear();
			utcBeginMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mCurrentLocalTimeMs));
			utcEndMs = utcBeginMs + DateTimeUtil.ONE_DAY_MILLIS - 1;
    		mSensorMovementLevelList = mDatabaseMgr.getMovementGraphInfo(mMonitSensor.deviceId, utcBeginMs, utcEndMs);
            if (DBG) Log.d(TAG, "loadMovementGraphData : " + utcBeginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcBeginMs)
                    + ") ~ " + utcEndMs + " / " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcEndMs)
                    + " / " + mSensorMovementLevelList.size());
			return;
		}

		mSensorGraphInfoList = mDatabaseMgr.getSensorGraphInfoList(mMonitSensor.deviceId, utcBeginMs, utcEndMs);
		if (DBG) Log.d(TAG, "loadGraphData : " + utcBeginMs + " / " + DateTimeUtil.getNotConvertedDateTime(utcBeginMs)
				+ " ~ " + utcEndMs + " / " + DateTimeUtil.getNotConvertedDateTime(utcEndMs)
				+ " / " + mSensorGraphInfoList.size());

		mSensorGraphDiaperChangedList.clear();
		mSensorGraphPeeDetectedList.clear();
		mSensorGraphPooDetectedList.clear();
		mSensorGraphFartDetectedList.clear();

		for (SensorGraphInfo info : mSensorGraphInfoList) {
			mSensorGraphDiaperChangedList.add((double)info.cntDiaperChanged);
			mSensorGraphPeeDetectedList.add((double)info.cntPeeDetected);
			mSensorGraphPooDetectedList.add((double)info.cntPooDetected);
			mSensorGraphFartDetectedList.add((double)info.cntFartDetected);
		}

		switch (mCurrentViewIndex) {
			case VIEW_GRAPH_DIAPER:
				gvGraph.setValues(mSensorGraphDiaperChangedList);
				break;
			case VIEW_GRAPH_PEE:
				gvGraph.setValues(mSensorGraphPeeDetectedList);
				break;
			case VIEW_GRAPH_POO:
				gvGraph.setValues(mSensorGraphPooDetectedList);
				break;
			case VIEW_GRAPH_FART:
				gvGraph.setValues(mSensorGraphFartDetectedList);
				break;
		}
	}

	private void _updateMovementGraph() {
		if (gvGraph.getVisibility() == View.VISIBLE) {
			gvGraph.setVisibility(View.GONE);
		}

		if (rctnMovementGraph.getVisibility() == View.GONE) {
			rctnMovementGraph.setVisibility(View.VISIBLE);
		}

		tvTime.setText(getString(R.string.sensor_move_graph_now));
		tvAverage.setText(getString(R.string.sensor_move_graph_deep_sleep));

		gvMovementGraph.setBeginTimeMs(DateTimeUtil.convertLocalToUTCTimeMs(mCurrentLocalTimeMs));
		gvMovementGraph.setGraphType(MovementGraphView.GRAPH_TYPE_MOVEMENT);
		gvMovementGraph.setValues(mSensorMovementLevelList);
		gvMovementGraph.invalidate();
	}

	private void _updateGraph() {
		if (gvGraph.getVisibility() == View.GONE) {
			gvGraph.setVisibility(View.VISIBLE);
		}
		if (rctnMovementGraph.getVisibility() == View.VISIBLE) {
			rctnMovementGraph.setVisibility(View.GONE);
		}

		switch (mCurrentDurationIndex) {
			case DURATION_WEEK:
				tvTime.setText(getString(R.string.sensor_graph_server_average_week));
				tvAverage.setText(getString(R.string.sensor_graph_average_week));
				break;
			case DURATION_MONTH:
				tvTime.setText(getString(R.string.sensor_graph_server_average_month));
				tvAverage.setText(getString(R.string.sensor_graph_average_month));
				break;
		}

		// 평균 업데이트
		double sum, avg;
		sum = 0;
		int cnt = 0;
		switch (mCurrentViewIndex) {
			case VIEW_GRAPH_DIAPER:
				cnt = mSensorGraphDiaperChangedList.size();
				if (cnt > 0) {
					for (double d : mSensorGraphDiaperChangedList) {
						sum += d;
					}
					avg = (sum + 0.0) / cnt;
					tvAverageValue.setText(((int)(avg * 10) / 10.0) + "");
				}
				if (mPeerDiaperChangedAverage == 0) {
					tvTimeValue.setText("-");
				} else {
					tvTimeValue.setText(mPeerDiaperChangedAverage + "");
				}
				break;
			case VIEW_GRAPH_PEE:
				cnt = mSensorGraphPeeDetectedList.size();
				if (cnt > 0) {
					for (double d : mSensorGraphPeeDetectedList) {
						sum += d;
					}
					avg = (sum + 0.0) / cnt;
					tvAverageValue.setText(((int)(avg * 10) / 10.0) + "");
				}
				if (mPeerPeeDetectedAverage == 0) {
					tvTimeValue.setText("-");
				} else {
					tvTimeValue.setText(mPeerPeeDetectedAverage + "");
				}
				break;
			case VIEW_GRAPH_POO:
				cnt = mSensorGraphPooDetectedList.size();
				if (cnt > 0) {
					for (double d : mSensorGraphPooDetectedList) {
						sum += d;
					}
					avg = (sum + 0.0) / cnt;
					tvAverageValue.setText(((int)(avg * 10) / 10.0) + "");
				}
				if (mPeerPooDetectedAverage == 0) {
					tvTimeValue.setText("-");
				} else {
					tvTimeValue.setText(mPeerPooDetectedAverage + "");
				}
				break;
			case VIEW_GRAPH_FART:
				cnt = mSensorGraphFartDetectedList.size();
				if (cnt > 0) {
					for (double d : mSensorGraphFartDetectedList) {
						sum += d;
					}
					avg = (sum + 0.0) / cnt;
					tvAverageValue.setText(((int)(avg * 10) / 10.0) + "");
				}
				if (mPeerFartDetectedAverage == 0) {
					tvTimeValue.setText("-");
				} else {
					tvTimeValue.setText(mPeerFartDetectedAverage + "");
				}
				break;
		}

		gvGraph.invalidate();
	}

	private void _updateDate() {
		if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
			if (lctnDuration.getVisibility() == View.VISIBLE) {
				lctnDuration.setVisibility(View.GONE);
			}
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
			return;
		}

		if (lctnDuration.getVisibility() == View.GONE) {
			lctnDuration.setVisibility(View.VISIBLE);
		}
		// Begin, End 시간 계산(범위 벗어나는경우 변경되지 않도록)
		if (mCurrentDurationIndex == DURATION_WEEK) {
			mEndLocalTimeMs = mCurrentLocalTimeMs;
			mBeginLocalTimeMs = mEndLocalTimeMs - (DateTimeUtil.ONE_WEEK_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
		} else if (mCurrentDurationIndex == DURATION_MONTH) {
			mEndLocalTimeMs = mCurrentLocalTimeMs;
			mBeginLocalTimeMs = mEndLocalTimeMs - (DateTimeUtil.ONE_MONTH_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
		}

		if (mTodayLocalTimeMs <= mEndLocalTimeMs) {
			switch(mCurrentDurationIndex) {
				case DURATION_WEEK:
					mCurrentLocalTimeMs = mTodayLocalTimeMs;
					mEndLocalTimeMs = mCurrentLocalTimeMs;
					mBeginLocalTimeMs = mEndLocalTimeMs - (DateTimeUtil.ONE_WEEK_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
					break;
				case DURATION_MONTH:
					mCurrentLocalTimeMs = mTodayLocalTimeMs;
					mEndLocalTimeMs = mCurrentLocalTimeMs;
					mBeginLocalTimeMs = mEndLocalTimeMs - (DateTimeUtil.ONE_MONTH_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
					break;
			}
			btnNext.setVisibility(View.GONE);
		} else {
			btnNext.setVisibility(View.VISIBLE);
		}

		if (mBirthdayLocalMs >= mBeginLocalTimeMs) {
			switch(mCurrentDurationIndex) {
				case DURATION_WEEK:
					mCurrentLocalTimeMs = mBirthdayLocalMs + (DateTimeUtil.ONE_WEEK_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
					mEndLocalTimeMs = mCurrentLocalTimeMs;
					mBeginLocalTimeMs = mEndLocalTimeMs - (DateTimeUtil.ONE_WEEK_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
					break;
				case DURATION_MONTH:
					mCurrentLocalTimeMs = mBirthdayLocalMs + (DateTimeUtil.ONE_MONTH_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
					mEndLocalTimeMs = mCurrentLocalTimeMs;
					mBeginLocalTimeMs = mEndLocalTimeMs - (DateTimeUtil.ONE_MONTH_MILLIS - DateTimeUtil.ONE_DAY_MILLIS);
					break;
			}
			btnPrev.setVisibility(View.GONE);
		} else {
			btnPrev.setVisibility(View.VISIBLE);
		}

		// Begin, End 시간 기준으로 나머지 시간 계산
		long diffBetweenBirthdayAndSelectedDay = mBeginLocalTimeMs - mBirthdayLocalMs;
		int dMonth = (int)(diffBetweenBirthdayAndSelectedDay / (DateTimeUtil.ONE_MONTH_MILLIS));
		int dWeek = (int)(diffBetweenBirthdayAndSelectedDay / (DateTimeUtil.ONE_WEEK_MILLIS));

		if (DBG) Log.d(TAG, "Birthday: " + mBirthdayLocalMs + " / " + "SelectedDay: " + mBeginLocalTimeMs);

		// GraphView Days설정
		Calendar c = Calendar.getInstance();
		int lastDay = 7;
		switch(mCurrentDurationIndex) {
			case DURATION_WEEK:
				tvDay.setText("D + " + dWeek + getString(R.string.sensor_graph_week));
				lastDay = 7;
				break;
			case DURATION_MONTH:
				tvDay.setText("D + " + dMonth + getString(R.string.sensor_graph_month));
				lastDay = 30;
				break;
		}
		tvDate.setText(DateTimeUtil.getDateString(mBeginLocalTimeMs, Locale.getDefault().getLanguage()) + " ~ " + DateTimeUtil.getDateString(mEndLocalTimeMs, Locale.getDefault().getLanguage()));

		int[] days = new int[lastDay];
		String[] strDays = new String[lastDay];
		for (int i = 0 ; i < lastDay; i++) {
			c.setTimeInMillis(mBeginLocalTimeMs + DateTimeUtil.ONE_DAY_MILLIS * i);
			days[i] = c.get(Calendar.DAY_OF_MONTH);
			strDays[i] = (c.get(Calendar.MONTH) + 1) + "/" + days[i];
			if (DBG) Log.d(TAG, "[days] : " + c.get(Calendar.DAY_OF_MONTH) + " / " + c.get(Calendar.HOUR_OF_DAY) + " / " + mEndLocalTimeMs);
		}
		gvGraph.setDays(days, strDays);
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
		((DeviceSensorActivity)mMainActivity).updateNewMark();
		//updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
