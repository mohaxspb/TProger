package ru.kuchanov.tproger.robospice;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.ArrayList;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.utils.HtmlParsing;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestCategoriesArtsFromBottom extends SpiceRequest<Articles>
{
    public static final String LOG = RoboSpiceRequestCategoriesArtsFromBottom.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    String url;
    String category;
    int page;

    public RoboSpiceRequestCategoriesArtsFromBottom(Context ctx, String category, int page)
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
        Log.i(LOG, "ArrayListModel loadDataFromNetwork() called");

        String responseBody = makeRequest();

        ArrayList<Article> list=new ArrayList<>();

        int categoryId = Category.getCategoryIdByUrl(this.category, databaseHelper);

        try
        {
            list = HtmlParsing.parseForArticlesList(responseBody, databaseHelper);
        }
        catch (Exception e)
        {
            if(e.getMessage()!=null)
            {
                if(e.getMessage().equals(Const.ERROR_404_WHILE_PARSING_PAGE))
                {
                    //here can be only one artCat with nextArtId = -1
                    //so get it and set it's isBottom to true;
                    ArticleCategory bottomArtCat=databaseHelper.getDao(ArticleCategory.class).queryBuilder().
                            where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                            and().eq(ArticleCategory.FIELD_NEXT_ARTICLE_ID, -1).queryForFirst();
                    bottomArtCat.setInitialInCategory(true);
                    databaseHelper.getDao(ArticleCategory.class).createOrUpdate(bottomArtCat);
                }
            }
            e.printStackTrace();
        }

        //write to DB
        list = Article.writeArtsList(list, databaseHelper);



        ArticleCategory.writeArtsListToArtCatFromBottom(list, categoryId, page, databaseHelper);

        Articles articles = new Articles();
        articles.setResult(list);

        return articles;
    }

    private String makeRequest() throws Exception
    {
        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(this.url);

        Response response = client.newCall(request.build()).execute();

        return response.body().string();
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     *
     */
//    public String createCacheKey()
//    {
//        return "categoriesArtsList." + category + Const.SLASH + page;
//    }
}