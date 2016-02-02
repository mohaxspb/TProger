package ru.kuchanov.tproger.robospice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.ArticleTag;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.robospice.db.TagsCategories;

/**
 * Created by Юрий on 17.10.2015 16:57 20:31.
 * For TProger.
 */
public class MyRoboSpiceDatabaseHelper extends RoboSpiceDatabaseHelper
{
    public static final String LOG = MyRoboSpiceDatabaseHelper.class.getSimpleName();

    public final static String DB_NAME = "tpoger_db";
    public final static int DB_VERSION = 1;

    Context context;

    public MyRoboSpiceDatabaseHelper(Context context, String databaseName, int databaseVersion)
    {
        super(context, databaseName, databaseVersion);
        this.context = context;
    }

    /**
     * @return true if it is category, false is it's tag or null if cant find this url in DB
     */
    public static Boolean isCategoryOrTagOrDoNotExists(MyRoboSpiceDatabaseHelper h, String url)
    {
        if (Category.getCategoryByUrl(url, h) == null)
        {
            if (Tag.getTagByUrl(url, h) == null)
            {
                return null;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
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
            TableUtils.dropTable(connectionSource, Tag.class, true);
            TableUtils.dropTable(connectionSource, ArticleTag.class, true);
            TableUtils.dropTable(connectionSource, TagsCategories.class, true);

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
            TableUtils.createTableIfNotExists(connectionSource, Category.class);
            TableUtils.createTableIfNotExists(connectionSource, Article.class);
            TableUtils.createTableIfNotExists(connectionSource, ArticleCategory.class);
            TableUtils.createTableIfNotExists(connectionSource, Articles.class);
            TableUtils.createTableIfNotExists(connectionSource, Tag.class);
            TableUtils.createTableIfNotExists(connectionSource, ArticleTag.class);
            TableUtils.createTableIfNotExists(connectionSource, TagsCategories.class);
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
        //write initial info for tags and cats
        String[] catsTitle = context.getResources().getStringArray(R.array.categories_title);
        String[] catsUrl = context.getResources().getStringArray(R.array.categories_url);
        String[] tagsTitle = context.getResources().getStringArray(R.array.tags_title);
        String[] tagsUrl = context.getResources().getStringArray(R.array.tags_url);

        ArrayList<Category> cats = new ArrayList<>();
        for (int i = 0; i < catsTitle.length; i++)
        {
            String title = catsTitle[i];
            String url = catsUrl[i];
            Category c = new Category();
            c.setTitle(title);
            c.setUrl(url);
            cats.add(c);
        }
        ArrayList<Tag> tags = new ArrayList<>();
        for (int i = 0; i < tagsTitle.length; i++)
        {
            String title = tagsTitle[i];
            String url = tagsUrl[i];
            Tag tag = new Tag();
            tag.setTitle(title);
            tag.setUrl(url);
            tags.add(tag);
        }

        long startTime = System.currentTimeMillis();
        try
        {
            for (Category c : cats)
            {
                getDaoCategory().create(c);
            }
            for (Tag c : tags)
            {
                getDaoTag().create(c);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            long resultTime = System.currentTimeMillis() - startTime;
            Log.d(LOG, "resultTime is: " + resultTime);
        }
    }

    public void recreateDB()
    {
        Log.i(LOG, "recreateDB called");
        try
        {
            TableUtils.dropTable(connectionSource, Article.class, true);
            TableUtils.dropTable(connectionSource, Articles.class, true);

            TableUtils.dropTable(connectionSource, ArticleCategory.class, true);

            TableUtils.dropTable(connectionSource, Category.class, true);
            TableUtils.dropTable(connectionSource, Tag.class, true);
            TableUtils.dropTable(connectionSource, TagsCategories.class, true);

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

    public Dao<ArticleTag, Integer> getDaoArtTag()
    {
        Dao<ArticleTag, Integer> daoArtCat = null;
        try
        {
            daoArtCat = this.getDao(ArticleTag.class);
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