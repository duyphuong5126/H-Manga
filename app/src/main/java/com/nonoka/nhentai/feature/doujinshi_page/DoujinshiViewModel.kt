package com.nonoka.nhentai.feature.doujinshi_page

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.di.qualifier.DefaultDispatcher
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.di.qualifier.MainDispatcher
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.NHENTAI_T
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.ui.shared.model.GalleryUiState.DoujinshiItem
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.util.FileService
import com.nonoka.nhentai.util.capitalized
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

data class DoujinshiState(
    val coverUrl: String,
    val coverRatio: Float,

    val id: String,
    val primaryTitle: String,
    val secondaryTitle: String?,

    val tags: Map<String, List<TagInfo>>,

    val pageCount: String,
    val updatedAt: String,
    val comment: String,

    val previewThumbnails: List<String>,

    val favoritesLabel: String,

    val origin: Doujinshi,

    val isDownloaded: Boolean = false
)

data class TagInfo(val label: String, val count: String)

data class CommentState(
    val userName: String,
    val avatarUrl: String,
    val body: String,
    val postDate: String
)

@HiltViewModel
class DoujinshiViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
    private val fileService: FileService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val decimalFormat = DecimalFormat("#,###")
    private val dateTimeFormat =
        SimpleDateFormat("hh:mm aaa - EEE, MMM d, yyyy", Locale.getDefault())

    val doujinshiState = mutableStateOf<DoujinshiState?>(null)

    val recommendedDoujinshis = mutableStateListOf<DoujinshiItem>()
    val comments = mutableStateListOf<CommentState>()
    val lastReadPageIndex = mutableStateOf<Int?>(null)

    val mainLoadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)
    val recommendationLoadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)
    val commentLoadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)

    val downloadRequestId = mutableStateOf<UUID?>(null)
    val favoriteStatus = mutableStateOf(false)

    fun init(doujinshiId: String) {
        Timber.d("Download>>> init doujinshiId=$doujinshiId")
        lastReadPageIndex.value = null
        mainLoadingState.value = LoadingUiState.Loading("Loading, please wait")
        viewModelScope.launch(mainDispatcher) {
            doujinshiRepository.getDoujinshi(doujinshiId)
                .doOnSuccess {
                    processDoujinshiData(it)
                    loadRecommendedDoujinshis(it.id)
                    loadComments(it.id)
                    loadLastReadPageIndex(it.id)
                    loadDownloadedStatus(it.id)
                    loadFavoriteStatus(it.id)
                }
                .doOnError {
                    mainLoadingState.value = LoadingUiState.Idle
                }
        }
    }

    private fun processDoujinshiData(doujinshi: Doujinshi) {
        mainLoadingState.value = LoadingUiState.Idle
        viewModelScope.launch(defaultDispatcher) {
            val artistCount = doujinshi.tags
                .count {
                    it.type.trim().lowercase() == "artist"
                }
            doujinshiState.value = DoujinshiState(
                id = doujinshi.id,
                primaryTitle = doujinshi.previewTitle,
                secondaryTitle = doujinshi.title.japaneseName,
                tags = doujinshi.tags.groupBy { tag ->
                    tag.type.capitalized()
                }.mapValues { (_, value) ->
                    value.map {
                        TagInfo(
                            label = it.name,
                            count = "(${decimalFormat.format(it.count)})"
                        )
                    }
                },
                coverUrl = doujinshi.cover,
                coverRatio = doujinshi.coverRatio,
                pageCount = if (doujinshi.numOfPages > 1) "${doujinshi.numOfPages} pages" else "${doujinshi.numOfPages} page",
                updatedAt = "Updated at ${dateTimeFormat.format(doujinshi.updateAt * 1000)}",
                comment = "Please support ${if (artistCount > 1) "these artists" else "this artist"}",
                previewThumbnails = doujinshi.images.pages.mapIndexed { index, imageMeasurements ->
                    val thumbnailType = imageMeasurements.imageType
                    "$NHENTAI_T/galleries/${doujinshi.mediaId}/${index + 1}t.$thumbnailType"
                },
                favoritesLabel = if (doujinshi.numOfFavorites > 0) "Favorite (${
                    decimalFormat.format(
                        doujinshi.numOfFavorites
                    )
                })" else "Favorite",
                origin = doujinshi
            )
        }
    }

    private fun loadRecommendedDoujinshis(doujinshiId: String) {
        viewModelScope.launch(mainDispatcher) {
            recommendationLoadingState.value = LoadingUiState.Loading("Loading...")
            recommendedDoujinshis.clear()
            doujinshiRepository.getRecommendedDoujinshis(doujinshiId).doOnSuccess {
                viewModelScope.launch(defaultDispatcher) {
                    recommendedDoujinshis.addAll(
                        it.map(::DoujinshiItem),
                    )
                }
                recommendationLoadingState.value = LoadingUiState.Idle
            }.doOnError {
                Timber.d("Test>>> failed to load recommendation of $doujinshiId: $it")
                recommendationLoadingState.value = LoadingUiState.Idle
            }
        }
    }

    private fun loadComments(doujinshiId: String) {
        viewModelScope.launch(mainDispatcher) {
            commentLoadingState.value = LoadingUiState.Loading("Loading...")
            comments.clear()
            doujinshiRepository.getComments(doujinshiId).doOnSuccess { commentList ->
                Timber.d("Comments>>> ${commentList.size}")
                viewModelScope.launch(defaultDispatcher) {
                    comments.addAll(
                        commentList.map {
                            CommentState(
                                it.poster.userName,
                                it.poster.avatarUrl,
                                body = it.body,
                                postDate = dateTimeFormat.format(
                                    Date(it.postDate)
                                )
                            )
                        },
                    )
                }
                commentLoadingState.value = LoadingUiState.Idle
            }.doOnError {
                commentLoadingState.value = LoadingUiState.Idle
            }
        }
    }

    fun loadLastReadPageIndex(doujinshiId: String) {
        viewModelScope.launch(ioDispatcher) {
            doujinshiRepository.getLastReadPageIndex(doujinshiId).doOnSuccess {
                if (it >= 0) {
                    lastReadPageIndex.value = it
                }
            }.doOnError {
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }

    fun resetLastReadPage(doujinshiId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            doujinshiRepository.getDoujinshi(doujinshiId).doOnSuccess {
                try {
                    val updateSuccess = doujinshiRepository.setReadDoujinshi(it, null)
                    if (updateSuccess) {
                        lastReadPageIndex.value = null
                    }
                    onSuccess()
                } catch (error: Throwable) {
                    Timber.e("Could not load last read page index with error $error")
                }
            }.doOnError {
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }

    fun deleteDownloadedData(doujinshi: Doujinshi, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            val doujinshiId = doujinshi.id
            fileService.deleteDoujinshiFolder(doujinshiId).doOnSuccess { success ->
                Timber.d("Delete result of doujin $doujinshiId: $success")
                if (success) {
                    val updatedSucceeded =
                        doujinshiRepository.setDownloadedDoujinshi(doujinshi, false)
                    if (updatedSucceeded) {
                        doujinshiState.value = doujinshiState.value?.copy(isDownloaded = false)
                    }
                    onSuccess()
                }
            }.doOnError {
                Timber.e("Could not delete downloaded doujinshi $doujinshiId with error $it")
            }
        }
    }

    fun loadDownloadedStatus(doujinshiId: String, onSuccess: () -> Unit = {}) {
        Timber.d("Download>>> loadDownloadedStatus doujinshiId=$doujinshiId")
        viewModelScope.launch(ioDispatcher) {
            doujinshiRepository.isDoujinshiDownloaded(doujinshiId).doOnSuccess { isDownloaded ->
                Timber.d("Download>>> loadDownloadedStatus doujinshiId=$doujinshiId, isDownloaded=$isDownloaded")
                doujinshiState.value = doujinshiState.value?.copy(isDownloaded = isDownloaded)
                onSuccess()
            }.doOnError {
                Timber.d("Download>>> loadDownloadedStatus doujinshiId=$doujinshiId, error=$it")
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }

    private fun loadFavoriteStatus(doujinshiId: String) {
        viewModelScope.launch(ioDispatcher) {
            doujinshiRepository.getFavoriteStatus(doujinshiId).doOnSuccess { isFavorite ->
                favoriteStatus.value = isFavorite
            }.doOnError {
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }

    fun toggleFavoriteStatus(doujinshi: Doujinshi, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            val newFavoriteStatus = !favoriteStatus.value
            doujinshiRepository.setFavoriteDoujinshi(doujinshi, newFavoriteStatus)
                .doOnSuccess { updateSuccess ->
                    if (updateSuccess) {
                        favoriteStatus.value = newFavoriteStatus
                        onSuccess()
                    }
                }.doOnError {
                    Timber.e("Could not update favorite status with error $it")
                }
        }
    }
}