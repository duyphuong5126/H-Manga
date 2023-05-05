package com.nonoka.nhentai.helper

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import org.apache.commons.text.StringEscapeUtils
import timber.log.Timber

class WebDataCrawler : WebViewClient() {
    private val dataReadyCallbacks = HashMap<String, ArrayList<(String, String) -> Unit>>()
    private val errorCallbacks = HashMap<String, ArrayList<(String, String) -> Unit>>()

    private var requester: ((String) -> Unit)? = null

    fun registerRequester(requester: (String) -> Unit) {
        this.requester = requester
    }

    fun clearRequester() {
        requester = null
    }

    override fun onPageFinished(webview: WebView, url: String) {
        super.onPageFinished(webview, url)

        webview.postVisualStateCallback(
            System.currentTimeMillis(),
            object : WebView.VisualStateCallback() {
                override fun onComplete(requestId: Long) {
                    Timber.d("onComplete of $url")
                    webview.evaluateJavascript(HTML_PARSE_SCRIPT) { rawData ->
                        onDataReady(url, cleanUpJson(rawData))
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
        requester?.invoke(url)
    }

    private fun onDataReady(url: String, data: String) {
        val callbacks = dataReadyCallbacks[url] ?: arrayListOf()
        while (callbacks.isNotEmpty()) {
            callbacks.removeFirst().invoke(url, data)
        }
        errorCallbacks[url]?.clear()
    }

    private fun onError(url: String, error: String) {
        val callbacks = errorCallbacks[url] ?: arrayListOf()
        while (callbacks.isNotEmpty()) {
            callbacks.removeFirst().invoke(url, error)
        }
        dataReadyCallbacks[url]?.clear()
    }

    private fun cleanUpJson(rawData: String): String {
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