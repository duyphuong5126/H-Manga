package com.nonoka.nhentai.domain.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.COVER
import com.nonoka.nhentai.domain.entity.PAGES
import com.nonoka.nhentai.domain.entity.THUMBNAIL

data class DoujinshiImages(
    @field:SerializedName(PAGES) val pages: List<ImageMeasurements>,
    @field:SerializedName(COVER) val cover: ImageMeasurements,
    @field:SerializedName(THUMBNAIL) val thumbnail: ImageMeasurements
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(ImageMeasurements) ?: emptyList(),
        parcel.readParcelable(
            ImageMeasurements::class.java.classLoader
        ) ?: ImageMeasurements.defaultInstance,
        parcel.readParcelable(
            ImageMeasurements::class.java.classLoader
        ) ?: ImageMeasurements.defaultInstance
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(pages)
        parcel.writeParcelable(cover, flags)
        parcel.writeParcelable(thumbnail, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DoujinshiImages> {
        val defaultInstance
            get() = DoujinshiImages(
                emptyList(),
                ImageMeasurements.defaultInstance,
                ImageMeasurements.defaultInstance
            )

        override fun createFromParcel(parcel: Parcel): DoujinshiImages {
            return DoujinshiImages(parcel)
        }

        override fun newArray(size: Int): Array<DoujinshiImages?> {
            return arrayOfNulls(size)
        }
    }
}
