package apps.chans.com.syena;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.view.ExpandableAdapter;
import apps.chans.com.syena.web.request.LocationRequest;
import apps.chans.com.syena.web.response.LocationResponse;

/**
 * Created by sitir on 31-01-2017.
 */

public class LocationFetchRestTask extends AsyncTask<Object, Watch, Void> {
    public static String LOG_TAG = LocationFetchRestTask.class.getSimpleName();
    private String url;
    private ExpandableAdapter adapter;
    private Watch watch;
    private RequestQueue queue;
    private int retries = 10, count = 0;

    public LocationFetchRestTask(ExpandableAdapter adapter, RequestQueue queue, Watch watch, String url) {
        this.adapter = adapter;
        this.queue = queue;
        this.watch = watch;
        this.url = url;
    }

    /*
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double calculateDistance(double lat1, double lat2, double lon1,
                                           double lon2, double el1, double el2) {

        final int radius = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radius * c * 1000; // convert to meters

       /* double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);*/
        return distance;
    }

    public void incrementCount() {
        count++;
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (watch.getSource() == null || watch.getTarget() == null) return null;
        Log.d(LOG_TAG, "Started background job for " + watch.getTarget().getEmail());
        final String targetEmail = watch.getTarget().getEmail();

    // loop every 'refreshInterval' seconds and get the location updates of target
        while (watch.isActive() && count <= retries) {
            Log.d(LOG_TAG, "Looping in seconds :  " + watch.getWatchConfiguration().getRefreshInterval() * 1000);

            StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(LOG_TAG, url + " " + targetEmail + " Got response from server : " + response);
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                LocationResponse locationResponse = mapper.readValue(response.toString(), LocationResponse.class);
                                switch (locationResponse.getStatus()) {
                                    case LocationResponse.SUCCESS:
                                        double distance = calculateDistance(DataSource.latitude, locationResponse.getLatitude(),
                                                DataSource.longitude, locationResponse.getLongitude(), 0, 0);
                                        watch.getTarget().setLatitude(locationResponse.getLatitude());
                                        watch.getTarget().setLongitude(locationResponse.getLongitude());
                                        watch.getTarget().setAltitude(locationResponse.getAltitude());
                                        watch.getWatchStatus().setStatus(locationResponse.getWatchStatus());
                                        watch.getWatchStatus().setDistanceApart(distance);
                                        publishProgress(watch);
                                        break;
                                    case LocationResponse.ERROR:
                                    case LocationResponse.INVALID_EMAIL:
                                    case LocationResponse.INVALID_INSTALLATION_ID:
                                    case LocationResponse.INVALID_WATCH:
                                    case LocationResponse.NO_VALID_MEMBER:
                                    default:
                                        incrementCount();
                                        break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(LOG_TAG, "Got error from server : " + error);
                    Log.d(LOG_TAG, "Got error from server, details: " + getStackTrace(error));


                    incrementCount();

                }

            }

            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Installation-Id", DataSource.instance.getInstallationId());
                    return headers;
                }
            };
            queue.add(stringRequest);
            try {
                Thread.sleep(watch.getWatchConfiguration().getRefreshInterval() * 1000);
            } catch (InterruptedException e) {
                Log.d(LOG_TAG + "-ERROR", e.getMessage() + e.getLocalizedMessage());
            }
        }
        return null;
    }
    /**
     * Creates and returns a {@link java.lang.String} from t’s stacktrace
     *
     * @param t Throwable whose stack trace is required
     * @return String representing the stack trace of the exception
     */
    public String getStackTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        t.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();
        printWriter.close();
        String trace = stringWriter.toString();
        try {
            stringWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return trace;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(LOG_TAG, "In pre execute");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(LOG_TAG, "In postExecute");
        if (count >= retries) {
            adapter.updateWatchErrorStatus(watch);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onProgressUpdate(Watch... values) {
        super.onProgressUpdate(values);
        if (count == retries) {
            adapter.updateWatchErrorStatus(values[0]);
            adapter.notifyDataSetChanged();
        } else {
            adapter.updateWatchStatus(values[0]);
            adapter.notifyDataSetChanged();
        }
        Log.d(LOG_TAG, "In onProgressUpdate : " + values[0]);

    }

}
