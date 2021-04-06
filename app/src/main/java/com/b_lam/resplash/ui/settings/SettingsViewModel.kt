package com.b_lam.resplash.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.GlideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(private val context: Context) : ViewModel() {

    var shouldRestartMainActivity = false

    private val _glideCacheSize = MutableLiveData(getGlideCacheSize())
    val glideCacheSize: LiveData<Long?> = _glideCacheSize

    fun launchClearCache() {
        viewModelScope.launch {
            clearCache()
            _glideCacheSize.postValue(getGlideCacheSize())
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