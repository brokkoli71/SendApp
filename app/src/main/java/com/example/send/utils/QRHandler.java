package com.example.send.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.example.send.R;

import net.glxn.qrgen.android.QRCode;

public class QRHandler {
    final String serverCommunicationKey;
    Resources resources;
    boolean includedTCP = false;

    public QRHandler(Resources resources){
        this.resources = resources;
        serverCommunicationKey = Values.getRandomString(8);
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
}
