package ru.kuchanov.tproger.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.robospice.MySpiceManager;

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
    protected boolean isTabletMode;

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
        BusProvider.getInstance().unregister(this);
    }
}
