package com.b_lam.resplash.domain

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.b_lam.resplash.util.NetworkState

data class Listing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val networkState: LiveData<NetworkState>,
    val refresh: () -> Unit,
    val refreshState: LiveData<NetworkState>,
    val retry: () -> Unit
)