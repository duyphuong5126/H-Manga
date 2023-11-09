package com.nonoka.nhentai.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

const val imageFolderTemplate = "images/%s"

interface FileService {
    fun getDoujinImageLocalPath(doujinshiId: String, imageName: String): File
}

class FileServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileService {
    override fun getDoujinImageLocalPath(doujinshiId: String, imageName: String): File {
        val directory = File(context.filesDir, String.format(imageFolderTemplate, doujinshiId))
        return File(directory, imageName)
    }


}