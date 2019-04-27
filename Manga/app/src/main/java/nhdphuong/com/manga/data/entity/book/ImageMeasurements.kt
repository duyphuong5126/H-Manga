package nhdphuong.com.manga.data.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 3/24/18.
 */
data class ImageMeasurements(
        @field:SerializedName(Constants.IMAGE_TYPE) private val type: String,
        @field:SerializedName(Constants.IMAGE_WIDTH) val width: Int,
        @field:SerializedName(Constants.IMAGE_HEIGHT) val height: Int
) : Parcelable {
    val imageType: String
        get() = if (Constants.PNG_TYPE.equals(type, ignoreCase = true)) {
            Constants.PNG
        } else {
            Constants.JPG
        }

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: Constants.JPG_TYPE,
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
            get() = ImageMeasurements(Constants.JPG_TYPE, 0, 0)

        override fun createFromParcel(parcel: Parcel): ImageMeasurements {
            return ImageMeasurements(parcel)
        }

        override fun newArray(size: Int): Array<ImageMeasurements?> {
            return arrayOfNulls(size)
        }
    }
}
