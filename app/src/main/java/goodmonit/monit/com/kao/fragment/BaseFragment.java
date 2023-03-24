package goodmonit.monit.com.kao.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import goodmonit.monit.com.kao.activity.MainActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.DatabaseManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.services.ConnectionManager;

public class BaseFragment extends Fragment {
    private static final String TAG = Configuration.BASE_TAG + "BaseFragment";
	private static final boolean DBG = Configuration.DBG;

	public static final String START_FRAGMENT = "start_fragment";

	//public static final int MSG_SAVE_SCREEN_ANALYTICS = 1;

	// 1 depth Fragment
	public static final int ID_SENSOR_STATUS		= 0;
	public static final int ID_MESSAGE 				= 1;
	public static final int ID_DIARY	 			= 2;
	public static final int ID_USER	 				= 3;
	public static final int ID_SETTING	 			= 4;

	public Context mContext;
	public Activity mMainActivity;

	public ServerQueryManager mServerQueryMgr;
	public PreferenceManager mPreferenceMgr;
	public ConnectionManager mConnectionMgr;
	public DatabaseManager mDatabaseMgr;
	public ScreenInfo mScreenInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
        return null;
    }

	@Override
	public void onResume() {
		super.onResume();
		/*
		if (mScreenInfo != null) {
			mScreenInfo.inType = 2;
			mScreenInfo.inUtcTimeStampMs = System.currentTimeMillis();
		}
		*/
	}

    @Override
	public void onPause() {
    	super.onPause();
    	/*
		if (mScreenInfo != null) {
			mScreenInfo.outType = 2;
			mScreenInfo.outUtcTimeStampMs = System.currentTimeMillis();
			mFragmentHandler.obtainMessage(MSG_SAVE_SCREEN_ANALYTICS, mScreenInfo);
		}
		*/
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//if (DBG) Log.i(TAG, "onDestroy");
	}

	public void setToolbarTitle(String title) {
		if (mMainActivity != null) {
			((MainActivity)mMainActivity).setToolbarTitle(title);
		}
	}

	private Handler mFragmentHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				/*
				case MSG_SAVE_SCREEN_ANALYTICS:
					ScreenInfo info = (ScreenInfo) msg.obj;
					DatabaseManager.getInstance(getContext()).insertDB(info);
					break;
				*/
			}
		}
	};
}
