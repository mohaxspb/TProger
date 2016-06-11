package ru.kuchanov.tproger.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;

/**
 * Created by Юрий on 16.10.2015 16:43 20:51.
 * For TProger.
 */
public class RoboSpiceRequestArticleOffline extends SpiceRequest<Article>
{
    public static final String LOG = RoboSpiceRequestArticleOffline.class.getSimpleName();

    private Context ctx;
    private Article article;

    public RoboSpiceRequestArticleOffline(Context ctx, Article article)
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

        //get from DB by URL to DB
        Article artinDB = Article.getArticleByUrl(databaseHelper, article.getUrl());
        //check if art in DB has text. If not we must return null and start loading from web
        if (artinDB != null && artinDB.getText() == null)
        {
            return null;
        }

        return Article.getArticleByUrl(databaseHelper, article.getUrl());
    }
}