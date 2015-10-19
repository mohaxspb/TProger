package ru.kuchanov.tproger;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 * Created by Юрий on 19.10.2015 17:26.
 * For ExpListTest.
 */
public abstract class RecyclerViewOnScrollListener extends OnScrollListener
{
    static final String LOG = RecyclerViewOnScrollListener.class.getSimpleName();

    private LinearLayoutManager manager;

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
        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        visibleItemCount = manager.getChildCount();
        totalItemCount = manager.getItemCount();
        firstVisibleItem = manager.findFirstVisibleItemPosition();
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