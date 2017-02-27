package apps.chans.com.syena.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
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

    public void addEntry(String email, String name, boolean enabled) {
        if (watchers == null)
            watchers = new ArrayList<>();
        watchers.add(new Entry(email, name, enabled));
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

        public Entry() {
        }

        Entry(String email, String name, boolean enabled) {
            this.email = email;
            this.name = name;
            this.enabled = enabled;
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

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "{email: \"" + email + "\", name: " + name + "\"}";
        }
    }
}
