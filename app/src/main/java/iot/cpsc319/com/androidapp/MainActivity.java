package iot.cpsc319.com.androidapp;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

// TODO check android target api (20 or 21 okay?)
public class MainActivity extends ActionBarActivity {

    static final String TAG = "SomeApp";

    private Intent recordingIntent;
    private WeakReference<RecordingService> mService;
    private boolean serviceRunning;
    private Button button;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "MainActivity onServiceConnected");
            buttonOn();
            mService = ((RecordingService.LocalBinder) service).getService();
            RecordingService s;
            if ((s = mService.get()) != null) {
                s.setMainActivity(new WeakReference<>(MainActivity.this));
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceRunning = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false);
        Log.i(TAG, "MainActivity onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "MainActivity onPause");
        if (serviceRunning) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "MainActivity onResume");
        if (serviceRunning)
            bindService(new Intent(this, RecordingService.class), mConnection, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MainActivity onDestroy");
    }

    public void onButtonClick(View view) {
        if (button.getText().equals(getString(R.string.button_start))) {
            recordingIntent = new Intent(this, RecordingService.class);
            startService(recordingIntent);
            bindService(new Intent(this, RecordingService.class), mConnection, 0);
        } else {
            buttonOff();
            stopService(recordingIntent);
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            Intent settingIntent = new Intent(this, UserPreferenceActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void display(String str) {
        TextView tv = (TextView) findViewById(R.id.display);
        tv.setText(str);
    }

    private void buttonOn() {
        display("0");
        button.setText(R.string.button_stop);
        button.setBackground(getResources().getDrawable(R.drawable.button_stop));
        serviceRunning = true;
    }

    void buttonOff() {
        button.setText(R.string.button_start);
        button.setBackground(getResources().getDrawable(R.drawable.button_start));
        serviceRunning = false;
    }

    private boolean isRecording() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo s : am.getRunningServices(Integer.MAX_VALUE)) {
            if (s.service.getClassName().equals(RecordingService.class.getName()))
                return true;
        }
        return false;
    }
}
