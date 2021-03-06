package ru.kuchanov.tproger.robospice.db;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 20.10.2015 0:48 3:03.
 * For TProger.
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

    @DatabaseField(columnName = FIELD_ARTICLE_ID)
    private int articleId;

    @DatabaseField(columnName = FIELD_CATEGORY_ID)
    private int categoryId;

    /**
     * next means that if this is first in list sorted by pubDate and has id=1
     * so next has id=2
     */
    @DatabaseField(columnName = FIELD_NEXT_ARTICLE_ID)
    private int nextArticleId = -1;

    /**
     * next means that if this is second in list sorted by pubDate and has id=2
     * so previous has id=1
     */
    @DatabaseField(columnName = FIELD_PREVIOUS_ARTICLE_ID)
    private int previousArticleId = -1;

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = FIELD_IS_INITIAL_IN_CATEGORY)
    private boolean isInitialInCategory;

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = FIELD_IS_TOP_IN_CATEGORY)
    private boolean isTopInCategory;

    public static void writeArtsListToArtCatFromBottom(ArrayList<Article> arts, int categoryId, int page, MyRoboSpiceDatabaseHelper h)
    {
        //0. Get topArtCat
        //if it's null, throw exception
        //1. Get last artCat for page.
        //2. Set its nextArtId to first of given
        //3. Set prevArtId of first of given to id of artCat from 1)
        //4. loop through given list and set next/prev artId checking for matching to artCat of same category
        //with prevArtId=-1
        //if matching
        //5. stop loop set nextArtId of previous of given list to matched
        //set prevArtId of matched to previous of given list
        //6. if given list.size<DefaultNumOfArtsInPage set last of list isInitial to true
        //7. write artCat

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
                throw new IllegalArgumentException("topArtCat is null and page isn't 1. That can't be");
            }
            //1.
            artCatListOfPreviousArts = ArticleCategory.getArtCatListFromGivenArticleId(topArtCat.getArticleId(), categoryId, h, true);
            //check size
            //if 0 - throw Exception
            //if <Default_Quont_Arts_on_Page - cant be - throw Exception
            //so... only ten can be...

            ArticleCategory lastArtCatByPage = artCatListOfPreviousArts.get(artCatListOfPreviousArts.size() - 1);
            int lastArticleIdInPreviousIteration = lastArtCatByPage.getArticleId();
            for (int i = 1; i < page - 1; i++)
            {
                artCatListOfPreviousArts = ArticleCategory.getArtCatListFromGivenArticleId(lastArticleIdInPreviousIteration, categoryId, h, false);
//                if(artCatListOfPreviousArts.size()==0)
//                {
//                    break;
//                }
                lastArtCatByPage = artCatListOfPreviousArts.get(artCatListOfPreviousArts.size() - 1);
                lastArticleIdInPreviousIteration = lastArtCatByPage.getArticleId();
            }
            //1)now we can check if we have some art in page, that we have load
            //2)and if so we can write only new arts and update old;
            //else we'll update lastArtCatByPage and write all new
            //3)ALSO we must check for every art with prevId=-1 for matching with given list
            //if match we can update it and write only new

            //get list of artCat for current page
            //1)
            artCatListOfPreviousArts = ArticleCategory.getArtCatListFromGivenArticleId(lastArticleIdInPreviousIteration, categoryId, h, false);
            if (artCatListOfPreviousArts.size() != 0)
            {
                lastArtCatByPage = artCatListOfPreviousArts.get(artCatListOfPreviousArts.size() - 1);
                lastArticleIdInPreviousIteration = lastArtCatByPage.getArticleId();

                boolean matched = false;

                match_from_top_loop:
                for (int i = 0; i < arts.size(); i++)
                {
                    Article a = arts.get(i);
                    if (a.getId() == lastArticleIdInPreviousIteration)
                    {
                        if (!matched)
                        {
                            //first matched!
                            //2)
                            int nextArtId = (i == arts.size() - 1) ? -1 : arts.get(i + 1).getId();
                            lastArtCatByPage.setNextArticleId(nextArtId);
                            daoArtCat.createOrUpdate(lastArtCatByPage);
                            matched = true;
                        }
                    }
                    else
                    {
                        if (matched)
                        {
                            //3)
                            ArrayList<ArticleCategory> artCatsWithoutPrevArtId = ArticleCategory.getArtCatsWithoutPrevArtId(h, categoryId);
                            for (int u = 0; u < artCatsWithoutPrevArtId.size(); u++)
                            {
                                ArticleCategory artCat = artCatsWithoutPrevArtId.get(u);
                                if (artCat.getArticleId() == a.getId())
                                {
                                    //maybe if i=0 we are in case of connecting 10 to 10 arts?..
                                    int prevArtId;
                                    if (i == 0)
                                    {
                                        prevArtId = lastArticleIdInPreviousIteration;
                                    }
                                    else
                                    {
                                        prevArtId = arts.get(i - 1).getId();
                                    }

                                    artCat.setPreviousArticleId(prevArtId);
                                    daoArtCat.createOrUpdate(artCat);
                                    //break;
                                    break match_from_top_loop;
                                }
                            }

                            ArticleCategory artCatToWrite = new ArticleCategory();
                            artCatToWrite.setCategoryId(categoryId);
                            artCatToWrite.setArticleId(a.getId());
                            Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                            int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                            artCatToWrite.setNextArticleId(nextArtIdInLoop);
                            artCatToWrite.setPreviousArticleId(arts.get(i - 1).getId());

                            artCatListToWrite.add(artCatToWrite);
                        }
                    }
                }

                for (ArticleCategory artCat : artCatListToWrite)
                {
                    daoArtCat.create(artCat);
                }
            }
            else
            {
                lastArtCatByPage.setNextArticleId(arts.get(0).getId());
                daoArtCat.createOrUpdate(lastArtCatByPage);

                match_from_bottom_loop:
                for (int i = 0; i < arts.size(); i++)
                {
                    Article a = arts.get(i);

                    //3)
                    ArrayList<ArticleCategory> artCatsWithoutPrevArtId = ArticleCategory.getArtCatsWithoutPrevArtId(h, categoryId);
                    for (int u = 0; u < artCatsWithoutPrevArtId.size(); u++)
                    {
                        ArticleCategory artCat = artCatsWithoutPrevArtId.get(u);
                        if (artCat.getArticleId() == a.getId())
                        {
                            //maybe if i=0 we are in case of connecting 10 to 10 arts?..
                            int prevArtId;
                            if (i == 0)
                            {
                                prevArtId = lastArticleIdInPreviousIteration;
                            }
                            else
                            {
                                prevArtId = arts.get(i - 1).getId();
                            }

                            artCat.setPreviousArticleId(prevArtId);
                            daoArtCat.createOrUpdate(artCat);
                            //break;
                            break match_from_bottom_loop;
                        }
                    }

                    ArticleCategory artCatToWrite = new ArticleCategory();
                    artCatToWrite.setCategoryId(categoryId);
                    artCatToWrite.setArticleId(a.getId());
                    Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                    int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                    artCatToWrite.setNextArticleId(nextArtIdInLoop);
                    int prevArtId = (i == 0) ? lastArtCatByPage.getArticleId() : arts.get(i - 1).getId();
                    artCatToWrite.setPreviousArticleId(prevArtId);

                    artCatListToWrite.add(artCatToWrite);
                }

                for (ArticleCategory artCat : artCatListToWrite)
                {
                    daoArtCat.create(artCat);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * creates list of ArticleCategory obj and writes them to DB
     *
     * @return quontity of new articles in category  (-1 on first loading, and 10 if cant match for old topArtCat, which
     * means that we do not now we have 10 or more new articles)
     */
    public static int writeArtsListToArtCatFromTop(ArrayList<Article> arts, int categoryId, MyRoboSpiceDatabaseHelper h)
    {
        //0. Get topArtCat
        //if it's null, so it's first time we write artCat
        //so simply write it
        //and return -1
        //1. else
        //now we must check if we have equals artId in given list and topArtCat
        //if true we must
        //1) set isTop for old artCat to false
        //2) update prevArtId for oldTopArtCat
        //3) set isTop for given list.get(0) to true
        //4) set nextArtId for last art in given list (last, that dont have equal id with oldTopArtCat
        //5) return how many new arts we get

        /**
         * can be
         * (-1) - initial loading
         * (0) - no new
         * (1-9) exact quont of new arts
         * (10) - 10 or more new arts;
         */
        int quontOfNewArtsInCategory = -1;

        ArrayList<ArticleCategory> artCatListToWrite = new ArrayList<>();

        try
        {
            Dao<ArticleCategory, Integer> daoArtCat = h.getDao(ArticleCategory.class);

            ArticleCategory topArtCat = daoArtCat.queryBuilder().
                    where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                    and().eq(ArticleCategory.FIELD_IS_TOP_IN_CATEGORY, true).queryForFirst();
            //0.
            if (topArtCat == null)
            {
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
                    artCatToWrite.setNextArticleId(nextArtIdInLoop);
                    artCatToWrite.setPreviousArticleId(arts.get(i - 1).getId());

                    artCatListToWrite.add(artCatToWrite);
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
                return quontOfNewArtsInCategory;
            }

            //1.
            for (int i = 0; i < arts.size(); i++)
            {
                Article a = arts.get(i);
                if (a.getId() == topArtCat.getArticleId())
                {
                    //1)
                    quontOfNewArtsInCategory = i;
                    if (i == 0)
                    {
                        //nothing to write
                        return quontOfNewArtsInCategory;
//                        break;
                    }
                    else
                    {
                        //1)
                        topArtCat.setTopInCategory(false);
                        //2)
                        topArtCat.setPreviousArticleId(arts.get(i - 1).getId());
                        daoArtCat.update(topArtCat);

                        //4)
                        artCatListToWrite.get(i - 1).setNextArticleId(topArtCat.getArticleId());
                        quontOfNewArtsInCategory = i;
                        break;
                    }
                }
                else
                {
                    ArticleCategory artCatToWrite = new ArticleCategory();
                    artCatToWrite.setArticleId(a.getId());
                    artCatToWrite.setCategoryId(categoryId);
                    //check if there is such entry in DB and set it's id to artCat obj
                    ArticleCategory artCatInDB = ArticleCategory.getByArticleAndCategoryIds(h, artCatToWrite.getArticleId(), artCatToWrite.getCategoryId());
                    if (artCatInDB != null)
                    {
                        artCatToWrite.setId(artCatInDB.getId());
                    }

                    Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                    int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                    artCatToWrite.setNextArticleId(nextArtIdInLoop);
                    int prevArtId = (i != 0) ? arts.get(i - 1).getId() : -1;
                    artCatToWrite.setPreviousArticleId(prevArtId);

                    artCatListToWrite.add(artCatToWrite);

                    quontOfNewArtsInCategory = i;

                    //also check if we at last iteration
                    //which means that no given article matched oldTopArtCat
                    //and so we have >=10 new arts
                    if (i == arts.size() - 1)
                    {
                        //return 10, which means >=10... Yeah it sucks(((
//                        quontOfNewArtsInCategory++;
                        quontOfNewArtsInCategory++;
                    }
                }
            }
            //3)
            //and clear is top status for all other artTags
            ArrayList<ArticleCategory> articleCatsThatAreTop;
            articleCatsThatAreTop = (ArrayList<ArticleCategory>) daoArtCat.queryBuilder().where().eq(FIELD_IS_TOP_IN_CATEGORY, true).and().eq(FIELD_CATEGORY_ID, categoryId).query();
            for (ArticleCategory articleCategory : articleCatsThatAreTop)
            {
                articleCategory.setTopInCategory(false);
                daoArtCat.createOrUpdate(articleCategory);
            }

            artCatListToWrite.get(0).setTopInCategory(true);

            for (ArticleCategory artCat : artCatListToWrite)
            {
                daoArtCat.createOrUpdate(artCat);
            }

            //write initial artTag if it is
            if (arts.size() > 0 && arts.size() < Const.NUM_OF_ARTS_ON_PAGE)
            {
                ArticleCategory initialArtCat = ArticleCategory.getByArticleAndCategoryIds(h, arts.get(arts.size() - 1).getId(), categoryId);
                initialArtCat.setInitialInCategory(true);
                h.getDaoArtCat().createOrUpdate(initialArtCat);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return quontOfNewArtsInCategory;
    }

    public static ArticleCategory getByArticleAndCategoryIds(MyRoboSpiceDatabaseHelper h, int articleId, int category_id)
    {
        ArticleCategory artCat = null;
        try
        {
            Dao<ArticleCategory, Integer> daoArticleCategory = h.getDao(ArticleCategory.class);

            artCat = daoArticleCategory.queryBuilder().where().
                    eq(ArticleCategory.FIELD_CATEGORY_ID, category_id).and().
                    eq(ArticleCategory.FIELD_ARTICLE_ID, articleId).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return artCat;
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

            if (curArtCat.getNextArticleId() == -1)
            {
//                Log.i(LOG, "returning artCatListFromGivenId with size: " + list.size());
                return list;
            }

            for (int i = 0; i < Const.NUM_OF_ARTS_ON_PAGE; i++)
            {
                if (i == 0)
                {
                    if (includeGiven)
                    {
                        list.add(curArtCat);
//                        Log.i(LOG, "curArtCat.getNextArticleId: " + curArtCat.getNextArticleId());
                        continue;
                    }
                }
//                Log.i(LOG, "curArtCat.getNextArticleId: " + curArtCat.getNextArticleId());

                if (curArtCat.getNextArticleId() == -1)
                {
//                    Log.i(LOG, "returning artCatListFromGivenId with size: " + list.size());
                    return list;
                }

                Article nextArt = daoArticle.queryBuilder().where().eq(Article.FIELD_ID, curArtCat.getNextArticleId()).queryForFirst();
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

//        Log.i(LOG, "returning artCatListFromGivenId with size: " + list.size());
        return list;
    }

    public static ArrayList<ArticleCategory> getArtCatListFromTop(int categoryId, MyRoboSpiceDatabaseHelper h)
    {
        ArticleCategory topArtCat = ArticleCategory.getTopArtCat(categoryId, h);
        return ArticleCategory.getArtCatListFromGivenArticleId(topArtCat.getArticleId(), categoryId, h, true);
    }

    public static ArticleCategory getTopArtCat(int category_id, MyRoboSpiceDatabaseHelper h)
    {
        ArticleCategory topArtCat = null;
        try
        {
            Dao<ArticleCategory, Integer> daoArtCat = h.getDao(ArticleCategory.class);
            topArtCat = daoArtCat.queryBuilder().
                    where().eq(ArticleCategory.FIELD_CATEGORY_ID, category_id).
                    and().eq(ArticleCategory.FIELD_IS_TOP_IN_CATEGORY, true).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return topArtCat;
    }

    public static ArrayList<ArticleCategory> getArtCatsWithoutPrevArtId(MyRoboSpiceDatabaseHelper h, int categoryId)
    {
        ArrayList<ArticleCategory> artCatsWithoutPrevArtId = new ArrayList<>();
        try
        {
            artCatsWithoutPrevArtId = (ArrayList<ArticleCategory>) h.getDao(ArticleCategory.class).queryBuilder().
                    where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                    and().eq(ArticleCategory.FIELD_PREVIOUS_ARTICLE_ID, -1).query();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return artCatsWithoutPrevArtId;
    }

    public static ArrayList<ArticleCategory> getArtCatsWithoutNextArtId(MyRoboSpiceDatabaseHelper h, int categoryId)
    {
        ArrayList<ArticleCategory> artCatsWithoutNextArtId = new ArrayList<>();
        try
        {
            artCatsWithoutNextArtId = (ArrayList<ArticleCategory>) h.getDao(ArticleCategory.class).queryBuilder().
                    where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                    and().eq(ArticleCategory.FIELD_NEXT_ARTICLE_ID, -1).query();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return artCatsWithoutNextArtId;
    }

    public static ArticleCategory getPrevArtCat(MyRoboSpiceDatabaseHelper h, ArticleCategory artCat)
    {
        ArticleCategory prevArtCat = null;
        try
        {
            prevArtCat = h.getDaoArtCat().queryBuilder().where().eq(ArticleCategory.FIELD_ARTICLE_ID, artCat.getPreviousArticleId()).
                    and().eq(ArticleCategory.FIELD_CATEGORY_ID, artCat.getCategoryId()).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return prevArtCat;
    }

    public static ArticleCategory getNextArtCat(MyRoboSpiceDatabaseHelper h, ArticleCategory artCat)
    {
        ArticleCategory nextArtCat = null;
        try
        {
            nextArtCat = h.getDaoArtCat().queryBuilder().where().eq(ArticleCategory.FIELD_ARTICLE_ID, artCat.getNextArticleId()).
                    and().eq(ArticleCategory.FIELD_CATEGORY_ID, artCat.getCategoryId()).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return nextArtCat;
    }

    /**
     * Searches list of ArtCatTable entries for ones, that have no nextArtCat and deletes it;
     *
     * @return quont of deleted arts;
     */
    public static int deleteAllLastArtCatInCategory(MyRoboSpiceDatabaseHelper h, String categoryUrl)
    {
        int quontOfDeletedArtCats = 0;
        Log.i(LOG, "deleting last arts from DB");
        ArrayList<ArticleCategory> artCatListToDelete = ArticleCategory.getArtCatsWithoutNextArtId(h, Category.getCategoryIdByUrl(categoryUrl, h));
        for (ArticleCategory artCatToDelete : artCatListToDelete)
        {
            try
            {
                ArticleCategory prevArtCat = ArticleCategory.getPrevArtCat(h, artCatToDelete);
                prevArtCat.setNextArticleId(-1);
                h.getDaoArtCat().createOrUpdate(prevArtCat);

                //also delete article obj
                Article a = Article.getArticleById(h, artCatToDelete.getArticleId());
                Article.printInLog(a);
                int updatedRows = h.getDaoArticle().delete(a);
                Log.i(LOG, "updatedRows: " + updatedRows);

                h.getDaoArtCat().delete(artCatToDelete);
                quontOfDeletedArtCats++;
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                quontOfDeletedArtCats--;
            }
        }
        return quontOfDeletedArtCats;
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

    public boolean isInitialInCategory()
    {
        return isInitialInCategory;
    }

    public void setInitialInCategory(boolean initialInCategory)
    {
        this.isInitialInCategory = initialInCategory;
    }

    public boolean isTopInCategory()
    {
        return isTopInCategory;
    }

    public void setTopInCategory(boolean topInCategory)
    {
        this.isTopInCategory = topInCategory;
    }
}