package mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.ConnectActionListener;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import sensors.SensorHandler;

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
// TODO: app freezes if wifi is lost after it starts
public class MqttPublisher implements MqttCallback, Observer {

    private String brokerUrl;
    private String clientId;
    private MqttAsyncClient client;
    private Context parentContext;
    private MqttConnectOptions connectOptions;
    private Deque<String> msqQueue = new LinkedList<>();
    private String log = "";
    public int msgCount = 0;

    private List<Observable> toObserve = new ArrayList<>(); // sensor handler classes are added here
    private TextView view;

    public MqttPublisher(String clientId, Context parentContext){
        this.clientId = clientId;
        this.parentContext = parentContext;
        this.connectOptions = new MqttConnectOptions();
        setConnectOptions(getConnectOptions());
    }

    public void publish(TopicMsg tm){
        // make sure we're connected to the internet
        ConnectivityManager connMgr = (ConnectivityManager)
                parentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // if connected: start connection, publish to broker, then close connection
            startConnection();
            String topic = tm.getTopic();
            String _msg = tm.getMsg();
            MqttMessage msg = new MqttMessage(_msg.getBytes());
            try {
                IMqttToken sendToken = client.publish(topic, msg);
                sendToken.waitForCompletion();
                System.out.println("Sending message: " + new String(msg.getPayload(), StandardCharsets.UTF_8));
                updateScreen(String.format("Sending msg %d:\n    %s\n", ++msgCount, _msg));
            } catch (MqttException e) {
                System.out.println("Error while sending msg: " + e.getMessage());
                // e.printStackTrace();
            } finally {
                stopConnection();
            }
        } else {
            // just throw away the message for now...
            System.out.println("Not connected. Message not sent.");
        }
    }

    public void startConnection(){
        IMqttToken connectToken = null;
        try {
            connectToken = client.connect(getConnectOptions());
            connectToken.waitForCompletion();
        } catch (MqttException e) {
            System.out.println("Problem connecting.");
            if (connectToken != null){
                System.out.println(connectToken.getResponse());
            }
            // for now, do nothing useful if we can't connect
        }

        client.setCallback(this);
    }

    public void stopConnection(){
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.out.println("Problem disconnecting...");
            // for now, do nothing useful
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
        MqttMessage msg = null;
        try {
            msg = token.getMessage();
            if (msg == null){
                System.out.println("Message delivered.");
                updateScreen("    Successfully delivered.\n");
            } else {
                System.out.println("Delivering message: " + msg.toString());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // adds this publisher as an observer for all observables that send it data to publish
    public void setupObserver() {
        System.out.println("Adding observer to (" + toObserve.size() + ") observables");
        for (Observable o : this.toObserve){
            o.addObserver(this);
        }
    }

    public void addObservable(Observable o){
        System.out.println("Adding " + o.getClass().toString() + " to list of observables");
        toObserve.add(o);
    }

    /**
     * Publishes the message in a TopicMsg to this publisher's broker on the topic specified in
     * the TopicMsg.
     *
     * @param observable is a SensorHandler
     * @param data is a TopicMsg
     */
    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof SensorHandler && data instanceof TopicMsg){
            ((SensorHandler) observable).updateScreen();
            publish((TopicMsg) data);
        } else {
            throw new Error("Tried to publish incorrect data type: " + data.getClass());
        }
    }

    public MqttAsyncClient getClient() {
        return client;
    }

    public void setClient(MqttAsyncClient client) {
        this.client = client;
    }

    public void setupClient(){
        if (getBrokerUrl() != null){
            try {
                // We can use file or memory persistence, for now use memory since it's a bit simpler
                // must set the app's writable directory, default is root dir. which is not writable!
                //String appCacheDir = getParentContext().getCacheDir().getAbsolutePath();
                //MqttDefaultFilePersistence fp = new MqttDefaultFilePersistence(appCacheDir);
                MemoryPersistence mp = new MemoryPersistence();
                setClient(new MqttAsyncClient(getBrokerUrl(), getClientId(), mp));
            } catch (MqttException e) {
                System.out.println("Problem setting the MqttAsyncClient");
            }
        } else {
            throw new Error("Broker url and/or port are null");
        }
    }

    public String getClientId() {
        return clientId;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    public void setConnectOptions(MqttConnectOptions connectOptions) {
        connectOptions.setUserName("ehcxlgcl");
        connectOptions.setPassword("AQsUmTw6wYee".toCharArray());
    }

    public View getView() {
        return view;
    }

    public void setView(TextView v) {
        this.view = v;
        view.setMovementMethod(new ScrollingMovementMethod());
    }

    public Context getParentContext() {
        return parentContext;
    }

    public void updateScreen(String msg){
        // update the log, then send tell the UI thread to update the screen
        updateLog(msg);
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setText(log);
            }
        });
    }

    private void updateLog(String msg){
        if (msqQueue.size() > 6) {
            msqQueue.pollFirst();
        }
        msqQueue.add(msg);
        String newLog = "";
        for (String s : this.msqQueue){
            newLog += s;
        }
        this.log = newLog;
    }
}

