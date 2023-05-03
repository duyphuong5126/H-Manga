package com.nonoka.nhentai.feature.bypass_security

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nonoka.nhentai.databinding.ActivityByPassingBinding
import com.nonoka.nhentai.feature.MainActivity
import com.nonoka.nhentai.helper.WebDataParsingClient
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.byPassingResult.collect { result ->
                    Timber.tag("Test>>>").d("result=$result")
                    viewBinding.message.text = result.label
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

        val priceExtractionWebViewClient =
            WebDataParsingClient(onDataReady = { url, data ->
                Timber.tag("Test>>>").e("url=$url\ndata=$data")
                viewModel.validateData(data)
            }, onError = { url, errorMessage ->
                Timber.tag("Test>>>").e("url=$url\nerrorMessage=$errorMessage")
            }, lifecycleScope)

        viewBinding.webView.webViewClient = priceExtractionWebViewClient
        viewBinding.webView.loadUrl("https://nhentai.net/api/galleries/all?page=1")
    }
}