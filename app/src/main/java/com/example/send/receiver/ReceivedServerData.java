package com.example.send.receiver;

import android.net.Uri;

import java.net.URL;

public class ReceivedServerData {
    URL url;
    String fileName;
    int dataType;

    public ReceivedServerData(String fileName, int dataType, URL url) {
        this.fileName = fileName;
        this.dataType = dataType;
        this.url = url;
    }
}
