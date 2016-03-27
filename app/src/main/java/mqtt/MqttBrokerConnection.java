package mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

/**
 * This class is used to manage a connection to an MQTT broker. The network operations are
 * performed asyncronously (on worker threads).
 *
 * Typical usage:
 * 1. Construct a MqttBrokerConnection
 * 2. Call startAndWaitForConnectionToBroker()
 * 3. call getMqttClient to obtain a configured MQTT mqttClient
 * 4. To stop the connection, call stopConnectionToBroker()
 *
 */
public class MqttBrokerConnection {

    private static final String TAG = "SomeApp";

    private String BROKER_URL;
    private String USERNAME = "defaultwatch";
    private String PASSWORD = "vandricowatch";

    private String urlServingBrokerIP =
            "https://storage.googleapis.com/ssl-team10-cs319/broker_ips.txt"; // file containing broker's IP address
    private String brokerInsecurePort = "1883";
    private String brokerSecurePort = "8883";

    private MqttPublisher publisher;
    private MqttConnectOptions connectOptions;
    private IMqttAsyncClient mqttClient;
    private boolean ENCRYPT;

    private Context parentContext;
    private String clientId;

    private boolean isConnectedToBroker;


    public MqttBrokerConnection(Context parentContext, String clientId, MqttPublisher publisher, boolean encrypted){
        this.parentContext = parentContext;
        this.clientId = clientId;
        this.publisher = publisher;
        this.ENCRYPT = encrypted;
        this.connectOptions = createConnectOptions();
        this.isConnectedToBroker = false;
    }

    public void startAndWaitForConnectionToBroker() throws ConnectivityException {

        setBrokerUrl(); // must be done before creating a client
        setMqttClient(createMqttClient());

        ConnectToBrokerTask task = new ConnectToBrokerTask();
        task.execute();
        try {
            ConnectivityException e = (ConnectivityException) task.get(); // wait until connected
            if (e != null){
                throw e;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void stopConnection() throws ConnectivityException {
        try {
            getMqttClient().disconnect();
        } catch (MqttException e) {
            throw new ConnectivityException(e.getMessage());
        }
    }

    private MqttConnectOptions createConnectOptions() {

        MqttConnectOptions connectOptions = new MqttConnectOptions();

        System.out.println("# Starting connect optinos");
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());

        if (ENCRYPT){
            SSLSupplier sslSupplier = new HttpSSLSupplier();
            try {
                sslSupplier.setupSSL();
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
                e.printStackTrace();
            }

            connectOptions.setSocketFactory(sslSupplier.getSslSocketFactory());
        }

        System.out.println("# Reached end of connection options");
        return connectOptions;
    }

    private IMqttAsyncClient createMqttClient(){
        MqttAsyncClient mqttClient = null;

        if (getBrokerUrl() != null){
            try {
                // We can use file or memory persistence, for now use memory since it's a bit simpler
                // must set the app's writable directory, default is root dir. which is not writable!
                //String appCacheDir = getParentContext().getCacheDir().getAbsolutePath();
                //MqttDefaultFilePersistence fp = new MqttDefaultFilePersistence(appCacheDir);
                MemoryPersistence mp = new MemoryPersistence();
                mqttClient = new MqttAsyncClient(BROKER_URL, clientId, mp);
            } catch (MqttException e) {
                System.out.println("Problem setting the MqttAsyncClient");
            }
        } else {
            throw new Error("Broker url and/or port are null");
        }
        System.out.println("### Created mqtt client");
        return mqttClient;
    }

    public boolean isConnectedToInternet(){

        ConnectivityManager connMgr = (ConnectivityManager)
                getParentContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else {
            throw new Error("Null NetworkInfo");
        }
    }

    private String getBrokerUrl() {
        return BROKER_URL;
    }

    private void setBrokerUrl() throws ConnectivityException {
//        BROKER_URL = "ssl://54.92.237.174:27981"; // mqtt cloud

        SetBrokerUrlTask task = new SetBrokerUrlTask();
        task.execute();
        try {
            ConnectivityException e = (ConnectivityException) task.get();
            if (e != null){
                throw e;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    private MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    public IMqttAsyncClient getMqttClient() {
        return mqttClient;
    }

    private void setMqttClient(IMqttAsyncClient mqttAsyncClient) {
        this.mqttClient = mqttAsyncClient;
    }

    public Context getParentContext() {
        return parentContext;
    }

    public boolean getIsConnectedToBroker(){
        return this.isConnectedToBroker;
    }

    public boolean setIsConnectedToBroker(boolean status){
        this.isConnectedToBroker = status;
        return status;
    }

    private class ConnectToBrokerTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            System.out.println("#### Starting async task: connect to broker");
            ConnectivityException exception = null;
            try {
                startConnectionToBroker();
            } catch (ConnectivityException e) {
                exception = e;
            }
            System.out.println("#### Finished async task: connected to broker? " + getIsConnectedToBroker());
            return exception;
        }

        private void startConnectionToBroker() throws ConnectivityException {
            if (!isConnectedToInternet()) {
                throw new ConnectivityException("Network unavailable");
            }

            IMqttToken connectToken = null;
            try {
                connectToken = getMqttClient().connect(getConnectOptions());
                connectToken.waitForCompletion();
                setIsConnectedToBroker(true);
            } catch (MqttException e) {
                String msg = e.getMessage();
                if (connectToken != null) {
                    msg += "\t" + connectToken.getResponse();
                }
                throw new ConnectivityException(msg);
            }

            getMqttClient().setCallback(publisher);
        }
    }

    private class SetBrokerUrlTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            ConnectivityException exception = null;
            System.out.println("#### Starting async task: set broker url");
            try {
                findBrokerUrl();
            } catch (IOException e) {
                exception = new ConnectivityException(e);
            }
            System.out.println("#### Finished async task: set broker url to " + getBrokerUrl());
            return exception;
        }

        private void findBrokerUrl() throws IOException {
            // Adapted from http://developer.android.com/training/basics/network-ops/connecting.html
            InputStream is = null;
            int len = 500;


            URL url = new URL(urlServingBrokerIP);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "Http response when getting broker IP address: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String ipAddress = reader.readLine();

            System.out.println("IP data: " + ipAddress);

            if (ENCRYPT) {
                BROKER_URL = "tcp://" + ipAddress + ":" + brokerSecurePort;
            } else {
                BROKER_URL = "tcp://" + ipAddress + ":" + brokerInsecurePort;
            }

            System.out.println("Broker found at: " + BROKER_URL);

            // Makes sure that the InputStream is closed after the app is
            // finished using it
            is.close();

        }
    }
}
