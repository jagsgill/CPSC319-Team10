package sensordata;

public class AccDataPoint extends SensorDataPoint {
    public final float x;
    public final float y;
    public final float z;

    public AccDataPoint(float x, float y, float z, long time) {
        super(time);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public StringBuilder toStringBuilder() {
        return new StringBuilder()
                .append(timeStamp)
                .append(',')
                .append(x)
                .append(',')
                .append(y)
                .append(',')
                .append(z);
    }
}
