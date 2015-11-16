package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.Date;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 20.10.2015 0:48.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "article_category")
public class Category
{
    public static final String LOG = Category.class.getSimpleName();
    public static final String FIELD_URL = "url";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_REFRESHED = "refreshed";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, columnName = FIELD_URL)
    private String url;

    @DatabaseField(canBeNull = false, columnName = FIELD_TITLE)
    private String title;

    @DatabaseField(canBeNull = false, columnName = FIELD_REFRESHED)
    private Date refreshed = new Date(0);

    /**
     * @return id of category by url or -1 id can't find or on SQLException
     */
    public static int getCategoryIdByUrl(String url, MyRoboSpiceDatabaseHelper h)
    {
        int id = -1;

        Category c = null;
        try
        {
            c = h.getDao(Category.class).queryBuilder().where().eq(Category.FIELD_URL, url).queryForFirst();
            if (c != null)
            {
                id = c.getId();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return id;
    }

    public static Category getCategoryByUrl(String url, MyRoboSpiceDatabaseHelper h)
    {
        Category c = null;
        try
        {
            c = h.getDaoCategory().queryBuilder().where().eq(Category.FIELD_URL, url).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return c;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Date getRefreshed()
    {
        return refreshed;
    }

    public void setRefreshed(Date refreshed)
    {
        this.refreshed = refreshed;
    }
}