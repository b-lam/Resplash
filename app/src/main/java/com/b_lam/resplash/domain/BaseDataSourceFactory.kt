package com.b_lam.resplash.domain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.b_lam.resplash.di.Properties

abstract class BaseDataSourceFactory<T> : DataSource.Factory<Int, T>() {

    private val sourceLiveData = MutableLiveData<BaseDataSource<T>>()

    private val config = Config(
        pageSize = Properties.DEFAULT_PAGE_SIZE,
        initialLoadSizeHint = Properties.DEFAULT_PAGE_SIZE,
        prefetchDistance = Properties.DEFAULT_PAGE_SIZE / 2,
        enablePlaceholders = false
    )

    abstract fun createDataSource(): BaseDataSource<T>

    override fun create(): DataSource<Int, T> {
        val source = createDataSource()
        sourceLiveData.postValue(source)
        return source
    }

    fun createListing() = Listing<T>(
        pagedList = LivePagedListBuilder(this, config).build(),
        networkState = Transformations.switchMap(
            this.sourceLiveData,
            BaseDataSource<T>::networkState
        ),
        refresh = { this.sourceLiveData.value?.invalidate() },
        refreshState = Transformations.switchMap(
            this.sourceLiveData,
            BaseDataSource<T>::initialLoadState
        ),
        retry = {}
    )
}
