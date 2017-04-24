package apps.chans.com.syena.web.response;

/**
 * Created by sitir on 24-04-2017.
 */

public class CloudinaryConfigResponse {

    private String cloudName;

    private int key;

    private String secret;

    private boolean timeStamp;

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(boolean timeStamp) {
        this.timeStamp = timeStamp;
    }
}
