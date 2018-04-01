package com.b_lam.resplash.util;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.tools.AuthManager;

/**
 * Created by Brandon on 2/4/2018.
 */

public class ResplashBackupAgent extends BackupAgentHelper {

    static final String DEFAULT_PREFS = Utils.getDefaultSharedPreferencesName(Resplash.getInstance());
    static final String AUTH_MANAGER_PREFS = AuthManager.PREFERENCE_NAME;
    static final String USER_GROUP_PREFS = Resplash.RESPLASH_USER_GROUP;

    static final String PREFS_BACKUP_KEY = "resplash_prefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, DEFAULT_PREFS, AUTH_MANAGER_PREFS, USER_GROUP_PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
