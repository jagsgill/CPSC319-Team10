package iot.cpsc319.com.androidapp;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
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
