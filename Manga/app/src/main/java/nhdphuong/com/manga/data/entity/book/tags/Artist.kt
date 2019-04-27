package nhdphuong.com.manga.data.entity.book.tags

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

@Entity(tableName = Constants.TABLE_ARTIST, indices = [Index(value = [Constants.NAME])])
data class Artist(
        @field:SerializedName(Constants.ID)
        @PrimaryKey @ColumnInfo(name = Constants.ID)
        var tagId: Long,

        @field:SerializedName(Constants.TYPE) @ColumnInfo(name = Constants.TYPE) var type: String,
        @field:SerializedName(Constants.NAME) @ColumnInfo(name = Constants.NAME) var name: String,
        @field:SerializedName(Constants.URL) @ColumnInfo(name = Constants.URL) var url: String,
        @field:SerializedName(Constants.COUNT) @ColumnInfo(name = Constants.COUNT) var count: Long
) : Parcelable, ITag {

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
            parcel.readString() ?: Constants.ARTIST,
            parcel.readString() ?: "",
            parcel.readString() ?: "",
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

    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist {
            return Artist(parcel)
        }

        override fun newArray(size: Int): Array<Artist?> {
            return arrayOfNulls(size)
        }
    }
}
