package apps.chans.com.syena.web.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sitir on 13-02-2017.
 */

public class GetWatchesResponse implements  Serializable{

    private String email;

    private List<GetWatchesResponse.Member> watchMembers;

    /**
     * @return the watchMembers
     */
    public List<GetWatchesResponse.Member> getWatchMembers() {
        return watchMembers;
    }

    /**
     * @param watchMembers
     *            the watchMembers to set
     */
    public void setWatchMembers(List<GetWatchesResponse.Member> watchMembers) {
        this.watchMembers = watchMembers;
    }

    public void addEntry(String email, String name) {
        if (watchMembers == null)
            watchMembers = new ArrayList<>();
        watchMembers.add(new GetWatchesResponse.Member(email, name));

    }

    public class Member implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -8090556785484270675L;
        String email;
        String name;

        Member(String email, String name) {
            this.email = email;
            this.name = name;
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
    }

}
