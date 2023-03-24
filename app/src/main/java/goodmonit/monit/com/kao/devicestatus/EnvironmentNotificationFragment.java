package goodmonit.monit.com.kao.devicestatus;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.NotiManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.message.NotificationMsgAdapter;
import goodmonit.monit.com.kao.message.RecyclerViewAdapter;
import goodmonit.monit.com.kao.services.ConnectionManager;

import static goodmonit.monit.com.kao.message.RecyclerViewAdapter.LOADING_MESSAGE_SEC;

public class EnvironmentNotificationFragment extends BaseFragment {
	private static final String TAG = Configuration.BASE_TAG + "Notification";
	private static final boolean DBG = Configuration.DBG;

	private RecyclerView rvNotificationList;
	private long mDeviceId;
	private int mDeviceType;

	private ArrayList<NotificationMessage> mNotificationMessageList;
	private ArrayList<Integer> mFilteredTypeList;
	private RecyclerViewAdapter mMsgAdapter;
	private TextView tvEmpty;
	private long mLastLoadedMessageTimeMs;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_aqmhub_notification, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mServerQueryMgr = ServerQueryManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(1103);
		mNotificationMessageList = new ArrayList<>();
		mFilteredTypeList = new ArrayList<>();
		mLastLoadedMessageTimeMs = System.currentTimeMillis();
		_initView(view);

		return view;
	}

	private void _initView(View v) {
		tvEmpty = (TextView)v.findViewById(R.id.tv_notification_empty);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
		rvNotificationList = (RecyclerView) v.findViewById(R.id.rv_notification_list);
		mMsgAdapter = new NotificationMsgAdapter(mContext);

		loadMessageList();

		rvNotificationList.setAdapter(mMsgAdapter);
		mMsgAdapter.setLinearLayoutManager(linearLayoutManager);
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
						ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getAQMHubMessages(mDeviceId, mLastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
						if (messageList != null && messageList.size() > 0) {
							for (NotificationMessage mm : messageList) {
								mNotificationMessageList.add(mm);
								if (mLastLoadedMessageTimeMs > mm.timeMs) {
									mLastLoadedMessageTimeMs = mm.timeMs;
								}
								if (DBG) Log.d(TAG, "msg: " + mm.toString());
							}
							mMsgAdapter.addList(messageList);
							showFilteredList();
							mMsgAdapter.setMoreLoading(false);
						}
					}
				}, LOADING_MESSAGE_SEC * 1000);
			}
		});
		rvNotificationList.setLayoutManager(linearLayoutManager);
		rvNotificationList.setItemAnimator(new DefaultItemAnimator());

		showFilteredList();
	}

	public void loadLatestMessageList() {
		if (DBG) Log.e(TAG, "loadLatestMessageList");
		mLastLoadedMessageTimeMs = System.currentTimeMillis();
		loadMessageList();
	}

	public void loadMessageList() {
		if (DBG) Log.e(TAG, "loadMessageList");
		if (mNotificationMessageList == null) {
			mNotificationMessageList = new ArrayList<>();
		}
		mNotificationMessageList.clear();
		long latestCheckedNotificationIdx = mPreferenceMgr.getLatestCheckedNotificationIndex(mDeviceType, mDeviceId);

		long latestSavedNotificationIdx = mPreferenceMgr.getLatestSavedNotificationIndex(mDeviceType, mDeviceId, 0);
		if (latestSavedNotificationIdx < mPreferenceMgr.getLatestSavedNotificationIndex(mDeviceType, mDeviceId, 1)) {
			latestSavedNotificationIdx = mPreferenceMgr.getLatestSavedNotificationIndex(mDeviceType, mDeviceId, 1);
		}
		if (latestSavedNotificationIdx < mPreferenceMgr.getLatestSavedNotificationIndex(mDeviceType, mDeviceId, 2)) {
			latestSavedNotificationIdx = mPreferenceMgr.getLatestSavedNotificationIndex(mDeviceType, mDeviceId, 2);
		}
		if (mPreferenceMgr.getLatestCheckedNotificationIndex(mDeviceType, mDeviceId) < latestSavedNotificationIdx) {
			mPreferenceMgr.setLatestCheckedNotificationIndex(mDeviceType, mDeviceId, latestSavedNotificationIdx);
		}

		mMsgAdapter.setLatestCheckedNotificationIndex(latestCheckedNotificationIdx);

		ArrayList<NotificationMessage> messageList = DatabaseManager.getInstance(mContext).getAQMHubMessages(mDeviceId, mLastLoadedMessageTimeMs, RecyclerViewAdapter.COUNT_LOADED_MESSAGES_AT_ONCE);
		if (messageList != null && messageList.size() > 0) {
			for (NotificationMessage mm : messageList) {
				mNotificationMessageList.add(mm);
				if (mLastLoadedMessageTimeMs > mm.timeMs) {
					mLastLoadedMessageTimeMs = mm.timeMs;
				}
				if (DBG) Log.d(TAG, "msg: " + mm.toString());
			}
			if (DBG) Log.e(TAG, "updateMessageList : " + latestCheckedNotificationIdx + " / " + messageList.get(0).msgId + " / " + mLastLoadedMessageTimeMs);
		}
	}

	public void showFilteredList() {
		if (DBG) Log.e(TAG, "showFilteredList");
		if (mNotificationMessageList == null) return;

		if (mNotificationMessageList.size() == 0) {
			mMsgAdapter.setList(mNotificationMessageList);
			tvEmpty.setVisibility(View.VISIBLE);
			return;
		} else {
			tvEmpty.setVisibility(View.GONE);
		}

		mMsgAdapter.setList(mNotificationMessageList);
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
		if (mMsgAdapter != null) {
			((NotificationMsgAdapter)mMsgAdapter).updatePreference();
		}
		showFilteredList();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
