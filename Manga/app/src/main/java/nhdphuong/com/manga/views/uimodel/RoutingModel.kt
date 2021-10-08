package nhdphuong.com.manga.views.uimodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed interface RoutingModel : Parcelable {
    @Parcelize
    data class BookQuerying(val bookId: String) : RoutingModel

    @Parcelize
    data class Search(val searchInfo: String) : RoutingModel
}
