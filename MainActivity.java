package com.example.permissionteste;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClassPermissionNepo.CallbackNepo{


    Button camera,escrita, dobro , todos;
    ClassPermissionNepo nepo;
    String[] manifest = {Manifest.permission.READ_EXTERNAL_STORAGE,
            //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.ACCOUNT_MANAGER,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.BATTERY_STATS,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH};

    List<String> amountDanied = new ArrayList<>();
    List<String> amountGrated = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nepo = new ClassPermissionNepo(manifest,this,this,this);

        verificaPermissoes();

    }

    public Boolean verificaPermissoes(){

        for(String permissao : manifest){
            if(ActivityCompat.checkSelfPermission(this,permissao)
                    == PackageManager.PERMISSION_DENIED
            ) amountDanied.add(permissao);
            else amountGrated.add(permissao);
        }

        if(amountDanied.size() > 1){
            ActivityCompat.requestPermissions(this,
                    amountDanied.toArray(new String[amountDanied.size()]),
                    amountDanied.size());
            Toast.makeText(MainActivity.this,"Negados " + amountDanied.size() + "Permitidos " + amountGrated.size(),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    @Override
    public void onResult(String message, Boolean existDanied, List<String> amountDanied, List<String> amountGrated) {
        Toast.makeText(MainActivity.this,"teste",Toast.LENGTH_LONG).show();
    }
}
