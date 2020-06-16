package com.b_lam.resplash.ui.search

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.photo.SearchPhotoPagingSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_search_photo_filter.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchPhotoFilterBottomSheet : BottomSheetDialogFragment() {

    private val sharedViewModel: SearchViewModel by sharedViewModel()

    private var searchParametersChanged = false

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
        return inflater.inflate(R.layout.bottom_sheet_search_photo_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderButtonId = when (sharedViewModel.order) {
            SearchPhotoPagingSource.Companion.Order.RELEVANT -> R.id.order_relevance_button
            else -> R.id.order_latest_button
        }
        order_by_toggle_group.check(orderButtonId)
        order_by_toggle_group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val order = when (checkedId) {
                    R.id.order_relevance_button -> SearchPhotoPagingSource.Companion.Order.RELEVANT
                    else -> SearchPhotoPagingSource.Companion.Order.LATEST
                }
                sharedViewModel.order = order
                searchParametersChanged = true
            }
        }

        val contentFilterButtonId = when (sharedViewModel.contentFilter) {
            SearchPhotoPagingSource.Companion.ContentFilter.LOW -> R.id.content_filter_low_button
            else -> R.id.content_filter_high_button
        }
        content_filter_toggle_group.check(contentFilterButtonId)
        content_filter_toggle_group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val contentFilter = when (checkedId) {
                    R.id.content_filter_low_button -> SearchPhotoPagingSource.Companion.ContentFilter.LOW
                    else -> SearchPhotoPagingSource.Companion.ContentFilter.HIGH
                }
                sharedViewModel.contentFilter = contentFilter
                searchParametersChanged = true
            }
        }

        val items = enumValues<SearchPhotoPagingSource.Companion.Color>()
        val titles = items.map { getString(it.titleRes) }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_list, titles)
        val colorFilterDropdownMenu = (color_filter_dropdown_menu.editText as? AutoCompleteTextView)
        colorFilterDropdownMenu?.setAdapter(adapter)
        colorFilterDropdownMenu?.setText(titles[items.indexOf(sharedViewModel.color)], false)
        colorFilterDropdownMenu?.setOnItemClickListener { _, _, position, _ ->
            val color = items[position]
            if (color != sharedViewModel.color) {
                searchParametersChanged = true
                sharedViewModel.color = color
            }
        }

        val orientationButtonId = when (sharedViewModel.orientation) {
            SearchPhotoPagingSource.Companion.Orientation.ANY -> R.id.orientation_any_button
            SearchPhotoPagingSource.Companion.Orientation.PORTRAIT -> R.id.orientation_portrait_button
            SearchPhotoPagingSource.Companion.Orientation.LANDSCAPE -> R.id.orientation_landscape_button
            else -> R.id.orientation_square_button
        }
        orientation_toggle_group.check(orientationButtonId)
        orientation_toggle_group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val orientation = when (checkedId) {
                    R.id.orientation_any_button -> SearchPhotoPagingSource.Companion.Orientation.ANY
                    R.id.orientation_portrait_button -> SearchPhotoPagingSource.Companion.Orientation.PORTRAIT
                    R.id.orientation_landscape_button -> SearchPhotoPagingSource.Companion.Orientation.LANDSCAPE
                    else -> SearchPhotoPagingSource.Companion.Orientation.SQUARISH
                }
                sharedViewModel.orientation = orientation
                searchParametersChanged = true
            }
        }

        apply_button.setOnClickListener { dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (searchParametersChanged) {
            sharedViewModel.filterPhotoSearch()
        }
    }

    companion object {

        val TAG = SearchPhotoFilterBottomSheet::class.java.simpleName

        fun newInstance() = SearchPhotoFilterBottomSheet()
    }
}