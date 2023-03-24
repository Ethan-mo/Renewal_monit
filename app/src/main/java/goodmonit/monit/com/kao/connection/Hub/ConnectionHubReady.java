package goodmonit.monit.com.kao.connection.Hub;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class ConnectionHubReady extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "ReadyHub";
	private static final boolean DBG = Configuration.DBG;

	private static final int STEP1_POWER_ON			= 1;
	private static final int STEP2_CHECK_LED		= 2;
	private int mStep;

	private static final int MSG_CHANGE_ANIMATION	= 1;
	private static final int MSG_REFRESH_PROGRESS 	= 2;
	private static final long CHANGE_ANIMATION_INTERVAL_MS = 1000;
	private int mAnimationIndex;

	private Button btnConnect, btnHelp;
	private ImageView ivAnimationStep1, ivAnimationStep2;
	private TextView tvDetail;
	private ViewSwitcher vsAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_ready, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mConnectionMgr = ConnectionManager.getInstance();
		mScreenInfo = new ScreenInfo(701);
        _initView(view);
		setView(STEP1_POWER_ON);
        return view;
    }

	private void _initView(View v) {
		btnConnect = (Button)v.findViewById(R.id.btn_connection_hub_start_connect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mStep == STEP1_POWER_ON) {
					vsAnimation.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_from_right));
					vsAnimation.showNext();
					setView(STEP2_CHECK_LED);
				} else if (mStep == STEP2_CHECK_LED) {
					((ConnectionActivity) mMainActivity).showFragment(ConnectionActivity.STEP_HUB_PUT_SENSOR_TO_HUB);
				}
			}
		});

		btnHelp = (Button)v.findViewById(R.id.btn_connection_hub_ready_help);
		btnHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).showHelpContents(11, 19);
			}
		});

		ivAnimationStep1 = (ImageView)v.findViewById(R.id.iv_connection_hub_ready_animation_step1);
		ivAnimationStep2 = (ImageView)v.findViewById(R.id.iv_connection_hub_ready_animation_step2);
		vsAnimation = (ViewSwitcher)v.findViewById(R.id.vs_connection_hub_ready_animation);
		tvDetail = (TextView)v.findViewById(R.id.tv_connection_hub_ready_detail);
    }

	private void setView(int step) {
		mStep = step;
		mAnimationIndex = 0;
		switch (step) {
			case STEP1_POWER_ON:
				tvDetail.setText(getString(R.string.connection_hub_ready_detail_step1));
				btnConnect.setText(getString(R.string.btn_next));
				break;
			case STEP2_CHECK_LED:
				tvDetail.setText(getString(R.string.connection_hub_ready_detail_step2));
				btnConnect.setText(getString(R.string.btn_next));
				break;
		}
	}

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_PROGRESS:
					this.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS, 1000);

					break;
				case MSG_CHANGE_ANIMATION:
					if (mStep == STEP1_POWER_ON) {
						switch(mAnimationIndex % 3) {
							case 0:
								ivAnimationStep1.setImageResource(R.drawable.ani_hub_ready1);
								this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS);
								break;
							case 1:
								ivAnimationStep1.setImageResource(R.drawable.ani_hub_ready2);
								this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS);
								break;
							case 2:
								ivAnimationStep1.setImageResource(R.color.colorTransparent);
								this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS / 2);
								break;
						}
					} else if (mStep == STEP2_CHECK_LED) {
						switch(mAnimationIndex % 2) {
							case 0:
								ivAnimationStep2.setImageResource(R.drawable.ani_hub_ready5);
								break;
							case 1:
								ivAnimationStep2.setImageResource(R.drawable.ani_hub_ready6);
								break;
						}
						this.sendEmptyMessageDelayed(MSG_CHANGE_ANIMATION, CHANGE_ANIMATION_INTERVAL_MS / 2);
					}
					mAnimationIndex++;
					break;
			}
		}
	};

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
		mHandler.removeMessages(MSG_REFRESH_PROGRESS);
		mHandler.removeMessages(MSG_CHANGE_ANIMATION);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();
		mHandler.sendEmptyMessage(MSG_REFRESH_PROGRESS);
		mAnimationIndex = 0;
		mHandler.sendEmptyMessage(MSG_CHANGE_ANIMATION);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
