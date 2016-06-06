package ru.kuchanov.tproger.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.receiver.ReceiverTimer;

/**
 * Created by Юрий on 05.06.2016 19:14.
 * For TProger.
 */
public class NotificationUtils
{
    private static final int ID = 678;
    private static final String TAG = NotificationUtils.class.getSimpleName();

    public static void setAlarm(Context ctx)
    {
        AlarmManager am = (AlarmManager) ctx.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentToTimerReceiver = new Intent(ctx.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction(ctx.getString(R.string.receiver_timer_action));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx.getApplicationContext(),
                ID,
                intentToTimerReceiver,
                PendingIntent.FLAG_CANCEL_CURRENT);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int periodInMinutes = Integer.parseInt(pref.getString(ctx.getString(R.string.pref_notifications_key_period), "30"));
        Log.i(TAG, "setting alarm with period " + periodInMinutes);
        long periodInMiliseconds = periodInMinutes * 60 * 1000;
//        //TODO test
        periodInMiliseconds = 1000 * 20;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + periodInMiliseconds, periodInMiliseconds, pendingIntent);
        }
        else
        {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + periodInMiliseconds, periodInMiliseconds, pendingIntent);
        }
    }

    private static void cancelAlarm(Context ctx)
    {
        Log.i(TAG, "cancelAlarm");
        AlarmManager am = (AlarmManager) ctx.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentToTimerReceiver = new Intent(ctx.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction(ctx.getString(R.string.receiver_timer_action));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx.getApplicationContext(), ID,
                intentToTimerReceiver,
                PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static void checkAlarm(Context ctx)
    {
        Log.d(TAG, "checkAlarm");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean notifIsOn = pref.getBoolean(ctx.getString(R.string.pref_notifications_key_enable), false);
        Log.d(TAG, "notifIsOn: " + notifIsOn);
        if (notifIsOn)
        {
            cancelAlarm(ctx);
            setAlarm(ctx);
        }
        else
        {
            cancelAlarm(ctx);
        }
    }

    public static void checkIfSet(Context ctx)
    {
        Log.d(TAG, "checkIfSet");

        Intent intent2check = new Intent(ctx.getApplicationContext(), ReceiverTimer.class);
        intent2check.setAction(ctx.getString(R.string.receiver_timer_action));
        boolean alarmUp = PendingIntent.getBroadcast(
                ctx.getApplicationContext(),
                ID,
                intent2check,
                PendingIntent.FLAG_NO_CREATE)
                != null;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean notifIsOn = pref.getBoolean(ctx.getString(R.string.pref_notifications_key_enable), false);
        if (!alarmUp && notifIsOn)
        {
            setAlarm(ctx);
        }
    }
}
