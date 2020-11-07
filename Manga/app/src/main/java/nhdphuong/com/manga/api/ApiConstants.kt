package nhdphuong.com.manga.api

/*
 * Created by nhdphuong on 3/24/18.
 */
object ApiConstants {
    const val NHENTAI_DB = "https://raw.githubusercontent.com/duyphuong5126/NHentaiDB/master"
    const val NHENTAI_HOME = "https://nhentai.net"
    private const val NHENTAI_I = "https://i.nhentai.net"
    private const val NHENTAI_T = "https://t.nhentai.net"

    private fun getThumbnailUrl(mediaId: String): String = "$NHENTAI_T/galleries/$mediaId"

    fun getBookThumbnailById(
        mediaId: String,
        imageType: String
    ): String = "$NHENTAI_T/galleries/$mediaId/thumb$imageType"

    fun getBookCover(mediaId: String): String = "${getThumbnailUrl(mediaId)}/cover.jpg"

    fun getThumbnailByPage(
        mediaId: String,
        pageNumber: Int,
        imageType: String
    ): String = "${getThumbnailUrl(mediaId)}/${pageNumber}t.$imageType"

    private fun getGalleryUrl(mediaId: String): String = "$NHENTAI_I/galleries/$mediaId"

    fun getPictureUrl(
        mediaId: String,
        pageNumber: Int,
        imageType: String
    ): String = "${getGalleryUrl(mediaId)}/$pageNumber.$imageType"

    fun getSharablePageUrl(bookId: String, pageNumber: Int): String =
        "$NHENTAI_HOME/g/$bookId/$pageNumber/"

    fun getCommentPosterAvatarUrl(avatarUrl: String): String {
        return "$NHENTAI_I/$avatarUrl"
    }
}
