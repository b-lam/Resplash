package com.b_lam.resplash.domain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.*
import com.b_lam.resplash.di.Properties

// TODO: Can I get rid of this and the loading state?
abstract class BasePagingSourceFactory<T : Any> {

    private val sourceLiveData = MutableLiveData<BasePagingSource<T>>()

    private val pagingConfig = PagingConfig(
        pageSize = Properties.DEFAULT_PAGE_SIZE,
        initialLoadSize = Properties.DEFAULT_PAGE_SIZE,
        prefetchDistance = Properties.DEFAULT_PAGE_SIZE / 2,
        enablePlaceholders = false,
    )

    abstract fun createDataSource(): BasePagingSource<T>

    fun createListing() = Listing(
        pagingData = Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                createDataSource().also {
                    sourceLiveData.postValue(it)
                }
            }
        ).liveData,
        networkState = Transformations.switchMap(
            this.sourceLiveData,
            BasePagingSource<T>::networkState
        ),
        refresh = { this.sourceLiveData.value?.invalidate() }
    )
}
