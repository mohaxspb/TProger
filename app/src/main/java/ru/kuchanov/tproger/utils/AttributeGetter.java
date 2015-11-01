package ru.kuchanov.tproger.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

/**
 * Created by Юрий on 28.09.2015 0:54.
 * For ExpListTest.
 */
public class AttributeGetter
{
    public static int getColor(Context ctx, int addressInRClass)
    {
        int colorId;
        int[] attrs = new int[]{addressInRClass};
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        colorId = ta.getColor(0, Color.GRAY);
        ta.recycle();

        return colorId;
    }

    public static int getHeight(Context ctx, int addressInRClass)
    {
        int height;
        int[] attrs = new int[]{addressInRClass};
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        height = ta.getInt(0, -1);
        ta.recycle();

        return height;
    }

    public static int getDimentionPixelSize(Context ctx, int addressInRClass)
    {
        int value;
        int[] attrs = new int[]{addressInRClass};
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        value = ta.getDimensionPixelSize(0, -1);
        ta.recycle();

        return value;
    }
}
