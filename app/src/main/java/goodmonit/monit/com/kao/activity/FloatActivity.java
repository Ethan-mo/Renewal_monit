package goodmonit.monit.com.kao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.message.NotificationType;

public class FloatActivity extends BaseActivity {
    private static final String TAG = Configuration.BASE_TAG + "FloatActvity";
    private static final boolean DBG = Configuration.DBG;

    private Button btnClose, btnAddDiapeChanged, btnAddChat;
    private Button btnAddSleep;
    private Button btnAddFeedingBottleFormulaMilk;
    //private Button btnAddDiaperMixed, btnAddPee, btnAddPoo;
    //private Button btnAddFeedingNursedBreastMilk, btnAddFeedingBottleBreastMilk, btnAddFeedingBabyFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);

        mContext = this;

        _initView();
    }

    private void _initView() {
        btnClose = (Button)findViewById(R.id.btn_float_notification_close);
        btnAddDiapeChanged = (Button)findViewById(R.id.btn_float_notification_add_diaper);
        btnAddChat = (Button)findViewById(R.id.btn_float_notification_add_chat);
        btnAddSleep = (Button)findViewById(R.id.btn_float_notification_add_sleep);
        btnAddFeedingBottleFormulaMilk = (Button)findViewById(R.id.btn_float_notification_add_feeding_bottle_formula_milk);
        //btnAddFeedingBabyFood = (Button)findViewById(R.id.btn_float_notification_add_feeding_baby_food);
        //btnAddPee = (Button)findViewById(R.id.btn_float_notification_add_pee);
        //btnAddPoo = (Button)findViewById(R.id.btn_float_notification_add_poo);
        //btnAddDiaperMixed = (Button)findViewById(R.id.btn_float_notification_add_mixed);
        //btnAddFeedingNursedBreastMilk = (Button)findViewById(R.id.btn_float_notification_add_feeding_nursed_breast_milk);
        //btnAddFeedingBottleBreastMilk = (Button)findViewById(R.id.btn_float_notification_add_feeding_bottle_breast_milk);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(-1, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddDiapeChanged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.DIAPER_CHANGED, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.CHAT_USER_FEEDBACK, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.BABY_SLEEP, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddFeedingBottleFormulaMilk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.BABY_FEEDING_BOTTLE_FORMULA_MILK, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

/*
        btnAddFeedingBabyFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.BABY_FEEDING_BABY_FOOD, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddPee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.PEE_DETECTED, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddPoo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.POO_DETECTED, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddDiaperMixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.ABNORMAL_DETECTED, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddFeedingBottleBreastMilk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.BABY_FEEDING_BOTTLE_BREAST_MILK, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnAddFeedingNursedBreastMilk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(NotificationType.BABY_FEEDING_NURSED_BREAST_MILK, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(-1, intent);
        finish();
        overridePendingTransition(0, 0);
    }

}
