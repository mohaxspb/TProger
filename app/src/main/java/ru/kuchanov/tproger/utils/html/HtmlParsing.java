package ru.kuchanov.tproger.utils.html;

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

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.tproger.robospice.db.Article;

/**
 * Created by Юрий on 19.10.2015 23:06 19:21.
 * For TProger.
 */
public class HtmlParsing
{
    private static final String LOG = HtmlParsing.class.getSimpleName();

    public static ArrayList<Article> parseForArticlesList(Document doc, MyRoboSpiceDatabaseHelper h) throws Exception
    {
//        Document doc = Jsoup.parse(html);

        //check title if it contains "Страница не найдена" and throw exception
        //that means, that we try to load page, that not exists
        Element pageTitle = doc.getElementsByTag("title").first();
        if (pageTitle.html().contains("Страница не найдена"))
        {
            throw new Exception(Const.ERROR_404_WHILE_PARSING_PAGE);
//            return null;
        }

        Element mainColumns = doc.getElementById("main_columns");
        for (Element el : doc.getElementsByClass("columns"))
        {
            Log.i(LOG, el.html());
        }
        Elements articlesAll = mainColumns.getElementsByTag("article");
        Elements articles = new Elements();
        for (Element element : articlesAll)
        {
            if (element.className().contains("type-post"))
            {
                articles.add(element);
            }
        }
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
//                Log.i(LOG, title + " is already in DB");
                continue;
            }
//            else
//            {
//                //else continue this loop
////                Log.i(LOG, title + "is NOT in DB");
//            }

            Element postMeta = postTitleBox.getElementsByClass("post-meta").first();
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

//            Element tagMain = li.getElementsByTag("a").get(0);
//            String tagMainUrl = tagMain.attr("href");
//            String tagMainTitle = tagMain.text();

            //image
            String imageUrl = null;
            int imageWidth = 0;
            int imageHeight = 0;
            Elements imageDiv = article.getElementsByClass("entry-image");
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
            Element previewDiv = article.getElementsByClass("entry-content").first();
            previewDiv.select("footer").remove();
//            Log.d(LOG, previewDiv.html());
            //remove scripts
            for (Element script : previewDiv.getElementsByTag("script"))
            {
                script.remove();
            }
//            Log.d(LOG, previewDiv.html());
            preview = previewDiv.html();

            Article a = new Article();
            a.setUrl(url);
            a.setTitle(title);
//            a.setTagMainTitle(tagMainTitle);
//            a.setTagMainUrl(tagMainUrl);
            a.setPubDate(pubDate);
            a.setImageUrl(imageUrl);
            a.setImageHeight(imageHeight);
            a.setImageWidth(imageWidth);
            a.setPreview(preview);

            list.add(a);
        }

        return list;
    }

    public static ArrayList<Element> getElementListFromHtml(String html)
    {
        return Jsoup.parse(html).body().children();
    }

    //    public static Article parseArticle(/*MyRoboSpiceDatabaseHelper h, */String html, String url) throws Exception
    public static Article parseArticle(String html, String url) throws Exception
    {
        //so at least we'll have url in assed article.
        //And, if it was in DB we also have all data (id, preview etc)

        Document doc = Jsoup.parse(html);

        Element pageTitle = doc.getElementsByTag("title").first();
        if (pageTitle.html().contains("Страница не найдена"))
        {
            throw new Exception(Const.ERROR_404_WHILE_PARSING_PAGE);
        }

        Element postTitleBox = doc.getElementsByClass("post-title").first();

        Element h1 = postTitleBox.getElementsByTag("h1").first();
        String title = h1.text();

        Element postMeta = postTitleBox.getElementsByClass("post-meta").first();
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
        Elements imageDiv = doc.getElementsByClass("entry-image");
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

        //preview from meta tag
        Element metaDescription = doc.getElementsByAttributeValue("name", "description").first();
        String preview = metaDescription.attr("content");

        //article text
        String text;
        Element textDiv = doc.getElementsByClass("entry-content").first();
        //remove scripts
        for (Element script : textDiv.getElementsByTag("script"))
        {
            script.remove();
        }
        textDiv.select("footer").remove();
        text = textDiv.html();

        Article parsedArticle = new Article();

        parsedArticle.setUrl(url);
        parsedArticle.setTitle(title);
        parsedArticle.setTagMainTitle(tagMainTitle);
        parsedArticle.setTagMainUrl(tagMainUrl);
        parsedArticle.setPubDate(pubDate);
        parsedArticle.setImageUrl(imageUrl);
        parsedArticle.setImageHeight(imageHeight);
        parsedArticle.setImageWidth(imageWidth);
        parsedArticle.setPreview(preview);
        parsedArticle.setText(text);

//        Article.printInLog(parsedArticle);

//        ArrayList<HtmlToView.TextType> textNodes = HtmlToView.getTextPartSummary(HtmlParsing.getElementListFromHtml(parsedArticle.getText()));
//        for (HtmlToView.TextType type : textNodes)
//        {
//            Log.i(LOG, type.toString());
//        }

        return parsedArticle;
    }

    public static AccordionContent parseAccordion(String accordionHtml)
    {
        Document doc = Jsoup.parse(accordionHtml);
        String title = doc.getElementsByClass("accordion-heading").first().getElementsByTag("a").first().text();

        Element collapsedPart = doc.getElementsByClass("accordion-inner").first();
        Element imgTag = collapsedPart.getElementsByTag("img").first();
        String imageUrl = imgTag.attr("src");
        int imgWidth = Integer.parseInt(imgTag.attr("width"));
        int imgHeight = Integer.parseInt(imgTag.attr("height"));

        return new AccordionContent(title, imageUrl, imgWidth, imgHeight);
    }

    public static class AccordionContent
    {
        private String title;
        private String imageUrl;
        private int imgWidth;
        private int imgHeight;

        public AccordionContent(String title, String imageUrl, int imgWidth, int imgHeight)
        {
            this.title = title;
            this.imageUrl = imageUrl;
            this.imgWidth = imgWidth;
            this.imgHeight = imgHeight;
        }

        public String getTitle()
        {
            return title;
        }

        public String getImageUrl()
        {
            return imageUrl;
        }

        public int getImgWidth()
        {
            return imgWidth;
        }

        public int getImgHeight()
        {
            return imgHeight;
        }
    }
}