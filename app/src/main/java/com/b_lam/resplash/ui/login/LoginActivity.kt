package com.b_lam.resplash.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.login.LoginRepository.Companion.unsplashAuthCallback
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.customtabs.CustomTabsHelper
import com.b_lam.resplash.util.loadBlurredImage
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.toast
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity() {

    override val viewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.add_account)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.bannerPhotoLiveData.observe(this) {
            banner_image_view.loadBlurredImage(it.urls.small, it.color)
        }

        join_button.setOnClickListener { openUnsplashJoinTab() }
        login_button.setOnClickListener { openUnsplashLoginTab() }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { uri ->
            if (uri.authority.equals(unsplashAuthCallback)) {
                uri.getQueryParameter("code")?.let { code ->
                    viewModel.getAccessToken(code).observe(this) {
                        when (it) {
                            is Result.Loading -> {
                                content_loading_layout.isVisible = true
                            }
                            is Result.Success -> {
                                toast(R.string.login_success)
                                finish()
                            }
                            is Result.Error, Result.NetworkError -> {
                                toast(R.string.oops)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openUnsplashJoinTab() = openCustomTab(getString(R.string.unsplash_join_url))

    private fun openUnsplashLoginTab() = openCustomTab(viewModel.loginUrl)

    private fun openCustomTab(uriString: String) {
        CustomTabsHelper.openCustomTab(this, Uri.parse(uriString), sharedPreferencesRepository.theme)
    }
}
