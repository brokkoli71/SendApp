package com.example.send;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerReceiver extends AsyncTask<String, Void, String> {
    private static final int CONNECTION_TIMEOUT=10000;
    private static final int READ_TIMEOUT=15000;
    final int WAITING_TIME = 500;

    private final String server_url;
    private final String server_pwd;

    ServerReceiver(String url, String password){
        this.server_url = url;
        this.server_pwd = password;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.w("server_receiver", s);
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection conn;
        Log.w("server_receiver", "start");
        try {
            URL url = new URL(server_url);

            conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);
            Log.w("server_receiver", "setup ready");
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("password", server_pwd)
                    .appendQueryParameter("receiver", strings[0]);
            String query = builder.build().getEncodedQuery();
            Log.w("server_receiver", "query ready");

            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            Log.w("server_receiver", "request send");

        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e("server_receiver", "connectivity error");
            Toaster.makeToast("konnte keine Verbindung aufbauen");
            return "connectivity error";
        }

        try {
            int response_code = conn.getResponseCode();
            Log.w("server_receiver", "got response:"+response_code);
            // Check if successful connection made
            if (response_code == HttpURLConnection.HTTP_OK) {

                // Read data sent from server
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Toaster.makeToast(result.toString(), true);
                //todo: handle result.toString());
                return "success";
            }else{

                return("unsuccessful");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "exception";
        } finally {
            conn.disconnect();
        }
    }
}
