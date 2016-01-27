package ru.kuchanov.tproger.utils.html;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.nodes.Element;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.AttributeGetter;
import ru.kuchanov.tproger.utils.UILImageGetter;

/**
 * Created by Юрий on 03.11.2015 17:35.
 * For ExpListTest.
 */
public class HtmlToView
{
    protected static final String LOG = HtmlToView.class.getSimpleName();

    public static void add(LinearLayout lin, ArrayList<Element> list)
    {
        Context ctx = lin.getContext();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);

        int textColorPrimary = AttributeGetter.getColor(ctx, android.R.attr.textColorPrimary);

        LinearLayout.LayoutParams linParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        lin.removeAllViews();

        boolean previousTagIsUnsupported = false;
        String prevHtml = "";

        for (int i = 0; i < list.size(); i++)
        {
            Element el = list.get(i);
//            Log.i(LOG, el.toString());

            if (HtmlTextFormatting.isUnsupportedTag(el) || HtmlTextFormatting.hasInnerUnsupportedTags(el))
            {
                WebView webView;
                String html = prevHtml + ((i != 0 && !previousTagIsUnsupported)
                        ? el.toString() : list.get(i - 1).toString() + el.toString());
                if (previousTagIsUnsupported)
                {
                    webView = (WebView) lin.getChildAt(lin.getChildCount() - 1);
                }
                else
                {
                    webView = new WebView(ctx);
                    webView.setLayoutParams(linParams);
                    lin.addView(webView);
                }
                if (i == list.size() - 1)
                {
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    break;
                }
                Element nextEl = list.get(i + 1);
                if (HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl))
                {
                    prevHtml += html;
                    continue;
                }
                else
                {
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    prevHtml = "";
                }
            }
            else
            {
                TextView textView;
                String html;

                if (previousTagIsUnsupported)
                {
                    if (i == list.size() - 1)
                    {
                        textView = new TextView(ctx);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary));
                        textView.setTextColor(textColorPrimary);
                        textView.setLayoutParams(linParams);
                        lin.addView(textView);

                        html = el.toString();

                        setTextToTextView(textView, html, ctx);
                        break;
                    }
                    Element nextEl = list.get(i + 1);
                    if ((HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl)))
                    {
                        textView = new TextView(ctx);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary));
                        textView.setTextColor(textColorPrimary);
                        textView.setLayoutParams(linParams);
                        lin.addView(textView);

                        html = el.toString();
                        setTextToTextView(textView, html, ctx);
                        prevHtml = "";
                    }
                }
                else
                {
                    //prev is TExtView
                    if (i == 0)
                    {
                        html = prevHtml + el.toString();

                        if (list.size() != i + 1)
                        {
                            Element nextEl = list.get(i + 1);
                            if ((HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl)))
                            {
                                textView = new TextView(ctx);
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary));
                                textView.setTextColor(textColorPrimary);
                                textView.setLayoutParams(linParams);
                                lin.addView(textView);

                                setTextToTextView(textView, html, ctx);
                                prevHtml = "";
                            }
                            else
                            {
                                textView = new TextView(ctx);
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary));
                                textView.setTextColor(textColorPrimary);
                                textView.setLayoutParams(linParams);
                                lin.addView(textView);

                                prevHtml = html;
                            }
                        }
                        else
                        {
                            textView = new TextView(ctx);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary));
                            textView.setTextColor(textColorPrimary);
                            textView.setLayoutParams(linParams);
                            lin.addView(textView);

                            setTextToTextView(textView, html, ctx);
                            break;
                        }
                    }
                    else
                    {
                        textView = (TextView) lin.getChildAt(lin.getChildCount() - 1);
                        html = prevHtml + el.toString();
                        if (i == list.size() - 1)
                        {
                            setTextToTextView(textView, html, ctx);
                            break;
                        }
                        Element nextEl = list.get(i + 1);
                        if ((HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl)))
                        {
                            setTextToTextView(textView, html, ctx);
                            prevHtml = "";
                        }
                        else
                        {
                            prevHtml = html;
                        }
                    }
                }
            }
            previousTagIsUnsupported = (HtmlTextFormatting.isUnsupportedTag(el) || HtmlTextFormatting.hasInnerUnsupportedTags(el));
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
        textView.setText(
                Html.fromHtml(
                        textToSet, new UILImageGetter(textView, ctx), new MyHtmlTagHandler(ctx)));

//        textView.setText(
//                Html.fromHtml(
//                        textToSet, new ImageLoaderFresco(ctx, textView), new MyHtmlTagHandler(ctx)));

        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        CharSequence text = textView.getText();
        if (text instanceof Spannable)
        {
            textView.setText(MakeLinksClicable.reformatText(text));
        }
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