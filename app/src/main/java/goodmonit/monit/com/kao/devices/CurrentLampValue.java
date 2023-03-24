package goodmonit.monit.com.kao.devices;
public class CurrentLampValue {
	public boolean power;
	public int bright;
	public int color;
	public float temperature;
	public float humidity;
	public float vocFromSensor;
	public int batteryPowerFromSensor;

	public String toString() {
		return temperature + ", " +
				humidity + ", " +
				vocFromSensor + ", " +
				batteryPowerFromSensor + ", " +
				power + ", " +
				bright + ", " +
				color;
	}
}