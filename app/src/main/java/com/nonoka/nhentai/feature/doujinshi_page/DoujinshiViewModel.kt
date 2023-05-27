package com.nonoka.nhentai.feature.doujinshi_page

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.NHENTAI_T
import com.nonoka.nhentai.domain.entity.PNG
import com.nonoka.nhentai.domain.entity.book.Doujinshi
import com.nonoka.nhentai.feature.home.GalleryUiState.DoujinshiItem
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.util.capitalized
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
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

@HiltViewModel
class DoujinshiViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
) : ViewModel() {
    private val decimalFormat = DecimalFormat("#,###")
    private val dateTimeFormat =
        SimpleDateFormat("hh:mm aaa - EEE, MMM d, yyyy", Locale.getDefault())

    val doujinshiState = mutableStateOf<DoujinshiState?>(null)

    val recommendedDoujinshis = mutableStateListOf<DoujinshiItem>()

    var mainLoadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)
    var recommendationLoadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)

    fun init(doujinshiId: String) {
        Timber.d("Test>>> Load doujinshi $doujinshiId")
        mainLoadingState.value = LoadingUiState.Loading("Loading, please wait")
        viewModelScope.launch(Dispatchers.Main) {
            doujinshiRepository.getDoujinshi(doujinshiId)
                .doOnSuccess {
                    processDoujinshiData(it)
                    loadRecommendedDoujinshis(it.bookId)
                }
                .doOnError {
                    Timber.e("Test>>> Failed to load doujinshi $doujinshiId with error $it")
                    mainLoadingState.value = LoadingUiState.Idle
                }
        }
    }

    private fun processDoujinshiData(doujinshi: Doujinshi) {
        Timber.d("Test>>> Loaded doujinshi ${doujinshi.bookId}")
        mainLoadingState.value = LoadingUiState.Idle
        viewModelScope.launch(Dispatchers.Default) {
            val artistCount = doujinshi.tags
                .count {
                    it.type.trim().lowercase() == "artist"
                }
            doujinshiState.value = DoujinshiState(
                id = doujinshi.bookId,
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
                Timber.d("Test>>> loaded recommendation of $doujinshiId")
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
}