package sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.lang.ref.WeakReference;

import iot.cpsc319.com.androidapp.RecordingService;
import mqtt.MqttPublisher;
import sensordata.BatteryDataPoint;

public class BatteryRecorder extends Recorder<BatteryDataPoint> {
    int preivousLevel = -100;
    WeakReference<BroadcastReceiver> batteryLevelReceiver;

    public BatteryRecorder(RecordingService recordingService) {
        super(recordingService);
    }

    @Override
    public void start() {
        stop();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelReceiver = new WeakReference<BroadcastReceiver>(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                if (rawLevel >= 0 && scale > 0) {
                    int level = (rawLevel * 100) / scale;
                    if (Math.abs(preivousLevel - level) > 5) {
                        preivousLevel = level;
                        data.addDataPoint(new BatteryDataPoint(level));
                    }
                }
            }
        });
        BroadcastReceiver receiver;
        if (batteryLevelReceiver != null && (receiver = batteryLevelReceiver.get()) != null)
            service.registerReceiver(receiver, filter);
    }

    @Override
    public void stop() {
        BroadcastReceiver receiver;
        if (batteryLevelReceiver != null && (receiver = batteryLevelReceiver.get()) != null) {
            service.unregisterReceiver(receiver);
            batteryLevelReceiver = null;
        }

    }
}
