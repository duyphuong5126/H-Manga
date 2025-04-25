package com.nonoka.nhentai.feature.collection

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.GalleryPageNotExistException
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val decimalFormat = DecimalFormat("#,###")

    val galleryItems = mutableStateListOf<GalleryUiState>()

    val collectionCountLabel = mutableStateOf("")

    val loadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)
    private val loadingData get() = loadingState.value is LoadingUiState.Loading

    private var pageIndex = 0
    private val noMoreData = AtomicBoolean(false)

    //val reset = mutableStateOf(false)

    fun loadMore() {
        Timber.d("Collection>>> loadMore")
        if (loadingData || noMoreData.get()) {
            Timber.d("Collection>>> Page $pageIndex is being loaded")
            return
        }
        loadPage(pageIndex++)
    }

    fun resetList() {
        // todo
        Timber.d("Collection>>> refresh, loadingData=$loadingData")
        if (loadingData) {
            Timber.d("Collection>>> Page $pageIndex is being loaded")
            return
        }
        noMoreData.set(false)
        pageIndex = 0
        galleryItems.clear()
        loadPage(0)
    }

    private fun loadPage(pageIndex: Int) {
        Timber.d("Collection>>> loadPage $pageIndex")
        loadingState.value = LoadingUiState.Loading("Loading")
        viewModelScope.launch(ioDispatcher) {
            val pageData = ArrayList<GalleryUiState>()
            try {
                val result = doujinshiRepository.getCollectionPage(pageIndex)
                val resultList = result.doujinshiList.map {
                    GalleryUiState.DoujinshiItem(it)
                }
                Timber.d("Collection>>> resultList of page $pageIndex ${resultList.size}, first item=${resultList.firstOrNull()?.doujinshi?.title?.prettyName}")
                if (resultList.isNotEmpty()) {
                    pageData.add(GalleryUiState.Title("Page ${pageIndex + 1}"))
                    pageData.addAll(resultList)

                    collectionCountLabel.value =
                        decimalFormat.format(doujinshiRepository.getCollectionSize())
                }
                finishLoading(pageIndex)
            } catch (error: Throwable) {
                finishLoading(pageIndex)
                Timber.e("Collection>>> load error $error")
                if (error is GalleryPageNotExistException) {
                    pageData.add(GalleryUiState.Title("Page ${pageIndex + 1} - Not exist"))
                } else {
                    pageData.add(GalleryUiState.Title("Failed to load page ${pageIndex + 1}"))
                }
            }
            noMoreData.compareAndSet(false, pageData.isEmpty())
            if (pageData.isNotEmpty()) {
                galleryItems.addAll(pageData)
            }
        }
    }

    private fun finishLoading(pageIndex: Int) {
        if (pageIndex > 0) {
            viewModelScope.launch(ioDispatcher) {
                delay(500)
                loadingState.value = LoadingUiState.Idle
            }
        } else {
            loadingState.value = LoadingUiState.Idle
        }
    }
}