package ru.kuchanov.tproger.robospice.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestArticleOffline extends SpiceRequest<Article>
{
    public static final String LOG = RoboSpiceRequestArticleOffline.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    Article article;

    public RoboSpiceRequestArticleOffline(Context ctx, Article article)
    {
        super(Article.class);

        this.ctx = ctx;
        this.article = article;
    }

    @Override
    public Article loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork() called");

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        //get article from DB
        return Article.getArticleByUrl(databaseHelper, article.getUrl());
    }
}