package com.b_lam.resplash.ui.collection.detail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_edit_collection.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditCollectionBottomSheet : BottomSheetDialogFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

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
        return inflater.inflate(R.layout.bottom_sheet_edit_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.collectionLiveData.observe(viewLifecycleOwner) {
            collection_name_text_input_layout.editText?.setText(it.title)
            collection_description_text_input_layout.editText?.setText(it.description)
            make_collection_private_checkbox.isChecked = it.private ?: false
        }

        cancel_collection_button.setOnClickListener { dismiss() }
        delete_collection_button.setOnClickListener {
            are_you_sure_text_view.isVisible = true
            delete_no_collection_button.isVisible = true
            delete_yes_collection_button.isVisible = true
            delete_collection_button.isVisible = false
            cancel_collection_button.isVisible = false
            save_collection_button.isVisible = false
        }
        delete_no_collection_button.setOnClickListener {
            are_you_sure_text_view.isVisible = false
            delete_no_collection_button.isVisible = false
            delete_yes_collection_button.isVisible = false
            delete_collection_button.isVisible = true
            cancel_collection_button.isVisible = true
            save_collection_button.isVisible = true
        }

        save_collection_button.setOnClickListener {
            if (isInputValid()) {
                progress_bar.isVisible = true
                delete_collection_button.isEnabled = false
                cancel_collection_button.isEnabled = false
                save_collection_button.isEnabled = false
                sharedViewModel.updateCollection(
                    collection_name_text_input_layout.editText?.text.toString(),
                    collection_description_text_input_layout.editText?.text.toString(),
                    make_collection_private_checkbox.isChecked
                )
                sharedViewModel.updateCollectionResultLiveData.observeEvent(viewLifecycleOwner) {
                    if (it !is Result.Success) context.toast(R.string.oops)
                    dismiss()
                }
            } else {
                showErrorMessage()
            }
        }

        delete_yes_collection_button.setOnClickListener {
            progress_bar.isVisible = true
            delete_no_collection_button.isEnabled = false
            delete_yes_collection_button.isEnabled = false
            sharedViewModel.deleteCollection()
            sharedViewModel.deleteCollectionResultLiveData.observeEvent(viewLifecycleOwner) {
                if (it !is Result.Success) context.toast(R.string.oops)
                dismiss()
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

    companion object {

        val TAG = EditCollectionBottomSheet::class.java.simpleName

        fun newInstance() = EditCollectionBottomSheet()
    }
}