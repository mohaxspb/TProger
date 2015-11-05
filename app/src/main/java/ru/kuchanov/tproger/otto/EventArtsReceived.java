package ru.kuchanov.tproger.otto;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.db.Article;

/**
 * Created by Юрий on 18.10.2015 20:12.
 * For ExpListTest.
 */
public class EventArtsReceived
{
    private ArrayList<Article> arts = new ArrayList<>();

    public EventArtsReceived(ArrayList<Article> arts)
    {
        this.arts = arts;
    }

    public ArrayList<Article> getArts()
    {
        return arts;
    }
}
