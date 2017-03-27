package apps.chans.com.syena;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Watch;

/**
 * Created by sitir on 20-03-2017.
 */

public class FriendProfileActivity extends AppCompatActivity {

    private final String LOG_TAG = getClass().getSimpleName();
    private Watch selectedWatch;
    private DataSource dataSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreate");
        super.onCreate(savedInstanceState);

        dataSource = DataSource.instance;

        int position = (int) getIntent().getSerializableExtra("PROFILE_WATCH_POSITION");
        selectedWatch = dataSource.getWatchList().get(position);

        Log.d(LOG_TAG, "setting activity's contentView");
        setContentView(R.layout.friend_profile_layout);

        Log.d(LOG_TAG, "Setting support action bar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.friendProfileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner timeIntervalSpinner = (Spinner) findViewById(R.id.timeIntervalSpinner);
        String[] timeIntervalVals = getResources().getStringArray(R.array.time_interval);
        ArrayAdapter adapter1 = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, timeIntervalVals);
        timeIntervalSpinner.setSelection(adapter1.getPosition(String.valueOf(selectedWatch.getWatchConfiguration().getRefreshInterval())));

        EditText safeDistanceText = (EditText) findViewById(R.id.safeDistanceEditText);
        safeDistanceText.setText(String.valueOf(selectedWatch.getWatchConfiguration().getSafeDistance()));
        if (!TextUtils.isEmpty(selectedWatch.getNickName())) {
            EditText nickNameET = (EditText) FriendProfileActivity.this.findViewById(R.id.nickNameET);
            nickNameET.setText(selectedWatch.getNickName());
        }

        TextView emailValueTV = (TextView) findViewById(R.id.friendEmailValueTV);
        emailValueTV.setText(selectedWatch.getTarget().getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "Inflating options menu");
        getMenuInflater().inflate(R.menu.friend_profile_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "Options item selected, itemId: " + item.getItemId());
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.tick_mark_item:
                Log.d(LOG_TAG, "Selected save member details option");
                new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {
                                                  boolean dataChanged = false;
                                                  Map<String, String> watchDetails = new HashMap<String, String>();
                                                  Spinner timeIntervalSpinner = (Spinner) FriendProfileActivity.this.findViewById(R.id.timeIntervalSpinner);
                                                  Integer timeInterval = Integer.parseInt(timeIntervalSpinner.getSelectedItem().toString());
                                                  if (selectedWatch.getWatchConfiguration().getRefreshInterval() != timeInterval) {
                                                      dataChanged = true;
                                                      selectedWatch.getWatchConfiguration().setRefreshInterval(timeInterval);
                                                      watchDetails.put("refreshInterval", timeInterval.toString());
                                                  }
                                                  EditText safeDistanceText = (EditText) FriendProfileActivity.this.findViewById(R.id.safeDistanceEditText);
                                                  if (!TextUtils.isEmpty(safeDistanceText.getText())) {
                                                      int safeDistance = Integer.parseInt(safeDistanceText.getText().toString());
                                                      if (selectedWatch.getWatchConfiguration().getSafeDistance() != safeDistance) {
                                                          dataChanged = true;
                                                          selectedWatch.getWatchConfiguration().setSafeDistance(safeDistance);
                                                          watchDetails.put("safeDistance", String.valueOf(safeDistance));
                                                      }
                                                  }
                                                  EditText nickNameET = (EditText) FriendProfileActivity.this.findViewById(R.id.nickNameET);
                                                  if (!TextUtils.isEmpty(nickNameET.getText())) {
                                                      if (TextUtils.isEmpty(selectedWatch.getNickName()) || !selectedWatch.getNickName().equals(nickNameET.getText().toString()))
                                                          selectedWatch.setNickName(nickNameET.getText().toString());
                                                      dataChanged = true;
                                                      watchDetails.put("nickName", nickNameET.getText().toString());

                                                  }

                                                  if (dataChanged) {
                                                      dataSource.saveWatch(selectedWatch.getTarget().getEmail(), watchDetails);
                                                  }
                                              }
                                          }
                        , 100);

                result = true;
                break;
            case R.id.delete_item:
                Log.d(LOG_TAG, "Selected delete watch member option");
                dataSource.deleteWatch(selectedWatch.getTarget().getEmail());
                result = true;
                break;

        }

        if (result) finish();
        return result;
    }
}
