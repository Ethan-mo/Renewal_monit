package goodmonit.monit.com.kao.devicestatus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceEnvironmentActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.DeviceAQMHub;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.devices.EnvironmentCheckManager;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.VersionManager;
import goodmonit.monit.com.kao.util.UnitConvertUtil;
import goodmonit.monit.com.kao.widget.WheelTimePicker;
import goodmonit.monit.com.kao.widget.WheelView;

public class EnvironmentStatus2Fragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "EnvironmentFrag2";
	private static final boolean DBG = Configuration.DBG;

	private static final int MSG_REFRESH_VIEW 			= 1;

	private static final int OPEN_LAMP_SETTING_INTERVAL_SEC 	= 3;
	private static final int CLOSE_LAMP_SETTING_INTERVAL_SEC 	= 30;
	private static final int REFRESH_VIEW_INTERVAL_SEC 	= 1;
	//private static final String ALLOWANCE_SERIAL_FOR_LAMP_SETTING = "HKM";
	private static final String[] WHITE_LIST_SERIAL_FOR_LAMP_SETTING = {"HKM", "HKU"};
	private static final String[] BLACK_LIST_SERIAL_FOR_LAMP_SETTING = {"HKU851"};

	private LinearLayout lctnStatusItem1, lctnStatusItem2, lctnStatusItem3;

	private ImageView ivStatusItem1Icon;
	private TextView tvStatusItem1Title, tvStatusItem1Content, tvStatusItem1ContentExtra;

	private ImageView ivStatusItem2Icon;
	private TextView tvStatusItem2Title, tvStatusItem2Content, tvStatusItem2ContentExtra;

	private ImageView ivStatusItem3Icon;
	private TextView tvStatusItem3Title, tvStatusItem3Content, tvStatusItem3ContentExtra;

	private boolean isLoaded = false;

	private DeviceAQMHub mHub;
	private EnvironmentCheckManager mEnvironmentMgr;

	/* Related to Lamp Setting */
	private WheelTimePicker wtpTimer;
	private TextView tvLampOffTimerRemainingTime;
	private ImageView ivLampSectionTimerIcon;

	private Button btnLampBrightnessDown, btnLampBrightnessUp, btnLampPowerEnabled;
	private Button btnLampSectionOffTimerStart;
	private View vBrightnessLv1, vBrightnessLv2, vBrightnessLv3, vBrightnessLv4, vBrightnessLv5;

	private Button btnDeviceOperationStatus;
	private TextView tvDeviceOperationStatus;

	private boolean isAvailableForLampRemoteSetting = true;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_aqmhub_status2, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mEnvironmentMgr = new EnvironmentCheckManager(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(1101);

		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			isAvailableForLampRemoteSetting = false;
		}

        _initView(view);

        return view;
    }

	private void _initView(View v) {
		lctnStatusItem1 = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_item1);
		lctnStatusItem2 = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_item2);
		lctnStatusItem3 = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_item3);
		lctnStatusItem3.setVisibility(View.GONE);

		ivStatusItem1Icon = (ImageView)v.findViewById(R.id.iv_device_detail_status_item1_icon);
		ivStatusItem2Icon = (ImageView)v.findViewById(R.id.iv_device_detail_status_item2_icon);
		ivStatusItem3Icon = (ImageView)v.findViewById(R.id.iv_device_detail_status_item3_icon);

		tvStatusItem1Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_title);
		tvStatusItem2Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_title);
		tvStatusItem3Title = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_title);
		tvStatusItem1Title.setText(getString(R.string.device_environment_temperature));
		tvStatusItem2Title.setText(getString(R.string.device_environment_humidity));
		tvStatusItem3Title.setText(getString(R.string.device_environment_voc));

		tvStatusItem1Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_content);
		tvStatusItem2Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_content);
		tvStatusItem3Content = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_content);

		tvStatusItem1ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item1_content_extra);
		tvStatusItem2ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item2_content_extra);
		tvStatusItem3ContentExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_item3_content_extra);

		vBrightnessLv1 = (View)v.findViewById(R.id.v_device_detail_status_lamp_brightness_lv1);
		vBrightnessLv2 = (View)v.findViewById(R.id.v_device_detail_status_lamp_brightness_lv2);
		vBrightnessLv3 = (View)v.findViewById(R.id.v_device_detail_status_lamp_brightness_lv3);
		vBrightnessLv4 = (View)v.findViewById(R.id.v_device_detail_status_lamp_brightness_lv4);
		vBrightnessLv5 = (View)v.findViewById(R.id.v_device_detail_status_lamp_brightness_lv5);

		btnDeviceOperationStatus = (Button)v.findViewById(R.id.btn_device_detail_status_device_operation_status);
		tvDeviceOperationStatus = (TextView)v.findViewById(R.id.tv_device_detail_status_device_operation_status);

		btnLampPowerEnabled = (Button)v.findViewById(R.id.btn_device_detail_status_lamp_power);
		btnLampPowerEnabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mHub != null) {
					int power = mHub.getLampPower();
					if (power == DeviceStatus.LAMP_POWER_OFF) {
						power = DeviceStatus.LAMP_POWER_ON;
						if (mHub.getBrightLevel() == DeviceStatus.BRIGHT_OFF) {
							if (DBG) Log.d(TAG, "Power On, but brightness 0");
							// 수유등을 직접 누르면서 전원OFF, 밝기OFF 설정되어 있다면,
							// 전원을 켜면서 밝기레벨을 1로 변경해줘야함
							mHub.setBrightLevel(DeviceStatus.BRIGHT_ON1);
							((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(DeviceStatus.BRIGHT_ON1);
							setLampBrightLevelUI(DeviceStatus.BRIGHT_ON1);
						} else {
							setLampBrightLevelUI(mHub.getBrightLevel());
						}
					} else {
						power = DeviceStatus.LAMP_POWER_OFF;
						btnLampPowerEnabled.setSelected(false);
						setLampBrightLevelUI(DeviceStatus.BRIGHT_OFF);

						// 꺼짐 타이머 초기화
						mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, 0);
						((DeviceEnvironmentActivity)mMainActivity).updateLampOffTimer(0);
					}

					mHub.setLampPower(power);
					setLampPowerUI(power);
					((DeviceEnvironmentActivity)mMainActivity).updateLampPower(power);
					_updateLampOffTimerUI();
				}
			}
		});

		btnLampBrightnessUp = (Button)v.findViewById(R.id.btn_device_detail_status_lamp_brightness_up);
		btnLampBrightnessUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mHub != null) {
					int brightLevel = mHub.getBrightLevel();
					if (brightLevel >= DeviceStatus.BRIGHT_ON5) {
						// 최대밝기, 아무것도 안함
						return;
					} else if (brightLevel >= DeviceStatus.BRIGHT_ON4) {
						brightLevel = DeviceStatus.BRIGHT_ON5;
					} else if (brightLevel >= DeviceStatus.BRIGHT_ON3) {
						brightLevel = DeviceStatus.BRIGHT_ON4;
					} else if (brightLevel >= DeviceStatus.BRIGHT_ON2) {
						brightLevel = DeviceStatus.BRIGHT_ON3;
					} else if (brightLevel >= DeviceStatus.BRIGHT_ON1) {
						brightLevel = DeviceStatus.BRIGHT_ON2;
					}
					mHub.setBrightLevelRemoteFromApp(brightLevel);
					setLampBrightLevelUI(brightLevel);
					((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(brightLevel);
					_updateLampOffTimerUI();
				}
			}
		});

		btnLampBrightnessDown = (Button)v.findViewById(R.id.btn_device_detail_status_lamp_brightness_down);
		btnLampBrightnessDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mHub != null) {
					int brightLevel = mHub.getBrightLevel();
					if (brightLevel <= DeviceStatus.BRIGHT_ON1) {
						// 최소밝기, 아무것도 안함
						return;
					} else if (brightLevel <= DeviceStatus.BRIGHT_ON2) {
						brightLevel = DeviceStatus.BRIGHT_ON1;
					} else if (brightLevel <= DeviceStatus.BRIGHT_ON3) {
						brightLevel = DeviceStatus.BRIGHT_ON2;
					} else if (brightLevel <= DeviceStatus.BRIGHT_ON4) {
						brightLevel = DeviceStatus.BRIGHT_ON3;
					} else if (brightLevel <= DeviceStatus.BRIGHT_ON5) {
						brightLevel = DeviceStatus.BRIGHT_ON4;
					}
					mHub.setBrightLevelRemoteFromApp(brightLevel);
					setLampBrightLevelUI(brightLevel);
					((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(brightLevel);
					_updateLampOffTimerUI();
				}
			}
		});
		tvLampOffTimerRemainingTime = (TextView)v.findViewById(R.id.tv_device_detail_status_environment_lamp_section_timer_remaining_time);
		ivLampSectionTimerIcon = (ImageView)v.findViewById(R.id.iv_device_detail_status_environment_lamp_section_timer);

		wtpTimer = (WheelTimePicker) v.findViewById(R.id.wtp_device_detail_status_environment_lamp_timer);
		wtpTimer.setTime(0, 0, 0);
		wtpTimer.showSecond(false);
		wtpTimer.showSelectionDivider(false);
		wtpTimer.setOnSelectedTimeListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onSelectedTime(int hour, int minute, int second) {
				if (DBG) Log.d(TAG, "onSelectedTime: " + hour + ":" + minute + ":" + second + "->" + wtpTimer.getTimeTotalSecond());
			}
		});

		btnLampSectionOffTimerStart = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_section_timer_start);
		btnLampSectionOffTimerStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnLampSectionOffTimerStart.isSelected()) { // Cancel 인 경우
					mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, 0);
					((DeviceEnvironmentActivity)mMainActivity).updateLampOffTimer(0);
				} else {
					long now = System.currentTimeMillis();
					now += wtpTimer.getTimeTotalSecond() * 1000;
					mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, now);
					((DeviceEnvironmentActivity)mMainActivity).updateLampOffTimer(now);
				}
				_updateLampOffTimerUI();
			}
		});

		isLoaded = true;
    }

	public void setLampPowerUI(int power) {
		if (DBG) Log.d(TAG, "setLampPowerUI: " + power);
		if (power == DeviceStatus.LAMP_POWER_ON) {
			btnLampPowerEnabled.setSelected(true);
			btnLampBrightnessDown.setEnabled(true);
			btnLampBrightnessUp.setEnabled(true);
		} else {
			btnLampPowerEnabled.setSelected(false);
			btnLampBrightnessDown.setEnabled(false);
			btnLampBrightnessUp.setEnabled(false);
		}
	}

    public void setLampBrightLevelUI(int brightLevel) {
		boolean power = (mHub.getLampPower() == DeviceStatus.LAMP_POWER_ON ? true : false);
		if (DBG) Log.d(TAG, "setLampBrightLevelUI: " + brightLevel + " / " + power);
		vBrightnessLv5.setSelected((brightLevel >= DeviceStatus.BRIGHT_ON5) && power);
		vBrightnessLv4.setSelected((brightLevel >= DeviceStatus.BRIGHT_ON4) && power);
		vBrightnessLv3.setSelected((brightLevel >= DeviceStatus.BRIGHT_ON3) && power);
		vBrightnessLv2.setSelected((brightLevel >= DeviceStatus.BRIGHT_ON2) && power);
		vBrightnessLv1.setSelected((brightLevel >= DeviceStatus.BRIGHT_ON1) && power);
	}

	public void setConnected(boolean connected) {
		if (!isAdded() || mHub == null) {
			return;
		}
		if (connected) {
			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated_kc);
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated_kc);
			} else {
				ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_deactivated);
			}

			tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
			tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));

			tvStatusItem1Content.setVisibility(View.VISIBLE);
			tvStatusItem2Content.setVisibility(View.VISIBLE);
			tvStatusItem3Content.setVisibility(View.VISIBLE);
			tvStatusItem1Title.setText(getString(R.string.device_environment_temperature));
			tvStatusItem2Title.setText(getString(R.string.device_environment_humidity));
			tvStatusItem3Title.setText(getString(R.string.device_environment_voc));

			tvStatusItem1ContentExtra.setVisibility(View.VISIBLE);
			tvStatusItem2ContentExtra.setVisibility(View.VISIBLE);
			tvStatusItem1ContentExtra.setText(mPreferenceMgr.getTemperatureScale());
			tvStatusItem2ContentExtra.setText("%");

			btnLampPowerEnabled.setEnabled(true);
			if (mHub != null) {
				setLampBrightLevelUI(mHub.getBrightLevel());
				setLampPowerUI(mHub.getLampPower());
			}

			btnDeviceOperationStatus.setSelected(true);
			tvDeviceOperationStatus.setText(R.string.device_sensor_operation_connected);

		} else {
			ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_deactivated);
			ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_deactivated);
			ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_deactivated);

			tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));

			tvStatusItem1Content.setVisibility(View.INVISIBLE);
			tvStatusItem2Content.setVisibility(View.INVISIBLE);
			tvStatusItem3Content.setVisibility(View.INVISIBLE);
			tvStatusItem1ContentExtra.setVisibility(View.INVISIBLE);
			tvStatusItem2ContentExtra.setVisibility(View.INVISIBLE);

			btnLampPowerEnabled.setEnabled(false);
			btnLampBrightnessDown.setEnabled(false);
			btnLampBrightnessUp.setEnabled(false);
			setLampBrightLevelUI(DeviceStatus.BRIGHT_OFF);
			setLampPowerUI(DeviceStatus.LAMP_POWER_OFF);

			btnDeviceOperationStatus.setSelected(false);
			tvDeviceOperationStatus.setText(R.string.device_sensor_operation_disconnected);
		}
	}

	public void setTemperatureStatus(float temperature) {
		if (mPreferenceMgr.getTemperatureScale().equals(mContext.getString(R.string.unit_temperature_fahrenheit))) {
			tvStatusItem1Content.setText(UnitConvertUtil.getFahrenheitFromCelsius(temperature) + "");
		} else {
			tvStatusItem1Content.setText(temperature + "");
		}

		switch (mHub.getTemperatureStatus()) {
			case EnvironmentCheckManager.HIGH:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning_high_kc);
				} else {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning);
				}
				break;
			case EnvironmentCheckManager.NORMAL:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated_kc);
				} else {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				}
				break;
			case EnvironmentCheckManager.LOW:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextWarningBlue));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning_low_kc);
				} else {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning);
				}
				break;
		}
	}

	public void setHumidityStatus(float humidity) {
		tvStatusItem2Content.setText(humidity + "");

		switch(mHub.getHumidityStatus()) {
			case EnvironmentCheckManager.HIGH:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning_high_kc);
					if (mHub.getTemperatureStatus() != EnvironmentCheckManager.NORMAL) { // 온도가 Warning이면 Description을 바꿀 필요 없음, 온도우선
						break;
					}
				} else {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning);
				}
				break;
			case EnvironmentCheckManager.NORMAL:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated_kc);
				} else {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
				}
				break;
			case EnvironmentCheckManager.LOW:
				if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning_low_kc);
					if (mHub.getTemperatureStatus() != EnvironmentCheckManager.NORMAL) { // 온도가 Warning이면 Description을 바꿀 필요 없음, 온도우선
						break;
					}
				} else {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning);
				}
				break;
		}
	}

	public void setVocStatus(float voc) {
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) return; // KC는 VOC관련 피처 제거

		tvStatusItem3Content.setText(mEnvironmentMgr.getVocString(voc));

		switch (mHub.getVocStatus()) {
			case EnvironmentCheckManager.HIGH:
				tvStatusItem3Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
				tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_warning);

				break;
			case EnvironmentCheckManager.NORMAL:
				tvStatusItem3Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
				tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
				ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
				break;
		}
	}

	public void setSensorAttached(boolean attached) {
		if (attached) {
			lctnStatusItem3.setVisibility(View.VISIBLE);
			ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
		}  else {
			lctnStatusItem3.setVisibility(View.GONE);
			ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_deactivated);
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvStatusItem3Content.setVisibility(View.INVISIBLE);
		}
	}

	public void refreshView() {
		if (mHub == null) {
			return;
		}
		try {
			if (mHub.getConnectionState() == DeviceConnectionState.BLE_CONNECTED ||
					mHub.getConnectionState() == DeviceConnectionState.WIFI_CONNECTED) {
				if (mHub.getTemperature() == -1 || mHub.getHumidity() == -1) {
					setConnected(false);
					return;
				}

				setConnected(true);

				setLampPowerUI(mHub.getLampPower());
				setLampBrightLevelUI(mHub.getBrightLevel());
				setTemperatureStatus(mHub.getTemperature());
				setHumidityStatus(mHub.getHumidity());

				int attachedId = mHub.getSensorAttached();
				if (attachedId != DeviceStatus.SENSOR_DETACHED) {
					setSensorAttached(true);
					setVocStatus(mHub.getVoc());
					String sensorName = "";
					if (mPreferenceMgr != null) sensorName = mPreferenceMgr.getDeviceName(DeviceType.DIAPER_SENSOR, attachedId);
					//tvWhereConnected.setText("Charging the sensor\n" + sensorName + "(" + attachedId + ")");
				} else {
					setSensorAttached(false);
					//tvWhereConnected.setText("");
				}
			} else {
				setConnected(false);
				setSensorAttached(false);
			}

			_updateLampOffTimerUI();
		} catch (IllegalStateException ex) {
			if (DBG) Log.e(TAG, "refreshView exception");
		}
	}

	private void _updateLampOffTimerUI() {
		long now = System.currentTimeMillis();
		long lampOffTimerMs = mPreferenceMgr.getLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId);
		long diffMs = lampOffTimerMs - now;
		if (lampOffTimerMs > 0 && diffMs < 0) {
			mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, 0);
			mHub.setBrightLevel(DeviceStatus.BRIGHT_OFF);
		}

		// 허브 밝기가 꺼진 상태라면, Timer 설정에 대한 내용 필요 없음
		if (mHub.getLampPower() == DeviceStatus.LAMP_POWER_OFF) {

			// Timer가 동작하고 있던 상태라면,(TimePicker 숨겨진 상태)
			if (wtpTimer.getVisibility() == View.GONE) {
				wtpTimer.setVisibility(View.VISIBLE);
				tvLampOffTimerRemainingTime.setText("00:00");
				tvLampOffTimerRemainingTime.setVisibility(View.GONE);
				ivLampSectionTimerIcon.setVisibility(View.GONE);
			}
			// Enabled 되어 있으면 TimePicker가 더이상 움직이지 않도록 설정
			if (wtpTimer.isEnabled() == true) {
				wtpTimer.onExpanded();
				wtpTimer.setTime(0, 0, 0);
				wtpTimer.setEnabled(false);
			}
			// Timer가 동작하고 있던 상태라면, (Start 버튼이 활성화 된 상태)
			if (btnLampSectionOffTimerStart.isEnabled() == true) {
				btnLampSectionOffTimerStart.setText(R.string.btn_start);
				btnLampSectionOffTimerStart.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
				btnLampSectionOffTimerStart.setEnabled(false);
			}
		} else { // 허브 밝기가 켜진 상태라면,
			// 1. 타이머가 설정되어 있음
			if (lampOffTimerMs > 0 && diffMs > 0) {
				int hour, minute, second;
				diffMs = diffMs / 1000; // Minutes
				second = (int)(diffMs % 60);
				diffMs = diffMs / 60;
				minute = (int)(diffMs % 60); // +1을 더하는 이유는 59초 남으면 00:00 으로 표기됨
				hour = (int)(diffMs / 60);

				// 타이머가 설정되어 있으므로, TimePicker가 삭제되어야함
				if (wtpTimer.getVisibility() == View.VISIBLE) {
					wtpTimer.setVisibility(View.GONE);
					//wtpTimer.onCollapsed();
				}
				// 타이머 남은시간 보이기
				if (tvLampOffTimerRemainingTime.getVisibility() == View.GONE) {
					tvLampOffTimerRemainingTime.setVisibility(View.VISIBLE);
					ivLampSectionTimerIcon.setVisibility(View.VISIBLE);
				}
				// 타이머 남은시간 업데이트
				if (tvLampOffTimerRemainingTime.getText().toString().contains(":")) {
					tvLampOffTimerRemainingTime.setText(String.format("%02d %02d", hour, minute));
				} else {
					tvLampOffTimerRemainingTime.setText(String.format("%02d:%02d", hour, minute));
				}
				// 타이머 시작 버튼 Disabled 되어있으면 Enabled로 변경
				if (btnLampSectionOffTimerStart.isEnabled() == false) {
					btnLampSectionOffTimerStart.setEnabled(true);
				}
				// 타이머 시작 버튼이 Deselected 되어있으면, Selected로 변경
				if (btnLampSectionOffTimerStart.isSelected() == false) {
					btnLampSectionOffTimerStart.setSelected(true);
					btnLampSectionOffTimerStart.setText(R.string.btn_cancel);
					btnLampSectionOffTimerStart.setTextColor(getResources().getColor(R.color.colorTextGrey));
				}
			} else { // 타이머가 설정되어 있지 않음
				// Timer 시간이 설정되어있다면, 0으로 설정
				// 수유등 Section이 켜져있는 상태라면,

				// 타이머 남은시간 숨기기
				if (tvLampOffTimerRemainingTime.getVisibility() == View.VISIBLE) {
					tvLampOffTimerRemainingTime.setVisibility(View.GONE);
					ivLampSectionTimerIcon.setVisibility(View.GONE);
				}
				// TimePicker가 Disabled 되어 있으면 TimePicker가 움직이도록 설정
				if (wtpTimer.isEnabled() == false) {
					wtpTimer.onExpanded();
					wtpTimer.setTime(0, 0, 0);
					wtpTimer.setEnabled(true);
				}

				// TimePicker가 숨겨져있다면, 초기화 후 보이기
				if (wtpTimer.getVisibility() == View.GONE) {
					wtpTimer.setVisibility(View.VISIBLE);
					wtpTimer.onExpanded();
					// TimePicker 첫 설정
					wtpTimer.setTime(0, 0, 0);
				}
				// 타이머 시작 버튼이 Disabled 되어있으면 Enabled로 변경
				if (btnLampSectionOffTimerStart.isEnabled() == false) {
					btnLampSectionOffTimerStart.setEnabled(true);
					btnLampSectionOffTimerStart.setText(R.string.btn_start);
					btnLampSectionOffTimerStart.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
				}
				// 타이머 시작 버튼이 Selected로 되어있으면, deselected로 변경
				if (btnLampSectionOffTimerStart.isSelected() == true) {
					btnLampSectionOffTimerStart.setSelected(false);
					btnLampSectionOffTimerStart.setText(R.string.btn_start);
					btnLampSectionOffTimerStart.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
				}
			}
		}
	}

	public void setAQMHubDeviceInfo(DeviceAQMHub hub) {
		if (DBG) Log.d(TAG, "setAQMHubDeviceInfo: " + hub.toString());
		mHub = hub;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_REFRESH_VIEW:
					mHandler.removeMessages(MSG_REFRESH_VIEW);
					mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, REFRESH_VIEW_INTERVAL_SEC * 1000L);
					refreshView();
					break;
			}
		}
	};

    @Override
	public void onPause() {
    	super.onPause();
    	if (DBG) Log.i(TAG, "onPause");
		mHandler.removeMessages(MSG_REFRESH_VIEW);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG) Log.i(TAG, "onResume");
		mMainActivity = getActivity();

		((DeviceEnvironmentActivity)mMainActivity).updateNewMark();
		mHub = ((DeviceEnvironmentActivity)mMainActivity).getAQMHubObject();
		if (mHub == null) {
			if (DBG) Log.e(TAG, "Object NULL");
			mMainActivity.finish();
			return;
		}

		String firmwareVersion = mHub.firmwareVersion;
		if (new VersionManager(mContext).supportLampSetting(firmwareVersion)) {

		}
		mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}