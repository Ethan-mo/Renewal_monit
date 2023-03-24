package goodmonit.monit.com.kao.devices;

import android.content.Context;

import goodmonit.monit.com.kao.R;

public class EnvironmentCheckManager {
	public static final int LOW 	= -1;
	public static final int NORMAL 	= 0;
	public static final int HIGH 	= 1;

	private float mTemperature;
	private float mHumidity;
	private float mVoc;

	private int mVocStatus;
	private int mTemperatureStatus;
	private int mHumidityStatus;

	private float mMinTemperature;
	private float mMaxTemperature;
	private float mMinHumidity;
	private float mMaxHumidity;

	private int mScore;
	private Context mContext;

	public EnvironmentCheckManager(Context context) {
		mContext = context;
		mScore = 0;
		mVocStatus = NORMAL;
		mTemperatureStatus = NORMAL;
		mHumidityStatus = NORMAL;
	}

	public void setTemperatureThreshold(float minTemperature, float maxTemperature) {
		/*
		if (PreferenceManager.getInstance(mContext).getTemperatureScale().equals(mContext.getString(R.string.unit_temperature_fahrenheit))) {
			mMinTemperature = UnitConvertUtil.getFahrenheitFromCelsius(minTemperature);
			mMaxTemperature = UnitConvertUtil.getFahrenheitFromCelsius(maxTemperature);
		} else {
		*/
			mMinTemperature = minTemperature;
			mMaxTemperature = maxTemperature;
		//}
		setTemperatureThres(minTemperature, maxTemperature);

		setTemperature(mTemperature);
		setHumidity(mHumidity);
		setVoc(mVoc);
		updateScore();
	}

	public void setHumidityThreshold(float minHumidity, float maxHumidity) {
		mMinHumidity = minHumidity;
		mMaxHumidity = maxHumidity;
		setHumidityThres(minHumidity, maxHumidity);
		setTemperature(mTemperature);
		setHumidity(mHumidity);
		setVoc(mVoc);
		updateScore();
	}

	public void setTemperature(float temperature) {
		mTemperature = temperature;
		if (mMaxTemperature < temperature) {
			mTemperatureStatus = HIGH;
		} else if (mMinTemperature > temperature) {
			mTemperatureStatus = LOW;
		} else {
			mTemperatureStatus = NORMAL;
		}
	}

	public void setHumidity(float humidity) {
        mHumidity = humidity;
		if (mMaxHumidity < humidity) {
			mHumidityStatus = HIGH;
		} else if (mMinHumidity > humidity) {
			mHumidityStatus = LOW;
		} else {
			mHumidityStatus = NORMAL;
		}
	}

	public void setVoc(float voc) {
        mVoc = voc;
		if (isVocWarning(voc)) {
			mVocStatus = HIGH;
		} else {
			mVocStatus = NORMAL;
		}
	}

	public void updateScore() {
		mScore = updateScore(mTemperature, mHumidity, mVoc);
	}

	public int getScore() {
		updateScore();
		return mScore;
	}

	public String getScoreDescription() {
		switch(getScoreStage(mScore)) {
			case 0:
				return mContext.getString(R.string.device_environment_voc_good);
			case 1:
				return mContext.getString(R.string.device_environment_voc_normal);
			case 2:
				return mContext.getString(R.string.device_environment_voc_not_good);
			case 3:
				return mContext.getString(R.string.device_environment_voc_very_bad);
			default:
				return mContext.getString(R.string.device_environment_voc_very_bad);
		}
	}

	public String getVocString(float voc) {
		switch(getVocStage(voc)) {
			case -1:
				return mContext.getString(R.string.device_environment_voc_abnormal);
			case 0:
				return mContext.getString(R.string.device_environment_voc_good);
			case 1:
				return mContext.getString(R.string.device_environment_voc_normal);
			case 2:
				return mContext.getString(R.string.device_environment_voc_not_good);
			case 3:
				return mContext.getString(R.string.device_environment_voc_very_bad);
			default:
				return mContext.getString(R.string.device_environment_voc_very_bad);
		}
	}

	public int calculateScore(float temperature, float humidity, float voc) {
		return updateScore(temperature, humidity, voc);
	}

	public int getVocStatus() {
		return mVocStatus;
	}

	public int getTemperatureStatus() {
		return mTemperatureStatus;
	}

	public int getHumidityStatus() {
		return mHumidityStatus;
	}

	private native int getScoreStage(float score);
	private native int getVocStage(float voc);
	private native void setTemperatureThres(float min, float max);
	private native void setHumidityThres(float min, float max);
	private native boolean isVocWarning(float voc);
	private native int updateScore(float temperature, float humidity, float voc);
	static {
		System.loadLibrary("native-lib");
	}
}