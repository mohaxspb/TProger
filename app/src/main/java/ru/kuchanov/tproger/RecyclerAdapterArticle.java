package ru.kuchanov.tproger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.DipToPx;
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
    private static int paddingsInDp = 5;
    int sizeOfArticleParts = 0;
    float recyclerWidth;
    private ArrayList<HtmlToView.TextType> textTypes = new ArrayList<>();
    private ArrayList<String> listOfParts = new ArrayList<>();
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

        listOfParts = HtmlToView.getTextPartsList(HtmlParsing.getElementListFromHtml(article.getText()));


        recyclerWidth = ctx.getResources().getDisplayMetrics().widthPixels;
        if (isTabletMode)
        {
            //here we mast change width as there will be a drawer in left part of screen
            recyclerWidth = recyclerWidth / 3 * 2;
        }
        //minusing paddings
        recyclerWidth -= DipToPx.convert(paddingsInDp * 2, ctx);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder vh;

        View itemLayoutView;

        int windowBackgroundColor = AttributeGetter.getColor(ctx, android.R.attr.windowBackground);

        switch (viewType)
        {
            case TYPE_TITLE:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_title, parent, false);
                vh = new ViewHolderTitle(itemLayoutView);
                break;
            default:
            case TYPE_TEXT:
                TextView textView = new TextView(ctx);
                textView.setBackgroundColor(windowBackgroundColor);
                int padding = (int) DipToPx.convert(3, ctx);
                textView.setPadding(padding, 0, padding, 0);
                textView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                itemLayoutView = textView;
                vh = new ViewHolderText(itemLayoutView);
                break;
            case TYPE_WEB_VIEW:
                WebView webView = new WebView(ctx);
                webView.getSettings().setUseWideViewPort(true);
                webView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                webView.setBackgroundColor(windowBackgroundColor);
                itemLayoutView = webView;
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

        int textSizePrimary = AttributeGetter.getDimentionPixelSize(ctx, R.dimen.text_size_primary);
        int textSizeSecondary = AttributeGetter.getDimentionPixelSize(ctx, R.dimen.text_size_secondary);

        String currentHtml;

        switch (this.getItemViewType(position))
        {
            case TYPE_TITLE:
                final ViewHolderTitle holderTitle = (ViewHolderTitle) holder;
                //TITLE
                holderTitle.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
                holderTitle.title.setText(Html.fromHtml(article.getTitle()));
                //date
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'в' HH:mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//                Log.i(LOG, sdf.format(pubDate));//prints date in the format sdf
                holderTitle.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeSecondary);
                holderTitle.date.setText(sdf.format(article.getPubDate()));

                //image

                LinearLayout.LayoutParams paramsImg;
                if (article.getImageUrl() != null)
                {
                    paramsImg = (LinearLayout.LayoutParams) holderTitle.image.getLayoutParams();

                    float scale = recyclerWidth / article.getImageWidth();
                    float height = (scale) * article.getImageHeight();

                    paramsImg.width = (int) recyclerWidth;
                    paramsImg.height = (int) height;

                    holderTitle.image.setLayoutParams(paramsImg);

                    imageLoader.displayImage(article.getImageUrl(), holderTitle.image);
                }
                else
                {
                    holderTitle.image.setImageDrawable(null);
                    paramsImg = (LinearLayout.LayoutParams) holderTitle.image.getLayoutParams();
                    paramsImg.height = 0;
                    holderTitle.image.setLayoutParams(paramsImg);
                }
                //TODO test!
//                final String url = "http://cdn.tproger.ru/wp-content/uploads/2015/12/lena_optimized2.gif";
//                GifDecoderView gifDecoderView = new GifDecoderView(ctx, url);
//                holderTitle.root.addView(gifDecoderView);

                //fresco gif test
                Uri uri = Uri.parse("http://cdn.tproger.ru/wp-content/uploads/2015/12/lena_optimized2.gif");
                final SimpleDraweeView draweeView = new SimpleDraweeView(ctx);
                final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) recyclerWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

                draweeView.setLayoutParams(params);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .setControllerListener(new BaseControllerListener<ImageInfo>()
                        {
                            @Override
                            public void onFinalImageSet(
                                    String id,
                                    @Nullable ImageInfo imageInfo,
                                    @Nullable Animatable anim)
                            {
                                if (imageInfo == null)
                                {
                                    return;
                                }
                                //TODO check if size of image is less then containers size
                                //and if so - set draweeView size to size of image;
                                Log.i("Final image received!", "Size " + imageInfo.getWidth() + " x " + imageInfo.getHeight());
                                float scale = recyclerWidth / imageInfo.getWidth();
                                float height = scale * imageInfo.getHeight();
                                params.height = (int) height;
                                draweeView.setLayoutParams(params);
                            }

                            @Override
                            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo)
                            {
                                Log.i(LOG, "Intermediate image received");
                            }

                            @Override
                            public void onFailure(String id, Throwable throwable)
                            {
                                Log.e(LOG, "Error loading!");
                            }
                        })
                        .build();
                draweeView.setController(controller);
                holderTitle.root.addView(draweeView);
                break;
            default:
            case TYPE_TEXT:
                ViewHolderText holderText = (ViewHolderText) holder;

                //TODO
                currentHtml = this.listOfParts.get(position - 1);
                HtmlToView.setTextToTextView(holderText.text, currentHtml, ctx);
                break;
            case TYPE_WEB_VIEW:
                ViewHolderWebView holderWebView = (ViewHolderWebView) holder;
                currentHtml = this.listOfParts.get(position - 1);
                holderWebView.webView.loadDataWithBaseURL(null, currentHtml, "text/html", "UTF-8", null);
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
        public LinearLayout root;
        public ImageView image;
        public TextView title;
        public TextView date;

        public ViewHolderTitle(View v)
        {
            super(v);
            root = (LinearLayout) v;
            image = (ImageView) v.findViewById(R.id.img);
            date = (TextView) v.findViewById(R.id.date);
            title = (TextView) v.findViewById(R.id.title);
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