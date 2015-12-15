package ru.kuchanov.tproger.activity;

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
import android.support.design.widget.TabLayout;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.DrawerUpdateSelected;
import ru.kuchanov.tproger.navigation.ImageChanger;
import ru.kuchanov.tproger.navigation.MyOnOffsetChangedListener;
import ru.kuchanov.tproger.navigation.NavigationViewOnNavigationItemSelectedListener;
import ru.kuchanov.tproger.navigation.OnPageChangeListenerMain;
import ru.kuchanov.tproger.navigation.PagerAdapterMain;
import ru.kuchanov.tproger.navigation.TabLayoutOnTabSelectedListener;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventArtsReceived;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.utils.DataBaseFileSaver;
import ru.kuchanov.tproger.utils.MyColorFilter;
import ru.kuchanov.tproger.utils.MyRandomUtil;
import ru.kuchanov.tproger.utils.MyUIL;
import ru.kuchanov.tproger.utils.anim.ChangeImageWithAlpha;

public class ActivityMain extends AppCompatActivity implements DrawerUpdateSelected, ImageChanger, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String NAV_ITEM_ID = "NAV_ITEM_ID";
    protected static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";
    protected static final String KEY_PREV_COVER_SOURCE = "KEY_PREV_COVER_SOURCE";

    private final static String LOG = ActivityMain.class.getSimpleName();

    protected final int[] coverImgsIds = {R.drawable.tproger_small, R.drawable.cremlin, R.drawable.petergof};
    protected Toolbar toolbar;
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected NavigationView navigationView;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected boolean drawerOpened;
    protected ViewPager pager;
    protected CoordinatorLayout coordinatorLayout;

    protected int checkedDrawerItemId = R.id.tab_1;
    protected boolean isCollapsed = true;
    protected View cover2Border;
    protected AppBarLayout appBar;
    protected TabLayout tabLayout;
    protected boolean fullyExpanded = true;
    ///////////
    protected MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    protected MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
    //listeners for navView and pager
    OnPageChangeListenerMain onPageChangeListenerMain;
    NavigationViewOnNavigationItemSelectedListener navigationViewOnNavigationItemSelectedListener;
    //    protected View cover2;
    private View coverThatChangesAlpha;
    private ImageView cover;
    private int verticalOffsetPrevious = 0;
    private Context ctx;
    private SharedPreferences pref;
    ///animations
    private ArrayList<Article> artsWithImage = new ArrayList<>();
    private int prevPosOfImage = -1;
    private Timer timer;
    private TimerTask timerTask;


    //    private SupportAnimator animator;
    private ChangeImageWithAlpha cr;

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

        setContentView(R.layout.activity_main);

        restoreState(savedInstanceState);
        restoreFromIntent();

        initializeViews();

        setUpNavigationDrawer();
        setUpPagerAndTabs();

        appBar.addOnOffsetChangedListener(new MyOnOffsetChangedListener(this));

        setUpBackgroundAnimation();

        this.onArtsReceived(new EventArtsReceived(new ArrayList<>(artsWithImage)));

        this.pref.registerOnSharedPreferenceChangeListener(this);

        MyColorFilter.applyColorFromAttr(ctx, cover, R.attr.colorAccent);

        cr = new ChangeImageWithAlpha();
        cr.setValues(ctx, coverThatChangesAlpha, cover, artsWithImage);
    }

    private void initializeViews()
    {
        cover = (ImageView) findViewById(R.id.cover);
        coverThatChangesAlpha = findViewById(R.id.cover_to_fill);
        cover2Border = findViewById(R.id.cover_2_border);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        appBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        pager = (ViewPager) findViewById(R.id.pager);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
    }


    private void setUpBackgroundAnimation()
    {
        cover.setAlpha(0f);
        cover.setScaleX(1.3f);
        cover.setScaleY(1.3f);
        cover.animate().alpha(1).setDuration(600);

        coverThatChangesAlpha.setVisibility(View.INVISIBLE);

        this.startAnimation();
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
                    updateNavigationViewState(checkedDrawerItemId);
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }
        navigationViewOnNavigationItemSelectedListener = new NavigationViewOnNavigationItemSelectedListener(this, drawerLayout, pager);

        navigationView.setNavigationItemSelectedListener(navigationViewOnNavigationItemSelectedListener);

        updateNavigationViewState(this.checkedDrawerItemId);
    }

    private void setUpPagerAndTabs()
    {
        pager.setAdapter(new PagerAdapterMain(this.getSupportFragmentManager(), 3));

        onPageChangeListenerMain = new OnPageChangeListenerMain(this, this);

        pager.addOnPageChangeListener(onPageChangeListenerMain);

        tabLayout.addTab(tabLayout.newTab().setText("Tab 111111111111"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 222222222222"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 333333333333"));

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayoutOnTabSelectedListener(this, pager));

        navigationViewOnNavigationItemSelectedListener.onNavigationItemSelected(navigationView.getMenu().findItem(checkedDrawerItemId));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        navigationView.setCheckedItem(checkedDrawerItemId);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");

        MyRoboSpiceDatabaseHelper h = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

        int id = item.getItemId();

        boolean nightModeIsOn = this.pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false);
        boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);

        switch (id)
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                this.startActivity(intent);
                return true;
            case android.R.id.home:
                this.drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.night_mode_switcher:
                this.pref.edit().putBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, !nightModeIsOn).commit();
                this.recreate();
                return true;
            case R.id.list_style_switcher:
                this.pref.edit().putBoolean(ctx.getString(R.string.pref_design_key_list_style), !isGridManager).commit();
                this.supportInvalidateOptionsMenu();
                return true;
            case R.id.text_size_dialog:
                FragmentDialogTextAppearance frag = FragmentDialogTextAppearance.newInstance();
                frag.show(getFragmentManager(), "TextAppearance");
                return true;
            case R.id.db_export:
                String DBWritingResult = DataBaseFileSaver.copyDatabase(ctx, MyRoboSpiceDatabaseHelper.DB_NAME);
                Toast.makeText(ctx, DBWritingResult, Toast.LENGTH_LONG).show();
                return true;
            case R.id.db_recreate:
                h.recreateDB();
                this.recreate();
                return true;
            case R.id.db_delete_last_art_cat:
                int quontOfDeletedArtCats = ArticleCategory.deleteAllLastArtCatInCategory(h, "");
                Log.i(LOG, "quontOfDeletedArtCats: " + quontOfDeletedArtCats);
                return true;
            case R.id.db_delete_first_artcat_in_category:
                Category.deleteFirstInCatAndUpdateSecond(h, "");
                return true;
        }

        return super.onOptionsItemSelected(item);
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

            boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
            MenuItem listStyleMenuItem = menu.findItem(R.id.list_style_switcher);
            if (isGridManager)
            {
                listStyleMenuItem.setChecked(true);
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, this.checkedDrawerItemId);
        outState.putBoolean(KEY_IS_COLLAPSED, isCollapsed);
        outState.putInt(KEY_PREV_COVER_SOURCE, this.prevPosOfImage);
        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE, artsWithImage);
    }

    private void restoreState(Bundle state)
    {
        if (state != null)
        {
            checkedDrawerItemId = state.getInt(NAV_ITEM_ID, R.id.tab_1);
            isCollapsed = state.getBoolean(KEY_IS_COLLAPSED, false);
            prevPosOfImage = state.getInt(KEY_PREV_COVER_SOURCE, -1);
            artsWithImage = state.getParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE);
        }
        else
        {
            Log.e(LOG, "state is null while restoring it from savedInstanceState");
        }
    }

    private void restoreFromIntent()
    {
        Intent curIntent = getIntent();

        this.checkedDrawerItemId = curIntent.getIntExtra(NAV_ITEM_ID, R.id.tab_1);
    }

    @Override
    public void updateNavigationViewState(int checkedDrawerItemId)
    {
        this.checkedDrawerItemId = checkedDrawerItemId;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void updateImage(final int positionInPager)
    {
//        Log.i(LOG, "updateImage with position in pager: "+positionInPager);

        //that is normal. Use it if other attempts fails;
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
//        if (pager.getCurrentItem() != 0)
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

        //TODO add supporing preSet images showing
        cr.animate(0);
    }


    @Override
    protected void onStart()
    {
        Log.i(LOG, "onStart called!");
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
        Log.i(LOG, "onResume called!");
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

    @Override
    protected void onStop()
    {
        Log.i(LOG, "onStop called!");
        super.onStop();
        //should unregister in onStop to avoid some issues while pausing activity/fragment
        //see http://stackoverflow.com/a/19737191/3212712
        BusProvider.getInstance().unregister(this);

        //stop and cancel all timers that manages animations
        if (timer != null && timerTask != null)
        {
            timerTask.cancel();
            timer.cancel();

            timer = null;
            timerTask = null;
        }
    }

    @Override
    protected void onRestart()
    {
        Log.i(LOG, "onRestart called!");
        super.onRestart();

        //check if timer is null (it's null after onStop)
        //and restart it by calling onArtsReceiver to recreate it
        this.onArtsReceived(new EventArtsReceived(this.artsWithImage));
    }

    @Override
    protected void onPause()
    {
        Log.i(LOG, "onPause called!");
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

    @Subscribe
    public void onArtsReceived(final EventArtsReceived event)
    {
        Log.i(LOG, "EventArtsReceived: " + String.valueOf(event.getArts().size()));

        artsWithImage.clear();
        for (Article a : event.getArts())
        {
            if (a.getImageUrl() != null)
            {
                artsWithImage.add(a);
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
            return;
        }

        if (artsWithImage.size() != 0)
        {
            if (timer != null && timerTask != null)
            {
                timerTask.cancel();
                timer.cancel();
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
                            updateImageFromArts(artsWithImage);
                        }
                    });
                }
            };
            timer.schedule(timerTask, 0, 5000);


            if (cr == null)
            {
                cr = new ChangeImageWithAlpha();
                cr.setValues(ctx, coverThatChangesAlpha, cover, artsWithImage);
            }
            else
            {
                cr.updateArtsList(artsWithImage);
            }
        }
    }

    public void updateImageFromArts(final ArrayList<Article> artsWithImage)
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

        coverThatChangesAlpha.setVisibility(View.INVISIBLE);

        //prevent showing transition coloring if cover isn't showing
        if (this.cover.getAlpha() == 0)
        {
            MyUIL.getDefault(ctx).displayImage(artsWithImage.get(positionInList).getImageUrl(), cover);
            return;
        }

        cr.animate(positionInList);
    }


    public void startAnimation()
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

    public TabLayout getTabLayout()
    {
        return tabLayout;
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