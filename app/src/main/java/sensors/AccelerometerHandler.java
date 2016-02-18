package sensors;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import mqtt.TopicMsg;
import sensordata.AccelerometerDataSet;
import sensordata.SensorDataSet;

public class AccelerometerHandler extends SensorHandler {

    private AccelerometerDataSet data;
    private TextView xView, yView, zView;

    // hold "current" values we can send to the UI thread for updating on the screen
    float currX;
    float currY;
    float currZ;

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
        String topic = "hello/world";
        String msg = "x: " + x + " y: " + y + " z: " + z;
        setChanged();
        notifyObservers(new TopicMsg(topic, msg));
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
        currX = top.getX();
        currY = top.getY();
        currZ = top.getZ();

        // send a UI update requests to the UI thread
        System.out.println("*** updating screen: " + currX + " " + currY + currZ);
        xView.post(new Runnable() {
            @Override
            public void run() {
                xView.setText(String.format("X axis\t\t%f", currX));
            }
        });

        yView.post(new Runnable() {
            @Override
            public void run() {
                yView.setText(String.format("Y axis\t\t%f", currY));
            }
        });

        zView.post(new Runnable() {
            @Override
            public void run() {
                zView.setText(String.format("Z axis\t\t%f", currZ));
            }
        });
    }

    @Override
    public void setViews(View... views) {
        this.xView = (TextView) views[0];
        this.yView = (TextView) views[1];
        this.zView = (TextView) views[2];
    }

}
