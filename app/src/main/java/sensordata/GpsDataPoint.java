package sensordata;

public class GpsDataPoint extends SensorDataPoint {
    public final double lat;
    public final double lng;

    public GpsDataPoint(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public StringBuilder toStringBuilder() {
        return new StringBuilder()
                .append(timeStamp)
                .append(',')
                .append(lat)
                .append(',')
                .append(lng);
    }
}
