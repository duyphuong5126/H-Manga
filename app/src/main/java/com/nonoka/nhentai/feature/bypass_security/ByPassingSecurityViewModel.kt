package com.nonoka.nhentai.feature.bypass_security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ByPassingSecurityViewModel : ViewModel() {
    private val _byPassingResult = MutableStateFlow(ByPassingResult.Loading)
    val byPassingResult: StateFlow<ByPassingResult> = _byPassingResult

    fun validateData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch {
            _byPassingResult.emit(ByPassingResult.Loading)
        }
    }

    fun onError(error: String) {
        Timber.e("Bypassing error $error")
        viewModelScope.launch {
            _byPassingResult.emit(ByPassingResult.Failure)
        }
    }
}