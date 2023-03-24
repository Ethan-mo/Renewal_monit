package goodmonit.monit.com.kao.devices;

/**
 * Created by Jake on 2018-06-15.
 */

public class CurrentSensorLog {
    public CurrentSensorLog() {

    }
    public long timeSec;
    public int type;
    public long id;
    public int status;
    public int battery;
    public int sex;
    public int months;
    public String data;

    public String toString() {
        return timeSec + "," + type + "," + id + "," + status + "," + battery + "," + sex + "," + months + "," + data;
    }
}
