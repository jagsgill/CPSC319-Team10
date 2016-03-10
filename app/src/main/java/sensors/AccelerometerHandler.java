package sensors;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import sensordata.AccelerometerDataSet;

public class AccelerometerHandler extends SensorHandler {
    private float sensitivity; // in m/(s^2)
    private long lastStepTime = 0;
    private final int StepMinInterval = 300; // in milliseconds

    public AccelerometerHandler(SensorManager sm, float sensitivity) {
        super();
        super.setSensorManager(sm);
        super.setSensor(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        this.sensitivity = sensitivity;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long currTime = System.currentTimeMillis(); // !!!
        boolean isStep = false;
        if (currTime - lastStepTime > StepMinInterval) {
            double net = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            isStep = net > sensitivity;
            if (isStep)
                lastStepTime = currTime;
        }
        setChanged();
        AccelerometerDataSet.AccelDataPoint data =
                new AccelerometerDataSet.AccelDataPoint(x, y, z, currTime, isStep);
        notifyObservers(data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something if sensor accuracy changes
    }

    @Override
    public void start() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        lastStepTime = System.currentTimeMillis();
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void setSensitivity(float f) {
        sensitivity = f;
    }

}
