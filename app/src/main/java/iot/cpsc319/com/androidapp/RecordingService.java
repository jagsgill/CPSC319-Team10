package iot.cpsc319.com.androidapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import mqtt.ConnectivityException;
import sensors.GpsRecorder;
import mqtt.MqttPublisher;
import mqtt.TopicMsg;
import sensors.AccRecorder;

public class RecordingService extends Service {

    private static final String TAG = "SomeApp";

    private final String clientId = getSerialNumber();
    private LocalBinder mBinder = new LocalBinder();
    private WeakReference<MainActivity> mMainActivity;
    private AccRecorder accelerometer;
    private MqttPublisher mqttPublisher;
    private GpsRecorder gps;
    private int batteryLevel;
    private BroadcastReceiver batteryLevelReceiver;

    public class LocalBinder extends Binder {
        WeakReference<RecordingService> getService() {
            return new WeakReference<>(RecordingService.this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "RecordingService Start");

        mqttPublisher = new MqttPublisher(clientId, getApplicationContext());
        try {
            mqttPublisher.startConnection();
        } catch (ConnectivityException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(this, "Network not found, exiting...", Toast.LENGTH_SHORT).show();
            stopSelf();
            return 0;
        }

        gps = new GpsRecorder(this);
        gps.start();

        accelerometer = new AccRecorder(this);
        accelerometer.start();

        // todo a separate recorder?
        registerBatteryLevelReceiver();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (accelerometer != null)
            accelerometer.stop();
        if (gps != null)
            gps.stop();
        if (mqttPublisher != null) {
            try {
                mqttPublisher.stopConnection();
            } catch (ConnectivityException e) {
                Log.i(TAG, e.getMessage());
            }
        }
        if (batteryLevelReceiver != null) {
            this.unregisterReceiver(batteryLevelReceiver);
        }
        Log.i(TAG, "RecordingService onDestroy");
    }

    // format data and publish when called
    // TODO: try to separate sensor data publishing?
    // Watches can publish any topic under sensors/<client id>/<...>. Other topics are banned!
    // e.g. sensors/<client id>/accel
    //      sensors/<client id>/gps
    //      sensors/<client id>/batteryanduploadrate
    // If not separated, broker manager can currently handle combined data under sensors/<client id>/combined
    // which includes accel & gps data only at the moment (same as for first demo)...
    // The last part of topic should match what is used in the server's broker manager!
    public void update() {
        try {
            mqttPublisher.publish(new TopicMsg("client/watch/" + clientId + "/combined",
                    "Acceleration: " + accelerometer.retrieveData()
                            + (gps.hasData() ? ("\r\nLocation: " + gps.retrieveData()) : " "
                            + "\r\nBattery Level: " + batteryLevel + "%")));
        } catch (ConnectivityException e) {
            MainActivity act;
            if ((act = mMainActivity.get()) != null) {
                act.buttonOff();
            }
            Toast.makeText(this, "Network not found, exiting...", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    public void setMainActivity(WeakReference<MainActivity> act) {
        mMainActivity = act;
    }

    public void onStep(int step) {
        step++;
        MainActivity act;
        if ((act = mMainActivity.get()) != null)
            act.display(Integer.toString(step));
    }

    /**
     * Returns the unique serial number of the device.
     * More info at {@link 'http://developer.samsung.com/technical-doc/view.do?v=T000000103'}
     */
    private String getSerialNumber() {
        String serialnum = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serialnum = (String) (get.invoke(c, "ro.serialno", "unknown"));
        } catch (Exception ignored) {
            // we should not reach here
            // there should be a serial number available for all watches used...
        }
        return serialnum;
    }

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = -1;
                if (rawLevel >= 0 && scale > 0) {
                    level = (rawLevel * 100) / scale;
                }
                if (level != batteryLevel) {
                    batteryLevel = level;
                }
            }
        };
        this.registerReceiver(batteryLevelReceiver, filter);
    }
}
