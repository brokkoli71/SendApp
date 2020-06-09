package com.example.send;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPReceiver implements  Runnable {
    private MainActivity mainActivity; //for making Toasts and setting imageView

    TCPReceiver(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9700);
            Log.w("receiver", "waiting for client");

            while (true) {
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
                    Toaster.makeToast("received data: " + len + " Bytes");
                } else{
                    Toaster.makeToast("data size is 0");
                    Log.e("receiver", "data size is 0");
                }

                File saveToFile = ReceivedDataHandler.getAvailableFile(fileName, mainActivity);

                try {
                    FileOutputStream fos=new FileOutputStream(saveToFile.getPath());

                    fos.write(byteData);
                    fos.close();

                    Log.w("receiver", "saved file: "+ saveToFile.getAbsolutePath());

                    ReceivedDataHandler.handleType(dataType,saveToFile, mainActivity);
                }catch (IOException e) {
                    Toaster.makeToast("fehler beim speichern (Order konnte evtl nicht erstellt werden)", true);
                    Log.e("receiver", "could not save file", e);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}