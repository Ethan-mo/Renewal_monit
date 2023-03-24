package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.UserInfo.Group;
import goodmonit.monit.com.kao.UserInfo.GroupInfo;
import goodmonit.monit.com.kao.UserInfo.MyUserInfo;
import goodmonit.monit.com.kao.UserInfo.UserInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.Policy;
import goodmonit.monit.com.kao.constants.SignInState;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class UserInfoManager {
	private static final String TAG = Configuration.BASE_TAG + "UserInfoManager";
	private static final boolean DBG = Configuration.DBG;

    private Context mContext;
	public ArrayList<Group> mGroupList; // 전체그룹에 대한 리스트
	public ArrayList<UserInfo> mUserInfoList; // 전체계정정보에 대한 리스트
	public MyUserInfo myUserInfo; // 내 계정정보 저장
	private int mMyGroupCount = 0;

	private static UserInfoManager mUserInfoMgr = null;
	private PreferenceManager mPreferenceMgr;

    public static UserInfoManager getInstance(Context context) {
    	if (DBG) Log.d(TAG, "getInstance()");
    	if (mUserInfoMgr == null) {
			mUserInfoMgr = new UserInfoManager(context);
    	}
    	return mUserInfoMgr;
    }

	public UserInfoManager(Context context) {
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
		myUserInfo = new MyUserInfo();
		mGroupList = new ArrayList<Group>();
		mUserInfoList = new ArrayList<UserInfo>();
		mMyGroupCount = 0;
		refreshMyUserInfo();
	}

	public void refreshMyUserInfo() {
		myUserInfo.email = mPreferenceMgr.getSigninEmail();
		myUserInfo.accountId = mPreferenceMgr.getAccountId();
		myUserInfo.token = mPreferenceMgr.getSigninToken();
		myUserInfo.pushToken = mPreferenceMgr.getPushToken();
		myUserInfo.nickName = mPreferenceMgr.getProfileNickname();
		myUserInfo.birthdayString = mPreferenceMgr.getProfileBirthday();
		myUserInfo.sex = mPreferenceMgr.getProfileSex();
		myUserInfo.shortId = mPreferenceMgr.getShortId();
		myUserInfo.cloudId = mPreferenceMgr.getAccountId();

		if (DBG) Log.i(TAG, "refreshInfo : " +
				myUserInfo.email + " / " +
				myUserInfo.accountId + " / " +
				myUserInfo.token + " / " +
				myUserInfo.pushToken + " / " +
				myUserInfo.nickName + " / " +
				myUserInfo.birthdayString + " / " +
				myUserInfo.sex + " / " +
				myUserInfo.shortId + " / " +
				myUserInfo.cloudId);
	}

	public Group getMyGroupInfo() {
		if (mGroupList != null && mGroupList.size() > 0) {
			return mGroupList.get(0);
		} else {
			return null;
		}
	}

	public void addMyGroupMember(UserInfo userInfo) {
		mGroupList.get(0).addMember(userInfo);
	}

	public void removeMyGroupMember(UserInfo userInfo) {
		mGroupList.get(0).removeMember(userInfo);
	}

	/**
	 *  initGroupList
	 *  전체 그룹정보 초기화하기
	 */
	public void initGroupList() {
		mGroupList.clear();
	}

	/**
	 *  initUserInfoList
	 *  전체 사용자 정보 초기화하기
	 */
	public void initUserInfoList() {
		mUserInfoList.clear();
	}

	/**
	 *  getGroupList
	 *  전체 그룹정보(그룹이름, 그룹멤버, 그룹디바이스) 받아오기
	 */
	public ArrayList<Group> getGroupList() {
		return mGroupList;
	}

	/**
	 *  getGroupCount
	 *  전체 그룹의 개수 받아오기
	 */
	public int getGroupListCount() {
		return mGroupList.size();
	}

	/**
	 *  getMyGroupCount
	 *  내 그룹의 구성원 수 받아오기
	 */
	public int getMyGroupCount() {
		return mMyGroupCount;
	}

	/**
	 *  refreshGroupList
	 *  전체 그룹정보(그룹이름, 그룹멤버, 그룹디바이스) 갱신하기
	 */
	public void refreshGroupList() {
		initGroupList();
		mMyGroupCount = 0;
		for (UserInfo userInfo : mUserInfoList) {
			if (userInfo.cloudId == mPreferenceMgr.getAccountId()) {
				mMyGroupCount++;
			}
			_addUserInfo(userInfo);
		}

		// 3. 각 그룹에 Device정보 배치하기
		for (DeviceDiaperSensor sensor : ConnectionManager.mRegisteredDiaperSensorList.values()) {
			for (Group group : mGroupList) {
				if (group.getId() == sensor.cloudId) {
					group.addDevice(sensor.getDeviceInfo());
					break;
				}
			}
		}

		for (DeviceAQMHub hub : ConnectionManager.mRegisteredAQMHubList.values()) {
			for (Group group : mGroupList) {
				if (group.getId() == hub.cloudId) {
					group.addDevice(hub.getDeviceInfo());
					break;
				}
			}
		}

		//mGroupList = groupList;
		if (DBG) Log.d(TAG, "refreshGroupList : " + mGroupList.size());
	}

//	/**
//	 *  addGroup
//	 *  전체 그룹정보 리스트에 해당 그룹정보를 추가하고 DB에서도 추가함
//	 */
//	public void addGroup(Group group) {
//		DatabaseManager.getInstance(mContext).insertDB(group.getGroupInfo());
//		mGroupList.add(group);
//	}

	/**
	 *  removeGroup
	 *  전체 그룹정보 리스트에서 해당 그룹정보를 삭제하고 DB에서도 삭제함
	 *  해당 그룹의 BLE Device도 DB에서 삭제함
	 */
	public void removeGroup(Group group) {
		// 1. 그룹 리스트 및 DB에서 삭제
		mGroupList.remove(group);

		// 2. UserInfo 리스트에서 삭제
		// Object 삭제시에는 바로 삭제가 불가능. Concurrent exception 발생.
		// 따라서 맨 뒤에서부터 삭제를 해야 에러가 안남
		int cntUserInfoList = mUserInfoList.size();
		for (int i = cntUserInfoList - 1; i >= 0 ; i--) {
			if (mUserInfoList.get(i).cloudId == group.getId()) {
				mUserInfoList.remove(i);
			}
		}

		// 3. BLE Connection을 위한 리스트 및 DB에서 삭제
		Iterator<DeviceBLEConnection> itr = ConnectionManager.getDeviceBLEConnectionList().values().iterator();
		while (itr.hasNext()) {
			DeviceBLEConnection bleConnection = itr.next();
			if (bleConnection.getDeviceInfo().cloudId == group.getId()) {
				ConnectionManager.mRegisteredDiaperSensorList.remove(bleConnection.getDeviceInfo().deviceId);
				bleConnection.unregister();
				itr.remove();
			}
		}
	}

	/**
	 *  addUserInfo
	 *  클라우드에서 갱신한 UserInfo 를 추가하면서 각 그룹에 배치하는 작업을 진행함
	 */
	public void addUserInfo(UserInfo userinfo) {
		mUserInfoList.add(userinfo);
		_addUserInfo(userinfo);
	}

	/**
	 *  removeUserInfo
	 *  클라우드에서 삭제한 UserInfo 를 갱신하면서 각 그룹에 배치하는 작업을 진행함
	 */
	public void removeUserInfo(UserInfo userinfo) {
		mUserInfoList.remove(userinfo);
		for (Group gr : mGroupList) {
			gr.removeMember(userinfo);
		}
	}

	/**
	 *  _addUserInfo
	 *  클라우드에서 갱신한 UserInfo 를 추가하면서 각 그룹에 배치하는 작업을 진행함
	 */
	private void _addUserInfo(UserInfo userinfo) {
		boolean added = false;
		// 1. 이미 생성된 그룹이 있는지 확인한다.
		for (Group gr : mGroupList) {
			if (gr.getId() == userinfo.cloudId) { // 생성된 그룹이 있다면, 그 그룹에 넣는다.
				if (DBG) Log.i(TAG, "Input existed Group : " + userinfo.cloudId);
				gr.addMember(userinfo);
				added = true;
				break;
			}
		}
		if (added) return;

		// 2. 기존 DB에 저장된 그룹ID가 있다면 그룹에 기존 이름을 넣어 그룹 객체를 만들고
		//    없다면, '새그룹' 이름을 넣어 새로운 그룹 객체를 만든다.
		GroupInfo groupInfo = new GroupInfo(userinfo.cloudId, mContext.getString(R.string.group_name_default));

		// 3. 그룹을 그룹 리스트에 추가한다.
		Group gr = new Group(groupInfo);
		gr.addMember(userinfo);
		if (myUserInfo.cloudId == userinfo.cloudId) {
			if (DBG) Log.i(TAG, "Input my Group : " + userinfo.cloudId);
			mGroupList.add(0, gr); // 내 그룹은 0번 index
		} else {
			if (DBG) Log.i(TAG, "Input new Group : " + userinfo.cloudId);
			mGroupList.add(gr); // 내 그룹이 아니면 뒤로 붙이면 됨
		}
	}

	/**
	 *  getEmailAddress
	 *  account에 맞는 이메일 주소를 반환
	 */
	public String getEmailAddress(long accountId) {
		String email = null;
		for (UserInfo userInfo : mUserInfoList) {
			if (userInfo.accountId == accountId) {
				email = userInfo.email;
				break;
			}
		}
		return email;
	}

	/**
	 *  getUserNickname
	 *  account에 맞는 Nickname을 반환
	 */
	public String getUserNickname(long accountId) {
		String nickname = null;
		for (UserInfo userInfo : mUserInfoList) {
			if (userInfo.accountId == accountId) {
				nickname = userInfo.nickName;
				break;
			}
		}
		return nickname;
	}

	public void signout() {
		_initMyUserInfo();
		mGroupList.clear();
		mUserInfoList.clear();
		if (ConnectionManager.getInstance(null) != null) {
			ConnectionManager.getInstance(null).clearRegisteredDevices();
		}

		mPreferenceMgr.setLatestCheckedNotificationIndex(DeviceType.SYSTEM, 0, 0);
		mPreferenceMgr.setLatestSavedNotificationIndex(DeviceType.SYSTEM, 0, 0, 0);

		// Terms of conditions, Terms of use
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_TERMS_OF_USE_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_TERMS_OF_USE_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.YK_TERMS_OF_USE_KR, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_KR, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_GLOBAL) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_GLOBAL, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_GLOBAL, null);
		}

		// Privacy
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PRIVACY_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PRIVACY_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.YK_PRIVACY_KR, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.PRIVACY_KR, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GLOBAL) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GLOBAL, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GLOBAL, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GDPR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GDPR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.PRIVACY_GDPR, null);
		}

		// Collect info
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_COLLECT_INFO_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_COLLECT_INFO_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.YK_COLLECT_INFO_KR, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.COLLECT_INFO_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.COLLECT_INFO_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.COLLECT_INFO_KR, null);
		}

		// 3rd party
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PROVIDE_3RD_PARTY_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PROVIDE_3RD_PARTY_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.YK_PROVIDE_3RD_PARTY_KR, null);
		}
		if (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PROVIDE_3RD_PARTY_KR) != -1) {
			mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PROVIDE_3RD_PARTY_KR, -1);
			mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), Policy.PROVIDE_3RD_PARTY_KR, null);
		}
	}

	public void leave() {
		signout();
	}

	public void _initMyUserInfo() {
		mPreferenceMgr.setSigninState(SignInState.STEP_SIGN_IN);
		mPreferenceMgr.setSigninToken(null);
		mPreferenceMgr.setSigninEmail(null);
		mPreferenceMgr.setAccountId(-1);
		mPreferenceMgr.setShortId(null);
	}
}