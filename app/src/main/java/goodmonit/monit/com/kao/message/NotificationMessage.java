package goodmonit.monit.com.kao.message;

import android.content.Context;

import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class NotificationMessage {
	public long msgId;
	public int notiType;
	public int deviceType;
	public long deviceId;
	public String extra;
	public long timeMs;
	public long serverNotiId;

	public NotificationMessage(long msgId, int notiType, int deviceType, long deviceId, String extra, long timeMs, long serverNotiId) {
		this.msgId = msgId;
		this.notiType = notiType;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.extra = extra;
		this.timeMs = timeMs - timeMs % 1000;
		this.serverNotiId = serverNotiId;
	}

	public NotificationMessage(int notiType, int deviceType, long deviceId, String extra, long timeMs) {
		this.msgId = -1;
		this.notiType = notiType;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.extra = extra;
		this.timeMs = timeMs - timeMs % 1000;
		this.serverNotiId = -1;
	}

	public NotificationMessage(int notiType, int deviceType, long deviceId, String extra, long timeMs, long serverNotiId) {
		this.msgId = -1;
		this.notiType = notiType;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.extra = extra;
		this.timeMs = timeMs - timeMs % 1000;
		this.serverNotiId = serverNotiId;
	}

	public long insertDB(Context c) {
		return DatabaseManager.getInstance(c).insertDB(this);
	}

	public int updateDB(Context c) {
		return DatabaseManager.getInstance(c).updateDB(this);
	}

	public int deleteDB(Context c) {
		return DatabaseManager.getInstance(c).deleteDB(this);
	}

	public String toString() {
		return this.msgId + " / " + this.serverNotiId + " / " + this.notiType + " / " + this.deviceType + " / " + this.deviceId + " / " + this.extra + " / " + DateTimeUtil.getDebuggingDateTimeString(this.timeMs) + "(" + this.timeMs + ")";
	}
}
