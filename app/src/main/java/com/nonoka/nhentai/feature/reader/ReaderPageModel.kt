package com.nonoka.nhentai.feature.reader

import java.io.File

sealed class ReaderPageModel {
    abstract val width: Int
    abstract val height: Int

    data class RemotePage(
        val url: String,
        override val width: Int,
        override val height: Int
    ) : ReaderPageModel()

    data class LocalPage(
        val file: File,
        override val width: Int,
        override val height: Int
    ) : ReaderPageModel()
}
