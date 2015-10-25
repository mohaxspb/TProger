package ru.kuchanov.tproger;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.db.Article;

public class RecyclerAdapterArtsList extends RecyclerView.Adapter<RecyclerAdapterArtsList.ViewHolder>
{
    public static final String LOG = RecyclerAdapterArtsList.class.getSimpleName();

    private ArrayList<Article> artsList;

    public RecyclerAdapterArtsList(ArrayList<Article> dataset)
    {
        artsList = dataset;
    }

    public void addData(ArrayList<Article> dataToAdd)
    {
        int prevSize = artsList.size();
        artsList.addAll(dataToAdd);
        this.notifyItemRangeInserted(prevSize, artsList.size());
    }

    @Override
    public RecyclerAdapterArtsList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.mTextView.setText(artsList.get(position).getTitle());
    }

    @Override
    public int getItemCount()
    {
        return artsList.size();
    }

    public void notifyRemoveEach()
    {
        for (int i = 0; i < artsList.size(); i++)
        {
            notifyItemRemoved(i);
        }
    }

    public void notifyAddEach()
    {
        for (int i = 0; i < artsList.size(); i++)
        {
            notifyItemInserted(i);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView mTextView;

        public ViewHolder(View v)
        {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.title);
        }
    }
}
