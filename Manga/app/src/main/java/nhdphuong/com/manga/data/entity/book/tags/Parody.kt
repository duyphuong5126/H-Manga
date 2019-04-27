package nhdphuong.com.manga.data.entity.book.tags

import android.arch.persistence.room.*
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.io.Serializable

@Entity(tableName = Constants.TABLE_PARODY, indices = [Index(value = [Constants.NAME])])
class Parody(
        @field:SerializedName(Constants.ID)
        @PrimaryKey @ColumnInfo(name = Constants.ID) var tagId: Long,

        @field:SerializedName(Constants.TYPE) @ColumnInfo(name = Constants.TYPE) var type: String,
        @field:SerializedName(Constants.NAME) @ColumnInfo(name = Constants.NAME) var name: String,
        @field:SerializedName(Constants.URL) @ColumnInfo(name = Constants.URL) var url: String,
        @field:SerializedName(Constants.COUNT) @ColumnInfo(name = Constants.COUNT) var count: Long
) : Serializable, ITag {

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

    @Ignore
    override fun id(): Long = tagId

    @Ignore
    override fun type(): String = type

    @Ignore
    override fun name(): String = name

    @Ignore
    override fun url(): String = url

    @Ignore
    override fun count(): Long = count

    override fun toString(): String {
        return "Tag $type - id: $tagId - name: $name"
    }
}
