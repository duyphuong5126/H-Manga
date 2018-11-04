package nhdphuong.com.manga.data.repository

import kotlinx.coroutines.experimental.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.TagDataSource
import nhdphuong.com.manga.scope.Local
import nhdphuong.com.manga.scope.Remote
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(@Remote private val mTagRemoteDataSource: TagDataSource.Remote,
                                        @Local private val mTagLocalDataSource: TagDataSource.Local){
    companion object {
        private const val TAG = "TagRepository"
        private const val TOTAL_TAG_TYPES = 8
    }

    fun fetchAllTagLists(onComplete:(Boolean) -> Unit) {
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

        launch {
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
}