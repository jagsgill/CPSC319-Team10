package iot.cpsc319.com.androidapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.reflect.Method;

import mqtt.MqttPublisher;
import sensors.AccelerometerHandler;
import sensors.SensorHandler;

// TODO check android target api (20 or 21 okay?)
public class MainActivity extends ActionBarActivity {

    private SensorManager sensorManager;
    private SensorHandler accelHandler;
    private String clientId;
    private MqttPublisher mqttPublisher;
    private int VERY_LONG_SENSOR_DELAY = 1 * 1000000; // in microseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // boilerplate setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get a clientId for this device
        this.clientId = getSerialNumber();

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.mqttPublisher = new MqttPublisher(clientId, this);

        // set up sensor handlers
        setupSensorHandlers();

        setupUserInterface();

        // set up the MqttPublisher for sending data
        setupPublisher();
    }

    private void setupPublisher(){
        mqttPublisher.setBrokerUrl("tcp://test.mosquitto.org:1883");
        mqttPublisher.setupClient();

        // add all classes that generate publishable data to publisher's list of observables
        mqttPublisher.addObservable(getAccelHandler());

        // then add publisher as observer for all these data generators
        mqttPublisher.setupObserver();
    }

    private void setupSensorHandlers(){
        this.accelHandler = new AccelerometerHandler(getSensorManager());
    }

    private void setupUserInterface(){
        // send sensor UI elements to their handlers
        TextView xView = (TextView) findViewById(R.id.xval);
        TextView yView = (TextView) findViewById(R.id.yval);
        TextView zView = (TextView) findViewById(R.id.zval);
        getAccelHandler().setViews(xView, yView, zView);

        // mqtt UI elements
        TextView screenLog = (TextView) findViewById(R.id.screenLog);
        this.mqttPublisher.setView(screenLog);
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

    protected void onResume() {
        super.onResume();

        // register all listeners for sensors when app resumes running
        // use a very long sensor delay for now... to keep traffic to public test broker low!
        Sensor accel = getAccelHandler().getSensor();
        getSensorManager().registerListener(getAccelHandler(), accel, VERY_LONG_SENSOR_DELAY);
    }

    protected void onPause() {
        super.onPause();

        // unregister all sensor listeners when app is paused
        getSensorManager().unregisterListener(getAccelHandler());
    }

    /**
     * Returns the unique serial number of the device.
     * More info at {@link 'http://developer.samsung.com/technical-doc/view.do?v=T000000103'}
     */
    private String getSerialNumber(){
        String serialnum = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class );
            serialnum = (String)(   get.invoke(c, "ro.serialno", "unknown" )  );
        }
        catch (Exception ignored)
        {
            // we should not reach here
            // there should be a serial number available for all watches used...
        }
        return serialnum;
    }
}
