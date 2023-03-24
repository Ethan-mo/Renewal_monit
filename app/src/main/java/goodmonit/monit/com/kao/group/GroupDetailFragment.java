package goodmonit.monit.com.kao.group;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.UserInfo.Group;
import goodmonit.monit.com.kao.UserInfo.UserInfo;
import goodmonit.monit.com.kao.activity.GroupActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.widget.GroupDetailShareDevice;

public class GroupDetailFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "GroupDetail";
	private static final boolean DBG = Configuration.DBG;

	public static final int MSG_REFRESH_VIEW = 1;

	private TextView tvHeaderDescription, tvFooterDescription;
	private TextView tvShareDeviceTitle, tvMemberTitle;
	private TextView tvGroupLeaderId, tvGroupLeaderShortId, tvShareDeviceEmpty;
	private LinearLayout lctnShareDeviceList;
	private ArrayList<GroupMember> mGroupMemberList;
	private Group mSelectedGroup;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_group_detail, container, false);

		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(801);
		mGroupMemberList = new ArrayList<>();
        _initView(view);

		_setHeaderText();
		_setGroupMember();
		_setShareDeviceList();
		_setFooterText();

        return view;
    }

    public void setSelectedGroup(Group selectedGroup) {
		mSelectedGroup = selectedGroup;
	}

	private void _setGroupLeader(String id, String shortId) {
		tvGroupLeaderId.setText(id);
		tvGroupLeaderShortId.setText(shortId);
	}

	private void _setGroupMember() {
		if (mGroupMemberList != null) {
			for (GroupMember groupMember : mGroupMemberList) {
				if (groupMember == null) continue;
				groupMember.initialize();
			}
		}

		if (mSelectedGroup == null) return;

		ArrayList<UserInfo> userInfoList = mSelectedGroup.getMemberList();
		if (userInfoList != null) {
			if (mSelectedGroup.getLeaderInfo().accountId == mPreferenceMgr.getAccountId()) {
				tvMemberTitle.setText(getString(R.string.group_title_shared_member_count, userInfoList.size() - 1).toUpperCase());
			} else {
				tvMemberTitle.setText(getString(R.string.group_title_shared_member_count, userInfoList.size()).toUpperCase());
			}
		} else {
			tvMemberTitle.setText(getString(R.string.group_title_shared_member_count, 0).toUpperCase());
		}

		if (userInfoList != null) {
			int familyType = 0;
			for (int i = 0; i < userInfoList.size(); i++) {
				final UserInfo userInfo = userInfoList.get(i);
				if (userInfo == null) continue;

				if (userInfo.groupLeader || userInfo.accountId == mSelectedGroup.getId()) {
					_setGroupLeader(userInfo.nickName, userInfo.shortId);
					continue;
				}

				if (userInfo.familyType < GroupFamilyType.MOM || userInfo.familyType > GroupFamilyType.GRANDPA) {
					familyType = GroupFamilyType.MOM;
				} else {
					familyType = userInfo.familyType;
				}
				familyType--; // GroupMemberList Index Range 0~3, familyType Range 1~4

				mGroupMemberList.get(familyType).setActivated(true);
				mGroupMemberList.get(familyType).setMemberId(userInfo.nickName);
				mGroupMemberList.get(familyType).setMemberShortId(userInfo.shortId);
				mGroupMemberList.get(familyType).setMemberAccountId(userInfo.accountId);
				mGroupMemberList.get(familyType).setMemberEmail(userInfo.email);
			}
		}
	}

	private void _setHeaderText() {
		if (mPreferenceMgr == null || mSelectedGroup == null) {
			tvHeaderDescription.setText(getString(R.string.group_title_sharing_description));
			return;
		}

		if (mPreferenceMgr.getAccountId() == mSelectedGroup.getId()) {
			tvHeaderDescription.setText(getString(R.string.group_title_sharing_description));
		} else {
			tvHeaderDescription.setText(getString(R.string.group_title_shared_description));
		}
	}

	private void _setFooterText() {
		if (mPreferenceMgr.getAccountId() == mSelectedGroup.getId()) {
			tvFooterDescription.setText(getString(R.string.group_share_member_description));
		} else {
			tvFooterDescription.setText(getString(R.string.group_leave_description));
		}
	}

	private void _setShareDeviceList() {
		lctnShareDeviceList.removeAllViews();

		ArrayList<DeviceInfo> deviceInfoList = mSelectedGroup.getDeviceList();

		if (deviceInfoList != null) {
			tvShareDeviceTitle.setText(getString(R.string.group_title_device_count, deviceInfoList.size()).toUpperCase());
		} else {
			tvShareDeviceTitle.setText(getString(R.string.group_title_device_count, 0).toUpperCase());
		}

		if (deviceInfoList != null) {
			GroupDetailShareDevice shareDeviceList = null;
			if (deviceInfoList.size() > 0) {
				tvShareDeviceEmpty.setVisibility(View.GONE);
			}
			for (int i = 0; i < deviceInfoList.size(); i++) {
				DeviceInfo deviceInfo = deviceInfoList.get(i);
				if (i % 2 == 0) {
					shareDeviceList = new GroupDetailShareDevice(mContext);
					lctnShareDeviceList.addView(shareDeviceList);
					shareDeviceList.setLeftDeviceType(deviceInfo.type);
					shareDeviceList.setLeftDeviceName(deviceInfo.name);
				} else {
					shareDeviceList.setRightDeviceType(deviceInfo.type);
					shareDeviceList.setRightDeviceName(deviceInfo.name);
				}
			}
		}
	}

	private void _initView(View v) {
		tvHeaderDescription = (TextView)v.findViewById(R.id.tv_group_detail_header_description);
		tvFooterDescription = (TextView)v.findViewById(R.id.tv_group_detail_footer_description);
		tvShareDeviceTitle = (TextView)v.findViewById(R.id.tv_group_detail_share_device_title);
		tvMemberTitle = (TextView)v.findViewById(R.id.tv_group_detail_member_title);
		lctnShareDeviceList = (LinearLayout)v.findViewById(R.id.lctn_group_detail_share_device_list);
		tvGroupLeaderId = (TextView)v.findViewById(R.id.tv_group_detail_leader_id);
		tvGroupLeaderShortId = (TextView)v.findViewById(R.id.tv_group_detail_leader_short_id);
		tvShareDeviceEmpty = (TextView)v.findViewById(R.id.tv_group_detail_share_device_list_empty);

		mGroupMemberList.add(
				new GroupMember(mContext,
						GroupFamilyType.MOM,
						v.findViewById(R.id.iv_group_detail_member1_icon),
						v.findViewById(R.id.lctn_group_detail_member1_info),
						v.findViewById(R.id.tv_group_detail_member1_info_id),
						v.findViewById(R.id.tv_group_detail_member1_info_short_id),
						v.findViewById(R.id.btn_group_detail_member1_add),
						v.findViewById(R.id.v_group_detail_member1_divider_left),
						v.findViewById(R.id.v_group_detail_member1_divider_right),
						v.findViewById(R.id.v_group_detail_member1_divider_top),
						v.findViewById(R.id.v_group_detail_member1_divider_bottom)));

		mGroupMemberList.add(
				new GroupMember(mContext,
						GroupFamilyType.DAD,
						v.findViewById(R.id.iv_group_detail_member2_icon),
						v.findViewById(R.id.lctn_group_detail_member2_info),
						v.findViewById(R.id.tv_group_detail_member2_info_id),
						v.findViewById(R.id.tv_group_detail_member2_info_short_id),
						v.findViewById(R.id.btn_group_detail_member2_add),
						v.findViewById(R.id.v_group_detail_member2_divider_left),
						v.findViewById(R.id.v_group_detail_member2_divider_right),
						v.findViewById(R.id.v_group_detail_member2_divider_top),
						v.findViewById(R.id.v_group_detail_member2_divider_bottom)));

		mGroupMemberList.add(
				new GroupMember(mContext,
						GroupFamilyType.GRANDMA,
						v.findViewById(R.id.iv_group_detail_member3_icon),
						v.findViewById(R.id.lctn_group_detail_member3_info),
						v.findViewById(R.id.tv_group_detail_member3_info_id),
						v.findViewById(R.id.tv_group_detail_member3_info_short_id),
						v.findViewById(R.id.btn_group_detail_member3_add),
						v.findViewById(R.id.v_group_detail_member3_divider_left),
						v.findViewById(R.id.v_group_detail_member3_divider_right),
						v.findViewById(R.id.v_group_detail_member3_divider_top),
						v.findViewById(R.id.v_group_detail_member3_divider_bottom)));

		mGroupMemberList.add(
				new GroupMember(mContext,
						GroupFamilyType.GRANDPA,
						v.findViewById(R.id.iv_group_detail_member4_icon),
						v.findViewById(R.id.lctn_group_detail_member4_info),
						v.findViewById(R.id.tv_group_detail_member4_info_id),
						v.findViewById(R.id.tv_group_detail_member4_info_short_id),
						v.findViewById(R.id.btn_group_detail_member4_add),
						v.findViewById(R.id.v_group_detail_member4_divider_left),
						v.findViewById(R.id.v_group_detail_member4_divider_right),
						v.findViewById(R.id.v_group_detail_member4_divider_top),
						v.findViewById(R.id.v_group_detail_member4_divider_bottom)));

    }

    private void refreshView() {
		_setHeaderText();
		_setGroupMember();
		_setShareDeviceList();
		_setFooterText();
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
		((GroupActivity)mMainActivity).updateNewMark();
		refreshView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}

	public void inviteMember(int index) {
		((GroupActivity)mMainActivity).showInviteMemberDialog(index);
	}

	public void deleteMember(long accountId) {
		((GroupActivity)mMainActivity).showDeleteMemberDialog(accountId);
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

	class GroupMember {
		private Context mmContext;
		private int mmFamilyType;
		private String mmEmail;
		private long mmAccountId;
		private boolean isActiviated;
		private ImageView ivIcon;
		private TextView tvMemberId, tvMemberShortId;
		private LinearLayout lctnMemberInfo;
		private Button btnAdd;
		private View dividerLeft, dividerRight, dividerTop, dividerBottom;

		public GroupMember(
				Context context,
				int familyType,
				View resImageViewIcon,
				View resLinearLayoutInfo,
				View resTextViewId,
				View resTextViewShortId,
				View resButtonAdd,
				View resDividerLeft,
				View resDividerRight,
				View resDividerTop,
				View resDividerBottom) {
			mmContext = context;
			mmFamilyType = familyType;
			ivIcon = (ImageView)resImageViewIcon;
			lctnMemberInfo = (LinearLayout)resLinearLayoutInfo;
			tvMemberId = (TextView)resTextViewId;
			tvMemberShortId = (TextView)resTextViewShortId;
			btnAdd = (Button)resButtonAdd;
			dividerLeft = resDividerLeft;
			dividerRight = resDividerRight;
			dividerBottom = resDividerBottom;
			dividerTop = resDividerTop;
			setActivated(false);

			btnAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					inviteMember(mmFamilyType);
				}
			});
			lctnMemberInfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteMember(mmAccountId);
				}
			});

		}

		public void initialize() {
			// mmFamilyType = 0; index는 엄마, 아빠, 할아버지, 할머니를 의미하므로 초기화하면 안됨
			mmEmail = null;
			mmAccountId = 0;
			setActivated(false);
		}

		public void setActivated(boolean activated) {
			isActiviated = activated;
			if (activated) {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivIcon.setBackgroundResource(R.drawable.ic_group_account_activated);
				} else {
					switch (mmFamilyType) {
						case GroupFamilyType.MOM:
							ivIcon.setBackgroundResource(R.drawable.ic_group_mom_activated);
							break;
						case GroupFamilyType.DAD:
							ivIcon.setBackgroundResource(R.drawable.ic_group_dad_activated);
							break;
						case GroupFamilyType.GRANDMA:
							ivIcon.setBackgroundResource(R.drawable.ic_group_grandma_activated);
							break;
						case GroupFamilyType.GRANDPA:
							ivIcon.setBackgroundResource(R.drawable.ic_group_grandpa_activated);
							break;
					}
				}
				dividerLeft.setBackgroundColor(mmContext.getResources().getColor(R.color.colorPrimary));
				dividerTop.setBackgroundColor(mmContext.getResources().getColor(R.color.colorPrimary));
				dividerBottom.setBackgroundColor(mmContext.getResources().getColor(R.color.colorPrimary));
				dividerRight.setBackgroundColor(mmContext.getResources().getColor(R.color.colorPrimary));
				lctnMemberInfo.setVisibility(View.VISIBLE);
				btnAdd.setVisibility(View.GONE);
			} else {
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					ivIcon.setBackgroundResource(R.drawable.ic_group_account_deactivated);
				} else {
					switch (mmFamilyType) {
						case GroupFamilyType.MOM:
							ivIcon.setBackgroundResource(R.drawable.ic_group_mom_deactivated);
							break;
						case GroupFamilyType.DAD:
							ivIcon.setBackgroundResource(R.drawable.ic_group_dad_deactivated);
							break;
						case GroupFamilyType.GRANDMA:
							ivIcon.setBackgroundResource(R.drawable.ic_group_grandma_deactivated);
							break;
						case GroupFamilyType.GRANDPA:
							ivIcon.setBackgroundResource(R.drawable.ic_group_grandpa_deactivated);
							break;
					}
				}
				dividerLeft.setBackgroundColor(mmContext.getResources().getColor(R.color.colorDivider));
				dividerTop.setBackgroundColor(mmContext.getResources().getColor(R.color.colorDivider));
				dividerBottom.setBackgroundColor(mmContext.getResources().getColor(R.color.colorDivider));
				dividerRight.setBackgroundColor(mmContext.getResources().getColor(R.color.colorDivider));
				lctnMemberInfo.setVisibility(View.GONE);
				btnAdd.setVisibility(View.VISIBLE);
			}
		}

		public void setMemberAccountId(long accountId) {
			mmAccountId = accountId;
		}

		public void setMemberEmail(String email) {
			mmEmail = email;
		}

		public void setMemberId(String name) {
			tvMemberId.setText(name);
		}

		public void setMemberShortId(String shortId) {
			tvMemberShortId.setText(shortId);
		}
	}

}