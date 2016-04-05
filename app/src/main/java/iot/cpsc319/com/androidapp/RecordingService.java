package iot.cpsc319.com.androidapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
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

    private static final String TAG = "SomeApp";

    //private final String clientId = getSerialNumber();
    private LocalBinder mBinder = new LocalBinder();
    private WeakReference<MainActivity> mMainActivity;
    private AccRecorder accelerometer;
    private MqttPublisher mqttPublisher;
    private GpsRecorder gps;
    private BatteryRecorder battery;
    private String devId;
    boolean errFlag;

    public class LocalBinder extends Binder {
        WeakReference<RecordingService> getService() {
            return new WeakReference<>(RecordingService.this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "RecordingService Start");

        devId = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        mqttPublisher = new MqttPublisher(devId, getApplicationContext());
        try {
            mqttPublisher.startConnection();
        } catch (ConnectivityException e) {
            errFlag = true;
            Log.i(TAG, e.getMessage());
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
        super.onDestroy();
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
            String msg = "Acceleration: " + accelerometer.retrieveData()
                    + (gps.hasData() ? ("\r\nLocation: " + gps.retrieveData()) : " ")
                    + (battery.hasData() ? ("\r\nBattery Level: " + battery.retrieveData()) : " ");
            mqttPublisher.publish(new TopicMsg("client/watch/"+devId+"/combined",msg));
        } catch (ConnectivityException e) {
            MainActivity act;
            if (mMainActivity != null && (act = mMainActivity.get()) != null) {
                act.buttonOff();
            }
            Toast.makeText(this, "Network dead!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    public void setMainActivity(WeakReference<MainActivity> act) {
        mMainActivity = act;
    }

    public void onStep(int step) {
        step++;
        MainActivity act;
        if (mMainActivity!=null && (act = mMainActivity.get()) != null)
            act.display(Integer.toString(step));
    }

    /**
     * Returns the unique serial number of the device.
     * More info at {@link 'http://developer.samsung.com/technical-doc/view.do?v=T000000103'}
     */
    // todo get unique device number
/*    private String getSerialNumber() {
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
    */
}
