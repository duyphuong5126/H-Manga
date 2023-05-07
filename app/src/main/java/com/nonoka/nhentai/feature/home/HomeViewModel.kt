package com.nonoka.nhentai.feature.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.book.SortOption
import com.nonoka.nhentai.paging.PagingDataLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository
) : ViewModel(), PagingDataLoader<GalleryUiState> {
    val filters = mutableStateListOf<String>()
    val galleryCountLabel = mutableStateOf("")

    val sortOption = mutableStateOf(SortOption.Recent)

    private val decimalFormat = DecimalFormat("#,###")

    fun addFilter(filter: String) {
        if (filter.isNotBlank()) {
            filters.add(filter)
        }
    }

    fun selectSortOption(sortOption: SortOption) {
        this.sortOption.value = sortOption
    }

    override suspend fun loadPage(pageIndex: Int): List<GalleryUiState> {
        Timber.d("Loading page $pageIndex")
        val pageData = ArrayList<GalleryUiState>()

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
        return pageData
    }
}