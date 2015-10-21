package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;

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
//    public static final String FIELD_INITIAL_ART_URL = "initialArtUrl";
//    public static final String FIELD_TOP_ART_URL = "topArtUrl";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, columnName = FIELD_URL)
    private String url;

    @DatabaseField(canBeNull = false, columnName = FIELD_TITLE)
    private String title;

//    @DatabaseField(columnName = FIELD_INITIAL_ART_URL)
//    private String initialArtUrl;
//
//    @DatabaseField(columnName = FIELD_TOP_ART_URL)
//    private String topArtUrl;

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

//    public String getInitialArtUrl()
//    {
//        return initialArtUrl;
//    }
//
//    public void setInitialArtUrl(String initialArtUrl)
//    {
//        this.initialArtUrl = initialArtUrl;
//    }
//
//    public String getTopArtUrl()
//    {
//        return topArtUrl;
//    }
//
//    public void setTopArtUrl(String topArtUrl)
//    {
//        this.topArtUrl = topArtUrl;
//    }

    /**
     *
     * @return id of category by url or -1 id can't find or on SQLException
     */
    public static int getCategoryIdByUrl(String url, MyRoboSpiceDatabaseHelper h)
    {
        int id=-1;

        Category c= null;
        try
        {
            c = h.getDao(Category.class).queryBuilder().where().eq(Category.FIELD_URL, url).queryForFirst();
            if (c!=null)
            {
                id=c.getId();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return id;
    }
}
