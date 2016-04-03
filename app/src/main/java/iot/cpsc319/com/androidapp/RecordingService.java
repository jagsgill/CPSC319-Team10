package iot.cpsc319.com.androidapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import mqtt.ConnectivityException;
import sensors.BatteryRecorder;
import sensors.GpsRecorder;
import mqtt.MqttPublisher;
import mqtt.TopicMsg;
import sensors.AccRecorder;

public class RecordingService extends Service {

    private static final String TAG =  "SomeApp";

    private final String clientId = getSerialNumber();
    private LocalBinder mBinder = new LocalBinder();
    private WeakReference<MainActivity> mMainActivity;
    private AccRecorder accelerometer;
    private MqttPublisher mqttPublisher;
    private GpsRecorder gps;
    private BatteryRecorder battery;

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

        battery = new BatteryRecorder(this);
        battery.start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (battery != null)
            battery.stop();
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
        Log.i(TAG, "RecordingService onDestroy");
    }

    // format data and publish
    public void update() {
        try {
            String msg = String.format("%s&%s&%s", accelerometer.retrieveData(), gps.retrieveData(),
                    battery.retrieveData());
            mqttPublisher.publish(new TopicMsg("hello/world", msg));
        } catch (ConnectivityException e) {
            MainActivity act;
            if ((act = mMainActivity.get()) != null) {
                act.buttonOff();
            }
            Toast.makeText(this, "Network not found, exiting...", Toast.LENGTH_LONG).show();
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
    private String getSerialNumber(){
        String serialnum = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class );
            serialnum = (String)(   get.invoke(c, "ro.serialno", "unknown" )  );
        }
        catch (Exception ignored)
        {
            // we should not reach here
            // there should be a serial number available for all watches used...
        }
        return serialnum;
    }
}
