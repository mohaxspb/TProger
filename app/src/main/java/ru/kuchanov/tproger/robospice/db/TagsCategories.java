package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 16.10.2015 18:45 17:10.
 * For TProger.
 */
@DatabaseTable(tableName = "tags_categories")
public class TagsCategories
{
    public static final String LOG = TagsCategories.class.getSimpleName();

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @ForeignCollectionField(eager = false)
    private Collection<Tag> tags;

    @ForeignCollectionField(eager = false)
    private Collection<Category> categories;



    public static void deleteAllEntries(MyRoboSpiceDatabaseHelper h)
    {
        try
        {
            ArrayList<TagsCategories> articles = (ArrayList<TagsCategories>) h.getDao(TagsCategories.class).queryForAll();
            h.getDao(TagsCategories.class).delete(articles);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Collection<Tag> getTags()
    {
        return tags;
    }

    public void setTags(Collection<Tag> result)
    {
        this.tags = result;
    }

    public Collection<Category> getCategories()
    {
        return categories;
    }

    public void setCategories(Collection<Category> result)
    {
        this.categories = result;
    }
}