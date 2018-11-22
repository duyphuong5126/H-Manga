package nhdphuong.com.manga.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.TagDataSource
import nhdphuong.com.manga.data.entity.book.tags.*
import nhdphuong.com.manga.scope.Local
import nhdphuong.com.manga.scope.Remote
import nhdphuong.com.manga.scope.corountine.IO
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(@Remote private val mTagRemoteDataSource: TagDataSource.Remote,
                                        @Local private val mTagLocalDataSource: TagDataSource.Local,
                                        @IO private val io: CoroutineScope) {
    companion object {
        private const val TAG = "TagRepository"
        private const val TOTAL_TAG_TYPES = 8
    }

    fun fetchAllTagLists(onComplete: (Boolean) -> Unit) {
        val tagCount = AtomicInteger(0)
        val handleSuccess = {
            if (tagCount.incrementAndGet() >= TOTAL_TAG_TYPES) {
                onComplete(true)
            }
        }
        val handleError = {
            if (tagCount.incrementAndGet() >= TOTAL_TAG_TYPES) {
                onComplete(false)
            }
        }

        io.launch {
            launch {
                mTagRemoteDataSource.fetchArtistsList(onSuccess = { artists ->
                    Logger.d(TAG, "artists=${artists?.size ?: 0}")
                    artists?.let {
                        mTagLocalDataSource.insertArtistsList(artists)
                        handleSuccess()
                        return@fetchArtistsList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchCategoriesList(onSuccess = { categories ->
                    Logger.d(TAG, "categories=${categories?.size ?: 0}")
                    categories?.let {
                        mTagLocalDataSource.insertCategoriesList(categories)
                        handleSuccess()
                        return@fetchCategoriesList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchCharactersList(onSuccess = { characters ->
                    Logger.d(TAG, "characters=${characters?.size ?: 0}")
                    characters?.let {
                        mTagLocalDataSource.insertCharactersList(characters)
                        handleSuccess()
                        return@fetchCharactersList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchGroupsList(onSuccess = { groups ->
                    Logger.d(TAG, "groups=${groups?.size ?: 0}")
                    groups?.let {
                        mTagLocalDataSource.insertGroupsList(groups)
                        handleSuccess()
                        return@fetchGroupsList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchLanguagesList(onSuccess = { languages ->
                    Logger.d(TAG, "languages=${languages?.size ?: 0}")
                    languages?.let {
                        mTagLocalDataSource.insertLanguagesList(languages)
                        handleSuccess()
                        return@fetchLanguagesList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchParodiesList(onSuccess = { parodies ->
                    Logger.d(TAG, "parodies=${parodies?.size ?: 0}")
                    parodies?.let {
                        mTagLocalDataSource.insertParodiesList(parodies)
                        handleSuccess()
                        return@fetchParodiesList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchTagsList(onSuccess = { tags ->
                    Logger.d(TAG, "tags=${tags?.size ?: 0}")
                    tags?.let {
                        mTagLocalDataSource.insertTagsList(tags)
                        handleSuccess()
                        return@fetchTagsList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
            launch {
                mTagRemoteDataSource.fetchUnknownTypesList(onSuccess = { unknownTypes ->
                    Logger.d(TAG, "unknownTypes=${unknownTypes?.size ?: 0}")
                    unknownTypes?.let {
                        mTagLocalDataSource.insertUnknownTypesList(unknownTypes)
                        handleSuccess()
                        return@fetchUnknownTypesList
                    }
                    handleError()
                }, onError = {
                    handleError()
                })
            }
        }
    }

    suspend fun getTagCount(): Int = mTagLocalDataSource.getTagCount()

    suspend fun getTagsCountByPrefix(firstChar: Char): Int = mTagLocalDataSource.getTagCountByPrefix("$firstChar%")

    suspend fun getTagCountBySpecialCharactersPrefix(): Int = mTagLocalDataSource.getTagCountBySpecialCharactersPrefix()

    suspend fun getTagsByPrefixAscending(prefixChar: Char, limit: Int, offset: Int): List<Tag> = mTagLocalDataSource.getTagsByPrefixAscending("$prefixChar%", limit, offset)

    suspend fun getTagsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Tag> = mTagLocalDataSource.getTagsBySpecialCharactersPrefixAscending(limit, offset)

    suspend fun getTagsByPopularityAscending(limit: Int, offset: Int): List<Tag> = mTagLocalDataSource.getTagsByPopularityAscending(limit, offset)

    suspend fun getTagsByPopularityDescending(limit: Int, offset: Int): List<Tag> = mTagLocalDataSource.getTagsByPopularityDescending(limit, offset)

    suspend fun getArtistsCount(): Int = mTagLocalDataSource.getArtistsCount()

    suspend fun getArtistsCountByPrefix(firstChar: Char): Int = mTagLocalDataSource.getArtistsCountByPrefix("$firstChar%")

    suspend fun getArtistsCountBySpecialCharactersPrefix(): Int = mTagLocalDataSource.getArtistsCountBySpecialCharactersPrefix()

    suspend fun getArtistsByPrefixAscending(prefixChar: Char, limit: Int, offset: Int): List<Artist> = mTagLocalDataSource.getArtistsByPrefixAscending("$prefixChar%", limit, offset)

    suspend fun getArtistsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Artist> = mTagLocalDataSource.getArtistsBySpecialCharactersPrefixAscending(limit, offset)

    suspend fun getArtistsByPopularityAscending(limit: Int, offset: Int): List<Artist> = mTagLocalDataSource.getArtistsByPopularityAscending(limit, offset)

    suspend fun getArtistsByPopularityDescending(limit: Int, offset: Int): List<Artist> = mTagLocalDataSource.getArtistsByPopularityDescending(limit, offset)

    suspend fun getCharactersCount(): Int = mTagLocalDataSource.getCharactersCount()

    suspend fun getCharactersCountByPrefix(firstChar: Char): Int = mTagLocalDataSource.getCharactersCountByPrefix("$firstChar%")

    suspend fun getCharactersCountBySpecialCharactersPrefix(): Int = mTagLocalDataSource.getCharactersCountBySpecialCharactersPrefix()

    suspend fun getCharactersByPrefixAscending(prefixChar: Char, limit: Int, offset: Int): List<Character> = mTagLocalDataSource.getCharactersByPrefixAscending("$prefixChar%", limit, offset)

    suspend fun getCharactersBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Character> = mTagLocalDataSource.getCharactersBySpecialCharactersPrefixAscending(limit, offset)

    suspend fun getCharactersByPopularityAscending(limit: Int, offset: Int): List<Character> = mTagLocalDataSource.getCharactersByPopularityAscending(limit, offset)

    suspend fun getCharactersByPopularityDescending(limit: Int, offset: Int): List<Character> = mTagLocalDataSource.getCharactersByPopularityDescending(limit, offset)

    suspend fun getGroupsCount(): Int = mTagLocalDataSource.getGroupsCount()

    suspend fun getGroupsCountByPrefix(firstChar: Char): Int = mTagLocalDataSource.getGroupsCountByPrefix("$firstChar%")

    suspend fun getGroupsCountBySpecialCharactersPrefix(): Int = mTagLocalDataSource.getGroupsCountBySpecialCharactersPrefix()

    suspend fun getGroupsByPrefixAscending(prefixChar: Char, limit: Int, offset: Int): List<Group> = mTagLocalDataSource.getGroupsByPrefixAscending("$prefixChar%", limit, offset)

    suspend fun getGroupsBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Group> = mTagLocalDataSource.getGroupsBySpecialCharactersPrefixAscending(limit, offset)

    suspend fun getGroupsByPopularityAscending(limit: Int, offset: Int): List<Group> = mTagLocalDataSource.getGroupsByPopularityAscending(limit, offset)

    suspend fun getGroupsByPopularityDescending(limit: Int, offset: Int): List<Group> = mTagLocalDataSource.getGroupsByPopularityDescending(limit, offset)

    suspend fun getParodiesCount(): Int = mTagLocalDataSource.getParodiesCount()

    suspend fun getParodiesCountByPrefix(firstChar: Char): Int = mTagLocalDataSource.getParodiesCountByPrefix("$firstChar%")

    suspend fun getParodiesCountBySpecialCharactersPrefix(): Int = mTagLocalDataSource.getParodiesCountBySpecialCharactersPrefix()

    suspend fun getParodiesByPrefixAscending(prefixChar: Char, limit: Int, offset: Int): List<Parody> = mTagLocalDataSource.getParodiesByPrefixAscending("$prefixChar%", limit, offset)

    suspend fun getParodiesBySpecialCharactersPrefixAscending(limit: Int, offset: Int): List<Parody> = mTagLocalDataSource.getParodiesBySpecialCharactersPrefixAscending(limit, offset)

    suspend fun getParodiesByPopularityAscending(limit: Int, offset: Int): List<Parody> = mTagLocalDataSource.getParodiesByPopularityAscending(limit, offset)

    suspend fun getParodiesByPopularityDescending(limit: Int, offset: Int): List<Parody> = mTagLocalDataSource.getParodiesByPopularityDescending(limit, offset)
}