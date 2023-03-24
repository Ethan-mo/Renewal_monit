package goodmonit.monit.com.kao.message;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class CloudNotificationMessageAdapter extends RecyclerViewAdapter {
    private static final String TAG = Configuration.BASE_TAG + "CloudAdapter";

    public CloudNotificationMessageAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return new CloudNotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_group_notification_row, parent, false));
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_recyclerview_progressbar, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CloudNotificationViewHolder) {
            final CloudNotificationViewHolder holder = (CloudNotificationViewHolder) viewHolder;

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
            } else {
                holder.lctnMessage.setVisibility(View.VISIBLE);
                holder.tvDate.setVisibility(View.GONE);
                holder.ivIcon.setBackgroundResource(NotificationType.getIconResource(type));

                if (mLatestCheckedNotificationIndex < msg.msgId) {
                    holder.ivNewMark.setVisibility(View.VISIBLE);
                } else {
                    holder.ivNewMark.setVisibility(View.GONE);
                }

                switch (type) {
                    case NotificationType.MY_CLOUD_INVITE:
                    case NotificationType.MY_CLOUD_DELETE:
                    case NotificationType.MY_CLOUD_LEAVE:
                    case NotificationType.MY_CLOUD_REQUEST:
                        holder.tvDescription.setText(mContext.getString(R.string.group_mygroup));
                        holder.tvDescriptionExtra.setText(mContext.getString(NotificationType.getStringResource(type)) + " : " + msg.extra);
                        break;
                    case NotificationType.OTHER_CLOUD_INVITED:
                    case NotificationType.OTHER_CLOUD_DELETED:
                    case NotificationType.OTHER_CLOUD_LEAVE:
                    case NotificationType.OTHER_CLOUD_REQUEST:
                        holder.tvDescription.setText(msg.extra + " " + mContext.getString(R.string.group_title_group));
                        holder.tvDescriptionExtra.setText(mContext.getString(NotificationType.getStringResource(type)));
                        break;
                    case NotificationType.CLOUD_INIT_DEVICE:
                        holder.tvDescription.setText(mContext.getString(R.string.group_mygroup));
                        holder.tvDescriptionExtra.setText(mContext.getString(NotificationType.getStringResource(type)) + " : " + msg.extra);
                        break;
                    default:
                        holder.tvDescription.setText(mContext.getString(NotificationType.getStringResource(type)));
                        break;
                }
                holder.tvTime.setText(DateTimeUtil.getStringSpecificTimeInDay(DateTimeUtil.TYPE_COLON, msg.timeMs));
                holder.vAboveDivider.setVisibility(View.GONE);

                if (position == mmList.size() - 1) {
                    holder.vContinuousDivider.setVisibility(View.GONE);
                    holder.vBelowDivider.setVisibility(View.VISIBLE);
                } else {
                    holder.vContinuousDivider.setVisibility(View.VISIBLE);
                    holder.vBelowDivider.setVisibility(View.GONE);
                }
            }
        }
    }
}