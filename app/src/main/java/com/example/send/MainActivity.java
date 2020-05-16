package com.example.send;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 0;


    private SendingTaskData sendingTaskData;
    private ReceiverServer receiverServer;
    int cacheSize;

    EditText e1, e2;
    public ImageView imageView;
    Button buttonSend, buttonSelect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        // Use 1/8th of the available memory for this memory cache.
        cacheSize = maxMemory / 8;
        Log.w("onCreate", "cashSize = "+cacheSize+" Bytes");

        e2 = findViewById(R.id.editText2);
        buttonSend = findViewById(R.id.button);
        buttonSelect = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendingTaskData!=null) {

//                        InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
//                        byte[] bytesArray = new byte[inputStream.available()];
//                        inputStream.read(bytesArray);

                        SendingTask sendingTask = new SendingTask();
                        String IP = e2.getText().toString();
                        //for easier dev-testing
                        if (!IP.contains("."))
                            IP = "192.168.0."+IP;

                    sendingTaskData.IP = IP;
                    if (sendingTaskData.isSendable()){
                        sendingTask.execute(sendingTaskData);
                        Toast.makeText(getApplicationContext(), "sending "+sendingTaskData.getBytes()+" Bytes", Toast.LENGTH_SHORT).show();
                    }
                    else Log.e("send", "sendingTaskData not sendable");


                }

                /* old way
                Drawable drawable = imageView.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapData = stream.toByteArray();*/

            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissions();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);

                Log.w("pick_file", "request send:"+ PICKFILE_REQUEST_CODE);
//                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(pickPhoto, PICK_PHOTO);
            }
        });

        TextView textView = findViewById(R.id.textView);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        textView.setText(String.format("%s%s", getString(R.string.showIPTextView), ip));
        //for testing
        textView.setVisibility(View.INVISIBLE);
        e2.setText(ip);

        Intent intent = getIntent();
        String action = intent.getAction();
        final String type = intent.getType();
        // send multiple Data might get implemented later

        if(Intent.ACTION_SEND.equals(action)){
            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri!=null){
                sendingTaskData = new SendingTaskData(uri, getContentResolver());
                Log.w("receive_from_app", "received \""+uri+"\"");
                final View content = findViewById(android.R.id.content);
                content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        setIconInImageView(type);
                    }
                });
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
        Log.w("pick_file", "requesting permission");
        EasyPermissions.requestPermissions(MainActivity.this, "Access for storage",
                101, galleryPermissions);
    }
    private Bitmap readPictureFromSelectedFileUri(){
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = getContentResolver().openInputStream(sendingTaskData.selectedFileUri);
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (Exception e){
            Log.e("set_img", "could not decode uri");
        }
        try {
            assert inputStream != null;
            inputStream.close();
        }catch (Exception ignored){}

        return bitmap;
    }
    public void setPictureInImageView(Bitmap bitmap) {
        //bitmap gets resized to not take to much RAM
        int newWidth =  getResources().getDimensionPixelSize(R.dimen.inner_content_width);
        bitmap = ImageHelper.fitWidthBitmap(bitmap, newWidth);

        //checking if enough space on Screen available
        int[] selectButtonLocation = new int[2];
        int[] imageViewLocation = new int[2];
        buttonSelect.getLocationOnScreen(selectButtonLocation);
        imageView.getLocationOnScreen(imageViewLocation);

        int minWhitespace = getResources().getDimensionPixelSize(R.dimen.min_whitespace);
        int availableSpace = (selectButtonLocation[1] - imageViewLocation[1]) - minWhitespace;
        Log.w("set_img", "availableSpace:" + availableSpace);
        Log.w("set_img", "sendButtonLocation:" + selectButtonLocation[1] + ", imageViewLocation:" + imageViewLocation[1] + ", minWhitespace:" + minWhitespace);

        if (availableSpace >= bitmap.getHeight()) {
            imageView.getLayoutParams().height = bitmap.getHeight();
        } else {
            imageView.getLayoutParams().height = availableSpace;
            bitmap = ImageHelper.fitHeightBitmap(bitmap, availableSpace);
        }
        //remove background otherwise there would be two frames for image
        imageView.setBackground(null);

        //round corners to fit UI style
        bitmap = ImageHelper.getRoundedCornerBitmap(bitmap, getResources().getDimensionPixelSize(R.dimen.round_corners));

        LayerDrawable layerDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.image_view);/*drawable*/
        Drawable newDrawable = new BitmapDrawable(getResources(), bitmap);
        layerDrawable.setDrawableByLayerId(R.id.image_view_drawable, newDrawable);
        imageView.setImageDrawable(layerDrawable);
        Log.w("set_img", "height:" + bitmap.getHeight() + ", width:" + bitmap.getWidth() + ", bytes:" + bitmap.getByteCount());
    }



    //reference: https://medium.com/@hasangi/capture-image-or-choose-from-gallery-photos-implementation-for-android-a5ca59bc6883
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.w("pick_file", "request handling:"+", "
                +(resultCode==RESULT_CANCELED?"canceled":(resultCode==RESULT_OK?"result ok":resultCode)));
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == PICKFILE_REQUEST_CODE) {
                if (resultCode == RESULT_OK && intent != null) {
                    Uri uri = intent.getData();

                    sendingTaskData = new SendingTaskData(uri, getContentResolver());
                    setIconInImageView(sendingTaskData.mime);
                }
            }
        }
    }

    public void setIconInImageView (String type){
        try {
            Log.w("set_img", "img type is "+type);
            if (type.startsWith("image/"))
                setPictureInImageView(readPictureFromSelectedFileUri());
            else{
                imageView.setBackground(getResources().getDrawable(R.drawable.image_view));
                if (type.startsWith("audio/"))
                    imageView.setImageDrawable(getResources().getDrawable(Values.AUDIO_IMAGE));
                else if (type.startsWith("video/"))
                    imageView.setImageDrawable(getResources().getDrawable(Values.VIDEO_IMAGE));
                else if (type.startsWith("text/"))
                    imageView.setImageDrawable(getResources().getDrawable(Values.TEXT_IMAGE));
                else
                    imageView.setImageDrawable(getResources().getDrawable(Values.DEFAULT_IMAGE));
            }
            //if other type..
        }catch (NullPointerException e){
            e.printStackTrace();
            imageView.setImageDrawable(getResources().getDrawable(Values.DEFAULT_IMAGE));
        }

    }




}
