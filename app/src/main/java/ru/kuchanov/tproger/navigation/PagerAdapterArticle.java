package ru.kuchanov.tproger.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import ru.kuchanov.tproger.fragment.FragmentArticle;
import ru.kuchanov.tproger.robospice.db.Article;

/**
 * Created by Юрий on 17.09.2015 18:06 19:33.
 * For TProger.
 */
public class PagerAdapterArticle extends FragmentStatePagerAdapter
{
    private ArrayList<Article> artsList;

    public PagerAdapterArticle(FragmentManager fm, ArrayList<Article> artsList)
    {
        super(fm);
        this.artsList = artsList;
//        this.artsList = new ArrayList<>(artsList);
    }

    @Override
    public Fragment getItem(int position)
    {
        return FragmentArticle.newInstance(artsList.get(position));
    }

    @Override
    public int getCount()
    {
        return artsList.size();
    }
}