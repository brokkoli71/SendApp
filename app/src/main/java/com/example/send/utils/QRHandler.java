package com.example.send.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.example.send.R;

import net.glxn.qrgen.android.QRCode;

public class QRHandler {
    private final String serverCommunicationKey;
    private Resources resources;
    private boolean includedTCP = false;
    private String receiverIP;

    public QRHandler(Resources resources){
        this.resources = resources;
        serverCommunicationKey = Values.getRandomString(8);
    }

    public QRHandler(String toDecode){
        toDecode = toDecode.split(":\\/\\/")[1];
        if (toDecode.contains("?")){
            String[] toDecodeArray = toDecode.split("\\?");
            receiverIP = toDecodeArray[0];
            serverCommunicationKey = toDecodeArray[1];
            includedTCP = true;
            return;
        }
        serverCommunicationKey = toDecode;
    }


    public Bitmap getQRCode(String ip){
        String qrContent = resources.getString(R.string.app_id_key)+"://";
        if (!ip.equals("0.0.0.0")){
            qrContent += (ip+"?");
            includedTCP = true;
        }
        qrContent += serverCommunicationKey;

        return QRCode.from(qrContent).withSize(500, 500).bitmap();
    }

    public boolean isIncludedTCP() {
        return includedTCP;
    }

    public String getServerCommunicationKey() {
        return serverCommunicationKey;
    }

    public String getReceiverIP() {
        return receiverIP;
    }
}
