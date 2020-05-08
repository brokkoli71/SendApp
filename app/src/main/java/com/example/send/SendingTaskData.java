package com.example.send;

public class SendingTaskData {
    public byte[] byteData;
    public String IP;
    public int dataType;
    public String fileName;


    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_JPG = 1;
    public final static int TYPE_PNG = 2;
    public final static int TYPE_MP3 = 3;
    public final static int TYPE_MP4 = 4;
    public final static int TYPE_JPEG = 5;


    public SendingTaskData(int dataType, byte[] byteData, String IP, String fileName) {
        this.byteData = byteData;
        this.IP = IP;
        this.dataType = dataType;
        this.fileName = fileName;
    }
}
