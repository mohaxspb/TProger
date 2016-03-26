package ru.kuchanov.tproger.utils;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.support.annotation.AttrRes;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Юрий on 13.12.2015 20:08 18:46.
 * For TProger.
 */
public class MyColorFilter
{
    public static void applyColor(ImageView imageView, int color)
    {
        imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static void applyColorFromAttr(Context ctx, ImageView imageView, @AttrRes int attr)
    {
        int color = AttributeGetter.getColor(ctx, attr);
        imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static void applyColorFromAttr(Context ctx, View view, int attr)
    {
        int color = AttributeGetter.getColor(ctx, attr);
        view.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static void applyGreyFilter(ImageView imageView)
    {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }
}
