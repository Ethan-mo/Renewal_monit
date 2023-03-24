package goodmonit.monit.com.kao.devices;

import android.content.Context;

import goodmonit.monit.com.kao.managers.DatabaseManager;

public class MovementGraphInfo {
    public long deviceId;
    public long startUtcTimeMs;
    public String value;
    public int count;
    public long created;

    public long insertDB(Context c) {
        return DatabaseManager.getInstance(c).insertDB(this);
    }

    public String toString() {
        return this.deviceId + " / " + this.startUtcTimeMs + " / " + this.value + " / " + this.count;
    }
}