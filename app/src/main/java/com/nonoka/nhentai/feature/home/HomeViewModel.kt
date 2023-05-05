package com.nonoka.nhentai.feature.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.paging.PagingDataLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository
) : ViewModel(), PagingDataLoader<GalleryUiState> {
    val filters = mutableStateListOf<String>()
    fun addFilter(filter: String) {
        filters.add(filter)
    }

    override suspend fun loadPage(pageIndex: Int): List<GalleryUiState> {
        Timber.d("Loading page $pageIndex")
        val result = ArrayList<GalleryUiState>()
        result.add(GalleryUiState.Title("Page ${pageIndex + 1}"))
        result.addAll(doujinshiRepository.getGalleryPage(pageIndex).doujinshiList.map {
            GalleryUiState.DoujinshiItem(it)
        })
        return result
    }
}