package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.CurrentSensorLog;
import goodmonit.monit.com.kao.devices.CurrentSensorValue;


public class DebugManager {
	private static final String TAG = Configuration.BASE_TAG + "DebugManager";
	private static final boolean DBG = Configuration.DBG;

    private Context mContext;
    private File mFile, mFilePath;
    private String mLatestSavedFileName;
    private FileOutputStream mFileOutputStream;
    private PrintWriter mPrinterWriter;
	private static long mAccountId;

	private static DebugManager mDebugManager = null;
    public static DebugManager getInstance(Context context) {
    	if (mDebugManager == null) {
    		mDebugManager = new DebugManager(context);
    	}
    	mAccountId = PreferenceManager.getInstance(context).getAccountId();
    	return mDebugManager;
    }
	
	public DebugManager(Context context) {
		mContext = context;
		//mFilePath = new File(Configuration.LOGGING_FILE_DIRECTORY);
		//if (!mFilePath.exists()) {
		//	mFilePath.mkdir();
		//}
	}

	private long timeStampMs = 0;
	public boolean saveEssentialHistoryFile(CurrentSensorValue value) {
		/*
		long now = System.currentTimeMillis();
		if (now - timeStampMs> 1000 * 3) { // 차이가 3초 이상 나는 경우 now 갱신
			timeStampMs = now;
		} else {
			timeStampMs += 1000;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(DateFormat.format("HH:mm:ss", timeStampMs));
		sb.append(",");
		sb.append("1"); // 성별
		sb.append(",");
		sb.append("0"); // 개월수
		sb.append(",");
		sb.append("0"); // UserInput
		sb.append(",");
		sb.append(value.temperature);
		sb.append(",");
		sb.append(value.humidity);
		sb.append(",");
		sb.append(value.voc);
		sb.append(",");
		sb.append(value.touch);
		sb.append(",");
		sb.append(value.acceleration);
		sb.append("\n");

		return _saveInternalFile("history", sb.toString());
		*/
		return true;
	}

	public boolean saveEssentialHistoryFile(CurrentSensorLog log) {
		/*
		StringBuilder sb = new StringBuilder();
		sb.append(log.toString());
		sb.append("\n");
		return _saveInternalFile(log.type + "_" + log.id + "_" + mAccountId, sb.toString());
		*/
		return true;
	}

	public void saveDebuggingLog(String data) {
		/*
		long now = System.currentTimeMillis();
		String line = DateFormat.format("HH:mm:ss", now) + "." + (now % 1000) + ", " + data + "\n";
		_saveInternalFile("debug", line);
		*/
	}

	/*
	public void saveChatMessageInternalFile() {
		if (DBG) Log.d(TAG, "saveChatMessageInternalFile");
		FileOutputStream fos = null;
		try {
			final String fileName = "chat_" + DateFormat.format("yyMMdd", System.currentTimeMillis()).toString() + "_.txt";
			fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);

			for (NotificationMessage msg : DatabaseManager.getInstance(mContext).getNotificationMessages()) {
				String chatMessage = DateTimeUtil.getDebuggingDateTimeString(msg.timeMs) + "," + msg.deviceId + ",";
				chatMessage += mContext.getString(NotificationType.getStringResource(msg.notiType)) + ",";
				chatMessage += msg.extra.replace("\n", " ");
				chatMessage += "\n";
				fos.write(chatMessage.getBytes());
			}
			fos.close();
		} catch (IOException e) {
			if (DBG) Log.e(TAG, "createFile failed");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
	*/

	private boolean _saveInternalFile(final String name, final String data) {
		FileOutputStream fos = null;
		try {
			final String fileName = DateFormat.format("yyMMdd", System.currentTimeMillis()).toString() + "_" + name + "_.csv";
			//final String fileName = name + "_" + DateFormat.format("HHmm", System.currentTimeMillis()).toString() + "_.txt";
			fos = mContext.openFileOutput(fileName, Context.MODE_APPEND);
			fos.write(data.getBytes());
			fos.close();
			return true;
		} catch (IOException e) {
			if (DBG) Log.e(TAG, "saveFile failed");
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
				}
			}
			return false;
		}
	}

/*
 * Deprecated Function

	// Deprecated
	public void saveChatMessageExternalFile() {
		if (DBG) Log.d(TAG, "saveChatMessageFile");
		try {
			File file = new File(mFilePath, "chat_" + DateFormat.format("yyMMdd", System.currentTimeMillis()).toString() + ".txt");
			FileOutputStream fos = new FileOutputStream(file, false);
			PrintWriter pw = new PrintWriter(fos);
			AlarmMessageManager chatManager = AlarmMessageManager.getInstance(mContext);
			for (MonitMessage cm : chatManager.getAlarmMessages()) {
				String chatMessage = DateTimeUtil.getDebuggingDateTimeString(cm.getTimeMs()) + "," +
						cm.getSrcName() + "," +
						cm.getMessage().replace("\n", " ") + "," +
						cm.getAnswer();
				pw.println(chatMessage);
			}
			pw.flush();
			pw.close();
			fos.close();
		} catch (IOException e) {
			if (DBG) Log.e(TAG, "createFile failed");
		}
	}

	// Deprecated
	public boolean saveAllHistoryFile(CurrentSensorValue value) {
		long now = System.currentTimeMillis();
		if (now - timeStampMs> 1000 * 3) { // 차이가 3초 이상 나는 경우 now 갱신
			timeStampMs = now;
		} else {
			timeStampMs += 1000;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(DateFormat.format("HH:mm:ss", timeStampMs));
		sb.append(",");
		sb.append(value.temperature);
		sb.append(",");
		sb.append(value.humidity);
		sb.append(",");
		sb.append(value.voc);
		sb.append(",");
		sb.append(value.battery);
		sb.append(",");
		sb.append(value.touch);
		sb.append(",");
		sb.append(value.co2);
		sb.append(",");
		sb.append(value.raw);
		sb.append(",");
		sb.append(value.comp);
		sb.append(",");
		sb.append(value.ethanol);
		sb.append(",");
		sb.append(value.pressure);
		sb.append(",");
		sb.append(value.x_axis);
		sb.append(",");
		sb.append(value.y_axis);
		sb.append(",");
		sb.append(value.z_axis);

		return _saveInternalFile("all_data", sb.toString());
	}

	// Deprecated
	public String loadFile(final String filename) {
		String data = "";
		try {
			FileInputStream fis = mContext.openFileInput(Global.SAVE_FILE_DIRECTORY + filename);
			byte[] buffer = new byte[fis.available()];
		    fis.read(buffer);
		    data = new String(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	// Deprecated
	private boolean _saveExternalFile(final String name, final String data) {
		try {
			String fileName = name + "_" + DateFormat.format("yyMMdd", System.currentTimeMillis()).toString() + ".txt";
			File file = new File(mFilePath, fileName);
			FileOutputStream fos = new FileOutputStream(file, true);
			PrintWriter pw = new PrintWriter(fos);
			pw.println(data);
			pw.flush();
			pw.close();
			fos.close();
			return true;
		} catch (IOException e) {
			if (DBG) Log.e(TAG, "saveFile failed");
			return false;
		}
	}

	// Deprecated

	public void saveRawToInternalData() {
		String data = null;
		InputStream inputStream;
		ByteArrayOutputStream byteArrayOutputStream;

		mContext.deleteFile("debug_180110_.txt");
		mContext.deleteFile("debug_180111_.txt");
		mContext.deleteFile("debug_180112_.txt");
		mContext.deleteFile("debug_180113_.txt");
		mContext.deleteFile("debug_180114_.txt");

		int i;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			inputStream = mContext.getResources().openRawResource(R.raw.test1);
			FileOutputStream fos = mContext.openFileOutput("debug_180110_.txt", Context.MODE_APPEND);
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}

			data = new String(byteArrayOutputStream.toByteArray(),"MS949");
			inputStream.close();
			fos.write(data.getBytes());
			fos.close();

			byteArrayOutputStream = new ByteArrayOutputStream();
			inputStream = mContext.getResources().openRawResource(R.raw.test2);
			fos = mContext.openFileOutput("debug_180111_.txt", Context.MODE_APPEND);
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}

			data = new String(byteArrayOutputStream.toByteArray(),"MS949");
			inputStream.close();
			fos.write(data.getBytes());
			fos.close();

			byteArrayOutputStream = new ByteArrayOutputStream();
			inputStream = mContext.getResources().openRawResource(R.raw.test3);
			fos = mContext.openFileOutput("debug_180112_.txt", Context.MODE_APPEND);
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}

			data = new String(byteArrayOutputStream.toByteArray(),"MS949");
			inputStream.close();
			fos.write(data.getBytes());
			fos.close();

			byteArrayOutputStream = new ByteArrayOutputStream();
			inputStream = mContext.getResources().openRawResource(R.raw.test4);
			fos = mContext.openFileOutput("debug_180113_.txt", Context.MODE_APPEND);
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}

			data = new String(byteArrayOutputStream.toByteArray(),"MS949");
			inputStream.close();
			fos.write(data.getBytes());
			fos.close();

			byteArrayOutputStream = new ByteArrayOutputStream();
			inputStream = mContext.getResources().openRawResource(R.raw.test5);
			fos = mContext.openFileOutput("debug_180114_.txt", Context.MODE_APPEND);
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}

			data = new String(byteArrayOutputStream.toByteArray(),"MS949");
			inputStream.close();
			fos.write(data.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

*/

}