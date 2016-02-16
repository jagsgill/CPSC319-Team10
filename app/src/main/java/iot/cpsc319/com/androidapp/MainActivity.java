package iot.cpsc319.com.androidapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import sensors.AccelerometerHandler;
import sensors.SensorHandler;

// TODO check android target api (20 or 21 okay?)
// TODO refactor sensor data fetcher into a different class
public class MainActivity extends ActionBarActivity {

    private TextView tv, tv1, tv2;
    private TextView screenLog;

    private SensorManager sensorManager;
    private SensorHandler accelHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up sensor handlers
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE)
        this.accelHandler = new AccelerometerHandler(getSensorManager());

        // accelerometer UI elements
        tv = (TextView) findViewById(R.id.xval);
        tv1 = (TextView) findViewById(R.id.yval);
        tv2 = (TextView) findViewById(R.id.zval);

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
}
