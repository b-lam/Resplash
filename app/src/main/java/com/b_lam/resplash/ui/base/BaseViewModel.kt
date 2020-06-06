package com.b_lam.resplash.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b_lam.resplash.util.livedata.Event

abstract class BaseViewModel : ViewModel() {

    private val authRequiredMutableLiveData = MutableLiveData<Event<Int>>()
    val authRequiredLiveData: LiveData<Event<Int>> = authRequiredMutableLiveData
}
