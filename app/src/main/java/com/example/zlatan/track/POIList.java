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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    List<String> primjerListe = new ArrayList<String>();
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

        //primjerListe = new ArrayList<String>();

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
            menu.setHeaderTitle("Send command to " + primjerListe.get(info.position));
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

        Intent returnIntent = new Intent();
        returnIntent.putExtra("poi_id", Integer.toString(info.position));
        returnIntent.putExtra("command_id", Integer.toString(menuItemIndex));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return true;
    }

    @Override
    public void onResume() {
        /*primjerListe.add("Vozilo 1");
        primjerListe.add("Vozilo 2");
        primjerListe.add("Vozilo 3");
        primjerListe.add("Vozilo 4");

        ListView lw = (ListView) findViewById(R.id.poi_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, primjerListe);
        lw.setAdapter(adapter);
        registerForContextMenu(lw);*/
        FetchPOIListClass task = new FetchPOIListClass();
        String params[] = {session_key};
        task.execute(params);

        super.onResume();
    }

    public class FetchPOIListClass extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
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
                    List<String> tempData = new ArrayList<String>();
                    for(int i = 0; i < poiList.length(); i++) {
                        JSONObject singlePoi = poiList.getJSONObject(i);
                        String singlePoiName = singlePoi.getString("name");
                        tempData.add(singlePoiName);
                    }
                    String[] data = new String[tempData.size()];
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
        protected void onPostExecute(String[] result) {
            if(result != null) {
                List<String> tempList = new ArrayList<String>();
                for(int i = 0; i < result.length; i++) {
                    tempList.add(result[i]);
                }

                primjerListe = tempList;

                ListView lw = (ListView) findViewById(R.id.poi_list);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(POIList.this, android.R.layout.simple_list_item_1, tempList);
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
