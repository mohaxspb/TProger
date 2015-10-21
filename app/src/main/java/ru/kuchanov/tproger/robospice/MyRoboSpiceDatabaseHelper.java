package ru.kuchanov.tproger.robospice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;

import java.sql.SQLException;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Category;

/**
 * Created by Юрий on 17.10.2015 16:57.
 * For ExpListTest.
 */
public class MyRoboSpiceDatabaseHelper extends RoboSpiceDatabaseHelper
{
    public static final String LOG = MyRoboSpiceDatabaseHelper.class.getSimpleName();

    public final static String DB_NAME = "tpoger_db";
    public final static int DB_VERSION = 1;

    public MyRoboSpiceDatabaseHelper(Context context, String databaseName, int databaseVersion)
    {
        super(context, databaseName, databaseVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        // override if needed
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
    {
        try
        {
            Log.i(LOG, "onCreate");
            //writeArtsList category table
            TableUtils.createTableIfNotExists(connectionSource, Category.class);
            //writeArtsList article table
            TableUtils.createTableIfNotExists(connectionSource, Article.class);
            //writeArtsList artCatTable table
            TableUtils.createTableIfNotExists(connectionSource, ArticleCategory.class);

            Log.i(LOG, "all tables have been created");

            //fill with initial data
            fillTables();
        }
        catch (SQLException e)
        {
            Log.e(LOG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    private void fillTables()
    {
        Category c = new Category();
        c.setTitle("Главная");
        c.setUrl("");
        try
        {
            this.getDao(Category.class).create(c);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}