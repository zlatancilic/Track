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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AddPOI extends AppCompatActivity {

    List<String> poi_types_array;
    private final String LOG_TAG = AddPOI.class.getSimpleName();
    private String session_key = null;
    SharedPreferences sharedpreferences;
    String fetched_poi_name = "";
    String fetched_poi_description = "";
    double fetched_poi_lat = 0.0;
    double fetched_poi_long = 0.0;
    int fetched_company_id = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
/*
        poi_types_array = new ArrayList<String>();
        poi_types_array.add("Vehicle");
        poi_types_array.add("Cellphone");
        poi_types_array.add("Buliding");


        Spinner spinner = (Spinner) findViewById(R.id.poi_type_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                R.layout.spinner_item, poi_types_array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select Type");
*/


        Button fab = (Button) findViewById(R.id.create_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView poi_name_tw = (TextView) findViewById(R.id.poi_name);
                TextView poi_desc_tw = (TextView) findViewById(R.id.poi_description);

                fetched_poi_name = poi_name_tw.getText().toString();
                fetched_poi_description = poi_desc_tw.getText().toString();

                AddPOIClass task = new AddPOIClass();
                String params[] = {};
                task.execute(params);
            }
        });
    }

    public class AddPOIClass extends AsyncTask<String, Void, POI[]> {
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
                apiConnection.setRequestMethod("POST");
                apiConnection.setRequestProperty("Authorization", "Token token=" + local_session_key);
                apiConnection.setRequestProperty("Content-Type", "application/json");
                apiConnection.setDoOutput(true);
                apiConnection.setDoInput(true);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date today = Calendar.getInstance().getTime();
                String fetched_date = df.format(today);

                double max_lat = 50.00;
                double min_lat = 45.70;
                double max_long = 22.00;
                double min_long = 2.00;

                Random r = new Random();
                fetched_poi_lat = min_lat + (max_lat - min_lat) * r.nextDouble();
                fetched_poi_long = min_long + (max_long - min_long) * r.nextDouble();

                JSONObject poi_json = new JSONObject();
                poi_json.put("name", fetched_poi_name);
                poi_json.put("description", fetched_poi_description);
                poi_json.put("date_added", fetched_date);
                poi_json.put("lat", fetched_poi_lat);
                poi_json.put("lng", fetched_poi_long);
                poi_json.put("company_id", fetched_company_id);

                JSONObject main_json = new JSONObject();
                main_json.put("point_of_interest", poi_json);
                
                String str = main_json.toString();
                byte[] outputInBytes = str.getBytes("UTF-8");
                OutputStream os = apiConnection.getOutputStream();
                os.write(outputInBytes);
                os.close();

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
                    JSONObject singlePoi = responseJsonObject.getJSONObject("point_of_interest");
                    List<POI> tempData = new ArrayList<POI>();
                    String singlePoiName = singlePoi.getString("name");
                    int singlePOIId = singlePoi.getInt("id");
                    String singlePOIDescription = singlePoi.getString("description");
                    String singlePOIDate = singlePoi.getString("date_added");
                    POI tempObject = new POI(singlePOIId, singlePoiName, singlePOIDescription, singlePOIDate);
                    tempData.add(tempObject);
                    POI[] data = new POI[tempData.size()];
                    tempData.toArray(data);
                    return data;
                }
                catch(JSONException je) {
                    Log.e(LOG_TAG, "JSON Error", je);
                }


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
        protected void onPostExecute(POI[] result) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("poi_id", Integer.toString(result[0].getId()));
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }

}
