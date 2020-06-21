package com.b_lam.resplash.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val photoRepository: PhotoRepository
) : BaseViewModel() {

    private val _bannerPhotoLiveData by lazy {
        val liveData = MutableLiveData<Photo>()
        viewModelScope.launch {
            val result = photoRepository.getRandomPhoto(featured = true)
            if (result is Result.Success) liveData.value = result.value
        }
        return@lazy liveData
    }
    val bannerPhotoLiveData: LiveData<Photo> = _bannerPhotoLiveData

    val loginUrl = loginRepository.loginUrl

    fun getAccessToken(code: String) = liveData(viewModelScope.coroutineContext) {
        emit(Result.Loading)

        val accessTokenResult = loginRepository.getAccessToken(code)
        if (accessTokenResult is Result.Success) { loginRepository.getMe() }

        emit(accessTokenResult)
    }
}