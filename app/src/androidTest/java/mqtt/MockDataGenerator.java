package mqtt;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import mqtt.TopicMsg;

/**
 *  This class can be used to generate random string data using a base string.
 *
 */
public class MockDataGenerator extends Observable{

    private int count;
    private static int SEND_DELAY = 2000;   // in milliseconds
    Timer timer = new Timer();
    private String topic;

    public MockDataGenerator(String topic) {
        this.count = 1;
        this.topic = topic;

        // Notify observers after `SEND_DELAY forever until `this is destroyed
        TimerTask sendData = new TimerTask(){
            @Override
            public void run() {
                setChanged();
                notifyObservers(generateNextTopicMsg());
            }
        };
        timer.schedule(sendData, SEND_DELAY, SEND_DELAY);
    }

    public TopicMsg generateNextTopicMsg(){
        String msg = String.format("%s_msg%i: %f", topic, count++ , Math.random());
        return new TopicMsg(topic, msg);
    }

}
