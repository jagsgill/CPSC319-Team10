package iot.cpsc319.com.androidapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

import location.LocationHandler;
import mqtt.MqttPublisher;
import mqtt.TopicMsg;
import sensordata.AccelerometerDataSet;
import sensors.AccelerometerHandler;

public class RecordingService extends Service implements Observer,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG =  "SomeApp";

    private long lastUploadTime;
    private long uploadRate; // in milliseconds
    private float  sensitivity = 15; // in m/(s^2)
    private int step = 0;
    private Pair<Double, Double> latlng;

    private final String clientId = getSerialNumber();
    private LocalBinder mBinder = new LocalBinder();
    private WeakReference<MainActivity> mMainActivity;
    private AccelerometerHandler accelerometer;
    private AccelerometerDataSet dataSet;
    private MqttPublisher mqttPublisher;
    private LocationHandler gps;



    public class LocalBinder extends Binder {
        WeakReference<RecordingService> getService() {
            return new WeakReference<>(RecordingService.this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "RecordingService Start");
        retrievePreferences(true);

        // setup publisher
        // String clientId = getSerialNumber();
        mqttPublisher = new MqttPublisher(clientId, getApplicationContext());

        //setup GPS
        gps = new LocationHandler();
        gps.addObserver(this);

        // setup accelerometer
        SensorManager mSensorManager = (SensorManager) getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        accelerometer = new AccelerometerHandler(mSensorManager, sensitivity);
        accelerometer.addObserver(this);
        dataSet = new AccelerometerDataSet();
        lastUploadTime = System.currentTimeMillis();
        accelerometer.start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        flush();
        accelerometer.stop();
        Log.i(TAG, "RecordingService onDestroy");
    }

    @Override
    public void update(Observable observable, Object data) {

        if (observable instanceof AccelerometerHandler &&
                data instanceof AccelerometerDataSet.AccelDataPoint) {
            AccelerometerDataSet.AccelDataPoint point = (AccelerometerDataSet.AccelDataPoint) data;

            dataSet.addDataPoint(point);
            if (point.isStep)
                incrementStep();

            long currTime = System.currentTimeMillis();
            if (currTime - lastUploadTime > uploadRate) {
                lastUploadTime = currTime;
                flush();
            }
        } else if (observable instanceof LocationHandler && data instanceof Pair) {
            //latlng = (Pair<Double, Double>) data;
        } else {
            throw new Error("RecordingService received incorrect data type: " + data.getClass());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        retrievePreferences(false);
    }

    private void retrievePreferences(boolean needRegistration) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (needRegistration)
            pref.registerOnSharedPreferenceChangeListener(this);
        String defaultUploadRate = getString(R.string.default_upload_rate);
        String defaultSensitivity = getString(R.string.default_upload_rate);
        uploadRate = Integer.parseInt(
                pref.getString(getString(R.string.saved_upload_rate), defaultUploadRate));
        sensitivity = Float.parseFloat(
                pref.getString(getString(R.string.saved_sensitivity), defaultSensitivity));
        if (accelerometer != null)
            accelerometer.setSensitivity(sensitivity);
    }

    private void flush() {
        if (dataSet.isEmpty())
            return;
        AccelerometerDataSet.AccelDataPoint point;
        StringBuilder msg = new StringBuilder();
        float x, y, z;
        long time;
        while ((point = dataSet.removeDataPoint()) != null) {
            x = point.getX();
            y = point.getY();
            z = point.getZ();
            time = point.getTime();
            /*
            String loc;
            if (latlng != null) {
                loc = latlng.first + "," + latlng.second;
            } else {
                loc = "null,null";
            }
            */
            msg.append(clientId).append(",").append(time).append(",").append(x).append(",")
                    .append(y).append(",").append(z).append(",").append("None, None");
        }
        String topic = "hello/world";
        mqttPublisher.publish(new TopicMsg(topic, msg.toString()));
    }

    public void setMainActivity(WeakReference<MainActivity> act) {
        mMainActivity = act;
    }

    private void incrementStep() {
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
