package ru.kuchanov.tproger.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.utils.html.HtmlParsing;

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

    boolean resetCategoryInDB = false;

    public RoboSpiceRequestCategoriesArts(Context ctx, String category)
    {
        super(Articles.class);

        this.ctx = ctx;
        this.category = category;

//        this.url = "http://tproger.ru/page/1/";
        this.url = Const.DOMAIN_MAIN + category + Const.SLASH + "page" + Const.SLASH + 1 + Const.SLASH;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);

    }

    public void setResetCategoryInDB()
    {
        resetCategoryInDB = true;
    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork called");

        Category cat = Category.getCategoryByUrl(this.category, databaseHelper);
        if (cat == null)
        {
            //TODO need to think can we create new one here
            throw new NullPointerException("no such category in DB!");
        }

        if (resetCategoryInDB)
        {
            Log.i(LOG, "resetCategoryInDB");
            //all we need - is to delete all artCat by category...
            ArrayList<ArticleCategory> allArtCatList = (ArrayList<ArticleCategory>) databaseHelper.getDaoArtCat().queryBuilder().
                    where().eq(ArticleCategory.FIELD_CATEGORY_ID, cat.getId()).query();
            databaseHelper.getDaoArtCat().delete(allArtCatList);
        }

        String responseBody = makeRequest();

        ArrayList<Article> list = HtmlParsing.parseForArticlesList(responseBody, databaseHelper);
        //write to DB
        list = Article.writeArtsList(list, databaseHelper);

        int newArtsQuont = ArticleCategory.writeArtsListToArtCatFromTop(list, cat.getId(), databaseHelper);
        //we can pass quont through Articles class via field...
        Log.i(LOG, "newArtsQuont: " + newArtsQuont);

        //update refreshed date of category to currentTimeInMills
        cat.setRefreshed(Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00")).getTime());
        databaseHelper.getDaoCategory().createOrUpdate(cat);

        Articles articles = new Articles();
        articles.setNumOfNewArts(newArtsQuont);
        articles.setResult(list);

        //TODO if need
        //parse and write new categories and tags to DB
        this.updateTagsAndCategoriesIfNeed(responseBody);

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

    private void updateTagsAndCategoriesIfNeed(String html)
    {
        Document document = Jsoup.parse(html);
        Element divCategories = document.getElementsByClass("bl_category").first();
//        Element divTags = document.getElementsByClass("widget_tag_cloud").first();
        Element divTags = document.getElementById("tag_cloud-3");

        ArrayList<Category> categoriesFromDB = new ArrayList<>();
        ArrayList<Tag> tagsFromDB = new ArrayList<>();
        try
        {
            categoriesFromDB = (ArrayList<Category>) databaseHelper.getDaoCategory().queryForAll();
            tagsFromDB = (ArrayList<Tag>) databaseHelper.getDaoTag().queryForAll();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        ArrayList<Category> categoriesToWrite = new ArrayList<>();

        Elements liWithCategory = divCategories.getElementsByTag("li");
        for (Element element : liWithCategory)
        {
            Element aTag = element.getElementsByTag("a").first();
            Category category = new Category();
            category.setUrl(aTag.attr("href"));
            category.setTitle(aTag.text());

            if (categoriesFromDB.contains(category))
            {
                Log.d(LOG, category.getTitle() + " already in DB");
//                continue;
            }
            else
            {
                categoriesToWrite.add(category);
            }
        }
        //parse tags
        ArrayList<Tag> tagsToWrite = new ArrayList<>();

        Elements aWithTag = divTags.getElementsByTag("a");
        for (Element aTag : aWithTag)
        {
//            Element aTag = element.getElementsByTag("a").first();
            Tag tag = new Tag();
            tag.setUrl(aTag.attr("href"));
            tag.setTitle(aTag.text());

            if (tagsFromDB.contains(tag))
            {
                Log.d(LOG, tag.getTitle() + " already in DB");
            }
            else
            {
                tagsToWrite.add(tag);
            }
        }

        //write them
        try
        {
            for (Category c : categoriesToWrite)
            {
                databaseHelper.getDaoCategory().createOrUpdate(c);
            }
            for (Tag t : tagsToWrite)
            {
                databaseHelper.getDaoTag().createOrUpdate(t);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}