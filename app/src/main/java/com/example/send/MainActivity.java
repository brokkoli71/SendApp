package com.example.send;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        e1 = findViewById(R.id.editText);
        e2 = findViewById(R.id.editText2);
        Button b = findViewById(R.id.button);
        Button b2 = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);




        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
        });

        TextView textView = findViewById(R.id.textView);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        textView.setText("Meine IP ist "+ip);

        Thread myThread = new Thread(new MyServer());
        myThread.start();
    }

    @Override //https://medium.com/@hasangi/capture-image-or-choose-from-gallery-photos-implementation-for-android-a5ca59bc6883
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
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

    class MyServer implements  Runnable{
        ServerSocket serverSocket;
        Socket mySocket;
        DataInputStream dis;
        String message;
        boolean lookingForData = true;
        Handler handler = new Handler();
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(9700);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "waiting for client", Toast.LENGTH_SHORT);
                    }
                });
                while (lookingForData){
                    mySocket = serverSocket.accept();
                    dis = new DataInputStream(mySocket.getInputStream());
                    message = dis.readUTF();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void send(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute(e1.getText().toString(), e2.getText().toString());
    }

    class BackgroundTask extends AsyncTask<String, Void, String>{
        Socket s;
        DataOutputStream dos;
        String ip, message;

        @Override
        protected String doInBackground(String... params) {
            ip = params[0];
            message = params[1];

            try {
                s = new Socket(ip, 9700);
                dos = new DataOutputStream(s.getOutputStream());
                dos.writeUTF(message);

                dos.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
