package com.solvian.gbt.core.util.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.karumi.dexter.DexterBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PermissionUtils implements  PermissionControl{

    public PermissionUtils(){}

    @Override
    public void verifyPermission(Activity activity) {
        Map<String,Boolean> permissions = PermissionGlobal.hasGroupPermission(PermissionGlobal.permission,activity);
        List<String> _permissions = new ArrayList<>();

        for(Map.Entry<String,Boolean> map: permissions.entrySet()){
            if(!map.getValue())_permissions.add(map.getKey());
        }

        if(_permissions.size() > 0)
            PermissionGlobal.requestPermissionRegister((_permissions.toArray(new String[_permissions.size()])),activity,PermissionGlobal.requestCode[1]);
    }

}
