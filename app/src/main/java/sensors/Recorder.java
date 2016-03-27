package sensors;

import iot.cpsc319.com.androidapp.RecordingService;
import sensordata.SensorDataPoint;
import sensordata.SensorDataSet;

public abstract class Recorder<T extends SensorDataPoint> {
    protected SensorDataSet<T> data = new SensorDataSet<>();
    protected RecordingService service;

    protected Recorder(RecordingService recordingService) {
        this.service = recordingService;
    }

    public String retrieveData() {
        return data.flushToString();
    }

    public boolean hasData() {
        return !data.isEmpty();
    }

    public abstract void start();
    public abstract void stop();
}
