package ru.kuchanov.tproger.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.ArticleTag;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.utils.html.HtmlParsing;

/**
 * Created by Юрий on 16.10.2015 16:43 20:07.
 * For TProger.
 */
public class RoboSpiceRequestCategoriesArtsFromBottom extends SpiceRequest<Articles>
{
    public /*static final*/ String LOG = RoboSpiceRequestCategoriesArtsFromBottom.class.getSimpleName();

    //    private Context ctx;
    private MyRoboSpiceDatabaseHelper databaseHelper;
    private String url;
    private String categoryOrTagUrl;
    private int page;

    private int categoryId;
    private int tagId;

    public RoboSpiceRequestCategoriesArtsFromBottom(Context ctx, String categoryOrTagUrl, int page)
    {
        super(Articles.class);

//        this.ctx = ctx;
        this.categoryOrTagUrl = categoryOrTagUrl;
        this.page = page;

        this.url = ((categoryOrTagUrl.startsWith("http")) ? "" : Const.DOMAIN_MAIN) + categoryOrTagUrl + ((categoryOrTagUrl.endsWith("/") || categoryOrTagUrl.equals("")) ? "" : Const.SLASH) + "page" + Const.SLASH + page + Const.SLASH;

        this.LOG += "#" + url;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "loadDataFromNetwork() called");
        ArrayList<Article> listOfArticles;

        String responseBody = makeRequest();
        Document document = Jsoup.parse(responseBody);

        Boolean isCategoryOrTagOrDoNotExists = MyRoboSpiceDatabaseHelper.isCategoryOrTagOrDoNotExists(databaseHelper, this.categoryOrTagUrl);
        if (isCategoryOrTagOrDoNotExists == null)
        {
            throw new IllegalStateException("I cant imaging how it can be...");
        }

        boolean isCategory = isCategoryOrTagOrDoNotExists;
        if (isCategory)
        {
            Category category = Category.getCategoryByUrl(categoryOrTagUrl, databaseHelper);
            categoryId = category.getId();
        }
        else
        {
            Tag tag = Tag.getTagByUrl(categoryOrTagUrl, databaseHelper);
            tagId = tag.getId();
        }

        try
        {
            listOfArticles = HtmlParsing.parseForArticlesList(document, databaseHelper);
        }
        catch (Exception e)
        {
            if (e.getMessage() != null)
            {
                if (e.getMessage().equals(Const.ERROR_404_WHILE_PARSING_PAGE))
                {
                    //here can be only one artCat with nextArtId = -1
                    //so get it and set it's isBottom to true;
                    if (isCategory)
                    {
                        ArticleCategory bottomArtCat = databaseHelper.getDaoArtCat().queryBuilder().
                                where().eq(ArticleCategory.FIELD_CATEGORY_ID, categoryId).
                                and().eq(ArticleCategory.FIELD_NEXT_ARTICLE_ID, -1).queryForFirst();
                        bottomArtCat.setInitialInCategory(true);
                        databaseHelper.getDaoArtCat().createOrUpdate(bottomArtCat);
                    }
                    else
                    {
                        ArticleTag bottomArtTag = databaseHelper.getDaoArtTag().queryBuilder().
                                where().eq(ArticleTag.FIELD_TAG_ID, tagId).
                                and().eq(ArticleTag.FIELD_NEXT_ARTICLE_ID, -1).queryForFirst();
                        bottomArtTag.setInitialInTag(true);
                        databaseHelper.getDaoArtTag().createOrUpdate(bottomArtTag);
                    }
                }
            }
            e.printStackTrace();
            throw new Exception(Const.ERROR_404_WHILE_PARSING_PAGE);
        }

        //write to DB
        listOfArticles = Article.writeArtsList(listOfArticles, databaseHelper);


        if (isCategory)
        {
            ArticleCategory.writeArtsListToArtCatFromBottom(listOfArticles, categoryId, page, databaseHelper);
        }
        else
        {
            ArticleTag.writeArtsListToArtCatFromBottom(listOfArticles, tagId, page, databaseHelper);
        }

        Articles articles = new Articles();
        articles.setResult(listOfArticles);

        if (listOfArticles.size() < Const.NUM_OF_ARTS_ON_PAGE)
        {
            articles.setContainsBottomArt(true);
        }

        return articles;
    }

    private String makeRequest() throws Exception
    {
        Log.i(LOG, "makeRequest with url: " + url);
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Request.Builder request = new Request.Builder();
        request.url(this.url);
        Response response = client.newCall(request.build()).execute();

//        OkHttpClient client = new OkHttpClient.Builder()
////                .followRedirects(false)
//                .build();
//
//        String url = "http://www.scp-wiki.net/random:random-scp";
//
//        Request.Builder request = new Request.Builder();
//        request.url(url);
//        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        request.addHeader("Accept-Encoding", "gzip, deflate, sdch");
//        request.addHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
//        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36");
//        request.addHeader("referer", "http://www.scp-wiki.net/");
//        request.addHeader("save", "data:on");
//        request.addHeader("upgrade-insecure-requests", "1");
//        request.get();
//
//        Response response = client.newCall(request.build()).execute();
//
//        Log.i("LOG", response.toString());
//        Log.i("LOG", response.body().string());

        return response.body().string();
    }
}