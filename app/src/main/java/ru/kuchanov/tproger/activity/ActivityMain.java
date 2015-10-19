package ru.kuchanov.tproger.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.navigation.DrawerUpdateSelected;
import ru.kuchanov.tproger.navigation.ImageChanger;
import ru.kuchanov.tproger.navigation.NavigationViewOnNavigationItemSelectedListener;
import ru.kuchanov.tproger.navigation.PagerAdapterMain;
import ru.kuchanov.tproger.navigation.PagerAdapterOnPageChangeListener;
import ru.kuchanov.tproger.navigation.TabLayoutOnTabSelectedListener;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventCollapsed;
import ru.kuchanov.tproger.otto.EventExpanded;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.utils.ScreenProperties;

public class ActivityMain extends AppCompatActivity implements DrawerUpdateSelected, ImageChanger
{
    protected static final String NAV_ITEM_ID = "NAV_ITEM_ID";
    protected static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";

    private final static String LOG = ActivityMain.class.getSimpleName();

    protected final int[] coverImgsIds = {R.drawable.drawer_header, R.drawable.cremlin, R.drawable.petergof};
    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected ImageView cover;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected boolean drawerOpened;
    protected ViewPager pager;
    protected int checkedDrawerItemId;
    protected SharedPreferences pref;
    protected boolean isCollapsed;
    protected View cover2;
    protected AppBarLayout appBar;
    //    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected TabLayout tabLayout;
    private Context ctx;

    protected int verticalOffsetPrevious=0;
    protected boolean fullyExpanded=true;

    public boolean isFullyExpanded()
    {
        return fullyExpanded;
    }
    protected AppBarLayout.OnOffsetChangedListener onOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener()
    {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
        {
            if(verticalOffset<0)
            {
                BusProvider.getInstance().post(new EventCollapsed());
                if(verticalOffsetPrevious==0)
                {
                    BusProvider.getInstance().post(new EventCollapsed());
                    fullyExpanded=false;
                }
            }
            else
            {
                if(verticalOffsetPrevious<0)
                {
                    BusProvider.getInstance().post(new EventExpanded());
                    fullyExpanded=true;
                }
            }
            verticalOffsetPrevious=verticalOffset;
//            Log.i(LOG, "verticalOffset: "+verticalOffset);

                    //move backgroubng image and its bottom border
                    cover.setY(verticalOffset * 0.7f);
            View cover2outSide = findViewById(R.id.cover_2);
            cover2outSide.setY(verticalOffset * 0.7f);

            if (verticalOffset < -appBarLayout.getHeight() * 0.7f)
            {
                if (cover.getAlpha() != 0)
                {
                    cover.animate().alpha(0).setDuration(600);
                }
            }
            else
            {
                //show cover if we start to expand collapsingToolbarLayout
                int heightOfToolbarAndStatusBar = toolbar.getHeight() + ScreenProperties.getStatusBarHeight(ctx);
                int s = appBarLayout.getHeight() - heightOfToolbarAndStatusBar;
                isCollapsed = (verticalOffset > -s);// ? false : true;
                if (cover.getAlpha() < 1 && isCollapsed)
                {
                    cover.animate().alpha(1).setDuration(600);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG, "onCreate");

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

        setContentView(R.layout.activity_main);

        restoreState(savedInstanceState);

        setUpNavigationDrawer();
        setUpPagerAndTabs();

        appBar = (AppBarLayout) this.findViewById(R.id.app_bar_layout);
        appBar.setExpanded(isCollapsed, true);
        appBar.addOnOffsetChangedListener(onOffsetChangedListener);

        setUpBackgroundAnimation();

        MyRoboSpiceDatabaseHelper helper=new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

        try
        {
            ArrayList<Article> list= (ArrayList<Article>) helper.getDao(Article.class).queryForAll();
            int listSize=list.size();
            Log.i(LOG, "listSize= "+listSize);

            for (Article a:list)
            {
                Log.i(LOG, "Article.getId()= "+a.getId());
            }

            ArrayList<Articles> listArts= (ArrayList<Articles>) helper.getDao(Articles.class).queryForAll();
            int listSizeArts=listArts.size();
            Log.i(LOG, "listSize= "+listSizeArts);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void restoreState(Bundle state)
    {
        if (state == null)
        {
            checkedDrawerItemId = R.id.tab_1;
            isCollapsed = true;
        }
        else
        {
            checkedDrawerItemId = state.getInt(NAV_ITEM_ID, R.id.tab_1);
            isCollapsed = state.getBoolean(KEY_IS_COLLAPSED, false);
        }
    }

    private void setUpBackgroundAnimation()
    {
        cover = (ImageView) findViewById(R.id.cover);
        cover.setAlpha(0f);
        cover.setScaleX(1.3f);
        cover.setScaleY(1.3f);
        cover.animate().alpha(1).setDuration(600);

        cover2 = findViewById(R.id.cover_2_inside);
        cover2.setAlpha(0);

        this.startAnimation();
    }

    private void setUpPagerAndTabs()
    {
        pager = (ViewPager) this.findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapterMain(this.getSupportFragmentManager(), 3));
        pager.addOnPageChangeListener(new PagerAdapterOnPageChangeListener(this, this));

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 111111111111"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 222222222222"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 333333333333"));

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayoutOnTabSelectedListener(this, pager));
    }

    protected void setUpNavigationDrawer()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);

            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        NavigationViewOnNavigationItemSelectedListener navList;
        navList = new NavigationViewOnNavigationItemSelectedListener(this, drawerLayout, pager);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(navList);

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
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");

        int id = item.getItemId();

        boolean nightModeIsOn = this.pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false);

        switch (id)
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                this.startActivity(intent);
                return true;
            case android.R.id.home:
                this.drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.night_mode_switcher:
                if (nightModeIsOn)
                {
                    this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false).commit();
                }
                else
                {
                    this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, true).commit();
                }
                this.recreate();
                return true;
            case R.id.debug:
                MyRoboSpiceDatabaseHelper h=new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

                try
                {
                    h.clearTableFromDataBase(Article.class);
                    h.clearTableFromDataBase(Articles.class);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, this.checkedDrawerItemId);
        outState.putBoolean(KEY_IS_COLLAPSED, isCollapsed);

    }

    @Override
    public void updateNavigationViewState(int checkedDrawerItemId)
    {
        this.checkedDrawerItemId = checkedDrawerItemId;
        supportInvalidateOptionsMenu();
    }

    //workaround from http://stackoverflow.com/a/30337653/3212712
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
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void updateImage(final int positionInPager)
    {
//        Log.i(LOG, "updateImage with position in pager: "+positionInPager);

        cover2.setAlpha(0);
        cover2.setScaleX(1);
        cover2.setScaleY(1);
        cover2.animate().cancel();

        Log.e(LOG, "isCollapsed: " + String.valueOf(isCollapsed));
        if (!isCollapsed)
        {
            final AppBarLayout appBar = (AppBarLayout) this.findViewById(R.id.app_bar_layout);
            appBar.setExpanded(true, true);
        }

        //prevent showing transition coloring if cover isn't showing
        if (this.cover.getAlpha() == 0)
        {
            cover.setImageResource(coverImgsIds[positionInPager]);
            return;
        }

        cover2.animate().alpha(1).scaleX(15).scaleY(15).setDuration(600).setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                cover.setImageResource(coverImgsIds[positionInPager]);
                cover2.animate().alpha(0).setDuration(600);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });
    }

    public void startAnimation()
    {
        cover.setVisibility(View.VISIBLE);

        final int animResId = R.anim.cover_image;

        Animation anim = AnimationUtils.loadAnimation(this, animResId);
        anim.setAnimationListener(new Animation.AnimationListener()
        {

            @Override
            public void onAnimationEnd(Animation arg0)
            {
                Animation anim = AnimationUtils.loadAnimation(ctx, animResId);
                anim.setAnimationListener(this);
                cover.startAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation arg0)
            {
            }

            @Override
            public void onAnimationStart(Animation arg0)
            {
            }
        });

        cover.startAnimation(anim);
    }

    public boolean getIsCollapsed()
    {
        return this.isCollapsed;
    }
}