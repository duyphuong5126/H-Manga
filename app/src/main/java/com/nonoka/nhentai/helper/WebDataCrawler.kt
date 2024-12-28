package com.nonoka.nhentai.helper

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import timber.log.Timber

class WebDataCrawler(private val connectTimeout: Long) : WebViewClient() {
    private val dataReadyCallbacks = HashMap<String, ArrayList<(String, String) -> Unit>>()
    private val errorCallbacks = HashMap<String, ArrayList<(String, String) -> Unit>>()

    private var requester: ((String) -> Unit)? = null

    private var coroutineScope: CoroutineScope? = null
    private var timeoutJob: Job? = null

    fun initCoroutineScope(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }

    fun clear() {
        dataReadyCallbacks.clear()
        errorCallbacks.clear()
        timeoutJob?.cancel()
        coroutineScope = null
        requester = null
    }

    fun registerRequester(requester: (String) -> Unit) {
        this.requester = requester
    }

    override fun onPageFinished(webview: WebView, url: String) {
        super.onPageFinished(webview, url)

        webview.postVisualStateCallback(
            System.currentTimeMillis(),
            object : WebView.VisualStateCallback() {
                override fun onComplete(requestId: Long) {
                    Timber.d("onComplete of $url")
                    webview.evaluateJavascript(HTML_PARSE_SCRIPT) { rawData ->
                        val cleanData = cleanUpHtmlTags(rawData)
                        try {
                            JsonParser().parse(cleanData)
                            onDataReady(url, cleanData)
                        } catch (error: Throwable) {
                            onError(url, error.message.orEmpty())
                        }
                    }
                }
            },
        )
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        super.onReceivedError(view, request, error)
        onError(request.url.toString(), error.toString())
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        val url = request.url.toString()
        val errorMessage = "Http error: ${errorResponse.statusCode} - ${errorResponse.reasonPhrase}"
        onError(url, errorMessage)
    }

    fun load(
        url: String,
        onDataReady: (String, String) -> Unit,
        onError: (String, String) -> Unit
    ) {
        if (!dataReadyCallbacks.containsKey(url)) {
            dataReadyCallbacks[url] = ArrayList()
        }
        dataReadyCallbacks[url]?.add(onDataReady)
        if (!errorCallbacks.containsKey(url)) {
            errorCallbacks[url] = ArrayList()
        }
        errorCallbacks[url]?.add(onError)
        Timber.d("Test>>> crawler load url=$url")
        requester?.run {
            invoke(url)

            timeoutJob = coroutineScope?.launch(Dispatchers.IO) {
                delay(connectTimeout)
                onError(url, "Timeout")
            }
        }
    }

    private fun onDataReady(url: String, data: String) {
        Timber.d("Test>>> crawler loaded url=$url, data=$data")
        val callbacks = dataReadyCallbacks[url] ?: arrayListOf()
        while (callbacks.isNotEmpty()) {
            val callback = callbacks.removeAt(0)
            callback.invoke(url, data)
        }
    }

    private fun onError(url: String, error: String) {
        Timber.e("Test>>> crawler loaded url=$url, error=$error")
        val callbacks = errorCallbacks[url] ?: arrayListOf()
        while (callbacks.isNotEmpty()) {
            callbacks.removeAt(0).invoke(url, error)
        }
    }

    private fun cleanUpHtmlTags(rawData: String): String {
        return StringEscapeUtils.unescapeJson(
            rawData.replace("\"\\u003Chtml>", "")
                .replace("\\u003C/html>\"", ""),
        )
    }

    companion object {
        private const val HTML_PARSE_SCRIPT =
            "(function() { return ('<html>'+document.getElementsByTagName('body')[0].innerText+'</html>'); })();"
    }
}