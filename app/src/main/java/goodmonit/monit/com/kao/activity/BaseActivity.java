package goodmonit.monit.com.kao.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = Configuration.BASE_TAG + "Base";

    public static final int MSG_SHOW_PROGRESS_BAR           = 1;
    public static final int MSG_SHOW_DIALOG                 = 2;
    public static final int MSG_SHOW_TOAST                  = 3;
    public static final int MSG_SHOW_IMAGE_TOAST            = 4;
    //public static final int MSG_SAVE_SCREEN_ANALYTICS       = 5;

    public static final int DIALOG_FINISH_APPLICATION       = 1;
    public static final int DIALOG_ERROR_COMMUNICATION      = 2;

    public TextView tvToolbarTitle;
    public ImageView ivToolbarNewRight, ivToolbarCenter;
    public Button btnToolbarRight, btnToolbarLeft, btnToolbarRight2;

    public Context mContext;
    public PreferenceManager mPreferenceMgr;
    public ServerQueryManager mServerQueryMgr;
    public ConnectionManager mConnectionMgr;
    public ScreenInfo mScreenInfo;
    public DatabaseManager mDatabaseMgr;

    public RelativeLayout rctnProgress;
    private SimpleDialog mDlgFinish, mDlgCommunicationError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        if (mScreenInfo != null) {
            mScreenInfo.inType = 2;
            mScreenInfo.inUtcTimeStampMs = System.currentTimeMillis();
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
        if (mScreenInfo != null) {
            mScreenInfo.outType = 2;
            mScreenInfo.outUtcTimeStampMs = System.currentTimeMillis();
            mBaseHandler.obtainMessage(MSG_SAVE_SCREEN_ANALYTICS, mScreenInfo).sendToTarget();
        }
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Handler mBaseHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_PROGRESS_BAR:
                    if (rctnProgress != null) {
                        if (msg.arg1 == View.VISIBLE) {
                            rctnProgress.setVisibility(View.VISIBLE);
                        } else {
                            rctnProgress.setVisibility(View.GONE);
                        }
                    }
                    break;
                case MSG_SHOW_DIALOG:
                    int type = msg.arg1;
                    String extra = (String)msg.obj;
                    _showDialog(type, extra);
                    break;
                case MSG_SHOW_TOAST:
                    String message = (String)msg.obj;
                    _showToast(message);
                    break;
                case MSG_SHOW_IMAGE_TOAST:
                    int imgRes = (int)msg.obj;
                    _showImageToast(imgRes);
                    break;
                /*
                case MSG_SAVE_SCREEN_ANALYTICS:
                    ScreenInfo info = (ScreenInfo)msg.obj;
                    DatabaseManager.getInstance(getApplicationContext()).insertDB(info);
                    break;
                */
            }
        }
    };

    @Override
    public void onBackPressed() {
        showFinishApplicationDialog();
    }

    public void showProgressBar(boolean show) {
        if (show) {
            mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_PROGRESS_BAR, View.VISIBLE, -1));
        } else {
            mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_PROGRESS_BAR, View.GONE, -1));
        }
    }

    public void showFinishApplicationDialog() {
        mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_DIALOG, DIALOG_FINISH_APPLICATION, -1));
    }

    public void showCommunicationErrorDialog(int responseCode) {
        mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_DIALOG, DIALOG_ERROR_COMMUNICATION, -1, responseCode + ""));
    }

    public void showCommunicationErrorDialog(String responseCode) {
        mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_DIALOG, DIALOG_ERROR_COMMUNICATION, -1, responseCode));
    }

    public void showToast(String message) {
        mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_TOAST, message));
    }

    private void _showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void showImageToast(int imgRes) {
        mBaseHandler.sendMessage(mBaseHandler.obtainMessage(MSG_SHOW_IMAGE_TOAST, imgRes));
    }

    public void _showImageToast(int imgRes) {
        Toast toast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        ImageView imageView = new ImageView(mContext);
        imageView.setMaxWidth((int)mContext.getResources().getDimension(R.dimen.image_toast_size));
        imageView.setMaxHeight((int)mContext.getResources().getDimension(R.dimen.image_toast_size));
        imageView.setBackgroundResource(imgRes);
        toast.setView(imageView);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void _showDialog(int type, String extra) {
        switch(type) {
            case DIALOG_FINISH_APPLICATION:
                if (mDlgFinish == null) {
                    mDlgFinish = new SimpleDialog(
                            mContext,
                            getString(R.string.dialog_contents_finish_applcation),
                            getString(R.string.btn_cancel),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    mDlgFinish.dismiss();
                                }
                            },
                            getString(R.string.btn_finish),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    mDlgFinish.dismiss();
                                    PreferenceManager.getInstance(mContext).setAppClosed(true);
                                    finish();
                                }
                            });
                }
                if (!mDlgFinish.isShowing()) {
                    try {
                        mDlgFinish.show();
                    } catch (Exception e) {

                    }
                }
                break;
            case DIALOG_ERROR_COMMUNICATION:
                String responseCode = extra;
                mDlgCommunicationError = new SimpleDialog(
                        mContext,
                        getString(R.string.dialog_contents_err_communication_with_server, responseCode),
                        getString(R.string.btn_ok),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mDlgCommunicationError.dismiss();
                            }
                        });
                try {
                    mDlgCommunicationError.show();
                } catch (Exception e) {

                }
                break;
        }
    }

    public void checkInvalidToken() {
        if (PreferenceManager.getInstance(mContext).getInvalidTokenReceived()) {
            PreferenceManager.getInstance(mContext).setInvalidTokenReceived(false);
            showToast(mContext.getString(R.string.toast_invalid_user_session));
            UserInfoManager.getInstance(mContext).signout();

            overridePendingTransition(0, 0);
            finish();
        }
    }
}
