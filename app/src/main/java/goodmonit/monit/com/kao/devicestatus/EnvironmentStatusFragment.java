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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.activity.DeviceEnvironmentActivity;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.CurrentLampValue;
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
import goodmonit.monit.com.kao.widget.TooltipBox;
import goodmonit.monit.com.kao.widget.WheelTimePicker;
import goodmonit.monit.com.kao.widget.WheelView;

public class EnvironmentStatusFragment extends BaseFragment {
    private static final String TAG = Configuration.BASE_TAG + "EnvironmentFrag";
	private static final boolean DBG = Configuration.DBG;

	private static final int MSG_REFRESH_VIEW 			= 1;

	private static final int OPEN_LAMP_SETTING_INTERVAL_SEC 	= 3;
	private static final int CLOSE_LAMP_SETTING_INTERVAL_SEC 	= 30;
	private static final int REFRESH_VIEW_INTERVAL_SEC 	= 1;
	//private static final String ALLOWANCE_SERIAL_FOR_LAMP_SETTING = "HKM";
	private static final String[] WHITE_LIST_SERIAL_FOR_LAMP_SETTING = {"HKM", "HKU"};
	private static final String[] BLACK_LIST_SERIAL_FOR_LAMP_SETTING = {"HKU851"};

	private LinearLayout lctnStatusScore;

	private LinearLayout lctnStatusItem1, lctnStatusItem2, lctnStatusItem3;

	private RelativeLayout rctnStatusBackground;

	private ImageView ivStatusItem1Icon;
	private TextView tvStatusItem1Title, tvStatusItem1Content, tvStatusItem1ContentExtra;

	private ImageView ivStatusItem2Icon;
	private TextView tvStatusItem2Title, tvStatusItem2Content, tvStatusItem2ContentExtra;

	private ImageView ivStatusItem3Icon;
	private TextView tvStatusItem3Title, tvStatusItem3Content, tvStatusItem3ContentExtra;

	private TextView tvStatusDescription, tvStatusDescriptionExtra;
	private TextView tvWhereConnected;

	private boolean isLoaded = false;

	private DeviceAQMHub mHub;
	private EnvironmentCheckManager mEnvironmentMgr;
	private int mDescriptionType = 0;
	private int mDescriptionExtraType = 0;

	/* Related to Lamp Setting */
	private LinearLayout lctnLampSection;
	private WheelTimePicker wtpTimer;
	private TextView tvLampOffTimerRemainingTime;

	private Button btnOpenLampSection;
	private ProgressBar pbLampSection;
	private Button btnLampOff, btnLampOn1, btnLampOn2, btnLampOn3;
	private TextView tvLampSectionOffTimerRemainingTime;
	private Button btnLampSectionOffTimerStart;
	private ImageView ivLampSectionTimerIcon;

	private long mOpenLampSettingSec = 0;
	private boolean isAvailableForLampRemoteSetting = true;

	/* Tooltip Box */
	private TooltipBox tbLampSection, tbLampBrightnessButton, tbLampOffTimer;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.content_device_detail_aqmhub_status, container, false);
		mContext = inflater.getContext();
		mPreferenceMgr = PreferenceManager.getInstance(getContext());
		mEnvironmentMgr = new EnvironmentCheckManager(mContext);
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mScreenInfo = new ScreenInfo(1101);

		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			isAvailableForLampRemoteSetting = false;
		}

        _initView(view);
		_initLampView(view);
		_initTooltipView(view);

        return view;
    }

	private void _initView(View v) {
		rctnStatusBackground = (RelativeLayout)v.findViewById(R.id.rctn_device_detail_status_background);
		rctnStatusBackground.setBackgroundResource(0);

		lctnStatusScore = (LinearLayout)v.findViewById(R.id.lctn_device_status_environment_score);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_transparent_kc);
		} else {
			lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_transparent);
		}
		lctnStatusScore.setVisibility(View.VISIBLE);

		lctnStatusItem1 = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_item1);
		lctnStatusItem2 = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_item2);
		lctnStatusItem3 = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_item3);

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

		tvStatusDescription = (TextView)v.findViewById(R.id.tv_device_detail_status_description);
		tvStatusDescriptionExtra = (TextView)v.findViewById(R.id.tv_device_detail_status_description_extra);

		tvWhereConnected = (TextView)v.findViewById(R.id.tv_device_detail_status_aqmhub_where_conn);
		if (Configuration.MASTER) tvWhereConnected.setVisibility(View.VISIBLE);

		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			tvStatusItem1Title.setVisibility(View.GONE);
			tvStatusItem2Title.setVisibility(View.GONE);
			tvStatusItem3Title.setVisibility(View.GONE);
			lctnStatusItem3.setVisibility(View.GONE);
		}

		isLoaded = true;
    }

    private void _initLampView(View v) {
		lctnLampSection = (LinearLayout)v.findViewById(R.id.lctn_device_detail_status_environment_lamp_section);
		btnOpenLampSection = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_section);
		if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
			btnOpenLampSection.setBackgroundResource(R.drawable.bg_lamp_setting_button_selector_kc);
		}

		btnLampOff = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_off);
		btnLampOn1 = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_on_phase1);
		btnLampOn2 = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_on_phase2);
		btnLampOn3 = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_on_phase3);
		pbLampSection = (ProgressBar)v.findViewById(R.id.pb_device_detail_status_environment_lamp_section);
		wtpTimer = (WheelTimePicker) v.findViewById(R.id.wtp_device_detail_status_environment_lamp_timer);
		tvLampOffTimerRemainingTime = (TextView)v.findViewById(R.id.tv_device_detail_status_environment_lamp_timer_remaining_time);
		tvLampSectionOffTimerRemainingTime = (TextView)v.findViewById(R.id.tv_device_detail_status_environment_lamp_section_timer_remaining_time);
		btnLampSectionOffTimerStart = (Button)v.findViewById(R.id.btn_device_detail_status_environment_lamp_section_timer_start);
		ivLampSectionTimerIcon = (ImageView)v.findViewById(R.id.iv_device_detail_status_environment_lamp_section_timer);

		wtpTimer.setTime(0, 0, 0);
		wtpTimer.showSecond(false);
		wtpTimer.showSelectionDivider(false);
		wtpTimer.setOnSelectedTimeListener(new WheelView.OnWheelViewListener() {
			@Override
			public void onSelectedTime(int hour, int minute, int second) {
				if (DBG) Log.d(TAG, "onSelectedTime: " + hour + ":" + minute + ":" + second + "->" + wtpTimer.getTimeTotalSecond());
			}
		});

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

		btnOpenLampSection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (lctnLampSection.getVisibility() == View.VISIBLE) {
					tvLampOffTimerRemainingTime.setVisibility(View.VISIBLE);
					lctnLampSection.setVisibility(View.GONE);
					_updateLampOffTimerUI();
				} else {
					// 툴팁박스 닫기
					tbLampSection.setVisibility(View.GONE);

					// 수유등 컨트롤 판넬 열기
					tvLampOffTimerRemainingTime.setText("");
					tvLampOffTimerRemainingTime.setVisibility(View.GONE);

					// 허브 30초간 연속 패킷전송 설정을 위해 패킷 전송
					((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(-1);

					// 이전에 수유등 컨트롤 판넬을 연지 30초가 지났으면, 3초간 기다렸다가 판넬 열기
					// 허브가 기존 10초에 한번 Polling모드에서 30초간 연속 패킷 전송 모드로 설정이 되어야 하므로...
					if ((mOpenLampSettingSec == 0)
							|| (System.currentTimeMillis() - mOpenLampSettingSec > CLOSE_LAMP_SETTING_INTERVAL_SEC * 1000)) // 30초 이상 지났으면 다시
					{
						if (DBG) Log.d(TAG, "need warm up time");
						mOpenLampSettingSec = System.currentTimeMillis();
						btnOpenLampSection.setVisibility(View.GONE);
						pbLampSection.setVisibility(View.VISIBLE);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								btnOpenLampSection.setVisibility(View.VISIBLE);
								pbLampSection.setVisibility(View.GONE);
								lctnLampSection.setVisibility(View.VISIBLE);
								//wtpTimer.setTime(0, 0, 0);
								wtpTimer.onExpanded(); // Lamp Section 켜지면 나타나야함
								_updateLampOffTimerUI();
							}
						}, OPEN_LAMP_SETTING_INTERVAL_SEC * 1000);
					} else {
						// 허브가 30초간 연속 패킷 전송 모드로 동작하고 있다면 바로 판넬 열기
						lctnLampSection.setVisibility(View.VISIBLE);
						//wtpTimer.setTime(0, 0, 0);
						wtpTimer.onExpanded(); // Lamp Section 켜지면 나타나야함
						_updateLampOffTimerUI();
					}
				}
			}
		});

		btnLampOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOpenLampSettingSec = System.currentTimeMillis();
				if (mHub != null) {
					mHub.setBrightLevelRemoteFromApp(DeviceStatus.BRIGHT_OFF);
				}
				setBrightLevel(DeviceStatus.BRIGHT_OFF);
				((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(DeviceStatus.BRIGHT_OFF);
				((DeviceEnvironmentActivity)mMainActivity).updateLampPower(DeviceStatus.LAMP_POWER_OFF);
				mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, 0);
				((DeviceEnvironmentActivity)mMainActivity).updateLampOffTimer(0);
				_updateLampOffTimerUI();
			}
		});
		btnLampOn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOpenLampSettingSec = System.currentTimeMillis();
				if (mHub != null) {
					mHub.setBrightLevelRemoteFromApp(DeviceStatus.BRIGHT_ON2);
				}
				setBrightLevel(DeviceStatus.BRIGHT_ON2);
				((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(DeviceStatus.BRIGHT_ON2);
				((DeviceEnvironmentActivity)mMainActivity).updateLampPower(DeviceStatus.LAMP_POWER_ON);
				_updateLampOffTimerUI();
			}
		});
		btnLampOn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOpenLampSettingSec = System.currentTimeMillis();
				if (mHub != null) {
					mHub.setBrightLevelRemoteFromApp(DeviceStatus.BRIGHT_ON4);
				}
				setBrightLevel(DeviceStatus.BRIGHT_ON4);
				((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(DeviceStatus.BRIGHT_ON4);
				((DeviceEnvironmentActivity)mMainActivity).updateLampPower(DeviceStatus.LAMP_POWER_ON);
				_updateLampOffTimerUI();
			}
		});
		btnLampOn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOpenLampSettingSec = System.currentTimeMillis();
				if (mHub != null) {
					mHub.setBrightLevelRemoteFromApp(DeviceStatus.BRIGHT_ON5);
				}
				setBrightLevel(DeviceStatus.BRIGHT_ON5);
				((DeviceEnvironmentActivity)mMainActivity).updateLampBrightLevel(DeviceStatus.BRIGHT_ON5);
				((DeviceEnvironmentActivity)mMainActivity).updateLampPower(DeviceStatus.LAMP_POWER_ON);
				_updateLampOffTimerUI();
			}
		});
	}

	public void _initTooltipView(View v) {
		tbLampSection = (TooltipBox)v.findViewById(R.id.tb_device_detail_status_environment_lamp_section);
		tbLampSection.setDescription(mServerQueryMgr.getParameter(2000));
		tbLampSection.setCloseButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPreferenceMgr.setDoNotShowTooltipBox(tbLampSection.getDescription(), true);
			}
		});
		tbLampOffTimer = (TooltipBox)v.findViewById(R.id.tb_device_detail_status_environment_lamp_off_timer);
		tbLampOffTimer.setDescription(mServerQueryMgr.getParameter(2001));
		tbLampOffTimer.setCloseButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPreferenceMgr.setDoNotShowTooltipBox(tbLampOffTimer.getDescription(), true);
			}
		});
		tbLampBrightnessButton = (TooltipBox)v.findViewById(R.id.tb_device_detail_status_environment_lamp_brightness_button);
		tbLampBrightnessButton.setDescription(mServerQueryMgr.getParameter(2002));
		tbLampBrightnessButton.setCloseButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPreferenceMgr.setDoNotShowTooltipBox(tbLampBrightnessButton.getDescription(), true);
			}
		});
	}

    public void setBrightLevel(int brightLevel) {
		btnLampOff.setSelected(false);
		btnLampOn1.setSelected(false);
		btnLampOn2.setSelected(false);
		btnLampOn3.setSelected(false);

		if (rctnStatusBackground != null) {
			switch(brightLevel) {
				case DeviceStatus.BRIGHT_ON2:
					rctnStatusBackground.setBackgroundResource(R.drawable.bg_lamp_bright_phase1);
					btnLampOn1.setSelected(true);
					break;
				case DeviceStatus.BRIGHT_ON4:
					rctnStatusBackground.setBackgroundResource(R.drawable.bg_lamp_bright_phase2);
					btnLampOn2.setSelected(true);
					break;
				case DeviceStatus.BRIGHT_ON5:
					rctnStatusBackground.setBackgroundResource(R.drawable.bg_lamp_bright_phase3);
					btnLampOn3.setSelected(true);
					break;
				case DeviceStatus.BRIGHT_OFF:
					rctnStatusBackground.setBackgroundResource(0);
					btnLampOff.setSelected(true);
					break;
			}
		}
	}

	private boolean isAvailableForLampSetting(String serial) {
		boolean lampSettingWhiteList = false;
		boolean lampSettingBlackList = false;
		for (String whitelist : WHITE_LIST_SERIAL_FOR_LAMP_SETTING) {
			if (serial != null && serial.contains(whitelist)) {
				lampSettingBlackList = false;
				for (String blacklist : BLACK_LIST_SERIAL_FOR_LAMP_SETTING) {
					if (serial.contains(blacklist)) {
						lampSettingBlackList = true;
						break;
					}
				}
				if (lampSettingBlackList == false) {
					lampSettingWhiteList = true;
					break;
				}
			}
		}
		//return lampSettingWhiteList;
		return true;
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

			tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
			tvStatusDescriptionExtra.setText("");

			if (Configuration.APP_MODE == Configuration.APP_KC_HUGGIES_X_MONIT) {
				if (isAvailableForLampRemoteSetting == true) {
					btnOpenLampSection.setEnabled(true);
				}
				if (mHub.getTemperatureStatus() == EnvironmentCheckManager.NORMAL
						&& mHub.getHumidityStatus() == EnvironmentCheckManager.NORMAL) {

					lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_transparent_kc);
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorPrimary));
					tvStatusDescription.setText(getString(R.string.device_environment_status_normal_detail));
					tvStatusDescriptionExtra.setText("");
				} else {
					if (mHub.getTemperatureStatus() == EnvironmentCheckManager.HIGH) {
						lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_warning_transparent_red_kc);
					} else if (mHub.getTemperatureStatus() == EnvironmentCheckManager.LOW) {
						lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_warning_transparent_blue_kc);
					} else if (mHub.getHumidityStatus() != EnvironmentCheckManager.NORMAL) {
						lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_warning_transparent_orange_kc);
					} else {
						lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_transparent_kc);
					}
				}
			} else {
				if (isAvailableForLampRemoteSetting == true) {
					btnOpenLampSection.setEnabled(true);
				}
				if (mHub.getVocStatus() == EnvironmentCheckManager.NORMAL
						&& mHub.getTemperatureStatus() == EnvironmentCheckManager.NORMAL
						&& mHub.getHumidityStatus() == EnvironmentCheckManager.NORMAL) {

					lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_transparent);
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
					tvStatusDescription.setText(getString(R.string.device_environment_status_normal_detail));
					tvStatusDescriptionExtra.setText("");
				} else {
					lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_connected_warning_transparent);
				}
			}

			if (mHub != null) {
				setBrightLevel(mHub.getBrightLevel());
			}
		} else {
			lctnStatusScore.setBackgroundResource(R.drawable.ic_device_detail_aqmhub_disconnected_transparent);

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

			tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
			tvStatusDescription.setText(getString(R.string.device_hub_disconnected_title));

			tvStatusDescriptionExtra.setText(getString(R.string.device_hub_disconnected_detail));

			setBrightLevel(DeviceStatus.BRIGHT_OFF);

			if (isAvailableForLampRemoteSetting == true) {
				btnOpenLampSection.setEnabled(false);
			}
		}
	}

	// Deprecated
    public void setEnvironmentValue(CurrentLampValue value) {
		try {
			if (isLoaded) {
				setTemperatureStatus(value.temperature);
				setHumidityStatus(value.humidity);
				//if (isSensorAttached) {
				setVocStatus(value.vocFromSensor);
				//}
			}
		} catch (IllegalStateException ex) {
			if (DBG) Log.e(TAG, "setSensorValue exception");
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
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				} else {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning);
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}

				switch(mDescriptionType) {
					case 0:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_high));
						break;
					case 1:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_high2));
						break;
					case 2:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_high3));
						break;
					case 3:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_high4));
						break;
					case 4:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_high5));
						break;
					default:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_high));
						break;
				}

				switch(mDescriptionExtraType) {
					case 0:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_high_action));
						break;
					case 1:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_high_action2));
						break;
					case 2:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_high_action3));
						break;
					case 3:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_high_action4));
						break;
					case 4:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_high_action5));
						break;
					default:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_high_action));
						break;
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
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarningBlue));
				} else {
					tvStatusItem1Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem1Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_warning);
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}

				switch(mDescriptionType) {
					case 0:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_low));
						break;
					case 1:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_low2));
						break;
					case 2:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_low3));
						break;
					case 3:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_low4));
						break;
					case 4:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_low5));
						break;
					default:
						tvStatusDescription.setText(getString(R.string.device_environment_temperature_low));
						break;
				}

				switch(mDescriptionExtraType) {
					case 0:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_low_action));
						break;
					case 1:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_low_action2));
						break;
					case 2:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_low_action3));
						break;
					case 3:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_low_action4));
						break;
					case 4:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_low_action5));
						break;
					default:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_temperature_low_action));
						break;
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
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
				} else {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning);
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}

				switch(mDescriptionType) {
					case 0:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_high));
						break;
					case 1:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_high2));
						break;
					case 2:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_high3));
						break;
					case 3:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_high4));
						break;
					case 4:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_high5));
						break;
					default:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_high));
						break;
				}

				switch(mDescriptionExtraType) {
					case 0:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_high_action));
						break;
					case 1:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_high_action2));
						break;
					case 2:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_high_action3));
						break;
					case 3:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_high_action4));
						break;
					case 4:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_high_action5));
						break;
					default:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_high_action));
						break;
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
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarningOrange));
				} else {
					tvStatusItem2Content.setTextColor(getResources().getColor(R.color.colorTextWarning));
					tvStatusItem2Title.setTextColor(getResources().getColor(R.color.colorTextWarning));
					ivStatusItem2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_warning);
					tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));
				}

				switch(mDescriptionType) {
					case 0:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_low));
						break;
					case 1:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_low2));
						break;
					case 2:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_low3));
						break;
					case 3:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_low4));
						break;
					case 4:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_low5));
						break;
					default:
						tvStatusDescription.setText(getString(R.string.device_environment_humidity_low));
						break;
				}

				switch(mDescriptionExtraType) {
					case 0:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_low_action));
						break;
					case 1:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_low_action2));
						break;
					case 2:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_low_action3));
						break;
					case 3:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_low_action4));
						break;
					case 4:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_low_action5));
						break;
					default:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_humidity_low_action));
						break;
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
				tvStatusDescription.setTextColor(getResources().getColor(R.color.colorTextWarning));

				switch(mDescriptionType) {
					case 0:
						tvStatusDescription.setText(getString(R.string.device_environment_voc_bad_detected));
						break;
					case 1:
						tvStatusDescription.setText(getString(R.string.device_environment_voc_bad_detected2));
						break;
					case 2:
						tvStatusDescription.setText(getString(R.string.device_environment_voc_bad_detected3));
						break;
					case 3:
						tvStatusDescription.setText(getString(R.string.device_environment_voc_bad_detected4));
						break;
					case 4:
						tvStatusDescription.setText(getString(R.string.device_environment_voc_bad_detected5));
						break;
					default:
						tvStatusDescription.setText(getString(R.string.device_environment_voc_bad_detected));
						break;
				}

				switch(mDescriptionExtraType) {
					case 0:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_voc_bad_action));
						break;
					case 1:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_voc_bad_action2));
						break;
					case 2:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_voc_bad_action3));
						break;
					case 3:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_voc_bad_action4));
						break;
					case 4:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_voc_bad_action5));
						break;
					default:
						tvStatusDescriptionExtra.setText(getString(R.string.device_environment_voc_bad_action));
						break;
				}

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
			ivStatusItem3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
			tvStatusItem3Title.setTextColor(getResources().getColor(R.color.colorTextEnvironmentCategory));
			tvStatusItem3Content.setVisibility(View.VISIBLE);
		}  else {
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

				setTemperatureStatus(mHub.getTemperature());
				setHumidityStatus(mHub.getHumidity());

				int attachedId = mHub.getSensorAttached();
				if (attachedId != DeviceStatus.SENSOR_DETACHED) {
					setSensorAttached(true);
					setVocStatus(mHub.getVoc());
					String sensorName = "";
					if (mPreferenceMgr != null) sensorName = mPreferenceMgr.getDeviceName(DeviceType.DIAPER_SENSOR, attachedId);
					tvWhereConnected.setText("Charging the sensor\n" + sensorName + "(" + attachedId + ")");
				} else {
					setSensorAttached(false);
					tvWhereConnected.setText("");
				}
			} else {
				setConnected(false);
			}

			if (isAvailableForLampRemoteSetting == true) {
				// Lamp Section 자동닫기
				if ((mOpenLampSettingSec > 0) && (System.currentTimeMillis() - mOpenLampSettingSec > CLOSE_LAMP_SETTING_INTERVAL_SEC * 1000)) {
					tvLampOffTimerRemainingTime.setVisibility(View.VISIBLE);
					lctnLampSection.setVisibility(View.GONE);
				}

				_updateLampOffTimerUI();
			}
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
		if (mHub.getBrightLevel() == DeviceStatus.BRIGHT_OFF) {
			// 수유등 Section이 켜져있는 상태라면,
			if (lctnLampSection.getVisibility() == View.VISIBLE) {
				if (tbLampOffTimer.getVisibility() == View.VISIBLE) {
					tbLampOffTimer.setVisibility(View.GONE);
				}

				if ((mPreferenceMgr.getDoNotShowTooltipBox(tbLampBrightnessButton.getDescription()) == false)
						&& (tbLampBrightnessButton.getVisibility() == View.GONE)) {
					tbLampBrightnessButton.setVisibility(View.VISIBLE);
				}
				// Timer가 동작하고 있던 상태라면,(TimePicker 숨겨진 상태)
				if (wtpTimer.getVisibility() == View.GONE) {
					wtpTimer.setVisibility(View.VISIBLE);
					tvLampSectionOffTimerRemainingTime.setText("00:00");
					tvLampSectionOffTimerRemainingTime.setVisibility(View.GONE);
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
			} else {
				if (tbLampBrightnessButton.getVisibility() == View.VISIBLE) {
					tbLampBrightnessButton.setVisibility(View.GONE);
				}
				if (tbLampOffTimer.getVisibility() == View.VISIBLE) {
					tbLampOffTimer.setVisibility(View.GONE);
				}

				tvLampOffTimerRemainingTime.setText("");
				tvLampOffTimerRemainingTime.setVisibility(View.GONE);
			}
		} else { // 허브 밝기가 켜진 상태라면,

			if (tbLampBrightnessButton.getVisibility() == View.VISIBLE) {
				tbLampBrightnessButton.setVisibility(View.GONE);
			}

			// 1. 타이머가 설정되어 있음
			if (lampOffTimerMs > 0 && diffMs > 0) {
				int hour, minute, second;
				diffMs = diffMs / 1000; // Minutes
				second = (int)(diffMs % 60);
				diffMs = diffMs / 60;
				minute = (int)(diffMs % 60); // +1을 더하는 이유는 59초 남으면 00:00 으로 표기됨
				hour = (int)(diffMs / 60);

				// 수유등 Section이 켜져있는 상태라면,
				if (lctnLampSection.getVisibility() == View.VISIBLE) {
					// 타이머가 설정되어 있으므로, TimePicker가 삭제되어야함
					if (wtpTimer.getVisibility() == View.VISIBLE) {
						wtpTimer.setVisibility(View.GONE);
						//wtpTimer.onCollapsed();
					}
					// 타이머 아이콘 보이기
					if (ivLampSectionTimerIcon.getVisibility() == View.GONE) {
						ivLampSectionTimerIcon.setVisibility(View.VISIBLE);
					}
					// 타이머 남은시간 보이기
					if (tvLampSectionOffTimerRemainingTime.getVisibility() == View.GONE) {
						tvLampSectionOffTimerRemainingTime.setVisibility(View.VISIBLE);
					}
					// 타이머 남은시간 업데이트
					if (tvLampSectionOffTimerRemainingTime.getText().toString().contains(":")) {
						tvLampSectionOffTimerRemainingTime.setText(String.format("%02d %02d", hour, minute));
					} else {
						tvLampSectionOffTimerRemainingTime.setText(String.format("%02d:%02d", hour, minute));
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
				} else { // 수유등 Section이 켜져있지 않다면
					// 타이머 시작버튼이 숨겨졌으면, 보이기
					if (tvLampOffTimerRemainingTime.getVisibility() == View.GONE && pbLampSection.getVisibility() == View.GONE) { // 3초간 대기상태에서는 보이지 않기
						tvLampOffTimerRemainingTime.setVisibility(View.VISIBLE);
					}
					// 타이머 남은시간 업데이트
					if (tvLampOffTimerRemainingTime.getText().toString().contains(":")) {
						tvLampOffTimerRemainingTime.setText(String.format("%02d %02d", hour, minute));
					} else {
						tvLampOffTimerRemainingTime.setText(String.format("%02d:%02d", hour, minute));
					}
				}
			} else { // 타이머가 설정되어 있지 않음
				// Timer 시간이 설정되어있다면, 0으로 설정
				//if (mPreferenceMgr.getLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId) > 0) {
				//	mPreferenceMgr.setLampOffTimerTargetMs(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId, 0);
				//}

				// 수유등 Section이 켜져있는 상태라면,
				if (lctnLampSection.getVisibility() == View.VISIBLE) {
					// 타이머 남은시간 숨기기
					if (tvLampSectionOffTimerRemainingTime.getVisibility() == View.VISIBLE) {
						tvLampSectionOffTimerRemainingTime.setVisibility(View.GONE);
					}
					// 타이머 아이콘 숨기기
					if (ivLampSectionTimerIcon.getVisibility() == View.VISIBLE) {
						ivLampSectionTimerIcon.setVisibility(View.GONE);
					}
					// TimePicker가 Disabled 되어 있으면 TimePicker가 움직이도록 설정
					if (wtpTimer.isEnabled() == false) {
						wtpTimer.onExpanded();
						wtpTimer.setTime(0, 0, 0);
						wtpTimer.setEnabled(true);
					}

					if ((mPreferenceMgr.getDoNotShowTooltipBox(tbLampOffTimer.getDescription()) == false)
							&& (tbLampOffTimer.getVisibility() == View.GONE)) {
						tbLampOffTimer.setVisibility(View.VISIBLE);
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
				} else {
					// 수유등 Section이 꺼져있는 상태라면,
					// 굳이 타이머 남은시간이 보일 필요가 없음
					tvLampOffTimerRemainingTime.setVisibility(View.GONE);

					if (tbLampBrightnessButton.getVisibility() == View.VISIBLE) {
						tbLampBrightnessButton.setVisibility(View.GONE);
					}
					if (tbLampOffTimer.getVisibility() == View.VISIBLE) {
						tbLampOffTimer.setVisibility(View.GONE);
					}
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

		Random r = new Random();
		mDescriptionType = r.nextInt(5);
		mDescriptionExtraType = r.nextInt(5);
		mMainActivity = getActivity();

		((DeviceEnvironmentActivity)mMainActivity).updateNewMark();
		mHub = ((DeviceEnvironmentActivity)mMainActivity).getAQMHubObject();
		if (mHub == null) {
			if (DBG) Log.e(TAG, "Object NULL");
			mMainActivity.finish();
			return;
		}

		String firmwareVersion = mHub.firmwareVersion;
		String serial = mPreferenceMgr.getDeviceSerialNumber(DeviceType.AIR_QUALITY_MONITORING_HUB, mHub.deviceId);
		//isAvailableForLampRemoteSetting = isAvailableForLampSetting(serial);

		if (isAvailableForLampRemoteSetting == true) {
			if ((mOpenLampSettingSec > 0) && (System.currentTimeMillis() - mOpenLampSettingSec > CLOSE_LAMP_SETTING_INTERVAL_SEC * 1000)) {
				tvLampOffTimerRemainingTime.setVisibility(View.GONE);
				lctnLampSection.setVisibility(View.GONE);
			} else {
				if (new VersionManager(mContext).supportLampSetting(firmwareVersion)) {
					if (btnOpenLampSection.getVisibility() == View.GONE) {
						btnOpenLampSection.setVisibility(View.VISIBLE);
					}
				}
			}

			if (mPreferenceMgr.getDoNotShowTooltipBox(tbLampSection.getDescription()) == false) {
				tbLampSection.setVisibility(View.VISIBLE);
			}
		}

		mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DBG) Log.i(TAG, "onDestroy");
	}
}