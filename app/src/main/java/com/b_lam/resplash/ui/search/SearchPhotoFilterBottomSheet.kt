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
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.BottomSheetSearchPhotoFilterBinding
import com.b_lam.resplash.domain.photo.SearchPhotoDataSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchPhotoFilterBottomSheet : BottomSheetDialogFragment() {

    private val sharedViewModel: SearchViewModel by sharedViewModel()

    private val binding: BottomSheetSearchPhotoFilterBinding by viewBinding(CreateMethod.INFLATE)

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
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val orderButtonId = when (sharedViewModel.order) {
                SearchPhotoDataSource.Companion.Order.RELEVANT -> R.id.order_relevance_button
                else -> R.id.order_latest_button
            }
            orderByToggleGroup.check(orderButtonId)
            orderByToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val order = when (checkedId) {
                        R.id.order_relevance_button -> SearchPhotoDataSource.Companion.Order.RELEVANT
                        else -> SearchPhotoDataSource.Companion.Order.LATEST
                    }
                    sharedViewModel.order = order
                    searchParametersChanged = true
                }
            }

            val contentFilterButtonId = when (sharedViewModel.contentFilter) {
                SearchPhotoDataSource.Companion.ContentFilter.LOW -> R.id.content_filter_low_button
                else -> R.id.content_filter_high_button
            }
            contentFilterToggleGroup.check(contentFilterButtonId)
            contentFilterToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val contentFilter = when (checkedId) {
                        R.id.content_filter_low_button -> SearchPhotoDataSource.Companion.ContentFilter.LOW
                        else -> SearchPhotoDataSource.Companion.ContentFilter.HIGH
                    }
                    sharedViewModel.contentFilter = contentFilter
                    searchParametersChanged = true
                }
            }

            val items = enumValues<SearchPhotoDataSource.Companion.Color>()
            val titles = items.map { getString(it.titleRes) }
            val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_list, titles)
            val colorFilterDropdownMenu = colorFilterDropdownMenu.editText as? AutoCompleteTextView
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
                SearchPhotoDataSource.Companion.Orientation.ANY -> R.id.orientation_any_button
                SearchPhotoDataSource.Companion.Orientation.PORTRAIT -> R.id.orientation_portrait_button
                SearchPhotoDataSource.Companion.Orientation.LANDSCAPE -> R.id.orientation_landscape_button
                else -> R.id.orientation_square_button
            }
            orientationToggleGroup.check(orientationButtonId)
            orientationToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val orientation = when (checkedId) {
                        R.id.orientation_any_button -> SearchPhotoDataSource.Companion.Orientation.ANY
                        R.id.orientation_portrait_button -> SearchPhotoDataSource.Companion.Orientation.PORTRAIT
                        R.id.orientation_landscape_button -> SearchPhotoDataSource.Companion.Orientation.LANDSCAPE
                        else -> SearchPhotoDataSource.Companion.Orientation.SQUARISH
                    }
                    sharedViewModel.orientation = orientation
                    searchParametersChanged = true
                }
            }

            applyButton.setOnClickListener { dismiss() }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (searchParametersChanged) {
            sharedViewModel.filterPhotoSearch()
        }
    }

    companion object {

        val TAG: String = SearchPhotoFilterBottomSheet::class.java.simpleName

        fun newInstance() = SearchPhotoFilterBottomSheet()
    }
}