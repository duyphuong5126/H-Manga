package nhdphuong.com.manga.data

import com.google.gson.Gson

class SerializationServiceImpl : SerializationService {
    override fun <T : Any> serialize(data: T): String {
        return Gson().toJson(data)
    }

    override fun <T : Any> deserialize(json: String, targetClass: Class<T>): T {
        return Gson().fromJson(json, targetClass)
    }
}