package com.example.send;

public class ClientTaskData {
    public byte[] byteData;
    public String IP;
    public int dataType;

    public final static int TYPE_IMG = 0;

    public ClientTaskData(int dataType ,byte[] byteData, String IP) {
        this.byteData = byteData;
        this.IP = IP;
        this.dataType = dataType;
    }
}
