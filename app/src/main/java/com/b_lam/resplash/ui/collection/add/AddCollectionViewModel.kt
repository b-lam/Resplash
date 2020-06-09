package com.b_lam.resplash.ui.collection.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class AddCollectionViewModel(
    private val collectionRepository: CollectionRepository,
    loginRepository: LoginRepository
) : BaseViewModel() {

    private val collectionListing = MutableLiveData(
        loginRepository.getUsername()?.let {
            collectionRepository.getUserCollections(it, viewModelScope)
        }
    )

    val collectionsLiveData = Transformations.switchMap(collectionListing) { it?.pagedList }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) { it?.networkState }
    val collectionsRefreshStateLiveData = Transformations.switchMap(collectionListing) { it?.refreshState }

    fun createCollection(
        title: String,
        description: String?,
        private: Boolean?
    ) {
        // TODO: Show some kind of loading state
        viewModelScope.launch {
            val result = collectionRepository.createCollection(title, description, private)
            // TODO: Post result to livedata
            // TODO: Go back to first page, reset input and invalidate listing
            // TODO: Handle error
        }
    }

    fun addPhotoToCollection(collectionId: Int, photoId: String) {

    }

    fun removePhotoFromCollection(collectionId: Int, photoId: String) {

    }
}