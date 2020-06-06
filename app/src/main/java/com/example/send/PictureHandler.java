package com.example.send;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

public class PictureHandler {

    static private Bitmap getBitmapFromUri(Uri uri, ContentResolver contentResolver){
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
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

}
