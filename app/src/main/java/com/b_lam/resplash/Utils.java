package com.b_lam.resplash;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;

import com.b_lam.resplash.R;

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
}
