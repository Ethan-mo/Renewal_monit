package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class TooltipBox extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "Tooltip";
	private static final boolean DBG = Configuration.DBG;
	private Context mContext;

	private RelativeLayout rctnBackground;
	private TextView tvTitle, tvContents;
	private Button btnClose;
	private String mTitle, mContents;
	private int mBackgroundRes;
	private View.OnClickListener mCloseButtonListener;
	private String mDescription;

	public TooltipBox(Context context) {
		super(context);
		mContext = context;
		_initView();
	}

	public TooltipBox(Context context,
                      String title,
                      String contents) {
		super(context);
		mContext = context;
		mTitle = title;
		mContents = contents;
		_initView();
	}

	public TooltipBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		_getAttrs(attrs);
		_initView();
	}

	public TooltipBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_getAttrs(attrs, defStyle);
		_initView();
	}

	private void _getAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TooltipBox);
		_setTypeArray(typedArray);
	}

	private void _getAttrs(AttributeSet attrs, int defStyle) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TooltipBox, defStyle, 0);
		_setTypeArray(typedArray);
	}

	private void _setTypeArray(TypedArray typedArray) {
		mTitle = typedArray.getString(R.styleable.TooltipBox_tooltipBoxTitle);
		mContents = typedArray.getString(R.styleable.TooltipBox_tooltipBoxContents);
		mBackgroundRes = typedArray.getResourceId(R.styleable.TooltipBox_tooltipBoxBackground, R.drawable.bg_tooltip_box_up_right);
	}

	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_tooltip_box, this, false);
		addView(v);

		rctnBackground = (RelativeLayout)v.findViewById(R.id.rctn_tooltip_box);
		tvTitle = (TextView)v.findViewById(R.id.tv_tooltip_box_title);
		tvContents = (TextView)v.findViewById(R.id.tv_tooltip_box_contents);
		btnClose = (Button)v.findViewById(R.id.btn_tooltip_box_close);

		if (mTitle == null || mTitle.length() == 0) {
			tvTitle.setVisibility(View.GONE);
		} else {
			tvTitle.setText(mTitle);
		}

		if (mContents == null || mContents.length() == 0) {
			tvContents.setVisibility(View.GONE);
		} else {
			tvContents.setText(mContents);
		}
		rctnBackground.setBackgroundResource(mBackgroundRes);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TooltipBox.this.setVisibility(View.GONE);
				if (mCloseButtonListener != null) {
					mCloseButtonListener.onClick(v);
				}
			}
		});
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setCloseButtonListener(OnClickListener listener) {
		mCloseButtonListener = listener;
	}
}