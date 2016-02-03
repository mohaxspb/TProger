package ru.kuchanov.tproger.robospice.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import ru.kuchanov.tproger.robospice.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 16.10.2015 18:45 19:45.
 * For TProger.
 */
@DatabaseTable(tableName = "articles")
public class Articles
{
    public static final String LOG = Articles.class.getSimpleName();

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @ForeignCollectionField(eager = false)
    private Collection<Article> result;

    @DatabaseField
    private boolean containsBottomArt = false;

    /**
     * can be
     * (-1) - initial loading
     * (0) - no new
     * (1-9) exact quont of new arts
     * (10) - 10 or more new arts;
     * <p/>
     * or
     * -2 if is not setted
     */
    @DatabaseField
    private int numOfNewArts = -2;

    public static void deleteAllEntries(MyRoboSpiceDatabaseHelper h)
    {
        try
        {
            ArrayList<Articles> articles = (ArrayList<Articles>) h.getDao(Articles.class).queryForAll();
            h.getDao(Articles.class).delete(articles);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

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

    public int getNumOfNewArts()
    {
        return numOfNewArts;
    }

    public void setNumOfNewArts(int numOfNewArts)
    {
        this.numOfNewArts = numOfNewArts;
    }
}