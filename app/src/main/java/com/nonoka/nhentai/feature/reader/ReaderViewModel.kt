package com.nonoka.nhentai.feature.reader

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.di.qualifier.DefaultDispatcher
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.di.qualifier.MainDispatcher
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.NHENTAI_I
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.ImageMeasurements
import com.nonoka.nhentai.util.FileService
import com.nonoka.nhentai.feature.reader.ReaderPageModel.RemotePage
import com.nonoka.nhentai.feature.reader.ReaderPageModel.LocalPage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

data class ReaderState(
    val title: String,
    val images: List<ReaderPageModel>,
    val thumbnailList: List<String>,
    val pageIndicatorTemplate: String,
    val isDownloaded: Boolean
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
    private val fileService: FileService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val state = mutableStateOf<ReaderState?>(null)

    var toolBarsVisible = mutableStateOf(true)

    var focusedIndex = mutableIntStateOf(-1)
    var focusingIndexRequest = MutableSharedFlow<Int?>()

    private var currentDoujinshi: Doujinshi? = null

    fun init(doujinshiId: String, startIndex: Int = -1) {
        Timber.d("Load doujinshi $doujinshiId")
        if (startIndex >= 0) {
            viewModelScope.launch(defaultDispatcher) {
                delay(1000)
                focusingIndexRequest.emit(startIndex)
            }
        }
        viewModelScope.launch(mainDispatcher) {
            doujinshiRepository.getDoujinshi(doujinshiId)
                .doOnSuccess(this@ReaderViewModel::processDoujinshiData)
                .doOnError {
                    Timber.e("Failed to load doujinshi $doujinshiId with error $it")
                }
        }
    }

    fun requestReadingPage(index: Int) {
        focusedIndex.intValue = index
        viewModelScope.launch {
            focusingIndexRequest.emit(index)
        }
    }

    fun onPageFocused(index: Int) {
        viewModelScope.launch(ioDispatcher) {
            currentDoujinshi?.let {
                val result = doujinshiRepository.setReadDoujinshi(it, index)
                Timber.d("Test>>> set read doujinshi result: $result")
            }
        }
    }

    private fun processDoujinshiData(doujinshi: Doujinshi) {
        currentDoujinshi = doujinshi
        viewModelScope.launch(defaultDispatcher) {
            var isDownloaded = false
            withContext(ioDispatcher) {
                doujinshiRepository.isDoujinshiDownloaded(doujinshi.id)
            }.doOnSuccess {
                isDownloaded = it
            }
            state.value = ReaderState(
                title = doujinshi.previewTitle,
                images = doujinshi.images.pages.mapIndexed { index, imageMeasurements ->
                    if (isDownloaded) getLocalPage(
                        doujinshi.id,
                        index,
                        imageMeasurements
                    ) else getRemotePage(doujinshi.mediaId, index, imageMeasurements)
                },
                thumbnailList = doujinshi.previewThumbnailList,
                pageIndicatorTemplate = "Page %d of ${doujinshi.numOfPages}",
                isDownloaded = isDownloaded
            )
        }
    }

    private fun getRemotePage(
        mediaId: String,
        index: Int,
        imageMeasurements: ImageMeasurements
    ): RemotePage {
        val url = getPictureUrl(mediaId, index + 1, imageMeasurements.imageType)
        return RemotePage(
            url = url,
            width = imageMeasurements.width,
            height = imageMeasurements.height,
        )
    }

    private fun getLocalPage(
        doujinshiId: String,
        index: Int,
        imageMeasurements: ImageMeasurements
    ): LocalPage {
        val imageName = "${index + 1}.${imageMeasurements.imageType}"

        return LocalPage(
            file = fileService.getDoujinImageLocalPath(doujinshiId, imageName),
            width = imageMeasurements.width,
            height = imageMeasurements.height,
        )
    }

    private fun getGalleryUrl(mediaId: String): String = "$NHENTAI_I/galleries/$mediaId"

    private fun getPictureUrl(
        mediaId: String,
        pageNumber: Int,
        imageType: String
    ): String = "${getGalleryUrl(mediaId)}/$pageNumber.$imageType"
}