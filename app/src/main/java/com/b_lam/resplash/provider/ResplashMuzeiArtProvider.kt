package com.b_lam.resplash.provider

import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.util.safeApiCall
import com.b_lam.resplash.util.warn
import com.b_lam.resplash.worker.MuzeiWorker
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.io.InputStream

class ResplashMuzeiArtProvider : MuzeiArtProvider() {

    override fun onLoadRequested(initial: Boolean) {
        val context = context ?: return
        MuzeiWorker.scheduleJob(context, get())
    }

    override fun openFile(artwork: Artwork): InputStream {
        return super.openFile(artwork).also {
            artwork.token?.run {
                try {
                    val downloadService: DownloadService by inject()
                    CoroutineScope(Job() + Dispatchers.IO).launch {
                        safeApiCall(Dispatchers.IO) { downloadService.trackDownload(this@run) }
                    }
                } catch (e: Exception) {
                    warn("Error reporting download to Unsplash", e)
                }
            }
        }
    }
}