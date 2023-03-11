package com.b_lam.resplash.domain

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState

abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {

    val networkState = MutableLiveData<PagingNetworkState>()

    abstract suspend fun getPage(page: Int, perPage: Int): List<T>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val pageNumber = params.key ?: 1
        return try {
            if (pageNumber == 1) {
                networkState.postValue(PagingNetworkState.Refreshing)
            }
            val response = getPage(pageNumber, params.loadSize)
            if (pageNumber == 1 && response.isEmpty()) {
                networkState.postValue(PagingNetworkState.Empty)
            } else {
                networkState.postValue(PagingNetworkState.Success)
            }
            LoadResult.Page(
                data = response,
                prevKey = if (pageNumber == 1) null else pageNumber - 1,
                nextKey = if (response.size < params.loadSize) null else pageNumber + 1
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error if it is an
            // expected error (such as a network failure).
            if (pageNumber == 1) {
                networkState.postValue(PagingNetworkState.RefreshError("Error: $e"))
            } else {
                networkState.postValue(PagingNetworkState.PageError("Error: $e"))
            }
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

sealed interface PagingNetworkState {
    object Empty : PagingNetworkState
    object Refreshing : PagingNetworkState
    object Success : PagingNetworkState
    data class PageError(val message: String? = null) : PagingNetworkState
    data class RefreshError(val message: String? = null) : PagingNetworkState
}
