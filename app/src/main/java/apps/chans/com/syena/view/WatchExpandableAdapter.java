package apps.chans.com.syena.view;

import android.app.Activity;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.FriendProfileActivity;
import apps.chans.com.syena.LocationFetchRestTask;
import apps.chans.com.syena.MainActivity;
import apps.chans.com.syena.R;
import apps.chans.com.syena.WatchFragment;
import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.entities.WatchStatus;

import static apps.chans.com.syena.datasource.DataSource.requestTimeOut;

/**
 * Created by sitir on 09-03-2017.
 */

public class WatchExpandableAdapter extends BaseExpandableListAdapter {

    private WatchFragment mainActivity;
    private int groupRes;
    private int childRes;
    private RequestQueue queue;
    private DataSource dataSource;
    private WatchExpandableAdapter adapter;
    private String LOG_TAG = getClass().getSimpleName();

    public WatchExpandableAdapter(WatchFragment mainActivity, int groupRes, int childRes) {
        Log.d(LOG_TAG, "Constructing WatchExpandableAdapter instance");
        this.mainActivity = mainActivity;
        this.groupRes = groupRes;
        this.childRes = childRes;
        this.queue = MainActivity.queue;
        this.dataSource = DataSource.instance;
        adapter = this;
        Log.d(LOG_TAG, "Sorting watch list by 'active' first and the by 'name', in the constuctor");
        dataSource.sort();
    }

    @Override
    public int getGroupCount() {
        // Log.d(LOG_TAG, "Getting group count");
        if (dataSource.getWatchList() == null) return 0;

        return dataSource.getWatchList().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //Log.d(LOG_TAG, "Getting children count");
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        //Log.d(LOG_TAG, "Getting group object for groupPosition: " + groupPosition);
        return dataSource.getWatchList().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        //Log.d(LOG_TAG, "Getting child object for groupPosition: " + groupPosition);
        return dataSource.getWatchList().get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        //Log.d(LOG_TAG, "Getting getGroupId for groupPosition: " + groupPosition);
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // Log.d(LOG_TAG, "Getting getChildId for groupPosition: " + groupPosition);
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "Getting groupView for groupPosition: " + groupPosition);
        Watch watch = dataSource.getWatchList().get(groupPosition);
        if (watch.getViewHolder() == null) {
            Log.d(LOG_TAG, "ViewHolder for watch for groupPosition: " + groupPosition + " is null, inflating a new view");
            convertView = mainActivity.getLayoutInflater(null).inflate(groupRes, null);
            watch.initViewHolder(convertView);
        }
        convertView = watch.getViewHolder().view;
        ImageView profilePicIcon = (ImageView) convertView.findViewById(R.id.watch_profile_pic_icon);
        if (watch.getTarget().getProfilePic() == null) {
            dataSource.loadProfilePicIcon(mainActivity.getContext(), watch.getTarget(), profilePicIcon, true);
        }
        profilePicIcon.setImageBitmap(watch.getTarget().getProfilePicSmall());
        profilePicIcon.setTag(groupPosition);
        profilePicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                Log.d(LOG_TAG, "In profile pic icon onClick, position: " + position);
                Intent memberProfileActivity = new Intent(mainActivity.getContext(), FriendProfileActivity.class);
                memberProfileActivity.putExtra("PROFILE_WATCH_POSITION", position);
                mainActivity.startActivity(memberProfileActivity);
                //return true;
            }
        });
        Log.d(LOG_TAG, watch + ", watch status: " + watch.isActive() + ", switch checked: " + watch.getViewHolder().startSwitch.isChecked());
        Switch startSwitch = watch.getViewHolder().startSwitch;
        startSwitch.setChecked(watch.isActive());
        try {
            Log.d(LOG_TAG, "Temporary sleeping for 100ms to check switch functionality");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startSwitch.setTag(groupPosition);
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                doRestTaskFormalities(buttonView, isChecked);
            }
        });
        if (watch.getViewHolder() != null && watch.getViewHolder().locationFetchRestTask == null)
            doRestTaskFormalities(startSwitch, watch.isActive());
        TextView displayNameText = watch.getViewHolder().displayNameTextView;
        if (!StringUtils.isBlank(watch.getNickName()))
            displayNameText.setText(watch.getNickName());
        else if (!StringUtils.isBlank(watch.getTarget().getDisplayName()))
            displayNameText.setText(watch.getTarget().getDisplayName());
        else displayNameText.setText(watch.getTarget().getEmail());

        TextView smallStatusText = watch.getViewHolder().statusTextView;
        smallStatusText.setText(watch.getWatchStatus().getMessage());
        watch.getViewHolder().childViewExpanded = isExpanded;
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "Getting childView for groupPosition: " + groupPosition);
        if (convertView == null) {
            convertView = mainActivity.getLayoutInflater(null).inflate(childRes, null);
        }
        TextView detailStatusText = (TextView) convertView.findViewById(R.id.detailStatusText);
        detailStatusText.setText(MainActivity.dataSource.getWatchList().get(groupPosition).getWatchStatus().getDescription());
        //this.notifyDataSetChanged();

        ImageView image = (ImageView) convertView.findViewById(R.id.configImage);
        image.setTag(groupPosition);
        Log.d(LOG_TAG, "In getChildView , groupPosition:" + groupPosition + ", convertView:" + convertView + " setting tag : " + image.getTag());
        Log.d(LOG_TAG, "In getChildView , groupPosition:" + groupPosition + ", ViewHolder reference: " + dataSource.getWatchList().get(groupPosition).getViewHolder());
        dataSource.getWatchList().get(groupPosition).getViewHolder().compassPointer = (ImageView) convertView.findViewById(R.id.compassPointerImageView);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private void doRestTaskFormalities(CompoundButton buttonView, boolean isChecked) {
        Log.d(LOG_TAG, "In doRestTaskFormalities ");
        buttonView.setChecked(isChecked);
        Watch currentWatch = dataSource.getWatchList().get((Integer) buttonView.getTag());
        Log.d(LOG_TAG, "Switch status isChecked: " + isChecked + " for groupPosition: " + buttonView.getTag() + ", and watch: " + currentWatch);
        currentWatch.setActive(isChecked);
        currentWatch.getViewHolder().startSwitch.setChecked(isChecked);
        class MySR extends StringRequest {
            Watch currWatch;

            MySR(Watch currentWatch, String url) {
                super(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null)
                                    Log.d(LOG_TAG, "Got error from server : " + error.networkResponse.statusCode + ", Not-modified: " + error.networkResponse.notModified);
                            }
                        });
                this.currWatch = currentWatch;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Installation-Id", DataSource.instance.getInstallationId());
                return headers;
            }
        }
        if (isChecked) {
            currentWatch.getViewHolder().statusTextView.setText(mainActivity.getResources().getString(R.string.defaultStatusMessage));
            // Start the watch instance.
            Log.d(LOG_TAG, "Requesting startWatch for " + currentWatch.getTarget().getEmail());
            String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.get_start_watch_url, currentWatch.getSource().getEmail(), currentWatch.getTarget().getEmail());
            MySR startWatchRequest = new MySR(currentWatch, url) {
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    if (response.statusCode >= 200 && response.statusCode < 300) {
                        Log.d(LOG_TAG, "Starting async task for watch " + currWatch);
                        String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.get_location_request_url, currWatch.getSource().getEmail(), currWatch.getTarget().getEmail());
                        LocationFetchRestTask rt = new LocationFetchRestTask(adapter, queue, currWatch, url);
                        if (currWatch.getViewHolder() != null)
                            currWatch.getViewHolder().locationFetchRestTask = rt;
                        rt.executeOnExecutor(LocationFetchRestTask.THREAD_POOL_EXECUTOR);
                        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));

                    } else {
                        Log.d(LOG_TAG, "Status code is not success. Returning error response");
                        adapter.updateWatchErrorStatus(this.currWatch);
                        adapter.notifyDataSetChanged();
                        return Response.error(new VolleyError(response));
                    }
                }
            };
            startWatchRequest.setRetryPolicy(new DefaultRetryPolicy(
                    requestTimeOut,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(startWatchRequest);
        } else {
            currentWatch.setActive(false);
            currentWatch.getViewHolder().statusTextView.setText("");
            Log.d(LOG_TAG, "Requesting stopWatch for " + currentWatch);
            String url = mainActivity.getString(R.string.server_url) + mainActivity.getString(R.string.get_stop_watch_url, currentWatch.getSource().getEmail(), currentWatch.getTarget().getEmail());
            MySR stopWatchRequest = new MySR(currentWatch, url) {
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    if (response.statusCode >= 200 && response.statusCode < 300) {
                        Log.d(LOG_TAG, "Stopping async task for watch " + currWatch);
                        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                    } else {
                        Log.d(LOG_TAG, "Status code is not success. Returning error response");
                        adapter.updateWatchErrorStatus(currWatch);
                        adapter.notifyDataSetChanged();
                        return Response.error(new VolleyError(response));
                    }
                }
            };
            stopWatchRequest.setRetryPolicy(new DefaultRetryPolicy(
                    requestTimeOut,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stopWatchRequest);
        }
    }


    public void updateWatchErrorStatus(Watch watch) {
        watch.getWatchStatus().setCode(WatchStatus.ENDED);
        Activity activity = mainActivity.getActivity();
        if (activity != null && mainActivity.isAdded())
            watch.getWatchStatus().setMessage(mainActivity.getString(R.string.errorStatusMessage));
        watch.getWatchStatus().setDescription("Call to server failed even after several retries. Please check the network settings or logs. If issue still persists contact support team.");
    }

    public void updateWatchStatus(Watch watch) {
        double dist = watch.getWatchStatus().getDistanceApart();
        double allowedDistance = watch.getWatchConfiguration().getSafeDistance();
        Double distance = dist;
        if (dist > 1609) {
            distance = dist * 0.000621371192;
        }
        Activity activity = mainActivity.getActivity();
        if (activity != null && mainActivity.isAdded()) {
            if (distance > allowedDistance - 1 && distance < allowedDistance) {
                watch.getWatchStatus().setCode(WatchStatus.AT_BORDER);
                watch.getWatchStatus().setMessage(mainActivity.getResources().getString(R.string.statusMessage, "At Border", distance, (distance == dist) ? "mts" : "miles"));
                watch.getWatchStatus().setDescription(
                        mainActivity.getResources().getString(
                                R.string.detailedMessage,
                                distance,
                                (distance == dist) ? "mts" : "miles",
                                watch.getTarget().getLatitude(),
                                watch.getTarget().getLongitude()));
            } else if (distance > allowedDistance) {
                watch.getWatchStatus().setCode(WatchStatus.OUT_OF_RANGE);
                watch.getWatchStatus().setMessage(mainActivity.getResources().getString(R.string.statusMessage, "Out Of Range", distance, (distance == dist) ? "mts" : "miles"));
                watch.getWatchStatus().setDescription(
                        mainActivity.getResources().getString(
                                R.string.detailedMessage,
                                distance,
                                (distance == dist) ? "mts" : "miles",
                                watch.getTarget().getLatitude(),
                                watch.getTarget().getLongitude()));

            } else {
                watch.getWatchStatus().setCode(WatchStatus.IN_RANGE);
                watch.getWatchStatus().setMessage(mainActivity.getResources().getString(R.string.statusMessage, "", distance, (distance == dist) ? "mts" : "miles"));
                watch.getWatchStatus().setDescription(
                        mainActivity.getResources().getString(
                                R.string.detailedMessage,
                                distance,
                                (distance == dist) ? "mts" : "miles",
                                watch.getTarget().getLatitude(),
                                watch.getTarget().getLongitude()));
            }
            Log.d(LOG_TAG, watch.getTarget().getEmail() + " lat " + watch.getTarget().getLatitude() + " lon " + watch.getTarget().getLongitude());
            mainActivity.setPointer(watch.getTarget());
        }
    }
}
