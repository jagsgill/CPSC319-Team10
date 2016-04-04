package mqtt;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/*
 * This class is adapted from the one provided by Sharon Ben Asher at https://gist.github.com/sharonbn/4104301,

 * The Mosquitto broker is incompatible with the javax.net.SocketFactory classes.
 * This class will create a custom SocketFactory subclass that will work with Mosquitto broker.
 *
 * Changes made:
 *  - Download certificates/keys using asynchronous HTTP requests
 *  - Use Spongy Castle instead of Bouncy Castle due to some library conflict between
 *    Android and Bouncy Castle
 *
 */
public class SSLSupplier {

    private Context parentContext;
    private final String password = "";

    private String TAG = "SomeApp";
    private String urlCaCertificate = "https://storage.googleapis.com/ssl-team10-cs319/ca.crt";
    private String urlClientCertificate = "https://storage.googleapis.com/ssl-team10-cs319/client.pem";
    private String urlClientKey = "https://storage.googleapis.com/ssl-team10-cs319/client.key";

    public SSLSupplier(Context parentContext){
        this.parentContext = parentContext;
    }

    private void getFile(String url, String filepath) throws ConnectivityException {

        DownloadHttpFileTask task = new DownloadHttpFileTask();
        task.execute(url, filepath);
        try {
            Object result = task.get();

            if (result instanceof Exception) {
                throw new ConnectivityException(((Exception) result).getMessage());
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private String getPath(String filename){
        return parentContext.getFilesDir() + "/" + filename;
    }

    public SSLSocketFactory getSocketFactory () throws ConnectivityException, IOException {

        Provider provider = new org.spongycastle.jce.provider.BouncyCastleProvider();
        Security.insertProviderAt(provider, 1);

        JcaX509CertificateConverter certificateConverter =
                new JcaX509CertificateConverter().setProvider(provider);
        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter();

        // load CA certificate
        String caCertFile = getPath("ca.crt");
        getFile(urlCaCertificate, caCertFile);
        System.out.println("### \n" + caCertFile);

        PEMParser reader = new PEMParser(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(caCertFile))));
        X509CertificateHolder caCertHolder = (X509CertificateHolder)reader.readObject();
        X509Certificate caCert = null;
        try {
            caCert = certificateConverter.getCertificate(caCertHolder);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        reader.close();

        // load client certificate
        String clientCertFile = getPath("client.pem");
        getFile(urlClientCertificate, clientCertFile);
        System.out.println("### \n" + caCertFile);

        reader = new PEMParser(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(clientCertFile))));
        X509CertificateHolder clientCertHolder = (X509CertificateHolder)reader.readObject();
        X509Certificate clientCert = null;
        try {
            clientCert = certificateConverter.getCertificate(clientCertHolder);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        reader.close();

        // load client private key
        String clientKeyFile = getPath("client.key");
        getFile(urlClientKey, clientKeyFile);
        System.out.println("### \n" + caCertFile);

        reader = new PEMParser(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(clientKeyFile))));
        PrivateKeyInfo key = (PrivateKeyInfo)reader.readObject();
        PrivateKey privateKey = keyConverter.getPrivateKey(key);
        reader.close();

        // CA certificate is used to authenticate server
        SSLContext context = null;
        try {
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKs);

            // client key and certificates are sent to server so it can authenticate us
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", clientCert);
            ks.setKeyEntry("private-key", privateKey, password.toCharArray(), new java.security.cert.Certificate[]{clientCert});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());

            // finally, create SSL socket factory
            context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (CertificateException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new ConnectivityException(e.getMessage());
        }

        return context.getSocketFactory();
    }

    private class DownloadHttpFileTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            Exception exception = null;
            try {
                downloadFile((String) params[0], (String) params[1]);
            } catch (IOException e) {
                System.out.println("#### SSL exception while downloading file: " + e.getMessage());
                exception = e;
            }
            return exception;
        }

        private void downloadFile(String _url, String filepath) throws IOException {
            // Adapted from http://developer.android.com/training/basics/network-ops/connecting.html
            InputStream is = null;

            URL url = new URL(_url);
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
            String line;

            File file = new File(filepath); // gets rid of any old file at the filepath
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(file, true));

                while ((line = reader.readLine()) != null) {
                    out.write((line + "\n").getBytes());
                    System.out.println(line);
                }

            }finally {
                if (out != null) {
                    out.close();
                }
            }

            is.close();
        }
    }
}