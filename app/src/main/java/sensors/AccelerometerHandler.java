package sensors;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import sensordata.AccelerometerData;

public class AccelerometerHandler extends SensorHandler {

    private AccelerometerData data;

    public AccelerometerHandler(SensorManager sm) {
        super();
        super.setSensorManager(sm);
        super.setSensor(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        this.data = new AccelerometerData();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long currTime = System.currentTimeMillis();
        getData().addDataPoint(x, y ,z, currTime);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something if sensor accuracy changes
    }

    public AccelerometerData getData() {
        return this.data;
    }

}
