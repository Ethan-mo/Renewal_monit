package goodmonit.monit.com.kao.connection.Hub;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.ConnectionActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.RangeSeekBar;
import goodmonit.monit.com.kao.widget.ValidationEditText;
import goodmonit.monit.com.kao.widget.ValidationRadio;
import goodmonit.monit.com.kao.widget.ValidationRange;
import goodmonit.monit.com.kao.widget.ValidationWidget;

public class ConnectionHubCompleted extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "HubCompleted";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	private static final int MIN_TEMPERATURE_CELSIUS = 10;
	private static final int MAX_TEMPERATURE_CELSIUS = 40;
	private static final int MIN_TEMPERATURE_FAHRENHEIT = 50;
	private static final int MAX_TEMPERATURE_FAHRENHEIT = 104;
	private static final int MIN_HUMIDITY = 10;
	private static final int MAX_HUMIDITY = 90;

	private LinearLayout lctnSetting;
	private ValidationEditText vetRoomName;
	private ValidationRadio vrTemperatureUnit;
	private ValidationManager mValidationMgr;

	private ValidationRange vrTemperatureRange, vrHumidityRange;

	private TextView tvTitle, tvDetail;
	private String mTitle, mDetail;
	private boolean showHubSetting = true;

	private DeviceAQMHub mHubDevice;

	private float mMaxTemperature, mMinTemperature, mMaxHumidity, mMinHumidity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_connection_hub_completed, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(704);
		mValidationMgr = new ValidationManager(inflater.getContext());

		if (mHubDevice == null) {
			mHubDevice = ((ConnectionActivity)getActivity()).getConnectedHubDevice();
			if (DBG) Log.i(TAG, "getConnectedHubDevice: " + mHubDevice.toString());
		}
		mMinTemperature = mHubDevice.getMinTemperature();
		mMaxTemperature = mHubDevice.getMaxTemperature();
		mMinHumidity = mHubDevice.getMinHumidity();
		mMaxHumidity = mHubDevice.getMaxHumidity();

        _initView(view);
        return view;
    }

	private void _initView(View v) {
		tvTitle = (TextView)v.findViewById(R.id.tv_connection_hub_connected_title);
		tvDetail = (TextView)v.findViewById(R.id.tv_connection_hub_connected_detail);

		lctnSetting = (LinearLayout)v.findViewById(R.id.lctn_connection_hub_connected_setting);
		vetRoomName = (ValidationEditText)v.findViewById(R.id.vet_input_hub_info_roomname);
		vetRoomName.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vetRoomName.setValid(mValidationMgr.isValidRoomname(vetRoomName.getText()));
			}
		});

		vrTemperatureUnit = (ValidationRadio)v.findViewById(R.id.vr_input_hub_info_temperature_unit);
		vrTemperatureUnit.setValidationUpdateListener(new ValidationWidget.ValidationListener() {
			@Override
			public void updateValidation() {
				vrTemperatureUnit.setValid(true);
				String setUnit = "C";
				if (vrTemperatureUnit.getSelectedRadioIndex() == 1) { // Celsius
					setUnit = "C";
					mPreferenceMgr.setTemperatureScale(getString(R.string.unit_temperature_celsius));
				} else {
					setUnit = "F";
					mPreferenceMgr.setTemperatureScale(getString(R.string.unit_temperature_fahrenheit));
				}
				mServerQueryMgr.setAppInfo(setUnit, new ServerManager.ServerResponseListener() {
					@Override
					public void onReceive(int responseCode, String errCode, String data) {
						if (InternetErrorCode.SUCCEEDED.equals(errCode)) {

						}
					}
				});
				_updateTemperatureScale();

				mMaxTemperature = mHubDevice.getMaxTemperature();
				mMinTemperature = mHubDevice.getMinTemperature();
				if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
					mMaxTemperature = (int)UnitConvertUtil.getFahrenheitFromCelsius(mMaxTemperature);
					mMinTemperature = (int)UnitConvertUtil.getFahrenheitFromCelsius(mMinTemperature);
				}
				vrTemperatureRange.initialize();
			}
		});
		vrTemperatureUnit.addOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_hideKeyboard();
			}
		});

		vrTemperatureRange = (ValidationRange)v.findViewById(R.id.vr_input_hub_info_temperature_range);
		_updateTemperatureScale();
		vrTemperatureRange.updateView();
		vrTemperatureRange.setOnValueChangedListener(new RangeSeekBar.OnRangeSeekBarListener() {
			@Override
			public void onValueChanged(int mMinValue, int mMaxValue, int leftPos, int rightPos) {
				vrTemperatureRange.setText(leftPos + mPreferenceMgr.getTemperatureScale() + " ~ " + rightPos + mPreferenceMgr.getTemperatureScale());
				vrTemperatureRange.setLeftThumbPos(leftPos);
				vrTemperatureRange.setLeftThumbTextView(leftPos + "");
				vrTemperatureRange.setRightThumbPos(rightPos);
				vrTemperatureRange.setRightThumbTextView(rightPos + "");
				mMinTemperature = leftPos;
				mMaxTemperature = rightPos;
			}
		});

		vrHumidityRange = (ValidationRange)v.findViewById(R.id.vr_input_hub_info_humidity_range);
		vrHumidityRange.setMinValue(MIN_HUMIDITY);
		vrHumidityRange.setMaxValue(MAX_HUMIDITY);
		vrHumidityRange.setScale("%");
		vrHumidityRange.setLeftThumbPos((int)mMinHumidity);
		vrHumidityRange.setRightThumbPos((int)mMaxHumidity);
		vrHumidityRange.setLeftThumbTextView((int)mMinHumidity + "");
		vrHumidityRange.setRightThumbTextView((int)mMaxHumidity + "");
		vrHumidityRange.updateView();
		vrHumidityRange.setOnValueChangedListener(new RangeSeekBar.OnRangeSeekBarListener() {
			@Override
			public void onValueChanged(int mMinValue, int mMaxValue, int leftPos, int rightPos) {
				vrHumidityRange.setText(leftPos + "% ~ " + rightPos + "%");
				vrHumidityRange.setLeftThumbPos(leftPos);
				vrHumidityRange.setLeftThumbTextView(leftPos + "");
				vrHumidityRange.setRightThumbPos(rightPos);
				vrHumidityRange.setRightThumbTextView(rightPos + "");
				mMinHumidity = leftPos;
				mMaxHumidity = rightPos;
			}
		});
    }

    private void _updateTemperatureScale() {
		mMaxTemperature = mHubDevice.getMaxTemperature();
		mMinTemperature = mHubDevice.getMinTemperature();
		vrTemperatureRange.setScale(mPreferenceMgr.getTemperatureScale());
		if (mPreferenceMgr.getTemperatureScale().equals(getString(R.string.unit_temperature_fahrenheit))) {
			mMaxTemperature = (int)UnitConvertUtil.getFahrenheitFromCelsius(mMaxTemperature);
			mMinTemperature = (int)UnitConvertUtil.getFahrenheitFromCelsius(mMinTemperature);
			vrTemperatureRange.setMinValue(MIN_TEMPERATURE_FAHRENHEIT);
			vrTemperatureRange.setMaxValue(MAX_TEMPERATURE_FAHRENHEIT);
			vrTemperatureRange.setScale(getString(R.string.unit_temperature_fahrenheit));
		} else {
			vrTemperatureRange.setMinValue(MIN_TEMPERATURE_CELSIUS);
			vrTemperatureRange.setMaxValue(MAX_TEMPERATURE_CELSIUS);
			vrTemperatureRange.setScale(getString(R.string.unit_temperature_celsius));
		}
		vrTemperatureRange.setLeftThumbPos((int)mMinTemperature);
		vrTemperatureRange.setLeftThumbTextView((int)mMinTemperature + "");
		vrTemperatureRange.setRightThumbPos((int)mMaxTemperature);
		vrTemperatureRange.setRightThumbTextView((int)mMaxTemperature + "");
		vrTemperatureRange.updateView();
	}

	private void _hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(vetRoomName.getWindowToken(), 0);
	}

    public void showHubSettingView(boolean show) {
		showHubSetting = show;

		if (showHubSetting) {
			if (lctnSetting != null) {
				lctnSetting.setVisibility(View.VISIBLE);
			}
			if (tvTitle != null) {
				tvTitle.setText(R.string.connection_hub_connected_title);
			}
			if (tvDetail != null) {
				tvDetail.setText(R.string.connection_hub_connected_detail_with_setting);
			}
		} else {
			if (lctnSetting != null) {
				lctnSetting.setVisibility(View.GONE);
			}
		}
	}

	public void setDescriptionContents(String description) {
		mDetail = description;
		if (tvDetail != null) {
			tvDetail.setText(description);
		}
	}

	public float getMaxHumidity() {
		return mMaxHumidity;
	}

	public float getMinHumidity() {
		return mMinHumidity;
	}

	public float getMaxTemperature() {
		return mMaxTemperature;
	}

	public float getMinTemperature() {
		return mMinTemperature;
	}

	public String getName() {
    	return vetRoomName.getText();
	}

	public boolean isCompleted() {
		if (!vetRoomName.isValid()) {
			vetRoomName.showWarning(true, 1000);
		}
		if (!vrTemperatureUnit.isValid()) {
			vrTemperatureUnit.showWarning(true, 1000);
		}

		if (!vrTemperatureRange.isValid()) {
			vrTemperatureRange.showWarning(true, 1000);
		}
		if (!vrHumidityRange.isValid()) {
			vrHumidityRange.showWarning(true, 1000);
		}

		if (vetRoomName.isValid() && vrTemperatureUnit.isValid() && vrTemperatureRange.isValid() && vrHumidityRange.isValid()) {
			return true;
		}
		return false;
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
		((ConnectionActivity)mMainActivity).closeApConnectionDialog();

		if (mTitle != null) {
			tvTitle.setText(mTitle);
		}

		if(mDetail != null) {
			tvDetail.setText(mDetail);
		}

		if (showHubSetting) {
			if (lctnSetting != null) {
				lctnSetting.setVisibility(View.VISIBLE);
			}
			if (tvTitle != null) {
				tvTitle.setText(R.string.connection_hub_connected_title);
			}
			if (tvDetail != null) {
				tvDetail.setText(R.string.connection_hub_connected_detail_with_setting);
			}
		} else {
			if (lctnSetting != null) {
				lctnSetting.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}
