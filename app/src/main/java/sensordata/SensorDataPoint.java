package sensordata;

public abstract class SensorDataPoint {
    public final long timeStamp;

    public SensorDataPoint() {
        timeStamp = System.currentTimeMillis();
    }

    public SensorDataPoint(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public abstract StringBuilder toStringBuilder();
}
