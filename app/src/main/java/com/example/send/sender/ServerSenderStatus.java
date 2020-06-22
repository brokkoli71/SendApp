package com.example.send.sender;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ServerSenderStatus {

    private static final int CONNECTION_TIMEOUT=10000;
    private static final int READ_TIMEOUT=15000;

    String url_response, taskID, password, query;
    HttpURLConnection conn;

    public ServerSenderStatus(String url_response, String taskID, String password) {
        this.url_response = url_response;
        this.taskID = taskID;
        this.password = password;

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("password", password)
                .appendQueryParameter("task_id", taskID);
        query = builder.build().getEncodedQuery();
        Log.w("server_sender_status", "req built");
    }
    boolean isReceived(){
        // Open connection for sending data
        try {
            conn = (HttpURLConnection) new URL(url_response).openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            // Check if successful connection made
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Log.w("server_sender_status", result.toString());
                if (result.toString().equals("true")){
                    return true;
                }
            }else{
                Log.e("server_sender_status", "HTTP error: "+ conn.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server_sender_status", e.toString());
        }finally {
            conn.disconnect();
        }
        return false;
    }
}
