package apps.chans.com.syena;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ExpandableListView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.view.WatchersExpandableAdapter;
import apps.chans.com.syena.web.response.GetWatchersResponse;

/**
 * Created by sitir on 16-02-2017.
 */

public class WatchersActivity extends AppCompatActivity {
    private String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreate of WatchersActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watchers_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.watchersToolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        String url = getString(R.string.server_url) + getString(R.string.get_watchers_url,DataSource.instance.getEmail());
        Log.d(LOG_TAG, "URL : " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, "GetWatchersResponse - Received response from server : " + response);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    GetWatchersResponse getWatchersResponse = mapper.readValue(response, GetWatchersResponse.class);
                    if (getWatchersResponse != null) {
                        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.watchers_expandable_list_view);

                        WatchersExpandableAdapter wAdapter = new WatchersExpandableAdapter(WatchersActivity.this, R.layout.watchers_group_view, R.layout.watchers_list_view, getWatchersResponse);
                        expandableListView.setAdapter(wAdapter);
                    }

                } catch (IOException e) {
                    Log.d(LOG_TAG, "Exception Occurred, ", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "Error response received, ", error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(getString(R.string.hp_Installation_Id), DataSource.instance.getInstallationId());
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(stringRequest);


    }

}
