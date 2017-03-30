package apps.chans.com.syena.view;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.MainActivity;
import apps.chans.com.syena.R;
import apps.chans.com.syena.WatcherProfileActivity;
import apps.chans.com.syena.WatchersFragment;
import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.web.request.WatchAccessRequest;
import apps.chans.com.syena.web.response.GetWatchersResponse;

import static apps.chans.com.syena.datasource.DataSource.requestTimeOut;

/**
 * Created by sitir on 16-02-2017.
 */

public class WatchersExpandableListAdapter extends BaseExpandableListAdapter {

    private int groupViewId, listViewId;
    private WatchersFragment watchersActivity;
    //private GetWatchersResponse getWatchersResponse;
    private String LOG_TAG = getClass().getSimpleName();
    private DataSource dataSource;

    public WatchersExpandableListAdapter(WatchersFragment watchersActivity, int watchers_group_view, int watchers_list_view) {
        Log.d(LOG_TAG, "In constructor");
        this.watchersActivity = watchersActivity;
        groupViewId = watchers_group_view;
        listViewId = watchers_list_view;
        dataSource = DataSource.instance;
    }

    @Override
    public int getGroupCount() {
        if (dataSource.getWatchersResponse() == null) return 0;
        int count = dataSource.getWatchersResponse().getWatchers().size();
        Log.d(LOG_TAG, "Group size : " + count);
        return count;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Log.d(LOG_TAG, "In getChildrenCount");
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        Log.d(LOG_TAG, "In getGroup");
        return dataSource.getWatchersResponse().getWatchers().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Log.d(LOG_TAG, "In getChild");
        return dataSource.getWatchersResponse().getWatchers().get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "In getGroupView, groupPosition: " + groupPosition);
        final GetWatchersResponse.Entry watcher = dataSource.getWatchersResponse().getWatchers().get(groupPosition);
        ViewHolder vh = null;
        if (convertView == null) {
            Log.d(LOG_TAG, "convertView is null");

            convertView = watchersActivity.getLayoutInflater(null).inflate(groupViewId, null);
            vh = new ViewHolder();
            TextView tvWatchersGroupDisplay = (TextView) convertView.findViewById(R.id.tv_watchers_group_display);
            TextView tvWatchersGroupDesc = (TextView) convertView.findViewById(R.id.tv_watchers_group_desc);
            Switch watcherEnabledSwitch = (Switch) convertView.findViewById(R.id.watcher_enabled_switch);
            ImageView watcherProfilePicIcon = (ImageView) convertView.findViewById(R.id.watcher_profile_pic_icon);
            vh.tvWatchersGroupDesc = tvWatchersGroupDesc;
            vh.tvWatchersGroupDisplay = tvWatchersGroupDisplay;
            vh.watcherEnabledSwitch = watcherEnabledSwitch;
            vh.profilePicIcon = watcherProfilePicIcon;
        } else {
            Log.d(LOG_TAG, "convertView is not null");
            vh = (ViewHolder) convertView.getTag();
        }
        Log.d(LOG_TAG, "....Setting data for this item:");
        Log.d(LOG_TAG, "Name : " + watcher.getName());
        Log.d(LOG_TAG, "Email : " + watcher.getEmail());
        Log.d(LOG_TAG, "isEnabled: " + watcher.isEnabled());
        if (watcher.getProfilePicSmall() != null)
            vh.profilePicIcon.setImageBitmap(watcher.getProfilePicSmall());
        else
            dataSource.loadProfilePicIcon(watchersActivity.getContext(), watcher, vh.profilePicIcon, true);
        vh.profilePicIcon.setTag(groupPosition);
        vh.profilePicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                Log.d(LOG_TAG, "In profile pic icon onClick, position: " + position);
                Intent watcherProfileActivity = new Intent(watchersActivity.getContext(), WatcherProfileActivity.class);
                watcherProfileActivity.putExtra("PROFILE_WATCHER_POSITION", position);
                watchersActivity.startActivity(watcherProfileActivity);
            }
        });
        if (StringUtils.isBlank(watcher.getName()))
            vh.tvWatchersGroupDisplay.setText(watcher.getEmail());
        else vh.tvWatchersGroupDisplay.setText(watcher.getName());
        if (watcher.getStatus().equalsIgnoreCase("IN_ACTIVE")) {
            vh.tvWatchersGroupDesc.setText("Not watching");
        } else {
            vh.tvWatchersGroupDesc.setText("Watching since: " + watcher.getWatchingSince());
        }
        vh.watcherEnabledSwitch.setChecked(watcher.isEnabled());
        vh.watcherEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(LOG_TAG, "Switch is checked");
                } else {
                    Log.d(LOG_TAG, "Switch is un-checked");
                }
                WatchAccessRequest watchAccessRequest = new WatchAccessRequest();
                watchAccessRequest.setTarget(watcher.getEmail());
                watchAccessRequest.setFlag(isChecked);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JSONObject jsonObject = new JSONObject(mapper.writeValueAsString(watchAccessRequest));
                    String url = watchersActivity.getString(R.string.server_url) + watchersActivity.getString(R.string.post_watch_access_url, DataSource.instance.getEmail());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }) {
                        @Override
                        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                            Log.d(LOG_TAG, "In parseNetworkReponse : " + response);
                            Log.d(LOG_TAG, "Status code : " + response.statusCode);
                            if (response.statusCode >= 200 && response.statusCode <= 299) {

                                return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                            } else {
                                return Response.error(new VolleyError(response));
                            }
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Installation-Id", DataSource.instance.getInstallationId());
                            return headers;
                        }
                    };
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                            requestTimeOut,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    MainActivity.queue.add(jsonObjectRequest);
                    MainActivity.queue.start();
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "Exception Occurred", e);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Exception Occurred", e);
                }
            }
        });
        convertView.setTag(vh);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = watchersActivity.getLayoutInflater(null).inflate(listViewId, null);
        }
        TextView tvWatchersListDesc = (TextView) convertView.findViewById(R.id.tv_watchers_list_desc);
        tvWatchersListDesc.setText(dataSource.getWatchersResponse().getWatchers().get(groupPosition).getEmail());
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class ViewHolder {
        TextView tvWatchersGroupDisplay;
        TextView tvWatchersGroupDesc;
        Switch watcherEnabledSwitch;
        ImageView profilePicIcon;
    }
}
