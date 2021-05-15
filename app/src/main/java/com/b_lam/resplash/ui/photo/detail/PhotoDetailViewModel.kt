package com.b_lam.resplash.ui.photo.detail

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.*
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.di.Properties
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.error
import com.b_lam.resplash.util.livedata.lazyMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PhotoDetailViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _photoDetailsLiveData: Map<String, LiveData<Photo>> = lazyMap {
        val liveData = MutableLiveData<Photo>()
        viewModelScope.launch {
            val result = photoRepository.getPhotoDetails(it)
            when (result) {
                is Result.Success -> {
                    liveData.postValue(result.value)
                    _currentUserCollectionIds.postValue(
                        result.value.current_user_collections?.map { it.id }?.toMutableList())
                }
            }
        }
        return@lazyMap liveData
    }

    private val _currentUserCollectionIds = MutableLiveData<MutableList<String>?>()
    val currentUserCollectionIds: LiveData<MutableList<String>?> = _currentUserCollectionIds

    private val _userCollections = MutableLiveData<MutableList<Collection>?>()
    val userCollections: LiveData<MutableList<Collection>?> = _userCollections

    private val _wallpaperBitmap = MutableLiveData<Result<Bitmap>>()
    val wallpaperBitmap: LiveData<Result<Bitmap>> = _wallpaperBitmap

    var downloadId: Long? = null
    var downloadUUID: UUID? = null

    fun photoDetailsLiveData(id: String): LiveData<Photo> = _photoDetailsLiveData.getValue(id)

    fun likePhoto(id: String) = viewModelScope.launch { photoRepository.likePhoto(id) }

    fun unlikePhoto(id: String) = viewModelScope.launch { photoRepository.unlikePhoto(id) }

    fun isUserAuthorized() = loginRepository.isAuthorized()

    fun addPhotoToCollection(collectionId: String, photoId: String, position: Int) =
        liveData(viewModelScope.coroutineContext) {
            emit(Result.Loading)

            val result = collectionRepository.addPhotoToCollection(collectionId, photoId)
            if (result is Result.Success) {
                val newIdList = _currentUserCollectionIds.value ?: mutableListOf()
                newIdList.add(collectionId)
                _currentUserCollectionIds.postValue(newIdList)

                val newCollectionsList = _userCollections.value
                result.value.collection?.let { newCollectionsList?.set(position, it) }
                _userCollections.postValue(newCollectionsList)
            }

            emit(result)
        }

    fun removePhotoFromCollection(collectionId: String, photoId: String, position: Int) =
        liveData(viewModelScope.coroutineContext) {
            emit(Result.Loading)

            val result = collectionRepository.removePhotoFromCollection(collectionId, photoId)
            if (result is Result.Success) {
                val newList = _currentUserCollectionIds.value ?: mutableListOf()
                newList.remove(collectionId)
                _currentUserCollectionIds.postValue(newList)

                val newCollectionsList = _userCollections.value
                result.value.collection?.let { newCollectionsList?.set(position, it) }
                _userCollections.postValue(newCollectionsList)
            }

            emit(result)
        }

    private var page = 1
    var isLoading = false
    var onLastPage = false

    fun refresh() {
        page = 1
        isLoading = false
        onLastPage = false
        loadMore()
    }

    fun loadMore() {
        viewModelScope.launch(Dispatchers.Default) {
            isLoading = true
            val username = loginRepository.getUsername() ?: return@launch
            val result = collectionRepository.getUserCollections(username, page)
            if (result is Result.Success) {
                val newList = _userCollections.value ?: mutableListOf()
                newList.addAll(result.value)
                _userCollections.postValue(newList)
                onLastPage = result.value.isEmpty() || result.value.size < Properties.DEFAULT_PAGE_SIZE
                page++
            }
            isLoading = false
        }
    }

    fun createCollection(
        title: String,
        description: String?,
        private: Boolean?,
        photoId: String
    ) = liveData {
        emit(Result.Loading)

        val createResult = collectionRepository.createCollection(title, description, private)
        if (createResult is Result.Success) {

            var newCollection = createResult.value

            val addResult = collectionRepository.addPhotoToCollection(newCollection.id, photoId)
            if (addResult is Result.Success) {
                val newIdList = _currentUserCollectionIds.value ?: mutableListOf()
                newIdList.add(newCollection.id)
                _currentUserCollectionIds.postValue(newIdList)

                addResult.value.collection?.let { newCollection = it }
            }

            val newList = _userCollections.value ?: mutableListOf()
            newList.add(0, newCollection)
            _userCollections.postValue(newList)
        }

        emit(createResult)
    }

    fun prepareBitmapFromUri(
        contentResolver: ContentResolver,
        uri: Uri
    ) {
        viewModelScope.launch {
            val bitmapResult = decodeBitmap(contentResolver, uri)
            _wallpaperBitmap.postValue(bitmapResult)
        }
    }

    private suspend fun decodeBitmap(
        contentResolver: ContentResolver,
        uri: Uri
    ): Result<Bitmap> {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                Result.Success(bitmap)
            } catch (e: Exception) {
                val message = "Error decoding bitmap"
                error(message, e)
                Result.Error(error = message)
            }
        }
    }
}

