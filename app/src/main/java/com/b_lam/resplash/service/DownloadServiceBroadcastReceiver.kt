package com.b_lam.resplash.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadServiceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fileName = intent.getStringExtra(EXTRA_CANCEL_FILE_NAME) ?: return
        DownloadJobIntentService.cancelDownload(context, fileName)
    }

    companion object {

        const val EXTRA_CANCEL_FILE_NAME = "extra_cancel_file_name"
    }
}