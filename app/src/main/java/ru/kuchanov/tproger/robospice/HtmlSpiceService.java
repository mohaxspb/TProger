package ru.kuchanov.tproger.robospice;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.ormlite.InDatabaseObjectPersisterFactory;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;

/**
 * Created by Юрий on 16.10.2015 16:31.
 * For ExpListTest.
 */
public class HtmlSpiceService extends SpiceService
{
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException
    {
        CacheManager cacheManager = new CacheManager();

        List<Class<?>> classCollection = new ArrayList<Class<?>>();

        // add persisted classes to class collection
        classCollection.add(Article.class);
        classCollection.add(Articles.class);
        classCollection.add(ArticleCategory.class);
        classCollection.add(Category.class);

        // init
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(application, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        InDatabaseObjectPersisterFactory inDatabaseObjectPersisterFactory = new InDatabaseObjectPersisterFactory(application, databaseHelper, classCollection);
        cacheManager.addPersister(inDatabaseObjectPersisterFactory);

        return cacheManager;
    }

    @Override
    public int getThreadCount()
    {
        return 3;
    }
}