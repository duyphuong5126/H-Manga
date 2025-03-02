package com.nonoka.nhentai.feature

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.domain.DoujinshiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DoujinshiStatusViewModel @Inject constructor(
    private val doujinshiRepository: DoujinshiRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val favoriteIds = mutableStateOf<List<String>>(emptyList())
    val readIds = mutableStateOf<List<String>>(emptyList())
    val downloadedIds = mutableStateOf<List<String>>(emptyList())

    fun reload() {
        Timber.d("reload")
        viewModelScope.launch(ioDispatcher) {
            loadFavoriteIds()
            loadReadIds()
            loadDownloadedIds()
        }
    }

    private suspend fun loadFavoriteIds() {
        try {
            val favIds = doujinshiRepository.getFavoriteDoujinshis()
            favoriteIds.value = favIds
            Timber.d("favoriteIds=$favIds")
        } catch (error: Throwable) {
            Timber.e("Failed to get favorite IDs with error: $error")
        }
    }

    private suspend fun loadReadIds() {
        try {
            val reads = doujinshiRepository.getReadDoujinshis()
            readIds.value = reads
            Timber.d("readIds=$reads")
        } catch (error: Throwable) {
            Timber.e("Failed to get read IDs with error: $error")
        }
    }

    private suspend fun loadDownloadedIds() {
        try {
            val downloadeds = doujinshiRepository.getDownloadedDoujinshis()
            downloadedIds.value = downloadeds
            Timber.d("downloadedIds=$downloadeds")
        } catch (error: Throwable) {
            Timber.e("Failed to get downloaded IDs with error: $error")
        }
    }
}