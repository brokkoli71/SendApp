package com.example.send.utils;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.send.ui.SendFragment;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionHandler {
    public static void getPermissions(Context context, Fragment host){
        String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(context, galleryPermissions)) {
            return;
        }
        Log.w("pick_file", "requesting permission");
        EasyPermissions.requestPermissions(host, "Access for storage",
                101, galleryPermissions);
    }
}
