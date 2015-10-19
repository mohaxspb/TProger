package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Юрий on 20.10.2015 0:48.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "article_category")
public class ArticleCategory
{
    public static final String LOG = ArticleCategory.class.getSimpleName();
    public static final String FIELD_ARTICLE_ID = "articleId";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_NEXT_ARTICLE_ID = "nextArticleId";
    public static final String FIELD_PREVIOUS_ARTICLE_ID = "previousArticleId";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, columnName = FIELD_ARTICLE_ID)
    private int articleId;

    @DatabaseField(canBeNull = false, columnName = FIELD_CATEGORY_ID)
    private int categoryId;

    @DatabaseField(columnName = FIELD_NEXT_ARTICLE_ID)
    private int nextArticleId;

    @DatabaseField(columnName = FIELD_PREVIOUS_ARTICLE_ID)
    private int previousArticleId;

    public int getArticleId()
    {
        return articleId;
    }

    public void setArticleId(int articleId)
    {
        this.articleId = articleId;
    }

    public int getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }

    public int getNextArticleId()
    {
        return nextArticleId;
    }

    public void setNextArticleId(int nextArticleId)
    {
        this.nextArticleId = nextArticleId;
    }

    public int getPreviousArticleId()
    {
        return previousArticleId;
    }

    public void setPreviousArticleId(int previousArticleId)
    {
        this.previousArticleId = previousArticleId;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
