package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;

public class GroupMember extends LinearLayout {
	private static final String TAG = Configuration.BASE_TAG + "GroupMember";

	private TextView tvNickName;
	private TextView tvDetail;
	private TextView tvLeader;
    private Button btnRemove;
    private ViewSwitcher viewSwitcher;
    private View vSameCategoryDivider, vOtherCategoryDivider;

    private boolean isRemovable = true;
	private boolean isLeader = false;
	private OnClickListener mClickListener;
    private OnClickListener mRemoveListener;
	private String mNickName, mShortId, mEmail;

    private float mDefaultSwipeRange = 100;
    private float mPosX;

	public GroupMember(Context context) {
		super(context);
		_initView();
		_setView();
	}

	public GroupMember(Context context, AttributeSet attrs) {
		super(context, attrs);
		_initView();
		_setView();
	}

	public GroupMember(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_initView();
		_setView();
	}


	private void _initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.widget_group_member, this, false);
		addView(v);
        viewSwitcher = (ViewSwitcher)v.findViewById(R.id.vs_widget_group_member);
        vSameCategoryDivider = v.findViewById(R.id.v_widget_group_member_divider_same_category);
        vOtherCategoryDivider = v.findViewById(R.id.v_widget_group_member_divider_other_category);
		tvNickName = (TextView) v.findViewById(R.id.tv_widget_group_member_nickname);
        tvDetail = (TextView) v.findViewById(R.id.tv_widget_group_member_shortid);
		tvLeader = (TextView) v.findViewById(R.id.tv_widget_group_member_leader);
        btnRemove = (Button)v.findViewById(R.id.btn_widget_group_member_delete);
        btnRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRemoveListener != null) {
                    mRemoveListener.onClick(v);
                }
            }
        });

		tvLeader.setVisibility(View.GONE);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float posX = event.getX();
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPosX = posX;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mPosX - posX > mDefaultSwipeRange) {
                            if (viewSwitcher.getCurrentView().getId() == R.id.rctn_widget_group_member_show_info) {
                                viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_in_from_right));
                                viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_out_to_left));
                                viewSwitcher.showNext();
                                mPosX = posX;
                            }
                            return false;
                        } else if (mPosX - posX < - mDefaultSwipeRange) {
                            if (viewSwitcher.getCurrentView().getId() == R.id.rctn_widget_group_member_delete_info) {
                                viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_in_from_left));
                                viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_out_to_right));
                                viewSwitcher.showNext();
                                mPosX = posX;
                            }
                            return false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mPosX - posX < 50 && mPosX - posX > -50) {
                            if (mClickListener != null) {
                                mClickListener.onClick(null);
                            }
                        }
                        break;
                }
                return true;
            }
        });
        setOtherCategoryDivider(true);
        setRemovable(true);
	}

	public void setRemovable(boolean removable) {
        isRemovable = removable;
        if (removable) {
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float posX = event.getX();
                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mPosX = posX;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mPosX - posX > mDefaultSwipeRange) {
                                if (viewSwitcher.getCurrentView().getId() == R.id.rctn_widget_group_member_show_info) {
                                    viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_in_from_right));
                                    viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_out_to_left));
                                    viewSwitcher.showNext();
                                    mPosX = posX;
                                }
                                return false;
                            } else if (mPosX - posX < - mDefaultSwipeRange) {
                                if (viewSwitcher.getCurrentView().getId() == R.id.rctn_widget_group_member_delete_info) {
                                    viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_in_from_left));
                                    viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_out_to_right));
                                    viewSwitcher.showNext();
                                    mPosX = posX;
                                }
                                return false;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (mPosX - posX < 50 && mPosX - posX > -50) {
                                if (mClickListener != null) {
                                    mClickListener.onClick(null);
                                }
                            }
                            break;
                    }
                    return true;
                }
            });
        } else {
            this.setOnTouchListener(null);
        }
    }

	private void _setView() {
		setLeader(false);
	}

	public void setNickName(String name) {
		mNickName = name;
		tvNickName.setText(name);
	}

	public void setShortId(String shortid) {
		mShortId = shortid;
        tvDetail.setText("#" + shortid);
	}

    public void setEmail(String email) {
        mEmail = email;
        tvDetail.setText(email);
    }

    public String getEmail() {
        return mEmail;
    }

	public String getNickname() {
		return mNickName;
	}

	public String getShortId() {
		return mShortId;
	}

	public boolean isLeader() { return isLeader; }

	public void setLeader(boolean leader) {
		isLeader = leader;
		if (leader) {
			tvLeader.setVisibility(View.VISIBLE);
		} else {
			tvLeader.setVisibility(View.GONE);
		}
	}

    public void setOnRemoveListener(OnClickListener listener) {
        mRemoveListener = listener;
    }

	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}

	public void setOtherCategoryDivider(boolean other) {
        if (other) {
            vOtherCategoryDivider.setVisibility(View.VISIBLE);
            vSameCategoryDivider.setVisibility(View.GONE);
        } else {
            vOtherCategoryDivider.setVisibility(View.GONE);
            vSameCategoryDivider.setVisibility(View.VISIBLE);
        }
    }

    public void showMainView() {
        if (viewSwitcher.getCurrentView().getId() == R.id.rctn_widget_group_member_delete_info) {
            viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_in_from_left));
            viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_slide_out_to_right));
            viewSwitcher.showNext();
        }
    }
}