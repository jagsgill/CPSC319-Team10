package sensors;


import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

import java.util.Observable;

import sensordata.SensorDataSet;

public abstract class SensorHandler extends Observable implements SensorEventListener {

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

    public abstract void updateScreen();

    public abstract void setViews(View... views);

    public abstract SensorDataSet getSensorDataSet();
}
