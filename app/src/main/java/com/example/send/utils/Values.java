package com.example.send.utils;

import com.example.send.R;

import java.util.Random;

public class Values {
    public static final int AUDIO_IMAGE = R.drawable.ic_music_note;
    public static final int VIDEO_IMAGE = R.drawable.ic_videocam;
    public static final int TEXT_IMAGE = R.drawable.ic_text;
    public static final int DEFAULT_IMAGE = R.drawable.ic_file;

    public static final int PICKFILE_REQUEST_CODE = 2;
    public static final int SCAN_QR_REQUEST_CODE = 10;

    public static final int APP_ID_KEY = R.string.app_id_key;


    //for tcp communication
    public static final int SEND_REQ_KEY = 0;
    //add more later

    public static final String TCP_CONNECTION_AVAILABLE = "rdy";

    public static final int SOCKET_PORT_REQ = 9700;
    public static final int SOCKET_PORT_RESPONSE = 9701;
    public static final int SOCKET_PORT_SEND = 9702;


    public static String getRandomString(int len) {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        char tempChar;
        for (int i = 0; i < len; i++){
            tempChar = (char) (generator.nextInt(26) + 97); //only lowercase
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
