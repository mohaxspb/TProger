package ru.kuchanov.tproger.fragment.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.kuchanov.tproger.R;

/**
 * Created by Юрий on 21.09.2015 18:01 19:36.
 * For TProger.
 */
public class FragmentPreferenceNotifications extends PreferenceFragment
{
    private static final String TAG = FragmentPreferenceNotifications.class.getSimpleName();
    Context ctx;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        ctx = activity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_notification);

        Log.d(TAG, "ctx==null: " + String.valueOf(ctx == null));
    }
}