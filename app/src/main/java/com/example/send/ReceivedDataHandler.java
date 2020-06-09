package com.example.send;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReceivedDataHandler {

    private static void setImageDrawable(final int path, final MainActivity mainActivity){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.imageView.setImageDrawable(mainActivity.getResources().getDrawable(path));
            }
        });
    }
    private static void setPictureInImageView(final Uri uri, final MainActivity mainActivity){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setPictureInImageView(readPictureFromFileUri(uri, mainActivity.getContentResolver()));
            }
        });
    }

    static Bitmap readPictureFromFileUri(Uri uri, ContentResolver contentResolver){
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

    static File getAvailableFile(String fileName, MainActivity mainActivity){
        //create folder if not exists
        String stringFolder = Environment.getExternalStorageDirectory()+"/SendApp";
        File myFolder =new File(stringFolder);
        if (!myFolder.exists()){
            if(!myFolder.mkdir()){
                Toaster.makeToast("Fehler beim erstellen des Ordners");
                Log.e("file_saver", "could not add new folder \"SendApp\"");
            }else {
                Log.w("file_saver", "added new folder \"SendApp\"");
            }
        }

        File file = new File(myFolder, fileName);
        int i = 0;
        if (file.exists()){
            String[] splitFileName = fileName.split("(\\.)(?!.*\\1)");
            String newFileName = "";
            while (file.exists()){
                newFileName = splitFileName[0]+"_"+i+"."+splitFileName[1];
                file = new File(myFolder, newFileName);
                i++;
            }
            Log.w("file_saver", "filename taken: "+ fileName);
            Log.w("file_saver", "new filename: "+ newFileName);
        }else{
            Log.w("file_saver", "filename: "+ fileName);
        }
        return file;
    }


//    old fun
//    private static String saveData(String fileName, byte[] byteData, MainActivity mainActivity){
//        //create folder if not exists
//        String stringFolder = Environment.getExternalStorageDirectory()+"/SendApp";
//        File myFolder =new File(stringFolder);
//        if (!myFolder.exists()){
//            if(!myFolder.mkdir()){
//                makeToast("Fehler beim erstellen des Ordners", mainActivity);
//            }
//        }
//
//        //save data to storage
//        File file = new File(myFolder, fileName);
//        int i = 0;
//        Log.w("receiver", "filename: "+ fileName);
//        if (file.exists()){
//            String[] splitFileName = fileName.split("(\\.)(?!.*\\1)");
//            while (file.exists()){
//                String newFileName = splitFileName[0]+"_"+i+"."+splitFileName[1];
//                file = new File(myFolder, newFileName);
//                Log.w("receiver", "filename: "+ newFileName);
//                i++;
//            }
//        }
//
//        try {
//            FileOutputStream fos=new FileOutputStream(file.getPath());
//
//            fos.write(byteData);
//            fos.close();
//
//            Log.w("receiver", "saved file: "+ file.getAbsolutePath());
//            return file.getAbsolutePath();
//        }catch (IOException e) {
//            makeToast("could not save file", mainActivity);
//            Log.e("receiver", "could not save file", e);
//        }
//        return null;
//    }

    static void handleType(int dataType, File saveToFile, MainActivity mainActivity){
        //handle received data depending on data type in future
        String path = saveToFile.getAbsolutePath();
        switch (dataType) {

            case SendingTaskData.TYPE_JPEG:
            case SendingTaskData.TYPE_JPG:
            case SendingTaskData.TYPE_PNG:
                Toaster.makeToast("Image saved: "+path);
                Uri uri = Uri.parse(saveToFile.toString());
                setPictureInImageView(uri, mainActivity);
                //TODO weiter arbeiten: bild in imageview setzen
                break;

            case SendingTaskData.TYPE_MP3:
                Toaster.makeToast("Audio saved: "+path);
                setImageDrawable(Values.AUDIO_IMAGE, mainActivity);
                break;

            case SendingTaskData.TYPE_MP4:
                Toaster.makeToast("Video saved: "+path);
                setImageDrawable(Values.VIDEO_IMAGE, mainActivity);
                break;

            default:
                Log.e("file_saver", "unknown data type");
                Toaster.makeToast("unknown data type");

                Toaster.makeToast("saved: "+path);
                setImageDrawable(Values.DEFAULT_IMAGE, mainActivity);
        }
    }
}
