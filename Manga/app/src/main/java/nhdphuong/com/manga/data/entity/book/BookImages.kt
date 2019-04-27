package nhdphuong.com.manga.data.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 3/24/18.
 */
data class BookImages(
        @field:SerializedName(Constants.PAGES) val pages: List<ImageMeasurements>,
        @field:SerializedName(Constants.COVER) val cover: ImageMeasurements,
        @field:SerializedName(Constants.THUMBNAIL) val thumbnail: ImageMeasurements
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

    companion object CREATOR : Parcelable.Creator<BookImages> {
        val defaultInstance
            get() = BookImages(emptyList(),
                    ImageMeasurements.defaultInstance,
                    ImageMeasurements.defaultInstance
            )

        override fun createFromParcel(parcel: Parcel): BookImages {
            return BookImages(parcel)
        }

        override fun newArray(size: Int): Array<BookImages?> {
            return arrayOfNulls(size)
        }
    }
}
