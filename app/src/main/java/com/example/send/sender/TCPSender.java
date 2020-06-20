package com.example.send.sender;

import android.os.AsyncTask;

import com.example.send.SendingTaskData;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender extends AsyncTask<SendingTaskData, Void, String> {
    private Socket s;
    private DataOutputStream dos;
    private String ip, fileName;
    private int dataType;
    private byte[] byteData;

    TCPSender(String ip){
        this.ip = ip;
    }
    @Override
    protected String doInBackground(SendingTaskData... sendingTaskData) {
        dataType = sendingTaskData[0].getDataType();
        byteData = sendingTaskData[0].getByteData();
        fileName = sendingTaskData[0].getFileName();

        try {
            s = new Socket(ip, 9700);
            dos = new DataOutputStream(s.getOutputStream());

            dos.writeInt(dataType);
            dos.writeUTF(fileName);
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
