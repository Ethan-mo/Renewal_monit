package goodmonit.monit.com.kao.devicestatus;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceSensorActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.SensorGraphInfo;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.MovementManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.widget.GraphViewDiaper;
import goodmonit.monit.com.kao.widget.GraphViewMovement;
import goodmonit.monit.com.kao.widget.GraphViewMovementDetail;
import goodmonit.monit.com.kao.widget.GraphViewSleeping;

public class DiaperGraph2Fragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SensorGraph2";
	private static final boolean DBG = Configuration.DBG;

	private static final int DURATION_WEEK			= 1;
	private static final int DURATION_MONTH			= 2;

	private static final int VIEW_GRAPH_DIAPER		= 1;
	private static final int VIEW_GRAPH_PEE  		= 2;
	private static final int VIEW_GRAPH_POO			= 3;
	private static final int VIEW_GRAPH_FART		= 4;
	private static final int VIEW_GRAPH_MOVEMENT	= 5;
	private static final int VIEW_GRAPH_SLEEPING	= 6;

	private static final int VIEW_LIMITED_GRAPH_DAY	= 10;

	private Button btnTabDiaper, btnTabPee, btnTabPoo, btnTabFart, btnTabSleeping, btnTabMovement;
	private Button btnPrev, btnNext;
	private Button btnDurationWeek, btnDurationMonth;
	private TextView tvDate, tvDay;
	private TextView tvNoData;
	private TextView tvGraphMaxValue, tvGraphMinValue;

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
	private LinearLayout lctnDuration;

	private int mCurrentViewIndex;
	private int mCurrentDurationIndex;

	private long mTodayLocalTimeMs, mTodayUtcTimeMs;
	private long mCurrentLocalTimeMs, mCurrentUtcTimeMs;
	private long mBeginLocalTimeMs, mBeginUtcTimeMs;
	private long mEndLocalTimeMs, mEndUtcTimeMs;

	private DeviceDiaperSensor mMonitSensor;
	private ArrayList<SensorGraphInfo> mSensorGraphInfoList = new ArrayList<>();

	private SimpleDialog mDlgMovementInfo;

	private GraphViewDiaper gvDiaperGraph;
	private GraphViewSleeping gvSleepingGraph;
	private GraphViewMovement gvMovementGraph;
	private GraphViewMovementDetail gvMovementDetailGraph;
	private SimpleDialog mDlgSleepInfo;

	private long mBirthdayUtcMs, mBirthdayLocalMs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_diaper_graph2, container, false);

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

        return view;
    }

	private void _initView(View v) {
		btnPrev = (Button)v.findViewById(R.id.btn_graph_prev);
		btnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
				switch (mCurrentDurationIndex) {
					case DURATION_WEEK:
						mCurrentLocalTimeMs -= DateTimeUtil.ONE_WEEK_MILLIS;
						break;
					case DURATION_MONTH:
						mCurrentLocalTimeMs -= DateTimeUtil.ONE_MONTH_MILLIS;
						break;
				}
				updateView();
			}
		});

		btnNext = (Button)v.findViewById(R.id.btn_graph_next);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
				switch (mCurrentDurationIndex) {
					case DURATION_WEEK:
						mCurrentLocalTimeMs += DateTimeUtil.ONE_WEEK_MILLIS;
						break;
					case DURATION_MONTH:
						mCurrentLocalTimeMs += DateTimeUtil.ONE_MONTH_MILLIS;
						break;
				}
				updateView();
			}
		});

		btnDurationWeek = (Button)v.findViewById(R.id.btn_graph_duration_week);
		btnDurationWeek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
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
			}
		});

		btnDurationMonth = (Button)v.findViewById(R.id.btn_graph_duration_month);
		btnDurationMonth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
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
			}
		});

		tvDate = (TextView)v.findViewById(R.id.tv_graph_date);
		tvDay = (TextView)v.findViewById(R.id.tv_graph_day);

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

		btnTabSleeping = (Button)v.findViewById(R.id.btn_graph_tab_sleeping);
		btnTabSleeping.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_selectTabButton(VIEW_GRAPH_SLEEPING);
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
				updateView();
				//_getSensorMovementInfo();
			}
		});

		btnTabMovement = (Button)v.findViewById(R.id.btn_graph_tab_movement);
		btnTabMovement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_selectTabButton(VIEW_GRAPH_MOVEMENT);
				((DeviceSensorActivity)mMainActivity).showProgressBar(true);
				updateView();
				//_getSensorMovementInfo();
			}
		});

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

		gvDiaperGraph = (GraphViewDiaper)v.findViewById(R.id.gv_diaper_graph);
		gvDiaperGraph.setNoDataTextView(tvNoData);
		gvDiaperGraph.setDetailSection(rctnDetailSection);
		gvDiaperGraph.setDetailDateTextView(tvDetailDate);

		gvDiaperGraph.setDayTotalDiaperChangedTextView(tvDetailContent1);
		gvDiaperGraph.setDayTotalPeeCountTextView(tvDetailContent2);
		gvDiaperGraph.setDayTotalPooCountTextView(tvDetailContent3);
		gvDiaperGraph.setDayTotalSoiledCountTextView(tvDetailContent4);

		gvSleepingGraph = (GraphViewSleeping) v.findViewById(R.id.gv_sleeping_graph);
		gvSleepingGraph.setNoDataTextView(tvNoData);
		gvSleepingGraph.setDetailSection(rctnDetailSection);
		gvSleepingGraph.setDetailDateTextView(tvDetailDate);
		gvSleepingGraph.setDayTotalSleepTimeTextView(tvDetailContent1);
		gvSleepingGraph.setDayTotalAwakeCountTimeTextView(tvDetailContent2);
		//gvSleepingGraph.setDayDeepSleepTimeTextView(tvDetailContent2);
		//gvSleepingGraph.setDaySleepScoreTextView(tvDetailContent3);
		//gvSleepingGraph.setDayMovementCountTextView(tvDetailContent2);
		//gvSleepingGraph.setDaySleepScoreTextView(tvDetailContent3);

		gvMovementDetailGraph = (GraphViewMovementDetail)v.findViewById(R.id.gv_movement_detail_graph);
		gvMovementGraph = (GraphViewMovement)v.findViewById(R.id.gv_movement_graph);
		gvMovementGraph.setNoDataTextView(tvNoData);
		gvMovementGraph.setDetailSection(rctnDetailSection);
		gvMovementGraph.setDetailDateTextView(tvDetailDate);
		gvMovementGraph.setDayAverageMovementLevelTextView(tvDetailContent1);
		gvMovementGraph.setDayAverageMovementStringTextView(tvDetailContent2);
		gvMovementGraph.setDetailGraph(gvMovementDetailGraph);
		gvMovementGraph.setDetailGraphSection(rctnDetailSection);

		// 소변, 대변, 방귀 그래프 삭제
		btnTabPee.setVisibility(View.GONE);
		btnTabPoo.setVisibility(View.GONE);
		btnTabFart.setVisibility(View.GONE);
    }

//    private void _getSensorMovementInfo() {
//		mServerQueryMgr.getSensorMovementInfo(
//				mMonitSensor.deviceId,
//				DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(mPreferenceMgr.getLatestMovementGraphUpdatedTimeSec(mMonitSensor.deviceId) * 1000),
//				new ServerManager.ServerResponseListener(){
//					@Override
//					public void onReceive(int responseCode, String errCode, String data) {
//						try {
//							JSONObject jobj = new JSONObject(data);
//							String startTime = jobj.getString(mServerQueryMgr.getParameter(15));
//							String value = jobj.getString(mServerQueryMgr.getParameter(51));
//							int count = jobj.getInt(mServerQueryMgr.getParameter(127));
//
//							Date date = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(startTime); // 로컬시간을 UTC로 변경하는 줄 알고 -9로 변경됨
//							long utcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()); // +9로 실제 UTC로 변경
//
//							if (count > 0) {
//								MovementGraphInfo movementGraphInfo = new MovementGraphInfo();
//								movementGraphInfo.deviceId = mMonitSensor.deviceId;
//								movementGraphInfo.startUtcTimeMs = utcTimeMs;
//								movementGraphInfo.value = value;
//								movementGraphInfo.created = System.currentTimeMillis();
//								movementGraphInfo.count = count;
//								long res = movementGraphInfo.insertDB(mContext);
//								if (DBG) Log.d(TAG, "insert movement data: " + res);
//
//								long latestUpdatedUtcTimeMs = utcTimeMs + (count - 1) * 10 * 1000;
//								mPreferenceMgr.setLatestSensorMovementGraphUpdatedUtcTimeMs(mMonitSensor.deviceId, latestUpdatedUtcTimeMs);
//
//								if (DBG) Log.d(TAG, "setSensorMovementGraphTargetUpdateUtcTimeMs: " + mMonitSensor.deviceId + " / " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(latestUpdatedUtcTimeMs));
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//
//						mMainActivity.runOnUiThread(new Runnable() {
//							public void run() {
//								updateView();
//							}
//						});
//					}
//				});
//	}

	private void _selectDurationButton(int position) {
		mCurrentDurationIndex = position;

		for (int i = DURATION_WEEK; i <= DURATION_MONTH; i++) {
			if (i == position) {
				switch (i) {
					case DURATION_WEEK:
						btnDurationWeek.setSelected(true);
						break;
					case DURATION_MONTH:
						btnDurationMonth.setSelected(true);
						break;
				}
			} else {
				switch (i) {
					case DURATION_WEEK:
						btnDurationWeek.setSelected(false);
						break;
					case DURATION_MONTH:
						btnDurationMonth.setSelected(false);
						break;
				}
			}
		}
	}

	private void _selectTabButton(int position) {
		mCurrentViewIndex = position;

		for (int i = VIEW_GRAPH_DIAPER; i <= VIEW_GRAPH_SLEEPING; i++) {
			if (i == position) {
				switch (i) {
					case VIEW_GRAPH_DIAPER:
						btnTabDiaper.setSelected(true);
						btnTabDiaper.setTextColor(getResources().getColor(R.color.colorDiaryTextDiaper));
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
					case VIEW_GRAPH_SLEEPING:
						btnTabSleeping.setSelected(true);
						btnTabSleeping.setTextColor(getResources().getColor(R.color.colorDiaryTextSleep));
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
					case VIEW_GRAPH_SLEEPING:
						btnTabSleeping.setSelected(false);
						btnTabSleeping.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
						break;
				}
			}
		}
	}

    public void updateView() {
        if (DBG) Log.d(TAG, "updateView Start");
		_updateDate();

		long utcBeginMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mBeginLocalTimeMs));
		long utcEndMs = DateTimeUtil.convertLocalToUTCTimeMs(DateTimeUtil.getDayBeginMillis(mEndLocalTimeMs) + DateTimeUtil.ONE_DAY_MILLIS - 1);

		_loadGraphData(utcBeginMs, utcEndMs);
		//_updateUnit();

		if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
			_updateMovementGraph();

			if (gvDiaperGraph.getVisibility() == View.VISIBLE) {
				gvDiaperGraph.setVisibility(View.GONE);
			}
			if (gvSleepingGraph.getVisibility() == View.VISIBLE) {
				gvSleepingGraph.setVisibility(View.GONE);
			}
			if (gvMovementGraph.getVisibility() == View.GONE) {
				gvMovementGraph.setVisibility(View.VISIBLE);
			}

			gvMovementGraph.setBeginTimeMs(utcBeginMs);
			gvMovementGraph.invalidate();

		} else if (mCurrentViewIndex == VIEW_GRAPH_SLEEPING) {
			_updateSleepingGraph();

			if (gvDiaperGraph.getVisibility() == View.VISIBLE) {
				gvDiaperGraph.setVisibility(View.GONE);
			}
			if (gvMovementGraph.getVisibility() == View.VISIBLE) {
				gvMovementGraph.setVisibility(View.GONE);
			}
			if (gvSleepingGraph.getVisibility() == View.GONE) {
				gvSleepingGraph.setVisibility(View.VISIBLE);
			}

			gvSleepingGraph.setBeginTimeMs(utcBeginMs);
			gvSleepingGraph.invalidate();
		} else {
			_updateDiaperGraph();

			if (gvMovementGraph.getVisibility() == View.VISIBLE) {
				gvMovementGraph.setVisibility(View.GONE);
			}
			if (gvSleepingGraph.getVisibility() == View.VISIBLE) {
				gvSleepingGraph.setVisibility(View.GONE);
			}
			if (gvDiaperGraph.getVisibility() == View.GONE) {
				gvDiaperGraph.setVisibility(View.VISIBLE);
			}

			gvDiaperGraph.setBeginTimeMs(utcBeginMs);
			gvDiaperGraph.invalidate();
		}
		((DeviceSensorActivity)mMainActivity).showProgressBar(false);
        if (DBG) Log.d(TAG, "updateView End");
	}

	private void _loadGraphData(long utcBeginMs, long utcEndMs) {
        if (DBG) Log.d(TAG, "_loadGraphData Start");

        switch(mCurrentViewIndex) {
			case VIEW_GRAPH_DIAPER:
				mSensorGraphInfoList = mDatabaseMgr.getDiaperChangedInfoList(mMonitSensor.deviceId, utcBeginMs, utcEndMs);
				if (DBG) Log.d(TAG, "loadDiaperGraphData : " + utcBeginMs + " / " + DateTimeUtil.getNotConvertedDateTime(utcBeginMs)
						+ " ~ " + utcEndMs + " / (" + DateTimeUtil.getNotConvertedDateTime(utcEndMs)
						+ ") / " + mSensorGraphInfoList.size());
				break;

			case VIEW_GRAPH_MOVEMENT:
				mSensorGraphInfoList = mDatabaseMgr.getSleepingGraphInfo(mMonitSensor.deviceId, utcBeginMs, utcEndMs);
				if (DBG) Log.d(TAG, "loadMovementGraphData : " + utcBeginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcBeginMs)
						+ ") ~ " + utcEndMs + " / (" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcEndMs)
						+ ") / " + mSensorGraphInfoList.size());
				break;

			case VIEW_GRAPH_SLEEPING:
				mSensorGraphInfoList = mDatabaseMgr.getSleepingGraphInfo(mMonitSensor.deviceId, utcBeginMs, utcEndMs);
				if (DBG) Log.d(TAG, "loadSleepingGraphData : " + utcBeginMs + "(" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcBeginMs)
						+ ") ~ " + utcEndMs + " / (" + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcEndMs)
						+ ") / " + mSensorGraphInfoList.size());

				break;

			default:
				/*
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

				*/
				break;
		}
	}

	private void _updateDiaperGraph() {
		lctnDuration.setVisibility(View.VISIBLE);

		rctnDetailSection.setBackgroundColor(getResources().getColor(R.color.colorDiaryTextDiaper));
		rctnDetailGraph.setVisibility(View.GONE);
		rctnAverage1.setVisibility(View.VISIBLE);
		rctnAverage2.setVisibility(View.VISIBLE);
		rctnAverage3.setVisibility(View.VISIBLE);
		rctnDetail1.setVisibility(View.VISIBLE);
		rctnDetail2.setVisibility(View.VISIBLE);
		rctnDetail3.setVisibility(View.VISIBLE);
		rctnDetail4.setVisibility(View.VISIBLE);

		tvGraphMaxValue.setVisibility(View.GONE);
		tvGraphMinValue.setVisibility(View.GONE);

		tvAverageTitle1.setText(R.string.sensor_diaper_graph_average_total_count);
		tvAverageTitle2.setText(R.string.sensor_diaper_graph_average_pee_count);
		tvAverageTitle3.setText(R.string.sensor_diaper_graph_average_poo_count);
		tvAverageTitle1.setOnClickListener(null);
		tvAverageTitle2.setOnClickListener(null);
		tvAverageTitle3.setOnClickListener(null);

		tvDetailTitle1.setText(R.string.sensor_diaper_graph_day_total_count);
		tvDetailTitle2.setText(R.string.sensor_diaper_graph_day_pee_count);
		tvDetailTitle3.setText(R.string.sensor_diaper_graph_day_poo_count);
		tvDetailTitle4.setText(R.string.sensor_diaper_graph_day_soiled_count);
		tvDetailTitle1.setOnClickListener(null);
		tvDetailTitle2.setOnClickListener(null);
		tvDetailTitle3.setOnClickListener(null);
		tvDetailTitle4.setOnClickListener(null);

		tvAverageContentScale1.setVisibility(View.VISIBLE);
		tvAverageContentScale2.setVisibility(View.VISIBLE);
		tvAverageContentScale3.setVisibility(View.VISIBLE);
		tvAverageContentScale1.setText(R.string.sensor_graph_count);
		tvAverageContentScale2.setText(R.string.sensor_graph_count);
		tvAverageContentScale3.setText(R.string.sensor_graph_count);

		tvDetailContentScale1.setVisibility(View.VISIBLE);
		tvDetailContentScale2.setVisibility(View.VISIBLE);
		tvDetailContentScale3.setVisibility(View.VISIBLE);
		tvDetailContentScale4.setVisibility(View.VISIBLE);
		tvDetailContentScale1.setText(R.string.sensor_graph_count);
		tvDetailContentScale2.setText(R.string.sensor_graph_count);
		tvDetailContentScale3.setText(R.string.sensor_graph_count);
		tvDetailContentScale4.setText(R.string.sensor_graph_count);

		// 평균 구하기
		float sumDiaperChanged = 0;
		float sumPeeDetected = 0;
		float sumPooDetected = 0;

		for (int i = 0; i < mSensorGraphInfoList.size(); i++) {
			sumDiaperChanged += mSensorGraphInfoList.get(i).cntDiaperChanged;
			sumPeeDetected += mSensorGraphInfoList.get(i).cntPeeDetected;
			sumPooDetected += mSensorGraphInfoList.get(i).cntPooDetected;
		}
		float avgDiaperChanged = sumDiaperChanged / mSensorGraphInfoList.size();
		float avgPeeDetected = sumPeeDetected / mSensorGraphInfoList.size();
		float avgPooDetected = sumPooDetected / mSensorGraphInfoList.size();
		avgDiaperChanged = (int)(avgDiaperChanged * 10) / 10.0f;
		avgPeeDetected = (int)(avgPeeDetected * 10) / 10.0f;
		avgPooDetected = (int)(avgPooDetected * 10) / 10.0f;

		tvAverageContent1.setText(avgDiaperChanged + "");
		tvAverageContent2.setText(avgPeeDetected + "");
		tvAverageContent3.setText(avgPooDetected + "");

		gvDiaperGraph.setValues(mSensorGraphInfoList);
	}

	private void _updateSleepingGraph() {
		lctnDuration.setVisibility(View.VISIBLE);

		rctnDetailSection.setBackgroundColor(getResources().getColor(R.color.colorGraphDeepSleepSelected));
		rctnDetailGraph.setVisibility(View.GONE);
		rctnAverage1.setVisibility(View.VISIBLE);
		rctnAverage2.setVisibility(View.VISIBLE);
		rctnAverage3.setVisibility(View.GONE);
		rctnDetail1.setVisibility(View.VISIBLE);
		rctnDetail2.setVisibility(View.VISIBLE);
		rctnDetail3.setVisibility(View.GONE);
		rctnDetail4.setVisibility(View.GONE);

		tvGraphMaxValue.setVisibility(View.GONE);
		tvGraphMinValue.setVisibility(View.GONE);

		tvAverageTitle1.setText(R.string.sensor_sleep_graph_average_total_time);
		tvAverageTitle2.setText(R.string.sensor_sleep_graph_average_movement_count);
		//tvAverageTitle2.setText(R.string.sensor_sleep_graph_average_total_deep_sleep_time);

		tvAverageTitle1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgSleepInfo == null) {
					mDlgSleepInfo = new SimpleDialog(mContext,
							"수면 정보 안내",
							"총 수면 시간이란?\n- 센서가 감지한 수면 시간입니다.\n수면 그래프에서 진한 파란색 부분은 뒤척이거나 움직임이 없이 깊게 수면한 시간이며, 연한 파란색 부분은 뒤척이거나 움직임이 감지된 수면시간입니다..\n보호자와 아기가 같이 자는 경우, 보호자의 움직임으로 인해 아기에게 영향이 있을 수 있습니다.",
							getString(R.string.btn_close),
							new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									mDlgSleepInfo.dismiss();
								}
							});
					mDlgSleepInfo.setContentsGravity(Gravity.LEFT);
				}
				if (mDlgSleepInfo != null && !mDlgSleepInfo.isShowing()) {
					mDlgSleepInfo.show();
				}
			}
		});
		tvAverageTitle2.setOnClickListener(null);
		tvAverageTitle3.setOnClickListener(null);

		tvDetailTitle1.setText(R.string.sensor_sleep_graph_day_total_time);
		tvDetailTitle2.setText(R.string.sensor_sleep_graph_day_movement_count);
		//tvDetailTitle3.setText(R.string.sensor_graph_sleeping_score);
		tvDetailTitle1.setOnClickListener(null);
		tvDetailTitle2.setOnClickListener(null);
		tvDetailTitle3.setOnClickListener(null);
		tvDetailTitle4.setOnClickListener(null);

		tvAverageContentScale1.setVisibility(View.INVISIBLE);
		tvAverageContentScale2.setVisibility(View.VISIBLE);
		tvAverageContentScale3.setVisibility(View.INVISIBLE);
		tvAverageContentScale2.setText(R.string.sensor_graph_count);

		tvDetailContentScale1.setVisibility(View.INVISIBLE);
		tvDetailContentScale2.setVisibility(View.VISIBLE);
		tvDetailContentScale3.setVisibility(View.INVISIBLE);
		tvDetailContentScale4.setVisibility(View.INVISIBLE);
		tvDetailContentScale2.setText(R.string.sensor_graph_count);

		MovementManager movementMgr = new MovementManager();
		int sumSleepTimeSec = 0;
		int sumConvertedSleepTimeSec = 0;
		int sumDeepSleepTimeSec = 0;
		int sumAwakeTimes = 0;

		for (int day = 0; day < mSensorGraphInfoList.size(); day++) {
			movementMgr.setSleepingData(mSensorGraphInfoList.get(day).movementValues, mSensorGraphInfoList.get(day).sleepingValues);

			if (mPreferenceMgr.getAutoSleepingDetectionEnabled(mMonitSensor.deviceId)) {
				mSensorGraphInfoList.get(day).sleepingValues = movementMgr.getOriginalSleepingData();
				mSensorGraphInfoList.get(day).convertedSleepTimeSec = movementMgr.getTotalSleepSec();
			} else {
				mSensorGraphInfoList.get(day).sleepingValues = movementMgr.getConvertedSleepingData();
				mSensorGraphInfoList.get(day).convertedSleepTimeSec = movementMgr.getTotalConvertedSleepSec();
			}
			mSensorGraphInfoList.get(day).sleepTimeSec = movementMgr.getTotalSleepSec();
			mSensorGraphInfoList.get(day).deepSleepTimeSec = movementMgr.getTotalDeepSleepSec();

			sumSleepTimeSec += movementMgr.getTotalSleepSec();
			sumConvertedSleepTimeSec += movementMgr.getTotalConvertedSleepSec();
			sumDeepSleepTimeSec += movementMgr.getTotalDeepSleepSec();
			sumAwakeTimes += mSensorGraphInfoList.get(day).cntMovementDetected;
		}

		int avgSleepTimeSec = sumSleepTimeSec / mSensorGraphInfoList.size();
		int avgDeepSleepTimeSec = sumDeepSleepTimeSec / mSensorGraphInfoList.size();
		int avgConvertedSleepTimeSec = sumConvertedSleepTimeSec / mSensorGraphInfoList.size();
		float avgAwakeTimes = (int)(sumAwakeTimes / mSensorGraphInfoList.size() * 10) / 10.0f;

		int totalConvertedSleepHour = (avgConvertedSleepTimeSec / 60) / 60;
		int totalConvertedSleepMinute = (avgConvertedSleepTimeSec / 60) % 60;
		String convertedSleepTime = "";
		if (totalConvertedSleepHour == 0) {
			convertedSleepTime = totalConvertedSleepMinute + getResources().getString(R.string.time_elapsed_minute);
		} else {
			convertedSleepTime = totalConvertedSleepHour + getResources().getString(R.string.time_elapsed_hour) + " " + totalConvertedSleepMinute + getResources().getString(R.string.time_elapsed_minute);
		}

		tvAverageContent1.setText(convertedSleepTime + "");
		tvAverageContent2.setText(avgAwakeTimes + "");

		gvSleepingGraph.setValues(mSensorGraphInfoList);
	}

	private void _updateMovementGraph() {
		lctnDuration.setVisibility(View.VISIBLE);

		rctnDetailSection.setBackgroundColor(getResources().getColor(R.color.colorTextDiaperCategory));
		rctnDetailGraph.setVisibility(View.VISIBLE);
		rctnAverage1.setVisibility(View.VISIBLE);
		rctnAverage2.setVisibility(View.VISIBLE);
		rctnAverage3.setVisibility(View.GONE);
		rctnDetail1.setVisibility(View.VISIBLE);
		rctnDetail2.setVisibility(View.VISIBLE);
		rctnDetail3.setVisibility(View.GONE);
		rctnDetail4.setVisibility(View.GONE);

		tvGraphMaxValue.setVisibility(View.GONE);
		tvGraphMinValue.setVisibility(View.GONE);

		tvAverageTitle1.setText(R.string.sensor_movement_graph_average_value);
		tvAverageTitle2.setText(R.string.sensor_movement_graph_average_text);
		tvAverageTitle1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDlgMovementInfo == null) {
					mDlgMovementInfo = new SimpleDialog(mContext,
							"활동량 정보 안내",
							"활동량이란?\n- 센서에서 감지된 움직임의 정도를 종합하여 나타낸 수치이며, 그래프는 10분간 평균 움직임 정도를 나타내고 있습니다.\n센서는 움직임이 없는 상황부터 성인이 빠르게 뛰는 경우까지 감지가 가능합니다.\n\n활동량 레벨\n- 0부터 12까지의 레벨로 나타나며, 레벨에 따라 차분, 활발, 매우활발 등으로 표기됩니다.",
							getString(R.string.btn_close),
							new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									mDlgMovementInfo.dismiss();
								}
							});
					mDlgMovementInfo.setContentsGravity(Gravity.LEFT);
				}

				if (mDlgMovementInfo != null && !mDlgMovementInfo.isShowing()) {
					mDlgMovementInfo.show();
				}
			}
		});
		tvAverageTitle2.setOnClickListener(null);
		tvAverageTitle3.setOnClickListener(null);

		tvDetailTitle1.setText(R.string.sensor_movement_graph_day_value);
		tvDetailTitle2.setText(R.string.sensor_movement_graph_day_text);
		tvDetailTitle1.setOnClickListener(null);
		tvDetailTitle2.setOnClickListener(null);
		tvDetailTitle3.setOnClickListener(null);
		tvDetailTitle4.setOnClickListener(null);

		tvAverageContentScale1.setVisibility(View.INVISIBLE);
		tvAverageContentScale2.setVisibility(View.INVISIBLE);
		tvAverageContentScale3.setVisibility(View.INVISIBLE);

		tvDetailContentScale1.setVisibility(View.INVISIBLE);
		tvDetailContentScale2.setVisibility(View.INVISIBLE);
		tvDetailContentScale3.setVisibility(View.INVISIBLE);
		tvDetailContentScale4.setVisibility(View.INVISIBLE);

		MovementManager movementMgr = new MovementManager();
		int sumMovementLevel = 0;
		int cntMovementLevel = 0;

		for (int day = 0; day < mSensorGraphInfoList.size(); day++) {
			movementMgr.setMovementData(mSensorGraphInfoList.get(day).movementValues, mSensorGraphInfoList.get(day).sleepingValues);
			mSensorGraphInfoList.get(day).movementValues = movementMgr.getConvertedMovementData();
			mSensorGraphInfoList.get(day).sumMovementLevel = movementMgr.getTotalMovementLevel();
			mSensorGraphInfoList.get(day).cntMovementLevel = movementMgr.getTotalMovementCount();
			mSensorGraphInfoList.get(day).avgMovementLevel = (int)((float)movementMgr.getTotalMovementLevel() / movementMgr.getTotalMovementCount() * 10) / 10.0f;

			sumMovementLevel += movementMgr.getTotalMovementLevel();
			cntMovementLevel += movementMgr.getTotalMovementCount();
		}

		float avgMovementLevel = (int)((float)sumMovementLevel / cntMovementLevel * 10) / 10.0f;

		tvAverageContent1.setText(avgMovementLevel + "");
		tvAverageContent2.setText(DeviceStatus.getMovementStringResource((int)avgMovementLevel));

		gvMovementGraph.setValues(mSensorGraphInfoList);
	}

	private void _updateDate() {
//		if (mCurrentViewIndex == VIEW_GRAPH_MOVEMENT) {
//			if (lctnDuration.getVisibility() == View.VISIBLE) {
//				lctnDuration.setVisibility(View.GONE);
//			}
//			tvDate.setText(DateTimeUtil.getDateString(mCurrentLocalTimeMs, Locale.getDefault().getLanguage()));
//			long diff = (mTodayLocalTimeMs - mCurrentLocalTimeMs) / DateTimeUtil.ONE_DAY_MILLIS;
//			if (diff == 0) {
//				tvDay.setText(getString(R.string.hub_graph_today));
//			} else {
//				tvDay.setText(getString(R.string.hub_graph_ago, diff + ""));
//			}
//			if (mTodayLocalTimeMs == mCurrentLocalTimeMs) {
//				btnNext.setVisibility(View.GONE);
//			} else {
//				btnNext.setVisibility(View.VISIBLE);
//			}
//
//			if (mTodayLocalTimeMs - DateTimeUtil.ONE_DAY_MILLIS * VIEW_LIMITED_GRAPH_DAY >= mCurrentLocalTimeMs) {
//				btnPrev.setVisibility(View.GONE);
//			} else {
//				btnPrev.setVisibility(View.VISIBLE);
//			}
//			return;
//		}

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

		switch(mCurrentViewIndex) {
			case VIEW_GRAPH_DIAPER:
				gvDiaperGraph.setDays(days, strDays);
				break;
			case VIEW_GRAPH_SLEEPING:
				gvSleepingGraph.setDays(days, strDays);
				break;
			case VIEW_GRAPH_MOVEMENT:
				gvMovementGraph.setDays(days, strDays);
				break;
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
		((DeviceSensorActivity)mMainActivity).updateNewMark();
		//updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
