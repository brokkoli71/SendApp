package com.example.send;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender extends AsyncTask<String, Void, Void> {

    Socket s;
    DataOutputStream dos;
    PrintWriter pw;

    @Override
    protected Void doInBackground(String... voids) {

        String message = voids[0];
        try {
            s = new Socket("2003:d6:4701:4500:1d:446a:99bb:59b6", 7800);
            //s = new Socket("192.168.232.2", 7800);
            pw = new PrintWriter(s.getOutputStream());
            pw.write(message);
            Log.w("test", message);
            pw.flush();
            pw.close();
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
