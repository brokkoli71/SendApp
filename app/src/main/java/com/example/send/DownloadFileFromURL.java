package com.example.send;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

public class DownloadFileFromURL extends AsyncTask<ReceivedServerData, String, String> {

    private MainActivity mainActivity;
    private ProgressDialog pDialog;

    public DownloadFileFromURL(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(mainActivity);
        pDialog.setMessage("Downloading file. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    protected String doInBackground(ReceivedServerData... receivedServerData) {
        int count;
        try {
            String fileName = receivedServerData[0].fileName;
            int dataType = receivedServerData[0].dataType;
            URL url = receivedServerData[0].url;

            URLConnection connection = url.openConnection();
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            ReceivedDataHandler.getAvailableFile(fileName, mainActivity);
            // Output stream
            File saveToFile = ReceivedDataHandler.getAvailableFile(fileName, mainActivity);
            OutputStream output = new FileOutputStream(saveToFile);

            byte[] data = new byte[1024];

            long total = 0;
            Log.w("file_saver", "start downloading");

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }
            Log.w("file_saver", "downloaded and saved"+total+" bytes");
            Toaster.makeToast(fileName+" wurde gespeichert ("+total+" Bytes)", true);
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            ReceivedDataHandler.handleType(dataType, saveToFile, mainActivity);

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        pDialog.dismiss();
    }

}