package goodmonit.monit.com.kao.signup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.SignupActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class SignupContent2 extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "Signup2";
	private static final boolean DBG = Configuration.DBG;

	private TextView tvCheckAuthEmailDescription, tvNotAuthenticated;
	private Button btnSendAuthEmail;
	private ImageView ivLogo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_signup2, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mScreenInfo = new ScreenInfo(202);

        _initView(view);

        return view;
    }

	private void _initView(View v) {
		ivLogo = (ImageView)v.findViewById(R.id.iv_activity_signup_check_auth_email_logo);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			ivLogo.setImageResource(R.drawable.logo_kchuggies);
		} else {
			ivLogo.setImageResource(R.drawable.ic_logo_green);
		}

		tvCheckAuthEmailDescription = (TextView)v.findViewById(R.id.tv_activity_signup_check_auth_email_description);
		tvNotAuthenticated = (TextView)v.findViewById(R.id.tv_activity_signup_not_checked_auth_email);
		btnSendAuthEmail = (Button)v.findViewById(R.id.btn_activity_signup_send_email);
		SpannableString content = new SpannableString(getString(R.string.signup_send_auth_email));
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		btnSendAuthEmail.setText(content);
		btnSendAuthEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((SignupActivity)mMainActivity).sendAuthenticationEmail();
			}
		});
		tvCheckAuthEmailDescription.setText(getString(R.string.signup_check_auth_email_description, mPreferenceMgr.getSigninEmail()));
	}

	public void showWarningMessage(String errcode) {
		mHandler.obtainMessage(SignupActivity.MSG_SHOW_WARNING_MESSAGE, errcode).sendToTarget();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case SignupActivity.MSG_SHOW_WARNING_MESSAGE:
					String errcode = (String)msg.obj;
					if (InternetErrorCode.ERR_NOT_AUTHENTICATED.equals(errcode)) {
						//Animation animation = new AlphaAnimation(0, 100);
						//animation.setDuration(1000);
						tvNotAuthenticated.setVisibility(View.VISIBLE);

						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								Animation animation = new AlphaAnimation(100, 0);
								animation.setDuration(1000);
								tvNotAuthenticated.setVisibility(View.GONE);
								tvNotAuthenticated.setAnimation(animation);
							}
						}, 1000);
					}
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
