package ru.kuchanov.tproger.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
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

    private Context ctx;

    public RecyclerAdapterCatsTags(ArrayList<Tag> tags, ArrayList<Category> cats, Context ctx)
    {
        this.tags = tags;
        this.cats = cats;
        this.ctx = ctx;
    }

    public void setDataType(int type)
    {
        this.dataType = type;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_min, parent, false);
        return new HolderTitle(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final Category category;
        final Tag tag;

        final HolderTitle holderTitle = (HolderTitle) holder;

        int textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);
        float resultUiTextSizeInPx = uiTextScale * textSizePrimary;
        holderTitle.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, resultUiTextSizeInPx);

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
                        ActivityCategoriesAndTags.startActivityCatsAndTags(ctx, cats, tags, TYPE_CATEGORY, position);
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
                        ActivityCategoriesAndTags.startActivityCatsAndTags(ctx, cats, tags, TYPE_TAG, position);
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

    public static class HolderTitle extends RecyclerView.ViewHolder
    {
        //        public View container;
        public CardView cardView;
        public TextView title;

        public HolderTitle(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            cardView = (CardView) v.findViewById(R.id.cardView);
//            container = v;
        }
    }
}