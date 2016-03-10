package iot.cpsc319.com.androidapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class UserPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_screen);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
        updateSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary();
    }

    private void updateSummary() {
        PreferenceCategory cat = (PreferenceCategory) getPreferenceScreen().getPreference(0);
        for (int i = 0; i < cat.getPreferenceCount(); i++) {
            ListPreference pref = (ListPreference) cat.getPreference(i);
            String summary = pref.getEntry().toString();
            pref.setSummary(summary);
        }
    }
}