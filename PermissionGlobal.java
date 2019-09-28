package com.solvian.gbt.core.util.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.solvian.gbt.core.util.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PermissionGlobal {

    private PermissionGlobal(){}

    public final static String[] permission = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public final static Integer[] requestCode = {0001,0002,0003,0004,0005};

    public static Boolean needRequestPermission(){
        Boolean needPermission = true;
        if(Build.VERSION.SDK_INT < 22){
            needPermission = false;
        }
        return needPermission;
    }

    public static Boolean hasPermissionRegister(String permission, Context context){
        Boolean hasPermission = false;
        if(ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED){
            hasPermission = true;
        }
        return hasPermission;
    }

    public static Map<String,Boolean> hasGroupPermission(String[] permissions, Context context){
        Map<String,Boolean> resultMap = new HashMap<>();

        for(String permission: permissions){
            if(PermissionGlobal.hasPermissionRegister(permission,context)){
                resultMap.put(permission,true);
            }else resultMap.put(permission,false);
        }

        return resultMap;
    }

    public static void requestPermissionRegister(String[] permissions, Activity activity,Integer requestCode){
       ActivityCompat.requestPermissions(activity,permissions,requestCode);
    }

    public static Integer permissionNeverDenied(String[] permissions,Activity activity){
        Integer denied = 0;
        for(String permission : permissions){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)){
                denied++;
            }
        }
        return denied;
    }

}
