package ru.kuchanov.tproger.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.util.ArrayList;
import java.util.Collections;

import ru.kuchanov.tproger.Const;
import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.SingltonRoboSpice;
import ru.kuchanov.tproger.activity.ActivityMain;
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
    private static final int NOTIFICATION_ID = 859;
    private MySpiceManager spiceManager;
    private Context ctx;

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

        RoboSpiceRequestCategoriesArts request = new RoboSpiceRequestCategoriesArts(ctx, "");
        spiceManager.execute(request, LOG, DurationInMillis.ALWAYS_EXPIRED, new CategoriesArtsRequestListener());
    }

    public void sendNotification(int newArtsCount, ArrayList<Article> dataFromWeb)
    {
        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.mipmap.ic_launcher);

        //Set the text that is displayed in the status bar when the notification first arrives.
        builder.setTicker(dataFromWeb.get(0).getTitle());

        // This intent is fired when notification is clicked
        Intent intent = new Intent(ctx, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
//        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));

        // Content text, which appears in smaller text below the title
        //				builder.setContentText("Новые статьи");

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        builder.setSubText(dataFromWeb.get(0).getTitle());//"Всего новых статей:");

        builder.setAutoCancel(true);

        String title;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (newArtsCount == Const.NUM_OF_ARTS_ON_PAGE)
        {
            title = "Более " + Const.NUM_OF_ARTS_ON_PAGE + " новых статей";
            inboxStyle.setBigContentTitle(ctx.getString(R.string.notif_new_arts_big_content_title));
        }
        else
        {
            title = "Новые статьи: " + newArtsCount;
            inboxStyle.setBigContentTitle("Новых статей: " + newArtsCount);
        }

        for (int i = 0; i < newArtsCount && dataFromWeb.size() < i; i++)
        {
            inboxStyle.addLine(dataFromWeb.get(i).getTitle());
        }
        builder.setNumber(newArtsCount);

        // Moves the expanded layout object into the notification object.
        builder.setStyle(inboxStyle);

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle(title);

        builder.setVibrate(new long[]{500, 500, 500, 500, 500});
        //LED
        builder.setLights(Color.WHITE, 3000, 3000);
        //Sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        // Will display the notification in the notification bar
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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

            ArrayList<Article> articleArrayList = new ArrayList<>(articles.getResult());

            Log.i(LOG, "RECEIVE " + articleArrayList.size() + " arts");

            //sort by time
            Collections.sort(articleArrayList, new Article.PubDateComparator());

            int numOfNewArts = articles.getNumOfNewArts();
            Log.d(LOG, "numOfNewArts: " + numOfNewArts);
            switch (numOfNewArts)
            {
                case -2:
                    //not set - do nothing
                case -1:
                    //initial loading  - do nothing
                case 0:
                    //no new arts, do nothing
                    break;
                default:
                    Log.d(LOG, "Обнаружено " + numOfNewArts + " новых статей!");
                    sendNotification(numOfNewArts, articleArrayList);
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
