package com.example.send;

import android.widget.Toast;

public class Toaster {
    static MainActivity mainActivity;

    public static void init(MainActivity theMainActivity){
        mainActivity = theMainActivity;
    }

    public static void makeToast(final String msg){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
