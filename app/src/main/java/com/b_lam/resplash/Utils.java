package com.b_lam.resplash;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import java.util.Random;

/**
 * Created by Brandon on 10/7/2016.
 */

public class Utils {

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getGirdColumnCount(Context context) {
        if (isLandscape(context)) {
            if (isTabletDevice(context)) {
                return 3;
            } else {
                return 2;
            }
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
            String mLayoutType = sharedPreferences.getString("item_layout", "List");
            if(mLayoutType.equals("List") || mLayoutType.equals("Cards")){
                return 1;
            }else{
                return 2;
            }
        }
    }

    public static boolean isLandscape(Context context) {
        return context.getResources()
                .getConfiguration()
                .orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }

    public static String getDefaultSharedPreferencesName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return PreferenceManager.getDefaultSharedPreferencesName(context);
        } else {
            return context.getPackageName() + "_preferences";
        }
    }

    private static void setUserGroup(int userGroup) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Resplash.RESPLASH_USER_GROUP, userGroup);
        editor.apply();
    }

    public static int getUserGroup() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        int userGroup = sharedPreferences.getInt(Resplash.RESPLASH_USER_GROUP, 0);
        if (userGroup == 0) {
            userGroup = new Random().nextInt(3) + 1;
            setUserGroup(userGroup);
        }

        return userGroup;
    }
}
