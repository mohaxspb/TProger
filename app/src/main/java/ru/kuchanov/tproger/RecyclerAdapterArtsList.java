package ru.kuchanov.tproger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.DipToPx;
import ru.kuchanov.tproger.utils.HtmlTextFormatting;
import ru.kuchanov.tproger.utils.MakeLinksClicable;
import ru.kuchanov.tproger.utils.MyHtmlTagHandler;
import ru.kuchanov.tproger.utils.MyUIL;
import ru.kuchanov.tproger.utils.UILImageGetter;

public class RecyclerAdapterArtsList extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final String LOG = RecyclerAdapterArtsList.class.getSimpleName();
    //    boolean showMaxInfo;
    private ArrayList<Article> artsList;
    private SharedPreferences pref;
    private Context ctx;
    private ImageLoader imageLoader;

    public RecyclerAdapterArtsList(Context ctx, ArrayList<Article> dataset)
    {
        this.ctx = ctx;
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
//        showMaxInfo = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_style), false);

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

        boolean showMaxInfo = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_style), false);
        if (showMaxInfo)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_max, parent, false);
            vh = new ViewHolderMaximum(v);
            return vh;
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_min, parent, false);
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
        return (showMaxInfo) ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        Article a = artsList.get(position);
        boolean showMaxInfo = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_style), false);
        if (showMaxInfo)
        {
            ViewHolderMaximum maxHolder = (ViewHolderMaximum) holder;
            maxHolder.title.setText(a.getTitle());

            //Main image
            LinearLayout.LayoutParams paramsImg;
            if (a.getImageUrl() != null)
            {
                paramsImg = (LinearLayout.LayoutParams) maxHolder.img.getLayoutParams();

                int widthDevice = ctx.getResources().getDisplayMetrics().widthPixels;
                float width = widthDevice;

                boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);
                if (isGridManager)
                {
                    int numOfColsInGridLayoutManager = Integer.parseInt(pref.getString(ctx.getString(R.string.pref_design_key_col_num), "2"));
                    width /= (float) numOfColsInGridLayoutManager;
                }

                float scale = width / a.getImageWidth();
                float height = (scale) * a.getImageHeight();

                paramsImg.width = (int) width;
                paramsImg.height = (int) height;

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
            LinearLayout.LayoutParams paramsPreview;
            boolean showPreview = pref.getBoolean(ctx.getString(R.string.pref_design_key_art_card_preview_show), false);
            if (showPreview)
            {
                if (HtmlTextFormatting.hasUnsupportedTags(a.getPreview()))
                {
                    paramsPreview = (LinearLayout.LayoutParams) maxHolder.preview.getLayoutParams();
                    paramsPreview.height = 0;
                    maxHolder.preview.setLayoutParams(paramsPreview);

                    WebView webView = new WebView(ctx);

                    int indexOfPreview = maxHolder.mainLin.indexOfChild(maxHolder.preview);
                    maxHolder.mainLin.addView(webView, indexOfPreview);

                    LinearLayout.LayoutParams webParams = (LinearLayout.LayoutParams) webView.getLayoutParams();
                    webParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    webParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    webView.setLayoutParams(webParams);

                    Log.i(LOG, a.getPreview());

//                    webView.loadData(a.getPreview(), "text/html", "utf-8");
                    webView.loadDataWithBaseURL(null, a.getPreview(), "text/html", "UTF-8", null);
                }
                else
                {
                    paramsPreview = (LinearLayout.LayoutParams) maxHolder.preview.getLayoutParams();
                    paramsPreview.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    maxHolder.preview.setLayoutParams(paramsPreview);

                    maxHolder.preview.setText(
                            Html.fromHtml(
                                    a.getPreview(), new UILImageGetter(maxHolder.preview, ctx), new MyHtmlTagHandler()));

                    maxHolder.preview.setLinksClickable(true);
                    maxHolder.preview.setMovementMethod(LinkMovementMethod.getInstance());

                    CharSequence text = maxHolder.preview.getText();
                    if (text instanceof Spannable)
                    {
                        int end = text.length();
                        Spannable sp = (Spannable) maxHolder.preview.getText();
                        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                        SpannableStringBuilder style = new SpannableStringBuilder(text);
                        //					style.clearSpans();//should clear old spans
                        for (URLSpan url : urls)
                        {
                            style.removeSpan(url);
                            MakeLinksClicable.CustomerTextClick click = new MakeLinksClicable.CustomerTextClick(url.getURL());
                            style.setSpan(click, sp.getSpanStart(url), sp.getSpanEnd(url),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        maxHolder.preview.setText(style);
                    }
                }

            }
            else
            {
                paramsPreview = (LinearLayout.LayoutParams) maxHolder.preview.getLayoutParams();
                paramsPreview.height = 0;
                maxHolder.preview.setLayoutParams(paramsPreview);

                boolean hasWebView = false;
                View webView=null;
                for (int i = 0; i < maxHolder.mainLin.getChildCount() && !hasWebView; i++)
                {
                    View v = maxHolder.mainLin.getChildAt(i);

                    hasWebView = v instanceof WebView;
                    webView=v;
                }
                if(hasWebView)
                {
                    maxHolder.mainLin.removeView(webView);
                }

            }
            //date
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'в' HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//                Log.i(LOG, sdf.format(pubDate));//prints date in the format sdf
            maxHolder.date.setText(sdf.format(a.getPubDate()));
        }
        else
        {
            ViewHolderMinimum minHolder = (ViewHolderMinimum) holder;
            minHolder.title.setText(a.getTitle());
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
        public TextView date;
        public ImageView img;
        public ImageView overflow;
        public TextView preview;

        public LinearLayout mainLin;

        public ViewHolderMaximum(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            date = (TextView) v.findViewById(R.id.date);
            img = (ImageView) v.findViewById(R.id.art_card_img);
            overflow = (ImageView) v.findViewById(R.id.actions);
            preview = (TextView) v.findViewById(R.id.preview);
            mainLin = (LinearLayout) v.findViewById(R.id.art_card_main_lin);
        }
    }
}
