package nhdphuong.com.manga.data.entity.book.tags

interface ITag {
    val id: Long
    val type: String
    val name: String
    val url: String
    val count: Long
}