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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.databinding.BottomSheetAddCollectionBinding
import com.b_lam.resplash.ui.photo.detail.PhotoDetailViewModel
import com.b_lam.resplash.ui.widget.recyclerview.RecyclerViewPaginator
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddCollectionBottomSheet : BottomSheetDialogFragment(), AddCollectionAdapter.ItemEventCallback {

    private val sharedViewModel: PhotoDetailViewModel by sharedViewModel()

    private val binding: BottomSheetAddCollectionBinding by viewBinding(CreateMethod.INFLATE)

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
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            addCollectionButton.setOnClickListener { showCreateLayout() }

            cancelCollectionButton.setOnClickListener {
                showAddLayout()
                resetInput()
            }

            createCollectionButton.setOnClickListener {
                if (isInputValid()) {
                    photoId?.let { photoId ->
                        sharedViewModel.createCollection(
                            collectionNameTextInputLayout.editText?.text.toString(),
                            collectionDescriptionTextInputLayout.editText?.text.toString(),
                            makeCollectionPrivateCheckbox.isChecked,
                            photoId
                        ).observe(viewLifecycleOwner) {
                            createLoadingLayout.isVisible = it is Result.Loading
                            when (it) {
                                is Result.Success -> {
                                    recyclerView.scrollToPosition(0)
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

            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply {
                    addItemDecoration(SpacingItemDecoration(context, R.dimen.keyline_7, RecyclerView.HORIZONTAL))
                }
                adapter = collectionAdapter
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
                contentLoadingLayout.isVisible = it == null
                emptyStateLayout.root.isVisible = it?.isEmpty() ?: false
                collectionAdapter.submitList(it) { collectionAdapter.notifyDataSetChanged() }
            }

            if (sharedViewModel.userCollections.value == null) {
                sharedViewModel.refresh()
            }
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
        val name = binding.collectionNameTextInputLayout.editText?.text.toString()
        val description = binding.collectionDescriptionTextInputLayout.editText?.text.toString()
        return name.isNotBlank() && name.length <= 60 && description.length <= 250
    }

    private fun showErrorMessage() = with(binding) {
        if (collectionNameTextInputLayout.editText?.text.toString().isBlank()) {
            collectionNameTextInputLayout.error = getString(R.string.collection_name_required)
            collectionNameTextInputLayout.editText?.doOnTextChanged { text, _, _, _ ->
                if (collectionNameTextInputLayout.error.toString().isNotBlank() &&
                    text?.isBlank() != true) {
                        collectionNameTextInputLayout.error = null
                }
            }
        }
    }

    private fun showAddLayout() = with(binding) {
        addToCollectionLayout.visibility = View.VISIBLE
        createCollectionLayout.visibility = View.INVISIBLE
    }

    private fun showCreateLayout() = with(binding) {
        addToCollectionLayout.visibility = View.INVISIBLE
        createCollectionLayout.visibility = View.VISIBLE
    }

    private fun resetInput() = with(binding) {
        collectionNameTextInputLayout.editText?.setText("")
        collectionNameTextInputLayout.error = null
        collectionDescriptionTextInputLayout.editText?.setText("")
        makeCollectionPrivateCheckbox.isChecked = false
    }

    private fun setupEmptyState() {
        context?.let {
            binding.emptyStateLayout.root.setBackgroundColor(
                ContextCompat.getColor(it, R.color.color_tinted_surface))
        }
        binding.emptyStateLayout.emptyErrorStateTitle.text = getString(R.string.empty_state_title)
    }

    companion object {

        val TAG: String = AddCollectionBottomSheet::class.java.simpleName

        private const val ARGUMENT_PHOTO_ID = "argument_photo_id"

        fun newInstance(id: String) = AddCollectionBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_PHOTO_ID, id)
            }
        }
    }
}