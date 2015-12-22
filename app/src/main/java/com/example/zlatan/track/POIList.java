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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class POIList extends AppCompatActivity {

    List<POI> primjerListe = new ArrayList<POI>();
    String[] commandList = {"Close windows","Stop vehicle","Lock vehicle"};
    private String session_key = null;
    private final String LOG_TAG = POIList.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poilist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().hasExtra("session_key")) {
            session_key = getIntent().getStringExtra("session_key");
        }

        ListView lw = (ListView) findViewById(R.id.poi_list);
        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("poi_id",Integer.toString(position));
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
            POI tempObject = primjerListe.get(info.position);
            menu.setHeaderTitle("Send command to " + tempObject.getName());
            String[] menuItems = commandList;
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        POI tempObject = primjerListe.get(info.position);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("poi_id", Integer.toString(tempObject.getId()));
        returnIntent.putExtra("command_id", Integer.toString(menuItemIndex));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return true;
    }

    @Override
    public void onResume() {
        FetchPOIListClass task = new FetchPOIListClass();
        String params[] = {session_key};
        task.execute(params);

        super.onResume();
    }

    public class FetchPOIListClass extends AsyncTask<String, Void, POI[]> {
        @Override
        protected POI[] doInBackground(String... params) {
            HttpURLConnection apiConnection = null;
            BufferedReader responseBuffer = null;
            String apiResponse = null;
            String local_session_key = params[0];

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
                    JSONArray poiList = responseJsonObject.getJSONArray("point_of_interests");
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

                primjerListe = tempList;

                ListView lw = (ListView) findViewById(R.id.poi_list);
                ArrayAdapter<POI> adapter = new ArrayAdapter<POI>(POIList.this, android.R.layout.simple_list_item_1, tempList);
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
