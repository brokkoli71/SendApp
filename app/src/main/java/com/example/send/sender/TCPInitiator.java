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
    private static final int CONNECTION_TIMEOUT = 10000;

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
            s = new Socket(ip, Values.SOCKET_PORT_REQ); //todo issue #11 java.net.ConnectException: failed to connect to /192.168.1.11 (port 9700) from /:: (port 38986): connect failed: ECONNREFUSED (Connection refused)
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(message[0]);
            dos.close();
            s.close();
            Log.w("tcp_init", "req send");
        } catch (IOException e) {
            e.printStackTrace();
            return "exception";
        }

        try (ServerSocket serverSocket = new ServerSocket(Values.SOCKET_PORT_RESPONSE)) {
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);
            final Socket mySocket = serverSocket.accept();

            DataInputStream dis = new DataInputStream(mySocket.getInputStream());
            String response = dis.readUTF();
            dis.close();
            Log.w("tcp_init", "got response: " + response);
            return response;

        } catch (SocketTimeoutException e) {
            Toaster.makeToast("konnte keine TCP Verbindung aufbauen");
            Log.e("tcp_init", "connection timeout", e);
            return "connection timeout";

        } catch (IOException e) {
            e.printStackTrace();
            return "exception";
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if (response.equals(Values.TCP_CONNECTION_AVAILABLE)){
            tcpSend(ip);
            return;
        }
        Log.e("tcp_init", "could not connect: \""+response+"\"");
        serverSend(serverCommunicationKey);
    }

    void tcpSend(String IP){
        if (sendingTaskData==null)
            return;
        TCPSender tcpSender = new TCPSender(IP);
        tcpSender.execute(sendingTaskData);
        Toaster.makeToast("Sende "+sendingTaskData.getBytes()+" Bytes direkt");
    }

    void serverSend(String key){
        if (sendingTaskData==null)
            return;
        ServerSender serverSender = new ServerSender(context, key);
        serverSender.execute(sendingTaskData);
        Toast.makeText(context, "sending "+sendingTaskData.getBytes()+" Bytes to Server", Toast.LENGTH_SHORT).show();
    }
}