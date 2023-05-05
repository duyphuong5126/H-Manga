package com.nonoka.nhentai.feature.bypass_security

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nonoka.nhentai.databinding.ActivityByPassingBinding
import com.nonoka.nhentai.feature.MainActivity
import com.nonoka.nhentai.helper.WebDataCrawler
import kotlinx.coroutines.launch
import timber.log.Timber

class ByPassingActivity : ComponentActivity() {
    private val viewModel by viewModels<ByPassingSecurityViewModel>()

    private lateinit var viewBinding: ActivityByPassingBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityByPassingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.refresher.setOnRefreshListener {
            viewBinding.refresher.isRefreshing = false
            viewBinding.webView.loadUrl("https://nhentai.net/api/galleries/all?page=1")
            viewModel.onRetry()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.byPassingResult.collect { result ->
                    Timber.d("result=$result")
                    viewBinding.message.text = result.label
                    val isLoading =
                        result == ByPassingResult.Loading || result == ByPassingResult.Processing
                    viewBinding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE

                    val isFailed = result == ByPassingResult.Failure
                    viewBinding.loadingArea.visibility = if (isFailed) View.GONE else View.VISIBLE

                    if (result == ByPassingResult.Success) {
                        MainActivity.start(this@ByPassingActivity)
                    }
                }
            }
        }

        viewBinding.webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        val priceExtractionWebViewClient = WebDataCrawler()
        priceExtractionWebViewClient.registerRequester(viewBinding.webView::loadUrl)

        viewBinding.webView.webViewClient = priceExtractionWebViewClient
        priceExtractionWebViewClient.load(
            "https://nhentai.net/api/galleries/all?page=1",
            onDataReady = { url, data ->
                Timber.d("url=$url\ndata=$data")
                viewModel.validateData(data)
            },
            onError = { url, errorMessage ->
                Timber.e("url=$url\nerrorMessage=$errorMessage")
                viewModel.onError(errorMessage)
            },
        )

    }
}