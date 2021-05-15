package com.b_lam.resplash.ui.main

import com.b_lam.resplash.ui.topic.TopicAdapter
import com.b_lam.resplash.ui.topic.TopicFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainTopicFragment : TopicFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override val pagedListAdapter =
        TopicAdapter(itemEventCallback, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshPhotos() }
            topicsRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            topicsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            topicsLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = MainTopicFragment()
    }
}