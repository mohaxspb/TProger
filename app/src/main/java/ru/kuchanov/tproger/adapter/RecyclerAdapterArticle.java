package ru.kuchanov.tproger.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.fragment.FragmentDialogCodeRepresenter;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.DipToPx;
import ru.kuchanov.tproger.utils.UILImageGetter;
import ru.kuchanov.tproger.utils.html.CodeRepresenter;
import ru.kuchanov.tproger.utils.html.HtmlParsing;
import ru.kuchanov.tproger.utils.html.HtmlToView;
import ru.kuchanov.tproger.utils.html.MyHtmlTagHandler;

public class RecyclerAdapterArticle extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
//    private static final String LOG = RecyclerAdapterArticle.class.getSimpleName();

    private static final int TYPE_TITLE = 0;
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_CODE = 2;
    private static final int TYPE_TAGS = 3;
    private static final int TYPE_TO_READ_MORE = 4;
    private static final int TYPE_COMMENTS = 5;
    private static final int TYPE_ACCORDION = 6;
    private static final int TYPE_POLL = 7;
    private static final int TYPE_GALLERY = 8;
    private static final int TYPE_TABLE = 9;
    private static final int TYPE_WELL = 10;

    private int sizeOfArticleParts = 0;
    private float recyclerWidth;
    private ArrayList<HtmlToView.TextType> textTypes = new ArrayList<>();
    private ArrayList<String> listOfParts = new ArrayList<>();
    private SharedPreferences pref;
    private Article article;
    private Context ctx;
    private int arrowUp;
    private int arrowDown;

    public RecyclerAdapterArticle(Context ctx, Article article)
    {
        this.ctx = ctx;
        this.article = article;
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        this.textTypes = HtmlToView.getTextPartSummary(HtmlParsing.getElementListFromHtml(article.getText()));

        sizeOfArticleParts = 1; //for title
        sizeOfArticleParts += 1; //for comments
        sizeOfArticleParts += 1; //for tags
        sizeOfArticleParts += 1; //for toReadMore
        sizeOfArticleParts += textTypes.size(); //for artText

        listOfParts = HtmlToView.getTextPartsList(HtmlParsing.getElementListFromHtml(article.getText()));

        recyclerWidth = ctx.getResources().getDisplayMetrics().widthPixels;
        //minusing paddings
        recyclerWidth -= DipToPx.convert((int) (ctx.getResources().getDimension(R.dimen.recycler_padding) /
                        ctx.getResources().getDisplayMetrics().density * 2),
                ctx);

        arrowDown = AttributeGetter.getDrawableId(ctx, R.attr.arrowDownIcon);
        arrowUp = AttributeGetter.getDrawableId(ctx, R.attr.arrowUpIcon);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder vh;

        View itemLayoutView;

        switch (viewType)
        {
            case TYPE_TITLE:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_article_title, parent, false);
                vh = new ViewHolderTitle(itemLayoutView);
                break;
            case TYPE_CODE:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_article_code_representer_main, parent, false);
                vh = new ViewHolderCode(itemLayoutView);
                break;
            case TYPE_ACCORDION:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_article_accordeon, parent, false);
                vh = new ViewHolderAccordeon(itemLayoutView);
                break;
            case TYPE_POLL:
                //TODO
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_article_poll, parent, false);
                vh = new ViewHolderPoll(itemLayoutView);
                break;
            case TYPE_WELL:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_article_well, parent, false);
                vh = new ViewHolderWell(itemLayoutView);
                break;
            case TYPE_TABLE:
                itemLayoutView = new TextView(ctx);
                vh = new ViewHolderTags(itemLayoutView);
                //TODO
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
            default:
            case TYPE_TEXT:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_article_text, parent, false);
                vh = new ViewHolderText(itemLayoutView);
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
                case Code:
                    return TYPE_CODE;
                case Accordion:
                    return TYPE_ACCORDION;
                case Well:
                    return TYPE_WELL;
                case Poll:
                    return TYPE_POLL;
                case Table:
                    return TYPE_TABLE;
                default:
                case Text:
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position)
    {
        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);
        float artTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_article), 0.75f);

        int textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        int textSizeSecondary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_secondary);

        float scaledTextSizePrimary = artTextScale * textSizePrimary;
//        Log.i(LOG, "textSizePrimary: "+textSizePrimary);
//        Log.i(LOG, "textSizeSecondary: "+textSizeSecondary);

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
                break;
            default:
            case TYPE_TEXT:
                ViewHolderText holderText = (ViewHolderText) holder;

                currentHtml = this.listOfParts.get(position - 1);
//                Log.e(LOG, currentHtml);

                holderText.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
                HtmlToView.setTextToTextView(holderText.text, currentHtml, ctx);
                break;
            case TYPE_CODE:
                ViewHolderCode holderCode = (ViewHolderCode) holder;
                //remove all childView to prevent repeating
                holderCode.content.removeAllViews();

                currentHtml = this.listOfParts.get(position - 1);

                final CodeRepresenter codeRepresenter = CodeRepresenter.parseTableForCodeLines(ctx, currentHtml);

                for (int i = 0; i < codeRepresenter.getLines().size(); i++)
                {
                    String codeLine = codeRepresenter.getLines().get(i);
//                    Log.i(LOG, codeLine);
                    LinearLayout codeLineLayout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.recycler_article_code_representer_code_line, holderCode.content, false);

                    TextView lineNumber = (TextView) codeLineLayout.findViewById(R.id.line_number);
                    String lineNumberString = String.valueOf(i + 1);
                    String additionalSpacesAfterLineNumber = "";
                    String finalLineNumber = String.valueOf(codeRepresenter.getLines().size());
                    for (int u = lineNumberString.length(); u < finalLineNumber.length(); u++)
                    {
                        if (additionalSpacesAfterLineNumber.length() < finalLineNumber.length())
                        {
                            additionalSpacesAfterLineNumber += "  ";
                        }
                    }
                    String number = " " + lineNumberString + " " + additionalSpacesAfterLineNumber;
                    lineNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
                    lineNumber.setText(number);

                    TextView lineCode = (TextView) codeLineLayout.findViewById(R.id.line_code);
                    lineCode.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
                    lineCode.setText(Html.fromHtml(codeLine));

                    //each second line must have darker color
                    if (i % 2 != 0)
                    {
                        codeLineLayout.setBackgroundResource(R.color.material_teal_200);
                        lineNumber.setBackgroundResource(R.color.material_teal_400);
                        lineCode.setBackgroundResource(R.color.material_teal_200);
                    }

                    holderCode.content.addView(codeLineLayout);
                }
                //add clickListener to btns
                holderCode.copy.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        StringBuilder builder = new StringBuilder();
                        for (String codeLine : codeRepresenter.getLines())
                        {
                            builder.append(Html.fromHtml(codeLine).toString()).append("\n");
                        }
                        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("code", builder.toString());
                        clipboard.setPrimaryClip(clip);
                    }
                });
                holderCode.show.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ArrayList<String> lines = codeRepresenter.getLines();
                        FragmentDialogCodeRepresenter fragmentDialogCodeRepresenter = FragmentDialogCodeRepresenter.newInstance(lines);
                        fragmentDialogCodeRepresenter.show(((AppCompatActivity) ctx).getFragmentManager(), FragmentDialogCodeRepresenter.LOG);
                    }
                });
                break;
            case TYPE_TAGS:
                //TODO
                break;
            case TYPE_TO_READ_MORE:
                //TODO
                break;
            case TYPE_COMMENTS:
                //TODO
                break;
            case TYPE_ACCORDION:
                final ViewHolderAccordeon holderAccordeon = (ViewHolderAccordeon) holder;
                String accrodionHtml = this.listOfParts.get(position - 1);
                final HtmlParsing.AccordionContent accordionContent = HtmlParsing.parseAccordion(accrodionHtml);

                int windowBackgroundDark = AttributeGetter.getColor(ctx, R.attr.windowBackgroundDark);
                holderAccordeon.title.setBackgroundColor(windowBackgroundDark);
                holderAccordeon.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
                holderAccordeon.title.setText(accordionContent.getTitle());

                final LinearLayout.LayoutParams paramsImage = (LinearLayout.LayoutParams) holderAccordeon.image.getLayoutParams();

                holderAccordeon.title.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (paramsImage.height == 0)
                        {
                            float scale = recyclerWidth / accordionContent.getImgWidth();
                            float height = scale * accordionContent.getImgHeight();
                            paramsImage.height = (int) height;
                            holderAccordeon.image.setLayoutParams(paramsImage);

                            holderAccordeon.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, arrowUp, 0);
                        }
                        else
                        {
                            paramsImage.height = 0;
                            holderAccordeon.image.setLayoutParams(paramsImage);

                            holderAccordeon.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, arrowDown, 0);
                        }
                    }
                });
                //fresco gif
                Uri uri = Uri.parse(accordionContent.getImageUrl());
                final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) recyclerWidth, 0);

                holderAccordeon.image.setLayoutParams(params);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .build();
                holderAccordeon.image.setController(controller);
                break;
            case TYPE_POLL:
                //TODO
                break;
            case TYPE_GALLERY:
                //TODO
                break;
            case TYPE_TABLE:
                //TODO
                break;
            case TYPE_WELL:
                final ViewHolderWell holderWell = (ViewHolderWell) holder;
                holderWell.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
                holderWell.textView.setText(Html.fromHtml(listOfParts.get(position - 1),
                        new UILImageGetter(holderWell.textView, ctx),
                        new MyHtmlTagHandler(ctx)));
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return this.sizeOfArticleParts;
    }

    public static class ViewHolderTitle extends RecyclerView.ViewHolder
    {
        public LinearLayout root;
        public TextView title;
        public TextView date;

        public ViewHolderTitle(View v)
        {
            super(v);
            root = (LinearLayout) v;
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

    public static class ViewHolderCode extends RecyclerView.ViewHolder
    {
        public LinearLayout root;
        public LinearLayout content;
        public Button show;
        public Button copy;

        public ViewHolderCode(View v)
        {
            super(v);
            root = (LinearLayout) v;
            content = (LinearLayout) v.findViewById(R.id.content);
            show = (Button) v.findViewById(R.id.show);
            copy = (Button) v.findViewById(R.id.copy);
        }
    }

    //TODO
    public static class ViewHolderTags extends RecyclerView.ViewHolder
    {

        public ViewHolderTags(View v)
        {
            super(v);
        }
    }

    //TODO
    public static class ViewHolderToReadMore extends RecyclerView.ViewHolder
    {

        public ViewHolderToReadMore(View v)
        {
            super(v);
        }
    }

    //TODO
    public static class ViewHolderComments extends RecyclerView.ViewHolder
    {

        public ViewHolderComments(View v)
        {
            super(v);
        }
    }

    //TODO
    public static class ViewHolderAccordeon extends RecyclerView.ViewHolder
    {
        public LinearLayout root;
        public TextView title;
        public SimpleDraweeView image;

        public ViewHolderAccordeon(View v)
        {
            super(v);
            root = (LinearLayout) v;
            image = (SimpleDraweeView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
        }
    }

    //TODO
    public static class ViewHolderPoll extends RecyclerView.ViewHolder
    {

        public ViewHolderPoll(View v)
        {
            super(v);
        }
    }

    //TODO
    public static class ViewHolderGallery extends RecyclerView.ViewHolder
    {

        public ViewHolderGallery(View v)
        {
            super(v);
        }
    }

    //TODO
    public static class ViewHolderTable extends RecyclerView.ViewHolder
    {

        public ViewHolderTable(View v)
        {
            super(v);
        }
    }

    public static class ViewHolderWell extends RecyclerView.ViewHolder
    {
        public CardView card;
        public TextView textView;

        public ViewHolderWell(View v)
        {
            super(v);
            card = (CardView) v.findViewById(R.id.cardView);
            textView = (TextView) v.findViewById(R.id.text_view);
        }
    }
}