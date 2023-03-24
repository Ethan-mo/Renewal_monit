package goodmonit.monit.com.kao.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class BootCompleteReceiver extends BroadcastReceiver {
	private static final String TAG = Configuration.BASE_TAG + "BootReceiver";
	private static final boolean DBG = Configuration.DBG;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			if (ConnectionManager.getInstance(null) == null) {
				try {
					if (DBG) Log.e(TAG, "start service3");
					context.startService(new Intent(context, ConnectionManager.class));
				} catch(Exception e) {

				}
			}
		}
	}
}