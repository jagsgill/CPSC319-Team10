package iot.cpsc319.com.androidapp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.ref.WeakReference;

import mqtt.ConnectivityException;
import mqtt.MqttPublisher;
import mqtt.TopicMsg;
import sensors.AccRecorder;
import sensors.GpsRecorder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RecordingServiceTest {

    @Mock(name = "mqttPublisher")
    private MqttPublisher mqttPublisherMock;
    @Mock(name = "mMainActivity")
    private WeakReference<MainActivity> mMainActivityMock;
    @Mock(name = "accelerometer")
    private AccRecorder accelerometerMock;
    @Mock(name = "gps")
    private GpsRecorder gpsMock;

    @InjectMocks
    private RecordingService recordingService = new RecordingService();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testUpdate_mqttPublishedMsg() throws ConnectivityException {
        doReturn(true).when(gpsMock).hasData();

        recordingService.update();

        verify(mqttPublisherMock, times(1)).publish(any(TopicMsg.class));
        verify(accelerometerMock, times(1)).retrieveData();
        verify(gpsMock, times(1)).retrieveData();

    }

    @Test
    public void testOnDestroy() throws ConnectivityException {
        recordingService.onDestroy();

        verify(accelerometerMock, times(1)).stop();
        verify(gpsMock, times(1)).stop();
        verify(mqttPublisherMock, times(1)).stopConnection();

    }


}

