package ru.kuchanov.tproger.test;

import android.content.Context;
import android.graphics.Canvas;
import android.support.design.widget.TabLayout;
import android.util.Log;

/**
 * Created by Юрий on 26.03.2016 1:01.
 * For TProger.
 */
public class MyTabLayout extends TabLayout
{
    private static final String LOG = MyTabLayout.class.getSimpleName();

    public MyTabLayout(Context context)
    {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Log.i(LOG, "onDraw called");
    }
}
