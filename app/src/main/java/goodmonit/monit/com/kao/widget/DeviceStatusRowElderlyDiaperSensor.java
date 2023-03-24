package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceBLEConnection;
import goodmonit.monit.com.kao.devices.DeviceConnectionState;
import goodmonit.monit.com.kao.devices.DeviceElderlyDiaperSensor;
import goodmonit.monit.com.kao.devices.DeviceStatus;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.message.FeedbackMsgAdapter;
import goodmonit.monit.com.kao.message.NotificationMessage;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class DeviceStatusRowElderlyDiaperSensor extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "ElderlyRow";
	private static final boolean DBG = Configuration.DBG;

	private boolean isConnected = false;
	private int mDeviceConnectionType = DeviceConnectionState.DISCONNECTED;

	private int mStatus = DeviceStatus.OPERATION_IDLE;
	private int mMovement = DeviceStatus.MOVEMENT_NO_MOVEMENT;
	private int mDetectedStatus = DeviceStatus.DETECT_NONE;
	private int mAnimationIndex = 1;

	private Button btnType1, btnType2, btnType3, btnType4, btnType5, btnType6;

	public float[] currentMultiTouch = new float[9];
	public int strapBatteryPower;
	public boolean isStrapAttached;

	private ImageView[] ivMultiTouchCh;
	private TextView[] tvMultiTouchCh;
	private TextView tvDeviceEtc;

	private DeviceElderlyDiaperSensor mDevice;

	private SimpleDialog mConfirmDialog;
	private int mSelectedMode;

	// Feedback Notification
	private TextView tvElderlyNotificationTitle;
	private RecyclerView rvElderlyNotificationView;
	private FeedbackMsgAdapter msgAdapter;
	private long lastLoadedMessageTimeMs;
	private ArrayList<NotificationMessage> arrNotificationMsgList;

	public DeviceStatusRowElderlyDiaperSensor(Context context) {
		super(context);
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
		_initView();
		_setView();
	}

	public DeviceStatusRowElderlyDiaperSensor(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
		_initView();
		_setView();
	}

	public DeviceStatusRowElderlyDiaperSensor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mPreferenceMgr = PreferenceManager.getInstance(context);
		_initView();
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_elderly_device_status_row, this, false);
		addView(v);

		setBackgroundResource(R.drawable.bg_btn_white_darklight_selector);

		mThemeColor = getContext().getResources().getColor(R.color.colorTextLampCategory);
		mUnavailableColor = getContext().getResources().getColor(R.color.colorTextDeviceDisconnected);

		btnReconnect = (Button)v.findViewById(R.id.btn_device_status_reconnect);

		tvTitle = (TextView)v.findViewById(R.id.tv_device_status_title);
		tvDeviceName = (TextView)v.findViewById(R.id.tv_device_status_name);
		tvDescription = (TextView)v.findViewById(R.id.tv_device_status_description);

		lctnDeviceStatusDashboard = (LinearLayout)v.findViewById(R.id.lctn_device_status_dashboard);
		lctnDeviceStatusDashboard.setVisibility(View.GONE);

		ivDeviceIcon = (TextView)v.findViewById(R.id.tv_device_status_device_icon);
		ivDeviceIconBackground = (ImageView)v.findViewById(R.id.iv_device_status_device_icon_background);

		lctnDeviceStatusItem1 = (LinearLayout)v.findViewById(R.id.lctn_device_status_item1);
		tvStatus1Text = (TextView)v.findViewById(R.id.tv_device_status_text1);
		tvStatus1Text.setText("");
		tvStatus1TextUnit = (TextView)v.findViewById(R.id.tv_device_status_text1_unit);
		tvStatus1TextUnit.setText("");
		tvStatus1Content = (TextView)v.findViewById(R.id.tv_device_status_content1);
		tvStatus1Content.setText("");
		ivStatus1Icon = (ImageView)v.findViewById(R.id.iv_device_status_icon1);
		ivStatus1Icon.setBackgroundResource(0);
		ivStatus1New = (ImageView)v.findViewById(R.id.iv_device_status_icon1_new);

		lctnDeviceStatusItem2 = (LinearLayout)v.findViewById(R.id.lctn_device_status_item2);
		tvStatus2Text = (TextView)v.findViewById(R.id.tv_device_status_text2);
		tvStatus2Text.setText("");
		tvStatus2TextUnit = (TextView)v.findViewById(R.id.tv_device_status_text2_unit);
		tvStatus2TextUnit.setText("");
		tvStatus2Content = (TextView)v.findViewById(R.id.tv_device_status_content2);
		tvStatus2Content.setText("");
		ivStatus2Icon = (ImageView)v.findViewById(R.id.iv_device_status_icon2);
		ivStatus2Icon.setBackgroundResource(0);
		ivStatus2New = (ImageView)v.findViewById(R.id.iv_device_status_icon2_new);

		lctnDeviceStatusItem3 = (LinearLayout)v.findViewById(R.id.lctn_device_status_item3);
		tvStatus3Text = (TextView)v.findViewById(R.id.tv_device_status_text3);
		tvStatus3Text.setText("");
		tvStatus3TextUnit = (TextView)v.findViewById(R.id.tv_device_status_text3_unit);
		tvStatus3TextUnit.setText("");
		tvStatus3Content = (TextView)v.findViewById(R.id.tv_device_status_content3);
		tvStatus3Content.setText("");
		ivStatus3Icon = (ImageView)v.findViewById(R.id.iv_device_status_icon3);
		ivStatus3Icon.setBackgroundResource(0);
		ivStatus3New = (ImageView)v.findViewById(R.id.iv_device_status_icon3_new);

		ivConnectionType = (ImageView)v.findViewById(R.id.iv_device_status_connection_type);
		ivConnectionType.setBackgroundResource(0);

		lctnDeviceBatteryPower = (LinearLayout)v.findViewById(R.id.lctn_device_status_battery_power);

		ivBatteryPower = (ImageView)v.findViewById(R.id.iv_device_status_battery_power);
		ivBatteryPower.setBackgroundResource(0);

		tvBatteryPower = (TextView)v.findViewById(R.id.tv_device_status_battery_power);

		ivBatteryChargingPower = (ImageView)v.findViewById(R.id.iv_device_status_battery_charging_power);
		tvBatteryChargingPower = (TextView)v.findViewById(R.id.tv_device_status_battery_charging_power);

		ivNewMark = (ImageView)v.findViewById(R.id.iv_device_status_new_mark);

		rctnTitlebar = (RelativeLayout)v.findViewById(R.id.rctn_device_status_titlebar);
		vUnderlineOtherCategory = v.findViewById(R.id.v_device_status_underline_other_category);
		vUnderlineSameCategory = v.findViewById(R.id.v_device_status_underline_same_category);
		vUnderlineSameCategory.setVisibility(View.GONE);

		lctnDeviceMultiTouchDashboard = (LinearLayout)v.findViewById(R.id.lctn_device_status_multitouch_dashboard);
		ivMultiTouchCh = new ImageView[10];
		tvMultiTouchCh = new TextView[9];
		ivMultiTouchCh[0] = (ImageView)v.findViewById(R.id.iv_device_status_ch1);
		ivMultiTouchCh[1] = (ImageView)v.findViewById(R.id.iv_device_status_ch2);
		ivMultiTouchCh[2] = (ImageView)v.findViewById(R.id.iv_device_status_ch3);
		ivMultiTouchCh[3] = (ImageView)v.findViewById(R.id.iv_device_status_ch4);
		ivMultiTouchCh[4] = (ImageView)v.findViewById(R.id.iv_device_status_ch5);
		ivMultiTouchCh[5] = (ImageView)v.findViewById(R.id.iv_device_status_ch6);
		ivMultiTouchCh[6] = (ImageView)v.findViewById(R.id.iv_device_status_ch7);
		ivMultiTouchCh[7] = (ImageView)v.findViewById(R.id.iv_device_status_ch8);
		ivMultiTouchCh[8] = (ImageView)v.findViewById(R.id.iv_device_status_ch9);
		ivMultiTouchCh[9] = (ImageView)v.findViewById(R.id.iv_device_status_ch_prev);

		tvMultiTouchCh[0] = (TextView)v.findViewById(R.id.tv_device_status_ch1);
		tvMultiTouchCh[1] = (TextView)v.findViewById(R.id.tv_device_status_ch2);
		tvMultiTouchCh[2] = (TextView)v.findViewById(R.id.tv_device_status_ch3);
		tvMultiTouchCh[3] = (TextView)v.findViewById(R.id.tv_device_status_ch4);
		tvMultiTouchCh[4] = (TextView)v.findViewById(R.id.tv_device_status_ch5);
		tvMultiTouchCh[5] = (TextView)v.findViewById(R.id.tv_device_status_ch6);
		tvMultiTouchCh[6] = (TextView)v.findViewById(R.id.tv_device_status_ch7);
		tvMultiTouchCh[7] = (TextView)v.findViewById(R.id.tv_device_status_ch8);
		tvMultiTouchCh[8] = (TextView)v.findViewById(R.id.tv_device_status_ch9);

		rvElderlyNotificationView = (RecyclerView)v.findViewById(R.id.rv_elderly_notification_list);
		tvElderlyNotificationTitle = (TextView)v.findViewById(R.id.tv_elderly_notification_title);
		msgAdapter = new FeedbackMsgAdapter(mContext);
		rvElderlyNotificationView.setAdapter(msgAdapter);
		msgAdapter.setRecyclerView(rvElderlyNotificationView);
		lastLoadedMessageTimeMs = -1;
		arrNotificationMsgList = new ArrayList<>();
//
//		mConfirmDialog = new SimpleDialog(
//				mContext,
//				"기저귀 확인 입력",
//				"깨끗을 입력하셨습니다. 맞습니까?",
//				"취소",
//				new OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						mConfirmDialog.dismiss();
//					}
//				},
//				"확인",
//				new OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						setDiaperAlarm(mSelectedMode);
//						mConfirmDialog.dismiss();
//					}
//				});

        btnType1 = (Button)v.findViewById(R.id.btn_device_status_btn1);
        btnType1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				mSelectedMode = 100;
				Toast.makeText(mContext, mDeviceName + " 센서 정보 깨끗이 입력되었습니다.", Toast.LENGTH_LONG).show();
				mDevice.initTouchDetectedInfo();
				setDiaperAlarm(mSelectedMode);
            }
        });
        /*
        btnType2 = (Button)v.findViewById(R.id.btn_device_status_btn2);
        btnType2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				mSelectedMode = 101;
				Toast.makeText(mContext, mDeviceName + " 센서 정보 소변지림이 입력되었습니다.", Toast.LENGTH_LONG).show();
				mDevice.initTouchDetectedInfo();
				setDiaperAlarm(mSelectedMode);
            }
        });
        btnType3 = (Button)v.findViewById(R.id.btn_device_status_btn3);
        btnType3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				mSelectedMode = 102;
				Toast.makeText(mContext, mDeviceName + " 센서 정보 대변지림이 입력되었습니다.", Toast.LENGTH_LONG).show();
				mDevice.initTouchDetectedInfo();
				setDiaperAlarm(mSelectedMode);
            }
        });
        */
        btnType4 = (Button)v.findViewById(R.id.btn_device_status_btn4);
        btnType4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				mSelectedMode = 103;
				Toast.makeText(mContext, mDeviceName + " 센서 정보 소변이 입력되었습니다.", Toast.LENGTH_LONG).show();
				mDevice.initTouchDetectedInfo();
				setDiaperAlarm(mSelectedMode);
            }
        });
        btnType5 = (Button)v.findViewById(R.id.btn_device_status_btn5);
        btnType5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				mSelectedMode = 104;
				Toast.makeText(mContext, mDeviceName + " 센서 정보 대변이 입력되었습니다.", Toast.LENGTH_LONG).show();
				mDevice.initTouchDetectedInfo();
				setDiaperAlarm(mSelectedMode);
            }
        });

        btnType6 = (Button)v.findViewById(R.id.btn_device_status_btn6);
        btnType6.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(mContext, mDeviceName + " 감지이력이 초기화 되었습니다.", Toast.LENGTH_LONG).show();
				if (mDevice != null) {
					mDevice.initTouchDetectedInfo();
				}
			}
		});

		tvDeviceEtc = (TextView)v.findViewById(R.id.tv_device_status_multitouch_etc);
	}

	public RecyclerView getRecyclerView() {
		return rvElderlyNotificationView;
	}

	public FeedbackMsgAdapter getFeedbackMsgAdapter() {
		return msgAdapter;
	}

	public void setLastLoadedMessageTimeMs(long timeMs) {
		lastLoadedMessageTimeMs = timeMs;
	}

	public long getLastLoadedMessageTimeMs() {
		return lastLoadedMessageTimeMs;
	}

	public ArrayList<NotificationMessage> getNotificationMsgList() {
		return arrNotificationMsgList;
	}

	public void showFeedbackMessageList(boolean show) {
		if (show) {
			tvElderlyNotificationTitle.setVisibility(View.VISIBLE);
			rvElderlyNotificationView.setVisibility(View.VISIBLE);
		} else {
			tvElderlyNotificationTitle.setVisibility(View.GONE);
			rvElderlyNotificationView.setVisibility(View.GONE);
		}
	}

	public void setNotificationMsgList(ArrayList<NotificationMessage> msgList) {
		arrNotificationMsgList = msgList;
	}

	public void setNotificationListOnUpdateView(FeedbackMsgAdapter.OnUpdateViewListener listener) {
		if (msgAdapter != null) {
			msgAdapter.setOnUpdateViewListener(listener);
		}
	}

	private void setDiaperAlarm(int diaperStatus) {

	    long now = System.currentTimeMillis();

        final long selectedDateTimeMs = now;
        final int selectedMode = diaperStatus;
        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode);
        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));

        ServerQueryManager.getInstance(mContext).setDiaperChanged(
                mDeviceType,
                mDeviceId,
                mDeviceEnc,
                selectedDateTimeMs,
                selectedMode + "",
                new ServerManager.ServerResponseListener() {
                    @Override
                    public void onReceive(int responseCode, String errCode, String data) {
                        long utcRespTimeSec = 0;
                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                            String time = ServerManager.getStringFromJSONObj(data, ServerQueryManager.getInstance(mContext).getParameter(15));
                            if (time != null && time.length() > 0) {
                                if (time.contains("-")) { // YYMMDD-HHMMSS 형식
                                    try {
                                        Date date = new SimpleDateFormat(ServerQueryManager.getInstance(mContext).getParameter(1)).parse(time);
                                        // date.getTime() 은 Local시간을 UTC로 변경하므로 다시 +9 해야함
                                        utcRespTimeSec = DateTimeUtil.convertUTCToLocalTimeMs(date.getTime()) / 1000;
                                    } catch (ParseException e) {
                                    }
                                } else { // Timestamp형식
                                    try {
                                        utcRespTimeSec = Long.parseLong(time);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }

                        if (utcRespTimeSec <= 0) utcRespTimeSec = selectedDateTimeMs / 1000;
                        String utcTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(utcRespTimeSec * 1000);
                        if (DBG) Log.d(TAG, "utc datetime string: " + utcTimeString);
                        ConnectionManager.getInstance().getNotificationFromCloudV2(mDeviceType, mDeviceId);
                    }
                });
    }

	private void _setView() {
		isConnected = true;
		setConnected(false);
		setOperationStatus(DeviceStatus.OPERATION_SENSING);
		setBatteryStatus(100, false);
		setMovementStatus(DeviceStatus.MOVEMENT_NO_MOVEMENT, false);
		setDiaperStatus(DeviceStatus.DETECT_NONE);

		tvTitle.setText(getContext().getString(R.string.device_type_elderly_diaper_sensor));
		tvStatus1Text.setText("");
		tvStatus2Text.setText("");
		tvStatus3Text.setText("");

		tvStatus1Content.setText(getContext().getString(R.string.device_sensor_diaper_status));
		tvStatus2Content.setText(getContext().getString(R.string.device_sensor_voc));
		tvStatus3Content.setText(getContext().getString(R.string.device_sensor_movement));

		tvDescription.setText(getContext().getString(R.string.device_sensor_disconnected));
		btnReconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDeviceId > 0) {
					DeviceBLEConnection bleConnection = ConnectionManager.getDeviceBLEConnection(mDeviceId, DeviceType.DIAPER_SENSOR);
					if (bleConnection != null) {
						bleConnection.requestForceLeScan();
						bleConnection.connect();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								btnReconnect.setText(mContext.getString(R.string.btn_connect));
							}
						}, 3000);
						btnReconnect.setText(mContext.getString(R.string.btn_connecting));
					} else {
						if (DBG) Log.e(TAG, "bleConnection NULL");
					}
				}
			}
		});
	}

	private float mTemperature, mHumidity, mVoc;
	private int currentTouchDetectedCount, mContamination;
	private long latestTouchDetectedTimeMs;
	private boolean needToChangeScale = false;
	public void setTemperature(float temperature) {
		if (temperature > 100) {
			needToChangeScale = true;
			mTemperature = temperature / 100.0f;
		} else {
			needToChangeScale = false;
			mTemperature = temperature;
		}
		tvStatus1Content.setText(temperature + "℃");
	}

	public void setHumidity(float humidity) {
		if (needToChangeScale == true) {
			mHumidity = humidity / 100.0f;
		} else {
			mHumidity = humidity;
		}
		tvStatus2Content.setText(humidity + "%");
	}

	public void setVoc(float voc) {
		if (needToChangeScale == true) {
			mVoc = voc / 100.0f;
		} else {
			mVoc = voc;
		}

		int elapsedTimeMin = (int)(System.currentTimeMillis() - latestTouchDetectedTimeMs) / 1000 / 60;
		if (currentTouchDetectedCount == 0) {
			tvDeviceEtc.setText("[T]" + mTemperature + "℃, [H]" + mHumidity + "%, [V]" + mVoc + ", [B]" + strapBatteryPower + "%" + "\n[오염도]" + mContamination + "%" + ", [감지횟수] 이력없음");
		} else {
			tvDeviceEtc.setText("[T]" + mTemperature + "℃, [H]" + mHumidity + "%, [V]" + mVoc + ", [B] " + strapBatteryPower + "%" + "\n[오염도]" + mContamination + "%" + ", [감지횟수]" + currentTouchDetectedCount + ", [최근감지시간]" + elapsedTimeMin + "분전");
		}

		tvStatus3Content.setText(voc + "");
	}

	public void setContamination(int contamination) {
		mContamination = contamination;
	}

	public void setTouchDetectedInfo(int count, long latestDetectedTimeMs) {
		currentTouchDetectedCount = count;
		latestTouchDetectedTimeMs = latestDetectedTimeMs;
	}

	public void setConnectionType(int deviceConnectionState) {
		if (mDeviceConnectionType != deviceConnectionState) {
			mDeviceConnectionType = deviceConnectionState;
			switch (deviceConnectionState) {
				case DeviceConnectionState.DISCONNECTED:
					ivConnectionType.setBackgroundResource(0);
					break;
				case DeviceConnectionState.WIFI_CONNECTED:
					ivConnectionType.setBackgroundResource(R.drawable.device_connection_wifi);
					break;
				case DeviceConnectionState.BLE_CONNECTED:
					ivConnectionType.setBackgroundResource(R.drawable.device_connection_bluetooth);
					break;
			}
		}
	}

	public void showNewMark(boolean show) {
		if (show) {
			ivNewMark.setVisibility(View.VISIBLE);
		} else {
			ivNewMark.setVisibility(View.GONE);
		}
	}

	public void showAlarmMark(boolean show) {
		if (show) {
			ivStatus1New.setVisibility(View.VISIBLE);
		} else {
			ivStatus1New.setVisibility(View.GONE);
		}
	}

	public void setConnected(boolean connected) {
		if (isConnected == connected) return;

		if (DBG) Log.d(TAG, "setConnected : " + connected);
		isConnected = connected;
		if (isConnected) {
			ivConnectionType.setVisibility(View.VISIBLE);
			//lctnDeviceStatusDashboard.setVisibility(View.VISIBLE);
            lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
			tvDescription.setVisibility(View.GONE);
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor_connected);
			if (Configuration.CERTIFICATE_MODE) {
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_environment_temperature_activated);
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_environment_humidity_activated);
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_environment_voc_activated);
			} else {
				ivStatus1Icon.setBackgroundResource(0);
				ivStatus2Icon.setBackgroundResource(0);
				ivStatus3Icon.setBackgroundResource(0);
			}
			setBatteryStatus(100, false);
			btnReconnect.setVisibility(View.GONE);
		} else {
			ivConnectionType.setVisibility(View.GONE);
			setConnectionType(DeviceConnectionState.DISCONNECTED);
			tvTitle.setText(getContext().getString(R.string.device_sensing_diaper));
			ivDeviceIcon.setBackgroundResource(R.drawable.ic_device_sensor_disconnected);
			lctnDeviceStatusDashboard.setVisibility(View.GONE);
            lctnDeviceBatteryPower.setVisibility(View.GONE);
			lctnDeviceMultiTouchDashboard.setVisibility(View.GONE);
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
			tvDescription.setText(mContext.getString(R.string.device_sensor_disconnected));
			btnReconnect.setVisibility(View.VISIBLE);
			ivNewMark.setVisibility(View.GONE);
			ivStatus1New.setVisibility(View.GONE);
			ivStatus2New.setVisibility(View.GONE);
			ivStatus3New.setVisibility(View.GONE);
			lctnDeviceBatteryPower.setVisibility(View.GONE);
			ivBatteryChargingPower.setVisibility(View.GONE);
			tvBatteryChargingPower.setVisibility(View.GONE);
		}
	}

	public void setBatteryStatus(int batteryPower, boolean isCharging) {
        if (isCharging) {
            if (batteryPower == 100) {
                tvDescription.setText(mContext.getString(R.string.device_sensor_diaper_status_charged_detail));
                ivBatteryChargingPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charged_row);
            } else {
                tvDescription.setText(mContext.getString(R.string.device_sensor_diaper_status_charging_detail));
                ivBatteryChargingPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_charging_row);
            }
            tvBatteryChargingPower.setText(batteryPower + "%");
			lctnDeviceBatteryPower.setVisibility(View.GONE);
        } else {
        	if (isConnected) {
				lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
			}
            tvBatteryPower.setText(batteryPower + "");
            if (batteryPower >= 96) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_100_row);
            } else if (batteryPower >= 90) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_9x_row);
            } else if (batteryPower >= 80) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_8x_row);
            } else if (batteryPower >= 70) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_7x_row);
            } else if (batteryPower >= 60) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_6x_row);
            } else if (batteryPower >= 50) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_5x_row);
            } else if (batteryPower >= 40) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_4x_row);
            } else if (batteryPower >= 30) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_3x_row);
            } else if (batteryPower >= 20) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_2x_row);
            } else if (batteryPower > 0) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_1x_row);
            } else if (batteryPower == 0) {
                ivBatteryPower.setBackgroundResource(R.drawable.ic_sensor_diaper_battery_0_row);
            }
        }
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setOperationStatus(int status) {
		mStatus = status;

		if (isConnected) {
			switch (status) {
				case DeviceStatus.OPERATION_IDLE:
					lctnDeviceStatusDashboard.setVisibility(View.GONE);
					lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
					lctnDeviceMultiTouchDashboard.setVisibility(View.GONE);
					ivBatteryChargingPower.setVisibility(View.GONE);
					tvBatteryChargingPower.setVisibility(View.GONE);
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					tvDescription.setText(R.string.device_sensor_diaper_status_idle_detail);
					break;
				case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
				case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
				case DeviceStatus.OPERATION_CABLE_CHARGING:
				case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				case DeviceStatus.OPERATION_HUB_CHARGING:
					lctnDeviceStatusDashboard.setVisibility(View.GONE);
					lctnDeviceBatteryPower.setVisibility(View.VISIBLE);
					lctnDeviceMultiTouchDashboard.setVisibility(View.GONE);
					ivBatteryChargingPower.setVisibility(View.VISIBLE);
					tvBatteryChargingPower.setVisibility(View.VISIBLE);
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setText("센서가 충전중입니다.");
					tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					break;
				case DeviceStatus.OPERATION_GAS_DETECTED:
				case DeviceStatus.OPERATION_AVOID_SENSING:
				case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
				case DeviceStatus.OPERATION_DEBUG_CHARGING:
				case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
				case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
				case DeviceStatus.OPERATION_SENSING:
				default:
					//lctnDeviceStatusDashboard.setVisibility(View.VISIBLE);
					lctnDeviceBatteryPower.setVisibility(View.GONE);
					ivBatteryChargingPower.setVisibility(View.GONE);
					tvBatteryChargingPower.setVisibility(View.GONE);
					tvDescription.setVisibility(View.GONE);
					break;
			}
		}
	}

	public void setVocStatus(float vocValue) {
		if (DBG) Log.d(TAG, "setVocStatus: " + vocValue);
		switch (mStatus) {
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				ivStatus2Icon.setBackgroundResource(R.drawable.ic_sensor_voc_deactivated);
				tvStatus2Content.setText("-");
				break;
			case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
			case DeviceStatus.OPERATION_DEBUG_CHARGING:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
			case DeviceStatus.OPERATION_GAS_DETECTED:
			case DeviceStatus.OPERATION_AVOID_SENSING:
			case DeviceStatus.OPERATION_SENSING:
			case DeviceStatus.OPERATION_IDLE:
				ivStatus2Icon.setBackgroundResource(DeviceStatus.getDiaperSensorVocIconResource(vocValue));
				tvStatus2Content.setText(getResources().getString(DeviceStatus.getDiaperSensorVocStringResource(vocValue)));
				tvStatus2Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperSensorVocStringColorResource(vocValue)));
				break;
		}
	}


	public void setMultiTouch(float[] touch) {
		String strStrapValues = "";
		for (int i = 0; i < touch.length; i++) {
			strStrapValues += (int)(touch[i]) + ", ";
		}

		if (DBG) Log.d(TAG, "setMultiTouch: " + strStrapValues);

		if (touch != null) {
			for (int i = 0; i < touch.length; i++) {
				currentMultiTouch[i] = touch[i];
			}
		}

		int currLevel = 1;
		int nextLevel = 2;

		if (currentMultiTouch[0] > DeviceStatus.THRESHOLD_TOUCH_LEVEL4) {
			ivMultiTouchCh[9].setBackgroundResource(R.drawable.elderly_gradient_lv4lv4);
		} else if (currentMultiTouch[0] > DeviceStatus.THRESHOLD_TOUCH_LEVEL3) {
			ivMultiTouchCh[9].setBackgroundResource(R.drawable.elderly_gradient_lv3lv3);
		} else if (currentMultiTouch[0] > DeviceStatus.THRESHOLD_TOUCH_LEVEL2) {
			ivMultiTouchCh[9].setBackgroundResource(R.drawable.elderly_gradient_lv2lv2);
		} else {
			ivMultiTouchCh[9].setBackgroundResource(R.drawable.elderly_gradient_lv1lv1);
		}

		if (currentMultiTouch[8] > DeviceStatus.THRESHOLD_TOUCH_LEVEL4) {
			ivMultiTouchCh[8].setBackgroundResource(R.drawable.elderly_gradient_lv4lv4);
		} else if (currentMultiTouch[8] > DeviceStatus.THRESHOLD_TOUCH_LEVEL3) {
			ivMultiTouchCh[8].setBackgroundResource(R.drawable.elderly_gradient_lv3lv3);
		} else if (currentMultiTouch[8] > DeviceStatus.THRESHOLD_TOUCH_LEVEL2) {
			ivMultiTouchCh[8].setBackgroundResource(R.drawable.elderly_gradient_lv2lv2);
		} else {
			ivMultiTouchCh[8].setBackgroundResource(R.drawable.elderly_gradient_lv1lv1);
		}
		tvMultiTouchCh[8].setText(((int)currentMultiTouch[8]) + "");

		for (int i = 0; i < 8; i++) {
			if (currentMultiTouch[i] > DeviceStatus.THRESHOLD_TOUCH_LEVEL4) {
				currLevel = 4;
			} else if (currentMultiTouch[i] > DeviceStatus.THRESHOLD_TOUCH_LEVEL3) {
				currLevel = 3;
			} else if (currentMultiTouch[i] > DeviceStatus.THRESHOLD_TOUCH_LEVEL2) {
				currLevel = 2;
			} else {
				currLevel = 1;
			}

			if (currentMultiTouch[i + 1] > DeviceStatus.THRESHOLD_TOUCH_LEVEL4) {
				nextLevel = 4;
			} else if (currentMultiTouch[i + 1] > DeviceStatus.THRESHOLD_TOUCH_LEVEL3) {
				nextLevel = 3;
			} else if (currentMultiTouch[i + 1] > DeviceStatus.THRESHOLD_TOUCH_LEVEL2) {
				nextLevel = 2;
			} else {
				nextLevel = 1;
			}

			if (currLevel == 1) {
				switch(nextLevel) {
					case 1:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv1lv1);
						break;
					case 2:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv1lv2);
						break;
					case 3:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv1lv3);
						break;
					case 4:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv1lv4);
						break;
				}
			} else if (currLevel == 2) {
				switch(nextLevel) {
					case 1:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv2lv1);
						break;
					case 2:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv2lv2);
						break;
					case 3:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv2lv3);
						break;
					case 4:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv2lv4);
						break;
				}
			} else if (currLevel == 3) {
				switch(nextLevel) {
					case 1:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv3lv1);
						break;
					case 2:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv3lv2);
						break;
					case 3:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv3lv3);
						break;
					case 4:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv3lv4);
						break;
				}
			} else if (currLevel == 4) {
				switch(nextLevel) {
					case 1:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv4lv1);
						break;
					case 2:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv4lv2);
						break;
					case 3:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv4lv3);
						break;
					case 4:
						ivMultiTouchCh[i].setBackgroundResource(R.drawable.elderly_gradient_lv4lv4);
						break;
				}
			}
			tvMultiTouchCh[i].setText(((int)currentMultiTouch[i]) + "");
		}

	}

	public void setStrapAttached(boolean attached) {
		if (DBG) Log.d(TAG, "setStrapAttached: " + attached);
		isStrapAttached = attached;

		if (mStatus >= DeviceStatus.OPERATION_STRAP_CONNECTED) {
			isStrapAttached = true;
		} else {
			isStrapAttached = false;
		}

		if (isStrapAttached) {
			switch (mStatus) {
                case DeviceStatus.OPERATION_IDLE:
                    lctnDeviceMultiTouchDashboard.setVisibility(View.GONE);
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                    tvDescription.setText("기저귀 부착이 감지되지 않아 센서가 대기중입니다.\n센서를 기저귀에 다시 부착하시면 30초 이내로 모니터링이 활성화됩니다.");
                    break;
                default:
                    lctnDeviceMultiTouchDashboard.setVisibility(View.VISIBLE);
                    tvDescription.setVisibility(View.GONE);
                    break;
            }
		} else {
			lctnDeviceMultiTouchDashboard.setVisibility(View.GONE);
			switch (mStatus) {
				case DeviceStatus.OPERATION_IDLE:
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					tvDescription.setText(R.string.device_sensor_diaper_status_idle_detail);
					break;
				case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
				case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
				case DeviceStatus.OPERATION_CABLE_CHARGING:
				case DeviceStatus.OPERATION_HUB_NO_CHARGE:
				case DeviceStatus.OPERATION_HUB_CHARGING:
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setText("센서가 충전중입니다.");
					tvDescription.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					break;
				case DeviceStatus.OPERATION_GAS_DETECTED:
				case DeviceStatus.OPERATION_AVOID_SENSING:
				case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
				case DeviceStatus.OPERATION_DEBUG_CHARGING:
				case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
				case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
				case DeviceStatus.OPERATION_SENSING:
                default:
					tvDescription.setVisibility(View.VISIBLE);
					tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimaryDark));
					tvDescription.setText("스트랩이 연결되지 않았습니다. 스트랩을 연결해주세요.");
					break;
			}
		}
	}

	public void setStrapBatteryPower(int power) {
		if (power != -1) {
			if (power > 100) {
				strapBatteryPower = power / 100;
			} else {
				strapBatteryPower = power;
			}
		}
	}

	public void setMovementStatus(final int movement, final boolean isSleeping) {
		mMovement = movement;

		switch (mStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
				tvStatus3Content.setText("-");
				break;
			case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
			case DeviceStatus.OPERATION_DEBUG_CHARGING:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
			case DeviceStatus.OPERATION_GAS_DETECTED:
			case DeviceStatus.OPERATION_AVOID_SENSING:
			case DeviceStatus.OPERATION_SENSING:
			case DeviceStatus.OPERATION_IDLE:
				ivStatus3Icon.setBackgroundResource(R.drawable.ic_sensor_movement_activated);
				tvStatus3Content.setTextColor(getResources().getColor(R.color.colorTextPrimary));
				if (isSleeping) {
					tvStatus3Content.setText(getContext().getString(DeviceStatus.getMovementStringResource(DeviceStatus.MOVEMENT_SLEEP)));
				} else {
					tvStatus3Content.setText(getContext().getString(DeviceStatus.getMovementStringResource(movement)));
				}
				break;

		}
	}

	public void setDiaperScore(int score) {
		switch (mStatus) {
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_movement_deactivated);
				tvStatus1Content.setText("-");
				break;
			case DeviceStatus.OPERATION_DEBUG_NO_CHARGE:
			case DeviceStatus.OPERATION_DEBUG_CHARGING:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_FULLY:
			case DeviceStatus.OPERATION_DEBUG_CHARGED_ERROR:
			case DeviceStatus.OPERATION_GAS_DETECTED:
			case DeviceStatus.OPERATION_AVOID_SENSING:
			case DeviceStatus.OPERATION_SENSING:
			case DeviceStatus.OPERATION_IDLE:
				ivStatus1Icon.setBackgroundResource(DeviceStatus.getDiaperScoreIconResource(score));
				tvStatus1Content.setText(mContext.getString(DeviceStatus.getDiaperScoreStringResource(score)));
				tvStatus1Content.setTextColor(getResources().getColor(DeviceStatus.getDiaperScoreColorResource(score)));
				break;
		}
	}

	public void setDiaperStatus(int detectedStatus) {
		mDetectedStatus = detectedStatus;
		switch (mStatus) {
			case DeviceStatus.OPERATION_IDLE:
			case DeviceStatus.OPERATION_CABLE_NO_CHARGE:
			case DeviceStatus.OPERATION_CABLE_CHARGING:
			case DeviceStatus.OPERATION_CABLE_CHARGED_FULLY:
			case DeviceStatus.OPERATION_HUB_NO_CHARGE:
			case DeviceStatus.OPERATION_HUB_CHARGING:
			case DeviceStatus.OPERATION_HUB_CHARGED_FULLY:
				return;
		}
		switch (detectedStatus) {
			case DeviceStatus.DETECT_ABNORMAL:
				ivStatus1Icon.setBackgroundResource(R.drawable.ic_sensor_diaper_activated_phase2);
				tvStatus1Content.setText(mContext.getString(DeviceStatus.getDiaperScoreStringResource(0)));
				break;
		}
	}

	protected Context mContext;
	protected int mThemeColor, mUnavailableColor;
	protected TextView tvTitle;
	protected TextView tvDeviceName;
	protected TextView ivDeviceIcon;
	protected TextView tvDescription;
	protected LinearLayout lctnDeviceStatusDashboard, lctnDeviceStatusItem1, lctnDeviceStatusItem2, lctnDeviceStatusItem3, lctnDeviceBatteryPower, lctnDeviceMultiTouchDashboard;
	protected RelativeLayout rctnTitlebar;
	protected ImageView ivDeviceIconBackground;
	protected ImageView ivStatus1New, ivStatus2New, ivStatus3New;
	protected TextView tvStatus1Text, tvStatus2Text, tvStatus3Text;
	protected TextView tvStatus1TextUnit, tvStatus2TextUnit, tvStatus3TextUnit;
	protected ImageView ivStatus1Icon, ivStatus2Icon, ivStatus3Icon;
	protected ImageView ivConnectionType, ivNewMark, ivBatteryPower, ivBatteryChargingPower;
	protected TextView tvStatus1Content, tvStatus2Content, tvStatus3Content;
	protected String mDeviceName;
	protected long mDeviceId;
	protected int mDeviceType;
	protected String mDeviceEnc;
	protected PreferenceManager mPreferenceMgr;
	protected View vUnderlineOtherCategory, vUnderlineSameCategory;
	protected Button btnReconnect;
	protected TextView tvBatteryPower, tvBatteryChargingPower;

	public void setTitle(String title) {
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}

	public void setDeviceIcon(int resId) {
		if (ivDeviceIcon != null) {
			ivDeviceIcon.setBackgroundResource(resId);
		}
	}

	public void setDeviceName(String name) {
		mDeviceName = name;
		if (tvDeviceName != null) {
			tvDeviceName.setText(name);
		}
	}

	public void setDescription(String description) {
		if (tvDescription != null) {
			tvDescription.setText(description);
		}
	}

	public void showDescription(boolean show) {
		if (tvDescription != null) {
			if (show) {
				tvDescription.setVisibility(View.VISIBLE);
			} else {
				tvDescription.setVisibility(View.GONE);
			}
		}
	}

	public void setStatus1Text(String text) {
		if (tvStatus1Text != null) {
			tvStatus1Text.setText(text);
		}
	}

	public void setStatus2Text(String text) {
		if (tvStatus2Text != null) {
			tvStatus2Text.setText(text);
		}
	}

	public void setStatus3Text(String text) {
		if (tvStatus3Text != null) {
			tvStatus3Text.setText(text);
		}
	}

	public void setStatus1Icon(int resId) {
		if (ivStatus1Icon != null) {
			ivStatus1Icon.setBackgroundResource(resId);
		}
	}

	public void setStatus2Icon(int resId) {
		if (ivStatus2Icon != null) {
			ivStatus2Icon.setBackgroundResource(resId);
		}
	}

	public void setStatus3Icon(int resId) {
		if (ivStatus3Icon != null) {
			ivStatus3Icon.setBackgroundResource(resId);
		}
	}

	public void setStatus1Content(String text) {
		if (tvStatus1Content != null) {
			tvStatus1Content.setText(text);
		}
	}

	public void setStatus2Content(String text) {
		if (tvStatus2Content != null) {
			tvStatus2Content.setText(text);
		}
	}

	public void setStatus3Content(String text) {
		if (tvStatus3Content != null) {
			tvStatus3Content.setText(text);
		}
	}

	public void setDeviceId(long deviceId) {
		mDeviceId = deviceId;
		mDevice = ConnectionManager.getDeviceElderlyDiaperSensor(deviceId);
	}

	public long getDeviceId() {
		return mDeviceId;
	}

    public void setDeviceType(int deviceType) {
        mDeviceType = deviceType;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceEnc(String deviceEnc) {
        mDeviceEnc = deviceEnc;
    }

    public String getDeviceEnc() {
        return mDeviceEnc;
    }

	public String getDeviceName() {
		return mDeviceName;
	}

	public void showTitlebar(boolean show) {
		if (show) {
			rctnTitlebar.setVisibility(View.VISIBLE);
		} else {
			rctnTitlebar.setVisibility(View.GONE);
		}
	}

	public void setOtherCategory(boolean other) {
		if (other) {
			vUnderlineSameCategory.setVisibility(View.GONE);
			vUnderlineOtherCategory.setVisibility(View.VISIBLE);
		} else {
			vUnderlineSameCategory.setVisibility(View.VISIBLE);
			vUnderlineOtherCategory.setVisibility(View.GONE);
		}
	}

	public void setDeviceStatusItem3Visible(boolean visible) {
		if (lctnDeviceStatusItem3 != null) {
			if (visible) {
				lctnDeviceStatusItem3.setVisibility(View.VISIBLE);
			} else {
				lctnDeviceStatusItem3.setVisibility(View.GONE);
			}
		}
	}
}