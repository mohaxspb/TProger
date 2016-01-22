package ru.kuchanov.tproger.robospice.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.Date;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 20.10.2015 0:48.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "tag")
public class Tag implements Parcelable
{
    public static final String LOG = Tag.class.getSimpleName();
    public static final String FIELD_URL = "url";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_REFRESHED = "refreshed";
    //    Parcel implementation/////////////////////////////
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>()
    {

        @Override
        public Tag createFromParcel(Parcel source)
        {
            return new Tag(source);
        }

        @Override
        public Tag[] newArray(int size)
        {
            return new Tag[size];
        }
    };
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false, columnName = FIELD_URL)
    private String url;
    @DatabaseField(canBeNull = false, columnName = FIELD_TITLE)
    private String title;
    @DatabaseField(canBeNull = false, columnName = FIELD_REFRESHED)
    private Date refreshed = new Date(0);

    //    Parcel implementation/////////////////////////////
    private Tag(Parcel in)
    {
        this.id = in.readInt();
        this.url = in.readString();
        this.title = in.readString();

        this.refreshed = new Date(in.readLong());
    }

    /**
     * empty constructor
     */
    public Tag()
    {

    }

    /**
     * @return id of category by url or -1 id can't find or on SQLException
     */
    public static int getCategoryIdByUrl(String url, MyRoboSpiceDatabaseHelper h)
    {
        int id = -1;

        Tag c;
        try
        {
            c = h.getDao(Tag.class).queryBuilder().where().eq(Tag.FIELD_URL, url).queryForFirst();
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

    public static Tag getCategoryByUrl(String url, MyRoboSpiceDatabaseHelper h)
    {
        Tag c = null;
        try
        {
            c = h.getDaoTag().queryBuilder().where().eq(Tag.FIELD_URL, url).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return c;
    }

    /**
     * @return true if lastRefreshedDate was more than refreshPeriod mills ago from now;
     */
    public static boolean refreshDateExpired(Tag category, Context ctx)
    {
        long currentTimeInMills = System.currentTimeMillis();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        int refreshPeriodInHours = ctx.getResources().getInteger(R.integer.refresh_period_hours);
        long millsInRefreshPeriod = refreshPeriodInHours * 60 * 60 * 1000;

        long millsFromLastRefresh = currentTimeInMills - category.getRefreshed().getTime();

        return millsFromLastRefresh > millsInRefreshPeriod;
    }

    /**
     * For test purposes. Calling it we can test reaction on loading from web and getting new arts;
     * Method searches through artCatTable for isTop for given category, deletes it and updates
     * next artCat to be isTop;
     */
    public static void deleteFirstInCatAndUpdateSecond(MyRoboSpiceDatabaseHelper h, String categoryUrl)
    {
        Tag category = Tag.getCategoryByUrl(categoryUrl, h);
        ArticleCategory topArtCat = ArticleCategory.getTopArtCat(category.getId(), h);
        ArticleCategory secondArtCat = ArticleCategory.getNextArtCat(h, topArtCat);

        secondArtCat.setTopInCategory(true);
        secondArtCat.setPreviousArticleId(-1);

        try
        {
            h.getDaoArtCat().delete(topArtCat);
            h.getDaoArtCat().createOrUpdate(secondArtCat);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
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

    //    Parcel implementation/////////////////////////////
    @Override
    public int describeContents()
    {
        return 0;
    }

    //    Parcel implementation/////////////////////////////
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(title);

        dest.writeLong(refreshed.getTime());
    }
}