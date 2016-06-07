package ru.kuchanov.tproger.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.fragment.FragmentCategoriesAndTags;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.DrawerUpdateSelected;
import ru.kuchanov.tproger.navigation.FabUpdater;
import ru.kuchanov.tproger.navigation.MyOnOffsetChangedListener;
import ru.kuchanov.tproger.navigation.NavigationViewOnNavigationItemSelectedListener;
import ru.kuchanov.tproger.navigation.OnPageChangeListenerMain;
import ru.kuchanov.tproger.navigation.PagerAdapterMain;
import ru.kuchanov.tproger.navigation.TabLayoutOnTabSelectedListener;
import ru.kuchanov.tproger.otto.EventCatsTagsShow;
import ru.kuchanov.tproger.otto.EventRestartShowingArtsImgs;
import ru.kuchanov.tproger.otto.EventShowImage;
import ru.kuchanov.tproger.otto.SingltonOtto;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.DataBaseFileSaver;
import ru.kuchanov.tproger.utils.MyColorFilter;
import ru.kuchanov.tproger.utils.NotificationUtils;
import ru.kuchanov.tproger.utils.anim.MyAnimationUtils;

public class ActivityMain extends ActivityBase implements DrawerUpdateSelected, FabUpdater, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String NAV_ITEM_ID = "NAV_ITEM_ID";
    protected static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";

    private final static String LOG = ActivityMain.class.getSimpleName();
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected ViewPager pager;
    protected CoordinatorLayout coordinatorLayout;
    protected int checkedDrawerItemId = R.id.tab_1;
    protected boolean isCollapsed = true;
    protected View cover2Border;
    protected AppBarLayout appBar;
    protected TabLayout tabLayout;
    protected boolean fullyExpanded = true;

    private NavigationViewOnNavigationItemSelectedListener navigationViewOnNavigationItemSelectedListener;
    private FloatingActionButton fab;
    private View coverThatChangesAlpha;
    private ImageView cover;
    private int verticalOffsetPrevious = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG, "onCreate");
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

        //check if notif is on and enable and set it if need
        NotificationUtils.checkIfSet(getApplicationContext());

        setContentView(R.layout.activity_main);

        restoreState(savedInstanceState);
        restoreFromIntent();

        initializeViews();

        setUpNavigationDrawer();
        setUpPagerAndTabs();

        appBar.addOnOffsetChangedListener(new MyOnOffsetChangedListener(this));

        this.pref.registerOnSharedPreferenceChangeListener(this);

        MyColorFilter.applyColorFromAttr(ctx, cover, R.attr.colorAccent);

        cover.setAlpha(0f);
        cover.setScaleX(1.3f);
        cover.setScaleY(1.3f);
        cover.animate().alpha(1).setDuration(600);

        coverThatChangesAlpha.setVisibility(View.INVISIBLE);
        MyAnimationUtils.startTranslateAnimation(ctx, cover);
    }

    protected void initializeViews()
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

        fab = (FloatingActionButton) findViewById(R.id.fab);
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
                }

                public void onDrawerOpened(View drawerView)
                {
                    updateNavigationViewState(checkedDrawerItemId);
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            drawerLayout.addDrawerListener(mDrawerToggle);
        }
        navigationViewOnNavigationItemSelectedListener = new NavigationViewOnNavigationItemSelectedListener(this, drawerLayout, pager);

        navigationView.setNavigationItemSelectedListener(navigationViewOnNavigationItemSelectedListener);

        updateNavigationViewState(this.checkedDrawerItemId);
    }

    private void setUpPagerAndTabs()
    {
        pager.setAdapter(new PagerAdapterMain(this.getSupportFragmentManager(), 3, ctx));

        OnPageChangeListenerMain onPageChangeListenerMain = new OnPageChangeListenerMain(this, this);

        pager.addOnPageChangeListener(onPageChangeListenerMain);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);

                //updateImage for 2 and 3 frags with default logo
                if (position != 0)
                {
                    updateImage(new EventShowImage(null));
                }
                else
                {
                    SingltonOtto.getInstance().post(new EventRestartShowingArtsImgs());
                }

                //show collapsed toolbar with tabs on pageChanging
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
        });

        String[] drawerItems = ctx.getResources().getStringArray(R.array.drawer_items);

        tabLayout.addTab(tabLayout.newTab().setText(drawerItems[0]));
        tabLayout.addTab(tabLayout.newTab().setText(drawerItems[1]));
        tabLayout.addTab(tabLayout.newTab().setText(drawerItems[2]));

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayoutOnTabSelectedListener(this, pager));

        navigationViewOnNavigationItemSelectedListener.onNavigationItemSelected(navigationView.getMenu().findItem(checkedDrawerItemId));
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

        boolean nightModeIsOn = this.pref.getBoolean(getString(R.string.pref_design_key_night_mode), false);
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
                this.pref.edit().putBoolean(getString(R.string.pref_design_key_night_mode), !nightModeIsOn).commit();
//                this.recreate();
                return true;
            case R.id.list_style_switcher:
                this.pref.edit().putBoolean(ctx.getString(R.string.pref_design_key_list_style), !isGridManager).apply();
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

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, this.checkedDrawerItemId);
        outState.putBoolean(KEY_IS_COLLAPSED, isCollapsed);
    }

    private void restoreState(Bundle state)
    {
        if (state != null)
        {
            checkedDrawerItemId = state.getInt(NAV_ITEM_ID, R.id.tab_1);
            isCollapsed = state.getBoolean(KEY_IS_COLLAPSED, false);
        }
        else
        {
            Log.e(LOG, "state is null while restoring it from savedInstanceState");
        }
    }

    private void restoreFromIntent()
    {
        Intent curIntent = getIntent();
        if (curIntent.hasExtra(NAV_ITEM_ID))
        {
            this.checkedDrawerItemId = curIntent.getIntExtra(NAV_ITEM_ID, R.id.tab_1);
        }
    }

    @Override
    public void updateNavigationViewState(int checkedDrawerItemId)
    {
        this.checkedDrawerItemId = checkedDrawerItemId;
        supportInvalidateOptionsMenu();
    }

    @Subscribe
    public void updateImage(EventShowImage eventShowImage)
    {
//        Log.d(LOG, "updateImage called");
        String imageUrl = eventShowImage.getImageUrl();
        MyAnimationUtils.changeImageWithAlphaAnimation(coverThatChangesAlpha, cover, imageUrl);
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

//    public OnPageChangeListenerMain getOnPageChangeListenerMain()
//    {
//        return onPageChangeListenerMain;
//    }

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

    @SuppressLint("CommitPrefEdits")
    @Override
    public void updateFAB(final int positionInViewPager)
    {
        switch (positionInViewPager)
        {
            case 0:
                fab.setImageResource(AttributeGetter.getDrawableId(ctx, R.attr.downloadIconWhite));
                fab.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.d(LOG, "FAB clicked for load arts with pagers position: " + positionInViewPager);
                        //TODO load arts
                    }
                });
                break;
            case 1:
                final String key = getString(R.string.pref_design_key_category_in_cats_or_tags);
                final boolean showCategories = pref.getBoolean(key, true);
                int attr = showCategories ? R.attr.formatIndentIncreaseIconWhite : R.attr.formatIndentDecreaseIconWhite;
                int imgId = AttributeGetter.getDrawableId(ctx, attr);
                fab.setImageResource(imgId);
                fab.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.d(LOG, "FAB clicked for show tags/cats with pagers position: " + positionInViewPager);

                        //change image
                        String key = getString(R.string.pref_design_key_category_in_cats_or_tags);
                        boolean showCategories = pref.getBoolean(key, true);
                        int attr = !showCategories ? R.attr.formatIndentIncreaseIconWhite : R.attr.formatIndentDecreaseIconWhite;
                        int imgId = AttributeGetter.getDrawableId(ctx, attr);
                        fab.setImageResource(imgId);

                        //update pref value
                        pref.edit().putBoolean(key, !showCategories).commit();


                        //change type in recyclers adapter
                        int dataType = pref.getBoolean(key, true) ? FragmentCategoriesAndTags.TYPE_CATEGORY : FragmentCategoriesAndTags.TYPE_TAG;
                        String newType = (dataType == FragmentCategoriesAndTags.TYPE_CATEGORY) ? "TYPE_CATEGORY" : "TYPE_TAG";
                        Log.d(LOG, "FAB clicked type: " + newType);
                        SingltonOtto.getInstance().post(new EventCatsTagsShow(dataType));
                    }
                });
                break;
            case 2:
                fab.setImageResource(AttributeGetter.getDrawableId(ctx, R.attr.downloadIconWhite));
                fab.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.d(LOG, "FAB clicked for load arts with pagers position: " + positionInViewPager);
                        //TODO load arts
                    }
                });
                break;
        }
    }
}