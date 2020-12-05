package nhdphuong.com.manga.api

import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain

/*
 * Created by nhdphuong on 3/24/18.
 */
object ApiConstants {
    const val NHENTAI_DB = "https://raw.githubusercontent.com/duyphuong5126/NHentaiDB/master"
    const val NHENTAI_HOME = "https://nhentai.net"
    private const val NHENTAI_I = "https://i.nhentai.net"
    private const val NHENTAI_T = "https://t.nhentai.net"

    var alternativeDomain: AlternativeDomain? = null

    private val useAlternativeDomain: Boolean get() = alternativeDomain != null

    val homeUrl: String
        get() {
            return if (useAlternativeDomain) {
                alternativeDomain?.homeUrl ?: NHENTAI_HOME
            } else NHENTAI_HOME
        }

    private val imageUrl: String
        get() {
            return if (useAlternativeDomain) {
                alternativeDomain?.imageUrl ?: NHENTAI_I
            } else NHENTAI_I
        }

    private val thumbnailUrl: String
        get() {
            return if (useAlternativeDomain) {
                alternativeDomain?.thumbnailUrl ?: NHENTAI_T
            } else NHENTAI_T
        }

    private fun getThumbnailUrl(mediaId: String): String = "$thumbnailUrl/galleries/$mediaId"

    fun getBookThumbnailById(
        mediaId: String,
        imageType: String
    ): String = "$thumbnailUrl/galleries/$mediaId/thumb$imageType"

    fun getBookCover(mediaId: String): String = "${getThumbnailUrl(mediaId)}/cover.jpg"

    fun getThumbnailByPage(
        mediaId: String,
        pageNumber: Int,
        imageType: String
    ): String = "${getThumbnailUrl(mediaId)}/${pageNumber}t.$imageType"

    private fun getGalleryUrl(mediaId: String): String = "$imageUrl/galleries/$mediaId"

    fun getPictureUrl(
        mediaId: String,
        pageNumber: Int,
        imageType: String
    ): String = "${getGalleryUrl(mediaId)}/$pageNumber.$imageType"

    fun getSharablePageUrl(bookId: String, pageNumber: Int): String =
        "$homeUrl/g/$bookId/$pageNumber/"

    fun getCommentPosterAvatarUrl(avatarUrl: String): String {
        return "$imageUrl/$avatarUrl"
    }
}
