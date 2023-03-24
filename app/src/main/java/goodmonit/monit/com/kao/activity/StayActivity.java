package goodmonit.monit.com.kao.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import goodmonit.monit.com.kao.constants.Configuration;

public class StayActivity extends Activity {
    private static final String TAG = Configuration.BASE_TAG + "Stay";
    private static final boolean DBG = Configuration.DBG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.d(TAG, "onCreate");
        overridePendingTransition(0, 0);
        finish();
    }
}
