package apps.chans.com.syena.entities;

/**
 * Created by sitir on 25-01-2017.
 */
public class WatchConfiguration <K,V>{

    private int safeDistance=1;

    private int refreshInterval=5;

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public int getSafeDistance() {
        return safeDistance;
    }

    public void setSafeDistance(int safeDistance) {
        this.safeDistance = safeDistance;
    }
}
class  ConfigEntry<K,V>{

    private K key;

    private V value;


    public ConfigEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
