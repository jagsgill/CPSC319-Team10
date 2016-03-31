package sensors;

import android.location.Location;

import org.junit.Test;

import iot.cpsc319.com.androidapp.RecordingService;
import sensordata.GpsDataPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class GpsRecorderTest {
    private static final double TEST_LONGITUDE = 12d;
    private static final double TEST_LATITUDE = 55d;
    RecordingService recordingService = mock(RecordingService.class);
    GpsRecorder gpsRecorder = new GpsRecorder(recordingService);

    @Test
    public void testOnLocationChange_dataAdded() {


        Location location = new Location("provider");
        location.setLongitude(TEST_LONGITUDE);
        location.setLatitude(TEST_LATITUDE);
        gpsRecorder.onLocationChanged(location);

        assertTrue(gpsRecorder.hasData());
        GpsDataPoint dataPointAdded = gpsRecorder.data.popDataPoint();
        assertEquals(TEST_LONGITUDE, dataPointAdded.lng, 0d);
        assertEquals(TEST_LATITUDE, dataPointAdded.lat, 0d);

    }

}