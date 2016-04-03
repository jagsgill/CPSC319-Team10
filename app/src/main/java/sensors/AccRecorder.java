package sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.widget.Toast;


import iot.cpsc319.com.androidapp.MainActivity;
import iot.cpsc319.com.androidapp.R;
import iot.cpsc319.com.androidapp.RecordingService;
import mqtt.ConnectivityException;
import mqtt.MqttPublisher;
import mqtt.TopicMsg;
import sensordata.AccDataPoint;

public class AccRecorder extends Recorder<AccDataPoint> implements SensorEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private SensorManager sensorManager;

    private long lastUploadTime = System.currentTimeMillis();
    private long lastStepTime = lastUploadTime;
    private long uploadRate; // in milliseconds
    private float sensitivity; // in m/(s^2)
    private final int MIN_STEP_INTERVAL = 300; // in milliseconds
    private int step = 0;

    public AccRecorder(RecordingService service) {
        super(service);
    }

    @Override
    public void start() {
        retrievePreferences(true);
        sensorManager = (SensorManager) service.getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
        service.update();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long currTime = System.currentTimeMillis();
        AccDataPoint point = new AccDataPoint(x, y, z, currTime);
        data.addDataPoint(point);

        if (currTime - lastUploadTime > uploadRate && !data.isEmpty()) {
            lastUploadTime = currTime;
            service.update();
        }

        if (currTime - lastStepTime > MIN_STEP_INTERVAL) {
            double net = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            if (net > sensitivity && service != null) {
                lastStepTime = currTime;
                service.onStep(++step);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something if sensor accuracy changes
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        retrievePreferences(false);
    }

    private void retrievePreferences(boolean needRegistration) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(service);
        if (needRegistration)
            pref.registerOnSharedPreferenceChangeListener(this);
        String defaultUploadRate = service.getString(R.string.default_upload_rate);
        String defaultSensitivity = service.getString(R.string.default_upload_rate);
        uploadRate = Integer.parseInt(
                pref.getString(service.getString(R.string.saved_upload_rate), defaultUploadRate));
        sensitivity = Float.parseFloat(
                pref.getString(service.getString(R.string.saved_sensitivity), defaultSensitivity));
    }
}
