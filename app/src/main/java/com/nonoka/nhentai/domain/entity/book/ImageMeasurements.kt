package com.nonoka.nhentai.domain.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.IMAGE_HEIGHT
import com.nonoka.nhentai.domain.entity.IMAGE_TYPE
import com.nonoka.nhentai.domain.entity.IMAGE_WIDTH
import com.nonoka.nhentai.domain.entity.JPG
import com.nonoka.nhentai.domain.entity.JPG_TYPE
import com.nonoka.nhentai.domain.entity.PNG
import com.nonoka.nhentai.domain.entity.PNG_TYPE

data class ImageMeasurements(
    @field:SerializedName(IMAGE_TYPE) private val type: String,
    @field:SerializedName(IMAGE_WIDTH) val width: Int,
    @field:SerializedName(IMAGE_HEIGHT) val height: Int
) : Parcelable {
    val imageType: String
        get() = if (PNG_TYPE.equals(type, ignoreCase = true)) {
            PNG
        } else {
            JPG
        }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: JPG_TYPE,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageMeasurements> {
        val defaultInstance
            get() = ImageMeasurements(JPG_TYPE, 0, 0)

        override fun createFromParcel(parcel: Parcel): ImageMeasurements {
            return ImageMeasurements(parcel)
        }

        override fun newArray(size: Int): Array<ImageMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
