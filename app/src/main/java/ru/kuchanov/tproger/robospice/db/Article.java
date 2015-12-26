package ru.kuchanov.tproger.robospice.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 16.10.2015 16:47.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "article")
public class Article implements Parcelable
{
    public static final String LOG = Article.class.getSimpleName();

    public static final String KEY_ARTICLE = "KEY_ARTICLE";
    public static final String KEY_ARTICLES_LIST = "KEY_ARTICLES_LIST";
    public static final String KEY_ARTICLES_LIST_WITH_IMAGE = "KEY_ARTICLES_LIST_WITH_IMAGE";

    public static final String FIELD_URL = "url";
    public static final String FIELD_ID = "id";

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>()
    {

        @Override
        public Article createFromParcel(Parcel source)
        {
            return new Article(source);
        }

        @Override
        public Article[] newArray(int size)
        {
            return new Article[size];
        }
    };
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = FIELD_ID)
    private int id;
    @DatabaseField(canBeNull = false, columnName = FIELD_URL)
    private String url;
    @DatabaseField(canBeNull = false)
    private String title;
    @DatabaseField(dataType = DataType.DATE)
    private Date pubDate;
    @DatabaseField
    private String tagMainTitle;
    @DatabaseField
    private String tagMainUrl;
    @DatabaseField
    private String imageUrl;
    @DatabaseField
    private int imageWidth;
    @DatabaseField
    private int imageHeight;
    @DatabaseField
    private String preview;
    @DatabaseField
    private String text;
    @DatabaseField
    private boolean isRead;
    @DatabaseField(foreign = true)
    private Articles result;

    private Article(Parcel in)
    {
        this.id = in.readInt();
        this.url = in.readString();
        this.title = in.readString();

        this.pubDate = new Date(in.readLong());

        this.tagMainTitle = in.readString();
        this.tagMainUrl = in.readString();

        this.imageUrl = in.readString();
        this.imageWidth = in.readInt();
        this.imageHeight = in.readInt();

        this.preview = in.readString();
        this.text = in.readString();
        this.isRead = in.readByte() != 0; //myBoolean == true if byte != 0
    }

    /**
     * empty constructor
     */
    public Article()
    {

    }

    public static Article getArticleByUrl(MyRoboSpiceDatabaseHelper h, String url)
    {
        Article a = null;
        try
        {
//            a = h.getDao(Article.class).queryForEq(FIELD_URL, url).get(0);
            a = h.getDao(Article.class).queryBuilder().where().eq(FIELD_URL, url).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return a;
    }

    /**
     * @return article id for url or -1 on error of if can't find
     */
    public static int getArticleIdByUrl(MyRoboSpiceDatabaseHelper h, String url)
    {
        int id = -1;
        try
        {
            Article a = h.getDao(Article.class).queryBuilder().where().eq(FIELD_URL, url).queryForFirst();
            if (a == null)
            {
                throw new NullPointerException("Article is null while searhing for it by url in GetIdByUrl. WTF&?!");
            }
            id = h.getDao(Article.class).queryBuilder().where().eq(FIELD_URL, url).queryForFirst().getId();
        }
        catch (NullPointerException | SQLException e)
        {
            e.printStackTrace();
        }
        return id;
    }

    public static ArrayList<Article> writeArtsList(ArrayList<Article> dataToWrite, MyRoboSpiceDatabaseHelper h)
    {
        ArrayList<Article> createdData = new ArrayList<>();

        for (Article a : dataToWrite)
        {
//            Log.i(LOG, "a.getId(): " + a.getId());

            if (a.getId() != 0)
            {
                //already in DB
            }
            else
            {
                try
                {
                    h.getDao(Article.class).create(a);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            createdData.add(a);
        }

        return createdData;
    }

    public static ArrayList<Article> getArticleListFromArtCatList(ArrayList<ArticleCategory> artCatList, MyRoboSpiceDatabaseHelper h)
    {
        ArrayList<Article> artsList = new ArrayList<>();

        try
        {
            Dao<Article, Integer> daoArt = h.getDao(Article.class);

//            Log.i(LOG, "artCatList.size(): " + artCatList.size());

            for (ArticleCategory artCat : artCatList)
            {
                Article a = daoArt.queryBuilder().where().eq(Article.FIELD_ID, artCat.getArticleId()).queryForFirst();
                artsList.add(a);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return artsList;
    }

    public static void printInLog(Article a)
    {
        Log.i(LOG, "!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.i(LOG, String.valueOf(a.getId()));
        Log.i(LOG, String.valueOf(a.getTitle()));
        Log.i(LOG, String.valueOf(a.getUrl()));
        Log.i(LOG, String.valueOf(a.getPubDate()));
        Log.i(LOG, String.valueOf(a.getImageUrl()));
        Log.i(LOG, String.valueOf(a.getImageHeight()));
        Log.i(LOG, String.valueOf(a.getImageWidth()));
        Log.i(LOG, String.valueOf(a.getTagMainTitle()));
        Log.i(LOG, String.valueOf(a.getTagMainUrl()));
        Log.i(LOG, String.valueOf(a.getPreview()));
        Log.i(LOG, a.getText());
        Log.i(LOG, "!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public static void printListInLog(ArrayList<Article> list)
    {
        for (Article a : list)
        {
            Article.printInLog(a);
        }
    }

    public static Article getArticleById(MyRoboSpiceDatabaseHelper h, int articleId)
    {
        Article a = null;

        try
        {
            a = h.getDaoArticle().queryBuilder().where().eq(Article.FIELD_ID, articleId).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return a;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return this.getUrl() + ", id = " + id;
    }

    public Articles getResult()
    {
        return result;
    }

    public void setResult(Articles result)
    {
        this.result = result;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public Date getPubDate()
    {
        return pubDate;
    }

    public void setPubDate(Date pubDate)
    {
        this.pubDate = pubDate;
    }

    public String getTagMainTitle()
    {
        return tagMainTitle;
    }

    public void setTagMainTitle(String tagMainTitle)
    {
        this.tagMainTitle = tagMainTitle;
    }

    public int getImageWidth()
    {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth)
    {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight()
    {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight)
    {
        this.imageHeight = imageHeight;
    }

    public String getPreview()
    {
        return preview;
    }

    //////PARCEL implementation

//     id; url; title; pubDate; tagMainTitle; tagMainUrl;imageUrl; imageWidth;imageHeight;preview;text;isRead

    public void setPreview(String preview)
    {
        this.preview = preview;
    }

    public String getTagMainUrl()
    {
        return tagMainUrl;
    }

    public void setTagMainUrl(String tagMainUrl)
    {
        this.tagMainUrl = tagMainUrl;
    }

    public String getText()
    {
        return text;
    }
//    Parcel implementation/////////////////////////////

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(title);

        dest.writeLong(pubDate.getTime());

        dest.writeString(tagMainTitle);
        dest.writeString(tagMainUrl);

        dest.writeString(imageUrl);
        dest.writeInt(imageWidth);
        dest.writeInt(imageHeight);

        dest.writeString(preview);
        dest.writeString(text);
        dest.writeByte((byte) (isRead ? 1 : 0)); //if myBoolean == true, byte == 1
    }

    public boolean isRead()
    {
        return isRead;
    }

    public void setIsRead(boolean isRead)
    {
        this.isRead = isRead;
    }

    public static class PubDateComparator implements Comparator<Article>
    {
        @Override
        public int compare(Article o1, Article o2)
        {
            return o2.getPubDate().compareTo(o1.getPubDate());
        }
    }
}