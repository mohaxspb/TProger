package ru.kuchanov.tproger.utils;

import android.util.Log;

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

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;

/**
 * Created by Юрий on 19.10.2015 23:06.
 * For ExpListTest.
 */
public class HtmlParsing
{
    static final String LOG = HtmlParsing.class.getSimpleName();

    public static ArrayList<Article> parseForArticlesList(String html, MyRoboSpiceDatabaseHelper h)
    {
        Document doc = Jsoup.parse(html);
        Element mainColumns = doc.getElementById("main_columns");
        Elements articles = mainColumns.getElementsByTag("article");
        ArrayList<Article> list = new ArrayList<>();
        for (Element article : articles)
        {
            Element postTitleBox = article.getElementsByClass("post-title").get(0);

            Element h1 = postTitleBox.getElementsByTag("h1").get(0);
            Element link = h1.getElementsByTag("a").get(0);
            String url = link.attr("href");
            String title = link.text();
//            Log.i(LOG, "articleTitle: " + title);


            Article artInDB = Article.getArticleByUrl(h, url);
            if (artInDB != null)
            {
                //check if this article is already in DB and set it to list and goto next iteration;
                list.add(artInDB);
                Log.i(LOG, title + "is already in DB");
                continue;
            }
            else
            {
                //else continue this loop
                Log.i(LOG, title + "is NOT in DB");
            }

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
            preview = previewDiv.html();

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

        return list;
    }
}