package mqtt;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 *  Unit tests for MqttPublisher class. The number of sensors sending data to be published
 *  is modeled using mock data generators.
 *
 *  REQUIRES:
 *      - AN INTERNET CONNECTION,
 *      - at least one of the public testing brokers is available
 *      - the topic strings passed to data generators are unique (3rd party messages posted
 *            to the the test topics will cause test failure!!
 */
@RunWith(AndroidJUnit4.class)
public class MqttPublisherTest {

    // *********
    // Set-up an MqttPublisher
    private String clientId = "___TestClient___";
    private Context parentContext = new MockContext();

    private MqttPublisher publisher;

    // *********
    // Objects that will send data to the MqttPublisher

    private int numGenerators = 10;
    private List<Observable> generators = new ArrayList<>();
    private String baseTopic = "319.iot/";


    @Before
    public void initialSetup(){
        publisher = new MockMqttPublisher(clientId, parentContext);

        // build the data generators
        generators.clear();
        for (int i = 0; i < numGenerators; i++){
            String topic = baseTopic + i;
            generators.add(new MockDataGenerator(topic));
        }

        //publisher.setupObserver();
        publisher.setupClient();
        System.out.println("done setup!");
    }

    @Test
    public void sanityCheck() {
        Assert.assertEquals(1, 1);
    }

}
