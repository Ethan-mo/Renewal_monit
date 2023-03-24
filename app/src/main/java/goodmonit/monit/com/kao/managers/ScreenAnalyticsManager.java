package goodmonit.monit.com.kao.managers;

import android.content.Context;

import java.util.ArrayList;

import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class ScreenAnalyticsManager {
	private Context mContext;

	public ScreenAnalyticsManager(Context context) {
		mContext = context;
	}

	public void sendScreenAnalytics() {
		ArrayList<ScreenInfo> screenInfoList = DatabaseManager.getInstance(mContext).getScreenInfo();
		if (screenInfoList != null && screenInfoList.size() > 0) {
			for (ScreenInfo info : screenInfoList) {
				final ScreenInfo finalInfo = info;
				ServerQueryManager.getInstance(mContext).setScreenAnalytics(
						finalInfo.screenType,
						finalInfo.inType,
						finalInfo.outType,
						DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(finalInfo.inUtcTimeStampMs),
						DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(finalInfo.outUtcTimeStampMs),
						new ServerManager.ServerResponseListener() {
							@Override
							public void onReceive(int responseCode, String errCode, String data) {
								if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
									DatabaseManager.getInstance(mContext).deleteDB(finalInfo);
								}
							}
						});
			}
		}
	}
}