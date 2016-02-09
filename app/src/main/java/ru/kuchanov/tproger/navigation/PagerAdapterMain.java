package ru.kuchanov.tproger.navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.fragment.FragmentCategoriesAndTags;
import ru.kuchanov.tproger.fragment.FragmentCategory;
import ru.kuchanov.tproger.fragment.FragmentTab3;

/**
 * Created by Юрий on 17.09.2015 18:06 19:11.
 * For TProger.
 */
public class PagerAdapterMain extends FragmentStatePagerAdapter
{
    private static final String LOG = PagerAdapterMain.class.getSimpleName();

    private Context ctx;
    private int mNumOfTabs;
    private SharedPreferences pref;

    public PagerAdapterMain(FragmentManager fm, int NumOfTabs, Context ctx)
    {
        super(fm);
        this.ctx = ctx;
        this.mNumOfTabs = NumOfTabs;

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return FragmentCategory.newInstance("");
            case 1:
                String key = ctx.getString(R.string.pref_design_key_category_in_cats_or_tags);
                boolean showCategories = pref.getBoolean(key, true);
                Log.d(LOG, "showCategories: " + showCategories);
                int dataType = showCategories ? FragmentCategoriesAndTags.TYPE_CATEGORY : FragmentCategoriesAndTags.TYPE_TAG;
                return FragmentCategoriesAndTags.newInstance(dataType);
            case 2:
                //TODO add frag with saved arts
                return new FragmentTab3();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return mNumOfTabs;
    }
}