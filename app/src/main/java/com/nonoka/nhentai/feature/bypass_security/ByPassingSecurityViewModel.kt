package com.nonoka.nhentai.feature.bypass_security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nonoka.nhentai.di.qualifier.IODispatcher
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
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

    fun validateData(data: String) {
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
        _byPassingResult.tryEmit(ByPassingResult.Loading)
    }

    fun onError(error: String) {
        Timber.e("Bypassing error $error")
        _byPassingResult.tryEmit(ByPassingResult.Failure)
    }
}