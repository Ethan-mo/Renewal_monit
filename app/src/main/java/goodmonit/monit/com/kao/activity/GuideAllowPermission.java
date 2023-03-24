package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class GuideAllowPermission extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Guide";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_CHECK_PERMISSION_STEP                  = 1;

    private static final int REQUEST_CODE_ALLOW_POWER_OPTIMIZATION  	= 1;
    private static final int REQUEST_CODE_ALLOW_BLE_SCAN              	= 2;
    private static final int REQUEST_CODE_ALLOW_FINE_LOCATION           = 3;

    private static final int STEP_ALLOW_BLE_PERMISSION          = 1;
    private static final int STEP_ALLOW_FINE_LOCATION_PERMISSION    = 2;
    private static final int STEP_ALLOW_BATTERY_OPTIMIZATION    = 3;
    private static final int STEP_COMPLETED                     = 4;

    private int mCurrentStep = 1;
    private Button btnConfirm;
    private TextView tvPermissionTitle, tvPermissionDescription, tvTitle1, tvDescription1, tvTitle2, tvDescription2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_guide_allow_permission);
        _setToolBar();

        mContext = this;

        _initView();
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getString(R.string.dialog_permission_title));

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        btnToolbarRight.setVisibility(View.GONE);

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
        //mConnectionMgr = ConnectionManager.getInstance(null);
    }

    @Override
    public void onBackPressed() {
        if (DBG) Log.i(TAG, "onBackPressed");
        mCurrentStep = STEP_ALLOW_BLE_PERMISSION;
        mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
    }

    private void _initView() {
        tvPermissionTitle = (TextView)findViewById(R.id.tv_guide_allow_permission_title);
        tvPermissionDescription = (TextView)findViewById(R.id.tv_guide_allow_permission_description);
        tvTitle1 = (TextView) findViewById(R.id.tv_guide_allow_permission_description1_title);
        tvDescription1 = (TextView) findViewById(R.id.tv_guide_allow_permission_description1_description);
        tvTitle2 = (TextView) findViewById(R.id.tv_guide_allow_permission_description2_title);
        tvDescription2 = (TextView) findViewById(R.id.tv_guide_allow_permission_description2_description);
        if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
            tvPermissionDescription.setText(getString(R.string.guide_permission_description_kc));
            tvDescription1.setText(getString(R.string.guide_permission_description1_kc));
            tvDescription2.setText(getString(R.string.guide_permission_description2_kc));
        } else {
            tvPermissionDescription.setText(getString(R.string.guide_permission_description));
            tvDescription1.setText(getString(R.string.guide_permission_description1));
            tvDescription2.setText(getString(R.string.guide_permission_description2));
        }

        btnConfirm = (Button)findViewById(R.id.btn_guide_allow_permission_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentStep = STEP_ALLOW_BLE_PERMISSION;
                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (DBG) Log.d(TAG, "onRequestPermissionsResult : " + requestCode);
        switch (requestCode) {
            case REQUEST_CODE_ALLOW_BLE_SCAN:
                /* Add dialog to explain the detail reason when user select no button
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCurrentStep = STEP_ALLOW_BATTERY_OPTIMIZATION;
                } else {
                    mCurrentStep = STEP_ALLOW_BATTERY_OPTIMIZATION;
                }
                */
                mCurrentStep = STEP_ALLOW_FINE_LOCATION_PERMISSION;
                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                break;
            case REQUEST_CODE_ALLOW_FINE_LOCATION:
                mCurrentStep = STEP_ALLOW_BATTERY_OPTIMIZATION;
                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) Log.d(TAG, "onActivityResult : " + requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_ALLOW_POWER_OPTIMIZATION:
                /*
                if (resultCode == Activity.RESULT_OK) {
                    mCurrentStep = STEP_COMPLETED;
                } else {
                    mCurrentStep = STEP_COMPLETED;
                }
                */
                mCurrentStep = STEP_COMPLETED;
                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_PERMISSION_STEP:
                    if (DBG) Log.d(TAG, "step: " + mCurrentStep);

                    if (mCurrentStep == STEP_ALLOW_BLE_PERMISSION) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            // Quick permission check
                            int permissionCheck = mContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                            if (permissionCheck != 0) {
                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ALLOW_BLE_SCAN); //Any number
                            } else {
                                mCurrentStep = STEP_ALLOW_FINE_LOCATION_PERMISSION;
                                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                            }
                        } else {
                            mCurrentStep = STEP_COMPLETED;
                            mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                        }

                    } else if (mCurrentStep == STEP_ALLOW_FINE_LOCATION_PERMISSION) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            // Quick permission check
                            int permissionCheck = mContext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
                            if (permissionCheck != 0) {
                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ALLOW_FINE_LOCATION); //Any number
                            } else {
                                mCurrentStep = STEP_ALLOW_BATTERY_OPTIMIZATION;
                                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                            }
                        } else {
                            mCurrentStep = STEP_COMPLETED;
                            mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                        }
                    } else if (mCurrentStep == STEP_ALLOW_BATTERY_OPTIMIZATION) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                            boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(mContext.getPackageName());
                            if (DBG) Log.d(TAG, "reg white list : " + isWhiteListing);
                            if (isWhiteListing == false) {
                                Intent powerOptimizationIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                powerOptimizationIntent.setData(Uri.parse("package:" + mContext.getPackageName()));
                                startActivityForResult(powerOptimizationIntent, REQUEST_CODE_ALLOW_POWER_OPTIMIZATION);
                            } else {
                                mCurrentStep = STEP_COMPLETED;
                                mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                            }
                        } else {
                            mCurrentStep = STEP_COMPLETED;
                            mHandler.sendEmptyMessage(MSG_CHECK_PERMISSION_STEP);
                        }
                    } else if (mCurrentStep == STEP_COMPLETED) {
                        finish();
                    }
                    break;
            }
        }
    };
}
