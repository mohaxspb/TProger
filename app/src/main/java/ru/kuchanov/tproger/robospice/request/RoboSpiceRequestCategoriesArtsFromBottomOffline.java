package ru.kuchanov.tproger.robospice.request;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.octo.android.robospice.request.SpiceRequest;

import java.util.ArrayList;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;

/**
 * Created by Юрий on 16.10.2015 16:43 20:06.
 * For TProger.
 */
public class RoboSpiceRequestCategoriesArtsFromBottomOffline extends SpiceRequest<Articles>
{
    public static final String LOG = RoboSpiceRequestCategoriesArtsFromBottomOffline.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    String url;
    String category;
    int page;

    public RoboSpiceRequestCategoriesArtsFromBottomOffline(Context ctx, String category, int page)
    {
        super(Articles.class);

        this.ctx = ctx;
        this.category = category;
        this.page = page;

        this.url = Const.DOMAIN_MAIN + category + Const.SLASH + "page" + Const.SLASH + page + Const.SLASH;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork() called");

        ArrayList<Article> list = new ArrayList<>();

        int categoryId = Category.getCategoryIdByUrl(this.category, databaseHelper);

        //try getting arts from DB
        Dao<ArticleCategory, Integer> daoArtCat = databaseHelper.getDao(ArticleCategory.class);
        Dao<Article, Integer> daoArt = databaseHelper.getDao(Article.class);

        //0.
        ArticleCategory topArtCat = daoArtCat.queryBuilder().
                where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                and().eq(ArticleCategory.FIELD_IS_TOP_IN_CATEGORY, true).queryForFirst();

//        Log.i(LOG, "page: " + page);
//        Log.i(LOG, "get arts for 1 page");
        ArrayList<ArticleCategory> artCatListFromDBFromGivenPage = ArticleCategory.getArtCatListFromGivenArticleId(topArtCat.getArticleId(), categoryId, databaseHelper, true);

        ArticleCategory lastArtCatByPage = artCatListFromDBFromGivenPage.get(artCatListFromDBFromGivenPage.size() - 1);
        int lastArticleIdInPreviousIteration = lastArtCatByPage.getArticleId();
        for (int i = 1; i < page; i++)
        {
//            Log.i(LOG, "get arts for " + String.valueOf(i + 1) + " page");
            artCatListFromDBFromGivenPage = ArticleCategory.getArtCatListFromGivenArticleId(lastArticleIdInPreviousIteration, categoryId, databaseHelper, false);
            if (artCatListFromDBFromGivenPage.size() == 0)
            {
                break;
            }
            lastArtCatByPage = artCatListFromDBFromGivenPage.get(artCatListFromDBFromGivenPage.size() - 1);
            lastArticleIdInPreviousIteration = lastArtCatByPage.getArticleId();
        }

        boolean isLastArtCatByPageIsBottom = lastArtCatByPage.isInitialInCategory();
//        Log.i(LOG, "isLastArtCatByPageIsBottom: "+String.valueOf(isLastArtCatByPageIsBottom));
//        Log.i(LOG, "artCatListFromDBFromGivenPage.size(): "+artCatListFromDBFromGivenPage.size());

        if ((artCatListFromDBFromGivenPage.size() == Const.NUM_OF_ARTS_ON_PAGE) || isLastArtCatByPageIsBottom)
        {
//            Log.i(LOG, "(artCatListFromDBFromGivenPage.size() == Const.NUM_OF_ARTS_ON_PAGE) || isLastArtCatByPageIsBottom");
            for (ArticleCategory artCat : artCatListFromDBFromGivenPage)
            {
                Article a = daoArt.queryBuilder().where().eq(Article.FIELD_ID, artCat.getArticleId()).queryForFirst();
                list.add(a);
            }
//            Article.printListInLog(list);

            Articles articles = new Articles();
            articles.setResult(list);

            if(isLastArtCatByPageIsBottom)
            {
                articles.setContainsBottomArt(true);
            }

            return articles;
        }
        else
        {
//            Log.i(LOG, "else");
            //so less than default num of art by page in DB, so start loading from network;
            return null;
        }
    }
}