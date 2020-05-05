package com.example.send;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverServer implements  Runnable {
    private ServerSocket serverSocket;
    private Socket mySocket;
    private DataInputStream dis;
    private byte[] byteData;
    private boolean lookingForData = true;


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9700);
            Log.w("receiver", "waiting for client");

            while (lookingForData) {
                mySocket = serverSocket.accept();
                Log.w("receiver", "new socket");
                dis = new DataInputStream(mySocket.getInputStream());

                int dataType = dis.readInt();

                switch (dataType) {
                    case SendingTaskData.TYPE_IMG:
                        final int len = dis.readInt();
                        byteData = new byte[len];
                        if (len > 0) {
                            dis.readFully(byteData);
                            Log.w("receiver", "received data: " + len + " Bytes");

                            //Handle received Data


                        } else Log.e("receiver", "img size is 0");
                        break;
                    default:
                        Log.e("receiver", "unknown data type");
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}