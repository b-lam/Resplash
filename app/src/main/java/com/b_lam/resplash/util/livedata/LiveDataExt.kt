package com.b_lam.resplash.util.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

inline fun <T> LiveData<Event<T>>.observeEvent(
    owner: LifecycleOwner,
    crossinline onEventUnhandledContent: (T) -> Unit
) {
    observe(owner, Observer { it?.getContentIfNotHandled()?.let(onEventUnhandledContent) })
}

/**
 * Example usage:
 *
    private val contactsLiveData: Map<Parameters, LiveData<Contacts>> = lazyMap { parameters ->
        val liveData = MutableLiveData<Contacts>()
        getContactsUseCase.loadContacts(parameters) { liveData.value = it }
        return@lazyMap liveData
    }

    fun contacts(parameters: Parameters): LiveData<Contacts> = contactsLiveData.getValue(parameters)
 *
 */
fun <K, V> lazyMap(initializer: (K) -> V): Map<K, V> {
    val map = mutableMapOf<K, V>()
    return map.withDefault { key ->
        val newValue = initializer(key)
        map[key] = newValue
        return@withDefault newValue
    }
}