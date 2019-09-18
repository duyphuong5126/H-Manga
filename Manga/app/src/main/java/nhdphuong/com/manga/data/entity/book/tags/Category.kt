package nhdphuong.com.manga.data.entity.book.tags

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

@Entity(tableName = Constants.TABLE_CATEGORY, indices = [Index(value = [Constants.NAME])])
data class Category(
        @field:SerializedName(Constants.ID)
        @PrimaryKey @ColumnInfo(name = Constants.ID)
        var tagId: Long,

        @field:SerializedName(Constants.TYPE) @ColumnInfo(name = Constants.TYPE) var type: String,
        @field:SerializedName(Constants.NAME) @ColumnInfo(name = Constants.NAME) var name: String,
        @field:SerializedName(Constants.URL) @ColumnInfo(name = Constants.URL) var url: String,
        @field:SerializedName(Constants.COUNT) @ColumnInfo(name = Constants.COUNT) var count: Long
) : Parcelable, ITag {

    @Suppress("unused")
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

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: Constants.CATEGORY,
            parcel.readString().orEmpty(),
            parcel.readString().orEmpty(),
            parcel.readLong()
    )

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(tagId)
        parcel.writeString(type)
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeLong(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}
