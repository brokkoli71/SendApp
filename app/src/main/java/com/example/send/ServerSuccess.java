package com.example.send;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerSuccess extends AsyncTask<Integer, String, String> {
    private static final int CONNECTION_TIMEOUT=10000;
    private static final int READ_TIMEOUT=15000;

    private MainActivity mainActivity;

    private final String server_url;
    private final String server_pwd;

    public ServerSuccess(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.server_url = mainActivity.getString(R.string.server_url_success);
        this.server_pwd = mainActivity.getString(R.string.pwd);
    }

    @Override
    protected String doInBackground(Integer... taskID) {
        HttpURLConnection conn;
        try {
            URL url = new URL(server_url);

            conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(false);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("password", server_pwd)
                    .appendQueryParameter("task_id", Integer.toString(taskID[0]));
            String query = builder.build().getEncodedQuery();

            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            Log.w("server_success", "response send");

        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e("server_receiver", "connectivity error");
            Toaster.makeToast("konnte keine Verbindung aufbauen");
            return "connectivity error";
        }
        return null;
    }
}
