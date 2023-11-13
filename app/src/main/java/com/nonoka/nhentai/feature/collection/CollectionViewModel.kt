package com.nonoka.nhentai.feature.collection

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.GalleryPageNotExistException
import com.nonoka.nhentai.paging.PagingDataLoader
import com.nonoka.nhentai.paging.PagingDataSource
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), PagingDataLoader<GalleryUiState> {
    private val decimalFormat = DecimalFormat("#,###")

    val collectionFlow = Pager(
        PagingConfig(
            pageSize = 25,
            prefetchDistance = 5,
            initialLoadSize = 25,
        )
    ) {
        PagingDataSource(this)
    }.flow.cachedIn(viewModelScope)

    val collectionCountLabel = mutableStateOf("")

    val loadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)

    override suspend fun loadPage(pageIndex: Int): List<GalleryUiState> {
        val pageData = ArrayList<GalleryUiState>()
        try {
            val result = doujinshiRepository.getCollectionPage(pageIndex)
            val resultList = result.doujinshiList.map {
                GalleryUiState.DoujinshiItem(it)
            }
            if (resultList.isNotEmpty()) {
                pageData.add(GalleryUiState.Title("Page ${pageIndex + 1}"))
                pageData.addAll(resultList)

                collectionCountLabel.value =
                    decimalFormat.format(doujinshiRepository.getCollectionSize())
            }
            finishLoading(pageIndex)
        } catch (error: Throwable) {
            finishLoading(pageIndex)
            if (error is GalleryPageNotExistException) {
                throw error
            }
        }
        return pageData
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