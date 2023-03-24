package goodmonit.monit.com.kao.util;

import android.content.Context;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class UnitConvertUtil {
	/*
	 * Weight(kg) : kg <-> lb <-> oz
	 */
	public static float getKgFromLb(float weight) {
		weight *= (float)0.453592;
		return (int)(weight * 10) / (float)10.0;
	}

	public static float getLbFromKg(float weight) {
		weight *= (float)2.204623;
		return (int)(weight * 10) / (float)10.0;
	}

	public static float getOzFromLb(float weight) {
		weight *= (float)0.0625;
		return (int)(weight * 10) / (float)10.0;
	}

	/*
	 * Length(cm) : cm <-> ft <-> inch
	 */
	public static float getFtFromCm(float length) {
		length *= (float)0.032808;
		return (int)(length * 10) / (float)10.0;
	}

	public static float getCmFromFt(float length) {
		length *= (float)30.48;
		return (int)(length * 10) / (float)10.0;
	}

	public static float getInFromFt(float length) {
		length *= (float)12;
		return (int)(length * 10) / (float)10.0;
	}

	/*
	 * Amount(ml) : ml <-> oz <-> g
	 */
	public static float getMlFromOz(float amount) {
		amount *= (float)29.57353;
		return (int)(amount * 10) / (float)10.0;
	}

	public static float getOzFromMl(float amount) {
		amount *= (float)0.033814;
		return (int)(amount * 10) / (float)10.0;
	}

	public static float getGramFromOz(float amount) {
		amount *= (float)0.035274;
		return (int)(amount * 10) / (float)10.0;
	}
	public static float getOzFromGram(float amount) {
		amount *= (float)28.349523;
		return (int)(amount * 10) / (float)10.0;
	}

	/*
	 * Temperature(℃) : ℃ <-> ℉
	 * Any temperature to Celsius temperature
	 */
	public static float getFahrenheitFromCelsius(float temperature) {
		float converted = (int)(temperature * 1.8 * 2 + 0.5) / (float)2.0 + 32;
		return (int)(converted * 10) / (float)10.0;
	}

	public static float getCelsiusFromFahrenheit(float temperature) {
		float converted = (temperature - 32) * (float)10 / (float)18;
		return (int)(converted * 10) / (float)10.0;
	}

	public static float getConvertedTemperature(Context c, float celsius) {
        if (PreferenceManager.getInstance(c).getTemperatureScale().equals(c.getString(R.string.unit_temperature_celsius))) {
            return celsius;
        } else {
            return getFahrenheitFromCelsius(celsius);
        }
    }
}
