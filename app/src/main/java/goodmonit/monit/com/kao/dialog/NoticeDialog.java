package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class NoticeDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "NoticeDlg";

	private Button btnDoNotShow, btnClose, btnMore;
	private TextView tvTitle, tvContents;
	private View vDivider;

	private String mTitle, mContents;
	private View.OnClickListener mDoNotShowButtonListener, mCloseButtonListener, mMoreButtonListener;
	private boolean showMore;

	public NoticeDialog(Context context,
                        String title,
                        String contents) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mContents = contents;
	}

	public NoticeDialog(Context context,
                        String title,
                        String contents,
                        View.OnClickListener doNotShowButtonListener,
                        View.OnClickListener closeButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mContents = contents;
		mDoNotShowButtonListener = doNotShowButtonListener;
		mCloseButtonListener = closeButtonListener;
	}

	public NoticeDialog(Context context,
						String title,
						String contents,
						View.OnClickListener doNotShowButtonListener,
						View.OnClickListener closeButtonListener,
						View.OnClickListener moreButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mContents = contents;
		mDoNotShowButtonListener = doNotShowButtonListener;
		mCloseButtonListener = closeButtonListener;
		mMoreButtonListener = moreButtonListener;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.dialog_notice);
        _initView();
		showMoreButton(showMore);
    }

	private void _initView() {
		tvTitle = (TextView)findViewById(R.id.tv_notice_dialog_title);
		tvContents = (TextView)findViewById(R.id.tv_notice_dialog_contents);
		btnDoNotShow = (Button)findViewById(R.id.btn_notice_dialog_do_not_show_this_again);
		btnClose = (Button)findViewById(R.id.btn_notice_dialog_close);
		btnMore = (Button)findViewById(R.id.btn_notice_dialog_more);
		vDivider = findViewById(R.id.v_notice_dialog_divider_btn_center);

		tvTitle.setText(mTitle);
		tvContents.setText(mContents);
		btnDoNotShow.setOnClickListener(mDoNotShowButtonListener);
		btnClose.setOnClickListener(mCloseButtonListener);
		btnMore.setOnClickListener(mMoreButtonListener);
    }

    public void setDoNotShowButtonListener(View.OnClickListener listener) {
		mDoNotShowButtonListener = listener;
		if (btnDoNotShow != null) {
			btnDoNotShow.setOnClickListener(mDoNotShowButtonListener);
		}
	}

	public void setCloseButtonListener(View.OnClickListener listener) {
		mCloseButtonListener = listener;
		if (btnClose != null) {
			btnClose.setOnClickListener(mCloseButtonListener);
		}
	}

	public void setMoreButtonListener(View.OnClickListener listener) {
		mMoreButtonListener = listener;
		if (btnMore != null) {
			btnMore.setOnClickListener(mMoreButtonListener);
		}
	}

	public void showMoreButton(boolean show) {
		showMore = show;
		if (show) {
			if (btnMore != null) {
				btnMore.setVisibility(View.VISIBLE);
			}
			if (vDivider != null) {
				vDivider.setVisibility(View.VISIBLE);
			}
		} else {
			if (btnMore != null) {
				btnMore.setVisibility(View.GONE);
			}
			if (vDivider != null) {
				vDivider.setVisibility(View.GONE);
			}
		}
	}
}
