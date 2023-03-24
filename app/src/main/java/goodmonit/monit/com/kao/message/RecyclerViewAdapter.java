package goodmonit.monit.com.kao.message;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.dialog.NotificationDialog;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.util.DateTimeUtil;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final String TAG = Configuration.BASE_TAG + "rvAdapter";
    public static final int COUNT_LOADED_MESSAGES_AT_ONCE   = 30;
    public static final int LOADING_MESSAGE_SEC             = 1;

    protected final int VIEW_PROGRESSBAR  = 0;
    protected final int VIEW_ITEM         = 1;

	protected Context mContext;
    protected ArrayList<NotificationMessage> mmList = new ArrayList<>();
    protected long mLatestCheckedNotificationIndex;
    protected int mFocusedItem = 0;

    protected View.OnClickListener mOnClickListener;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    protected boolean isMoreLoading = false;
    protected int visibleThreshold = 1;
    protected int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem;
    protected OnLoadMoreListener mOnLoadMoreListener;

    protected NotificationMessage mSelectedNotificationMessage;
    protected NotificationDialog mDlgNotification;
    protected SimpleDialog mDlgDelete;

    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public RecyclerViewAdapter(Context context) {
        mContext = context;
        this.setLinearLayoutManager(new LinearLayoutManagerWrapper(mContext, LinearLayoutManager.VERTICAL, false));
        mDlgNotification = new NotificationDialog(mContext,
                mSelectedNotificationMessage,
                mContext.getString(R.string.btn_close),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDlgNotification.dismiss();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSelectedNotificationMessage == null) return;
                        ServerQueryManager.getInstance(mContext).removeNotification(
                                mSelectedNotificationMessage,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                        }
                                    }
                                });
                        mSelectedNotificationMessage.deleteDB(mContext);
                        remove(mSelectedNotificationMessage);
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                        mDlgNotification.dismiss();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSelectedNotificationMessage == null) return;
                        if ("2".equals(mSelectedNotificationMessage.extra)) { // 오알람체크(틀림) 일때
                            mSelectedNotificationMessage.extra = "1";
                            mDlgNotification.setWrongAlarm(false);
                        } else {
                            mSelectedNotificationMessage.extra = "2";
                            mDlgNotification.setWrongAlarm(true);
                        }
                        ServerQueryManager.getInstance(mContext).setNotificationFeedback(
                                mSelectedNotificationMessage,
                                new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            mSelectedNotificationMessage.updateDB(mContext);
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

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager) {
        mLinearLayoutManager = linearLayoutManager;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mLinearLayoutManager == null) {
                    Log.e(TAG, "LinearLayoutManager NULL");
                }
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
                if (!isMoreLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        isMoreLoading = true;
                        mOnLoadMoreListener.onLoadMore();
                    }
                }
            }
        });
        mRecyclerView = recyclerView;
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    public void setMoreLoading(boolean loading) {
        isMoreLoading = loading;
    }

    public void addList(ArrayList<NotificationMessage> list){
        mmList.addAll(list);
        notifyItemRangeChanged(0, list.size());
    }

    public void setList(ArrayList<NotificationMessage> msgList) {
        ArrayList<NotificationMessage> tempMessageList = new ArrayList<>();
        if (msgList == null) {
            mmList.clear();
            return;
        }
        long dayMillis = 0;
        for (NotificationMessage mm : msgList) {
            long msgDayMillis = DateTimeUtil.getDayBeginMillis(DateTimeUtil.convertUTCToLocalTimeMs(mm.timeMs));
            if (dayMillis == 0 || dayMillis != msgDayMillis) {
                NotificationMessage date = new NotificationMessage(NotificationType.CHAT_DATE_LINE, 0, 0, DateTimeUtil.getDateString(mm.timeMs, "ko"), 0, -1);
                tempMessageList.add(date);
                dayMillis = msgDayMillis;
            }
            tempMessageList.add(mm);
        }
        mmList = tempMessageList;
    }

    public void add(int idx, NotificationMessage mm) {
        mmList.add(idx, mm);
    }

    public void add(NotificationMessage mm) {
        mmList.add(mm);
    }

    public void clear() {
    	mmList.clear();
    }

    public void remove(int pos) {
        if (pos > mmList.size() - 1) return;
        mmList.remove(pos);
        if (mmList.size() == 1) { // 날짜만 남은 경우
            mmList.clear();
        }
    }

    public void remove(NotificationMessage mm) {
        mmList.remove(mm);
        if (mmList.size() == 1) { // 날짜만 남은 경우
            mmList.clear();
        }
    }

    public void setLatestCheckedNotificationIndex(long idx) {
        mLatestCheckedNotificationIndex = idx;
    }

    public void setFocusedItem(int position){
        notifyItemChanged(mFocusedItem);
        mFocusedItem = position;
        notifyItemChanged(mFocusedItem);
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = mFocusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(mFocusedItem);
            mFocusedItem = tryFocusItem;
            notifyItemChanged(mFocusedItem);
            lm.scrollToPosition(mFocusedItem);
            return true;
        }

        return false;
    }

    public void setProgressMore(final boolean isProgress) {
        final int size = mmList.size();
        if (isProgress) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    // Row for progressbar added
                    mmList.add(null);
                    notifyItemInserted(size - 1);
                }
            });
        } else {
            if (size > 1) {
                NotificationMessage lastMsg = mmList.get(size - 1);
                if (lastMsg == null) {
                    // Row for progressbar removed
                    mmList.remove(size - 1);
                    notifyItemRemoved(size);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mmList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESSBAR;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

    }

    @Override
    public int getItemCount() {
        return mmList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView ivIcon;
        TextView tvDescription;
        TextView tvTime;
        View vContinuousDivider;
        View vAboveDivider;
        View vBelowDivider;
        LinearLayout lctnMessage;
        ImageView ivNewMark;
        Button btnRemove;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.tv_notification_row_date);
            ivIcon = (ImageView)itemView.findViewById(R.id.iv_notification_row_icon);
            tvDescription = (TextView)itemView.findViewById(R.id.tv_notification_row_description);
            tvTime = (TextView)itemView.findViewById(R.id.tv_notification_row_time);
            vContinuousDivider = itemView.findViewById(R.id.v_notification_row_continuous_divider);
            vAboveDivider = itemView.findViewById(R.id.v_notification_row_above_divider);
            vBelowDivider = itemView.findViewById(R.id.v_notification_row_below_divider);
            lctnMessage = (LinearLayout)itemView.findViewById(R.id.lctn_notification_row_message);
            ivNewMark = (ImageView)itemView.findViewById(R.id.iv_notification_row_new_mark);
            btnRemove = (Button)itemView.findViewById(R.id.btn_notification_row_remove);
        }
    }

    static public class FeedbackNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView ivIcon;
        TextView tvDescription;
        TextView tvTime;
        TextView tvFeedbackInput;
        View vContinuousDivider;
        View vAboveDivider;
        View vBelowDivider;
        ViewSwitcher vsMessage;
        Button btnAnswer1;
        Button btnAnswer2;
        Button btnAnswer3;
        Button btnRemove;
        Button btnModify;
        ImageView ivNewMark;

        public FeedbackNotificationViewHolder(View itemView) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.tv_notification_row_feedback_date);
            ivIcon = (ImageView)itemView.findViewById(R.id.iv_notification_row_feedback_icon);
            tvDescription = (TextView)itemView.findViewById(R.id.tv_notification_row_feedback_description);
            tvTime = (TextView)itemView.findViewById(R.id.tv_notification_row_feedback_time);
            tvFeedbackInput = (TextView)itemView.findViewById(R.id.tv_notification_row_feedback_input);
            vContinuousDivider = itemView.findViewById(R.id.v_notification_row_feedback_continuous_divider);
            vAboveDivider = itemView.findViewById(R.id.v_notification_row_feedback_above_divider);
            vBelowDivider = itemView.findViewById(R.id.v_notification_row_feedback_below_divider);
            btnAnswer1 = (Button)itemView.findViewById(R.id.btn_notification_row_feedback_answer_1);
            btnAnswer2 = (Button)itemView.findViewById(R.id.btn_notification_row_feedback_answer_2);
            btnAnswer3 = (Button)itemView.findViewById(R.id.btn_notification_row_feedback_answer_3);
            btnRemove = (Button)itemView.findViewById(R.id.btn_notification_row_feedback_answer_remove);
            btnModify = (Button)itemView.findViewById(R.id.btn_notification_row_feedback_answer_modify);
            btnModify.setVisibility(View.GONE);
            vsMessage = (ViewSwitcher)itemView.findViewById(R.id.vs_widget_notification_row_feedback);
            ivNewMark = (ImageView)itemView.findViewById(R.id.iv_notification_row_feedback_new_mark);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar;
        public ProgressViewHolder(View v) {
            super(v);
            pBar = (ProgressBar) v.findViewById(R.id.recyclerview_progressbar);
        }
    }

    static class CloudNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView ivIcon;
        TextView tvDescription;
        TextView tvDescriptionExtra;
        TextView tvTime;
        View vContinuousDivider;
        View vAboveDivider;
        View vBelowDivider;
        LinearLayout lctnMessage;
        ImageView ivNewMark;

        public CloudNotificationViewHolder(View itemView) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.tv_notification_row_date);
            ivIcon = (ImageView)itemView.findViewById(R.id.iv_notification_row_icon);
            tvDescription = (TextView)itemView.findViewById(R.id.tv_notification_row_description);
            tvDescriptionExtra = (TextView)itemView.findViewById(R.id.tv_notification_row_description_extra);
            tvTime = (TextView)itemView.findViewById(R.id.tv_notification_row_time);
            vContinuousDivider = itemView.findViewById(R.id.v_notification_row_continuous_divider);
            vAboveDivider = itemView.findViewById(R.id.v_notification_row_above_divider);
            vBelowDivider = itemView.findViewById(R.id.v_notification_row_below_divider);
            lctnMessage = (LinearLayout)itemView.findViewById(R.id.lctn_notification_row_message);
            ivNewMark = (ImageView)itemView.findViewById(R.id.iv_notification_row_new_mark);
        }
    }

    public class LinearLayoutManagerWrapper extends LinearLayoutManager {
        public LinearLayoutManagerWrapper(Context context) {
            super(context);
        }

        public LinearLayoutManagerWrapper(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public LinearLayoutManagerWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }
}