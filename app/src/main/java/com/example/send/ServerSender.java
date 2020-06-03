package com.example.send;

import android.os.AsyncTask;
import android.util.Log;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class ServerSender extends AsyncTask<SendingTaskData, Void, String> {

    private final String url;
    private MainActivity mainActivity;

    String ip, fileName;
    int dataType;
    byte[] byteData;
    String password;
    String receiver;

    //Todo: add user ID to identify receiver
    ServerSender(String url, MainActivity context, String password, String receiver){
            this.url = url;
            this.mainActivity = context;
            this.password = password;
            this.receiver = receiver;
        }
    @Override
    protected String doInBackground(SendingTaskData... sendingTaskData) {
        dataType = sendingTaskData[0].dataType;
        byteData = sendingTaskData[0].byteData;
        fileName = sendingTaskData[0].fileName;
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addTextBody("password", password);

            entityBuilder.addTextBody("data_type", Integer.toString(dataType));
            entityBuilder.addTextBody("file_name", fileName);
            entityBuilder.addTextBody("byte_size", Integer.toString(byteData.length));
            entityBuilder.addTextBody("receiver", receiver);


            if(byteData != null)
            {
                entityBuilder.addBinaryBody("data", byteData, ContentType.create("text/plain"), "filename");
            }

            //todo gives always same output on server, maybe "temp_name" needs to be changed, doesn't drop first request file
            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            final String result = EntityUtils.toString(httpEntity);
            Log.v("result", result);

            return result;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}


