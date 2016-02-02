package ru.kuchanov.tproger.robospice.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.ArticleCategory;
import ru.kuchanov.tproger.robospice.db.ArticleTag;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RoboSpiceRequestCategoriesArtsOffline extends SpiceRequest<Articles>
{
    public static final String LOG = RoboSpiceRequestCategoriesArtsOffline.class.getSimpleName();

//    private Context ctx;
    private MyRoboSpiceDatabaseHelper databaseHelper;
//    private String url;
    private String categoryOrTagUrl;

    public RoboSpiceRequestCategoriesArtsOffline(Context ctx, String categoryOrTagUrl)
    {
        super(Articles.class);

//        this.ctx = ctx;
        this.categoryOrTagUrl = categoryOrTagUrl;

//        this.url = "http://tproger.ru/page/1/";
//        this.url = Const.DOMAIN_MAIN + categoryOrTagUrl + Const.SLASH + "page" + Const.SLASH + 1 + Const.SLASH;

        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork() called");
        ArrayList<Article> list;

        //get list from DB

        Boolean isCategoryOrTagOrDoNotExists = MyRoboSpiceDatabaseHelper.isCategoryOrTagOrDoNotExists(databaseHelper, this.categoryOrTagUrl);
        if (isCategoryOrTagOrDoNotExists == null)
        {
            //TODO seems to be, that we must create it...
            //we'll make via return null, and so we'll start load from web, where we can create new one
            return null;
        }
        else
        {
            boolean isCategoryOrTag = isCategoryOrTagOrDoNotExists;
            if (isCategoryOrTag)
            {
                Category category = Category.getCategoryByUrl(categoryOrTagUrl, databaseHelper);
                int categoryId = Category.getCategoryIdByUrl(category.getUrl(), databaseHelper);

                if (ArticleCategory.getTopArtCat(categoryId, databaseHelper) != null)
                {
                    ArrayList<ArticleCategory> artCatList = ArticleCategory.getArtCatListFromTop(categoryId, databaseHelper);

                    list = Article.getArticleListFromArtCatList(artCatList, databaseHelper);
                    Articles articles = new Articles();
                    articles.setResult(list);
                    return articles;
                }
                else
                {
                    //So it's first time we asking for cache and there is no cache
                    //so load from web via returning null
                    return null;
                }
            }
            else
            {
                Tag tag = Tag.getTagByUrl(categoryOrTagUrl, databaseHelper);
                int tagId = Tag.getTagIdByUrl(tag.getUrl(), databaseHelper);

                if (ArticleTag.getTopArtCat(tagId, databaseHelper) != null)
                {
                    ArrayList<ArticleTag> artCatList = ArticleTag.getArtCatListFromTop(tagId, databaseHelper);

                    list = Article.getArticleListFromArtTagList(artCatList, databaseHelper);
                    Articles articles = new Articles();
                    articles.setResult(list);
                    return articles;
                }
                else
                {
                    //So it's first time we asking for cache and there is no cache
                    //so load from web via returning null
                    return null;
                }
            }
        }
    }
}