package mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;

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
public class MqttPublisher implements MqttCallback {
    static final String TAG = "SomeApp";
    // Our own brokers:
    // unencrypted: tcp://130.211.153.252:1883
    // encrypted: tcp://130.211.153.252:8883
    private static final String BROKER_URL = "tcp://130.211.153.252:1883";  // google cloud
    private static final String USERNAME = "ehcxlgcl";
    private static final String PASSWORD = "AQsUmTw6wYee";
//    private static final String BROKER_URL = "tcp://54.92.237.174:17981"; // mqtt cloud
//    private static final String USERNAME = "ehcxlgcl";
//    private static final String PASSWORD = "AQsUmTw6wYee";

    private String clientId;
    private MqttAsyncClient client;
    private Context parentContext;
    private MqttConnectOptions connectOptions;
    private Deque<String> msqQueue = new LinkedList<>();
    private String log = "";

    private TextView view;

    public MqttPublisher(String clientId, Context parentContext){
        this.clientId = clientId;
        this.parentContext = parentContext;
        this.connectOptions = new MqttConnectOptions();
        setConnectOptions(getConnectOptions());
        setupClient();
    }

    public void publish(TopicMsg tm){
        // TODO: app freezes at startup if no wifi
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
                //updateScreen(String.format("Sending msg %d:\n    %s\n", ++msgCount, _msg));
                Log.i(TAG, "message sent");
            } catch (MqttException e) {
                System.out.println("Error while sending msg: " + e.getMessage());
                Log.i(TAG, "Error while sending msg: " + e.getMessage());
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
            Log.i(TAG, "Problem connecting: " + e.getMessage());
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
        MqttMessage msg;
        try {
            msg = token.getMessage();
            if (msg == null){
                System.out.println("Message delivered.");
                Log.i(TAG, "Message delivered.");
                //updateScreen("    Successfully delivered.\n");
            } else {
                System.out.println("Delivering message: " + msg.toString());
                Log.i(TAG, "Delivering message: " + msg.toString());
            }
        } catch (MqttException e) {
            e.printStackTrace();
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
                setClient(new MqttAsyncClient(BROKER_URL, clientId, mp));
            } catch (MqttException e) {
                System.out.println("Problem setting the MqttAsyncClient");
            }
        } else {
            throw new Error("Broker url and/or port are null");
        }
    }

    public String getBrokerUrl() {
        return BROKER_URL;
    }

    public MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    public void setConnectOptions(MqttConnectOptions connectOptions) {
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());
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

