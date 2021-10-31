package com.b_lam.resplash.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivityWebviewBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.error
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.toast

class WebViewActivity : BaseActivity(R.layout.activity_webview) {

    override val viewModel: ViewModel? = null

    override val binding: ActivityWebviewBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val url = intent.getStringExtra(EXTRA_URL)

        if (url == null) {
            toast(R.string.oops)
            finish()
        } else {
            setupActionBar(R.id.toolbar) {
                title = url
                setDisplayHomeAsUpEnabled(true)
            }
            setupWebView()
            binding.webview.loadUrl(url)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                return if (url.startsWith("http://") || url.startsWith("https://")) {
                    false
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        view.context.startActivity(intent)
                        true
                    } catch (e: Exception) {
                        error("shouldOverrideUrlLoading Exception", e)
                        true
                    }
                }

            }
        }
        binding.webview.settings.javaScriptEnabled = true
    }

    companion object {

        private const val EXTRA_URL = "extra_url"

        fun createIntent(context: Context, uri: Uri) =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, uri.toString())
            }
    }
}