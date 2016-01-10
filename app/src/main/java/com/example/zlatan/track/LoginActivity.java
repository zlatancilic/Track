package com.example.zlatan.track;

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
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private final String LOG_TAG = LoginActivity.class.getSimpleName();

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String KeyUsername = "usernameTestKey";
    public static final String KeyPassword = "passwordTestKey";
    public static final String KeyAppKey = "appKeyTestKey";
    public static final String KeyComapnyId = "comapnyIdTestKey";
    SharedPreferences sharedpreferences;
    int fetchedCompanyId;
    String enteredUsername;
    String enteredPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button signInBtn = (Button) findViewById(R.id.sign_in_button);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInAttempt(v);
            }
        });
    }

    private void signInAttempt(View v) {
        EditText usernameET = (EditText) findViewById(R.id.username);
        EditText passwordET = (EditText) findViewById(R.id.password);

        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();

        enteredUsername = username;
        enteredPassword = password;

        FetchSessionKeyClass task = new FetchSessionKeyClass();
        String params[] = {username, password};
        task.execute(params);
    }

    public class FetchSessionKeyClass extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection apiConnection = null;
            BufferedReader responseBuffer = null;
            String apiResponse = null;
            String username = params[0];
            String password = params[1];

            final String BASE_URL = "tracking-service-api.herokuapp.com";
            final String ADDED_PATH = "v1/login";

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(BASE_URL)
                        .appendEncodedPath(ADDED_PATH);
                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                apiConnection = (HttpURLConnection) url.openConnection();
                apiConnection.setRequestMethod("GET");
                apiConnection.setRequestProperty("Authorization", getB64Auth(username, password));
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
                    JSONObject resposneJsonObject = new JSONObject(apiResponse);
                    JSONObject userData = resposneJsonObject.getJSONObject("user");
                    String key = userData.getString("session_key");
                    fetchedCompanyId = userData.getInt("company_id");
                    return key;
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
        protected void onPostExecute(String result) {
            if(result != null) {
                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(KeyUsername, enteredUsername);
                editor.putString(KeyPassword, enteredPassword);
                editor.putString(KeyAppKey, result);
                editor.putInt(KeyComapnyId, fetchedCompanyId);
                editor.commit();

                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                //mainActivityIntent.putExtra("user_id", result);
                startActivity(mainActivityIntent);
            }
            else {
                Context context = getApplicationContext();
                CharSequence text = "Password or username not correct";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.BOTTOM, 0, 10);
                toast.show();
            }
        }
    }

    @Override
    protected void onResume() {
        //sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(KeyUsername) && sharedpreferences.contains(KeyPassword)) {
            String username = sharedpreferences.getString(KeyUsername, null);
            String password = sharedpreferences.getString(KeyPassword, null);
            String appKey = sharedpreferences.getString(KeyAppKey, null);
            if (!username.equals(null) && !password.equals(null)) {
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                mainActivityIntent.putExtra("user_id", appKey);
                startActivity(mainActivityIntent);
            }
        }
        super.onResume();
    }

    private String getB64Auth (String login, String pass) {
        String source=login+":"+pass;
        String ret="Basic "+Base64.encodeToString(source.getBytes(),Base64.URL_SAFE| Base64.NO_WRAP);
        return ret;
    }

}
