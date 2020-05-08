package com.example.send;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PHOTO = 1;
    private static final int TAKE_PHOTO = 0;

    private Uri selectedImageUri = null;
    private ReceiverServer receiverServer;
    int cacheSize;

    EditText e1, e2;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        // Use 1/8th of the available memory for this memory cache.
        cacheSize = maxMemory / 8;
        Log.w("onCreate", "cashSize = "+cacheSize+" Bytes");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Bald...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        e2 = findViewById(R.id.editText2);
        Button buttonSend = findViewById(R.id.button);
        Button buttonPickPhoto = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedImageUri!=null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        byte[] bytesArray = new byte[inputStream.available()];
                        inputStream.read(bytesArray);

                        SendingTask sendingTask = new SendingTask();
                        String ip = e2.getText().toString();
                        //for easier dev-testing
                        if (!ip.contains("."))
                            ip = "192.168.0."+ip;

                        Log.w("send", "sending " + bytesArray.length + " Bytes");

                        Log.w("send", "path: "+selectedImageUri.getPath());

                        String fileName;
                        try{
                            //get part of path after last "/"
                            fileName = selectedImageUri.getPath().split("(/)(?!.*\\1)")[1];
                        }catch (ArrayIndexOutOfBoundsException e){
                            fileName = selectedImageUri.getPath();
                        }

                        String dataTypeStr;
                        try {
                            dataTypeStr = fileName.split("(\\.)(?!.*\\1)")[1];
                        }catch (ArrayIndexOutOfBoundsException e){
                            //if format not "file://" but i.e. "content://" then might not be with data format
                            String newPath = getRealPathFromUri(selectedImageUri);
                            Log.w("newPath", newPath);
                            fileName = newPath.split("(/)(?!.*\\1)")[1];
                                try {
                                    dataTypeStr = fileName.split("(\\.)(?!.*\\1)")[1];
                                }catch (ArrayIndexOutOfBoundsException e2){
                                    dataTypeStr = "";
                            }
                        }

                        Log.w("send", "fileName: " +fileName);
                        Log.w("send", "dataType: "+dataTypeStr);
                        int dataType;
                        switch (dataTypeStr){
                            case ("jpg"): dataType = SendingTaskData.TYPE_JPG; break;
                            case ("png"): dataType = SendingTaskData.TYPE_PNG; break;
                            case ("mp3"): dataType = SendingTaskData.TYPE_MP3; break;
                            case ("mp4"): dataType = SendingTaskData.TYPE_MP4; break;
                            case ("jpeg"): dataType = SendingTaskData.TYPE_JPEG; break;
                            default: dataType = SendingTaskData.TYPE_UNKNOWN; break;
                        }

                        sendingTask.execute(new SendingTaskData(dataType, bytesArray, ip, fileName));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                /* old way
                Drawable drawable = imageView.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapData = stream.toByteArray();*/

            }
        });

        buttonPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissions();

                Log.w("pick_photo", "request send:"+ PICK_PHOTO);
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , PICK_PHOTO);
            }
        });

        TextView textView = findViewById(R.id.textView);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        textView.setText(String.format("%s%s", getString(R.string.showIPTextView), ip));

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        // send multiple Data might get implemented later

        if(Intent.ACTION_SEND.equals(action)){
            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri!=null){
                selectedImageUri = uri;
                Log.w("receive_from_app", "received \""+uri+"\"");
                if (type.startsWith("image/"))
                    setPictureInImageView();
            }else{
                Log.e("receive_from_app", "uri not readable");
            }
        }

        receiverServer = new ReceiverServer(this);
        Thread myThread = new Thread(receiverServer);
        myThread.start();
    }

    private void getPermissions(){
        String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(MainActivity.this, galleryPermissions)) {
            return;
        }
        Log.w("pick_photo", "requesting permission");
        EasyPermissions.requestPermissions(MainActivity.this, "Access for storage",
                101, galleryPermissions);

    }
    private void setPictureInImageView(){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        if (selectedImageUri != null) {
            Cursor cursor = getContentResolver().query(selectedImageUri,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                Log.w("display_photo", "displaying "+picturePath);
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

                //before drawing image in imageView the bitmap size will be checked
                if (bitmap.getByteCount()<cacheSize)//evtl frueher checken
                    imageView.setImageBitmap(bitmap);
                else{
                    Toast.makeText(this, "to large to display but can be send",
                            Toast.LENGTH_LONG).show();
                    //handle...
                }
                cursor.close();
            }
        }
    }

    //reference: https://medium.com/@hasangi/capture-image-or-choose-from-gallery-photos-implementation-for-android-a5ca59bc6883
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("pick_photo", "request handling:"+", "
                +(resultCode==RESULT_CANCELED?"canceled":(resultCode==RESULT_OK?"result ok":resultCode)));
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    //not implemented yet
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImageBitmap = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImageBitmap);
                    }
                    break;
                case PICK_PHOTO:
                    if (resultCode == RESULT_OK && data != null) {
                        this.selectedImageUri = data.getData();
                        setPictureInImageView();
                    }
                    break;
            }
        }
    }


    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
