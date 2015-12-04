package ru.kuchanov.tproger.navigation;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.activity.ActivityMain;

public class OnNavigationItemSelectedListenerArticleActivity implements NavigationView.OnNavigationItemSelectedListener
{
    Context ctx;

    public OnNavigationItemSelectedListenerArticleActivity(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        int checkedDrawerItemId = menuItem.getItemId();

        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        switch (checkedDrawerItemId)
        {
            case R.id.tab_1:
                intent.setClass(ctx, ActivityMain.class);
                intent.putExtra(ActivityMain.NAV_ITEM_ID, R.id.tab_1);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                break;
            case R.id.tab_2:
                intent.setClass(ctx, ActivityMain.class);
                intent.putExtra(ActivityMain.NAV_ITEM_ID, R.id.tab_2);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                break;
            case R.id.tab_3:
                intent.setClass(ctx, ActivityMain.class);
                intent.putExtra(ActivityMain.NAV_ITEM_ID, R.id.tab_3);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                break;
        }

        ctx.startActivity(intent);

        return true;
    }
}