package com.nonoka.nhentai.feature.doujinshi_page

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.NHENTAI_T
import com.nonoka.nhentai.domain.entity.PNG
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.ui.shared.model.GalleryUiState.DoujinshiItem
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.util.capitalized
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
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

    val favoritesLabel: String
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
    val downloadedStatus = mutableStateOf(false)

    fun init(doujinshiId: String) {
        lastReadPageIndex.value = null
        mainLoadingState.value = LoadingUiState.Loading("Loading, please wait")
        viewModelScope.launch(Dispatchers.Main) {
            doujinshiRepository.getDoujinshi(doujinshiId)
                .doOnSuccess {
                    processDoujinshiData(it)
                    loadRecommendedDoujinshis(it.id)
                    loadComments(it.id)
                    loadLastReadPageIndex(it.id)
                    loadDownloadedStatus(it.id)
                }
                .doOnError {
                    mainLoadingState.value = LoadingUiState.Idle
                }
        }
    }

    private fun processDoujinshiData(doujinshi: Doujinshi) {
        mainLoadingState.value = LoadingUiState.Idle
        viewModelScope.launch(Dispatchers.Default) {
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
                    val thumbnailType =
                        if (imageMeasurements.imageType == PNG) ".png" else ".jpg"
                    "$NHENTAI_T/galleries/${doujinshi.mediaId}/${index + 1}t$thumbnailType"
                },
                favoritesLabel = if (doujinshi.numOfFavorites > 0) "Favorite (${
                    decimalFormat.format(
                        doujinshi.numOfFavorites
                    )
                })" else "Favorite"
            )
        }
    }

    private fun loadRecommendedDoujinshis(doujinshiId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            recommendationLoadingState.value = LoadingUiState.Loading("Loading...")
            recommendedDoujinshis.clear()
            doujinshiRepository.getRecommendedDoujinshis(doujinshiId).doOnSuccess {
                viewModelScope.launch(Dispatchers.Default) {
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
        viewModelScope.launch(Dispatchers.Main) {
            commentLoadingState.value = LoadingUiState.Loading("Loading...")
            comments.clear()
            doujinshiRepository.getComments(doujinshiId).doOnSuccess { commentList ->
                Timber.d("Comments>>> ${commentList.size}")
                viewModelScope.launch(Dispatchers.Default) {
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
        viewModelScope.launch(Dispatchers.IO) {
            doujinshiRepository.getLastReadPageIndex(doujinshiId).doOnSuccess {
                if (it >= 0) {
                    lastReadPageIndex.value = it
                }
            }.doOnError {
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }

    fun resetLastReadPage(doujinshiId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            doujinshiRepository.getDoujinshi(doujinshiId).doOnSuccess {
                try {
                    val updateSuccess = doujinshiRepository.setReadDoujinshi(it, null)
                    if (updateSuccess) {
                        lastReadPageIndex.value = null
                    }
                } catch (error: Throwable) {
                    Timber.e("Could not load last read page index with error $error")
                }
            }.doOnError {
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }

    private fun loadDownloadedStatus(doujinshiId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            doujinshiRepository.isDoujinshiDownloaded(doujinshiId).doOnSuccess { isDownloaded ->
                downloadedStatus.value = isDownloaded
            }.doOnError {
                Timber.e("Could not load last read page index with error $it")
            }
        }
    }
}