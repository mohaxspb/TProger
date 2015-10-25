package ru.kuchanov.tproger.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.tproger.AppSinglton;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapter;
import ru.kuchanov.tproger.RecyclerViewOnScrollListener;
import ru.kuchanov.tproger.custom.view.MySwipeRefreshLayout;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventCollapsed;
import ru.kuchanov.tproger.otto.EventExpanded;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.RoboSpiceRequestCategoriesArts;
import ru.kuchanov.tproger.robospice.RoboSpiceRequestCategoriesArtsFromBottom;
import ru.kuchanov.tproger.robospice.RoboSpiceRequestCategoriesArtsFromBottomOffline;
import ru.kuchanov.tproger.robospice.RoboSpiceRequestCategoriesArtsOffline;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;

/**
 * Created by Юрий on 17.09.2015 17:20.
 * For ExpListTest.
 */
public class FragmentCategory extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String LOG = FragmentCategory.class.getSimpleName();
    public static final String KEY_CATEGORY = "keyCategory";
    public static final String KEY_CURRENT_PAGE_TO_LOAD = "keyCurrentPageToLoad";
    public static final String KEY_IS_LOADING = "isLoading";

    protected MySpiceManager spiceManager = AppSinglton.getInstance().getSpiceManager();
    protected MySpiceManager spiceManagerOffline = AppSinglton.getInstance().getSpiceManagerOffline();
    protected MySwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    //    private String lastRequestCacheKey;
    private String category;
    private Context ctx;
    private int currentPageToLoad = 1;
    private boolean isLoading = false;

    private SharedPreferences pref;

    private ArrayList<Article> artsList = new ArrayList<Article>();

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
        Log.i(LOG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_LOADING, isLoading);
        outState.putInt(KEY_CURRENT_PAGE_TO_LOAD, currentPageToLoad);
        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST, artsList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreate called");
        super.onCreate(savedInstanceState);

        Bundle args = this.getArguments();
        this.category = args.getString(KEY_CATEGORY);

        if (savedInstanceState != null)
        {
            this.isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            this.currentPageToLoad = savedInstanceState.getInt(KEY_CURRENT_PAGE_TO_LOAD);
            this.artsList = savedInstanceState.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
        }

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_category, container, false);

        swipeRefreshLayout = (MySwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new MySwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                currentPageToLoad = 1;
                performRequest(1, true);
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler);

        boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
        if (isGridManager)
        {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
        else
        {
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        }

        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);

        //fill recycler with data of make request for it
        if (artsList.size() != 0)
        {
            ArrayList<String> mDataSet = new ArrayList<>();
            for (Article a : artsList)
            {
                mDataSet.add(a.getTitle());
            }

            recyclerView.setAdapter(new RecyclerAdapter(mDataSet));

            recyclerView.clearOnScrollListeners();
            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener()
            {
                @Override
                public void onLoadMore()
                {
                    Log.i(LOG, "OnLoadMore called!");
                    currentPageToLoad++;
                    performRequest(currentPageToLoad, false);
                }
            });
        }

        swipeRefreshLayout.setRefreshing(isLoading);

        return v;
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(LOG, "onAttach called");
        super.onAttach(context);

        this.ctx = this.getActivity();
    }

    @Override
    public void onDetach()
    {
        Log.i(LOG, "onDetach called");
        super.onDetach();
    }

    @Override
    public void onStart()
    {
        Log.i(LOG, "onStart called");
        super.onStart();

        spiceManager.start(ctx);
        spiceManager.addListenerIfPending(Articles.class, "unused", new ListFollowersRequestListener());
        spiceManagerOffline.start(ctx);
        spiceManagerOffline.addListenerIfPending(Articles.class, "unused", new ListFollowersRequestListener());
        //make request for it
        if (artsList.size() == 0)
        {
            performRequest(1, false);
        }
    }

    @Override
    public void onStop()
    {
        Log.i(LOG, "onStop called");
        super.onStop();

        spiceManager.shouldStop();
        spiceManagerOffline.shouldStop();
    }

    private void performRequest(int page, boolean forceRefresh)
    {
        Log.i(LOG, "performRequest with page: " + page + " and forceRefresh: " + String.valueOf(forceRefresh));

        if (page == 1)
        {
            //if !forceRefresh we must load arts from DB
            if (!forceRefresh)
            {
                RoboSpiceRequestCategoriesArtsOffline requestFromDB = new RoboSpiceRequestCategoriesArtsOffline(ctx, category);
                spiceManagerOffline.execute(requestFromDB, "unused", DurationInMillis.ALWAYS_EXPIRED, new ListFollowersRequestListener());
            }
            else
            {
                RoboSpiceRequestCategoriesArts request = new RoboSpiceRequestCategoriesArts(ctx, category, page);
                spiceManager.execute(request, "unused", DurationInMillis.ALWAYS_EXPIRED, new ListFollowersRequestListener());
            }
        }
        else
        {
            if (!forceRefresh)
            {
                RoboSpiceRequestCategoriesArtsFromBottomOffline request = new RoboSpiceRequestCategoriesArtsFromBottomOffline(ctx, category, page);
                spiceManagerOffline.execute(request, "unused", DurationInMillis.ALWAYS_EXPIRED, new ListFollowersRequestListener());
            }
            else
            {
                RoboSpiceRequestCategoriesArtsFromBottom request = new RoboSpiceRequestCategoriesArtsFromBottom(ctx, category, page);
                spiceManager.execute(request, "unused", DurationInMillis.ALWAYS_EXPIRED, new ListFollowersRequestListener());
            }
        }

        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);
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
//        Log.i(LOG, "EventExpanded: " + String.valueOf(event.isExpanded()));
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setLayoutMovementEnabled(true);
    }

    @Subscribe
    public void onCollapsed(EventCollapsed event)
    {
//        Log.i(LOG, "EventCollapsed: " + String.valueOf(event.isCollapsed()));
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setLayoutMovementEnabled(false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.i(LOG, "onSharedPreferenceChanged with key: " + key);
        if(!isAdded())
        {
            return;
        }
        if (key.equals(this.getString(R.string.pref_design_key_list_style)))
        {
            boolean isGridManager = sharedPreferences.getBoolean(key, false);

            if (isGridManager)
            {
                ((RecyclerAdapter) this.recyclerView.getAdapter()).notifyRemoveEach();
                this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                ((RecyclerAdapter) this.recyclerView.getAdapter()).notifyAddEach();
            }
            else
            {
                ((RecyclerAdapter) this.recyclerView.getAdapter()).notifyRemoveEach();
                this.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                ((RecyclerAdapter) this.recyclerView.getAdapter()).notifyAddEach();
            }
        }
    }

    //inner class of your spiced Activity
    private class ListFollowersRequestListener implements PendingRequestListener<Articles>
    {
        @Override
        public void onRequestFailure(SpiceException e)
        {
            if (!isAdded())
            {
                Log.e(LOG, "frag not added");
                return;
            }
            if (e instanceof NoNetworkException)
            {
                //Toast "you got no connection
                Toast.makeText(ctx, "Не удалось подключиться к интернету", Toast.LENGTH_SHORT).show();
                Log.i(LOG, "NoNetworkException: Не удалось подключиться к интернету");
                //TODO so we must load from cache (try to get data from roboSpice cache
            }
            else
            {
                e.printStackTrace();
            }
            isLoading = false;
            swipeRefreshLayout.setRefreshing(false);
            if (currentPageToLoad > 1)
            {
                currentPageToLoad--;
            }
        }

        @Override
        public void onRequestSuccess(Articles articles)
        {
            if (!isAdded())
            {
                Log.e(LOG, "frag not added");
                return;
            }
            if (articles == null || articles.getResult() == null)
            {
                //no data in cache?..
                Log.i(LOG, "no data in cache for page: " + currentPageToLoad);
                performRequest(currentPageToLoad, true);
                return;
            }
            ArrayList<Article> list = new ArrayList<Article>(articles.getResult());

            Log.i(LOG, "RECEIVE " + list.size() + " arts for page: " + currentPageToLoad);

            Collections.sort(list, new Article.PubDateComparator());

            ArrayList<String> mDataSet = new ArrayList<String>();
            for (Article a : list)
            {
                Article.printInLog(a);
                mDataSet.add(a.getTitle());
            }

            if (currentPageToLoad > 1)
            {
                artsList.addAll(list);
                ((RecyclerAdapter) recyclerView.getAdapter()).addData(mDataSet);
            }
            else
            {
                artsList = new ArrayList<>(list);
                recyclerView.setAdapter(new RecyclerAdapter(mDataSet));
            }

            recyclerView.clearOnScrollListeners();
            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener()
            {
                @Override
                public void onLoadMore()
                {
                    Log.i(LOG, "OnLoadMore called!");
                    currentPageToLoad++;
                    performRequest(currentPageToLoad, false);
                }
            });
            isLoading = false;
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onRequestNotFound()
        {
            Log.i(LOG, "onRequestNotFound called");
        }
    }
}