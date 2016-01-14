package com.example.zlatan.track;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.zlatan.track.Objects.POI;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static Context contextOfApplication;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    SharedPreferences sharedpreferences;
    int activeTrackingPOIID;
    MarkerOptions currentMarker = null;
    boolean trackingActive = false;
    boolean expiredKey = false;

    private MapView mapView = null;
    private String session_key = null;

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contextOfApplication = getApplicationContext();

        expiredKey = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackingActive = false;
                StopTrackingPOIClass task = new StopTrackingPOIClass();
                String params[] = {Integer.toString(activeTrackingPOIID)};
                task.execute(params);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapView = (MapView) findViewById(R.id.mapboxMapView);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(new LatLng(43.853258, 18.411144));
        mapView.setZoomLevel(10);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    static final int PICK_POI_REQUEST = 1;
    static final int EDIT_POI_REQUEST = 2;
    static final int SEND_COMMAND_REQUEST = 2;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_poilist) {

            Intent pickPoiIntent = new Intent(this, POIList.class);
            startActivityForResult(pickPoiIntent, PICK_POI_REQUEST);
        }
        else if(id == R.id.nav_poi_edit_create) {
            Intent pickPoiIntent = new Intent(this, EditCreateListPOI.class);
            startActivityForResult(pickPoiIntent, EDIT_POI_REQUEST);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_POI_REQUEST) {
            if (resultCode == RESULT_OK) {
                if(data.hasExtra("command_id")) {
                    //String poiId = data.getStringExtra("poi_id");
                    //String commandId = data.getStringExtra("command_id");
                    Context context = getApplicationContext();
                    //String name = data.getStringExtra("poi_id");
                    CharSequence text = "Click on POI to see state.";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    String dataMessage = data.getStringExtra("poi_id");
                    activeTrackingPOIID = Integer.parseInt(dataMessage);
                    StartTrackingPOIClass task = new StartTrackingPOIClass();
                    String params[] = {dataMessage};
                    task.execute(params);
                }
                else {
                    String dataMessage = data.getStringExtra("poi_id");
                    activeTrackingPOIID = Integer.parseInt(dataMessage);
                    StartTrackingPOIClass task = new StartTrackingPOIClass();
                    String params[] = {dataMessage};
                    task.execute(params);
                }


            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        expiredKey = false;
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public class StartTrackingPOIClass extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection apiConnection = null;
            BufferedReader responseBuffer = null;
            String apiResponse = null;


            sharedpreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
            String local_session_key = sharedpreferences.getString(LoginActivity.KeyAppKey, null);

            if(local_session_key == null) {
                return null;
            }

            final String BASE_URL = "tracking-service-api.herokuapp.com";
            final String ADDED_PATH = "v1/poi/" + params[0] + "/start_tracking";

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("POST");
                apiConnection.setRequestProperty("Authorization", "Token token=" + local_session_key);

                apiConnection.connect();

                int responseCode = apiConnection.getResponseCode();
                if(responseCode == 204 || responseCode == 422)
                    return "OK";
                else if(responseCode == 401) {
                    InputStream inputStream = apiConnection.getErrorStream();
                    StringBuffer stringBuffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }

                    responseBuffer = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while((line = responseBuffer.readLine()) != null) {
                        stringBuffer.append(line + "\n");
                    }

                    if(stringBuffer.length() == 0) {
                        return null;
                    }

                    apiResponse = stringBuffer.toString();
                    try {
                        JSONObject responseJsonObject = new JSONObject(apiResponse);
                        if(responseJsonObject.has("error")) {
                            String message = responseJsonObject.getString("error");
                            if(message.equals("No authorization")) {
                                expiredKey = true;
                                return null;
                            }
                        }
                    }
                    catch(JSONException je) {
                        Log.e(LOG_TAG, "JSON Error", je);
                    }

                }

                return null;
            }
            catch (IOException ex) {
                Log.e(LOG_TAG, "Error", ex);
                return null;
            }
            finally {
                if(apiConnection != null) {
                    apiConnection.disconnect();
                }
                if(responseBuffer != null) {
                    try {
                        responseBuffer.close();
                    }
                    catch (final IOException exc) {
                        Log.e(LOG_TAG, "Error closing stream", exc);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                if(expiredKey) {
                    RefreshSessionKey rfk = new RefreshSessionKey();
                    String rfkParams[] = {};
                    rfk.execute(rfkParams);
                    StartTrackingPOIClass task = new StartTrackingPOIClass();
                    String params[] = {Integer.toString(activeTrackingPOIID)};
                    task.execute(params);
                }
                else {
                    Context context = getApplicationContext();
                    CharSequence text = "Something went wrong";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.BOTTOM, 0, 10);
                    toast.show();
                }
            }
            else if(result.equals("OK")) {
                trackingActive = true;
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
                fab.setVisibility(View.VISIBLE);
                TrackPOIClass task = new TrackPOIClass();
                String params[] = {};
                task.execute(params);
            }
        }
    }

    public class TrackPOIClass extends AsyncTask<String, Void, POI[]> {
        private final String WINDOWS_DOWN_INFO = "Windows DOWN";
        private final String WINDOWS_UP_INFO = "Windows UP";
        private final String ENGINE_SHUT_INFO = "Engine SHUT";
        private final String ENGINE_START_INFO = "Engine RUNNING";
        private final String CAR_LOCK_INFO = "Vehicle LOCKED";
        private final String CAR_UNLOCK_INFO = "Vehicle UNLOCKED";
        @Override
        protected POI[] doInBackground(String... params) {
            HttpURLConnection apiConnection = null;
            BufferedReader responseBuffer = null;
            String apiResponse = null;


            sharedpreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
            String local_session_key = sharedpreferences.getString(LoginActivity.KeyAppKey, null);

            if(local_session_key == null) {
                POI toReturn[] = null;
                return toReturn;
            }

            final String BASE_URL = "tracking-service-api.herokuapp.com";
            final String ADDED_PATH = "v1/poi/" + Integer.toString(activeTrackingPOIID) + "/track";

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("POST");
                apiConnection.setRequestProperty("Authorization", "Token token=" + local_session_key);

                apiConnection.connect();
                InputStream inputStream;
                int responseCode = apiConnection.getResponseCode();
                if (responseCode == 401) {
                    inputStream = apiConnection.getErrorStream();
                }
                else {
                    inputStream = apiConnection.getInputStream();
                }
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                responseBuffer = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = responseBuffer.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }

                if(stringBuffer.length() == 0) {
                    return null;
                }

                apiResponse = stringBuffer.toString();
                try {
                    JSONObject responseJsonObject = new JSONObject(apiResponse);
                    if(responseJsonObject.has("error")) {
                        String message = responseJsonObject.getString("error");
                        if(message.equals("No authorization")) {
                            expiredKey = true;
                            return null;
                        }
                    }
                    JSONObject singlePoi = responseJsonObject.getJSONObject("poi");
                    List<POI> tempData = new ArrayList<POI>();
                    String singlePoiName = singlePoi.getString("name");
                    int singlePOIId = singlePoi.getInt("id");
                    String singlePOIDescription = singlePoi.getString("description");
                    String singlePOIDate = singlePoi.getString("date_added");
                    float singlePOILat = (float) singlePoi.getDouble("lat");
                    float singlePOILon = (float) singlePoi.getDouble("lng");
                    int singlePOICompanyId = singlePoi.getInt("company_id");
                    JSONObject singlePOIState = singlePoi.getJSONObject("state");
                    boolean singlePOIEngine = singlePOIState.getBoolean("motor_running");
                    boolean singlePOIWindows = singlePOIState.getBoolean("windows_up");
                    boolean singlePOILock = singlePOIState.getBoolean("car_locked");
                    POI tempObject = new POI(singlePOIId, singlePoiName, singlePOIDescription, singlePOIDate, singlePOILat, singlePOILon, singlePOICompanyId);
                    tempObject.setEngineRunning(singlePOIEngine);
                    tempObject.setWindowsUp(singlePOIWindows);
                    tempObject.setCarLocked(singlePOILock);
                    tempData.add(tempObject);
                    POI[] data = new POI[tempData.size()];
                    tempData.toArray(data);
                    return data;
                }
                catch(JSONException je) {
                    Log.e(LOG_TAG, "JSON Error", je);
                }


            }
            catch (IOException ex) {
                Log.e(LOG_TAG, "Error", ex);
                return null;
            }
            finally {
                if(apiConnection != null) {
                    apiConnection.disconnect();
                }
                if(responseBuffer != null) {
                    try {
                        responseBuffer.close();
                    }
                    catch (final IOException exc) {
                        Log.e(LOG_TAG, "Error closing stream", exc);
                    }
                }
            }

            return null;
        }

        private String setInfo(POI clickedPOI) {
            String commandWindows = (!clickedPOI.getWindowsUp()) ? WINDOWS_DOWN_INFO : WINDOWS_UP_INFO;
            String commandEngine = (!clickedPOI.getEngineRunning()) ? ENGINE_SHUT_INFO : ENGINE_START_INFO;
            String commandLock = (!clickedPOI.getCarLocked()) ? CAR_UNLOCK_INFO : CAR_LOCK_INFO;

            return commandEngine + "\n" + commandWindows + "\n" + commandLock;
        }

        @Override
        protected void onPostExecute(POI[] result) {
            if(trackingActive) {
                if (result != null) {
                    if (result.length == 1) {
                        LatLng latLong = new LatLng(result[0].getLatitude(), result[0].getLongitude());
                        if (mapView != null) {
                            if (latLong != null) {
                                if (currentMarker != null) {
                                    mapView.removeMarker(currentMarker.getMarker());
                                }
                                mapView.setCenterCoordinate(latLong);
                                mapView.setZoomLevel(15);
                                currentMarker = new MarkerOptions().position(latLong).title("Vozilo" + result[0].getName()).snippet(setInfo(result[0]));
                                mapView.addMarker(currentMarker);
                                currentMarker.getMarker().showInfoWindow();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        TrackPOIClass task = new TrackPOIClass();
                                        String params[] = {};
                                        task.execute(params);
                                    }
                                }, 1000);
                            } else {
                                Log.d("MissingValues", "Variable latLong is null");
                            }
                        } else {
                            Log.d("MissingValues", "Variable mapView is null");
                        }
                    }

                }
                else {
                    if(expiredKey) {
                        RefreshSessionKey rfk = new RefreshSessionKey();
                        String rfkParams[] = {};
                        rfk.execute(rfkParams);
                        TrackPOIClass task = new TrackPOIClass();
                        String params[] = {};
                        task.execute(params);
                    }
                }
            }
        }
    }

    public class StopTrackingPOIClass extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection apiConnection = null;
            BufferedReader responseBuffer = null;
            String apiResponse = null;


            sharedpreferences = getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
            String local_session_key = sharedpreferences.getString(LoginActivity.KeyAppKey, null);

            if(local_session_key == null) {
                return null;
            }

            final String BASE_URL = "tracking-service-api.herokuapp.com";
            final String ADDED_PATH = "v1/poi/" + params[0] + "/stop_tracking";

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("POST");
                apiConnection.setRequestProperty("Authorization", "Token token=" + local_session_key);

                apiConnection.connect();

                int responseCode = apiConnection.getResponseCode();
                if(responseCode == 204 || responseCode == 422)
                    return "OK";
                else if(responseCode == 401) {
                    InputStream inputStream = apiConnection.getErrorStream();
                    StringBuffer stringBuffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }

                    responseBuffer = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while((line = responseBuffer.readLine()) != null) {
                        stringBuffer.append(line + "\n");
                    }

                    if(stringBuffer.length() == 0) {
                        return null;
                    }

                    apiResponse = stringBuffer.toString();
                    try {
                        JSONObject responseJsonObject = new JSONObject(apiResponse);
                        if(responseJsonObject.has("error")) {
                            String message = responseJsonObject.getString("error");
                            if(message.equals("No authorization")) {
                                expiredKey = true;
                                return null;
                            }
                        }
                    }
                    catch(JSONException je) {
                        Log.e(LOG_TAG, "JSON Error", je);
                    }

                }

                return null;
            }
            catch (IOException ex) {
                Log.e(LOG_TAG, "Error", ex);
                return null;
            }
            finally {
                if(apiConnection != null) {
                    apiConnection.disconnect();
                }
                if(responseBuffer != null) {
                    try {
                        responseBuffer.close();
                    }
                    catch (final IOException exc) {
                        Log.e(LOG_TAG, "Error closing stream", exc);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                if(expiredKey) {
                    RefreshSessionKey rfk = new RefreshSessionKey();
                    String rfkParams[] = {};
                    rfk.execute(rfkParams);
                    StopTrackingPOIClass task = new StopTrackingPOIClass();
                    String params[] = {Integer.toString(activeTrackingPOIID)};
                    task.execute(params);
                }
                else {
                    Context context = getApplicationContext();
                    CharSequence text = "Something went wrong";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.BOTTOM, 0, 10);
                    toast.show();
                }
            }
            else if(result.equals("OK")) {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
                fab.setVisibility(View.INVISIBLE);
                Context context = getApplicationContext();
                CharSequence text = "Tracking stopped";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.BOTTOM, 0, 10);
                toast.show();
            }
        }
    }
}
