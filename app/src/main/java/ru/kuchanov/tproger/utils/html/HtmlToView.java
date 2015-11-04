package ru.kuchanov.tproger.utils.html;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.nodes.Element;

import java.util.ArrayList;

import ru.kuchanov.tproger.utils.UILImageGetter;

/**
 * Created by Юрий on 03.11.2015 17:35.
 * For ExpListTest.
 */
public class HtmlToView
{

    static final String LOG = HtmlParsing.class.getSimpleName();

    public static void add(LinearLayout lin, ArrayList<Element> list)
    {
        Context ctx = lin.getContext();

        LinearLayout.LayoutParams linParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        lin.removeAllViews();

        boolean previousTagIsUnsupported = false;
        String prevHtml = "";

        for (int i = 0; i < list.size(); i++)
        {
            Element el = list.get(i);

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
                TextView textView = null;
                String html = "";

                if (previousTagIsUnsupported)
                {
                    if (i == list.size() - 1)
                    {
                        textView = new TextView(ctx);
                        textView.setLayoutParams(linParams);
                        lin.addView(textView);

                        html = el.toString();
                        setTextToTextView(lin, textView, html, ctx);
                        break;
                    }
                    Element nextEl = list.get(i + 1);
                    if ((HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl)))
                    {
                        textView = new TextView(ctx);
                        textView.setLayoutParams(linParams);
                        lin.addView(textView);

                        html = el.toString();
                        setTextToTextView(lin, textView, html, ctx);
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
                                textView.setLayoutParams(linParams);
                                lin.addView(textView);

                                setTextToTextView(lin, textView, html, ctx);
                                prevHtml = "";
                            }
                            else
                            {
                                textView = new TextView(ctx);
                                textView.setLayoutParams(linParams);
                                lin.addView(textView);

                                prevHtml = html;
                            }
                        }
                        else
                        {
                            textView = new TextView(ctx);
                            textView.setLayoutParams(linParams);
                            lin.addView(textView);

                            setTextToTextView(lin, textView, html, ctx);
                            break;
                        }
                    }
                    else
                    {
                        textView = (TextView) lin.getChildAt(lin.getChildCount() - 1);
                        html = prevHtml + el.toString();
                        if (i == list.size() - 1)
                        {
                            setTextToTextView(lin, textView, html, ctx);
                            break;
                        }
                        Element nextEl = list.get(i + 1);
                        if ((HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl)))
                        {
                            setTextToTextView(lin, textView, html, ctx);
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

    private static void setTextToTextView(LinearLayout lin, TextView textView, String textToSet, Context ctx)
    {
//        LinearLayout.LayoutParams linParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        textView.setLayoutParams(linParams);
//        lin.addView(textView);

        textToSet = textToSet.replaceAll("</p>", "");
        textToSet = textToSet.replaceAll("<p>", "<p></p>");

        textView.setText(
                Html.fromHtml(
                        textToSet, new UILImageGetter(textView, ctx), new MyHtmlTagHandler()));

        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        CharSequence text = textView.getText();
        if (text instanceof Spannable)
        {
            textView.setText(MakeLinksClicable.reformatText(text));
        }
    }
}


//else
//        {
//        TextView textView;
////                String html = (i==0 || previousTagIsUnsupported) ? el.toString() : list.get(i - 1).toString() + el.toString();
//        String html = "";
//        if (i == 0)
//        {
//        textView = new TextView(ctx);
//        textView.setLayoutParams(linParams);
//        lin.addView(textView);
//
//        html = el.toString();
//        html = html.replaceAll("</p>", "");
//        html = html.replaceAll("<p>", "<p></p>");
//
//        textView.setText(
//        Html.fromHtml(
//        html, new UILImageGetter(textView, ctx), new MyHtmlTagHandler()));
//
//        textView.setLinksClickable(true);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
//
//        CharSequence text = textView.getText();
//        if (text instanceof Spannable)
//        {
//        textView.setText(MakeLinksClicable.reformatText(text));
//        }
//        }
//        else if (previousTagIsUnsupported)
//        {
//        textView = new TextView(ctx);
//        textView.setLayoutParams(linParams);
//        lin.addView(textView);
//
//        html = el.toString();
//        html = html.replaceAll("</p>", "");
//        html = html.replaceAll("<p>", "<p></p>");
//
//        textView.setText(
//        Html.fromHtml(
//        html, new UILImageGetter(textView, ctx), new MyHtmlTagHandler()));
//
//        textView.setLinksClickable(true);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
//
//        CharSequence text = textView.getText();
//        if (text instanceof Spannable)
//        {
//        textView.setText(MakeLinksClicable.reformatText(text));
//        }
//        }
//        else
//        {
//        textView = (TextView) lin.getChildAt(lin.getChildCount() - 1);
//
////                    html = list.get(i - 1).toString();// + el.toString();
////                    String textToAppend=el.toString();
////                    textToAppend = textToAppend.replaceAll("</p>", "");
////                    textToAppend = textToAppend.replaceAll("<p>", "<p></p>");
////                    html+=textToAppend;
//
//        html = list.get(i - 1).toString() + el.toString();
//        html = html.replaceAll("</p>", "");
//        html = html.replaceAll("<p>", "<p></p>");
//
//        Log.i(LOG, html);
//
//        if (i == list.size() - 1)
//        {
//        textView.setText(
//        Html.fromHtml(
//        html, new UILImageGetter(textView, ctx), new MyHtmlTagHandler()));
//
//        textView.setLinksClickable(true);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
//
//        CharSequence text = textView.getText();
//        if (text instanceof Spannable)
//        {
//        textView.setText(MakeLinksClicable.reformatText(text));
//        }
//        break;
//        }
//        Element nextEl = list.get(i + 1);
//        if (HtmlTextFormatting.isUnsupportedTag(nextEl) || HtmlTextFormatting.hasInnerUnsupportedTags(nextEl))
//        {
//        continue;
//        }
//        else
//        {
//        textView.setText(
//        Html.fromHtml(
//        html, new UILImageGetter(textView, ctx), new MyHtmlTagHandler()));
//
//        textView.setLinksClickable(true);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
//
//        CharSequence text = textView.getText();
//        if (text instanceof Spannable)
//        {
//        textView.setText(MakeLinksClicable.reformatText(text));
//        }
//        }
//
//
//        }
////                TextView textView = new TextView(ctx);
////                textView.setLayoutParams(linParams);
////                lin.addView(textView);
////
////                String textToSet = el.toString();
////                textToSet = textToSet.replaceAll("</p>", "");
////                textToSet = textToSet.replaceAll("<p>", "<p></p>");
////
////                textView.setText(
////                        Html.fromHtml(
////                                textToSet, new UILImageGetter(textView, ctx), new MyHtmlTagHandler()));
////
////                textView.setLinksClickable(true);
////                textView.setMovementMethod(LinkMovementMethod.getInstance());
////
////                CharSequence text = textView.getText();
////                if (text instanceof Spannable)
////                {
////                    textView.setText(MakeLinksClicable.reformatText(text));
////                }
//
//        }
