package sensors;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.TextView;

import sensordata.AccelerometerDataSet;
import sensordata.SensorDataSet;

public class AccelerometerHandler extends SensorHandler {

    private AccelerometerDataSet data;
    private TextView xView, yView, zView;

    public AccelerometerHandler(SensorManager sm) {
        super();
        super.setSensorManager(sm);
        super.setSensor(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        this.data = new AccelerometerDataSet();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long currTime = System.currentTimeMillis();
        getSensorDataSet().addDataPoint(x, y, z, currTime);
        notifyObservers(getSensorDataSet());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something if sensor accuracy changes
    }

    @Override
    public AccelerometerDataSet getSensorDataSet() {
        return this.data;
    }

    @Override
    public void updateScreen() {
        AccelerometerDataSet.AccelDataPoint top = getSensorDataSet().getData().pollFirst();
        xView.setText(String.format("X axis\t\t%f", top.getX()));
        yView.setText(String.format("Y axis" + "\t\t%f", top.getY()));
        zView.setText(String.format("Z axis" + "\t\t%f", top.getZ()));
    }

    @Override
    public void setViews(View... views) {
        this.xView = (TextView) views[0];
        this.yView = (TextView) views[1];
        this.zView = (TextView) views[2];
    }

}