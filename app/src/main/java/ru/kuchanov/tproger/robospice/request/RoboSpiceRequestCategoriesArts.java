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

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.ArticleTag;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;
import ru.kuchanov.tproger.utils.WriteFile;
import ru.kuchanov.tproger.utils.html.HtmlParsing;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestCategoriesArts extends SpiceRequest<Articles>
{
    public static final String LOG = RoboSpiceRequestCategoriesArts.class.getSimpleName();
    boolean resetCategoryInDB = false;
    private Context ctx;
    private MyRoboSpiceDatabaseHelper databaseHelper;
    private String url;
    private String categoryOrTagUrl;

    public RoboSpiceRequestCategoriesArts(Context ctx, String categoryOrTagUrl)
    {
        super(Articles.class);

        this.ctx = ctx;
        this.categoryOrTagUrl = categoryOrTagUrl;

//        this.url = "http://tproger.ru/page/1/";
        if (categoryOrTagUrl.startsWith("http"))
        {
            this.url = categoryOrTagUrl;
        }
        else
        {
            this.url = Const.DOMAIN_MAIN + categoryOrTagUrl;// /*+ Const.SLASH*/ + "page" + Const.SLASH + 1;// + Const.SLASH;
        }
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
        Articles articles = new Articles();

        String responseBody = makeRequest();
        Document document = Jsoup.parse(responseBody);

        ArrayList<Article> list = HtmlParsing.parseForArticlesList(document, databaseHelper);
        //write to DB
        list = Article.writeArtsList(list, databaseHelper);
        int newArtsQuont;

        Boolean isCategoryOrTagOrDoNotExists = MyRoboSpiceDatabaseHelper.isCategoryOrTagOrDoNotExists(databaseHelper, this.categoryOrTagUrl);
        if (isCategoryOrTagOrDoNotExists == null)
        {
            Log.i(LOG, "NO such cat/tag in DB, so crete new one!");
            //seems to be, that we must create it...
            String title;
            String urlOfCatOrTag;

            if (url.contains("/category/"))
            {
                isCategoryOrTagOrDoNotExists = true;

                title = document.head().getElementsByAttributeValue("og:title", "content").first().attr("content");
                if (categoryOrTagUrl.startsWith("http"))
                {
                    int indexOfCategory = categoryOrTagUrl.indexOf("/category/");
                    urlOfCatOrTag = categoryOrTagUrl.substring(indexOfCategory);
                }
                else
                {
                    urlOfCatOrTag = categoryOrTagUrl;
                }
                Category category = new Category();
                category.setTitle(title);
                category.setUrl(urlOfCatOrTag);
                databaseHelper.getDaoCategory().createOrUpdate(category);

            }
            else if (url.contains("/tag/"))
            {
                isCategoryOrTagOrDoNotExists = false;

                title = document.head().getElementsByAttributeValue("og:title", "content").first().attr("content");
                if (categoryOrTagUrl.startsWith("http"))
                {
                    int indexOfTag = categoryOrTagUrl.indexOf("/tag/");
                    urlOfCatOrTag = categoryOrTagUrl.substring(indexOfTag);
                }
                else
                {
                    urlOfCatOrTag = categoryOrTagUrl;
                }
                Tag tag = new Tag();
                tag.setTitle(title);
                tag.setUrl(urlOfCatOrTag);
                databaseHelper.getDaoTag().createOrUpdate(tag);
            }
            else
            {
                throw new NullPointerException("can't figure out if it's category or tag...");
            }
        }

        boolean isCategoryOrTag = isCategoryOrTagOrDoNotExists;
        if (isCategoryOrTag)
        {
            Category category = Category.getCategoryByUrl(categoryOrTagUrl, databaseHelper);

            if (resetCategoryInDB)
            {
                Log.i(LOG, "resetCategoryInDB");
                //all we need - is to delete all artCat by category...
                ArrayList<ArticleCategory> allArtCatList = (ArrayList<ArticleCategory>) databaseHelper.getDaoArtCat().queryBuilder().
                        where().eq(ArticleCategory.FIELD_CATEGORY_ID, category.getId()).query();
                databaseHelper.getDaoArtCat().delete(allArtCatList);
            }

            newArtsQuont = ArticleCategory.writeArtsListToArtCatFromTop(list, category.getId(), databaseHelper);
            //we can pass quont through Articles class via field...

            //update refreshed date of category to currentTimeInMills
//            Log.d(LOG, "category refreshed: " + category.getRefreshed());
            category.setRefreshed(Calendar.getInstance().getTime());
            /*boolean isUpdated =*/
            databaseHelper.getDaoCategory().createOrUpdate(category).isUpdated();
//            Log.d(LOG, "update status is: " + isUpdated);
//            Category updated = Category.getCategoryByUrl(categoryOrTagUrl, databaseHelper);
//            Log.d(LOG, "category refreshed: " + updated.getRefreshed());
        }
        else
        {
            Tag tag = Tag.getTagByUrl(categoryOrTagUrl, databaseHelper);

            if (resetCategoryInDB)
            {
                Log.i(LOG, "resetCategoryInDB");
                //all we need - is to delete all artCat by category...
                ArrayList<ArticleTag> allArtCatList = (ArrayList<ArticleTag>) databaseHelper.getDaoArtTag().queryBuilder().
                        where().eq(ArticleTag.FIELD_TAG_ID, tag.getId()).query();
                databaseHelper.getDaoArtTag().delete(allArtCatList);
            }

            newArtsQuont = ArticleTag.writeArtsListToArtCatFromTop(list, tag.getId(), databaseHelper);
            //we can pass quont through Articles class via field...

            //update refreshed date of category to currentTimeInMills
//            tag.setRefreshed(Calendar.getInstance().getTime());
//            databaseHelper.getDaoTag().createOrUpdate(tag);
//            Log.d(LOG, "tag refreshed: " + tag.getRefreshed());
            tag.setRefreshed(Calendar.getInstance().getTime());
            /*boolean isUpdated = */
            databaseHelper.getDaoTag().createOrUpdate(tag).isUpdated();
//            Log.d(LOG, "update status is: " + isUpdated);
//            Tag updated = Tag.getTagByUrl(categoryOrTagUrl, databaseHelper);
//            Log.d(LOG, "tag refreshed: " + updated.getRefreshed());
        }
        ///////////////////////////////////

        articles.setNumOfNewArts(newArtsQuont);
        articles.setResult(list);
        //check if we receive less then Const.NUM_OF_ARTS_ON_PAGE, and if so make last artCat/artTag isBottom
        //and set value to Articles obj
        //TODO test
        Log.d(LOG, "list.size(): " + list.size());

        if (list.size() < Const.NUM_OF_ARTS_ON_PAGE)
        {
            articles.setContainsBottomArt(true);
        }
        Log.d(LOG, "articles.isContainsBottomArt(): " + articles.isContainsBottomArt());
        //TODO if need
        //parse and write new categories and tags to DB
//        this.updateTagsAndCategoriesIfNeed(responseBody);

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
            String url = aTag.attr("href");
            url = url.replace(Const.DOMAIN_MAIN, "");
            category.setUrl(url);
            category.setTitle(aTag.text());

//            if (categoriesFromDB.contains(category))
//            {
//                Log.d(LOG, category.getTitle() + " already in DB");
//                continue;
//            }
            if (!categoriesFromDB.contains(category))
            {
                categoriesToWrite.add(category);
            }
        }

        //parse tags
        ArrayList<Tag> tagsToWrite = new ArrayList<>();

        Elements aWithTag = divTags.getElementsByTag("a");
        for (Element aTag : aWithTag)
        {
            Tag tag = new Tag();
            String url = aTag.attr("href");
            url = url.replace(Const.DOMAIN_MAIN, "");
            tag.setUrl(url);
            tag.setTitle(aTag.text());

//            if (tagsFromDB.contains(tag))
//            {
//                Log.d(LOG, tag.getTitle() + " already in DB");
//            }
//            else
            if (!tagsFromDB.contains(tag))
            {
                tagsToWrite.add(tag);
            }
        }

        //just get initial info for DB. Need to comment it in release;
//        createInitialTagCategoriesInfoFile(categoriesFromDB, tagsFromDB);

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

    /**
     * Used to create initial data for filling data base tables via copying writened data from result file to res/values
     */
    private void createInitialTagCategoriesInfoFile(ArrayList<Category> cats, ArrayList<Tag> tags)
    {
        StringBuilder builder = new StringBuilder();

        //categories
        builder.append("<string-array name=\"categories_title\" formatted=\"false\">\n");
        for (Category category : cats)
        {
            builder.append("<item><![CDATA[").append(category.getTitle()).append("]]></item>\n");
        }
        builder.append("</string-array>");
        builder.append("\n").append("\n");

        builder.append("<string-array name=\"categories_url\" formatted=\"false\">\n");
        for (Category category : cats)
        {
            builder.append("<item><![CDATA[").append(category.getUrl()).append("]]></item>\n");
        }
        builder.append("</string-array>");
        builder.append("\n").append("\n");

        //tags
        builder.append("<string-array name=\"tags_title\" formatted=\"false\">\n");
        for (Tag tag : tags)
        {
            builder.append("<item><![CDATA[").append(tag.getTitle()).append("]]></item>\n");
        }
        builder.append("</string-array>");
        builder.append("\n").append("\n");

        builder.append("<string-array name=\"tags_url\" formatted=\"false\">");
        for (Tag tag : tags)
        {
            builder.append("<item><![CDATA[").append(tag.getUrl()).append("]]></item>\n");
        }
        builder.append("</string-array>");

        WriteFile write = new WriteFile(builder.toString(), "DB_DEBUG", "cats_and_tags_res.txt", ctx);
        write.execute();
    }
}