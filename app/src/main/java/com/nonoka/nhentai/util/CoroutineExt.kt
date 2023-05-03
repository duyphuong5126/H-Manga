package com.nonoka.nhentai.util

import kotlinx.coroutines.delay

suspend fun delayWhile(checker: () -> Boolean, interval: Long): Boolean {
    return if (checker.invoke()) {
        delay(interval)
        delayWhile(checker, interval)
    } else {
        true
    }
}