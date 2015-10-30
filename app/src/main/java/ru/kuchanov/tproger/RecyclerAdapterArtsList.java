package ru.kuchanov.tproger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.MyUIL;

public class RecyclerAdapterArtsList extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final String LOG = RecyclerAdapterArtsList.class.getSimpleName();
    boolean isSimple;
    private ArrayList<Article> artsList;
    private SharedPreferences pref;
    private Context ctx;
    private ImageLoader imageLoader;

    public RecyclerAdapterArtsList(Context ctx, ArrayList<Article> dataset)
    {
        this.ctx = ctx;
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        isSimple = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_style), false);

        imageLoader = MyUIL.get(ctx);

        artsList = dataset;
    }

    public void addData(ArrayList<Article> dataToAdd)
    {
        int prevSize = artsList.size();
        artsList.addAll(dataToAdd);
        this.notifyItemRangeInserted(prevSize, artsList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder vh;

        if (isSimple)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_min, parent, false);
            vh = new ViewHolderMinimum(v);
            return vh;
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_max, parent, false);
            vh = new ViewHolderMaximum(v);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        Article a = artsList.get(position);
        if (isSimple)
        {
            ViewHolderMinimum minHolder = (ViewHolderMinimum) holder;
            minHolder.title.setText(a.getTitle());
        }
        else
        {
            ViewHolderMaximum maxHolder = (ViewHolderMaximum) holder;
            maxHolder.title.setText(a.getTitle());

            Log.i(LOG, "a.getImageUrl(): " + a.getImageUrl());
//            Log.i(LOG, "a.getImageWidth(): " + a.getImageWidth());
//            Log.i(LOG, "a.getImageHeight(): " + a.getImageHeight());

            LinearLayout.LayoutParams paramsImg;
            if (a.getImageUrl() != null)
            {
                paramsImg = (LinearLayout.LayoutParams) maxHolder.img.getLayoutParams();

                int widthDevice = ctx.getResources().getDisplayMetrics().widthPixels;
                float scale = (float) widthDevice / a.getImageWidth();
                int height = (int) scale * a.getImageHeight();

//                paramsImg.width = widthDevice;
                paramsImg.height = height;

                maxHolder.img.setLayoutParams(paramsImg);

                imageLoader.displayImage(a.getImageUrl(), maxHolder.img);
            }
            else
            {
                maxHolder.img.setImageDrawable(null);
                paramsImg = (LinearLayout.LayoutParams) maxHolder.img.getLayoutParams();
                paramsImg.height = 0;
                maxHolder.img.setLayoutParams(paramsImg);
            }
        }
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

    public static class ViewHolderMinimum extends RecyclerView.ViewHolder
    {
        public TextView title;

        public ViewHolderMinimum(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
        }
    }

    public static class ViewHolderMaximum extends RecyclerView.ViewHolder
    {
        public TextView title;
        public ImageView img;

        public ViewHolderMaximum(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            img = (ImageView) v.findViewById(R.id.art_card_img);
        }
    }
}
