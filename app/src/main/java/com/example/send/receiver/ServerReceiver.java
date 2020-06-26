package com.example.send.receiver;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.example.send.R;
import com.example.send.utils.Toaster;
import com.example.send.ui.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerReceiver extends AsyncTask<String, Void, String> {
    private static final int CONNECTION_TIMEOUT=10000;
    private static final int READ_TIMEOUT=15000;
    final int CHECK_RESULT_TIMEOUT = 500;

    private final String server_url;
    private final String server_pwd;
    private final Context context;
    private final int availableSpace;
    private final ImageView targetView;
    private final String id;

    public ServerReceiver(Context context, ImageView targetView, int availableSpace, String id){
        this.context = context;
        this.server_url = context.getString(R.string.server_url_out);
        this.server_pwd = context.getString(R.string.pwd);
        this.targetView = targetView;
        this.availableSpace = availableSpace;
        this.id = id;
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

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("password", server_pwd)
                    .appendQueryParameter("receiver", id);
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

                return result.toString();
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

    @Override
    protected void onPostExecute(String message) {
        super.onPostExecute(message);
        Log.w("server_receiver", "message: "+message);

        if (message.equals("exception")||
                message.equals("unsuccessful")||
                message.equals("connectivity error")){
            Log.e("server_receiver", "got "+message);
        }else if (message.equals("no result")){
            Log.w("server_receiver", "no file sent (yet)");
            if(!isCancelled()){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new ServerReceiver(context, targetView, availableSpace, id).execute();
                    }
                }, CHECK_RESULT_TIMEOUT);


            }
        }else{
            try {
                String[] messageSplit = message.split("\\?");
                String filename = messageSplit[0];
                int dataType = Integer.parseInt(messageSplit[1]);
                int taskID = Integer.parseInt(messageSplit[2]);

                URL url = new URL(context.getString(R.string.server_url_files)+filename);
                DownloadFileFromURL downloader = new DownloadFileFromURL(context, taskID, targetView, availableSpace);
                downloader.execute(new ReceivedServerData(filename, dataType, url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

}

