package ru.kuchanov.tproger.robospice;
//

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

/**
 * Created by Юрий on 16.10.2015 18:45.
 * For ExpListTest.
 */
@DatabaseTable
public class ArrayListModel
{
    @DatabaseField(id = true)
    private int id;

    @ForeignCollectionField(eager = false)
    private Collection<Model> result;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Collection<Model> getResult()
    {
        return result;
    }

    public void setResult(Collection<Model> result)
    {
        this.result = result;
    }
}


//import java.util.ArrayList;
//
//import ru.kuchanov.tproger.robospice.Model;
//
//public class ArrayListModel extends ArrayList<Model>
//{
//    private static final long serialVersionUID = 8192333539004718470L;
//}