package com.b_lam.resplash.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.authorization.model.AccessToken
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

    private val _loginStateLiveData = MutableLiveData<Result<AccessToken>>()
    val loginStateLiveData: LiveData<Result<AccessToken>> = _loginStateLiveData

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

    fun getAccessToken(code: String) {
        viewModelScope.launch {
            val accessToken = loginRepository.getAccessToken(code)
            _loginStateLiveData.postValue(accessToken)
        }
    }
}