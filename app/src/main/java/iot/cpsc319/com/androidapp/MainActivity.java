package iot.cpsc319.com.androidapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import sensors.AccelerometerHandler;
import sensors.SensorHandler;

// TODO check android target api (20 or 21 okay?)
public class MainActivity extends ActionBarActivity implements Observer {

    private TextView screenLog;

    private SensorManager sensorManager;
    private SensorHandler accelHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up sensor handlers
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelHandler = new AccelerometerHandler(getSensorManager());

        // set up observables for sensor value changes
        getAccelHandler().addObserver(this);

        // send sensor UI elements to their handlers
        TextView xView = (TextView) findViewById(R.id.xval);
        TextView yView = (TextView) findViewById(R.id.yval);
        TextView zView = (TextView) findViewById(R.id.zval);
        getAccelHandler().setViews(xView, yView, zView);

        // mqtt UI elements
        this.screenLog = (TextView) findViewById(R.id.screenLog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public SensorManager getSensorManager() {
        return this.sensorManager;
    }

    public SensorHandler getAccelHandler(){
        return this.accelHandler;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof SensorHandler){
            ((SensorHandler) observable).updateScreen();
        } else{
            throw new Error("Trying to observe something that's not a SensorHandler: " + observable.getClass());
        }
    }

    protected void onResume() {
        super.onResume();

        // register all listeners for sensors when app resumes running
        Sensor accel = getAccelHandler().getSensor();
        getSensorManager().registerListener(getAccelHandler(), accel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();

        // unregister all sensor listeners when app is paused
        getSensorManager().unregisterListener(getAccelHandler());
    }
}
