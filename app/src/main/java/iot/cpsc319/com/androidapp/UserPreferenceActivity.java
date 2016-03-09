package iot.cpsc319.com.androidapp;

import android.preference.PreferenceActivity;

import java.util.List;


public class UserPreferenceActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_pref, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return UserPreferenceFragment.class.getName().equals(fragmentName);
    }


}
