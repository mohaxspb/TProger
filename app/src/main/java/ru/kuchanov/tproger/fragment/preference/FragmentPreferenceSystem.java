package ru.kuchanov.tproger.fragment.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ru.kuchanov.tproger.R;

/**
 * Created by Юрий on 21.09.2015 16:36.
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
public class FragmentPreferenceSystem extends PreferenceFragment
{
    private final static String LOG = FragmentPreferenceSystem.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_system);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
//        ActivitySettings.bindPreferenceSummaryToValue(findPreference(this.getString(R.string.pref_design_key_col_num)));


    }
}
