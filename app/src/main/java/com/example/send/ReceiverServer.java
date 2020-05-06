package com.example.send;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverServer implements  Runnable {
    private ServerSocket serverSocket;
    private Socket mySocket;
    private DataInputStream dis;
    private byte[] byteData;
    private boolean lookingForData = true;
    MainActivity mainActivity; //for making Toasts

    public ReceiverServer(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
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
                String fileName = dis.readUTF();

                switch (dataType) {
                    case SendingTaskData.TYPE_IMG:
                        final int len = dis.readInt();
                        byteData = new byte[len];
                        if (len > 0) {
                            dis.readFully(byteData);
                            Log.w("receiver", "received data: " + len + " Bytes");

                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mainActivity.getApplicationContext(), "received data: " + len + " Bytes", Toast.LENGTH_LONG).show();
                                }
                            });

                            //Handle received image data
                            File photo = new File(Environment.getExternalStorageDirectory(), fileName);
                            int i = 0;
                            Log.w("receiver", "filename: "+ fileName);
                            while (photo.exists()){
                                photo = new File(Environment.getExternalStorageDirectory(), fileName+"_"+i);
                                Log.w("receiver", "filename: "+ fileName);
                            }
                            try {
                                FileOutputStream fos=new FileOutputStream(photo.getPath());

                                fos.write(byteData);
                                fos.close();

                                Log.w("receiver", "saved file: "+ photo.getAbsolutePath());

                                final String toastMessage = "saved file: "+ photo.getAbsolutePath();
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mainActivity.getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }catch (IOException e) {
                                Log.e("receiver", "could not save file", e);
                            }


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