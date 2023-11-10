package com.nonoka.nhentai.util

import android.content.Context
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.Resource.Success
import com.nonoka.nhentai.domain.Resource.Error
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

const val imageFolderTemplate = "images/%s"

interface FileService {
    fun getDoujinImageLocalPath(doujinshiId: String, imageName: String): File

    suspend fun deleteDoujinshiFolder(doujinshiId: String): Resource<Boolean>
}

class FileServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileService {

    override fun getDoujinImageLocalPath(doujinshiId: String, imageName: String): File {
        val directory = File(context.filesDir, String.format(imageFolderTemplate, doujinshiId))
        return File(directory, imageName)
    }

    override suspend fun deleteDoujinshiFolder(doujinshiId: String): Resource<Boolean> {
        val directory = File(context.filesDir, String.format(imageFolderTemplate, doujinshiId))
        return try {
            Success(directory.deleteRecursively())
        } catch (error: Throwable) {
            Error(error)
        }
    }
}