package goodmonit.monit.com.kao.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceInfo;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class GuideDirectConnectionActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Direct";
    private static final boolean DBG = Configuration.DBG;

    private long mConnectedSensorDeviceId;
    private DeviceDiaperSensor mMonitSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_direct_connection);

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);

        mConnectedSensorDeviceId = getIntent().getLongExtra("targetDeviceId", -1);
        _setToolBar();
        _initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
        mConnectionMgr = ConnectionManager.getInstance(mHandler);

        mMonitSensor = ConnectionManager.getDeviceDiaperSensor(mConnectedSensorDeviceId);
        if (mMonitSensor != null) {
            if (DBG) Log.d(TAG, "targetDevice : [" + mMonitSensor.deviceId + "] " + mMonitSensor.name);
        } else {
            if (DBG) Log.e(TAG, "targetDevice NULL : " + mConnectedSensorDeviceId);
            finish();
            return;
        }

        if (mMonitSensor.getConnectionState() == DeviceConnectionState.BLE_CONNECTED) {
            finish();
        }
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle.setVisibility(View.GONE);
        btnToolbarRight.setVisibility(View.GONE);

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }

    private void _initView() {
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ConnectionManager.MSG_BLE_CONNECTION_STATE_CHANGE:
                    final int state = msg.arg1;
                    final DeviceInfo deviceInfo = (DeviceInfo)msg.obj;

                    if (deviceInfo == null) break;
                    if (DBG) Log.d(TAG, "MSG_BLE_CONNECTION_STATE_CHANGE : [" + deviceInfo.deviceId + "] " + state);

                    if (deviceInfo.type == DeviceType.DIAPER_SENSOR && deviceInfo.deviceId == mConnectedSensorDeviceId && state == DeviceConnectionState.BLE_CONNECTED) {
                        showToast(getString(R.string.toast_sensor_is_connected_directly));
                        finish();
                    }
                    break;

            }
        }
    };

}
