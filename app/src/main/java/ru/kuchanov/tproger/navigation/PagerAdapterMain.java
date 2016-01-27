package ru.kuchanov.tproger.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.kuchanov.tproger.fragment.FragmentCategories;
import ru.kuchanov.tproger.fragment.FragmentCategory;
import ru.kuchanov.tproger.fragment.FragmentTab2;
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
                FragmentCategory tab1 = FragmentCategory.newInstance("");
                return tab1;
            case 1:
//                FragmentTab2 tab2 = new FragmentTab2();
//                return tab2;
                //TODO create from prefs with desired type
                FragmentCategories fragmentCategories = FragmentCategories.newInstance(FragmentCategories.TYPE_CATEGORY);
                return fragmentCategories;
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