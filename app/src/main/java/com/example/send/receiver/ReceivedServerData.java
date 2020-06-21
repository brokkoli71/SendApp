package com.example.send.receiver;

import android.net.Uri;

import java.io.File;
import java.net.URL;

public class ReceivedServerData {
    URL url;
    String fileName;
    int dataType;
    File saveToFile;

    public ReceivedServerData(String fileName, int dataType, URL url) {
        this.fileName = fileName;
        this.dataType = dataType;
        this.url = url;
    }

    public File getSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(File saveToFile) {
        this.saveToFile = saveToFile;
    }
}
