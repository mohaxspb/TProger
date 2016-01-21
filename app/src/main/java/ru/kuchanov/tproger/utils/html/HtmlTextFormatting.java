package ru.kuchanov.tproger.utils.html;

import org.jsoup.nodes.Element;

public class HtmlTextFormatting
{
//    private static final String LOG = HtmlTextFormatting.class.getSimpleName();

    /**
     * @return true if given el has inner tags with name "table"
     */
    public static boolean hasInnerUnsupportedTags(Element el)
    {
        return el.getElementsByTag("table").size() != 0;
    }

    //TODO delete it. Change by enum types

    /**
     * @return true if given el's tag's name is "table"
     */
    public static boolean isUnsupportedTag(Element el)
    {
        return el.tagName().equals("table");
    }

    public static HtmlToView.TextType tagType(Element el)
    {
        String tagName = el.tagName();
        String tagClass = el.className();

        if (tagClass.contains("crayon-syntax"))
        {
            return HtmlToView.TextType.Code;
        }
        if (tagName.equals("table") || (el.getElementsByTag("table").size() != 0))
        {
            return HtmlToView.TextType.Table;
        }
        if (tagClass.contains("accordion"))
        {
            return HtmlToView.TextType.Accordion;
        }
        if (tagClass.contains("wp-polls"))
        {
            return HtmlToView.TextType.Poll;
        }
        if (tagClass.equals("well"))
        {
            return HtmlToView.TextType.Well;
        }

        return HtmlToView.TextType.Text;
    }
}