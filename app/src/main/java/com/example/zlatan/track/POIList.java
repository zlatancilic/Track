package com.example.zlatan.track;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zlatan.track.Objects.POI;

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
import java.util.ArrayList;
import java.util.List;

public class POIList extends AppCompatActivity {

    List<POI> primjerListe = new ArrayList<POI>();
    String[] commandList = {"Close windows","Stop vehicle","Lock vehicle"};
    private String session_key = null;
    private final String LOG_TAG = POIList.class.getSimpleName();
    SharedPreferences sharedpreferences;
    boolean expiredKey = false;
    POI forUpdate;

    private final String WINDOWS_DOWN_COMMAND = "Close windows";
    private final String WINDOWS_UP_COMMAND = "Open windows";
    private final String ENGINE_SHUT_COMMAND = "Shut engine down";
    private final String ENGINE_START_COMMAND = "Power engine up";
    private final String CAR_LOCK_COMMAND = "Lock vehicle";
    private final String CAR_UNLOCK_COMMAND = "Unlock vehicle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poilist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        expiredKey = false;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().hasExtra("session_key")) {
            session_key = getIntent().getStringExtra("session_key");
        }

        ListView lw = (ListView) findViewById(R.id.poi_list);
        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                POI clicked_poi = (POI) parent.getItemAtPosition(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("poi_id",Integer.toString(clicked_poi.getId()));
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });


    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.poi_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            ListView lv = (ListView) v;
            POI tempObject = (POI) lv.getItemAtPosition(info.position);

            menu.setHeaderTitle("Send command to " + tempObject.getName());
            String[] menuItems = setUpCommands(tempObject);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    private String[] setUpCommands(POI clickedPOI) {
        String commandWindows = (clickedPOI.getWindowsUp()) ? WINDOWS_DOWN_COMMAND : WINDOWS_UP_COMMAND;
        String commandEngine = (clickedPOI.getEngineRunning()) ? ENGINE_SHUT_COMMAND : ENGINE_START_COMMAND;
        String commandLock = (clickedPOI.getCarLocked()) ? CAR_UNLOCK_COMMAND : CAR_LOCK_COMMAND;

        String[] toReturn = {commandEngine, commandLock, commandWindows};
        return toReturn;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String selectedCommand = item.toString();

        POI tempObject = primjerListe.get(info.position);

        tempObject = commandToPOI(tempObject, selectedCommand);
        forUpdate = tempObject;

        //Context context = getApplicationContext();
        //CharSequence text = Integer.toString(forUpdate.getCompanyId());
        //int duration = Toast.LENGTH_SHORT;

        //Toast toast = Toast.makeText(context, text, duration);
        //toast.setGravity(Gravity.BOTTOM, 0, 10);
        //toast.show();

        SendCommandClass task = new SendCommandClass();
        String params[] = {};
        task.execute(params);

        //Intent returnIntent = new Intent();
        //returnIntent.putExtra("poi_id", Integer.toString(tempObject.getId()));
        //returnIntent.putExtra("command_id", Integer.toString(menuItemIndex));
        //setResult(Activity.RESULT_OK, returnIntent);
        //finish();
        return true;
    }

    private POI commandToPOI(POI poi, String command) {
        switch (command) {
            case WINDOWS_DOWN_COMMAND :
                poi.setWindowsUp(false);
                break;
            case WINDOWS_UP_COMMAND :
                poi.setWindowsUp(true);
                break;
            case CAR_UNLOCK_COMMAND :
                poi.setCarLocked(false);
                break;
            case CAR_LOCK_COMMAND:
                poi.setCarLocked(true);
                break;
            case ENGINE_SHUT_COMMAND :
                poi.setEngineRunning(false);
                break;
            case ENGINE_START_COMMAND :
                poi.setEngineRunning(true);
                break;
            default:
                Log.d(LOG_TAG, "Invalid command passed");
        }
        return poi;
    }

    @Override
    public void onResume() {
        expiredKey = false;
        FetchPOIListClass task = new FetchPOIListClass();
        String params[] = {};
        task.execute(params);

        super.onResume();
    }

    public class FetchPOIListClass extends AsyncTask<String, Void, POI[]> {
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
            final String ADDED_PATH = "v1/poi";

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("GET");
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
                    JSONArray poiList = responseJsonObject.getJSONArray("pois");
                    List<POI> tempData = new ArrayList<POI>();
                    for(int i = 0; i < poiList.length(); i++) {
                        JSONObject singlePoi = poiList.getJSONObject(i);
                        String singlePoiName = singlePoi.getString("name");
                        int singlePOIId = singlePoi.getInt("id");
                        int singlePOICompanyId = singlePoi.getInt("company_id");
                        String singlePOIDescription = singlePoi.getString("description");
                        String singlePOIDate = singlePoi.getString("date_added");
                        float singlePOILat = (float) singlePoi.getDouble("lat");
                        float singlePOILon = (float) singlePoi.getDouble("lng");
                        JSONObject singlePOIState = singlePoi.getJSONObject("state");
                        boolean singlePOIEngine = singlePOIState.getBoolean("motor_running");
                        boolean singlePOIWindows = singlePOIState.getBoolean("windows_up");
                        boolean singlePOILock = singlePOIState.getBoolean("car_locked");
                        POI tempObject = new POI(singlePOIId, singlePoiName, singlePOIDescription, singlePOIDate, singlePOILat, singlePOILon, singlePOICompanyId);
                        tempObject.setEngineRunning(singlePOIEngine);
                        tempObject.setWindowsUp(singlePOIWindows);
                        tempObject.setCarLocked(singlePOILock);
                        tempData.add(tempObject);
                    }
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

        @Override
        protected void onPostExecute(POI[] result) {
            if(result != null) {
                List<POI> tempList = new ArrayList<POI>();
                for(int i = 0; i < result.length; i++) {
                    tempList.add(result[i]);
                }

                primjerListe = tempList;

                ListView lw = (ListView) findViewById(R.id.poi_list);
                ArrayAdapter<POI> adapter = new ArrayAdapter<POI>(POIList.this, android.R.layout.simple_list_item_1, tempList);
                lw.setAdapter(adapter);
                registerForContextMenu(lw);
            }
            else {
                if(expiredKey) {
                    RefreshSessionKey rfk = new RefreshSessionKey();
                    String rfkParams[] = {};
                    rfk.execute(rfkParams);
                    FetchPOIListClass task = new FetchPOIListClass();
                    String params[] = {};
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
        }
    }

    public class SendCommandClass extends AsyncTask<String, Void, String> {
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
            final String ADDED_PATH = "v1/poi/" + Integer.toString(forUpdate.getId());;

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("PUT");
                apiConnection.setRequestProperty("Authorization", "Token token=" + local_session_key);
                apiConnection.setRequestProperty("Content-Type", "application/json");
                apiConnection.setDoOutput(true);
                apiConnection.setDoInput(true);


                JSONObject poi_json = new JSONObject();
                poi_json.put("name", forUpdate.getName());
                poi_json.put("description", forUpdate.getDescription());
                poi_json.put("date_added", forUpdate.getDateAdded());
                poi_json.put("lat", forUpdate.getLatitude());
                poi_json.put("lng", forUpdate.getLongitude());
                poi_json.put("company_id", forUpdate.getCompanyId());

                JSONObject state = new JSONObject();
                state.put("motor_running", forUpdate.getEngineRunning());
                state.put("windows_up", forUpdate.getWindowsUp());
                state.put("car_locked", forUpdate.getCarLocked());

                poi_json.put("state", state);

                JSONObject main_json = new JSONObject();
                main_json.put("point_of_interest", poi_json);

                String str = main_json.toString();
                byte[] outputInBytes = str.getBytes("UTF-8");
                OutputStream os = apiConnection.getOutputStream();
                os.write(outputInBytes);
                os.close();

                apiConnection.connect();

                int responseCode = apiConnection.getResponseCode();
                if (responseCode == 204)
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
            catch (JSONException jex) {

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

        @Override
        protected void onPostExecute(String result) {
            if(expiredKey && result == null) {
                RefreshSessionKey rfk = new RefreshSessionKey();
                String rfkParams[] = {};
                rfk.execute(rfkParams);
                SendCommandClass task = new SendCommandClass();
                String params[] = {};
                task.execute(params);
            }
            else {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("poi_id", Integer.toString(forUpdate.getId()));
                returnIntent.putExtra("command_id", 0);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }



}
