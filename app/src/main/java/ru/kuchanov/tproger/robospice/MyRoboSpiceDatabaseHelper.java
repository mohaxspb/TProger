package ru.kuchanov.tproger.robospice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;

import java.sql.SQLException;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;

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
        try
        {
            TableUtils.dropTable(connectionSource, Category.class, true);
            TableUtils.dropTable(connectionSource, Article.class, true);
            TableUtils.dropTable(connectionSource, ArticleCategory.class, true);
            TableUtils.dropTable(connectionSource, Articles.class, true);

            this.onCreate(database, connectionSource);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
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

            TableUtils.createTableIfNotExists(connectionSource, Articles.class);

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

    public void recreateDB()
    {
        Log.i(LOG, "recreateDB called");
        try
        {
            TableUtils.dropTable(connectionSource, Category.class, true);
            TableUtils.dropTable(connectionSource, Article.class, true);
            TableUtils.dropTable(connectionSource, ArticleCategory.class, true);
            TableUtils.dropTable(connectionSource, Articles.class, true);

            this.onCreate(this.getWritableDatabase(), connectionSource);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Dao<ArticleCategory, Integer> getDaoArtCat()
    {
        Dao<ArticleCategory, Integer> daoArtCat = null;
        try
        {
            daoArtCat = this.getDao(ArticleCategory.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoArtCat;
    }

    public Dao<Article, Integer> getDaoArticle()
    {
        Dao<Article, Integer> daoArticle = null;
        try
        {
            daoArticle = this.getDao(Article.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoArticle;
    }

    public Dao<Category, Integer> getDaoCategory()
    {
        Dao<Category, Integer> daoCategory = null;
        try
        {
            daoCategory = this.getDao(Category.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoCategory;
    }

    public Dao<Tag, Integer> getDaoTag()
    {
        Dao<Tag, Integer> daoTag = null;
        try
        {
            daoTag = this.getDao(Tag.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoTag;
    }
}