package ru.kuchanov.tproger.navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import ru.kuchanov.tproger.fragment.FragmentCategories;
import ru.kuchanov.tproger.fragment.FragmentCategory;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;

/**
 * Created by Юрий on 17.09.2015 18:06 19:11.
 * For TProger.
 */
public class PagerAdapterCatsAndTags extends FragmentStatePagerAdapter
{
    private static final String LOG = PagerAdapterCatsAndTags.class.getSimpleName();

    private Context ctx;
    private SharedPreferences pref;
    private ArrayList<Category> cats;
    private ArrayList<Tag> tags;
    private int curType;// = FragmentCategories.TYPE_CATEGORY;

    public PagerAdapterCatsAndTags(FragmentManager fm, Context ctx, ArrayList<Category> cats, ArrayList<Tag> tags, int curType)
    {
        super(fm);
        this.ctx = ctx;
        this.cats = cats;
        this.tags = tags;
        this.curType = curType;

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (this.curType)
        {
            case FragmentCategories.TYPE_CATEGORY:
                return FragmentCategory.newInstance(cats.get(position).getUrl());
            case FragmentCategories.TYPE_TAG:
                return FragmentCategory.newInstance(tags.get(position).getUrl());
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        switch (curType)
        {
            default:
            case FragmentCategories.TYPE_CATEGORY:
                return cats.size();
            case FragmentCategories.TYPE_TAG:
                return tags.size();
        }
    }

    public void setCurType(int curType)
    {
        this.curType = curType;
    }
}