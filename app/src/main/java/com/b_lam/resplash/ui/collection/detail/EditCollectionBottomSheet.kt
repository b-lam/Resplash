package com.b_lam.resplash.ui.collection.detail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.BottomSheetEditCollectionBinding
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditCollectionBottomSheet : BottomSheetDialogFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

    private val binding: BottomSheetEditCollectionBinding by viewBinding(CreateMethod.INFLATE)

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
            sharedViewModel.collectionLiveData.observe(viewLifecycleOwner) {
                collectionNameTextInputLayout.editText?.setText(it.title)
                collectionDescriptionTextInputLayout.editText?.setText(it.description)
                makeCollectionPrivateCheckbox.isChecked = it.private ?: false
            }

            cancelCollectionButton.setOnClickListener { dismiss() }
            deleteCollectionButton.setOnClickListener {
                areYouSureTextView.isVisible = true
                deleteNoCollectionButton.isVisible = true
                deleteYesCollectionButton.isVisible = true
                deleteCollectionButton.isVisible = false
                cancelCollectionButton.isVisible = false
                saveCollectionButton.isVisible = false
            }
            deleteNoCollectionButton.setOnClickListener {
                areYouSureTextView.isVisible = false
                deleteNoCollectionButton.isVisible = false
                deleteYesCollectionButton.isVisible = false
                deleteCollectionButton.isVisible = true
                cancelCollectionButton.isVisible = true
                saveCollectionButton.isVisible = true
            }

            saveCollectionButton.setOnClickListener {
                if (isInputValid()) {
                    progressBar.isVisible = true
                    deleteCollectionButton.isEnabled = false
                    cancelCollectionButton.isEnabled = false
                    saveCollectionButton.isEnabled = false
                    sharedViewModel.updateCollection(
                        collectionNameTextInputLayout.editText?.text.toString(),
                        collectionDescriptionTextInputLayout.editText?.text.toString(),
                        makeCollectionPrivateCheckbox.isChecked
                    )
                    sharedViewModel.updateCollectionResultLiveData.observeEvent(viewLifecycleOwner) {
                        if (it !is Result.Success) context.toast(R.string.oops)
                        dismiss()
                    }
                } else {
                    showErrorMessage()
                }
            }

            deleteYesCollectionButton.setOnClickListener {
                progressBar.isVisible = true
                deleteNoCollectionButton.isEnabled = false
                deleteYesCollectionButton.isEnabled = false
                sharedViewModel.deleteCollection()
                sharedViewModel.deleteCollectionResultLiveData.observeEvent(viewLifecycleOwner) {
                    if (it !is Result.Success) context.toast(R.string.oops)
                    dismiss()
                }
            }
        }
    }

    private fun isInputValid(): Boolean {
        val name = binding.collectionNameTextInputLayout.editText?.text.toString()
        val description = binding.collectionDescriptionTextInputLayout.editText?.text.toString()
        return name.isNotBlank() && name.length <= 60 && description.length <= 250
    }

    private fun showErrorMessage() {
        if (binding.collectionNameTextInputLayout.editText?.text.toString().isBlank()) {
            binding.collectionNameTextInputLayout.error = getString(R.string.collection_name_required)
            binding.collectionNameTextInputLayout.editText?.doOnTextChanged { text, _, _, _ ->
                if (binding.collectionNameTextInputLayout.error.toString().isNotBlank() &&
                    text?.isBlank() != true) {
                    binding.collectionNameTextInputLayout.error = null
                }
            }
        }
    }

    companion object {

        val TAG: String = EditCollectionBottomSheet::class.java.simpleName

        fun newInstance() = EditCollectionBottomSheet()
    }
}