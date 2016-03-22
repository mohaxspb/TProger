package ru.kuchanov.tproger.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.html.HtmlParsing;

/**
 * Created by Юрий on 16.10.2015 16:43 20:51.
 * For TProger.
 */
public class RoboSpiceRequestArticle extends SpiceRequest<Article>
{
    public static final String LOG = RoboSpiceRequestArticle.class.getSimpleName();

    Context ctx;
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
        Log.i(LOG, "loadDataFromNetwork called");
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

        String responseBody = makeRequest();

        Article loadedArticle = HtmlParsing.parseArticle(responseBody, article.getUrl());
        //write to DB
        Article artinDB = Article.getArticleByUrl(databaseHelper, loadedArticle.getUrl());
        if (artinDB != null)
        {
            boolean isRead = artinDB.isRead();
            //TODO add tags comments and other info from article html
            int id = artinDB.getId();
            loadedArticle.setIsRead(isRead);
            loadedArticle.setId(id);
            databaseHelper.getDaoArticle().createOrUpdate(loadedArticle);
        }

        return loadedArticle;
    }

    private String makeRequest() throws Exception
    {
        Log.i(LOG, "mekeRequest with url: " + article.getUrl());
        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(article.getUrl());

        Response response = client.newCall(request.build()).execute();

        return response.body().string();
    }
}