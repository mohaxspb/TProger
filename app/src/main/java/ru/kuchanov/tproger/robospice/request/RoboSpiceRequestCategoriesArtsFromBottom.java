package ru.kuchanov.tproger.robospice.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

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
    public static final String LOG = RoboSpiceRequestCategoriesArtsFromBottom.class.getSimpleName();

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

        this.url = Const.DOMAIN_MAIN + categoryOrTagUrl + Const.SLASH + "page" + Const.SLASH + page + Const.SLASH;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork() called");
        ArrayList<Article> listOfArticles;

        String responseBody = makeRequest();
        Document document = Jsoup.parse(responseBody);

        Boolean isCategoryOrTagOrDoNotExists = MyRoboSpiceDatabaseHelper.isCategoryOrTagOrDoNotExists(databaseHelper, this.categoryOrTagUrl);
        if (isCategoryOrTagOrDoNotExists == null)
        {
            throw new Exception("I cant imaging how it can be...");
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
}