package com.example.send;

import android.net.Uri;

import java.net.URL;

public class ReceivedServerData {
    URL url;
    String fileName;
    //int dataType; todo: add later

    public ReceivedServerData(String fileName, URL url) {
        this.fileName = fileName;
        this.url = url;
    }
}
