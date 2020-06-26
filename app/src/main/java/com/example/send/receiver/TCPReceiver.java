package com.example.send.receiver;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.example.send.ui.MainActivity;
import com.example.send.utils.Toaster;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class TCPReceiver implements  Runnable {
    private Context context;
    ImageView targetView;
    int availableSpace;

    public TCPReceiver(Context context, ImageView targetView, int availableSpace){
        this.context = context;
        this.targetView = targetView;
        this.availableSpace = availableSpace;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9700);
            Log.w("receiver", "waiting for client");

            Socket mySocket = serverSocket.accept();
            Log.w("receiver", "new socket");

            onReceiving();

            DataInputStream dis = new DataInputStream(mySocket.getInputStream());

            final int dataType = dis.readInt();
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

            final File saveToFile = ReceivedDataHandler.getAvailableFile(fileName);

            try {
                FileOutputStream fos=new FileOutputStream(saveToFile.getPath());

                fos.write(byteData);
                fos.close();

                Log.w("receiver", "saved file: "+ saveToFile.getAbsolutePath());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ReceivedDataHandler.handleType(dataType, saveToFile, targetView, availableSpace, context);
                    }
                });

            }catch (IOException e) {
                Toaster.makeToast("fehler beim speichern (Order konnte evtl nicht erstellt werden)", true);
                Log.e("receiver", "could not save file", e);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public abstract void runOnUiThread(Runnable runnable);

    public abstract void onReceiving();

}