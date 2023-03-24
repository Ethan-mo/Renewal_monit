package goodmonit.monit.com.kao.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class NuguActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Nugu";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_REFRESH_VIEW = 1;
    public static final String BROADCAST_MESSAGE_AUTH_COMPLETED = "monit.com.monit.oauth.completed";

    private LinearLayout lctnAuthInfo;
    private Button btnLaunchNuguApp, btnGetAuthKey;
    private TextView tvMembershipCode, tvValidAuthTime, tvAuthKey;
    private Button btnCopyMembershipCode, btnCopyAuthKey;
    private ScrollView svContainer;

    private String mAuthKey;
    private long mExpiredUtcTimeMs;
    private boolean isAuthCompleted;

    private BroadcastReceiver mAuthCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DBG) Log.d(TAG, "onReceive: " + action);
            if (BROADCAST_MESSAGE_AUTH_COMPLETED.equals(action)) {
                isAuthCompleted = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nugu);
        _setToolBar();

        mContext = this;
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mScreenInfo = new ScreenInfo(404);
        isAuthCompleted = false;

        _initView();

        tvMembershipCode.setText(mPreferenceMgr.getShortId());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_MESSAGE_AUTH_COMPLETED);
        if (DBG) Log.d(TAG, "register Receiver: " + mAuthCompletedReceiver);
        registerReceiver(mAuthCompletedReceiver, intentFilter);
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_white);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        btnToolbarRight.setVisibility(View.GONE);
        tvToolbarTitle = ((TextView)findViewById(R.id.tv_toolbar_title));
        tvToolbarTitle.setText(getString(R.string.nugu_title));
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        svContainer = (ScrollView)findViewById(R.id.sv_nugu_container);
        lctnAuthInfo = (LinearLayout)findViewById(R.id.lctn_nugu_get_authentication_info);
        tvAuthKey = (TextView)findViewById(R.id.tv_nugu_authentication_key);
        tvValidAuthTime = (TextView)findViewById(R.id.tv_nugu_authentication_key_valid_time);
        tvMembershipCode = (TextView)findViewById(R.id.tv_nugu_membership_code);

        btnLaunchNuguApp = (Button)findViewById(R.id.btn_nugu_launch_app);
        btnLaunchNuguApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageInfo pi = null;
                try {
                    pi = mContext.getPackageManager().getPackageInfo("com.skt.aladdin", 0);
                } catch (PackageManager.NameNotFoundException e) { // 마켓으로 이동
                    if (DBG) Log.e(TAG, "Nugu not found");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.skt.aladdin")));
                }
                if (pi != null) { // 앱 실행
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.skt.aladdin");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        btnGetAuthKey = (Button)findViewById(R.id.btn_nugu_get_authentication_key);
        btnGetAuthKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthKey == null || mExpiredUtcTimeMs == 0 || System.currentTimeMillis() >= mExpiredUtcTimeMs) {
                    // getKeyFromServer;
                    mServerQueryMgr.getAuthToken(new ServerManager.ServerResponseListener() {
                        @Override
                        public void onReceive(int responseCode, String errCode, String data) {
                            if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                try {
                                    JSONObject jObject = new JSONObject(data);
                                    String token = jObject.optString(mServerQueryMgr.getParameter(122), null);
                                    if (token != null) {
                                        mPreferenceMgr.setOtpAuthToken(token);
                                        mAuthKey = token;
                                    }
                                    String expiredTime = jObject.optString(mServerQueryMgr.getParameter(15), null);
                                    if (expiredTime != null) {
                                        // Time이 UTC로 들어옴
                                        Date expiredDate = new SimpleDateFormat(mServerQueryMgr.getParameter(1)).parse(expiredTime);
                                        long expiredUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(expiredDate.getTime()); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
                                        mPreferenceMgr.setOtpAuthTokenExpiredUtcTimeStampMs(expiredUtcTimeMs);
                                        mExpiredUtcTimeMs = expiredUtcTimeMs;
                                        if (DBG) Log.d(TAG, "valid time(sec): " + ((expiredUtcTimeMs - System.currentTimeMillis()) / 1000));
                                    }
                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                        public void run() {
                                            lctnAuthInfo.setVisibility(View.VISIBLE);
                                            svContainer.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    svContainer.fullScroll(View.FOCUS_DOWN);
                                                }
                                            });
                                        }
                                    });
                                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 1000L);
                                } catch (JSONException e) {
                                    if (DBG) Log.e(TAG, e.toString());
                                } catch (ParseException e) {
                                    if (DBG) Log.e(TAG, e.toString());
                                }
                            }
                        }
                    });
                } else {
                    lctnAuthInfo.setVisibility(View.VISIBLE);
                    svContainer.post(new Runnable() {
                        @Override
                        public void run() {
                            svContainer.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 1000L);
                }
            }
        });

        btnCopyAuthKey = (Button)findViewById(R.id.btn_nugu_copy_authentication_key);
        btnCopyAuthKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyClipboard(mAuthKey);
            }
        });

        btnCopyMembershipCode = (Button)findViewById(R.id.btn_nugu_copy_membership_code);
        btnCopyMembershipCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyClipboard(mPreferenceMgr.getShortId());
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_VIEW:
                    removeMessages(MSG_REFRESH_VIEW);
                    sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 1000);
                    _refreshView();
                    break;
            }
        }
    };

    private void _refreshView() {
        long nowMs = System.currentTimeMillis();
        if (mExpiredUtcTimeMs >= nowMs) {
            int min = (int)((mExpiredUtcTimeMs - nowMs) / 1000 / 60);
            int sec = (int)((mExpiredUtcTimeMs - nowMs) / 1000 % 60);
            tvAuthKey.setText(mAuthKey);
            tvValidAuthTime.setText(String.format("%02d:%02d", min, sec));
        } else {
            lctnAuthInfo.setVisibility(View.GONE);
            mExpiredUtcTimeMs = 0;
            mAuthKey = null;
            mHandler.removeMessages(MSG_REFRESH_VIEW);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        long nowMs = System.currentTimeMillis();
        mAuthKey = mPreferenceMgr.getOtpAuthToken();
        mExpiredUtcTimeMs = mPreferenceMgr.getOtpAuthTokenExpiredUtcTimeStampMs();

        if (isAuthCompleted) {
            showToast(getString(R.string.toast_oauth_authentication_succeeded));
            lctnAuthInfo.setVisibility(View.GONE);
            mPreferenceMgr.setOtpAuthToken(null);
            mPreferenceMgr.setOtpAuthTokenExpiredUtcTimeStampMs(0);
            mExpiredUtcTimeMs = 0;
            mAuthKey = null;
        }

        if (mExpiredUtcTimeMs > nowMs && mAuthKey != null) {
            lctnAuthInfo.setVisibility(View.VISIBLE);
            svContainer.fullScroll(View.FOCUS_DOWN);
            mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
        } else {
            mPreferenceMgr.setOtpAuthToken(null);
            mPreferenceMgr.setOtpAuthTokenExpiredUtcTimeStampMs(0);
            mExpiredUtcTimeMs = 0;
            mAuthKey = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void copyClipboard(String content) {
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", content);
        clipboardManager.setPrimaryClip(clipData);
        showToast(getString(R.string.toast_copy_completed));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG, "onDestroy");
        if (DBG) Log.d(TAG, "unregister Receiver: " + mAuthCompletedReceiver);
        unregisterReceiver(mAuthCompletedReceiver);
    }

}