package goodmonit.monit.com.kao.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.fragment.BaseFragment;
import goodmonit.monit.com.kao.fragment.DiaryFragment;
import goodmonit.monit.com.kao.fragment.MessageFragment;
import goodmonit.monit.com.kao.managers.PreferenceManager;
import goodmonit.monit.com.kao.managers.ServerQueryManager;
import goodmonit.monit.com.kao.setting.SettingFragment;

public class MainActivity extends BaseActivity {
        //implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = Configuration.BASE_TAG + "MainActivity";
    private static final boolean DBG = Configuration.DBG;

    public static final String BROADCAST_MSG_UPDATE_TAB_BADGE	= "goodmonit.monit.com.BROADCAST_MSG_UPDATE_TAB_BADGE";
    public static final String EXTRA_TAB_ID     = "tabId";
    public static final String EXTRA_SHOW_BADGE = "showBadge";
    public static final String EXTRA_BADGE_TEXT = "badgeText";

    private ViewPager viewPager;
    private ImageButton[] ibtnTab = new ImageButton[5];
    private TabPagerAdapter tabPagerAdapter;
    private TextView tvTabTitle;

    //private SensorStatusFragment mSensorFragment;
    private DiaryFragment mDiaryFragment;
    private SettingFragment mSettingFragment;
    private MessageFragment mMessageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        */

        _setToolBar();

        mContext = this;
        mPreferenceMgr = PreferenceManager.getInstance(mContext);
        mServerQueryMgr = ServerQueryManager.getInstance(mContext);

        _initView();

        int startFragment = getIntent().getIntExtra(BaseFragment.START_FRAGMENT, -1);
        if (startFragment == -1) {
            startFragment = mPreferenceMgr.getLatestForegroundFragmentId();
        }

        selectTab(startFragment);

        mServerQueryMgr.updatePushToken();
    }

    private void _setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        tvTabTitle = (TextView)findViewById(R.id.tv_toolbar_title);
        btnToolbarRight = (Button) findViewById(R.id.btn_toolbar_right);
        btnToolbarRight.setVisibility(View.GONE);
    }

    public void setToolbarTitle(String title) {
        tvTabTitle.setText(title);
    }

    private void _initView() {
        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.tab_viewpager);

        ibtnTab[0] = (ImageButton)findViewById(R.id.ibtn_tab_sensor);
        ibtnTab[1] = (ImageButton)findViewById(R.id.ibtn_tab_message);
        ibtnTab[2] = (ImageButton)findViewById(R.id.ibtn_tab_diary);
        ibtnTab[3] = (ImageButton)findViewById(R.id.ibtn_tab_user);
        ibtnTab[4] = (ImageButton)findViewById(R.id.ibtn_tab_setting);
        for (int i = 0; i < ibtnTab.length; i++) {
            final int position = i;
            ibtnTab[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTab(position);
                }
            });
        }

        // Creating TabPagerAdapter adapter
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), 5);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                _selectTabButton(position);
                setToolbarTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void selectTab(int position) {
        viewPager.setCurrentItem(position);
        _selectTabButton(position);
        setToolbarTitle(position);
    }

    public void setToolbarTitle(int position) {
        switch(position) {
            case BaseFragment.ID_SENSOR_STATUS:
                setToolbarTitle(getString(R.string.title_sensor_status));
                break;
            case BaseFragment.ID_MESSAGE:
                setToolbarTitle(getString(R.string.title_message));
                break;
            case BaseFragment.ID_DIARY:
                setToolbarTitle(getString(R.string.title_diary));
                break;
            case BaseFragment.ID_USER:
                setToolbarTitle(getString(R.string.title_userinfo));
                break;
            case BaseFragment.ID_SETTING:
                setToolbarTitle(getString(R.string.title_setting));
                break;
        }
    }

    private void _selectTabButton(int position) {
        for (int i = 0; i < ibtnTab.length; i++) {
            if (i == position) {
                ibtnTab[position].setSelected(true);
            } else {
                ibtnTab[i].setSelected(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class TabPagerAdapter extends FragmentStatePagerAdapter {

        // Count number of tabs
        private int tabCount;

        public TabPagerAdapter(FragmentManager fm, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            // Returning the current tabs
            switch (position) {
                case BaseFragment.ID_SENSOR_STATUS:
                    /*
                    if (mSensorFragment == null) {
                        if (DBG) Log.i(TAG, "create SensorStatusFragment");
                        mSensorFragment = new SensorStatusFragment();
                    }
                    return mSensorFragment;
                    */
                case BaseFragment.ID_MESSAGE:
                    if (mMessageFragment == null) {
                        if (DBG) Log.i(TAG, "create mMessageFragment");
                        mMessageFragment = new MessageFragment();
                    }
                    return mMessageFragment;
                case BaseFragment.ID_DIARY:
                    if (mDiaryFragment == null) {
                        if (DBG) Log.i(TAG, "create mDiaryFragment");
                        mDiaryFragment = new DiaryFragment();
                    }
                    return mDiaryFragment;
                case BaseFragment.ID_SETTING:
                    if (mSettingFragment == null) {
                        if (DBG) Log.i(TAG, "create mSettingFragment");
                        mSettingFragment = new SettingFragment();
                    }
                    return mSettingFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean createOptionMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    */

    /*
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    */
}
