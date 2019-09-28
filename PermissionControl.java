package com.solvian.gbt.core.util.permissions;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface PermissionControl {

    void verifyPermission(Activity activity);

}
