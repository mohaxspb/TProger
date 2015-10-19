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
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.utils.HtmlParsing;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestCategoriesArts extends SpiceRequest<Articles>
{
    public static final String LOG = RoboSpiceRequestCategoriesArts.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    String url;
    String category;
    int page;

    public RoboSpiceRequestCategoriesArts(Context ctx, String category, int page)
    {
        super(Articles.class);

        this.ctx = ctx;
        this.category = category;
        this.page = page;

//        this.url = "http://tproger.ru/page/1/";
        this.url = Const.DOMAIN_MAIN + category + Const.SLASH + "page" + Const.SLASH + page + Const.SLASH;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "ArrayListModel loadDataFromNetwork() called");

        String responseBody = makeRequest();

        ArrayList<Article> list = HtmlParsing.parseForArticlesList(responseBody, databaseHelper);
        //write to DB
        list = Article.create(list, databaseHelper);

        Articles arrayListModel = new Articles();
        arrayListModel.setResult(list);

        return arrayListModel;
    }

    private String makeRequest() throws Exception
    {
        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(this.url);

        Response response = client.newCall(request.build()).execute();
//        String responseBody = response.body().string();

        return response.body().string();
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     *
     */
    public String createCacheKey()
    {
        return "categoriesArtsList." + category + Const.SLASH + page;
    }
}