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

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapterArticle;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestArticle;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.SpacesItemDecoration;
import ru.kuchanov.tproger.utils.html.HtmlParsing;
import ru.kuchanov.tproger.utils.html.HtmlToView;

/**
 * Created by Юрий on 17.09.2015 17:20.
 * For ExpListTest.
 */
public class FragmentArticle extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_IS_LOADING = "isLoading";
    public static final String KEY_ = "isLoading";
    //    public static final String KEY_ARTICLE_URL = "KEY_ARTICLE_URL";
    public String LOG;
    protected MySpiceManager spiceManager;
    protected MySpiceManager spiceManagerOffline;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;

    private Article article;
    private AppCompatActivity act;
    private Context ctx;
    private boolean isLoading = false;

    private SharedPreferences pref;

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

            //TODO test some article
            this.article.setUrl(Const.Articles.CODE_AND_SPOILER_GIF);
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
                performRequest();
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

//        BusProvider.getInstance().register(this);
        spiceManager = SingltonRoboSpice.getInstance().getSpiceManagerArticle();
        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOfflineArticle();
        //remove spiceServiceStart to on resume
    }

    @Override
    public void onStop()
    {
//        Log.i(LOG, "onStop called");
        super.onStop();
        //remove spiceServiceStart to onPause

        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
//        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume()
    {
//        Log.i(LOG, "onResume called");
        super.onResume();

        spiceManager.addListenerIfPending(Article.class, "unused", new ArticleRequestListener());
        spiceManagerOffline.addListenerIfPending(Article.class, "unused", new ArticleRequestListener());
        //make request for it
        //TODO
        if (article.getText() == null)
        {
            performRequest();
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
        if (key.equals(this.getString(R.string.pref_design_key_text_size_ui)))
        {
            //here we need change only one item (first)
            recyclerView.getAdapter().notifyItemChanged(0);
        }
        if (key.equals(this.getString(R.string.pref_design_key_text_size_article)))
        {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void setLoading(final boolean isLoading)
    {
//        Log.i(LOG, "isLoading: " + isLoading);
        this.isLoading = isLoading;
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

        RoboSpiceRequestArticle request = new RoboSpiceRequestArticle(ctx, article);
        spiceManager.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new ArticleRequestListener());
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
        public void onRequestSuccess(Article loadedArticle)
        {
            Log.i(LOG, "onRequestSuccess");
            if (!isAdded())
            {
                Log.e(LOG, "frag not added");
                return;
            }
            setLoading(false);

            article = loadedArticle;

            recyclerView.setAdapter(new RecyclerAdapterArticle(ctx, loadedArticle));
            //TODO
//                //update cover
//                BusProvider.getInstance().post(new EventArtsReceived(artsList));
//            }

            //////////////////////
//            ArrayList<HtmlToView.TextType> types = HtmlToView.getTextPartSummary(HtmlParsing.getElementListFromHtml(article.getText()));
//            ArrayList<String> listOfParts = HtmlToView.getTextPartsList(HtmlParsing.getElementListFromHtml(article.getText()));

//            Log.i(LOG, "types size: " + types.size());
//            Log.i(LOG, "listOfParts size: " + listOfParts.size());
//            Log.i(LOG, "!!!!!!!!!!!!!!!!!!!!!!!");
//            for (String s : listOfParts)
//            {
//                Log.i(LOG, s.substring(0));
//            }
//            Log.i(LOG, "!!!!!!!!!!!!!!!!!!!!!!!");
        }

        @Override
        public void onRequestNotFound()
        {
//            Log.i(LOG, "onRequestNotFound called");
        }
    }
}