package com.nonoka.nhentai.helper

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import timber.log.Timber

class WebDataParsingClient(
    private val onDataReady: (String, String) -> Unit,
    private val onError: (String, String) -> Unit,
    private val coroutineScope: CoroutineScope,
) : WebViewClient() {
    override fun onPageFinished(webview: WebView, url: String) {
        super.onPageFinished(webview, url)

        webview.postVisualStateCallback(
            System.currentTimeMillis(),
            object : WebView.VisualStateCallback() {
                override fun onComplete(requestId: Long) {
                    Timber.tag("Test>>>").d("onComplete of $url")
                    coroutineScope.launch {
                        delay(20000)
                        webview.evaluateJavascript(HTML_PARSE_SCRIPT) { rawData ->
                            onDataReady(url, cleanUpJson(rawData))
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
        onError(
            request.url.toString(),
            "Http error: ${errorResponse.statusCode} - ${errorResponse.reasonPhrase}"
        )
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