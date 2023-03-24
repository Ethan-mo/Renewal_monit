package goodmonit.monit.com.kao.devicestatus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceSensorActivity;
import goodmonit.monit.com.kao.activity.FloatActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.NotiManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.message.FeedbackMsgAdapter;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationMsgAdapter;
import goodmonit.monit.com.kao.message.NotificationType;
import goodmonit.monit.com.kao.message.RecyclerViewAdapter;
import goodmonit.monit.com.kao.services.ConnectionManager;

import static goodmonit.monit.com.kao.message.RecyclerViewAdapter.LOADING_MESSAGE_SEC;

public class DiaperNotificationFragment extends BaseFragment {
	private static final String TAG = Configuration.BASE_TAG + "Notification";
	private static final boolean DBG = Configuration.DBG;
	/*
    private void addDateLineIfNeeded(MonitMessage cm) {
        boolean addDate = false;
        long msgDayMillis = DateTimeUtil.getDayBeginMillis(cm.getTimeMs());
        if (mAdapter.getCount() == 0) {
            addDate = true;
        } else {
            MonitMessage lastCm = mAdapter.getItem(mAdapter.getCount() - 1);
            if (DateTimeUtil.getDayBeginMillis(lastCm.getTimeMs()) != msgDayMillis) {
                addDate = true;
            }
        }

        if (addDate) {
            MonitMessage date = new MonitMessage(
                    MessageType.TYPE_ALARM_DATE,
                    DateTimeUtil.getDateString(cm.getTimeMs(), "kr"));
            mAdapter.add(date);
        }
    }
    */

	private Button btnFilterFartDetected, btnFilterPeeDetected, btnFilterPooDetected, btnFilterAbnormalDetected, btnFilterDiaperChanged;
	private RecyclerView rvNotificationList;

	private TextView tvEmpty;
	private long mDeviceId;
	private int mDeviceType;
	private long mLastLoadedMessageTimeMs;

	/**
	 * Feedback menu
	 */
	private SimpleDialog mDlgInputFeedback;
	private Button btnAddNotification;

	private LinearLayout lctnFilterSection;
	private ArrayList<NotificationMessage> mNotificationMessageList;
	private ArrayList<Integer> mFilteredTypeList;
	private RecyclerViewAdapter mMsgAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_diaper_notification, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mServerQueryMgr = ServerQueryManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(903);
		mNotificationMessageList = new ArrayList<>();
		mFilteredTypeList = new ArrayList<>();
		_initView(view);

		return view;
	}

	private void _initView(View v) {
		tvEmpty = (TextView)v.findViewById(R.id.tv_notification_empty);

		lctnFilterSection = (LinearLayout) v.findViewById(R.id.lctn_notification_filter_section);

		/*
		btnFilterDiaperChanged = (Button)v.findViewById(R.id.btn_notification_filter_diaper_changed);
		btnFilterDiaperChanged.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean selected = v.isSelected();
				if (selected) {
					mFilteredTypeList.remove((Integer)NotificationType.DIAPER_CHANGED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.DIAPER_CHANGED, false);
				} else {
					mFilteredTypeList.add((Integer)NotificationType.DIAPER_CHANGED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.DIAPER_CHANGED, true);
				}
				v.setSelected(!selected);
				showFilteredList();
			}
		});

		btnFilterPeeDetected = (Button)v.findViewById(R.id.btn_notification_filter_pee_detected);
		btnFilterPeeDetected.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean selected = v.isSelected();
				if (selected) {
					mFilteredTypeList.remove((Integer)NotificationType.PEE_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.PEE_DETECTED, false);
				} else {
					mFilteredTypeList.add((Integer)NotificationType.PEE_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.PEE_DETECTED, true);
				}
				v.setSelected(!selected);
				showFilteredList();
			}
		});

		btnFilterPooDetected = (Button)v.findViewById(R.id.btn_notification_filter_poo_detected);
		btnFilterPooDetected.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean selected = v.isSelected();
				if (selected) {
					mFilteredTypeList.remove((Integer)NotificationType.POO_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.POO_DETECTED, false);
				} else {
					mFilteredTypeList.add((Integer)NotificationType.POO_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.POO_DETECTED, true);
				}
				v.setSelected(!selected);
				showFilteredList();
			}
		});

		btnFilterAbnormalDetected = (Button)v.findViewById(R.id.btn_notification_filter_abnormal_detected);
		btnFilterAbnormalDetected.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean selected = v.isSelected();
				if (selected) {
					mFilteredTypeList.remove((Integer)NotificationType.ABNORMAL_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.ABNORMAL_DETECTED, false);
				} else {
					mFilteredTypeList.add((Integer)NotificationType.ABNORMAL_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.ABNORMAL_DETECTED, true);
				}
				v.setSelected(!selected);
				showFilteredList();
			}
		});

		btnFilterFartDetected = (Button)v.findViewById(R.id.btn_notification_filter_fart_detected);
		btnFilterFartDetected.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean selected = v.isSelected();
				if (selected) {
					mFilteredTypeList.remove((Integer)NotificationType.FART_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.FART_DETECTED, false);
				} else {
					mFilteredTypeList.add((Integer)NotificationType.FART_DETECTED);
					mPreferenceMgr.setDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.FART_DETECTED, true);
				}
				v.setSelected(!selected);
				showFilteredList();
			}
		});

		boolean filterSelected = mPreferenceMgr.getDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.DIAPER_CHANGED);
		btnFilterDiaperChanged.setSelected(filterSelected);
		if (filterSelected) {
			mFilteredTypeList.add((Integer)NotificationType.DIAPER_CHANGED);
		} else {
			mFilteredTypeList.remove((Integer)NotificationType.DIAPER_CHANGED);
		}

		filterSelected = mPreferenceMgr.getDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.PEE_DETECTED);
		btnFilterPeeDetected.setSelected(filterSelected);
		if (filterSelected) {
			mFilteredTypeList.add((Integer)NotificationType.PEE_DETECTED);
		} else {
			mFilteredTypeList.remove((Integer)NotificationType.PEE_DETECTED);
		}

		filterSelected = mPreferenceMgr.getDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.POO_DETECTED);
		btnFilterPooDetected.setSelected(filterSelected);
		if (filterSelected) {
			mFilteredTypeList.add((Integer)NotificationType.POO_DETECTED);
		} else {
			mFilteredTypeList.remove((Integer)NotificationType.POO_DETECTED);
		}

		filterSelected = mPreferenceMgr.getDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.ABNORMAL_DETECTED);
		btnFilterAbnormalDetected.setSelected(filterSelected);
		if (filterSelected) {
			mFilteredTypeList.add((Integer)NotificationType.ABNORMAL_DETECTED);
		} else {
			mFilteredTypeList.remove((Integer)NotificationType.ABNORMAL_DETECTED);
		}

		filterSelected = mPreferenceMgr.getDeviceNotificationFilterSelected(mDeviceType, mDeviceId, NotificationType.FART_DETECTED);
		btnFilterFartDetected.setSelected(filterSelected);
		if (filterSelected) {
			mFilteredTypeList.add((Integer)NotificationType.FART_DETECTED);
		} else {
			mFilteredTypeList.remove((Integer)NotificationType.FART_DETECTED);
		}

		lctnFilterSection.setVisibility(View.VISIBLE);
		*/

		// 필터 삭제
		mFilteredTypeList.add((Integer)NotificationType.DIAPER_CHANGED);
		mFilteredTypeList.add((Integer)NotificationType.PEE_DETECTED);
		mFilteredTypeList.add((Integer)NotificationType.POO_DETECTED);
		mFilteredTypeList.add((Integer)NotificationType.ABNORMAL_DETECTED);
		mFilteredTypeList.add((Integer)NotificationType.FART_DETECTED);
		mFilteredTypeList.add((Integer)NotificationType.DIAPER_NEED_TO_CHANGE);
        mFilteredTypeList.add((Integer)NotificationType.BABY_SLEEP);
		mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BABY_FOOD);
		mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK);
		mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK);
		mFilteredTypeList.add((Integer)NotificationType.BABY_FEEDING_NURSED_BREAST_MILK);
		lctnFilterSection.setVisibility(View.GONE);

		// Add Notification
		btnAddNotification = (Button)v.findViewById(R.id.btn_notification_add_notification);
		btnAddNotification.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mMainActivity, FloatActivity.class);
				mMainActivity.startActivityForResult(intent, DeviceSensorActivity.REQCODE_FLOAT_ADD_NOTIFICATION);
				mMainActivity.overridePendingTransition(0, 0);
			}
		});

		if (Configuration.BETA_TEST_MODE) {
			btnAddNotification.setVisibility(View.VISIBLE);
		} else {
			btnAddNotification.setVisibility(View.GONE);
		}

		rvNotificationList = (RecyclerView) v.findViewById(R.id.rv_notification_list);
		if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK) {
			mMsgAdapter = new FeedbackMsgAdapter(mContext);
		} else {
			mMsgAdapter = new NotificationMsgAdapter(mContext);
		}

		rvNotificationList.setAdapter(mMsgAdapter);
		mMsgAdapter.setRecyclerView(rvNotificationList);
		mMsgAdapter.setOnLoadMoreListener(new NotificationMsgAdapter.OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if (DBG) Log.d(TAG, "onLoadMore");
				mMsgAdapter.setProgressMore(true);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mMsgAdapter.setProgressMore(false);
						ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getDiaperSensorMessagesV2(DeviceType.DIAPER_SENSOR, mDeviceId, mLastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
						if (messageList != null && messageList.size() > 0) {
							for (NotificationMessage mm : messageList) {
								mNotificationMessageList.add(mm);
								if (mLastLoadedMessageTimeMs > mm.timeMs) {
									mLastLoadedMessageTimeMs = mm.timeMs;
								}
							}
							mMsgAdapter.addList(messageList);
							showFilteredList();
							mMsgAdapter.setMoreLoading(false);
						}
					}
				}, LOADING_MESSAGE_SEC * 1000);
			}
		});
		//LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
		//rvNotificationList.setLayoutManager(linearLayoutManager);
		//rvNotificationList.setItemAnimator(new DefaultItemAnimator());

		loadMessageList();
		showFilteredList();
	}

	public void loadLatestMessageList() {
		if (DBG) Log.e(TAG, "loadLatestMessageList");
		mLastLoadedMessageTimeMs = System.currentTimeMillis();
		loadMessageList();
	}

	public void loadMessageList() {
		mLastLoadedMessageTimeMs = System.currentTimeMillis();
		if (DBG) Log.e(TAG, "loadMessageList: " + mLastLoadedMessageTimeMs);
		if (mNotificationMessageList == null) {
			mNotificationMessageList = new ArrayList<>();
		}
		mNotificationMessageList.clear();
		long latestCheckedNotificationIdx = mPreferenceMgr.getLatestCheckedNotificationIndex(mDeviceType, mDeviceId);
		mPreferenceMgr.setLatestCheckedNotificationIndex(mDeviceType, mDeviceId, mPreferenceMgr.getLatestSavedNotificationIndex(mDeviceType, mDeviceId, 0));
		mMsgAdapter.setLatestCheckedNotificationIndex(latestCheckedNotificationIdx);

		ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getDiaperSensorMessagesV2(DeviceType.DIAPER_SENSOR, mDeviceId, mLastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
		if (messageList != null && messageList.size() > 0) {
			for (NotificationMessage mm : messageList) {
				mNotificationMessageList.add(mm);
				if (mLastLoadedMessageTimeMs > mm.timeMs) {
					mLastLoadedMessageTimeMs = mm.timeMs;
				}
				if (DBG) Log.d(TAG, "msg: " + mm.toString());
			}
			if (DBG) Log.e(TAG, "loadMessageList : " + latestCheckedNotificationIdx + " / " + messageList.get(0).msgId + " / checked: " + mPreferenceMgr.getLatestCheckedNotificationIndex(mDeviceType, mDeviceId));
		}
	}

	public void showFilteredList() {
		if (DBG) Log.e(TAG, "showFilteredList");
		if (mNotificationMessageList == null) return;
		boolean isValid;
		ArrayList<NotificationMessage> tempNotificationMessageList = new ArrayList<>();
		for (NotificationMessage mm : mNotificationMessageList) {
			isValid = false;
			for (int filterType : mFilteredTypeList) {
				if (mm.notiType == filterType) {
					if (mm.notiType == NotificationType.PEE_DETECTED || mm.notiType == NotificationType.POO_DETECTED || mm.notiType == NotificationType.FART_DETECTED || mm.notiType == NotificationType.ABNORMAL_DETECTED || mm.notiType == NotificationType.DIAPER_NEED_TO_CHANGE) {
						if (Configuration.BETA_TEST_MODE) {
							if (mm.extra != null && !mm.extra.equals("-") && !mm.extra.equals("")) {
								// 피드백이 입력되어 있으면 보여주지 않기
							} else {
								// 입력이 안되어있으면, 보여주기
								isValid = true;
							}
						} else {
							isValid = true;
						}
					} else {
						isValid = true;
					}
					break;
				} else if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK && mm.notiType == NotificationType.CHAT_USER_INPUT) {
					isValid = true;
				} else if (Configuration.ALLOW_DIAPER_DETECT_FEEDBACK && mm.notiType == NotificationType.CHAT_USER_FEEDBACK) {
					// 피드백이 입력되어 있지 않으면 보여주기
					if (mm.extra != null && (mm.extra.equals("d10") || mm.extra.equals("d40") || mm.extra.equals("11") || mm.extra.equals("12") || mm.extra.equals("13") || mm.extra.equals("-") || mm.extra.equals(""))) {
						isValid = true;
					}
					break;
				} else if (mm.notiType == NotificationType.DIAPER_DETACHMENT_DETECTED) {
					isValid = true;
					break;
				}
			}
			if (isValid) {
				tempNotificationMessageList.add(mm);
				if (DBG) Log.d(TAG, "  add: " + mm.toString());
			}
		}

		if (tempNotificationMessageList.size() == 0) {
			mMsgAdapter.setList(tempNotificationMessageList);
			tvEmpty.setVisibility(View.VISIBLE);
			return;
		} else {
			tvEmpty.setVisibility(View.GONE);
		}
		mMsgAdapter.setList(tempNotificationMessageList);
		mMsgAdapter.notifyDataSetChanged();
	}

	public void setDeviceId(long deviceId) {
		mDeviceId = deviceId;
	}

	public void setDeviceType(int type) {
		mDeviceType = type;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (DBG) Log.i(TAG, "onPause");

		NotiManager.getInstance(mContext).cancelMessageNotification(mDeviceType, mDeviceId);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		if (ConnectionManager.getInstance() != null) {
			ConnectionManager.getInstance().getNotificationFromCloudV2(mDeviceType, mDeviceId);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
