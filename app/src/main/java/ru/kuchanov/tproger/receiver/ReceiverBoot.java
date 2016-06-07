package ru.kuchanov.tproger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.kuchanov.tproger.utils.NotificationUtils;

/**
 * Created by Юрий on 30.04.2016 20:40.
 * For TProger.
 */
public class ReceiverBoot extends BroadcastReceiver
{
    private static final String LOG = ReceiverBoot.class.getName();

    @Override
    public void onReceive(Context ctx, Intent intent)
    {
        Log.d(LOG, "onReceive " + intent.getAction());

        NotificationUtils.checkAlarm(ctx.getApplicationContext());
    }
}