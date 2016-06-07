package ru.kuchanov.tproger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.util.ArrayList;
import java.util.Collections;

import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.robospice.MySpiceManager;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.robospice.db.Articles;
import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestCategoriesArts;

/**
 * Created by Юрий on 30.04.2016 20:43.
 * For TProger.
 */
public class ReceiverTimer extends BroadcastReceiver
{
    private static final String LOG = ReceiverTimer.class.getSimpleName();
    private Context ctx;
    private MySpiceManager spiceManager;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOG, "onReceive " + intent.getAction());
        this.ctx = context;

        spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
//        spiceManager.addListenerIfPending(Articles.class, LOG, new CategoriesArtsRequestListener());

        RoboSpiceRequestCategoriesArts request = new RoboSpiceRequestCategoriesArts(ctx, "");
        spiceManager.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new CategoriesArtsRequestListener());
    }

    //inner class of your spiced Activity
    private class CategoriesArtsRequestListener implements PendingRequestListener<Articles>
    {
        @Override
        public void onRequestFailure(SpiceException e)
        {
            Log.i(LOG, "onRequestFailure with error = " + e.getClass().getSimpleName());
            spiceManager.shouldStop();
        }

        @Override
        public void onRequestSuccess(Articles articles)
        {
            Log.i(LOG, "onRequestSuccess");

            ArrayList<Article> list = new ArrayList<>(articles.getResult());

            Log.i(LOG, "RECEIVE " + list.size() + " arts");

            Collections.sort(list, new Article.PubDateComparator());

            int numOfNewArts = articles.getNumOfNewArts();
            Log.d(LOG, "numOfNewArts: " + numOfNewArts);
            switch (numOfNewArts)
            {
                case -2:
                    //not set - do nothing
                case -1:
                    //initial loading  - do nothing
                    break;
                case 0:
                    Toast.makeText(ctx, "Новых статей не обнаружено!", Toast.LENGTH_SHORT).show();
                    break;
                case 10:
                    Toast.makeText(ctx, "Обнаружено более 10 новых статей!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(ctx, "Обнаружено " + numOfNewArts + " новых статей!", Toast.LENGTH_SHORT).show();
                    break;
            }

            spiceManager.shouldStop();
        }

        @Override
        public void onRequestNotFound()
        {
//            Log.i(LOG, "onRequestNotFound called");
        }
    }
}
