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
    private boolean lookingForData = true;
    private MainActivity mainActivity; //for making Toasts

    public ReceiverServer(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9700);
            Log.w("receiver", "waiting for client");

            while (lookingForData) {
                Socket mySocket = serverSocket.accept();
                Log.w("receiver", "new socket");
                DataInputStream dis = new DataInputStream(mySocket.getInputStream());

                int dataType = dis.readInt();
                String fileName = dis.readUTF();

                int len = dis.readInt();
                byte[] byteData = new byte[len];
                if (len > 0) {
                    dis.readFully(byteData);
                    Log.w("receiver", "received data: " + len + " Bytes");
                    makeToast("received data: " + len + " Bytes");
                } else{
                    makeToast("data size is 0");
                    Log.e("receiver", "data size is 0");
                }
                //handle received data depending on data type in future
                switch (dataType) {
                    case SendingTaskData.TYPE_JPEG:
                    case SendingTaskData.TYPE_JPG:
                    case SendingTaskData.TYPE_PNG:
                        saveData(fileName, byteData);
                        makeToast("Image saved");
                        break;
                    case SendingTaskData.TYPE_MP3:
                        saveData(fileName, byteData);
                        makeToast("Audio saved");
                        break;
                    case SendingTaskData.TYPE_MP4:
                        saveData(fileName, byteData);
                        makeToast("Video saved");
                        break;
                    default:
                        Log.e("receiver", "unknown data type");
                        makeToast("unknown data type");
                        saveData(fileName, byteData);
                        makeToast("saved");
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveData(String fileName, byte[] byteData){
        //create folder if not exists
        String stringFolder = Environment.getExternalStorageDirectory()+"/SendApp";
        File myFolder =new File(stringFolder);
        if (!myFolder.exists()){
            if(!myFolder.mkdir()){
                makeToast("Fehler beim erstellen des Ordners");             }
        }

        //save photo to storage
        File photo = new File(myFolder, fileName);
        int i = 0;
        Log.w("receiver", "filename: "+ fileName);
        while (photo.exists()){
            photo = new File(myFolder, fileName+"_"+i);
            Log.w("receiver", "filename: "+ fileName);
        }
        try {
            FileOutputStream fos=new FileOutputStream(photo.getPath());

            fos.write(byteData);
            fos.close();

            Log.w("receiver", "saved file: "+ photo.getAbsolutePath());

            makeToast("saved file: "+ photo.getAbsolutePath());

        }catch (IOException e) {
            makeToast("could not save file");
            Log.e("receiver", "could not save file", e);
        }
    }
    private void makeToast(final String msg){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}