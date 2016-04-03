package sensordata;

public class BatteryDataPoint extends SensorDataPoint {
    public final int batteryLevel;

    public BatteryDataPoint(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public StringBuilder toStringBuilder() {
        return new StringBuilder()
                .append(timeStamp)
                .append(',')
                .append(batteryLevel);
    }
}
