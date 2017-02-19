package apps.chans.com.syena.datasource;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
    public static double latitude = 0.0;
    public static double longitude = 0.0;
    public static double altitude = 0.0;
    public static String EMAIL_VERIFY_URL = "/member/get-or-create";
    public static String PIN_VERIFY_URL = "/member/add";
    public Member currentMember;
    public Member selectedMember;
    public List<Watch> watchList = new ArrayList<Watch>();
    private Context context;
    private String email;
    private String installationId;
    private GetWatchersResponse watchers;

    public DataSource(Context context) {
        this.context = context;
        //this.currentMember = new Member(email);
        File directory = context.getFilesDir();
        File emailFile = new File(directory, "Email-Id");
        File installationIdFile = new File(directory, "Installation-Id");
        String email = null;
        if (emailFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(emailFile));
                email = br.readLine();
                Log.d("Email-ID", "Email read from file" + email);
                if (StringUtils.isBlank(email)) this.email = null;
                else
                    this.email = email;
                br.close();
            } catch (IOException e) {
                Log.d("GetWatchesResponse", "Error occurred while getting 'Email-Id' from file, app may not function properly. " + getStackTrace(e));
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
                Log.d("DataSource", "INSTALLATION-ID" + id);
                br.close();
            } catch (IOException e) {
                Log.d("EmailVerifyResponse", "Error occurred while getting 'Installation-Id' from file, app may not function properly. " + getStackTrace(e));
            }
        }
        instance = this;
    }

    public void sort() {
        Collections.sort(watchList);
    }

    public List<Watch> getWatchList(boolean reloadFromServer) {
        if (email == null) {
            Log.d("Datasource", "Source Email is null.");
            return null;
        }
        if (reloadFromServer) {
            refreshWatchList();
        }

        if (watchList == null) watchList = new ArrayList<Watch>();
        return watchList;
    }

    private void refreshWatchList() {
        final ObjectMapper mapper = new ObjectMapper();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET,
                context.getString(R.string.server_url) + EMAIL_VERIFY_URL + "?email=" + email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("GetWatchesResponse", response);
                        GetWatchesResponse getWatchesResponse = null;
                        try {
                            getWatchesResponse = mapper.readValue(response.toString(), GetWatchesResponse.class);
                        } catch (IOException e) {
                            Log.d("EmailVerifyResponse", getStackTrace(e));
                            return;
                        }
                        if (null == getWatchesResponse
                                || getWatchesResponse.getWatchMembers() == null
                                || getWatchesResponse.getWatchMembers().size() <= 0) {

                            Log.d("GetWatchesResponse", "Empty response received from server");
                            return;
                        }

                        Member source = new Member(email);
                        for (GetWatchesResponse.Member member : getWatchesResponse.getWatchMembers()) {
                            Member target = new Member(member.getEmail(), member.getName());
                            Watch watch = new Watch(source, target);
                            getWatchList(false).add(watch);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d("EmailVerifyResponse-Err", "Error occured " + error.getLocalizedMessage());

                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(jsonObjectRequest);

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
        String url = context.getString(R.string.server_url) + context.getString(R.string.location_update_url);
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setEmail(email);
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
                            Log.d("LocationUpdate", "Response from server is successful");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("LocationUpdate", "Response from server is failed");
                            if (error != null && error.networkResponse != null)
                                Log.d("ResponseStatus:", "LocationUpdate Response status code : " + error.networkResponse.statusCode);
                        }
                    });

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
        File directory = context.getFilesDir();
        File emailFile = new File(directory, "Email-Id");
        emailFile.delete();
        if (emailFile.exists()) {
            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(emailFile));
                br.write("");
                br.close();
            } catch (IOException e) {
                Log.d("GetWatchesResponse", "Error occurred while ERASING file data, app may not function properly. " + getStackTrace(e));
            }
        }

    }

    public void sendJsonRequest(int method, final String url, Object request, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        final ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonReq = null;

        try {
            jsonReq = new JSONObject(mapper.writeValueAsString(request));
        } catch (IOException | JSONException e) {
            Log.d(url, getStackTrace(e));
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                method,
                context.getString(R.string.server_url) + url,
                jsonReq,
                responseListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(context.getString(R.string.hp_Installation_Id), getInstallationId());
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(jsonObjectRequest);
    }

    public void sendStringRequest(int method, final String url, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        StringRequest jsonObjectRequest = new StringRequest(
                method,
                context.getString(R.string.server_url) + url,
                responseListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(context.getString(R.string.hp_Installation_Id), getInstallationId());
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(jsonObjectRequest);
    }

    public void addToWatch(String targetEmail) {
        Member target = new Member(targetEmail);
        if (currentMember.getWatchMap().get(target) == null) {
            Watch w = new Watch(currentMember, target);
            w.getWatchStatus().setDescription(context.getString(R.string.defaultDetailedMessage));
            currentMember.getWatchMap().put(target, w);
        }

        if (!getWatchList().contains(currentMember.getWatchMap().get(target)))
            getWatchList().add(currentMember.getWatchMap().get(target));
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
