package com.example.send.sender;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.example.send.R;
import com.example.send.utils.Toaster;
import com.example.send.ui.MainActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


public class ServerSender extends AsyncTask<SendingTaskData, Integer, String> {

    final int CHECK_STATUS_TIMEOUT = 1000;

    private final String url_send;
    private final String url_response;
    private MainActivity mainActivity;
    private ProgressDialog pDialog, pDialog2;


    String ip, fileName;
    int dataType;
    byte[] byteData;
    String password;
    String receiver;

    public ServerSender(MainActivity context, String receiver){
            this.url_send = context.getString(R.string.server_url_in);
            this.url_response = context.getString(R.string.server_url_response);
            this.mainActivity = context;
            this.password = context.getString(R.string.pwd);
            this.receiver = receiver;
        }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(mainActivity);
        pDialog.setMessage("Hochladen...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(1);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false); //todo: add functionality on canceling -> cancel sending
        pDialog.show();
    }
    @Override
    protected String doInBackground(SendingTaskData... sendingTaskData) {
        dataType = sendingTaskData[0].dataType;
        byteData = sendingTaskData[0].byteData;
        fileName = sendingTaskData[0].fileName;
        HttpClient client;

        //depending on Android Version
        try {
            client = HttpClientBuilder.create().build();
        }catch (NoSuchFieldError e){
            client = new DefaultHttpClient();
        }
        try {
            HttpPost post = new HttpPost(url_send);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addTextBody("password", password);

            entityBuilder.addTextBody("data_type", Integer.toString(dataType));
            entityBuilder.addTextBody("file_name", fileName);
            entityBuilder.addTextBody("byte_size", Integer.toString(byteData.length));
            entityBuilder.addTextBody("receiver", receiver);


            if(byteData != null){
                entityBuilder.addBinaryBody("data", byteData, ContentType.create("text/plain"), "filename");
            }

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            String taskID = EntityUtils.toString(httpEntity);

            publishProgress( 1);
            Log.w("server_sender", "taskID = "+taskID);

            ServerSenderStatus serverSenderStatus = new ServerSenderStatus(url_response, taskID, password);

            while (!serverSenderStatus.isReceived()){
                Thread.sleep(CHECK_STATUS_TIMEOUT);//TODO: work from here, use handler instead?
            }

            Toaster.makeToast("datei wurde empfangen");
            Log.w("server_sender", "success");

            return taskID;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    protected void onProgressUpdate(Integer... progress) {
        pDialog.setProgress(progress[0]);
        //todo: no pdialog -> other feedback instead
        if (progress[0] == pDialog.getMax()){
            pDialog.dismiss();
            pDialog2 = new ProgressDialog(mainActivity);
            pDialog2.setMessage("warte auf empfaenger...");
            pDialog2.setIndeterminate(false);
            pDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog2.setCancelable(false);
            pDialog2.show();
        }
    }
    @Override
    protected void onPostExecute(String message) {
        super.onPostExecute(message);
        try {
            pDialog2.dismiss();
        }catch (NullPointerException e){
            pDialog.dismiss();
        }
    }
}


