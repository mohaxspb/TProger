package ru.kuchanov.tproger.utils;

/*
 28.03.2015
SpacesItemDecoration.java
Created by Kuchanov Yuri,
mohax.spb@gmail.com
 */

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration
{
    private int space;

    public SpacesItemDecoration(int space)
    {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        outRect.bottom = space;
    }
}