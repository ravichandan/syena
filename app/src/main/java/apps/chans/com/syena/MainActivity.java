package apps.chans.com.syena;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Member;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.view.WatchExpandableAdapter;
import apps.chans.com.syena.web.request.PinValidationRequest;
import apps.chans.com.syena.web.response.EmailVerifyResponse;
import apps.chans.com.syena.web.response.PinValidationResponse;

import static apps.chans.com.syena.datasource.DataSource.requestTimeOut;

//import static apps.chans.com.syena.datasource.DataSource.latitude;
//import static apps.chans.com.syena.datasource.DataSource.longitude;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static DataSource dataSource;
    public static RequestQueue queue;
    private static boolean loggedInAlready;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    MyLocationHandler locationHandler;
    private int TAG_MEMBER_RESULT = 1;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private WatchExpandableAdapter adapter;
    private ExpandableListView expandableListView;
    private SwipeRefreshLayout activity_main;
    private String LOG_TAG = MainActivity.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * 10);
        mLocationRequest.setFastestInterval(1000 * 5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreate, savedInstanceState: " + savedInstanceState);
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
       /* boolean accel = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        boolean magnet = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        boolean gyro = sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(LOG_TAG, "1111results of listener registers: " + accel + magnet + gyro);*/

        queue = Volley.newRequestQueue(this);
        queue.start();

        Log.d(LOG_TAG, "Starting MainActivity, dataSource: " + dataSource);

        if (this.dataSource == null) {
            dataSource = new DataSource(this);
            loggedInAlready = false;
        }
        Log.d(LOG_TAG, "Starting MainActivity, loggedInAlready: " + loggedInAlready + ", dataSource -> email: " + dataSource.getEmail());
        if (loggedInAlready) {
            welcome();
        } else {
            setContentView(R.layout.empty_home_page);
            if (!StringUtils.isBlank(dataSource.getEmail())) {
                Log.d(LOG_TAG, "Datasource email is not blank : " + dataSource.getEmail());
                Button defaultLoginButton = (Button) findViewById(R.id.homeLogin);
                defaultLoginButton.setEnabled(false);
                autoLogin(dataSource.getEmail());


            } else {
                Log.d(LOG_TAG, "Datasource email is  blank, opening login screen ");
                Button defaultLoginButton = (Button) findViewById(R.id.homeLogin);
                defaultLoginButton.setEnabled(true);
                Button homeLogin = (Button) findViewById(R.id.homeLogin);
                homeLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLoginPopup();
                    }
                });
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart is called");
        if (mGoogleApiClient != null) {
            Log.d(LOG_TAG, "Trying to connect mGoogleApiClient");
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //for system oriented sensor listeners
        boolean accel = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        boolean magnet = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        //boolean gyro = sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);
        //Log.d(LOG_TAG, "results of listener registers: " + accel + magnet);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && locationHandler != null) {
            locationHandler.startLocationUpdates();
            Log.d(LOG_TAG, "Location update resumed .....................");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
        if (locationHandler != null) locationHandler.stopLocationUpdates();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop is called");
        if (mGoogleApiClient != null) {
            Log.d(LOG_TAG, "Trying to disconnect mGoogleApiClient");
            mGoogleApiClient.disconnect();
        }
        queue.stop();

    }

    private void autoLogin(String email) {
        final ObjectMapper mapper = new ObjectMapper();
        Log.d(LOG_TAG, "In autoLogin :   " + email);
        String url = getString(R.string.server_url) + getString(R.string.get_or_create_url, email);
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, response);
                EmailVerifyResponse verifyResponse = null;
                try {
                    verifyResponse = mapper.readValue(response.toString(), EmailVerifyResponse.class);
                } catch (IOException e) {
                    Log.d(LOG_TAG, getStackTrace(e));
                    dataSource.eraseEmailData();
                    //recreate();
                    return;
                }
                if (null == verifyResponse) {
                    Log.d(LOG_TAG, "Empty response received from server");
                    return;
                }
                try {
                    Log.d(LOG_TAG, "Response status : " + verifyResponse.getStatus());

                    switch (verifyResponse.getStatus()) {
                        case EmailVerifyResponse.NEW_ENTRY:
                        case EmailVerifyResponse.NEW_DEVICE:
                            dataSource.currentMember = new Member(verifyResponse.getEmail());
                            File directory = getFilesDir();
                            File installationIdFile = new File(directory, "Installation-Id");
                            try {
                                installationIdFile.createNewFile();
                                FileOutputStream fos = new FileOutputStream(installationIdFile);
                                fos.write(verifyResponse.getInstallationId().getBytes());
                                fos.flush();
                                fos.close();
                                BufferedReader br = new BufferedReader(new FileReader(installationIdFile));
                                dataSource.setInstallationId(br.readLine());
                                Log.d(LOG_TAG, "INSTALLATION-ID" + dataSource.getInstallationId());
                                br.close();
                            } catch (IOException e) {
                                Log.d(LOG_TAG, "Error occurred while getting 'Installation-Id' from file, app may not function properly. " + getStackTrace(e));
                            }
                            showPinPopup(R.layout.pin_generate_popup, verifyResponse.getEmail());

                            break;
                        case EmailVerifyResponse.SUCCESS:
                            welcome();
                            break;
                        case EmailVerifyResponse.INVALID_EMAIL:
                        case EmailVerifyResponse.NO_VALID_MEMBER_TXN:
                        case EmailVerifyResponse.NO_VALID_MEMBER:
                        case EmailVerifyResponse.SENDING_EMAIL_FAILED:
                        case EmailVerifyResponse.ERROR:
                        default:
                            dataSource.eraseEmailData();
                            //recreate();
                            return;
                    }
                } catch (Exception e) {
                    dataSource.eraseEmailData();
                    //recreate();
                    return;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(LOG_TAG, "EmailVerifyResponse-Err, Error occured ", error);
                dataSource.eraseEmailData();
                //recreate();
                return;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put(getString(R.string.hp_Installation_Id), dataSource.getInstallationId());
                return headers;
            }
        };
        Log.d(LOG_TAG, "Sending autologin request for email");

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private void showLoginPopup() {
        int result = 0;
        Log.d(LOG_TAG, "Showing Login popup, ");
        final View loginView = getLayoutInflater().inflate(R.layout.login_popup, null);
        final PopupWindow popupWindow = new PopupWindow(loginView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(loginView, Gravity.CENTER, 0, 0);
        popupWindow.setOutsideTouchable(false);
        final Button pinSubmitButton = (Button) loginView.findViewById(R.id.loginEnterButton);
        pinSubmitButton.setTag(loginView);
        pinSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = (View) v.getTag();
                pinSubmitButton.setText(getString(R.string.sendingText));
                pinSubmitButton.setEnabled(false);
                EditText emailText = (EditText) view.findViewById(R.id.emailText);
                if (emailText == null || emailText.getText() == null || StringUtils.isBlank(emailText.getText().toString())) {
                    Log.d(LOG_TAG, "Pin should not be empty");
                    TextView emailResonseLabelTextView = (TextView) loginView.findViewById(R.id.emailResponseLabel);
                    emailResonseLabelTextView.setText("Email should not be empty");
                    pinSubmitButton.setText(getString(R.string.enter_text));
                    pinSubmitButton.setEnabled(true);
                    return;
                }
                System.out.println("Email Text : " + emailText.getText());
                //TODO validate email
                final ObjectMapper mapper = new ObjectMapper();
                String url = getString(R.string.server_url) + getString(R.string.get_or_create_url, emailText.getText().toString());
                Log.d(LOG_TAG, "URL : " + url);
                StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "EmailVerifyResponse: " + response);
                        EmailVerifyResponse verifyResponse = null;
                        TextView emailResponseLabelTextView = (TextView) loginView.findViewById(R.id.emailResponseLabel);
                        try {
                            verifyResponse = mapper.readValue(response.toString(), EmailVerifyResponse.class);
                        } catch (IOException e) {
                            Log.d(LOG_TAG, "Error occured in EmailVerifyResponse: ", e);
                            pinSubmitButton.setText(getString(R.string.enter_text));
                            emailResponseLabelTextView.setText(e.getMessage() + " " + e.getLocalizedMessage());
                            return;
                        }
                        if (null == verifyResponse) {
                            pinSubmitButton.setText(getString(R.string.enter_text));
                            emailResponseLabelTextView.setText("Empty response received from server");
                            return;
                        }

                        try {
                            Log.d(LOG_TAG, "EmailVerifyResponse-> Response status : " + verifyResponse.getStatus());

                            switch (verifyResponse.getStatus()) {
                                case EmailVerifyResponse.NEW_ENTRY:
                                case EmailVerifyResponse.NEW_DEVICE:
                                    popupWindow.dismiss();
                                    File directory = getFilesDir();
                                    File installationIdFile = new File(directory, "Installation-Id");

                                    try {
                                        installationIdFile.createNewFile();
                                        FileOutputStream fos = new FileOutputStream(installationIdFile);
                                        fos.write(verifyResponse.getInstallationId().getBytes());
                                        fos.flush();
                                        fos.close();
                                        BufferedReader br = new BufferedReader(new FileReader(installationIdFile));
                                        dataSource.setInstallationId(br.readLine());
                                        Log.d(LOG_TAG, "INSTALLATION-ID" + dataSource.getInstallationId());
                                        br.close();
                                    } catch (IOException e) {
                                        Log.d(LOG_TAG, "EmailVerifyResponse, Error occurred while getting 'Installation-Id' from file, app may not function properly. ", (e));
                                    }
                                    showPinPopup(R.layout.pin_generate_popup, verifyResponse.getEmail());
                                    break;
                                case EmailVerifyResponse.SUCCESS:
                                    popupWindow.dismiss();
                                    welcome();
                                    break;
                                case EmailVerifyResponse.INVALID_EMAIL:
                                    emailResponseLabelTextView.setText("Invalid email");
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    pinSubmitButton.setEnabled(true);
                                    break;
                                case EmailVerifyResponse.NO_VALID_MEMBER_TXN:
                                    emailResponseLabelTextView.setText("No MemberTxn found. Try again");
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    pinSubmitButton.setEnabled(true);
                                    break;
                                case EmailVerifyResponse.NO_VALID_MEMBER:
                                    emailResponseLabelTextView.setText("No Valid member found");
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    pinSubmitButton.setEnabled(true);
                                    break;
                                case EmailVerifyResponse.SENDING_EMAIL_FAILED:
                                    emailResponseLabelTextView.setText("Sending Pin in email failed. Try again");
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    break;
                                case EmailVerifyResponse.ERROR:
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    emailResponseLabelTextView.setText("System error occurred. Please try again");
                                    pinSubmitButton.setEnabled(true);
                                    break;
                                default:
                                    Log.d(LOG_TAG, "EmailVerifyResponse, Default case, status : " + verifyResponse.getStatus());
                                    popupWindow.dismiss();
                                    break;
                            }
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error occured in EmailVerifyResponse-> ", (e));
                            emailResponseLabelTextView.setText("System error occurred. Please try again");
                            pinSubmitButton.setText(getString(R.string.enter_text));
                            pinSubmitButton.setEnabled(true);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e(LOG_TAG, "EmailVerifyResponse-Err, Error occurred ", error);
                        TextView emailResponseLabelTextView = (TextView) loginView.findViewById(R.id.emailResponseLabel);
                        emailResponseLabelTextView.setText("System error occurred. Please try again");
                        pinSubmitButton.setText(getString(R.string.enter_text));
                        pinSubmitButton.setEnabled(true);
                    }
                });
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        requestTimeOut,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonObjectRequest);
                //JSONObject response = future.get(60, TimeUnit.SECONDS);

            }
        });
    }

    private void showPinPopup(int pin_popup, final String email) {
        BufferedWriter bw = null;
        BufferedReader br = null;
        try {
            File directory = getFilesDir();
            File emailFile = new File(directory, "Email-Id");
            Log.d(LOG_TAG, "Creating new emailFile: " + emailFile.getAbsolutePath());
            emailFile.createNewFile();
            Log.d(LOG_TAG, "Created new emailFile: " + emailFile.exists());
            bw = new BufferedWriter(new FileWriter(emailFile));
            bw.write(email);
            bw.flush();
            bw.close();
            br = new BufferedReader(new FileReader(emailFile));
            Log.d(LOG_TAG, "Email read from file" + br.readLine());
            br.close();
            if (!StringUtils.isBlank(email))
                dataSource.setEmail(email);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error occurred while getting 'Email-Id' from file, app may not function properly. " + getStackTrace(e));
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final View loginView = getLayoutInflater().inflate(pin_popup, null);
        final PopupWindow popupWindow = new PopupWindow(loginView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(loginView, Gravity.CENTER, 0, 0);
        popupWindow.setOutsideTouchable(false);
        final Button pinSubmitButton = (Button) loginView.findViewById(R.id.pinSubmitButton);
        pinSubmitButton.setTag(loginView);
        pinSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinSubmitButton.setEnabled(false);
                View view = (View) v.getTag();
                EditText pinText = (EditText) view.findViewById(R.id.pinText);
                if (pinText == null || pinText.getText() == null || StringUtils.isBlank(pinText.getText().toString())) {
                    Log.d(LOG_TAG, "Pin should not be empty");
                    TextView pinResponseLabelTextView = (TextView) loginView.findViewById(R.id.pin_server_response_label);
                    pinResponseLabelTextView.setText("Pin should not be empty");
                    pinSubmitButton.setText(getString(R.string.enter_text));
                    pinSubmitButton.setEnabled(true);
                    return;
                }
                System.out.println("pinText Text : " + pinText.getText());
                //TODO validate email & pin
                PinValidationRequest request = new PinValidationRequest();
                request.setPin(pinText.getText().toString());
                final ObjectMapper mapper = new ObjectMapper();
                JSONObject jsonReq = null;
                final TextView pinResponseLabelTextView = (TextView) loginView.findViewById(R.id.pin_server_response_label);

                try {
                    jsonReq = new JSONObject(mapper.writeValueAsString(request));
                } catch (IOException | JSONException e) {
                    Log.e(LOG_TAG, getStackTrace(e));
                    pinResponseLabelTextView.setText("System error occured in app.");
                    pinSubmitButton.setText(getString(R.string.enter_text));
                    pinSubmitButton.setEnabled(true);
                    return;
                }
                String url = getString(R.string.server_url) + getString(R.string.pin_verify_url, email);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonReq,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(LOG_TAG, "PinValidationResponse: " + response.toString());
                                PinValidationResponse verifyResponse;
                                try {
                                    verifyResponse = mapper.readValue(response.toString(), PinValidationResponse.class);
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, "PinValidationResponse", e);
                                    pinResponseLabelTextView.setText("Received response can't be parsed.");
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    pinSubmitButton.setEnabled(true);
                                    return;
                                }
                                if (verifyResponse == null) {
                                    pinResponseLabelTextView.setText("Empty response received.");
                                    pinSubmitButton.setText(getString(R.string.enter_text));
                                    pinSubmitButton.setEnabled(true);
                                    return;
                                }
                                switch (verifyResponse.getStatus()) {
                                    case PinValidationResponse.SUCCESS:

                                        popupWindow.dismiss();
                                        File directory = getFilesDir();
                                        File emailIdFile = new File(directory, "Email-Id");

                                        try {
                                            emailIdFile.createNewFile();
                                            FileOutputStream fos = new FileOutputStream(emailIdFile);
                                            fos.write(email.getBytes());
                                            fos.flush();
                                            fos.close();
                                            BufferedReader br = new BufferedReader(new FileReader(emailIdFile));
                                            Log.d(LOG_TAG, br.readLine());
                                            br.close();
                                        } catch (IOException e) {
                                            Log.e(LOG_TAG, "Error occurred while getting 'Installation-Id' from file, app may not function properly. " + getStackTrace(e));
                                        }
                                        welcome();
                                        break;
                                    case PinValidationResponse.INVALID_EMAIL:
                                        pinResponseLabelTextView.setText("Invalid email");
                                        pinSubmitButton.setText(getString(R.string.enter_text));
                                        pinSubmitButton.setEnabled(true);
                                        break;
                                    case PinValidationResponse.INVALID_INSTALLATION_ID:
                                        pinResponseLabelTextView.setText("Invalid member");
                                        pinSubmitButton.setText(getString(R.string.enter_text));
                                        pinSubmitButton.setEnabled(true);
                                        break;
                                    case PinValidationResponse.INVALID_PIN:
                                        pinResponseLabelTextView.setText("Invalid PIN");
                                        pinSubmitButton.setText(getString(R.string.enter_text));
                                        pinSubmitButton.setEnabled(true);
                                        break;
                                    case PinValidationResponse.NO_VALID_MEMBER:
                                        pinResponseLabelTextView.setText("No valid member found");
                                        pinSubmitButton.setText(getString(R.string.enter_text));
                                        pinSubmitButton.setEnabled(true);
                                        break;
                                    case PinValidationResponse.NO_VALID_MEMBER_TXN:
                                        pinResponseLabelTextView.setText("No valid membertxn found");
                                        pinSubmitButton.setText(getString(R.string.enter_text));
                                        pinSubmitButton.setEnabled(true);
                                        break;
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Log.e(LOG_TAG, "Error occured " + getStackTrace(error));
                                pinSubmitButton.setText(getString(R.string.enter_text));
                                pinResponseLabelTextView.setText("System error occurred. Please try again");
                                pinSubmitButton.setText(getString(R.string.enter_text));
                                pinSubmitButton.setEnabled(true);
                            }
                        }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(getString(R.string.hp_Installation_Id), dataSource.getInstallationId());
                        return headers;
                    }
                };
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        requestTimeOut,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonObjectRequest);
            }
        });

    }

    /**
     * Creates and returns a {@link java.lang.String} from t’s stacktrace
     *
     * @param t Throwable whose stack trace is required
     * @return String representing the stack trace of the exception
     */
    public String getStackTrace(Throwable t) {
        StringWriter stringWritter = new StringWriter();
        PrintWriter printWritter = new PrintWriter(stringWritter, true);
        t.printStackTrace(printWritter);
        printWritter.flush();
        stringWritter.flush();
        printWritter.close();
        String trace = stringWritter.toString();
        try {
            stringWritter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return trace;
    }

    private void welcome() {
        Log.d(LOG_TAG, "In Welcome");

        loggedInAlready = true;
        if (dataSource.currentMember == null)
            dataSource.currentMember = new Member(dataSource.getEmail());

        setContentView(R.layout.activity_main);
        activity_main = (SwipeRefreshLayout) findViewById(R.id.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        activity_main.setColorSchemeResources(R.color.blue, R.color.green, R.color.orange);
        activity_main.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (dataSource != null)
                                                      dataSource.getWatchList(true);
                                                  if (adapter != null)
                                                      adapter.notifyDataSetChanged();
                                              }
                                          }

                        , 2000);

            }
        });
        expandableListView = (ExpandableListView) this.findViewById(R.id.expandableListView);

        adapter = new WatchExpandableAdapter(this, R.layout.watch_group_view, R.layout.watch_list_view);

        expandableListView.setAdapter(adapter);
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.collapseGroup(i);
        }
        Log.d(LOG_TAG, "Registering location listener");
        createLocationRequest();
        locationHandler = new MyLocationHandler();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(locationHandler)
                .addOnConnectionFailedListener(locationHandler)
                .build();
        if (mGoogleApiClient != null) {
            Log.d(LOG_TAG, "Trying to connect mGoogleApiClient");
            mGoogleApiClient.connect();
        }
        ImageView tagMemberIV= (ImageView) findViewById(R.id.tagMemberIV);
        tagMemberIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagMember(v);
            }
        });
        /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = lm.getBestProvider(criteria, true);
        Log.d(LOG_TAG, "Selected provider : " + provider);
        LocationListener locationHandler = new MyLocationHandler();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(LOG_TAG, "Permissions are not available");
            return;
        } else {
            Log.d(LOG_TAG, "Permissions are  available");
        }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Log.d(LOG_TAG, "GPS is enabled in this servicee");
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            Log.d(LOG_TAG, "NETWORK_PROVIDER is enabled in this servicee");
        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }
        Location lastGpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastNtwLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d(LOG_TAG, "Last known location " + lastGpsLocation);
        Log.d(LOG_TAG, "Last known location network provider " + lastNtwLocation);
        if (lastGpsLocation != null || lastNtwLocation != null) {
            if (lastNtwLocation != null && lastGpsLocation != null) {
                if (lastNtwLocation.getTime() > lastGpsLocation.getTime())
                    locationHandler.onLocationChanged(lastNtwLocation);
                else
                    locationHandler.onLocationChanged(lastGpsLocation);
            }
            if (lastNtwLocation != null) locationHandler.onLocationChanged(lastNtwLocation);
            else locationHandler.onLocationChanged(lastGpsLocation);
        }


        lm.requestLocationUpdates(provider, 400, 1f, locationHandler);*/
//        expandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        dataSource.getWatchList(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.getWatchersItemMenu:
                //Call server to get watchers
                Intent intent = new Intent(this, WatchersActivity.class);
                startActivity(intent);
                result = true;
                break;

            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    public void tagMember(View view) {
        Log.d(LOG_TAG, "In tagMember");
        Intent intent = new Intent(this, TagMemberActivity.class);
        startActivityForResult(intent, TAG_MEMBER_RESULT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAG_MEMBER_RESULT && resultCode == Activity.RESULT_OK) {
            adapter.notifyDataSetChanged();
            List<Watch> watchList = dataSource.getWatchList();
            System.out.print(watchList);
        }
    }

    public void configureWatch(View view) {

        Watch w1 = dataSource.getWatchList().get((Integer) view.getTag());
        if (w1 != null)
            dataSource.selectedMember = w1.getTarget();

        if (dataSource.currentMember.getWatchMap().get(dataSource.selectedMember) == null)
            dataSource.currentMember.getWatchMap().put(dataSource.selectedMember, new Watch(dataSource.currentMember, dataSource.selectedMember));
        //LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View configurePopup = getLayoutInflater().inflate(R.layout.configure_watch_popup, null);

        final PopupWindow popup = new PopupWindow(MainActivity.this);

        popup.setContentView(configurePopup);
        popup.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        popup.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        popup.setFocusable(true);

        Button okConfigure = (Button) configurePopup.findViewById(R.id.okConfigure);
        okConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setNewConfiguration(configurePopup);
                popup.dismiss();
            }
        });
        Button cancelConfigure = (Button) configurePopup.findViewById(R.id.cancelConfigure);
        cancelConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        Spinner timeSpinner = (Spinner) configurePopup.findViewById(R.id.timeSpinner);
        String items[] = getResources().getStringArray(R.array.time_interval);
        ArrayAdapter adapter1 = new ArrayAdapter(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, items);
        timeSpinner.setAdapter(adapter1);
        if (dataSource.selectedMember != null && dataSource.currentMember.getWatchMap().get(dataSource.selectedMember) != null) {
            timeSpinner.setSelection(adapter1.getPosition(String.valueOf(dataSource.currentMember.getWatchMap().get(dataSource.selectedMember).getWatchConfiguration().getRefreshInterval())));
            EditText et = (EditText) configurePopup.findViewById(R.id.safeDistanceText);
            et.setText(String.valueOf(dataSource.currentMember.getWatchMap().get(dataSource.selectedMember).getWatchConfiguration().getSafeDistance()));
        }
        popup.showAtLocation(configurePopup, Gravity.CENTER, 0, 0);


    }

    private void setNewConfiguration(View v) {


        Spinner intervalSpinner = (Spinner) v.findViewById(R.id.intervalSpinner);
        Spinner timeSpinner = (Spinner) v.findViewById(R.id.timeSpinner);
        timeSpinner.getSelectedItemPosition();
        int intervalFactor = Integer.parseInt(timeSpinner.getSelectedItem().toString());

        // TODO int intervalPos=intervalSpinner.getSelectedItemPosition();
        /*if(intervalPos==1)
            intervalFactor=60*intervalFactor;
        if(intervalPos==2)
            intervalFactor=60*24*intervalFactor;
        */
        dataSource.currentMember.getWatchMap().get(dataSource.selectedMember).getWatchConfiguration().setRefreshInterval(intervalFactor);
        EditText et = (EditText) v.findViewById(R.id.safeDistanceText);
        if (!TextUtils.isEmpty(et.getText())) {
            dataSource.currentMember.getWatchMap().get(dataSource.selectedMember).getWatchConfiguration().setSafeDistance(Integer.parseInt(et.getText().toString()));
        }
    }

    public void notifyWatchesDataSet() {
        Log.d(LOG_TAG, "Notifying dataset for watches");
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void stopSwipeRefresh() {
        Log.d(LOG_TAG, "Stopping refreshing of swipeview");
        if (activity_main != null) {
            activity_main.setRefreshing(false);
            Log.d(LOG_TAG, "Stopped refreshing of swipeview, and swipeview status is " + activity_main.isRefreshing());
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = 0;
        float data[] = null;
        // float[] mGData = new float[3];
        //float[] mMData = new float[3];

        boolean ready = false;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            data = mGravity;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            data = mGeomagnetic;
        } else return;

        for (int i = 0; i < 3; i++)
            data[i] = event.values[i];

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[16];
            float I[] = new float[16];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            //Log.d(LOG_TAG, "rotation matrix result=" + success);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // Log.d(LOG_TAG, "orientation vals: " + orientation[0] + " " + orientation[1] + " " + orientation[2]);
                degree = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }
        //Log.d(LOG_TAG, "Azimuth: " + degree);
        // = Math.round(event.values[0]);
        //Log.d(LOG_TAG, "In onSensorChanged, degree: " + degree);
        if (dataSource.currentMember != null) {
            dataSource.currentMember.setDegree(degree);
            for (Member member : dataSource.currentMember.getWatchMap().keySet())
                setPointer(member);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in scope for now
    }

    public void setPointer(Member target) {
        if (!dataSource.currentMember.getWatchMap().get(target).isActive())
            return;
        double lat2 = dataSource.currentMember.getLatitude();
        double lat1 = target.getLatitude();
        double lon2 = dataSource.currentMember.getLongitude();
        double lon1 = target.getLongitude();
        //Log.d(LOG_TAG, "In setPointer" + target.getEmail() + " " + lat1 + " " + lon1 + " " + lat2 + " " + lon2);
        double dLat = Math.toRadians(lat1 - lat2);
        double dLon = Math.toRadians(lon1 - lon2);

        //lat1 = Math.toRadians(lat1);
        //lat2 = Math.toRadians(lat2);

        //double y = Math.sin(dLon) * Math.cos(lat2);
        //double x = Math.cos(lat1) * Math.sin(lat2) -
        //        Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(dLon, dLat));
        //Log.d(LOG_TAG,"Brng "+brng);
        //Log.d("SetPointer", "Curr: "+dataSource.currentMember.getEmail()+" target : "+target.getEmail()+" Brng " + brng + " dLon " + dLon + " dLat " + dLat + " lat1 " + lat1 + " lat2 " + lat2 + " lon1 " + lon1 + " lon2 " + lon2);
        // fix negative degrees
        if (brng < 0) {
            //    brng = 360 + brng;
        }

        float degree = (float) brng - (dataSource.currentMember.getDegree() * (float) (180.0f / Math.PI));//-(float) brng ;
        //Log.d(LOG_TAG,"degree "+degree);

        if (degree < 0) degree = degree + 360;

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                dataSource.currentMember.getDegree(),
                degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(0);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        if (dataSource.currentMember.getWatchMap().get(target) != null
                && dataSource.currentMember.getWatchMap().get(target).getViewHolder() != null
                && dataSource.currentMember.getWatchMap().get(target).getViewHolder().compassPointer != null) {
            //dataSource.currentMember.getWatchMap().get(target).getViewHolder().compassPointer.startAnimation(ra);
            //Log.d(LOG_TAG, target.getEmail() + " Rotating image by degree: " + degree);
            dataSource.currentMember.getWatchMap().get(target).getViewHolder().compassPointer.setRotation(degree);
        }
        //dataSource.currentMember.setDegree(-degree);
    }

    private class MyLocationHandler implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        public MyLocationHandler() {
            Log.d(LOG_TAG, "In location listener");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "onLocationChanged : " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getAltitude());
            dataSource.currentMember.setLatitude(location.getLatitude());
            dataSource.currentMember.setLongitude(location.getLongitude());

            // Intent email = new Intent(Intent.ACTION_SEND);
            // email.putExtra(Intent.EXTRA_EMAIL, new String[]{"chandan.ravi1987@gmail.com"});
            //email.putExtra(Intent.EXTRA_SUBJECT, "Location changed");
            //  email.putExtra(Intent.EXTRA_TEXT, "location changed, latitude : " + latitude + ", longitude : " + longitude + ", altitude : " + location.getAltitude());

            //need this to prompts email client only
            // email.setType("message/rfc822");

            // startActivity(Intent.createChooser(email, "Choose an Email client :"));

            dataSource.updateLocation(dataSource.currentMember.getLatitude(), dataSource.currentMember.getLongitude(), location.getAltitude());
        }


        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(LOG_TAG, "In onConnected of MyLocationHandler");
            startLocationUpdates();
        }

        public void startLocationUpdates() {
            Log.d(LOG_TAG, "In startLocationUpdates, checking for permissions");

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(LOG_TAG, "In startLocationUpdates, Permissions are not available");

                return;
            }
            Log.d(LOG_TAG, "In startLocationUpdates, permissions are available.");

            if (mGoogleApiClient != null && mLocationRequest != null) {
                Log.d(LOG_TAG, "In startLocationUpdates, Requesting location updates");

                PendingResult<Status> pendingResult =
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(LOG_TAG, "In onConnectionSuspended of MyLocationHandler");

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(LOG_TAG, "In onConnectionFailed of MyLocationHandler");

        }

        protected void stopLocationUpdates() {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(LOG_TAG, "Location update stopped .......................");
        }
    }
}
