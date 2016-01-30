package ru.kuchanov.tproger.navigation;

import android.support.v4.view.ViewPager;
import android.util.Log;

import ru.kuchanov.tproger.R;

public class OnPageChangeListenerMain implements ViewPager.OnPageChangeListener
{
    private final static String LOG = OnPageChangeListenerMain.class.getSimpleName();

    private DrawerUpdateSelected drawerUpdateSelected;
    private ImageChanger imageChanger;
    private FabUpdater fabUpdater;

    public OnPageChangeListenerMain(DrawerUpdateSelected drawerUpdateSelected, ImageChanger imageChanger, FabUpdater fabUpdater)
    {
        this.drawerUpdateSelected = drawerUpdateSelected;
        this.imageChanger = imageChanger;
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
        imageChanger.updateImage(position);
        fabUpdater.updateFAB(position);
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }
}