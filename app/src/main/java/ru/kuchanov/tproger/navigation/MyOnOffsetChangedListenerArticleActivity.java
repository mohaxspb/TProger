package ru.kuchanov.tproger.navigation;

import android.support.design.widget.AppBarLayout;

import ru.kuchanov.tproger.activity.ActivityArticle;
import ru.kuchanov.tproger.utils.DipToPx;
import ru.kuchanov.tproger.utils.ScreenProperties;

/**
 * Created by Юрий on 11.11.2015 18:58 21:11.
 * For TProger.
 */
public class MyOnOffsetChangedListenerArticleActivity implements AppBarLayout.OnOffsetChangedListener
{
    public static final String LOG = MyOnOffsetChangedListenerArticleActivity.class.getSimpleName();

    private ActivityArticle activityArticle;

    public MyOnOffsetChangedListenerArticleActivity(ActivityArticle activityArticle)
    {
        this.activityArticle = activityArticle;
    }

    /**
     * @param verticalOffset is getY() of appBarLayout
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
    {
//        Log.i(LOG, "verTOff: " + verticalOffset);
//        int minToolbarHeight = ViewCompat.getMinimumHeight(activityArticle.getToolbar());
//        Log.i(LOG, "minToolbarHeight: " + minToolbarHeight);
//        Log.i(LOG, "appBarLayout.getHeight():  " + appBarLayout.getHeight());
//        Log.i(LOG, "activityArticle.getCollapsingToolbarLayout().getHeight():  " + activityArticle.getCollapsingToolbarLayout().getHeight());
        if (verticalOffset < 0)
        {
//            BusProvider.getInstance().post(new EventCollapsed());
            if (activityArticle.getVerticalOffsetPrevious() == 0)
            {
//                BusProvider.getInstance().post(new EventCollapsed());
                activityArticle.setFullyExpanded(false);
            }
        }
        else
        {
            if (activityArticle.getVerticalOffsetPrevious() < 0)
            {
//                BusProvider.getInstance().post(new EventExpanded());
                activityArticle.setFullyExpanded(true);
            }
        }
        activityArticle.setVerticalOffsetPrevious(verticalOffset);

//            Log.i(LOG, "verticalOffset: "+verticalOffset);

        //move background image and its bottom border
        activityArticle.getToolbarImage().setY(verticalOffset * 0.7f);
        activityArticle.getCover2Border().setY(verticalOffset * 0.7f + DipToPx.convert(290, activityArticle));

        if (verticalOffset < -appBarLayout.getHeight() * 0.7f)
        {
            if (activityArticle.getToolbarImage().getAlpha() != 0)
            {
                activityArticle.getToolbarImage().animate().alpha(0).setDuration(600);
            }
        }
        else
        {
            //show toolbarImage if we start to expand collapsingToolbarLayout
            int heightOfToolbarAndStatusBar = activityArticle.getToolbar().getHeight() + ScreenProperties.getStatusBarHeight(activityArticle);
            int s = appBarLayout.getHeight() - heightOfToolbarAndStatusBar;
            activityArticle.setCollapsed(verticalOffset > -s);
//            isCollapsed = (verticalOffset > -s);// ? false : true;
            if (activityArticle.getToolbarImage().getAlpha() < 1 && activityArticle.getIsCollapsed())
            {
                activityArticle.getToolbarImage().animate().alpha(1).setDuration(600);
            }
        }
    }
}