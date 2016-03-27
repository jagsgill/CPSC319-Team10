package sensordata;

import java.util.ArrayDeque;
import java.util.Deque;

// can get rid of this class !!!
public class SensorDataSet<T extends SensorDataPoint> {
    private Deque<T> data = new ArrayDeque<>();

    public boolean isEmpty() {
        return data.peek() == null;
    }

    public void addDataPoint(T point){
        data.add(point);
    }

    public T popDataPoint() {
        return data.poll();
    }

    public String flushToString() {
        if (isEmpty())
            return null;

        StringBuilder sb = new StringBuilder();
        T point;
        while ((point = popDataPoint()) != null) {
            sb.append(point.toStringBuilder()).append('$');
        }
        return sb.toString();
    }
}
