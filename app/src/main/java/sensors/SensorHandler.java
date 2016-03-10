package sensors;


import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

import java.util.Observable;

import sensordata.SensorDataSet;

public abstract class SensorHandler extends Observable implements SensorEventListener {

    protected SensorManager sensorManager;
    protected Sensor sensor;

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public abstract void start();

    public abstract void stop();
}
