package com.b_lam.resplash.domain

import androidx.lifecycle.LiveData
import androidx.paging.PagingData

data class Listing<T : Any>(
    val pagingData: LiveData<PagingData<T>>,
    val networkState: LiveData<PagingNetworkState>,
    val refresh: () -> Unit
)
