package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationAgreementPolicy extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "ValidAgreement";

	private Context mContext;
	private Button btnPolicy, btnShow;
	private TextView tvWarning;
	private View vUnderLine;
	private String mShowUrl;
	private boolean mShowUnderline;

	public ValidationAgreementPolicy(Context context) {
		super(context);
		mContext = context;
		_initView();
	}

	public ValidationAgreementPolicy(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_initView();
	}

	public ValidationAgreementPolicy(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		_initView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_agreement_policy, this, false);
		addView(v);

		btnPolicy = (Button)v.findViewById(R.id.btn_widget_agreement_policy_agree);
		btnPolicy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean selected = v.isSelected();
				if (selected) {

				} else {

				}
				v.setSelected(!selected);
			}
		});
		btnShow = (Button)v.findViewById(R.id.btn_widget_agreement_policy_link);
		btnShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mShowUrl != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mShowUrl));
					mContext.startActivity(intent);
				}
			}
		});

		tvWarning = (TextView)v.findViewById(R.id.tv_widget_agreement_policy_warning);
		vUnderLine = v.findViewById(R.id.v_widget_agreement_policy_underline);
		if (mShowUnderline) {
			vUnderLine.setVisibility(View.VISIBLE);
		} else {
			vUnderLine.setVisibility(View.GONE);
		}
	}

	public void setWarning(String warning) {
		if (tvWarning != null) {
			tvWarning.setText(warning);
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

	public void setShowUrl(String url) {
		mShowUrl = url;
	}

	public boolean isAgreed() {
		return btnPolicy.isSelected();
	}

	public void setPolicyName(String name) {
		if (btnPolicy != null) {
			btnPolicy.setText(name);
		}
	}

	public void setSelected(boolean selected) {
		btnPolicy.setSelected(selected);
	}

	public void showUnderline(boolean show) {
		mShowUnderline = show;
		if (vUnderLine != null) {
			if (mShowUnderline) {
				vUnderLine.setVisibility(View.VISIBLE);
			} else {
				vUnderLine.setVisibility(View.GONE);
			}
		}
	}
}