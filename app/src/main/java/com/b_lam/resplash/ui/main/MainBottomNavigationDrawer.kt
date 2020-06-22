package com.b_lam.resplash.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.util.loadProfilePicture
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.NavigationMenuView
import kotlinx.android.synthetic.main.main_bottom_navigation_drawer_layout.*
import kotlinx.android.synthetic.main.main_bottom_navigation_drawer_profile_content.*
import kotlinx.android.synthetic.main.main_bottom_navigation_drawer_profile_header.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainBottomNavigationDrawer : BottomSheetDialogFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

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
    ): View {
        return inflater.inflate(R.layout.main_bottom_navigation_drawer_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawer_navigation_view.setNavigationItemSelectedListener { onNavigationItemSelected(it) }
        header_navigation_view.setNavigationItemSelectedListener { onNavigationItemSelected(it) }
        (header_navigation_view.getChildAt(0) as? NavigationMenuView)?.isVerticalScrollBarEnabled = false
        expandable_profile.setOnExpandChangeListener { isExpanded ->
            val drawableRes = if (isExpanded) R.drawable.ic_expand_less_18dp else R.drawable.ic_expand_more_18dp
            header_subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableRes, 0)
        }

        with(sharedViewModel) {
            resplashProLiveData.observe(viewLifecycleOwner) {
                with(drawer_navigation_view.menu) {
                    findItem(R.id.action_upgrade).isVisible = !(it?.entitled ?: false)
                    findItem(R.id.action_donate).isVisible = it?.entitled ?: false
                }
            }
            authorizedLiveData.observe(viewLifecycleOwner) {
                with(header_navigation_view.menu) {
                    setGroupVisible(R.id.group_unauthorized, !it)
                    setGroupVisible(R.id.group_authorized, it)
                }
            }
            usernameLiveData.observe(viewLifecycleOwner) {
                header_title.text = it ?: getString(R.string.app_name)
            }
            emailLiveData.observe(viewLifecycleOwner) {
                header_subtitle.text = it ?: getString(R.string.header_subtitle)
            }
            profilePictureLiveData.observe(viewLifecycleOwner) { url ->
                url?.let {
                    header_image_view.loadProfilePicture(it)
                } ?: run {
                    header_image_view.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
        }
    }

    private fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        sharedViewModel.onNavigationItemSelected(menuItem.itemId)
        return true
    }

    companion object {

        val TAG = MainBottomNavigationDrawer::class.java.simpleName

        fun newInstance() = MainBottomNavigationDrawer()
    }
}
