package com.b_lam.resplash.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.domain.photo.UserLikesPagingSource
import com.b_lam.resplash.domain.photo.UserPhotoPagingSource
import com.b_lam.resplash.domain.user.UserRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val _getUserResultLiveData = MutableLiveData<Event<Result<User>>>()
    val getUserResultLiveData: LiveData<Event<Result<User>>> = _getUserResultLiveData

    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User> = _userLiveData

    fun getUser(username: String) {
        viewModelScope.launch {
            val result = userRepository.getUserPublicProfile(username)
            if (result is Result.Success) {
                setUser(result.value)
            }
            _getUserResultLiveData.postValue(Event(result))
        }
    }

    fun setUser(user: User) = _userLiveData.postValue(user)

    fun getUserPhotos(username: String): Flow<PagingData<Photo>> {
        return photoRepository.getUserPhotos(
            username,
            UserPhotoPagingSource.Companion.Order.LATEST,
            false,
            null,
            null,
            UserPhotoPagingSource.Companion.Orientation.ALL
        ).cachedIn(viewModelScope)
    }

    fun getUserLikes(username: String): Flow<PagingData<Photo>> {
        return photoRepository.getUserLikes(
            username,
            UserLikesPagingSource.Companion.Order.LATEST,
            UserLikesPagingSource.Companion.Orientation.ALL
        ).cachedIn(viewModelScope)
    }

    fun getUserCollections(username: String): Flow<PagingData<Collection>> {
        return collectionRepository.getUserCollections(username).cachedIn(viewModelScope)
    }

    fun trackDownload(id: String) = viewModelScope.launch { photoRepository.trackDownload(id) }

    fun isOwnProfile() = userLiveData.value?.username == loginRepository.getUsername()
}