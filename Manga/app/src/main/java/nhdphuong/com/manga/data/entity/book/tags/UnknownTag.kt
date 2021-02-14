package nhdphuong.com.manga.data.entity.book.tags

import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

@Entity(tableName = Constants.TABLE_UNKNOWN, indices = [Index(value = [Constants.NAME])])
data class UnknownTag(
    @field:SerializedName(Constants.ID)
    @PrimaryKey @ColumnInfo(name = Constants.ID) override val id: Long,

    @field:SerializedName(Constants.TYPE) @ColumnInfo(name = Constants.TYPE) override val type: String,
    @field:SerializedName(Constants.NAME) @ColumnInfo(name = Constants.NAME) override val name: String,
    @field:SerializedName(Constants.URL) @ColumnInfo(name = Constants.URL) override val url: String,
    @field:SerializedName(Constants.COUNT) @ColumnInfo(name = Constants.COUNT) override val count: Long
) : Parcelable, ITag {

    @Suppress("unused")
    val jsonValue: JsonObject
        get() {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constants.ID, id)
            jsonObject.addProperty(Constants.TYPE, type)
            jsonObject.addProperty(Constants.NAME, name)
            jsonObject.addProperty(Constants.URL, url)
            jsonObject.addProperty(Constants.COUNT, count)
            return jsonObject
        }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readLong()
    )

    override fun toString(): String {
        return "Tag $type - id: $id - name: $name"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(type)
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeLong(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UnknownTag> {
        override fun createFromParcel(parcel: Parcel): UnknownTag {
            return UnknownTag(parcel)
        }

        override fun newArray(size: Int): Array<UnknownTag?> {
            return arrayOfNulls(size)
        }
    }
}
