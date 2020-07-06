package com.b_lam.resplash.util.customtabs

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import com.b_lam.resplash.util.getCustomTabsColorScheme

class CustomTabsHelper {

    companion object {

        fun openCustomTab(
            context: Context,
            uri: Uri,
            theme: String? = null
        ) {
            val customTabsIntent = CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
                .setColorScheme(getCustomTabsColorScheme(theme))
                .build()
            openCustomTab(context, customTabsIntent, uri)
        }

        /**
         * Opens the URL on a Custom Tab if possible.
         * Otherwise falls back to opening it in the default browser
         *
         * @param context          The host activity
         * @param customTabsIntent A CustomTabsIntent to be used if Custom Tabs is available
         * @param uri              The Uri to be opened
         */
        private fun openCustomTab(
            context: Context,
            customTabsIntent: CustomTabsIntent,
            uri: Uri
        ) {
            val packageName = CustomTabsPackageHelper.getPackageNameToUse(context, uri)

            // If we cant find a package name, it means there's no browser that supports Chrome
            // Custom Tabs installed. So, we fallback to the web-view
            if (packageName == null) {
                launchFallback(context, uri)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    customTabsIntent.intent.putExtra(
                        Intent.EXTRA_REFERRER,
                        Uri.parse("${Intent.URI_ANDROID_APP_SCHEME}//${context.packageName}")
                    )
                }
                customTabsIntent.intent.setPackage(packageName)

                try {
                    customTabsIntent.launchUrl(context, uri)
                } catch (e: ActivityNotFoundException) {
                    launchFallback(context, uri)
                }
            }
        }

        private fun launchFallback(context: Context, uri: Uri) {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()) {
                context.startActivity(intent)
            }
        }
    }
}