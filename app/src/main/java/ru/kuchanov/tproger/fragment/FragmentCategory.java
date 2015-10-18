package ru.kuchanov.tproger.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapter;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventCollapsed;
import ru.kuchanov.tproger.otto.EventExpanded;
import ru.kuchanov.tproger.robospice.HtmlSpiceService;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.RoboSpiceRequestCategoriesArts;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;

/**
 * Created by Юрий on 17.09.2015 17:20.
 * For ExpListTest.
 */
public class FragmentCategory extends Fragment
{
    public static final String LOG = FragmentCategory.class.getSimpleName();
    public static final String KEY_CATEGORY = "keyCategory";
    public static final String KEY_LAST_REQUEST_CACHE_KEY = "keyLastRequestCacheKey";

    protected SpiceManager spiceManager = new MySpiceManager(HtmlSpiceService.class);
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    String lastRequestCacheKey;
    String category;
    private Context ctx;

    public static FragmentCategory newInstance(String category)
    {
        FragmentCategory frag = new FragmentCategory();
        Bundle b = new Bundle();
        b.putString(KEY_CATEGORY, category);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_LAST_REQUEST_CACHE_KEY, this.lastRequestCacheKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle args = this.getArguments();
        this.category = args.getString(KEY_CATEGORY);

        if (savedInstanceState != null)
        {
            this.lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_CACHE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_category, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                performRequest(1);
            }
        });
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));


//        String[] mDataSet = new String[100];
//        for (int i = 0; i < 100; i++)
//        {
//            mDataSet[i] = "Tab1, item" + i;
//        }

//        recyclerView.setAdapter(new RecyclerAdapter(mDataSet));

        return v;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        this.ctx = this.getActivity();

        spiceManager.start(this.getActivity());
    }

    @Override
    public void onStop()
    {
        spiceManager.shouldStop();
        super.onStop();
    }

    private void performRequest(int page)
    {
        RoboSpiceRequestCategoriesArts request = new RoboSpiceRequestCategoriesArts(ctx, category, page);
        lastRequestCacheKey = request.createCacheKey();

        spiceManager.execute(request, lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new ListFollowersRequestListener());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onExpanded(EventExpanded event)
    {
        Log.i(LOG, "EventExpanded: " + String.valueOf(event.isExpanded()));
        swipeRefreshLayout.setEnabled(true);
    }

    @Subscribe
    public void onCollapsed(EventCollapsed event)
    {
        Log.i(LOG, "EventCollapsed: " + String.valueOf(event.isCollapsed()));
        swipeRefreshLayout.setEnabled(false);
    }

    //inner class of your spiced Activity
    private class ListFollowersRequestListener implements RequestListener<Articles>
    {

        @Override
        public void onRequestFailure(SpiceException e)
        {
            //update your UI
            Toast.makeText(ctx, "Fail", Toast.LENGTH_SHORT).show();
            Log.i(LOG, "Fail");
        }

        @Override
        public void onRequestSuccess(Articles listFollowers)
        {
            //update your UI
//            Toast.makeText(ctx, listFollowers.getResult().toArray()[0].toString(), Toast.LENGTH_SHORT).show();
//            Log.i(LOG, "listFollowers.getResult().size(): "+listFollowers.getResult().size());
//            Log.i(LOG, "listFollowers.getResult().toArray()[0].toString(): "+listFollowers.getResult().toArray()[0].toString());
            Log.i(LOG, "listFollowers.getResult().size(): " + listFollowers.getResult().size());
            Log.i(LOG, "listFollowers.getResult().toArray()[0].toString(): " + listFollowers.getResult().toArray()[0].toString());


            ArrayList<Article> list = new ArrayList<Article>(listFollowers.getResult());
//            ArrayList<String> listStr = new ArrayList<>();
            String[] mDataSet = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
            {
                Article a = list.get(i);
                Log.i(LOG, "!!!!!!!!!!!!!!!!!!!!!!!!!");
                Log.i(LOG, String.valueOf(a.getId()));
                Log.i(LOG, String.valueOf(a.getTitle()));
//                Log.i(LOG,  String.valueOf(a.getUrl()));
//                Log.i(LOG,  String.valueOf(a.getPubDate()));
//                Log.i(LOG,  String.valueOf(a.getImageUrl()));
//                Log.i(LOG,  String.valueOf(a.getImageHeight()));
//                Log.i(LOG,  String.valueOf(a.getImageWidth()));
//                Log.i(LOG,  String.valueOf(a.getTagMainTitle()));
//                Log.i(LOG,  String.valueOf(a.getTagMainUrl()));
//                Log.i(LOG,  String.valueOf(a.getPreview()));
                Log.i(LOG, "!!!!!!!!!!!!!!!!!!!!!!!!!");

                mDataSet[i] = a.getTitle();
            }


            recyclerView.setAdapter(new RecyclerAdapter(mDataSet));
            swipeRefreshLayout.setRefreshing(false);

//            swipeRefreshLayout.isNestedScrollingEnabled()
        }
    }

}