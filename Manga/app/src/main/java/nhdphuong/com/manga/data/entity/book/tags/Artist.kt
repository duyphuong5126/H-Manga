package nhdphuong.com.manga.data.entity.book.tags

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.io.Serializable

@Entity(tableName = Constants.TABLE_ARTIST, indices = [Index(value = [Constants.NAME])])
class Artist(@field:SerializedName(Constants.ID) @PrimaryKey @ColumnInfo(name = Constants.ID) var tagId: Long,
             @field:SerializedName(Constants.TYPE) @ColumnInfo(name = Constants.TYPE) var type: String,
             @field:SerializedName(Constants.NAME) @ColumnInfo(name = Constants.NAME) var name: String,
             @field:SerializedName(Constants.URL) @ColumnInfo(name = Constants.URL) var url: String,
             @field:SerializedName(Constants.COUNT) @ColumnInfo(name = Constants.COUNT) var count: Long) : Serializable {

    val jsonValue: JsonObject
        get() {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constants.ID, tagId)
            jsonObject.addProperty(Constants.TYPE, type)
            jsonObject.addProperty(Constants.NAME, name)
            jsonObject.addProperty(Constants.URL, url)
            jsonObject.addProperty(Constants.COUNT, count)
            return jsonObject
        }

    override fun toString(): String {
        return "Tag $type - id: $tagId - name: $name"
    }
}