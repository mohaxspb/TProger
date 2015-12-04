package ru.kuchanov.tproger.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.fragment.FragmentCategory;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.ImageChanger;
import ru.kuchanov.tproger.navigation.OnNavigationItemSelectedListenerArticleActivity;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventArtsReceived;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.MyRandomUtil;
import ru.kuchanov.tproger.utils.MyUIL;

public class ActivityArticle extends AppCompatActivity implements /*DrawerUpdateSelected,*/ ImageChanger, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_CURRENT_ARTICLE_POSITION_IN_LIST = "KEY_CURRENT_ARTICLE_POSITION_IN_LIST";
    //    protected static final String NAV_ITEM_ID = "NAV_ITEM_ID";
    protected static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";
    protected static final String KEY_PREV_COVER_SOURCE = "KEY_PREV_COVER_SOURCE";
    private final static String LOG = ActivityArticle.class.getSimpleName();

    protected final int[] coverImgsIds = {R.drawable.tproger_small, R.drawable.cremlin, R.drawable.petergof};
    //views for tabletMode
    protected Toolbar toolbarLeft;
    protected CollapsingToolbarLayout collapsingToolbarLayoutLeft;
    protected CoordinatorLayout coordinatorLayoutLeft;
    protected ImageView coverLeft;
    protected View cover2Left;
    protected View cover2BorderLeft;
    protected AppBarLayout appBarLeft;
    protected LinearLayout mainContainer;
    protected FrameLayout leftContainer;
    //main views
    protected Toolbar toolbar;
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected NavigationView navigationView;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected boolean drawerOpened;
    protected ViewPager pager;
    protected CoordinatorLayout coordinatorLayout;
    //    protected int checkedDrawerItemId = R.id.tab_1;
    protected boolean isCollapsed = true;
    protected ImageView cover;
    protected View cover2;
    protected View cover2Border;
    protected AppBarLayout appBar;
    protected boolean fullyExpanded = true;
    /**
     * list of articles to show in pager and recyclerView
     */
    private ArrayList<Article> artsList = new ArrayList<>();
    private int currentPositionOfArticleInList = 0;
    //
    private int verticalOffsetPrevious = 0;
    private Context ctx;
    private SharedPreferences pref;
    private ArrayList<Article> artsWithImage = new ArrayList<>();
    private int prevPosOfImage = -1;

    private Timer timer;
    private TimerTask timerTask;

    private boolean isTabletMode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreate");

        this.ctx = this;

        //get default settings to get all settings later
        PreferenceManager.setDefaultValues(this, R.xml.pref_design, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_about, true);
        this.pref = PreferenceManager.getDefaultSharedPreferences(this);
        //set theme before super and set content to apply it
        int themeId = (pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false)) ? R.style.My_Theme_Dark : R.style.My_Theme_Light;
        this.setTheme(themeId);
        //call super after setTheme to set it 0_0
        super.onCreate(savedInstanceState);

        isTabletMode = pref.getBoolean(getString(R.string.pref_design_key_tablet_mode), false);

        //TODO make layout for phone
        setContentView(R.layout.activity_article);

        restoreStateFromIntent(this.getIntent().getExtras());
        restoreState(savedInstanceState);

        initializeViews();

        setUpNavigationDrawer();
        setUpPagerAndTabs();

//        appBar.addOnOffsetChangedListener(new MyOnOffsetChangedListener(this));

        if (isTabletMode)
        {
            setUpBackgroundAnimation(cover, cover2);
            setUpBackgroundAnimation(coverLeft, cover2Left);

            //Add category fragment
            FragmentCategory fragmentCategory;
            fragmentCategory = (FragmentCategory) getSupportFragmentManager().findFragmentById(R.id.container_left);
            if (fragmentCategory == null)
            {
                fragmentCategory = FragmentCategory.newInstance("");

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.container_left, fragmentCategory);
                fragmentTransaction.commit();
            }
        }
        else
        {
            setUpBackgroundAnimation(cover, cover2);
        }

        this.onArtsReceived(new EventArtsReceived(artsWithImage));

        this.pref.registerOnSharedPreferenceChangeListener(this);


    }

    private void initializeViews()
    {
        if (isTabletMode)
        {
            coverLeft = (ImageView) findViewById(R.id.cover_left);
            cover2Left = findViewById(R.id.cover_2_inside_left);
            cover2BorderLeft = findViewById(R.id.cover_2_border_left);

            appBarLeft = (AppBarLayout) this.findViewById(R.id.app_bar_layout_left);

            toolbarLeft = (Toolbar) findViewById(R.id.toolbar_left);
            collapsingToolbarLayoutLeft = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_left);

            coordinatorLayoutLeft = (CoordinatorLayout) this.findViewById(R.id.coordinator_left);

            mainContainer = (LinearLayout) findViewById(R.id.container_main);
            leftContainer = (FrameLayout) findViewById(R.id.container_left);
        }


        cover = (ImageView) findViewById(R.id.cover);
        cover2 = findViewById(R.id.cover_2_inside);
        cover2Border = findViewById(R.id.cover_2_border);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        appBar = (AppBarLayout) this.findViewById(R.id.app_bar_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        coordinatorLayout = (CoordinatorLayout) this.findViewById(R.id.coordinator);

        pager = (ViewPager) this.findViewById(R.id.pager);
    }

    private void setUpPagerAndTabs()
    {
        //TODO make adapter for articles;
//        pager.setAdapter(new PagerAdapterMain(this.getSupportFragmentManager(), 3));
//        pager.addOnPageChangeListener(new PagerAdapterOnPageChangeListener(this, this));
    }

    protected void setUpNavigationDrawer()
    {
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.hello_world, R.string.hello_world)
            {
                public void onDrawerClosed(View view)
                {
                    supportInvalidateOptionsMenu();
                    drawerOpened = false;
                }

                public void onDrawerOpened(View drawerView)
                {
                    drawerOpened = true;
//                    updateNavigationViewState(checkedDrawerItemId);
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }
        OnNavigationItemSelectedListenerArticleActivity navCL;
        navCL = new OnNavigationItemSelectedListenerArticleActivity(ctx);

        navigationView.setNavigationItemSelectedListener(navCL);

//        updateNavigationViewState(this.checkedDrawerItemId);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

        boolean nightModeIsOn = this.pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false);

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
                if (nightModeIsOn)
                {
                    this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false).commit();
                }
                else
                {
                    this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, true).commit();
                }
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
        outState.putInt(KEY_PREV_COVER_SOURCE, this.prevPosOfImage);
        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE, artsWithImage);

        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST, artsList);
        outState.putInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, currentPositionOfArticleInList);
    }

    @Override
    public void onBackPressed()
    {
        Log.i(LOG, "onBackPressed");
        super.onBackPressed();
    }

    private void restoreState(Bundle state)
    {
        if (state != null)
        {
//            checkedDrawerItemId = state.getInt(NAV_ITEM_ID, R.id.tab_1);
            isCollapsed = state.getBoolean(KEY_IS_COLLAPSED, false);
            prevPosOfImage = state.getInt(KEY_PREV_COVER_SOURCE, -1);
            artsWithImage = state.getParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE);

            artsList = state.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
            currentPositionOfArticleInList = state.getInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, 0);
        }
        else
        {
            Log.e(LOG, "state is null while restoring it from bundle");
        }
    }

    private void restoreStateFromIntent(Bundle stateFromIntent)
    {
        if (stateFromIntent != null)
        {
            artsList = stateFromIntent.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
            currentPositionOfArticleInList = stateFromIntent.getInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, 0);
        }
    }

    //workaround from http://stackoverflow.com/a/30337653/3212712
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch (Exception e)
                {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }

            boolean nightModeIsOn = this.pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false);
            MenuItem themeMenuItem = menu.findItem(R.id.night_mode_switcher);
            if (nightModeIsOn)
            {
                themeMenuItem.setChecked(true);
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void updateImage(final int positionInPager)
    {
//        Log.i(LOG, "updateImage with position in pager: "+positionInPager);

        cover2.setAlpha(0);
        cover2.setScaleX(1);
        cover2.setScaleY(1);
        cover2.animate().cancel();

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

        //prevent changing images if we are not on artsListFragment in main pager
        if (pager.getCurrentItem() != 0)
        {
            if (timer != null && timerTask != null)
            {
                timerTask.cancel();
                timer.cancel();
            }
        }
        else
        {
            this.onArtsReceived(new EventArtsReceived(artsWithImage));
            return;
        }

        //prevent showing transition coloring if cover isn't showing
        if (this.cover.getAlpha() == 0)
        {
            cover.setImageResource(coverImgsIds[positionInPager]);
            return;
        }

        cover2.animate().alpha(1).scaleX(15).scaleY(15).setDuration(600).setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                cover.setImageResource(coverImgsIds[positionInPager]);
                cover2.animate().alpha(0).setDuration(600);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });
    }

    @Override
    protected void onStart()
    {
        Log.i(LOG, "onStart called!");
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onStop()
    {
        Log.i(LOG, "onStop called!");
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onResume()
    {
        Log.i(LOG, "onResume called!");
        super.onResume();
    }

    @Subscribe
    public void onArtsReceived(final EventArtsReceived event)
    {
        Log.i(LOG, "EventArtsReceived: " + String.valueOf(event.getArts().size()));

        //fill list of arts in activity;
        this.artsList.clear();
        this.artsList.addAll(event.getArts());

        //fill artsWithImage list
        artsWithImage = new ArrayList<>();
        //we need to create new instance of list, because if we clera old,
        //we'll get 0 size list in onAnimationEnd...
        //And i dont now why((((
//        artsWithImage.clear();
        for (Article a : event.getArts())
        {
            if (a.getImageUrl() != null)
            {
                artsWithImage.add(a);
            }
        }

        if (timer != null && timerTask != null)
        {
            timerTask.cancel();
            timer.cancel();
        }

        //prevent changing images if we are not in tabletMode
        if (!this.isTabletMode || artsWithImage.size() == 0)
        {
            return;
        }

        timer = new Timer();
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateImageFromArts(artsWithImage, coverLeft, cover2Left);
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    public void updateImageFromArts(final ArrayList<Article> artsWithImage, final ImageView cover, final View cover2)
    {
//        Log.i(LOG, "updateImage with position in pager: "+positionInPager);
        final int positionInList;
        switch (artsWithImage.size())
        {
            case 0:
                //cant be, but return anyway;
                return;
            case 1:
                positionInList = 0;
                break;
            default:
                positionInList = MyRandomUtil.nextInt(prevPosOfImage, artsWithImage.size());
                break;
        }
        prevPosOfImage = positionInList;

        cover2.setAlpha(0);
        cover2.setScaleX(1);
        cover2.setScaleY(1);
        cover2.animate().cancel();

        //prevent showing transition coloring if cover isn't showing
        if (cover.getAlpha() == 0)
        {
            MyUIL.getDefault(ctx).displayImage(artsWithImage.get(positionInList).getImageUrl(), cover);
            return;
        }

        cover2.animate().alpha(1).scaleX(15).scaleY(15).setDuration(800).setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
//                if (artsWithImage.size() != 0)
//                {
                MyUIL.getDefault(ctx).displayImage(artsWithImage.get(positionInList).getImageUrl(), cover);
//                }
                cover2.animate().alpha(0).setDuration(800);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });
    }

    private void setUpBackgroundAnimation(View cover, View cover2)
    {
        cover.setAlpha(0f);
        cover.setScaleX(1.3f);
        cover.setScaleY(1.3f);
        cover.animate().alpha(1).setDuration(600);

        cover2.setAlpha(0);

        this.startAnimation(cover);
    }

    public void startAnimation(final View cover)
    {
        cover.setVisibility(View.VISIBLE);

        final int animResId = R.anim.cover_image;

        Animation anim = AnimationUtils.loadAnimation(this, animResId);
        anim.setAnimationListener(new Animation.AnimationListener()
        {

            @Override
            public void onAnimationEnd(Animation arg0)
            {
                Animation anim = AnimationUtils.loadAnimation(ctx, animResId);
                anim.setAnimationListener(this);
                cover.startAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation arg0)
            {
            }

            @Override
            public void onAnimationStart(Animation arg0)
            {
            }
        });

        cover.startAnimation(anim);
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

    public ImageView getCover()
    {
        return cover;
    }

    public View getCover2Border()
    {
        return cover2Border;
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    public ViewPager getPager()
    {
        return this.pager;
    }


    public CollapsingToolbarLayout getCollapsingToolbarLayout()
    {
        return collapsingToolbarLayout;
    }

//    public boolean isFullyExpanded()
//    {
//        return fullyExpanded;
//    }

    public void setFullyExpanded(boolean fullyExpanded)
    {
        this.fullyExpanded = fullyExpanded;
//        Log.i(LOG, "fullyExpanded: " + fullyExpanded);
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