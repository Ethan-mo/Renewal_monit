package goodmonit.monit.com.kao.group;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.UserInfo.Group;
import goodmonit.monit.com.kao.activity.GroupActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.widget.GroupSharedListItem;

public class GroupSharedFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "SharedFragment";
	private static final boolean DBG = Configuration.DBG;

	public static final int MSG_REFRESH_VIEW = 1;

	public TextView tvHeaderDescription;
	public TextView tvEmptyList;
	public LinearLayout lctnSharedGroupList;

	public UserInfoManager mUserInfoMgr;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_group_shared_list, container, false);

		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mUserInfoMgr = UserInfoManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(802);
        _initView(view);

        return view;
    }

	private void _initView(View v) {
		tvHeaderDescription = (TextView)v.findViewById(R.id.tv_group_shared_list_header_description);
		lctnSharedGroupList = (LinearLayout)v.findViewById(R.id.lctn_group_shared_list);
		tvEmptyList = (TextView)v.findViewById(R.id.tv_group_shared_list_empty);
    }

    public void refreshView() {
		lctnSharedGroupList.removeAllViews();
		tvEmptyList.setVisibility(View.VISIBLE);
		for (Group group : mUserInfoMgr.getGroupList()) {
			final Group selectedGroup = group;
			if (selectedGroup.getLeaderInfo().accountId == mPreferenceMgr.getAccountId()) continue;

			if (tvEmptyList.getVisibility() == View.VISIBLE) {
				tvEmptyList.setVisibility(View.GONE);
			} else { // EmptyList가 GONE이면 하나 이상의 아이템이 있으므로, 그때부터 간격을 벌림
				lctnSharedGroupList.addView(getLayoutInflater().inflate(R.layout.widget_setting_divider, null));
			}

			GroupSharedListItem item = new GroupSharedListItem(mContext);
			item.setGroupLeaderId(selectedGroup.getLeaderInfo().nickName);
			item.setGroupLeaderShortId(selectedGroup.getLeaderInfo().shortId);
			item.setDeviceList(selectedGroup.getDeviceList());
			item.setOnLeaveListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((GroupActivity)mMainActivity).showLeaveCloudDialog(selectedGroup);
				}
			});
			lctnSharedGroupList.addView(item);
		}
	}

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
		mHandler.removeMessages(MSG_REFRESH_VIEW);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		((GroupActivity)mMainActivity).updateNewMark();
		refreshView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_VIEW:
					refreshView();
					break;
			}
		}
	};
}