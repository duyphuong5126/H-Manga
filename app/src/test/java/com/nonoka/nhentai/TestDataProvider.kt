package com.nonoka.nhentai

import com.nonoka.nhentai.domain.entity.PNG_TYPE
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshiImages
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshiTitle
import com.nonoka.nhentai.domain.entity.doujinshi.ImageMeasurements
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


object TestDataProvider {
    @Throws(Throwable::class)
    private fun open(filename: String): FileInputStream {
        val classLoader = javaClass.classLoader
        val resource = classLoader!!.getResource(filename)
        val file = File(resource.path)
        return FileInputStream(file)
    }

    fun providesGalleryPageJson(): String {
        val inputStreamReader = InputStreamReader(open("gallery_page_result.json"))
        val bufferedReader = BufferedReader(inputStreamReader)
        val sb = StringBuilder()
        var s = ""
        while (!bufferedReader.readLine()?.also { s = it }.isNullOrBlank()) {
            sb.append(s)
        }
        return sb.toString()
    }

    fun providesDoujinshiList(count: Int): List<Doujinshi> {
        return mutableListOf<Doujinshi>().apply {
            for (i in 0 until count) {
                add(providesDoujinshi("Test_${System.currentTimeMillis()}_$i"))
            }
        }
    }

    fun providesDoujinshi(id: String): Doujinshi {
        val image = ImageMeasurements(type = PNG_TYPE, width = 100, height = 200)
        return Doujinshi(
            id = id,
            mediaId = "",
            title = DoujinshiTitle(englishName = "", japaneseName = "", prettyName = ""),
            scanlator = "",
            images = DoujinshiImages(pages = emptyList(), cover = image, thumbnail = image),
            numOfFavorites = 0,
            numOfPages = 0,
            tags = emptyList(),
            updateAt = System.currentTimeMillis()
        )
    }
}