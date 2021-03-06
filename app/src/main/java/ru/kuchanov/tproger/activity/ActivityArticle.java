package ru.kuchanov.tproger.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.ImageChanger;
import ru.kuchanov.tproger.navigation.MyOnOffsetChangedListenerArticleActivity;
import ru.kuchanov.tproger.navigation.OnNavigationItemSelectedListenerArticleActivity;
import ru.kuchanov.tproger.navigation.PagerAdapterArticle;
import ru.kuchanov.tproger.otto.EventArtsReceived;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.anim.MyAnimationUtils;

public class ActivityArticle extends ActivityBase implements ImageChanger, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_CURRENT_ARTICLE_POSITION_IN_LIST = "KEY_CURRENT_ARTICLE_POSITION_IN_LIST";
    public static final String KEY_CURRENT_CATEGORY_OR_TAG_URL = "KEY_CURRENT_CATEGORY_OR_TAG_URL";
    private static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";
    //    private static final String KEY_PREV_COVER_SOURCE = "KEY_PREV_COVER_SOURCE";
    private final static String LOG = ActivityArticle.class.getSimpleName();

//    protected final int[] coverImgsIds = {R.drawable.tproger_small, R.drawable.cremlin, R.drawable.petergof};

    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected ViewPager pager;
    protected CoordinatorLayout coordinatorLayout;
    protected ImageView toolbarImage;
    protected View coverThatChangesAlpha;
    protected View cover2Border;
    protected AppBarLayout appBar;
    /**
     * list of articles to show in pager and recyclerView
     */
    private ArrayList<Article> artsList = new ArrayList<>();
    private int currentPositionOfArticleInList = -1;
    //
//    private ArrayList<Article> artsWithImage = new ArrayList<>();

    private String categoryOrTagUrl;
    private boolean fullyExpanded=true;
    private boolean isCollapsed = true;
    private int verticalOffsetPrevious = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate");
        this.ctx = this;

        //get default settings to get all settings later
        PreferenceManager.setDefaultValues(this, R.xml.pref_design, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_about, true);
        this.pref = PreferenceManager.getDefaultSharedPreferences(this);
        //set theme before super and set content to apply it
        int themeId = (pref.getBoolean(getString(R.string.pref_design_key_night_mode), false)) ? R.style.My_Theme_Dark : R.style.My_Theme_Light;
        this.setTheme(themeId);
        //call super after setTheme to set it 0_0
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article);

        restoreData(savedInstanceState, getIntent().getExtras());

        initializeViews();

        setUpNavigationDrawer(false, new OnNavigationItemSelectedListenerArticleActivity(ctx));
        setUpPager();

        appBar.addOnOffsetChangedListener(new MyOnOffsetChangedListenerArticleActivity(this));

        toolbarImage.setAlpha(0f);
        toolbarImage.setScaleX(1.3f);
        toolbarImage.setScaleY(1.3f);
        toolbarImage.animate().alpha(1).setDuration(600);

        coverThatChangesAlpha.setVisibility(View.INVISIBLE);
        MyAnimationUtils.startTranslateAnimation(ctx, toolbarImage);

        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    protected void initializeViews()
    {
        toolbarImage = (ImageView) findViewById(R.id.cover);
        coverThatChangesAlpha = findViewById(R.id.cover_to_fill);
        cover2Border = findViewById(R.id.cover_2_border);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        appBar = (AppBarLayout) this.findViewById(R.id.app_bar_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        coordinatorLayout = (CoordinatorLayout) this.findViewById(R.id.coordinator);

        pager = (ViewPager) this.findViewById(R.id.pager);
    }

    private void setUpPager()
    {
        pager.setAdapter(new PagerAdapterArticle(this.getSupportFragmentManager(), this.artsList));
        ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                currentPositionOfArticleInList = position;
                collapsingToolbarLayout.setTitle(artsList.get(currentPositionOfArticleInList).getTitle());
                String imgUrl = artsList.get(currentPositionOfArticleInList).getImageUrl();
                MyAnimationUtils.changeImageWithAlphaAnimation(coverThatChangesAlpha, toolbarImage, imgUrl);
                if (!isCollapsed)
                {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
                    AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();

                    if (behavior != null)
                    {
                        int toolbarMinHeight = ViewCompat.getMinimumHeight(toolbar);
                        behavior.onNestedPreScroll(coordinatorLayout, appBar, pager, 0, -2 * toolbarMinHeight, new int[2]);
                    }
                }
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);
        //fix not calling to onPageSelected if need to select first page
        if (currentPositionOfArticleInList == 0)
        {
            onPageChangeListener.onPageSelected(currentPositionOfArticleInList);
        }
        else
        {
            this.pager.setCurrentItem(currentPositionOfArticleInList, true);
        }
    }

    /* Called whenever we call supportInvalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onPrepareOptionsMenu called");
        //recreate navigationView's menu, uncheck all items and set new checked item
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer);
        navigationView.getMenu().findItem(R.id.tab_1).setChecked(false);
        navigationView.getMenu().findItem(R.id.tab_2).setChecked(false);
        navigationView.getMenu().findItem(R.id.tab_3).setChecked(false);
//        navigationView.setCheckedItem(checkedDrawerItemId);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");

        int id = item.getItemId();

        boolean nightModeIsOn = this.pref.getBoolean(getString(R.string.pref_design_key_night_mode), false);

        switch (id)
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                this.startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.night_mode_switcher:
                this.pref.edit().putBoolean(getString(R.string.pref_design_key_night_mode), !nightModeIsOn).commit();
                this.recreate();
                return true;
            case R.id.text_size_dialog:
                FragmentDialogTextAppearance frag = FragmentDialogTextAppearance.newInstance();
                frag.show(getFragmentManager(), "TextAppearance");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
//        outState.putInt(NAV_ITEM_ID, this.checkedDrawerItemId);
        outState.putBoolean(KEY_IS_COLLAPSED, isCollapsed);
//        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE, artsWithImage);

        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST, artsList);
        outState.putInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, currentPositionOfArticleInList);
        outState.putString(KEY_CURRENT_CATEGORY_OR_TAG_URL, categoryOrTagUrl);
    }

    private void restoreData(Bundle savedInstanceState, Bundle args)
    {
        if (savedInstanceState != null)
        {
            if (savedInstanceState.containsKey(Article.KEY_ARTICLES_LIST))
            {
                this.artsList.clear();
                ArrayList<Article> articles = savedInstanceState.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
                if (articles != null)
                {
                    this.artsList.addAll(articles);
                }
            }
            currentPositionOfArticleInList = savedInstanceState.getInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, 0);
            this.categoryOrTagUrl = savedInstanceState.getString(KEY_CURRENT_CATEGORY_OR_TAG_URL);
        }
        else
        {
            if (args.containsKey(Article.KEY_ARTICLES_LIST))
            {
                this.artsList.clear();
                ArrayList<Article> articles = args.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
                if (articles != null)
                {
                    this.artsList.addAll(articles);
                }
            }
            currentPositionOfArticleInList = args.getInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, 0);
            this.categoryOrTagUrl = args.getString(KEY_CURRENT_CATEGORY_OR_TAG_URL);
        }
    }

    @Override
    public void updateImage(final int positionInPager)
    {
//        Log.i(LOG, "updateImage with position in pager: "+positionInPager);

        coverThatChangesAlpha.setAlpha(0);
        coverThatChangesAlpha.setScaleX(1);
        coverThatChangesAlpha.setScaleY(1);
        coverThatChangesAlpha.animate().cancel();

        //TODO that is normal. Use it if other attempts fails;
//        appBar.setExpanded(isCollapsed, true);

        if (!isCollapsed)
        {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();

            if (behavior != null)
            {
                int toolbarMinHeight = ViewCompat.getMinimumHeight(toolbar);
                behavior.onNestedPreScroll(coordinatorLayout, appBar, pager, 0, -2 * toolbarMinHeight, new int[2]);
            }
        }
    }

    @Subscribe
    public void onArtsReceived(final EventArtsReceived event)
    {
        Log.i(LOG, "EventArtsReceived: " + String.valueOf(event.getArts().size()));

        //fill list of arts in activity;
        this.artsList.clear();
        this.artsList.addAll(event.getArts());

        pager.getAdapter().notifyDataSetChanged();

//        //fill artsWithImage list
//        artsWithImage.clear();
//        //we need to create new instance of list, because if we clera old,
//        //we'll get 0 size list in onAnimationEnd...
//        //And i dont now why((((
////        artsWithImage.clear();
//        for (Article a : event.getArts())
//        {
//            if (a.getImageUrl() != null)
//            {
//                artsWithImage.add(a);
//            }
//        }
    }

    public boolean getIsCollapsed()
    {
        return this.isCollapsed;
    }

    public void setCollapsed(boolean isCollapsed)
    {
        this.isCollapsed = isCollapsed;
    }

    public int getVerticalOffsetPrevious()
    {
        return verticalOffsetPrevious;
    }

    public void setVerticalOffsetPrevious(int verticalOffsetPrevious)
    {
        this.verticalOffsetPrevious = verticalOffsetPrevious;
    }

    public CollapsingToolbarLayout getCollapsingToolbarLayout()
    {
        return collapsingToolbarLayout;
    }

    public View getCover2Border()
    {
        return cover2Border;
    }

    public void setFullyExpanded(boolean fullyExpanded)
    {
        this.fullyExpanded = fullyExpanded;
//        Log.i(LOG, "fullyExpanded: " + fullyExpanded);
    }

    public ImageView getToolbarImage()
    {
        return toolbarImage;
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(this.getString(R.string.pref_design_key_night_mode)))
        {
            this.recreate();
        }
    }
}