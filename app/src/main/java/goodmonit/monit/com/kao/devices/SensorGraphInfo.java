package goodmonit.monit.com.kao.devices;

import java.util.ArrayList;

public class SensorGraphInfo {
    public long deviceId;
    public int cntDiaperChanged;
    public int cntPeeDetected;
    public int cntPooDetected;
    public int cntFartDetected;
    public int cntSoiledDetected;
    public int cntMovementDetected;
    public ArrayList<Integer> movementValues;
    public ArrayList<Integer> sleepingValues;
    public int convertedSleepTimeSec;
    public int sleepTimeSec;
    public int deepSleepTimeSec;
    public long timeSec;
    public int sumMovementLevel;
    public int cntMovementLevel;
    public float avgMovementLevel;
}