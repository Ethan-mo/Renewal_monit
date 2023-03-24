package goodmonit.monit.com.kao.connection.Package;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;

public class ConnectionMonitPackageHowToAttachSensor extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "PkgHow";
	private static final boolean DBG = Configuration.DBG;

	private Button btnClose;
	private ViewFlipper vfHowToAttach;
	private ImageView ivHowToAttachImage1, ivHowToAttachImage2;
	private TextView tvHowToAttachDescription1, tvHowToAttachDescription2;
	private ImageView ivHowToAttachDot1, ivHowToAttachDot2;
	private int mViewStep = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_monit_package_how_to_attach_sensor, container, false);
        mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mScreenInfo = new ScreenInfo(603);

        _initView(view);

        return view;
    }

	private void _initView(View v) {
    	btnClose = (Button)v.findViewById(R.id.btn_connection_monit_package_how_to_attach_close);
    	btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ConnectionActivity)mContext).checkDeviceFirmwareVersion();
			}
		});

		ivHowToAttachImage1 = (ImageView)v.findViewById(R.id.iv_connection_monit_package_how_to_attach1);
		ivHowToAttachImage2 = (ImageView)v.findViewById(R.id.iv_connection_monit_package_how_to_attach2);

		ivHowToAttachDot1 = (ImageView)v.findViewById(R.id.iv_connection_monit_package_how_to_attach_dot1);
		ivHowToAttachDot2 = (ImageView)v.findViewById(R.id.iv_connection_monit_package_how_to_attach_dot2);

		tvHowToAttachDescription1 = (TextView)v.findViewById(R.id.tv_connection_monit_package_how_to_attach_description1);
		tvHowToAttachDescription2 = (TextView)v.findViewById(R.id.tv_connection_monit_package_how_to_attach_description2);

    	vfHowToAttach = (ViewFlipper) v.findViewById(R.id.vf_connection_monit_package_how_to_attach);
		vfHowToAttach.setAutoStart(false);
		vfHowToAttach.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mViewStep == 1) {
					mViewStep = 2;
					vfHowToAttach.showNext();
				} else {
					mViewStep = 1;
					vfHowToAttach.showPrevious();
				}
				_updateView();
			}
		});

		_updateView();
	}

	private void _updateView() {
		if (DBG) Log.d(TAG, "updateView: " + mViewStep);
		if (mViewStep == 1) {
			ivHowToAttachDot1.setSelected(true);
			ivHowToAttachDot2.setSelected(false);
			btnClose.setEnabled(false);
		} else {
			ivHowToAttachDot1.setSelected(false);
			ivHowToAttachDot2.setSelected(true);
			btnClose.setEnabled(true);
		}
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
		((ConnectionActivity)mMainActivity).updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
