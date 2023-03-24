package goodmonit.monit.com.kao.message;

import goodmonit.monit.com.kao.util.DateTimeUtil;

public class NotificationMessageEdit {
	public long serverNotiId;
	public int editType;
	public String editExtra;
	public long editTimeMs;

	public NotificationMessageEdit(long serverNotiId, int editType, String editExtra, long editTimeMs) {
		this.serverNotiId = serverNotiId;
		this.editType = editType;
		this.editExtra = editExtra;
		this.editTimeMs = editTimeMs;
	}

	public String toString() {
		return this.serverNotiId + " / " + this.editType + " / " + this.editExtra + " / " + DateTimeUtil.getDebuggingDateTimeString(this.editTimeMs) + "(" + this.editTimeMs + ")";
	}
}
