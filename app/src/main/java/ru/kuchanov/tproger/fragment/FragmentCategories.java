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

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapter;
import ru.kuchanov.tproger.RecyclerAdapterCatsTags;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.activity.ActivityArticle;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
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
    public static final String KEY_CUR_CATEGORY_TYPE = "KEY_CUR_CATEGORY_TYPE";
    //    public static final String KEY_IS_LOADING = "isLoading";
    protected MySpiceManager spiceManagerOffline;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    private int curCategoryType = 0;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Tag> tags = new ArrayList<>();
    private AppCompatActivity act;
    private Context ctx;
    //    private boolean isLoading = false;
    private SharedPreferences pref;
    private int numOfColsInGridLayoutManager = 2;

    public static FragmentCategories newInstance(int categoryType)
    {
        FragmentCategories frag = new FragmentCategories();
        Bundle b = new Bundle();
        b.putInt(KEY_CUR_CATEGORY_TYPE, categoryType);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
//        Log.i(LOG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);

//        outState.putBoolean(KEY_IS_LOADING, isLoading);
        outState.putParcelableArrayList(Category.LOG, categories);
        outState.putParcelableArrayList(Tag.LOG, tags);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate called");
        super.onCreate(savedInstanceState);

        Bundle args = this.getArguments();
        this.curCategoryType = args.getInt(KEY_CUR_CATEGORY_TYPE);

        MyRoboSpiceDatabaseHelper databaseHelper;
        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

        if (savedInstanceState != null)
        {
//            this.isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            this.categories = savedInstanceState.getParcelableArrayList(Category.LOG);
            this.tags = savedInstanceState.getParcelableArrayList(Tag.LOG);
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

        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);

        //fill recycler with data of make request for it
        switch (curCategoryType)
        {
            case TYPE_CATEGORY:
                if (categories.size() != 0)
                {
//                    recyclerView.setAdapter(new RecyclerAdapterArtsList(ctx, artsList));
                    //TODO
                }
                else
                {
                    this.setLoading(true);
                }
                break;
            case TYPE_TAG:
                if (tags.size() != 0)
                {
//                    recyclerView.setAdapter(new RecyclerAdapterArtsList(ctx, artsList));
                    //TODO
                }
                else
                {
                    this.setLoading(true);
                }
                break;
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
//        Log.i(LOG, "onStart called");
        super.onStart();
        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
        spiceManagerOffline.addListenerIfPending(TagsCategories.class, "unused", new TagsCategoriesRequestListener());
    }

    @Override
    public void onStop()
    {
//        Log.i(LOG, "onStop called from activity: " + getActivity().getClass().getSimpleName());
        super.onStop();
        //remove spiceServiceStart to onPause
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