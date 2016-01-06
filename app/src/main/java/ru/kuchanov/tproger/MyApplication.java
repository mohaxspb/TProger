package ru.kuchanov.tproger;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by Юрий on 06.01.2016 15:14.
 * For ExpListTest.
 */
public class MyApplication extends Application
{
    static final String LOG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate()
    {
        //fresco
        Fresco.initialize(this);

        super.onCreate();
    }
}