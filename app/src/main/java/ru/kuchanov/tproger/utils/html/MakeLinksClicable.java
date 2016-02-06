package ru.kuchanov.tproger.utils.html;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.AttributeGetter;

public class MakeLinksClicable
{
    private final static String LOG = MakeLinksClicable.class.getSimpleName();

    public static SpannableStringBuilder reformatText(Context ctx, CharSequence text)
    {
        int end = text.length();
        Spannable sp = (Spannable) text;

        //restyling quotes
        replaceQuoteSpans(ctx, sp);

        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        //					style.clearSpans();//should clear old spans
        for (URLSpan url : urls)
        {
            style.removeSpan(url);
            MakeLinksClicable.CustomerTextClick click = new MakeLinksClicable.CustomerTextClick(url.getURL());
            style.setSpan(click, sp.getSpanStart(url), sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return style;
    }

    //quotes
    //see http://stackoverflow.com/a/29114976/3212712
    private static void replaceQuoteSpans(Context ctx, Spannable spannable)
    {
        int colorBackground = AttributeGetter.getColor(ctx, R.attr.windowBackgroundDark);
        int colorStripe = AttributeGetter.getColor(ctx, R.attr.colorAccent);

        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);

        for (QuoteSpan quoteSpan : quoteSpans)
        {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(new CustomQuoteSpan(
                            colorBackground,
                            colorStripe,
                            5,
                            10),
                    start,
                    end,
                    flags);
        }
    }

    public static class CustomerTextClick extends ClickableSpan
    {
        String mUrl;

        public CustomerTextClick(String url)
        {
            mUrl = url;
        }

        @Override
        public void onClick(View widget)
        {
            Log.i(LOG, "url clicked: " + this.mUrl);
//        }
            //TODO add check by url
//            if (this.mUrl.contains("odnako.org/blogs/"))
//            {
//                final AppCompatActivity act = (AppCompatActivity) widget.getContext();
//
//                Fragment newFragment = new FragmentArticle();
//                Article a = new Article();
//                a.setUrl(this.mUrl);
//                a.setTitle(this.mUrl);
//                Bundle b = new Bundle();
//                b.putParcelable(Article.KEY_CURENT_ART, a);
//                b.putBoolean("isSingle", true);
//                newFragment.setArguments(b);
//
//                FragmentTransaction ft = act.getSupportFragmentManager().beginTransaction();
//                ft.replace(R.id.container_right, newFragment, FragmentArticle.LOG);
//                ft.addToBackStack(null);
//                ft.commit();
//
//                //setBackButton to toolbar and its title
//                Toolbar toolbar;
//                boolean twoPane = PreferenceManager.getDefaultSharedPreferences(act).getBoolean(
//                        ActivityPreference.PREF_KEY_TWO_PANE, false);
//                if (!twoPane)
//                {
//                    //So it's article activity
//                    ((ActivityBase) act).mDrawerToggle.setDrawerIndicatorEnabled(false);
//                    toolbar = (Toolbar) act.findViewById(R.id.toolbar);
//                    toolbar.setTitle("Статья");
//                }
//                else
//                {
//                    //we are on main activity, so we must set toggle to rightToolbar
//                    toolbar = (Toolbar) act.findViewById(R.id.toolbar_right);
//                    toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
//                    toolbar.setNavigationOnClickListener(new OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v)
//                        {
//                            act.onBackPressed();
//                        }
//                    });
//                    toolbar.setTitle("Статья");
//                }
//            }
//            else
//            {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(mUrl));
//                widget.getContext().startActivity(i);
        }
//            			Toast.makeText(widget.getContext(), mUrl, Toast.LENGTH_LONG).show();
    }
}