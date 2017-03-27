package apps.chans.com.syena;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Member;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.view.WatchExpandableAdapter;

/**
 * Created by sitir on 18-03-2017.
 */

public class WatchFragment extends Fragment implements SensorEventListener {
    private final String LOG_TAG = getClass().getSimpleName();
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    MyLocationHandler locationHandler;
    private DataSource dataSource;
    private SwipeRefreshLayout watchSwipeLayout;
    private WatchExpandableAdapter adapter;
    private ExpandableListView expandableListView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];

    public WatchFragment() {
        dataSource = DataSource.instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        dataSource = DataSource.instance;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart is called");
        if (mGoogleApiClient != null) {
            Log.d(LOG_TAG, "Trying to connect mGoogleApiClient");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (locationHandler != null) locationHandler.stopLocationUpdates();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Pausing all active watches");
                for (Watch w : dataSource.getWatchList()) {
                    if (!w.isActive()) continue;
                    if (w.getViewHolder() == null) continue;
                    if (w.getViewHolder().locationFetchRestTask == null) continue;
                    w.getViewHolder().locationFetchRestTask.pause = true;
                }
            }
        }, 100);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop is called");
        if (mGoogleApiClient != null) {
            Log.d(LOG_TAG, "Trying to disconnect mGoogleApiClient");
            mGoogleApiClient.disconnect();
        }
        dataSource.endAllLocationFetchTasks();
        MainActivity.queue.stop();
    }


    public void notifyWatchesDataSet() {
        Log.d(LOG_TAG, "Notifying data-set for watches");
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void stopSwipeRefresh() {
        Log.d(LOG_TAG, "Stopping refreshing of swipeview");
        if (watchSwipeLayout != null) {
            watchSwipeLayout.setRefreshing(false);
            Log.d(LOG_TAG, "Stopped refreshing of swipeview, and swipeview status is " + watchSwipeLayout.isRefreshing());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //for system oriented sensor listeners
        boolean accel = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        boolean magnet = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        //boolean gyro = sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);
        //Log.d(LOG_TAG, "results of listener registers: " + accel + magnet);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && locationHandler != null) {
            locationHandler.startLocationUpdates();
            Log.d(LOG_TAG, "Location update resumed .....................");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Resuming all active watches");
                for (Watch w : dataSource.getWatchList()) {
                    if (!w.isActive()) continue;
                    if (w.getViewHolder() == null) continue;
                    if (w.getViewHolder().locationFetchRestTask == null) continue;
                    w.getViewHolder().locationFetchRestTask.pause = false;
                }
            }
        }, 100);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (dataSource == null) dataSource = DataSource.instance;
        Log.d(LOG_TAG, "In onCreateView, inflating layout");
        View baseView = inflater.inflate(R.layout.watch_container, container, false);
        Log.d(LOG_TAG, "Successfully inflated layout");
        watchSwipeLayout = (SwipeRefreshLayout) baseView.findViewById(R.id.watch_swipe_layout);
        watchSwipeLayout.setColorSchemeResources(R.color.blue, R.color.green, R.color.orange);
        watchSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "In onRefresh of watch_swipe_layout");
                new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {

                                                  if (dataSource != null)
                                                      dataSource.getWatchList(true);
                                                  if (adapter != null)
                                                      adapter.notifyDataSetChanged();
                                                  watchSwipeLayout.setRefreshing(false);
                                              }
                                          }

                        , 2000);

            }
        });
        Log.d(LOG_TAG, "Creating expandableListView");
        expandableListView = (ExpandableListView) baseView.findViewById(R.id.expandableListView);

        adapter = new WatchExpandableAdapter(this, R.layout.watch_group_view, R.layout.watch_list_view);

        expandableListView.setAdapter(adapter);
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "In Item Long click, position: " + position + ", id: " + id);
                Intent memberProfileActivity = new Intent(WatchFragment.this.getContext(), FriendProfileActivity.class);
                memberProfileActivity.putExtra("PROFILE_WATCH_POSITION", position);
                startActivity(memberProfileActivity);
                return true;
            }
        });
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.collapseGroup(i);
        }
        Log.d(LOG_TAG, "Registering location listener");
        createLocationRequest();
        locationHandler = new MyLocationHandler();
        mGoogleApiClient = new GoogleApiClient.Builder(this.getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(locationHandler)
                .addOnConnectionFailedListener(locationHandler)
                .build();
        if (mGoogleApiClient != null) {
            Log.d(LOG_TAG, "Trying to connect mGoogleApiClient");
            mGoogleApiClient.connect();
        }

        dataSource.getWatchList(true);
        return baseView;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = 0;
        float data[] = null;
        // float[] mGData = new float[3];
        //float[] mMData = new float[3];

        boolean ready = false;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            data = mGravity;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            data = mGeomagnetic;
        } else return;

        for (int i = 0; i < 3; i++)
            data[i] = event.values[i];

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[16];
            float I[] = new float[16];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            //Log.d(LOG_TAG, "rotation matrix result=" + success);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // Log.d(LOG_TAG, "orientation vals: " + orientation[0] + " " + orientation[1] + " " + orientation[2]);
                degree = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }
        //Log.d(LOG_TAG, "Azimuth: " + degree);
        // = Math.round(event.values[0]);
        //Log.d(LOG_TAG, "In onSensorChanged, degree: " + degree);
        if (dataSource.currentMember != null) {
            dataSource.currentMember.setDegree(degree);
            for (Member member : dataSource.currentMember.getWatchMap().keySet())
                setPointer(member);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in scope for now
    }

    public void setPointer(Member target) {
        Watch currentWatch = dataSource.currentMember.getWatchMap().get(target);
        if (!currentWatch.isActive() || !currentWatch.getViewHolder().childViewExpanded)
            return;
        double lat1 = dataSource.currentMember.getLatitude();
        double lat2 = target.getLatitude();
        double lon1 = dataSource.currentMember.getLongitude();
        double lon2 = target.getLongitude();
        Log.d(LOG_TAG, "In setPointer" + target.getEmail() + " " + lat1 + " " + lon1 + " " + lat2 + " " + lon2);
        double dLon = (lon2 - lon1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

        float degree = (float) brng - (dataSource.currentMember.getDegree() * (float) (180.0f / Math.PI));//-(float) brng ;

        if (degree > 360) degree = degree - 360;
        Log.d(LOG_TAG, "degree " + degree + " device degree: " + Math.toDegrees(dataSource.currentMember.getDegree()) + " rand calc" + (dataSource.currentMember.getDegree() * (float) (180.0f / Math.PI)));

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                dataSource.currentMember.getDegree(),
                degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(99);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        if (currentWatch != null
                && currentWatch.getViewHolder() != null
                && currentWatch.getViewHolder().compassPointer != null) {
            //dataSource.currentMember.getWatchMap().get(target).getViewHolder().compassPointer.startAnimation(ra);
            //Log.d(LOG_TAG, target.getEmail() + " Rotating image by degree: " + degree);
            currentWatch.getViewHolder().compassPointer.setRotation(degree);
        }
        //dataSource.currentMember.setDegree(-degree);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * 10);
        mLocationRequest.setFastestInterval(1000 * 5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1);
    }

    private class MyLocationHandler implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        public MyLocationHandler() {
            Log.d(LOG_TAG, "In location listener");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "onLocationChanged : " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getAltitude());
            dataSource.currentMember.setLatitude(location.getLatitude());
            dataSource.currentMember.setLongitude(location.getLongitude());

            // Intent email = new Intent(Intent.ACTION_SEND);
            // email.putExtra(Intent.EXTRA_EMAIL, new String[]{"chandan.ravi1987@gmail.com"});
            //email.putExtra(Intent.EXTRA_SUBJECT, "Location changed");
            //  email.putExtra(Intent.EXTRA_TEXT, "location changed, latitude : " + latitude + ", longitude : " + longitude + ", altitude : " + location.getAltitude());

            //need this to prompts email client only
            // email.setType("message/rfc822");

            // startActivity(Intent.createChooser(email, "Choose an Email client :"));

            dataSource.updateLocation(dataSource.currentMember.getLatitude(), dataSource.currentMember.getLongitude(), location.getAltitude());
        }


        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(LOG_TAG, "In onConnected of MyLocationHandler");
            startLocationUpdates();
        }

        public void startLocationUpdates() {
            Log.d(LOG_TAG, "In startLocationUpdates, checking for permissions");

            if (ActivityCompat.checkSelfPermission(WatchFragment.this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(WatchFragment.this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(LOG_TAG, "In startLocationUpdates, Permissions are not available");

                return;
            }
            Log.d(LOG_TAG, "In startLocationUpdates, permissions are available.");

            if (mGoogleApiClient != null && mLocationRequest != null) {
                Log.d(LOG_TAG, "In startLocationUpdates, Requesting location updates");

                PendingResult<Status> pendingResult =
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(LOG_TAG, "In onConnectionSuspended of MyLocationHandler");

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(LOG_TAG, "In onConnectionFailed of MyLocationHandler");

        }

        protected void stopLocationUpdates() {
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
            Log.d(LOG_TAG, "Location update stopped .......................");
        }
    }

}
