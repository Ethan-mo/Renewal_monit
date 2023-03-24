package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.analytics.ScreenInfo;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.constants.InternetErrorCode;
import goodmonit.monit.com.kao.constants.Policy;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.managers.sm;

public class AgreementActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "Agree";
    private static final boolean DBG = Configuration.DBG;

    private static final int MSG_UPDATE_AGREEMENT           = 1;

    private sm mStringMgr;
    private Button btnAgreeAll;
    private Button btnAgreeTermsAndConditions, btnShowTermsAndConditions;
    private Button btnAgreePrivacy, btnShowPrivacy;
    private Button btnAgreeCollectInfo, btnShowCollectInfo;
    private Button btnAgreeProvide3rdParty, btnShowProvide3rdParty;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(this);
        mServerQueryMgr = ServerQueryManager.getInstance(this);
        mScreenInfo = new ScreenInfo(103);
        mStringMgr = new sm();
        _setToolBar();
        _initView();

        String agreedType = getIntent().getStringExtra(mStringMgr.getParameter(84));
        mHandler.obtainMessage(MSG_UPDATE_AGREEMENT, agreedType).sendToTarget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume");
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        btnToolbarLeft = (Button) findViewById(R.id.btn_toolbar_left);
        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvToolbarTitle.setText(getString(R.string.agreement_title));
        btnToolbarRight.setVisibility(View.GONE);
        btnToolbarLeft.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        showToast(getString(R.string.account_warning_agreement));
    }

    private boolean _checkConfirmAvailable() {
        return btnAgreeTermsAndConditions.isSelected()
                && btnAgreePrivacy.isSelected()
                && btnAgreeCollectInfo.isSelected()
                && btnAgreeProvide3rdParty.isSelected();
    }

    private void _updateConfirmButton() {
        if (_checkConfirmAvailable()) {
            btnConfirm.setEnabled(true);
            btnConfirm.setBackgroundResource(R.drawable.bg_btn_radius_green_darkgreen_selector);
            btnConfirm.setTextColor(getResources().getColor(R.color.colorWhite));
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackgroundResource(R.drawable.bg_btn_radius_white_solid_shadow_border);
            btnConfirm.setTextColor(getResources().getColor(R.color.colorTextNotSelected));
        }
    }

    private void _initView() {
        btnConfirm = (Button)findViewById(R.id.btn_agreement_activity_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateData = null;
                try {
                    JSONObject jobjTermsOfUse = new JSONObject();
                    jobjTermsOfUse.put(mServerQueryMgr.getParameter(20), Policy.TERMS_OF_USE_KR);
                    jobjTermsOfUse.put(mServerQueryMgr.getParameter(98), (btnAgreeTermsAndConditions.isSelected() ? 1 : 0));

                    JSONObject jobjPrivacy = new JSONObject();
                    jobjPrivacy.put(mServerQueryMgr.getParameter(20), Policy.PRIVACY_KR);
                    jobjPrivacy.put(mServerQueryMgr.getParameter(98), (btnAgreePrivacy.isSelected() ? 1 : 0));

                    JSONObject jobjCollectInfo = new JSONObject();
                    jobjCollectInfo.put(mServerQueryMgr.getParameter(20), Policy.COLLECT_INFO_KR);
                    jobjCollectInfo.put(mServerQueryMgr.getParameter(98), (btnAgreeCollectInfo.isSelected() ? 1 : 0));

                    JSONObject jobj3rdParty = new JSONObject();
                    jobj3rdParty.put(mServerQueryMgr.getParameter(20), Policy.PROVIDE_3RD_PARTY_KR);
                    jobj3rdParty.put(mServerQueryMgr.getParameter(98), (btnAgreeCollectInfo.isSelected() ? 1 : 0));

                    updateData = "[" +
                            jobjTermsOfUse.toString() + "," +
                            jobjPrivacy.toString() + "," +
                            jobjCollectInfo.toString() + "," +
                            jobj3rdParty.toString() + "," +
                            "]";
                } catch (JSONException e) {
                    if (DBG) Log.e(TAG, e.toString());
                } catch (NullPointerException e) {
                    if (DBG) Log.e(TAG, e.toString());
                }

                mServerQueryMgr.setPolicy(updateData,
                        new ServerManager.ServerResponseListener() {
                            @Override
                            public void onReceive(int responseCode, String errCode, String data) {
                                if (InternetErrorCode.SUCCEEDED.equals(errCode)) {
                                    if (DBG) Log.d(TAG, "setPolicy : " + data);
                                    finish();
                                } else if (InternetErrorCode.ERR_EXPIRED_TOKEN.equals(errCode) || InternetErrorCode.ERR_INVALID_TOKEN.equals(errCode)) {
                                    showToast(getString(R.string.dialog_contents_err_communication_with_server, errCode));
                                    finish();
                                } else {
                                    showToast(getString(R.string.dialog_contents_err_communication_with_server, errCode));
                                }
                            }
                        });
            }
        });

        btnAgreeAll = (Button)findViewById(R.id.btn_agreement_activity_all);
        btnAgreeAll.setSelected(false);
        btnAgreeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = !v.isSelected();
                btnAgreeAll.setSelected(selected);
                if (btnAgreeTermsAndConditions != null) {
                    btnAgreeTermsAndConditions.setSelected(selected);
                }
                if (btnAgreePrivacy != null) {
                    btnAgreePrivacy.setSelected(selected);
                }
                if (btnAgreeCollectInfo != null) {
                    btnAgreeCollectInfo.setSelected(selected);
                }
                if (btnAgreeProvide3rdParty != null) {
                    btnAgreeProvide3rdParty.setSelected(selected);
                }

                _updateConfirmButton();
            }
        });
        btnAgreeTermsAndConditions = (Button)findViewById(R.id.btn_agreement_activity_service);
        btnAgreeTermsAndConditions.setSelected(false);
        btnAgreeTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                _updateConfirmButton();
            }
        });
        btnAgreePrivacy = (Button)findViewById(R.id.btn_agreement_activity_privacy);
        btnAgreePrivacy.setSelected(false);
        btnAgreePrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                _updateConfirmButton();
            }
        });
        btnAgreeCollectInfo = (Button)findViewById(R.id.btn_agreement_activity_collectinfo);
        btnAgreeCollectInfo.setSelected(false);
        btnAgreeCollectInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                _updateConfirmButton();
            }
        });
        btnAgreeProvide3rdParty = (Button)findViewById(R.id.btn_agreement_activity_provide_3rd_party);
        btnAgreeProvide3rdParty.setSelected(false);
        btnAgreeProvide3rdParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                _updateConfirmButton();
            }
        });
        btnShowTermsAndConditions = (Button)findViewById(R.id.btn_agreement_activity_show_service);
        btnShowTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = null;
                url = mServerQueryMgr.getParameter(320);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        btnShowPrivacy = (Button)findViewById(R.id.btn_agreement_activity_show_privacy);
        btnShowPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = null;
                url = mServerQueryMgr.getParameter(340);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        btnShowCollectInfo = (Button)findViewById(R.id.btn_agreement_activity_show_collectinfo);
        btnShowCollectInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = null;
                url = mServerQueryMgr.getParameter(360);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        btnShowProvide3rdParty = (Button)findViewById(R.id.btn_agreement_activity_show_provide_3rd_party);
        btnShowProvide3rdParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = null;
                url = mServerQueryMgr.getParameter(380);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_AGREEMENT:
                    String data = (String)msg.obj;

                    try {
                        JSONObject jObject = new JSONObject(data);
                        String policyData = jObject.getString(mServerQueryMgr.getParameter(11));
                        JSONArray jarr = new JSONArray(policyData);
                        if (jarr != null) {
                            for (int i = 0; i < jarr.length(); i++) {
                                JSONObject jobj = jarr.getJSONObject(i);
                                if (jobj == null) continue;
                                int ptype = jobj.getInt(mServerQueryMgr.getParameter(20));
                                int agree = jobj.getInt(mServerQueryMgr.getParameter(98));
                                String time = jobj.getString(mServerQueryMgr.getParameter(15));

                                mPreferenceMgr.setPolicyAgreed(mPreferenceMgr.getAccountId(), ptype, agree);
                                mPreferenceMgr.setPolicySetTime(mPreferenceMgr.getAccountId(), ptype, time);
                                if (DBG) Log.d(TAG, "[policy] " + ptype + " / " + agree + " / " + time);
                            }
                        }
                    } catch (Exception e) {
                        if (DBG) Log.e(TAG, "JSONException: " + e.toString());
                    }

                    if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.TERMS_OF_USE_KR) == 1)
                            || (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_TERMS_OF_USE_KR) == 1)) {
                        btnAgreeTermsAndConditions.setSelected(true);
                    } else {
                        btnAgreeTermsAndConditions.setSelected(false);
                    }

                    if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PRIVACY_KR) == 1)
                            || (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PRIVACY_KR) == 1)) {
                        btnAgreePrivacy.setSelected(true);
                    } else {
                        btnAgreePrivacy.setSelected(false);
                    }

                    if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.COLLECT_INFO_KR) == 1)
                            || (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_COLLECT_INFO_KR) == 1)) {
                        btnAgreeCollectInfo.setSelected(true);
                    } else {
                        btnAgreeCollectInfo.setSelected(false);
                    }

                    if ((mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.PROVIDE_3RD_PARTY_KR) == 1)
                            || (mPreferenceMgr.getPolicyAgreed(mPreferenceMgr.getAccountId(), Policy.YK_PROVIDE_3RD_PARTY_KR) == 1)) {
                        btnAgreeProvide3rdParty.setSelected(true);
                    } else {
                        btnAgreeProvide3rdParty.setSelected(false);
                    }

                    _updateConfirmButton();
                    if (btnConfirm.isEnabled()) {
                        if (DBG) Log.d(TAG, "All agreed");
                        finish();
                        overridePendingTransition(0, 0);
                    }
                    break;
            }
        }
    };
}
