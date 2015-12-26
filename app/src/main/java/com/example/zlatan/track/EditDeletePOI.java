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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zlatan.track.Objects.POI;

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

public class EditDeletePOI extends AppCompatActivity {

    private final String LOG_TAG = AddPOI.class.getSimpleName();
    SharedPreferences sharedpreferences;
    POI selectedPOI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete_poi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent receiveIntent = getIntent();
        if(receiveIntent.hasExtra("poi")) {
            selectedPOI = (POI) receiveIntent.getSerializableExtra("poi");
            int poiId = selectedPOI.getId();

            TextView nameTV = (TextView) findViewById(R.id.poi_name_edit);
            TextView descriptionTV = (TextView) findViewById(R.id.poi_description_edit);
            nameTV.setText(selectedPOI.getName());
            descriptionTV.setText(selectedPOI.getDescription());

            Context context = getApplicationContext();
            CharSequence text = Integer.toString(poiId);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }


        Button deleteBT = (Button) findViewById(R.id.delete_button);
        deleteBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePOIClass task = new DeletePOIClass();
                String params[] = {};
                task.execute(params);
            }
        });

        Button editBT = (Button) findViewById(R.id.edit_button);
        editBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView nameTV = (TextView) findViewById(R.id.poi_name_edit);
                TextView descriptionTV = (TextView) findViewById(R.id.poi_description_edit);
                selectedPOI.setName(nameTV.getText().toString());
                selectedPOI.setDescription(descriptionTV.getText().toString());
                EditPOIClass task = new EditPOIClass();
                String params[] = {};
                task.execute(params);

            }
        });


    }

    public class DeletePOIClass extends AsyncTask<String, Void, String> {
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
            final String ADDED_PATH = "v1/poi/" + Integer.toString(selectedPOI.getId());

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("DELETE");
                apiConnection.setRequestProperty("Authorization", "Token token=" + local_session_key);

                apiConnection.connect();

                int responseCode = apiConnection.getResponseCode();
                if (responseCode == 204)
                    return "OK";

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
            Intent returnIntent = new Intent();
            String extra = "";
            if(result == null)
                extra = "Error while trying to delete POI";
            else if(result.equals("OK"))
                extra = "POI successfully deleted";
            returnIntent.putExtra("result", extra);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }

    public class EditPOIClass extends AsyncTask<String, Void, String> {
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
            final String ADDED_PATH = "v1/poi/" + Integer.toString(selectedPOI.getId());;

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
                poi_json.put("name", selectedPOI.getName());
                poi_json.put("description", selectedPOI.getDescription());
                poi_json.put("date_added", selectedPOI.getDateAdded());
                poi_json.put("lat", selectedPOI.getLatitude());
                poi_json.put("lng", selectedPOI.getLongitude());
                poi_json.put("company_id", selectedPOI.getCompanyId());

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
            Intent returnIntent = new Intent();
            String extra = "";
            if(result == null)
                extra = "Error while trying to edit POI";
            else if(result.equals("OK"))
                extra = "POI successfully edited";
            returnIntent.putExtra("result", extra);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }
}
