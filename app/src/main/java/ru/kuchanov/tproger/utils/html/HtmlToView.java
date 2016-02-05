package ru.kuchanov.tproger.utils.html;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

import org.jsoup.nodes.Element;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.fragment.FragmentDialogCodeRepresenter;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.DipToPx;
import ru.kuchanov.tproger.utils.UILImageGetter;

import static ru.kuchanov.tproger.RecyclerAdapterArticle.ViewHolderAccordeon;
import static ru.kuchanov.tproger.RecyclerAdapterArticle.ViewHolderCode;
import static ru.kuchanov.tproger.RecyclerAdapterArticle.ViewHolderPoll;
import static ru.kuchanov.tproger.RecyclerAdapterArticle.ViewHolderText;
import static ru.kuchanov.tproger.RecyclerAdapterArticle.ViewHolderWell;

/**
 * Created by Юрий on 03.11.2015 17:35 4:16.
 * For TProger.
 */
public class HtmlToView
{
    private static final String LOG = HtmlToView.class.getSimpleName();

    public static void add(LinearLayout parent, ArrayList<Element> elements)
    {
        parent.removeAllViews();

        final Context ctx = parent.getContext();

        ArrayList<TextType> textTypes = getTextPartSummary(elements);
        ArrayList<String> textParts = getTextPartsList(elements);

        int windowBackgroundColor = AttributeGetter.getColor(ctx, android.R.attr.windowBackground);
        int colorAccent = AttributeGetter.getColor(ctx, android.R.attr.colorAccent);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        final boolean isTabletMode = pref.getBoolean(ctx.getResources().getString(R.string.pref_design_key_tablet_mode), false);

        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);

        int textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        int textSizeSecondary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_secondary);

        float scaledTextSizePrimary = uiTextScale * textSizePrimary;
        float scaledTextSizeSecondary = uiTextScale * textSizeSecondary;

        for (int i = 0; i < textParts.size(); i++)
        {
            View itemLayoutView;

            String curHtml = textParts.get(i);
            //TODO test
//            curHtml = "<upgradedquote><blockquote>upgradedquoteupgradedquoteupgradedquoteupgradedquoteupgradedquoteupgradedquote\n" +
//                    "upgradedquoteupgradedquoteupgradedquoteupgradedquoteupgradedquoteupgradedquote</blockquote></upgradedquote>";
            curHtml = "<blockquote>upgradedquoteupgradedquoteupgradedq\n" +
                    "uoteupgradedquoteupgradedquoteupgradedquote\n" +
                    "upgradedquoteupgradedquoteupgradedquoteupgradedq\n" +
                    "uoteupgradedquoteupgradedquote</blockquote>";
            TextType curType = textTypes.get(i);

            switch (curType)
            {
                case Accordion:
                    itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_accordeon, parent, false);
                    final ViewHolderAccordeon holderAccordeon = new ViewHolderAccordeon(itemLayoutView);
                    final HtmlParsing.AccordionContent accordionContent = HtmlParsing.parseAccordion(curHtml);

                    int windowBackgroundDark = AttributeGetter.getColor(ctx, R.attr.windowBackgroundDark);
                    holderAccordeon.title.setBackgroundColor(windowBackgroundDark);
                    holderAccordeon.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
                    holderAccordeon.title.setText(accordionContent.getTitle());

                    final int arrowDown = AttributeGetter.getDrawableId(ctx, R.attr.arrowDownIcon);
                    final int arrowUp = AttributeGetter.getDrawableId(ctx, R.attr.arrowUpIcon);

                    final LinearLayout.LayoutParams paramsImage = (LinearLayout.LayoutParams) holderAccordeon.image.getLayoutParams();

                    float recyclerWidth = ctx.getResources().getDisplayMetrics().widthPixels;
                    int paddingsInDp = 5;
                    if (isTabletMode)
                    {
                        //here we mast change width as there will be a drawer in left part of screen
                        recyclerWidth = recyclerWidth / 3 * 2;
                    }
                    //minusing paddings
                    recyclerWidth -= DipToPx.convert(paddingsInDp * 2, ctx);

                    final float finalRecyclerWidth = recyclerWidth;

                    holderAccordeon.title.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (paramsImage.height == 0)
                            {

                                float scale = finalRecyclerWidth / accordionContent.getImgWidth();
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
                case Table:
                    itemLayoutView = new TextView(ctx);
                    //TODO
                    break;
                case Well:
                    itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_well, parent, false);
                    ViewHolderWell holderWell = new ViewHolderWell(itemLayoutView);
                    holderWell.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
                    holderWell.textView.setText(Html.fromHtml(curHtml,
                            new UILImageGetter(holderWell.textView, ctx),
                            new MyHtmlTagHandler(ctx)));
                    break;
                case Code:
                    itemLayoutView = LayoutInflater.from(ctx).inflate(R.layout.recycler_item_code_representer_main, parent, false);
                    ViewHolderCode holderCode = new ViewHolderCode(itemLayoutView);

                    holderCode.content.removeAllViews();

                    final CodeRepresenter codeRepresenter = CodeRepresenter.parseTableForCodeLines(ctx, curHtml);

                    for (int countenr = 0; countenr < codeRepresenter.getLines().size(); countenr++)
                    {
                        String codeLine = codeRepresenter.getLines().get(countenr);
//                    Log.i(LOG, codeLine);
                        LinearLayout codeLineLayout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.recycler_item_code_representer_code_line, holderCode.content, false);

                        TextView lineNumber = (TextView) codeLineLayout.findViewById(R.id.line_number);
                        String number = " " + countenr + " ";
                        lineNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizeSecondary);
                        lineNumber.setText(number);

                        TextView lineCode = (TextView) codeLineLayout.findViewById(R.id.line_code);
                        lineCode.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizeSecondary);
                        lineCode.setText(Html.fromHtml(codeLine));

                        //each second line must have darker color
                        if (countenr % 2 != 0)
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
                case Poll:
                    //TODO
                    itemLayoutView = LayoutInflater.from(ctx).inflate(R.layout.article_poll, parent, false);
                    ViewHolderPoll holderPoll = new ViewHolderPoll(itemLayoutView);
                    Log.d(LOG, "type POLL!");
                    break;
                default:
                case Text:
                    TextView textView = new TextView(ctx);
                    int padding = (int) DipToPx.convert(3, ctx);
                    textView.setPadding(padding, 0, padding, 0);
                    textView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                    itemLayoutView = textView;
                    ViewHolderText holderText = new ViewHolderText(itemLayoutView);
                    //TODO test quotes
                    Spanned spanned = Html.fromHtml(curHtml, new UILImageGetter(holderText.text, ctx), new MyHtmlTagHandler(ctx));
                    MakeLinksClicable.reformatText(spanned);
                    holderText.text.setText(spanned);
//                    holderText.text.setText(Html.fromHtml(curHtml, new UILImageGetter(holderText.text, ctx), new MyHtmlTagHandler(ctx)));
                    break;
            }
            parent.addView(itemLayoutView);
        }
    }

    /**
     * sets text to TextView via Html.fromHtml <br>
     * also makes lins clickable and sets a listener for clicks <br>
     * also ads imageLoader for images inside text <br>
     * also adds supporting for list tags
     */
    public static void setTextToTextView(TextView textView, String textToSet, Context ctx)
    {
//        Log.i(LOG, textToSet);
        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        //TODO test qoute
        Spanned spannable = Html.fromHtml(textToSet, new UILImageGetter(textView, ctx), new MyHtmlTagHandler(ctx));
        MakeLinksClicable.reformatText(spannable);

        textView.setText(spannable);



//        CharSequence text = textView.getText();
//        if (text instanceof Spannable)
//        {
//            textView.setText(MakeLinksClicable.reformatText(text));
//        }
    }

    public static ArrayList<TextType> getTextPartSummary(ArrayList<Element> list)
    {
        ArrayList<TextType> listOfTypes = new ArrayList<>();

        for (Element el : list)
        {
            listOfTypes.add(HtmlTextFormatting.tagType(el));
        }
//        for (TextType type : listOfTypes)
//        {
//            Log.i(LOG, type.toString());
//        }

        return listOfTypes;
    }

    public static ArrayList<String> getTextPartsList(ArrayList<Element> list)
    {
        ArrayList<String> listOfTextParts = new ArrayList<>();

        for (Element el : list)
        {
            listOfTextParts.add(el.toString());
        }

        return listOfTextParts;
    }

    public enum TextType
    {
        Code, Text, Accordion, Poll, Table, Well
    }
}