package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class ValidationAgreement extends ValidationWidget {
	private static final String TAG = Configuration.BASE_TAG + "ValidAgreement";

	private Context mContext;
	private Button btnAgreement;
	private Button btnPrivacy, btnTerms, btnWarranty;
	private boolean showAgreement = false;

	public ValidationAgreement(Context context) {
		super(context);
		mContext = context;
		_initView();
		_setView();
	}

	public ValidationAgreement(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_initView();
		_getAttrs(attrs);
		_setView();
	}

	public ValidationAgreement(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		_initView();
		_getAttrs(attrs, defStyle);
		_setView();
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_validation_agreement, this, false);
		addView(v);

		tvTitle = (TextView) v.findViewById(R.id.tv_validation_agreement_title);
		tvWarning = (TextView) v.findViewById(R.id.tv_validation_agreement_warning);
		ivValidationChecked = (ImageView) v.findViewById(R.id.iv_validation_agreement_checked);
		vUnderline = v.findViewById(R.id.v_validation_agreement_underline);
		tvWarning.setVisibility(View.GONE);

		btnAgreement = (Button)v.findViewById(R.id.btn_validation_agreement_agreement);
		btnAgreement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnAgreement.isSelected()) {
					_setAgreementSelected(false);
					setValid(false);
				} else {
					_setAgreementSelected(true);
					setValid(true);
				}
				if (mValidationUpdateListener != null) {
					mValidationUpdateListener.updateValidation();
				}
			}
		});

//		btnPrivacy = (Button)v.findViewById(R.id.btn_validation_agreement_privacy);
//		btnPrivacy.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String url = mServer
//				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//				mContext.startActivity(intent);
//			}
//		});
//		btnTerms = (Button)v.findViewById(R.id.btn_validation_agreement_terms);
//		btnTerms.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String url = ServerQueryManager.SERVICE_URL;
//				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//				mContext.startActivity(intent);
//			}
//		});

//		btnWarranty = (Button)v.findViewById(R.id.btn_validation_agreement_warranty);
//		btnWarranty.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String url = ServerQueryManager.WARRANTY_URL;
//				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//				mContext.startActivity(intent);
//			}
//		});

		_setAgreementSelected(false);
		setValid(false);
	}

	private void _setAgreementSelected(boolean selected) {
		if (selected) {
			btnAgreement.setSelected(true);
			btnAgreement.setTextColor(getContext().getResources().getColor(R.color.colorTextSelected));
		} else {
			btnAgreement.setSelected(false);
			btnAgreement.setTextColor(getContext().getResources().getColor(R.color.colorTextPrimary));
		}
	}

	private void _setView() {
		showUnderLine(showUnderline);
	}

	private void _getAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValidationWidget);
		_setTypeArray(typedArray);
	}

	private void _getAttrs(AttributeSet attrs, int defStyle) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValidationWidget, defStyle, 0);
		_setTypeArray(typedArray);
	}

	private void _setTypeArray(TypedArray typedArray) {
		tvTitle.setText(
				typedArray.getString(R.styleable.ValidationWidget_textTitle));
		tvWarning.setText(
				typedArray.getString(R.styleable.ValidationWidget_textWarning));

		showUnderline = typedArray.getBoolean(R.styleable.ValidationWidget_showUnderline, true);
		showAgreement = typedArray.getBoolean(R.styleable.ValidationWidget_showAgreement, false);
	}

	public boolean isShowingAgreement() {
		return showAgreement;
	}

	public boolean isAgreed() {
		return btnAgreement.isSelected();
	}
}