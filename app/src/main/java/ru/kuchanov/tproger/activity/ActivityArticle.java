package ru.kuchanov.tproger.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.fragment.FragmentCategory;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.ImageChanger;
import ru.kuchanov.tproger.navigation.OnNavigationItemSelectedListenerArticleActivity;
import ru.kuchanov.tproger.navigation.PagerAdapterArticle;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventArtsReceived;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.MyColorFilter;
import ru.kuchanov.tproger.utils.MyRandomUtil;
import ru.kuchanov.tproger.utils.MyUIL;
import ru.kuchanov.tproger.utils.anim.ChangeImageWithAlpha;

public class ActivityArticle extends AppCompatActivity implements /*DrawerUpdateSelected,*/ ImageChanger, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_CURRENT_ARTICLE_POSITION_IN_LIST = "KEY_CURRENT_ARTICLE_POSITION_IN_LIST";
    public static final String KEY_CURRENT_CATEGORY_OR_TAG_URL = "KEY_CURRENT_CATEGORY_OR_TAG_URL";
    private static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";
    private static final String KEY_PREV_COVER_SOURCE = "KEY_PREV_COVER_SOURCE";
    private final static String LOG = ActivityArticle.class.getSimpleName();

    protected final int[] coverImgsIds = {R.drawable.tproger_small, R.drawable.cremlin, R.drawable.petergof};
    //views for tabletMode
//    protected Toolbar toolbarLeft;
    protected CollapsingToolbarLayout collapsingToolbarLayoutLeft;
    protected CoordinatorLayout coordinatorLayoutLeft;
    protected ImageView coverLeft;
    protected View cover2Left;
    protected View cover2BorderLeft;
    protected AppBarLayout appBarLeft;
    protected LinearLayout mainContainer;
    protected FrameLayout leftContainer;

    protected Toolbar toolbarRight;
    //main views
    protected Toolbar toolbar;
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected NavigationView navigationView;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected boolean drawerOpened;
    protected ViewPager pager;
    protected CoordinatorLayout coordinatorLayout;
    protected boolean isCollapsed = true;
    protected ImageView cover;
    protected View cover2;
    protected View cover2Border;
    protected AppBarLayout appBar;
    protected boolean fullyExpanded = true;
    ///////////
    /////////////
//    ChangeImageWithAlpha changeImageWithAlpha;
    ChangeImageWithAlpha changeImageWithAlphaLeft;
    ImageLoader imageLoader;
    ///////////
//    protected MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManagerArticle();
//    protected MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOfflineArticle();
    private MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    private MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
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

    private String categoryOrTagUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
//        Log.i(LOG, "onCreate");

        this.ctx = this;

        imageLoader = MyUIL.get(ctx);

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

        isTabletMode = pref.getBoolean(getString(R.string.pref_design_key_tablet_mode), true);

        //TODO make layout for phone
        setContentView(R.layout.activity_article_tablet);

        if (savedInstanceState == null)
        {
            restoreStateFromIntent(this.getIntent().getExtras());
        }
        else
        {
            restoreState(savedInstanceState);
        }

        initializeViews();

        setUpNavigationDrawer();
        setUpPager();

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
                fragmentCategory = FragmentCategory.newInstance(categoryOrTagUrl);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.container_left, fragmentCategory);
                fragmentTransaction.commit();
            }
        }
        else
        {
            setUpBackgroundAnimation(cover, cover2);
        }

        this.onArtsReceived(new EventArtsReceived(new ArrayList<>(artsWithImage)));

        this.pref.registerOnSharedPreferenceChangeListener(this);


        if (isTabletMode)
        {
            /////////
            MyColorFilter.applyColorFromAttr(ctx, coverLeft, R.attr.colorAccent);

            changeImageWithAlphaLeft = new ChangeImageWithAlpha();
            changeImageWithAlphaLeft.setValues(ctx, cover2Left, coverLeft, artsWithImage);
        }
    }

    private void initializeViews()
    {
        if (isTabletMode)
        {
            coverLeft = (ImageView) findViewById(R.id.cover_left);
            cover2Left = findViewById(R.id.cover_to_fill_left);
            cover2BorderLeft = findViewById(R.id.cover_2_border_left);

            appBarLeft = (AppBarLayout) this.findViewById(R.id.app_bar_layout_left);

            collapsingToolbarLayoutLeft = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_left);

            coordinatorLayoutLeft = (CoordinatorLayout) this.findViewById(R.id.coordinator_left);

            mainContainer = (LinearLayout) findViewById(R.id.container_main);
            leftContainer = (FrameLayout) findViewById(R.id.container_left);

            toolbarRight = (Toolbar) findViewById(R.id.toolbar_right);
        }


        cover = (ImageView) findViewById(R.id.cover);
        cover2 = findViewById(R.id.cover_to_fill);
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
        //make adapter for articles;
        pager.setAdapter(new PagerAdapterArticle(this.getSupportFragmentManager(), this.artsList));
        //TODO
//        pager.addOnPageChangeListener(new OnPageChangeListenerMain(this, this));
    }

    protected void setUpNavigationDrawer()
    {
        //changing statusBarColor
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().setStatusBarColor(AttributeGetter.getColor(ctx, R.attr.colorPrimaryDark));
        }

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
            //show arrow instead of hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(false);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }
        OnNavigationItemSelectedListenerArticleActivity navCL;
        navCL = new OnNavigationItemSelectedListenerArticleActivity(ctx);

        navigationView.setNavigationItemSelectedListener(navCL);
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

    //workaround from http://stackoverflow.com/a/30337653/3212712 to show menu icons
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
                this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, !nightModeIsOn).commit();
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
        outState.putString(KEY_CURRENT_CATEGORY_OR_TAG_URL, categoryOrTagUrl);
    }

    @Override
    public void onBackPressed()
    {
        Log.i(LOG, "onBackPressed");
        if (drawerOpened)
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    private void restoreState(Bundle state)
    {
        isCollapsed = state.getBoolean(KEY_IS_COLLAPSED, false);
        prevPosOfImage = state.getInt(KEY_PREV_COVER_SOURCE, -1);
        artsWithImage = state.getParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE);

        artsList = state.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
        currentPositionOfArticleInList = state.getInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, 0);
        this.categoryOrTagUrl = state.getString(KEY_CURRENT_CATEGORY_OR_TAG_URL);
    }

    private void restoreStateFromIntent(Bundle stateFromIntent)
    {
        if (stateFromIntent != null)
        {
            artsList = stateFromIntent.getParcelableArrayList(Article.KEY_ARTICLES_LIST);
            currentPositionOfArticleInList = stateFromIntent.getInt(KEY_CURRENT_ARTICLE_POSITION_IN_LIST, 0);
            this.categoryOrTagUrl = stateFromIntent.getString(KEY_CURRENT_CATEGORY_OR_TAG_URL);
        }
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
        if (positionInPager != 0)
        {
            if (timer != null && timerTask != null)
            {
                timerTask.cancel();
                timer.cancel();
            }
        }
        else
        {
            this.onArtsReceived(new EventArtsReceived(new ArrayList<>(artsWithImage)));
            return;
        }

        //prevent showing transition coloring if cover isn't showing
        if (this.cover.getAlpha() == 0)
        {
            cover.setImageResource(coverImgsIds[positionInPager]);
            return;
        }
//        ChangeImageWithAlpha changeImageWithAlpha;
//        ChangeImageWithAlpha changeImageWithAlphaLeft;
        if (isTabletMode)
        {
            changeImageWithAlphaLeft.animate(0);
        }
    }

    @Override
    protected void onPause()
    {
//        Log.i(LOG, "onPause called!");
        super.onPause();

        if (spiceManager.isStarted())
        {
            spiceManager.shouldStop();
        }
        if (spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.shouldStop();
        }
    }

    @Override
    protected void onStop()
    {
//        Log.i(LOG, "onStop called with hash: " + this.hashCode());
        super.onStop();
        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onRestart()
    {
//        Log.i(LOG, "onRestart called!");
        super.onRestart();

        //check if timer is null (it's null after onStop)
        //and restart it by calling onArtsReceiver to recreate it
        this.onArtsReceived(new EventArtsReceived(this.artsWithImage));
    }


    @Override
    protected void onStart()
    {
//        Log.i(LOG, "onStart called!");
        super.onStart();
        BusProvider.getInstance().register(this);

        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
        if (!spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.start(ctx);
        }
    }

    @Override
    protected void onResume()
    {
//        Log.i(LOG, "onResume called!");
//        Log.i(LOG, "onResume called with hash: "+this.hashCode());
        super.onResume();

        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
        if (!spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.start(ctx);
        }
    }

    @Subscribe
    public void onArtsReceived(final EventArtsReceived event)
    {
//        Log.i(LOG, "EventArtsReceived: " + String.valueOf(event.getArts().size()));

        //fill list of arts in activity;
        this.artsList.clear();
        this.artsList.addAll(event.getArts());

        //fill artsWithImage list
        artsWithImage.clear();
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

        if (isTabletMode)
        {
            if (changeImageWithAlphaLeft == null)
            {
                changeImageWithAlphaLeft = new ChangeImageWithAlpha();
                changeImageWithAlphaLeft.setValues(ctx, cover2Left, coverLeft, artsWithImage);
            }
            else
            {
                changeImageWithAlphaLeft.updateArtsList(artsWithImage);
            }
        }
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
            imageLoader.displayImage(artsWithImage.get(positionInList).getImageUrl(), cover, DisplayImageOptions.createSimple());
            return;
        }

        cover2.setVisibility(View.INVISIBLE);

        //prevent showing transition coloring if cover isn't showing
        if (this.cover.getAlpha() == 0)
        {
            imageLoader.displayImage(artsWithImage.get(positionInList).getImageUrl(), cover, DisplayImageOptions.createSimple());
            return;
        }

        if (isTabletMode)
        {
            changeImageWithAlphaLeft.animate(positionInList);
        }
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

    public Toolbar getToolbarRight()
    {
        return toolbarRight;
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