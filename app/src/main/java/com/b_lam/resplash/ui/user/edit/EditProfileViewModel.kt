package com.b_lam.resplash.ui.user.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.user.model.Me
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _initialUserLiveData by lazy {
        val liveData = MutableLiveData<Result<Me>>()
        viewModelScope.launch {
            val result = loginRepository.getMe()
            liveData.postValue(result)
        }
        return@lazy liveData
    }
    val userLiveData: LiveData<Result<Me>> = _initialUserLiveData

    private val _updatedUserLiveData = MutableLiveData<Result<Me>>()
    val updatedUserLiveData: LiveData<Result<Me>> = _updatedUserLiveData

    fun updateUserProfile(
        username: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        url: String?,
        instagramUsername: String?,
        location: String?,
        bio: String?
    ) {
        viewModelScope.launch {
            val result = loginRepository.updateMe(
                username, firstName, lastName, email, url, instagramUsername, location, bio
            )
            _updatedUserLiveData.postValue(result)
        }
    }
}