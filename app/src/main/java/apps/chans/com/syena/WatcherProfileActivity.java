package apps.chans.com.syena;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.web.response.GetWatchersResponse;

/**
 * Created by sitir on 29-03-2017.
 */

public class WatcherProfileActivity extends AppCompatActivity {

    private final String LOG_TAG = getClass().getSimpleName();
    private GetWatchersResponse.Entry selectedWatcher;
    private DataSource dataSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreate");

        super.onCreate(savedInstanceState);
        dataSource = DataSource.instance;

        int position = (int) getIntent().getSerializableExtra("PROFILE_WATCHER_POSITION");
        selectedWatcher = dataSource.getWatchersResponse().getWatchers().get(position);

        Log.d(LOG_TAG, "setting activity's contentView");
        setContentView(R.layout.watcher_profile_layout);


        Log.d(LOG_TAG, "Setting support action bar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.watcherProfileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.watcherProfileCollapsingToolbar);
        if (StringUtils.isBlank(selectedWatcher.getName()))
            ctl.setTitle(selectedWatcher.getEmail());
        else
            ctl.setTitle(selectedWatcher.getName());
        ImageView imageView = (ImageView) findViewById(R.id.watcherProfilePictureIV);
        if (selectedWatcher.getProfilePic() != null) {
            imageView.setImageBitmap(selectedWatcher.getProfilePic());
        } else {
            dataSource.loadProfilePicIcon(this, selectedWatcher, imageView, false);
        }

        TextView watcherEmailValueTV = (TextView) findViewById(R.id.watcherEmailValueTV);
        watcherEmailValueTV.setText(selectedWatcher.getEmail());
        TextView watcherDisplayNameValueTV = (TextView) findViewById(R.id.watcherDisplayNameValueTV);
        watcherDisplayNameValueTV.setText(selectedWatcher.getName());
        TextView watcherStatusValueTV = (TextView) findViewById(R.id.watcherStatusValueTV);
        watcherStatusValueTV.setText(selectedWatcher.getStatus());
        TextView watcherWatchingSinceValueTV = (TextView) findViewById(R.id.watcherWatchingSinceValueTV);
        watcherWatchingSinceValueTV.setText(selectedWatcher.getWatchingSince().toString());


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
            case android.R.id.home:
                Log.d(LOG_TAG, "Home button clicked. Going to previous activity");
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
