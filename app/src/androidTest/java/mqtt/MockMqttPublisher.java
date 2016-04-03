package mqtt;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.ArrayList;
import java.util.List;

import mqtt.MqttPublisher;

/**
 *
 *  This class behaves exactly as MqttPublisher except:
 *      - it goes through a list of public test brokers trying to successfully connect to one
 *        (since public brokers may not be available when needed!)
 *
 */
public class MockMqttPublisher extends MqttPublisher {

    private List<String> brokerUrls = new ArrayList<>();

    public MockMqttPublisher(String clientId, Context parentContext) {
        super(clientId, parentContext);

        // public brokers found via Google search:
        brokerUrls.add("tcp://test.mosquitto.org:1883");
        brokerUrls.add("tcp://broker.hivemq.com:1883");
        brokerUrls.add("broker.mqttdashboard.com:1883");
    }

//    @Override
//    public void setupClient(){
//        for(int i = 0; i < brokerUrls.size(); i++) {
//            //super.setBrokerUrl(this.brokerUrls.get(i));
//            super.setupClient();
//            // if successfully connected to a broker, then don't look at the others
//            if (super.getClient() != null){
//                return;
//            }
//        }
//
//        // if we reach here, we could not connect to any brokers
//        throw new Error("Could not connect to any brokers!");
//    }
}
