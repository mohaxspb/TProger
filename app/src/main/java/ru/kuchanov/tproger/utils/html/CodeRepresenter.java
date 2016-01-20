package ru.kuchanov.tproger.utils.html;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;

/**
 * Created by Юрий on 18.01.2016 18:01.
 * For TProger.
 */
public class CodeRepresenter
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

    public CodeRepresenter(ArrayList<String> lines)
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
        int adressOfColor;
        switch (type)
        {
            default:
                adressOfColor = R.color.my_material_grey_600;
                break;
            case Clazz:
                adressOfColor = R.color.material_indigo_300;
                break;
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
        return Integer.toHexString(ContextCompat.getColor(ctx, adressOfColor)).substring(2);
    }

    public static CodeRepresenter parseTableForCodeLines(Context ctx, String html)
    {
        ArrayList<String> codeLines = new ArrayList<>();

        Document doc = Jsoup.parse(html);

        Element el = doc.getElementsByClass("crayon-pre").first();

        ArrayList<Element> lines = el.children();

        for (Element element : lines)
        {
            for (Element span : element.children())
            {
//                <font color='#123456'>text</font>
                String spanClass = span.className();
                CodeRepresenter.SpanType spanType = CodeRepresenter.getTypeBySpanClass(spanClass);
                String spanColorInHex = "#" + CodeRepresenter.getColorForType(ctx, spanType);
//                Log.i(LOG, spanColorInHex);

                Element font = new Element(Tag.valueOf("font"), "");
                font.attr("color", spanColorInHex);
                font.text(span.text());
                span.replaceWith(font);
            }
            codeLines.add(element.html());
        }

        return new CodeRepresenter(codeLines);
    }

    public ArrayList<String> getLines()
    {
        return lines;
    }

    public enum SpanType
    {
        //blue darkBlue    violet  orange  red     blue
        Clazz, Var, Bool, Comment, Import, Number, Operand
    }
}
