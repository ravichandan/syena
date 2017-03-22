package apps.chans.com.syena.entities;

import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.Serializable;

import apps.chans.com.syena.LocationFetchRestTask;
import apps.chans.com.syena.R;

/**
 * Created by sitir on 25-01-2017.
 */

public class Watch implements Comparable<Watch>,Serializable {

    private Member source;

    private Member target;

    private boolean active;

    private WatchConfiguration watchConfiguration;

    private transient WatchStatus watchStatus;

    private transient ViewHolder viewHolder;

    private String nickName;

    public Watch(Member source, Member target) {
        this.source = source;
        this.target = target;
        watchStatus = new WatchStatus();
        watchConfiguration = new WatchConfiguration();
    }

    public ViewHolder getViewHolder() {
//        if (viewHolder != null)
  //          viewHolder.startSwitch.setChecked(active);
        return viewHolder;
    }

    public void setViewHolder(ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    public void removeViewHolder() {
        this.viewHolder = null;
    }

    public void initViewHolder(View view) {
        this.viewHolder = new ViewHolder(view);
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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
    public boolean equals(Object obj) {

        if (!(obj instanceof Watch)) return false;
        Watch that = (Watch) obj;
        if (!this.source.equals(that.source)) return false;
        if (!this.target.equals(that.target)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Watch: " + source + " -> " + target + ", active : " + active;
    }

    public class ViewHolder {
        public Switch startSwitch;
        public TextView statusTextView;
        public TextView displayNameTextView;
        public View view;
        public boolean childViewExpanded;
        public ImageView compassPointer;
        public LocationFetchRestTask locationFetchRestTask;

        public ViewHolder(View view) {
            this.view = view;
            this.startSwitch = (Switch) view.findViewById(R.id.startWatchSwitch);
            this.statusTextView = (TextView) view.findViewById(R.id.smallStatusText);
            this.displayNameTextView = (TextView) view.findViewById(R.id.displayNameText);

        }

    }
}
