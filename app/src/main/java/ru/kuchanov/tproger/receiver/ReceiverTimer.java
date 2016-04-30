package ru.kuchanov.tproger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Юрий on 30.04.2016 20:43.
 * For TProger.
 */
public class ReceiverTimer extends BroadcastReceiver
{
    private static final String LOG = ReceiverTimer.class.getSimpleName();
    private Context ctx;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOG, "onReceive " + intent.getAction());
        this.ctx = ctx;
        //TODO
    }
}
