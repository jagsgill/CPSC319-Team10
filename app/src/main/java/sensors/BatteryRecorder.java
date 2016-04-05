package sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import java.lang.ref.WeakReference;

import iot.cpsc319.com.androidapp.RecordingService;
import sensordata.BatteryDataPoint;

public class BatteryRecorder extends Recorder<BatteryDataPoint> {
    static final String TAG = "SomeApp";
    int previousLevel = -100;
    BroadcastReceiver batteryLevelReceiver;

    public BatteryRecorder(RecordingService recordingService) {
        super(recordingService);
    }

    @Override
    public void start() {
        stop();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (rawLevel >= 0 && scale > 0) {
                    int level = Math.round((rawLevel / (float) scale) * 100);
                    previousLevel = level;
                    data.addDataPoint(new BatteryDataPoint(level));
                    Log.i(TAG, "Battery Level: " + level);
                }
            }
        };

        service.registerReceiver(batteryLevelReceiver, filter);
    }

    @Override
    public void stop() {
        if (batteryLevelReceiver != null) {
            service.unregisterReceiver(batteryLevelReceiver);
            batteryLevelReceiver = null;
        }
    }

}
