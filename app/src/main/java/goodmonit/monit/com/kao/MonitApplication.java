package goodmonit.monit.com.kao;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.ServerQueryManager;

/**
 * Created by Jake on 2017-06-29.
 */
public class MonitApplication extends Application {
    private static final String TAG = Configuration.BASE_TAG + "Application";
    private static final boolean DBG = Configuration.DBG;
    public static boolean isBackground = true;

    private static MonitApplication mInstance;
    /*
    public static MonitApplication getInstance(){
        return mInstance;
    }
    */
    @Override
    public void onCreate() {
        super.onCreate();
        if (DBG) Log.i(TAG, "onCreate");
        Fabric.with(this, new Crashlytics());
        mInstance = this;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    if (!isBackground) {
                        isBackground = true;
                        if (DBG) Log.i(TAG, "app status foreground screen off");
                    }
                    //if (Configuration.LOGGING) DebugManager.getInstance(mInstance).saveDebuggingLog("Scr Off");
                    if (DBG) Log.i(TAG, "app status foreground screen off");
                } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    //if (Configuration.LOGGING) DebugManager.getInstance(mInstance).saveDebuggingLog("Scr On");
                    if (DBG) Log.i(TAG, "app status foreground screen on");
                }
            }
        }, intentFilter);

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (DBG) Log.i(TAG, "app status onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (DBG) Log.i(TAG, "app status onActivityStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (DBG) Log.i(TAG, "app status onActivityResumed");
                if (isBackground) {
                    isBackground = false;
                    if (DBG) Log.i(TAG, "app status foreground");
                    //if (Configuration.LOGGING) DebugManager.getInstance(mInstance).saveDebuggingLog("fg on");
                    ServerQueryManager.getInstance(activity.getApplication().getApplicationContext()).setAccountActiveUser(null);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (DBG) Log.i(TAG, "app status onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (DBG) Log.i(TAG, "app status onActivityStopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {;}

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (DBG) Log.i(TAG, "app status onActivityDestroyed");
            }
        });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (DBG) Log.i(TAG, "app status onLowMemory");
        //if (Configuration.LOGGING) DebugManager.getInstance(mInstance).saveDebuggingLog("low mem off");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (DBG) Log.i(TAG, "app status onTerminate");
        //if (Configuration.LOGGING) DebugManager.getInstance(mInstance).saveDebuggingLog("term off");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (DBG) Log.i(TAG, "app status onTrimMemory: " + level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackground = true;
            if (DBG) Log.i(TAG, "app status background");
            //if (Configuration.LOGGING) DebugManager.getInstance(mInstance).saveDebuggingLog("bg off");
        }
    }
}