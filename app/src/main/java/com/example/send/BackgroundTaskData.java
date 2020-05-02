package com.example.send;

public class BackgroundTaskData {
    public byte[] byteData;
    public String IP;
    public String header;

    public BackgroundTaskData(String header,byte[] byteData, String IP) {
        this.byteData = byteData;
        this.IP = IP;
        this.header = header;
    }
}
