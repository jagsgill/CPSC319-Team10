package sensors;


import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorHandler implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
