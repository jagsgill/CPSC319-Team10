package mqtt;

/**
 * Created by j on 2016-02-16.
 */
public class TopicMsg {

    private String topic;
    private String msg;
    private int qos;
    private boolean retained;

    public TopicMsg(String topic, String msg) {
        this.topic = topic;
        this.msg = msg;
        this.qos = 0;
        this.retained = false;
    }

    public TopicMsg(String topic, String msg, int qos, boolean retained){
        this.topic = topic;
        this.msg = msg;
        this.qos = qos;
        this.retained = retained;
    }

    public String getTopic() {
        return topic;
    }

    public String getMsg() {
        return msg;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }
}
