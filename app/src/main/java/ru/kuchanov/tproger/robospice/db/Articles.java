package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

/**
 * Created by Юрий on 16.10.2015 18:45.
 * For ExpListTest.
 */
@DatabaseTable
public class Articles
{
    public static final String LOG = Articles.class.getSimpleName();

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @ForeignCollectionField(eager = false)
    private Collection<Article> result;

    @DatabaseField
    private boolean containsBottomArt=false;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Collection<Article> getResult()
    {
//        Log.i(LOG, "start logging articles result");
//      for(Article a: result)
//      {
//          Article.printInLog(a);
//      }
//        Log.i(LOG, "stop logging articles result");

        return result;
    }



    public void setResult(Collection<Article> result)
    {
        this.result = result;
    }

    public boolean isContainsBottomArt()
    {
        return containsBottomArt;
    }

    public void setContainsBottomArt(boolean containsBottomArt)
    {
        this.containsBottomArt = containsBottomArt;
    }
}