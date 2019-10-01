package com.solvian.gbt.main.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;

import android.view.KeyEvent;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.solvian.gbt.BuildConfig;
import com.solvian.gbt.R;
import com.solvian.gbt.core.activity.StrictSystemConfigActivity;
import com.solvian.gbt.core.manager.SessionManager;
import com.solvian.gbt.core.model.Login;
import com.solvian.gbt.core.service.ServicesManager;
import com.solvian.gbt.core.task.LoginTask;
import com.solvian.gbt.core.task.LoginTask.loginListener;
import com.solvian.gbt.core.task.UpdateAppTask;
import com.solvian.gbt.core.util.DialogHelper;
import com.solvian.gbt.core.util.permissions.PermissionControl;
import com.solvian.gbt.core.util.permissions.PermissionGlobal;
import com.solvian.gbt.core.util.permissions.PermissionUtils;
import com.solvian.gbt.core.util.UiUtils;
import com.solvian.gbt.databinding.LoginBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class LoginActivity extends StrictSystemConfigActivity {

    private LoginBinding binding;
    private AsyncTask loginTask;
    private LoginTask.loginListener loginTaskListener;
    private PermissionControl permission;

    @Inject
    ServicesManager servicesManager;
    @Inject
    SessionManager sessionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        super.ignoreSessionValidation(false);

        binding = DataBindingUtil.setContentView(this, R.layout.login);
        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loginTaskListener = new LoginTaskListener();
        permission = new PermissionUtils();

        if (sessionManager.isSessionActive()) {
            beginSession();
        } else if (loginTask != null && loginTask.getStatus() == AsyncTask.Status.RUNNING) {
            makeProgressDialog("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loginTask != null) {
            loginTask.cancel(false);
        }
        loginTaskListener = null;
    }

    /* Presenter methods */
    private boolean validateFields() {
        return binding.txtUser.length() > 0 && binding.txtPassword.length() > 0;
    }

    private void doLogin() {

        UiUtils.hideSoftInput(binding.getRoot());

        String username = binding.txtUser.getText().toString();
        String password = binding.txtPassword.getText().toString();
        loginTask = new LoginTask(this.getApplicationContext(), loginTaskListener)
                .execute(username, password);
    }

    public void updateApp(String apkUrl) {
        new UpdateAppTask(LoginActivity.this, apkUrl).execute();
    }

    private void processLogin(Login login) {
        dismissProgressDialog();

        if (login == null) {
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.login_error_remote_issue), getString(R.string.login_action_ok), Snackbar.LENGTH_LONG, null);
            return;
        }

        switch (login.getCode()) {
            case Login.SUCCESS:
                beginSession();
                return;

            case Login.NO_NETWORK:
            case Login.NO_CONNECTION:
                showNetworkErrorDialog(getString(R.string.login_error_no_connection));
                return;

            case Login.NEWER_APP_VERSION_AVAILABLE:
                showUpdateAppDialog(login);
                return;


            case Login.CLIENT_ERROR:
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.login_error_client), getString(R.string.login_action_ok), Snackbar.LENGTH_LONG, null);
                return;

            case Login.PARAMETER_ERROR:
            case Login.INVALID_LOGIN_PASSWORD:
            case Login.USER_ALREADY_IN_USE:
            case Login.USER_IS_INATIVE:
            case Login.UNAUTHORIZED:
            case Login.UNKNOWN_ERROR:
                UiUtils.showSnackbar(binding.getRoot(), login.getMessage(), getString(R.string.login_action_ok), Snackbar.LENGTH_LONG, null);
                return;

            default:
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.login_error_unknown), getString(R.string.login_action_ok), Snackbar.LENGTH_LONG, null);
                break;
        }
    }

    public void beginSession() {
        servicesManager.startServices();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /* View methods */
    private void setupViews() {
        String appVersion = String.format(getString(R.string.login_app_version), BuildConfig.VERSION_NAME, BuildConfig.VERSION_NUMBER);

        binding.appVersion.setText(appVersion);

        binding.txtPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    doLogin();
                    return true;
                }
                return false;
            }
        });

        binding.btnLoginAjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, AdjustmentsActivity.class));
            }
        });

        binding.btnLoginOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(PermissionGlobal.permissionNeverDenied(PermissionGlobal.permission,LoginActivity.this) > 0) {
                    PermissionGlobal.requestPermissionRegister(PermissionGlobal.permission,LoginActivity.this,PermissionGlobal.requestCode[0]);
                    return;
                }

                if (!validateFields()) {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.login_error_form_validation), getString(R.string.login_action_ok), Snackbar.LENGTH_SHORT, null);
                    return;
                }

                if(PermissionGlobal.needRequestPermission()){
                    permission.verifyPermission(LoginActivity.this);
                }

                doLogin();
            }
        });
    }

    private void makeProgressDialog(String message) {
        if (message != null) {
            binding.progressOverlay.progressText.setText(message);
        }

        binding.progressOverlay.getRoot().setVisibility(View.VISIBLE);
    }

    private void dismissProgressDialog() {
        binding.progressOverlay.getRoot().setVisibility(View.GONE);
    }

    private void showUpdateAppDialog(final Login l) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.app_name));
        alertDialog.setNeutralButton(R.string.label_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateApp(l.getApkUrl());
                        dialog.dismiss();
                    }
                });
        alertDialog.setMessage(l.getMessage());
        alertDialog.create();
        alertDialog.show();
    }

    private void showNetworkErrorDialog(String message) {
        final DialogInterface.OnClickListener openWirelessSettingsAction = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        };

        final DialogInterface.OnClickListener openWifiSettingsAction = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        };

        DialogHelper.showDialog(this, message,
                getString(R.string.label_ignore),
                getString(R.string.label_open_network_settings),
                getString(R.string.label_open_wifi_settings),
                null,
                openWirelessSettingsAction,
                openWifiSettingsAction);
    }

    /* Listener interface implementations */
    private class LoginTaskListener implements loginListener {

        public LoginTaskListener() {
        }

        @Override
        public void onPreExecuteLogin() {
            makeProgressDialog("");
        }

        @Override
        public void onPostExecuteLogin(final Login login) {
            processLogin(login);
        }

        @Override
        public void onCancelLogin() {
            dismissProgressDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0001:
                    UiUtils.showSnackbar(binding.getRoot(), "You to denied the smartfield, to never using permissions, open config to activat now", "Activet",
                            Snackbar.LENGTH_INDEFINITE, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.fromParts("package", getPackageName(), null)));
                                }
                            });
                break;
        }
    }
}
