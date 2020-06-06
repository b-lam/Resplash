package com.b_lam.resplash.ui.user.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.user.model.Me
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val initialUserMutableLiveData by lazy {
        val liveData = MutableLiveData<Result<Me>>()
        viewModelScope.launch {
            val result = loginRepository.getMe()
            liveData.postValue(result)
        }
        return@lazy liveData
    }
    val userLiveData: LiveData<Result<Me>> = initialUserMutableLiveData

    private val updatedUserMutableLiveData = MutableLiveData<Result<Me>>()
    val updatedUserLiveData: LiveData<Result<Me>> = updatedUserMutableLiveData

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
            updatedUserMutableLiveData.postValue(result)
        }
    }
}