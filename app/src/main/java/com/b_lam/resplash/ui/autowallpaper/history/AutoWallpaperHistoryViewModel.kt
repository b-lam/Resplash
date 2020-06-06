package com.b_lam.resplash.ui.autowallpaper.history

import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.domain.autowallpaper.AutoWallpaperRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class AutoWallpaperHistoryViewModel(
    private val autoWallpaperRepository: AutoWallpaperRepository
) : BaseViewModel() {

    init {
        viewModelScope.launch {
            autoWallpaperRepository.deleteOldAutoWallpaperHistory()
        }
    }

    val wallpaperHistoryLiveData = autoWallpaperRepository.getAutoWallpaperHistory()

    fun clearAllWallpaperHistory() {
        viewModelScope.launch {
            autoWallpaperRepository.deleteAllAutoWallpaperHistory()
        }
    }
}
