package goodmonit.monit.com.kao.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ProgressCircleDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "Processing";

	private TextView tvContents;
	private Button btnCancel;

	private String mContents, mBtnName;
	private View.OnClickListener mBtnListener;
	private ProgressBar pbCircle;

	public ProgressCircleDialog(Context context, String contents, String btnName, View.OnClickListener btnListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mContents = contents;
		mBtnName = btnName;
		mBtnListener = btnListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.5f;
		getWindow().setAttributes(lpWindow);

		setContentView(R.layout.dialog_progress_circle);

		tvContents = (TextView)findViewById(R.id.tv_dialog_progress_circle_contents);
		btnCancel = (Button)findViewById(R.id.btn_dialog_progrss_circle_cancel);
		pbCircle = (ProgressBar)findViewById(R.id.pb_dialog_progress_circle);

		setContents(mContents);
		btnCancel.setText(mBtnName);
		btnCancel.setOnClickListener(mBtnListener);
	}

	public void showProcessBar(boolean show) {
		if (pbCircle != null) {
			if (show) {
				pbCircle.setVisibility(View.VISIBLE);
			} else {
				pbCircle.setVisibility(View.GONE);
			}
		}
	}

	public void setContents(String contents) {
		if (tvContents != null) {
			tvContents.setText(contents);
		}
	}

	public void setBtnName(String name) {
		if (btnCancel != null) {
			btnCancel.setText(name);
		}
	}

	@Override
	public void onBackPressed() {
		if (mBtnListener != null) {
			mBtnListener.onClick(null);
		}
	}
}