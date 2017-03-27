package apps.chans.com.syena.datasource;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apps.chans.com.syena.MainActivity;
import apps.chans.com.syena.R;
import apps.chans.com.syena.entities.Member;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.web.request.LocationUpdateRequest;
import apps.chans.com.syena.web.response.GetWatchersResponse;
import apps.chans.com.syena.web.response.GetWatchesResponse;

/**
 * Created by sitir on 25-01-2017.
 */

public class DataSource {
    public static DataSource instance;
    public static List<Member> memberList = new ArrayList<Member>();
    //public static double latitude = 0.0;
    //public static double longitude = 0.0;
    public static double altitude = 0.0;
    public static int requestTimeOut = 600000;
    public Member currentMember;
    public Member selectedMember;
    public List<Watch> watchList = new ArrayList<Watch>();
    private String LOG_TAG = getClass().getSimpleName();
    private MainActivity mainActivity;
    private String email;
    private String installationId;
    private GetWatchersResponse watchers;

    public DataSource(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        //this.currentMember = new Member(email);
        File directory = mainActivity.getFilesDir();
        File emailFile = new File(directory, "Email-Id");
        File installationIdFile = new File(directory, "Installation-Id");
        String email = null;
        Log.d(LOG_TAG, "Email file path " + emailFile.getAbsolutePath());

        if (emailFile.exists()) {
            Log.d(LOG_TAG, "Email file exists ");
            try {
                BufferedReader br = new BufferedReader(new FileReader(emailFile));
                email = br.readLine();
                Log.d(LOG_TAG, "Email read from file" + email);
                if (StringUtils.isBlank(email)) this.email = null;
                else
                    this.email = email;
                br.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "GetWatchesResponse : Error occurred while getting 'Email-Id' from file, app may not function properly. ", e);
            }
        }
        if (installationIdFile.exists()) {
            try {
                installationIdFile.createNewFile();
                BufferedReader br = new BufferedReader(new FileReader(installationIdFile));
                String id = br.readLine();
                if (StringUtils.isBlank(id)) this.installationId = null;
                else
                    this.installationId = id;
                Log.d(LOG_TAG, "INSTALLATION-ID: " + id);
                br.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, "EmailVerifyResponse : Error occurred while getting 'Installation-Id' from file, app may not function properly. " + getStackTrace(e));
            }
        }
        instance = this;
    }

    public void sort() {
        Collections.sort(watchList);
        for (Watch w : watchList) {
            w.removeViewHolder();
            //if (w.getViewHolder() != null && w.getViewHolder().startSwitch != null)
            //w.getViewHolder().startSwitch.setChecked(w.isActive());
        }
    }

    public List<Watch> getWatchList(boolean reloadFromServer) {
        if (email == null) {
            Log.d(LOG_TAG, "Source Email is null.");
            //return null;
        }
        if (reloadFromServer) {
            refreshWatchList();
        }

        if (watchList == null) watchList = new ArrayList<Watch>();
        return watchList;
    }

    private void refreshWatchList() {
        final ObjectMapper mapper = new ObjectMapper();
        String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.get_watches_url, email);
        Log.d(LOG_TAG, "Getting watches data from server, url: " + url);
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mainActivity.stopSwipeRefresh();
                        Log.d(LOG_TAG, "GetWatchesResponse: " + response);
                        GetWatchesResponse getWatchesResponse = null;
                        try {
                            getWatchesResponse = mapper.readValue(response.toString(), GetWatchesResponse.class);
                        } catch (IOException e) {
                            Log.d(LOG_TAG, "EmailVerifyResponse: ", e);
                            return;
                        }
                        if (null == getWatchesResponse
                                || getWatchesResponse.getWatchMembers() == null
                                || getWatchesResponse.getWatchMembers().size() <= 0) {

                            Log.d(LOG_TAG, "GetWatchesResponse: Empty response received from server");
                            return;
                        }
                        if (currentMember == null) currentMember = new Member(email);

                        List<Watch> wl = new ArrayList<>();
                        for (GetWatchesResponse.Entry entry : getWatchesResponse.getWatchMembers()) {
                            Log.d(LOG_TAG, "Entry: " + entry.getEmail());
                            Member target = new Member(entry.getEmail(), entry.getName());
                            Watch watch = new Watch(currentMember, target);
                            if (currentMember.getWatchMap().get(target) != null) {
                                Watch tmpWatch = currentMember.getWatchMap().get(target);
                                watch.getWatchConfiguration().setSafeDistance(tmpWatch.getWatchConfiguration().getSafeDistance());
                                watch.getWatchConfiguration().setRefreshInterval(tmpWatch.getWatchConfiguration().getRefreshInterval());
                                //TODO remove the next line after nickname api is written
                                watch.setNickName(tmpWatch.getNickName());
                                if (tmpWatch.getViewHolder() != null) {
                                    watch.setViewHolder(tmpWatch.getViewHolder());
                                    if (tmpWatch.getViewHolder().locationFetchRestTask != null)
                                        tmpWatch.getViewHolder().locationFetchRestTask.endTask();
                                }
                            }
                            watch.setActive(entry.isWatchActive());
                            currentMember.getWatchMap().put(target, watch);
                            wl.add(watch);
                        }
                        Log.d(LOG_TAG, "Resetting watchList");
                        getWatchList().clear();
                        getWatchList().addAll(wl);
                        try {
                            //mainActivity.stopSwipeRefresh();
                            mainActivity.notifyWatchesDataSet();
                            mainActivity.stopSwipeRefresh();
                        } finally {
                            mainActivity.stopSwipeRefresh();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "EmailVerifyResponse-Err: Error occured ", error);
                        mainActivity.stopSwipeRefresh();

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(mainActivity.getString(R.string.hp_Installation_Id), getInstallationId());
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(jsonObjectRequest);
        //MainActivity.queue.start();

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

    public List<Watch> getWatchList() {
        return getWatchList(false);
    }

    public GetWatchersResponse getWatchers() {
        return watchers;
    }

    public void setWatchers(GetWatchersResponse watchers) {
        this.watchers = watchers;
    }

    public void updateLocation(double latitude, double longitude, double altitude) {
        Log.d(LOG_TAG, "Sending location update request to server");
        String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.location_update_url);
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setRequester(email);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setAltitude(altitude);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JSONObject jsonData = new JSONObject(mapper.writeValueAsString(request));
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(LOG_TAG, "LocationUpdate: Response from server is successful");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(LOG_TAG, "LocationUpdate: " + "Response from server is failed", error);
                            if (error != null && error.networkResponse != null)
                                Log.d(LOG_TAG, "ResponseStatus: LocationUpdate Response status code : " + error.networkResponse.statusCode);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    Map<String, String> headers = new HashMap<>();
                    headers.put(mainActivity.getString(R.string.hp_Installation_Id), getInstallationId());
                    return headers;
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    if (response.statusCode >= 200 && response.statusCode < 300) {
                        Log.d(LOG_TAG, "Status code is success");
                        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                    } else {
                        Log.d(LOG_TAG, "Status code is not success. Returning error response " + response.statusCode);
                        return Response.error(new VolleyError(response));
                    }
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    requestTimeOut,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainActivity.queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public void eraseEmailData() {
        Log.d(LOG_TAG, "Erasing email data");
        File directory = mainActivity.getFilesDir();
        File emailFile = new File(directory, "Email-Id");
        emailFile.delete();
        if (emailFile.exists()) {
            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(emailFile));
                br.write("");
                br.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, "GetWatchesResponse: Error occurred while ERASING file data, app may not function properly. " + getStackTrace(e));
            }
        }
        this.email = null;

    }

    public void sendJsonRequest(int method, final String url, Object request, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        final ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonReq = null;

        try {
            jsonReq = new JSONObject(mapper.writeValueAsString(request));
        } catch (IOException | JSONException e) {
            Log.d(LOG_TAG, url, e);
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                method,
                url,
                jsonReq,
                responseListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(mainActivity.getString(R.string.hp_Installation_Id), getInstallationId());
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(jsonObjectRequest);
        //MainActivity.queue.start();
    }

    public void sendStringRequest(int method, final String url, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        StringRequest jsonObjectRequest = new StringRequest(
                method,
                url,
                responseListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(mainActivity.getString(R.string.hp_Installation_Id), getInstallationId());
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Log.d(LOG_TAG, "Sending request to : " + jsonObjectRequest.getUrl());
        MainActivity.queue.add(jsonObjectRequest);
        // MainActivity.queue.start();
    }

    public void addToWatch(String targetEmail) {
        Member target = new Member(targetEmail);
        if (currentMember.getWatchMap().get(target) == null) {
            Watch w = new Watch(currentMember, target);
            w.getWatchStatus().setDescription(mainActivity.getString(R.string.defaultDetailedMessage));
            currentMember.getWatchMap().put(target, w);
        }

        if (!getWatchList().contains(currentMember.getWatchMap().get(target)))
            getWatchList().add(currentMember.getWatchMap().get(target));
    }

    public void makeAllWatchesInactive() {
        for (Watch w : getWatchList()) w.setActive(false);
    }

    public void endAllLocationFetchTasks() {
        for (Watch w : getWatchList()) {
            if (w.getViewHolder() != null && w.getViewHolder().locationFetchRestTask != null) {
                w.getViewHolder().locationFetchRestTask.endTask();
                w.getViewHolder().locationFetchRestTask = null;
            }
        }
    }

    public void saveWatch(String targetEmail, Map<String, String> watchDetails) {
        if (TextUtils.isEmpty(targetEmail) || watchDetails == null) return;
        Log.d(LOG_TAG, "Persisting watch to database.");
        final ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonReq = null;

        try {
            String watchDetailsStr = mapper.writeValueAsString(watchDetails);
            Log.d(LOG_TAG, "Watch Details String: " + watchDetailsStr);
            jsonReq = new JSONObject(watchDetailsStr);
        } catch (IOException | JSONException e) {
            Log.d(LOG_TAG, getStackTrace(e));
            return;
        } catch (Exception e) {
            Log.d(LOG_TAG, getStackTrace(e));
            return;
        }

        final String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.watch_details_url, currentMember.getEmail(), targetEmail);
        Log.d(LOG_TAG, "Sending request to url: " + url);
        MyJsonRequest jsonRequest = new MyJsonRequest(Request.Method.POST, url, jsonReq, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, url + " Got response from server : " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "ERROR response received from server", error);
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode >= 200 && response.statusCode < 300) {
                    Log.d(LOG_TAG, "Status code is success");
                    Toast.makeText(mainActivity.getApplicationContext(), "Saved successfully", Toast.LENGTH_SHORT).show();
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    Log.d(LOG_TAG, "Status code is not success. Returning error response " + response.statusCode);
                    Toast.makeText(mainActivity.getApplicationContext(), "Saving failed. Error code: " + response.statusCode, Toast.LENGTH_SHORT).show();
                    return Response.error(new VolleyError(response));
                }
            }
        };

        MainActivity.queue.add(jsonRequest);
        //MainActivity.queue.start();

    }

    public void deleteWatch(final String targetEmail) {

        if (TextUtils.isEmpty(targetEmail)) return;
        Log.d(LOG_TAG, "Sending request to remove watch for " + targetEmail);

        final String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.delete_watch_url, currentMember.getEmail(), targetEmail);
        Log.d(LOG_TAG, "Sending request to url: " + url);
        MyJsonRequest jsonRequest = new MyJsonRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, url + " Got response from server : " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "ERROR response received from server", error);
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode >= 200 && response.statusCode < 300) {
                    Log.d(LOG_TAG, "Status code is success");
                    Toast.makeText(mainActivity.getApplicationContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Deleting watch for " + targetEmail);
                    selectedMember.getWatchMap().remove(targetEmail);
                    watchList.remove(targetEmail);
                    memberList.remove(targetEmail);
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    Log.d(LOG_TAG, "Status code is not success. Returning error response " + response.statusCode);
                    Toast.makeText(mainActivity.getApplicationContext(), "Deleting failed. Error code: " + response.statusCode, Toast.LENGTH_SHORT).show();
                    return Response.error(new VolleyError(response));
                }
            }
        };

        MainActivity.queue.add(jsonRequest);
        // MainActivity.queue.start();

    }

    private class MyJsonRequest extends JsonObjectRequest {

        private Response response;

        public MyJsonRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            this.response = super.parseNetworkResponse(response);
            return this.response;
        }

        public Response getResponse() {
            return response;
        }
    }
}
