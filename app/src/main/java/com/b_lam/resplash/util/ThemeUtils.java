package com.b_lam.resplash.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.util.Log;
import android.util.TypedValue;

import com.b_lam.resplash.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Brandon on 6/13/2017.
 */

public class ThemeUtils {

    private final static String TAG = "ThemeUtils";

    public @StringDef({Theme.LIGHT, Theme.DARK, Theme.BLACK})
    @Retention(RetentionPolicy.SOURCE)
    @interface Theme {
        String LIGHT = "Light";
        String DARK = "Dark";
        String BLACK = "Black";
    }

    public static @Theme String getTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("theme", Theme.LIGHT);
    }

    @ColorInt
    public static int getThemeAttrColor(Context context, @AttrRes int colorAttr) {
        TypedArray array = context.obtainStyledAttributes(null, new int[]{colorAttr});
        try {
            return array.getColor(0, 0);
        } finally {
            array.recycle();
        }
    }

    @DrawableRes
    public static int getThemeAttrDrawable(Context context, @AttrRes int drawableAttr) {
        TypedArray array = context.obtainStyledAttributes(null, new int[]{drawableAttr});
        try {
            return array.getResourceId(0, 0);
        } finally {
            array.recycle();
        }
    }

    public static void setRecentAppsHeaderColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDescription
                    = new ActivityManager.TaskDescription(
                    activity.getString(R.string.app_name),
                    icon, getThemeAttrColor(activity, R.attr.colorPrimary));
            activity.setTaskDescription(taskDescription);
            icon.recycle();
        }
    }
}
