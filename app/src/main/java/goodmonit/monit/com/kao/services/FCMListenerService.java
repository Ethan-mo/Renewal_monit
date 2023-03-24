package goodmonit.monit.com.kao.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.PushManager;

public class FCMListenerService extends FirebaseMessagingService {
    private static final String TAG = Configuration.BASE_TAG + "FCMListener";
    private static final boolean DBG = Configuration.DBG;

    @Override
    public void onNewToken(String token) {
        if (DBG) Log.d(TAG, "onNewToken : " + token);
        PreferenceManager.getInstance(this).setPushToken(token);
        PreferenceManager.getInstance(this).setNeedToUpdatePushToken(PushManager.PUSH_FCM); // FCM Updated
    }

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        Map<String, String> data = msg.getData();
        String message = data.get("message");
        if (DBG) Log.d(TAG, "FCM Received : " + message);
        PushManager.getInstance(this).onMessageReceived(message);
    }
}