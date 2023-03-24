package goodmonit.monit.com.kao.UserInfo;

import java.util.ArrayList;

import goodmonit.monit.com.kao.devices.DeviceInfo;

public class Group {
	private GroupInfo mGroupInfo;
	private ArrayList<UserInfo> mMemberList;
	private ArrayList<DeviceInfo> mDeviceList;
	private int mLeaderIdx = 0;

	public Group(long cloudId, String name) {
		mGroupInfo = new GroupInfo(cloudId, name);
		mMemberList = new ArrayList<UserInfo>();
		mDeviceList = new ArrayList<DeviceInfo>();
	}

	public Group(GroupInfo groupInfo) {
		mGroupInfo = groupInfo;
		mMemberList = new ArrayList<UserInfo>();
		mDeviceList = new ArrayList<DeviceInfo>();
	}

	public long getId() {
		return mGroupInfo.cloudId;
	}

	public String getName() {
		return mGroupInfo.name;
	}
	
	public void setName(String name) {
		mGroupInfo.name = name;
	}

	public void addMember(UserInfo member) {
		if (member.accountId == mGroupInfo.cloudId) {
			mMemberList.add(0, member); // Leader
		} else {
			mMemberList.add(member);
		}
	}

	public void removeMember(UserInfo member) {
		mMemberList.remove(member);
	}

    public void removeMember(int idx) {
        mMemberList.remove(idx);
    }

	public UserInfo getLeaderInfo() {
		return mMemberList.get(0);
	}

	public UserInfo getUserInfo(int idx) {
		if (idx >= mMemberList.size() || idx < 0) {
			return mMemberList.get(0);
		}
		return mMemberList.get(idx);
	}

	public UserInfo getUserInfo(long accountId) {
		UserInfo info = null;
		for (UserInfo userInfo : mMemberList) {
			if (userInfo.accountId == accountId) {
				info = userInfo;
				break;
			}
		}

		return info;
	}

	public ArrayList<UserInfo> getMemberList() {
		return mMemberList;
	}

	public int getMemberCount() {
		return mMemberList.size();
	}

	public GroupInfo getGroupInfo() {
		return mGroupInfo;
	}

	public void addDevice(DeviceInfo deviceInfo) {
		mDeviceList.add(deviceInfo);
	}

	public void removeDevice(DeviceInfo deviceInfo) {
		mDeviceList.remove(deviceInfo);
	}

	public void removeDevice(int idx) {
		mDeviceList.remove(idx);
	}

	public ArrayList<DeviceInfo> getDeviceList() {
		return mDeviceList;
	}

	public int getDeviceCount() {
		return mDeviceList.size();
	}
}