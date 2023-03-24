package goodmonit.monit.com.kao.devices;

class DiaperStatusInfo {
	private final int MAX_DETECTION_TYPE = 6;

	public int[] detectionCount;
	public long[] latestDetectionUtcTimeSec;
	public boolean[] detectionChanged;
	public boolean[] ignoreInitialSetting; // 최초 센서 연결시 저장된 정보로 인해 detected 여부를 판단할 수 없을 때, 서버로 올리기 위한 flag

	public DiaperStatusInfo() {
		detectionCount = new int[MAX_DETECTION_TYPE];
		latestDetectionUtcTimeSec = new long[MAX_DETECTION_TYPE];
		detectionChanged = new boolean[MAX_DETECTION_TYPE];
		ignoreInitialSetting = new boolean[MAX_DETECTION_TYPE];

		for (int i = 0; i < MAX_DETECTION_TYPE; i++) {
			detectionCount[i] = 0;
			latestDetectionUtcTimeSec[i] = 0;
			detectionChanged[i] = false;
			ignoreInitialSetting[i] = true;
		}
	}

	public void initDiaperStatusInfo() {
		for (int i = 0; i < MAX_DETECTION_TYPE; i++) {
			detectionCount[i] = 0;
			latestDetectionUtcTimeSec[i] = 0;
			detectionChanged[i] = false;
			ignoreInitialSetting[i] = true;
		}
	}

	public void setDiaperStatusInfo(int[] diaperStatusInfo) {
		if (diaperStatusInfo == null) return;

		for (int i = 0; i < MAX_DETECTION_TYPE; i++) {
			detectionChanged[i] = false;

			if (diaperStatusInfo[i] > 0 && detectionCount[i] != diaperStatusInfo[i]) {
				detectionChanged[i] = true;
				detectionCount[i] = diaperStatusInfo[i];
			}
		}
	}

	public void setDiaperStatusDetectionTime(int detectionType, long utcTimeSec) {
		switch(detectionType) {
			case DeviceStatus.DETECT_PEE:
				latestDetectionUtcTimeSec[0] = utcTimeSec;
				break;
			case DeviceStatus.DETECT_POO:
				latestDetectionUtcTimeSec[1] = utcTimeSec;
				break;
			case DeviceStatus.DETECT_ABNORMAL:
				latestDetectionUtcTimeSec[2] = utcTimeSec;
				break;
			case DeviceStatus.DETECT_FART:
				latestDetectionUtcTimeSec[3] = utcTimeSec;
				break;
			case DeviceStatus.DETECT_DIAPER_DETACHED:
				latestDetectionUtcTimeSec[4] = utcTimeSec;
				break;
			case DeviceStatus.DETECT_DIAPER_ATTACHED:
				latestDetectionUtcTimeSec[5] = utcTimeSec;
				break;
			default:
				break;
		}
	}

	public int getPeeCount() {
		return detectionCount[0];
	}

	public int getPooCount() {
		return detectionCount[1];
	}

	public int getAbnormalCount() {
		return detectionCount[2];
	}

	public int getFartCount() {
		return detectionCount[3];
	}

	public int getDetachmentCount() {
		return detectionCount[4];
	}

	public int getAttachmentCount() {
		return detectionCount[5];
	}

	public boolean isPeeDetected() {
		return detectionChanged[0];
	}

	public boolean isPooDetected() {
		return detectionChanged[1];
	}

	public boolean isAbnormalDetected() {
		return detectionChanged[2];
	}

	public boolean isFartDetected() {
		return detectionChanged[3];
	}

	public boolean isDetachmentDetected() {
		return detectionChanged[4];
	}

	public boolean isAttachmentDetected() {
		return detectionChanged[5];
	}

	public int getDetectionArrayIndex(int detectionType) {
		switch(detectionType) {
			case DeviceStatus.DETECT_PEE:
				return 0;
			case DeviceStatus.DETECT_POO:
				return 1;
			case DeviceStatus.DETECT_ABNORMAL:
				return 2;
			case DeviceStatus.DETECT_FART:
				return 3;
			case DeviceStatus.DETECT_DIAPER_DETACHED:
				return 4;
			case DeviceStatus.DETECT_DIAPER_ATTACHED:
				return 5;
			default:
				return 0;
		}
	}
}