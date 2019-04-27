package nhdphuong.com.manga.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/*
 * Created by nhdphuong on 3/24/18.
 */
class GsonUTCDateAdapter : JsonDeserializer<Date> {

    private val dateFormat: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    init {
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): Date {
        return try {
            dateFormat.parse(json.asString)
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }

    }
}
