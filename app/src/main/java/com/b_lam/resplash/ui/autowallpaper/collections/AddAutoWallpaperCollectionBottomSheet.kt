package com.b_lam.resplash.ui.autowallpaper.collections

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
import com.b_lam.resplash.databinding.BottomSheetAddAutoWallpaperCollectionBinding
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddAutoWallpaperCollectionBottomSheet : BottomSheetDialogFragment() {

    private val sharedViewModel: AutoWallpaperCollectionViewModel by sharedViewModel()

    private val binding: BottomSheetAddAutoWallpaperCollectionBinding by viewBinding(CreateMethod.INFLATE)

    private val urlRegex = """https://unsplash.com/collections/(\w+).*""".toRegex()

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
            collectionUrlTextInputLayout.editText?.doOnTextChanged { url, _, _, _ ->
                val isValid = isCollectionUrlValid(url.toString())
                addCollectionButton.isEnabled = isValid
                collectionUrlTextInputLayout.error =
                    if (url.isNullOrBlank() || isValid) null else getString(R.string.auto_wallpaper_invalid_url)
            }

            addCollectionButton.setOnClickListener {
                progressBar.isVisible = true
                addCollectionButton.isEnabled = false
                val id = extractCollectionIdFromUrl(collectionUrlTextInputLayout.editText?.text.toString())
                id?.let { sharedViewModel.getCollectionDetailsAndAdd(id) }
            }
        }

        sharedViewModel.addCollectionResultLiveData.observeEvent(viewLifecycleOwner) {
            if (it !is Result.Success) { context?.toast(R.string.auto_wallpaper_could_not_add_collection) }
            dismiss()
        }
    }

    private fun isCollectionUrlValid(url: String) = urlRegex matches url

    private fun extractCollectionIdFromUrl(url: String): String? {
        return urlRegex.find(url)?.destructured?.let { (id) -> id }
    }

    companion object {

        val TAG: String = AddAutoWallpaperCollectionBottomSheet::class.java.simpleName

        fun newInstance() = AddAutoWallpaperCollectionBottomSheet()
    }
}
