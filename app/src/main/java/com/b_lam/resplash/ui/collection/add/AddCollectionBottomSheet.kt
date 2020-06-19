package com.b_lam.resplash.ui.collection.add

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.ui.photo.detail.PhotoDetailViewModel
import com.b_lam.resplash.ui.widget.recyclerview.RecyclerViewPaginator
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_add_collection.*
import kotlinx.android.synthetic.main.empty_error_state_layout.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddCollectionBottomSheet : BottomSheetDialogFragment(), AddCollectionAdapter.ItemEventCallback {

    private val sharedViewModel: PhotoDetailViewModel by sharedViewModel()

    private val collectionAdapter = AddCollectionAdapter(this)

    private var photoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARGUMENT_PHOTO_ID)?.let {
            photoId = it
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState)

        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet).apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_collection_button.setOnClickListener { showCreateLayout() }

        cancel_collection_button.setOnClickListener {
            showAddLayout()
            resetInput()
        }

        create_collection_button.setOnClickListener {
            if (isInputValid()) {
                photoId?.let { photoId ->
                    sharedViewModel.createCollection(
                        collection_name_text_input_layout.editText?.text.toString(),
                        collection_description_text_input_layout.editText?.text.toString(),
                        make_collection_private_checkbox.isChecked,
                        photoId
                    ).observe(viewLifecycleOwner) {
                        create_loading_layout.isVisible = it is Result.Loading
                        when (it) {
                            is Result.Success -> {
                                recycler_view.scrollToPosition(0)
                                showAddLayout()
                                resetInput()
                            }
                            is Result.Error, Result.NetworkError -> context?.toast(R.string.oops)
                        }
                    }
                }
            } else {
                showErrorMessage()
            }
        }

        recycler_view.apply {
            adapter = collectionAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply {
                addItemDecoration(SpacingItemDecoration(context, R.dimen.keyline_7, RecyclerView.HORIZONTAL))
            }
            RecyclerViewPaginator(
                onLoadMore = { sharedViewModel.loadMore() },
                isLoading = { sharedViewModel.isLoading },
                onLastPage = { sharedViewModel.onLastPage }
            ).attach(this)
        }

        setupEmptyState()

        sharedViewModel.currentUserCollectionIds.observe(viewLifecycleOwner) {
            collectionAdapter.setCurrentUserCollectionIds(it)
        }

        sharedViewModel.userCollections.observe(viewLifecycleOwner) {
            content_loading_layout.isVisible = it == null
            empty_state_layout.isVisible = it?.isEmpty() ?: false
            collectionAdapter.submitList(it) { collectionAdapter.notifyDataSetChanged() }
        }

        if (sharedViewModel.userCollections.value == null) {
            sharedViewModel.refresh()
        }
    }

    override fun onCollectionClick(collection: Collection, itemView: View, position: Int) {
        photoId?.let { photoId ->
            if (sharedViewModel.currentUserCollectionIds.value?.contains(collection.id) == true) {
                sharedViewModel.removePhotoFromCollection(collection.id, photoId, position)
                    .observe(viewLifecycleOwner) {
                        itemView.isClickable = it !is Result.Loading
                        itemView.findViewById<ProgressBar>(R.id.collection_add_progress).isVisible =
                            it is Result.Loading
                        itemView.findViewById<ImageView>(R.id.collection_added_icon).isVisible =
                            it is Result.Error || it is Result.NetworkError
                        if (it is Result.Error || it is Result.NetworkError) {
                            context?.toast(R.string.oops)
                        }
                    }
            } else {
                sharedViewModel.addPhotoToCollection(collection.id, photoId, position)
                    .observe(viewLifecycleOwner) {
                        itemView.isClickable = it !is Result.Loading
                        itemView.findViewById<ProgressBar>(R.id.collection_add_progress).isVisible =
                            it is Result.Loading
                        itemView.findViewById<ImageView>(R.id.collection_added_icon).isVisible =
                            it is Result.Success
                        if (it is Result.Error || it is Result.NetworkError) {
                            context?.toast(R.string.oops)
                        }
                    }
            }
        }
    }

    private fun isInputValid(): Boolean {
        val name = collection_name_text_input_layout.editText?.text.toString()
        val description = collection_description_text_input_layout.editText?.text.toString()
        return name.isNotBlank() && name.length <= 60 && description.length <= 250
    }

    private fun showErrorMessage() {
        if (collection_name_text_input_layout.editText?.text.toString().isBlank()) {
            collection_name_text_input_layout.error = getString(R.string.collection_name_required)
            collection_name_text_input_layout.editText?.doOnTextChanged { text, _, _, _ ->
                if (collection_name_text_input_layout.error.toString().isNotBlank() &&
                    text?.isBlank() != true) {
                    collection_name_text_input_layout.error = null
                }
            }
        }
    }

    private fun showAddLayout() {
        add_to_collection_layout.visibility = View.VISIBLE
        create_collection_layout.visibility = View.INVISIBLE
    }

    private fun showCreateLayout() {
        add_to_collection_layout.visibility = View.INVISIBLE
        create_collection_layout.visibility = View.VISIBLE
    }

    private fun resetInput() {
        collection_name_text_input_layout.editText?.setText("")
        collection_name_text_input_layout.error = null
        collection_description_text_input_layout.editText?.setText("")
        make_collection_private_checkbox.isChecked = false
    }

    private fun setupEmptyState() {
        context?.let {
            empty_state_layout.setBackgroundColor(
                ContextCompat.getColor(it, R.color.color_tinted_surface))
        }
        empty_state_layout.empty_error_state_title.text = getString(R.string.empty_state_title)
    }

    companion object {

        val TAG = AddCollectionBottomSheet::class.java.simpleName

        private const val ARGUMENT_PHOTO_ID = "argument_photo_id"

        fun newInstance(id: String) = AddCollectionBottomSheet()
            .apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_PHOTO_ID, id)
            }
        }
    }
}