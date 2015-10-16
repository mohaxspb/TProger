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
public class RequestList extends SpiceRequest<ArrayListModel>
//public class RequestList extends SpiceRequest<ArrayList<Model>>
{
    public static final String LOG = RequestList.class.getSimpleName();

    Context ctx;

    public RequestList(Context ctx)
    {
        super(ArrayListModel.class);

        this.ctx = ctx;
    }

    @Override
    public ArrayListModel loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "ArrayListModel loadDataFromNetwork() called");
        RoboSpiceDatabaseHelper databaseHelper = new RoboSpiceDatabaseHelper(ctx, "sample_database.db", 1);

        ArrayList<Model> list = (ArrayList<Model>) databaseHelper.getDao(Model.class).queryForAll();

        Log.i(LOG, "list.size() is: " + list.size());
        for (int i = 0; i < list.size(); i++)
        {
            Log.i(LOG, list.get(i).toString());
        }

//        ArrayListModel list = (ArrayListModel) databaseHelper.getDao(Model.class).queryForAll();
        if (list != null)
        {
            Log.i(LOG, "list.size() is: " + list.size());
            if (list.size() == 0)
            {
                for (int i = 0; i < 3; i++)
                {
                    Model model = new Model();
//                    model.setId(i);
                    list.add(model);
                }
            }
        }
        else
        {
            Log.i(LOG, "list is null");
        }

        ArrayListModel arrayListModel = databaseHelper.getDao(ArrayListModel.class).queryBuilder().queryForFirst();
        if (arrayListModel != null)
        {
            arrayListModel.setResult(list);
        }
        else
        {
            arrayListModel = new ArrayListModel();
            arrayListModel.setResult(list);
        }

//        for (Model m : arrayListModel.getResult())
//        {
//            m.setResult(arrayListModel);
//        }

        ArrayList<ArrayListModel> listModel = (ArrayList<ArrayListModel>) databaseHelper.getDao(ArrayListModel.class).queryForAll();
        Log.i(LOG, "listModel.size() is: " + listModel.size());

//        Model model=new Model();

        return arrayListModel;

//        return list;
    }
}