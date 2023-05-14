package com.nonoka.nhentai.domain.common.extension

import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.isNetworkError(): Boolean {
    return this is UnknownHostException || this is ConnectException || this is NoRouteToHostException
}

fun Throwable.isTimeoutError(): Boolean {
    return this is SocketTimeoutException
}