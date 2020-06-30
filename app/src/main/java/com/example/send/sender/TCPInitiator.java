package com.example.send.sender;

import android.os.AsyncTask;
import android.util.Log;

import com.example.send.utils.Values;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPInitiator extends AsyncTask<String, Void, String> {
    private Socket s;
    private DataOutputStream dos;
    private String ip, fileName;
    private int dataType;

    public TCPInitiator(String ip){
        this.ip = ip;
    }

    @Override
    protected String doInBackground(String... message) {
        try {
            s = new Socket(ip, 9700);
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(message[0]);
            dos.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ServerSocket serverSocket = new ServerSocket(9700);
            Socket mySocket = serverSocket.accept();

            DataInputStream dis = new DataInputStream(mySocket.getInputStream());
            String response = dis.readUTF();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}