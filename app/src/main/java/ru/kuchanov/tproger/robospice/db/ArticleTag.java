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
 * Created by Юрий on 20.10.2015 0:48.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "article_tag")
public class ArticleTag
{
    public static final String LOG = ArticleTag.class.getSimpleName();
    public static final String FIELD_ARTICLE_ID = "articleId";
    public static final String FIELD_TAG_ID = "FIELD_TAG_ID";
    public static final String FIELD_NEXT_ARTICLE_ID = "nextArticleId";
    public static final String FIELD_PREVIOUS_ARTICLE_ID = "previousArticleId";
    public static final String FIELD_IS_INITIAL_IN_TAG = "FIELD_IS_INITIAL_IN_TAG";
    public static final String FIELD_IS_TOP_IN_TAG = "FIELD_IS_TOP_IN_TAG";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_ARTICLE_ID)
    private int articleId;

    @DatabaseField(columnName = FIELD_TAG_ID)
    private int tagId;

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

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = FIELD_IS_INITIAL_IN_TAG)
    private boolean isInitialInTag;

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = FIELD_IS_TOP_IN_TAG)
    private boolean isTopInTag;

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

        ArrayList<ArticleTag> artCatListToWrite = new ArrayList<>();

        ArrayList<ArticleTag> artCatListOfPreviousArts;

        try
        {
            Dao<ArticleTag, Integer> daoArtCat = h.getDao(ArticleTag.class);

            //0.
            ArticleTag topArtCat = daoArtCat.queryBuilder().
                    where().eq(ArticleTag.FIELD_TAG_ID, categoryId).
                    and().eq(ArticleTag.FIELD_IS_TOP_IN_TAG, true).queryForFirst();
            if (topArtCat == null)
            {
                throw new IllegalArgumentException("topArtCat is null and page isn't 1. That can't be");
            }
            //1.
            artCatListOfPreviousArts = ArticleTag.getArtCatListFromGivenArticleId(topArtCat.getArticleId(), categoryId, h, true);
            //check size
            //if 0 - throw Exception
            //if <Default_Quont_Arts_on_Page - cant be - throw Exception
            //so... only ten can be...

            ArticleTag lastArtCatByPage = artCatListOfPreviousArts.get(artCatListOfPreviousArts.size() - 1);
            int lastArticleIdInPreviousIteration = lastArtCatByPage.getArticleId();
            for (int i = 1; i < page - 1; i++)
            {
                artCatListOfPreviousArts = ArticleTag.getArtCatListFromGivenArticleId(lastArticleIdInPreviousIteration, categoryId, h, false);
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
            artCatListOfPreviousArts = ArticleTag.getArtCatListFromGivenArticleId(lastArticleIdInPreviousIteration, categoryId, h, false);
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
                            ArrayList<ArticleTag> artCatsWithoutPrevArtId = ArticleTag.getArtCatsWithoutPrevArtId(h, categoryId);
                            for (int u = 0; u < artCatsWithoutPrevArtId.size(); u++)
                            {
                                ArticleTag artCat = artCatsWithoutPrevArtId.get(u);
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

                            ArticleTag artCatToWrite = new ArticleTag();
                            artCatToWrite.setTagId(categoryId);
                            artCatToWrite.setArticleId(a.getId());
                            Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                            int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                            artCatToWrite.setNextArticleId(nextArtIdInLoop);
                            artCatToWrite.setPreviousArticleId(arts.get(i - 1).getId());

                            artCatListToWrite.add(artCatToWrite);
                        }
                    }
                }

                for (ArticleTag artCat : artCatListToWrite)
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
                    ArrayList<ArticleTag> artCatsWithoutPrevArtId = ArticleTag.getArtCatsWithoutPrevArtId(h, categoryId);
                    for (int u = 0; u < artCatsWithoutPrevArtId.size(); u++)
                    {
                        ArticleTag artCat = artCatsWithoutPrevArtId.get(u);
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

                    ArticleTag artCatToWrite = new ArticleTag();
                    artCatToWrite.setTagId(categoryId);
                    artCatToWrite.setArticleId(a.getId());
                    Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                    int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                    artCatToWrite.setNextArticleId(nextArtIdInLoop);
                    int prevArtId = (i == 0) ? lastArtCatByPage.getArticleId() : arts.get(i - 1).getId();
                    artCatToWrite.setPreviousArticleId(prevArtId);

                    artCatListToWrite.add(artCatToWrite);
                }

                for (ArticleTag artCat : artCatListToWrite)
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

        ArrayList<ArticleTag> artCatListToWrite = new ArrayList<>();

        try
        {
            Dao<ArticleTag, Integer> daoArtCat = h.getDao(ArticleTag.class);

            ArticleTag topArtCat = daoArtCat.queryBuilder().
                    where().eq(ArticleTag.FIELD_TAG_ID, categoryId).
                    and().eq(ArticleTag.FIELD_IS_TOP_IN_TAG, true).queryForFirst();
            //0.
            if (topArtCat == null)
            {
                //so it's first loading of art (from top)
                //so write artCat and set isTop to true for first row
                ArticleTag artCat = new ArticleTag();
                artCat.setArticleId(arts.get(0).getId());
                artCat.setTagId(categoryId);
                artCat.setTopInTag(true);
                Article nextArt = (arts.size() > 1) ? arts.get(1) : null;
                int nextArtId = (nextArt == null) ? -1 : nextArt.getId();
                artCat.setNextArticleId(nextArtId);

                artCatListToWrite.add(artCat);

                for (int i = 1; i < arts.size(); i++)
                {
                    Article a = arts.get(i);

                    ArticleTag artCatToWrite = new ArticleTag();
                    artCatToWrite.setArticleId(a.getId());
                    artCatToWrite.setTagId(categoryId);
                    Article nextArtInLoop = (arts.size() > i + 1) ? arts.get(i + 1) : null;
                    int nextArtIdInLoop = (nextArtInLoop == null) ? -1 : nextArtInLoop.getId();
                    artCatToWrite.setNextArticleId(nextArtIdInLoop);
                    artCatToWrite.setPreviousArticleId(arts.get(i - 1).getId());

                    artCatListToWrite.add(artCatToWrite);
                }

                //set bottom art if size of arts<DefaultNumOnPage;
                if (arts.size() < Const.NUM_OF_ARTS_ON_PAGE)
                {
                    artCatListToWrite.get(artCatListToWrite.size() - 1).setInitialInTag(true);
                }

                for (ArticleTag artCatToWrite : artCatListToWrite)
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
                        topArtCat.setTopInTag(false);
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
                    ArticleTag artCatToWrite = new ArticleTag();
                    artCatToWrite.setArticleId(a.getId());
                    artCatToWrite.setTagId(categoryId);
                    //check if there is such entry in DB and set it's id to artCat obj
                    ArticleTag artCatInDB = ArticleTag.getByArticleAndCategoryIds(h, artCatToWrite.getArticleId(), artCatToWrite.getTagId());
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
            artCatListToWrite.get(0).setTopInTag(true);

            for (ArticleTag artCat : artCatListToWrite)
            {
                daoArtCat.createOrUpdate(artCat);
            }

            //write initial artTag if it is
            if (arts.size() > 0 && arts.size() < Const.NUM_OF_ARTS_ON_PAGE)
            {
                ArticleTag initialArtTag = ArticleTag.getByArticleAndCategoryIds(h, arts.get(arts.size() - 1).getId(), categoryId);
                initialArtTag.setInitialInTag(true);
                h.getDaoArtTag().createOrUpdate(initialArtTag);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return quontOfNewArtsInCategory;
    }

    public static ArticleTag getByArticleAndCategoryIds(MyRoboSpiceDatabaseHelper h, int articleId, int category_id)
    {
        ArticleTag artCat = null;
        try
        {
            Dao<ArticleTag, Integer> daoArticleCategory = h.getDao(ArticleTag.class);

            artCat = daoArticleCategory.queryBuilder().where().
                    eq(ArticleTag.FIELD_TAG_ID, category_id).and().
                    eq(ArticleTag.FIELD_ARTICLE_ID, articleId).queryForFirst();
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
    public static ArrayList<ArticleTag> getArtCatListFromGivenArticleId(int firstArticleId, int categoryId, MyRoboSpiceDatabaseHelper h, boolean includeGiven)
    {
        ArrayList<ArticleTag> list = new ArrayList<>();

        try
        {
            Dao<Article, Integer> daoArticle = h.getDao(Article.class);
            Dao<ArticleTag, Integer> daoArticleCategory = h.getDao(ArticleTag.class);

            ArticleTag artCatInitial = daoArticleCategory.queryBuilder()
                    .where().eq(ArticleTag.FIELD_ARTICLE_ID, firstArticleId)
                    .and().eq(ArticleTag.FIELD_TAG_ID, categoryId).queryForFirst();
            if (artCatInitial == null)
            {
                throw new NullPointerException("No ArticleCategory for given Article id. WTF?!");
            }

            ArticleTag curArtCat = artCatInitial;

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
                ArticleTag nextArtCat = daoArticleCategory.queryBuilder().
                        where().eq(ArticleTag.FIELD_ARTICLE_ID, nextArt.getId()).
                        and().eq(ArticleTag.FIELD_TAG_ID, categoryId).queryForFirst();
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

    public static ArrayList<ArticleTag> getArtCatListFromTop(int categoryId, MyRoboSpiceDatabaseHelper h)
    {
        ArticleTag topArtCat = ArticleTag.getTopArtCat(categoryId, h);
        return ArticleTag.getArtCatListFromGivenArticleId(topArtCat.getArticleId(), categoryId, h, true);
    }

    public static ArticleTag getTopArtCat(int category_id, MyRoboSpiceDatabaseHelper h)
    {
        ArticleTag topArtCat = null;
        try
        {
            Dao<ArticleTag, Integer> daoArtCat = h.getDao(ArticleTag.class);
            topArtCat = daoArtCat.queryBuilder().
                    where().eq(ArticleTag.FIELD_TAG_ID, category_id).
                    and().eq(ArticleTag.FIELD_IS_TOP_IN_TAG, true).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return topArtCat;
    }

    public static ArrayList<ArticleTag> getArtCatsWithoutPrevArtId(MyRoboSpiceDatabaseHelper h, int categoryId)
    {
        ArrayList<ArticleTag> artCatsWithoutPrevArtId = new ArrayList<>();
        try
        {
            artCatsWithoutPrevArtId = (ArrayList<ArticleTag>) h.getDao(ArticleTag.class).queryBuilder().
                    where().eq(ArticleTag.FIELD_TAG_ID, categoryId).
                    and().eq(ArticleTag.FIELD_PREVIOUS_ARTICLE_ID, -1).query();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return artCatsWithoutPrevArtId;
    }

    public static ArrayList<ArticleTag> getArtCatsWithoutNextArtId(MyRoboSpiceDatabaseHelper h, int categoryId)
    {
        ArrayList<ArticleTag> artCatsWithoutNextArtId = new ArrayList<>();
        try
        {
            artCatsWithoutNextArtId = (ArrayList<ArticleTag>) h.getDao(ArticleTag.class).queryBuilder().
                    where().eq(ArticleTag.FIELD_TAG_ID, categoryId).
                    and().eq(ArticleTag.FIELD_NEXT_ARTICLE_ID, -1).query();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return artCatsWithoutNextArtId;
    }

    public static ArticleTag getPrevArtCat(MyRoboSpiceDatabaseHelper h, ArticleTag artCat)
    {
        ArticleTag prevArtCat = null;
        try
        {
            prevArtCat = h.getDaoArtTag().queryBuilder().where().eq(ArticleTag.FIELD_ARTICLE_ID, artCat.getPreviousArticleId()).
                    and().eq(ArticleTag.FIELD_TAG_ID, artCat.getTagId()).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return prevArtCat;
    }

    public static ArticleTag getNextArtCat(MyRoboSpiceDatabaseHelper h, ArticleTag artCat)
    {
        ArticleTag nextArtCat = null;
        try
        {
            nextArtCat = h.getDaoArtTag().queryBuilder().where().eq(ArticleTag.FIELD_ARTICLE_ID, artCat.getNextArticleId()).
                    and().eq(ArticleTag.FIELD_TAG_ID, artCat.getTagId()).queryForFirst();
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
        ArrayList<ArticleTag> artCatListToDelete = ArticleTag.getArtCatsWithoutNextArtId(h, Category.getCategoryIdByUrl(categoryUrl, h));
        for (ArticleTag artCatToDelete : artCatListToDelete)
        {
            try
            {
                ArticleTag prevArtCat = ArticleTag.getPrevArtCat(h, artCatToDelete);
                prevArtCat.setNextArticleId(-1);
                h.getDaoArtTag().createOrUpdate(prevArtCat);

                //also delete article obj
                Article a = Article.getArticleById(h, artCatToDelete.getArticleId());
                Article.printInLog(a);
                int updatedRows = h.getDaoArticle().delete(a);
                Log.i(LOG, "updatedRows: " + updatedRows);

                h.getDaoArtTag().delete(artCatToDelete);
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

    public int getTagId()
    {
        return tagId;
    }

    public void setTagId(int tagId)
    {
        this.tagId = tagId;
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

    public boolean isInitialInTag()
    {
        return isInitialInTag;
    }

    public void setInitialInTag(boolean initialInTag)
    {
        this.isInitialInTag = initialInTag;
    }

    public boolean isTopInTag()
    {
        return isTopInTag;
    }

    public void setTopInTag(boolean topInTag)
    {
        this.isTopInTag = topInTag;
    }
}