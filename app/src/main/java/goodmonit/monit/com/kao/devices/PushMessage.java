package goodmonit.monit.com.kao.devices;

public class PushMessage {
    public int notiType;
    public int deviceType;
    public long deviceId;
    public String extra;
    public long timeMs;
    public boolean ignoreLatestComparison;

    public PushMessage() {}
}