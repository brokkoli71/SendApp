package com.example.send;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverServer implements  Runnable {
    private boolean lookingForData = true; //might get activated and deactivated in later versions
    private MainActivity mainActivity; //for making Toasts and setting imageView

    ReceiverServer(MainActivity mainActivity){
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
                String path;
                switch (dataType) {
                    case SendingTaskData.TYPE_JPEG:
                    case SendingTaskData.TYPE_JPG:
                    case SendingTaskData.TYPE_PNG:
                        path = saveData(fileName, byteData);
                        if (path!=null){
                            makeToast("Image saved: "+path);
                        }
                        break;
                    case SendingTaskData.TYPE_MP3:
                        path = saveData(fileName, byteData);
                        if (path!=null){
                            makeToast("Audio saved: "+path);
                            setImageDrawable(Values.AUDIO_IMAGE);
                        }
                        break;
                    case SendingTaskData.TYPE_MP4:
                        path = saveData(fileName, byteData);
                        if (path!=null){
                            makeToast("Video saved: "+path);
                            setImageDrawable(Values.VIDEO_IMAGE);
                        }
                        break;
                    default:
                        Log.e("receiver", "unknown data type");
                        makeToast("unknown data type");
                        path = saveData(fileName, byteData);
                        if (path!=null){
                            makeToast("saved: "+path);
                            setImageDrawable(Values.DEFAULT_IMAGE);
                        }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String saveData(String fileName, byte[] byteData){
        //create folder if not exists
        String stringFolder = Environment.getExternalStorageDirectory()+"/SendApp";
        File myFolder =new File(stringFolder);
        if (!myFolder.exists()){
            if(!myFolder.mkdir()){
                makeToast("Fehler beim erstellen des Ordners");
            }
        }

        //save data to storage
        File photo = new File(myFolder, fileName);
        int i = 0;
        Log.w("receiver", "filename: "+ fileName);
        if (photo.exists()){
            String[] splitFileName = fileName.split("(\\.)(?!.*\\1)");
            while (photo.exists()){
                String newFileName = splitFileName[0]+"_"+i+"."+splitFileName[1];
                photo = new File(myFolder, newFileName);
                Log.w("receiver", "filename: "+ newFileName);
                i++;
            }
        }

        try {
            FileOutputStream fos=new FileOutputStream(photo.getPath());

            fos.write(byteData);
            fos.close();

            Log.w("receiver", "saved file: "+ photo.getAbsolutePath());
            return photo.getAbsolutePath();
        }catch (IOException e) {
            makeToast("could not save file");
            Log.e("receiver", "could not save file", e);
        }
        return null;
    }

    private void setImageDrawable(final int path){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.imageView.setImageDrawable(mainActivity.getResources().getDrawable(path));
            }
        });
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