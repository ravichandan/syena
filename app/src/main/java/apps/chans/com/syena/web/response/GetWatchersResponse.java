package apps.chans.com.syena.web.response;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetWatchersResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3358182997365163161L;
    private List<Entry> watchers;

    /**
     * @return the watchers
     */
    public List<Entry> getWatchers() {
        if (watchers == null) watchers = new ArrayList<Entry>();
        return watchers;
    }

    /**
     * @param watchers the watchers to set
     */
    public void setWatchers(List<Entry> watchers) {
        this.watchers = watchers;
    }

    public void addEntry(String email, String name, boolean enabled, String status, Date watchingSince,byte[] profileImage) {
        if (watchers == null)
            watchers = new ArrayList<>();
        watchers.add(new Entry(email, name, enabled, status, watchingSince,profileImage));
    }

    public static class Entry implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -8090556785484270675L;
        @JsonProperty
        String email;
        @JsonProperty
        String name;
        @JsonProperty
        boolean enabled;
        @JsonProperty
        String status;
        @JsonProperty
        Date watchingSince;
        @JsonProperty
        byte[] profileImage;
        transient Bitmap profilePic;
        transient Bitmap profilePicSmall;

        public Entry() {
        }

        Entry(String email, String name, boolean enabled, String status, Date watchingSince,byte[] profileImage) {
            this.email = email;
            this.name = name;
            this.enabled = enabled;
            this.status = status;
            this.watchingSince = watchingSince;
            this.profileImage=profileImage;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
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

        public byte[] getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(byte[] profileImage) {
            this.profileImage = profileImage;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Date getWatchingSince() {
            return watchingSince;
        }

        public void setWatchingSince(Date watchingSince) {
            this.watchingSince = watchingSince;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "{email: \"" + email + "\", name: " + name + "\"}";
        }
    }
}
