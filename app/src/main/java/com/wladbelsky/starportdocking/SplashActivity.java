package com.wladbelsky.starportdocking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SplashActivity extends AppCompatActivity {

    public static final String serverIP = "http://212.232.72.212";//"http://10.0.2.2";
    private static String token = "";
    private static SharedPreferences mSettings;
    public static final String APP_PREF_FILENAME = "tokens";
    public static final String APP_PREF_TOKEN = "login_token";

    public static void setToken(String t)
    {
        token = t;
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREF_TOKEN, t);
        editor.apply();
    }

    public static void resetToken()
    {
        setToken("");
    }

    public static String getToken()
    {
        return token;
    }

    public static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        // read the JSON results into a string

        String jsonResult = inputStream.readLine();
        return jsonResult;
    }

    public static String postJsonOnServer(String urlString, String params)
    {
        OutputStream out = null;

        try {
            //String myURL = "http://myserver.com";
            //String params = "param1=1&param2=XXX";
            byte[] data = null;
            InputStream is = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
                OutputStream os = conn.getOutputStream();
                data = params.getBytes("UTF-8");
                os.write(data);
                data = null;

                conn.connect();
                int responseCode= conn.getResponseCode();
                Log.i("oracle", responseCode+"");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                is = conn.getInputStream();

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                data = baos.toByteArray();
            } catch (Exception e) {
                Log.e("oracle", e.getMessage());
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Exception ex) {}
            }

            return new String(data, "UTF-8");
        } catch (Exception e) {
            Log.e("oracle",e.getMessage());
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mSettings = getSharedPreferences(APP_PREF_FILENAME, Context.MODE_PRIVATE);
        token = mSettings.getString(APP_PREF_TOKEN, "");
        Log.i("oracle", token);
        Intent intent;
        if(token.isEmpty())
        {
            intent = new Intent(this, LoginActivity.class);
        }
        else
        {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);

        finish();
    }
}
