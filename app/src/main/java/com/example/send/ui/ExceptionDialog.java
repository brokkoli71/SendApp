package com.example.send.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

public class ExceptionDialog {

    public ExceptionDialog(final Context context, Exception e){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ups!");

        final TextView textView = new TextView(context);

        textView.setText(e.getMessage());
        builder.setView(textView);

        builder.setPositiveButton("OK", null);

        builder.show();
    }
}
