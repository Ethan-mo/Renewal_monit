package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationWidget extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "ValidationWidget";

	protected ValidationWidget.ValidationListener mValidationUpdateListener;
	protected ImageView ivValidationChecked;
	protected TextView tvTitle, tvWarning;
	protected View vUnderline;
	protected Context mContext;
	protected boolean isValid = false;
	protected boolean showUnderline = true;
	protected View.OnClickListener mClickListener;

	public static class ValidationListener {
		public void updateValidation() {
        }
	}

	public ValidationWidget(Context context) {
		super(context);
		mContext = context;
	}

	public ValidationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public ValidationWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public void setValidationUpdateListener(ValidationWidget.ValidationListener listener) {
		mValidationUpdateListener = listener;
	}

	public void setTitle(String title) {
		if (tvTitle == null) return;
		tvTitle.setText(title);
	}

	public void setWarning(String warning) {
		if (tvWarning == null) return;
		tvWarning.setText(warning);
	}

	public void setValid(boolean valid) {
		if (valid) {
			if (ivValidationChecked != null) {
				ivValidationChecked.setBackgroundResource(R.drawable.ic_validation_check_valid);
			}
			showWarning(false);
			isValid = true;
		} else {
			if (ivValidationChecked != null) {
				ivValidationChecked.setBackgroundResource(R.drawable.ic_validation_check_default);
			}
			showWarning(true);
			isValid = false;
		}
	}

	public boolean isValid() {
		return isValid;
	}

	public void showUnderLine(boolean show) {
		if (vUnderline == null) return;
		if (show) {
			vUnderline.setVisibility(View.VISIBLE);
		} else {
			vUnderline.setVisibility(View.GONE);
		}
	}

	public void showWarning(boolean show) {
		if (tvWarning == null)  {
			return;
		}
		if (show) {
			tvWarning.setVisibility(View.VISIBLE);
		} else {
			tvWarning.setVisibility(View.GONE);
		}
	}

	public void showWarning(boolean show, long autoHideTimeMs) {
		if (tvWarning == null)  {
			return;
		}
		if (show) {
			tvWarning.setVisibility(View.VISIBLE);
			if (autoHideTimeMs > 0) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						tvWarning.setVisibility(View.GONE);
					}
				}, autoHideTimeMs);
			}
		} else {
			tvWarning.setVisibility(View.GONE);
		}
	}

	public void addOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}

}