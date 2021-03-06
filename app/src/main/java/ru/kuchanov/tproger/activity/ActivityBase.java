package ru.kuchanov.tproger.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.lang.reflect.Method;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.otto.SingltonOtto;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.utils.AttributeGetter;

/**
 * Created by Юрий on 29.03.2016 16:54.
 * For TProger.
 */
public abstract class ActivityBase extends AppCompatActivity
{
    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;

    protected MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    protected MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();

    /**
     * should init it in onCreate
     */
    protected Context ctx;
    protected SharedPreferences pref;

    protected abstract void initializeViews();

    protected void setUpNavigationDrawer(boolean showHamburger, NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener)
    {
        //changing statusBarColor
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().setStatusBarColor(AttributeGetter.getColor(ctx, R.attr.colorPrimaryDark));
        }

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
                }

                public void onDrawerOpened(View drawerView)
                {

                }
            };
            //show arrow instead of hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(showHamburger);

            drawerLayout.addDrawerListener(mDrawerToggle);
        }

        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
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
    protected void onStart()
    {
//        Log.i(LOG, "onStart called!");
        super.onStart();
        SingltonOtto.getInstance().register(this);

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

    @Override
    protected void onStop()
    {
//        Log.i(LOG, "onStop called!");
        super.onStop();
        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
        SingltonOtto.getInstance().unregister(this);
    }

    /**
     * workaround from http://stackoverflow.com/a/30337653/3212712 to show menu icons
     */
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

            boolean nightModeIsOn = this.pref.getBoolean(getString(R.string.pref_design_key_night_mode), false);
            MenuItem themeMenuItem = menu.findItem(R.id.night_mode_switcher);
            if (nightModeIsOn && themeMenuItem != null)
            {
                themeMenuItem.setChecked(true);
            }

            boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
            MenuItem listStyleMenuItem = menu.findItem(R.id.list_style_switcher);
            if (isGridManager && listStyleMenuItem != null)
            {
                listStyleMenuItem.setChecked(true);
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void onBackPressed()
    {
//        Log.i(LOG, "onBackPressed");
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }
}