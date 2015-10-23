package ru.kuchanov.tproger;

import android.app.Application;

import ru.kuchanov.tproger.robospice.HtmlSpiceService;
import ru.kuchanov.tproger.robospice.HtmlSpiceServiceOffline;
import ru.kuchanov.tproger.robospice.MySpiceManager;

/**
 * Created by Юрий on 23.10.2015 1:21.
 * For ExpListTest.
 */
public class AppSinglton extends Application
{
    private static AppSinglton ourInstance = new AppSinglton();
    private MySpiceManager spiceManager= new MySpiceManager(HtmlSpiceService.class);

    private MySpiceManager spiceManagerOffline= new MySpiceManager(HtmlSpiceServiceOffline.class);

    public static AppSinglton getInstance() {
        return ourInstance;
    }

    public MySpiceManager getSpiceManager() {
        return spiceManager;
    }

    public MySpiceManager getSpiceManagerOffline() {
        return spiceManagerOffline;
    }
}