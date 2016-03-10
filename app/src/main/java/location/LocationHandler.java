package location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import java.util.Observable;

import mqtt.TopicMsg;

// todo combine location with accelerometer data?
public class LocationHandler extends Observable implements LocationListener {

    Location location;
    private TextView latView;
    private TextView longView;
    private double lat;
    private double lng;

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        lat = location.getLatitude();
        lng = location.getLongitude();
        setChanged();
        notifyObservers(new Pair<>(lat, lng));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
