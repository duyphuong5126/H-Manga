package nhdphuong.com.manga.data.adapter

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import nhdphuong.com.manga.Logger
import java.lang.reflect.Type

object JsonStringAdapter : JsonDeserializer<String> {
    private val firebaseCrashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    private val logger: Logger by lazy {
        Logger("JsonStringAdapter")
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String {
        return try {
            val result = json?.asString
            if (result == null) {
                logInvalidJson(NullPointerException(), json)
            }
            result ?: "n/a"
        } catch (throwable: Throwable) {
            logInvalidJson(throwable, json)
            "n/a"
        }
    }

    private fun logInvalidJson(error: Throwable, json: JsonElement?) {
        try {
            firebaseCrashlytics.setCustomKey("invalid_json", json.toString())
            firebaseCrashlytics.recordException(error)
        } catch (throwable: Throwable) {
            logger.e("Could not log invalid json with error $throwable")
        }
    }
}