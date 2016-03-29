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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.fragment.FragmentCategoriesAndTags;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.tproger.navigation.OnPageChangeListenerMain;
import ru.kuchanov.tproger.navigation.PagerAdapterCatsAndTags;
import ru.kuchanov.tproger.otto.BusProvider;
import ru.kuchanov.tproger.otto.EventCatsTagActivateItem;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.anim.ChangeImageWithAlpha;

/**
 * Created by Юрий on 01.02.2016 17:16.
 * For TProger.
 */
public class ActivityCategoriesAndTags extends ActivityBase
{
    //constants
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_IS_COLLAPSED = "KEY_IS_COLLAPSED";
    private static final String KEY_PREV_COVER_SOURCE = "KEY_PREV_COVER_SOURCE";
    private static final String LOG = ActivityCategoriesAndTags.class.getSimpleName();
    private int positionInList = -1;
    ////////
//    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    //    private NavigationView navigationView;
//    private DrawerLayout drawerLayout;
//    private ActionBarDrawerToggle mDrawerToggle;
    private boolean drawerOpened;
    private ViewPager pager;
    private CoordinatorLayout coordinatorLayout;
    private boolean isCollapsed = true;
    private View cover2Border;
    private AppBarLayout appBar;
    //    private MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
//    private MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
    private FloatingActionButton fab;
    //listeners for navView and pager
    private OnPageChangeListenerMain onPageChangeListenerMain;
    //    protected View cover2;
    private View coverThatChangesAlpha;
    private ImageView cover;
    private int verticalOffsetPrevious = 0;
    //    private Context ctx;
    //    private SharedPreferences pref;
    ///animations
    private ArrayList<Article> artsWithImage = new ArrayList<>();
    private int prevPosOfImage = -1;
    private Timer timer;
    private TimerTask timerTask;
    private ChangeImageWithAlpha cr;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Tag> tags = new ArrayList<>();
    private int curDataType;
    //
    private int numOfColsInGridLayoutManager;

//    private boolean isTabletMode;

    public static void startActivityCatsAndTags(Context ctx, ArrayList<Category> cats, ArrayList<Tag> tags, int curDataType, int positionInList)
    {
        Intent intent = new Intent(ctx, ActivityCategoriesAndTags.class);
        Bundle b = new Bundle();
        b.putParcelableArrayList(Category.LOG, cats);
        b.putParcelableArrayList(Tag.LOG, tags);
        b.putInt(FragmentCategoriesAndTags.KEY_CATS_OR_TAGS_DATA_TYPE, curDataType);
        b.putInt(KEY_POSITION, positionInList);
        intent.putExtras(b);

        ctx.startActivity(intent);
    }

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
        //get if isTabetMode
        this.isTabletMode = this.pref.getBoolean(getString(R.string.pref_design_key_tablet_mode), false);

        //set theme before super and set content to apply it
        int themeId = (pref.getBoolean(ActivitySettings.PREF_KEY_NIGHT_MODE, false)) ? R.style.My_Theme_Dark : R.style.My_Theme_Light;
        this.setTheme(themeId);
        //call super after setTheme to set it 0_0
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cats__and_tags_tablet);

        this.restoreState(savedInstanceState, getIntent().getExtras());

        this.initializeViews();
        this.setUpNavigationDrawer();

        //add fragCatsAndTags in left contatiner
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragCatsAndTags = manager.findFragmentById(R.id.container_left);
        switch (curDataType)
        {
            case FragmentCategoriesAndTags.TYPE_CATEGORY:
                if (isTabletMode)
                {
                    if (fragCatsAndTags == null)
                    {
                        fragCatsAndTags = FragmentCategoriesAndTags.newInstance(FragmentCategoriesAndTags.TYPE_CATEGORY, categories, tags, positionInList);
                        transaction.add(R.id.container_left, fragCatsAndTags);
                        transaction.commit();
                    }
                }
                break;
            case FragmentCategoriesAndTags.TYPE_TAG:
                if (isTabletMode)
                {
                    if (fragCatsAndTags == null)
                    {
                        fragCatsAndTags = FragmentCategoriesAndTags.newInstance(FragmentCategoriesAndTags.TYPE_TAG, categories, tags, positionInList);
                        transaction.add(R.id.container_left, fragCatsAndTags);
                        transaction.commit();
                    }
                }
                break;
        }

        //setup pager
        this.pager.setAdapter(new PagerAdapterCatsAndTags(manager, ctx, categories, tags, curDataType));
        ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                positionInList = position;

                String title = "";
                switch (curDataType)
                {
                    case FragmentCategoriesAndTags.TYPE_CATEGORY:
                        title = categories.get(positionInList).getTitle();
                        break;
                    case FragmentCategoriesAndTags.TYPE_TAG:
                        title = tags.get(positionInList).getTitle();
                        break;
                }
                collapsingToolbarLayout.setTitle(title);
                EventCatsTagActivateItem eventCatsTagActivateItem = new EventCatsTagActivateItem(position);
                BusProvider.getInstance().post(eventCatsTagActivateItem);
            }
        };
        this.pager.addOnPageChangeListener(onPageChangeListener);
        //fix not calling to onPageSelected if need to select first page
        if (positionInList == 0)
        {
            onPageChangeListener.onPageSelected(positionInList);
        }
        else
        {
            this.pager.setCurrentItem(positionInList, true);
        }
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

        pager = (ViewPager) findViewById(R.id.pager);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        fab = (FloatingActionButton) findViewById(R.id.fab);
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
                }
            };
            //show arrow instead of hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(false);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }
        NavigationView.OnNavigationItemSelectedListener navigationViewOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem item)
            {
                //TODO
                Log.d(LOG, "onNavigationItemSelected called");
                return false;
            }
        };
        navigationView.setNavigationItemSelectedListener(navigationViewOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStop()
    {
//        Log.i(LOG, "onStop called!");
        super.onStop();
        //stop and cancel all timers that manages animations
        if (timer != null && timerTask != null)
        {
            timerTask.cancel();
            timer.cancel();

            timer = null;
            timerTask = null;
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

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");
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
                onBackPressed();
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
        outState.putBoolean(KEY_IS_COLLAPSED, isCollapsed);
        outState.putInt(KEY_PREV_COVER_SOURCE, this.prevPosOfImage);
        outState.putParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE, artsWithImage);
        outState.putParcelableArrayList(Category.LOG, categories);
        outState.putParcelableArrayList(Tag.LOG, tags);
        outState.putInt(FragmentCategoriesAndTags.KEY_CATS_OR_TAGS_DATA_TYPE, curDataType);
        outState.putInt(KEY_POSITION, positionInList);
    }

    private void restoreState(Bundle savedInstanceState, Bundle args)
    {
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(this.getString(R.string.pref_design_key_col_num), "2"));

        if (savedInstanceState == null)
        {
            this.curDataType = args.getInt(FragmentCategoriesAndTags.KEY_CATS_OR_TAGS_DATA_TYPE);
            this.positionInList = args.getInt(KEY_POSITION);
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
            isCollapsed = savedInstanceState.getBoolean(KEY_IS_COLLAPSED, false);
            prevPosOfImage = savedInstanceState.getInt(KEY_PREV_COVER_SOURCE, -1);
            artsWithImage = savedInstanceState.getParcelableArrayList(Article.KEY_ARTICLES_LIST_WITH_IMAGE);

            this.positionInList = args.getInt(KEY_POSITION);
            this.curDataType = savedInstanceState.getInt(FragmentCategoriesAndTags.KEY_CATS_OR_TAGS_DATA_TYPE);
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
}