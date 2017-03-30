package apps.chans.com.syena.entities;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sitir on 25-01-2017.
 */

public class Member implements Serializable{

    private String displayName;

    private String email;

    private double latitude;
    private double longitude;
    private double altitude;
    private float degree;
    private Map<Member, Watch> watchMap;
    private Bitmap profilePic;
    private Bitmap profilePicSmall;
    public Member(String email) {
        this.email = email;
        watchMap = new HashMap<Member, Watch>();
    }

    public Member(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        watchMap = new HashMap<Member, Watch>();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<Member, Watch> getWatchMap() {
        return watchMap;
    }

    public void setWatchMap(Map<Member, Watch> watchMap) {
        this.watchMap = watchMap;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getDegree() {
        return degree;
    }

    public void setDegree(float degree) {
        this.degree = degree;
    }

    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }

    public Bitmap getProfilePicSmall() {
        return profilePicSmall;
    }

    public void setProfilePicSmall(Bitmap profilePicSmall) {
        this.profilePicSmall = profilePicSmall;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Member)) return false;
        Member that = (Member) obj;
        if (that.email != null && this.email != null)
            return that.email.equals(this.email);
        return false;
    }

    @Override
    public int hashCode() {
        return this.email.hashCode();
    }

    @Override
    public String toString() {
        return "Member " + (displayName == null ? email : displayName);
    }
}
