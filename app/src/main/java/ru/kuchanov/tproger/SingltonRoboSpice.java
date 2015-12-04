package ru.kuchanov.tproger;

import android.app.Application;

import ru.kuchanov.tproger.robospice.HtmlSpiceService;
import ru.kuchanov.tproger.robospice.HtmlSpiceServiceArticle;
import ru.kuchanov.tproger.robospice.HtmlSpiceServiceOffline;
import ru.kuchanov.tproger.robospice.HtmlSpiceServiceOfflineArticle;
import ru.kuchanov.tproger.robospice.MySpiceManager;

/**
 * Created by Юрий on 23.10.2015 1:21.
 * For ExpListTest.
 */
public class SingltonRoboSpice extends Application
{
    private static SingltonRoboSpice ourInstance = new SingltonRoboSpice();

    private MySpiceManager spiceManager = new MySpiceManager(HtmlSpiceService.class);
    private MySpiceManager spiceManagerOffline = new MySpiceManager(HtmlSpiceServiceOffline.class);

    private MySpiceManager spiceManagerArticle = new MySpiceManager(HtmlSpiceServiceArticle.class);
    private MySpiceManager spiceManagerOfflineArticle = new MySpiceManager(HtmlSpiceServiceOfflineArticle.class);

    public static SingltonRoboSpice getInstance()
    {
        return ourInstance;
    }

    public MySpiceManager getSpiceManager()
    {
        return spiceManager;
    }

    public MySpiceManager getSpiceManagerOffline()
    {
        return spiceManagerOffline;
    }

    public MySpiceManager getSpiceManagerArticle()
    {
        return spiceManagerArticle;
    }

    public MySpiceManager getSpiceManagerOfflineArticle()
    {
        return spiceManagerOfflineArticle;
    }
}