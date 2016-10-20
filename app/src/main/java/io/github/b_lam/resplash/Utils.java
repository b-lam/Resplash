package io.github.b_lam.resplash;

import android.content.Context;
import android.content.res.TypedArray;

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
}
