package ru.kuchanov.tproger.robospice;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;
import com.octo.android.robospice.request.SpiceRequest;

import java.util.ArrayList;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestModel extends SpiceRequest<Model>
{
    public static final String LOG=RoboSpiceRequestModel.class.getSimpleName();

    Context ctx;

    public RoboSpiceRequestModel(Context ctx)
    {
        super(Model.class);

        this.ctx=ctx;
    }

    @Override
    public Model loadDataFromNetwork() throws Exception
    {
        RoboSpiceDatabaseHelper databaseHelper = new RoboSpiceDatabaseHelper(ctx, "sample_database.db", 1 );

        ArrayList<Model> list = (ArrayList<Model>) databaseHelper.getDao(Model.class).queryForAll();
        if (list!=null)
        {
            Log.i(LOG, "list.size() is: "+list.size());
        }
        else
        {
            Log.i(LOG, "list is null");
        }

        Model model=new Model();

        return model;
    }
}