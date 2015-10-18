package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Юрий on 16.10.2015 16:47.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "article")
public class Article
{
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField(canBeNull = false)
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

    @DatabaseField(foreign = true)
    private Articles result;

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

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
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
}