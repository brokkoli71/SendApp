package com.example.send;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class SendingTaskData {
    public byte[] byteData;
    public String IP;
    public int dataType;
    public String fileName;
    public Uri selectedFileUri;


    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_JPG = 1;
    public final static int TYPE_PNG = 2;
    public final static int TYPE_MP3 = 3;
    public final static int TYPE_MP4 = 4;
    public final static int TYPE_JPEG = 5;

    SendingTaskData(Uri selectedFileUri, ContentResolver contentResolver){
        this.IP = IP;
        setFile(selectedFileUri, contentResolver);
    }

    public SendingTaskData(int dataType, byte[] byteData, String IP, String fileName) {
        this.byteData = byteData;
        this.IP = IP;
        this.dataType = dataType;
        this.fileName = fileName;
    }



    void setFile(Uri fileUri, ContentResolver contentResolver){

        this.selectedFileUri = fileUri;
        try {
            InputStream inputStream = contentResolver.openInputStream(selectedFileUri);

            byte[] bytesArray = new byte[inputStream.available()];
            inputStream.read(bytesArray);

            String fileName = getFileName(selectedFileUri, contentResolver);

//            try{
//                //get part of path after last "/"
//                fileName = selectedFileUri.getPath().split("(/)(?!.*\\1)")[1];
//            }catch (ArrayIndexOutOfBoundsException e){
//                fileName = selectedFileUri.getPath();
//            }

            String dataTypeStr;
            try {
                dataTypeStr = fileName.split("(\\.)(?!.*\\1)")[1];
            }catch (ArrayIndexOutOfBoundsException e){
                dataTypeStr = "";
                //old:
//                //if format not "file://" but i.e. "content://" then might not be with data format
//                Log.w("newPath","not working: " + selectedFileUri.getPath());
//                getFileName(selectedFileUri, contentResolver);
//                String newPath = getRealPathFromUri(selectedFileUri, contentResolver);
//                Log.w("newPath", newPath);
//                fileName = newPath.split("(/)(?!.*\\1)")[1];
//                try {
//                    dataTypeStr = fileName.split("(\\.)(?!.*\\1)")[1];
//                }catch (ArrayIndexOutOfBoundsException e2){
//                    dataTypeStr = "";
//                }
            }

            Log.w("send", "fileName: " +fileName);
            Log.w("send", "dataType: "+dataTypeStr);
            switch (dataTypeStr){
                case ("jpg"): dataType = SendingTaskData.TYPE_JPG; break;
                case ("png"): dataType = SendingTaskData.TYPE_PNG; break;
                case ("mp3"): dataType = SendingTaskData.TYPE_MP3; break;
                case ("mp4"): dataType = SendingTaskData.TYPE_MP4; break;
                case ("jpeg"): dataType = SendingTaskData.TYPE_JPEG; break;
                default: dataType = SendingTaskData.TYPE_UNKNOWN; break;
            }
            Log.w("send", "sending " + bytesArray.length + " Bytes");

            Log.w("send", "path: "+selectedFileUri.getPath());

            this.byteData = bytesArray;
            this.fileName = fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    String getRealPathFromUri(Uri contentUri, ContentResolver contentResolver) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = contentResolver.query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }catch (NullPointerException e){

        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
    public boolean isSendable(){
        return IP != null && IP.contains(".") && this.byteData != null && fileName != null;
    }
    public String getFileName(Uri uri, ContentResolver contentResolver) {
        String displayName = "";
        Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.w("filename", displayName);
        return displayName;
    }

}
