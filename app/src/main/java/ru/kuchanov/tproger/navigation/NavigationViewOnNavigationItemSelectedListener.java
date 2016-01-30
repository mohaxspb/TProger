package ru.kuchanov.tproger.navigation;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.activity.ActivitySettings;
import ru.kuchanov.tproger.utils.AttributeGetter;

public class NavigationViewOnNavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener
{
    private final static String LOG = NavigationViewOnNavigationItemSelectedListener.class.getSimpleName();

    private Context ctx;
    private DrawerUpdateSelected callbackDrawerSelected;
    private DrawerLayout drawerLayout;
    private ViewPager pager;

    public NavigationViewOnNavigationItemSelectedListener(DrawerUpdateSelected callbackDrawerSelected, DrawerLayout drawerLayout, ViewPager pager)
    {
        this.callbackDrawerSelected = callbackDrawerSelected;
        this.drawerLayout = drawerLayout;
        this.pager = pager;

        this.ctx = pager.getContext();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        int checkedDrawerItemId = menuItem.getItemId();
        Log.d(LOG, "onNavigationItemSelected called with checkedDrawerItemId: " + checkedDrawerItemId);

        String[] drawerItems = ctx.getResources().getStringArray(R.array.drawer_items);

        Snackbar snackbar;
        String snackBarMsg = "";
        int position = 0;
        switch (checkedDrawerItemId)
        {
            case R.id.tab_1:
                position = 0;
                snackBarMsg = drawerItems[position];
                pager.setCurrentItem(position, true);
                break;
            case R.id.tab_2:
                position = 1;
                snackBarMsg = drawerItems[position];
                pager.setCurrentItem(position, true);
                break;
            case R.id.tab_3:
                position = 2;
                snackBarMsg = drawerItems[position];
                pager.setCurrentItem(position, true);
                break;
            case R.id.tab_4:
//                position = 3;
//                snackBarMsg = drawerItems[position];
                drawerLayout.closeDrawer(GravityCompat.START);
                ctx.startActivity(new Intent(ctx, ActivitySettings.class));
                return false;
        }
        snackbar = Snackbar.make(pager, snackBarMsg, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        int colorId = AttributeGetter.getColor(pager.getContext(), R.attr.colorPrimaryDark);
        snackBarView.setBackgroundColor(colorId);
        snackbar.show();

        callbackDrawerSelected.updateNavigationViewState(checkedDrawerItemId);

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}