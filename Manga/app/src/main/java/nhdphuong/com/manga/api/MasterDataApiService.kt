package nhdphuong.com.manga.api

import nhdphuong.com.manga.data.entity.LatestAppVersion
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag
import nhdphuong.com.manga.data.entity.book.tags.Character
import retrofit2.Call
import retrofit2.http.GET

interface MasterDataApiService {
    @GET("${ApiConstants.NHENTAI_DB}/tags/artist.txt")
    fun getArtistsList(): Call<List<Artist>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/category.txt")
    fun getCategoriesList(): Call<List<Category>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/character.txt")
    fun getCharactersList(): Call<List<Character>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/group.txt")
    fun getGroupsList(): Call<List<Group>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/language.txt")
    fun getLanguagesList(): Call<List<Language>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/parody.txt")
    fun getParodiesList(): Call<List<Parody>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/tag.txt")
    fun getTagsList(): Call<List<Tag>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/unknown.txt")
    fun getUnknownTagsList(): Call<List<UnknownTag>>

    @GET("${ApiConstants.NHENTAI_DB}/tags/CurrentVersion.txt")
    fun getTagDataVersion(): Call<Long>

    @GET("${ApiConstants.NHENTAI_DB}/tags/AppVersion.txt")
    fun getAppVersion(): Call<Int>

    @GET("${ApiConstants.NHENTAI_DB}/tags/CurrentAppVersion.json")
    fun getLatestAppVersion(): Call<LatestAppVersion>
}
