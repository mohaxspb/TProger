package ru.kuchanov.tproger.utils.html;

/**
 * Created by Юрий on 01.11.2015 23:45.
 * For ExpListTest.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

import java.util.ArrayList;
import java.util.Iterator;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.DipToPx;

/**
 * class for formatting Html and extracting some tags from p tag;
 */
public class HtmlTextFormatting
{
    private static final String LOG = HtmlTextFormatting.class.getSimpleName();
//    static final String LOG = HtmlTextFormatting.class.getSimpleName();
//
//    private static final String TAG_IMG = "img";
//    private static final String TAG_INPUT = "input";
//    private static final String TAG_A = "a";
//    private static final String TAG_IFRAME = "iframe";
//
//    private static final String ARROW_OPEN = "<";
//    private static final String ARROW_CLOSE = ">";
//    private static final String DOMAIN_NAME = "http://odnako.org";
//
//    private static TagNode formatedArticle;
//    private static String tagHtml;
//
//    /**
//     * Loop through given tags and if found input or img tags in p tag extract
//     * it to top level of document structure, converting it to image tag, that
//     * make allowed to show their images in ImageView.
//     *
//     * Also convert iframe tag to a tag.
//     *
//     * @param tags
//     * @return TagNode with formated tags. I.e. extracting img tags to the top
//     *         level from p tag, dividing it into 2 parts
//     */
//    public static TagNode format(TagNode[] tags)
//    {
//        formatedArticle = new TagNode("div");
//        HtmlCleaner cleaner = new HtmlCleaner();
//        for (int i = 0; i < tags.length; i++)
//        {
//            TagNode curTag = tags[i];
//            if (curTag.getName().equals("p") || curTag.getName().equals("div")
//                    )
//            {
//                if (curTag.getChildTags().length != 0)
//                {
//                    tagHtml = cleaner.getInnerHtml(curTag);
//
//                    for (int u = 0; u < curTag.getChildTags().length; u++)
//                    {
//                        TagNode curInnerTag = curTag.getChildTags()[u];
//                        if (curInnerTag.getName().equals(TAG_INPUT))
//                        {
//                            extractImgTags(curInnerTag, TAG_INPUT);
//                        }//if input tag
//                        else if (curInnerTag.getName().equals(TAG_IMG))
//                        {
//                            extractImgTags(curInnerTag, TAG_IMG);
//                        }//if img tag
//                        else if (curInnerTag.getName().equals(TAG_IFRAME))
//                        {
//                            extractIframeTags(curInnerTag, TAG_IFRAME);
//                        }//if img tag
//                        else if (curInnerTag.getName().equals(TAG_A)
//                                && curInnerTag.getChildTags().length != 0
//                                && curInnerTag.getChildTags()[0].getName().equals(TAG_IMG))
//                        {
//                            //we must here create 2 new TagNodes with text before and after img or input node
//                            extractImgTags(curInnerTag.getChildTags()[0], TAG_IMG);
//                        }//if we have img tag inside of a tag. I.e. in developers article. =)
//
//                        if (u == curTag.getChildTags().length - 1)
//                        {
//                            TagNode firstTag = new TagNode("p");
//                            firstTag.addChild(new ContentNode(tagHtml));
//                            formatedArticle.addChild(firstTag);
//                        }
//                    }//loop of p tag children
//                }//if has children
//                else
//                {
//                    formatedArticle.addChild(curTag);
//                }
//            }//if tag is p or div
//            else if(curTag.getName().equals("h1")
//                    || curTag.getName().equals("h2")
//                    || curTag.getName().equals("h3")
//                    || curTag.getName().equals("h4")
//                    || curTag.getName().equals("h5")
//                    || curTag.getName().equals("h6"))
//            {
//                formatedArticle.addChild(curTag);
//            }
//            else
//            {
//                //place tag inside of p tag
//                TagNode pTag = new TagNode("p");
//                pTag.addChild(curTag);
//                TagNode brTag = new TagNode("br");
//                pTag.addChild(brTag);
//                formatedArticle.addChild(pTag);
//                //formatedArticle.addChild(curTag);
//            }
//        }//loop of articles tags
//        return formatedArticle;
//    }
//
//    private static void extractImgTags(TagNode curInnerTag, String tagType)
//    {
//        //we must here create 2 new TagNodes with text before and after img or input node
//        String subStrStartsWithInput = tagHtml.substring(tagHtml.indexOf(ARROW_OPEN + tagType));
//        String subStrWithInput = subStrStartsWithInput.substring(0,
//                subStrStartsWithInput.indexOf(ARROW_CLOSE) + 1);
//        tagHtml = tagHtml.replaceFirst(subStrWithInput, Article.DIVIDER);
//        String[] dividedTag = tagHtml.split(Article.DIVIDER);
//        //create new p tag with innerHtml of parent p tag from 0 to input tag
//        //check if inputTag is not first in parent
//        if (dividedTag.length != 0)
//        {
//            TagNode firstTag = new TagNode("p");
//            firstTag.addChild(new ContentNode(dividedTag[0]));
//            if (dividedTag.length == 1)
//            {
//                //and finally set innerHtml of our parent p tag to second part of our array
//                tagHtml = dividedTag[0];
//            }
//            else
//            {
//                //and finally set innerHtml of our parent p tag to second part of our array
//                tagHtml = dividedTag[1];
//            }
//            //add them to our formated tag
//            formatedArticle.addChild(firstTag);
//        }
//        else
//        {
//            //and finally set innerHtml of our parent p tag to second part of our array
//            tagHtml = "";
//        }
//        //create img tag with info from input
//        TagNode imgTag = new TagNode("img");
//        String imgUrl = curInnerTag.getAttributeByName("src");
//        if (imgUrl.startsWith("/"))
//        {
//            imgUrl = DOMAIN_NAME + imgUrl;
//        }
//        imgTag.addAttribute("src", imgUrl);
//        imgTag.addAttribute("style", curInnerTag.getAttributeByName("style"));
//        //add them to our formated tag
//        formatedArticle.addChild(imgTag);
//    }
//
//    private static void extractIframeTags(TagNode curInnerTag, String tagType)
//    {
//        //<p><iframe allowfullscreen="" frameborder="0" height="315" src="https://www.youtube.com/embed/baZL7eY37-w" width="420"></iframe></p>
//
//        //we must here create 2 new TagNodes with text before and after img or input node
//        String subStrStartsWithInput = tagHtml.substring(tagHtml.indexOf(ARROW_OPEN + tagType));
//        String subStrWithInput = subStrStartsWithInput.substring(0,
//                subStrStartsWithInput.indexOf(ARROW_CLOSE) + 1);
//        tagHtml = tagHtml.replaceFirst(subStrWithInput, Article.DIVIDER);
//        String[] dividedTag = tagHtml.split(Article.DIVIDER);
//        //create new p tag with innerHtml of parent p tag from 0 to input tag
//        //check if inputTag is not first in parent
//        if (dividedTag.length != 0)
//        {
//            TagNode firstTag = new TagNode("p");
//            firstTag.addChild(new ContentNode(dividedTag[0]));
//            if (dividedTag.length == 1)
//            {
//                //and finally set innerHtml of our parent p tag to second part of our array
//                tagHtml = dividedTag[0];
//            }
//            else
//            {
//                //and finally set innerHtml of our parent p tag to second part of our array
//                tagHtml = dividedTag[1];
//            }
//            //add them to our formated tag
//            formatedArticle.addChild(firstTag);
//        }
//        else
//        {
//            //and finally set innerHtml of our parent p tag to second part of our array
//            tagHtml = "";
//        }
//        //create p tag with a tag from iframes src
//        TagNode pTag = new TagNode("p");
//        String link = "<a href='" + curInnerTag.getAttributeByName("src") + "'>" + "ссылка на видео" + "</a>";
//        pTag.addChild(new ContentNode(link));
//        //add them to our formated tag
//        formatedArticle.addChild(pTag);
//    }
//
//    public static TagNode reduceTagsQuont(TagNode data)
//    {
//        TagNode[] formatedArticleTagsArr = data.getChildTags();
//
//        formatedArticle = new TagNode("div");
//
//        for (int i = 0; i < formatedArticleTagsArr.length; i++)
//        {
//            if (formatedArticleTagsArr[i].getName().equals("img"))
//            {
//                formatedArticle.addChild(formatedArticleTagsArr[i]);
//            }
//            else
//            {
//                //add first divTag if final tag has no childs
//                if (!formatedArticle.hasChildren())
//                {
//                    TagNode divTag = new TagNode("div");
//                    formatedArticle.addChild(divTag);
//                }
//                //check name of last tag
//                if (formatedArticle.getChildTags()[formatedArticle.getChildTags().length - 1].getName().equals("img"))
//                {
//                    TagNode divTag = new TagNode("div");
//                    divTag.addChild(formatedArticleTagsArr[i]);
//                    formatedArticle.addChild(divTag);
//                }
//                else
//                {
//                    formatedArticle.getChildTags()[formatedArticle.getChildTags().length - 1]
//                            .addChild(formatedArticleTagsArr[i]);
//                }
//            }
//        }
//        return formatedArticle;
//    }
//
//    public static TagNode replaceATags(TagNode tag)
//    {
//        HtmlCleaner hc = new HtmlCleaner();
//        String innerHtml = Html.fromHtml(hc.getInnerHtml(tag), null, new MyHtmlTagHandler()).toString();
//
//        TagNode ttttt = hc.clean(innerHtml);
//        ArrayList<TagNode> innerATags = new ArrayList<TagNode>(ttttt.getAllElementsList(true));
//
//        for (TagNode aTag : innerATags)
//        {
//            if (aTag.getName().equals("a"))
//            {
//                String attr = aTag.getAttributeByName("href");
//                String text = aTag.getText().toString();
//                int firstATagIndex = innerHtml.indexOf("<a");
//                int firstATagTextIndex = innerHtml.indexOf(text);
//                String aTagsStartString = innerHtml.substring(firstATagIndex, firstATagTextIndex + text.length());
//                innerHtml = innerHtml.replace(aTagsStartString, text + " (" + attr + ") ");
//                innerHtml = innerHtml.replace("<a/>", "");
//            }
//        }
//
//        TagNode newTagToReturn = new TagNode(tag.getName());
//        newTagToReturn.addChild(new ContentNode(innerHtml));
//
//        return newTagToReturn;
//    }

    /**
     * @return true if elems in parsed html has inner tags with name "table"
     */
    public static boolean hasUnsupportedTags(String html)
    {
        Document doc = Jsoup.parse(html);
        return doc.getElementsByTag("table").size() != 0;
    }

    /**
     * @return true if given el has inner tags with name "table"
     */
    public static boolean hasInnerUnsupportedTags(Element el)
    {
        return el.getElementsByTag("table").size() != 0;
    }

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

        if (tagName.equals("table") || (el.getElementsByTag("table").size() != 0))
        {
            return HtmlToView.TextType.Table;
        }
        if (tagClass.contains("accordion"))
        {
            return HtmlToView.TextType.Accordeon;
        }
        if (tagClass.contains("wp-polls"))
        {
            return HtmlToView.TextType.Poll;
        }

        return HtmlToView.TextType.Text;
    }

    public static String removeTextSizeStyleAttrsFromTable(Context ctx, String html)
    {
        Document doc = Jsoup.parse(html);
        ArrayList<Element> elements = doc.getAllElements();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        float artTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_article), 0.75f);
        int textSizeInSp = (int) (ctx.getResources().getDimension(R.dimen.text_size_primary) / ctx.getResources().getDisplayMetrics().density * artTextScale);
        int textSizeInPx = (int) DipToPx.convert(textSizeInSp, ctx);

        for (Element el : elements)
        {
//            if(el.className().equals("crayon-nums-content"))
//            {
//                String attr = el.attr("style");
//                attr = attr.replace("font-size: 12px !important;", "");
//                attr = attr.replace("line-height: 15px !important;", "line-height: 105px !important;");
//                el.removeAttr("style");
//                el.attr("style", attr);
//            }

            if (!el.hasAttr("style"))
            {
                continue;
            }

            String attr = el.attr("style");
//            attr = attr.replace("font-size: 12px !important;", "");
//            attr = attr.replace("line-height: 15px !important;", "");
            attr = attr.replace("font-size: 12px !important;", "font-size: " + textSizeInPx + "px !important;");
            attr = attr.replace("line-height: 15px !important;", "line-height: " + textSizeInPx + "px !important;");
            el.removeAttr("style");
            el.attr("style", attr);
        }
        return doc.outerHtml();
    }

    public static CodeTableContent parseTableForCodeLines(Context ctx, String html)
    {
        ArrayList<String> codeLines = new ArrayList<>();

        Document doc = Jsoup.parse(html);

        Element el = doc.getElementsByClass("crayon-pre").first();

        ArrayList<Element> lines = el.children();

        for (Element element : lines)
        {
//            codeLines.add(Html.fromHtml(element.html()).toString());
            for (Element span : element.children())
            {
//                <font color='#123456'>text</font>
                String spanClass = span.className();
                CodeTableContent.SpanType spanType = CodeTableContent.getTypeBySpanClass(spanClass);
                String spanColorInHex = "#" + CodeTableContent.getColorForType(ctx, spanType);
//                Log.i(LOG, spanColorInHex);

                Element font= new Element(Tag.valueOf("font"), "");
                font.attr("color", spanColorInHex);
                font.text(span.text());
                span.replaceWith(font);
            }
            codeLines.add(element.html());
        }

        return new CodeTableContent(codeLines);
    }

    /**
     * class-representation for parsed code section from TProger.ru <br/>
     * holds {@code ArrayList<String>} of lines of code
     */
    public static class CodeTableContent
    {
        public static final String CR = "crayon-";

        public static final String TYPE_CN = CR + "cn";//number
        public static final String TYPE_E = CR + "e";//clazz
        public static final String TYPE_V = CR + "v";//var
        public static final String TYPE_R = CR + "r";//new import
        public static final String TYPE_C = CR + "c";//comment
        public static final String TYPE_T = CR + "t";//bool
        public static final String TYPE_O = CR + "o";//operand

        private ArrayList<String> lines;

        public CodeTableContent(ArrayList<String> lines)
        {
            this.lines = lines;
        }

        public static SpanType getTypeBySpanClass(String spanClass)
        {
            SpanType type;

            switch (spanClass)
            {
                case TYPE_CN:
                    type = SpanType.Number;
                    break;
                case TYPE_E:
                    type = SpanType.Clazz;
                    break;
                default:
                case TYPE_V:
                    type = SpanType.Var;
                    break;
                case TYPE_R:
                    type = SpanType.Import;
                    break;
                case TYPE_C:
                    type = SpanType.Comment;
                    break;
                case TYPE_T:
                    type = SpanType.Bool;
                    break;
                case TYPE_O:
                    type = SpanType.Operand;
                    break;
            }
            return type;
        }

        public static String getColorForType(Context ctx, SpanType type)
        {
            int adressOfColor = R.color.my_material_grey_600;

            switch (type)
            {
                case Clazz:
                    adressOfColor = R.color.material_indigo_300;
                    break;
                default:
                case Var:
                    adressOfColor = R.color.material_indigo_600;
                    break;
                case Bool:
                    adressOfColor = R.color.material_teal_500;
                    break;
                case Comment:
                    adressOfColor = R.color.material_amber_700;
                    break;
                case Import:
                    adressOfColor = R.color.material_teal_500;
                    break;
                case Number:
                    adressOfColor = R.color.material_red_500;
                    break;
                case Operand:
                    adressOfColor = R.color.material_indigo_300;
                    break;
            }

//            return ContextCompat.getColor(ctx, adressOfColor);
//            return ctx.getResources().getString(adressOfColor);
            return Integer.toHexString(ContextCompat.getColor(ctx, adressOfColor)).substring(2);
        }

        public ArrayList<String> getLines()
        {
            return lines;
        }


        public enum SpanType
        {
            //blue darkBlue    violet  orange  red     blue
            Clazz, Var, Bool, Comment, Import, Number, Operand;
        }
    }
}
