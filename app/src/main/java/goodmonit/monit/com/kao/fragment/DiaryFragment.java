package goodmonit.monit.com.kao.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class DiaryFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "DiaryFragment";
	private static final boolean DBG = Configuration.DBG;

    private static final int MSG_UPDATE_VIEW	= 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_diary, container, false);

		mPreferenceMgr = PreferenceManager.getInstance(getContext());

        _initView(view);
        return view;
    }

	private void _initView(View v) {

    }

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
		mPreferenceMgr.setLatestForegroundFragmentId(ID_DIARY);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
