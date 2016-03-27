package sensors;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import iot.cpsc319.com.androidapp.RecordingService;
import sensordata.GpsDataPoint;
import sensors.Recorder;

public class GpsRecorder extends Recorder<GpsDataPoint> implements LocationListener {
    private static final String TAG = "SomeApp";

    private final int MIN_UPDATE_INTERVAL = 10 * 1000; // in milliseconds
    private final int MIN_UPDATE_DISTANCE = 15; // in meters

    LocationManager locationManager;

    public GpsRecorder(RecordingService service) {
        super(service);
    }

    @Override
    public void start() {
        locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_UPDATE_INTERVAL,
                MIN_UPDATE_DISTANCE,
                this);
    }

    @Override
    public void stop() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "GpsRecorder: lat: " + location.getLatitude() + "lng: " + location.getLongitude());
        GpsDataPoint point = new GpsDataPoint(location.getLatitude(), location.getLongitude());
        data.addDataPoint(point);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}
