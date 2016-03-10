package sensordata;


import java.util.Deque;
import java.util.LinkedList;

public class AccelerometerDataSet extends SensorDataSet {

    private Deque<AccelDataPoint> data = new LinkedList<>();

    public AccelerometerDataSet(){
    }

    public boolean isEmpty() {
        return data.peek() == null;
    }

    public void addDataPoint(AccelDataPoint point){
        getData().add(point);
    }

    public AccelDataPoint removeDataPoint() {
        return data.poll();
    }

    public Deque<AccelDataPoint> getData(){
        return this.data;
    }

    public static class AccelDataPoint {
        float x;
        float y;
        float z;
        long time; // in milliseconds since epoch
        public boolean isStep;
        public double lat;
        public double lng;

        public AccelDataPoint(float x, float y, float z, long time, boolean isStep){
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
            this.isStep = isStep;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public long getTime() {
            return time;
        }
    }

}
