package com.example.send.receiver;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.example.send.R;
import com.example.send.sender.SendingTaskData;
import com.example.send.utils.ImageHelper;
import com.example.send.utils.PermissionHandler;
import com.example.send.utils.Toaster;
import com.example.send.utils.Values;
import com.example.send.ui.MainActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ReceivedDataHandler {

    public static Bitmap readPictureFromFileUri(Uri uri, ContentResolver contentResolver, int maxHeight, int maxWidth){
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (Exception e){
            Log.e("set_img", "could not decode uri", e);
        }finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bitmap = ImageHelper.fitSizeBitmap(bitmap, maxHeight, maxWidth);
        try {
            bitmap = ImageHelper.modifyOrientation(bitmap, ImageHelper.getRealPathFromUri(uri, contentResolver));
            Log.w("set_img", "orientation changed");
        }catch (IOException e){
            Log.e("set_img", "orientation changing failed",e);
        }
        return bitmap;
    }

    public static File getAvailableFile(String fileName){

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

    public static void handleType(int dataType, File saveToFile, ImageView targetView, int availableSpace, final Context context){
        //handle received data depending on data type in future
        String path = saveToFile.getAbsolutePath();
        switch (dataType) {

            case SendingTaskData.TYPE_JPEG:
            case SendingTaskData.TYPE_JPG:
            case SendingTaskData.TYPE_PNG:
                Toaster.makeToast("Image saved: "+path);
                final Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", saveToFile);

                int maxWidth =  context.getResources().getDimensionPixelSize(R.dimen.inner_content_width);
                Bitmap bitmap = readPictureFromFileUri(uri, context.getContentResolver(), availableSpace, maxWidth);

                //todo issue #7: images wrong resolution
                //todo issue #8: images wrong orientation (rotation)
                ImageHelper.setPictureInImageView(bitmap, targetView, context.getResources());

                break;

            case SendingTaskData.TYPE_MP3:
                Toaster.makeToast("Audio saved: "+path);
                targetView.setImageDrawable(context.getResources().getDrawable(Values.AUDIO_IMAGE));
                break;

            case SendingTaskData.TYPE_MP4:
                Toaster.makeToast("Video saved: "+path);
                targetView.setImageDrawable(context.getResources().getDrawable(Values.VIDEO_IMAGE));
                break;

            default:
                Log.e("file_saver", "unknown data type");
                Toaster.makeToast("unknown data type");

                Toaster.makeToast("saved: "+path);
                targetView.setImageDrawable(context.getResources().getDrawable(Values.DEFAULT_IMAGE));
        }
    }
}
