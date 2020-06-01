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


public class ServerSender extends AsyncTask<byte[], Void, String> {

    private static final String USER_ID = "user";
    private static final String DATA = "data";
    private final String url;
    private final String userId;
    private MainActivity mainActivity;

    ServerSender(String url, String userId, MainActivity context){
            this.url = url;
            this.userId = userId;
            this.mainActivity = context;
        }
    @Override
    protected String doInBackground(byte[]... data) {
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addTextBody(USER_ID, userId);


            if(data != null)
            {
                entityBuilder.addBinaryBody(DATA,  data[0], ContentType.create("text/plain"), "tmp");
            }

            //todo gives always same output on server, maybe "temp_name" needs to be changed, doesn't drop first request file
            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            final String result = EntityUtils.toString(httpEntity);
            Log.v("result", result);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.textView.setText(result);
                }
            });
            return result;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}


