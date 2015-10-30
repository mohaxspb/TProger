package ru.kuchanov.tproger;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{
    public static final String LOG = RecyclerAdapter.class.getSimpleName();

    private ArrayList<String> mDataset;

    public RecyclerAdapter(ArrayList<String> dataset)
    {
        mDataset = dataset;
    }

    public void addData(ArrayList<String> dataToAdd)
    {
        int prevSize = mDataset.size();
        mDataset.addAll(dataToAdd);
        this.notifyItemRangeInserted(prevSize, mDataset.size());
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_min, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public void notifyRemoveEach()
    {
        for (int i = 0; i < mDataset.size(); i++)
        {
            notifyItemRemoved(i);
        }
    }

    public void notifyAddEach()
    {
        for (int i = 0; i < mDataset.size(); i++)
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
