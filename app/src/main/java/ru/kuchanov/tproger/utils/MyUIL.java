package ru.kuchanov.tproger.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

import java.io.File;

import ru.kuchanov.tproger.R;

/**
 * Created by Юрий on 31.10.2015 0:03.
 * For ExpListTest.
 */
public class MyUIL
{
    public static ImageLoader get(Context act)
    {
        //UniversalImageLoader
        File cacheDir = new File(Environment.getExternalStorageDirectory(), "Odnako/Cache");

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(10))
//                .showImageOnLoading(R.drawable.ic_refresh_grey600_48dp)
                .showImageForEmptyUri(R.drawable.ic_crop_original_grey600_48dp)
                .showImageOnFail(R.drawable.ic_crop_original_grey600_48dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        //switch to true if you want logging
        L.writeLogs(false);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(act)
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();

        if (!imageLoader.isInited())
        {
            imageLoader.init(config);
        }

        return imageLoader;

    }

    public static ImageLoader getDefault(Context act)
    {
        //UniversalImageLoader
        File cacheDir = new File(Environment.getExternalStorageDirectory(), "Odnako/Cache");

        //switch to true if you want logging
        L.writeLogs(false);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(act)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();

        if (!imageLoader.isInited())
        {
            imageLoader.init(config);
        }

        return imageLoader;

    }
}