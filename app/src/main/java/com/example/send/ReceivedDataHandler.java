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

    private void makeToast(final String msg, final MainActivity mainActivity){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String saveData(String fileName, byte[] byteData, MainActivity mainActivity){
        //create folder if not exists
        String stringFolder = Environment.getExternalStorageDirectory()+"/SendApp";
        File myFolder =new File(stringFolder);
        if (!myFolder.exists()){
            if(!myFolder.mkdir()){
                makeToast("Fehler beim erstellen des Ordners", mainActivity);
            }
        }

        //save data to storage
        File photo = new File(myFolder, fileName);
        int i = 0;
        Log.w("receiver", "filename: "+ fileName);
        if (photo.exists()){
            String[] splitFileName = fileName.split("(\\.)(?!.*\\1)");
            while (photo.exists()){
                String newFileName = splitFileName[0]+"_"+i+"."+splitFileName[1];
                photo = new File(myFolder, newFileName);
                Log.w("receiver", "filename: "+ newFileName);
                i++;
            }
        }

        try {
            FileOutputStream fos=new FileOutputStream(photo.getPath());

            fos.write(byteData);
            fos.close();

            Log.w("receiver", "saved file: "+ photo.getAbsolutePath());
            return photo.getAbsolutePath();
        }catch (IOException e) {
            makeToast("could not save file", mainActivity);
            Log.e("receiver", "could not save file", e);
        }
        return null;
    }
}
