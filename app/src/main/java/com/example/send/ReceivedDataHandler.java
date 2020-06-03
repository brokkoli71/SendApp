package com.example.send;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceivedDataHandler {

    private static void setImageDrawable(final int path, final MainActivity mainActivity){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.imageView.setImageDrawable(mainActivity.getResources().getDrawable(path));
            }
        });
    }

    static File getAvailableFile(String fileName, MainActivity mainActivity){
        //create folder if not exists
        String stringFolder = Environment.getExternalStorageDirectory()+"/SendApp";
        File myFolder =new File(stringFolder);
        if (!myFolder.exists()){
            if(!myFolder.mkdir()){
                Toaster.makeToast("Fehler beim erstellen des Ordners");
            }
        }

        //save data to storage
        File file = new File(myFolder, fileName);
        int i = 0;
        Log.w("receiver", "filename: "+ fileName);
        if (file.exists()){
            String[] splitFileName = fileName.split("(\\.)(?!.*\\1)");
            while (file.exists()){
                String newFileName = splitFileName[0]+"_"+i+"."+splitFileName[1];
                file = new File(myFolder, newFileName);
                Log.w("receiver", "filename: "+ newFileName);
                i++;
            }
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
                Log.e("receiver", "unknown data type");
                Toaster.makeToast("unknown data type");

                Toaster.makeToast("saved: "+path);
                setImageDrawable(Values.DEFAULT_IMAGE, mainActivity);
        }
    }
}
