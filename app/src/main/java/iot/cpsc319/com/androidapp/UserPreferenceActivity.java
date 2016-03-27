package iot.cpsc319.com.androidapp;

import android.preference.PreferenceActivity;

import java.util.List;

public class UserPreferenceActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UserPreferenceFragment()).commit();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return UserPreferenceFragment.class.getName().equals(fragmentName);
    }
}
