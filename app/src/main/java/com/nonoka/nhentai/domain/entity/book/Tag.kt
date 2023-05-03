package com.nonoka.nhentai.domain.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.COUNT
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.domain.entity.NAME
import com.nonoka.nhentai.domain.entity.TAG
import com.nonoka.nhentai.domain.entity.TYPE
import com.nonoka.nhentai.domain.entity.URL

data class Tag(
    @field:SerializedName(ID) val id: Long,

    @field:SerializedName(TYPE) val type: String,
    @field:SerializedName(NAME) val name: String,
    @field:SerializedName(URL) val url: String,
    @field:SerializedName(COUNT) val count: Long
) : Parcelable {

    val jsonValue: JsonObject
        get() {
            val jsonObject = JsonObject()
            jsonObject.addProperty(ID, id)
            jsonObject.addProperty(TYPE, type)
            jsonObject.addProperty(NAME, name)
            jsonObject.addProperty(URL, url)
            jsonObject.addProperty(COUNT, count)
            return jsonObject
        }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: TAG,
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

    companion object CREATOR : Parcelable.Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }
}
