package ru.kuchanov.tproger.robospice.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.html.HtmlParsing;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestArticle extends SpiceRequest<Article>
{
    public static final String LOG = RoboSpiceRequestArticle.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    Article article;

    public RoboSpiceRequestArticle(Context ctx, Article article)
    {
        super(Article.class);

        this.ctx = ctx;
        this.article = article;
    }

    @Override
    public Article loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork called");
        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

        String responseBody = makeRequest();

        //TODO
        Article loadedArticle = HtmlParsing.parseArticle(databaseHelper, responseBody, article.getUrl());
        //write to DB
//        Article artWritenToDB = Article.writeArtsList(list, databaseHelper);

        //TODO
        return loadedArticle;
    }

    private String makeRequest() throws Exception
    {
        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(article.getUrl());

        Response response = client.newCall(request.build()).execute();

        return response.body().string();
    }
}