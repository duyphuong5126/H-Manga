package nhdphuong.com.manga.data

interface SerializationService {
    fun <T : Any> serialize(data: T): String
    fun <T : Any> deserialize(json: String, targetClass: Class<T>): T
}