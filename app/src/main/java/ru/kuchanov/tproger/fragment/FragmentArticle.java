package ru.kuchanov.tproger.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapterArticle;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestArticle;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestArticleOffline;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.SpacesItemDecoration;

/**
 * Created by Юрий on 17.09.2015 17:20.
 * For ExpListTest.
 */
public class FragmentArticle extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_IS_LOADING = "isLoading";
    public static final String KEY_ = "isLoading";
    public static final String KEY_ARTICLE_URL = "KEY_ARTICLE_URL";
    public String LOG;// = FragmentArticle.class.getSimpleName();
    protected MySpiceManager spiceManager;
    protected MySpiceManager spiceManagerOffline;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;

    private Article article;
    private AppCompatActivity act;
    private Context ctx;
    private boolean isLoading = false;

    private SharedPreferences pref;

//    public static FragmentArticle newInstance(String articleUrl)
//    {
//        FragmentArticle frag = new FragmentArticle();
//        Bundle b = new Bundle();
//        b.putString(KEY_ARTICLE_URL, articleUrl);
//        frag.setArguments(b);
//
//        return frag;
//    }

    public static FragmentArticle newInstance(Article article)
    {
        FragmentArticle frag = new FragmentArticle();
        Bundle b = new Bundle();
        b.putParcelable(Article.KEY_ARTICLE, article);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
//        Log.i(LOG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_LOADING, isLoading);
        outState.putParcelable(Article.KEY_ARTICLE, article);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate called");
        super.onCreate(savedInstanceState);


//        MyRoboSpiceDatabaseHelper databaseHelper;
//        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
//        this.category = Category.getCategoryByUrl(categoryUrl, databaseHelper);

        if (savedInstanceState != null)
        {
            this.isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            this.article = savedInstanceState.getParcelable(Article.KEY_ARTICLE);
        }
        else
        {
            Bundle args = this.getArguments();
            this.article = args.getParcelable(Article.KEY_ARTICLE);
        }

        LOG = FragmentArticle.class.getSimpleName() + " - " + article.getUrl();

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_recycler_in_swipe, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);

        int actionBarSize = AttributeGetter.getDimentionPixelSize(ctx, android.R.attr.actionBarSize);
        swipeRefreshLayout.setProgressViewEndTarget(false, actionBarSize);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                //TODO
                performRequest(true);
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler);

        recyclerView.addItemDecoration(new SpacesItemDecoration(0));

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);

        //fill recycler with data of make request for it
        //TODO
        if (article.getText() != null)
        {
            recyclerView.setAdapter(new RecyclerAdapterArticle(ctx, article));
        }
//        if (artsList.size() != 0)
//        {
//            recyclerView.setAdapter(new RecyclerAdapterArtsList(ctx, artsList));
//
//            recyclerView.clearOnScrollListeners();
//            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener()
//            {
//                @Override
//                public void onLoadMore()
//                {
//                    Log.i(LOG, "OnLoadMore called!");
//                    currentPageToLoad++;
//                    performRequest(currentPageToLoad, false, false);
//                }
//            });
//        }

        this.setLoading(isLoading);

//        v.setBackgroundResource(R.drawable.cremlin);

        return v;
    }

    @Override
    public void onAttach(Context context)
    {
//        Log.i(LOG, "onAttach called");
        super.onAttach(context);
        this.ctx = this.getActivity();
        this.act = (AppCompatActivity) this.getActivity();
    }

    @Override
    public void onDetach()
    {
//        Log.i(LOG, "onDetach called");
        super.onDetach();
    }

    @Override
    public void onStart()
    {
//        Log.i(LOG, "onStart called from activity: " + getActivity().getClass().getSimpleName());
        super.onStart();

//        BusProvider.getInstance().register(this);
//
//        if (act instanceof ActivityArticle)
//        {
        spiceManager = SingltonRoboSpice.getInstance().getSpiceManagerArticle();
        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOfflineArticle();
//        }
//        else if (act instanceof ActivityMain)
//        {
//            spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
//            spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
//        }
//        else
//        {
//            throw new NullPointerException("need to add service for this activity: " + act.getClass().getSimpleName());
//        }

        //remove spiceServiceStart to on resume
    }

    @Override
    public void onStop()
    {
//        Log.i(LOG, "onStop called from activity: " + getActivity().getClass().getSimpleName());
        super.onStop();
        //remove spiceServiceStart to onPause

        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
//        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume()
    {
//        Log.i(LOG, "onResume called from activity: " + getActivity().getClass().getSimpleName());
        super.onResume();

        spiceManager.addListenerIfPending(Article.class, "unused", new ArticleRequestListener());
        spiceManagerOffline.addListenerIfPending(Article.class, "unused", new ArticleRequestListener());
        //make request for it
        //TODO
//        if (artsList.size() == 0)
//        {
//            performRequest(false, false);
//        }

        performRequest(false);
    }

    @Override
    public void onPause()
    {
//        Log.i(LOG, "onPause called from activity: " + getActivity().getClass().getSimpleName());
        super.onPause();
    }

//    @Subscribe
//    public void onExpanded(EventExpanded event)
//    {
////        Log.i(LOG, "EventExpanded: " + String.valueOf(event.isExpanded()));
//        swipeRefreshLayout.setEnabled(true);
//    }
//
//    @Subscribe
//    public void onCollapsed(EventCollapsed event)
//    {
////        Log.i(LOG, "EventCollapsed: " + String.valueOf(event.isCollapsed()));
//        swipeRefreshLayout.setEnabled(false);
//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
//        Log.i(LOG, "onSharedPreferenceChanged with key: " + key);
        if (!isAdded())
        {
            return;
        }
        if (key.equals(this.getString(R.string.pref_design_key_text_size_ui)))
        {
            //TODO i'm sure here we'll need change only one item (first)
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void setLoading(boolean isLoading)
    {
//        Log.i(LOG, "isLoading: " + isLoading +
//          " swipeRefreshLayout.isRefreshing(): " + swipeRefreshLayout.isRefreshing());
        this.isLoading = isLoading;

        if (isLoading && swipeRefreshLayout.isRefreshing())
        {
//            Log.i(LOG, "isLoading and  swipeRefreshLayout.isRefreshing() are both TRUE, so RETURN!!!");
            return;
        }

        if (isLoading)
        {
//            Log.i(LOG, "isLoading is TRUE!!!");
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void performRequest(boolean forceRefresh)
    {
        Log.i(LOG, "performRequest forceRefresh: " + forceRefresh);

        this.setLoading(true);
        //if !forceRefresh we must load arts from DB
        //but previously we must check if given art  obj has text...
        //It's not common situation must think about it TODO
        forceRefresh = article.getText() == null;
        if (!forceRefresh)
        {
            RoboSpiceRequestArticleOffline requestFromDB = new RoboSpiceRequestArticleOffline(ctx, article);
            spiceManagerOffline.execute(requestFromDB, LOG, DurationInMillis.ALWAYS_EXPIRED, new ArticleRequestListener());
        }
        else
        {
            RoboSpiceRequestArticle request = new RoboSpiceRequestArticle(ctx, article);
            spiceManager.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new ArticleRequestListener());
        }
    }

    //inner class of your spiced Activity
    private class ArticleRequestListener implements PendingRequestListener<Article>
    {
        @Override
        public void onRequestFailure(SpiceException e)
        {
            Log.i(LOG, "onRequestFailure");
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
                //so we must load from cache (try to get data from roboSpice cache ??? maybe not...
                //cause we try to load from cache first, so previously loaded arts are still here;
            }
            else
            {
                e.printStackTrace();
            }

            setLoading(false);
        }

        @Override
        public void onRequestSuccess(Article article)
        {
            Log.i(LOG, "onRequestSuccess");
            if (!isAdded())
            {
                Log.e(LOG, "frag not added");
                return;
            }
//            if (articles == null || articles.getResult() == null)
//            {
//                //no data in cache?..
//                Log.i(LOG, "no data in cache for page: " + currentPageToLoad);
//                performRequest(currentPageToLoad, true, false);
//                return;
//            }
//
//            ArrayList<Article> list = new ArrayList<>(articles.getResult());
//
            setLoading(false);

            article = article;
            recyclerView.setAdapter(new RecyclerAdapterArticle(ctx, article));
//
//            if (list.size() != Const.NUM_OF_ARTS_ON_PAGE && !articles.isContainsBottomArt())
//            {
//                //error in DB - need to reset category;
//                Log.i(LOG, "error in DB - need to reset category;");
//                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
//                artsList = new ArrayList<>();
////                ((RecyclerAdapterArtsList) recyclerView.getAdapter()).notifyRemoveEach();
//
////                ((RecyclerAdapterArtsList) recyclerView.getAdapter()).addData(artsList);
//                currentPageToLoad = 1;
//                performRequest(currentPageToLoad, true, true);
//
//                return;
//            }
//
//            Log.i(LOG, "RECEIVE " + list.size() + " arts for page: " + currentPageToLoad);
//
//            Collections.sort(list, new Article.PubDateComparator());
//
//            if (currentPageToLoad > 1)
//            {
//                int prevListSize = artsList.size();
//                artsList.addAll(list);
//                recyclerView.getAdapter().notifyItemRangeInserted(prevListSize, artsList.size());
//
//                //update cover
//                BusProvider.getInstance().post(new EventArtsReceived(artsList));
//            }
//            else
//            {
//                int prevSize = artsList.size();
//                artsList.clear();
//                artsList.addAll(list);
//                if (recyclerView.getAdapter() == null)
//                {
//                    recyclerView.setAdapter(new RecyclerAdapterArtsList(ctx, artsList));
//                    recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
//                }
//                else
//                {
//                    recyclerView.getAdapter().notifyItemRangeRemoved(0, prevSize);
//                }
//
//                //update cover
//                BusProvider.getInstance().post(new EventArtsReceived(artsList));
//            }
        }

        @Override
        public void onRequestNotFound()
        {
//            Log.i(LOG, "onRequestNotFound called");
        }
    }
}