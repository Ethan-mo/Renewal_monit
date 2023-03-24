package goodmonit.monit.com.kao.connection.DiaperSensor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.widget.GuideViewPager;

public class ConnectionMonitCompleted extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "MonitCompleted";
	private static final boolean DBG = Configuration.DBG;

	private static final int MSG_REFRESH_VIEW = 1;

	private Button btnConnectOtherDevice;
	private GuideViewPager vpHowToAttach;
	private ImageView ivHowToAttach;
	private int mViewStep = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_monit_completed, container, false);
        mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(603);

        _initView(view);

		//mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
        return view;
    }

	private void _initView(View v) {
		btnConnectOtherDevice = (Button)v.findViewById(R.id.btn_connection_monit_sensor_connected_connect_other_device);
		btnConnectOtherDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((ConnectionActivity)mMainActivity).showFragment(ConnectionActivity.STEP_SELECT_DEVICE);
			}
		});
		//ivHowToAttach = (ImageView)v.findViewById(R.id.iv_connection_monit_sensor_connected_how_to_attach);
		vpHowToAttach = (GuideViewPager)v.findViewById(R.id.vp_connection_monit_sensor_connected_how_to_attach);
		vpHowToAttach.addViewPage(R.drawable.ani_monit_attach1);
		vpHowToAttach.addViewPage(R.drawable.ani_monit_attach2);
		vpHowToAttach.addViewPage(R.drawable.ani_monit_attach3);
		vpHowToAttach.setCurrentViewPageIdx(0);
		vpHowToAttach.setCurrentIndicator(0);
    }

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_VIEW:
					switch(mViewStep % 4)
					{
						case 0:
							ivHowToAttach.setImageResource(R.drawable.ani_monit_attach1);
							sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 1000);
							break;
						case 1:
							ivHowToAttach.setImageResource(R.drawable.ani_monit_attach2);
							sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 1000);
							break;
						case 2:
							ivHowToAttach.setImageResource(R.drawable.ani_monit_attach3);
							sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 1000);
							break;
						case 3:
							ivHowToAttach.setImageResource(0);
							sendEmptyMessageDelayed(MSG_REFRESH_VIEW, 500);
					}
					mViewStep++;
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
		((ConnectionActivity)mMainActivity).updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
		mHandler.removeMessages(MSG_REFRESH_VIEW);
	}
}
