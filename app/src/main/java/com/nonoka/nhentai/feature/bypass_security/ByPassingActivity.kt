package com.nonoka.nhentai.feature.bypass_security

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import com.nonoka.nhentai.R
import com.nonoka.nhentai.databinding.ActivityByPassingBinding
import com.nonoka.nhentai.feature.MainActivity
import com.nonoka.nhentai.helper.ClientType
import com.nonoka.nhentai.helper.crawlerMap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
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
            crawlerMap[ClientType.ByPassing]?.load(
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
            viewModel.onRetry()
        }

        val imageViewTarget = DrawableImageViewTarget(viewBinding.loadingImage)
        Glide.with(viewBinding.loadingImage.context)
            .load(R.drawable.ic_loading_cat_transparent)
            .into<Target<Drawable>>(imageViewTarget)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.byPassingResult.collect { result ->
                    Timber.d("result=$result")
                    if (result == ByPassingResult.Success) {
                        MainActivity.start(this@ByPassingActivity)
                        finish()
                        return@collect
                    }
                    val isLoading =
                        result == ByPassingResult.Loading || result == ByPassingResult.Processing
                    viewBinding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE

                    val isFailed = result == ByPassingResult.Failure
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = if (isFailed) getColor(R.color.grey24) else Color.WHITE
                    WindowCompat.getInsetsController(
                        window,
                        window.decorView
                    ).isAppearanceLightStatusBars = !isFailed
                    viewBinding.refresher.isEnabled = !isLoading
                    viewBinding.loadingArea.visibility = if (isLoading) View.VISIBLE else View.GONE
                    viewBinding.errorArea.visibility = if (isFailed) View.VISIBLE else View.GONE
                    viewBinding.errorText.text =
                        if (isFailed) getString(R.string.general_failure_message) else ""
                }
            }
        }

        viewBinding.webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        val priceExtractionWebViewClient = crawlerMap[ClientType.ByPassing]!!
        priceExtractionWebViewClient.initCoroutineScope(lifecycleScope)
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

    override fun onDestroy() {
        super.onDestroy()
        crawlerMap[ClientType.ByPassing]!!.clear()
    }
}