package ru.kuchanov.tproger.fragment.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.activity.ActivitySettings;
import ru.kuchanov.tproger.fragment.FragmentDialogTextAppearance;

/**
 * Created by Юрий on 21.09.2015 16:36.
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
public class FragmentPreferenceDesign extends PreferenceFragment
{
//    private final static String LOG = FragmentPreferenceDesign.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_design);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        ActivitySettings.bindPreferenceSummaryToValue(findPreference(this.getString(R.string.pref_design_key_col_num)));

        //textSize
        Preference prefTextSize = findPreference(this.getString(R.string.pref_design_key_text_size));
        prefTextSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                FragmentDialogTextAppearance frag = FragmentDialogTextAppearance.newInstance();
                frag.show(getFragmentManager(), "TextAppearance");
                return false;
            }
        });

        //hide cats or tags default
        Preference preference = findPreference(getString(R.string.pref_design_key_category_in_cats_or_tags));
        PreferenceCategory mCategory = (PreferenceCategory) findPreference(getString(R.string.pref_design_main_category_key));
        mCategory.removePreference(preference);
    }
}
