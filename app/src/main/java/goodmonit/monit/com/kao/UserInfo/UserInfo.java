package goodmonit.monit.com.kao.UserInfo;

public class UserInfo {
	public long accountId;
	public long cloudId;
	public String nickName;
	public String shortId;
	public String email;
	public int familyType;
	public boolean groupLeader = false;

	public UserInfo() {
    }

	public UserInfo(long _accountId, long _cloudId, String _email, String _nickName, String _shortId, int _familyType) {
		accountId = _accountId;
		email = _email;
		cloudId = _cloudId;
		nickName = _nickName;
		shortId = _shortId;
		familyType = _familyType;
	}

	public String toString() {
		return accountId + " / "+ email + " / " + cloudId + " / " + nickName + " / " + shortId + " / " + familyType;
	}
}