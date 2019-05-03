package com.b_lam.resplash.helpers.customtabs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent

class CustomTabsHelper {

    companion object {
        /**
         * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it in the default browser
         *
         * @param context          The host activity
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available
         * @param uri              the Uri to be opened
         */
        fun openCustomTab(context: Context,
                          customTabsIntent: CustomTabsIntent,
                          uri: Uri) {
            val packageName = CustomTabsPackageHelper.getPackageNameToUse(context)

            //If we cant find a package name, it means there's no browser that supports
            //Chrome Custom Tabs installed. So, we fallback to the web-view
            if (packageName == null) {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    customTabsIntent.intent
                            .putExtra(Intent.EXTRA_REFERRER,
                                    Uri.parse(Intent.URI_ANDROID_APP_SCHEME.toString() + "//" + context.packageName))
                }

                customTabsIntent.intent.setPackage(packageName)
                customTabsIntent.launchUrl(context, uri)
            }
        }
    }
}