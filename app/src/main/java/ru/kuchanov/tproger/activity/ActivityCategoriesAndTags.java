package ru.kuchanov.tproger.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.DrawerUpdateSelected;
import ru.kuchanov.tproger.navigation.NavigationViewOnNavigationItemSelectedListener;
import ru.kuchanov.tproger.navigation.OnPageChangeListenerMain;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.anim.ChangeImageWithAlpha;

/**
 * Created by Юрий on 01.02.2016 17:16.
 * For TProger.
 */
public class ActivityCategoriesAndTags extends AppCompatActivity implements DrawerUpdateSelected
{
    private static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";
    private static final String KEY_PREV_COVER_SOURCE = "KEY_PREV_COVER_SOURCE";
    private final static String LOG = ActivityCategoriesAndTags.class.getSimpleName();
    private static final String NAV_ITEM_ID = "NAV_ITEM_ID";
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean drawerOpened;
    private ViewPager pager;
    private CoordinatorLayout coordinatorLayout;
    private int checkedDrawerItemId = R.id.tab_1;
    private boolean isCollapsed = true;
    private View cover2Border;
    private AppBarLayout appBar;

    private MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    private MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
    private NavigationViewOnNavigationItemSelectedListener navigationViewOnNavigationItemSelectedListener;
    private FloatingActionButton fab;
    //listeners for navView and pager
    private OnPageChangeListenerMain onPageChangeListenerMain;
    //    protected View cover2;
    private View coverThatChangesAlpha;
    private ImageView cover;
    private int verticalOffsetPrevious = 0;
    private Context ctx;
    private SharedPreferences pref;
    ///animations
    private ArrayList<Article> artsWithImage = new ArrayList<>();
    private int prevPosOfImage = -1;
    private Timer timer;
    private TimerTask timerTask;

    private ChangeImageWithAlpha cr;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate");

        this.ctx = this;

        //get default settings to get all settings later
        PreferenceManager.setDefaultValues(this, R.xml.pref_design, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_about, true);
        this.pref = PreferenceManager.getDefaultSharedPreferences(this);
        //set theme before super and set content to apply it
        int themeId = (pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false)) ? R.style.My_Theme_Dark : R.style.My_Theme_Light;
        this.setTheme(themeId);
        //call super after setTheme to set it 0_0
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cats__and_tags_tablet);

        this.initializeViews();
        this.setUpNavigationDrawer();
    }

    private void initializeViews()
    {
        cover = (ImageView) findViewById(R.id.cover);
        coverThatChangesAlpha = findViewById(R.id.cover_to_fill);
        cover2Border = findViewById(R.id.cover_2_border);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        appBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        pager = (ViewPager) findViewById(R.id.pager);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    protected void setUpNavigationDrawer()
    {
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.hello_world, R.string.hello_world)
            {
                public void onDrawerClosed(View view)
                {
                    supportInvalidateOptionsMenu();
                    drawerOpened = false;
                }

                public void onDrawerOpened(View drawerView)
                {
                    drawerOpened = true;
                    updateNavigationViewState(checkedDrawerItemId);
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }
        navigationViewOnNavigationItemSelectedListener = new NavigationViewOnNavigationItemSelectedListener(this, drawerLayout, pager);

        navigationView.setNavigationItemSelectedListener(navigationViewOnNavigationItemSelectedListener);

        updateNavigationViewState(this.checkedDrawerItemId);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart()
    {
//        Log.i(LOG, "onStart called!");
        super.onStart();
        BusProvider.getInstance().register(this);

        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
        if (!spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.start(ctx);
        }
    }

    @Override
    protected void onResume()
    {
//        Log.i(LOG, "onResume called!");
        super.onResume();

        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
        if (!spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.start(ctx);
        }
    }


    @Override
    protected void onStop()
    {
//        Log.i(LOG, "onStop called!");
        super.onStop();
        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
        BusProvider.getInstance().unregister(this);

        //stop and cancel all timers that manages animations
        if (timer != null && timerTask != null)
        {
            timerTask.cancel();
            timer.cancel();

            timer = null;
            timerTask = null;
        }
    }


    @Override
    protected void onPause()
    {
//        Log.i(LOG, "onPause called!");
        super.onPause();

        if (spiceManager.isStarted())
        {
            spiceManager.shouldStop();
        }
        if (spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.shouldStop();
        }
    }

    /* Called whenever we call supportInvalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onPrepareOptionsMenu called");
        //recreate navigationView's menu, uncheck all items and set new checked item
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer);
        navigationView.getMenu().findItem(R.id.tab_1).setChecked(false);
        navigationView.getMenu().findItem(R.id.tab_2).setChecked(false);
        navigationView.getMenu().findItem(R.id.tab_3).setChecked(false);
        navigationView.setCheckedItem(checkedDrawerItemId);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");
        int id = item.getItemId();

        boolean nightModeIsOn = this.pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false);
        boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);

        switch (id)
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                this.startActivity(intent);
                return true;
            case android.R.id.home:
                //TODO
                return true;
            case R.id.night_mode_switcher:
                this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, !nightModeIsOn).commit();
                this.recreate();
                return true;
            case R.id.list_style_switcher:
                this.pref.edit().putBoolean(ctx.getString(R.string.pref_design_key_list_style), !isGridManager).commit();
                this.supportInvalidateOptionsMenu();
                return true;
            case R.id.text_size_dialog:
                FragmentDialogTextAppearance frag = FragmentDialogTextAppearance.newInstance();
                frag.show(getFragmentManager(), "TextAppearance");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //workaround from http://stackoverflow.com/a/30337653/3212712 to show menu icons
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch (Exception e)
                {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }

            boolean nightModeIsOn = this.pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false);
            MenuItem themeMenuItem = menu.findItem(R.id.night_mode_switcher);
            if (nightModeIsOn)
            {
                themeMenuItem.setChecked(true);
            }

            boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
            MenuItem listStyleMenuItem = menu.findItem(R.id.list_style_switcher);
            if (isGridManager)
            {
                listStyleMenuItem.setChecked(true);
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, this.checkedDrawerItemId);
        outState.putBoolean(KEY_IS_COLLAPSED, isCollapsed);
        outState.putInt(KEY_PREV_COVER_SOURCE, this.prevPosOfImage);
        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE, artsWithImage);
    }

    @Override
    public void updateNavigationViewState(int checkedDrawerItemId)
    {
        this.checkedDrawerItemId = checkedDrawerItemId;
        supportInvalidateOptionsMenu();
    }
}
