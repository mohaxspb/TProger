package ru.kuchanov.tproger.robospice;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Юрий on 16.10.2015 16:47.
 * For ExpListTest.
 */
@DatabaseTable(tableName = "model")
public class Model
{
//    @DatabaseField(id = true)
    @DatabaseField( generatedId = true, allowGeneratedIdInsert=true)
    private int id;

    @DatabaseField
    private String login = "testLogin";

    @DatabaseField(foreign = true)
    private ArrayListModel result;

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    @Override
    public String toString()
    {
        return this.getLogin() + ", id = " + id;
    }

    public ArrayListModel getResult()
    {
        return result;
    }

    public void setResult(ArrayListModel result)
    {
        this.result = result;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}