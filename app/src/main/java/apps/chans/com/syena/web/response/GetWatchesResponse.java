package apps.chans.com.syena.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sitir on 13-02-2017.
 */

public class GetWatchesResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6732734458240496034L;

    private String email;

    private List<Entry> watchEntries;

    public GetWatchesResponse() {
    }

    public GetWatchesResponse(String email, List<Entry> watchMembers) {
        this.email = email;
        this.watchEntries = watchMembers;
    }

    /**
     * @return the watchEntries
     */
    public List<Entry> getWatchMembers() {
        return watchEntries;
    }

    /**
     * @param watchMembers the watchEntries to set
     */
    public void setWatchMembers(List<Entry> watchMembers) {
        this.watchEntries = watchMembers;
    }

    public void addEntry(String email, String name, boolean enabled, boolean watchActive) {
        if (watchEntries == null)
            watchEntries = new ArrayList<>();
        watchEntries.add(new Entry(email, name, enabled, watchActive));

    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public static class Entry implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -8090556785484270675L;
        @JsonProperty("email")
        String email;
        @JsonProperty("name")
        String name;
        @JsonProperty("enabled")
        boolean enabled;
        @JsonProperty
        boolean watchActive;

        public Entry() {
        }

        public Entry(String email, String name, boolean enabled, boolean watchActive) {
            this.email = email;
            this.name = name;
            this.enabled = enabled;
            this.watchActive = watchActive;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "{email: \"" + email + "\", name: " + name + "\"}";
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

        /**
         * @return the enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled the enabled to set
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isWatchActive() {
            return watchActive;
        }

        public void setWatchActive(boolean watchActive) {
            this.watchActive = watchActive;
        }
    }

}
