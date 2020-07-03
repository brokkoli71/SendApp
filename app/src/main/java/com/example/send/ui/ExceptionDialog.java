package com.example.send.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class ExceptionDialog {

    public ExceptionDialog(final Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ups!");

        final TextView textView = new TextView(context);

        textView.setText(message);
        builder.setView(textView);

        builder.setPositiveButton("Screenshot gemacht -> an mich schicken", null);

        builder.show();
    }

    public ExceptionDialog(final Context context, Exception e) {
        this(context, Log.getStackTraceString(e).replace(' ', '\u00A0'));
    }
}
