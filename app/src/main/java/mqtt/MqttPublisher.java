package mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * Typical workflow for using this class:
 * 1. Construct an MqttPublisher
 * 2. add all observable classes to {@code toObserve}
 * 2. set connection options: broker url, port
 *
 * Starting and closing the connection are handled at the time of publishing data
 *
 */

public class MqttPublisher implements MqttCallback {

    static final String TAG = "SomeApp";

    public boolean ENCRYPT = true;
    private MqttBrokerConnection brokerConnection;
    private IMqttAsyncClient mqttClient;

    private String clientId;

    private Context parentContext;

    public MqttPublisher(String clientId, Context parentContext){
        this.clientId = clientId;
        this.parentContext = parentContext;
        this.brokerConnection = new MqttBrokerConnection(getParentContext(), getClientId(), this, getIsEncrypted());
    }

    public void startConnection() throws ConnectivityException {
        brokerConnection.startAndWaitForConnectionToBroker();
        this.mqttClient = brokerConnection.getMqttClient();
    }

    public void stopConnection() throws ConnectivityException {
        brokerConnection.stopConnection();
    }

    // startConnection must be called before publish
    public void publish(TopicMsg tm) throws ConnectivityException {
        if (connectedToBroker()) {
            String topic = tm.getTopic();
            String _msg = tm.getMsg();
            MqttMessage msg = new MqttMessage(_msg.getBytes());
            try {
                IMqttToken sendToken = mqttClient.publish(topic, msg);
                sendToken.waitForCompletion();
                Log.i(TAG, "message sent");
            } catch (MqttException e) {
                throw new ConnectivityException(e);
            }
        } else {
            throw new ConnectivityException("Network unavailable");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Lost connection due to: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // for receiving messages, not used at all for now
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        /*
        MqttMessage msg;
        try {
            msg = token.getMessage();
            if (msg == null){
                System.out.println("Message delivered.");
                Log.i(TAG, "Message delivered.");
            } else {
                System.out.println("Delivering message: " + msg.toString());
                Log.i(TAG, "Delivering message: " + msg.toString());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        */
    }

    public boolean getIsEncrypted(){
        return ENCRYPT;
    }

    public boolean setIsEncrypted(){
        return ENCRYPT;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Context getParentContext() {
        return parentContext;
    }

    private boolean connectedToBroker(){
        return brokerConnection.isConnectedToInternet() && brokerConnection.getIsConnectedToBroker();
    }

}

