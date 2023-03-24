package goodmonit.monit.com.kao.devices;

import android.content.Context;
import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class DiaperCheckManager {
	private static final String TAG = Configuration.BASE_TAG + "DiaperCheckMgr";
	private static final boolean DBG = Configuration.DBG;

	public String strTime;

	/** Variable **/
	private double lastHumid;
	private double currentHumid;
	private double humidDiff;
	private double lastVoc;
	private double currentVoc;
	private double vocDiff;

	/** Number of sensing data **/
	public int totalHumidityNum;
	public int totalVocNum;

	private static final double VOC_INCREASED_DEMO_MAX_MINUTES = 0.5; // 30초
	private static final double VOC_INCREASED_DEMO_DIFF_THRESHOLD = 30;
	private static final double VOC_INCREASED_DEMO_DETECT_COUNT = 10;

	private long demoCurrentTimeSec;
	private long demoLastPeeDetectedTimeSec;
	private long demoLastPooDetectedTimeSec;

	private PreferenceManager mPrefMgr;

	public DiaperCheckManager(Context c) {
		if (DBG) Log.i(TAG, "Created");
		mPrefMgr = PreferenceManager.getInstance(c);
	}

	public void setDemoValue(double humid, double voc) {
		humid = (int)(humid * 100) / 100.0;
		if (currentHumid == 0) {
			currentHumid = humid;
			lastHumid = humid;
		} else {
			humidDiff = (int)((currentHumid - lastHumid) * 100) / 100.0;
			lastHumid = currentHumid;
			currentHumid = humid;
		}

		voc = (int)(voc * 100) / 100.0;
		if (currentVoc == 0) {
			currentVoc = voc;
			lastVoc = voc;
		} else {
			vocDiff = (int)((currentVoc - lastVoc) * 100) / 100.0;
			lastVoc = currentVoc;
			currentVoc = voc;
		}

		if (DBG) Log.d(TAG, "setDemoValue : " + humid + " / " + voc);
	}

	public int checkDemo() {
		demoCurrentTimeSec = System.currentTimeMillis() / 1000;
		if (DBG) Log.d(TAG, "checkDemo: humid: " + humidDiff + "/" + mPrefMgr.getDemoInfoThreshold() + "(" + totalHumidityNum + "/" + mPrefMgr.getDemoInfoCount() + ") voc: " + vocDiff + "(" + totalVocNum + ")");
		//if (DBG) Log.d(TAG, "diaper check : " + humidDiff + "(" + currentHumid + "-" + lastHumid +  ")/" + HUMID_INCREASED_DEMO_DIFF_THRESHOLD + " => " + totalNum);
		if ((humidDiff > mPrefMgr.getDemoInfoThreshold())) {
			totalHumidityNum++;

			if (totalHumidityNum >= mPrefMgr.getDemoInfoCount()) {
				if ((demoLastPeeDetectedTimeSec == 0) || ((demoCurrentTimeSec - demoLastPeeDetectedTimeSec) > mPrefMgr.getDemoInfoIgnoreDelaySec())) {
					demoLastPeeDetectedTimeSec = demoCurrentTimeSec;
					return DeviceStatus.DETECT_PEE;

				} else {
					if (DBG) Log.e(TAG, "Not passed : " + (demoCurrentTimeSec - demoLastPeeDetectedTimeSec) + "/" + mPrefMgr.getDemoInfoIgnoreDelaySec() + " => " + demoCurrentTimeSec + " / " + demoLastPeeDetectedTimeSec);
					totalHumidityNum = 0;
				}
			} else {
				if (DBG) Log.e(TAG, "Under : " + totalHumidityNum + " / " + mPrefMgr.getDemoInfoCount());
			}
		} else if (humidDiff < 0) {
			totalHumidityNum = 0;
		}

		if ((vocDiff > VOC_INCREASED_DEMO_DIFF_THRESHOLD)) {
			totalVocNum++;

			if (totalVocNum >= VOC_INCREASED_DEMO_DETECT_COUNT) {
				if (demoLastPooDetectedTimeSec == 0 || demoCurrentTimeSec - demoLastPooDetectedTimeSec > VOC_INCREASED_DEMO_MAX_MINUTES * 60) {
					demoLastPooDetectedTimeSec = demoCurrentTimeSec;
					//return DeviceStatus.DETECT_POO;

				} else {
					//if (DBG) Log.e(TAG, "Not passed : " + (demoCurrentTimeSec - demoLastPeeDetectedTimeSec) + " / " + (HUMID_INCREASED_DEMO_MAX_MINUTES * 60));
					totalVocNum = 0;
				}
			} else {
				//if (DBG) Log.e(TAG, "Under : " + totalNum + " / " + HUMID_INCREASED_DEMO_DETECT_COUNT);
			}
		} else if (vocDiff < 0) {
			totalVocNum = 0;
		}

		return DeviceStatus.DETECT_NONE;
	}

	public void initDemoValue() {
		currentHumid = 0;
		lastHumid = 0;
		demoLastPeeDetectedTimeSec = 0;
		totalHumidityNum = 0;
		totalVocNum = 0;
	}
}