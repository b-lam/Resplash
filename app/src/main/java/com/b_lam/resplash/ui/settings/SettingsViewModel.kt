package com.b_lam.resplash.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(private val context: Context) : BaseViewModel() {

    var shouldRestartMainActivity = false

    val glideCacheSize = MutableLiveData(getGlideCacheSize())

    fun launchClearCache() {
        viewModelScope.launch {
            clearCache()
            glideCacheSize.postValue(getGlideCacheSize())
        }
    }

    private suspend fun clearCache() = withContext(Dispatchers.Default) {
        GlideApp.get(context).clearDiskCache()
    }

    private fun getGlideCacheSize() = GlideApp.getPhotoCacheDir(context)?.dirSize()

    private fun File.dirSize(): Long {
        if (this.exists()) {
            var result: Long = 0
            listFiles()?.forEach { aFileList ->
                result += if (aFileList.isDirectory) {
                    this.dirSize()
                } else {
                    aFileList.length()
                }
            }
            return result / 1024 / 1024
        }
        return 0
    }
}