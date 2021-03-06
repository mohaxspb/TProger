package ru.kuchanov.tproger.navigation;

import android.support.v4.view.ViewPager;
import android.util.Log;

import ru.kuchanov.tproger.R;

public class OnPageChangeListenerMain extends ViewPager.SimpleOnPageChangeListener
{
    private final static String LOG = OnPageChangeListenerMain.class.getSimpleName();

    private DrawerUpdateSelected drawerUpdateSelected;
    private FabUpdater fabUpdater;

    public OnPageChangeListenerMain(DrawerUpdateSelected drawerUpdateSelected, FabUpdater fabUpdater)
    {
        this.drawerUpdateSelected = drawerUpdateSelected;
        this.fabUpdater = fabUpdater;
    }

    @Override
    public void onPageSelected(int position)
    {
        Log.i(LOG, "onPageSelected with position: " + position);
        int checkedDrawerItemId = R.id.tab_1;
        switch (position)
        {
            case 0:
                checkedDrawerItemId = R.id.tab_1;
                break;
            case 1:
                checkedDrawerItemId = R.id.tab_2;
                break;
            case 2:
                checkedDrawerItemId = R.id.tab_3;
                break;
        }
        drawerUpdateSelected.updateNavigationViewState(checkedDrawerItemId);
        fabUpdater.updateFAB(position);
    }
}