package apps.chans.com.syena;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import apps.chans.com.syena.datasource.DataSource;

/**
 * Created by sitir on 25-03-2017.
 */

public class ProfileActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private int IMAGE_SELECTION_RESULT = 1;
    private String imagePath;
    private Uri imageUri;
    private DataSource dataSource;
    private Bitmap imageBitmap;

    public Bitmap scaleDown(Bitmap realImage, float maxSize,
                            boolean filter) {
        Log.d(LOG_TAG, "Scaling down to maxSize: " + maxSize + ", image: " + realImage);
        float ratio = Math.min(
                (float) maxSize / realImage.getWidth(),
                (float) maxSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreate");
        super.onCreate(savedInstanceState);
        this.dataSource = DataSource.instance;
        Log.d(LOG_TAG, "Seting profile_layout");
        setContentView(R.layout.profile_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView uploadProfilePictureIV = (ImageView) findViewById(R.id.uploadProfilePictureIV);
        Log.d(LOG_TAG, "Setting onClickListener for uploadProfilePictureIV");
        uploadProfilePictureIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "In onClick, to upload new image, starting image selection intent");
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_SELECTION_RESULT);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "In onActivityResult");
        if (requestCode == IMAGE_SELECTION_RESULT && resultCode == Activity.RESULT_OK) {
            Log.d(LOG_TAG, "Got success result for image selection");
            imageUri = data.getData();
            getSupportLoaderManager().initLoader(1, null, this);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "Inflating profile_toolbar_menu");
        getMenuInflater().inflate(R.menu.profile_toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;
        Log.d(LOG_TAG, "In onOptionsItemSelected()");
        switch (item.getItemId()) {

            case R.id.profile_save:
                Log.d(LOG_TAG, "profile_save option has been selected");
                result = true;
                break;
        }

        return false;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "In onCreateLoader(), imageUri: " + imageUri);
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, imageUri, projection, null, null, null);
        Log.d(LOG_TAG, "Returning cursorLoader");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        Log.d(LOG_TAG, "In onLoadFinished");
        if (data != null) {
            Log.d(LOG_TAG, "Cursor is not null");
            int columnIndex = data.getColumnIndex(MediaStore.Images.Media.DATA);
            data.moveToFirst();
            imagePath = data.getString(columnIndex);
        } else {
            imagePath = imageUri.getPath();
        }
        Log.d(LOG_TAG, "Image path: " + imagePath);
        uploadImage(imagePath);
    }

    private void uploadImage(String imagePath) {
        Log.d(LOG_TAG, "Uploading image.., imagePath: " + imagePath);
        String url = getString(R.string.server_url) + getString(R.string.upload_pic_url, dataSource.getEmail());
        imageBitmap = BitmapFactory.decodeFile(imagePath);
        imageBitmap = scaleDown(imageBitmap, 640f, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        final String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d(LOG_TAG, "Encoded image: " + encodedImage);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, "In onResponse, response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "Error response received", error);
            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("image", encodedImage);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put(getString(R.string.hp_Installation_Id), dataSource.getInstallationId());
                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode >= 200 && response.statusCode < 300) {
                    Log.d(LOG_TAG, "Status code is success");
                    Toast.makeText(ProfileActivity.this.getApplicationContext(), "Saved successfully", Toast.LENGTH_SHORT).show();
                    ProfileActivity.this.updateProfilePic();
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    Log.d(LOG_TAG, "Status code is not success. Returning error response " + response.statusCode);
                    Toast.makeText(ProfileActivity.this.getApplicationContext(), "Saving failed. Error code: " + response.statusCode, Toast.LENGTH_SHORT).show();
                    return Response.error(new VolleyError(response));
                }
            }
        };
        MainActivity.queue.add(request);
        MainActivity.queue.start();

    }


    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(LOG_TAG, "In onLoaderReset()");
    }

    private void updateProfilePic() {
        Log.d(LOG_TAG, "Updating profile picture with the new selected one");
        ImageView profilePictureIV = (ImageView) findViewById(R.id.profilePictureIV);
        profilePictureIV.setImageBitmap(imageBitmap);
        Log.d(LOG_TAG, "Updated profile picture");
        finish();
    }
}
