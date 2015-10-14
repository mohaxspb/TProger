package ru.kuchanov.tproger.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ru.kuchanov.tproger.R;

/**
 * Created by Юрий on 21.09.2015 18:01.
 * For ExpListTest.
 */
public class FragmentPreferenceNotifications extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_notification);
    }
}