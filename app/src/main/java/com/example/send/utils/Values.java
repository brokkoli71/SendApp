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
