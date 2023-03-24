package goodmonit.monit.com.kao.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.UserInfo.Group;
import goodmonit.monit.com.kao.UserInfo.UserInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.devices.DeviceType;
import goodmonit.monit.com.kao.dialog.SimpleDialog;
import goodmonit.monit.com.kao.group.GroupDetailFragment;
import goodmonit.monit.com.kao.group.GroupNotificationFragment;
import goodmonit.monit.com.kao.group.GroupSharedFragment;
import goodmonit.monit.com.kao.managers.FirebaseAnalyticsManager;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.UserInfoManager;
import goodmonit.monit.com.kao.managers.ValidationManager;
import goodmonit.monit.com.kao.services.ConnectionManager;
import goodmonit.monit.com.kao.widget.NotoButton;

public class GroupActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Group";
    private static final boolean DBG = Configuration.DBG;

    private static final int VIEW_SHARING           = 0;
    private static final int VIEW_SHARED            = 1;
    private static final int VIEW_NOTIFICATION      = 2;

    private GroupDetailFragment mSharingFragment;
    private GroupSharedFragment mSharedFragment;
    private GroupNotificationFragment mNotificationFragment;

    private Button btnTabSharing, btnTabShared, btnTabNotification;
    private ImageView ivTabNotificationNew;

    private int mCurrentViewIndex;

    private SimpleDialog mDlgDeleteMember, mDlgLeaveGroup, mDlgInviteMember;
    private UserInfoManager mUserInfoMgr;
    private ValidationManager mValidationMgr;
    private Group mSelectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        _setToolBar();

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mUserInfoMgr = UserInfoManager.getInstance(this);
        mValidationMgr = new ValidationManager(this);

        mUserInfoMgr.refreshGroupList();

        _initView();

        mSharingFragment = new GroupDetailFragment();
        mSharedFragment = new GroupSharedFragment();
        mNotificationFragment = new GroupNotificationFragment();

        mSelectedGroup = mUserInfoMgr.getMyGroupInfo();
        mSharingFragment.setSelectedGroup(mSelectedGroup);

        //viewPager.setCurrentItem(VIEW_DIAPER_STATUS);
        _selectTabButton(VIEW_SHARING);
        _showFragment(VIEW_SHARING);

        if (ConnectionManager.getInstance() != null) {
            ConnectionManager.getInstance().getCloudNotificationFromCloudV2();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
        mConnectionMgr = ConnectionManager.getInstance(mHandler);
        updateNewMark();
    }

    public void updateNewMark() {
        if (mPreferenceMgr != null && ivTabNotificationNew != null) {
            long idx = mPreferenceMgr.getLatestSavedNotificationIndex(DeviceType.SYSTEM, 0, 0);
            if (DBG) Log.d(TAG, "updateNewMark : " + idx + " / " + mPreferenceMgr.getLatestCheckedNotificationIndex(DeviceType.SYSTEM, 0));
            if (mPreferenceMgr.getLatestCheckedNotificationIndex(DeviceType.SYSTEM, 0) < idx) {
                ivTabNotificationNew.setVisibility(View.VISIBLE);
            } else {
                ivTabNotificationNew.setVisibility(View.GONE);
            }
        }
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_light);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ivToolbarCenter = (ImageView) findViewById(R.id.iv_toolbar_main_light_center);
        ivToolbarCenter.setVisibility(View.GONE);
        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_main_light_center);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_main_light_right);
        btnToolbarRight.setVisibility(View.GONE);

        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_main_light_left);
        btnToolbarLeft.setBackgroundResource(R.drawable.ic_direction_left_black);
        btnToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
            }
        });

        tvToolbarTitle.setText(getString(R.string.group_mygroup));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
    }

    private void _initView() {
        rctnProgress = (RelativeLayout) findViewById(R.id.rctn_progress_bar);

        btnTabSharing = (Button)findViewById(R.id.btn_tabbar_device_detail_item1);
        btnTabSharing.setText(getString(R.string.group_title_sharing));
        btnTabSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showFragment(VIEW_SHARING);
                _selectTabButton(VIEW_SHARING);
            }
        });

        btnTabShared = (Button)findViewById(R.id.btn_tabbar_device_detail_item2);
        btnTabShared.setText(getString(R.string.group_title_shared));
        btnTabShared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showFragment(VIEW_SHARED);
                _selectTabButton(VIEW_SHARED);
            }
        });

        btnTabNotification = (Button)findViewById(R.id.btn_tabbar_device_detail_item3);
        btnTabNotification.setText(getString(R.string.tab_notification));
        btnTabNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showFragment(VIEW_NOTIFICATION);
                _selectTabButton(VIEW_NOTIFICATION);
            }
        });
        ivTabNotificationNew = (ImageView)findViewById(R.id.iv_tabbar_device_detail_item3_new);

        if (mDlgDeleteMember == null) {
            mDlgDeleteMember = new SimpleDialog(GroupActivity.this,
                    getString(R.string.group_delete_dialog_title),
                    "",
                    getString(R.string.btn_cancel),
                    null,
                    getString(R.string.btn_remove),
                    null);

            mDlgDeleteMember.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        if (mDlgLeaveGroup == null) {
            mDlgLeaveGroup = new SimpleDialog(GroupActivity.this,
                    getString(R.string.dialog_group_leave_title),
                    "",
                    getString(R.string.btn_cancel),
                    null,
                    getString(R.string.btn_group_leave),
                    null);

            mDlgLeaveGroup.setButtonColor(
                    getResources().getColor(R.color.colorTextPrimary),
                    getResources().getColor(R.color.colorTextWarning));
        }

        if (mDlgInviteMember == null) {
            mDlgInviteMember = new SimpleDialog(GroupActivity.this,
                    getString(R.string.group_invite_member),
                    "",
                    getString(R.string.btn_cancel),
                    null,
                    getString(R.string.btn_ok),
                    null);
            mDlgInviteMember.setInputMode(true);
            mDlgInviteMember.setHintText(getString(R.string.group_invite_member_hint));
            mDlgInviteMember.setContentsGravity(Gravity.LEFT);
        }
    }

    private void _showFragment(int idx) {
        Fragment fr = null;
        switch(idx) {
            case VIEW_SHARING:
                if (mSharingFragment == null) {
                    mSharingFragment = new GroupDetailFragment();
                }
                fr = mSharingFragment;
                break;
            case VIEW_SHARED:
                if (mSharedFragment == null) {
                    mSharedFragment = new GroupSharedFragment();
                }
                fr = mSharedFragment;
                break;
            case VIEW_NOTIFICATION:
                if (mNotificationFragment == null) {
                    mNotificationFragment = new GroupNotificationFragment();
                }
                fr = mNotificationFragment;
                break;
        }

        if (fr != null) {
            try {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();

                if (mCurrentViewIndex < idx) {
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                } else if (mCurrentViewIndex > idx) {
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right, R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                }

                mCurrentViewIndex = idx;
                fragmentTransaction.replace(R.id.fragment_group, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } catch (IllegalStateException e) {

            }
        }
    }

    private void _selectTabButton(int position) {
        mCurrentViewIndex = position;
        btnTabSharing.setSelected(false);
        btnTabSharing.setTextColor(getResources().getColor(R.color.colorTextGrey));
        ((NotoButton)btnTabSharing).setTypeface("regular");
        btnTabShared.setSelected(false);
        btnTabShared.setTextColor(getResources().getColor(R.color.colorTextGrey));
        ((NotoButton)btnTabShared).setTypeface("regular");
        btnTabNotification.setSelected(false);
        btnTabNotification.setTextColor(getResources().getColor(R.color.colorTextGrey));
        ((NotoButton)btnTabNotification).setTypeface("regular");

        switch (position) {
            case VIEW_SHARING:
                btnTabSharing.setSelected(true);
                btnTabSharing.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabSharing).setTypeface("medium");
                break;
            case VIEW_SHARED:
                btnTabShared.setSelected(true);
                btnTabShared.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabShared).setTypeface("medium");
                break;
            case VIEW_NOTIFICATION:
                btnTabNotification.setSelected(true);
                btnTabNotification.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                ((NotoButton)btnTabNotification).setTypeface("medium");
                break;
        }
    }

    private int mInviteMemberIndex = 0;
    public void showInviteMemberDialog(int index) {
        if (mDlgInviteMember != null) {
            mInviteMemberIndex = index;
            mDlgInviteMember.setContents(getString(R.string.group_invite_description) + getString(R.string.dialog_contents_input_invitee_short_id) + "\n" + getString(R.string.group_short_id_description));
            mDlgInviteMember.setInputText("");
            mDlgInviteMember.setButtonListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgInviteMember.dismiss();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String inputShortId = mDlgInviteMember.getInputText();
                            if (!mValidationMgr.isValidShortId(inputShortId)) {
                                showToast(getString(R.string.group_warning_invalid_short_id));
                            } else {
                                showProgressBar(true);
                                mServerQueryMgr.inviteCloudMember(inputShortId, mInviteMemberIndex, new ServerManager.ServerResponseListener() {
                                    @Override
                                    public void onReceive(int responseCode, String errCode, String data) {
                                        showProgressBar(false);
                                        if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                            try {
                                                JSONObject jObject = new JSONObject(data);
                                                String nickname = jObject.getString(mServerQueryMgr.getParameter(16));
                                                long accountId = jObject.getLong(mServerQueryMgr.getParameter(3));

                                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                                    showToast(getString(R.string.toast_invite_group_member_succeeded));
                                                    UserInfo addUserInfo = new UserInfo(accountId, mPreferenceMgr.getAccountId(), null, nickname, inputShortId, mInviteMemberIndex);
                                                    FirebaseAnalyticsManager.getInstance(mContext).sendGroupInvitation(mPreferenceMgr.getAccountId(), nickname, inputShortId);
                                                    mUserInfoMgr.addUserInfo(addUserInfo);
                                                    if (mSharingFragment != null && mCurrentViewIndex == VIEW_SHARING) {
                                                        mSharingFragment.mHandler.obtainMessage(GroupDetailFragment.MSG_REFRESH_VIEW).sendToTarget();
                                                    }
                                                } else {
                                                    showToast(getString(R.string.toast_invite_group_member_failed));
                                                }
                                            } catch (JSONException e) {
                                                if (DBG) Log.e(TAG, e.toString());
                                            }
                                        } else {
                                            showCommunicationErrorDialog(responseCode);
                                        }
                                        mDlgInviteMember.dismiss();
                                    }
                                });
                            }
                        }
                    });

            if (!mDlgInviteMember.isShowing()) {
                try {
                    mDlgInviteMember.show();
                } catch (Exception e) {

                }
            }
        }
    }

    private long mDeleteMemberAccountId = 0;
    public void showDeleteMemberDialog(long accountId) {
        if (mDlgDeleteMember != null) {
            Group myGroup = mUserInfoMgr.getMyGroupInfo();
            final UserInfo userInfo = myGroup.getUserInfo(accountId);
            mDeleteMemberAccountId = accountId;
            mDlgDeleteMember.setContents(userInfo.nickName + "(" + userInfo.shortId + ") " + getString(R.string.group_delete_dialog_description));
            mDlgDeleteMember.setButtonListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgDeleteMember.dismiss();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showProgressBar(true);
                            mServerQueryMgr.deleteCloudMember(mDeleteMemberAccountId, new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    showProgressBar(false);
                                    if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            mUserInfoMgr.removeUserInfo(userInfo);
                                            showToast(getString(R.string.toast_delete_group_member_succeeded));
                                            if (mSharingFragment != null && mCurrentViewIndex == VIEW_SHARING) {
                                                mSharingFragment.mHandler.obtainMessage(GroupDetailFragment.MSG_REFRESH_VIEW).sendToTarget();
                                            }
                                        } else {
                                            showToast(getString(R.string.toast_delete_group_member_failed));
                                        }
                                    } else {
                                        showCommunicationErrorDialog(responseCode);
                                    }
                                    mDlgDeleteMember.dismiss();
                                }
                            });
                        }
                    });

            if (!mDlgDeleteMember.isShowing()) {
                try {
                    mDlgDeleteMember.show();
                } catch (Exception e) {

                }
            }
        }
    }

    private long mLeaveCloudId = 0;
    public void showLeaveCloudDialog(final Group selectedGroup) {
        if (mDlgLeaveGroup != null) {
            mLeaveCloudId = selectedGroup.getLeaderInfo().accountId;
            mDlgLeaveGroup.setContents(getString(R.string.dialog_contents_group_leave_confirm, selectedGroup.getLeaderInfo().nickName, selectedGroup.getLeaderInfo().shortId));
            mDlgLeaveGroup.setButtonListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDlgLeaveGroup.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showProgressBar(true);
                            mServerQueryMgr.leaveCloud(mLeaveCloudId, new ServerManager.ServerResponseListener() {
                                @Override
                                public void onReceive(int responseCode, String errCode, String data) {
                                    showProgressBar(false);
                                    if (responseCode == ServerManager.RESPONSE_CODE_OK) {
                                        if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                            showToast(getString(R.string.toast_leave_group_succeeded));
                                            mUserInfoMgr.removeGroup(selectedGroup);
                                            if (mSharedFragment != null && mCurrentViewIndex == VIEW_SHARED) {
                                                mSharedFragment.mHandler.obtainMessage(GroupSharedFragment.MSG_REFRESH_VIEW).sendToTarget();
                                            }
                                        } else {
                                            showToast(getString(R.string.toast_leave_group_failed));
                                        }
                                    } else {
                                        showCommunicationErrorDialog(responseCode);
                                    }
                                    mDlgLeaveGroup.dismiss();
                                }
                            });
                        }
                    });
            if (!mDlgLeaveGroup.isShowing()) {
                try {
                    mDlgLeaveGroup.show();
                } catch (Exception e) {

                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ConnectionManager.MSG_NOTIFICATION_MESSAGE_UPDATED:
                    if (DBG) Log.d(TAG, "MSG_NOTIFICATION_MESSAGE_UPDATED");
                    updateNewMark();
                    if (mNotificationFragment != null && mCurrentViewIndex == VIEW_NOTIFICATION) {
                        mNotificationFragment.loadMessageList();
                        mNotificationFragment.showFilteredList();
                        ivTabNotificationNew.setVisibility(View.GONE);
                    }
                    break;
                case ConnectionManager.MSG_USER_INFO_UPDATED:
                    if (DBG) Log.d(TAG, "MSG_USER_INFO_UPDATED");
                    showToast(getString(R.string.toast_sharing_member_renewed));
                    if (mSharingFragment != null && mCurrentViewIndex == VIEW_SHARING) {
                        mSelectedGroup = mUserInfoMgr.getMyGroupInfo();
                        mSharingFragment.setSelectedGroup(mSelectedGroup);
                        mSharingFragment.mHandler.obtainMessage(GroupDetailFragment.MSG_REFRESH_VIEW).sendToTarget();
                    }
                    if (mSharedFragment != null && mCurrentViewIndex == VIEW_SHARED) {
                        mSharedFragment.mHandler.obtainMessage(GroupSharedFragment.MSG_REFRESH_VIEW).sendToTarget();
                    }
                    break;
            }
        }
    };
}
