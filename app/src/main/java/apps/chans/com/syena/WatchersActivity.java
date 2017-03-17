package apps.chans.com.syena;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.android.volley.toolbox.JsonObjectRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.view.WatchersExpandableAdapter;
import apps.chans.com.syena.web.response.GetWatchersResponse;

import static apps.chans.com.syena.datasource.DataSource.requestTimeOut;

/**
 * Created by sitir on 16-02-2017.
 */

public class WatchersActivity extends AppCompatActivity {
    private String LOG_TAG = getClass().getSimpleName();
    private SwipeRefreshLayout watchersSwipeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreate of WatchersActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watchers_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.watchersToolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        getDataFromServer();
        watchersSwipeView = (SwipeRefreshLayout) findViewById(R.id.watchersSwipeView);
        watchersSwipeView.setColorSchemeResources(R.color.blue, R.color.green, R.color.orange);
        watchersSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getDataFromServer();
                    }
                }, 2000);
            }
        });

    }

    public void getDataFromServer() {
        String url = getString(R.string.server_url) + getString(R.string.get_watchers_url, DataSource.instance.getEmail());
        Log.d(LOG_TAG, "URL : " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, "GetWatchersResponse - Received response from server : " + response.toString());
                ObjectMapper mapper = new ObjectMapper();
                try {
                    GetWatchersResponse getWatchersResponse = mapper.readValue(response.toString(), GetWatchersResponse.class);
                    if (getWatchersResponse != null) {
                        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.watchers_expandable_list_view);

                        WatchersExpandableAdapter wAdapter = new WatchersExpandableAdapter(WatchersActivity.this, R.layout.watchers_group_view, R.layout.watchers_list_view, getWatchersResponse);
                        expandableListView.setAdapter(wAdapter);
                    }

                } catch (IOException e) {
                    Log.d(LOG_TAG, "Exception Occurred, ", e);
                } finally {
                    watchersSwipeView.setRefreshing(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "Error response received, ", error);
                watchersSwipeView.setRefreshing(false);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(getString(R.string.hp_Installation_Id), DataSource.instance.getInstallationId());
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.queue.add(jsonObjectRequest);
        MainActivity.queue.start();
    }

}
