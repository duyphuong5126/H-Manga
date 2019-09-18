package nhdphuong.com.manga.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.TagDataSource
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.scope.Local
import nhdphuong.com.manga.scope.Remote
import nhdphuong.com.manga.scope.corountine.IO
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
        @Remote private val mTagRemoteDataSource: TagDataSource.Remote,
        @Local private val mTagLocalDataSource: TagDataSource.Local,
        @IO private val io: CoroutineScope
) {
    companion object {
        private const val TAG = "TagRepository"
        private const val TOTAL_TAG_TYPES = 8
    }

    @Volatile
    private var isDownloading: Boolean = false

    fun fetchAllTagLists(onComplete: (Boolean) -> Unit) {
        Logger.d(TAG, "fetchAllTagLists, is downloading started=$isDownloading")
        if (isDownloading) {
            return
        }
        isDownloading = true
        val tagCount = AtomicInteger(0)
        val successCount = AtomicInteger(0)
        val handleResponse: (Boolean) -> Unit = { success ->
            if (success) {
                successCount.incrementAndGet()
            }
            if (tagCount.incrementAndGet() >= TOTAL_TAG_TYPES) {
                onComplete(successCount.get() >= TOTAL_TAG_TYPES)
                isDownloading = false
            }
        }

        io.launch {
            launch {
                mTagRemoteDataSource.fetchArtistsList(onSuccess = { artists ->
                    Logger.d(TAG, "artists=${artists?.size ?: 0}")
                    handleResponse(artists != null)
                    artists?.let {
                        mTagLocalDataSource.insertArtistsList(artists)
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchCategoriesList(onSuccess = { categories ->
                    Logger.d(TAG, "categories=${categories?.size ?: 0}")
                    handleResponse(categories != null)
                    categories?.let {
                        mTagLocalDataSource.insertCategoriesList(categories)
                        return@fetchCategoriesList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchCharactersList(onSuccess = { characters ->
                    Logger.d(TAG, "characters=${characters?.size ?: 0}")
                    handleResponse(characters != null)
                    characters?.let {
                        mTagLocalDataSource.insertCharactersList(characters)
                        return@fetchCharactersList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchGroupsList(onSuccess = { groups ->
                    Logger.d(TAG, "groups=${groups?.size ?: 0}")
                    handleResponse(groups != null)
                    groups?.let {
                        mTagLocalDataSource.insertGroupsList(groups)
                        return@fetchGroupsList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchLanguagesList(onSuccess = { languages ->
                    Logger.d(TAG, "languages=${languages?.size ?: 0}")
                    handleResponse(languages != null)
                    languages?.let {
                        mTagLocalDataSource.insertLanguagesList(languages)
                        return@fetchLanguagesList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchParodiesList(onSuccess = { parodies ->
                    Logger.d(TAG, "parodies=${parodies?.size ?: 0}")
                    handleResponse(parodies != null)
                    parodies?.let {
                        mTagLocalDataSource.insertParodiesList(parodies)
                        return@fetchParodiesList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchTagsList(onSuccess = { tags ->
                    Logger.d(TAG, "tags=${tags?.size ?: 0}")
                    handleResponse(tags != null)
                    tags?.let {
                        mTagLocalDataSource.insertTagsList(tags)
                        return@fetchTagsList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
            launch {
                mTagRemoteDataSource.fetchUnknownTypesList(onSuccess = { unknownTypes ->
                    Logger.d(TAG, "unknownTypes=${unknownTypes?.size ?: 0}")
                    handleResponse(unknownTypes != null)
                    unknownTypes?.let {
                        mTagLocalDataSource.insertUnknownTypesList(unknownTypes)
                        return@fetchUnknownTypesList
                    }
                }, onError = {
                    handleResponse(false)
                })
            }
        }
    }

    suspend fun getTagCount(): Int = mTagLocalDataSource.getTagCount()

    suspend fun getTagsCountByPrefix(firstChar: Char): Int {
        return mTagLocalDataSource.getTagCountByPrefix("$firstChar%")
    }

    suspend fun getTagCountBySpecialCharactersPrefix(): Int {
        return mTagLocalDataSource.getTagCountBySpecialCharactersPrefix()
    }

    suspend fun getTagsByPrefixAscending(prefixChar: Char, limit: Int, offset: Int): List<Tag> {
        return mTagLocalDataSource.getTagsByPrefixAscending(
                "$prefixChar%", limit, offset
        )
    }

    suspend fun getTagsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Tag> {
        return mTagLocalDataSource.getTagsBySpecialCharactersPrefixAscending(limit, offset)
    }

    @Suppress("unused")
    suspend fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag> {
        return mTagLocalDataSource.getTagsByPopularityAscending(limit, offset)
    }

    suspend fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag> {
        return mTagLocalDataSource.getTagsByPopularityDescending(limit, offset)
    }

    suspend fun getArtistsCount(): Int = mTagLocalDataSource.getArtistsCount()

    suspend fun getArtistsCountByPrefix(firstChar: Char): Int {
        return mTagLocalDataSource.getArtistsCountByPrefix("$firstChar%")
    }

    suspend fun getArtistsCountBySpecialCharactersPrefix(): Int {
        return mTagLocalDataSource.getArtistsCountBySpecialCharactersPrefix()
    }

    suspend fun getArtistsByPrefixAscending(
            prefixChar: Char,
            limit: Int,
            offset: Int
    ): List<Artist> {
        return mTagLocalDataSource.getArtistsByPrefixAscending(
                "$prefixChar%", limit, offset
        )
    }

    suspend fun getArtistsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Artist> {
        return mTagLocalDataSource.getArtistsBySpecialCharactersPrefixAscending(limit, offset)
    }

    @Suppress("unused")
    suspend fun getArtistsByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Artist> = mTagLocalDataSource.getArtistsByPopularityAscending(limit, offset)

    suspend fun getArtistsByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Artist> = mTagLocalDataSource.getArtistsByPopularityDescending(limit, offset)

    suspend fun getCharactersCount(): Int {
        return mTagLocalDataSource.getCharactersCount()
    }

    suspend fun getCharactersCountByPrefix(firstChar: Char): Int {
        return mTagLocalDataSource.getCharactersCountByPrefix("$firstChar%")
    }

    suspend fun getCharactersCountBySpecialCharactersPrefix(): Int {
        return mTagLocalDataSource.getCharactersCountBySpecialCharactersPrefix()
    }

    suspend fun getCharactersByPrefixAscending(
            prefixChar: Char,
            limit: Int,
            offset: Int
    ): List<Character> {
        return mTagLocalDataSource.getCharactersByPrefixAscending(
                "$prefixChar%", limit, offset
        )
    }

    suspend fun getCharactersBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Character> {
        return mTagLocalDataSource.getCharactersBySpecialCharactersPrefixAscending(limit, offset)
    }

    @Suppress("unused")
    suspend fun getCharactersByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Character> = mTagLocalDataSource.getCharactersByPopularityAscending(limit, offset)

    suspend fun getCharactersByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Character> = mTagLocalDataSource.getCharactersByPopularityDescending(limit, offset)

    suspend fun getGroupsCount(): Int = mTagLocalDataSource.getGroupsCount()

    suspend fun getGroupsCountByPrefix(firstChar: Char): Int {
        return mTagLocalDataSource.getGroupsCountByPrefix("$firstChar%")
    }

    suspend fun getGroupsCountBySpecialCharactersPrefix(): Int {
        return mTagLocalDataSource.getGroupsCountBySpecialCharactersPrefix()
    }

    suspend fun getGroupsByPrefixAscending(
            prefixChar: Char,
            limit: Int,
            offset: Int
    ): List<Group> {
        return mTagLocalDataSource.getGroupsByPrefixAscending(
                "$prefixChar%", limit, offset
        )
    }

    suspend fun getGroupsBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagLocalDataSource.getGroupsBySpecialCharactersPrefixAscending(limit, offset)

    @Suppress("unused")
    suspend fun getGroupsByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagLocalDataSource.getGroupsByPopularityAscending(limit, offset)

    suspend fun getGroupsByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Group> = mTagLocalDataSource.getGroupsByPopularityDescending(limit, offset)

    suspend fun getParodiesCount(): Int = mTagLocalDataSource.getParodiesCount()

    suspend fun getParodiesCountByPrefix(firstChar: Char): Int {
        return mTagLocalDataSource.getParodiesCountByPrefix("$firstChar%")
    }

    suspend fun getParodiesCountBySpecialCharactersPrefix(): Int {
        return mTagLocalDataSource.getParodiesCountBySpecialCharactersPrefix()
    }

    suspend fun getParodiesByPrefixAscending(
            prefixChar: Char,
            limit: Int,
            offset: Int
    ): List<Parody> {
        return mTagLocalDataSource.getParodiesByPrefixAscending(
                "$prefixChar%", limit, offset
        )
    }

    suspend fun getParodiesBySpecialCharactersPrefixAscending(
            limit: Int,
            offset: Int
    ): List<Parody> {
        return mTagLocalDataSource.getParodiesBySpecialCharactersPrefixAscending(limit, offset)
    }

    @Suppress("unused")
    suspend fun getParodiesByPopularityAscending(
            limit: Int,
            offset: Int
    ): List<Parody> = mTagLocalDataSource.getParodiesByPopularityAscending(limit, offset)

    suspend fun getParodiesByPopularityDescending(
            limit: Int,
            offset: Int
    ): List<Parody> = mTagLocalDataSource.getParodiesByPopularityDescending(limit, offset)


    fun getCurrentVersion(onSuccess: (Long) -> Unit, onError: () -> Unit) {
        io.launch {
            mTagRemoteDataSource.fetchCurrentVersion(onSuccess, onError)
        }
    }
}
