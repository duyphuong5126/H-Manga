package nhdphuong.com.manga.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import nhdphuong.com.manga.data.adapter.JsonIntAdapter
import nhdphuong.com.manga.data.adapter.JsonLongAdapter
import nhdphuong.com.manga.data.adapter.JsonStringAdapter

class SerializationServiceImpl : SerializationService {
    override fun <T : Any> serialize(data: T): String {
        return Gson().toJson(data)
    }

    override fun <T : Any> deserialize(json: String, targetClass: Class<T>): T {
        val gSon = GsonBuilder()
            .registerTypeAdapter(Int::class.java, JsonIntAdapter)
            .registerTypeAdapter(Long::class.java, JsonLongAdapter)
            .registerTypeAdapter(String::class.java, JsonStringAdapter)
            .create()
        return gSon.fromJson(json, targetClass)
    }
}