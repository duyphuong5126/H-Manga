package com.nonoka.nhentai.domain.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.TITLE_ENG
import com.nonoka.nhentai.domain.entity.TITLE_JAPANESE
import com.nonoka.nhentai.domain.entity.TITLE_PRETTY

data class DoujinshiTitle(
    @field:SerializedName(TITLE_ENG) val englishName: String?,
    @field:SerializedName(TITLE_JAPANESE) val japaneseName: String?,
    @field:SerializedName(TITLE_PRETTY) val prettyName: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(englishName)
        parcel.writeString(japaneseName)
        parcel.writeString(prettyName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DoujinshiTitle> {
        val defaultInstance
            get() = DoujinshiTitle("", "", "")

        override fun createFromParcel(parcel: Parcel): DoujinshiTitle {
            return DoujinshiTitle(parcel)
        }

        override fun newArray(size: Int): Array<DoujinshiTitle?> {
            return arrayOfNulls(size)
        }
    }
}
