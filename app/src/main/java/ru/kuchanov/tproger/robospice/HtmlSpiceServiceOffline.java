package ru.kuchanov.tproger.robospice;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.ormlite.InDatabaseObjectPersisterFactory;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.TagsCategories;

/**
 * Created by Юрий on 16.10.2015 16:31.
 * For ExpListTest.
 */
public class HtmlSpiceServiceOffline extends SpiceService
{
    @Override
    protected NetworkStateChecker getNetworkStateChecker()
    {
        return new NetworkStateChecker()
        {
            @Override
            public boolean isNetworkAvailable(Context context)
            {
                return true;
            }

            @Override
            public void checkPermissions(Context context)
            {
                //do noting
            }
        };
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException
    {
        CacheManager cacheManager = new CacheManager();

        List<Class<?>> classCollection = new ArrayList<>();

        // add persisted classes to class collection
        classCollection.add(Article.class);
        classCollection.add(Articles.class);

        classCollection.add(TagsCategories.class);

        // init
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(application, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        InDatabaseObjectPersisterFactory inDatabaseObjectPersisterFactory = new InDatabaseObjectPersisterFactory(application, databaseHelper, classCollection);
        cacheManager.addPersister(inDatabaseObjectPersisterFactory);

        return cacheManager;
    }

    @Override
    public int getThreadCount()
    {
        return this.getResources().getInteger(R.integer.roboSpiceThreadCount);
    }
}