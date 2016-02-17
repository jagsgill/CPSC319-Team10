package mqtt;

/**
 * Created by j on 2016-02-16.
 */
public class TopicMsg {

    private String topic;
    private String msg;

    public TopicMsg(String topic, String msg) {
        this.topic = topic;
        this.msg = msg;
    }

    public String getTopic() {
        return topic;
    }

    public String getMsg() {
        return msg;
    }
}
