package nhdphuong.com.manga.data.entity.book.tags

interface ITag {
    fun id(): Long
    fun type(): String
    fun name(): String
    fun url(): String
    fun count(): Long
}