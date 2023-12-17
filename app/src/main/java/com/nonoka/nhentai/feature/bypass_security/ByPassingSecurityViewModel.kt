package com.nonoka.nhentai.feature.bypass_security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class ByPassingSecurityViewModel @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _byPassingResult = MutableStateFlow(ByPassingResult.Loading)
    val byPassingResult: StateFlow<ByPassingResult> = _byPassingResult

    private val pendingJobs = arrayListOf<Job>()

    override fun onCleared() {
        super.onCleared()
        for (i in pendingJobs.indices) {
            pendingJobs.removeAt(i).cancel()
        }
    }

    fun validateData(data: String) {
        cancelPendingJobs()
        viewModelScope.launch(ioDispatcher) {
            _byPassingResult.emit(ByPassingResult.Processing)
            try {
                val doujinshis = Gson().fromJson(data, DoujinshisResult::class.java)
                Timber.d("Doujinshis=${doujinshis.doujinshiList.size}")
                _byPassingResult.emit(ByPassingResult.Success)
            } catch (error: Throwable) {
                Timber.e("Unable to parse data with error $error")
                _byPassingResult.emit(ByPassingResult.Failure)
            }
        }
    }

    fun onRetry() {
        cancelPendingJobs()
        _byPassingResult.tryEmit(ByPassingResult.Loading)
    }

    fun onError(error: String) {
        cancelPendingJobs()
        Timber.e("Bypassing error $error")
        if (error.contains("403")) {
            pendingJobs.add(viewModelScope.launch {
                delay(10000)
                _byPassingResult.tryEmit(ByPassingResult.Failure)
            })
        } else {
            _byPassingResult.tryEmit(ByPassingResult.Failure)
        }
    }

    private fun cancelPendingJobs() {
        for (i in pendingJobs.indices) {
            pendingJobs.removeAt(i).cancel()
        }
    }
}