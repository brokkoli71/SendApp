package com.example.send.receiver;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.send.ui.MainActivity;
import com.example.send.utils.Toaster;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<ReceivedServerData, String, String> {

    private Context context;
    private ProgressDialog pDialog;
    private int taskID;

    public DownloadFileFromURL(Context context, int taskID) {
        this.context = context;
        this.taskID = taskID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Downloading file. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
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

            // Output stream
            File saveToFile = ReceivedDataHandler.getAvailableFile(fileName);
            OutputStream output = new FileOutputStream(saveToFile);

            byte[] data = new byte[10240];

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
            Log.w("file_saver", "downloaded and saved "+total+" bytes");
            Log.w("file_saver", "filepath: "+saveToFile.toString());
            Toaster.makeToast(fileName+" wurde gespeichert ("+total+" Bytes)", true);
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            ReceivedDataHandler.handleType(dataType, saveToFile, context);

            return "success";
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return "error";
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
    protected void onPostExecute(String message) {
        // dismiss the dialog after the file was downloaded
        pDialog.dismiss();
        if (message.equals("success")){
            ServerSuccess serverSuccess = new ServerSuccess(context);
            serverSuccess.execute(taskID);

        }
    }

}