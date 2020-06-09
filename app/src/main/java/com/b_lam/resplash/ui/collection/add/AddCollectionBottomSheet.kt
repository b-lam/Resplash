package com.b_lam.resplash.ui.collection.add

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_to_collection_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_add_collection.*
import kotlinx.android.synthetic.main.create_collection_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCollectionBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: AddCollectionViewModel by viewModel()

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

        arguments?.getParcelable<Photo>(ARGUMENT_PHOTO)?.let {

        }

        view_pager.apply {
            adapter =
                CollectionManagementPagerAdapter()
            offscreenPageLimit = 2
        }

        add_collection_button.setOnClickListener { view_pager.currentItem = 1 }

        cancel_collection_button.setOnClickListener {
            view_pager.currentItem = 0
            resetInput()
        }

        create_collection_button.setOnClickListener {
            if (isInputValid()) {
                viewModel.createCollection(
                    collection_name_text_input_layout.editText?.text.toString(),
                    collection_description_text_input_layout.editText?.text.toString(),
                    make_collection_private_checkbox.isChecked
                )
            } else {
                showErrorMessage()
            }
        }

        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
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

    private fun resetInput() {
        collection_name_text_input_layout.editText?.setText("")
        collection_name_text_input_layout.error = null
        collection_description_text_input_layout.editText?.setText("")
        make_collection_private_checkbox.isChecked = false
    }

    private class CollectionManagementPagerAdapter : PagerAdapter() {

        val layoutIds = listOf(R.id.add_to_collection_layout, R.id.create_collection_layout)

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return container.findViewById(layoutIds[position])
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as? View)
        }

        override fun isViewFromObject(view: View, `object`: Any) = view == `object`

        override fun getCount() = layoutIds.size
    }

    companion object {

        val TAG = AddCollectionBottomSheet::class.java.simpleName

        private const val ARGUMENT_PHOTO = "argument_photo"

        fun newInstance(photo: Photo) = AddCollectionBottomSheet()
            .apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_PHOTO, photo)
            }
        }
    }
}