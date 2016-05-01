package ru.kuchanov.tproger.fragment.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.NotificationUtils;

/**
 * Created by Юрий on 21.09.2015 18:01 19:36.
 * For TProger.
 */
public class FragmentPreferenceNotifications extends PreferenceFragment
{
    private static final String LOG = FragmentPreferenceNotifications.class.getSimpleName();
    SharedPreferences pref;
    Context ctx;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public void onAttach(Activity act)
    {
        super.onAttach(act);
        ctx = act;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_notification);

        pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        SwitchPreference notifOnOff = (SwitchPreference) findPreference(getString(R.string.pref_notifications_key_enable));
        notifOnOff.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                boolean isOn = pref.getBoolean(preference.getKey(), false);
                Log.d(LOG, "isOn: " + isOn);
                if (isOn)
                {
                    NotificationUtils.setAlarm(ctx);
                }
                else
                {
                    NotificationUtils.cancelAlarm(ctx);
                }
                return true;
            }
        });
    }
}