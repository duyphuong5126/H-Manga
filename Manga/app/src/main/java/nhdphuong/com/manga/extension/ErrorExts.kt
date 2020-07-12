package nhdphuong.com.manga.extension

import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

fun Throwable.isNetworkError(): Boolean {
    return this is NoRouteToHostException || cause is NoRouteToHostException ||
            this is UnknownHostException || cause is UnknownHostException ||
            this is ConnectException || cause is ConnectException
}
