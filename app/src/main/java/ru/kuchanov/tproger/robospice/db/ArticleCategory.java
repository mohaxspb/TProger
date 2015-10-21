package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

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
    public static final String FIELD_IS_INITIAL_IN_CATEGORY = "isInitialInCategory";
    public static final String FIELD_IS_TOP_IN_CATEGORY = "isTopInCategory";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, columnName = FIELD_ARTICLE_ID)
    private int articleId;

    @DatabaseField(canBeNull = false, columnName = FIELD_CATEGORY_ID)
    private int categoryId;

    @DatabaseField(columnName = FIELD_NEXT_ARTICLE_ID)
    private int nextArticleId = -1;

    @DatabaseField(columnName = FIELD_PREVIOUS_ARTICLE_ID)
    private int previousArticleId = -1;

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = FIELD_IS_INITIAL_IN_CATEGORY)
    private boolean isInitialInCategory;

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = FIELD_IS_TOP_IN_CATEGORY)
    private boolean isTopInCategory;

    public static void writeArtsList(ArrayList<Article> arts, int categoryId, int page, MyRoboSpiceDatabaseHelper h)
    {
        //0. Get topArtCat
        //if it's null, so it's first time we write artCat
        //so simply write it
        //1. Get last artCat for page.
        //2. Set its nextArtId to first of given
        //3. Set prevArtId of first of given to id of artCat from 1)
        //4. loop through given list and set next/prev artId

        ArrayList<ArticleCategory> artCatListToWrite = new ArrayList<>();

        ArrayList<ArticleCategory> artCatListOfPreviousArts;

        try
        {
            Dao<ArticleCategory, Integer> daoArtCat = h.getDao(ArticleCategory.class);

            //0.
            ArticleCategory topArtCat = daoArtCat.queryBuilder().
                    where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                    and().eq(ArticleCategory.FIELD_IS_TOP_IN_CATEGORY, true).queryForFirst();
            if (topArtCat == null)
            {
                if (page != 1)
                {
                    throw new IllegalArgumentException("topArtCat is null and page isn't 1. Thats cant be");
                }
                //so it's first loading of art (from top)
                //so write artCat and set isTop to true for first row
                ArticleCategory artCat = new ArticleCategory();
                artCat.setArticleId(arts.get(0).getId());
                artCat.setCategoryId(categoryId);
                artCat.setTopInCategory(true);
                Article nextArt = (arts.size() > 1) ? arts.get(1) : null;
                int nextArtId = (nextArt == null) ? -1 : nextArt.getId();
                artCat.setNextArticleId(nextArtId);

                artCatListToWrite.add(artCat);

                for (int i = 1; i < arts.size(); i++)
                {
                    Article a = arts.get(i);

                    ArticleCategory artCatToWrite = new ArticleCategory();
                    artCatToWrite.setArticleId(a.getId());
                    artCatToWrite.setCategoryId(categoryId);
                    Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                    int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                    artCat.setNextArticleId(nextArtIdInLoop);
                    artCat.setPreviousArticleId(arts.get(i - 1).getId());

                    artCatListToWrite.add(artCat);
                }

                //set bottom art if size of arts<DefaultNumOnPage;
                if (arts.size() < Const.NUM_OF_ARTS_ON_PAGE)
                {
                    artCatListToWrite.get(artCatListToWrite.size() - 1).setInitialInCategory(true);
                }

                for (ArticleCategory artCatToWrite : artCatListToWrite)
                {
                    daoArtCat.create(artCatToWrite);
                }
                return;
            }

            //1.
            artCatListOfPreviousArts = ArticleCategory.getArtCatListFromGivenArticleId(topArtCat.getArticleId(), categoryId, h, true);
            //check if we have page==1
            //if so we must check if we have equals artId in given list and topArtCat
            //if true we must
            //1) set isTop for old artCat to false
            //2) set isTop for given list.get(0) to true
            //3) update prevArtId for oldTopArtCat
            //4) set nextArtId for last art in given list (last, that dont have equal id with oldTopArtCat

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }


        //set next/prev arts url to artcile obj for middle arts


    }

    /**
     * @return first 10 artCat from given article id including given or not
     */
    public static ArrayList<ArticleCategory> getArtCatListFromGivenArticleId(int firstArticleId, int categoryId, MyRoboSpiceDatabaseHelper h, boolean includeGiven)
    {
        ArrayList<ArticleCategory> list = new ArrayList<>();

        try
        {
            Dao<Article, Integer> daoArticle = h.getDao(Article.class);
            Dao<ArticleCategory, Integer> daoArticleCategory = h.getDao(ArticleCategory.class);

            ArticleCategory artCatInitial = daoArticleCategory.queryBuilder()
                    .where().eq(ArticleCategory.FIELD_ARTICLE_ID, firstArticleId)
                    .and().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).queryForFirst();
            if (artCatInitial == null)
            {
                throw new NullPointerException("No ArticleCategory for given Article id. WTF?!");
            }

            ArticleCategory curArtCat = artCatInitial;

            for (int i = 0; i < Const.NUM_OF_ARTS_ON_PAGE; i++)
            {
                if (i == 0)
                {
                    if (includeGiven)
                    {
                        list.add(curArtCat);
                        continue;
                    }
                }
                Article nextArt = daoArticle.queryBuilder().where().eq(Article.FIELD_ID, curArtCat.getArticleId()).queryForFirst();
                ArticleCategory nextArtCat = daoArticleCategory.queryBuilder().
                        where().eq(ArticleCategory.FIELD_ARTICLE_ID, nextArt.getId()).
                        and().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).queryForFirst();
                if (nextArtCat == null)
                {
                    break;
                }
                else
                {
                    list.add(nextArtCat);
                    curArtCat = nextArtCat;
                }
            }
        }
        catch (NullPointerException | SQLException e)
        {
            e.printStackTrace();
        }

        return list;
    }

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

    public boolean getInitialInCategory()
    {
        return isInitialInCategory;
    }

    public void setInitialInCategory(boolean initialInCategory)
    {
        this.isInitialInCategory = initialInCategory;
    }

    public boolean getTopInCategory()
    {
        return isTopInCategory;
    }

    public void setTopInCategory(boolean topInCategory)
    {
        this.isTopInCategory = topInCategory;
    }
}
