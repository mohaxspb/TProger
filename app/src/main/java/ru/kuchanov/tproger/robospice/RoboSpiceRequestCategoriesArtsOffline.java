package ru.kuchanov.tproger.robospice;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import java.util.ArrayList;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestCategoriesArtsOffline extends SpiceRequest<Articles>
{
    public static final String LOG = RoboSpiceRequestCategoriesArtsOffline.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    String url;
    String category;

    public RoboSpiceRequestCategoriesArtsOffline(Context ctx, String category)
    {
        super(Articles.class);

        Log.i(LOG, "category: "+category);

        this.ctx = ctx;
        this.category = category;

        Log.i(LOG, "this.category: "+this.category);

//        this.url = "http://tproger.ru/page/1/";
        this.url = Const.DOMAIN_MAIN + category + Const.SLASH + "page" + Const.SLASH + 1 + Const.SLASH;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "loadDataFromNetwork() called");

        ArrayList<Article> list = null;

        //get list from DB

        int categoryId = Category.getCategoryIdByUrl(this.category, databaseHelper);

        if (ArticleCategory.getTopArtCat(categoryId, databaseHelper) != null)
        {
            ArrayList<ArticleCategory> artCatList = ArticleCategory.getArtCatListFromTop(categoryId, databaseHelper);

            list = Article.getArticleListFromArtCatList(artCatList, databaseHelper);
        }

        Articles articles = new Articles();
        articles.setResult(list);

        return articles;
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     */
    public String createCacheKey()
    {
        return "categoriesArtsList." + category + Const.SLASH + 1;
    }
}