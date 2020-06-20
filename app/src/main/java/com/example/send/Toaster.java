package com.example.send;

import android.widget.Toast;

public class Toaster {
    static MainActivity mainActivity;

    public static void init(MainActivity theMainActivity){
        mainActivity = theMainActivity;
    }

    public static void makeToast(final String msg){
        makeToast(msg, false);
    }
    public static void makeToast(final String msg, final boolean doLong){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity.getApplicationContext(), msg, doLong?Toast.LENGTH_LONG:Toast.LENGTH_SHORT).show();
            }
        });
    }
}
