package com.example.zlatan.track;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zlatan on 07/01/16.
 */
public class RefreshSessionKey extends AsyncTask<String, Void, String> {

    public static final String KeyAppKey = "appKeyTestKey";
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String KeyUsername = "usernameTestKey";
    public static final String KeyPassword = "passwordTestKey";
    SharedPreferences sharedpreferences;
    private final String LOG_TAG = RefreshSessionKey.class.getSimpleName();
    int fetchedCompanyId;

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection apiConnection = null;
        BufferedReader responseBuffer = null;
        String apiResponse = null;
        Context applicationContext = MainActivity.getContextOfApplication();
        sharedpreferences = applicationContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String username = sharedpreferences.getString(KeyUsername, null);
        String password = sharedpreferences.getString(KeyPassword, null);


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
        editor.putString(KeyAppKey, result);editor.putInt(LoginActivity.KeyComapnyId, fetchedCompanyId);
        editor.commit();
        }
    }

    private String getB64Auth (String login, String pass) {
        String source=login+":"+pass;
        String ret="Basic "+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        return ret;
    }
}