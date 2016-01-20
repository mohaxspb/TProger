package ru.kuchanov.tproger.utils.html;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.AttributeGetter;


public class MyHtmlTagHandler implements TagHandler
{
    //    public static String FOUR_NON_BREAKED_SPACES = "&nbsp;&nbsp;&nbsp;&nbsp;";
    boolean first = true;
    String parent = null;
    int index = 1;

    Context ctx;

    public MyHtmlTagHandler(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
    {
        //make supporting background color via BackgroundColorSpan (use it for CODE TAG)
        if (tag.equalsIgnoreCase("code"))
        {
            processCode(opening, output);
        }
        if (tag.equalsIgnoreCase("strike") || tag.equals("s"))
        {
            processStrike(opening, output);
        }
        ////////
        if (tag.equals("ul"))
        {
            parent = "ul";
        }
        else if (tag.equals("ol"))
        {
            parent = "ol";
        }
        if (tag.equals("li"))
        {
            if (parent.equals("ul"))
            {
                if (first)
                {
                    output.append("\n\t• ");
                    first = false;
                }
                else
                {
                    first = true;
                }
            }
            else
            {
                if (first)
                {
                    output.append("\n\t").append(String.valueOf(index)).append(". ");
                    first = false;
                    index++;
                }
                else
                {
                    first = true;
                }
            }
        }
    }

    private void processStrike(boolean opening, Editable output)
    {
        int len = output.length();
        if (opening)
        {
            output.setSpan(new StrikethroughSpan(), len, len, Spanned.SPAN_MARK_MARK);
        }
        else
        {
            Object obj = getLast(output, StrikethroughSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len)
            {
                output.setSpan(new StrikethroughSpan(), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private Object getLast(Editable text, Class<?> kind)
    {
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0)
        {
            return null;
        }
        else
        {
            for (int i = objs.length; i > 0; i--)
            {
                if (text.getSpanFlags(objs[i - 1]) == Spanned.SPAN_MARK_MARK)
                {
                    return objs[i - 1];
                }
            }
            return null;
        }
    }

    private void processCode(boolean opening, Editable output)
    {
        int len = output.length();

        int windowBackgroundDark = AttributeGetter.getColor(ctx, R.attr.windowBackgroundDark);

        if (opening)
        {
            output.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.material_red_500)), len, len, Spanned.SPAN_MARK_MARK);
            output.setSpan(new BackgroundColorSpan(windowBackgroundDark), len, len, Spanned.SPAN_MARK_MARK);

        }
        else
        {
            Object obj = getLast(output, BackgroundColorSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len)
            {
                output.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.material_red_500)), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                output.setSpan(new BackgroundColorSpan(windowBackgroundDark), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}