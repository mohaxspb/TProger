package ru.kuchanov.tproger.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapterCatsTags;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.activity.ActivityArticle;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventCatsTagsShow;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.robospice.db.TagsCategories;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestTagsCategoriesOffline;
import ru.kuchanov.tproger.utils.AttributeGetter;

/**
 * Created by Юрий on 17.09.2015 17:20 16:55.
 * For TProger.
 */
public class FragmentCategories extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String LOG = FragmentCategories.class.getSimpleName();

    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_TAG = 1;
    public static final String KEY_CATS_OR_TAGS_DATA_TYPE = "KEY_CATS_OR_TAGS_DATA_TYPE";
    //    public static final String KEY_CUR_CATEGORY_TYPE = "KEY_CUR_CATEGORY_TYPE";
    //    public static final String KEY_IS_LOADING = "isLoading";
    protected MySpiceManager spiceManagerOffline;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    private int curCategoryType;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Tag> tags = new ArrayList<>();
    //    private AppCompatActivity act;
    private Context ctx;
    //    private boolean isLoading = false;
    private SharedPreferences pref;
    private int numOfColsInGridLayoutManager = 2;

    public static FragmentCategories newInstance(int categoryType)
    {
        FragmentCategories frag = new FragmentCategories();
        Bundle b = new Bundle();
        b.putInt(KEY_CATS_OR_TAGS_DATA_TYPE, categoryType);
        frag.setArguments(b);

        return frag;
    }

    public static FragmentCategories newInstance(int categoryType, ArrayList<Category> cats, ArrayList<Tag> tags)
    {
        FragmentCategories frag = new FragmentCategories();
        Bundle b = new Bundle();
        b.putInt(KEY_CATS_OR_TAGS_DATA_TYPE, categoryType);
        b.putParcelableArrayList(Category.LOG, cats);
        b.putParcelableArrayList(Tag.LOG, tags);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
//        Log.i(LOG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CATS_OR_TAGS_DATA_TYPE, curCategoryType);
        outState.putParcelableArrayList(Category.LOG, categories);
        outState.putParcelableArrayList(Tag.LOG, tags);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate called");
        super.onCreate(savedInstanceState);

        this.restoreData(savedInstanceState, getArguments());

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
//        this.curCategoryType = pref.getBoolean(getString(R.string.pref_design_key_category_in_cats_or_tags), true) ? TYPE_CATEGORY : TYPE_TAG;
        this.numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(this.getString(R.string.pref_design_key_col_num), "2"));
        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    private void restoreData(Bundle savedInstanceState, Bundle args)
    {
        if (savedInstanceState == null)
        {
            this.curCategoryType = args.getInt(KEY_CATS_OR_TAGS_DATA_TYPE);

            if (args.containsKey(Category.LOG))
            {
                this.categories.clear();
                ArrayList<Category> catsFromArgs = args.getParcelableArrayList(Category.LOG);
                if (catsFromArgs != null)
                {
                    this.categories.addAll(catsFromArgs);
                }
            }
            if (args.containsKey(Tag.LOG))
            {
                this.tags.clear();
                ArrayList<Tag> tagsFromArgs = args.getParcelableArrayList(Tag.LOG);
                if (tagsFromArgs != null)
                {
                    this.tags.addAll(tagsFromArgs);
                }
            }
        }
        else
        {
            this.curCategoryType = savedInstanceState.getInt(KEY_CATS_OR_TAGS_DATA_TYPE);
            if (savedInstanceState.containsKey(Category.LOG))
            {
                this.categories.clear();
                ArrayList<Category> catsFromArgs = savedInstanceState.getParcelableArrayList(Category.LOG);
                if (catsFromArgs != null)
                {
                    this.categories.addAll(catsFromArgs);
                }
            }
            if (savedInstanceState.containsKey(Tag.LOG))
            {
                this.tags.clear();
                ArrayList<Tag> tagsFromArgs = savedInstanceState.getParcelableArrayList(Tag.LOG);
                if (tagsFromArgs != null)
                {
                    this.tags.addAll(tagsFromArgs);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_recycler_in_swipe, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setEnabled(false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler);

        boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
        if (isGridManager)
        {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(numOfColsInGridLayoutManager, StaggeredGridLayoutManager.VERTICAL));
        }
        else
        {
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        }

        this.setItemAnimatorForRecyclerView();

        //fill recycler with data of make request for it
        switch (curCategoryType)
        {
            case TYPE_CATEGORY:
                if (categories.size() != 0)
                {
                    RecyclerAdapterCatsTags adapterCatsTags = new RecyclerAdapterCatsTags(tags, categories);
                    adapterCatsTags.setDataType(RecyclerAdapterCatsTags.TYPE_CATEGORY);
                    recyclerView.setAdapter(adapterCatsTags);
                }
                else
                {
                    this.setLoading(true);
                }
                break;
            case TYPE_TAG:
                if (tags.size() != 0)
                {
                    RecyclerAdapterCatsTags adapterCatsTags = new RecyclerAdapterCatsTags(tags, categories);
                    adapterCatsTags.setDataType(RecyclerAdapterCatsTags.TYPE_TAG);
                    recyclerView.setAdapter(adapterCatsTags);
                }
                else
                {
                    this.setLoading(true);
                }
                break;
        }

        return v;
    }

    private void setItemAnimatorForRecyclerView()
    {
        RecyclerView.ItemAnimator animator;
        Interpolator interpolator = new OvershootInterpolator(1f);
        animator = (curCategoryType == TYPE_CATEGORY) ? new SlideInLeftAnimator(interpolator) : new SlideInRightAnimator(interpolator);
        recyclerView.setItemAnimator(animator);
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);
    }

    @Override
    public void onAttach(Context context)
    {
//        Log.i(LOG, "onAttach called");
        super.onAttach(context);
        this.ctx = this.getActivity();
//        this.act = (AppCompatActivity) this.getActivity();
    }

    @Override
    public void onStart()
    {
//        Log.i(LOG, "onStart called");
        super.onStart();
        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
        spiceManagerOffline.addListenerIfPending(TagsCategories.class, "unused", new TagsCategoriesRequestListener());

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop()
    {
//        Log.i(LOG, "onStop called from activity: " + getActivity().getClass().getSimpleName());
        super.onStop();
        //remove spiceServiceStart to onPause

        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume()
    {
//        Log.i(LOG, "onResume called from activity: " + getActivity().getClass().getSimpleName());
        super.onResume();

        spiceManagerOffline.addListenerIfPending(TagsCategories.class, "unused", new TagsCategoriesRequestListener());
        //make request for it
        if (tags.size() == 0 && categories.size() == 0)
        {
            performRequest();
        }
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
            //TODO
            boolean isGridManager = sharedPreferences.getBoolean(key, false);
            boolean isOnArticleActivity = (ctx instanceof ActivityArticle);
            if (isGridManager && !isOnArticleActivity)
            {
//                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
//                this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(numOfColsInGridLayoutManager, StaggeredGridLayoutManager.VERTICAL));
//                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
            }
            else
            {
//                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
//                this.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
//                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
            }
        }
        if (key.equals(this.getString(R.string.pref_design_key_col_num)))
        {
            //TODO
            boolean isGridManager = sharedPreferences.getBoolean(this.getString(R.string.pref_design_key_list_style), false);

            this.numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(key, "2"));

            boolean isOnArticleActivity = (ctx instanceof ActivityArticle);
            if (isGridManager && !isOnArticleActivity)
            {
//                recyclerView.getAdapter().notifyItemRangeRemoved(0, artsList.size());
//                this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(numOfColsInGridLayoutManager, StaggeredGridLayoutManager.VERTICAL));
//                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
            }
        }
        if (key.equals(this.getString(R.string.pref_design_key_text_size_ui)))
        {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void setLoading(final boolean isLoading)
    {
//        Log.i(LOG, "isLoading: " + isLoading +
//          " swipeRefreshLayout.isRefreshing(): " + swipeRefreshLayout.isRefreshing());
//        this.isLoading = isLoading;

        if (isLoading && swipeRefreshLayout.isRefreshing())
        {
//            Log.i(LOG, "isLoading and  swipeRefreshLayout.isRefreshing() are both TRUE, so RETURN!!!");
            return;
        }

        int actionBarSize = AttributeGetter.getDimentionPixelSize(ctx, android.R.attr.actionBarSize);
        swipeRefreshLayout.setProgressViewEndTarget(false, actionBarSize);

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

    private void performRequest()
    {
        Log.i(LOG, "performRequest");

        this.setLoading(true);
        RoboSpiceRequestTagsCategoriesOffline requestFromDB = new RoboSpiceRequestTagsCategoriesOffline(ctx);
        spiceManagerOffline.execute(requestFromDB, "unused", DurationInMillis.ALWAYS_EXPIRED, new TagsCategoriesRequestListener());

    }

    @Subscribe
    public void onTypeChange(EventCatsTagsShow eventCatsTagsShow)
    {
        int type = eventCatsTagsShow.getDataType();
        String newType = (type == TYPE_CATEGORY) ? "TYPE_CATEGORY" : "TYPE_TAG";
        Log.d(LOG, "onTypeChange with type: " + newType);
//        curCategoryType = (curCategoryType == RecyclerAdapterCatsTags.TYPE_CATEGORY) ?
//                RecyclerAdapterCatsTags.TYPE_TAG : RecyclerAdapterCatsTags.TYPE_CATEGORY;
        curCategoryType = type;

        setItemAnimatorForRecyclerView();

        recyclerView.getAdapter().notifyItemRangeRemoved(0, recyclerView.getAdapter().getItemCount());
        ((RecyclerAdapterCatsTags) recyclerView.getAdapter()).setDataType(curCategoryType);
        recyclerView.getAdapter().notifyItemRangeInserted(0, recyclerView.getAdapter().getItemCount());
//        recyclerView.getAdapter().notifyDataSetChanged();

//        ((FabUpdater) act).updateFAB(1, curCategoryType);
    }

    private class TagsCategoriesRequestListener implements PendingRequestListener<TagsCategories>
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
        public void onRequestSuccess(TagsCategories tagsCategories)
        {
//            Log.i(LOG, "onRequestSuccess");
            if (!isAdded())
            {
                Log.e(LOG, "frag not added");
                return;
            }

//            ArrayList<Article> list = new ArrayList<>(t.getResult());

            setLoading(false);

            //TODO test
            tags.clear();
            tags.addAll(tagsCategories.getTags());
            categories.clear();
            categories.addAll(tagsCategories.getCategories());

//            ArrayList<String> dummyData = new ArrayList<>();
//            for (Category c : categories)
//            {
//                dummyData.add(c.getTitle());
//            }
//
//            recyclerView.setAdapter(new RecyclerAdapter(dummyData));
            RecyclerAdapterCatsTags adapterCatsTags = new RecyclerAdapterCatsTags(tags, categories);
            adapterCatsTags.setDataType(RecyclerAdapterCatsTags.TYPE_TAG);
            recyclerView.setAdapter(adapterCatsTags);


//            Log.i(LOG, "RECEIVE " + list.size() + " categories or tags");

            //TODO
//            int prevSize = artsList.size();
//            artsList.clear();
//            artsList.addAll(list);
//            if (recyclerView.getAdapter() == null)
//            {
//                recyclerView.setAdapter(new RecyclerAdapterArtsList(ctx, artsList));
//                recyclerView.getAdapter().notifyItemRangeInserted(0, artsList.size());
//            }
//            else
//            {
//                recyclerView.getAdapter().notifyItemRangeRemoved(0, prevSize);
//            }

        }

        @Override
        public void onRequestNotFound()
        {
//            Log.i(LOG, "onRequestNotFound called");
        }
    }
}