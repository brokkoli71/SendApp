package com.example.send;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PHOTO = 1;
    private static final int TAKE_PHOTO = 0;

    EditText e1, e2;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Bald...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        e1 = findViewById(R.id.editText);
        e2 = findViewById(R.id.editText2);
        Button buttonSend = findViewById(R.id.button);
        Button buttonPickPhoto = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable = imageView.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapData = stream.toByteArray();

                ClientTask clientTask = new ClientTask();
                String message = e1.getText().toString();
                String ip = e2.getText().toString();

                Log.w("send", "sending "+bitmapData+ " Bytes");

                clientTask.execute(new ClientTaskData(ClientTaskData.TYPE_IMG, bitmapData, ip));
            }
        });

        buttonPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                if (EasyPermissions.hasPermissions(MainActivity.this, galleryPermissions)) {
                    Log.w("pick_photo", "pick photo request send:"+ PICK_PHOTO);
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , PICK_PHOTO);
                } else {
                    Log.w("pick_photo", "request permission");
                    EasyPermissions.requestPermissions(MainActivity.this, "Access for storage",
                            101, galleryPermissions);
                }

            }
        });

        TextView textView = findViewById(R.id.textView);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        textView.setText("Meine IP ist "+ip);

        Thread myThread = new Thread(new MyServer());
        myThread.start();
    }

    //reference: https://medium.com/@hasangi/capture-image-or-choose-from-gallery-photos-implementation-for-android-a5ca59bc6883
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("pick_photo", "pick photo request handling:"+", "+(resultCode==RESULT_CANCELED?"canceled":(resultCode==RESULT_OK?"result ok":resultCode)));
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    //not implemented yet
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                    }

                    break;
                case PICK_PHOTO:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }
}
