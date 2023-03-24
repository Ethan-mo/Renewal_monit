package goodmonit.monit.com.kao.devices;
public class CurrentSensorValue {
	public int hour;		 	//
	public int minute;		 	//
	public int second;		 	//
	public int section;			//
	public float temperature; 	//
	public float humidity;  	//
	public float voc;			// 0~500
	public int battery;			// 0~200
	public float touch;			// 0~16000
	public int co2;				// 0~5500
	public int raw;				// 0~5500
	public float comp;			// 0~5500 
	public float ethanol;		//
	public long pressure;		//
	public float x_axis;		//
	public float y_axis;		//
	public float z_axis;		//
	public float acceleration;	//
	public long currentTimeSec;	//
	public long lastTimeSec;	//
	public int status_movement;
	public int status_diaper;
	public int status_operation;
	public int diaper_point;
	public int movement_point;
	public int count_pee_detected;
	public int count_poo_detected;
	public int count_abnormal_detected;
	public float translatedHumidity;
	
	public CurrentSensorValue() {
		x_axis = 0;
		y_axis = 0;
		z_axis = 1;
		status_movement = DeviceStatus.MOVEMENT_NO_MOVEMENT;
		status_diaper = DeviceStatus.DETECT_NONE;
		status_operation = DeviceStatus.OPERATION_SENSING;
		count_pee_detected = 0;
		count_poo_detected = 0;
		count_abnormal_detected = 0;
	}

	public void setData(String[] arrData) throws NumberFormatException {
		switch(arrData.length) {
			case 15:
				this.z_axis		= Float.parseFloat(arrData[14]);
			case 14:
				this.y_axis		= Float.parseFloat(arrData[13]);
			case 13:
				this.x_axis		= Float.parseFloat(arrData[12]);
				this.acceleration = (float)Math.sqrt(x_axis * x_axis + y_axis * y_axis + z_axis * z_axis);
			case 12:
				this.pressure	= Long.parseLong(arrData[11]);
			case 11:
				this.ethanol	= Float.parseFloat(arrData[10]);
			case 10:
				this.comp		= Float.parseFloat(arrData[9]);
			case 9:
				this.raw		= Integer.parseInt(arrData[8]);
			case 8:
				this.co2		= Integer.parseInt(arrData[7]);
			case 7:
				this.touch 		= Float.parseFloat(arrData[6]);
			case 6:
				this.battery	= Integer.parseInt(arrData[5]);
			case 5:
				this.voc 		= Float.parseFloat(arrData[4]);
			case 4:
				this.humidity 	= Float.parseFloat(arrData[3]);
			case 3:
				this.temperature= Float.parseFloat(arrData[2]);
			case 2:
				this.section	= Integer.parseInt(arrData[1]);
			case 1:
				String[] time = arrData[0].split(":");
				this.hour = Integer.parseInt(time[0]);
				this.minute = Integer.parseInt(time[1]);
				this.second = Integer.parseInt(time[2]);
				this.lastTimeSec = this.currentTimeSec;
				this.currentTimeSec = (long)(this.hour * 3600 + this.minute * 60 + this.second);
				break;
		}
	}

	public String toString() {
		return "t:" + temperature + ", " +
				"h:" + humidity + "(" + translatedHumidity + "), " +
				"v:" + voc + ", " +
				"t:" + touch + ", " +
				/*
				co2 + ", " +
				raw + ", " +
				comp + ", " +
				ethanol + ", " +
				pressure + ", " +
				*/
				"a:" + acceleration + ", " +
				"b:" + battery + ", " +
				"[diaper:" + diaper_point + "(" + status_diaper + "), movement:" + movement_point + "(" + status_movement + "), operation:" + status_operation + ", Detection:" + count_pee_detected + " / " + count_poo_detected + " / " + count_abnormal_detected + "]";
	}
}