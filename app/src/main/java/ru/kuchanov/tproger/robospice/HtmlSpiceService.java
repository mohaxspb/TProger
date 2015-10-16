package ru.kuchanov.tproger.robospice;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.ormlite.InDatabaseObjectPersisterFactory;
import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

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

        List< Class< ? >> classCollection = new ArrayList< Class< ? >>();

        // add persisted classes to class collection
        classCollection.add( Model.class );
        classCollection.add( ArrayListModel.class );

        // init
        RoboSpiceDatabaseHelper databaseHelper = new RoboSpiceDatabaseHelper( application, "sample_database.db", 1 );
        InDatabaseObjectPersisterFactory inDatabaseObjectPersisterFactory = new InDatabaseObjectPersisterFactory( application, databaseHelper, classCollection );
        cacheManager.addPersister( inDatabaseObjectPersisterFactory );

        return cacheManager;
    }

    @Override
    public int getThreadCount()
    {
        return 3;
    }
}