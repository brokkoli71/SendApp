package com.example.send;

import android.widget.Toast;

class Toaster {
    static MainActivity mainActivity;

    static void init(MainActivity theMainActivity){
        mainActivity = theMainActivity;
    }

    static void makeToast(final String msg){
        makeToast(msg, false);
    }
    static void makeToast(final String msg, final boolean doLong){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity.getApplicationContext(), msg, doLong?Toast.LENGTH_LONG:Toast.LENGTH_SHORT).show();
            }
        });
    }
}
