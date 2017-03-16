package apps.chans.com.syena.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.LocationFetchRestTask;
import apps.chans.com.syena.MainActivity;
import apps.chans.com.syena.R;
import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.entities.WatchStatus;

import static apps.chans.com.syena.datasource.DataSource.requestTimeOut;


/**
 * Created by sitir on 25-01-2017.
 */

public class ExpandableAdapter extends BaseExpandableListAdapter {

    private MainActivity context;
    private int groupRes;
    private int childRes;
    //private ExpandableListView expandableListView;
    private ExpandableAdapter adapter;
    private RequestQueue queue;
    private String LOG_TAG = ExpandableAdapter.class.getSimpleName();

    public ExpandableAdapter(MainActivity context, ExpandableListView expandableListView, int groupRes, int childRes) {
        this.context = context;
        //this.expandableListView = expandableListView;
        this.groupRes = groupRes;
        this.childRes = childRes;
        queue = MainActivity.queue;
        adapter = this;
    }

    @Override
    public int getGroupCount() {
        if (MainActivity.dataSource.getWatchList() == null) return 0;
        return MainActivity.dataSource.getWatchList().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        System.out.println("getChildrenCount,groupPosition :" + groupPosition);
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        System.out.println("getGroup,groupPosition :" + groupPosition);

        return MainActivity.dataSource.getWatchList().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        System.out.println("getChild,groupPosition :" + groupPosition + ", childPosition" + childPosition);

        return MainActivity.dataSource.getWatchList().get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        System.out.println("getting child psition,groupPosition :" + groupPosition + ", childPosition" + childPosition);
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        System.out.println("1***************In getGroupVIew , groupPosition:" + groupPosition + ", convertView:" + convertView);
        final Watch watch = MainActivity.dataSource.getWatchList().get(groupPosition);
        Switch startSwitch = null;
        ViewHolder vh = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(groupRes, null);
            startSwitch = (Switch) convertView.findViewById(R.id.startWatchSwitch);
            vh = new ViewHolder();
            startSwitch.setChecked(watch.isActive());
            vh.groupPosition = groupPosition;
            vh.statusText = (TextView) convertView.findViewById(R.id.smallStatusText);
            startSwitch.setTag(vh);
            startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ViewHolder vh = (ViewHolder) buttonView.getTag();
                    MainActivity.dataSource.getWatchList().get(vh.groupPosition).setActive(isChecked);
                    //MainActivity.dataSource.sort();
                    TextView smallStatusText = vh.statusText;
                    final Watch watch1 = MainActivity.dataSource.getWatchList().get(vh.groupPosition);
                    if (isChecked) {
                        //TODO do the watching by interacting with server and show the status.
                        smallStatusText.setText(context.getResources().getString(R.string.defaultStatusMessage));

                        // Start the watch instance.
                        Log.d(LOG_TAG, "Requesting startWatch for " + watch1.getTarget().getEmail());
                        String url = context.getString(R.string.server_url) + context.getString(R.string.get_start_watch_url, watch1.getSource().getEmail(), watch1.getTarget().getEmail());
                        StringRequest startWatchRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(LOG_TAG, "Got error from server while starting watch ", error);
                                Log.d(LOG_TAG, "Got error from server : " + error.networkResponse.statusCode + ", Not-modified: " + error.networkResponse.notModified);
                                adapter.updateWatchErrorStatus(watch1);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        ) {
                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                if (response.statusCode >= 200 && response.statusCode < 300) {
                                    Log.d(LOG_TAG, "Starting async task for watch : source: " + watch1.getSource().getEmail() + ", target: " + watch1.getTarget().getEmail());
                                    String url = context.getString(R.string.server_url) + context.getString(R.string.get_location_request_url, watch1.getSource().getEmail(), watch1.getTarget().getEmail());
                                   // LocationFetchRestTask rt = new LocationFetchRestTask(adapter, queue, watch1, url);
                                   // rt.execute();
                                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));

                                } else {
                                    Log.d(LOG_TAG, "Status code is not success. Returning error response");
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
                        startWatchRequest.setRetryPolicy(new DefaultRetryPolicy(
                                requestTimeOut,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(startWatchRequest);
                    } else {
                        smallStatusText.setText("");
                        Log.d(LOG_TAG, "Requesting stopWatch for " + watch1.getTarget().getEmail());
                        String url = context.getString(R.string.server_url) + context.getString(R.string.get_stop_watch_url, watch1.getSource().getEmail(), watch1.getTarget().getEmail());
                        StringRequest stopWatchRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(LOG_TAG, "Got error from server : " + error.networkResponse.statusCode + ", Not-modified: " + error.networkResponse.notModified);
                                adapter.updateWatchErrorStatus(watch1);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        ) {
                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                if (response.statusCode >= 200 && response.statusCode < 300) {
                                    Log.d(LOG_TAG, "Starting async task for watch : source: " + watch1.getSource().getEmail() + ", target: " + watch1.getTarget().getEmail());
                                    //LocationFetchRestTask rt = new LocationFetchRestTask(adapter, queue, watch1, context.getString(R.string.server_url) + context.getString(R.string.get_location_request_url, watch1.getTarget().getEmail(), watch1.getSource().getEmail()));
                                    //rt.execute();
                                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                                } else {
                                    Log.d(LOG_TAG, "Status code is not success. Returning error response");
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
                        stopWatchRequest.setRetryPolicy(new DefaultRetryPolicy(
                                requestTimeOut,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(stopWatchRequest);
                    }

                }
            });
            Log.d(LOG_TAG, "2***************In getGroupVIew , groupPosition:" + groupPosition + ", watchList.get(groupPosition):" + MainActivity.dataSource.getWatchList().get(groupPosition));

        } else {
            startSwitch = (Switch) convertView.findViewById(R.id.startWatchSwitch);
            vh = (ViewHolder) startSwitch.getTag();
            startSwitch.setChecked(watch.isActive());
            vh.groupPosition = groupPosition;
            vh.statusText = (TextView) convertView.findViewById(R.id.smallStatusText);
            startSwitch.setTag(vh);
        }


        TextView displayNameText = (TextView) convertView.findViewById(R.id.displayNameText);
        if (!StringUtils.isBlank(watch.getTarget().getDisplayName()))
            displayNameText.setText(watch.getTarget().getDisplayName());
        else displayNameText.setText(watch.getTarget().getEmail());

        TextView smallStatusText = (TextView) convertView.findViewById(R.id.smallStatusText);
        smallStatusText.setText(watch.getWatchStatus().getMessage());

        return convertView;

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
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        System.out.println("***************In getChildView , groupPosition:" + groupPosition + ", convertView:" + convertView);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(childRes, null);
        }
        TextView detailStatusText = (TextView) convertView.findViewById(R.id.detailStatusText);
        detailStatusText.setText(MainActivity.dataSource.getWatchList().get(groupPosition).getWatchStatus().getDescription());
        this.notifyDataSetChanged();

        ImageView image = (ImageView) convertView.findViewById(R.id.configImage);
        image.setTag(groupPosition);
        System.out.println("***************In getChildView , groupPosition:" + groupPosition + ", convertView:" + convertView + " setting tag : " + image.getTag());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        System.out.println("isChildSelectable ,groupPosition :" + groupPosition + ", childPosition" + childPosition);
        return true;
    }

    public void updateWatchStatus(Watch watch) {
        double distance = watch.getWatchStatus().getDistanceApart();
        double allowedDistance = watch.getWatchConfiguration().getSafeDistance();
        if (distance > allowedDistance - 1) {
            watch.getWatchStatus().setCode(WatchStatus.AT_BORDER);
            watch.getWatchStatus().setMessage(context.getResources().getString(R.string.statusMessage, "At Border", distance));
            watch.getWatchStatus().setDescription(
                    context.getResources().getString(
                            R.string.detailedMessage,
                            watch.getTarget().getDisplayName() == null ? "" : watch.getTarget().getDisplayName(),
                            "At Border",
                            distance,
                            watch.getTarget().getLatitude(),
                            watch.getTarget().getLongitude(),
                            watch.getTarget().getAltitude()));
        } else if (distance > allowedDistance) {
            watch.getWatchStatus().setCode(WatchStatus.OUT_OF_RANGE);
            watch.getWatchStatus().setMessage(context.getResources().getString(R.string.statusMessage, "Out Of Range", distance));
            watch.getWatchStatus().setDescription(
                    context.getResources().getString(
                            R.string.detailedMessage,
                            watch.getTarget().getDisplayName() == null ? "" : watch.getTarget().getDisplayName(), "Out Of Range",
                            distance,
                            watch.getTarget().getLatitude(),
                            watch.getTarget().getLongitude(),
                            watch.getTarget().getAltitude()));

        } else {
            watch.getWatchStatus().setCode(WatchStatus.IN_RANGE);
            watch.getWatchStatus().setMessage(context.getResources().getString(R.string.statusMessage, "In Range", distance));
            watch.getWatchStatus().setDescription(
                    context.getResources().getString(
                            R.string.detailedMessage,
                            watch.getTarget().getDisplayName() == null ? "" : watch.getTarget().getDisplayName(), "In Range",
                            distance,
                            watch.getTarget().getLatitude(),
                            watch.getTarget().getLongitude(),
                            watch.getTarget().getAltitude()));
        }
        context.setPointer(watch.getTarget());
    }

    public void updateWatchErrorStatus(Watch watch) {
        watch.getWatchStatus().setCode(WatchStatus.ENDED);
        watch.getWatchStatus().setMessage(context.getString(R.string.errorStatusMessage));
        watch.getWatchStatus().setDescription("Call to server failed even after several retries. Please check the network settings or logs. If issue still persists contact support team.");
    }

    static class ViewHolder {
        int groupPosition;
        TextView statusText;

    }
}
