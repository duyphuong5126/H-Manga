package com.nonoka.nhentai.feature.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.FilterRepository
import com.nonoka.nhentai.domain.entity.GalleryPageNotExistException
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption
import com.nonoka.nhentai.paging.PagingDataLoader
import com.nonoka.nhentai.paging.PagingDataSource
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
    private val filterRepository: FilterRepository,
) : ViewModel(), PagingDataLoader<GalleryUiState> {
    val lazyDoujinshisFlow = Pager(
        PagingConfig(
            pageSize = 25,
            prefetchDistance = 5,
            initialLoadSize = 25,
        )
    ) {
        PagingDataSource(this)
    }.flow.cachedIn(viewModelScope)

    val filters = mutableStateListOf<String>()
    val filterHistory = mutableStateListOf<String>()
    val galleryCountLabel = mutableStateOf("")

    val sortOption = mutableStateOf(SortOption.Recent)

    private val decimalFormat = DecimalFormat("#,###")

    val loadingState = mutableStateOf<LoadingUiState>(LoadingUiState.Idle)

    var searchTerm = mutableStateOf("")

    var reset = mutableStateOf(false)

    private val filterInitialized = AtomicBoolean(false)

    fun addFilter(filter: String) {
        if (filter.isNotBlank()) {
            val normalizedFilter = filter.trim().lowercase()
            if (!filters.contains(normalizedFilter)) {
                filters.add(normalizedFilter)
                viewModelScope.launch(Dispatchers.IO) {
                    filterRepository.activateFilter(normalizedFilter)
                }
            }
            if (!filterHistory.contains(normalizedFilter)) {
                filterHistory.add(normalizedFilter)
            }
        }
    }

    fun removeFilter(filter: String) {
        val normalizedFilter = filter.trim().lowercase()
        filters.remove(normalizedFilter)
        viewModelScope.launch(Dispatchers.IO) {
            filterRepository.deactivateFilter(normalizedFilter)
        }
        if (filters.isEmpty()) {
            sortOption.value = SortOption.Recent
        }
    }

    fun clearFilters() {
        filters.clear()
        sortOption.value = SortOption.Recent
    }

    fun selectSortOption(sortOption: SortOption) {
        this.sortOption.value = sortOption
    }

    override suspend fun loadPage(pageIndex: Int): List<GalleryUiState> {
        Timber.d("Gallery>>> Loading page $pageIndex")
        if (pageIndex == 0 && !filterInitialized.get()) {
            withContext(Dispatchers.IO) {
                try {
                    filters.clear()
                    filterHistory.clear()
                    filters.addAll(filterRepository.getActiveFilters())
                    filterHistory.addAll(filterRepository.getAllFilters())
                    filterInitialized.compareAndSet(false, true)
                } catch (error: Throwable) {
                    Timber.d("Gallery>>> failed to init filters with error $error")
                }
            }
        }
        val pageData = ArrayList<GalleryUiState>()
        try {
            val result = doujinshiRepository.getGalleryPage(pageIndex, filters, sortOption.value)
            val resultList = result.doujinshiList.map {
                GalleryUiState.DoujinshiItem(it)
            }
            if (resultList.isNotEmpty()) {
                pageData.add(GalleryUiState.Title("Page ${pageIndex + 1}"))
                pageData.addAll(resultList)

                galleryCountLabel.value =
                    "Result: ${decimalFormat.format(result.numOfPages * result.numOfBooksPerPage)} doujinshis"
            }
            finishLoading(pageIndex)
        } catch (error: Throwable) {
            Timber.d("Gallery>>> load error $error")
            finishLoading(pageIndex)
            if (error is GalleryPageNotExistException) {
                pageData.add(GalleryUiState.Title("Page ${pageIndex + 1} - Not exist"))
            } else {
                throw error
            }
        }
        return pageData
    }

    private fun finishLoading(pageIndex: Int) {
        if (pageIndex > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                delay(500)
                loadingState.value = LoadingUiState.Idle
            }
        } else {
            loadingState.value = LoadingUiState.Idle
        }
    }
}