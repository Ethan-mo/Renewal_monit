package goodmonit.monit.com.kao.signup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.SignupActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class SignupContent4 extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "Signup4";
	private static final boolean DBG = Configuration.DBG;

	private TextView tvMembershipCode, tvCompletedTitle, tvCompletedDescription;
	private ImageView ivCompletedLogo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_signup4, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mScreenInfo = new ScreenInfo(204);

        _initView(view);

        return view;
    }

	private void _initView(View v) {
    	ivCompletedLogo = (ImageView)v.findViewById(R.id.iv_activity_signup_completed_logo);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			ivCompletedLogo.setImageResource(R.drawable.logo_kchuggies);
		} else {
			ivCompletedLogo.setImageResource(R.drawable.ic_logo_green);
		}

		tvCompletedTitle = (TextView)v.findViewById(R.id.tv_activity_signup_completed_title);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			//tvCompletedTitle.setText(getString(R.string.account_signup_welcome_title_kc));
			tvCompletedTitle.setText("");
		} else {
			tvCompletedTitle.setText(getString(R.string.account_signup_welcome_title));
		}

		tvCompletedDescription = (TextView)v.findViewById(R.id.tv_activity_signup_completed_description);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			tvCompletedDescription.setText(getString(R.string.account_signup_welcome_description_kc));
		} else {
			tvCompletedDescription.setText(getString(R.string.account_signup_welcome_description));
		}

		tvMembershipCode = (TextView)v.findViewById(R.id.tv_activity_signup_membership_code);
		tvMembershipCode.setText(mPreferenceMgr.getShortId());
	}

	public void showWarningMessage(String errcode) {
		mHandler.obtainMessage(SignupActivity.MSG_SHOW_WARNING_MESSAGE, errcode).sendToTarget();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case SignupActivity.MSG_SHOW_WARNING_MESSAGE:
					break;
			}
		}
	};

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
