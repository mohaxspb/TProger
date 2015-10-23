package ru.kuchanov.tproger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by Юрий on 19.10.2015 17:26.
 * For ExpListTest.
 */
public abstract class RecyclerViewOnScrollListener extends OnScrollListener
{
    static final String LOG = RecyclerViewOnScrollListener.class.getSimpleName();

    private LinearLayoutManager managerLinear;
    private StaggeredGridLayoutManager managerGrid;

    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 3;
    private int firstVisibleItem, visibleItemCount, totalItemCount;

    public RecyclerViewOnScrollListener()
    {

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int x, int y)
    {
        Context ctx=recyclerView.getContext();
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean isLinearManager=pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);

        if(isLinearManager)
        {
            managerLinear = (LinearLayoutManager) recyclerView.getLayoutManager();
            visibleItemCount = managerLinear.getChildCount();
            totalItemCount = managerLinear.getItemCount();
            firstVisibleItem = managerLinear.findFirstVisibleItemPosition();
        }
        else
        {
            managerGrid = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            visibleItemCount = managerGrid.getChildCount();
            totalItemCount = managerGrid.getItemCount();
            firstVisibleItem = managerGrid.findFirstVisibleItemPositions(new int[2])[0];
        }

//        int lastVisibleItem = manager.findLastVisibleItemPosition();

//        Log.i(LOG, "totalItemCount: " + totalItemCount);
//        Log.i(LOG, "visibleItemCount: " + visibleItemCount);
//        Log.i(LOG, "firstVisibleItem: " + firstVisibleItem);
//        Log.i(LOG, "visibleThreshold: " + visibleThreshold);
//        Log.i(LOG, "lastVisibleItem: " + lastVisibleItem);

        if (loading)
        {
            if (totalItemCount > previousTotal)
            {
                loading = false;
                previousTotal = totalItemCount;
            }
        }


        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold))
        {
            // End has been reached
            //check if totaItemCount a multiple of 10
            if ((totalItemCount) % Const.NUM_OF_ARTS_ON_PAGE == 0)
            {
                // TODO if so we can load more from bottom
                //CHECK here situation when total quont of arts on are multiple of 30
                //to prevent a lot of requests
                onLoadMore();
                loading = true;
            }
            else
            {
                //if so, we have reached onSiteVeryBottomOfArtsList
                //so we do not need to start download
            }
        }
    }

    public abstract void onLoadMore();
}