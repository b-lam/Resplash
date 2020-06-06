package com.b_lam.resplash.domain

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.b_lam.resplash.util.NetworkState
import com.b_lam.resplash.util.error
import kotlinx.coroutines.*

abstract class BaseDataSource<T>(
    private val scope: CoroutineScope
) : PageKeyedDataSource<Int, T>() {

    private val initialCoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        this.error("An error happened: $e")
        initialLoadState.postValue(NetworkState.ERROR("Error: $e"))
    }

    private val postCoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        this.error("An error happened: $e")
        networkState.postValue(NetworkState.ERROR("Error: $e"))
    }

    private val supervisorJob = SupervisorJob()

    val networkState = MutableLiveData<NetworkState>()

    val initialLoadState = MutableLiveData<NetworkState>()

    abstract suspend fun getPage(page: Int, perPage: Int): List<T>

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, T>
    ) {
        initialLoadState.postValue(NetworkState.LOADING)
        networkState.postValue(NetworkState.LOADING)

        scope.launch(initialCoroutineExceptionHandler + supervisorJob) {
            val page = getPage(1, params.requestedLoadSize)
            if (page.isEmpty()) {
                initialLoadState.postValue(NetworkState.EMPTY)
            } else {
                initialLoadState.postValue(NetworkState.SUCCESS)
                networkState.postValue(NetworkState.SUCCESS)
            }
            callback.onResult(page, null, 2)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        networkState.postValue(NetworkState.LOADING)

        scope.launch(postCoroutineExceptionHandler + supervisorJob) {
            val page = getPage(params.key, params.requestedLoadSize)
            networkState.postValue(NetworkState.SUCCESS)
            callback.onResult(page, params.key - 1)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        networkState.postValue(NetworkState.LOADING)

        scope.launch(postCoroutineExceptionHandler + supervisorJob) {
            val page = getPage(params.key, params.requestedLoadSize)
            networkState.postValue(NetworkState.SUCCESS)
            callback.onResult(page, params.key + 1)
        }
    }

    override fun invalidate() {
        super.invalidate()
        supervisorJob.cancelChildren()
    }
}
