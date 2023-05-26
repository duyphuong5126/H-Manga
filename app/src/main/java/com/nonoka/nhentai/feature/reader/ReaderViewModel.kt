package com.nonoka.nhentai.feature.reader

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.NHENTAI_I
import com.nonoka.nhentai.domain.entity.book.Doujinshi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class ReaderState(
    val title: String,
    val images: List<ReaderPageModel>,
    val thumbnailList: List<String>,
    val pageIndicatorTemplate: String,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository
) : ViewModel() {
    val state = mutableStateOf<ReaderState?>(null)

    var toolBarsVisible = mutableStateOf(true)

    var focusedIndex = mutableStateOf(-1)
    var focusingIndexRequest = MutableSharedFlow<Int?>()

    fun init(doujinshiId: String, startIndex: Int = -1) {
        Timber.d("Load doujinshi $doujinshiId")
        if (startIndex >= 0) {
            viewModelScope.launch(Dispatchers.Default) {
                delay(1000)
                focusingIndexRequest.emit(startIndex)
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            doujinshiRepository.getDoujinshi(doujinshiId)
                .doOnSuccess(this@ReaderViewModel::processDoujinshiData)
                .doOnError {
                    Timber.e("Failed to load doujinshi $doujinshiId with error $it")
                }
        }
    }

    fun requestReadingPage(index: Int) {
        focusedIndex.value = index
        viewModelScope.launch {
            focusingIndexRequest.emit(index)
        }
    }

    private fun processDoujinshiData(doujinshi: Doujinshi) {
        viewModelScope.launch(Dispatchers.Default) {
            state.value = ReaderState(
                title = doujinshi.previewTitle,
                images = doujinshi.images.pages.mapIndexed { index, imageMeasurements ->
                    ReaderPageModel(
                        pageUrl = getPictureUrl(
                            doujinshi.mediaId,
                            index + 1,
                            imageMeasurements.imageType
                        ),
                        width = imageMeasurements.width,
                        height = imageMeasurements.height,
                    )
                },
                thumbnailList = doujinshi.previewThumbnailList,
                pageIndicatorTemplate = "Page %d of ${doujinshi.numOfPages}"
            )
        }
    }

    private fun getGalleryUrl(mediaId: String): String = "$NHENTAI_I/galleries/$mediaId"

    private fun getPictureUrl(
        mediaId: String,
        pageNumber: Int,
        imageType: String
    ): String = "${getGalleryUrl(mediaId)}/$pageNumber.$imageType"
}