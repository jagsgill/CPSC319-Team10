package sensordata;


import java.util.ArrayList;
import java.util.List;

public class AccelerometerData {

    private List<AccTuple> data = new ArrayList<AccTuple>();

    public AccelerometerData(){
    }

    public void addDataPoint(float x, float y, float z, long time){
        getData().add(new AccTuple(x, y, z, time));
    }

    public List<AccTuple> getData(){
        return this.data;
    }

    public class AccTuple {
        float x;
        float y;
        float z;
        long time; // in milliseconds since epoch

        public AccTuple(float x, float y, float z, long time){
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }
    }

}
