package ru.kuchanov.tproger.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Юрий on 24.03.2016 23:25.
 * For TProger.
 */
public class MinuteReceiver extends BroadcastReceiver
{
    private static final String LOG = MinuteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(LOG, "onReceive");
        Toast.makeText(context, "TEST", Toast.LENGTH_SHORT).show();
    }
}
