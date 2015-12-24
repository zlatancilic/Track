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
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.zlatan.track.Objects.POI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EditCreateListPOI extends AppCompatActivity {

    List<String> primjerListe = new ArrayList<String>();
    static final int ADD_POI_REQUEST = 5;
    private final String LOG_TAG = EditCreateListPOI.class.getSimpleName();
    private String session_key = null;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_create_list_poi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPoiIntent = new Intent(getApplicationContext(), AddPOI.class);
                //pickPoiIntent.putExtra("session_key", session_key);
                startActivityForResult(addPoiIntent, ADD_POI_REQUEST);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        /*primjerListe.add("Vozilo 1");
        primjerListe.add("Vozilo 2");
        primjerListe.add("Vozilo 3");
        primjerListe.add("Vozilo 4");

        ListView lw = (ListView) findViewById(R.id.edit_create_poi_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, primjerListe);
        lw.setAdapter(adapter);*/
        ListView lw = (ListView) findViewById(R.id.edit_create_poi_list);
        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                POI clicked_poi = (POI) parent.getItemAtPosition(position);
                Intent editPoiIntent = new Intent(getApplicationContext(), EditDeletePOI.class);
                editPoiIntent.putExtra("poi_id", clicked_poi.getId());
                startActivityForResult(editPoiIntent, ADD_POI_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ADD_POI_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if(data.hasExtra("poi_id")) {
                    String poiId = data.getStringExtra("poi_id");
                    Context context = getApplicationContext();
                    //String name = data.getStringExtra("poi_id");
                    CharSequence text = "Added POI with ID " + poiId;
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }
    }

    @Override
    public void onResume() {
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

                InputStream inputStream = apiConnection.getInputStream();
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
                    JSONArray poiList = responseJsonObject.getJSONArray("pois");
                    List<POI> tempData = new ArrayList<POI>();
                    for(int i = 0; i < poiList.length(); i++) {
                        JSONObject singlePoi = poiList.getJSONObject(i);
                        String singlePoiName = singlePoi.getString("name");
                        int singlePOIId = singlePoi.getInt("id");
                        String singlePOIDescription = singlePoi.getString("description");
                        String singlePOIDate = singlePoi.getString("date_added");
                        POI tempObject = new POI(singlePOIId, singlePoiName, singlePOIDescription, singlePOIDate);
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

                ListView lw = (ListView) findViewById(R.id.edit_create_poi_list);
                ArrayAdapter<POI> adapter = new ArrayAdapter<POI>(EditCreateListPOI.this, android.R.layout.simple_list_item_1, tempList);
                lw.setAdapter(adapter);
                registerForContextMenu(lw);
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
