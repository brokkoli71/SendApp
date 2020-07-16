package com.example.send.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;


public class ExceptionDialog {

    public ExceptionDialog(final Context context, String message, boolean messageScreenshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = "Ups!";
        if (messageScreenshot)
            title+=" Screenshot bitte mir schicken";
        builder.setTitle(title);

        final TextView textView = new TextView(context);

        textView.setText(message);
        textView.setTextSize(10);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setPadding(10, 0, 10, 0);

        final ScrollView scrollView = new ScrollView(context);
        scrollView.setScrollbarFadingEnabled(false);
        scrollView.addView(textView);
        builder.setView(scrollView);

        builder.setPositiveButton("OK", null);

        builder.show();
    }

    public ExceptionDialog(final Context context, Exception e, boolean messageScreenshot) {
        this(context, Log.getStackTraceString(e).replace(' ', '\u00A0'), messageScreenshot);
    }

    public ExceptionDialog(final Context context, Exception e){
        this(context, e, true);
    }
}
