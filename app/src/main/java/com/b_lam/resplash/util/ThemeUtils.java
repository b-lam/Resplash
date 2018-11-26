package com.b_lam.resplash.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

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
            if (icon != null) icon.recycle();
        }
    }
}
