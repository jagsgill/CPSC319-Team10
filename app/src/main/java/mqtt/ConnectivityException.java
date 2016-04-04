package mqtt;

public class ConnectivityException extends Exception {

    public ConnectivityException(Exception e) {
        super(e);
    }

    public ConnectivityException(String msg) {
        super(msg);
    }
}
