package ru.kuchanov.tproger.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.robospice.db.TagsCategories;

/**
 * Created by Юрий on 16.10.2015 16:43 18:06.
 * For TProger.
 */
public class RoboSpiceRequestTagsCategoriesOffline extends SpiceRequest<TagsCategories>
{
    public static final String LOG = RoboSpiceRequestTagsCategoriesOffline.class.getSimpleName();

    Context ctx;

    public RoboSpiceRequestTagsCategoriesOffline(Context ctx)
    {
        super(TagsCategories.class);

        this.ctx = ctx;
    }

    @Override
    public TagsCategories loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "loadDataFromNetwork called");
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        TagsCategories tagsCategories = new TagsCategories();

        ArrayList<Tag> tags = (ArrayList<Tag>) databaseHelper.getDaoTag().queryForAll();
        ArrayList<Category> categories = (ArrayList<Category>) databaseHelper.getDaoCategory().queryForAll();

        tagsCategories.setCategories(categories);
        tagsCategories.setTags(tags);

        return tagsCategories;
    }
}