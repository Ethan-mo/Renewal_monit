package goodmonit.monit.com.kao.message;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;
import goodmonit.monit.com.kao.util.UnitConvertUtil;

public class NotificationMsgAdapter extends RecyclerViewAdapter {
    private static final String TAG = Configuration.BASE_TAG + "MsgAdapter";

    private int mTemperatureUnit = 0; // Celcius
    private String mStrTemperatureUnit; // Celcius

    public NotificationMsgAdapter(Context context) {
        super(context);
    }

    public void updatePreference() {
        mStrTemperatureUnit = PreferenceManager.getInstance(mContext).getTemperatureScale();
        if (mStrTemperatureUnit.equals(mContext.getString(R.string.unit_temperature_celsius))) {
            mTemperatureUnit = 0;
        } else {
            mTemperatureUnit = 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_notification_row, parent, false));
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_recyclerview_progressbar, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof NotificationViewHolder) {
            final NotificationViewHolder holder = (NotificationViewHolder)viewHolder;

            final NotificationMessage msg = mmList.get(position);
            int type = msg.notiType;

            if (type == NotificationType.CHAT_DATE_LINE) {
                holder.lctnMessage.setVisibility(View.GONE);
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvDate.setText(msg.extra);
                holder.vContinuousDivider.setVisibility(View.GONE);
                holder.vAboveDivider.setVisibility(View.VISIBLE);
                holder.vBelowDivider.setVisibility(View.VISIBLE);
                holder.ivNewMark.setVisibility(View.GONE);
                holder.btnRemove.setVisibility(View.GONE);
            } else {
                holder.lctnMessage.setVisibility(View.VISIBLE);
                holder.tvDate.setVisibility(View.GONE);
                holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(type));
                holder.tvTime.setText(DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, msg.timeMs));
                holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                if (mLatestCheckedNotificationIndex < msg.msgId) {
                    holder.ivNewMark.setVisibility(View.VISIBLE);
                } else {
                    holder.ivNewMark.setVisibility(View.GONE);
                }

                switch (type) {
                    case NotificationType.CHAT_USER_INPUT:
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextComment));
                        holder.tvDescription.setText(msg.extra);
                        break;
                    case NotificationType.CHAT_USER_FEEDBACK:
                        holder.tvDescription.setText(msg.extra);
                        break;
                    case NotificationType.HIGH_TEMPERATURE:
                    case NotificationType.LOW_TEMPERATURE:
                        holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextWarningBlue));
                        try {
                            float temp = Float.parseFloat(msg.extra);
                            if (mTemperatureUnit == 0) { // Celcius
                                temp = (int)(temp * 10) / 10.0f;
                            } else { // Fahrenheit
                                temp = UnitConvertUtil.getFahrenheitFromCelsius(temp);
                            }
                            holder.tvDescription.append(" (" + temp + mStrTemperatureUnit + ")");
                        } catch (NumberFormatException e) {
                        }
                        break;
                    case NotificationType.HIGH_HUMIDITY:
                    case NotificationType.LOW_HUMIDITY:
                        holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextWarningBlue));
                        try {
                            int humidity = (int)Float.parseFloat(msg.extra);
                            holder.tvDescription.append(" (" + humidity + "%)");
                        } catch (NumberFormatException e) {
                        }
                        break;
                    case NotificationType.VOC_WARNING:
                        holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextWarningBlue));
                        break;
                    case NotificationType.BABY_SLEEP:
                        holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_sleep);
                        if (msg.extra == null || msg.extra.equals("-")) {
                            holder.tvDescription.setText(mContext.getString(R.string.notification_sleep_start));
                        } else {
                            long sleepStartUtcTimeMs = msg.timeMs;
                            String sleepStartTimeString = DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, sleepStartUtcTimeMs);
                            long sleepEndUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(DateTimeUtil.getUtcTimeStampFromLocalString(msg.extra, "yyMMdd-HHmmss"));
                            String sleepEndTimeString = DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, sleepEndUtcTimeMs);

                            int totalElapsedMinute = (int)((sleepEndUtcTimeMs / 1000 - sleepStartUtcTimeMs / 1000) / 60);
                            int elapsedHour = totalElapsedMinute / 60;
                            int elapsedMinute = totalElapsedMinute % 60;
                            String elapsedTimeString = "";
                            if (elapsedHour == 0) {
                                elapsedTimeString = elapsedMinute + mContext.getResources().getString(R.string.time_elapsed_minute);
                            } else {
                                elapsedTimeString = elapsedHour + mContext.getResources().getString(R.string.time_elapsed_hour) + elapsedMinute + mContext.getResources().getString(R.string.time_elapsed_minute);
                            }
                            //holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextSleep));
                            holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                            holder.tvDescription.setText(mContext.getString(R.string.notification_sleep_end) + " (" + elapsedTimeString + ")");
                            holder.tvTime.setText(DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, msg.timeMs) + "~" + DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, sleepEndUtcTimeMs));
                        }
                        break;
                    case NotificationType.BABY_FEEDING_BABY_FOOD:
                    case NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK:
                    case NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK:
                    case NotificationType.BABY_FEEDING_NURSED_BREAST_MILK:
                        holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(type));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextFeeding));
                        if (msg.extra == null || msg.extra.equals("-")) {
                            holder.tvDescription.setText(mContext.getString(type));
                        } else {
                            if (type == NotificationType.BABY_FEEDING_BABY_FOOD) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + " g)");
                            } else if (type == NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + " ml)");
                            } else if (type == NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + " ml)");
                            } else if (type == NotificationType.BABY_FEEDING_NURSED_BREAST_MILK) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + " min)");
                            }
                        }
                        break;
                    case NotificationType.DIAPER_CHANGED:
                        String extraType = null;
                        if ("1".equals(msg.extra)) {
                            holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_clean);
                            extraType = mContext.getString(R.string.notification_diaper_changed);
                        } else if ("2".equals(msg.extra)) {
                            holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_pee);
                            extraType = mContext.getString(R.string.device_sensor_diaper_status_pee);
                        } else if ("3".equals(msg.extra)) {
                            holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_poo);
                            extraType = mContext.getString(R.string.device_sensor_diaper_status_poo);
                        } else if ("4".equals(msg.extra)) {
                            holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_mixed);
                            extraType = mContext.getString(R.string.device_sensor_diaper_status_mixed);
                        } else {
                            holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_clean);
                            extraType = mContext.getString(R.string.notification_diaper_changed);
                        }

                        //holder.ivIcon.setBackgroundResource(R.drawable.ic_notification_diaper_changed);
                        holder.tvDescription.setText(extraType);
                        //holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextDiaper));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                        break;

                    case NotificationType.PEE_DETECTED:
                        holder.tvDescription.setText(mContext.getString(R.string.notification_diaper_status_diaper_soiled_title) + "(" + mContext.getString(R.string.device_sensor_diaper_status_pee) + ")");
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                        break;
                    case NotificationType.POO_DETECTED:
                        holder.tvDescription.setText(mContext.getString(R.string.notification_diaper_status_diaper_soiled_title) + "(" + mContext.getString(R.string.device_sensor_diaper_status_poo) + ")");
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                        break;
                    default:
                        holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                        break;
                }
                holder.vAboveDivider.setVisibility(View.GONE);

                if (position == mmList.size() - 1) {
                    holder.vContinuousDivider.setVisibility(View.GONE);
                    holder.vBelowDivider.setVisibility(View.VISIBLE);
                } else {
                    holder.vContinuousDivider.setVisibility(View.VISIBLE);
                    holder.vBelowDivider.setVisibility(View.GONE);
                }

                holder.btnRemove.setVisibility(View.GONE);
                holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ServerQueryManager.getInstance(mContext).removeNotification(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        }
                                    }
                                });
                        msg.deleteDB(mContext);
                        remove(position);
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.btnRemove.getVisibility() == View.VISIBLE) {
                            holder.btnRemove.setVisibility(View.GONE);
                        } else {
                            holder.btnRemove.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
    }
}