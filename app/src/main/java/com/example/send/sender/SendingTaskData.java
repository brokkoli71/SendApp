package com.example.send.sender;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class SendingTaskData {
    byte[] byteData;
    int dataType;
    String fileName;
    Uri selectedFileUri;
    String mime;

    public Uri getSelectedFileUri() {
        return selectedFileUri;
    }

    public String getMime() {
        return mime;
    }
    public byte[] getByteData() {
        return byteData;
    }

    public int getDataType() {
        return dataType;
    }

    public String getFileName() {
        return fileName;
    }

    //todo add feature #1000: add more in future
    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_JPG = 1;
    public final static int TYPE_PNG = 2;
    public final static int TYPE_MP3 = 3;
    public final static int TYPE_MP4 = 4;
    public final static int TYPE_JPEG = 5;

    public SendingTaskData(Uri selectedFileUri, ContentResolver contentResolver){
        setFile(selectedFileUri, contentResolver);
    }

    public SendingTaskData(int dataType, byte[] byteData, String fileName) {
        this.byteData = byteData;
        this.dataType = dataType;
        this.fileName = fileName;
    }



    private void setFile(Uri fileUri, ContentResolver contentResolver){
        this.mime = contentResolver.getType(fileUri);
        this.selectedFileUri = fileUri;
        try {
            InputStream inputStream = contentResolver.openInputStream(selectedFileUri);

            assert inputStream != null;
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
            switch (dataTypeStr.toLowerCase()){
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

    //can be removed?


    private String getFileName(Uri uri, ContentResolver contentResolver) {
        String displayName = "";
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.w("filename", displayName);
        return displayName;
    }

    public int getBytes(){
        return byteData.length;
    }
}
