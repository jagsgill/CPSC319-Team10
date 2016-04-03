package mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

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
    //private static final String BROKER_URL = "tcp://130.211.153.252:1883";  // google cloud
    //private static final String USERNAME = "ehcxlgcl";
    //private static final String PASSWORD = "AQsUmTw6wYee";
    private static final String BROKER_URL = "ssl://54.92.237.174:27981"; // mqtt cloud
    private static final String USERNAME = "ehcxlgcl";
    private static final String PASSWORD = "AQsUmTw6wYee";

    private ConnectivityManager connMgr;
    private String clientId;
    private MqttAsyncClient client;
    private MqttConnectOptions connectOptions;

    public MqttPublisher(String clientId, Context parentContext){
        this.clientId = clientId;
        connMgr = (ConnectivityManager) parentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        setConnectOptions();
        setupClient();
    }

    // startConnection must be called before publish
    public void publish(TopicMsg tm) throws ConnectivityException {
        // TODO: app freezes at startup if no wifi
        // make sure we're connected to the internet
        if (networkAvailable()) {
            // if connected: start connection, publish to broker, then close connection
            // startConnection();
            String topic = tm.getTopic();
            String _msg = tm.getMsg();
            MqttMessage msg = new MqttMessage(_msg.getBytes());
            try {
                IMqttToken sendToken = client.publish(topic, msg);
                sendToken.waitForCompletion();
                Log.i(TAG, "message sent");
            } catch (MqttException e) {
                throw new ConnectivityException(e);
            }
        } else {
            throw new ConnectivityException("Network unavailable");
        }
    }

    public void startConnection() throws ConnectivityException {
        if (! networkAvailable())
            throw new ConnectivityException("Network unavailable");

        IMqttToken connectToken = null;
        try {
            connectToken = client.connect(connectOptions);
            connectToken.waitForCompletion();
        } catch (MqttException e) {
            String msg = e.getMessage();
            if (connectToken != null){
                msg += "\t" + connectToken.getResponse();
            }
            throw new ConnectivityException(msg);
        }

        client.setCallback(this);
    }

    public void stopConnection() throws ConnectivityException {
        try {
            client.disconnect();
        } catch (MqttException e) {
            throw new ConnectivityException(e.getMessage());
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

    public void setConnectOptions() {
        connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            connectOptions.setSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

    }

    private boolean networkAvailable() {
        NetworkInfo info = connMgr.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}

