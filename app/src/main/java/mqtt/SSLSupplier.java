package mqtt;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * This class provides functionality to obtain SSL certificates and keys
 * provided by a broker.
 *
 * Reference: http://developer.android.com/training/articles/security-ssl.html
 *
 * Subclasses will obtain the required files in different ways depending on how the
 * broker supplies them (e.g. via http, https, mqtt, local file storage, etc.)
 *
 * Typical usage:
 * 1. Construct an SSLSupplier with the broker's IP and port.
 * 2. Call the method to obtain all SSL setup files and build an SSLSocketFactory
 * 2. Obtain an SSLSocketFactory that you can supply to your MQTT client.
 *
 */
public abstract class SSLSupplier {

    private SSLSocketFactory sslSocketFactory;
    private Certificate caCert;

    public SSLSupplier() {

    }

    // TODO: handle these exceptions
    public void setupSSL() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {

        System.out.println("Obtaining CA certificate from broker.");
        Certificate caCertificate = getCACertificate();
        System.out.println("Success: received CA certificate.");
        System.out.println("Setting up SSLSocketFactory.");
        KeyStore keyStore = getKeyStore(caCertificate);
        TrustManagerFactory tmf = getTrustManagerFactory(keyStore);
        SSLSocketFactory sf = getSSLSocketFactory(tmf);
        System.out.println("Success: setup SSLSocketFactory.");

        this.sslSocketFactory = sf;
    }

    public SSLSocketFactory getSslSocketFactory(){
        return this.sslSocketFactory;
    }

    abstract Certificate getCACertificate();

    private KeyStore getKeyStore(Certificate ca) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        return keyStore;

    }

    private TrustManagerFactory getTrustManagerFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        return tmf;
    }

    private SSLSocketFactory getSSLSocketFactory(TrustManagerFactory tmf) throws NoSuchAlgorithmException, KeyManagementException {

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

}
