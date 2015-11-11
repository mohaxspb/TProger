package ru.kuchanov.tproger.navigation;

import android.support.design.widget.AppBarLayout;

import ru.kuchanov.tproger.activity.ActivityMain;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventCollapsed;
import ru.kuchanov.tproger.otto.EventExpanded;
import ru.kuchanov.tproger.utils.ScreenProperties;

/**
 * Created by Юрий on 11.11.2015 18:58.
 * For ExpListTest.
 */
public class MyOnOffsetChangedListener implements AppBarLayout.OnOffsetChangedListener
{
    ActivityMain activityMain;

    public MyOnOffsetChangedListener(ActivityMain activityMain)
    {
        this.activityMain = activityMain;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
    {
        if (verticalOffset < 0)
        {
            BusProvider.getInstance().post(new EventCollapsed());
            if (activityMain.getVerticalOffsetPrevious() == 0)
            {
                BusProvider.getInstance().post(new EventCollapsed());
                activityMain.setFullyExpanded(false);
            }
        }
        else
        {
            if (activityMain.getVerticalOffsetPrevious() < 0)
            {
                BusProvider.getInstance().post(new EventExpanded());
                activityMain.setFullyExpanded(true);
            }
        }
        activityMain.setVerticalOffsetPrevious(verticalOffset);
//            Log.i(LOG, "verticalOffset: "+verticalOffset);

        //move background image and its bottom border
        activityMain.getCover().setY(verticalOffset * 0.7f);
       activityMain.getCover2().setY(verticalOffset * 0.7f);

        if (verticalOffset < -appBarLayout.getHeight() * 0.7f)
        {
            if (activityMain.getCover().getAlpha() != 0)
            {
                activityMain.getCover().animate().alpha(0).setDuration(600);
            }
        }
        else
        {
            //show cover if we start to expand collapsingToolbarLayout
            int heightOfToolbarAndStatusBar = activityMain.getToolbar().getHeight() + ScreenProperties.getStatusBarHeight(activityMain);
            int s = appBarLayout.getHeight() - heightOfToolbarAndStatusBar;
            activityMain.setCollapsed(verticalOffset > -s);
//            isCollapsed = (verticalOffset > -s);// ? false : true;
            if (activityMain.getCover().getAlpha() < 1 && activityMain.getIsCollapsed())
            {
                activityMain.getCover().animate().alpha(1).setDuration(600);
            }
        }
    }
}
