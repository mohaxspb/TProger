package ru.kuchanov.tproger.robospice;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RequestMainPageForArts extends SpiceRequest<Articles>
{
    public static final String LOG = RequestMainPageForArts.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    String url;

    public RequestMainPageForArts(Context ctx)
    {
        super(Articles.class);

        this.ctx = ctx;
        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        this.url = "http://tproger.ru/page/1/";
    }

    @Override
    public Articles loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "ArrayListModel loadDataFromNetwork() called");

        ArrayList<Article> list = new ArrayList<Article>();

        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(this.url);

        Response response = client.newCall(request.build()).execute();
        String responseBody = response.body().string();

        Document doc = Jsoup.parse(responseBody);

        Element mainColumns = doc.getElementById("main_columns");
        Elements articles = mainColumns.getElementsByTag("article");
        for (Element article : articles)
        {
//            for(Element e: article.getAllElements())
//            {
//                Log.i(LOG, e.className());
//            }

            Element postTitleBox = article.getElementsByClass("post-title").get(0);

            Element h1 = postTitleBox.getElementsByTag("h1").get(0);
            Element link = h1.getElementsByTag("a").get(0);
            String url = link.attr("href");
            String title = link.text();
//            Log.i(LOG, "articleTitle: " + title);

            Element postMeta = postTitleBox.getElementsByClass("post-meta").get(0);
            Element ul = postMeta.getElementsByTag("ul").get(0);
            Element li = ul.getElementsByTag("li").get(0);
            Element time = li.getElementsByTag("time").get(0);
            String timeStr = time.attr("datetime");
//            <time class="entry-date updated" datetime="2015-10-17T15:24:47+00:00">17 октября 2015 в 15:24</time>xxxx
            Date pubDate = new Date(0);
            try
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.getDefault());
                pubDate = df.parse(timeStr);
//                Log.i(LOG, "date:" + pubDate);//prints date in current locale
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'в' HH:mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//                Log.i(LOG, sdf.format(pubDate));//prints date in the format sdf
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }

            Element tagMain = li.getElementsByTag("a").get(0);
            String tagMainUrl = tagMain.attr("href");
            String tagMainTitle = tagMain.text();

            //image
            String imageUrl = null;
            int imageWidth = 0;
            int imageHeight = 0;
            Elements imageDiv = postTitleBox.getElementsByClass("entry-image");
            if (imageDiv.size() != 0)
            {
                Elements img = imageDiv.get(0).getElementsByTag("img");
                if (img.size() != 0)
                {
                    imageUrl = img.get(0).attr("src");
                    imageWidth = Integer.parseInt(img.get(0).attr("width"));
                    imageHeight = Integer.parseInt(img.get(0).attr("height"));
                }
            }
            String preview;
            Element previewDiv = article.getElementsByClass("entry-content").get(0);
//            Element previewP = previewDiv.getElementsByTag("p").get(0);
//            preview = previewP.html();
            preview=previewDiv.html();

            Article a = new Article();
            a.setUrl(url);
            a.setTitle(title);
            a.setTagMainTitle(tagMainTitle);
            a.setTagMainUrl(tagMainUrl);
            a.setPubDate(pubDate);
            a.setImageUrl(imageUrl);
            a.setImageHeight(imageHeight);
            a.setImageWidth(imageWidth);
            a.setPreview(preview);
            list.add(a);
        }


//        Log.i(LOG, "list.size() is: " + list.size());
//        for (int i = 0; i < list.size(); i++)
//        {
//            Log.i(LOG, list.get(i).toString());
//        }


        Articles arrayListModel = databaseHelper.getDao(Articles.class).queryBuilder().queryForFirst();
        if (arrayListModel != null)
        {
            arrayListModel.setResult(list);
        }
        else
        {
            arrayListModel = new Articles();
            arrayListModel.setResult(list);
        }

        return arrayListModel;
    }
}