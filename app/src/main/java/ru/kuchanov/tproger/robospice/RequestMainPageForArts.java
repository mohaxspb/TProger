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
import java.util.TimeZone;

/**
 * Created by Юрий on 16.10.2015 16:43.
 * For ExpListTest.
 */
public class RequestMainPageForArts extends SpiceRequest<ArrayListModel>
{
    public static final String LOG = RequestMainPageForArts.class.getSimpleName();

    Context ctx;
    MyRoboSpiceDatabaseHelper databaseHelper;
    String url;

    public RequestMainPageForArts(Context ctx)
    {
        super(ArrayListModel.class);

        this.ctx = ctx;
        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        this.url = "http://tproger.ru/page/1/";
    }

    @Override
    public ArrayListModel loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "ArrayListModel loadDataFromNetwork() called");

        ArrayList<Model> list;

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
//            Element titleBox = postTitleBox.getElementsByClass("title-box").get(0);

            Element h1 = postTitleBox.getElementsByTag("h1").get(0);
            Element link = h1.getElementsByTag("a").get(0);
            String url = link.attr("href");
            String title = link.text();

            Element postMeta = postTitleBox.getElementsByClass("post-meta").get(0);
            Element ul = postMeta.getElementsByTag("ul").get(0);
            Element li = ul.getElementsByTag("li").get(0);
            Element time = li.getElementsByTag("time").get(0);
            String timeStr = time.attr("datetime");
//            <time class="entry-date updated" datetime="2015-10-17T15:24:47+00:00">17 октября 2015 в 15:24</time>
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
            Date result = new Date(0);
            try
            {
                result = df.parse(timeStr);
                Log.i(LOG, "date:" + result);//prints date in current locale
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                Log.i(LOG, sdf.format(result));//prints date in the format sdf
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }

            Log.i(LOG, "articleTitle: " + title);
        }


//        Log.i(LOG, "list.size() is: " + list.size());
//        for (int i = 0; i < list.size(); i++)
//        {
//            Log.i(LOG, list.get(i).toString());
//        }


        ArrayListModel arrayListModel = databaseHelper.getDao(ArrayListModel.class).queryBuilder().queryForFirst();
//        if (arrayListModel != null)
//        {
//            arrayListModel.setResult(list);
//        }
//        else
//        {
//            arrayListModel = new ArrayListModel();
//            arrayListModel.setResult(list);
//        }

        return arrayListModel;
    }
}