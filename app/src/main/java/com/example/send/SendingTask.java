package com.example.send;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SendingTask extends AsyncTask<SendingTaskData, Void, String> {
    Socket s;
    DataOutputStream dos;
    String ip;
    int dataType;
    byte[] byteData;

    @Override
    protected String doInBackground(SendingTaskData... sendingTaskData) {
        ip = sendingTaskData[0].IP;
        dataType = sendingTaskData[0].dataType;
        byteData = sendingTaskData[0].byteData;

        try {
            s = new Socket(ip, 9700);
            dos = new DataOutputStream(s.getOutputStream());

            dos.writeInt(dataType);
            dos.writeInt(byteData.length);

            dos.write(byteData);
            dos.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
