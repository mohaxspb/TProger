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
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerViewOnScrollListener;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.activity.ActivityArticle;
import ru.kuchanov.tproger.adapter.RecyclerAdapterArtsList;
import ru.kuchanov.tproger.otto.EventArtsReceived;
import ru.kuchanov.tproger.otto.EventRestartShowingArtsImgs;
import ru.kuchanov.tproger.otto.EventShowImage;
import ru.kuchanov.tproger.otto.SingltonOtto;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestCategoriesArts;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestCategoriesArtsFromBottom;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestCategoriesArtsFromBottomOffline;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestCategoriesArtsOffline;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.MyRandomUtil;
import ru.kuchanov.tproger.utils.ScreenProperties;

/**
 * Created by Юрий on 17.09.2015 17:20 19:35.
 * For TProger.
 */
public class FragmentCategory extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_CATEGORY_OR_TAG_URL = "KEY_CATEGORY_OR_TAG_URL";
    public static final String KEY_CURRENT_PAGE_TO_LOAD = "KEY_CURRENT_PAGE_TO_LOAD";
    public static final String KEY_IS_LOADING = "KEY_IS_LOADING";
    public static final String KEY_IS_LOADING_FROM_TOP = "KEY_IS_LOADING_FROM_TOP";
    public static final String KEY_CURRENT_ACTIVATED_POSITION = "KEY_CURRENT_ACTIVATED_POSITION";

    protected MySpiceManager spiceManager;
    protected MySpiceManager spiceManagerOffline;

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    private MyRoboSpiceDatabaseHelper databaseHelper;
    private String LOG = FragmentCategory.class.getSimpleName();

    private String categoryOrTagUrl;
    private Category category;
    private Tag tag;
    private boolean isCategoryOrTag;
    private AppCompatActivity act;
    private Context ctx;
    private int currentPageToLoad = 1;
    private int currentActivatedPosition = -1;
    private boolean isLoading = false;
    private boolean isLoadingFromTop = true;
    private SharedPreferences pref;
    private int numOfColsInGridLayoutManager = 2;
    private ArrayList<Article> artsList = new ArrayList<>();

    //for changing image
    private Timer timer;
    private TimerTask timerTask;

    public static FragmentCategory newInstance(String category, int currentActivatedPosition)
    {
        FragmentCategory frag = new FragmentCategory();
        Bundle b = new Bundle();
        b.putString(KEY_CATEGORY_OR_TAG_URL, category);
        b.putInt(KEY_CURRENT_ACTIVATED_POSITION, currentActivatedPosition);
        frag.setArguments(b);

        return frag;
    }

    public static FragmentCategory newInstance(String category, int currentActivatedPosition, ArrayList<Article> artsList)
    {
        FragmentCategory frag = new FragmentCategory();
        Bundle b = new Bundle();
        b.putString(KEY_CATEGORY_OR_TAG_URL, category);
        b.putInt(KEY_CURRENT_ACTIVATED_POSITION, currentActivatedPosition);
        b.putParcelableArrayList(Article.KEY_ARTICLES_LIST, artsList);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
//        Log.i(LOG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_LOADING, isLoading);
        outState.putBoolean(KEY_IS_LOADING_FROM_TOP, isLoadingFromTop);
        outState.putInt(KEY_CURRENT_PAGE_TO_LOAD, currentPageToLoad);
        outState.putInt(KEY_CURRENT_ACTIVATED_POSITION, currentActivatedPosition);
        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST, artsList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate called");
        super.onCreate(savedInstanceState);

        Bundle args = this.getArguments();
        this.categoryOrTagUrl = args.getString(KEY_CATEGORY_OR_TAG_URL);
        this.currentActivatedPosition = args.getInt(KEY_CURRENT_ACTIVATED_POSITION);
        if (args.containsKey(Article.KEY_ARTICLES_LIST))
        {
            this.artsList = args.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
            Log.i(LOG, "currentPageToLoad: " + currentPageToLoad);
            Log.i(LOG, "artsList.size(): " + artsList.size());
            Log.i(LOG, " Const.NUM_OF_ARTS_ON_PAGE: " + Const.NUM_OF_ARTS_ON_PAGE);
            this.currentPageToLoad = (int) Math.floor(artsList.size() / Const.NUM_OF_ARTS_ON_PAGE);
            Log.i(LOG, "currentPageToLoad: " + currentPageToLoad);
        }

        this.LOG += "#" + this.categoryOrTagUrl;
//        Log.d(LOG, this.categoryOrTagUrl);


        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        Boolean isCategoryOrTagOrDoNotExists = MyRoboSpiceDatabaseHelper.isCategoryOrTagOrDoNotExists(databaseHelper, this.categoryOrTagUrl);
        if (isCategoryOrTagOrDoNotExists == null)
        {
            //seems to be, that we must create it...
            //and we'll create it while parsing html)
            Log.e(LOG, this.categoryOrTagUrl + " DOES NOT EXISTS IN DB! SO WE'LL CREATE IT WHILE PARSING!");
        }
        else
        {
            this.isCategoryOrTag = isCategoryOrTagOrDoNotExists;
//            String title = "not found";
            if (isCategoryOrTag)
            {
                this.category = Category.getCategoryByUrl(categoryOrTagUrl, databaseHelper);
//                title = category.getTitle();
            }
            else
            {
                this.tag = Tag.getTagByUrl(categoryOrTagUrl, databaseHelper);
//                title = tag.getTitle();
            }
//            Log.i(LOG, "CategoryFragment with category: " + title);
        }


        if (savedInstanceState != null)
        {
            this.isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            this.isLoadingFromTop = savedInstanceState.getBoolean(KEY_IS_LOADING_FROM_TOP);
            this.currentPageToLoad = savedInstanceState.getInt(KEY_CURRENT_PAGE_TO_LOAD);
            this.currentActivatedPosition = savedInstanceState.getInt(KEY_CURRENT_ACTIVATED_POSITION);
            this.artsList = savedInstanceState.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
        }

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(this.getString(R.string.pref_design_key_col_num), "2"));
        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_recycler_in_swipe, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                currentPageToLoad = 1;
                performRequest(1, true, false);
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler);

        boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
        boolean isOnArticleActivity = (ctx instanceof ActivityArticle);
        if (isGridManager && !isOnArticleActivity)
        {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(numOfColsInGridLayoutManager, StaggeredGridLayoutManager.VERTICAL));
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
            Log.i(LOG, "artsList.size() != 0");
            RecyclerAdapterArtsList adapterArtsList = new RecyclerAdapterArtsList(ctx, artsList, categoryOrTagUrl);
            recyclerView.setAdapter(adapterArtsList);

            recyclerView.clearOnScrollListeners();
            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener()
            {
                @Override
                public void onLoadMore()
                {
                    Log.i(LOG, "OnLoadMore called!");
                    currentPageToLoad++;
                    performRequest(currentPageToLoad, false, false);
                }
            });

//            BusProvider.getInstance().post(new EventArtsReceived(artsList));
        }
        else
        {
            this.setLoading(isLoading);
        }

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
    public void onStart()
    {
//        Log.i(LOG, "onStart called from activity: " + getActivity().getClass().getSimpleName());
        super.onStart();

        SingltonOtto.getInstance().register(this);

        spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();

        //remove spiceServiceStart to on resume
        spiceManager.addListenerIfPending(Articles.class, LOG, new CategoriesArtsRequestListener());
        spiceManagerOffline.addListenerIfPending(Articles.class, LOG, new CategoriesArtsRequestListener());
    }

    @Override
    public void onStop()
    {
//        Log.i(LOG, "onStop called from activity: " + getActivity().getClass().getSimpleName());
        super.onStop();
        //remove spiceServiceStart to onPause

        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
        SingltonOtto.getInstance().unregister(this);
    }

    @Override
    public void onResume()
    {
//        Log.i(LOG, "onResume called from activity: " + getActivity().getClass().getSimpleName());
        super.onResume();

//        spiceManager.addListenerIfPending(Articles.class, "unused", new CategoriesArtsRequestListener());
//        spiceManagerOffline.addListenerIfPending(Articles.class, "unused", new CategoriesArtsRequestListener());
        //make request for it
        if (artsList.size() == 0)
        {
            performRequest(1, false, false);
        }
        else
        {
            setTimer();
//            restartTimerForImagesShowing(null);
        }
    }

    @Override
    public void onPause()
    {
//        Log.i(LOG, "onPause called from activity: " + getActivity().getClass().getSimpleName());
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
//        Log.i(LOG, "onSharedPreferenceChanged with key: " + key);
        if (!isAdded())
        {
            return;
        }
        if (key.equals(this.getString(R.string.pref_design_key_list_style)))
        {
            boolean isGridManager = sharedPreferences.getBoolean(key, false);
            boolean isOnArticleActivity = (ctx instanceof ActivityArticle);
            if (isGridManager && !isOnArticleActivity)
            {
                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
                this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(numOfColsInGridLayoutManager, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
            }
            else
            {
                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
                this.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
            }
        }
        if (key.equals(this.getString(R.string.pref_design_key_col_num)))
        {
            boolean isGridManager = sharedPreferences.getBoolean(this.getString(R.string.pref_design_key_list_style), false);

            this.numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(key, "2"));

            boolean isOnArticleActivity = (ctx instanceof ActivityArticle);
            if (isGridManager && !isOnArticleActivity)
            {
                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
                this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(numOfColsInGridLayoutManager, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
            }
        }
        if (key.equals(this.getString(R.string.pref_design_key_art_card_style))
                || key.equals(this.getString(R.string.pref_design_key_art_card_preview_show))
                || key.equals(this.getString(R.string.pref_design_key_text_size_ui))
                || key.equals(this.getString(R.string.pref_design_key_art_card_preview_short))
                )
        {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void setLoading(final boolean isLoading)
    {
//        Log.i(LOG, "isLoading: " + isLoading +
//          " isLoadingFromTop: " + isLoadingFromTop +
//          " swipeRefreshLayout.isRefreshing(): " + swipeRefreshLayout.isRefreshing());
        this.isLoading = isLoading;

        if (isLoading && swipeRefreshLayout.isRefreshing())
        {
//            Log.i(LOG, "isLoading and  swipeRefreshLayout.isRefreshing() are both TRUE, so RETURN!!!");
            return;
        }

        int actionBarSize = AttributeGetter.getDimentionPixelSize(ctx, android.R.attr.actionBarSize);
        if (isLoading)
        {
//            Log.i(LOG, "isLoading is TRUE!!!");
//            swipeRefreshLayout.setEnabled(true);
//            swipeRefreshLayout.setLayoutMovementEnabled(true);
            if (this.isLoadingFromTop)
            {
                swipeRefreshLayout.setProgressViewEndTarget(false, actionBarSize);
            }
            else
            {
                int screenHeight = ScreenProperties.getHeight(act);
                swipeRefreshLayout.setProgressViewEndTarget(false, screenHeight - actionBarSize * 2);
            }
//            swipeRefreshLayout.setRefreshing(true);
        }
        else
        {
            swipeRefreshLayout.setProgressViewEndTarget(false, actionBarSize);
//            swipeRefreshLayout.setRefreshing(false);
        }

        //workaround from
        //http://stackoverflow.com/a/26910973/3212712
        swipeRefreshLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });
    }

    private void resetOnScroll()
    {
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener()
        {
            @Override
            public void onLoadMore()
            {
                Log.i(LOG, "OnLoadMore called!");
                currentPageToLoad++;
                performRequest(currentPageToLoad, false, false);
            }
        });
    }

    private void performRequest(int page, boolean forceRefresh, boolean resetCategoryInDB)
    {
        Log.i(LOG, "performRequest with page: " + page + " and forceRefresh: " + forceRefresh);

        if (page == 1)
        {
            isLoadingFromTop = true;
            this.setLoading(true);
            //if !forceRefresh we must load arts from DB
            if (!forceRefresh)
            {
                RoboSpiceRequestCategoriesArtsOffline requestFromDB = new RoboSpiceRequestCategoriesArtsOffline(ctx, categoryOrTagUrl);
                spiceManagerOffline.execute(requestFromDB, LOG, DurationInMillis.ALWAYS_EXPIRED, new CategoriesArtsRequestListener());
            }
            else
            {
                RoboSpiceRequestCategoriesArts request = new RoboSpiceRequestCategoriesArts(ctx, categoryOrTagUrl);
                if (resetCategoryInDB)
                {
                    request.setResetCategoryInDB();
                }
                spiceManager.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new CategoriesArtsRequestListener());
            }
        }
        else
        {
            isLoadingFromTop = false;
            this.setLoading(true);
            if (!forceRefresh)
            {
                RoboSpiceRequestCategoriesArtsFromBottomOffline request = new RoboSpiceRequestCategoriesArtsFromBottomOffline(ctx, categoryOrTagUrl, page);
                spiceManagerOffline.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new CategoriesArtsRequestListener());
            }
            else
            {
                RoboSpiceRequestCategoriesArtsFromBottom request = new RoboSpiceRequestCategoriesArtsFromBottom(ctx, categoryOrTagUrl, page);
                spiceManager.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new CategoriesArtsRequestListener());
            }
        }
    }

    @Subscribe
    public void restartTimerForImagesShowing(EventRestartShowingArtsImgs event)
    {
        Log.d(LOG, "restartTimerForImagesShowing");
//        setTimer();
//        if (getUserVisibleHint())
//        {
            final String imageUrl;
            ArrayList<Article> artsWithImage = new ArrayList<>();
            for (Article a : artsList)
            {
                if (a.getImageUrl() != null)
                {
                    artsWithImage.add(a);
                }
            }
            imageUrl = (artsWithImage.size() != 0) ? artsWithImage.get(MyRandomUtil.nextInt(0, artsWithImage.size())).getImageUrl() : null;
            SingltonOtto.getInstance().post(new EventShowImage(imageUrl));
//        }
    }

    private void stopTimer()
    {
        if (timer != null && timerTask != null)
        {
            timerTask.cancel();
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }

    private void setTimer()
    {
        stopTimer();
        //set and start timer
        if (timer == null && timerTask == null)
        {
            timer = new Timer();
            timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    //get new article with image
                    if (getUserVisibleHint())
                    {
                        Log.d(LOG, "timerTask run called");
                        final String imageUrl;
                        ArrayList<Article> artsWithImage = new ArrayList<>();
                        for (Article a : artsList)
                        {
                            if (a.getImageUrl() != null)
                            {
                                artsWithImage.add(a);
                            }
                        }
                        imageUrl = (artsWithImage.size() != 0) ? artsWithImage.get(MyRandomUtil.nextInt(0, artsWithImage.size())).getImageUrl() : null;
                        if (getActivity() != null)
                        {
                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    SingltonOtto.getInstance().post(new EventShowImage(imageUrl));
                                }
                            });
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 5000);
        }
    }

    //inner class of your spiced Activity
    private class CategoriesArtsRequestListener implements PendingRequestListener<Articles>
    {
        @Override
        public void onRequestFailure(SpiceException e)
        {
            Log.i(LOG, "onRequestFailure with error = " + e.getClass().getSimpleName());

            //here we must reduce pageNumber if it was more than 1
            if (currentPageToLoad != 1)
            {
                currentPageToLoad--;
            }

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
                if (e.getMessage() != null)
                {
                    if (e.getMessage().equals(Const.ERROR_404_WHILE_PARSING_PAGE))
                    {
                        Log.i(LOG, "seems to be we reached the last art in cat; So toast about it");
                        Toast.makeText(ctx, "Все статьи раздела загружены!", Toast.LENGTH_SHORT).show();
                        recyclerView.clearOnScrollListeners();
                    }
                }
                e.printStackTrace();
            }

            //reset onScrollListener (isuue #1)
            resetOnScroll();

            setLoading(false);
//            if (currentPageToLoad > 1)
//            {
//                currentPageToLoad--;
//            }
        }

        @Override
        public void onRequestSuccess(Articles articles)
        {
            Log.i(LOG, "onRequestSuccess");
            if (!isAdded())
            {
                Log.e(LOG, "frag not added");
                return;
            }
            if (articles == null || articles.getResult() == null)
            {
                //no data in cache?..
                Log.i(LOG, "no data in cache for page: " + currentPageToLoad);
                performRequest(currentPageToLoad, true, false);
                return;
            }
            //update category or tag obj, as we update it if receive arts from top
            if (currentPageToLoad == 1)
            {
                if (isCategoryOrTag)
                {
                    category = Category.getCategoryByUrl(categoryOrTagUrl, databaseHelper);
                }
                else
                {
                    tag = Tag.getTagByUrl(categoryOrTagUrl, databaseHelper);
                }
            }

            ArrayList<Article> list = new ArrayList<>(articles.getResult());

            resetOnScroll();
            setLoading(false);

            if (list.size() != Const.NUM_OF_ARTS_ON_PAGE && !articles.isContainsBottomArt())
            {
                //error in DB - need to reset category;
                Log.e(LOG, "error in DB - need to reset category;");
                Log.d(LOG, "list.size(): " + list.size() + " AND articles.isContainsBottomArt(): " + articles.isContainsBottomArt());
                int prevSize = artsList.size();

                if (recyclerView.getAdapter() != null)
                {
                    recyclerView.getAdapter().notifyItemRangeRemoved(0, prevSize);
                }
//                artsList = new ArrayList<>();
                artsList.clear();
                currentPageToLoad = 1;
                performRequest(currentPageToLoad, true, true);

                return;
            }
            else if (articles.isContainsBottomArt())
            {
                Log.i(LOG, "end of cats arts list reached");
                Toast.makeText(ctx, "Все статьи раздела загружены!", Toast.LENGTH_SHORT).show();
                //here we must reduce pageNumber if it was more than 1
//                if (currentPageToLoad != 1)
//                {
//                    currentPageToLoad--;
//                }
                //actually we must NOT do it as if we load second page this
                //will change page to 1 and we'll so reset adapter;
                recyclerView.clearOnScrollListeners();
            }

            Log.i(LOG, "RECEIVE " + list.size() + " arts for page: " + currentPageToLoad);

            Collections.sort(list, new Article.PubDateComparator());

            if (currentPageToLoad > 1)
            {
                int prevListSize = artsList.size();
                artsList.addAll(list);
                recyclerView.getAdapter().notifyItemRangeInserted(prevListSize, artsList.size());

                //update cover
                SingltonOtto.getInstance().post(new EventArtsReceived(artsList));
            }
            else
            {
                int prevSize = artsList.size();
                artsList.clear();
                artsList.addAll(list);
                if (recyclerView.getAdapter() == null)
                {
                    RecyclerAdapterArtsList adapterArtsList = new RecyclerAdapterArtsList(ctx, artsList, categoryOrTagUrl);
                    recyclerView.setAdapter(adapterArtsList);
                    recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
                }
                else
                {
                    recyclerView.getAdapter().notifyItemRangeRemoved(0, prevSize);
                }

                //update cover
                SingltonOtto.getInstance().post(new EventArtsReceived(artsList));
                setTimer();

                int newArtsQuont = articles.getNumOfNewArts();
                switch (newArtsQuont)
                {
                    case -2:
                        //not set - do nothing
//                        break;
                    case -1:
                        //initial loading  - do nothing
                        //here we can match current time-Category.refreshed with default refresh period and start request from web
                        String autoRefreshIsOnKey = ctx.getString(R.string.pref_system_key_autorenew);
                        boolean autoRefreshIsOn = pref.getBoolean(autoRefreshIsOnKey, false);
                        if (autoRefreshIsOn)
                        {
                            Log.i(LOG, "autoRefreshIsOn so start loading from web");
                            currentPageToLoad = 1;
                            performRequest(currentPageToLoad, true, false);
                        }
                        else
                        {
                            if (isCategoryOrTag)
                            {
                                if (Category.refreshDateExpired(category, ctx))
                                {
                                    Log.i(LOG, "autoRefreshIs OFF but refreshDate expired so start loading from web");
                                    currentPageToLoad = 1;
                                    performRequest(currentPageToLoad, true, false);
                                }
                            }
                            else
                            {
                                if (Tag.refreshDateExpired(tag, ctx))
                                {
                                    Log.i(LOG, "autoRefreshIs OFF but refreshDate expired so start loading from web");
                                    currentPageToLoad = 1;
                                    performRequest(currentPageToLoad, true, false);
                                }
                            }
                        }
                        break;
                    case 0:
                        Toast.makeText(ctx, "Новых статей не обнаружено!", Toast.LENGTH_SHORT).show();
                        break;
                    case 10:
                        Toast.makeText(ctx, "Обнаружено более 10 новых статей!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(ctx, "Обнаружено " + newArtsQuont + " новых статей!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        @Override
        public void onRequestNotFound()
        {
//            Log.i(LOG, "onRequestNotFound called");
        }
    }
}