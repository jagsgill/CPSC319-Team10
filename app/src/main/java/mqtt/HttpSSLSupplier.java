package mqtt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.security.cert.X509Certificate;

/**
 * This class obtains the required SSL files using an MQTT subscriber.
 */
public class HttpSSLSupplier extends SSLSupplier {

    private String urlCaCertificate = "https://storage.googleapis.com/ssl-team10-cs319/ca.crt";

    public HttpSSLSupplier() {
        super();
    }

    @Override
    protected Certificate getCACertificate() {

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        // Get CA certificate rom https://storage.googleapis.com/ssl-team10-cs319/ca.crt
        InputStream caInput = null;
        try {
            URL urlCACertificate = new URL(this.urlCaCertificate);
            byte[] certData = urlCACertificate.getFile().getBytes();
            caInput = new ByteArrayInputStream(new byte[4096]);
            caInput.read(certData);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                caInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Certificate ca = null;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("Received CA certificate from: " + urlCaCertificate);
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return ca;
    }

}
