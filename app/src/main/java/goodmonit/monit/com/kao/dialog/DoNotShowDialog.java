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

public class DoNotShowDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "DoNotShowDialog";

	private Button btnDoNotShow, btnConfirm;
	private TextView tvTitle, tvContents;

	private String mTitle, mContents;
	private View.OnClickListener mDoNotShowButtonListener, mConfirmButtonListener;

	public DoNotShowDialog(Context context,
						   String title,
						   String contents) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mContents = contents;
	}

	public DoNotShowDialog(Context context,
						   String title,
						   String contents,
						   View.OnClickListener doNotShowButtonListener,
						   View.OnClickListener confirmButtonListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mTitle = title;
		mContents = contents;
		mDoNotShowButtonListener = doNotShowButtonListener;
		mConfirmButtonListener = confirmButtonListener;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.dialog_do_not_show);
        _initView();
    }

	private void _initView() {
		tvTitle = (TextView)findViewById(R.id.tv_do_not_show_dialog_title);
		tvContents = (TextView)findViewById(R.id.tv_do_not_show_dialog_contents);
		btnDoNotShow = (Button)findViewById(R.id.btn_dialog_do_not_show_this_again);
		btnConfirm = (Button)findViewById(R.id.btn_dialog_do_not_show_confirm);

		tvTitle.setText(mTitle);
		tvContents.setText(mContents);
		btnDoNotShow.setOnClickListener(mDoNotShowButtonListener);
		btnConfirm.setOnClickListener(mConfirmButtonListener);
    }

    public void setDoNotShowButtonListener(View.OnClickListener listener) {
		mDoNotShowButtonListener = listener;
		if (btnDoNotShow != null) {
			btnDoNotShow.setOnClickListener(mDoNotShowButtonListener);
		}
	}

	public void setConfirmButtonListener(View.OnClickListener listener) {
		mConfirmButtonListener = listener;
		if (btnConfirm != null) {
			btnConfirm.setOnClickListener(mConfirmButtonListener);
		}
	}
}
