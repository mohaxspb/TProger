package ru.kuchanov.tproger.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.kuchanov.tproger.fragment.FragmentCategories;
import ru.kuchanov.tproger.fragment.FragmentCategory;
import ru.kuchanov.tproger.fragment.FragmentTab3;

/**
 * Created by Юрий on 17.09.2015 18:06.
 * For ExpListTest.
 */
public class PagerAdapterMain extends FragmentStatePagerAdapter
{
    int mNumOfTabs;

    public PagerAdapterMain(FragmentManager fm, int NumOfTabs)
    {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position)
    {

        switch (position)
        {
            case 0:
//                FragmentCategory tab1 = FragmentCategory.newInstance("");
//                return tab1;
                return FragmentCategory.newInstance("");
            case 1:
//                FragmentCategories fragmentCategories = FragmentCategories.newInstance();
//                return fragmentCategories;
                return FragmentCategories.newInstance();
            case 2:
                FragmentTab3 tab3 = new FragmentTab3();
                return tab3;
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