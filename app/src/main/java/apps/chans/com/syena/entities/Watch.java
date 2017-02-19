package apps.chans.com.syena.entities;

/**
 * Created by sitir on 25-01-2017.
 */

public class Watch implements Comparable<Watch> {

    private Member source;

    private Member target;

    private boolean active;

    private WatchConfiguration watchConfiguration;

    private WatchStatus watchStatus;

    public Watch(Member source, Member target) {
        this.source = source;
        this.target = target;
        watchStatus = new WatchStatus();
        watchConfiguration = new WatchConfiguration();
    }

    public Member getSource() {
        return source;
    }

    public void setSource(Member source) {
        this.source = source;
    }

    public Member getTarget() {
        return target;
    }

    public void setTarget(Member target) {
        this.target = target;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public WatchConfiguration getWatchConfiguration() {
        return watchConfiguration;
    }

    public void setWatchConfiguration(WatchConfiguration watchConfiguration) {
        this.watchConfiguration = watchConfiguration;
    }

    public WatchStatus getWatchStatus() {
        return watchStatus;
    }

    public void setWatchStatus(WatchStatus watchStatus) {
        this.watchStatus = watchStatus;
    }

    @Override
    public int compareTo(Watch o) {
        if (this.active && !o.active)
            return -1;
        if (this.active && o.active)
            return 0;
        return 1;
    }

    @Override
    public String toString() {
        return "target : "+target+", active : "+active;
    }
}
