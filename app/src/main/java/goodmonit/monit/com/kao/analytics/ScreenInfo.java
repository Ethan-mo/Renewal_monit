package goodmonit.monit.com.kao.analytics;

public class ScreenInfo {
    public int id;
    public int screenType;
    public int inType;
    public int outType;
    public long inUtcTimeStampMs;
    public long outUtcTimeStampMs;

    public ScreenInfo(int screenType) {
        this.id = -1;
        this.screenType = screenType;
        this.inType = 2;
        this.outType = 2;
        this.inUtcTimeStampMs = 0;
        this.outUtcTimeStampMs = 0;
    }

    public ScreenInfo(int screenType, long inUtcTimeStampMs, long outUtcTimeStampMs) {
        this.id = -1;
        this.screenType = screenType;
        this.inType = 2;
        this.outType = 2;
        this.inUtcTimeStampMs = inUtcTimeStampMs;
        this.outUtcTimeStampMs = outUtcTimeStampMs;
    }

    public ScreenInfo(int id, int screenType, int inType, int outType, long inUtcTimeStampMs, long outUtcTimeStampMs) {
        this.id = id;
        this.screenType = screenType;
        this.inType = inType;
        this.outType = outType;
        this.inUtcTimeStampMs = inUtcTimeStampMs;
        this.outUtcTimeStampMs = outUtcTimeStampMs;
    }
}
