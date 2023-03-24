package goodmonit.monit.com.kao.dfu;

import android.content.Intent;
import android.os.Bundle;

import goodmonit.monit.com.kao.activity.BaseActivity;
import goodmonit.monit.com.kao.constants.Configuration;

public class DfuNotificationActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "FirmwareUpdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot()) {
            // Start the app before finishing
            final Intent intent = new Intent(this, FirmwareUpdateActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(getIntent().getExtras()); // copy all extras
            startActivity(intent);
        }

        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish();
    }
}
