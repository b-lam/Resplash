package com.b_lam.resplash.ui.collection.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import com.b_lam.resplash.util.livedata.observeOnce
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CollectionDetailFragment : PhotoFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

    override val pagingAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.collectionLiveData.observeOnce(this) {
            swipe_refresh_layout.isEnabled = sharedViewModel.isOwnCollection()
        }
    }

    override fun observeEvents() {
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.collectionLiveData.observe(viewLifecycleOwner) { collection ->
            lifecycleScope.launch {
                sharedViewModel.getPhotosForCollection(collection.id).collectLatest {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    override fun trackDownload(id: String) {
        sharedViewModel.trackDownload(id)
    }

    companion object {

        fun newInstance() = CollectionDetailFragment()
    }
}