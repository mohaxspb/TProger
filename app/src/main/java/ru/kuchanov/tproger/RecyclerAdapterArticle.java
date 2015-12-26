package ru.kuchanov.tproger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.MyUIL;
import ru.kuchanov.tproger.utils.html.HtmlParsing;
import ru.kuchanov.tproger.utils.html.HtmlToView;

public class RecyclerAdapterArticle extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final String LOG = RecyclerAdapterArticle.class.getSimpleName();

    private static final int TYPE_TITLE = 0;
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_WEB_VIEW = 2;
    private static final int TYPE_TAGS = 3;
    private static final int TYPE_TO_READ_MORE = 4;
    private static final int TYPE_COMMENTS = 5;

    int sizeOfArticleParts = 0;
    private ArrayList<HtmlToView.TextType> textTypes = new ArrayList<>();

    private SharedPreferences pref;
    private Article article;
    private Context ctx;
    private ImageLoader imageLoader;
    private boolean isTabletMode;

    public RecyclerAdapterArticle(Context ctx, Article article)
    {
        this.ctx = ctx;
        this.article = article;
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.isTabletMode = pref.getBoolean(ctx.getResources().getString(R.string.pref_design_key_tablet_mode), false);

        imageLoader = MyUIL.get(ctx);

        this.textTypes = HtmlToView.getTextPartSummary(HtmlParsing.getElementListFromHtml(article.getText()));

        sizeOfArticleParts = 1; //for title
        sizeOfArticleParts += 1; //for comments
        sizeOfArticleParts += 1; //for tags
        sizeOfArticleParts += 1; //for toReadMore
        sizeOfArticleParts += textTypes.size(); //for artText
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder vh;

        View itemLayoutView;

        switch (viewType)
        {
            case TYPE_TITLE:
                itemLayoutView = new TextView(ctx);
                vh = new ViewHolderTitle(itemLayoutView);
                //TODO
                break;
            default:
            case TYPE_TEXT:
                itemLayoutView = new TextView(ctx);
                vh = new ViewHolderText(itemLayoutView);
                break;
            case TYPE_WEB_VIEW:
                itemLayoutView = new WebView(ctx);
                vh = new ViewHolderWebView(itemLayoutView);
                break;
            case TYPE_TAGS:
                itemLayoutView = new TextView(ctx);
                vh = new ViewHolderTags(itemLayoutView);
                //TODO
                break;
            case TYPE_TO_READ_MORE:
                itemLayoutView = new TextView(ctx);
                vh = new ViewHolderToReadMore(itemLayoutView);
                //TODO
                break;
            case TYPE_COMMENTS:
                itemLayoutView = new TextView(ctx);
                vh = new ViewHolderComments(itemLayoutView);
                //TODO
                break;
        }

        return vh;
    }

    @Override
    public int getItemViewType(int position)
    {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        if (position == 0)
        {
            return TYPE_TITLE;
        }

        if (position < textTypes.size() + 1)
        {
            int positionInTypesList = position - 1;

            HtmlToView.TextType curType = this.textTypes.get(positionInTypesList);
            switch (curType)
            {
                case Table:
                    return TYPE_WEB_VIEW;
                case Text:
                default:
                    return TYPE_TEXT;
            }
        }
        if (position == sizeOfArticleParts - 1)
        {
            return TYPE_COMMENTS;
        }
        else if (position == sizeOfArticleParts - 2)
        {
            return TYPE_TO_READ_MORE;
        }
        else if (position == sizeOfArticleParts - 3)
        {
            return TYPE_TAGS;
        }
        //can be, but we must return something
        return TYPE_TEXT;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);
        float artTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size), 0.75f);


        switch (this.getItemViewType(position))
        {
            case TYPE_TITLE:
                ViewHolderTitle holderTitle = (ViewHolderTitle) holder;
                holderTitle.title.setText(article.getTitle());
                break;
            default:
            case TYPE_TEXT:
                ViewHolderText holderText = (ViewHolderText) holder;
                holderText.text.setText(article.getText());
                break;
            case TYPE_WEB_VIEW:

                break;
            case TYPE_TAGS:

                break;
            case TYPE_TO_READ_MORE:

                break;
            case TYPE_COMMENTS:

                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return this.sizeOfArticleParts;
    }

    //TODO
    public static class ViewHolderTitle extends RecyclerView.ViewHolder
    {
        public TextView title;

        public ViewHolderTitle(View v)
        {
            super(v);
            title = (TextView) v;
//            title = (TextView) v.findViewById(R.id.title);
        }
    }

    public static class ViewHolderText extends RecyclerView.ViewHolder
    {
        public TextView text;

        public ViewHolderText(View v)
        {
            super(v);
            text = (TextView) v;
        }
    }

    public static class ViewHolderWebView extends RecyclerView.ViewHolder
    {
        public WebView webView;

        public ViewHolderWebView(View v)
        {
            super(v);
            webView = (WebView) v;
        }
    }

    //TODO
    public static class ViewHolderTags extends RecyclerView.ViewHolder
    {
//        public WebView webView;

        public ViewHolderTags(View v)
        {
            super(v);
//            webView = (WebView) v;
        }
    }

    //TODO
    public static class ViewHolderToReadMore extends RecyclerView.ViewHolder
    {
//        public WebView webView;

        public ViewHolderToReadMore(View v)
        {
            super(v);
//            webView = (WebView) v;
        }
    }

    //TODO
    public static class ViewHolderComments extends RecyclerView.ViewHolder
    {
//        public WebView webView;

        public ViewHolderComments(View v)
        {
            super(v);
//            webView = (WebView) v;
        }
    }
}