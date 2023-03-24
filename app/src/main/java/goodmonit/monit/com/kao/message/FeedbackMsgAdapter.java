package goodmonit.monit.com.kao.message;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.dialog.DiaperInputDialog;
import goodmonit.monit.com.kao.dialog.FeedingInputDialog;
import goodmonit.monit.com.kao.dialog.SleepInputDialog;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class FeedbackMsgAdapter extends RecyclerViewAdapter {
    private static final String TAG = Configuration.BASE_TAG + "FeedbackAdapter";
    private static final boolean DBG = Configuration.DBG;

    public Context mContext;
    public FeedingInputDialog mDlgFeedingModify;
    public SleepInputDialog mDlgSleepingModify;
    public DiaperInputDialog mDlgDiaperModify;

    protected OnUpdateViewListener mOnUpdateViewListener;
    public interface OnUpdateViewListener{
        void onUpdate();
    }

    public FeedbackMsgAdapter(Context context) {
        super(context);
        mContext = context;

        mDlgFeedingModify = new FeedingInputDialog(mContext,
                mContext.getString(R.string.dialog_sensor_feeding_input),
                mContext.getString(R.string.dialog_sensor_feeding_record),
                mContext.getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgFeedingModify.dismiss();
                    }
                },
                mContext.getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedDateTimeMs = mDlgFeedingModify.getDateTimeUtcMs();
                        int selectedMode = mDlgFeedingModify.getSelectedMode(); // 1: 모유, 2: 유축, 3: 분유, 4: 이유식
                        String extraValue = mDlgFeedingModify.getExtraValue();

                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode + " / " + extraValue);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));

                        mDlgFeedingModify.dismiss();

                        final NotificationMessage msg = mDlgFeedingModify.getNotificationData();
                        switch(selectedMode) {
                            case 1:
                                msg.notiType = NotificationType.BABY_FEEDING_NURSED_BREAST_MILK;
                                break;
                            case 2:
                                msg.notiType = NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK;
                                break;
                            case 3:
                                msg.notiType = NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK;
                                break;
                            case 4:
                                msg.notiType = NotificationType.BABY_FEEDING_BABY_FOOD;
                                break;
                        }

                        msg.extra = extraValue;
                        msg.timeMs = selectedDateTimeMs;

                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });

        mDlgSleepingModify = new SleepInputDialog(mContext,
                mContext.getString(R.string.dialog_sensor_sleep_input),
                mContext.getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgSleepingModify.dismiss();
                    }
                },
                mContext.getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedStartDateTimeMs = mDlgSleepingModify.getStartDateTimeUtcMs();
                        long selectedEndDateTimeMs = mDlgSleepingModify.getEndDateTimeUtcMs();
                        String endTimeString = DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedEndDateTimeMs);
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedStartDateTimeMs + " / " + selectedEndDateTimeMs);

                        mDlgSleepingModify.dismiss();

                        final NotificationMessage msg = mDlgSleepingModify.getNotificationData();
                        msg.extra = endTimeString;
                        msg.timeMs = selectedStartDateTimeMs;
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }
                                });


                    }
                });

        mDlgDiaperModify = new DiaperInputDialog(mContext,
                mContext.getString(R.string.dialog_sensor_diaper_changed_date_time),
                mContext.getString(R.string.dialog_sensor_diaper_change_record),
                mContext.getString(R.string.btn_cancel),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgDiaperModify.dismiss();
                    }
                },
                mContext.getString(R.string.btn_ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long selectedDateTimeMs = mDlgDiaperModify.getDateTimeUtcMs();
                        int selectedMode = mDlgDiaperModify.getSelectedMode();
                        if (DBG) Log.d(TAG, "selectedDateTimeMs: " + selectedDateTimeMs + " / " + selectedMode);
                        if (DBG) Log.d(TAG, "UTC: " + DateTimeUtil.getUtcDateTimeStringFromUtcTimestamp(selectedDateTimeMs));
                        if (DBG) Log.d(TAG, "LOCAL: " + DateTimeUtil.getLocalDateTimeStringFromUtcTimestamp(selectedDateTimeMs));

                        mDlgDiaperModify.dismiss();

                        final NotificationMessage msg = mDlgDiaperModify.getNotificationData();
                        msg.extra = selectedMode + "";
                        msg.timeMs = selectedDateTimeMs;

                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return new FeedbackNotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_notification_row_feedback, parent, false));
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_recyclerview_progressbar, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof FeedbackNotificationViewHolder) {
            final FeedbackNotificationViewHolder holder = (FeedbackNotificationViewHolder) viewHolder;
            final NotificationMessage msg = mmList.get(position);
            if (msg == null) return;

            int type = msg.notiType;

            if (type == NotificationType.CHAT_DATE_LINE) {
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.vsMessage.setVisibility(View.GONE);
                holder.ivIcon.setVisibility(View.GONE);
                holder.ivNewMark.setVisibility(View.GONE);
                holder.vContinuousDivider.setVisibility(View.GONE);
                holder.vAboveDivider.setVisibility(View.VISIBLE);
                holder.vBelowDivider.setVisibility(View.VISIBLE);
                holder.tvDate.setText(msg.extra);
                holder.tvFeedbackInput.setText("");
            } else {
                holder.tvDate.setVisibility(View.GONE);
                holder.vsMessage.setVisibility(View.VISIBLE);
                holder.ivIcon.setVisibility(View.VISIBLE);
                if (mLatestCheckedNotificationIndex < msg.msgId) {
                    holder.ivNewMark.setVisibility(View.VISIBLE);
                } else {
                    holder.ivNewMark.setVisibility(View.GONE);
                }
                holder.vAboveDivider.setVisibility(View.GONE);

                if (position == mmList.size() - 1) {
                    holder.vContinuousDivider.setVisibility(View.GONE);
                    holder.vBelowDivider.setVisibility(View.VISIBLE);
                } else {
                    holder.vContinuousDivider.setVisibility(View.VISIBLE);
                    holder.vBelowDivider.setVisibility(View.GONE);
                }

                if (holder.vsMessage.getCurrentView().getId() == R.id.lctn_notification_row_feedback_answer) {
                    holder.vsMessage.showPrevious();
                }

                holder.tvFeedbackInput.setText("");

                holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(type));
                holder.tvTime.setText(DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, msg.timeMs));
                holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                switch (type) {
                    case NotificationType.CHAT_USER_INPUT:
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextComment));
                        holder.tvDescription.setText(msg.extra);
                        break;
                    case NotificationType.CHAT_USER_FEEDBACK:
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextComment));
                        holder.ivIcon.setBackgroundResource(R.drawable.ic_notification_user_comment);

                        if (msg.extra.equals("d10")) { // 10분 후, 아무입력 없음
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_10min));
                        } else if (msg.extra.equals("d1010")) { // 10분 후, 깨끗 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_clean));
                        } else if (msg.extra.equals("d1011")) { // 10분 후, 소변 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_pee));
                        } else if (msg.extra.equals("d1012")) { // 10분 후, 대변 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_poo));
                        } else if (msg.extra.equals("d1013")) { // 10분 후, 대소변 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_mixed));
                        } else if (msg.extra.equals("d40")) { // 40분 후, 아무입력 없음
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_40min));
                        } else if (msg.extra.equals("d4010")) { // 40분 후, 깨끗 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_clean));
                        } else if (msg.extra.equals("d4011")) { // 40분 후, 소변 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_pee));
                        } else if (msg.extra.equals("d4012")) { // 40분 후, 대변 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_poo));
                        } else if (msg.extra.equals("d4013")) { // 40분 후, 대소변 입력
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_mixed));
                        } else if ("10".equals(msg.extra)) {
                            //holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_clean);
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_clean) + "!");
                        } else if ("11".equals(msg.extra)) {
                            //holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_pee);
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_pee) + "!");
                        } else if ("12".equals(msg.extra)) {
                            //holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_poo);
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_poo) + "!");
                        } else if ("13".equals(msg.extra)) {
                            //holder.ivIcon.setBackgroundResource(R.drawable.ic_diary_diaper_mixed);
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_mixed) + "!");
                        } else {
                            holder.tvDescription.setText(mContext.getString(R.string.notification_check_diaper_status_clean) + "!");
                        }
                        break;
                    case NotificationType.DIAPER_DETACHMENT_DETECTED:
                        if ("1".equals(msg.extra)) {
                            holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(NotificationType.DIAPER_CHANGED));
                            holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(NotificationType.DIAPER_CHANGED)));
                        } else {
                            holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(NotificationType.ABNORMAL_DETECTED));
                            holder.tvDescription.setText("이시간에 기저귀 교체를 하셨습니까?");
                        }
                        break;
                    case NotificationType.BABY_SLEEP:
                        holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(NotificationType.BABY_SLEEP));
                        if (msg.extra == null || msg.extra.equals("-")) {
                            holder.tvDescription.setText(mContext.getString(R.string.notification_sleep_start));
                        } else {
                            long sleepStartUtcTimeMs = msg.timeMs;
                            String sleepStartTimeString = DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, sleepStartUtcTimeMs);
                            long sleepEndUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(DateTimeUtil.getUtcTimeStampFromLocalString(msg.extra, "yyMMdd-HHmmss")); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴
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
                            holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextSleep));
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
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + "g)");
                            } else if (type == NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + "ml)");
                            } else if (type == NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + "ml)");
                            } else if (type == NotificationType.BABY_FEEDING_NURSED_BREAST_MILK) {
                                holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + " (" + msg.extra + "분)");
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

                        holder.tvDescription.setText(extraType);
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextDiaper));
                        break;
                    case NotificationType.PEE_DETECTED:
                    case NotificationType.POO_DETECTED:
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorDiaryTextComment));
                        holder.ivIcon.setBackgroundResource(R.drawable.ic_notification_user_comment);
                        if ("1".equals(msg.extra)) {
                            holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                            holder.tvFeedbackInput.setText("(0)");
                        } else if ("2".equals(msg.extra)) {
                            holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                            holder.tvFeedbackInput.setText("(X)");
                        } else if ("3".equals(msg.extra)) {
                            holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                            holder.tvFeedbackInput.setText("(?)");
                        } else {
                            holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)) + "\n" + mContext.getString(R.string.notification_confirm_detected_status));
                        }
                        break;
                    default:
                        holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                        holder.tvDescription.setTextColor(mContext.getResources().getColor(R.color.colorTextPrimary));
                        if ("1".equals(msg.extra)) {
                            holder.tvFeedbackInput.setText("(0)");
                        } else if ("2".equals(msg.extra)) {
                            holder.tvFeedbackInput.setText("(X)");
                        } else if ("3".equals(msg.extra)) {
                            holder.tvFeedbackInput.setText("(?)");
                        }
                        break;
                }
            }

            holder.btnAnswer1.setVisibility(View.GONE);
            holder.btnAnswer2.setVisibility(View.GONE);
            holder.btnAnswer3.setVisibility(View.GONE);
            holder.btnModify.setVisibility(View.GONE);

            if ((msg.notiType == NotificationType.PEE_DETECTED) ||
                    (msg.notiType == NotificationType.POO_DETECTED) ||
                    (msg.notiType == NotificationType.FART_DETECTED) ||
                    (msg.notiType == NotificationType.ABNORMAL_DETECTED)) {

                holder.btnAnswer1.setVisibility(View.VISIBLE);
                holder.btnAnswer1.setText(mContext.getString(R.string.feedback_true));
                holder.btnAnswer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = "1";
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            remove(position); // 베타테스트에서 입력하면 사라짐
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                    }
                });

                holder.btnAnswer2.setVisibility(View.VISIBLE);
                holder.btnAnswer2.setText(mContext.getString(R.string.feedback_false));
                holder.btnAnswer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = "2";
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            remove(position); // 베타테스트에서 입력하면 사라짐
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });

                holder.btnAnswer3.setVisibility(View.VISIBLE);
                holder.btnAnswer3.setText(mContext.getString(R.string.feedback_dk));
                holder.btnAnswer3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = "3";
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            remove(position); // 베타테스트에서 입력하면 사라짐
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                    }
                });
            } else if (msg.notiType == NotificationType.CHAT_USER_FEEDBACK || msg.extra.startsWith("d10") || msg.extra.startsWith("d40")) {
                holder.btnAnswer1.setVisibility(View.VISIBLE);
                holder.btnAnswer1.setText(mContext.getString(R.string.device_sensor_diaper_status_nothing));
                holder.btnAnswer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = msg.extra.substring(0, 3) + "10"; // d1010, d4010
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            remove(position); // 베타테스트에서 입력하면 사라짐
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });

                    }
                });

                holder.btnAnswer2.setVisibility(View.VISIBLE);
                holder.btnAnswer2.setText(mContext.getString(R.string.feedback_pee));
                holder.btnAnswer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = msg.extra.substring(0, 3) + "11"; // d1011, d4011
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            remove(position); // 베타테스트에서 입력하면 사라짐
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                    }
                });

                holder.btnAnswer3.setVisibility(View.VISIBLE);
                holder.btnAnswer3.setText(mContext.getString(R.string.feedback_poo));
                holder.btnAnswer3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = msg.extra.substring(0, 3) + "12"; // d1012, d4012
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            remove(position); // 베타테스트에서 입력하면 사라짐
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                    }
                });
            } else if (msg.notiType == NotificationType.BABY_FEEDING_NURSED_BREAST_MILK
                            || msg.notiType == NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK
                            || msg.notiType == NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK
                            || msg.notiType == NotificationType.BABY_FEEDING_BABY_FOOD) {

                //holder.btnModify.setVisibility(View.VISIBLE);
                holder.btnModify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            public void run() {
                                if (mDlgFeedingModify != null && !mDlgFeedingModify.isShowing()) {
                                    int selectedIdx = 0;
                                    if (msg.notiType == NotificationType.BABY_FEEDING_NURSED_BREAST_MILK) {
                                        selectedIdx = 1;
                                    } else if (msg.notiType == NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK) {
                                        selectedIdx = 2;
                                    } else if (msg.notiType == NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK) {
                                        selectedIdx = 3;
                                    } else if (msg.notiType == NotificationType.BABY_FEEDING_BABY_FOOD) {
                                        selectedIdx = 4;
                                    }
                                    mDlgFeedingModify.setNotificationData(msg);
                                    mDlgFeedingModify.setSelectedIndex(selectedIdx);
                                    mDlgFeedingModify.setDateTimeUtcMs(msg.timeMs);
                                    mDlgFeedingModify.show();
                                    if (holder.vsMessage.getCurrentView().getId() == R.id.lctn_notification_row_message) {
                                        holder.vsMessage.showNext();
                                    } else {
                                        holder.vsMessage.showPrevious();
                                    }
                                }
                            }
                        });
                    }
                });

            } else if (msg.notiType == NotificationType.BABY_SLEEP) {

                //holder.btnModify.setVisibility(View.VISIBLE);
                holder.btnModify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            public void run() {
                                if (mDlgSleepingModify != null && !mDlgSleepingModify.isShowing()) {
                                    mDlgSleepingModify.setNotificationData(msg);
                                    mDlgSleepingModify.setStartDateTimeUtcMs(msg.timeMs);
                                    long sleepEndUtcTimeMs = DateTimeUtil.convertUTCToLocalTimeMs(DateTimeUtil.getUtcTimeStampFromLocalString(msg.extra, "yyMMdd-HHmmss")); // Date(UTC) -> date.getTime() (UTC-9) -> +9를 해야 실제 UTC값이 나옴

                                    mDlgSleepingModify.setEndDateTimeUtcMs(sleepEndUtcTimeMs);
                                    mDlgSleepingModify.show();
                                    if (holder.vsMessage.getCurrentView().getId() == R.id.lctn_notification_row_message) {
                                        holder.vsMessage.showNext();
                                    } else {
                                        holder.vsMessage.showPrevious();
                                    }
                                }
                            }
                        });
                    }
                });

            } else if (msg.notiType == NotificationType.DIAPER_CHANGED) {

                //holder.btnModify.setVisibility(View.VISIBLE);
                holder.btnModify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            public void run() {
                                if (mDlgDiaperModify != null && !mDlgDiaperModify.isShowing()) {
                                    mDlgDiaperModify.setNotificationData(msg);
                                    mDlgDiaperModify.setDateTimeUtcMs(msg.timeMs);
                                    int selectedIdx = 1;
                                    try {
                                        selectedIdx = Integer.parseInt(msg.extra);
                                    } catch(Exception e) {

                                    }
                                    mDlgDiaperModify.setSelectedIndex(selectedIdx);
                                    mDlgDiaperModify.show();
                                    if (holder.vsMessage.getCurrentView().getId() == R.id.lctn_notification_row_message) {
                                        holder.vsMessage.showNext();
                                    } else {
                                        holder.vsMessage.showPrevious();
                                    }
                                }
                            }
                        });
                    }
                });

            } else if (msg.notiType == NotificationType.DIAPER_DETACHMENT_DETECTED && !"1".equals(msg.extra) && !"2".equals(msg.extra)) {
                // 입력하지 않은 기저귀 교체 감지 알람에 대해 입력을 받음
                holder.btnAnswer1.setVisibility(View.VISIBLE);
                holder.btnAnswer1.setText(mContext.getString(R.string.feedback_true));
                holder.btnAnswer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = "1";
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.updateDB(mContext);
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                    }
                });

                holder.btnAnswer2.setVisibility(View.VISIBLE);
                holder.btnAnswer2.setText(mContext.getString(R.string.feedback_false));
                holder.btnAnswer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.extra = "2";
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                msg,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            msg.deleteDB(mContext);
                                            remove(position);
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    notifyDataSetChanged();
                                                    if (mOnUpdateViewListener != null) {
                                                        mOnUpdateViewListener.onUpdate();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });
            }

            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        public void run() {
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
                                    if (mOnUpdateViewListener != null) {
                                        mOnUpdateViewListener.onUpdate();
                                    }
                                }
                            });
                        }
                    });
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.vsMessage.getCurrentView().getId() == R.id.lctn_notification_row_message) {
                        holder.vsMessage.showNext();
                    } else {
                        holder.vsMessage.showPrevious();
                    }
                }
            });
        }
    }

    public void setOnUpdateViewListener(OnUpdateViewListener listener) {
        mOnUpdateViewListener = listener;
    }
}