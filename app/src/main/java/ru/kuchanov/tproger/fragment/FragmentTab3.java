package ru.kuchanov.tproger.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.RecyclerAdapter;

public class FragmentTab3 extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_tab_3, container, false);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this.getActivity()));
//        String[] mDataSet = new String[100];
//        for (int i = 0; i < 100; i++)
//        {
//            mDataSet[i] = "Tab3, item" + i;
//        }
        ArrayList<String> mDataSet=new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            mDataSet.add("Tab3, item" + i);
        }

        recycler.setAdapter(new RecyclerAdapter(mDataSet));

        return v;
    }
}