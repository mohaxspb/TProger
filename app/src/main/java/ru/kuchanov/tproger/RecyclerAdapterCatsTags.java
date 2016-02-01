package ru.kuchanov.tproger;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.kuchanov.tproger.activity.ActivityCategoriesAndTags;
import ru.kuchanov.tproger.robospice.db.Category;
import ru.kuchanov.tproger.robospice.db.Tag;

public class RecyclerAdapterCatsTags extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final String LOG = RecyclerAdapterCatsTags.class.getSimpleName();

    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_TAG = 1;

    private int dataType = TYPE_CATEGORY;

    private ArrayList<Category> cats;
    private ArrayList<Tag> tags;

    public RecyclerAdapterCatsTags(ArrayList<Tag> tags, ArrayList<Category> cats)
    {
        this.tags = tags;
        this.cats = cats;
    }

    public void setDataType(int type)
    {
        this.dataType = type;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_min, parent, false);
        HolderTitle vh = new HolderTitle(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final Category category;
        final Tag tag;

        final HolderTitle holderTitle = (HolderTitle) holder;

        switch (dataType)
        {
            default:
            case TYPE_CATEGORY:
                category = cats.get(position);
                holderTitle.title.setText(category.getTitle());
                holderTitle.title.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.d(LOG, category.getTitle());
                        ActivityCategoriesAndTags.startActivityCatsAndTags(holderTitle.title.getContext(), cats, tags, TYPE_CATEGORY);
                    }
                });
                break;
            case TYPE_TAG:
                tag = tags.get(position);
                holderTitle.title.setText(tag.getTitle());
                holderTitle.title.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.d(LOG, tag.getTitle());
                        ActivityCategoriesAndTags.startActivityCatsAndTags(holderTitle.title.getContext(), cats, tags, TYPE_TAG);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        switch (dataType)
        {
            default:
            case TYPE_CATEGORY:
                return cats.size();
            case TYPE_TAG:
                return tags.size();
        }
    }

//    public void notifyRemoveEach()
//    {
//        for (int i = 0; i < tags.size(); i++)
//        {
//            notifyItemRemoved(i);
//        }
//    }
//
//    public void notifyAddEach()
//    {
//        for (int i = 0; i < tags.size(); i++)
//        {
//            notifyItemInserted(i);
//        }
//    }

    public static class HolderTitle extends RecyclerView.ViewHolder
    {
        public TextView title;

        public HolderTitle(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
        }
    }
}
