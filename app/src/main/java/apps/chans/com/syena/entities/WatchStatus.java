package apps.chans.com.syena.entities;

/**
 * Created by sitir on 25-01-2017.
 */
public class WatchStatus {

    public static final int IN_ACTIVE=0;

    public static final int IN_RANGE=1;

    public static final int AT_BORDER=2;

    public static final int OUT_OF_RANGE=3;

    public static final int ENDED=9;

    private String message;

    private int code;

    private double distanceApart;

    private String description;

    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDistanceApart() {
        return distanceApart;
    }

    public void setDistanceApart(double distanceApart) {
        this.distanceApart = distanceApart;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
