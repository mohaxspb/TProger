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
    @DatabaseField(/*id = true, */generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @ForeignCollectionField(eager = false)
    private Collection<Article> result;

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
        return result;
    }

    public void setResult(Collection<Article> result)
    {
        this.result = result;
    }
}