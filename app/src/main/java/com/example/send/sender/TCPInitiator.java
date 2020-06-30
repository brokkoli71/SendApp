package com.example.send.sender;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.send.ui.SendFragment;
import com.example.send.utils.QRHandler;
import com.example.send.utils.Toaster;
import com.example.send.utils.Values;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public class TCPInitiator extends AsyncTask<String, Void, String> {
    private static final int CONNECTION_TIMEOUT = 2000;

    private Socket s;
    private DataOutputStream dos;
    private String ip;

    private SendingTaskData sendingTaskData;
    private Context context;
    private String serverCommunicationKey;

    public TCPInitiator(QRHandler qrHandler, SendingTaskData sendingTaskData, Context context) {
        this.ip = qrHandler.getReceiverIP();
        this.sendingTaskData = sendingTaskData;
        this.context = context;
        this.serverCommunicationKey = qrHandler.getServerCommunicationKey();
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
            return "exception";
        }

        try {
            final ServerSocket serverSocket = new ServerSocket(9700);
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);
            final Socket mySocket = serverSocket.accept();

            DataInputStream dis = new DataInputStream(mySocket.getInputStream());
            String response = dis.readUTF();
            dis.close();
            return response;

        }catch (SocketTimeoutException e){
            Toaster.makeToast("konnte keine TCP Verbindung aufbauen");
            Log.e("tcp_init", "connection timeout");
            return "connection timeout";

        }catch (IOException e) {
            e.printStackTrace();
            return "exception";
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if (response.equals("ready")){
            tcpSend(ip);
            return;
        }
        serverSend(serverCommunicationKey);
    }

    void tcpSend(String IP){
        if (sendingTaskData==null)
            return;
        TCPSender tcpSender = new TCPSender(IP);
        tcpSender.execute(sendingTaskData);
        Toaster.makeToast("sending "+sendingTaskData.getBytes()+" Bytes to Server");
    }

    void serverSend(String key){
        if (sendingTaskData==null)
            return;
        ServerSender serverSender = new ServerSender(context, key);
        serverSender.execute(sendingTaskData);
        Toast.makeText(context, "sending "+sendingTaskData.getBytes()+" Bytes to Server", Toast.LENGTH_SHORT).show();
    }
}