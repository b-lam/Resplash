package com.b_lam.resplash.ui.main

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainPhotoFragment : PhotoFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    private var job: Job? = null

    override val pagingAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.photoOrderLiveData.observe(viewLifecycleOwner) { order ->
            job?.cancel()
            job = lifecycleScope.launch {
                sharedViewModel.getPhotos(order).collectLatest { pagingAdapter.submitData(it) }
            }
        }
    }

    override fun trackDownload(id: String) {
        sharedViewModel.trackDownload(id)
    }

    companion object {

        fun newInstance() = MainPhotoFragment()
    }
}