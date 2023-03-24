package goodmonit.monit.com.kao.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.PushManager;

/**
 * Created by Jake on 2017-09-25.
 */
public class PushyReceiver extends BroadcastReceiver {
    private static final String TAG = Configuration.BASE_TAG + "PushyReceiver";
    private static final boolean DBG = Configuration.DBG;

    @Override
    public void onReceive(Context context, Intent intent) {
        //boolean success = intent.getBooleanExtra("success", false);
        //int id = intent.getIntExtra("id", 0);
        String message = intent.getStringExtra("message");
        if (DBG) Log.d(TAG, "Pushy Received : " + message);
        PushManager.getInstance(context).onMessageReceived(message);
    }
}