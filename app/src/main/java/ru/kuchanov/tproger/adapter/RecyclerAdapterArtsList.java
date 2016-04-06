package ru.kuchanov.tproger.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.activity.ActivityArticle;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.DipToPx;
import ru.kuchanov.tproger.utils.MyUIL;
import ru.kuchanov.tproger.utils.html.HtmlParsing;
import ru.kuchanov.tproger.utils.html.HtmlToView;

public class RecyclerAdapterArtsList extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final String LOG = RecyclerAdapterArtsList.class.getSimpleName();
    private static final int SHOW_MAX_INFO = 1;
    private static final int SHOW_MIN_INFO = 0;
    private int textSizePrimary;
    private int textSizeSecondary;
    private ArrayList<Article> artsList;
    private SharedPreferences pref;
    private Context ctx;
    private ImageLoader imageLoader;

    private String categoryOrTagUrl;

    public RecyclerAdapterArtsList(Context ctx, ArrayList<Article> dataset, String categoryOrTagUrl)
    {
        this.ctx = ctx;
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        imageLoader = MyUIL.get(ctx);

        artsList = dataset;
        this.categoryOrTagUrl = categoryOrTagUrl;

        textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        textSizeSecondary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_secondary);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder vh;
        View v;
        switch (viewType)
        {
            case SHOW_MAX_INFO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_max, parent, false);
                vh = new ViewHolderMaximum(v);
                return vh;
            default:
            case SHOW_MIN_INFO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_min, parent, false);
                vh = new ViewHolderMinimum(v);
                return vh;
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        boolean showMaxInfo = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_style), false);
        return (showMaxInfo) ? SHOW_MAX_INFO : SHOW_MIN_INFO;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);

        final Article a = artsList.get(position);
        boolean showMaxInfo = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_style), false);

        if (showMaxInfo)
        {
            final ViewHolderMaximum maxHolder = (ViewHolderMaximum) holder;

            //TITLE
            maxHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
            maxHolder.title.setText(Html.fromHtml(a.getTitle()));

            maxHolder.topPanel.setOnClickListener(new ShowArticleCL(a, position));

            //Main image
            LinearLayout.LayoutParams paramsImg;
            if (a.getImageUrl() != null)
            {
                paramsImg = (LinearLayout.LayoutParams) maxHolder.img.getLayoutParams();

                float width = ctx.getResources().getDisplayMetrics().widthPixels;

                boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
                if (isGridManager)
                {
                    int numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(ctx.getString(R.string.pref_design_key_col_num), "2"));
                    width /= (float) numOfColsInGridLayoutManager;
                }
                //minusing paddings
                width -= DipToPx.convert(5 * 2, ctx);

                float scale = width / a.getImageWidth();
                float height = (scale) * a.getImageHeight();

                paramsImg.width = (int) width;
                paramsImg.height = (int) height;

                maxHolder.img.setLayoutParams(paramsImg);

                //fix runtime chnging images while scrolling
                maxHolder.img.setImageDrawable(null);
                imageLoader.displayImage(a.getImageUrl(), maxHolder.img);
            }
            else
            {
                maxHolder.img.setImageDrawable(null);
                paramsImg = (LinearLayout.LayoutParams) maxHolder.img.getLayoutParams();
                paramsImg.height = 0;
                maxHolder.img.setLayoutParams(paramsImg);
            }
            //overflowIcon
            View overflowParent = (View) maxHolder.overflow.getParent();
            LinearLayout.LayoutParams paramsOverflow;
            boolean showOverflow = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_more_actions), false);
            if (showOverflow)
            {
                paramsOverflow = (LinearLayout.LayoutParams) overflowParent.getLayoutParams();
                paramsOverflow.height = (int) DipToPx.convert(40, ctx);
                paramsOverflow.width = (int) DipToPx.convert(40, ctx);
                overflowParent.setLayoutParams(paramsOverflow);
                //TODO set onClick
                overflowParent.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.i(LOG, "overflowParent CLICKED!!!");
                    }
                });
            }
            else
            {
                paramsOverflow = (LinearLayout.LayoutParams) overflowParent.getLayoutParams();
                paramsOverflow.height = 0;
                paramsOverflow.width = 0;
                overflowParent.setLayoutParams(paramsOverflow);
                overflowParent.setOnClickListener(null);
            }
            //preview
            final FrameLayout.LayoutParams paramsPreview;
            paramsPreview = (FrameLayout.LayoutParams) maxHolder.preview.getLayoutParams();
            boolean showPreview = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_preview_show), false);
            if (showPreview)
            {
                ArrayList<Element> elements = HtmlParsing.getElementListFromHtml(a.getPreview());
                HtmlToView.add(maxHolder.preview, elements);

                //shorting preview field
                boolean shortPreview = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_preview_short), false);
                if (shortPreview)
                {
                    paramsPreview.height = (int) DipToPx.convert(100, ctx);
                    maxHolder.preview.setLayoutParams(paramsPreview);

                    maxHolder.previewCover.setVisibility(View.VISIBLE);
                    final int drawableId = AttributeGetter.getDrawableId(ctx, R.attr.cover_bottom_to_top);
                    maxHolder.bottomPanel.setBackgroundResource(drawableId);
                    final View.OnClickListener previewCoverCL = new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            boolean isFullHeight = paramsPreview.height == FrameLayout.LayoutParams.WRAP_CONTENT;
                            Log.i(LOG, "maxHolder.preview CLICKED!!! and isFullHeight: " + isFullHeight);
                            if (isFullHeight)
                            {
                                paramsPreview.height = (int) DipToPx.convert(100, ctx);
                                maxHolder.preview.setLayoutParams(paramsPreview);

                                maxHolder.previewCover.setVisibility(View.VISIBLE);
                                maxHolder.bottomPanel.setBackgroundResource(drawableId);
                            }
                            else
                            {
                                paramsPreview.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                                maxHolder.preview.setLayoutParams(paramsPreview);

                                maxHolder.previewCover.setVisibility(View.INVISIBLE);
                                maxHolder.bottomPanel.setBackgroundResource(android.R.color.transparent);
                            }
                        }
                    };
                    maxHolder.previewCover.setOnClickListener(previewCoverCL);
                }
                else
                {
                    maxHolder.previewCover.setVisibility(View.INVISIBLE);
                    maxHolder.bottomPanel.setBackgroundResource(android.R.color.transparent);

                    paramsPreview.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    maxHolder.preview.setLayoutParams(paramsPreview);
                }
            }
            else
            {
                if (maxHolder.preview.getChildCount() != 0)
                {
                    maxHolder.preview.removeAllViews();
                }
                paramsPreview.height = 0;
                maxHolder.preview.setLayoutParams(paramsPreview);

                maxHolder.preview.setOnClickListener(null);

                maxHolder.previewCover.setVisibility(View.INVISIBLE);
                maxHolder.bottomPanel.setBackgroundResource(android.R.color.transparent);
            }
            //date
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'в' HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//                Log.i(LOG, sdf.format(pubDate));//prints date in the format sdf
            maxHolder.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeSecondary);
            maxHolder.date.setText(sdf.format(a.getPubDate()));
        }
        else
        {
            ViewHolderMinimum minHolder = (ViewHolderMinimum) holder;
            minHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
            minHolder.title.setText(Html.fromHtml(a.getTitle()));
            minHolder.title.setOnClickListener(new ShowArticleCL(a, position));
        }
    }

    @Override
    public int getItemCount()
    {
        return artsList.size();
    }

    public static class ViewHolderMinimum extends RecyclerView.ViewHolder
    {
        public CardView cardView;
        public TextView title;

        public ViewHolderMinimum(View v)
        {
            super(v);
            cardView = (CardView) v;
            title = (TextView) v.findViewById(R.id.title);
        }
    }

    public static class ViewHolderMaximum extends RecyclerView.ViewHolder
    {
        public CardView cardView;
        public TextView title;
        public TextView date;
        public ImageView img;
        public ImageView overflow;

        public LinearLayout preview;

        public View previewCover;
        public LinearLayout bottomPanel;

        public LinearLayout topPanel;

        public ViewHolderMaximum(View v)
        {
            super(v);
            cardView = (CardView) v;
            title = (TextView) v.findViewById(R.id.title);
            date = (TextView) v.findViewById(R.id.date);
            img = (ImageView) v.findViewById(R.id.art_card_img);
            overflow = (ImageView) v.findViewById(R.id.actions);

            preview = (LinearLayout) v.findViewById(R.id.preview);
            previewCover = v.findViewById(R.id.preview_cover);
            bottomPanel = (LinearLayout) v.findViewById(R.id.bottom_panel);

            topPanel = (LinearLayout) v.findViewById(R.id.art_card_top_lin);
        }
    }

    class ShowArticleCL implements View.OnClickListener
    {
        Article a;
        int position;

        public ShowArticleCL(Article a, int position)
        {
            this.a = a;
            this.position = position;
        }

        @Override
        public void onClick(View v)
        {
            //need to switch by host activity and start Article activity or select art in ViewPager if we are on Article activity;
            Log.i(LOG, "title clicked: " + a.getUrl());
            if (!(ctx instanceof ActivityArticle))
            {
                Log.i(LOG, "clicked from activity: " + ctx.getClass().getSimpleName());
                Intent intent = new Intent(ctx, ActivityArticle.class);
                //paste arts and currently selected to intents extras
                Bundle b = new Bundle();
                b.putParcelableArrayList(Article.KEY_ARTICLES_LIST, artsList);
                b.putInt(ActivityArticle.KEY_CURRENT_ARTICLE_POSITION_IN_LIST, position);
                b.putString(ActivityArticle.KEY_CURRENT_CATEGORY_OR_TAG_URL, categoryOrTagUrl);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtras(b);
                ctx.startActivity(intent);
            }
            else
            {
                Log.i(LOG, "clicked from Article activity");
                //TODO
            }
        }
    }
}