package ru.kuchanov.tproger.utils.html;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;

import org.xml.sax.XMLReader;

import java.util.Stack;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.AttributeGetter;


public class MyHtmlTagHandler implements TagHandler
{
    private static final String LOG = MyHtmlTagHandler.class.getSimpleName();
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);
    Context ctx;
    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    Stack<String> lists = new Stack<>();

    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    Stack<Integer> olNextIndex = new Stack<>();

    public MyHtmlTagHandler(Context ctx)
    {
        this.ctx = ctx;
    }

    /**
     * @see android.text.Html
     */
    private static void start(Editable text, Object mark)
    {
        int len = text.length();
        text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
    }

    /**
     * Modified from {@link android.text.Html}
     */
    private static void end(Editable text, Class<?> kind, Object... replaces)
    {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);
        text.removeSpan(obj);
        if (where != len)
        {
            for (Object replace : replaces)
            {
                text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * @see android.text.Html
     */
    private static Object getLast(Spanned text, Class<?> kind)
    {
        /*
         * This knows that the last returned object from getSpans()
		 * will be the most recently added.
		 */
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0)
        {
            return null;
        }
        return objs[objs.length - 1];
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
    {
        if (tag.equalsIgnoreCase("html") || tag.equalsIgnoreCase("body"))
        {
            return;
        }
        //make supporting background color via BackgroundColorSpan (use it for CODE TAG)
        if (tag.equalsIgnoreCase("code"))
        {
            processCode(opening, output);
        }
        if (tag.equalsIgnoreCase("strike") || tag.equals("s"))
        {
            processStrike(opening, output);
        }

        if (tag.equalsIgnoreCase("ul") || tag.equalsIgnoreCase("ol") || tag.equalsIgnoreCase("li"))
        {
            processUlOlLi(opening, tag, output);
        }
        else
        {
            Log.e(LOG, "Unknown tag in TagHandler with name: " + tag);
        }
    }

    /**
     * @link https://bitbucket.org/Kuitsi/android-textview-html-list/src/c866e64acc3336890cfde00fae2e59565fe0c1bf/app/src/main/java/fi/iki/kuitsi/listtest/MyTagHandler.java?at=master&fileviewer=file-view-default
     */
    private void processUlOlLi(boolean opening, String tag, Editable output)
    {
        if (tag.equalsIgnoreCase("ul"))
        {
            if (opening)
            {
                lists.push(tag);
            }
            else
            {
                lists.pop();
            }
        }
        else if (tag.equalsIgnoreCase("ol"))
        {
            if (opening)
            {
                lists.push(tag);
                olNextIndex.push(1);//TODO: add support for lists starting other index than 1
            }
            else
            {
                lists.pop();
                olNextIndex.pop();
            }
        }
        else if (tag.equalsIgnoreCase("li"))
        {
            if (opening)
            {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n')
                {
                    output.append("\n");
                }
                String parentList = lists.peek();
                if (parentList.equalsIgnoreCase("ol"))
                {
                    start(output, new Ol());
                    output.append(olNextIndex.peek().toString()).append(". ");
                    olNextIndex.push(olNextIndex.pop() + 1);
                }
                else if (parentList.equalsIgnoreCase("ul"))
                {
                    start(output, new Ul());
                }
            }
            else
            {
                if (lists.peek().equalsIgnoreCase("ul"))
                {
                    if (output.length() > 0 && output.charAt(output.length() - 1) != '\n')
                    {
                        output.append("\n");
                    }
                    // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                    int bulletMargin = indent;
                    if (lists.size() > 1)
                    {
                        bulletMargin = indent - bullet.getLeadingMargin(true);
                        if (lists.size() > 2)
                        {
                            // This get's more complicated when we add a LeadingMarginSpan into the same line:
                            // we have also counter it's effect to BulletSpan
                            bulletMargin -= (lists.size() - 2) * listItemIndent;
                        }
                    }
                    BulletSpan newBullet = new BulletSpan(bulletMargin);
                    end(output,
                            Ul.class,
                            new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                            newBullet);
                }
                else if (lists.peek().equalsIgnoreCase("ol"))
                {
                    if (output.length() > 0 && output.charAt(output.length() - 1) != '\n')
                    {
                        output.append("\n");
                    }
                    int numberMargin = listItemIndent * (lists.size() - 1);
                    if (lists.size() > 2)
                    {
                        // Same as in ordered lists: counter the effect of nested Spans
                        numberMargin -= (lists.size() - 2) * listItemIndent;
                    }
                    end(output,
                            Ol.class,
                            new LeadingMarginSpan.Standard(numberMargin));
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

    private static class Ul
    {
    }

    private static class Ol
    {
    }
}