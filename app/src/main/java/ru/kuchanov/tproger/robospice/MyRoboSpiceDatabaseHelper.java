package ru.kuchanov.tproger.robospice;

import android.content.Context;

import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 17.10.2015 16:57.
 * For ExpListTest.
 */
public class MyRoboSpiceDatabaseHelper extends RoboSpiceDatabaseHelper
{
    public final static String DB_NAME = "tpoger_db";
    public final static int DB_VERSION = 1;

    public MyRoboSpiceDatabaseHelper(Context context, String databaseName, int databaseVersion)
    {
        super(context, databaseName, databaseVersion);
    }
}
