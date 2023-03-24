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

public class ProgressHorizontalDialog extends Dialog {
	private static final String TAG = Configuration.BASE_TAG + "Progress";

	private TextView tvContents;
	private Button btnCancel;
	private ProgressBar pbProgress;

	private String mContents, mBtnName;
	private View.OnClickListener mBtnListener;

    public ProgressHorizontalDialog(Context context, String contents, String btnName, View.OnClickListener btnListener) {
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
        
        setContentView(R.layout.dialog_progress_horizontal);
        
        tvContents = (TextView)findViewById(R.id.tv_dialog_progress_contents);
        btnCancel = (Button)findViewById(R.id.btn_dialog_progress_cancel);
		pbProgress = (ProgressBar)findViewById(R.id.pb_dialog_progress);
        
        setContents(mContents);
        btnCancel.setText(mBtnName);
        btnCancel.setOnClickListener(mBtnListener);
    }

	public void setContents(String contents) {
    	mContents = contents;
		if (tvContents != null) {
			tvContents.setText(contents);	
		}
	}

	public void setProgress(int progress) {
		if (progress < 0) {
			progress = 0;
		} else if (progress > 100) {
			progress = 100;
		}
		if (pbProgress != null) {
			pbProgress.setProgress(progress);
		}
	}

	public void setBtnName(String name) {
		if (btnCancel != null) {
			btnCancel.setText(name);	
		}
	}
}
