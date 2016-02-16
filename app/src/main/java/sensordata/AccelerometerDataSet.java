package sensordata;


import java.util.Deque;
import java.util.LinkedList;

public class AccelerometerDataSet extends SensorDataSet {

    private Deque<AccelDataPoint> data = new LinkedList<>();

    public AccelerometerDataSet(){
    }

    public void addDataPoint(float x, float y, float z, long time){
        getData().add(new AccelDataPoint(x, y, z, time));
    }

    public Deque<AccelDataPoint> getData(){
        return this.data;
    }

    public class AccelDataPoint {
        float x;
        float y;
        float z;
        long time; // in milliseconds since epoch

        public AccelDataPoint(float x, float y, float z, long time){
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
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
