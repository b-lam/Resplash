package com.b_lam.resplash.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.BottomSheetMainNavigationDrawerBinding
import com.b_lam.resplash.databinding.MainBottomNavigationDrawerProfileContentBinding
import com.b_lam.resplash.databinding.MainBottomNavigationDrawerProfileHeaderBinding
import com.b_lam.resplash.util.loadProfilePicture
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.NavigationMenuView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainBottomNavigationDrawer : BottomSheetDialogFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    private val navigationDrawerBinding:
            BottomSheetMainNavigationDrawerBinding by viewBinding(CreateMethod.INFLATE)

    private val navigationDrawerHeaderBinding:
            MainBottomNavigationDrawerProfileHeaderBinding by viewBinding(R.id.bottom_navigation_header)

    private val navigationDrawerContentBinding: 
            MainBottomNavigationDrawerProfileContentBinding by viewBinding(R.id.bottom_navigation_content)

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
    ): View = navigationDrawerBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigationDrawerBinding.drawerNavigationView.setNavigationItemSelectedListener {
            onNavigationItemSelected(it)
        }
        navigationDrawerContentBinding.headerNavigationView.setNavigationItemSelectedListener {
            onNavigationItemSelected(it)
        }
        (navigationDrawerContentBinding.headerNavigationView.getChildAt(0) as? NavigationMenuView)
            ?.isVerticalScrollBarEnabled = false
        navigationDrawerBinding.expandableProfile.setOnExpandChangeListener { isExpanded ->
            val drawableRes = if (isExpanded) R.drawable.ic_expand_less_18dp else R.drawable.ic_expand_more_18dp
            navigationDrawerHeaderBinding.headerSubtitle
                .setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableRes, 0)
        }

        with(sharedViewModel) {
            resplashProLiveData.observe(viewLifecycleOwner) {
                with(navigationDrawerBinding.drawerNavigationView.menu) {
                    findItem(R.id.action_upgrade).isVisible = !(it?.entitled ?: false)
                    findItem(R.id.action_donate).isVisible = it?.entitled ?: false
                }
            }
            authorizedLiveData.observe(viewLifecycleOwner) {
                with(navigationDrawerContentBinding.headerNavigationView.menu) {
                    setGroupVisible(R.id.group_unauthorized, !it)
                    setGroupVisible(R.id.group_authorized, it)
                }
            }
            usernameLiveData.observe(viewLifecycleOwner) {
                navigationDrawerHeaderBinding.headerTitle.text = it ?: getString(R.string.app_name)
            }
            emailLiveData.observe(viewLifecycleOwner) {
                navigationDrawerHeaderBinding.headerSubtitle.text = it ?: getString(R.string.header_subtitle)
            }
            profilePictureLiveData.observe(viewLifecycleOwner) { url ->
                url?.let {
                    navigationDrawerHeaderBinding.headerImageView.loadProfilePicture(it)
                } ?: run {
                    navigationDrawerHeaderBinding.headerImageView.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
        }
    }

    private fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        sharedViewModel.onNavigationItemSelected(menuItem.itemId)
        return true
    }

    companion object {

        val TAG: String = MainBottomNavigationDrawer::class.java.simpleName

        fun newInstance() = MainBottomNavigationDrawer()
    }
}
