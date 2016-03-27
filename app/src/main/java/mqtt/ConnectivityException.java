package mqtt;

public class ConnectivityException extends Exception {

    public ConnectivityException(Exception e) {
        super(e.getMessage());
    }

    public ConnectivityException(String msg) {
        super(msg);
    }
}
